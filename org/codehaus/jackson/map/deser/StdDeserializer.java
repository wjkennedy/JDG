// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.deser.std.StdScalarDeserializer;
import org.codehaus.jackson.map.deser.std.CalendarDeserializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.deser.std.ClassDeserializer;
import org.codehaus.jackson.type.JavaType;

@Deprecated
public abstract class StdDeserializer<T> extends org.codehaus.jackson.map.deser.std.StdDeserializer<T>
{
    protected StdDeserializer(final Class<?> vc) {
        super(vc);
    }
    
    protected StdDeserializer(final JavaType valueType) {
        super(valueType);
    }
    
    @Deprecated
    @JacksonStdImpl
    public class ClassDeserializer extends org.codehaus.jackson.map.deser.std.ClassDeserializer
    {
    }
    
    @Deprecated
    @JacksonStdImpl
    public class CalendarDeserializer extends org.codehaus.jackson.map.deser.std.CalendarDeserializer
    {
    }
    
    @Deprecated
    @JacksonStdImpl
    public static final class StringDeserializer extends StdScalarDeserializer<String>
    {
        public StringDeserializer() {
            super(String.class);
        }
        
        @Override
        public String deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonToken curr = jp.getCurrentToken();
            if (curr == JsonToken.VALUE_STRING) {
                return jp.getText();
            }
            if (curr == JsonToken.VALUE_EMBEDDED_OBJECT) {
                final Object ob = jp.getEmbeddedObject();
                if (ob == null) {
                    return null;
                }
                if (ob instanceof byte[]) {
                    return Base64Variants.getDefaultVariant().encode((byte[])ob, false);
                }
                return ob.toString();
            }
            else {
                if (curr.isScalarValue()) {
                    return jp.getText();
                }
                throw ctxt.mappingException(this._valueClass, curr);
            }
        }
        
        @Override
        public String deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
            return this.deserialize(jp, ctxt);
        }
    }
}
