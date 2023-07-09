// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.io.Writer;
import java.util.logging.Handler;

public class LogWriterHandler extends Handler
{
    private Writer writer;
    private final Object lock;
    
    public LogWriterHandler(final Writer inWriter) {
        this.lock = new Object();
        this.setLevel(Level.INFO);
        this.setFilter(null);
        this.setFormatter(new SimpleFormatter());
        this.setWriter(inWriter);
    }
    
    @Override
    public void publish(final LogRecord record) {
        final Formatter formatter = this.getFormatter();
        String formatted;
        try {
            formatted = formatter.format(record);
        }
        catch (final Exception ex) {
            this.reportError("Error Formatting record", ex, 5);
            return;
        }
        if (formatted.length() == 0) {
            return;
        }
        try {
            synchronized (this.lock) {
                final Writer writer = this.writer;
                if (writer != null) {
                    writer.write(formatted);
                }
            }
        }
        catch (final Exception ex) {
            this.reportError("Error writing message", ex, 1);
        }
    }
    
    @Override
    public void flush() {
        try {
            final Writer writer = this.writer;
            if (writer != null) {
                writer.flush();
            }
        }
        catch (final Exception ex) {
            this.reportError("Error on flush", ex, 1);
        }
    }
    
    @Override
    public void close() throws SecurityException {
        try (final Writer writer = this.writer) {}
        catch (final Exception ex) {
            this.reportError("Error closing writer", ex, 1);
        }
    }
    
    private void setWriter(final Writer writer) throws IllegalArgumentException {
        if (writer == null) {
            throw new IllegalArgumentException("Writer cannot be null");
        }
        this.writer = writer;
        try {
            writer.write(this.getFormatter().getHead(this));
        }
        catch (final Exception ex) {
            this.reportError("Error writing head section", ex, 1);
        }
    }
}
