// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.gss;

import org.ietf.jgss.GSSException;
import org.postgresql.util.internal.Nullness;
import java.io.IOException;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.GSSContext;
import java.io.InputStream;

public class GSSInputStream extends InputStream
{
    private GSSContext gssContext;
    private MessageProp messageProp;
    private InputStream wrapped;
    byte[] unencrypted;
    int unencryptedPos;
    int unencryptedLength;
    
    public GSSInputStream(final InputStream wrapped, final GSSContext gssContext, final MessageProp messageProp) {
        this.wrapped = wrapped;
        this.gssContext = gssContext;
        this.messageProp = messageProp;
    }
    
    @Override
    public int read() throws IOException {
        return 0;
    }
    
    @Override
    public int read(final byte[] buffer, final int pos, final int len) throws IOException {
        final byte[] int4Buf = new byte[4];
        int copyLength = 0;
        if (this.unencryptedLength > 0) {
            copyLength = Math.min(len, this.unencryptedLength);
            System.arraycopy(Nullness.castNonNull(this.unencrypted), this.unencryptedPos, buffer, pos, copyLength);
            this.unencryptedLength -= copyLength;
            this.unencryptedPos += copyLength;
        }
        else if (this.wrapped.read(int4Buf, 0, 4) == 4) {
            final int encryptedLength = (int4Buf[0] & 0xFF) << 24 | (int4Buf[1] & 0xFF) << 16 | (int4Buf[2] & 0xFF) << 8 | (int4Buf[3] & 0xFF);
            final byte[] encryptedBuffer = new byte[encryptedLength];
            this.wrapped.read(encryptedBuffer, 0, encryptedLength);
            try {
                final byte[] unencrypted = this.gssContext.unwrap(encryptedBuffer, 0, encryptedLength, this.messageProp);
                this.unencrypted = unencrypted;
                this.unencryptedLength = unencrypted.length;
                this.unencryptedPos = 0;
                copyLength = Math.min(len, unencrypted.length);
                System.arraycopy(unencrypted, this.unencryptedPos, buffer, pos, copyLength);
                this.unencryptedLength -= copyLength;
                this.unencryptedPos += copyLength;
            }
            catch (final GSSException e) {
                throw new IOException(e);
            }
            return copyLength;
        }
        return copyLength;
    }
}
