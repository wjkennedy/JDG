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
import com.atlassian.jira.plugins.datagenerator.drivers.CreateVersionsDriver;
import com.atlassian.jira.project.ProjectManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class VersionGenerator implements DataGenerator
{
    private static final Logger LOG;
    protected final ProjectManager projectManager;
    private final CreateVersionsDriver createVersionsDriver;
    private GeneratorConfiguration generatorConfiguration;
    
    @Autowired
    public VersionGenerator(@ComponentImport final ProjectManager projectManager, final CreateVersionsDriver createVersionsDriver) {
        this.projectManager = projectManager;
        this.createVersionsDriver = createVersionsDriver;
    }
    
    @Override
    public void generate(final GeneratorContext context) throws GenericEntityException, SQLException {
        this.generatorConfiguration = context.generatorConfiguration;
        final List<Project> projects = GeneratorConfigurationUtil.getProjects(this.generatorConfiguration, this.projectManager);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int totalVersionCount = this.generatorConfiguration.versionsCount;
        final int versionsPerProject = projects.isEmpty() ? 0 : (totalVersionCount / projects.size());
        final Iterator<Project> projectIterator = projects.iterator();
        final PhasedTimer phasedTimer = new PhasedTimer();
        context.resetProgress(String.format("Generating %d versions", totalVersionCount), totalVersionCount);
        int createdVersions = 0;
        while (projectIterator.hasNext()) {
            final Project project = projectIterator.next();
            final int versionCount = projectIterator.hasNext() ? versionsPerProject : (totalVersionCount - createdVersions);
            phasedTimer.startPhase("generating versions:");
            this.createVersionsDriver.generate(context, project, versionCount);
            createdVersions += versionCount;
            phasedTimer.stop();
        }
        stopWatch.stop();
        final String message = String.format("Created %d versions in %s", this.generatorConfiguration.versionsCount, stopWatch.toString());
        context.messages.add(message);
        context.messages.addAll(phasedTimer.toStrings());
        VersionGenerator.LOG.info(message);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)VersionGenerator.class);
    }
}
