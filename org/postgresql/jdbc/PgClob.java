// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.largeobject.LargeObject;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.InputStream;
import java.io.Writer;
import java.io.OutputStream;
import org.postgresql.Driver;
import java.io.Reader;
import java.sql.SQLException;
import org.postgresql.core.BaseConnection;
import java.sql.Clob;

public class PgClob extends AbstractBlobClob implements Clob
{
    public PgClob(final BaseConnection conn, final long oid) throws SQLException {
        super(conn, oid);
    }
    
    @Override
    public synchronized Reader getCharacterStream(final long pos, final long length) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "getCharacterStream(long, long)");
    }
    
    @Override
    public synchronized int setString(final long pos, final String str) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setString(long,str)");
    }
    
    @Override
    public synchronized int setString(final long pos, final String str, final int offset, final int len) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setString(long,String,int,int)");
    }
    
    @Override
    public synchronized OutputStream setAsciiStream(final long pos) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setAsciiStream(long)");
    }
    
    @Override
    public synchronized Writer setCharacterStream(final long pos) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "setCharacteStream(long)");
    }
    
    @Override
    public synchronized InputStream getAsciiStream() throws SQLException {
        return this.getBinaryStream();
    }
    
    @Override
    public synchronized Reader getCharacterStream() throws SQLException {
        final Charset connectionCharset = Charset.forName(this.conn.getEncoding().name());
        return new InputStreamReader(this.getBinaryStream(), connectionCharset);
    }
    
    @Override
    public synchronized String getSubString(final long i, final int j) throws SQLException {
        this.assertPosition(i, j);
        final LargeObject lo = this.getLo(false);
        lo.seek((int)i - 1);
        return new String(lo.read(j));
    }
    
    @Override
    public synchronized long position(final String pattern, final long start) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "position(String,long)");
    }
    
    @Override
    public synchronized long position(final Clob pattern, final long start) throws SQLException {
        this.checkFreed();
        throw Driver.notImplemented(this.getClass(), "position(Clob,start)");
    }
}
