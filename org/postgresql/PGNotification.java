// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql;

public interface PGNotification
{
    String getName();
    
    int getPID();
    
    String getParameter();
}
