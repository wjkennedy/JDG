// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import java.io.Serializable;
import java.util.HashSet;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.type.JavaType;
import java.util.Set;
import org.codehaus.jackson.map.Deserializers;

public class SecurityBeanDeserializer extends Deserializers.Base
{
    protected static final Set<String> BLOCKED_CLASS_NAMES;
    
    @Override
    public JsonDeserializer<?> findBeanDeserializer(final JavaType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        if (property != null && type != null && property.getType() != null && type.getRawClass() != null) {
            this.checkType(type, property.getType());
            if (property.getType().isContainerType()) {
                this.checkType(type, property.getType().getContentType());
                if (type.isMapLikeType()) {
                    this.checkType(type, property.getType().getKeyType());
                }
            }
        }
        return super.findBeanDeserializer(type, config, provider, beanDesc, property);
    }
    
    protected void checkType(final JavaType type, final JavaType fromPropertyType) {
        if (SecurityBeanDeserializer.BLOCKED_CLASS_NAMES.contains(fromPropertyType.getRawClass().getName()) && !SecurityBeanDeserializer.BLOCKED_CLASS_NAMES.contains(type.getRawClass().getName())) {
            throw new SecurityException(String.format("Prevented for security reasons deserializing %s as %s is too general a type.", type.getRawClass().getName(), fromPropertyType.getRawClass().getName()));
        }
    }
    
    static {
        final Set<String> s = new HashSet<String>();
        s.add(Object.class.getName());
        s.add(Comparable.class.getName());
        s.add(Serializable.class.getName());
        BLOCKED_CLASS_NAMES = s;
    }
}
