// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import org.codehaus.jackson.map.ser.impl.UnwrappingBeanPropertyWriter;
import java.util.Map;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.io.SerializedString;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.BeanProperty;

public class BeanPropertyWriter implements BeanProperty
{
    protected final AnnotatedMember _member;
    protected final Annotations _contextAnnotations;
    protected final JavaType _declaredType;
    protected final Method _accessorMethod;
    protected final Field _field;
    protected HashMap<Object, Object> _internalSettings;
    protected final SerializedString _name;
    protected final JavaType _cfgSerializationType;
    protected final JsonSerializer<Object> _serializer;
    protected PropertySerializerMap _dynamicSerializers;
    protected final boolean _suppressNulls;
    protected final Object _suppressableValue;
    protected Class<?>[] _includeInViews;
    protected TypeSerializer _typeSerializer;
    protected JavaType _nonTrivialBaseType;
    
    public BeanPropertyWriter(final AnnotatedMember member, final Annotations contextAnnotations, final String name, final JavaType declaredType, final JsonSerializer<Object> ser, final TypeSerializer typeSer, final JavaType serType, final Method m, final Field f, final boolean suppressNulls, final Object suppressableValue) {
        this(member, contextAnnotations, new SerializedString(name), declaredType, ser, typeSer, serType, m, f, suppressNulls, suppressableValue);
    }
    
    public BeanPropertyWriter(final AnnotatedMember member, final Annotations contextAnnotations, final SerializedString name, final JavaType declaredType, final JsonSerializer<Object> ser, final TypeSerializer typeSer, final JavaType serType, final Method m, final Field f, final boolean suppressNulls, final Object suppressableValue) {
        this._member = member;
        this._contextAnnotations = contextAnnotations;
        this._name = name;
        this._declaredType = declaredType;
        this._serializer = ser;
        this._dynamicSerializers = ((ser == null) ? PropertySerializerMap.emptyMap() : null);
        this._typeSerializer = typeSer;
        this._cfgSerializationType = serType;
        this._accessorMethod = m;
        this._field = f;
        this._suppressNulls = suppressNulls;
        this._suppressableValue = suppressableValue;
    }
    
    protected BeanPropertyWriter(final BeanPropertyWriter base) {
        this(base, base._serializer);
    }
    
    protected BeanPropertyWriter(final BeanPropertyWriter base, final JsonSerializer<Object> ser) {
        this._serializer = ser;
        this._member = base._member;
        this._contextAnnotations = base._contextAnnotations;
        this._declaredType = base._declaredType;
        this._accessorMethod = base._accessorMethod;
        this._field = base._field;
        if (base._internalSettings != null) {
            this._internalSettings = new HashMap<Object, Object>(base._internalSettings);
        }
        this._name = base._name;
        this._cfgSerializationType = base._cfgSerializationType;
        this._dynamicSerializers = base._dynamicSerializers;
        this._suppressNulls = base._suppressNulls;
        this._suppressableValue = base._suppressableValue;
        this._includeInViews = base._includeInViews;
        this._typeSerializer = base._typeSerializer;
        this._nonTrivialBaseType = base._nonTrivialBaseType;
    }
    
    public BeanPropertyWriter withSerializer(final JsonSerializer<Object> ser) {
        if (this.getClass() != BeanPropertyWriter.class) {
            throw new IllegalStateException("BeanPropertyWriter sub-class does not override 'withSerializer()'; needs to!");
        }
        return new BeanPropertyWriter(this, ser);
    }
    
    public BeanPropertyWriter unwrappingWriter() {
        return new UnwrappingBeanPropertyWriter(this);
    }
    
    public void setViews(final Class<?>[] views) {
        this._includeInViews = views;
    }
    
    public void setNonTrivialBaseType(final JavaType t) {
        this._nonTrivialBaseType = t;
    }
    
    public String getName() {
        return this._name.getValue();
    }
    
    public JavaType getType() {
        return this._declaredType;
    }
    
    public <A extends Annotation> A getAnnotation(final Class<A> acls) {
        return this._member.getAnnotation(acls);
    }
    
