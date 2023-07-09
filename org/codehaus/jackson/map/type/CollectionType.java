// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import org.codehaus.jackson.type.JavaType;

public final class CollectionType extends CollectionLikeType
{
    private CollectionType(final Class<?> collT, final JavaType elemT, final Object valueHandler, final Object typeHandler) {
        super(collT, elemT, valueHandler, typeHandler);
    }
    
    @Override
    protected JavaType _narrow(final Class<?> subclass) {
        return new CollectionType(subclass, this._elementType, null, null);
    }
    
    @Override
    public JavaType narrowContentsBy(final Class<?> contentClass) {
        if (contentClass == this._elementType.getRawClass()) {
            return this;
        }
        return new CollectionType(this._class, this._elementType.narrowBy(contentClass), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType widenContentsBy(final Class<?> contentClass) {
        if (contentClass == this._elementType.getRawClass()) {
            return this;
        }
        return new CollectionType(this._class, this._elementType.widenBy(contentClass), this._valueHandler, this._typeHandler);
    }
    
    public static CollectionType construct(final Class<?> rawType, final JavaType elemT) {
        return new CollectionType(rawType, elemT, null, null);
    }
    
    @Override
    public CollectionType withTypeHandler(final Object h) {
        return new CollectionType(this._class, this._elementType, this._valueHandler, h);
    }
    
    @Override
    public CollectionType withContentTypeHandler(final Object h) {
        return new CollectionType(this._class, this._elementType.withTypeHandler(h), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public CollectionType withValueHandler(final Object h) {
        return new CollectionType(this._class, this._elementType, h, this._typeHandler);
    }
    
    @Override
    public CollectionType withContentValueHandler(final Object h) {
        return new CollectionType(this._class, this._elementType.withValueHandler(h), this._valueHandler, this._typeHandler);
    }
    
    @Override
    public String toString() {
        return "[collection type; class " + this._class.getName() + ", contains " + this._elementType + "]";
    }
}
