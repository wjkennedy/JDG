// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.type.JavaType;
import java.util.HashSet;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@Deprecated
@JacksonStdImpl
public class MapSerializer extends org.codehaus.jackson.map.ser.std.MapSerializer
{
    protected MapSerializer() {
        this(null, null, null, false, null, null, null, null);
    }
    
    @Deprecated
    protected MapSerializer(final HashSet<String> ignoredEntries, final JavaType valueType, final boolean valueTypeIsStatic, final TypeSerializer vts) {
        super(ignoredEntries, MapSerializer.UNSPECIFIED_TYPE, valueType, valueTypeIsStatic, vts, null, null, null);
    }
    
    @Deprecated
    protected MapSerializer(final HashSet<String> ignoredEntries, final JavaType keyType, final JavaType valueType, final boolean valueTypeIsStatic, final TypeSerializer vts, final JsonSerializer<Object> keySerializer, final BeanProperty property) {
        super(ignoredEntries, keyType, valueType, valueTypeIsStatic, vts, keySerializer, null, property);
    }
    
    protected MapSerializer(final HashSet<String> ignoredEntries, final JavaType keyType, final JavaType valueType, final boolean valueTypeIsStatic, final TypeSerializer vts, final JsonSerializer<Object> keySerializer, final JsonSerializer<Object> valueSerializer, final BeanProperty property) {
        super(ignoredEntries, keyType, valueType, valueTypeIsStatic, vts, keySerializer, valueSerializer, property);
    }
}
