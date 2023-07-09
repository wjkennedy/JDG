// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.util.PGPropertyMaxResultBufferParser;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.SQLException;
import java.io.EOFException;
import org.postgresql.util.ByteStreamWriter;
import java.io.FilterOutputStream;
import java.io.BufferedOutputStream;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.io.IOException;
import org.postgresql.gss.GSSOutputStream;
import java.io.InputStream;
import org.postgresql.gss.GSSInputStream;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.GSSContext;
import java.io.Writer;
import java.io.OutputStream;
import java.net.Socket;
import org.postgresql.util.HostSpec;
import javax.net.SocketFactory;
import java.io.Flushable;
import java.io.Closeable;

public class PGStream implements Closeable, Flushable
{
    private final SocketFactory socketFactory;
    private final HostSpec hostSpec;
    private final byte[] int4Buf;
    private final byte[] int2Buf;
    private Socket connection;
    private VisibleBufferedInputStream pgInput;
    private OutputStream pgOutput;
    private byte[] streamBuffer;
    boolean gssEncrypted;
    private long nextStreamAvailableCheckTime;
    private int minStreamAvailableCheckDelay;
    private Encoding encoding;
    private Writer encodingWriter;
    private long maxResultBuffer;
    private long resultBufferByteCount;
    
    public boolean isGssEncrypted() {
        return this.gssEncrypted;
    }
    
    public void setSecContext(final GSSContext secContext) {
        final MessageProp messageProp = new MessageProp(0, true);
        this.pgInput = new VisibleBufferedInputStream(new GSSInputStream(this.pgInput.getWrapped(), secContext, messageProp), 8192);
        this.pgOutput = new GSSOutputStream(this.pgOutput, secContext, messageProp, 16384);
        this.gssEncrypted = true;
    }
    
    public PGStream(final SocketFactory socketFactory, final HostSpec hostSpec, final int timeout) throws IOException {
        this.gssEncrypted = false;
        this.minStreamAvailableCheckDelay = 1000;
        this.maxResultBuffer = -1L;
        this.resultBufferByteCount = 0L;
        this.socketFactory = socketFactory;
        this.hostSpec = hostSpec;
        final Socket socket = this.createSocket(timeout);
        this.changeSocket(socket);
        this.setEncoding(Encoding.getJVMEncoding("UTF-8"));
        this.int2Buf = new byte[2];
        this.int4Buf = new byte[4];
    }
    
    public PGStream(final PGStream pgStream, final int timeout) throws IOException {
        this.gssEncrypted = false;
        this.minStreamAvailableCheckDelay = 1000;
        this.maxResultBuffer = -1L;
        this.resultBufferByteCount = 0L;
        int sendBufferSize = 1024;
        int receiveBufferSize = 1024;
        int soTimeout = 0;
        boolean keepAlive = false;
        try {
            sendBufferSize = pgStream.getSocket().getSendBufferSize();
            receiveBufferSize = pgStream.getSocket().getReceiveBufferSize();
            soTimeout = pgStream.getSocket().getSoTimeout();
            keepAlive = pgStream.getSocket().getKeepAlive();
        }
        catch (final SocketException ex) {}
        pgStream.close();
        this.socketFactory = pgStream.socketFactory;
        this.hostSpec = pgStream.hostSpec;
        final Socket socket = this.createSocket(timeout);
        this.changeSocket(socket);
        this.setEncoding(Encoding.getJVMEncoding("UTF-8"));
        socket.setReceiveBufferSize(receiveBufferSize);
        socket.setSendBufferSize(sendBufferSize);
        this.setNetworkTimeout(soTimeout);
        socket.setKeepAlive(keepAlive);
        this.int2Buf = new byte[2];
        this.int4Buf = new byte[4];
    }
    
    @Deprecated
    public PGStream(final SocketFactory socketFactory, final HostSpec hostSpec) throws IOException {
        this(socketFactory, hostSpec, 0);
    }
    
