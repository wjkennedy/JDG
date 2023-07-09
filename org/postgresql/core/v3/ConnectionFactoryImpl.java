// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import org.postgresql.core.Tuple;
import org.postgresql.core.SetupQueryRunner;
import org.postgresql.jre7.sasl.ScramAuthenticator;
import org.postgresql.util.MD5Digest;
import org.postgresql.core.Utils;
import org.postgresql.util.ServerErrorMessage;
import org.postgresql.ssl.MakeSSL;
import org.postgresql.gss.MakeGSS;
import java.util.TimeZone;
import java.util.logging.LogRecord;
import org.postgresql.core.Version;
import org.postgresql.core.ServerVersion;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import org.postgresql.hostchooser.HostChooser;
import java.net.ConnectException;
import org.postgresql.hostchooser.GlobalHostStatusTracker;
import org.postgresql.hostchooser.CandidateHost;
import org.postgresql.hostchooser.HostStatus;
import java.util.HashMap;
import org.postgresql.hostchooser.HostChooserFactory;
import org.postgresql.core.SocketFactoryFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.util.internal.Nullness;
import org.postgresql.core.QueryExecutor;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.GSSEncMode;
import org.postgresql.jdbc.SslMode;
import org.postgresql.util.HostSpec;
import javax.net.SocketFactory;
import java.util.Properties;
import org.postgresql.sspi.ISSPIClient;
import org.postgresql.core.PGStream;
import java.util.logging.Logger;
import org.postgresql.core.ConnectionFactory;

public class ConnectionFactoryImpl extends ConnectionFactory
{
    private static final Logger LOGGER;
    private static final int AUTH_REQ_OK = 0;
    private static final int AUTH_REQ_KRB4 = 1;
    private static final int AUTH_REQ_KRB5 = 2;
    private static final int AUTH_REQ_PASSWORD = 3;
    private static final int AUTH_REQ_CRYPT = 4;
    private static final int AUTH_REQ_MD5 = 5;
    private static final int AUTH_REQ_SCM = 6;
    private static final int AUTH_REQ_GSS = 7;
    private static final int AUTH_REQ_GSS_CONTINUE = 8;
    private static final int AUTH_REQ_SSPI = 9;
    private static final int AUTH_REQ_SASL = 10;
    private static final int AUTH_REQ_SASL_CONTINUE = 11;
    private static final int AUTH_REQ_SASL_FINAL = 12;
    
    private ISSPIClient createSSPI(final PGStream pgStream, final String spnServiceClass, final boolean enableNegotiate) {
        try {
            final Class<ISSPIClient> c = (Class<ISSPIClient>)Class.forName("org.postgresql.sspi.SSPIClient");
            return c.getDeclaredConstructor(PGStream.class, String.class, Boolean.TYPE).newInstance(pgStream, spnServiceClass, enableNegotiate);
        }
        catch (final Exception e) {
            throw new IllegalStateException("Unable to load org.postgresql.sspi.SSPIClient. Please check that SSPIClient is included in your pgjdbc distribution.", e);
        }
    }
    
