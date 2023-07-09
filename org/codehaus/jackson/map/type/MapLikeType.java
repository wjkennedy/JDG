// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import java.util.Map;
import org.codehaus.jackson.type.JavaType;

public class MapLikeType extends TypeBase
{
    protected final JavaType _keyType;
    protected final JavaType _valueType;
    
    @Deprecated
    protected MapLikeType(final Class<?> mapType, final JavaType keyT, final JavaType valueT) {
        super(mapType, keyT.hashCode() ^ valueT.hashCode(), null, null);
        this._keyType = keyT;
        this._valueType = valueT;
    }
    
    protected MapLikeType(final Class<?> mapType, final JavaType keyT, final JavaType valueT, final Object valueHandler, final Object typeHandler) {
        super(mapType, keyT.hashCode() ^ valueT.hashCode(), valueHandler, typeHandler);
        this._keyType = keyT;
        this._valueType = valueT;
    }
    
    public static MapLikeType construct(final Class<?> rawType, final JavaType keyT, final JavaType valueT) {
        return new MapLikeType(rawType, keyT, valueT, null, null);
    }
    
    @Override
    protected JavaType _narrow(final Class<?> subclass) {
        return new MapLikeType(subclass, this._keyType, this._valueType, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType narrowContentsBy(final Class<?> contentClass) {
        if (contentClass == this._valueType.getRawClass()) {
            return this;
        }
        return new MapLikeType(this._class, this._keyType, this._valueType.narrowBy(contentClass), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType widenContentsBy(final Class<?> contentClass) {
        if (contentClass == this._valueType.getRawClass()) {
            return this;
        }
        return new MapLikeType(this._class, this._keyType, this._valueType.widenBy(contentClass), this._valueHandler, this._typeHandler);
    }
    
    public JavaType narrowKey(final Class<?> keySubclass) {
        if (keySubclass == this._keyType.getRawClass()) {
            return this;
        }
        return new MapLikeType(this._class, this._keyType.narrowBy(keySubclass), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    public JavaType widenKey(final Class<?> keySubclass) {
        if (keySubclass == this._keyType.getRawClass()) {
            return this;
        }
        return new MapLikeType(this._class, this._keyType.widenBy(keySubclass), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public MapLikeType withTypeHandler(final Object h) {
        return new MapLikeType(this._class, this._keyType, this._valueType, this._valueHandler, h);
    }
    
    @Override
    public MapLikeType withContentTypeHandler(final Object h) {
        return new MapLikeType(this._class, this._keyType, this._valueType.withTypeHandler(h), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public MapLikeType withValueHandler(final Object h) {
        return new MapLikeType(this._class, this._keyType, this._valueType, h, this._typeHandler);
    }
    
    @Override
    public MapLikeType withContentValueHandler(final Object h) {
        return new MapLikeType(this._class, this._keyType, this._valueType.withValueHandler(h), this._valueHandler, this._typeHandler);
    }
    
    @Override
    protected String buildCanonicalName() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this._class.getName());
        if (this._keyType != null) {
            sb.append('<');
            sb.append(this._keyType.toCanonical());
            sb.append(',');
            sb.append(this._valueType.toCanonical());
            sb.append('>');
        }
        return sb.toString();
    }
    
    @Override
    public boolean isContainerType() {
        return true;
    }
    
    @Override
    public boolean isMapLikeType() {
        return true;
    }
    
    @Override
    public JavaType getKeyType() {
        return this._keyType;
    }
    
    @Override
    public JavaType getContentType() {
        return this._valueType;
    }
    
    @Override
    public int containedTypeCount() {
        return 2;
    }
    
    @Override
    public JavaType containedType(final int index) {
        if (index == 0) {
            return this._keyType;
        }
        if (index == 1) {
            return this._valueType;
        }
        return null;
    }
    
    @Override
    public String containedTypeName(final int index) {
        if (index == 0) {
            return "K";
        }
        if (index == 1) {
            return "V";
        }
        return null;
    }
    
    @Override
    public StringBuilder getErasedSignature(final StringBuilder sb) {
        return TypeBase._classSignature(this._class, sb, true);
    }
    
    @Override
    public StringBuilder getGenericSignature(final StringBuilder sb) {
        TypeBase._classSignature(this._class, sb, false);
        sb.append('<');
        this._keyType.getGenericSignature(sb);
        this._valueType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }
    
    public MapLikeType withKeyTypeHandler(final Object h) {
        return new MapLikeType(this._class, this._keyType.withTypeHandler(h), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    public MapLikeType withKeyValueHandler(final Object h) {
        return new MapLikeType(this._class, this._keyType.withValueHandler(h), this._valueType, this._valueHandler, this._typeHandler);
    }
    
    public boolean isTrueMapType() {
        return Map.class.isAssignableFrom(this._class);
    }
    
    @Override
    public String toString() {
        return "[map-like type; class " + this._class.getName() + ", " + this._keyType + " -> " + this._valueType + "]";
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
        final MapLikeType other = (MapLikeType)o;
        return this._class == other._class && this._keyType.equals(other._keyType) && this._valueType.equals(other._valueType);
    }
}
