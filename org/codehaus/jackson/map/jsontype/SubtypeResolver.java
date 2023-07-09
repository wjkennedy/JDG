// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype;

import org.codehaus.jackson.map.introspect.AnnotatedClass;
import java.util.Collection;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.introspect.AnnotatedMember;

public abstract class SubtypeResolver
{
    public abstract void registerSubtypes(final NamedType... p0);
    
    public abstract void registerSubtypes(final Class<?>... p0);
    
    public abstract Collection<NamedType> collectAndResolveSubtypes(final AnnotatedMember p0, final MapperConfig<?> p1, final AnnotationIntrospector p2);
    
    public abstract Collection<NamedType> collectAndResolveSubtypes(final AnnotatedClass p0, final MapperConfig<?> p1, final AnnotationIntrospector p2);
}
