// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import java.lang.reflect.InvocationTargetException;
import org.codehaus.jackson.map.deser.impl.PropertyValueBuffer;
import org.codehaus.jackson.util.TokenBuffer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.deser.std.ContainerDeserializerBase;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.DeserializerProvider;
import java.util.Iterator;
import org.codehaus.jackson.map.BeanDescription;
import java.util.List;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.deser.impl.CreatorCollector;
import org.codehaus.jackson.map.deser.impl.ExternalTypeHandler;
import org.codehaus.jackson.map.deser.impl.UnwrappedPropertyHandler;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import org.codehaus.jackson.map.deser.impl.ValueInjector;
import org.codehaus.jackson.map.deser.impl.BeanPropertyMap;
import org.codehaus.jackson.map.deser.impl.PropertyBasedCreator;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.map.ResolvableDeserializer;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

@JsonCachable
public class BeanDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer
{
    protected final AnnotatedClass _forClass;
    protected final JavaType _beanType;
    protected final BeanProperty _property;
    protected final ValueInstantiator _valueInstantiator;
    protected JsonDeserializer<Object> _delegateDeserializer;
    protected final PropertyBasedCreator _propertyBasedCreator;
    protected boolean _nonStandardCreation;
    protected final BeanPropertyMap _beanProperties;
    protected final ValueInjector[] _injectables;
    protected SettableAnyProperty _anySetter;
    protected final HashSet<String> _ignorableProps;
    protected final boolean _ignoreAllUnknown;
    protected final Map<String, SettableBeanProperty> _backRefs;
    protected HashMap<ClassKey, JsonDeserializer<Object>> _subDeserializers;
    protected UnwrappedPropertyHandler _unwrappedPropertyHandler;
    protected ExternalTypeHandler _externalTypeIdHandler;
    
    @Deprecated
    public BeanDeserializer(final AnnotatedClass forClass, final JavaType type, final BeanProperty property, final CreatorCollector creators, final BeanPropertyMap properties, final Map<String, SettableBeanProperty> backRefs, final HashSet<String> ignorableProps, final boolean ignoreAllUnknown, final SettableAnyProperty anySetter) {
        this(forClass, type, property, creators.constructValueInstantiator(null), properties, backRefs, ignorableProps, ignoreAllUnknown, anySetter, null);
    }
    
    public BeanDeserializer(final BeanDescription beanDesc, final BeanProperty property, final ValueInstantiator valueInstantiator, final BeanPropertyMap properties, final Map<String, SettableBeanProperty> backRefs, final HashSet<String> ignorableProps, final boolean ignoreAllUnknown, final SettableAnyProperty anySetter, final List<ValueInjector> injectables) {
        this(beanDesc.getClassInfo(), beanDesc.getType(), property, valueInstantiator, properties, backRefs, ignorableProps, ignoreAllUnknown, anySetter, injectables);
    }
    
    protected BeanDeserializer(final AnnotatedClass forClass, final JavaType type, final BeanProperty property, final ValueInstantiator valueInstantiator, final BeanPropertyMap properties, final Map<String, SettableBeanProperty> backRefs, final HashSet<String> ignorableProps, final boolean ignoreAllUnknown, final SettableAnyProperty anySetter, final List<ValueInjector> injectables) {
        super(type);
        this._forClass = forClass;
        this._beanType = type;
        this._property = property;
        this._valueInstantiator = valueInstantiator;
        if (valueInstantiator.canCreateFromObjectWith()) {
            this._propertyBasedCreator = new PropertyBasedCreator(valueInstantiator);
        }
        else {
            this._propertyBasedCreator = null;
        }
        this._beanProperties = properties;
        this._backRefs = backRefs;
        this._ignorableProps = ignorableProps;
        this._ignoreAllUnknown = ignoreAllUnknown;
        this._anySetter = anySetter;
        this._injectables = (ValueInjector[])((injectables == null || injectables.isEmpty()) ? null : ((ValueInjector[])injectables.toArray(new ValueInjector[injectables.size()])));
        this._nonStandardCreation = (valueInstantiator.canCreateUsingDelegate() || this._propertyBasedCreator != null || !valueInstantiator.canCreateUsingDefault() || this._unwrappedPropertyHandler != null);
    }
    
    protected BeanDeserializer(final BeanDeserializer src) {
        this(src, src._ignoreAllUnknown);
    }
    
