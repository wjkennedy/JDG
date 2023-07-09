// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.BeanPropertyFilter;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.ser.AnyGetterWriter;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.schema.SchemaAware;
import org.codehaus.jackson.map.ResolvableSerializer;

public abstract class BeanSerializerBase extends SerializerBase<Object> implements ResolvableSerializer, SchemaAware
{
    protected static final BeanPropertyWriter[] NO_PROPS;
    protected final BeanPropertyWriter[] _props;
    protected final BeanPropertyWriter[] _filteredProps;
    protected final AnyGetterWriter _anyGetterWriter;
    protected final Object _propertyFilterId;
    
    protected BeanSerializerBase(final JavaType type, final BeanPropertyWriter[] properties, final BeanPropertyWriter[] filteredProperties, final AnyGetterWriter anyGetterWriter, final Object filterId) {
        super(type);
        this._props = properties;
        this._filteredProps = filteredProperties;
        this._anyGetterWriter = anyGetterWriter;
        this._propertyFilterId = filterId;
    }
    
    public BeanSerializerBase(final Class<?> rawType, final BeanPropertyWriter[] properties, final BeanPropertyWriter[] filteredProperties, final AnyGetterWriter anyGetterWriter, final Object filterId) {
        super(rawType);
        this._props = properties;
        this._filteredProps = filteredProperties;
        this._anyGetterWriter = anyGetterWriter;
        this._propertyFilterId = filterId;
    }
    
    protected BeanSerializerBase(final BeanSerializerBase src) {
        this(src._handledType, src._props, src._filteredProps, src._anyGetterWriter, src._propertyFilterId);
    }
    
    @Override
    public abstract void serialize(final Object p0, final JsonGenerator p1, final SerializerProvider p2) throws IOException, JsonGenerationException;
    
    @Override
    public void serializeWithType(final Object bean, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForObject(bean, jgen);
        if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, jgen, provider);
        }
        else {
            this.serializeFields(bean, jgen, provider);
        }
        typeSer.writeTypeSuffixForObject(bean, jgen);
    }
    
    protected void serializeFields(final Object bean, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        BeanPropertyWriter[] props;
        if (this._filteredProps != null && provider.getSerializationView() != null) {
            props = this._filteredProps;
        }
        else {
            props = this._props;
        }
        int i = 0;
        try {
            for (int len = props.length; i < len; ++i) {
                final BeanPropertyWriter prop = props[i];
                if (prop != null) {
                    prop.serializeAsField(bean, jgen, provider);
                }
            }
            if (this._anyGetterWriter != null) {
                this._anyGetterWriter.getAndSerialize(bean, jgen, provider);
            }
        }
        catch (final Exception e) {
            final String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            this.wrapAndThrow(provider, e, bean, name);
        }
        catch (final StackOverflowError e2) {
            final JsonMappingException mapE = new JsonMappingException("Infinite recursion (StackOverflowError)", e2);
            final String name2 = (i == props.length) ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name2));
            throw mapE;
        }
    }
    
    protected void serializeFieldsFiltered(final Object bean, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        BeanPropertyWriter[] props;
        if (this._filteredProps != null && provider.getSerializationView() != null) {
            props = this._filteredProps;
        }
        else {
            props = this._props;
        }
        final BeanPropertyFilter filter = this.findFilter(provider);
        if (filter == null) {
            this.serializeFields(bean, jgen, provider);
            return;
        }
        int i = 0;
        try {
            for (int len = props.length; i < len; ++i) {
                final BeanPropertyWriter prop = props[i];
                if (prop != null) {
                    filter.serializeAsField(bean, jgen, provider, prop);
                }
            }
            if (this._anyGetterWriter != null) {
                this._anyGetterWriter.getAndSerialize(bean, jgen, provider);
            }
        }
        catch (final Exception e) {
            final String name = (i == props.length) ? "[anySetter]" : props[i].getName();
            this.wrapAndThrow(provider, e, bean, name);
        }
        catch (final StackOverflowError e2) {
            final JsonMappingException mapE = new JsonMappingException("Infinite recursion (StackOverflowError)", e2);
            final String name2 = (i == props.length) ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name2));
            throw mapE;
        }
    }
    
    protected BeanPropertyFilter findFilter(final SerializerProvider provider) throws JsonMappingException {
        final Object filterId = this._propertyFilterId;
        final FilterProvider filters = provider.getFilterProvider();
        if (filters == null) {
            throw new JsonMappingException("Can not resolve BeanPropertyFilter with id '" + filterId + "'; no FilterProvider configured");
        }
        final BeanPropertyFilter filter = filters.findFilter(filterId);
        return filter;
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        final ObjectNode o = this.createSchemaNode("object", true);
        final ObjectNode propertiesNode = o.objectNode();
        for (int i = 0; i < this._props.length; ++i) {
            final BeanPropertyWriter prop = this._props[i];
            final JavaType propType = prop.getSerializationType();
            final Type hint = (propType == null) ? prop.getGenericPropertyType() : propType.getRawClass();
            JsonSerializer<Object> ser = prop.getSerializer();
            if (ser == null) {
                Class<?> serType = prop.getRawSerializationType();
                if (serType == null) {
                    serType = prop.getPropertyType();
                }
                ser = provider.findValueSerializer(serType, prop);
            }
            final JsonNode schemaNode = (ser instanceof SchemaAware) ? ((SchemaAware)ser).getSchema(provider, hint) : JsonSchema.getDefaultSchemaNode();
            propertiesNode.put(prop.getName(), schemaNode);
        }
        o.put("properties", propertiesNode);
        return o;
    }
    
    public void resolve(final SerializerProvider provider) throws JsonMappingException {
        final int filteredCount = (this._filteredProps == null) ? 0 : this._filteredProps.length;
        for (int i = 0, len = this._props.length; i < len; ++i) {
            BeanPropertyWriter prop = this._props[i];
            if (!prop.hasSerializer()) {
                JavaType type = prop.getSerializationType();
                if (type == null) {
                    type = provider.constructType(prop.getGenericPropertyType());
                    if (!type.isFinal()) {
                        if (type.isContainerType() || type.containedTypeCount() > 0) {
                            prop.setNonTrivialBaseType(type);
                        }
                        continue;
                    }
                }
                JsonSerializer<Object> ser = provider.findValueSerializer(type, prop);
                if (type.isContainerType()) {
                    final TypeSerializer typeSer = type.getContentType().getTypeHandler();
                    if (typeSer != null && ser instanceof ContainerSerializerBase) {
                        final JsonSerializer<Object> ser2 = ser = ((ContainerSerializerBase)ser).withValueTypeSerializer(typeSer);
                    }
                }
                prop = prop.withSerializer(ser);
                this._props[i] = prop;
                if (i < filteredCount) {
                    final BeanPropertyWriter w2 = this._filteredProps[i];
                    if (w2 != null) {
                        this._filteredProps[i] = w2.withSerializer(ser);
                    }
                }
            }
        }
        if (this._anyGetterWriter != null) {
            this._anyGetterWriter.resolve(provider);
        }
    }
    
    static {
        NO_PROPS = new BeanPropertyWriter[0];
    }
}
