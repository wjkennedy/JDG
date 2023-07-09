// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import org.postgresql.core.Utils;
import java.lang.ref.PhantomReference;
import org.postgresql.core.ResultCursor;

class Portal implements ResultCursor
{
    private final SimpleQuery query;
    private final String portalName;
    private final byte[] encodedName;
    private PhantomReference<?> cleanupRef;
    
    Portal(final SimpleQuery query, final String portalName) {
        this.query = query;
        this.portalName = portalName;
        this.encodedName = Utils.encodeUTF8(portalName);
    }
    
    @Override
    public void close() {
        if (this.cleanupRef != null) {
            this.cleanupRef.clear();
            this.cleanupRef.enqueue();
            this.cleanupRef = null;
        }
    }
    
    String getPortalName() {
        return this.portalName;
    }
    
    byte[] getEncodedPortalName() {
        return this.encodedName;
    }
    
    SimpleQuery getQuery() {
        return this.query;
    }
    
    void setCleanupRef(final PhantomReference<?> cleanupRef) {
        this.cleanupRef = cleanupRef;
    }
    
    @Override
    public String toString() {
        return this.portalName;
    }
}
