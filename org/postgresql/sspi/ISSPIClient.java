// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.sspi;

import java.io.IOException;
import java.sql.SQLException;

public interface ISSPIClient
{
    boolean isSSPISupported();
    
    void startSSPI() throws SQLException, IOException;
    
    void continueSSPI(final int p0) throws SQLException, IOException;
    
    void dispose();
}
