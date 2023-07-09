// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.ssl.LibPQFactory;
import javax.net.ssl.SSLSocketFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.ObjectFactory;
import org.postgresql.PGProperty;
import javax.net.SocketFactory;
import java.util.Properties;

public class SocketFactoryFactory
{
    public static SocketFactory getSocketFactory(final Properties info) throws PSQLException {
        final String socketFactoryClassName = PGProperty.SOCKET_FACTORY.get(info);
        if (socketFactoryClassName == null) {
            return SocketFactory.getDefault();
        }
        try {
            return ObjectFactory.instantiate(SocketFactory.class, socketFactoryClassName, info, true, PGProperty.SOCKET_FACTORY_ARG.get(info));
        }
        catch (final Exception e) {
            throw new PSQLException(GT.tr("The SocketFactory class provided {0} could not be instantiated.", socketFactoryClassName), PSQLState.CONNECTION_FAILURE, e);
        }
    }
    
    public static SSLSocketFactory getSslSocketFactory(final Properties info) throws PSQLException {
        final String classname = PGProperty.SSL_FACTORY.get(info);
        if (classname == null || "org.postgresql.ssl.jdbc4.LibPQFactory".equals(classname) || "org.postgresql.ssl.LibPQFactory".equals(classname)) {
            return new LibPQFactory(info);
        }
        try {
            return ObjectFactory.instantiate(SSLSocketFactory.class, classname, info, true, PGProperty.SSL_FACTORY_ARG.get(info));
        }
        catch (final Exception e) {
            throw new PSQLException(GT.tr("The SSLSocketFactory class provided {0} could not be instantiated.", classname), PSQLState.CONNECTION_FAILURE, e);
        }
    }
}
