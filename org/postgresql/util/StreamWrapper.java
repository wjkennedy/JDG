// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.io.ByteArrayInputStream;
import org.postgresql.util.internal.Nullness;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamWrapper
{
    private static final int MAX_MEMORY_BUFFER_BYTES = 51200;
    private static final String TEMP_FILE_PREFIX = "postgres-pgjdbc-stream";
    private final InputStream stream;
    private final byte[] rawData;
    private final int offset;
    private final int length;
    
    public StreamWrapper(final byte[] data, final int offset, final int length) {
        this.stream = null;
        this.rawData = data;
        this.offset = offset;
        this.length = length;
    }
    
    public StreamWrapper(final InputStream stream, final int length) {
        this.stream = stream;
        this.rawData = null;
        this.offset = 0;
        this.length = length;
    }
    
    public StreamWrapper(final InputStream stream) throws PSQLException {
        try {
            final ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream();
            final int memoryLength = copyStream(stream, memoryOutputStream, 51200);
            final byte[] rawData = memoryOutputStream.toByteArray();
            if (memoryLength == -1) {
                final File tempFile = File.createTempFile("postgres-pgjdbc-stream", null);
                final FileOutputStream diskOutputStream = new FileOutputStream(tempFile);
                diskOutputStream.write(rawData);
                int diskLength;
                try {
                    diskLength = copyStream(stream, diskOutputStream, Integer.MAX_VALUE - rawData.length);
                    if (diskLength == -1) {
                        throw new PSQLException(GT.tr("Object is too large to send over the protocol.", new Object[0]), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE);
                    }
                    diskOutputStream.flush();
                }
                finally {
                    diskOutputStream.close();
                }
                this.offset = 0;
                this.length = rawData.length + diskLength;
                this.rawData = null;
                this.stream = new FileInputStream(tempFile) {
                    private boolean closed = false;
                    private int position = 0;
                    
                    private void checkShouldClose(final int readResult) throws IOException {
                        if (readResult == -1) {
                            this.close();
                        }
                        else {
                            this.position += readResult;
                            if (this.position >= StreamWrapper.this.length) {
                                this.close();
                            }
                        }
                    }
                    
                    @Override
                    public int read(final byte[] b) throws IOException {
                        if (this.closed) {
                            return -1;
                        }
                        final int result = super.read(b);
                        this.checkShouldClose(result);
                        return result;
                    }
                    
                    @Override
                    public int read(final byte[] b, final int off, final int len) throws IOException {
                        if (this.closed) {
                            return -1;
                        }
                        final int result = super.read(b, off, len);
                        this.checkShouldClose(result);
                        return result;
                    }
                    
                    @Override
                    public void close() throws IOException {
                        if (!this.closed) {
                            super.close();
                            tempFile.delete();
                            this.closed = true;
                        }
                    }
                    
                    @Override
                    protected void finalize() throws IOException {
                        this.close();
                        try {
                            super.finalize();
                        }
                        catch (final RuntimeException e) {
                            throw e;
                        }
                        catch (final Error e2) {
                            throw e2;
                        }
                        catch (final IOException e3) {
                            throw e3;
                        }
                        catch (final Throwable e4) {
                            throw new RuntimeException("Unexpected exception from finalize", e4);
                        }
                    }
                };
            }
            else {
                this.rawData = rawData;
                this.stream = null;
                this.offset = 0;
                this.length = rawData.length;
            }
        }
        catch (final IOException e) {
            throw new PSQLException(GT.tr("An I/O error occurred while sending to the backend.", new Object[0]), PSQLState.IO_ERROR, e);
        }
    }
    
    public InputStream getStream() {
        if (this.stream != null) {
            return this.stream;
        }
        return new ByteArrayInputStream(Nullness.castNonNull(this.rawData), this.offset, this.length);
    }
    
    public int getLength() {
        return this.length;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    public byte[] getBytes() {
        return this.rawData;
    }
    
    @Override
    public String toString() {
        return "<stream of " + this.length + " bytes>";
    }
    
    private static int copyStream(final InputStream inputStream, final OutputStream outputStream, final int limit) throws IOException {
        int totalLength = 0;
        final byte[] buffer = new byte[2048];
        for (int readLength = inputStream.read(buffer); readLength > 0; readLength = inputStream.read(buffer)) {
            totalLength += readLength;
            outputStream.write(buffer, 0, readLength);
            if (totalLength >= limit) {
                return -1;
            }
        }
        return totalLength;
    }
}
