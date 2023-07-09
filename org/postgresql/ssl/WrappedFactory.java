// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import javax.net.ssl.SSLSocketFactory;

public abstract class WrappedFactory extends SSLSocketFactory
{
    protected SSLSocketFactory factory;
    
    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return this.factory.createSocket(host, port);
    }
    
    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return this.factory.createSocket(host, port);
    }
    
    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort) throws IOException {
        return this.factory.createSocket(host, port, localHost, localPort);
    }
    
    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress, final int localPort) throws IOException {
        return this.factory.createSocket(address, port, localAddress, localPort);
    }
    
    @Override
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException {
        return this.factory.createSocket(socket, host, port, autoClose);
    }
    
    @Override
    public String[] getDefaultCipherSuites() {
        return this.factory.getDefaultCipherSuites();
    }
    
    @Override
    public String[] getSupportedCipherSuites() {
        return this.factory.getSupportedCipherSuites();
    }
}
