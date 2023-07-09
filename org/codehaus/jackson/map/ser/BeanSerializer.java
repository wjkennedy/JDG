// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ser.impl.UnwrappingBeanSerializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.ser.std.BeanSerializerBase;

public class BeanSerializer extends BeanSerializerBase
{
    public BeanSerializer(final JavaType type, final BeanPropertyWriter[] properties, final BeanPropertyWriter[] filteredProperties, final AnyGetterWriter anyGetterWriter, final Object filterId) {
        super(type, properties, filteredProperties, anyGetterWriter, filterId);
    }
    
    public BeanSerializer(final Class<?> rawType, final BeanPropertyWriter[] properties, final BeanPropertyWriter[] filteredProperties, final AnyGetterWriter anyGetterWriter, final Object filterId) {
        super(rawType, properties, filteredProperties, anyGetterWriter, filterId);
    }
    
    protected BeanSerializer(final BeanSerializer src) {
        super(src);
    }
    
    protected BeanSerializer(final BeanSerializerBase src) {
        super(src);
    }
    
    public static BeanSerializer createDummy(final Class<?> forType) {
        return new BeanSerializer(forType, BeanSerializer.NO_PROPS, null, null, null);
    }
    
    @Override
    public JsonSerializer<Object> unwrappingSerializer() {
        return new UnwrappingBeanSerializer(this);
    }
    
    @Override
    public final void serialize(final Object bean, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeStartObject();
        if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, jgen, provider);
        }
        else {
            this.serializeFields(bean, jgen, provider);
        }
        jgen.writeEndObject();
    }
    
    @Override
    public String toString() {
        return "BeanSerializer for " + this.handledType().getName();
    }
}
