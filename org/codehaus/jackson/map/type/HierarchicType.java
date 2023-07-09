// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class HierarchicType
{
    protected final Type _actualType;
    protected final Class<?> _rawClass;
    protected final ParameterizedType _genericType;
    protected HierarchicType _superType;
    protected HierarchicType _subType;
    
    public HierarchicType(final Type type) {
        this._actualType = type;
        if (type instanceof Class) {
            this._rawClass = (Class)type;
            this._genericType = null;
        }
        else {
            if (!(type instanceof ParameterizedType)) {
                throw new IllegalArgumentException("Type " + type.getClass().getName() + " can not be used to construct HierarchicType");
            }
            this._genericType = (ParameterizedType)type;
            this._rawClass = (Class)this._genericType.getRawType();
        }
    }
    
    private HierarchicType(final Type actualType, final Class<?> rawClass, final ParameterizedType genericType, final HierarchicType superType, final HierarchicType subType) {
        this._actualType = actualType;
        this._rawClass = rawClass;
        this._genericType = genericType;
        this._superType = superType;
        this._subType = subType;
    }
    
    public HierarchicType deepCloneWithoutSubtype() {
        final HierarchicType sup = (this._superType == null) ? null : this._superType.deepCloneWithoutSubtype();
        final HierarchicType result = new HierarchicType(this._actualType, this._rawClass, this._genericType, sup, null);
        if (sup != null) {
            sup.setSubType(result);
        }
        return result;
    }
    
    public void setSuperType(final HierarchicType sup) {
        this._superType = sup;
    }
    
    public final HierarchicType getSuperType() {
        return this._superType;
    }
    
    public void setSubType(final HierarchicType sub) {
        this._subType = sub;
    }
    
    public final HierarchicType getSubType() {
        return this._subType;
    }
    
    public final boolean isGeneric() {
        return this._genericType != null;
    }
    
    public final ParameterizedType asGeneric() {
        return this._genericType;
    }
    
    public final Class<?> getRawClass() {
        return this._rawClass;
    }
    
    @Override
    public String toString() {
        if (this._genericType != null) {
            return this._genericType.toString();
        }
        return this._rawClass.getName();
    }
}
