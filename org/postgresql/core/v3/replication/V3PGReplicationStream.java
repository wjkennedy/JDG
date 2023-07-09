// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3.replication;

import java.util.Date;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;
import java.net.SocketTimeoutException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.SQLException;
import java.nio.ByteBuffer;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.ReplicationType;
import org.postgresql.copy.CopyDual;
import java.util.logging.Logger;
import org.postgresql.replication.PGReplicationStream;

public class V3PGReplicationStream implements PGReplicationStream
{
    private static final Logger LOGGER;
    public static final long POSTGRES_EPOCH_2000_01_01 = 946684800000L;
    private static final long NANOS_PER_MILLISECOND = 1000000L;
    private final CopyDual copyDual;
    private final long updateInterval;
    private final ReplicationType replicationType;
    private long lastStatusUpdate;
    private boolean closeFlag;
    private LogSequenceNumber lastServerLSN;
    private volatile LogSequenceNumber lastReceiveLSN;
    private volatile LogSequenceNumber lastAppliedLSN;
    private volatile LogSequenceNumber lastFlushedLSN;
    
    public V3PGReplicationStream(final CopyDual copyDual, final LogSequenceNumber startLSN, final long updateIntervalMs, final ReplicationType replicationType) {
        this.closeFlag = false;
        this.lastServerLSN = LogSequenceNumber.INVALID_LSN;
        this.lastReceiveLSN = LogSequenceNumber.INVALID_LSN;
        this.lastAppliedLSN = LogSequenceNumber.INVALID_LSN;
        this.lastFlushedLSN = LogSequenceNumber.INVALID_LSN;
        this.copyDual = copyDual;
        this.updateInterval = updateIntervalMs * 1000000L;
        this.lastStatusUpdate = System.nanoTime() - updateIntervalMs * 1000000L;
        this.lastReceiveLSN = startLSN;
        this.replicationType = replicationType;
    }
    
    @Override
    public ByteBuffer read() throws SQLException {
        this.checkClose();
        ByteBuffer payload;
        for (payload = null; payload == null && this.copyDual.isActive(); payload = this.readInternal(true)) {}
        return payload;
    }
    
    @Override
    public ByteBuffer readPending() throws SQLException {
        this.checkClose();
        return this.readInternal(false);
    }
    
    @Override
    public LogSequenceNumber getLastReceiveLSN() {
        return this.lastReceiveLSN;
    }
    
    @Override
    public LogSequenceNumber getLastFlushedLSN() {
        return this.lastFlushedLSN;
    }
    
    @Override
    public LogSequenceNumber getLastAppliedLSN() {
        return this.lastAppliedLSN;
    }
    
    @Override
    public void setFlushedLSN(final LogSequenceNumber flushed) {
        this.lastFlushedLSN = flushed;
    }
    
    @Override
    public void setAppliedLSN(final LogSequenceNumber applied) {
        this.lastAppliedLSN = applied;
    }
    
    @Override
    public void forceUpdateStatus() throws SQLException {
        this.checkClose();
        this.updateStatusInternal(this.lastReceiveLSN, this.lastFlushedLSN, this.lastAppliedLSN, true);
    }
    
    @Override
    public boolean isClosed() {
        return this.closeFlag || !this.copyDual.isActive();
    }
    
    private ByteBuffer readInternal(final boolean block) throws SQLException {
        boolean updateStatusRequired = false;
        while (this.copyDual.isActive()) {
            final ByteBuffer buffer = this.receiveNextData(block);
            if (updateStatusRequired || this.isTimeUpdate()) {
                this.timeUpdateStatus();
            }
            if (buffer == null) {
                return null;
            }
            final int code = buffer.get();
            switch (code) {
                case 107: {
                    updateStatusRequired = this.processKeepAliveMessage(buffer);
                    updateStatusRequired |= (this.updateInterval == 0L);
                    continue;
                }
                case 119: {
                    return this.processXLogData(buffer);
                }
                default: {
                    throw new PSQLException(GT.tr("Unexpected packet type during replication: {0}", Integer.toString(code)), PSQLState.PROTOCOL_VIOLATION);
                }
            }
        }
        return null;
    }
    
