// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.util.PhasedTimer;
import org.apache.commons.lang.time.StopWatch;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.plugins.datagenerator.drivers.CreateComponentsDriver;
import com.atlassian.jira.project.ProjectManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ComponentGenerator implements DataGenerator
{
    private static final Logger LOG;
    protected final ProjectManager projectManager;
    private final CreateComponentsDriver createComponentsDriver;
    private GeneratorConfiguration generatorConfiguration;
    
    @Autowired
    public ComponentGenerator(@ComponentImport final ProjectManager projectManager, final CreateComponentsDriver createComponentsDriver) {
        this.projectManager = projectManager;
        this.createComponentsDriver = createComponentsDriver;
    }
    
    @Override
    public void generate(final GeneratorContext context) throws GenericEntityException, SQLException {
        this.generatorConfiguration = context.generatorConfiguration;
        final List<Project> projects = GeneratorConfigurationUtil.getProjects(this.generatorConfiguration, this.projectManager);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int totalComponentCount = this.generatorConfiguration.componentsCount;
        final int componentsPerProject = projects.isEmpty() ? 0 : (totalComponentCount / projects.size());
        final Iterator<Project> projectIterator = projects.iterator();
        final PhasedTimer phasedTimer = new PhasedTimer();
        context.resetProgress(String.format("Generating %d components", totalComponentCount), totalComponentCount);
        int createdComponents = 0;
        while (projectIterator.hasNext()) {
            final Project project = projectIterator.next();
            final int componentCount = projectIterator.hasNext() ? componentsPerProject : (totalComponentCount - createdComponents);
            phasedTimer.startPhase("generating components:");
            this.createComponentsDriver.generate(context, project, componentCount);
            createdComponents += componentCount;
            phasedTimer.stop();
        }
        stopWatch.stop();
        final String message = String.format("Created %d components in %s", this.generatorConfiguration.componentsCount, stopWatch.toString());
        context.messages.add(message);
        context.messages.addAll(phasedTimer.toStrings());
        ComponentGenerator.LOG.info(message);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)ComponentGenerator.class);
    }
}
