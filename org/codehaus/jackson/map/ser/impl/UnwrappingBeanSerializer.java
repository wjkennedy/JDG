// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ser.std.BeanSerializerBase;

public class UnwrappingBeanSerializer extends BeanSerializerBase
{
    public UnwrappingBeanSerializer(final BeanSerializerBase src) {
        super(src);
    }
    
    @Override
    public JsonSerializer<Object> unwrappingSerializer() {
        return this;
    }
    
    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }
    
    @Override
    public final void serialize(final Object bean, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, jgen, provider);
        }
        else {
            this.serializeFields(bean, jgen, provider);
        }
    }
    
    @Override
    public String toString() {
        return "UnwrappingBeanSerializer for " + this.handledType().getName();
    }
}
