// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.plugins.access;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.plugins.datagenerator.config.module.DataPluginConfiguration;
import com.atlassian.jira.plugins.datagenerator.config.module.CommentParameters;
import io.atlassian.fugue.Option;
import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Either;
import com.atlassian.ozymandias.error.ModuleAccessError;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.plugin.Plugin;
import java.util.Iterator;
import com.google.common.collect.SetMultimap;
import java.util.List;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.HashMultimap;
import org.apache.commons.lang.StringUtils;
import java.util.Collections;
import com.atlassian.plugin.PluginState;
import com.atlassian.jira.plugins.datagenerator.descriptor.DataFactoryModuleDescriptor;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.google.common.base.Preconditions;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.ozymandias.SafeAccessViaPluginAccessor;
import com.atlassian.jira.project.Project;
import java.util.Collection;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class DataPluginConfigurationAccessor
{
    private static final Logger log;
    private static final Predicate<Collection<Project>> PROJECT_FILTER;
    private final SafeAccessViaPluginAccessor safeAccessor;
    private final PluginAccessor pluginAccessor;
    
    @Autowired
    public DataPluginConfigurationAccessor(final PluginAccessor pluginAccessor) {
        Preconditions.checkArgument(pluginAccessor != null, (Object)"pluginAccessor is required");
        this.safeAccessor = SafePluginPointAccess.to(pluginAccessor);
        this.pluginAccessor = pluginAccessor;
    }
    
    public Map<Project, Collection<String>> getProjectsWithPlugins() {
        final List<Map<String, Collection<Project>>> results = this.safeAccessor.forType(DataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> {
            final String pluginKey = module.getPluginKey();
            final Plugin plugin = this.pluginAccessor.getPlugin(pluginKey);
            final boolean pluginEnabled = plugin != null && plugin.getPluginState().equals((Object)PluginState.ENABLED);
            if (!pluginEnabled) {
                return Collections.emptyMap();
            }
            else {
                final String key2 = moduleDescriptor.getCompleteKey();
                if (StringUtils.isBlank(key2)) {
                    DataPluginConfigurationAccessor.log.warn("There is no complete key for a module");
                    return Collections.emptyMap();
                }
                else {
                    final Collection<Project> projects = module.getProjects();
                    if (projects == null) {
                        return Collections.emptyMap();
                    }
                    else {
                        return (Map<Object, Object>)Collections.singletonMap(key2, projects);
                    }
                }
            }
        });
        final SetMultimap<Project, String> multimap = (SetMultimap<Project, String>)HashMultimap.create();
        for (final Map<String, Collection<Project>> value : results) {
            for (final Map.Entry<String, Collection<Project>> entry : Maps.filterValues((Map)value, (Predicate)DataPluginConfigurationAccessor.PROJECT_FILTER).entrySet()) {
                final String key = entry.getKey();
                for (final Project project : entry.getValue()) {
                    multimap.put((Object)project, (Object)key);
                }
            }
        }
        return (Map<Project, Collection<String>>)ImmutableMap.copyOf(multimap.asMap());
    }
    
    public IssueCreationParameters getIssueCreationParameters(final String completeModuleKey, final Project project) {
        if (StringUtils.isBlank(completeModuleKey) || project == null) {
            DataPluginConfigurationAccessor.log.warn("Invalid parameters passed in to getIssueCreationParameters");
            return null;
        }
        final Either<? extends ModuleAccessError, IssueCreationParameters> result = this.safeAccessor.forKey(completeModuleKey, DataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> module.getIssueCreationParameters(project));
        if (result.isRight()) {
            return (IssueCreationParameters)result.right().get();
        }
        DataPluginConfigurationAccessor.log.warn("Error retrieving IssueCreationParameters for " + completeModuleKey + " : " + ((ModuleAccessError)result.left().get()).getErrorMessage());
        return IssueCreationParameters.EMPTY;
    }
    
    public void performIssueUpdates(final String completeModuleKey, final Project project, final Collection<Long> issueIds) {
        if (StringUtils.isBlank(completeModuleKey) || project == null || issueIds == null) {
            DataPluginConfigurationAccessor.log.warn("Invalid parameters passed in to performIssueUpdates");
            return;
        }
        final Collection<Long> immutableIssueIds = (Collection<Long>)ImmutableList.copyOf((Collection)issueIds);
        final Option<? extends ModuleAccessError> result = this.safeAccessor.forKey(completeModuleKey, DataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> module.performIssueUpdates(project, immutableIssueIds));
        if (result.isEmpty()) {
            return;
        }
        DataPluginConfigurationAccessor.log.warn("Error performing Issue Updates for " + completeModuleKey + " : " + ((ModuleAccessError)result.get()).getErrorMessage());
    }
    
    public void performPostReindexUpdates(final String completeModuleKey, final Project project) {
        if (StringUtils.isBlank(completeModuleKey) || project == null) {
            DataPluginConfigurationAccessor.log.warn("Invalid parameters passed in to performPostReindexUpdates");
            return;
        }
        final Option<? extends ModuleAccessError> result = this.safeAccessor.forKey(completeModuleKey, DataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> module.performPostReindexUpdates(project));
        if (result.isEmpty()) {
            return;
        }
        DataPluginConfigurationAccessor.log.warn("Error performing post reindex updates for " + completeModuleKey + " : " + ((ModuleAccessError)result.get()).getErrorMessage());
    }
    
    public CommentParameters getCommentAuthorAndProperties(final String completeModuleKey, final Project project, final Long issueId, final String issueAssignee, final String issueReporter) {
        if (StringUtils.isBlank(completeModuleKey) || project == null) {
            DataPluginConfigurationAccessor.log.warn("Invalid parameters passed in to getCommentAuthorAndProperties");
            return null;
        }
        final Either<? extends ModuleAccessError, CommentParameters> result = this.safeAccessor.forKey(completeModuleKey, DataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> module.getCommentAuthorAndProperties(project, issueId, issueAssignee, issueReporter));
        if (result.isRight()) {
            return (CommentParameters)result.right().get();
        }
        DataPluginConfigurationAccessor.log.warn("Error retrieving CommentParameters for " + completeModuleKey + " : " + ((ModuleAccessError)result.left().get()).getErrorMessage());
        return CommentParameters.EMPTY;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)DataPluginConfigurationAccessor.class);
        PROJECT_FILTER = (input -> input != null && !input.isEmpty());
    }
}
