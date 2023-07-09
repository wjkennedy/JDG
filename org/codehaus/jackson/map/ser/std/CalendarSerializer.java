// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import java.util.Calendar;

@JacksonStdImpl
public class CalendarSerializer extends ScalarSerializerBase<Calendar>
{
    public static CalendarSerializer instance;
    
    public CalendarSerializer() {
        super(Calendar.class);
    }
    
    @Override
    public void serialize(final Calendar value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        provider.defaultSerializeDateValue(value.getTimeInMillis(), jgen);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
        return this.createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS) ? "number" : "string", true);
    }
    
    static {
        CalendarSerializer.instance = new CalendarSerializer();
    }
}