    protected BeanDeserializer(final BeanDeserializer src, final boolean ignoreAllUnknown) {
        super(src._beanType);
        this._forClass = src._forClass;
        this._beanType = src._beanType;
        this._property = src._property;
        this._valueInstantiator = src._valueInstantiator;
        this._delegateDeserializer = src._delegateDeserializer;
        this._propertyBasedCreator = src._propertyBasedCreator;
        this._beanProperties = src._beanProperties;
        this._backRefs = src._backRefs;
        this._ignorableProps = src._ignorableProps;
        this._ignoreAllUnknown = ignoreAllUnknown;
        this._anySetter = src._anySetter;
        this._injectables = src._injectables;
        this._nonStandardCreation = src._nonStandardCreation;
        this._unwrappedPropertyHandler = src._unwrappedPropertyHandler;
    }
    
    @Override
    public JsonDeserializer<Object> unwrappingDeserializer() {
        if (this.getClass() != BeanDeserializer.class) {
            return this;
        }
        return new BeanDeserializer(this, true);
    }
    
    public boolean hasProperty(final String propertyName) {
        return this._beanProperties.find(propertyName) != null;
    }
    
    public int getPropertyCount() {
        return this._beanProperties.size();
    }
    
    public final Class<?> getBeanClass() {
        return this._beanType.getRawClass();
    }
    
    @Override
    public JavaType getValueType() {
        return this._beanType;
    }
    
    public Iterator<SettableBeanProperty> properties() {
        if (this._beanProperties == null) {
            throw new IllegalStateException("Can only call before BeanDeserializer has been resolved");
        }
        return this._beanProperties.allProperties();
    }
    
    public SettableBeanProperty findBackReference(final String logicalName) {
        if (this._backRefs == null) {
            return null;
        }
        return this._backRefs.get(logicalName);
    }
    
    public ValueInstantiator getValueInstantiator() {
        return this._valueInstantiator;
    }
    
