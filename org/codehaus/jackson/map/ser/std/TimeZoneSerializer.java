// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.util.TimeZone;

public class TimeZoneSerializer extends ScalarSerializerBase<TimeZone>
{
    public static final TimeZoneSerializer instance;
    
    public TimeZoneSerializer() {
        super(TimeZone.class);
    }
    
    @Override
    public void serialize(final TimeZone value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(value.getID());
    }
    
    @Override
    public void serializeWithType(final TimeZone value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForScalar(value, jgen, TimeZone.class);
        this.serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
    
    static {
        instance = new TimeZoneSerializer();
    }
}
