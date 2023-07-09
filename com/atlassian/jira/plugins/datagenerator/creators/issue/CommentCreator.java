// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.issue;

import java.util.Collection;
import java.util.ArrayList;
import com.atlassian.jira.plugins.datagenerator.drivers.CommentDriver;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.generators.factory.ProjectConfigurationsFactory;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import org.slf4j.Logger;
import com.atlassian.jira.plugins.datagenerator.plugins.access.DataPluginConfigurationAccessor;
import org.springframework.stereotype.Component;

@Component
public class CommentCreator
{
    private final DataPluginConfigurationAccessor dataPluginConfigurationAccessor;
    private final Logger LOG;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    
    @Autowired
    public CommentCreator(final DataPluginConfigurationAccessor dataPluginConfigurationAccessor, final JiraSequenceIdGenerator sequenceIdGenerator) {
        this.LOG = LoggerFactory.getLogger((Class)CommentCreator.class);
        this.dataPluginConfigurationAccessor = dataPluginConfigurationAccessor;
        this.sequenceIdGenerator = sequenceIdGenerator;
    }
    
    public void create(final List<IssueInfo> issues, final ProjectConfigurationsFactory.ProjectConfiguration projectConfiguration, final GeneratorContext context) {
        new RetryFunction<Void>("create comments").execute(() -> {
            final EntityManager entityManager = new EntityManager(this.sequenceIdGenerator, context.generatorConfiguration);
            final CommentDriver commentDriver = new CommentDriver(context, entityManager, this.dataPluginConfigurationAccessor);
            commentDriver.generate(issues, ProjectConfigurationsFactory.ProjectConfigurations.map(new ArrayList<ProjectConfigurationsFactory.ProjectConfiguration>() {
                final /* synthetic */ ProjectConfigurationsFactory.ProjectConfiguration val$projectConfiguration;
                
                {
                    this.add(this.val$projectConfiguration);
                }
            }));
            entityManager.shutdown();
            return null;
        });
    }
    
    public void create(final List<IssueInfo> issues, final Collection<ProjectConfigurationsFactory.ProjectConfiguration> projectConfigurations, final GeneratorContext context) {
        new RetryFunction<Void>("create comments").execute(() -> {
            EntityManager entityManager = null;
            try {
                entityManager = new EntityManager(this.sequenceIdGenerator, context.generatorConfiguration);
                final CommentDriver commentDriver = new CommentDriver(context, entityManager, this.dataPluginConfigurationAccessor);
                commentDriver.generate(issues, ProjectConfigurationsFactory.ProjectConfigurations.map(projectConfigurations));
                return;
            }
            finally {
                if (entityManager != null) {
                    try {
                        entityManager.shutdown();
                    }
                    catch (final Exception e) {
                        this.LOG.warn("Could not shut down enitity manager", (Throwable)e);
                    }
                }
            }
        });
    }
}
