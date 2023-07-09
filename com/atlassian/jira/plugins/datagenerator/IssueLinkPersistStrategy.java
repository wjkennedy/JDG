// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;

public class IssueLinkPersistStrategy
{
    private static final String ID = "id";
    private static final String LINK_TYPE_ID_FIELD_NAME = "linktype";
    private static final String SOURCE_ID_FIELD_NAME = "source";
    private static final String DESTINATION_ID_LINK_NAME = "destination";
    private static final String SEQUENCE_FIELD_NAME = "sequence";
    private final EntityHandler issueLinkHandler;
    
    public IssueLinkPersistStrategy(final EntityManager entityManager) {
        try {
            this.issueLinkHandler = entityManager.getEntityHandler("IssueLink");
        }
        catch (final GenericEntityException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void createIssueLink(final Long sourceId, final Long destinationId, final Long issueLinkTypeId, final Long sequence) {
        final Map<String, Object> fields = (Map<String, Object>)ImmutableMap.of((Object)"id", (Object)this.issueLinkHandler.getNextSequenceId(), (Object)"source", (Object)sourceId, (Object)"destination", (Object)destinationId, (Object)"linktype", (Object)issueLinkTypeId, (Object)"sequence", (Object)sequence);
        try {
            this.issueLinkHandler.store(fields);
        }
        catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void close() throws SQLException {
        this.issueLinkHandler.close();
    }
}
