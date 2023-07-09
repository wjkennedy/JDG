// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import org.slf4j.LoggerFactory;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.timestamp.TimestampGenerator;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import com.google.common.collect.Maps;
import java.sql.Timestamp;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.plugins.datagenerator.timestamp.FixedIntervalEndlessGenerator;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import org.slf4j.Logger;

public class WorklogDriver
{
    private static final Logger log;
    private final GeneratorConfiguration generatorConfiguration;
    private final List<String> allUsers;
    private final TextGenerator textGenerator;
    private final EntityManager entityManager;
    
    public WorklogDriver(final GeneratorContext context, final EntityManager entityManager) {
        this.generatorConfiguration = context.generatorConfiguration;
        this.allUsers = (List<String>)ImmutableList.copyOf((Collection)context.reporters.keySet());
        this.entityManager = entityManager;
        this.textGenerator = TextGenerator.getTextGenerator(this.generatorConfiguration, this.allUsers);
    }
    
    public void generate(final List<IssueInfo> forIssues) throws SQLException, GenericEntityException {
        final EntityHandler actionHandler = this.entityManager.getEntityHandler("Worklog");
        for (int i = 0; i < forIssues.size(); ++i) {
            final IssueInfo issueInfo = forIssues.get(i);
            final TimestampGenerator timestampGenerator = new FixedIntervalEndlessGenerator(issueInfo.created, this.generatorConfiguration.issues.avgWorklogInterval);
            int counter = Randomizer.randomLimitedGaussian(this.generatorConfiguration.issues.issueWorklogCount);
            String commentText;
            while (counter-- > 0 && (commentText = this.textGenerator.generateText()) != null) {
                final String author = Randomizer.randomItem(this.allUsers);
                final Timestamp timestamp = timestampGenerator.next();
                final Map<String, Object> worklog = Maps.newHashMap();
                worklog.put("id", actionHandler.getNextSequenceId());
                worklog.put("issue", issueInfo.id);
                worklog.put("author", author);
                worklog.put("updateauthor", author);
                worklog.put("type", "worklog");
                worklog.put("body", commentText);
                worklog.put("created", timestamp);
                worklog.put("updated", timestamp);
                worklog.put("startdate", timestamp);
                worklog.put("timeworked", timestamp.getTime());
                actionHandler.store(worklog);
            }
            if (i % 10000 == 0) {
                WorklogDriver.log.info(String.format("Created %d / %d worklogs", i, forIssues.size()));
            }
        }
        actionHandler.close();
    }
    
    static {
        log = LoggerFactory.getLogger((Class)WorklogDriver.class);
    }
}
