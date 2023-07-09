// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.BeanProperty;
import java.util.Collection;

public abstract class StaticListSerializerBase<T extends Collection<?>> extends SerializerBase<T>
{
    protected final BeanProperty _property;
    
    protected StaticListSerializerBase(final Class<?> cls, final BeanProperty property) {
        super(cls, false);
        this._property = property;
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
        final ObjectNode o = this.createSchemaNode("array", true);
        o.put("items", this.contentSchema());
        return o;
    }
    
    protected abstract JsonNode contentSchema();
}
