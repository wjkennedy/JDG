// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.module;

import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.DeserializationConfig;
import java.lang.reflect.Modifier;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import org.codehaus.jackson.map.AbstractTypeResolver;

public class SimpleAbstractTypeResolver extends AbstractTypeResolver
{
    protected final HashMap<ClassKey, Class<?>> _mappings;
    
    public SimpleAbstractTypeResolver() {
        this._mappings = new HashMap<ClassKey, Class<?>>();
    }
    
    public <T> SimpleAbstractTypeResolver addMapping(final Class<T> superType, final Class<? extends T> subType) {
        if (superType == subType) {
            throw new IllegalArgumentException("Can not add mapping from class to itself");
        }
        if (!superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException("Can not add mapping from class " + superType.getName() + " to " + subType.getName() + ", as latter is not a subtype of former");
        }
        if (!Modifier.isAbstract(superType.getModifiers())) {
            throw new IllegalArgumentException("Can not add mapping from class " + superType.getName() + " since it is not abstract");
        }
        this._mappings.put(new ClassKey(superType), subType);
        return this;
    }
    
    @Override
    public JavaType findTypeMapping(final DeserializationConfig config, final JavaType type) {
        final Class<?> src = type.getRawClass();
        final Class<?> dst = this._mappings.get(new ClassKey(src));
        if (dst == null) {
            return null;
        }
        return type.narrowBy(dst);
    }
    
    @Override
    public JavaType resolveAbstractType(final DeserializationConfig config, final JavaType type) {
        return null;
    }
}
