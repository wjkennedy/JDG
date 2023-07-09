// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.gss;

import org.ietf.jgss.GSSContext;
import java.io.IOException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import java.util.logging.Level;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSCredential;
import org.postgresql.core.PGStream;
import java.util.logging.Logger;
import java.security.PrivilegedAction;

class GssAction implements PrivilegedAction<Exception>
{
    private static final Logger LOGGER;
    private final PGStream pgStream;
    private final String host;
    private final String user;
    private final String kerberosServerName;
    private final boolean useSpnego;
    private final GSSCredential clientCredentials;
    private final boolean logServerErrorDetail;
    
    GssAction(final PGStream pgStream, final GSSCredential clientCredentials, final String host, final String user, final String kerberosServerName, final boolean useSpnego, final boolean logServerErrorDetail) {
        this.pgStream = pgStream;
        this.clientCredentials = clientCredentials;
        this.host = host;
        this.user = user;
        this.kerberosServerName = kerberosServerName;
        this.useSpnego = useSpnego;
        this.logServerErrorDetail = logServerErrorDetail;
    }
    
    private static boolean hasSpnegoSupport(final GSSManager manager) throws GSSException {
        final Oid spnego = new Oid("1.3.6.1.5.5.2");
        final Oid[] mechs2;
        final Oid[] mechs = mechs2 = manager.getMechs();
        for (final Oid mech : mechs2) {
            if (mech.equals(spnego)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Exception run() {
        try {
            final GSSManager manager = GSSManager.getInstance();
            GSSCredential clientCreds = null;
            final Oid[] desiredMechs = { null };
            if (this.clientCredentials == null) {
                if (this.useSpnego && hasSpnegoSupport(manager)) {
                    desiredMechs[0] = new Oid("1.3.6.1.5.5.2");
                }
                else {
                    desiredMechs[0] = new Oid("1.2.840.113554.1.2.2");
                }
                final GSSName clientName = manager.createName(this.user, GSSName.NT_USER_NAME);
                clientCreds = manager.createCredential(clientName, 28800, desiredMechs, 1);
            }
            else {
                desiredMechs[0] = new Oid("1.2.840.113554.1.2.2");
                clientCreds = this.clientCredentials;
            }
            final GSSName serverName = manager.createName(this.kerberosServerName + "@" + this.host, GSSName.NT_HOSTBASED_SERVICE);
            final GSSContext secContext = manager.createContext(serverName, desiredMechs[0], clientCreds, 0);
            secContext.requestMutualAuth(true);
            byte[] inToken = new byte[0];
            byte[] outToken = null;
            boolean established = false;
            while (!established) {
                outToken = secContext.initSecContext(inToken, 0, inToken.length);
                if (outToken != null) {
                    GssAction.LOGGER.log(Level.FINEST, " FE=> Password(GSS Authentication Token)");
                    this.pgStream.sendChar(112);
                    this.pgStream.sendInteger4(4 + outToken.length);
                    this.pgStream.send(outToken);
                    this.pgStream.flush();
                }
                if (!secContext.isEstablished()) {
                    final int response = this.pgStream.receiveChar();
                    switch (response) {
                        case 69: {
                            final int elen = this.pgStream.receiveInteger4();
                            final ServerErrorMessage errorMsg = new ServerErrorMessage(this.pgStream.receiveErrorString(elen - 4));
                            GssAction.LOGGER.log(Level.FINEST, " <=BE ErrorMessage({0})", errorMsg);
                            return new PSQLException(errorMsg, this.logServerErrorDetail);
                        }
                        case 82: {
                            GssAction.LOGGER.log(Level.FINEST, " <=BE AuthenticationGSSContinue");
                            final int len = this.pgStream.receiveInteger4();
                            final int type = this.pgStream.receiveInteger4();
                            inToken = this.pgStream.receive(len - 8);
                            continue;
                        }
                        default: {
                            return new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
                        }
                    }
                }
                else {
                    established = true;
                }
            }
        }
        catch (final IOException e) {
            return e;
        }
        catch (final GSSException gsse) {
            return new PSQLException(GT.tr("GSS Authentication failed", new Object[0]), PSQLState.CONNECTION_FAILURE, gsse);
        }
        return null;
    }
    
    static {
        LOGGER = Logger.getLogger(GssAction.class.getName());
    }
}
