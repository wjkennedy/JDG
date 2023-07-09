// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.util.Iterator;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import org.codehaus.jackson.map.util.Annotations;

public final class AnnotationMap implements Annotations
{
    protected HashMap<Class<? extends Annotation>, Annotation> _annotations;
    
    public AnnotationMap() {
    }
    
    private AnnotationMap(final HashMap<Class<? extends Annotation>, Annotation> a) {
        this._annotations = a;
    }
    
    public <A extends Annotation> A get(final Class<A> cls) {
        if (this._annotations == null) {
            return null;
        }
        return (A)this._annotations.get(cls);
    }
    
    public static AnnotationMap merge(final AnnotationMap primary, final AnnotationMap secondary) {
        if (primary == null || primary._annotations == null || primary._annotations.isEmpty()) {
            return secondary;
        }
        if (secondary == null || secondary._annotations == null || secondary._annotations.isEmpty()) {
            return primary;
        }
        final HashMap<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
        for (final Annotation ann : secondary._annotations.values()) {
            annotations.put(ann.annotationType(), ann);
        }
        for (final Annotation ann : primary._annotations.values()) {
            annotations.put(ann.annotationType(), ann);
        }
        return new AnnotationMap(annotations);
    }
    
    public int size() {
        return (this._annotations == null) ? 0 : this._annotations.size();
    }
    
    public void addIfNotPresent(final Annotation ann) {
        if (this._annotations == null || !this._annotations.containsKey(ann.annotationType())) {
            this._add(ann);
        }
    }
    
    public void add(final Annotation ann) {
        this._add(ann);
    }
    
    @Override
    public String toString() {
        if (this._annotations == null) {
            return "[null]";
        }
        return this._annotations.toString();
    }
    
    protected final void _add(final Annotation ann) {
        if (this._annotations == null) {
            this._annotations = new HashMap<Class<? extends Annotation>, Annotation>();
        }
        this._annotations.put(ann.annotationType(), ann);
    }
}
