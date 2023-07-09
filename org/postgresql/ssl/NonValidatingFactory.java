// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;

public class NonValidatingFactory extends WrappedFactory
{
    public NonValidatingFactory(final String arg) throws GeneralSecurityException {
        final SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { new NonValidatingTM() }, null);
        this.factory = ctx.getSocketFactory();
    }
    
    public static class NonValidatingTM implements X509TrustManager
    {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
        
        @Override
        public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
        }
        
        @Override
        public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
        }
    }
}
