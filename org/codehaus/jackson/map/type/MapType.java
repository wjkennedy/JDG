// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import org.codehaus.jackson.type.JavaType;

public final class MapType extends MapLikeType
{
    @Deprecated
    private MapType(final Class<?> mapType, final JavaType keyT, final JavaType valueT) {
        this(mapType, keyT, valueT, null, null);
    }
    
    private MapType(final Class<?> mapType, final JavaType keyT, final JavaType valueT, final Object valueHandler, final Object typeHandler) {
        super(mapType, keyT, valueT, valueHandler, typeHandler);
    }
    
    public static MapType construct(final Class<?> rawType, final JavaType keyT, final JavaType valueT) {
        return new MapType(rawType, keyT, valueT, null, null);
    }
    
    @Override
    protected JavaType _narrow(final Class<?> subclass) {
        return new MapType(subclass, this._keyType, this._valueType, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType narrowContentsBy(final Class<?> contentClass) {
        if (contentClass == this._valueType.getRawClass()) {
            return this;
        }
        return new MapType(this._class, this._keyType, this._valueType.narrowBy(contentClass), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType widenContentsBy(final Class<?> contentClass) {
        if (contentClass == this._valueType.getRawClass()) {
            return this;
        }
        return new MapType(this._class, this._keyType, this._valueType.widenBy(contentClass), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType narrowKey(final Class<?> keySubclass) {
        if (keySubclass == this._keyType.getRawClass()) {
            return this;
        }
        return new MapType(this._class, this._keyType.narrowBy(keySubclass), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType widenKey(final Class<?> keySubclass) {
        if (keySubclass == this._keyType.getRawClass()) {
            return this;
        }
        return new MapType(this._class, this._keyType.widenBy(keySubclass), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public MapType withTypeHandler(final Object h) {
        return new MapType(this._class, this._keyType, this._valueType, this._valueHandler, h);
    }
    
    @Override
    public MapType withContentTypeHandler(final Object h) {
        return new MapType(this._class, this._keyType, this._valueType.withTypeHandler(h), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public MapType withValueHandler(final Object h) {
        return new MapType(this._class, this._keyType, this._valueType, h, this._typeHandler);
    }
    
    @Override
    public MapType withContentValueHandler(final Object h) {
        return new MapType(this._class, this._keyType, this._valueType.withValueHandler(h), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public MapType withKeyTypeHandler(final Object h) {
        return new MapType(this._class, this._keyType.withTypeHandler(h), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public MapType withKeyValueHandler(final Object h) {
        return new MapType(this._class, this._keyType.withValueHandler(h), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public String toString() {
        return "[map type; class " + this._class.getName() + ", " + this._keyType + " -> " + this._valueType + "]";
    }
}
