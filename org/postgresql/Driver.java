// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql;

import java.util.concurrent.TimeUnit;
import java.sql.SQLFeatureNotSupportedException;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.HostSpec;
import org.postgresql.util.URLCoder;
import java.sql.DriverPropertyInfo;
import org.postgresql.jdbc.PgConnection;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.io.OutputStream;
import java.util.logging.StreamHandler;
import java.io.Writer;
import org.postgresql.util.LogWriterHandler;
import java.sql.DriverManager;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;
import org.postgresql.util.ExpressionProperties;
import java.util.Iterator;
import java.util.Set;
import java.security.AccessControlException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.SQLException;
import java.sql.Connection;
import java.io.InputStream;
import java.util.Enumeration;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import org.postgresql.util.SharedTimer;
import java.util.logging.Logger;

public class Driver implements java.sql.Driver
{
    private static Driver registeredDriver;
    private static final Logger PARENT_LOGGER;
    private static final Logger LOGGER;
    private static final SharedTimer SHARED_TIMER;
    private static final String DEFAULT_PORT = "5432";
    private Properties defaultProperties;
    private static String loggerHandlerFile;
    
    private synchronized Properties getDefaultProperties() throws IOException {
        if (this.defaultProperties != null) {
            return this.defaultProperties;
        }
        try {
            this.defaultProperties = AccessController.doPrivileged((PrivilegedExceptionAction<Properties>)new PrivilegedExceptionAction<Properties>() {
                @Override
                public Properties run() throws IOException {
                    return Driver.this.loadDefaultProperties();
                }
            });
        }
        catch (final PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
        return this.defaultProperties;
    }
    
    private Properties loadDefaultProperties() throws IOException {
        final Properties merged = new Properties();
        try {
            PGProperty.USER.set(merged, System.getProperty("user.name"));
        }
        catch (final SecurityException ex) {}
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl == null) {
            Driver.LOGGER.log(Level.FINE, "Can't find our classloader for the Driver; attempt to use the system class loader");
            cl = ClassLoader.getSystemClassLoader();
        }
        if (cl == null) {
            Driver.LOGGER.log(Level.WARNING, "Can't find a classloader for the Driver; not loading driver configuration from org/postgresql/driverconfig.properties");
            return merged;
        }
        Driver.LOGGER.log(Level.FINE, "Loading driver configuration via classloader {0}", cl);
        final ArrayList<URL> urls = new ArrayList<URL>();
        final Enumeration<URL> urlEnum = cl.getResources("org/postgresql/driverconfig.properties");
        while (urlEnum.hasMoreElements()) {
            urls.add(urlEnum.nextElement());
        }
        for (int i = urls.size() - 1; i >= 0; --i) {
            final URL url = urls.get(i);
            Driver.LOGGER.log(Level.FINE, "Loading driver configuration from: {0}", url);
            final InputStream is = url.openStream();
            merged.load(is);
            is.close();
        }
        return merged;
    }
    
    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (url == null) {
            throw new SQLException("url is null");
        }
        if (!url.startsWith("jdbc:postgresql:")) {
            return null;
        }
        Properties defaults;
        try {
            defaults = this.getDefaultProperties();
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("Error loading default settings from driverconfig.properties", new Object[0]), PSQLState.UNEXPECTED_ERROR, ioe);
        }
        Properties props = new Properties(defaults);
        if (info != null) {
            final Set<String> e = info.stringPropertyNames();
            for (final String propName : e) {
                final String propValue = info.getProperty(propName);
                if (propValue == null) {
                    throw new PSQLException(GT.tr("Properties for the driver contains a non-string value for the key ", new Object[0]) + propName, PSQLState.UNEXPECTED_ERROR);
                }
                props.setProperty(propName, propValue);
            }
        }
        if ((props = parseURL(url, props)) == null) {
            return null;
        }
        try {
            this.setupLoggerFromProperties(props);
            Driver.LOGGER.log(Level.FINE, "Connecting with URL: {0}", url);
            final long timeout = timeout(props);
            if (timeout <= 0L) {
                return makeConnection(url, props);
            }
            final ConnectThread ct = new ConnectThread(url, props);
            final Thread thread = new Thread(ct, "PostgreSQL JDBC driver connection thread");
            thread.setDaemon(true);
            thread.start();
            return ct.getResult(timeout);
        }
        catch (final PSQLException ex1) {
            Driver.LOGGER.log(Level.FINE, "Connection error: ", ex1);
            throw ex1;
        }
        catch (final AccessControlException ace) {
            throw new PSQLException(GT.tr("Your security policy has prevented the connection from being attempted.  You probably need to grant the connect java.net.SocketPermission to the database server host and port that you wish to connect to.", new Object[0]), PSQLState.UNEXPECTED_ERROR, ace);
        }
        catch (final Exception ex2) {
            Driver.LOGGER.log(Level.FINE, "Unexpected connection error: ", ex2);
            throw new PSQLException(GT.tr("Something unusual has occurred to cause the driver to fail. Please report this exception.", new Object[0]), PSQLState.UNEXPECTED_ERROR, ex2);
        }
    }
    
    private void setupLoggerFromProperties(final Properties props) {
        final String driverLogLevel = PGProperty.LOGGER_LEVEL.get(props);
        if (driverLogLevel == null) {
            return;
        }
        if ("OFF".equalsIgnoreCase(driverLogLevel)) {
            Driver.PARENT_LOGGER.setLevel(Level.OFF);
            return;
        }
        if ("DEBUG".equalsIgnoreCase(driverLogLevel)) {
            Driver.PARENT_LOGGER.setLevel(Level.FINE);
        }
        else if ("TRACE".equalsIgnoreCase(driverLogLevel)) {
            Driver.PARENT_LOGGER.setLevel(Level.FINEST);
        }
        final ExpressionProperties exprProps = new ExpressionProperties(new Properties[] { props, System.getProperties() });
        final String driverLogFile = PGProperty.LOGGER_FILE.get(exprProps);
        if (driverLogFile != null && driverLogFile.equals(Driver.loggerHandlerFile)) {
            return;
        }
        for (final Handler handlers : Driver.PARENT_LOGGER.getHandlers()) {
            handlers.close();
            Driver.PARENT_LOGGER.removeHandler(handlers);
            Driver.loggerHandlerFile = null;
        }
        Handler handler = null;
        if (driverLogFile != null) {
            try {
                handler = new FileHandler(driverLogFile);
                Driver.loggerHandlerFile = driverLogFile;
            }
            catch (final Exception ex) {
                System.err.println("Cannot enable FileHandler, fallback to ConsoleHandler.");
            }
        }
        final Formatter formatter = new SimpleFormatter();
        if (handler == null) {
            if (DriverManager.getLogWriter() != null) {
                handler = new LogWriterHandler(DriverManager.getLogWriter());
            }
            else if (DriverManager.getLogStream() != null) {
                handler = new StreamHandler(DriverManager.getLogStream(), formatter);
            }
            else {
                handler = new StreamHandler(System.err, formatter);
            }
        }
        else {
            handler.setFormatter(formatter);
        }
        final Level loggerLevel = Driver.PARENT_LOGGER.getLevel();
        if (loggerLevel != null) {
            handler.setLevel(loggerLevel);
        }
        Driver.PARENT_LOGGER.setUseParentHandlers(false);
        Driver.PARENT_LOGGER.addHandler(handler);
    }
    
    private static Connection makeConnection(final String url, final Properties props) throws SQLException {
        return new PgConnection(hostSpecs(props), user(props), database(props), props, url);
    }
    
    @Override
    public boolean acceptsURL(final String url) {
        return parseURL(url, null) != null;
    }
    
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
        Properties copy = new Properties(info);
        final Properties parse = parseURL(url, copy);
        if (parse != null) {
            copy = parse;
        }
        final PGProperty[] knownProperties = PGProperty.values();
        final DriverPropertyInfo[] props = new DriverPropertyInfo[knownProperties.length];
        for (int i = 0; i < props.length; ++i) {
            props[i] = knownProperties[i].toDriverPropertyInfo(copy);
        }
        return props;
    }
    
    @Override
    public int getMajorVersion() {
        return 42;
    }
    
    @Override
    public int getMinorVersion() {
        return 2;
    }
    
    @Deprecated
    public static String getVersion() {
        return "PostgreSQL JDBC Driver 42.2.25";
    }
    
    @Override
    public boolean jdbcCompliant() {
        return false;
    }
    
    public static Properties parseURL(final String url, final Properties defaults) {
        final Properties urlProps = new Properties(defaults);
        String urlServer = url;
        String urlArgs = "";
        final int qPos = url.indexOf(63);
        if (qPos != -1) {
            urlServer = url.substring(0, qPos);
            urlArgs = url.substring(qPos + 1);
        }
        if (!urlServer.startsWith("jdbc:postgresql:")) {
            Driver.LOGGER.log(Level.FINE, "JDBC URL must start with \"jdbc:postgresql:\" but was: {0}", url);
            return null;
        }
        urlServer = urlServer.substring("jdbc:postgresql:".length());
        if (urlServer.startsWith("//")) {
            urlServer = urlServer.substring(2);
            final int slash = urlServer.indexOf(47);
            if (slash == -1) {
                Driver.LOGGER.log(Level.WARNING, "JDBC URL must contain a / at the end of the host or port: {0}", url);
                return null;
            }
            urlProps.setProperty("PGDBNAME", URLCoder.decode(urlServer.substring(slash + 1)));
            final String[] addresses = urlServer.substring(0, slash).split(",");
            final StringBuilder hosts = new StringBuilder();
            final StringBuilder ports = new StringBuilder();
            for (final String address : addresses) {
                final int portIdx = address.lastIndexOf(58);
                if (portIdx != -1 && address.lastIndexOf(93) < portIdx) {
                    final String portStr = address.substring(portIdx + 1);
                    try {
                        final int port = Integer.parseInt(portStr);
                        if (port < 1 || port > 65535) {
                            Driver.LOGGER.log(Level.WARNING, "JDBC URL port: {0} not valid (1:65535) ", portStr);
                            return null;
                        }
                    }
                    catch (final NumberFormatException ignore) {
                        Driver.LOGGER.log(Level.WARNING, "JDBC URL invalid port number: {0}", portStr);
                        return null;
                    }
                    ports.append(portStr);
                    hosts.append(address.subSequence(0, portIdx));
                }
                else {
                    ports.append("5432");
                    hosts.append(address);
                }
                ports.append(',');
                hosts.append(',');
            }
            ports.setLength(ports.length() - 1);
            hosts.setLength(hosts.length() - 1);
            urlProps.setProperty("PGPORT", ports.toString());
            urlProps.setProperty("PGHOST", hosts.toString());
        }
        else {
            if (defaults == null || !defaults.containsKey("PGPORT")) {
                urlProps.setProperty("PGPORT", "5432");
            }
            if (defaults == null || !defaults.containsKey("PGHOST")) {
                urlProps.setProperty("PGHOST", "localhost");
            }
            if (defaults == null || !defaults.containsKey("PGDBNAME")) {
                urlProps.setProperty("PGDBNAME", URLCoder.decode(urlServer));
            }
        }
        final String[] split;
        final String[] args = split = urlArgs.split("&");
        for (final String token : split) {
            if (!token.isEmpty()) {
                final int pos = token.indexOf(61);
                if (pos == -1) {
                    urlProps.setProperty(token, "");
                }
                else {
                    urlProps.setProperty(token.substring(0, pos), URLCoder.decode(token.substring(pos + 1)));
                }
            }
        }
        return urlProps;
    }
    
    private static HostSpec[] hostSpecs(final Properties props) {
        final String[] hosts = Nullness.castNonNull(props.getProperty("PGHOST")).split(",");
        final String[] ports = Nullness.castNonNull(props.getProperty("PGPORT")).split(",");
        final HostSpec[] hostSpecs = new HostSpec[hosts.length];
        for (int i = 0; i < hostSpecs.length; ++i) {
            hostSpecs[i] = new HostSpec(hosts[i], Integer.parseInt(ports[i]));
        }
        return hostSpecs;
    }
    
    private static String user(final Properties props) {
        return props.getProperty("user", "");
    }
    
    private static String database(final Properties props) {
        return props.getProperty("PGDBNAME", "");
    }
    
    private static long timeout(final Properties props) {
        final String timeout = PGProperty.LOGIN_TIMEOUT.get(props);
        if (timeout != null) {
            try {
                return (long)(Float.parseFloat(timeout) * 1000.0f);
            }
            catch (final NumberFormatException e) {
                Driver.LOGGER.log(Level.WARNING, "Couldn't parse loginTimeout value: {0}", timeout);
            }
        }
        return DriverManager.getLoginTimeout() * 1000L;
    }
    
    public static SQLFeatureNotSupportedException notImplemented(final Class<?> callClass, final String functionName) {
        return new SQLFeatureNotSupportedException(GT.tr("Method {0} is not yet implemented.", callClass.getName() + "." + functionName), PSQLState.NOT_IMPLEMENTED.getState());
    }
    
    @Override
    public Logger getParentLogger() {
        return Driver.PARENT_LOGGER;
    }
    
    public static SharedTimer getSharedTimer() {
        return Driver.SHARED_TIMER;
    }
    
    public static void register() throws SQLException {
        if (isRegistered()) {
            throw new IllegalStateException("Driver is already registered. It can only be registered once.");
        }
        final Driver registeredDriver = new Driver();
        DriverManager.registerDriver(registeredDriver);
        Driver.registeredDriver = registeredDriver;
    }
    
    public static void deregister() throws SQLException {
        if (Driver.registeredDriver == null) {
            throw new IllegalStateException("Driver is not registered (or it has not been registered using Driver.register() method)");
        }
        DriverManager.deregisterDriver(Driver.registeredDriver);
        Driver.registeredDriver = null;
    }
    
    public static boolean isRegistered() {
        return Driver.registeredDriver != null;
    }
    
    static {
        PARENT_LOGGER = Logger.getLogger("org.postgresql");
        LOGGER = Logger.getLogger("org.postgresql.Driver");
        SHARED_TIMER = new SharedTimer();
        try {
            register();
        }
        catch (final SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    private static class ConnectThread implements Runnable
    {
        private final String url;
        private final Properties props;
        private Connection result;
        private Throwable resultException;
        private boolean abandoned;
        
        ConnectThread(final String url, final Properties props) {
            this.url = url;
            this.props = props;
        }
        
        @Override
        public void run() {
            Connection conn;
            Throwable error;
            try {
                conn = makeConnection(this.url, this.props);
                error = null;
            }
            catch (final Throwable t) {
                conn = null;
                error = t;
            }
            synchronized (this) {
                if (this.abandoned) {
                    if (conn != null) {
                        try {
                            conn.close();
                        }
                        catch (final SQLException ex) {}
                    }
                }
                else {
                    this.result = conn;
                    this.resultException = error;
                    this.notify();
                }
            }
        }
        
        public Connection getResult(final long timeout) throws SQLException {
            final long expiry = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + timeout;
            synchronized (this) {
                while (this.result == null) {
                    if (this.resultException != null) {
                        if (this.resultException instanceof SQLException) {
                            this.resultException.fillInStackTrace();
                            throw (SQLException)this.resultException;
                        }
                        throw new PSQLException(GT.tr("Something unusual has occurred to cause the driver to fail. Please report this exception.", new Object[0]), PSQLState.UNEXPECTED_ERROR, this.resultException);
                    }
                    else {
                        final long delay = expiry - TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                        if (delay <= 0L) {
                            this.abandoned = true;
                            throw new PSQLException(GT.tr("Connection attempt timed out.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
                        }
                        try {
                            this.wait(delay);
                        }
                        catch (final InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            this.abandoned = true;
                            throw new RuntimeException(GT.tr("Interrupted while attempting to connect.", new Object[0]));
                        }
                    }
                }
                return this.result;
            }
        }
    }
}
