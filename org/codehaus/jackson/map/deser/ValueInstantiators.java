// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.DeserializationConfig;

public interface ValueInstantiators
{
    ValueInstantiator findValueInstantiator(final DeserializationConfig p0, final BeanDescription p1, final ValueInstantiator p2);
    
    public static class Base implements ValueInstantiators
    {
        public ValueInstantiator findValueInstantiator(final DeserializationConfig config, final BeanDescription beanDesc, final ValueInstantiator defaultInstantiator) {
            return defaultInstantiator;
        }
    }
}
