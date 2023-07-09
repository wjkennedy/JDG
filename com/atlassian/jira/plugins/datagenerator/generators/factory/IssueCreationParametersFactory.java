// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.factory;

import java.util.Set;
import com.atlassian.jira.issue.fields.CustomField;
import java.util.List;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import com.google.common.collect.Sets;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.plugins.access.DataPluginConfigurationAccessor;
import org.springframework.stereotype.Component;

@Component
public class IssueCreationParametersFactory
{
    private final DataPluginConfigurationAccessor dataPluginConfigurationAccessor;
    
    @Autowired
    public IssueCreationParametersFactory(final DataPluginConfigurationAccessor dataPluginConfigurationAccessor) {
        this.dataPluginConfigurationAccessor = dataPluginConfigurationAccessor;
    }
    
    public IssueCreationParameters build(final ProjectConfigurationsFactory.ProjectConfiguration configuration) {
        final Set<String> assignees = Sets.newHashSet();
        final Set<String> reporters = Sets.newHashSet();
        final Set<CustomField> customFields = Sets.newHashSet();
        configuration.getPluginKeys().stream().forEach(pluginKey -> {
            final IssueCreationParameters parameters = this.dataPluginConfigurationAccessor.getIssueCreationParameters(pluginKey, configuration.getProject());
            assignees.addAll(parameters.getAssigneeUsernames());
            reporters.addAll(parameters.getReporterUsernames());
            customFields.addAll(parameters.getCustomFieldsToPopulate());
            return;
        });
        return new IssueCreationParameters((List<String>)ImmutableList.copyOf((Collection)assignees), (List<String>)ImmutableList.copyOf((Collection)reporters), (List<CustomField>)ImmutableList.copyOf((Collection)customFields));
    }
}
