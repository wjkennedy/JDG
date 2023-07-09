// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import java.util.HashMap;
import java.util.Map;

public abstract class InjectableValues
{
    public abstract Object findInjectableValue(final Object p0, final DeserializationContext p1, final BeanProperty p2, final Object p3);
    
    public static class Std extends InjectableValues
    {
        protected final Map<String, Object> _values;
        
        public Std() {
            this(new HashMap<String, Object>());
        }
        
        public Std(final Map<String, Object> values) {
            this._values = values;
        }
        
        public Std addValue(final String key, final Object value) {
            this._values.put(key, value);
            return this;
        }
        
        public Std addValue(final Class<?> classKey, final Object value) {
            this._values.put(classKey.getName(), value);
            return this;
        }
        
        @Override
        public Object findInjectableValue(final Object valueId, final DeserializationContext ctxt, final BeanProperty forProperty, final Object beanInstance) {
            if (!(valueId instanceof String)) {
                final String type = (valueId == null) ? "[null]" : valueId.getClass().getName();
                throw new IllegalArgumentException("Unrecognized inject value id type (" + type + "), expecting String");
            }
            final String key = (String)valueId;
            final Object ob = this._values.get(key);
            if (ob == null && !this._values.containsKey(key)) {
                throw new IllegalArgumentException("No injectable id with value '" + key + "' found (for property '" + forProperty.getName() + "')");
            }
            return ob;
        }
    }
}
