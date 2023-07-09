// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.issuehistory;

import com.atlassian.jira.plugins.datagenerator.db.ChangeItemEntity;
import com.atlassian.jira.plugins.datagenerator.db.ChangeGroupEntity;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;

public class IssueHistoryPersistStrategy implements AutoCloseable
{
    private final EntityHandler changeGroupHandler;
    private final EntityHandler changeItemHandler;
    
    public IssueHistoryPersistStrategy(final EntityManager entityManager) throws SQLException, GenericEntityException {
        this.changeGroupHandler = entityManager.getEntityHandler("ChangeGroup");
        this.changeItemHandler = entityManager.getEntityHandler("ChangeItem");
    }
    
    public void persist(final ChangeGroupEntity changeGroupEntity, final ChangeItemEntity changeItemEntity) throws SQLException {
        changeGroupEntity.store(this.changeGroupHandler);
        changeItemEntity.store(this.changeItemHandler);
    }
    
    @Override
    public void close() throws SQLException {
        this.changeGroupHandler.close();
        this.changeItemHandler.close();
    }
}
