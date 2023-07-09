// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db.postgres;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.InvalidTransactionException;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.GenericTransactionException;
import javax.transaction.SystemException;
import org.ofbiz.core.entity.TransactionFactory;
import org.ofbiz.core.entity.TransactionUtil;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class JiraSequenceIdGenerator
{
    private static final Logger LOG;
    private static final long DEFAULT_BANK_SIZE = 1000000L;
    private final ConcurrentHashMap<String, SequenceBank> sequenceBanks;
    
    public JiraSequenceIdGenerator() {
        this.sequenceBanks = new ConcurrentHashMap<String, SequenceBank>();
    }
    
    public long getNextSequenceId(final String sequenceName) {
        final SequenceBank sequenceBank = this.sequenceBanks.computeIfAbsent(sequenceName, name -> new SequenceBank(name, 1000000L));
        return sequenceBank.getNextSeqId();
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)JiraSequenceIdGenerator.class);
    }
    
    private class SequenceBank
    {
        private static final long startSeqId = 10000L;
        private static final int minWaitNanos = 500000;
        private static final int maxWaitNanos = 1000000;
        private static final int maxTries = 5;
        private final String seqName;
        private final long bankSize;
        private volatile long curSeqId;
        private volatile long maxSeqId;
        
        public SequenceBank(final String seqName, final long bankSize) {
            this.seqName = seqName;
            this.bankSize = bankSize;
            this.curSeqId = 0L;
            this.maxSeqId = 0L;
            this.fillBank();
        }
        
        public synchronized long getNextSeqId() {
            if (this.curSeqId < this.maxSeqId) {
                final long retSeqId = this.curSeqId;
                ++this.curSeqId;
                return retSeqId;
            }
            JiraSequenceIdGenerator.LOG.debug("Need filling new bank curSeqId=" + this.curSeqId + ", maxSeqId=" + this.maxSeqId);
            this.fillBank();
            if (this.curSeqId < this.maxSeqId) {
                final Long retSeqId2 = this.curSeqId;
                ++this.curSeqId;
                return retSeqId2;
            }
            JiraSequenceIdGenerator.LOG.error("[SequenceUtil.SequenceBank.getNextSeqId] Fill bank failed, returning null");
            return -1L;
        }
        
        protected synchronized void fillBank() {
            if (this.curSeqId < this.maxSeqId) {
                return;
            }
            long val1 = 0L;
            long val2 = 0L;
            boolean manualTX = true;
            Transaction suspendedTransaction = null;
            TransactionManager transactionManager = null;
            try {
                if (TransactionUtil.getStatus() == 0) {
                    manualTX = false;
                    try {
                        transactionManager = TransactionFactory.getTransactionManager();
                        if (transactionManager != null) {
                            suspendedTransaction = transactionManager.suspend();
                            manualTX = true;
                        }
                    }
                    catch (final SystemException e) {
                        JiraSequenceIdGenerator.LOG.error("System Error suspending transaction in sequence util", (Throwable)e);
                    }
                }
            }
            catch (final GenericTransactionException e2) {
                JiraSequenceIdGenerator.LOG.error("[SequenceUtil.SequenceBank.fillBank] Exception was thrown trying to check transaction status: ", (Throwable)e2);
            }
            Connection connection = null;
            try {
                connection = ConnectionFactory.getConnection("defaultDS");
            }
            catch (final SQLException | GenericEntityException sqle) {
                JiraSequenceIdGenerator.LOG.error("[SequenceUtil.SequenceBank.fillBank]: Unable to establish a connection with the database... Error was:", (Throwable)sqle);
            }
            PreparedStatement selectPstmt = null;
            PreparedStatement insertPstmt = null;
            PreparedStatement updatePstmt = null;
            try {
                try {
                    connection.setAutoCommit(false);
                }
                catch (final SQLException sqle2) {
                    manualTX = false;
                }
                int numTries = 0;
                while (val1 + this.bankSize != val2) {
                    ResultSet rs1 = null;
                    ResultSet rs2 = null;
                    try {
                        JiraSequenceIdGenerator.LOG.debug("[SequenceUtil.SequenceBank.fillBank] Trying to get a bank of sequenced ids for " + this.seqName + "; start of loop val1=" + val1 + ", val2=" + val2 + ", bankSize=" + this.bankSize);
                        if (selectPstmt == null) {
                            selectPstmt = connection.prepareStatement("SELECT seq_id FROM sequence_value_item WHERE seq_name = ?");
                        }
                        selectPstmt.setString(1, this.seqName);
                        selectPstmt.execute();
                        rs1 = selectPstmt.getResultSet();
                        if (rs1.next()) {
                            val1 = rs1.getLong(1);
                            if (updatePstmt == null) {
                                updatePstmt = connection.prepareStatement("UPDATE sequence_value_item SET seq_id = seq_id + " + this.bankSize + " WHERE seq_name = ?");
                            }
                            updatePstmt.setString(1, this.seqName);
                            updatePstmt.execute();
                            if (updatePstmt.getUpdateCount() <= 0) {
                                JiraSequenceIdGenerator.LOG.error("[SequenceUtil.SequenceBank.fillBank] update failed, no rows changes for seqName: " + this.seqName);
                                return;
                            }
                            if (manualTX) {
                                connection.commit();
                            }
                            selectPstmt.setString(1, this.seqName);
                            selectPstmt.execute();
                            rs2 = selectPstmt.getResultSet();
                            if (!rs2.next()) {
                                JiraSequenceIdGenerator.LOG.error("[SequenceUtil.SequenceBank.fillBank] second select failed: aborting, result set was empty for sequence: " + this.seqName);
                                return;
                            }
                            val2 = rs2.getLong(1);
                            if (manualTX) {
                                connection.commit();
                            }
                            if (val1 + this.bankSize != val2) {
                                if (numTries >= 5) {
                                    JiraSequenceIdGenerator.LOG.error("[SequenceUtil.SequenceBank.fillBank] maxTries (5) reached, giving up.");
                                    return;
                                }
                                final int waitTime = new Double(Math.random() * 500000.0).intValue() + 500000;
                                try {
                                    this.wait(0L, waitTime);
                                }
                                catch (final Exception e3) {
                                    JiraSequenceIdGenerator.LOG.error("Error waiting in sequence util", (Throwable)e3);
                                }
                            }
                            ++numTries;
                        }
                        else {
                            JiraSequenceIdGenerator.LOG.debug("[SequenceUtil.SequenceBank.fillBank] first select failed: trying to add row, result set was empty for sequence: " + this.seqName);
                            if (insertPstmt == null) {
                                insertPstmt = connection.prepareStatement("INSERT INTO sequence_value_item (seq_name, seq_id) VALUES (?, ?)");
                            }
                            insertPstmt.setString(1, this.seqName);
                            insertPstmt.setLong(2, 10000L);
                            insertPstmt.execute();
                            if (insertPstmt.getUpdateCount() <= 0) {
                                return;
                            }
                            continue;
                        }
                    }
                    finally {
                        this.closeQuietly(rs2);
                        this.closeQuietly(rs1);
                    }
                }
                this.curSeqId = val1;
                this.maxSeqId = val2;
                JiraSequenceIdGenerator.LOG.debug("[SequenceUtil.SequenceBank.fillBank] Successfully got a bank of sequenced ids for " + this.seqName + "; curSeqId=" + this.curSeqId + ", maxSeqId=" + this.maxSeqId + ", bankSize=" + this.bankSize);
            }
            catch (final SQLException sqle2) {
                JiraSequenceIdGenerator.LOG.warn("[SequenceUtil.SequenceBank.fillBank] SQL Exception", (Throwable)sqle2);
                return;
            }
            finally {
                this.closeQuietly(updatePstmt);
                this.closeQuietly(insertPstmt);
                this.closeQuietly(selectPstmt);
                this.closeQuietly(connection);
            }
            if (suspendedTransaction != null) {
                try {
                    if (transactionManager == null) {
                        transactionManager = TransactionFactory.getTransactionManager();
                    }
                    if (transactionManager != null) {
                        transactionManager.resume(suspendedTransaction);
                    }
                }
                catch (final InvalidTransactionException e4) {
                    JiraSequenceIdGenerator.LOG.error("InvalidTransaction Error resuming suspended transaction in sequence util", (Throwable)e4);
                }
                catch (final IllegalStateException e5) {
                    JiraSequenceIdGenerator.LOG.error("IllegalState Error resuming suspended transaction in sequence util", (Throwable)e5);
                }
                catch (final SystemException e6) {
                    JiraSequenceIdGenerator.LOG.error("System Error resuming suspended transaction in sequence util", (Throwable)e6);
                }
            }
        }
        
        private void closeQuietly(final Connection connection) {
            try {
                if (connection != null) {
                    connection.close();
                }
            }
            catch (final Exception sqle) {
                JiraSequenceIdGenerator.LOG.error("Error closing connection in sequence util", (Throwable)sqle);
            }
        }
        
        private void closeQuietly(final PreparedStatement stmt) {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (final Exception sqle) {
                JiraSequenceIdGenerator.LOG.error("Error closing statement in sequence util", (Throwable)sqle);
            }
        }
        
        private void closeQuietly(final ResultSet rs) {
            try {
                if (rs != null) {
                    rs.close();
                }
            }
            catch (final Exception sqle) {
                JiraSequenceIdGenerator.LOG.error("Error closing result set in sequence util", (Throwable)sqle);
            }
        }
    }
}
