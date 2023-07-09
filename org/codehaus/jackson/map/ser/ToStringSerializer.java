// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@Deprecated
@JacksonStdImpl
public final class ToStringSerializer extends org.codehaus.jackson.map.ser.std.ToStringSerializer
{
    public static final ToStringSerializer instance;
    
    static {
        instance = new ToStringSerializer();
    }
}