    public <A extends Annotation> A getContextAnnotation(final Class<A> acls) {
        return this._contextAnnotations.get(acls);
    }
    
    public AnnotatedMember getMember() {
        return this._member;
    }
    
    public Object getInternalSetting(final Object key) {
        if (this._internalSettings == null) {
            return null;
        }
        return this._internalSettings.get(key);
    }
    
    public Object setInternalSetting(final Object key, final Object value) {
        if (this._internalSettings == null) {
            this._internalSettings = new HashMap<Object, Object>();
        }
        return this._internalSettings.put(key, value);
    }
    
    public Object removeInternalSetting(final Object key) {
        Object removed = null;
        if (this._internalSettings != null) {
            removed = this._internalSettings.remove(key);
            if (this._internalSettings.size() == 0) {
                this._internalSettings = null;
            }
        }
        return removed;
    }
    
    public SerializedString getSerializedName() {
        return this._name;
    }
    
    public boolean hasSerializer() {
        return this._serializer != null;
    }
    
    public JsonSerializer<Object> getSerializer() {
        return this._serializer;
    }
    
    public JavaType getSerializationType() {
        return this._cfgSerializationType;
    }
    
    public Class<?> getRawSerializationType() {
        return (this._cfgSerializationType == null) ? null : this._cfgSerializationType.getRawClass();
    }
    
    public Class<?> getPropertyType() {
        if (this._accessorMethod != null) {
            return this._accessorMethod.getReturnType();
        }
        return this._field.getType();
    }
    
    public Type getGenericPropertyType() {
        if (this._accessorMethod != null) {
            return this._accessorMethod.getGenericReturnType();
        }
        return this._field.getGenericType();
    }
    
    public Class<?>[] getViews() {
        return this._includeInViews;
    }
    
    public void serializeAsField(final Object bean, final JsonGenerator jgen, final SerializerProvider prov) throws Exception {
        final Object value = this.get(bean);
        if (value == null) {
            if (!this._suppressNulls) {
                jgen.writeFieldName(this._name);
                prov.defaultSerializeNull(jgen);
            }
            return;
        }
        if (value == bean) {
            this._reportSelfReference(bean);
        }
        if (this._suppressableValue != null && this._suppressableValue.equals(value)) {
            return;
        }
        JsonSerializer<Object> ser = this._serializer;
        if (ser == null) {
            final Class<?> cls = value.getClass();
            final PropertySerializerMap map = this._dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = this._findAndAddDynamic(map, cls, prov);
            }
        }
        jgen.writeFieldName(this._name);
        if (this._typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        }
        else {
            ser.serializeWithType(value, jgen, prov, this._typeSerializer);
        }
    }
    
    protected JsonSerializer<Object> _findAndAddDynamic(final PropertySerializerMap map, final Class<?> type, final SerializerProvider provider) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult result;
        if (this._nonTrivialBaseType != null) {
            final JavaType t = provider.constructSpecializedType(this._nonTrivialBaseType, type);
            result = map.findAndAddSerializer(t, provider, this);
        }
        else {
            result = map.findAndAddSerializer(type, provider, this);
        }
        if (map != result.map) {
            this._dynamicSerializers = result.map;
        }
        return result.serializer;
    }
    
    public final Object get(final Object bean) throws Exception {
        if (this._accessorMethod != null) {
            return this._accessorMethod.invoke(bean, new Object[0]);
        }
        return this._field.get(bean);
    }
    
    protected void _reportSelfReference(final Object bean) throws JsonMappingException {
        throw new JsonMappingException("Direct self-reference leading to cycle");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(40);
        sb.append("property '").append(this.getName()).append("' (");
        if (this._accessorMethod != null) {
            sb.append("via method ").append(this._accessorMethod.getDeclaringClass().getName()).append("#").append(this._accessorMethod.getName());
        }
        else {
            sb.append("field \"").append(this._field.getDeclaringClass().getName()).append("#").append(this._field.getName());
        }
        if (this._serializer == null) {
            sb.append(", no static serializer");
        }
        else {
            sb.append(", static serializer of type " + this._serializer.getClass().getName());
        }
        sb.append(')');
        return sb.toString();
    }
}
