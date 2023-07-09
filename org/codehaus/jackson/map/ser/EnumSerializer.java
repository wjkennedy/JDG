// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.util.EnumValues;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@Deprecated
@JacksonStdImpl
public class EnumSerializer extends org.codehaus.jackson.map.ser.std.EnumSerializer
{
    public EnumSerializer(final EnumValues v) {
        super(v);
    }
}
