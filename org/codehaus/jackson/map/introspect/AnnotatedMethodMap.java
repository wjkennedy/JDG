// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public final class AnnotatedMethodMap implements Iterable<AnnotatedMethod>
{
    protected LinkedHashMap<MemberKey, AnnotatedMethod> _methods;
    
    public void add(final AnnotatedMethod am) {
        if (this._methods == null) {
            this._methods = new LinkedHashMap<MemberKey, AnnotatedMethod>();
        }
        this._methods.put(new MemberKey(am.getAnnotated()), am);
    }
    
    public AnnotatedMethod remove(final AnnotatedMethod am) {
        return this.remove(am.getAnnotated());
    }
    
    public AnnotatedMethod remove(final Method m) {
        if (this._methods != null) {
            return this._methods.remove(new MemberKey(m));
        }
        return null;
    }
    
    public boolean isEmpty() {
        return this._methods == null || this._methods.size() == 0;
    }
    
    public int size() {
        return (this._methods == null) ? 0 : this._methods.size();
    }
    
    public AnnotatedMethod find(final String name, final Class<?>[] paramTypes) {
        if (this._methods == null) {
            return null;
        }
        return this._methods.get(new MemberKey(name, paramTypes));
    }
    
    public AnnotatedMethod find(final Method m) {
        if (this._methods == null) {
            return null;
        }
        return this._methods.get(new MemberKey(m));
    }
    
    public Iterator<AnnotatedMethod> iterator() {
        if (this._methods != null) {
            return this._methods.values().iterator();
        }
        final List<AnnotatedMethod> empty = Collections.emptyList();
        return empty.iterator();
    }
}
