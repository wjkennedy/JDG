// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.util.EnumResolver;

@Deprecated
public class EnumDeserializer extends org.codehaus.jackson.map.deser.std.EnumDeserializer
{
    public EnumDeserializer(final EnumResolver<?> res) {
        super(res);
    }
}
