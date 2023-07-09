// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import java.io.InputStream;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public abstract class DbKeyStoreSocketFactory extends WrappedFactory
{
    public DbKeyStoreSocketFactory() throws DbKeyStoreSocketException {
        KeyStore keys;
        char[] password;
        try {
            keys = KeyStore.getInstance("JKS");
            password = this.getKeyStorePassword();
            keys.load(this.getKeyStoreStream(), password);
        }
        catch (final GeneralSecurityException gse) {
            throw new DbKeyStoreSocketException("Failed to load keystore: " + gse.getMessage());
        }
        catch (final FileNotFoundException fnfe) {
            throw new DbKeyStoreSocketException("Failed to find keystore file." + fnfe.getMessage());
        }
        catch (final IOException ioe) {
            throw new DbKeyStoreSocketException("Failed to read keystore file: " + ioe.getMessage());
        }
        try {
            final KeyManagerFactory keyfact = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyfact.init(keys, password);
            final TrustManagerFactory trustfact = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustfact.init(keys);
            final SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(keyfact.getKeyManagers(), trustfact.getTrustManagers(), null);
            this.factory = ctx.getSocketFactory();
        }
        catch (final GeneralSecurityException gse) {
            throw new DbKeyStoreSocketException("Failed to set up database socket factory: " + gse.getMessage());
        }
    }
    
    public abstract char[] getKeyStorePassword();
    
    public abstract InputStream getKeyStoreStream();
    
    public static class DbKeyStoreSocketException extends Exception
    {
        public DbKeyStoreSocketException(final String message) {
            super(message);
        }
    }
}
