// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.fields;

import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.Maps;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FieldMutators
{
    private final Map<String, FieldMutator> fieldMutators;
    private GeneratorContext generatorContext;
    private IssueCreationParameters issueCreationParameters;
    
    @Autowired
    public FieldMutators(final List<FieldMutator> fieldMutators) {
        this.fieldMutators = (Map<String, FieldMutator>)Maps.uniqueIndex((Iterable)fieldMutators, input -> input.fieldType());
    }
    
    public void init(final GeneratorContext generatorContext, final IssueCreationParameters issueCreationParameters) {
        this.generatorContext = generatorContext;
        this.issueCreationParameters = issueCreationParameters;
        for (final FieldMutator fieldMutator : this.fieldMutators.values()) {
            fieldMutator.init(generatorContext, issueCreationParameters);
        }
    }
    
    public void handle(final String field, final Map<Object, Object> sink) {
        final FieldMutator mutator = this.fieldMutators.get(field);
        if (mutator != null) {
            mutator.handle(sink);
        }
    }
    
    public ChangeItemBean handle(final String field, final Entity issue, final Timestamp currentNow) {
        final FieldMutator mutator = this.fieldMutators.get(field);
        return (mutator != null) ? mutator.handle(issue, currentNow) : null;
    }
    
    public GeneratorContext getGeneratorContext() {
        return this.generatorContext;
    }
    
    public IssueCreationParameters getIssueCreationParameters() {
        return this.issueCreationParameters;
    }
}