    private PGStream tryConnect(final String user, final String database, final Properties info, final SocketFactory socketFactory, final HostSpec hostSpec, final SslMode sslMode, final GSSEncMode gssEncMode) throws SQLException, IOException {
        final int connectTimeout = PGProperty.CONNECT_TIMEOUT.getInt(info) * 1000;
        PGStream newStream = new PGStream(socketFactory, hostSpec, connectTimeout);
        final int socketTimeout = PGProperty.SOCKET_TIMEOUT.getInt(info);
        if (socketTimeout > 0) {
            newStream.setNetworkTimeout(socketTimeout * 1000);
        }
        final String maxResultBuffer = PGProperty.MAX_RESULT_BUFFER.get(info);
        newStream.setMaxResultBuffer(maxResultBuffer);
        final boolean requireTCPKeepAlive = PGProperty.TCP_KEEP_ALIVE.getBoolean(info);
        newStream.getSocket().setKeepAlive(requireTCPKeepAlive);
        final int receiveBufferSize = PGProperty.RECEIVE_BUFFER_SIZE.getInt(info);
        if (receiveBufferSize > -1) {
            if (receiveBufferSize > 0) {
                newStream.getSocket().setReceiveBufferSize(receiveBufferSize);
            }
            else {
                ConnectionFactoryImpl.LOGGER.log(Level.WARNING, "Ignore invalid value for receiveBufferSize: {0}", receiveBufferSize);
            }
        }
        final int sendBufferSize = PGProperty.SEND_BUFFER_SIZE.getInt(info);
        if (sendBufferSize > -1) {
            if (sendBufferSize > 0) {
                newStream.getSocket().setSendBufferSize(sendBufferSize);
            }
            else {
                ConnectionFactoryImpl.LOGGER.log(Level.WARNING, "Ignore invalid value for sendBufferSize: {0}", sendBufferSize);
            }
        }
        if (ConnectionFactoryImpl.LOGGER.isLoggable(Level.FINE)) {
            ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Receive Buffer Size is {0}", newStream.getSocket().getReceiveBufferSize());
            ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Send Buffer Size is {0}", newStream.getSocket().getSendBufferSize());
        }
        newStream = this.enableGSSEncrypted(newStream, gssEncMode, hostSpec.getHost(), user, info, connectTimeout);
        if (!newStream.isGssEncrypted()) {
            newStream = this.enableSSL(newStream, sslMode, info, connectTimeout);
        }
        if (socketTimeout > 0) {
            newStream.setNetworkTimeout(socketTimeout * 1000);
        }
        final List<String[]> paramList = this.getParametersForStartup(user, database, info);
        this.sendStartupPacket(newStream, paramList);
        this.doAuthentication(newStream, hostSpec.getHost(), user, info);
        return newStream;
    }
    
