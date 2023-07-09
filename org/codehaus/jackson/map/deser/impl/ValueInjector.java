// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.BeanProperty;

public class ValueInjector extends BeanProperty.Std
{
    protected final Object _valueId;
    
    public ValueInjector(final String propertyName, final JavaType type, final Annotations contextAnnotations, final AnnotatedMember mutator, final Object valueId) {
        super(propertyName, type, contextAnnotations, mutator);
        this._valueId = valueId;
    }
    
    public Object findValue(final DeserializationContext context, final Object beanInstance) {
        return context.findInjectableValue(this._valueId, this, beanInstance);
    }
    
    public void inject(final DeserializationContext context, final Object beanInstance) throws IOException {
        this._member.setValue(beanInstance, this.findValue(context, beanInstance));
    }
}
