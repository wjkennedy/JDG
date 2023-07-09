// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import org.postgresql.util.internal.Nullness;
import java.io.Console;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.Callback;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.io.IOException;
import java.security.cert.Certificate;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStore;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import org.postgresql.jdbc.SslMode;
import javax.net.ssl.SSLContext;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.ObjectFactory;
import org.postgresql.PGProperty;
import javax.security.auth.callback.CallbackHandler;
import java.util.Properties;
import javax.net.ssl.KeyManager;

public class LibPQFactory extends WrappedFactory
{
    KeyManager km;
    boolean defaultfile;
    
    private CallbackHandler getCallbackHandler(final Properties info) throws PSQLException {
        final String sslpasswordcallback = PGProperty.SSL_PASSWORD_CALLBACK.get(info);
        if (sslpasswordcallback != null) {
            try {
                final CallbackHandler cbh = ObjectFactory.instantiate(CallbackHandler.class, sslpasswordcallback, info, false, null);
                return cbh;
            }
            catch (final Exception e) {
                throw new PSQLException(GT.tr("The password callback class provided {0} could not be instantiated.", sslpasswordcallback), PSQLState.CONNECTION_FAILURE, e);
            }
        }
        final CallbackHandler cbh = new ConsoleCallbackHandler(PGProperty.SSL_PASSWORD.get(info));
        return cbh;
    }
    
    private void initPk8(final String sslkeyfile, final String defaultdir, final Properties info) throws PSQLException {
        String sslcertfile = PGProperty.SSL_CERT.get(info);
        if (sslcertfile == null) {
            this.defaultfile = true;
            sslcertfile = defaultdir + "postgresql.crt";
        }
        this.km = new LazyKeyManager("".equals(sslcertfile) ? null : sslcertfile, "".equals(sslkeyfile) ? null : sslkeyfile, this.getCallbackHandler(info), this.defaultfile);
    }
    
    private void initP12(final String sslkeyfile, final Properties info) throws PSQLException {
        this.km = new PKCS12KeyManager(sslkeyfile, this.getCallbackHandler(info));
    }
    
    public LibPQFactory(final Properties info) throws PSQLException {
        try {
            final SSLContext ctx = SSLContext.getInstance("TLS");
            final String pathsep = System.getProperty("file.separator");
            String defaultdir;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                defaultdir = System.getenv("APPDATA") + pathsep + "postgresql" + pathsep;
            }
            else {
                defaultdir = System.getProperty("user.home") + pathsep + ".postgresql" + pathsep;
            }
            String sslkeyfile = PGProperty.SSL_KEY.get(info);
            if (sslkeyfile == null) {
                this.defaultfile = true;
                sslkeyfile = defaultdir + "postgresql.pk8";
            }
            if (sslkeyfile.endsWith(".p12") || sslkeyfile.endsWith(".pfx")) {
                this.initP12(sslkeyfile, info);
            }
            else {
                this.initPk8(sslkeyfile, defaultdir, info);
            }
            final SslMode sslMode = SslMode.of(info);
            TrustManager[] tm;
            if (!sslMode.verifyCertificate()) {
                tm = new TrustManager[] { new NonValidatingFactory.NonValidatingTM() };
            }
            else {
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
                KeyStore ks;
                try {
                    ks = KeyStore.getInstance("jks");
                }
                catch (final KeyStoreException e) {
                    throw new NoSuchAlgorithmException("jks KeyStore not available");
                }
                String sslrootcertfile = PGProperty.SSL_ROOT_CERT.get(info);
                if (sslrootcertfile == null) {
                    sslrootcertfile = defaultdir + "root.crt";
                }
                FileInputStream fis;
                try {
                    fis = new FileInputStream(sslrootcertfile);
                }
                catch (final FileNotFoundException ex) {
                    throw new PSQLException(GT.tr("Could not open SSL root certificate file {0}.", sslrootcertfile), PSQLState.CONNECTION_FAILURE, ex);
                }
                try {
                    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    final Object[] certs = cf.generateCertificates(fis).toArray(new Certificate[0]);
                    ks.load(null, null);
                    for (int i = 0; i < certs.length; ++i) {
                        ks.setCertificateEntry("cert" + i, (Certificate)certs[i]);
                    }
                    tmf.init(ks);
                }
                catch (final IOException ioex) {
                    throw new PSQLException(GT.tr("Could not read SSL root certificate file {0}.", sslrootcertfile), PSQLState.CONNECTION_FAILURE, ioex);
                }
                catch (final GeneralSecurityException gsex) {
                    throw new PSQLException(GT.tr("Loading the SSL root certificate {0} into a TrustManager failed.", sslrootcertfile), PSQLState.CONNECTION_FAILURE, gsex);
                }
                finally {
                    try {
                        fis.close();
                    }
                    catch (final IOException ex4) {}
                }
                tm = tmf.getTrustManagers();
            }
            try {
                final KeyManager km = this.km;
                ctx.init((KeyManager[])((km == null) ? null : new KeyManager[] { km }), tm, null);
            }
            catch (final KeyManagementException ex2) {
                throw new PSQLException(GT.tr("Could not initialize SSL context.", new Object[0]), PSQLState.CONNECTION_FAILURE, ex2);
            }
            this.factory = ctx.getSocketFactory();
        }
        catch (final NoSuchAlgorithmException ex3) {
            throw new PSQLException(GT.tr("Could not find a java cryptographic algorithm: {0}.", ex3.getMessage()), PSQLState.CONNECTION_FAILURE, ex3);
        }
    }
    
    public void throwKeyManagerException() throws PSQLException {
        if (this.km != null) {
            if (this.km instanceof LazyKeyManager) {
                ((LazyKeyManager)this.km).throwKeyManagerException();
            }
            if (this.km instanceof PKCS12KeyManager) {
                ((PKCS12KeyManager)this.km).throwKeyManagerException();
            }
        }
    }
    
    public static class ConsoleCallbackHandler implements CallbackHandler
    {
        private char[] password;
        
        ConsoleCallbackHandler(final String password) {
            this.password = null;
            if (password != null) {
                this.password = password.toCharArray();
            }
        }
        
        @Override
        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            final Console cons = System.console();
            final char[] password = this.password;
            if (cons == null && password == null) {
                throw new UnsupportedCallbackException(callbacks[0], "Console is not available");
            }
            for (final Callback callback : callbacks) {
                if (!(callback instanceof PasswordCallback)) {
                    throw new UnsupportedCallbackException(callback);
                }
                final PasswordCallback pwdCallback = (PasswordCallback)callback;
                if (password != null) {
                    pwdCallback.setPassword(password);
                }
                else {
                    pwdCallback.setPassword(Nullness.castNonNull(cons, "System.console()").readPassword("%s", pwdCallback.getPrompt()));
                }
            }
        }
    }
}
