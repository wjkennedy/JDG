// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;

public abstract class FilteredBeanPropertyWriter
{
    public static BeanPropertyWriter constructViewBased(final BeanPropertyWriter base, final Class<?>[] viewsToIncludeIn) {
        if (viewsToIncludeIn.length == 1) {
            return new SingleView(base, viewsToIncludeIn[0]);
        }
        return new MultiView(base, viewsToIncludeIn);
    }
    
    private static final class SingleView extends BeanPropertyWriter
    {
        protected final BeanPropertyWriter _delegate;
        protected final Class<?> _view;
        
        protected SingleView(final BeanPropertyWriter delegate, final Class<?> view) {
            super(delegate);
            this._delegate = delegate;
            this._view = view;
        }
        
        @Override
        public BeanPropertyWriter withSerializer(final JsonSerializer<Object> ser) {
            return new SingleView(this._delegate.withSerializer(ser), this._view);
        }
        
        @Override
        public void serializeAsField(final Object bean, final JsonGenerator jgen, final SerializerProvider prov) throws Exception {
            final Class<?> activeView = prov.getSerializationView();
            if (activeView == null || this._view.isAssignableFrom(activeView)) {
                this._delegate.serializeAsField(bean, jgen, prov);
            }
        }
    }
    
    private static final class MultiView extends BeanPropertyWriter
    {
        protected final BeanPropertyWriter _delegate;
        protected final Class<?>[] _views;
        
        protected MultiView(final BeanPropertyWriter delegate, final Class<?>[] views) {
            super(delegate);
            this._delegate = delegate;
            this._views = views;
        }
        
        @Override
        public BeanPropertyWriter withSerializer(final JsonSerializer<Object> ser) {
            return new MultiView(this._delegate.withSerializer(ser), this._views);
        }
        
        @Override
        public void serializeAsField(final Object bean, final JsonGenerator jgen, final SerializerProvider prov) throws Exception {
            final Class<?> activeView = prov.getSerializationView();
            if (activeView != null) {
                int i;
                int len;
                for (i = 0, len = this._views.length; i < len && !this._views[i].isAssignableFrom(activeView); ++i) {}
                if (i == len) {
                    return;
                }
            }
            this._delegate.serializeAsField(bean, jgen, prov);
        }
    }
}
