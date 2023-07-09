// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.codehaus.jackson.map.ser.BeanPropertyFilter;

public abstract class SimpleBeanPropertyFilter implements BeanPropertyFilter
{
    protected SimpleBeanPropertyFilter() {
    }
    
    public static SimpleBeanPropertyFilter filterOutAllExcept(final Set<String> properties) {
        return new FilterExceptFilter(properties);
    }
    
    public static SimpleBeanPropertyFilter filterOutAllExcept(final String... propertyArray) {
        final HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new FilterExceptFilter(properties);
    }
    
    public static SimpleBeanPropertyFilter serializeAllExcept(final Set<String> properties) {
        return new SerializeExceptFilter(properties);
    }
    
    public static SimpleBeanPropertyFilter serializeAllExcept(final String... propertyArray) {
        final HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new SerializeExceptFilter(properties);
    }
    
    public static class FilterExceptFilter extends SimpleBeanPropertyFilter
    {
        protected final Set<String> _propertiesToInclude;
        
        public FilterExceptFilter(final Set<String> properties) {
            this._propertiesToInclude = properties;
        }
        
        public void serializeAsField(final Object bean, final JsonGenerator jgen, final SerializerProvider provider, final BeanPropertyWriter writer) throws Exception {
            if (this._propertiesToInclude.contains(writer.getName())) {
                writer.serializeAsField(bean, jgen, provider);
            }
        }
    }
    
    public static class SerializeExceptFilter extends SimpleBeanPropertyFilter
    {
        protected final Set<String> _propertiesToExclude;
        
        public SerializeExceptFilter(final Set<String> properties) {
            this._propertiesToExclude = properties;
        }
        
        public void serializeAsField(final Object bean, final JsonGenerator jgen, final SerializerProvider provider, final BeanPropertyWriter writer) throws Exception {
            if (!this._propertiesToExclude.contains(writer.getName())) {
                writer.serializeAsField(bean, jgen, provider);
            }
        }
    }
}
