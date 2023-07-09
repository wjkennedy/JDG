// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.issue;

import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.plugins.datagenerator.drivers.CustomFieldDriver;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.generators.factory.ProjectConfigurationsFactory;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import org.slf4j.Logger;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.factory.IssueCreationParametersFactory;
import com.atlassian.jira.plugins.datagenerator.fields.FieldLayoutHelper;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.plugins.datagenerator.generators.CustomFieldGenerator;
import org.springframework.stereotype.Component;

@Component
public class CustomFieldCreator
{
    private final CustomFieldGenerator customFieldGenerator;
    private final CustomFieldManager customFieldManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldLayoutHelper fieldLayoutHelper;
    private final IssueCreationParametersFactory issueCreationParametersFactory;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    private final Logger LOG;
    
    @Autowired
    public CustomFieldCreator(final CustomFieldGenerator customFieldGenerator, final CustomFieldManager customFieldManager, final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, final FieldLayoutManager fieldLayoutManager, final IssueCreationParametersFactory issueCreationParametersFactory, final JiraSequenceIdGenerator sequenceIdGenerator) {
        this.LOG = LoggerFactory.getLogger((Class)CustomFieldCreator.class);
        this.customFieldGenerator = customFieldGenerator;
        this.customFieldManager = customFieldManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.issueCreationParametersFactory = issueCreationParametersFactory;
        this.fieldLayoutHelper = new FieldLayoutHelper(fieldLayoutManager);
        this.sequenceIdGenerator = sequenceIdGenerator;
    }
    
    public void create(final List<IssueInfo> issues, final ProjectConfigurationsFactory.ProjectConfiguration projectConfiguration, final GeneratorContext context) {
        new RetryFunction<Void>("create custom fields").execute(() -> {
            EntityManager entityManager = null;
            try {
                entityManager = new EntityManager(this.sequenceIdGenerator, context.generatorConfiguration);
                final IssueCreationParameters issueCreationParameters = this.issueCreationParametersFactory.build(projectConfiguration);
                new CustomFieldDriver(context, entityManager, this.customFieldGenerator, this.customFieldManager, this.issueTypeScreenSchemeManager, this.fieldLayoutHelper).generate(projectConfiguration.getProject(), issues, issueCreationParameters);
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
