// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.net.Socket;
import java.security.Principal;
import java.security.KeyStoreException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.security.KeyStore;
import org.postgresql.util.PSQLException;
import javax.security.auth.callback.CallbackHandler;
import javax.net.ssl.X509KeyManager;

public class PKCS12KeyManager implements X509KeyManager
{
    private final CallbackHandler cbh;
    private PSQLException error;
    private final String keyfile;
    private final KeyStore keyStore;
    boolean keystoreLoaded;
    
    public PKCS12KeyManager(final String pkcsFile, final CallbackHandler cbh) throws PSQLException {
        this.error = null;
        this.keystoreLoaded = false;
        try {
            this.keyStore = KeyStore.getInstance("pkcs12");
            this.keyfile = pkcsFile;
            this.cbh = cbh;
        }
        catch (final KeyStoreException kse) {
            throw new PSQLException(GT.tr("Unable to find pkcs12 keystore.", new Object[0]), PSQLState.CONNECTION_FAILURE, kse);
        }
    }
    
    public void throwKeyManagerException() throws PSQLException {
        if (this.error != null) {
            throw this.error;
        }
    }
    
    @Override
    public String[] getClientAliases(final String keyType, final Principal[] principals) {
        final String alias = this.chooseClientAlias(new String[] { keyType }, principals, null);
        return (String[])((alias == null) ? null : new String[] { alias });
    }
    
    @Override
    public String chooseClientAlias(final String[] strings, final Principal[] principals, final Socket socket) {
        if (principals == null || principals.length == 0) {
            return "user";
        }
        final X509Certificate[] certchain = this.getCertificateChain("user");
        if (certchain == null) {
            return null;
        }
        final X500Principal ourissuer = certchain[certchain.length - 1].getIssuerX500Principal();
        boolean found = false;
        for (final Principal issuer : principals) {
            if (ourissuer.equals(issuer)) {
                found = true;
            }
        }
        return found ? "user" : null;
    }
    
    @Override
    public String[] getServerAliases(final String s, final Principal[] principals) {
        return new String[0];
    }
    
    @Override
    public String chooseServerAlias(final String s, final Principal[] principals, final Socket socket) {
        return null;
    }
    
    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        try {
            this.loadKeyStore();
            final Certificate[] certs = this.keyStore.getCertificateChain(alias);
            if (certs == null) {
                return null;
            }
            final X509Certificate[] x509Certificates = new X509Certificate[certs.length];
            int i = 0;
            for (final Certificate cert : certs) {
                x509Certificates[i++] = (X509Certificate)cert;
            }
            return x509Certificates;
        }
        catch (final Exception kse) {
            this.error = new PSQLException(GT.tr("Could not find a java cryptographic algorithm: X.509 CertificateFactory not available.", new Object[0]), PSQLState.CONNECTION_FAILURE, kse);
            return null;
        }
    }
    
    @Override
    public PrivateKey getPrivateKey(final String s) {
        try {
            this.loadKeyStore();
            final PasswordCallback pwdcb = new PasswordCallback(GT.tr("Enter SSL password: ", new Object[0]), false);
            this.cbh.handle(new Callback[] { pwdcb });
            final KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(pwdcb.getPassword());
            final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)this.keyStore.getEntry("user", protParam);
            if (pkEntry == null) {
                return null;
            }
            final PrivateKey myPrivateKey = pkEntry.getPrivateKey();
            return myPrivateKey;
        }
        catch (final Exception ioex) {
            this.error = new PSQLException(GT.tr("Could not read SSL key file {0}.", this.keyfile), PSQLState.CONNECTION_FAILURE, ioex);
            return null;
        }
    }
    
    private synchronized void loadKeyStore() throws Exception {
        if (this.keystoreLoaded) {
            return;
        }
        final PasswordCallback pwdcb = new PasswordCallback(GT.tr("Enter SSL password: ", new Object[0]), false);
        try {
            this.cbh.handle(new Callback[] { pwdcb });
        }
        catch (final UnsupportedCallbackException ucex) {
            if (this.cbh instanceof LibPQFactory.ConsoleCallbackHandler && "Console is not available".equals(ucex.getMessage())) {
                this.error = new PSQLException(GT.tr("Could not read password for SSL key file, console is not available.", new Object[0]), PSQLState.CONNECTION_FAILURE, ucex);
            }
            else {
                this.error = new PSQLException(GT.tr("Could not read password for SSL key file by callbackhandler {0}.", this.cbh.getClass().getName()), PSQLState.CONNECTION_FAILURE, ucex);
            }
        }
        this.keyStore.load(new FileInputStream(new File(this.keyfile)), pwdcb.getPassword());
        this.keystoreLoaded = true;
    }
}
