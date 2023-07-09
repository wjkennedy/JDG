// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql;

import java.util.HashMap;
import java.sql.DriverPropertyInfo;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.util.Properties;
import java.util.Map;

public enum PGProperty
{
    ALLOW_ENCODING_CHANGES("allowEncodingChanges", "false", "Allow for changes in client_encoding"), 
    APPLICATION_NAME("ApplicationName", "PostgreSQL JDBC Driver", "Name of the Application (backend >= 9.0)"), 
    ASSUME_MIN_SERVER_VERSION("assumeMinServerVersion", (String)null, "Assume the server is at least that version"), 
    AUTOSAVE("autosave", "never", "Specifies what the driver should do if a query fails. In autosave=always mode, JDBC driver sets a savepoint before each query, and rolls back to that savepoint in case of failure. In autosave=never mode (default), no savepoint dance is made ever. In autosave=conservative mode, safepoint is set for each query, however the rollback is done only for rare cases like 'cached statement cannot change return type' or 'statement XXX is not valid' so JDBC driver rollsback and retries", false, new String[] { "always", "never", "conservative" }), 
    BINARY_TRANSFER("binaryTransfer", "true", "Use binary format for sending and receiving data if possible"), 
    BINARY_TRANSFER_DISABLE("binaryTransferDisable", "", "Comma separated list of types to disable binary transfer. Either OID numbers or names. Overrides values in the driver default set and values set with binaryTransferEnable."), 
    BINARY_TRANSFER_ENABLE("binaryTransferEnable", "", "Comma separated list of types to enable binary transfer. Either OID numbers or names"), 
    CANCEL_SIGNAL_TIMEOUT("cancelSignalTimeout", "10", "The timeout that is used for sending cancel command."), 
    CLEANUP_SAVEPOINTS("cleanupSavepoints", "false", "Determine whether SAVEPOINTS used in AUTOSAVE will be released per query or not", false, new String[] { "true", "false" }), 
    CONNECT_TIMEOUT("connectTimeout", "10", "The timeout value used for socket connect operations."), 
    CURRENT_SCHEMA("currentSchema", (String)null, "Specify the schema (or several schema separated by commas) to be set in the search-path"), 
    DATABASE_METADATA_CACHE_FIELDS("databaseMetadataCacheFields", "65536", "Specifies the maximum number of fields to be cached per connection. A value of {@code 0} disables the cache."), 
    DATABASE_METADATA_CACHE_FIELDS_MIB("databaseMetadataCacheFieldsMiB", "5", "Specifies the maximum size (in megabytes) of fields to be cached per connection. A value of {@code 0} disables the cache."), 
    DEFAULT_ROW_FETCH_SIZE("defaultRowFetchSize", "0", "Positive number of rows that should be fetched from the database when more rows are needed for ResultSet by each fetch iteration"), 
    DISABLE_COLUMN_SANITISER("disableColumnSanitiser", "false", "Enable optimization that disables column name sanitiser"), 
    ESCAPE_SYNTAX_CALL_MODE("escapeSyntaxCallMode", "select", "Specifies how the driver transforms JDBC escape call syntax into underlying SQL, for invoking procedures or functions. (backend >= 11)In escapeSyntaxCallMode=select mode (the default), the driver always uses a SELECT statement (allowing function invocation only).In escapeSyntaxCallMode=callIfNoReturn mode, the driver uses a CALL statement (allowing procedure invocation) if there is no return parameter specified, otherwise the driver uses a SELECT statement.In escapeSyntaxCallMode=call mode, the driver always uses a CALL statement (allowing procedure invocation only).", false, new String[] { "select", "callIfNoReturn", "call" }), 
    GSS_ENC_MODE("gssEncMode", "allow", "Force Encoded GSS Mode", false, new String[] { "disable", "allow", "prefer", "require" }), 
    GSS_LIB("gsslib", "auto", "Force SSSPI or GSSAPI", false, new String[] { "auto", "sspi", "gssapi" }), 
    HIDE_UNPRIVILEGED_OBJECTS("hideUnprivilegedObjects", "false", "Enable hiding of database objects for which the current user has no privileges granted from the DatabaseMetaData"), 
    HOST_RECHECK_SECONDS("hostRecheckSeconds", "10", "Specifies period (seconds) after which the host status is checked again in case it has changed"), 
    JAAS_APPLICATION_NAME("jaasApplicationName", (String)null, "Specifies the name of the JAAS system or application login configuration."), 
    JAAS_LOGIN("jaasLogin", "true", "Login with JAAS before doing GSSAPI authentication"), 
    KERBEROS_SERVER_NAME("kerberosServerName", (String)null, "The Kerberos service name to use when authenticating with GSSAPI."), 
    LOAD_BALANCE_HOSTS("loadBalanceHosts", "false", "If disabled hosts are connected in the given order. If enabled hosts are chosen randomly from the set of suitable candidates"), 
    LOGGER_FILE("loggerFile", (String)null, "File name output of the Logger"), 
    LOGGER_LEVEL("loggerLevel", (String)null, "Logger level of the driver", false, new String[] { "OFF", "DEBUG", "TRACE" }), 
    LOGIN_TIMEOUT("loginTimeout", "0", "Specify how long to wait for establishment of a database connection."), 
    LOG_SERVER_ERROR_DETAIL("logServerErrorDetail", "true", "Include full server error detail in exception messages. If disabled then only the error itself will be included."), 
    LOG_UNCLOSED_CONNECTIONS("logUnclosedConnections", "false", "When connections that are not explicitly closed are garbage collected, log the stacktrace from the opening of the connection to trace the leak source"), 
    MAX_RESULT_BUFFER("maxResultBuffer", (String)null, "Specifies size of buffer during fetching result set. Can be specified as specified size or percent of heap memory."), 
    OPTIONS("options", (String)null, "Specify 'options' connection initialization parameter."), 
    PASSWORD("password", (String)null, "Password to use when authenticating.", false), 
    PG_DBNAME("PGDBNAME", (String)null, "Database name to connect to (may be specified directly in the JDBC URL)", true), 
    PG_HOST("PGHOST", (String)null, "Hostname of the PostgreSQL server (may be specified directly in the JDBC URL)", false), 
    PG_PORT("PGPORT", (String)null, "Port of the PostgreSQL server (may be specified directly in the JDBC URL)"), 
    PREFER_QUERY_MODE("preferQueryMode", "extended", "Specifies which mode is used to execute queries to database: simple means ('Q' execute, no parse, no bind, text mode only), extended means always use bind/execute messages, extendedForPrepared means extended for prepared statements only, extendedCacheEverything means use extended protocol and try cache every statement (including Statement.execute(String sql)) in a query cache.", false, new String[] { "extended", "extendedForPrepared", "extendedCacheEverything", "simple" }), 
    PREPARED_STATEMENT_CACHE_QUERIES("preparedStatementCacheQueries", "256", "Specifies the maximum number of entries in per-connection cache of prepared statements. A value of {@code 0} disables the cache."), 
    PREPARED_STATEMENT_CACHE_SIZE_MIB("preparedStatementCacheSizeMiB", "5", "Specifies the maximum size (in megabytes) of a per-connection prepared statement cache. A value of {@code 0} disables the cache."), 
    PREPARE_THRESHOLD("prepareThreshold", "5", "Statement prepare threshold. A value of {@code -1} stands for forceBinary"), 
    PROTOCOL_VERSION("protocolVersion", (String)null, "Force use of a particular protocol version when connecting, currently only version 3 is supported.", false, new String[] { "3" }), 
    READ_ONLY("readOnly", "false", "Puts this connection in read-only mode"), 
    READ_ONLY_MODE("readOnlyMode", "transaction", "Controls the behavior when a connection is set to be read only, one of 'ignore', 'transaction', or 'always' When 'ignore', setting readOnly has no effect. When 'transaction' setting readOnly to 'true' will cause transactions to BEGIN READ ONLY if autocommit is 'false'. When 'always' setting readOnly to 'true' will set the session to READ ONLY if autoCommit is 'true' and the transaction to BEGIN READ ONLY if autocommit is 'false'.", false, new String[] { "ignore", "transaction", "always" }), 
    RECEIVE_BUFFER_SIZE("receiveBufferSize", "-1", "Socket read buffer size"), 
    REPLICATION("replication", (String)null, "Connection parameter passed in startup message, one of 'true' or 'database' Passing 'true' tells the backend to go into walsender mode, wherein a small set of replication commands can be issued instead of SQL statements. Only the simple query protocol can be used in walsender mode. Passing 'database' as the value instructs walsender to connect to the database specified in the dbname parameter, which will allow the connection to be used for logical replication from that database. (backend >= 9.4)"), 
    REWRITE_BATCHED_INSERTS("reWriteBatchedInserts", "false", "Enable optimization to rewrite and collapse compatible INSERT statements that are batched."), 
    SEND_BUFFER_SIZE("sendBufferSize", "-1", "Socket write buffer size"), 
    SOCKET_FACTORY("socketFactory", (String)null, "Specify a socket factory for socket creation"), 
    @Deprecated
    SOCKET_FACTORY_ARG("socketFactoryArg", (String)null, "Argument forwarded to constructor of SocketFactory class."), 
    SOCKET_TIMEOUT("socketTimeout", "0", "The timeout value used for socket read operations."), 
    SSL("ssl", (String)null, "Control use of SSL (any non-null value causes SSL to be required)"), 
    SSL_CERT("sslcert", (String)null, "The location of the client's SSL certificate"), 
    SSL_FACTORY("sslfactory", (String)null, "Provide a SSLSocketFactory class when using SSL."), 
    @Deprecated
    SSL_FACTORY_ARG("sslfactoryarg", (String)null, "Argument forwarded to constructor of SSLSocketFactory class."), 
    SSL_HOSTNAME_VERIFIER("sslhostnameverifier", (String)null, "A class, implementing javax.net.ssl.HostnameVerifier that can verify the server"), 
    SSL_KEY("sslkey", (String)null, "The location of the client's PKCS#8 SSL key"), 
    SSL_MODE("sslmode", (String)null, "Parameter governing the use of SSL", false, new String[] { "disable", "allow", "prefer", "require", "verify-ca", "verify-full" }), 
    SSL_PASSWORD("sslpassword", (String)null, "The password for the client's ssl key (ignored if sslpasswordcallback is set)"), 
    SSL_PASSWORD_CALLBACK("sslpasswordcallback", (String)null, "A class, implementing javax.security.auth.callback.CallbackHandler that can handle PassworCallback for the ssl password."), 
    SSL_ROOT_CERT("sslrootcert", (String)null, "The location of the root certificate for authenticating the server."), 
    SSPI_SERVICE_CLASS("sspiServiceClass", "POSTGRES", "The Windows SSPI service class for SPN"), 
    STRING_TYPE("stringtype", (String)null, "The type to bind String parameters as (usually 'varchar', 'unspecified' allows implicit casting to other types)", false, new String[] { "unspecified", "varchar" }), 
    TARGET_SERVER_TYPE("targetServerType", "any", "Specifies what kind of server to connect", false, new String[] { "any", "primary", "master", "slave", "secondary", "preferSlave", "preferSecondary" }), 
    TCP_KEEP_ALIVE("tcpKeepAlive", "false", "Enable or disable TCP keep-alive. The default is {@code false}."), 
    UNKNOWN_LENGTH("unknownLength", Integer.toString(Integer.MAX_VALUE), "Specifies the length to return for types of unknown length"), 
    USER("user", (String)null, "Username to connect to the database as.", true), 
    USE_SPNEGO("useSpnego", "false", "Use SPNEGO in SSPI authentication requests"), 
    XML_FACTORY_FACTORY("xmlFactoryFactory", "", "Factory class to instantiate factories for XML processing");
    
