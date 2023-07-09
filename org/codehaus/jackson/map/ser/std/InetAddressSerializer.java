// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.net.InetAddress;

public class InetAddressSerializer extends ScalarSerializerBase<InetAddress>
{
    public static final InetAddressSerializer instance;
    
    public InetAddressSerializer() {
        super(InetAddress.class);
    }
    
    @Override
    public void serialize(final InetAddress value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        String str = value.toString().trim();
        final int ix = str.indexOf(47);
        if (ix >= 0) {
            if (ix == 0) {
                str = str.substring(1);
            }
            else {
                str = str.substring(0, ix);
            }
        }
        jgen.writeString(str);
    }
    
    @Override
    public void serializeWithType(final InetAddress value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForScalar(value, jgen, InetAddress.class);
        this.serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
    
    static {
        instance = new InetAddressSerializer();
    }
}
