// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.logging.Level;
import java.util.Timer;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedTimer
{
    private static final AtomicInteger timerCount;
    private static final Logger LOGGER;
    private volatile Timer timer;
    private final AtomicInteger refCount;
    
    public SharedTimer() {
        this.refCount = new AtomicInteger(0);
    }
    
    public int getRefCount() {
        return this.refCount.get();
    }
    
    public synchronized Timer getTimer() {
        Timer timer = this.timer;
        if (timer == null) {
            final int index = SharedTimer.timerCount.incrementAndGet();
            final ClassLoader prevContextCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(null);
                timer = (this.timer = new Timer("PostgreSQL-JDBC-SharedTimer-" + index, true));
            }
            finally {
                Thread.currentThread().setContextClassLoader(prevContextCL);
            }
        }
        this.refCount.incrementAndGet();
        return timer;
    }
    
    public synchronized void releaseTimer() {
        final int count = this.refCount.decrementAndGet();
        if (count > 0) {
            SharedTimer.LOGGER.log(Level.FINEST, "Outstanding references still exist so not closing shared Timer");
        }
        else if (count == 0) {
            SharedTimer.LOGGER.log(Level.FINEST, "No outstanding references to shared Timer, will cancel and close it");
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
        }
        else {
            SharedTimer.LOGGER.log(Level.WARNING, "releaseTimer() called too many times; there is probably a bug in the calling code");
            this.refCount.set(0);
        }
    }
    
    static {
        timerCount = new AtomicInteger(0);
        LOGGER = Logger.getLogger(SharedTimer.class.getName());
    }
}
