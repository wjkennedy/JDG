// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.plugins.datagenerator.generators.project.ProjectCreationException;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.plugins.datagenerator.generators.project.EmptyProjectCreator;
import com.atlassian.jira.plugins.datagenerator.generators.project.SharedConfigProjectCreator;
import com.atlassian.jira.plugins.datagenerator.generators.project.TemplateProjectCreator;
import com.atlassian.jira.plugins.datagenerator.generators.project.ProjectCreator;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import com.google.common.base.Function;
import com.atlassian.jira.user.ApplicationUser;
import java.util.Collection;
import org.apache.commons.lang.time.StopWatch;
import com.google.common.collect.Lists;
import java.util.Collections;
import com.atlassian.jira.project.Project;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.io.InputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.plugins.datagenerator.ProjectUpdater;
import java.util.Properties;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.bc.project.ProjectService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ProjectGenerator
{
    private static final Logger LOG;
    private static final int MAX_RETRIES = 20;
    private static final String SHARED_CONFIG_OPTION = "SharedConfig";
    private final ProjectService projectService;
    protected final ProjectManager projectManager;
    private static final Properties PROJECTS;
    private final ProjectUpdater projectUpdater;
    private final UserUtils userUtils;
    
    @Autowired
    public ProjectGenerator(@ComponentImport final ProjectService projectService, @ComponentImport final ProjectManager projectManager, final ProjectUpdater projectUpdater, final UserUtils userUtils) {
        this.projectService = projectService;
        this.projectManager = projectManager;
        this.projectUpdater = projectUpdater;
        this.userUtils = userUtils;
    }
    
    private static Properties loadProjectsFromPropertiesFile() {
        final Properties projectProperties = new Properties();
        try {
            final InputStream source = ProjectGenerator.class.getClassLoader().getResourceAsStream("com/atlassian/jira/plugins/datagenerator/projects/project.properties");
            try {
                projectProperties.load(source);
                return projectProperties;
            }
            finally {
                source.close();
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<Project> generate(final GeneratorContext context) {
        final int projectCount = context.generatorConfiguration.projectCount;
        final String projectTemplate = context.generatorConfiguration.projectTemplate;
        if (projectCount <= 0) {
            context.messages.add("No project generation requested, skipping.");
            context.createdProjects = Collections.emptyList();
            return Collections.emptyList();
        }
        final List<Project> generatedProjects = Lists.newArrayListWithCapacity(projectCount);
        context.createdProjects = generatedProjects;
        context.resetProgress(String.format("Generating %d projects", projectCount), projectCount);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        this.generate(context, projectCount, projectTemplate, generatedProjects);
        stopWatch.stop();
        final String msg = String.format("Created %d projects in %s ", projectCount, stopWatch.toString());
        ProjectGenerator.LOG.info(msg);
        context.messages.add(msg);
        return generatedProjects;
    }
    
    private void generate(final GeneratorContext context, final int projectCount, final String projectTemplate, final List<Project> generatedProjects) {
        ProjectGenerator.LOG.info("Creating {} projects", (Object)projectCount);
        final ApplicationUser user = this.userUtils.getLoggedInUser();
        final Function<Map<String, String>, List<Project>> createProjectsFunction = (Function<Map<String, String>, List<Project>>)(input -> {
            if (input == null) {
                return Collections.emptyList();
            }
            final ProjectCreator projectCreator = this.getProjectCreator(this.getProjectCreationMode(projectTemplate), user, projectTemplate);
            final List<Project> createdProjects = Lists.newArrayListWithCapacity(input.size());
            for (final Map.Entry<String, String> entry : input.entrySet()) {
                final String key = entry.getKey();
                final String projectName = entry.getValue();
                try {
                    createdProjects.add(projectCreator.createProject(projectName, key));
                    ProjectGenerator.LOG.info("Created a project key=" + entry.getKey());
                }
                catch (final ProjectCreationException e) {
                    reportErrors(context, key, e.getErrorCollection());
                }
                context.incProgress();
            }
            return createdProjects;
        });
        List<Project> projects;
        for (int projectsToCreate = projectCount, retries = 0; projectsToCreate > 0 && retries++ < 20; projectsToCreate -= projects.size()) {
            projects = (List)createProjectsFunction.apply((Object)getRandomProjects(projectsToCreate));
            generatedProjects.addAll(projects);
        }
    }
    
    private ProjectCreationMode getProjectCreationMode(final String projectTemplate) {
        if ("SharedConfig".equalsIgnoreCase(projectTemplate)) {
            return ProjectCreationMode.SHARED_CONFIG;
        }
        if (StringUtils.isNotEmpty((CharSequence)projectTemplate) && this.isCreatingFromTemplatesAvailable()) {
            return ProjectCreationMode.TEMPLATE;
        }
        return ProjectCreationMode.EMPTY;
    }
    
    private ProjectCreator getProjectCreator(final ProjectCreationMode mode, final ApplicationUser user, final String projectTemplate) {
        switch (mode) {
            case TEMPLATE: {
                return new TemplateProjectCreator(this.projectService, projectTemplate, user);
            }
            case SHARED_CONFIG: {
                return new SharedConfigProjectCreator(this.projectService, user);
            }
            default: {
                return new EmptyProjectCreator(this.projectService, user, this.projectUpdater);
            }
        }
    }
    
    private static void reportErrors(final GeneratorContext context, final String key, final ErrorCollection errorCollection) {
        context.messages.add(String.format("Error creating random project %s, see log for details", key));
        ProjectGenerator.LOG.error(String.format("Error creating random project %s.", key));
        for (final Object errMsg : errorCollection.getErrorMessages()) {
            ProjectGenerator.LOG.error("    " + errMsg);
        }
        final Map<String, String> errorsSet = errorCollection.getErrors();
        for (final Map.Entry<String, String> err : errorsSet.entrySet()) {
            ProjectGenerator.LOG.error("    " + err.getKey() + ": " + err.getValue());
        }
    }
    
    private static Map<String, String> getRandomProjects(final int projectCount) {
        final Map<String, String> randomProjects = new HashMap<String, String>(projectCount);
        final List<String> randomProjectKeys = randomKeys(projectCount);
        for (final String key : randomProjectKeys) {
            randomProjects.put(key, ProjectGenerator.PROJECTS.getProperty(key));
        }
        return randomProjects;
    }
    
    private static List<String> randomKeys(final int projectCount) {
        final List<String> allKeys = new ArrayList<String>((Collection<? extends String>)ProjectGenerator.PROJECTS.keySet());
        return Randomizer.randomItems(projectCount, allKeys);
    }
    
    public boolean isCreatingFromTemplatesAvailable() {
        try {
            this.getClass().getClassLoader().loadClass("com.atlassian.jira.projecttemplates.service.ProjectTemplateService");
            return true;
        }
        catch (final ClassNotFoundException e) {
            ProjectGenerator.LOG.info("ProjectTemplateService is unavailable - creating projects from templates turned off");
            return false;
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)ProjectGenerator.class);
        PROJECTS = loadProjectsFromPropertiesFile();
    }
    
    private enum ProjectCreationMode
    {
        EMPTY, 
        SHARED_CONFIG, 
        TEMPLATE;
    }
}
