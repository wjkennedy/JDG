// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.timestamp.TimestampGenerator;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import com.google.common.collect.Maps;
import java.sql.Timestamp;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.plugins.datagenerator.timestamp.FixedIntervalEndlessGenerator;
import com.atlassian.jira.issue.Issue;
import java.util.Iterator;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.plugins.datagenerator.util.PhasedTimer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.issue.IssueManager;
import org.springframework.stereotype.Component;

@Component
public class AdditionalCommentGenerator
{
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final UserUtils userUtils;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    private GeneratorContext context;
    private EntityManager entityManager;
    private GeneratorConfiguration generatorConfiguration;
    private TextGenerator textGenerator;
    private List<String> allUsers;
    
    @Autowired
    public AdditionalCommentGenerator(@ComponentImport final IssueManager issueManager, @ComponentImport final ProjectManager projectManager, final UserUtils userUtils, final JiraSequenceIdGenerator sequenceIdGenerator) {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.userUtils = userUtils;
        this.sequenceIdGenerator = sequenceIdGenerator;
    }
    
    public void prepare(final GeneratorContext context) throws Exception {
        this.context = context;
        this.generatorConfiguration = context.generatorConfiguration;
        this.allUsers = Lists.newArrayList((Iterable)this.userUtils.getUsers().keySet());
        this.entityManager = new EntityManager(this.sequenceIdGenerator, this.generatorConfiguration);
        this.textGenerator = TextGenerator.getTextGenerator(this.generatorConfiguration, this.allUsers);
    }
    
    public void generate() throws Exception {
        int commentCount = this.context.generatorConfiguration.additionalComments;
        final List<Project> allProjectsToGenerate = GeneratorConfigurationUtil.getProjects(this.context.generatorConfiguration, this.projectManager);
        int issuesCount = 0;
        for (final Project p : allProjectsToGenerate) {
            issuesCount += (int)this.issueManager.getIssueCountForProject(p.getId());
        }
        double meanCommentPerIssue = commentCount / (double)issuesCount;
        final PhasedTimer phasedTimer = new PhasedTimer();
        this.context.resetProgress(String.format("Generating %d comments", commentCount), commentCount);
        phasedTimer.startPhase("generating comments:");
        for (final Project p2 : allProjectsToGenerate) {
            final Collection<Long> issueIds = this.issueManager.getIssueIdsForProject(p2.getId());
            for (final Long issueId : issueIds) {
                if (commentCount == 0) {
                    break;
                }
                final Issue issue = (Issue)this.issueManager.getIssueObject(issueId);
                int commentsForIssueCount;
                if (issuesCount == 1) {
                    commentsForIssueCount = commentCount;
                }
                else {
                    commentsForIssueCount = Math.min(commentCount, Randomizer.randomLimitedGaussian(meanCommentPerIssue));
                    commentCount -= commentsForIssueCount;
                    --issuesCount;
                    meanCommentPerIssue = commentCount / (double)issuesCount;
                }
                this.addCommentsToIssue(issue, commentsForIssueCount);
                this.context.progress.addAndGet(commentsForIssueCount);
            }
        }
        phasedTimer.stop();
        this.context.messages.addAll(phasedTimer.toStrings());
    }
    
    public void addCommentsToIssue(final Issue issue, final int commentCount) throws SQLException, GenericEntityException {
        final EntityHandler actionHandler = this.entityManager.getEntityHandler("Action");
        final TimestampGenerator timestampGenerator = new FixedIntervalEndlessGenerator(issue.getCreated().getTime(), this.generatorConfiguration.issues.avgCommentInterval);
        final List<Timestamp> timestamps = (List<Timestamp>)ImmutableList.of(((Iterator<Object>)timestampGenerator).next(), ((Iterator<Object>)timestampGenerator).next(), ((Iterator<Object>)timestampGenerator).next(), ((Iterator<Object>)timestampGenerator).next(), ((Iterator<Object>)timestampGenerator).next(), ((Iterator<Object>)timestampGenerator).next(), ((Iterator<Object>)timestampGenerator).next(), ((Iterator<Object>)timestampGenerator).next());
        for (int i = 0; i < commentCount; ++i) {
            final Timestamp timestamp = Randomizer.randomItem(timestamps);
            final String commentText = this.textGenerator.generateText();
            final String author = Randomizer.randomItem(this.allUsers);
            final Map<String, Object> comment = Maps.newHashMap();
            final Long commentId = actionHandler.getNextSequenceId();
            comment.put("id", commentId);
            comment.put("issue", issue.getId());
            comment.put("author", author);
            comment.put("updateauthor", author);
            comment.put("type", "comment");
            comment.put("body", commentText);
            comment.put("created", timestamp);
            comment.put("updated", timestamp);
            actionHandler.store(comment);
        }
        actionHandler.close();
    }
}