    @Override
    public QueryExecutor openConnectionImpl(final HostSpec[] hostSpecs, final String user, final String database, final Properties info) throws SQLException {
        final SslMode sslMode = SslMode.of(info);
        final GSSEncMode gssEncMode = GSSEncMode.of(info);
        final String targetServerTypeStr = Nullness.castNonNull(PGProperty.TARGET_SERVER_TYPE.get(info));
        HostRequirement targetServerType;
        try {
            targetServerType = HostRequirement.getTargetServerType(targetServerTypeStr);
        }
        catch (final IllegalArgumentException ex) {
            throw new PSQLException(GT.tr("Invalid targetServerType value: {0}", targetServerTypeStr), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
        }
        final SocketFactory socketFactory = SocketFactoryFactory.getSocketFactory(info);
        final HostChooser hostChooser = HostChooserFactory.createHostChooser(hostSpecs, targetServerType, info);
        final Iterator<CandidateHost> hostIter = hostChooser.iterator();
        final Map<HostSpec, HostStatus> knownStates = new HashMap<HostSpec, HostStatus>();
        while (hostIter.hasNext()) {
            final CandidateHost candidateHost = hostIter.next();
            final HostSpec hostSpec = candidateHost.hostSpec;
            ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Trying to establish a protocol version 3 connection to {0}", hostSpec);
            final HostStatus knownStatus = knownStates.get(hostSpec);
            if (knownStatus == null || candidateHost.targetServerType.allowConnectingTo(knownStatus)) {
                PGStream newStream = null;
                try {
                    try {
                        newStream = this.tryConnect(user, database, info, socketFactory, hostSpec, sslMode, gssEncMode);
                    }
                    catch (final SQLException e) {
                        if (sslMode == SslMode.PREFER && PSQLState.INVALID_AUTHORIZATION_SPECIFICATION.getState().equals(e.getSQLState())) {
                            Throwable ex2 = null;
                            try {
                                newStream = this.tryConnect(user, database, info, socketFactory, hostSpec, SslMode.DISABLE, gssEncMode);
                                ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Downgraded to non-encrypted connection for host {0}", hostSpec);
                            }
                            catch (final SQLException ee) {
                                ex2 = ee;
                            }
                            catch (final IOException ee2) {
                                ex2 = ee2;
                            }
                            if (ex2 != null) {
                                log(Level.FINE, "sslMode==PREFER, however non-SSL connection failed as well", ex2, new Object[0]);
                                e.addSuppressed(ex2);
                                throw e;
                            }
                        }
                        else {
                            if (sslMode != SslMode.ALLOW || !PSQLState.INVALID_AUTHORIZATION_SPECIFICATION.getState().equals(e.getSQLState())) {
                                throw e;
                            }
                            Throwable ex2 = null;
                            try {
                                newStream = this.tryConnect(user, database, info, socketFactory, hostSpec, SslMode.REQUIRE, gssEncMode);
                                ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Upgraded to encrypted connection for host {0}", hostSpec);
                            }
                            catch (final SQLException ee) {
                                ex2 = ee;
                            }
                            catch (final IOException ee2) {
                                ex2 = ee2;
                            }
                            if (ex2 != null) {
                                log(Level.FINE, "sslMode==ALLOW, however SSL connection failed as well", ex2, new Object[0]);
                                e.addSuppressed(ex2);
                                throw e;
                            }
                        }
                    }
                    final int cancelSignalTimeout = PGProperty.CANCEL_SIGNAL_TIMEOUT.getInt(info) * 1000;
                    Nullness.castNonNull(newStream);
                    final QueryExecutor queryExecutor = new QueryExecutorImpl(newStream, user, database, cancelSignalTimeout, info);
                    HostStatus hostStatus = HostStatus.ConnectOK;
                    if (candidateHost.targetServerType != HostRequirement.any) {
                        hostStatus = (this.isPrimary(queryExecutor) ? HostStatus.Primary : HostStatus.Secondary);
                    }
                    GlobalHostStatusTracker.reportHostStatus(hostSpec, hostStatus);
                    knownStates.put(hostSpec, hostStatus);
                    if (!candidateHost.targetServerType.allowConnectingTo(hostStatus)) {
                        queryExecutor.close();
                        continue;
                    }
                    this.runInitialQueries(queryExecutor, info);
                    return queryExecutor;
                }
                catch (final ConnectException cex) {
                    GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail);
                    knownStates.put(hostSpec, HostStatus.ConnectFail);
                    if (hostIter.hasNext()) {
                        log(Level.FINE, "ConnectException occurred while connecting to {0}", cex, hostSpec);
                        continue;
                    }
                    throw new PSQLException(GT.tr("Connection to {0} refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.", hostSpec), PSQLState.CONNECTION_UNABLE_TO_CONNECT, cex);
                }
                catch (final IOException ioe) {
                    this.closeStream(newStream);
                    GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail);
                    knownStates.put(hostSpec, HostStatus.ConnectFail);
                    if (hostIter.hasNext()) {
                        log(Level.FINE, "IOException occurred while connecting to {0}", ioe, hostSpec);
                        continue;
                    }
                    throw new PSQLException(GT.tr("The connection attempt failed.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT, ioe);
                }
                catch (final SQLException se) {
                    this.closeStream(newStream);
                    GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail);
                    knownStates.put(hostSpec, HostStatus.ConnectFail);
                    if (hostIter.hasNext()) {
                        log(Level.FINE, "SQLException occurred while connecting to {0}", se, hostSpec);
                        continue;
                    }
                    throw se;
                }
                break;
            }
            if (!ConnectionFactoryImpl.LOGGER.isLoggable(Level.FINER)) {
                continue;
            }
            ConnectionFactoryImpl.LOGGER.log(Level.FINER, "Known status of host {0} is {1}, and required status was {2}. Will try next host", new Object[] { hostSpec, knownStatus, candidateHost.targetServerType });
        }
        throw new PSQLException(GT.tr("Could not find a server with specified targetServerType: {0}", targetServerType), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
    }
    
    private List<String[]> getParametersForStartup(final String user, final String database, final Properties info) {
        final List<String[]> paramList = new ArrayList<String[]>();
        paramList.add(new String[] { "user", user });
        paramList.add(new String[] { "database", database });
        paramList.add(new String[] { "client_encoding", "UTF8" });
        paramList.add(new String[] { "DateStyle", "ISO" });
        paramList.add(new String[] { "TimeZone", createPostgresTimeZone() });
        final Version assumeVersion = ServerVersion.from(PGProperty.ASSUME_MIN_SERVER_VERSION.get(info));
        if (assumeVersion.getVersionNum() >= ServerVersion.v9_0.getVersionNum()) {
            paramList.add(new String[] { "extra_float_digits", "3" });
            final String appName = PGProperty.APPLICATION_NAME.get(info);
            if (appName != null) {
                paramList.add(new String[] { "application_name", appName });
            }
        }
        else {
            paramList.add(new String[] { "extra_float_digits", "2" });
        }
        final String replication = PGProperty.REPLICATION.get(info);
        if (replication != null && assumeVersion.getVersionNum() >= ServerVersion.v9_4.getVersionNum()) {
            paramList.add(new String[] { "replication", replication });
        }
        final String currentSchema = PGProperty.CURRENT_SCHEMA.get(info);
        if (currentSchema != null) {
            paramList.add(new String[] { "search_path", currentSchema });
        }
        final String options = PGProperty.OPTIONS.get(info);
        if (options != null) {
            paramList.add(new String[] { "options", options });
        }
        return paramList;
    }
    
    private static void log(final Level level, final String msg, final Throwable thrown, final Object... params) {
        if (!ConnectionFactoryImpl.LOGGER.isLoggable(level)) {
            return;
        }
        final LogRecord rec = new LogRecord(level, msg);
        rec.setLoggerName(ConnectionFactoryImpl.LOGGER.getName());
        rec.setParameters(params);
        rec.setThrown(thrown);
        ConnectionFactoryImpl.LOGGER.log(rec);
    }
    
    private static String createPostgresTimeZone() {
        final String tz = TimeZone.getDefault().getID();
        if (tz.length() <= 3 || !tz.startsWith("GMT")) {
            return tz;
        }
        final char sign = tz.charAt(3);
        String start = null;
        switch (sign) {
            case '+': {
                start = "GMT-";
                break;
            }
            case '-': {
                start = "GMT+";
                break;
            }
            default: {
                return tz;
            }
        }
        return start + tz.substring(4);
    }
    
    private PGStream enableGSSEncrypted(final PGStream pgStream, final GSSEncMode gssEncMode, final String host, final String user, final Properties info, final int connectTimeout) throws IOException, PSQLException {
        if (gssEncMode == GSSEncMode.DISABLE) {
            return pgStream;
        }
        if (gssEncMode == GSSEncMode.ALLOW) {
            return pgStream;
        }
        final String password = PGProperty.PASSWORD.get(info);
        ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " FE=> GSSENCRequest");
        pgStream.sendInteger4(8);
        pgStream.sendInteger2(1234);
        pgStream.sendInteger2(5680);
        pgStream.flush();
        final int beresp = pgStream.receiveChar();
        switch (beresp) {
            case 69: {
                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE GSSEncrypted Error");
                if (gssEncMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support GSS Encoding.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                pgStream.close();
                return new PGStream(pgStream.getSocketFactory(), pgStream.getHostSpec(), connectTimeout);
            }
            case 78: {
                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE GSSEncrypted Refused");
                if (gssEncMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support GSS Encryption.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                return pgStream;
            }
            case 71: {
                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE GSSEncryptedOk");
                try {
                    MakeGSS.authenticate(true, pgStream, host, user, password, PGProperty.JAAS_APPLICATION_NAME.get(info), PGProperty.KERBEROS_SERVER_NAME.get(info), false, PGProperty.JAAS_LOGIN.getBoolean(info), PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info));
                    return pgStream;
                }
                catch (final PSQLException ex) {
                    if (gssEncMode == GSSEncMode.PREFER) {
                        return new PGStream(pgStream, connectTimeout);
                    }
                }
                break;
            }
        }
        throw new PSQLException(GT.tr("An error occurred while setting up the GSS Encoded connection.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
    }
    
    private PGStream enableSSL(final PGStream pgStream, final SslMode sslMode, final Properties info, final int connectTimeout) throws IOException, PSQLException {
        if (sslMode == SslMode.DISABLE) {
            return pgStream;
        }
        if (sslMode == SslMode.ALLOW) {
            return pgStream;
        }
        ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " FE=> SSLRequest");
        pgStream.sendInteger4(8);
        pgStream.sendInteger2(1234);
        pgStream.sendInteger2(5679);
        pgStream.flush();
        final int beresp = pgStream.receiveChar();
        switch (beresp) {
            case 69: {
                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE SSLError");
                if (sslMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support SSL.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                return new PGStream(pgStream, connectTimeout);
            }
            case 78: {
                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE SSLRefused");
                if (sslMode.requireEncryption()) {
                    throw new PSQLException(GT.tr("The server does not support SSL.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                }
                return pgStream;
            }
            case 83: {
                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE SSLOk");
                MakeSSL.convert(pgStream, info);
                return pgStream;
            }
            default: {
                throw new PSQLException(GT.tr("An error occurred while setting up the SSL connection.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
            }
        }
    }
    
    private void sendStartupPacket(final PGStream pgStream, final List<String[]> params) throws IOException {
        if (ConnectionFactoryImpl.LOGGER.isLoggable(Level.FINEST)) {
            final StringBuilder details = new StringBuilder();
            for (int i = 0; i < params.size(); ++i) {
                if (i != 0) {
                    details.append(", ");
                }
                details.append(params.get(i)[0]);
                details.append("=");
                details.append(params.get(i)[1]);
            }
            ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " FE=> StartupPacket({0})", details);
        }
        int length = 8;
        final byte[][] encodedParams = new byte[params.size() * 2][];
        for (int j = 0; j < params.size(); ++j) {
            encodedParams[j * 2] = params.get(j)[0].getBytes("UTF-8");
            encodedParams[j * 2 + 1] = params.get(j)[1].getBytes("UTF-8");
            length += encodedParams[j * 2].length + 1 + encodedParams[j * 2 + 1].length + 1;
        }
        ++length;
        pgStream.sendInteger4(length);
        pgStream.sendInteger2(3);
        pgStream.sendInteger2(0);
        for (final byte[] encodedParam : encodedParams) {
            pgStream.send(encodedParam);
            pgStream.sendChar(0);
        }
        pgStream.sendChar(0);
        pgStream.flush();
    }
    
    private void doAuthentication(final PGStream pgStream, final String host, final String user, final Properties info) throws IOException, SQLException {
        final String password = PGProperty.PASSWORD.get(info);
        ISSPIClient sspiClient = null;
        ScramAuthenticator scramAuthenticator = null;
        try {
            while (true) {
                final int beresp = pgStream.receiveChar();
                switch (beresp) {
                    case 69: {
                        final int elen = pgStream.receiveInteger4();
                        final ServerErrorMessage errorMsg = new ServerErrorMessage(pgStream.receiveErrorString(elen - 4));
                        ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE ErrorMessage({0})", errorMsg);
                        throw new PSQLException(errorMsg, PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info));
                    }
                    case 82: {
                        final int msgLen = pgStream.receiveInteger4();
                        final int areq = pgStream.receiveInteger4();
                        switch (areq) {
                            case 5: {
                                final byte[] md5Salt = pgStream.receive(4);
                                if (ConnectionFactoryImpl.LOGGER.isLoggable(Level.FINEST)) {
                                    ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE AuthenticationReqMD5(salt={0})", Utils.toHexString(md5Salt));
                                }
                                if (password == null) {
                                    throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                                }
                                final byte[] digest = MD5Digest.encode(user.getBytes("UTF-8"), password.getBytes("UTF-8"), md5Salt);
                                if (ConnectionFactoryImpl.LOGGER.isLoggable(Level.FINEST)) {
                                    ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " FE=> Password(md5digest={0})", new String(digest, "US-ASCII"));
                                }
                                pgStream.sendChar(112);
                                pgStream.sendInteger4(4 + digest.length + 1);
                                pgStream.send(digest);
                                pgStream.sendChar(0);
                                pgStream.flush();
                                continue;
                            }
                            case 3: {
                                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, "<=BE AuthenticationReqPassword");
                                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " FE=> Password(password=<not shown>)");
                                if (password == null) {
                                    throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided.", new Object[0]), PSQLState.CONNECTION_REJECTED);
                                }
                                final byte[] encodedPassword = password.getBytes("UTF-8");
                                pgStream.sendChar(112);
                                pgStream.sendInteger4(4 + encodedPassword.length + 1);
                                pgStream.send(encodedPassword);
                                pgStream.sendChar(0);
                                pgStream.flush();
                                continue;
                            }
                            case 7:
                            case 9: {
                                final String gsslib = PGProperty.GSS_LIB.get(info);
                                final boolean usespnego = PGProperty.USE_SPNEGO.getBoolean(info);
                                boolean useSSPI = false;
                                if ("gssapi".equals(gsslib)) {
                                    ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Using JSSE GSSAPI, param gsslib=gssapi");
                                }
                                else if (areq == 7 && !"sspi".equals(gsslib)) {
                                    ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Using JSSE GSSAPI, gssapi requested by server and gsslib=sspi not forced");
                                }
                                else {
                                    sspiClient = this.createSSPI(pgStream, PGProperty.SSPI_SERVICE_CLASS.get(info), areq == 9 || (areq == 7 && usespnego));
                                    useSSPI = sspiClient.isSSPISupported();
                                    ConnectionFactoryImpl.LOGGER.log(Level.FINE, "SSPI support detected: {0}", useSSPI);
                                    if (!useSSPI) {
                                        sspiClient = null;
                                        if ("sspi".equals(gsslib)) {
                                            throw new PSQLException("SSPI forced with gsslib=sspi, but SSPI not available; set loglevel=2 for details", PSQLState.CONNECTION_UNABLE_TO_CONNECT);
                                        }
                                    }
                                    if (ConnectionFactoryImpl.LOGGER.isLoggable(Level.FINE)) {
                                        ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Using SSPI: {0}, gsslib={1} and SSPI support detected", new Object[] { useSSPI, gsslib });
                                    }
                                }
                                if (useSSPI) {
                                    Nullness.castNonNull(sspiClient).startSSPI();
                                    continue;
                                }
                                MakeGSS.authenticate(false, pgStream, host, user, password, PGProperty.JAAS_APPLICATION_NAME.get(info), PGProperty.KERBEROS_SERVER_NAME.get(info), usespnego, PGProperty.JAAS_LOGIN.getBoolean(info), PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(info));
                                continue;
                            }
                            case 8: {
                                Nullness.castNonNull(sspiClient).continueSSPI(msgLen - 8);
                                continue;
                            }
                            case 10: {
                                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE AuthenticationSASL");
                                scramAuthenticator = new ScramAuthenticator(user, Nullness.castNonNull(password), pgStream);
                                scramAuthenticator.processServerMechanismsAndInit();
                                scramAuthenticator.sendScramClientFirstMessage();
                                continue;
                            }
                            case 11: {
                                Nullness.castNonNull(scramAuthenticator).processServerFirstMessage(msgLen - 4 - 4);
                                continue;
                            }
                            case 12: {
                                Nullness.castNonNull(scramAuthenticator).verifyServerSignature(msgLen - 4 - 4);
                                continue;
                            }
                            case 0: {
                                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE AuthenticationOk");
                                break;
                            }
                            default: {
                                ConnectionFactoryImpl.LOGGER.log(Level.FINEST, " <=BE AuthenticationReq (unsupported type {0})", areq);
                                throw new PSQLException(GT.tr("The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.", areq), PSQLState.CONNECTION_REJECTED);
                            }
                        }
                        continue;
                    }
                    default: {
                        throw new PSQLException(GT.tr("Protocol error.  Session setup failed.", new Object[0]), PSQLState.PROTOCOL_VIOLATION);
                    }
                }
            }
        }
        finally {
            if (sspiClient != null) {
                try {
                    sspiClient.dispose();
                }
                catch (final RuntimeException ex) {
                    ConnectionFactoryImpl.LOGGER.log(Level.FINE, "Unexpected error during SSPI context disposal", ex);
                }
            }
        }
    }
    
    private void runInitialQueries(final QueryExecutor queryExecutor, final Properties info) throws SQLException {
        final String assumeMinServerVersion = PGProperty.ASSUME_MIN_SERVER_VERSION.get(info);
        if (Utils.parseServerVersionStr(assumeMinServerVersion) >= ServerVersion.v9_0.getVersionNum()) {
            return;
        }
        final int dbVersion = queryExecutor.getServerVersionNum();
        if (dbVersion >= ServerVersion.v9_0.getVersionNum()) {
            SetupQueryRunner.run(queryExecutor, "SET extra_float_digits = 3", false);
        }
        final String appName = PGProperty.APPLICATION_NAME.get(info);
        if (appName != null && dbVersion >= ServerVersion.v9_0.getVersionNum()) {
            final StringBuilder sql = new StringBuilder();
            sql.append("SET application_name = '");
            Utils.escapeLiteral(sql, appName, queryExecutor.getStandardConformingStrings());
            sql.append("'");
            SetupQueryRunner.run(queryExecutor, sql.toString(), false);
        }
    }
    
    private boolean isPrimary(final QueryExecutor queryExecutor) throws SQLException, IOException {
        final Tuple results = SetupQueryRunner.run(queryExecutor, "show transaction_read_only", true);
        final Tuple nonNullResults = Nullness.castNonNull(results);
        final String value = queryExecutor.getEncoding().decode(Nullness.castNonNull(nonNullResults.get(0)));
        return value.equalsIgnoreCase("off");
    }
    
    static {
        LOGGER = Logger.getLogger(ConnectionFactoryImpl.class.getName());
    }
}
