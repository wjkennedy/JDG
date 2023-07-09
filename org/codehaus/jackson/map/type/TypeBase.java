// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.type.JavaType;

public abstract class TypeBase extends JavaType implements JsonSerializableWithType
{
    volatile String _canonicalName;
    
    @Deprecated
    protected TypeBase(final Class<?> raw, final int hash) {
        super(raw, hash);
    }
    
    protected TypeBase(final Class<?> raw, final int hash, final Object valueHandler, final Object typeHandler) {
        super(raw, hash);
        this._valueHandler = valueHandler;
        this._typeHandler = typeHandler;
    }
    
    @Override
    public String toCanonical() {
        String str = this._canonicalName;
        if (str == null) {
            str = this.buildCanonicalName();
        }
        return str;
    }
    
    protected abstract String buildCanonicalName();
    
    @Override
    public abstract StringBuilder getGenericSignature(final StringBuilder p0);
    
    @Override
    public abstract StringBuilder getErasedSignature(final StringBuilder p0);
    
    @Override
    public <T> T getValueHandler() {
        return (T)this._valueHandler;
    }
    
    @Override
    public <T> T getTypeHandler() {
        return (T)this._typeHandler;
    }
    
    public void serializeWithType(final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
        typeSer.writeTypePrefixForScalar(this, jgen);
        this.serialize(jgen, provider);
        typeSer.writeTypeSuffixForScalar(this, jgen);
    }
    
    public void serialize(final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString(this.toCanonical());
    }
    
    protected static StringBuilder _classSignature(final Class<?> cls, final StringBuilder sb, final boolean trailingSemicolon) {
        if (cls.isPrimitive()) {
            if (cls == Boolean.TYPE) {
                sb.append('Z');
            }
            else if (cls == Byte.TYPE) {
                sb.append('B');
            }
            else if (cls == Short.TYPE) {
                sb.append('S');
            }
            else if (cls == Character.TYPE) {
                sb.append('C');
            }
            else if (cls == Integer.TYPE) {
                sb.append('I');
            }
            else if (cls == Long.TYPE) {
                sb.append('J');
            }
            else if (cls == Float.TYPE) {
                sb.append('F');
            }
            else if (cls == Double.TYPE) {
                sb.append('D');
            }
            else {
                if (cls != Void.TYPE) {
                    throw new IllegalStateException("Unrecognized primitive type: " + cls.getName());
                }
                sb.append('V');
            }
        }
        else {
            sb.append('L');
            final String name = cls.getName();
            for (int i = 0, len = name.length(); i < len; ++i) {
                char c = name.charAt(i);
                if (c == '.') {
                    c = '/';
                }
                sb.append(c);
            }
            if (trailingSemicolon) {
                sb.append(';');
            }
        }
        return sb;
    }
}
