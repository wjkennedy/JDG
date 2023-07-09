// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import java.util.HashMap;

@Deprecated
public final class EnumResolver<T extends Enum<T>> extends org.codehaus.jackson.map.util.EnumResolver<T>
{
    private EnumResolver(final Class<T> enumClass, final T[] enums, final HashMap<String, T> map) {
        super(enumClass, enums, map);
    }
}
