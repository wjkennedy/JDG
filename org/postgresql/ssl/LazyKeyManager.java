// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.spec.InvalidKeySpecException;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;
import java.io.RandomAccessFile;
import java.security.cert.Certificate;
import java.util.Collection;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.cert.CertificateException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.security.cert.CertificateFactory;
import javax.security.auth.x500.X500Principal;
import java.net.Socket;
import java.security.Principal;
import org.postgresql.util.PSQLException;
import javax.security.auth.callback.CallbackHandler;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;

public class LazyKeyManager implements X509KeyManager
{
    private X509Certificate[] cert;
    private PrivateKey key;
    private final String certfile;
    private final String keyfile;
    private final CallbackHandler cbh;
    private final boolean defaultfile;
    private PSQLException error;
    
    public LazyKeyManager(final String certfile, final String keyfile, final CallbackHandler cbh, final boolean defaultfile) {
        this.cert = null;
        this.key = null;
        this.error = null;
        this.certfile = certfile;
        this.keyfile = keyfile;
        this.cbh = cbh;
        this.defaultfile = defaultfile;
    }
    
    public void throwKeyManagerException() throws PSQLException {
        if (this.error != null) {
            throw this.error;
        }
    }
    
    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        if (this.certfile == null) {
            return null;
        }
        if (issuers == null || issuers.length == 0) {
            return "user";
        }
        final X509Certificate[] certchain = this.getCertificateChain("user");
        if (certchain == null) {
            return null;
        }
        final X500Principal ourissuer = certchain[certchain.length - 1].getIssuerX500Principal();
        boolean found = false;
        for (final Principal issuer : issuers) {
            if (ourissuer.equals(issuer)) {
                found = true;
            }
        }
        return found ? "user" : null;
    }
    
    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        return null;
    }
    
    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        if (this.cert == null && this.certfile != null) {
            CertificateFactory cf;
            try {
                cf = CertificateFactory.getInstance("X.509");
            }
            catch (final CertificateException ex) {
                this.error = new PSQLException(GT.tr("Could not find a java cryptographic algorithm: X.509 CertificateFactory not available.", new Object[0]), PSQLState.CONNECTION_FAILURE, ex);
                return null;
            }
            FileInputStream certfileStream = null;
            Collection<? extends Certificate> certs;
            try {
                certfileStream = new FileInputStream(this.certfile);
                certs = cf.generateCertificates(certfileStream);
            }
            catch (final FileNotFoundException ioex) {
                if (!this.defaultfile) {
                    this.error = new PSQLException(GT.tr("Could not open SSL certificate file {0}.", this.certfile), PSQLState.CONNECTION_FAILURE, ioex);
                }
                return null;
            }
            catch (final CertificateException gsex) {
                this.error = new PSQLException(GT.tr("Loading the SSL certificate {0} into a KeyManager failed.", this.certfile), PSQLState.CONNECTION_FAILURE, gsex);
                return null;
            }
            finally {
                if (certfileStream != null) {
                    try {
                        certfileStream.close();
                    }
                    catch (final IOException ioex2) {
                        if (!this.defaultfile) {
                            this.error = new PSQLException(GT.tr("Could not close SSL certificate file {0}.", this.certfile), PSQLState.CONNECTION_FAILURE, ioex2);
                        }
                    }
                }
            }
            this.cert = certs.toArray(new X509Certificate[0]);
        }
        return this.cert;
    }
    
    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        final String alias = this.chooseClientAlias(new String[] { keyType }, issuers, null);
        return (alias == null) ? new String[0] : new String[] { alias };
    }
    
    private static byte[] readFileFully(final String path) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(path, "r");
        try {
            final byte[] ret = new byte[(int)raf.length()];
            raf.readFully(ret);
            return ret;
        }
        finally {
            raf.close();
        }
    }
    
    @Override
    public PrivateKey getPrivateKey(final String alias) {
        try {
            if (this.key == null && this.keyfile != null) {
                final X509Certificate[] cert = this.getCertificateChain("user");
                if (cert == null || cert.length == 0) {
                    return null;
                }
                byte[] keydata;
                try {
                    keydata = readFileFully(this.keyfile);
                }
                catch (final FileNotFoundException ex) {
                    if (!this.defaultfile) {
                        throw ex;
                    }
                    return null;
                }
                final KeyFactory kf = KeyFactory.getInstance(cert[0].getPublicKey().getAlgorithm());
                try {
                    final KeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keydata);
                    this.key = kf.generatePrivate(pkcs8KeySpec);
                }
                catch (final InvalidKeySpecException ex2) {
                    final EncryptedPrivateKeyInfo ePKInfo = new EncryptedPrivateKeyInfo(keydata);
                    Cipher cipher;
                    try {
                        cipher = Cipher.getInstance(ePKInfo.getAlgName());
                    }
                    catch (final NoSuchPaddingException npex) {
                        throw new NoSuchAlgorithmException(npex.getMessage(), npex);
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
                        return null;
                    }
                    try {
                        final PBEKeySpec pbeKeySpec = new PBEKeySpec(pwdcb.getPassword());
                        pwdcb.clearPassword();
                        final SecretKeyFactory skFac = SecretKeyFactory.getInstance(ePKInfo.getAlgName());
                        final Key pbeKey = skFac.generateSecret(pbeKeySpec);
                        final AlgorithmParameters algParams = ePKInfo.getAlgParameters();
                        cipher.init(2, pbeKey, algParams);
                        final KeySpec pkcs8KeySpec2 = ePKInfo.getKeySpec(cipher);
                        this.key = kf.generatePrivate(pkcs8KeySpec2);
                    }
                    catch (final GeneralSecurityException ikex) {
                        this.error = new PSQLException(GT.tr("Could not decrypt SSL key file {0}.", this.keyfile), PSQLState.CONNECTION_FAILURE, ikex);
                        return null;
                    }
                }
            }
        }
        catch (final IOException ioex) {
            this.error = new PSQLException(GT.tr("Could not read SSL key file {0}.", this.keyfile), PSQLState.CONNECTION_FAILURE, ioex);
        }
        catch (final NoSuchAlgorithmException ex3) {
            this.error = new PSQLException(GT.tr("Could not find a java cryptographic algorithm: {0}.", ex3.getMessage()), PSQLState.CONNECTION_FAILURE, ex3);
            return null;
        }
        return this.key;
    }
    
    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        return new String[0];
    }
}
