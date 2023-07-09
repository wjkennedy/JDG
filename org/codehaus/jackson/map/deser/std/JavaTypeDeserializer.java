// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.JavaType;

public class JavaTypeDeserializer extends StdScalarDeserializer<JavaType>
{
    public JavaTypeDeserializer() {
        super(JavaType.class);
    }
    
    @Override
    public JavaType deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken curr = jp.getCurrentToken();
        if (curr == JsonToken.VALUE_STRING) {
            final String str = jp.getText().trim();
            if (str.length() == 0) {
                return this.getEmptyValue();
            }
            return ctxt.getTypeFactory().constructFromCanonical(str);
        }
        else {
            if (curr == JsonToken.VALUE_EMBEDDED_OBJECT) {
                return (JavaType)jp.getEmbeddedObject();
            }
            throw ctxt.mappingException(this._valueClass);
        }
    }
}
