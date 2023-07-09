// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import javax.net.ssl.HostnameVerifier;
import org.postgresql.PGProperty;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;
import org.postgresql.jdbc.SslMode;
import java.io.IOException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import javax.net.ssl.SSLSocket;
import org.postgresql.core.SocketFactoryFactory;
import java.util.logging.Level;
import java.util.Properties;
import org.postgresql.core.PGStream;
import java.util.logging.Logger;
import org.postgresql.util.ObjectFactory;

public class MakeSSL extends ObjectFactory
{
    private static final Logger LOGGER;
    
    public static void convert(final PGStream stream, final Properties info) throws PSQLException, IOException {
        MakeSSL.LOGGER.log(Level.FINE, "converting regular socket connection to ssl");
        final SSLSocketFactory factory = SocketFactoryFactory.getSslSocketFactory(info);
        SSLSocket newConnection;
        try {
            newConnection = (SSLSocket)factory.createSocket(stream.getSocket(), stream.getHostSpec().getHost(), stream.getHostSpec().getPort(), true);
            newConnection.setUseClientMode(true);
            newConnection.startHandshake();
        }
        catch (final IOException ex) {
            throw new PSQLException(GT.tr("SSL error: {0}", ex.getMessage()), PSQLState.CONNECTION_FAILURE, ex);
        }
        if (factory instanceof LibPQFactory) {
            ((LibPQFactory)factory).throwKeyManagerException();
        }
        final SslMode sslMode = SslMode.of(info);
        if (sslMode.verifyPeerName()) {
            verifyPeerName(stream, info, newConnection);
        }
        stream.changeSocket(newConnection);
    }
    
    private static void verifyPeerName(final PGStream stream, final Properties info, final SSLSocket newConnection) throws PSQLException {
        String sslhostnameverifier = PGProperty.SSL_HOSTNAME_VERIFIER.get(info);
        HostnameVerifier hvn;
        if (sslhostnameverifier == null) {
            hvn = PGjdbcHostnameVerifier.INSTANCE;
            sslhostnameverifier = "PgjdbcHostnameVerifier";
        }
        else {
            try {
                hvn = ObjectFactory.instantiate(HostnameVerifier.class, sslhostnameverifier, info, false, null);
            }
            catch (final Exception e) {
                throw new PSQLException(GT.tr("The HostnameVerifier class provided {0} could not be instantiated.", sslhostnameverifier), PSQLState.CONNECTION_FAILURE, e);
            }
        }
        if (hvn.verify(stream.getHostSpec().getHost(), newConnection.getSession())) {
            return;
        }
        throw new PSQLException(GT.tr("The hostname {0} could not be verified by hostnameverifier {1}.", stream.getHostSpec().getHost(), sslhostnameverifier), PSQLState.CONNECTION_FAILURE);
    }
    
    static {
        LOGGER = Logger.getLogger(MakeSSL.class.getName());
    }
}
