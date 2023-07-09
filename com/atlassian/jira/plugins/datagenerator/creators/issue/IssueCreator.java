// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.issue;

import java.util.function.Function;
import java.util.Iterator;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.drivers.CreateIssuesDriver;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.generators.factory.ProjectConfigurationsFactory;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.plugins.datagenerator.creators.listeners.CreationEmitter;
import org.slf4j.Logger;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import com.atlassian.jira.plugins.datagenerator.fields.FieldMutators;
import com.atlassian.jira.plugins.datagenerator.drivers.CreateIssuesDriverFactory;
import com.atlassian.jira.plugins.datagenerator.generators.factory.IssueCreationParametersFactory;
import org.springframework.stereotype.Component;
import com.atlassian.jira.plugins.datagenerator.creators.CreatedEvent;

@Component
public class IssueCreator implements CreatedEvent<IssueCreator>
{
    private final IssueCreationParametersFactory issueCreationParametersFactory;
    private final CreateIssuesDriverFactory createIssuesDriverFactory;
    private final FieldMutators fieldMutators;
    private final CommentCreator commentCreator;
    private final CustomFieldCreator customFieldCreator;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    private final Logger LOG;
    private final CreationEmitter emitter;
    private boolean withComments;
    private boolean withCustomFields;
    
    @Autowired
    public IssueCreator(final IssueCreationParametersFactory issueCreationParametersFactory, final CreateIssuesDriverFactory createIssuesDriverFactory, final FieldMutators fieldMutators, final CommentCreator commentCreator, final CustomFieldCreator customFieldCreator, final JiraSequenceIdGenerator sequenceIdGenerator) {
        this.LOG = LoggerFactory.getLogger((Class)IssueCreator.class);
        this.emitter = new CreationEmitter();
        this.withComments = false;
        this.withCustomFields = false;
        this.issueCreationParametersFactory = issueCreationParametersFactory;
        this.createIssuesDriverFactory = createIssuesDriverFactory;
        this.fieldMutators = fieldMutators;
        this.commentCreator = commentCreator;
        this.customFieldCreator = customFieldCreator;
        this.sequenceIdGenerator = sequenceIdGenerator;
    }
    
    private List<Integer> batches(final int issues, final int maxBatchSize) {
        final int batches = issues / maxBatchSize;
        final int rest = issues % maxBatchSize;
        final List<Integer> issuesBatches = Lists.newArrayListWithExpectedSize(batches + ((rest != 0) ? 1 : 0));
        for (int i = 0; i < batches; ++i) {
            issuesBatches.add(maxBatchSize);
        }
        if (rest != 0) {
            issuesBatches.add(rest);
        }
        return issuesBatches;
    }
    
    private List<IssueInfo> createIssues(final int issues, final ProjectConfigurationsFactory.ProjectConfiguration projectConfiguration, final GeneratorContext context) {
        return new RetryFunction<List>("create issues").execute(() -> {
            EntityManager entityManager = null;
            try {
                entityManager = new EntityManager(this.sequenceIdGenerator, context.generatorConfiguration);
                final IssueCreationParameters issueCreationParameters = this.issueCreationParametersFactory.build(projectConfiguration);
                this.fieldMutators.init(context, issueCreationParameters);
                final CreateIssuesDriver createIssuesDriver = this.createIssuesDriverFactory.create(context, entityManager);
                final List<IssueInfo> generated = createIssuesDriver.generate(projectConfiguration.getProject(), issueCreationParameters, issues);
                if (this.withComments) {
                    this.commentCreator.create(generated, projectConfiguration, context);
                }
                if (this.withCustomFields) {
                    this.customFieldCreator.create(generated, projectConfiguration, context);
                }
                this.emitter.emit(issues);
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
    
    public List<IssueInfo> create(final int issues, final ProjectConfigurationsFactory.ProjectConfiguration projectConfiguration, final GeneratorContext context) {
        final List<Integer> batches = this.batches(issues, 1000);
        final List<IssueInfo> created = Lists.newArrayListWithExpectedSize(issues);
        for (final int batch : batches) {
            created.addAll(this.createIssues(batch, projectConfiguration, context));
        }
        return created;
    }
    
    public List<IssueInfo> create(final int issues, final Collection<ProjectConfigurationsFactory.ProjectConfiguration> projectConfigurations, final GeneratorContext context) {
        final List<IssueInfo> generated = Lists.newArrayListWithCapacity(issues * projectConfigurations.size());
        projectConfigurations.forEach(projectConfiguration -> generated.addAll(this.create(issues, projectConfiguration, context)));
        return generated;
    }
    
    public IssueCreator withComments() {
        this.withComments = true;
        return this;
    }
    
    public IssueCreator withoutComments() {
        this.withComments = false;
        return this;
    }
    
    public IssueCreator withCustomFields() {
        this.withCustomFields = true;
        return this;
    }
    
    public IssueCreator withoutCustomFields() {
        this.withCustomFields = false;
        return this;
    }
    
    @Override
    public IssueCreator onCreated(final Function<Integer, ?> lambda) {
        this.emitter.register(lambda);
        return this;
    }
}