    private final String name;
    private final String defaultValue;
    private final boolean required;
    private final String description;
    private final String[] choices;
    private final boolean deprecated;
    private static final Map<String, PGProperty> PROPS_BY_NAME;
    
    private PGProperty(final String name, final String defaultValue, final String description) {
        this(name, defaultValue, description, false);
    }
    
    private PGProperty(final String name, final String defaultValue, final String description, final boolean required) {
        this(name, defaultValue, description, required, null);
    }
    
    private PGProperty(final String name, final String defaultValue, final String description, final boolean required, final String[] choices) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.required = required;
        this.description = description;
        this.choices = choices;
        try {
            this.deprecated = (PGProperty.class.getField(this.name()).getAnnotation(Deprecated.class) != null);
        }
        catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDefaultValue() {
        return this.defaultValue;
    }
    
    public boolean isRequired() {
        return this.required;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String[] getChoices() {
        return this.choices;
    }
    
    public boolean isDeprecated() {
        return this.deprecated;
    }
    
    public String get(final Properties properties) {
        return properties.getProperty(this.name, this.defaultValue);
    }
    
    public void set(final Properties properties, final String value) {
        if (value == null) {
            properties.remove(this.name);
        }
        else {
            properties.setProperty(this.name, value);
        }
    }
    
    public boolean getBoolean(final Properties properties) {
        return Boolean.parseBoolean(this.get(properties));
    }
    
    public int getIntNoCheck(final Properties properties) {
        final String value = this.get(properties);
        return Integer.parseInt(value);
    }
    
    public int getInt(final Properties properties) throws PSQLException {
        final String value = this.get(properties);
        try {
            return Integer.parseInt(value);
        }
        catch (final NumberFormatException nfe) {
            throw new PSQLException(GT.tr("{0} parameter value must be an integer but was: {1}", this.getName(), value), PSQLState.INVALID_PARAMETER_VALUE, nfe);
        }
    }
    
    public Integer getInteger(final Properties properties) throws PSQLException {
        final String value = this.get(properties);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (final NumberFormatException nfe) {
            throw new PSQLException(GT.tr("{0} parameter value must be an integer but was: {1}", this.getName(), value), PSQLState.INVALID_PARAMETER_VALUE, nfe);
        }
    }
    
    public void set(final Properties properties, final boolean value) {
        properties.setProperty(this.name, Boolean.toString(value));
    }
    
    public void set(final Properties properties, final int value) {
        properties.setProperty(this.name, Integer.toString(value));
    }
    
    public boolean isPresent(final Properties properties) {
        return this.getSetString(properties) != null;
    }
    
    public DriverPropertyInfo toDriverPropertyInfo(final Properties properties) {
        final DriverPropertyInfo propertyInfo = new DriverPropertyInfo(this.name, this.get(properties));
        propertyInfo.required = this.required;
        propertyInfo.description = this.description;
        propertyInfo.choices = this.choices;
        return propertyInfo;
    }
    
    public static PGProperty forName(final String name) {
        return PGProperty.PROPS_BY_NAME.get(name);
    }
    
    public String getSetString(final Properties properties) {
        final Object o = properties.get(this.name);
        if (o instanceof String) {
            return (String)o;
        }
        return null;
    }
    
    static {
        PROPS_BY_NAME = new HashMap<String, PGProperty>();
        for (final PGProperty prop : values()) {
            if (PGProperty.PROPS_BY_NAME.put(prop.getName(), prop) != null) {
                throw new IllegalStateException("Duplicate PGProperty name: " + prop.getName());
            }
        }
    }
}
