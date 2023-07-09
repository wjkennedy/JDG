// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import org.slf4j.LoggerFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.jdbc.SQLProcessor;
import org.ofbiz.core.entity.GenericDataSourceException;
import org.ofbiz.core.entity.jdbc.AutoCommitSQLProcessor;
import org.slf4j.Logger;

public class DbIndexUtils
{
    private static final Logger LOG;
    private static final String DROP_INDEX_QUERY = "DROP INDEX %s";
    private static final String CREATE_INDEX_QUERY = "CREATE INDEX %s ON %s (%s)";
    private static final String DB_HELPER_NAME = "defaultDS";
    
    public static void createIndexes() {
        SQLProcessor sqlProcessor = null;
        try {
            DbIndexUtils.LOG.warn("Enabling indexes");
            sqlProcessor = (SQLProcessor)new AutoCommitSQLProcessor("defaultDS");
            createIndex(sqlProcessor, "jiraissue", "issue_assignee", new String[] { "assignee" });
            createIndex(sqlProcessor, "jiraissue", "issue_created", new String[] { "created" });
            createIndex(sqlProcessor, "jiraissue", "issue_duedate", new String[] { "duedate" });
            createIndex(sqlProcessor, "jiraissue", "issue_proj_num", new String[] { "issuenum, project" });
            createIndex(sqlProcessor, "jiraissue", "issue_proj_status", new String[] { "project, issuestatus" });
            createIndex(sqlProcessor, "jiraissue", "issue_reporter", new String[] { "reporter" });
            createIndex(sqlProcessor, "jiraissue", "issue_resolutiondate", new String[] { "resolutiondate" });
            createIndex(sqlProcessor, "jiraissue", "issue_updated", new String[] { "updated" });
            createIndex(sqlProcessor, "jiraissue", "issue_votes", new String[] { "votes" });
            createIndex(sqlProcessor, "jiraissue", "issue_watches", new String[] { "watches" });
            createIndex(sqlProcessor, "jiraissue", "issue_workflow", new String[] { "workflow_id" });
            createIndex(sqlProcessor, "changegroup", "chggroup_author_created", new String[] { "author", "created" });
            createIndex(sqlProcessor, "changegroup", "chggroup_issue", new String[] { "issueid" });
            createIndex(sqlProcessor, "changeitem", "chgitem_chggrp", new String[] { "groupid" });
            createIndex(sqlProcessor, "changeitem", "chgitem_field", new String[] { "field" });
            createIndex(sqlProcessor, "jiraaction", "action_author_created", new String[] { "author", "created" });
            createIndex(sqlProcessor, "jiraaction", "action_issue", new String[] { "issueid" });
            createIndex(sqlProcessor, "os_currentstep", "wf_entryid", new String[] { "entry_id" });
            createIndex(sqlProcessor, "customfieldvalue", "cfvalue_issue", new String[] { "issue", "customfield" });
            DbIndexUtils.LOG.warn("Enabled all indexes");
        }
        finally {
            if (sqlProcessor != null) {
                try {
                    sqlProcessor.close();
                }
                catch (final GenericDataSourceException e) {
                    DbIndexUtils.LOG.warn("Could not commit enabling indexes", (Throwable)e);
                }
            }
        }
    }
    
    public static void dropIndexes() {
        SQLProcessor sqlProcessor = null;
        try {
            DbIndexUtils.LOG.warn("Disabling indexes");
            sqlProcessor = (SQLProcessor)new AutoCommitSQLProcessor("defaultDS");
            dropIndex(sqlProcessor, "jiraissue", "issue_assignee");
            dropIndex(sqlProcessor, "jiraissue", "issue_created");
            dropIndex(sqlProcessor, "jiraissue", "issue_duedate");
            dropIndex(sqlProcessor, "jiraissue", "issue_proj_num");
            dropIndex(sqlProcessor, "jiraissue", "issue_proj_status");
            dropIndex(sqlProcessor, "jiraissue", "issue_reporter");
            dropIndex(sqlProcessor, "jiraissue", "issue_resolutiondate");
            dropIndex(sqlProcessor, "jiraissue", "issue_updated");
            dropIndex(sqlProcessor, "jiraissue", "issue_votes");
            dropIndex(sqlProcessor, "jiraissue", "issue_watches");
            dropIndex(sqlProcessor, "jiraissue", "issue_workflow");
            dropIndex(sqlProcessor, "changegroup", "chggroup_author_created");
            dropIndex(sqlProcessor, "changegroup", "chggroup_issue");
            dropIndex(sqlProcessor, "changeitem", "chgitem_chggrp");
            dropIndex(sqlProcessor, "changeitem", "chgitem_field");
            dropIndex(sqlProcessor, "jiraaction", "action_author_created");
            dropIndex(sqlProcessor, "jiraaction", "action_issue");
            dropIndex(sqlProcessor, "os_currentstep", "wf_entryid");
            dropIndex(sqlProcessor, "customfieldvalue", "cfvalue_issue");
            DbIndexUtils.LOG.warn("Disabled all indexes");
        }
        finally {
            if (sqlProcessor != null) {
                try {
                    sqlProcessor.close();
                }
                catch (final GenericDataSourceException e) {
                    DbIndexUtils.LOG.warn("Could not commit disabling indexes", (Throwable)e);
                }
            }
        }
    }
    
    private static void createIndex(final SQLProcessor sqlProcessor, final String tableName, final String indexName, final String[] columns) {
        try {
            DbIndexUtils.LOG.warn(String.format("Creating index %s for %s", indexName, tableName));
            final StringBuilder sb = new StringBuilder();
            for (final String column : columns) {
                sb.append(column);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sqlProcessor.prepareStatement(String.format("CREATE INDEX %s ON %s (%s)", indexName, tableName, sb.toString()));
            sqlProcessor.executeUpdate();
            DbIndexUtils.LOG.warn(String.format("Created index %s for %s", indexName, tableName));
        }
        catch (final GenericEntityException e) {
            DbIndexUtils.LOG.error(String.format("Could not create index %s for %s", indexName, tableName), (Throwable)e);
        }
    }
    
    private static void dropIndex(final SQLProcessor sqlProcessor, final String tableName, final String indexName) {
        try {
            sqlProcessor.prepareStatement(String.format("DROP INDEX %s", indexName));
            sqlProcessor.executeUpdate();
            DbIndexUtils.LOG.warn(String.format("Disabled index %s for %s", indexName, tableName));
        }
        catch (final GenericEntityException e) {
            DbIndexUtils.LOG.error(String.format("Could not disable index %s for %s", indexName, tableName), (Throwable)e);
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)DbIndexUtils.class);
    }
}