    private ByteBuffer receiveNextData(final boolean block) throws SQLException {
        try {
            final byte[] message = this.copyDual.readFromCopy(block);
            if (message != null) {
                return ByteBuffer.wrap(message);
            }
            return null;
        }
        catch (final PSQLException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                return null;
            }
            throw e;
        }
    }
    
    private boolean isTimeUpdate() {
        if (this.updateInterval == 0L) {
            return false;
        }
        final long diff = System.nanoTime() - this.lastStatusUpdate;
        return diff >= this.updateInterval;
    }
    
    private void timeUpdateStatus() throws SQLException {
        this.updateStatusInternal(this.lastReceiveLSN, this.lastFlushedLSN, this.lastAppliedLSN, false);
    }
    
    private void updateStatusInternal(final LogSequenceNumber received, final LogSequenceNumber flushed, final LogSequenceNumber applied, final boolean replyRequired) throws SQLException {
        final byte[] reply = this.prepareUpdateStatus(received, flushed, applied, replyRequired);
        this.copyDual.writeToCopy(reply, 0, reply.length);
        this.copyDual.flushCopy();
        this.lastStatusUpdate = System.nanoTime();
    }
    
    private byte[] prepareUpdateStatus(final LogSequenceNumber received, final LogSequenceNumber flushed, final LogSequenceNumber applied, final boolean replyRequired) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(34);
        final long now = System.nanoTime() / 1000000L;
        final long systemClock = TimeUnit.MICROSECONDS.convert(now - 946684800000L, TimeUnit.MICROSECONDS);
        if (V3PGReplicationStream.LOGGER.isLoggable(Level.FINEST)) {
            V3PGReplicationStream.LOGGER.log(Level.FINEST, " FE=> StandbyStatusUpdate(received: {0}, flushed: {1}, applied: {2}, clock: {3})", new Object[] { received.asString(), flushed.asString(), applied.asString(), new Date(now) });
        }
        byteBuffer.put((byte)114);
        byteBuffer.putLong(received.asLong());
        byteBuffer.putLong(flushed.asLong());
        byteBuffer.putLong(applied.asLong());
        byteBuffer.putLong(systemClock);
        if (replyRequired) {
            byteBuffer.put((byte)1);
        }
        else {
            byteBuffer.put((byte)((received == LogSequenceNumber.INVALID_LSN) ? 1 : 0));
        }
        this.lastStatusUpdate = now;
        return byteBuffer.array();
    }
    
    private boolean processKeepAliveMessage(final ByteBuffer buffer) {
        this.lastServerLSN = LogSequenceNumber.valueOf(buffer.getLong());
        if (this.lastServerLSN.asLong() > this.lastReceiveLSN.asLong()) {
            this.lastReceiveLSN = this.lastServerLSN;
        }
        final long lastServerClock = buffer.getLong();
        final boolean replyRequired = buffer.get() != 0;
        if (V3PGReplicationStream.LOGGER.isLoggable(Level.FINEST)) {
            final Date clockTime = new Date(TimeUnit.MILLISECONDS.convert(lastServerClock, TimeUnit.MICROSECONDS) + 946684800000L);
            V3PGReplicationStream.LOGGER.log(Level.FINEST, "  <=BE Keepalive(lastServerWal: {0}, clock: {1} needReply: {2})", new Object[] { this.lastServerLSN.asString(), clockTime, replyRequired });
        }
        return replyRequired;
    }
    
    private ByteBuffer processXLogData(final ByteBuffer buffer) {
        final long startLsn = buffer.getLong();
        this.lastServerLSN = LogSequenceNumber.valueOf(buffer.getLong());
        final long systemClock = buffer.getLong();
        switch (this.replicationType) {
            case LOGICAL: {
                this.lastReceiveLSN = LogSequenceNumber.valueOf(startLsn);
                break;
            }
            case PHYSICAL: {
                final int payloadSize = buffer.limit() - buffer.position();
                this.lastReceiveLSN = LogSequenceNumber.valueOf(startLsn + payloadSize);
                break;
            }
        }
        if (V3PGReplicationStream.LOGGER.isLoggable(Level.FINEST)) {
            V3PGReplicationStream.LOGGER.log(Level.FINEST, "  <=BE XLogData(currWal: {0}, lastServerWal: {1}, clock: {2})", new Object[] { this.lastReceiveLSN.asString(), this.lastServerLSN.asString(), systemClock });
        }
        return buffer.slice();
    }
    
    private void checkClose() throws PSQLException {
        if (this.isClosed()) {
            throw new PSQLException(GT.tr("This replication stream has been closed.", new Object[0]), PSQLState.CONNECTION_DOES_NOT_EXIST);
        }
    }
    
    @Override
    public void close() throws SQLException {
        if (this.isClosed()) {
            return;
        }
        V3PGReplicationStream.LOGGER.log(Level.FINEST, " FE=> StopReplication");
        this.copyDual.endCopy();
        this.closeFlag = true;
    }
    
    static {
        LOGGER = Logger.getLogger(V3PGReplicationStream.class.getName());
    }
}
