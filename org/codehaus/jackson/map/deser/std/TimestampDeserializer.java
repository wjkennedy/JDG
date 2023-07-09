// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import java.sql.Timestamp;

public class TimestampDeserializer extends StdScalarDeserializer<Timestamp>
{
    public TimestampDeserializer() {
        super(Timestamp.class);
    }
    
    @Override
    public Timestamp deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new Timestamp(this._parseDate(jp, ctxt).getTime());
    }
}
