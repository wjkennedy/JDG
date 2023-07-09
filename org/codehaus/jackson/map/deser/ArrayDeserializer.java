// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.deser.std.ObjectArrayDeserializer;

@Deprecated
public class ArrayDeserializer extends ObjectArrayDeserializer
{
    @Deprecated
    public ArrayDeserializer(final ArrayType arrayType, final JsonDeserializer<Object> elemDeser) {
        this(arrayType, elemDeser, null);
    }
    
    public ArrayDeserializer(final ArrayType arrayType, final JsonDeserializer<Object> elemDeser, final TypeDeserializer elemTypeDeser) {
        super(arrayType, elemDeser, elemTypeDeser);
    }
}
