// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import java.util.Map;
import java.lang.reflect.Constructor;
import org.codehaus.jackson.type.JavaType;

@Deprecated
public class MapDeserializer extends org.codehaus.jackson.map.deser.std.MapDeserializer
{
    @Deprecated
    public MapDeserializer(final JavaType mapType, final Constructor<Map<Object, Object>> defCtor, final KeyDeserializer keyDeser, final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser) {
        super(mapType, defCtor, keyDeser, valueDeser, valueTypeDeser);
    }
    
    public MapDeserializer(final JavaType mapType, final ValueInstantiator valueInstantiator, final KeyDeserializer keyDeser, final JsonDeserializer<Object> valueDeser, final TypeDeserializer valueTypeDeser) {
        super(mapType, valueInstantiator, keyDeser, valueDeser, valueTypeDeser);
    }
    
    protected MapDeserializer(final MapDeserializer src) {
        super(src);
    }
}
