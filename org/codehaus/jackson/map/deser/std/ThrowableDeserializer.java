// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.deser.BeanDeserializer;

public class ThrowableDeserializer extends BeanDeserializer
{
    protected static final String PROP_NAME_MESSAGE = "message";
    
    public ThrowableDeserializer(final BeanDeserializer baseDeserializer) {
        super(baseDeserializer);
    }
    
    protected ThrowableDeserializer(final BeanDeserializer src, final boolean ignoreAllUnknown) {
        super(src, ignoreAllUnknown);
    }
    
    @Override
    public JsonDeserializer<Object> unwrappingDeserializer() {
        if (this.getClass() != ThrowableDeserializer.class) {
            return this;
        }
        return new ThrowableDeserializer(this, true);
    }
    
    @Override
    public Object deserializeFromObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._propertyBasedCreator != null) {
            return this._deserializeUsingPropertyBased(jp, ctxt);
        }
        if (this._delegateDeserializer != null) {
            return this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
        }
        if (this._beanType.isAbstract()) {
            throw JsonMappingException.from(jp, "Can not instantiate abstract type " + this._beanType + " (need to add/enable type information?)");
        }
        final boolean hasStringCreator = this._valueInstantiator.canCreateFromString();
        final boolean hasDefaultCtor = this._valueInstantiator.canCreateUsingDefault();
        if (!hasStringCreator && !hasDefaultCtor) {
            throw new JsonMappingException("Can not deserialize Throwable of type " + this._beanType + " without having a default contructor, a single-String-arg constructor; or explicit @JsonCreator");
        }
        Object throwable = null;
        Object[] pending = null;
        int pendingIx = 0;
        while (jp.getCurrentToken() != JsonToken.END_OBJECT) {
            final String propName = jp.getCurrentName();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            jp.nextToken();
            if (prop != null) {
                if (throwable != null) {
                    prop.deserializeAndSet(jp, ctxt, throwable);
                }
                else {
                    if (pending == null) {
                        final int len = this._beanProperties.size();
                        pending = new Object[len + len];
                    }
                    pending[pendingIx++] = prop;
                    pending[pendingIx++] = prop.deserialize(jp, ctxt);
                }
            }
            else if ("message".equals(propName) && hasStringCreator) {
                throwable = this._valueInstantiator.createFromString(jp.getText());
                if (pending != null) {
                    for (int i = 0, len2 = pendingIx; i < len2; i += 2) {
                        prop = (SettableBeanProperty)pending[i];
                        prop.set(throwable, pending[i + 1]);
                    }
                    pending = null;
                }
            }
            else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                jp.skipChildren();
            }
            else if (this._anySetter != null) {
                this._anySetter.deserializeAndSet(jp, ctxt, throwable, propName);
            }
            else {
                this.handleUnknownProperty(jp, ctxt, throwable, propName);
            }
            jp.nextToken();
        }
        if (throwable == null) {
            if (hasStringCreator) {
                throwable = this._valueInstantiator.createFromString(null);
            }
            else {
                throwable = this._valueInstantiator.createUsingDefault();
            }
            if (pending != null) {
                for (int j = 0, len3 = pendingIx; j < len3; j += 2) {
                    final SettableBeanProperty prop2 = (SettableBeanProperty)pending[j];
                    prop2.set(throwable, pending[j + 1]);
                }
            }
        }
        return throwable;
    }
}
