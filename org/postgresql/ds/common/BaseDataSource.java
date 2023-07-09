// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ds.common;

import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PreferQueryMode;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.Driver;
import org.postgresql.util.URLCoder;
import org.postgresql.util.ExpressionProperties;
import org.postgresql.util.internal.Nullness;
import org.postgresql.PGProperty;
import java.util.Arrays;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.Referenceable;
import javax.sql.CommonDataSource;

public abstract class BaseDataSource implements CommonDataSource, Referenceable
{
    private static final Logger LOGGER;
    private String[] serverNames;
    private String databaseName;
    private String user;
    private String password;
    private int[] portNumbers;
    private Properties properties;
    
    public BaseDataSource() {
        this.serverNames = new String[] { "localhost" };
        this.databaseName = "";
        this.portNumbers = new int[] { 0 };
        this.properties = new Properties();
    }
    
    public Connection getConnection() throws SQLException {
        return this.getConnection(this.user, this.password);
    }
    
    public Connection getConnection(final String user, final String password) throws SQLException {
        try {
            final Connection con = DriverManager.getConnection(this.getUrl(), user, password);
            if (BaseDataSource.LOGGER.isLoggable(Level.FINE)) {
                BaseDataSource.LOGGER.log(Level.FINE, "Created a {0} for {1} at {2}", new Object[] { this.getDescription(), user, this.getUrl() });
            }
            return con;
        }
        catch (final SQLException e) {
            BaseDataSource.LOGGER.log(Level.FINE, "Failed to create a {0} for {1} at {2}: {3}", new Object[] { this.getDescription(), user, this.getUrl(), e });
            throw e;
        }
    }
    
    @Override
    public PrintWriter getLogWriter() {
        return null;
    }
    
    @Override
    public void setLogWriter(final PrintWriter printWriter) {
    }
    
    @Deprecated
    public String getServerName() {
        return this.serverNames[0];
    }
    
    public String[] getServerNames() {
        return this.serverNames;
    }
    
    @Deprecated
    public void setServerName(final String serverName) {
        this.setServerNames(new String[] { serverName });
    }
    
    public void setServerNames(String[] serverNames) {
        if (serverNames == null || serverNames.length == 0) {
            this.serverNames = new String[] { "localhost" };
        }
        else {
            serverNames = serverNames.clone();
            for (int i = 0; i < serverNames.length; ++i) {
                final String serverName = serverNames[i];
                if (serverName == null || serverName.equals("")) {
                    serverNames[i] = "localhost";
                }
            }
            this.serverNames = serverNames;
        }
    }
    