    public void resolve(final DeserializationConfig config, final DeserializerProvider provider) throws JsonMappingException {
        final Iterator<SettableBeanProperty> it = this._beanProperties.allProperties();
        UnwrappedPropertyHandler unwrapped = null;
        ExternalTypeHandler.Builder extTypes = null;
        while (it.hasNext()) {
            SettableBeanProperty prop;
            final SettableBeanProperty origProp = prop = it.next();
            if (!prop.hasValueDeserializer()) {
                prop = prop.withValueDeserializer(this.findDeserializer(config, provider, prop.getType(), prop));
            }
            prop = this._resolveManagedReferenceProperty(config, prop);
            final SettableBeanProperty u = this._resolveUnwrappedProperty(config, prop);
            if (u != null) {
                prop = u;
                if (unwrapped == null) {
                    unwrapped = new UnwrappedPropertyHandler();
                }
                unwrapped.addProperty(prop);
            }
            prop = this._resolveInnerClassValuedProperty(config, prop);
            if (prop != origProp) {
                this._beanProperties.replace(prop);
            }
            if (prop.hasValueTypeDeserializer()) {
                final TypeDeserializer typeDeser = prop.getValueTypeDeserializer();
                if (typeDeser.getTypeInclusion() != JsonTypeInfo.As.EXTERNAL_PROPERTY) {
                    continue;
                }
                if (extTypes == null) {
                    extTypes = new ExternalTypeHandler.Builder();
                }
                extTypes.addExternal(prop, typeDeser.getPropertyName());
                this._beanProperties.remove(prop);
            }
        }
        if (this._anySetter != null && !this._anySetter.hasValueDeserializer()) {
            this._anySetter = this._anySetter.withValueDeserializer(this.findDeserializer(config, provider, this._anySetter.getType(), this._anySetter.getProperty()));
        }
        if (this._valueInstantiator.canCreateUsingDelegate()) {
            final JavaType delegateType = this._valueInstantiator.getDelegateType();
            if (delegateType == null) {
                throw new IllegalArgumentException("Invalid delegate-creator definition for " + this._beanType + ": value instantiator (" + this._valueInstantiator.getClass().getName() + ") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
            }
            final AnnotatedWithParams delegateCreator = this._valueInstantiator.getDelegateCreator();
            final BeanProperty.Std property = new BeanProperty.Std(null, delegateType, this._forClass.getAnnotations(), delegateCreator);
            this._delegateDeserializer = this.findDeserializer(config, provider, delegateType, property);
        }
        if (this._propertyBasedCreator != null) {
            for (final SettableBeanProperty prop : this._propertyBasedCreator.getCreatorProperties()) {
                if (!prop.hasValueDeserializer()) {
                    this._propertyBasedCreator.assignDeserializer(prop, this.findDeserializer(config, provider, prop.getType(), prop));
                }
            }
        }
        if (extTypes != null) {
            this._externalTypeIdHandler = extTypes.build();
            this._nonStandardCreation = true;
        }
        if ((this._unwrappedPropertyHandler = unwrapped) != null) {
            this._nonStandardCreation = true;
        }
    }
    
    protected SettableBeanProperty _resolveManagedReferenceProperty(final DeserializationConfig config, final SettableBeanProperty prop) {
        final String refName = prop.getManagedReferenceName();
        if (refName == null) {
            return prop;
        }
        final JsonDeserializer<?> valueDeser = prop.getValueDeserializer();
        SettableBeanProperty backProp = null;
        boolean isContainer = false;
        if (valueDeser instanceof BeanDeserializer) {
            backProp = ((BeanDeserializer)valueDeser).findBackReference(refName);
        }
        else if (valueDeser instanceof ContainerDeserializerBase) {
            final JsonDeserializer<?> contentDeser = ((ContainerDeserializerBase)valueDeser).getContentDeserializer();
            if (!(contentDeser instanceof BeanDeserializer)) {
                throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': value deserializer is of type ContainerDeserializerBase, but content type is not handled by a BeanDeserializer  (instead it's of type " + contentDeser.getClass().getName() + ")");
            }
            backProp = ((BeanDeserializer)contentDeser).findBackReference(refName);
            isContainer = true;
        }
        else {
            if (valueDeser instanceof AbstractDeserializer) {
                throw new IllegalArgumentException("Can not handle managed/back reference for abstract types (property " + this._beanType.getRawClass().getName() + "." + prop.getName() + ")");
            }
            throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': type for value deserializer is not BeanDeserializer or ContainerDeserializerBase, but " + valueDeser.getClass().getName());
        }
        if (backProp == null) {
            throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': no back reference property found from type " + prop.getType());
        }
        final JavaType referredType = this._beanType;
        final JavaType backRefType = backProp.getType();
        if (!backRefType.getRawClass().isAssignableFrom(referredType.getRawClass())) {
            throw new IllegalArgumentException("Can not handle managed/back reference '" + refName + "': back reference type (" + backRefType.getRawClass().getName() + ") not compatible with managed type (" + referredType.getRawClass().getName() + ")");
        }
        return new SettableBeanProperty.ManagedReferenceProperty(refName, prop, backProp, this._forClass.getAnnotations(), isContainer);
    }
    
    protected SettableBeanProperty _resolveUnwrappedProperty(final DeserializationConfig config, final SettableBeanProperty prop) {
        final AnnotatedMember am = prop.getMember();
        if (am != null && config.getAnnotationIntrospector().shouldUnwrapProperty(am) == Boolean.TRUE) {
            final JsonDeserializer<Object> orig = prop.getValueDeserializer();
            final JsonDeserializer<Object> unwrapping = orig.unwrappingDeserializer();
            if (unwrapping != orig && unwrapping != null) {
                return prop.withValueDeserializer(unwrapping);
            }
        }
        return null;
    }
    
