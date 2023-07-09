// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channels;
import java.nio.ByteBuffer;

public class ByteBufferByteStreamWriter implements ByteStreamWriter
{
    private final ByteBuffer buf;
    private final int length;
    
    public ByteBufferByteStreamWriter(final ByteBuffer buf) {
        this.buf = buf;
        this.length = buf.remaining();
    }
    
    @Override
    public int getLength() {
        return this.length;
    }
    
    @Override
    public void writeTo(final ByteStreamTarget target) throws IOException {
        final WritableByteChannel c = Channels.newChannel(target.getOutputStream());
        try {
            c.write(this.buf);
        }
        finally {
            c.close();
        }
    }
}
