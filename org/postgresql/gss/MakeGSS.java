// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.gss;

import java.security.PrivilegedAction;
import java.util.Set;
import java.io.IOException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import org.ietf.jgss.GSSCredential;
import javax.security.auth.Subject;
import java.security.AccessController;
import java.util.logging.Level;
import org.postgresql.core.PGStream;
import java.util.logging.Logger;

public class MakeGSS
{
    private static final Logger LOGGER;
    
    public static void authenticate(final boolean encrypted, final PGStream pgStream, final String host, final String user, final String password, String jaasApplicationName, String kerberosServerName, final boolean useSpnego, final boolean jaasLogin, final boolean logServerErrorDetail) throws IOException, PSQLException {
        MakeGSS.LOGGER.log(Level.FINEST, " <=BE AuthenticationReqGSS");
        if (jaasApplicationName == null) {
            jaasApplicationName = "pgjdbc";
        }
        if (kerberosServerName == null) {
            kerberosServerName = "postgres";
        }
        Exception result;
        try {
            boolean performAuthentication = jaasLogin;
            GSSCredential gssCredential = null;
            Subject sub = Subject.getSubject(AccessController.getContext());
            if (sub != null) {
                final Set<GSSCredential> gssCreds = sub.getPrivateCredentials(GSSCredential.class);
                if (gssCreds != null && !gssCreds.isEmpty()) {
                    gssCredential = gssCreds.iterator().next();
                    performAuthentication = false;
                }
            }
            if (performAuthentication) {
                final LoginContext lc = new LoginContext(jaasApplicationName, new GSSCallbackHandler(user, password));
                lc.login();
                sub = lc.getSubject();
            }
            if (encrypted) {
                final PrivilegedAction<Exception> action = new GssEncAction(pgStream, gssCredential, host, user, kerberosServerName, useSpnego, logServerErrorDetail);
                result = Subject.doAs(sub, action);
            }
            else {
                final PrivilegedAction<Exception> action = new GssAction(pgStream, gssCredential, host, user, kerberosServerName, useSpnego, logServerErrorDetail);
                result = Subject.doAs(sub, action);
            }
        }
        catch (final Exception e) {
            throw new PSQLException(GT.tr("GSS Authentication failed", new Object[0]), PSQLState.CONNECTION_FAILURE, e);
        }
        if (result instanceof IOException) {
            throw (IOException)result;
        }
        if (result instanceof PSQLException) {
            throw (PSQLException)result;
        }
        if (result != null) {
            throw new PSQLException(GT.tr("GSS Authentication failed", new Object[0]), PSQLState.CONNECTION_FAILURE, result);
        }
    }
    
    static {
        LOGGER = Logger.getLogger(MakeGSS.class.getName());
    }
}