    public HostSpec getHostSpec() {
        return this.hostSpec;
    }
    
    public Socket getSocket() {
        return this.connection;
    }
    
    public SocketFactory getSocketFactory() {
        return this.socketFactory;
    }
    
    public boolean hasMessagePending() throws IOException {
        boolean available = false;
        if (this.pgInput.available() > 0) {
            return true;
        }
        final long now = System.nanoTime() / 1000000L;
        if (now < this.nextStreamAvailableCheckTime && this.minStreamAvailableCheckDelay != 0) {
            return false;
        }
        final int soTimeout = this.getNetworkTimeout();
        this.connection.setSoTimeout(1);
        try {
            if (!this.pgInput.ensureBytes(1, false)) {
                return false;
            }
            available = (this.pgInput.peek() != -1);
        }
        catch (final SocketTimeoutException e) {
            return false;
        }
        finally {
            this.connection.setSoTimeout(soTimeout);
        }
        if (!available) {
            this.nextStreamAvailableCheckTime = now + this.minStreamAvailableCheckDelay;
        }
        return available;
    }
    
    public void setMinStreamAvailableCheckDelay(final int delay) {
        this.minStreamAvailableCheckDelay = delay;
    }
    
    private Socket createSocket(final int timeout) throws IOException {
        final Socket socket = this.socketFactory.createSocket();
        if (!socket.isConnected()) {
            final InetSocketAddress address = this.hostSpec.shouldResolve() ? new InetSocketAddress(this.hostSpec.getHost(), this.hostSpec.getPort()) : InetSocketAddress.createUnresolved(this.hostSpec.getHost(), this.hostSpec.getPort());
            socket.connect(address, timeout);
        }
        return socket;
    }
    
    public void changeSocket(final Socket socket) throws IOException {
        assert this.connection != socket : "changeSocket is called with the current socket as argument. This is a no-op, however, it re-allocates buffered streams, so refrain from excessive changeSocket calls";
        (this.connection = socket).setTcpNoDelay(true);
        this.pgInput = new VisibleBufferedInputStream(this.connection.getInputStream(), 8192);
        this.pgOutput = new BufferedOutputStream(this.connection.getOutputStream(), 8192);
        if (this.encoding != null) {
            this.setEncoding(this.encoding);
        }
    }
    
    public Encoding getEncoding() {
        return this.encoding;
    }
    
    public void setEncoding(final Encoding encoding) throws IOException {
        if (this.encoding != null && this.encoding.name().equals(encoding.name())) {
            return;
        }
        if (this.encodingWriter != null) {
            this.encodingWriter.close();
        }
        this.encoding = encoding;
        final OutputStream interceptor = new FilterOutputStream(this.pgOutput) {
            @Override
            public void flush() throws IOException {
            }
            
            @Override
            public void close() throws IOException {
                super.flush();
            }
        };
        this.encodingWriter = encoding.getEncodingWriter(interceptor);
    }
    
    public Writer getEncodingWriter() throws IOException {
        if (this.encodingWriter == null) {
            throw new IOException("No encoding has been set on this connection");
        }
        return this.encodingWriter;
    }
    
    public void sendChar(final int val) throws IOException {
        this.pgOutput.write(val);
    }
    
    public void sendInteger4(final int val) throws IOException {
        this.int4Buf[0] = (byte)(val >>> 24);
        this.int4Buf[1] = (byte)(val >>> 16);
        this.int4Buf[2] = (byte)(val >>> 8);
        this.int4Buf[3] = (byte)val;
        this.pgOutput.write(this.int4Buf);
    }
    
    public void sendInteger2(final int val) throws IOException {
        if (val < -32768 || val > 32767) {
            throw new IOException("Tried to send an out-of-range integer as a 2-byte value: " + val);
        }
        this.int2Buf[0] = (byte)(val >>> 8);
        this.int2Buf[1] = (byte)val;
        this.pgOutput.write(this.int2Buf);
    }
    
