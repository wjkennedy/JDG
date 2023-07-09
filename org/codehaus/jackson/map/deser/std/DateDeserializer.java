// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import java.util.Date;

public class DateDeserializer extends StdScalarDeserializer<Date>
{
    public DateDeserializer() {
        super(Date.class);
    }
    
    @Override
    public Date deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this._parseDate(jp, ctxt);
    }
}
