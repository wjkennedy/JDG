// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.type.JavaType;

public abstract class ClassIntrospector<T extends BeanDescription>
{
    protected ClassIntrospector() {
    }
    
    public abstract T forSerialization(final SerializationConfig p0, final JavaType p1, final MixInResolver p2);
    
    public abstract T forDeserialization(final DeserializationConfig p0, final JavaType p1, final MixInResolver p2);
    
    public abstract T forCreation(final DeserializationConfig p0, final JavaType p1, final MixInResolver p2);
    
    public abstract T forClassAnnotations(final MapperConfig<?> p0, final JavaType p1, final MixInResolver p2);
    
    public abstract T forDirectClassAnnotations(final MapperConfig<?> p0, final JavaType p1, final MixInResolver p2);
    
    @Deprecated
    public T forClassAnnotations(final MapperConfig<?> cfg, final Class<?> cls, final MixInResolver r) {
        return this.forClassAnnotations(cfg, cfg.constructType(cls), r);
    }
    
    @Deprecated
    public T forDirectClassAnnotations(final MapperConfig<?> cfg, final Class<?> cls, final MixInResolver r) {
        return this.forDirectClassAnnotations(cfg, cfg.constructType(cls), r);
    }
    
    public interface MixInResolver
    {
        Class<?> findMixInClassFor(final Class<?> p0);
    }
}
