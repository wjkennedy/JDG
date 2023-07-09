// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.module;

import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import org.codehaus.jackson.map.deser.ValueInstantiators;

public class SimpleValueInstantiators extends ValueInstantiators.Base
{
    protected HashMap<ClassKey, ValueInstantiator> _classMappings;
    
    public SimpleValueInstantiators() {
        this._classMappings = new HashMap<ClassKey, ValueInstantiator>();
    }
    
    public SimpleValueInstantiators addValueInstantiator(final Class<?> forType, final ValueInstantiator inst) {
        this._classMappings.put(new ClassKey(forType), inst);
        return this;
    }
    
    @Override
    public ValueInstantiator findValueInstantiator(final DeserializationConfig config, final BeanDescription beanDesc, final ValueInstantiator defaultInstantiator) {
        final ValueInstantiator inst = this._classMappings.get(new ClassKey(beanDesc.getBeanClass()));
        return (inst == null) ? defaultInstantiator : inst;
    }
}
