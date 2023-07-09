// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.PGNotification;

public class Notification implements PGNotification
{
    private final String name;
    private final String parameter;
    private final int pid;
    
    public Notification(final String name, final int pid) {
        this(name, pid, "");
    }
    
    public Notification(final String name, final int pid, final String parameter) {
        this.name = name;
        this.pid = pid;
        this.parameter = parameter;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public int getPID() {
        return this.pid;
    }
    
    @Override
    public String getParameter() {
        return this.parameter;
    }
}
