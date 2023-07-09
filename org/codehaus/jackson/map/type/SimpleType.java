// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.type.JavaType;

public final class SimpleType extends TypeBase
{
    protected final JavaType[] _typeParameters;
    protected final String[] _typeNames;
    
    protected SimpleType(final Class<?> cls) {
        this(cls, null, null, null, null);
    }
    
    @Deprecated
    protected SimpleType(final Class<?> cls, final String[] typeNames, final JavaType[] typeParams) {
        this(cls, typeNames, typeParams, null, null);
    }
    
    protected SimpleType(final Class<?> cls, final String[] typeNames, final JavaType[] typeParams, final Object valueHandler, final Object typeHandler) {
        super(cls, 0, valueHandler, typeHandler);
        if (typeNames == null || typeNames.length == 0) {
            this._typeNames = null;
            this._typeParameters = null;
        }
        else {
            this._typeNames = typeNames;
            this._typeParameters = typeParams;
        }
    }
    
    public static SimpleType constructUnsafe(final Class<?> raw) {
        return new SimpleType(raw, null, null, null, null);
    }
    
    @Override
    protected JavaType _narrow(final Class<?> subclass) {
        return new SimpleType(subclass, this._typeNames, this._typeParameters, this._valueHandler, this._typeHandler);
    }
    
    @Override
    public JavaType narrowContentsBy(final Class<?> subclass) {
        throw new IllegalArgumentException("Internal error: SimpleType.narrowContentsBy() should never be called");
    }
    
    @Override
    public JavaType widenContentsBy(final Class<?> subclass) {
        throw new IllegalArgumentException("Internal error: SimpleType.widenContentsBy() should never be called");
    }
    
    public static SimpleType construct(final Class<?> cls) {
        if (Map.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Can not construct SimpleType for a Map (class: " + cls.getName() + ")");
        }
        if (Collection.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Can not construct SimpleType for a Collection (class: " + cls.getName() + ")");
        }
        if (cls.isArray()) {
            throw new IllegalArgumentException("Can not construct SimpleType for an array (class: " + cls.getName() + ")");
        }
        return new SimpleType(cls);
    }
    
    @Override
    public SimpleType withTypeHandler(final Object h) {
        return new SimpleType(this._class, this._typeNames, this._typeParameters, this._valueHandler, h);
    }
    
    @Override
    public JavaType withContentTypeHandler(final Object h) {
        throw new IllegalArgumentException("Simple types have no content types; can not call withContenTypeHandler()");
    }
    
    @Override
    public SimpleType withValueHandler(final Object h) {
        if (h == this._valueHandler) {
            return this;
        }
        return new SimpleType(this._class, this._typeNames, this._typeParameters, h, this._typeHandler);
    }
    
    @Override
    public SimpleType withContentValueHandler(final Object h) {
        throw new IllegalArgumentException("Simple types have no content types; can not call withContenValueHandler()");
    }
    
    @Override
    protected String buildCanonicalName() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this._class.getName());
        if (this._typeParameters != null && this._typeParameters.length > 0) {
            sb.append('<');
            boolean first = true;
            for (final JavaType t : this._typeParameters) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append(',');
                }
                sb.append(t.toCanonical());
            }
            sb.append('>');
        }
        return sb.toString();
    }
    
    @Override
    public boolean isContainerType() {
        return false;
    }
    
    @Override
    public int containedTypeCount() {
        return (this._typeParameters == null) ? 0 : this._typeParameters.length;
    }
    
    @Override
    public JavaType containedType(final int index) {
        if (index < 0 || this._typeParameters == null || index >= this._typeParameters.length) {
            return null;
        }
        return this._typeParameters[index];
    }
    
    @Override
    public String containedTypeName(final int index) {
        if (index < 0 || this._typeNames == null || index >= this._typeNames.length) {
            return null;
        }
        return this._typeNames[index];
    }
    
    @Override
    public StringBuilder getErasedSignature(final StringBuilder sb) {
        return TypeBase._classSignature(this._class, sb, true);
    }
    
    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        TypeBase._classSignature(this._class, sb, false);
        if (this._typeParameters != null) {
            sb.append('<');
            for (final JavaType param : this._typeParameters) {
                sb = param.getGenericSignature(sb);
            }
            sb.append('>');
        }
        sb.append(';');
        return sb;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(40);
        sb.append("[simple type, class ").append(this.buildCanonicalName()).append(']');
        return sb.toString();
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
        final SimpleType other = (SimpleType)o;
        if (other._class != this._class) {
            return false;
        }
        final JavaType[] p1 = this._typeParameters;
        final JavaType[] p2 = other._typeParameters;
        if (p1 == null) {
            return p2 == null || p2.length == 0;
        }
        if (p2 == null) {
            return false;
        }
        if (p1.length != p2.length) {
            return false;
        }
        for (int i = 0, len = p1.length; i < len; ++i) {
            if (!p1[i].equals(p2[i])) {
                return false;
            }
        }
        return true;
    }
}
