// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.type.JavaType;
import java.lang.reflect.Modifier;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import java.lang.reflect.Method;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.map.ResolvableSerializer;

@JacksonStdImpl
public class JsonValueSerializer extends SerializerBase<Object> implements ResolvableSerializer, SchemaAware
{
    protected final Method _accessorMethod;
    protected JsonSerializer<Object> _valueSerializer;
    protected final BeanProperty _property;
    protected boolean _forceTypeInformation;
    
    public JsonValueSerializer(final Method valueMethod, final JsonSerializer<Object> ser, final BeanProperty property) {
        super(Object.class);
        this._accessorMethod = valueMethod;
        this._valueSerializer = ser;
        this._property = property;
    }
    
    @Override
    public void serialize(final Object bean, final JsonGenerator jgen, final SerializerProvider prov) throws IOException, JsonGenerationException {
        try {
            final Object value = this._accessorMethod.invoke(bean, new Object[0]);
            if (value == null) {
                prov.defaultSerializeNull(jgen);
                return;
            }
            JsonSerializer<Object> ser = this._valueSerializer;
            if (ser == null) {
                final Class<?> c = value.getClass();
                ser = prov.findTypedValueSerializer(c, true, this._property);
            }
            ser.serialize(value, jgen, prov);
        }
        catch (final IOException ioe) {
            throw ioe;
        }
        catch (final Exception e) {
            Throwable t;
            for (t = e; t instanceof InvocationTargetException && t.getCause() != null; t = t.getCause()) {}
            if (t instanceof Error) {
                throw (Error)t;
            }
            throw JsonMappingException.wrapWithPath(t, bean, this._accessorMethod.getName() + "()");
        }
    }
    
    @Override
    public void serializeWithType(final Object bean, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
        Object value = null;
        try {
            value = this._accessorMethod.invoke(bean, new Object[0]);
            if (value == null) {
                provider.defaultSerializeNull(jgen);
                return;
            }
            JsonSerializer<Object> ser = this._valueSerializer;
            if (ser != null) {
                if (this._forceTypeInformation) {
                    typeSer.writeTypePrefixForScalar(bean, jgen);
                }
                ser.serializeWithType(value, jgen, provider, typeSer);
                if (this._forceTypeInformation) {
                    typeSer.writeTypeSuffixForScalar(bean, jgen);
                }
                return;
            }
            final Class<?> c = value.getClass();
            ser = provider.findTypedValueSerializer(c, true, this._property);
            ser.serialize(value, jgen, provider);
        }
        catch (final IOException ioe) {
            throw ioe;
        }
        catch (final Exception e) {
            Throwable t;
            for (t = e; t instanceof InvocationTargetException && t.getCause() != null; t = t.getCause()) {}
            if (t instanceof Error) {
                throw (Error)t;
            }
            throw JsonMappingException.wrapWithPath(t, bean, this._accessorMethod.getName() + "()");
        }
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        return (this._valueSerializer instanceof SchemaAware) ? ((SchemaAware)this._valueSerializer).getSchema(provider, null) : JsonSchema.getDefaultSchemaNode();
    }
    
    public void resolve(final SerializerProvider provider) throws JsonMappingException {
        if (this._valueSerializer == null && (provider.isEnabled(SerializationConfig.Feature.USE_STATIC_TYPING) || Modifier.isFinal(this._accessorMethod.getReturnType().getModifiers()))) {
            final JavaType t = provider.constructType(this._accessorMethod.getGenericReturnType());
            this._valueSerializer = provider.findTypedValueSerializer(t, false, this._property);
            this._forceTypeInformation = this.isNaturalTypeWithStdHandling(t, this._valueSerializer);
        }
    }
    
    protected boolean isNaturalTypeWithStdHandling(final JavaType type, final JsonSerializer<?> ser) {
        final Class<?> cls = type.getRawClass();
        if (type.isPrimitive()) {
            if (cls != Integer.TYPE && cls != Boolean.TYPE && cls != Double.TYPE) {
                return false;
            }
        }
        else if (cls != String.class && cls != Integer.class && cls != Boolean.class && cls != Double.class) {
            return false;
        }
        return ser.getClass().getAnnotation(JacksonStdImpl.class) != null;
    }
    
    @Override
    public String toString() {
        return "(@JsonValue serializer for method " + this._accessorMethod.getDeclaringClass() + "#" + this._accessorMethod.getName() + ")";
    }
}
