// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class ClassDeserializer extends StdScalarDeserializer<Class<?>>
{
    public ClassDeserializer() {
        super(Class.class);
    }
    
    @Override
    public Class<?> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken curr = jp.getCurrentToken();
        if (curr == JsonToken.VALUE_STRING) {
            final String className = jp.getText();
            try {
                return ClassUtil.findClass(className);
            }
            catch (final ClassNotFoundException e) {
                throw ctxt.instantiationException(this._valueClass, e);
            }
        }
        throw ctxt.mappingException(this._valueClass, curr);
    }
}
