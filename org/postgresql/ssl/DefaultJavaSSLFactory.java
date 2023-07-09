// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import javax.net.ssl.SSLSocketFactory;
import java.util.Properties;

public class DefaultJavaSSLFactory extends WrappedFactory
{
    public DefaultJavaSSLFactory(final Properties info) {
        this.factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
    }
}
