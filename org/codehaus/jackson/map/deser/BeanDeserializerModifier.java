// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.DeserializationConfig;

public abstract class BeanDeserializerModifier
{
    public BeanDeserializerBuilder updateBuilder(final DeserializationConfig config, final BasicBeanDescription beanDesc, final BeanDeserializerBuilder builder) {
        return builder;
    }
    
    public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config, final BasicBeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
        return deserializer;
    }
}
