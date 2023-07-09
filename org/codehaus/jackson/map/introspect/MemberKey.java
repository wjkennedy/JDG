// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class MemberKey
{
    static final Class<?>[] NO_CLASSES;
    final String _name;
    final Class<?>[] _argTypes;
    
    public MemberKey(final Method m) {
        this(m.getName(), m.getParameterTypes());
    }
    
    public MemberKey(final Constructor<?> ctor) {
        this("", ctor.getParameterTypes());
    }
    
    public MemberKey(final String name, final Class<?>[] argTypes) {
        this._name = name;
        this._argTypes = ((argTypes == null) ? MemberKey.NO_CLASSES : argTypes);
    }
    
    @Override
    public String toString() {
        return this._name + "(" + this._argTypes.length + "-args)";
    }
    
    @Override
    public int hashCode() {
        return this._name.hashCode() + this._argTypes.length;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        final MemberKey other = (MemberKey)o;
        if (!this._name.equals(other._name)) {
            return false;
        }
        final Class<?>[] otherArgs = other._argTypes;
        final int len = this._argTypes.length;
        if (otherArgs.length != len) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            final Class<?> type1 = otherArgs[i];
            final Class<?> type2 = this._argTypes[i];
            if (type1 != type2) {
                if (!type1.isAssignableFrom(type2)) {
                    if (!type2.isAssignableFrom(type1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    static {
        NO_CLASSES = new Class[0];
    }
}
