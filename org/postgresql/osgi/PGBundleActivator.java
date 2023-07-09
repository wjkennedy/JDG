// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.osgi;

import java.util.Dictionary;
import org.osgi.service.jdbc.DataSourceFactory;
import java.util.Hashtable;
import org.postgresql.Driver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleActivator;

public class PGBundleActivator implements BundleActivator
{
    private ServiceRegistration<?> registration;
    
    public void start(final BundleContext context) throws Exception {
        if (!Driver.isRegistered()) {
            Driver.register();
        }
        if (dataSourceFactoryExists()) {
            this.registerDataSourceFactory(context);
        }
    }
    
    private static boolean dataSourceFactoryExists() {
        try {
            Class.forName("org.osgi.service.jdbc.DataSourceFactory");
            return true;
        }
        catch (final ClassNotFoundException ex) {
            return false;
        }
    }
    
    private void registerDataSourceFactory(final BundleContext context) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("osgi.jdbc.driver.class", Driver.class.getName());
        properties.put("osgi.jdbc.driver.name", "PostgreSQL JDBC Driver");
        properties.put("osgi.jdbc.driver.version", "42.2.25");
        this.registration = (ServiceRegistration<?>)context.registerService((Class)DataSourceFactory.class, (Object)new PGDataSourceFactory(), (Dictionary)properties);
    }
    
    public void stop(final BundleContext context) throws Exception {
        if (this.registration != null) {
            this.registration.unregister();
            this.registration = null;
        }
        if (Driver.isRegistered()) {
            Driver.deregister();
        }
    }
}