    public void send(final byte[] buf) throws IOException {
        this.pgOutput.write(buf);
    }
    
    public void send(final byte[] buf, final int siz) throws IOException {
        this.send(buf, 0, siz);
    }
    
    public void send(final byte[] buf, final int off, final int siz) throws IOException {
        final int bufamt = buf.length - off;
        this.pgOutput.write(buf, off, (bufamt < siz) ? bufamt : siz);
        for (int i = bufamt; i < siz; ++i) {
            this.pgOutput.write(0);
        }
    }
    
    public void send(final ByteStreamWriter writer) throws IOException {
        final FixedLengthOutputStream fixedLengthStream = new FixedLengthOutputStream(writer.getLength(), this.pgOutput);
        try {
            writer.writeTo(new ByteStreamWriter.ByteStreamTarget() {
                @Override
                public OutputStream getOutputStream() {
                    return fixedLengthStream;
                }
            });
        }
        catch (final IOException ioe) {
            throw ioe;
        }
        catch (final Exception re) {
            throw new IOException("Error writing bytes to stream", re);
        }
        for (int i = fixedLengthStream.remaining(); i > 0; --i) {
            this.pgOutput.write(0);
        }
    }
    
    public int peekChar() throws IOException {
        final int c = this.pgInput.peek();
        if (c < 0) {
            throw new EOFException();
        }
        return c;
    }
    
    public int receiveChar() throws IOException {
        final int c = this.pgInput.read();
        if (c < 0) {
            throw new EOFException();
        }
        return c;
    }
    
    public int receiveInteger4() throws IOException {
        if (this.pgInput.read(this.int4Buf) != 4) {
            throw new EOFException();
        }
        return (this.int4Buf[0] & 0xFF) << 24 | (this.int4Buf[1] & 0xFF) << 16 | (this.int4Buf[2] & 0xFF) << 8 | (this.int4Buf[3] & 0xFF);
    }
    
    public int receiveInteger2() throws IOException {
        if (this.pgInput.read(this.int2Buf) != 2) {
            throw new EOFException();
        }
        return (this.int2Buf[0] & 0xFF) << 8 | (this.int2Buf[1] & 0xFF);
    }
    
    public String receiveString(final int len) throws IOException {
        if (!this.pgInput.ensureBytes(len)) {
            throw new EOFException();
        }
        final String res = this.encoding.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
        this.pgInput.skip(len);
        return res;
    }
    
