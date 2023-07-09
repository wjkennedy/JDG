// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import org.slf4j.LoggerFactory;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import com.atlassian.jira.plugins.datagenerator.config.module.CommentParameters;
import java.util.Iterator;
import com.atlassian.jira.plugins.datagenerator.timestamp.TimestampGenerator;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import com.atlassian.jira.util.json.JSONObject;
import java.util.Collections;
import java.util.Set;
import java.sql.Timestamp;
import com.google.common.collect.Lists;
import com.atlassian.jira.plugins.datagenerator.config.module.CommentProperties;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.plugins.datagenerator.timestamp.FixedIntervalEndlessGenerator;
import com.google.common.collect.Maps;
import com.atlassian.jira.project.Project;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.plugins.access.DataPluginConfigurationAccessor;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import org.slf4j.Logger;

public class CommentDriver
{
    private static final Logger log;
    private final GeneratorConfiguration generatorConfiguration;
    private final List<String> allUsers;
    private final TextGenerator textGenerator;
    private final EntityManager entityManager;
    private final DataPluginConfigurationAccessor dataPluginConfigurationAccessor;
    
    public CommentDriver(final GeneratorContext context, final EntityManager entityManager, final DataPluginConfigurationAccessor dataPluginConfigurationAccessor) {
        this.entityManager = entityManager;
        this.generatorConfiguration = context.generatorConfiguration;
        this.allUsers = (List<String>)ImmutableList.copyOf((Collection)context.reporters.keySet());
        this.dataPluginConfigurationAccessor = dataPluginConfigurationAccessor;
        this.textGenerator = TextGenerator.getTextGenerator(this.generatorConfiguration, this.allUsers);
    }
    
    public void generate(final List<IssueInfo> forIssues, final Map<Project, Collection<String>> dataPluginPointProjects) throws SQLException, GenericEntityException {
        final Map<String, Set<CommentProperties>> potentialAuthors = Maps.newHashMap();
        final EntityHandler actionHandler = this.entityManager.getEntityHandler("Action");
        final EntityHandler entityPropertyHandler = this.entityManager.getEntityHandler("EntityProperty");
        for (int i = 0; i < forIssues.size(); ++i) {
            final IssueInfo issueInfo = forIssues.get(i);
            final TimestampGenerator timestampGenerator = new FixedIntervalEndlessGenerator(issueInfo.created, this.generatorConfiguration.issues.avgCommentInterval);
            int counter = Randomizer.randomLimitedGaussian(this.generatorConfiguration.issues.commentCount);
            String commentText;
            while (counter-- > 0 && (commentText = this.textGenerator.generateText()) != null) {
                potentialAuthors.clear();
                if (dataPluginPointProjects != null && dataPluginPointProjects.containsKey(issueInfo.getProjectObject())) {
                    for (final String completeKey : dataPluginPointProjects.get(issueInfo.getProjectObject())) {
                        final CommentParameters commentParameters = this.dataPluginConfigurationAccessor.getCommentAuthorAndProperties(completeKey, issueInfo.getProjectObject(), issueInfo.getId(), issueInfo.getAssignee(), issueInfo.getReporter());
                        final String username = commentParameters.getAuthor();
                        if (StringUtils.isNotBlank(username)) {
                            final Set<CommentProperties> commentPropertiesSet = Sets.newHashSet((Iterable)commentParameters.getCommentProperties());
                            if (potentialAuthors.containsKey(username)) {
                                commentPropertiesSet.addAll(potentialAuthors.get(username));
                            }
                            potentialAuthors.put(username, commentPropertiesSet);
                        }
                    }
                }
                final String author = Randomizer.randomItem(potentialAuthors.isEmpty() ? this.allUsers : Lists.newArrayList((Iterable)potentialAuthors.keySet()));
                final Timestamp timestamp = timestampGenerator.next();
                final Map<String, Object> comment = Maps.newHashMap();
                final Long commentId = actionHandler.getNextSequenceId();
                comment.put("id", commentId);
                comment.put("issue", issueInfo.id);
                comment.put("author", author);
                comment.put("updateauthor", author);
                comment.put("type", "comment");
                comment.put("body", commentText);
                comment.put("created", timestamp);
                comment.put("updated", timestamp);
                actionHandler.store(comment);
                final Set<CommentProperties> commentProperties = potentialAuthors.getOrDefault(author, Collections.emptySet());
                for (final CommentProperties commentProperty : commentProperties) {
                    for (final Map.Entry<String, JSONObject> properties : commentProperty.getEntityProperties().entrySet()) {
                        if (!StringUtils.isBlank((String)properties.getKey())) {
                            if (properties.getValue() == null) {
                                continue;
                            }
                            final Map<String, Object> propertyFieldMap = Maps.newHashMap();
                            propertyFieldMap.put("id", entityPropertyHandler.getNextSequenceId());
                            propertyFieldMap.put("entityName", commentProperty.getEntityName());
                            propertyFieldMap.put("entityId", commentId);
                            propertyFieldMap.put("propertyKey", properties.getKey());
                            propertyFieldMap.put("value", properties.getValue().toString());
                            propertyFieldMap.put("created", timestamp);
                            propertyFieldMap.put("updated", timestamp);
                            entityPropertyHandler.store(propertyFieldMap);
                        }
                    }
                }
            }
            if ((i + 1) % 10000 == 0) {
                CommentDriver.log.info(String.format("Created %d / %d comments", i, forIssues.size()));
            }
        }
        actionHandler.close();
        entityPropertyHandler.close();
    }
    
    static {
        log = LoggerFactory.getLogger((Class)CommentDriver.class);
    }
}