    protected SettableBeanProperty _resolveInnerClassValuedProperty(final DeserializationConfig config, final SettableBeanProperty prop) {
        final JsonDeserializer<Object> deser = prop.getValueDeserializer();
        if (deser instanceof BeanDeserializer) {
            final BeanDeserializer bd = (BeanDeserializer)deser;
            final ValueInstantiator vi = bd.getValueInstantiator();
            if (!vi.canCreateUsingDefault()) {
                final Class<?> valueClass = prop.getType().getRawClass();
                final Class<?> enclosing = ClassUtil.getOuterClass(valueClass);
                if (enclosing != null && enclosing == this._beanType.getRawClass()) {
                    for (final Constructor<?> ctor : valueClass.getConstructors()) {
                        final Class<?>[] paramTypes = ctor.getParameterTypes();
                        if (paramTypes.length == 1 && paramTypes[0] == enclosing) {
                            if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                                ClassUtil.checkAndFixAccess(ctor);
                            }
                            return new SettableBeanProperty.InnerClassProperty(prop, ctor);
                        }
                    }
                }
            }
        }
        return prop;
    }
    
    @Override
    public final Object deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            jp.nextToken();
            return this.deserializeFromObject(jp, ctxt);
        }
        switch (t) {
            case VALUE_STRING: {
                return this.deserializeFromString(jp, ctxt);
            }
            case VALUE_NUMBER_INT: {
                return this.deserializeFromNumber(jp, ctxt);
            }
            case VALUE_NUMBER_FLOAT: {
                return this.deserializeFromDouble(jp, ctxt);
            }
            case VALUE_EMBEDDED_OBJECT: {
                return jp.getEmbeddedObject();
            }
            case VALUE_TRUE:
            case VALUE_FALSE: {
                return this.deserializeFromBoolean(jp, ctxt);
            }
            case START_ARRAY: {
                return this.deserializeFromArray(jp, ctxt);
            }
            case FIELD_NAME:
            case END_OBJECT: {
                return this.deserializeFromObject(jp, ctxt);
            }
            default: {
                throw ctxt.mappingException(this.getBeanClass());
            }
        }
    }
    
    @Override
    public Object deserialize(final JsonParser jp, final DeserializationContext ctxt, final Object bean) throws IOException, JsonProcessingException {
        if (this._injectables != null) {
            this.injectValues(ctxt, bean);
        }
        if (this._unwrappedPropertyHandler != null) {
            return this.deserializeWithUnwrapped(jp, ctxt, bean);
        }
        if (this._externalTypeIdHandler != null) {
            return this.deserializeWithExternalTypeId(jp, ctxt, bean);
        }
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        while (t == JsonToken.FIELD_NAME) {
            final String propName = jp.getCurrentName();
            jp.nextToken();
            final SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                }
                catch (final Exception e) {
                    this.wrapAndThrow(e, bean, propName, ctxt);
                }
            }
            else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                jp.skipChildren();
            }
            else if (this._anySetter != null) {
                this._anySetter.deserializeAndSet(jp, ctxt, bean, propName);
            }
            else {
                this.handleUnknownProperty(jp, ctxt, bean, propName);
            }
            t = jp.nextToken();
        }
        return bean;
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }
    
    public Object deserializeFromObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (!this._nonStandardCreation) {
            final Object bean = this._valueInstantiator.createUsingDefault();
            if (this._injectables != null) {
                this.injectValues(ctxt, bean);
            }
            while (jp.getCurrentToken() != JsonToken.END_OBJECT) {
                final String propName = jp.getCurrentName();
                jp.nextToken();
                final SettableBeanProperty prop = this._beanProperties.find(propName);
                if (prop != null) {
                    try {
                        prop.deserializeAndSet(jp, ctxt, bean);
                    }
                    catch (final Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                }
                else {
                    this._handleUnknown(jp, ctxt, bean, propName);
                }
                jp.nextToken();
            }
            return bean;
        }
        if (this._unwrappedPropertyHandler != null) {
            return this.deserializeWithUnwrapped(jp, ctxt);
        }
        if (this._externalTypeIdHandler != null) {
            return this.deserializeWithExternalTypeId(jp, ctxt);
        }
        return this.deserializeFromObjectUsingNonDefault(jp, ctxt);
    }
    
    private final void _handleUnknown(final JsonParser jp, final DeserializationContext ctxt, final Object bean, final String propName) throws IOException, JsonProcessingException {
        if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
            jp.skipChildren();
        }
        else if (this._anySetter != null) {
            try {
                this._anySetter.deserializeAndSet(jp, ctxt, bean, propName);
            }
            catch (final Exception e) {
                this.wrapAndThrow(e, bean, propName, ctxt);
            }
        }
        else {
            this.handleUnknownProperty(jp, ctxt, bean, propName);
        }
    }
    
    protected Object deserializeFromObjectUsingNonDefault(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null) {
            return this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
        }
        if (this._propertyBasedCreator != null) {
            return this._deserializeUsingPropertyBased(jp, ctxt);
        }
        if (this._beanType.isAbstract()) {
            throw JsonMappingException.from(jp, "Can not instantiate abstract type " + this._beanType + " (need to add/enable type information?)");
        }
        throw JsonMappingException.from(jp, "No suitable constructor found for type " + this._beanType + ": can not instantiate from JSON object (need to add/enable type information?)");
    }
    
    public Object deserializeFromString(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null && !this._valueInstantiator.canCreateFromString()) {
            final Object bean = this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
            if (this._injectables != null) {
                this.injectValues(ctxt, bean);
            }
            return bean;
        }
        return this._valueInstantiator.createFromString(jp.getText());
    }
    
    public Object deserializeFromNumber(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        switch (jp.getNumberType()) {
            case INT: {
                if (this._delegateDeserializer != null && !this._valueInstantiator.canCreateFromInt()) {
                    final Object bean = this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
                    if (this._injectables != null) {
                        this.injectValues(ctxt, bean);
                    }
                    return bean;
                }
                return this._valueInstantiator.createFromInt(jp.getIntValue());
            }
            case LONG: {
                if (this._delegateDeserializer != null && !this._valueInstantiator.canCreateFromInt()) {
                    final Object bean = this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
                    if (this._injectables != null) {
                        this.injectValues(ctxt, bean);
                    }
                    return bean;
                }
                return this._valueInstantiator.createFromLong(jp.getLongValue());
            }
            default: {
                if (this._delegateDeserializer != null) {
                    final Object bean = this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
                    if (this._injectables != null) {
                        this.injectValues(ctxt, bean);
                    }
                    return bean;
                }
                throw ctxt.instantiationException(this.getBeanClass(), "no suitable creator method found to deserialize from JSON integer number");
            }
        }
    }
    
    public Object deserializeFromDouble(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        switch (jp.getNumberType()) {
            case FLOAT:
            case DOUBLE: {
                if (this._delegateDeserializer != null && !this._valueInstantiator.canCreateFromDouble()) {
                    final Object bean = this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
                    if (this._injectables != null) {
                        this.injectValues(ctxt, bean);
                    }
                    return bean;
                }
                return this._valueInstantiator.createFromDouble(jp.getDoubleValue());
            }
            default: {
                if (this._delegateDeserializer != null) {
                    return this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
                }
                throw ctxt.instantiationException(this.getBeanClass(), "no suitable creator method found to deserialize from JSON floating-point number");
            }
        }
    }
    
    public Object deserializeFromBoolean(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null && !this._valueInstantiator.canCreateFromBoolean()) {
            final Object bean = this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
            if (this._injectables != null) {
                this.injectValues(ctxt, bean);
            }
            return bean;
        }
        final boolean value = jp.getCurrentToken() == JsonToken.VALUE_TRUE;
        return this._valueInstantiator.createFromBoolean(value);
    }
    
    public Object deserializeFromArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null) {
            try {
                final Object bean = this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
                if (this._injectables != null) {
                    this.injectValues(ctxt, bean);
                }
                return bean;
            }
            catch (final Exception e) {
                this.wrapInstantiationProblem(e, ctxt);
            }
        }
        throw ctxt.mappingException(this.getBeanClass());
    }
    
    protected final Object _deserializeUsingPropertyBased(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final PropertyBasedCreator creator = this._propertyBasedCreator;
        final PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);
        TokenBuffer unknown = null;
        for (JsonToken t = jp.getCurrentToken(); t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            final String propName = jp.getCurrentName();
            jp.nextToken();
            final SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                final Object value = creatorProp.deserialize(jp, ctxt);
                if (buffer.assignParameter(creatorProp.getPropertyIndex(), value)) {
                    jp.nextToken();
                    Object bean;
                    try {
                        bean = creator.build(buffer);
                    }
                    catch (final Exception e) {
                        this.wrapAndThrow(e, this._beanType.getRawClass(), propName, ctxt);
                        continue;
                    }
                    if (bean.getClass() != this._beanType.getRawClass()) {
                        return this.handlePolymorphic(jp, ctxt, bean, unknown);
                    }
                    if (unknown != null) {
                        bean = this.handleUnknownProperties(ctxt, bean, unknown);
                    }
                    return this.deserialize(jp, ctxt, bean);
                }
            }
            else {
                final SettableBeanProperty prop = this._beanProperties.find(propName);
                if (prop != null) {
                    buffer.bufferProperty(prop, prop.deserialize(jp, ctxt));
                }
                else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                    jp.skipChildren();
                }
                else if (this._anySetter != null) {
                    buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(jp, ctxt));
                }
                else {
                    if (unknown == null) {
                        unknown = new TokenBuffer(jp.getCodec());
                    }
                    unknown.writeFieldName(propName);
                    unknown.copyCurrentStructure(jp);
                }
            }
        }
        Object bean2;
        try {
            bean2 = creator.build(buffer);
        }
        catch (final Exception e2) {
            this.wrapInstantiationProblem(e2, ctxt);
            return null;
        }
        if (unknown == null) {
            return bean2;
        }
        if (bean2.getClass() != this._beanType.getRawClass()) {
            return this.handlePolymorphic(null, ctxt, bean2, unknown);
        }
        return this.handleUnknownProperties(ctxt, bean2, unknown);
    }
    
    protected Object handlePolymorphic(final JsonParser jp, final DeserializationContext ctxt, Object bean, final TokenBuffer unknownTokens) throws IOException, JsonProcessingException {
        final JsonDeserializer<Object> subDeser = this._findSubclassDeserializer(ctxt, bean, unknownTokens);
        if (subDeser != null) {
            if (unknownTokens != null) {
                unknownTokens.writeEndObject();
                final JsonParser p2 = unknownTokens.asParser();
                p2.nextToken();
                bean = subDeser.deserialize(p2, ctxt, bean);
            }
            if (jp != null) {
                bean = subDeser.deserialize(jp, ctxt, bean);
            }
            return bean;
        }
        if (unknownTokens != null) {
            bean = this.handleUnknownProperties(ctxt, bean, unknownTokens);
        }
        if (jp != null) {
            bean = this.deserialize(jp, ctxt, bean);
        }
        return bean;
    }
    
    protected Object deserializeWithUnwrapped(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null) {
            return this._valueInstantiator.createUsingDelegate(this._delegateDeserializer.deserialize(jp, ctxt));
        }
        if (this._propertyBasedCreator != null) {
            return this.deserializeUsingPropertyBasedWithUnwrapped(jp, ctxt);
        }
        final TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();
        final Object bean = this._valueInstantiator.createUsingDefault();
        if (this._injectables != null) {
            this.injectValues(ctxt, bean);
        }
        while (jp.getCurrentToken() != JsonToken.END_OBJECT) {
            final String propName = jp.getCurrentName();
            jp.nextToken();
            final SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                }
                catch (final Exception e) {
                    this.wrapAndThrow(e, bean, propName, ctxt);
                }
            }
            else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                jp.skipChildren();
            }
            else {
                tokens.writeFieldName(propName);
                tokens.copyCurrentStructure(jp);
                if (this._anySetter != null) {
                    try {
                        this._anySetter.deserializeAndSet(jp, ctxt, bean, propName);
                    }
                    catch (final Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                }
            }
            jp.nextToken();
        }
        tokens.writeEndObject();
        this._unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean, tokens);
        return bean;
    }
    
    protected Object deserializeWithUnwrapped(final JsonParser jp, final DeserializationContext ctxt, final Object bean) throws IOException, JsonProcessingException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        final TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();
        while (t == JsonToken.FIELD_NAME) {
            final String propName = jp.getCurrentName();
            final SettableBeanProperty prop = this._beanProperties.find(propName);
            jp.nextToken();
            if (prop != null) {
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                }
                catch (final Exception e) {
                    this.wrapAndThrow(e, bean, propName, ctxt);
                }
            }
            else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                jp.skipChildren();
            }
            else {
                tokens.writeFieldName(propName);
                tokens.copyCurrentStructure(jp);
                if (this._anySetter != null) {
                    this._anySetter.deserializeAndSet(jp, ctxt, bean, propName);
                }
            }
            t = jp.nextToken();
        }
        tokens.writeEndObject();
        this._unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean, tokens);
        return bean;
    }
    
    protected Object deserializeUsingPropertyBasedWithUnwrapped(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final PropertyBasedCreator creator = this._propertyBasedCreator;
        final PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);
        final TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();
        for (JsonToken t = jp.getCurrentToken(); t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            final String propName = jp.getCurrentName();
            jp.nextToken();
            final SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                final Object value = creatorProp.deserialize(jp, ctxt);
                if (buffer.assignParameter(creatorProp.getPropertyIndex(), value)) {
                    t = jp.nextToken();
                    Object bean;
                    try {
                        bean = creator.build(buffer);
                    }
                    catch (final Exception e) {
                        this.wrapAndThrow(e, this._beanType.getRawClass(), propName, ctxt);
                        continue;
                    }
                    while (t == JsonToken.FIELD_NAME) {
                        jp.nextToken();
                        tokens.copyCurrentStructure(jp);
                        t = jp.nextToken();
                    }
                    tokens.writeEndObject();
                    if (bean.getClass() != this._beanType.getRawClass()) {
                        throw ctxt.mappingException("Can not create polymorphic instances with unwrapped values");
                    }
                    return this._unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean, tokens);
                }
            }
            else {
                final SettableBeanProperty prop = this._beanProperties.find(propName);
                if (prop != null) {
                    buffer.bufferProperty(prop, prop.deserialize(jp, ctxt));
                }
                else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                    jp.skipChildren();
                }
                else {
                    tokens.writeFieldName(propName);
                    tokens.copyCurrentStructure(jp);
                    if (this._anySetter != null) {
                        buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(jp, ctxt));
                    }
                }
            }
        }
        Object bean2;
        try {
            bean2 = creator.build(buffer);
        }
        catch (final Exception e2) {
            this.wrapInstantiationProblem(e2, ctxt);
            return null;
        }
        return this._unwrappedPropertyHandler.processUnwrapped(jp, ctxt, bean2, tokens);
    }
    
    protected Object deserializeWithExternalTypeId(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._propertyBasedCreator != null) {
            return this.deserializeUsingPropertyBasedWithExternalTypeId(jp, ctxt);
        }
        return this.deserializeWithExternalTypeId(jp, ctxt, this._valueInstantiator.createUsingDefault());
    }
    
    protected Object deserializeWithExternalTypeId(final JsonParser jp, final DeserializationContext ctxt, final Object bean) throws IOException, JsonProcessingException {
        final ExternalTypeHandler ext = this._externalTypeIdHandler.start();
        while (jp.getCurrentToken() != JsonToken.END_OBJECT) {
            final String propName = jp.getCurrentName();
            jp.nextToken();
            final SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                if (jp.getCurrentToken().isScalarValue()) {
                    ext.handleTypePropertyValue(jp, ctxt, propName, bean);
                }
                try {
                    prop.deserializeAndSet(jp, ctxt, bean);
                }
                catch (final Exception e) {
                    this.wrapAndThrow(e, bean, propName, ctxt);
                }
            }
            else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                jp.skipChildren();
            }
            else if (!ext.handleToken(jp, ctxt, propName, bean)) {
                if (this._anySetter != null) {
                    try {
                        this._anySetter.deserializeAndSet(jp, ctxt, bean, propName);
                    }
                    catch (final Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                }
                else {
                    this.handleUnknownProperty(jp, ctxt, bean, propName);
                }
            }
            jp.nextToken();
        }
        return ext.complete(jp, ctxt, bean);
    }
    
    protected Object deserializeUsingPropertyBasedWithExternalTypeId(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final ExternalTypeHandler ext = this._externalTypeIdHandler.start();
        final PropertyBasedCreator creator = this._propertyBasedCreator;
        final PropertyValueBuffer buffer = creator.startBuilding(jp, ctxt);
        final TokenBuffer tokens = new TokenBuffer(jp.getCodec());
        tokens.writeStartObject();
        for (JsonToken t = jp.getCurrentToken(); t == JsonToken.FIELD_NAME; t = jp.nextToken()) {
            final String propName = jp.getCurrentName();
            jp.nextToken();
            final SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                final Object value = creatorProp.deserialize(jp, ctxt);
                if (buffer.assignParameter(creatorProp.getPropertyIndex(), value)) {
                    t = jp.nextToken();
                    Object bean;
                    try {
                        bean = creator.build(buffer);
                    }
                    catch (final Exception e) {
                        this.wrapAndThrow(e, this._beanType.getRawClass(), propName, ctxt);
                        continue;
                    }
                    while (t == JsonToken.FIELD_NAME) {
                        jp.nextToken();
                        tokens.copyCurrentStructure(jp);
                        t = jp.nextToken();
                    }
                    if (bean.getClass() != this._beanType.getRawClass()) {
                        throw ctxt.mappingException("Can not create polymorphic instances with unwrapped values");
                    }
                    return ext.complete(jp, ctxt, bean);
                }
            }
            else {
                final SettableBeanProperty prop = this._beanProperties.find(propName);
                if (prop != null) {
                    buffer.bufferProperty(prop, prop.deserialize(jp, ctxt));
                }
                else if (!ext.handleToken(jp, ctxt, propName, null)) {
                    if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                        jp.skipChildren();
                    }
                    else if (this._anySetter != null) {
                        buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(jp, ctxt));
                    }
                }
            }
        }
        Object bean2;
        try {
            bean2 = creator.build(buffer);
        }
        catch (final Exception e2) {
            this.wrapInstantiationProblem(e2, ctxt);
            return null;
        }
        return ext.complete(jp, ctxt, bean2);
    }
    
    protected void injectValues(final DeserializationContext ctxt, final Object bean) throws IOException, JsonProcessingException {
        for (final ValueInjector injector : this._injectables) {
            injector.inject(ctxt, bean);
        }
    }
    
    @Override
    protected void handleUnknownProperty(final JsonParser jp, final DeserializationContext ctxt, final Object beanOrClass, final String propName) throws IOException, JsonProcessingException {
        if (this._ignoreAllUnknown || (this._ignorableProps != null && this._ignorableProps.contains(propName))) {
            jp.skipChildren();
            return;
        }
        super.handleUnknownProperty(jp, ctxt, beanOrClass, propName);
    }
    
    protected Object handleUnknownProperties(final DeserializationContext ctxt, final Object bean, final TokenBuffer unknownTokens) throws IOException, JsonProcessingException {
        unknownTokens.writeEndObject();
        final JsonParser bufferParser = unknownTokens.asParser();
        while (bufferParser.nextToken() != JsonToken.END_OBJECT) {
            final String propName = bufferParser.getCurrentName();
            bufferParser.nextToken();
            this.handleUnknownProperty(bufferParser, ctxt, bean, propName);
        }
        return bean;
    }
    
    protected JsonDeserializer<Object> _findSubclassDeserializer(final DeserializationContext ctxt, final Object bean, final TokenBuffer unknownTokens) throws IOException, JsonProcessingException {
        JsonDeserializer<Object> subDeser;
        synchronized (this) {
            subDeser = ((this._subDeserializers == null) ? null : this._subDeserializers.get(new ClassKey(bean.getClass())));
        }
        if (subDeser != null) {
            return subDeser;
        }
        final DeserializerProvider deserProv = ctxt.getDeserializerProvider();
        if (deserProv != null) {
            final JavaType type = ctxt.constructType(bean.getClass());
            subDeser = deserProv.findValueDeserializer(ctxt.getConfig(), type, this._property);
            if (subDeser != null) {
                synchronized (this) {
                    if (this._subDeserializers == null) {
                        this._subDeserializers = new HashMap<ClassKey, JsonDeserializer<Object>>();
                    }
                    this._subDeserializers.put(new ClassKey(bean.getClass()), subDeser);
                }
            }
        }
        return subDeser;
    }
    
    public void wrapAndThrow(Throwable t, final Object bean, final String fieldName, final DeserializationContext ctxt) throws IOException {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        final boolean wrap = ctxt == null || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
                throw (IOException)t;
            }
        }
        else if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw JsonMappingException.wrapWithPath(t, bean, fieldName);
    }
    
    public void wrapAndThrow(Throwable t, final Object bean, final int index, final DeserializationContext ctxt) throws IOException {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        final boolean wrap = ctxt == null || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonMappingException)) {
                throw (IOException)t;
            }
        }
        else if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw JsonMappingException.wrapWithPath(t, bean, index);
    }
    
    protected void wrapInstantiationProblem(Throwable t, final DeserializationContext ctxt) throws IOException {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        final boolean wrap = ctxt == null || ctxt.isEnabled(DeserializationConfig.Feature.WRAP_EXCEPTIONS);
        if (t instanceof IOException) {
            throw (IOException)t;
        }
        if (!wrap && t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw ctxt.instantiationException(this._beanType.getRawClass(), t);
    }
    
    @Deprecated
    public void wrapAndThrow(final Throwable t, final Object bean, final String fieldName) throws IOException {
        this.wrapAndThrow(t, bean, fieldName, null);
    }
    
    @Deprecated
    public void wrapAndThrow(final Throwable t, final Object bean, final int index) throws IOException {
        this.wrapAndThrow(t, bean, index, null);
    }
}