    public EncodingPredictor.DecodeResult receiveErrorString(final int len) throws IOException {
        if (!this.pgInput.ensureBytes(len)) {
            throw new EOFException();
        }
        EncodingPredictor.DecodeResult res;
        try {
            final String value = this.encoding.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
            res = new EncodingPredictor.DecodeResult(value, null);
        }
        catch (final IOException e) {
            res = EncodingPredictor.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
            if (res == null) {
                final Encoding enc = Encoding.defaultEncoding();
                final String value2 = enc.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len);
                res = new EncodingPredictor.DecodeResult(value2, enc.name());
            }
        }
        this.pgInput.skip(len);
        return res;
    }
    
    public String receiveString() throws IOException {
        final int len = this.pgInput.scanCStringLength();
        final String res = this.encoding.decode(this.pgInput.getBuffer(), this.pgInput.getIndex(), len - 1);
        this.pgInput.skip(len);
        return res;
    }
    
    public Tuple receiveTupleV3() throws IOException, OutOfMemoryError, SQLException {
        final int messageSize = this.receiveInteger4();
        final int nf = this.receiveInteger2();
        final int dataToReadSize = messageSize - 4 - 2 - 4 * nf;
        final byte[][] answer = new byte[nf][];
        this.increaseByteCounter(dataToReadSize);
        OutOfMemoryError oom = null;
        for (int i = 0; i < nf; ++i) {
            final int size = this.receiveInteger4();
            if (size != -1) {
                try {
                    this.receive(answer[i] = new byte[size], 0, size);
                }
                catch (final OutOfMemoryError oome) {
                    oom = oome;
                    this.skip(size);
                }
            }
        }
        if (oom != null) {
            throw oom;
        }
        return new Tuple(answer);
    }
    
    public byte[] receive(final int siz) throws IOException {
        final byte[] answer = new byte[siz];
        this.receive(answer, 0, siz);
        return answer;
    }
    
    public void receive(final byte[] buf, final int off, final int siz) throws IOException {
        int w;
        for (int s = 0; s < siz; s += w) {
            w = this.pgInput.read(buf, off + s, siz - s);
            if (w < 0) {
                throw new EOFException();
            }
        }
    }
    
    public void skip(final int size) throws IOException {
        for (long s = 0L; s < size; s += this.pgInput.skip(size - s)) {}
    }
    
    public void sendStream(final InputStream inStream, int remaining) throws IOException {
        final int expectedLength = remaining;
        if (this.streamBuffer == null) {
            this.streamBuffer = new byte[8192];
        }
        while (remaining > 0) {
            int count = (remaining > this.streamBuffer.length) ? this.streamBuffer.length : remaining;
            int readCount;
            try {
                readCount = inStream.read(this.streamBuffer, 0, count);
                if (readCount < 0) {
                    throw new EOFException(GT.tr("Premature end of input stream, expected {0} bytes, but only read {1}.", expectedLength, expectedLength - remaining));
                }
            }
            catch (final IOException ioe) {
                while (remaining > 0) {
                    this.send(this.streamBuffer, count);
                    remaining -= count;
                    count = ((remaining > this.streamBuffer.length) ? this.streamBuffer.length : remaining);
                }
                throw new PGBindException(ioe);
            }
            this.send(this.streamBuffer, readCount);
            remaining -= readCount;
        }
    }
    
    @Override
    public void flush() throws IOException {
        if (this.encodingWriter != null) {
            this.encodingWriter.flush();
        }
        this.pgOutput.flush();
    }
    
    public void receiveEOF() throws SQLException, IOException {
        final int c = this.pgInput.read();
        if (c < 0) {
            return;
        }
        throw new PSQLException(GT.tr("Expected an EOF from server, got: {0}", c), PSQLState.COMMUNICATION_ERROR);
    }
    
    @Override
    public void close() throws IOException {
        if (this.encodingWriter != null) {
            this.encodingWriter.close();
        }
        this.pgOutput.close();
        this.pgInput.close();
        this.connection.close();
    }
    
    public void setNetworkTimeout(final int milliseconds) throws IOException {
        this.connection.setSoTimeout(milliseconds);
        this.pgInput.setTimeoutRequested(milliseconds != 0);
    }
    
    public int getNetworkTimeout() throws IOException {
        return this.connection.getSoTimeout();
    }
    
    public void setMaxResultBuffer(final String value) throws PSQLException {
        this.maxResultBuffer = PGPropertyMaxResultBufferParser.parseProperty(value);
    }
    
    public void clearResultBufferCount() {
        this.resultBufferByteCount = 0L;
    }
    
    private void increaseByteCounter(final long value) throws SQLException {
        if (this.maxResultBuffer != -1L) {
            this.resultBufferByteCount += value;
            if (this.resultBufferByteCount > this.maxResultBuffer) {
                throw new PSQLException(GT.tr("Result set exceeded maxResultBuffer limit. Received:  {0}; Current limit: {1}", String.valueOf(this.resultBufferByteCount), String.valueOf(this.maxResultBuffer)), PSQLState.COMMUNICATION_ERROR);
            }
        }
    }
    
    public boolean isClosed() {
        return this.connection.isClosed();
    }
}
