// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent;

import java.sql.SQLFeatureNotSupportedException;
import org.postgresql.util.GT;
import org.postgresql.core.Version;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.BaseConnection;

public abstract class AbstractCreateSlotBuilder<T extends ChainedCommonCreateSlotBuilder<T>> implements ChainedCommonCreateSlotBuilder<T>
{
    protected String slotName;
    protected boolean temporaryOption;
    protected BaseConnection connection;
    
    protected AbstractCreateSlotBuilder(final BaseConnection connection) {
        this.temporaryOption = false;
        this.connection = connection;
    }
    
    protected abstract T self();
    
    @Override
    public T withSlotName(final String slotName) {
        this.slotName = slotName;
        return this.self();
    }
    
    @Override
    public T withTemporaryOption() throws SQLFeatureNotSupportedException {
        if (!this.connection.haveMinimumServerVersion(ServerVersion.v10)) {
            throw new SQLFeatureNotSupportedException(GT.tr("Server does not support temporary replication slots", new Object[0]));
        }
        this.temporaryOption = true;
        return this.self();
    }
}