    public String getDatabaseName() {
        return this.databaseName;
    }
    
    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }
    
    public abstract String getDescription();
    
    public String getUser() {
        return this.user;
    }
    
    public void setUser(final String user) {
        this.user = user;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(final String password) {
        this.password = password;
    }
    
    @Deprecated
    public int getPortNumber() {
        if (this.portNumbers == null || this.portNumbers.length == 0) {
            return 0;
        }
        return this.portNumbers[0];
    }
    
    public int[] getPortNumbers() {
        return this.portNumbers;
    }
    
    @Deprecated
    public void setPortNumber(final int portNumber) {
        this.setPortNumbers(new int[] { portNumber });
    }
    
    public void setPortNumbers(int[] portNumbers) {
        if (portNumbers == null || portNumbers.length == 0) {
            portNumbers = new int[] { 0 };
        }
        this.portNumbers = Arrays.copyOf(portNumbers, portNumbers.length);
    }
    
    public String getOptions() {
        return PGProperty.OPTIONS.get(this.properties);
    }
    
    public void setOptions(final String options) {
        PGProperty.OPTIONS.set(this.properties, options);
    }
    
    @Override
    public int getLoginTimeout() {
        return PGProperty.LOGIN_TIMEOUT.getIntNoCheck(this.properties);
    }
    
    @Override
    public void setLoginTimeout(final int loginTimeout) {
        PGProperty.LOGIN_TIMEOUT.set(this.properties, loginTimeout);
    }
    
    public int getConnectTimeout() {
        return PGProperty.CONNECT_TIMEOUT.getIntNoCheck(this.properties);
    }
    
    public void setConnectTimeout(final int connectTimeout) {
        PGProperty.CONNECT_TIMEOUT.set(this.properties, connectTimeout);
    }
    
    public int getProtocolVersion() {
        if (!PGProperty.PROTOCOL_VERSION.isPresent(this.properties)) {
            return 0;
        }
        return PGProperty.PROTOCOL_VERSION.getIntNoCheck(this.properties);
    }
    
    public void setProtocolVersion(final int protocolVersion) {
        if (protocolVersion == 0) {
            PGProperty.PROTOCOL_VERSION.set(this.properties, null);
        }
        else {
            PGProperty.PROTOCOL_VERSION.set(this.properties, protocolVersion);
        }
    }
    
    public int getReceiveBufferSize() {
        return PGProperty.RECEIVE_BUFFER_SIZE.getIntNoCheck(this.properties);
    }
    
    public void setReceiveBufferSize(final int nbytes) {
        PGProperty.RECEIVE_BUFFER_SIZE.set(this.properties, nbytes);
    }
    
    public int getSendBufferSize() {
        return PGProperty.SEND_BUFFER_SIZE.getIntNoCheck(this.properties);
    }
    
    public void setSendBufferSize(final int nbytes) {
        PGProperty.SEND_BUFFER_SIZE.set(this.properties, nbytes);
    }
    
    public void setPrepareThreshold(final int count) {
        PGProperty.PREPARE_THRESHOLD.set(this.properties, count);
    }
    
    public int getPrepareThreshold() {
        return PGProperty.PREPARE_THRESHOLD.getIntNoCheck(this.properties);
    }
    
    public int getPreparedStatementCacheQueries() {
        return PGProperty.PREPARED_STATEMENT_CACHE_QUERIES.getIntNoCheck(this.properties);
    }
    
    public void setPreparedStatementCacheQueries(final int cacheSize) {
        PGProperty.PREPARED_STATEMENT_CACHE_QUERIES.set(this.properties, cacheSize);
    }
    
    public int getPreparedStatementCacheSizeMiB() {
        return PGProperty.PREPARED_STATEMENT_CACHE_SIZE_MIB.getIntNoCheck(this.properties);
    }
    
    public void setPreparedStatementCacheSizeMiB(final int cacheSize) {
        PGProperty.PREPARED_STATEMENT_CACHE_SIZE_MIB.set(this.properties, cacheSize);
    }
    
    public int getDatabaseMetadataCacheFields() {
        return PGProperty.DATABASE_METADATA_CACHE_FIELDS.getIntNoCheck(this.properties);
    }
    
    public void setDatabaseMetadataCacheFields(final int cacheSize) {
        PGProperty.DATABASE_METADATA_CACHE_FIELDS.set(this.properties, cacheSize);
    }
    
    public int getDatabaseMetadataCacheFieldsMiB() {
        return PGProperty.DATABASE_METADATA_CACHE_FIELDS_MIB.getIntNoCheck(this.properties);
    }
    
    public void setDatabaseMetadataCacheFieldsMiB(final int cacheSize) {
        PGProperty.DATABASE_METADATA_CACHE_FIELDS_MIB.set(this.properties, cacheSize);
    }
    
    public void setDefaultRowFetchSize(final int fetchSize) {
        PGProperty.DEFAULT_ROW_FETCH_SIZE.set(this.properties, fetchSize);
    }
    
    public int getDefaultRowFetchSize() {
        return PGProperty.DEFAULT_ROW_FETCH_SIZE.getIntNoCheck(this.properties);
    }
    
    public void setUnknownLength(final int unknownLength) {
        PGProperty.UNKNOWN_LENGTH.set(this.properties, unknownLength);
    }
    
    public int getUnknownLength() {
        return PGProperty.UNKNOWN_LENGTH.getIntNoCheck(this.properties);
    }
    
    public void setSocketTimeout(final int seconds) {
        PGProperty.SOCKET_TIMEOUT.set(this.properties, seconds);
    }
    
    public int getSocketTimeout() {
        return PGProperty.SOCKET_TIMEOUT.getIntNoCheck(this.properties);
    }
    
    public void setCancelSignalTimeout(final int seconds) {
        PGProperty.CANCEL_SIGNAL_TIMEOUT.set(this.properties, seconds);
    }
    
    public int getCancelSignalTimeout() {
        return PGProperty.CANCEL_SIGNAL_TIMEOUT.getIntNoCheck(this.properties);
    }
    
    public void setSsl(final boolean enabled) {
        if (enabled) {
            PGProperty.SSL.set(this.properties, true);
        }
        else {
            PGProperty.SSL.set(this.properties, false);
        }
    }
    
    public boolean getSsl() {
        return PGProperty.SSL.getBoolean(this.properties) || "".equals(PGProperty.SSL.get(this.properties));
    }
    
    public void setSslfactory(final String classname) {
        PGProperty.SSL_FACTORY.set(this.properties, classname);
    }
    
    public String getSslfactory() {
        return PGProperty.SSL_FACTORY.get(this.properties);
    }
    
    public String getSslMode() {
        return PGProperty.SSL_MODE.get(this.properties);
    }
    
    public void setSslMode(final String mode) {
        PGProperty.SSL_MODE.set(this.properties, mode);
    }
    
    public String getSslFactoryArg() {
        return PGProperty.SSL_FACTORY_ARG.get(this.properties);
    }
    
    public void setSslFactoryArg(final String arg) {
        PGProperty.SSL_FACTORY_ARG.set(this.properties, arg);
    }
    
    public String getSslHostnameVerifier() {
        return PGProperty.SSL_HOSTNAME_VERIFIER.get(this.properties);
    }
    
    public void setSslHostnameVerifier(final String className) {
        PGProperty.SSL_HOSTNAME_VERIFIER.set(this.properties, className);
    }
    
    public String getSslCert() {
        return PGProperty.SSL_CERT.get(this.properties);
    }
    
    public void setSslCert(final String file) {
        PGProperty.SSL_CERT.set(this.properties, file);
    }
    
    public String getSslKey() {
        return PGProperty.SSL_KEY.get(this.properties);
    }
    
    public void setSslKey(final String file) {
        PGProperty.SSL_KEY.set(this.properties, file);
    }
    
    public String getSslRootCert() {
        return PGProperty.SSL_ROOT_CERT.get(this.properties);
    }
    
    public void setSslRootCert(final String file) {
        PGProperty.SSL_ROOT_CERT.set(this.properties, file);
    }
    
    public String getSslPassword() {
        return PGProperty.SSL_PASSWORD.get(this.properties);
    }
    
    public void setSslPassword(final String password) {
        PGProperty.SSL_PASSWORD.set(this.properties, password);
    }
    
    public String getSslPasswordCallback() {
        return PGProperty.SSL_PASSWORD_CALLBACK.get(this.properties);
    }
    
    public void setSslPasswordCallback(final String className) {
        PGProperty.SSL_PASSWORD_CALLBACK.set(this.properties, className);
    }
    
    public void setApplicationName(final String applicationName) {
        PGProperty.APPLICATION_NAME.set(this.properties, applicationName);
    }
    
    public String getApplicationName() {
        return Nullness.castNonNull(PGProperty.APPLICATION_NAME.get(this.properties));
    }
    
    public void setTargetServerType(final String targetServerType) {
        PGProperty.TARGET_SERVER_TYPE.set(this.properties, targetServerType);
    }
    
    public String getTargetServerType() {
        return Nullness.castNonNull(PGProperty.TARGET_SERVER_TYPE.get(this.properties));
    }
    
    public void setLoadBalanceHosts(final boolean loadBalanceHosts) {
        PGProperty.LOAD_BALANCE_HOSTS.set(this.properties, loadBalanceHosts);
    }
    
    public boolean getLoadBalanceHosts() {
        return PGProperty.LOAD_BALANCE_HOSTS.isPresent(this.properties);
    }
    
    public void setHostRecheckSeconds(final int hostRecheckSeconds) {
        PGProperty.HOST_RECHECK_SECONDS.set(this.properties, hostRecheckSeconds);
    }
    
    public int getHostRecheckSeconds() {
        return PGProperty.HOST_RECHECK_SECONDS.getIntNoCheck(this.properties);
    }
    
    public void setTcpKeepAlive(final boolean enabled) {
        PGProperty.TCP_KEEP_ALIVE.set(this.properties, enabled);
    }
    
    public boolean getTcpKeepAlive() {
        return PGProperty.TCP_KEEP_ALIVE.getBoolean(this.properties);
    }
    
    public void setBinaryTransfer(final boolean enabled) {
        PGProperty.BINARY_TRANSFER.set(this.properties, enabled);
    }
    
    public boolean getBinaryTransfer() {
        return PGProperty.BINARY_TRANSFER.getBoolean(this.properties);
    }
    
    public void setBinaryTransferEnable(final String oidList) {
        PGProperty.BINARY_TRANSFER_ENABLE.set(this.properties, oidList);
    }
    
    public String getBinaryTransferEnable() {
        return Nullness.castNonNull(PGProperty.BINARY_TRANSFER_ENABLE.get(this.properties));
    }
    
    public void setBinaryTransferDisable(final String oidList) {
        PGProperty.BINARY_TRANSFER_DISABLE.set(this.properties, oidList);
    }
    
    public String getBinaryTransferDisable() {
        return Nullness.castNonNull(PGProperty.BINARY_TRANSFER_DISABLE.get(this.properties));
    }
    
    public String getStringType() {
        return PGProperty.STRING_TYPE.get(this.properties);
    }
    
    public void setStringType(final String stringType) {
        PGProperty.STRING_TYPE.set(this.properties, stringType);
    }
    
    public boolean isColumnSanitiserDisabled() {
        return PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(this.properties);
    }
    
    public boolean getDisableColumnSanitiser() {
        return PGProperty.DISABLE_COLUMN_SANITISER.getBoolean(this.properties);
    }
    
    public void setDisableColumnSanitiser(final boolean disableColumnSanitiser) {
        PGProperty.DISABLE_COLUMN_SANITISER.set(this.properties, disableColumnSanitiser);
    }
    
    public String getCurrentSchema() {
        return PGProperty.CURRENT_SCHEMA.get(this.properties);
    }
    
    public void setCurrentSchema(final String currentSchema) {
        PGProperty.CURRENT_SCHEMA.set(this.properties, currentSchema);
    }
    
    public boolean getReadOnly() {
        return PGProperty.READ_ONLY.getBoolean(this.properties);
    }
    
    public void setReadOnly(final boolean readOnly) {
        PGProperty.READ_ONLY.set(this.properties, readOnly);
    }
    
    public String getReadOnlyMode() {
        return Nullness.castNonNull(PGProperty.READ_ONLY_MODE.get(this.properties));
    }
    
    public void setReadOnlyMode(final String mode) {
        PGProperty.READ_ONLY_MODE.set(this.properties, mode);
    }
    
    public boolean getLogUnclosedConnections() {
        return PGProperty.LOG_UNCLOSED_CONNECTIONS.getBoolean(this.properties);
    }
    
    public void setLogUnclosedConnections(final boolean enabled) {
        PGProperty.LOG_UNCLOSED_CONNECTIONS.set(this.properties, enabled);
    }
    
    public boolean getLogServerErrorDetail() {
        return PGProperty.LOG_SERVER_ERROR_DETAIL.getBoolean(this.properties);
    }
    
    public void setLogServerErrorDetail(final boolean enabled) {
        PGProperty.LOG_SERVER_ERROR_DETAIL.set(this.properties, enabled);
    }
    
    public String getAssumeMinServerVersion() {
        return PGProperty.ASSUME_MIN_SERVER_VERSION.get(this.properties);
    }
    
    public void setAssumeMinServerVersion(final String minVersion) {
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(this.properties, minVersion);
    }
    
    public String getJaasApplicationName() {
        return PGProperty.JAAS_APPLICATION_NAME.get(this.properties);
    }
    
    public void setJaasApplicationName(final String name) {
        PGProperty.JAAS_APPLICATION_NAME.set(this.properties, name);
    }
    
    public boolean getJaasLogin() {
        return PGProperty.JAAS_LOGIN.getBoolean(this.properties);
    }
    
    public void setJaasLogin(final boolean doLogin) {
        PGProperty.JAAS_LOGIN.set(this.properties, doLogin);
    }
    
    public String getKerberosServerName() {
        return PGProperty.KERBEROS_SERVER_NAME.get(this.properties);
    }
    
    public void setKerberosServerName(final String serverName) {
        PGProperty.KERBEROS_SERVER_NAME.set(this.properties, serverName);
    }
    
    public boolean getUseSpNego() {
        return PGProperty.USE_SPNEGO.getBoolean(this.properties);
    }
    
    public void setUseSpNego(final boolean use) {
        PGProperty.USE_SPNEGO.set(this.properties, use);
    }
    
    public String getGssLib() {
        return PGProperty.GSS_LIB.get(this.properties);
    }
    
    public void setGssLib(final String lib) {
        PGProperty.GSS_LIB.set(this.properties, lib);
    }
    
    public String getGssEncMode() {
        return Nullness.castNonNull(PGProperty.GSS_ENC_MODE.get(this.properties));
    }
    
    public void setGssEncMode(final String mode) {
        PGProperty.GSS_ENC_MODE.set(this.properties, mode);
    }
    
    public String getSspiServiceClass() {
        return PGProperty.SSPI_SERVICE_CLASS.get(this.properties);
    }
    
    public void setSspiServiceClass(final String serviceClass) {
        PGProperty.SSPI_SERVICE_CLASS.set(this.properties, serviceClass);
    }
    
    public boolean getAllowEncodingChanges() {
        return PGProperty.ALLOW_ENCODING_CHANGES.getBoolean(this.properties);
    }
    
    public void setAllowEncodingChanges(final boolean allow) {
        PGProperty.ALLOW_ENCODING_CHANGES.set(this.properties, allow);
    }
    
    public String getSocketFactory() {
        return PGProperty.SOCKET_FACTORY.get(this.properties);
    }
    
    public void setSocketFactory(final String socketFactoryClassName) {
        PGProperty.SOCKET_FACTORY.set(this.properties, socketFactoryClassName);
    }
    
    public String getSocketFactoryArg() {
        return PGProperty.SOCKET_FACTORY_ARG.get(this.properties);
    }
    
    public void setSocketFactoryArg(final String socketFactoryArg) {
        PGProperty.SOCKET_FACTORY_ARG.set(this.properties, socketFactoryArg);
    }
    
    public void setReplication(final String replication) {
        PGProperty.REPLICATION.set(this.properties, replication);
    }
    
    public String getEscapeSyntaxCallMode() {
        return Nullness.castNonNull(PGProperty.ESCAPE_SYNTAX_CALL_MODE.get(this.properties));
    }
    
    public void setEscapeSyntaxCallMode(final String callMode) {
        PGProperty.ESCAPE_SYNTAX_CALL_MODE.set(this.properties, callMode);
    }
    
    public String getReplication() {
        return PGProperty.REPLICATION.get(this.properties);
    }
    
    public String getLoggerLevel() {
        return PGProperty.LOGGER_LEVEL.get(this.properties);
    }
    
    public void setLoggerLevel(final String loggerLevel) {
        PGProperty.LOGGER_LEVEL.set(this.properties, loggerLevel);
    }
    
    public String getLoggerFile() {
        final ExpressionProperties exprProps = new ExpressionProperties(new Properties[] { this.properties, System.getProperties() });
        return PGProperty.LOGGER_FILE.get(exprProps);
    }
    
    public void setLoggerFile(final String loggerFile) {
        PGProperty.LOGGER_FILE.set(this.properties, loggerFile);
    }
    
    public String getUrl() {
        final StringBuilder url = new StringBuilder(100);
        url.append("jdbc:postgresql://");
        for (int i = 0; i < this.serverNames.length; ++i) {
            if (i > 0) {
                url.append(",");
            }
            url.append(this.serverNames[i]);
            if (this.portNumbers != null && this.portNumbers.length >= i && this.portNumbers[i] != 0) {
                url.append(":").append(this.portNumbers[i]);
            }
        }
        url.append("/");
        if (this.databaseName != null) {
            url.append(URLCoder.encode(this.databaseName));
        }
        final StringBuilder query = new StringBuilder(100);
        for (final PGProperty property : PGProperty.values()) {
            if (property.isPresent(this.properties)) {
                if (query.length() != 0) {
                    query.append("&");
                }
                query.append(property.getName());
                query.append("=");
                final String value = Nullness.castNonNull(property.get(this.properties));
                query.append(URLCoder.encode(value));
            }
        }
        if (query.length() > 0) {
            url.append("?");
            url.append((CharSequence)query);
        }
        return url.toString();
    }
    
    public String getURL() {
        return this.getUrl();
    }
    
    public void setUrl(final String url) {
        final Properties p = Driver.parseURL(url, null);
        if (p == null) {
            throw new IllegalArgumentException("URL invalid " + url);
        }
        for (final PGProperty property : PGProperty.values()) {
            if (!this.properties.containsKey(property.getName())) {
                this.setProperty(property, property.get(p));
            }
        }
    }
    
    public void setURL(final String url) {
        this.setUrl(url);
    }
    
    public String getProperty(final String name) throws SQLException {
        final PGProperty pgProperty = PGProperty.forName(name);
        if (pgProperty != null) {
            return this.getProperty(pgProperty);
        }
        throw new PSQLException(GT.tr("Unsupported property name: {0}", name), PSQLState.INVALID_PARAMETER_VALUE);
    }
    
    public void setProperty(final String name, final String value) throws SQLException {
        final PGProperty pgProperty = PGProperty.forName(name);
        if (pgProperty != null) {
            this.setProperty(pgProperty, value);
            return;
        }
        throw new PSQLException(GT.tr("Unsupported property name: {0}", name), PSQLState.INVALID_PARAMETER_VALUE);
    }
    
    public String getProperty(final PGProperty property) {
        return property.get(this.properties);
    }
    
    public void setProperty(final PGProperty property, final String value) {
        if (value == null) {
            return;
        }
        switch (property) {
            case PG_HOST: {
                this.setServerNames(value.split(","));
                break;
            }
            case PG_PORT: {
                final String[] ps = value.split(",");
                final int[] ports = new int[ps.length];
                for (int i = 0; i < ps.length; ++i) {
                    try {
                        ports[i] = Integer.parseInt(ps[i]);
                    }
                    catch (final NumberFormatException e) {
                        ports[i] = 0;
                    }
                }
                this.setPortNumbers(ports);
                break;
            }
            case PG_DBNAME: {
                this.setDatabaseName(value);
                break;
            }
            case USER: {
                this.setUser(value);
                break;
            }
            case PASSWORD: {
                this.setPassword(value);
                break;
            }
            default: {
                this.properties.setProperty(property.getName(), value);
                break;
            }
        }
    }
    
    protected Reference createReference() {
        return new Reference(this.getClass().getName(), PGObjectFactory.class.getName(), null);
    }
    
    @Override
    public Reference getReference() throws NamingException {
        final Reference ref = this.createReference();
        final StringBuilder serverString = new StringBuilder();
        for (int i = 0; i < this.serverNames.length; ++i) {
            if (i > 0) {
                serverString.append(",");
            }
            final String serverName = this.serverNames[i];
            serverString.append(serverName);
        }
        ref.add(new StringRefAddr("serverName", serverString.toString()));
        final StringBuilder portString = new StringBuilder();
        for (int j = 0; j < this.portNumbers.length; ++j) {
            if (j > 0) {
                portString.append(",");
            }
            final int p = this.portNumbers[j];
            portString.append(Integer.toString(p));
        }
        ref.add(new StringRefAddr("portNumber", portString.toString()));
        ref.add(new StringRefAddr("databaseName", this.databaseName));
        if (this.user != null) {
            ref.add(new StringRefAddr("user", this.user));
        }
        if (this.password != null) {
            ref.add(new StringRefAddr("password", this.password));
        }
        for (final PGProperty property : PGProperty.values()) {
            if (property.isPresent(this.properties)) {
                final String value = Nullness.castNonNull(property.get(this.properties));
                ref.add(new StringRefAddr(property.getName(), value));
            }
        }
        return ref;
    }
    
    public void setFromReference(final Reference ref) {
        this.databaseName = getReferenceProperty(ref, "databaseName");
        final String portNumberString = getReferenceProperty(ref, "portNumber");
        if (portNumberString != null) {
            final String[] ps = portNumberString.split(",");
            final int[] ports = new int[ps.length];
            for (int i = 0; i < ps.length; ++i) {
                try {
                    ports[i] = Integer.parseInt(ps[i]);
                }
                catch (final NumberFormatException e) {
                    ports[i] = 0;
                }
            }
            this.setPortNumbers(ports);
        }
        else {
            this.setPortNumbers(null);
        }
        final String serverName = Nullness.castNonNull(getReferenceProperty(ref, "serverName"));
        this.setServerNames(serverName.split(","));
        for (final PGProperty property : PGProperty.values()) {
            this.setProperty(property, getReferenceProperty(ref, property.getName()));
        }
    }
    
    private static String getReferenceProperty(final Reference ref, final String propertyName) {
        final RefAddr addr = ref.get(propertyName);
        if (addr == null) {
            return null;
        }
        return (String)addr.getContent();
    }
    
    protected void writeBaseObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.serverNames);
        out.writeObject(this.databaseName);
        out.writeObject(this.user);
        out.writeObject(this.password);
        out.writeObject(this.portNumbers);
        out.writeObject(this.properties);
    }
    
    protected void readBaseObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.serverNames = (String[])in.readObject();
        this.databaseName = (String)in.readObject();
        this.user = (String)in.readObject();
        this.password = (String)in.readObject();
        this.portNumbers = (int[])in.readObject();
        this.properties = (Properties)in.readObject();
    }
    
    public void initializeFrom(final BaseDataSource source) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        source.writeBaseObject(oos);
        oos.close();
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        this.readBaseObject(ois);
    }
    
    public PreferQueryMode getPreferQueryMode() {
        return PreferQueryMode.of(Nullness.castNonNull(PGProperty.PREFER_QUERY_MODE.get(this.properties)));
    }
    
    public void setPreferQueryMode(final PreferQueryMode preferQueryMode) {
        PGProperty.PREFER_QUERY_MODE.set(this.properties, preferQueryMode.value());
    }
    
    public AutoSave getAutosave() {
        return AutoSave.of(Nullness.castNonNull(PGProperty.AUTOSAVE.get(this.properties)));
    }
    
    public void setAutosave(final AutoSave autoSave) {
        PGProperty.AUTOSAVE.set(this.properties, autoSave.value());
    }
    
    public boolean getCleanupSavepoints() {
        return PGProperty.CLEANUP_SAVEPOINTS.getBoolean(this.properties);
    }
    
    public void setCleanupSavepoints(final boolean cleanupSavepoints) {
        PGProperty.CLEANUP_SAVEPOINTS.set(this.properties, cleanupSavepoints);
    }
    
    public boolean getReWriteBatchedInserts() {
        return PGProperty.REWRITE_BATCHED_INSERTS.getBoolean(this.properties);
    }
    
    public void setReWriteBatchedInserts(final boolean reWrite) {
        PGProperty.REWRITE_BATCHED_INSERTS.set(this.properties, reWrite);
    }
    
    public boolean getHideUnprivilegedObjects() {
        return PGProperty.HIDE_UNPRIVILEGED_OBJECTS.getBoolean(this.properties);
    }
    
    public void setHideUnprivilegedObjects(final boolean hideUnprivileged) {
        PGProperty.HIDE_UNPRIVILEGED_OBJECTS.set(this.properties, hideUnprivileged);
    }
    
    public String getMaxResultBuffer() {
        return PGProperty.MAX_RESULT_BUFFER.get(this.properties);
    }
    
    public void setMaxResultBuffer(final String maxResultBuffer) {
        PGProperty.MAX_RESULT_BUFFER.set(this.properties, maxResultBuffer);
    }
    
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger("org.postgresql");
    }
    
    public String getXmlFactoryFactory() {
        return Nullness.castNonNull(PGProperty.XML_FACTORY_FACTORY.get(this.properties));
    }
    
    public void setXmlFactoryFactory(final String xmlFactoryFactory) {
        PGProperty.XML_FACTORY_FACTORY.set(this.properties, xmlFactoryFactory);
    }
    
    public boolean isSsl() {
        return this.getSsl();
    }
    
    public String getSslfactoryarg() {
        return this.getSslFactoryArg();
    }
    
    public void setSslfactoryarg(final String arg) {
        this.setSslFactoryArg(arg);
    }
    
    public String getSslcert() {
        return this.getSslCert();
    }
    
    public void setSslcert(final String file) {
        this.setSslCert(file);
    }
    
    public String getSslmode() {
        return this.getSslMode();
    }
    
    public void setSslmode(final String mode) {
        this.setSslMode(mode);
    }
    
    public String getSslhostnameverifier() {
        return this.getSslHostnameVerifier();
    }
    
    public void setSslhostnameverifier(final String className) {
        this.setSslHostnameVerifier(className);
    }
    
    public String getSslkey() {
        return this.getSslKey();
    }
    
    public void setSslkey(final String file) {
        this.setSslKey(file);
    }
    
    public String getSslrootcert() {
        return this.getSslRootCert();
    }
    
    public void setSslrootcert(final String file) {
        this.setSslRootCert(file);
    }
    
    public String getSslpasswordcallback() {
        return this.getSslPasswordCallback();
    }
    
    public void setSslpasswordcallback(final String className) {
        this.setSslPasswordCallback(className);
    }
    
    public String getSslpassword() {
        return this.getSslPassword();
    }
    
    public void setSslpassword(final String sslpassword) {
        this.setSslPassword(sslpassword);
    }
    
    public int getRecvBufferSize() {
        return this.getReceiveBufferSize();
    }
    
    public void setRecvBufferSize(final int nbytes) {
        this.setReceiveBufferSize(nbytes);
    }
    
    public boolean isAllowEncodingChanges() {
        return this.getAllowEncodingChanges();
    }
    
    public boolean isLogUnclosedConnections() {
        return this.getLogUnclosedConnections();
    }
    
    public boolean isTcpKeepAlive() {
        return this.getTcpKeepAlive();
    }
    
    public boolean isReadOnly() {
        return this.getReadOnly();
    }
    
    public boolean isDisableColumnSanitiser() {
        return this.getDisableColumnSanitiser();
    }
    
    public boolean isLoadBalanceHosts() {
        return this.getLoadBalanceHosts();
    }
    
    public boolean isCleanupSavePoints() {
        return this.getCleanupSavepoints();
    }
    
    public void setCleanupSavePoints(final boolean cleanupSavepoints) {
        this.setCleanupSavepoints(cleanupSavepoints);
    }
    
    public boolean isReWriteBatchedInserts() {
        return this.getReWriteBatchedInserts();
    }
    
    static {
        LOGGER = Logger.getLogger(BaseDataSource.class.getName());
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (final ClassNotFoundException e) {
            throw new IllegalStateException("BaseDataSource is unable to load org.postgresql.Driver. Please check if you have proper PostgreSQL JDBC Driver jar on the classpath", e);
        }
    }
}
