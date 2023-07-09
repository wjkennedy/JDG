// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.annotate.JsonTypeIdResolver;
import org.codehaus.jackson.map.annotate.JsonTypeResolver;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonValueInstantiator;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonView;
import org.codehaus.jackson.map.annotate.NoClass;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.ser.std.RawSerializer;
import org.codehaus.jackson.annotate.JsonRawValue;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.annotate.JsonTypeName;
import java.util.ArrayList;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.map.jsontype.NamedType;
import java.util.List;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.annotate.JacksonInject;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.annotate.JsonIgnoreType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.annotate.JacksonAnnotation;
import java.lang.annotation.Annotation;
import org.codehaus.jackson.map.AnnotationIntrospector;

public class JacksonAnnotationIntrospector extends AnnotationIntrospector
{
    @Override
    public boolean isHandled(final Annotation ann) {
        final Class<? extends Annotation> acls = ann.annotationType();
        return acls.getAnnotation(JacksonAnnotation.class) != null;
    }
    
    @Override
    public String findEnumValue(final Enum<?> value) {
        return value.name();
    }
    
    @Override
    public Boolean findCachability(final AnnotatedClass ac) {
        final JsonCachable ann = ac.getAnnotation(JsonCachable.class);
        if (ann == null) {
            return null;
        }
        return ann.value() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    @Override
    public String findRootName(final AnnotatedClass ac) {
        final JsonRootName ann = ac.getAnnotation(JsonRootName.class);
        return (ann == null) ? null : ann.value();
    }
    
    @Override
    public String[] findPropertiesToIgnore(final AnnotatedClass ac) {
        final JsonIgnoreProperties ignore = ac.getAnnotation(JsonIgnoreProperties.class);
        return (String[])((ignore == null) ? null : ignore.value());
    }
    
    @Override
    public Boolean findIgnoreUnknownProperties(final AnnotatedClass ac) {
        final JsonIgnoreProperties ignore = ac.getAnnotation(JsonIgnoreProperties.class);
        return (ignore == null) ? null : Boolean.valueOf(ignore.ignoreUnknown());
    }
    
    @Override
    public Boolean isIgnorableType(final AnnotatedClass ac) {
        final JsonIgnoreType ignore = ac.getAnnotation(JsonIgnoreType.class);
        return (ignore == null) ? null : Boolean.valueOf(ignore.value());
    }
    
    @Override
    public Object findFilterId(final AnnotatedClass ac) {
        final JsonFilter ann = ac.getAnnotation(JsonFilter.class);
        if (ann != null) {
            final String id = ann.value();
            if (id.length() > 0) {
                return id;
            }
        }
        return null;
    }
    
    @Override
    public VisibilityChecker<?> findAutoDetectVisibility(final AnnotatedClass ac, final VisibilityChecker<?> checker) {
        final JsonAutoDetect ann = ac.getAnnotation(JsonAutoDetect.class);
        return (VisibilityChecker<?>)((ann == null) ? checker : checker.with(ann));
    }
    
    @Override
    public ReferenceProperty findReferenceType(final AnnotatedMember member) {
        final JsonManagedReference ref1 = member.getAnnotation(JsonManagedReference.class);
        if (ref1 != null) {
            return ReferenceProperty.managed(ref1.value());
        }
        final JsonBackReference ref2 = member.getAnnotation(JsonBackReference.class);
        if (ref2 != null) {
            return ReferenceProperty.back(ref2.value());
        }
        return null;
    }
    
    @Override
    public Boolean shouldUnwrapProperty(final AnnotatedMember member) {
        final JsonUnwrapped ann = member.getAnnotation(JsonUnwrapped.class);
        return (ann != null && ann.enabled()) ? Boolean.TRUE : null;
    }
    
    @Override
    public boolean hasIgnoreMarker(final AnnotatedMember m) {
        return this._isIgnorable(m);
    }
    
    @Override
    public Object findInjectableValueId(final AnnotatedMember m) {
        final JacksonInject ann = m.getAnnotation(JacksonInject.class);
        if (ann == null) {
            return null;
        }
        final String id = ann.value();
        if (id.length() != 0) {
            return id;
        }
        if (!(m instanceof AnnotatedMethod)) {
            return m.getRawType().getName();
        }
        final AnnotatedMethod am = (AnnotatedMethod)m;
        if (am.getParameterCount() == 0) {
            return m.getRawType().getName();
        }
        return am.getParameterClass(0).getName();
    }
    
    @Override
    public TypeResolverBuilder<?> findTypeResolver(final MapperConfig<?> config, final AnnotatedClass ac, final JavaType baseType) {
        return this._findTypeResolver(config, ac, baseType);
    }
    
    @Override
    public TypeResolverBuilder<?> findPropertyTypeResolver(final MapperConfig<?> config, final AnnotatedMember am, final JavaType baseType) {
        if (baseType.isContainerType()) {
            return null;
        }
        return this._findTypeResolver(config, am, baseType);
    }
    
    @Override
    public TypeResolverBuilder<?> findPropertyContentTypeResolver(final MapperConfig<?> config, final AnnotatedMember am, final JavaType containerType) {
        if (!containerType.isContainerType()) {
            throw new IllegalArgumentException("Must call method with a container type (got " + containerType + ")");
        }
        return this._findTypeResolver(config, am, containerType);
    }
    
    @Override
    public List<NamedType> findSubtypes(final Annotated a) {
        final JsonSubTypes t = a.getAnnotation(JsonSubTypes.class);
        if (t == null) {
            return null;
        }
        final JsonSubTypes.Type[] types = t.value();
        final ArrayList<NamedType> result = new ArrayList<NamedType>(types.length);
        for (final JsonSubTypes.Type type : types) {
            result.add(new NamedType(type.value(), type.name()));
        }
        return result;
    }
    
    @Override
    public String findTypeName(final AnnotatedClass ac) {
        final JsonTypeName tn = ac.getAnnotation(JsonTypeName.class);
        return (tn == null) ? null : tn.value();
    }
    
    @Override
    public boolean isIgnorableMethod(final AnnotatedMethod m) {
        return this._isIgnorable(m);
    }
    
    @Override
    public boolean isIgnorableConstructor(final AnnotatedConstructor c) {
        return this._isIgnorable(c);
    }
    
    @Override
    public boolean isIgnorableField(final AnnotatedField f) {
        return this._isIgnorable(f);
    }
    
    @Override
    public Object findSerializer(final Annotated a) {
        final JsonSerialize ann = a.getAnnotation(JsonSerialize.class);
        if (ann != null) {
            final Class<? extends JsonSerializer<?>> serClass = ann.using();
            if (serClass != JsonSerializer.None.class) {
                return serClass;
            }
        }
        final JsonRawValue annRaw = a.getAnnotation(JsonRawValue.class);
        if (annRaw != null && annRaw.value()) {
            final Class<?> cls = a.getRawType();
            return new RawSerializer(cls);
        }
        return null;
    }
    
    @Override
    public Class<? extends JsonSerializer<?>> findKeySerializer(final Annotated a) {
        final JsonSerialize ann = a.getAnnotation(JsonSerialize.class);
        if (ann != null) {
            final Class<? extends JsonSerializer<?>> serClass = ann.keyUsing();
            if (serClass != JsonSerializer.None.class) {
                return serClass;
            }
        }
        return null;
    }
    
    @Override
    public Class<? extends JsonSerializer<?>> findContentSerializer(final Annotated a) {
        final JsonSerialize ann = a.getAnnotation(JsonSerialize.class);
        if (ann != null) {
            final Class<? extends JsonSerializer<?>> serClass = ann.contentUsing();
            if (serClass != JsonSerializer.None.class) {
                return serClass;
            }
        }
        return null;
    }
    
    @Override
    public JsonSerialize.Inclusion findSerializationInclusion(final Annotated a, final JsonSerialize.Inclusion defValue) {
        final JsonSerialize ann = a.getAnnotation(JsonSerialize.class);
        if (ann != null) {
            return ann.include();
        }
        final JsonWriteNullProperties oldAnn = a.getAnnotation(JsonWriteNullProperties.class);
        if (oldAnn != null) {
            final boolean writeNulls = oldAnn.value();
            return writeNulls ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_NULL;
        }
        return defValue;
    }
    
    @Override
    public Class<?> findSerializationType(final Annotated am) {
        final JsonSerialize ann = am.getAnnotation(JsonSerialize.class);
        if (ann != null) {
            final Class<?> cls = ann.as();
            if (cls != NoClass.class) {
                return cls;
            }
        }
        return null;
    }
    
    @Override
    public Class<?> findSerializationKeyType(final Annotated am, final JavaType baseType) {
        final JsonSerialize ann = am.getAnnotation(JsonSerialize.class);
        if (ann != null) {
            final Class<?> cls = ann.keyAs();
            if (cls != NoClass.class) {
                return cls;
            }
        }
        return null;
    }
    
    @Override
    public Class<?> findSerializationContentType(final Annotated am, final JavaType baseType) {
        final JsonSerialize ann = am.getAnnotation(JsonSerialize.class);
        if (ann != null) {
            final Class<?> cls = ann.contentAs();
            if (cls != NoClass.class) {
                return cls;
            }
        }
        return null;
    }
    
    @Override
    public JsonSerialize.Typing findSerializationTyping(final Annotated a) {
        final JsonSerialize ann = a.getAnnotation(JsonSerialize.class);
        return (ann == null) ? null : ann.typing();
    }
    
    @Override
    public Class<?>[] findSerializationViews(final Annotated a) {
        final JsonView ann = a.getAnnotation(JsonView.class);
        return (Class<?>[])((ann == null) ? null : ann.value());
    }
    
    @Override
    public String[] findSerializationPropertyOrder(final AnnotatedClass ac) {
        final JsonPropertyOrder order = ac.getAnnotation(JsonPropertyOrder.class);
        return (String[])((order == null) ? null : order.value());
    }
    
    @Override
    public Boolean findSerializationSortAlphabetically(final AnnotatedClass ac) {
        final JsonPropertyOrder order = ac.getAnnotation(JsonPropertyOrder.class);
        return (order == null) ? null : Boolean.valueOf(order.alphabetic());
    }
    
    @Override
    public String findGettablePropertyName(final AnnotatedMethod am) {
        final JsonProperty pann = am.getAnnotation(JsonProperty.class);
        if (pann != null) {
            return pann.value();
        }
        final JsonGetter ann = am.getAnnotation(JsonGetter.class);
        if (ann != null) {
            return ann.value();
        }
        if (am.hasAnnotation(JsonSerialize.class) || am.hasAnnotation(JsonView.class)) {
            return "";
        }
        return null;
    }
    
    @Override
    public boolean hasAsValueAnnotation(final AnnotatedMethod am) {
        final JsonValue ann = am.getAnnotation(JsonValue.class);
        return ann != null && ann.value();
    }
    
    @Override
    public String findSerializablePropertyName(final AnnotatedField af) {
        final JsonProperty pann = af.getAnnotation(JsonProperty.class);
        if (pann != null) {
            return pann.value();
        }
        if (af.hasAnnotation(JsonSerialize.class) || af.hasAnnotation(JsonView.class)) {
            return "";
        }
        return null;
    }
    
    @Override
    public Class<? extends JsonDeserializer<?>> findDeserializer(final Annotated a) {
        final JsonDeserialize ann = a.getAnnotation(JsonDeserialize.class);
        if (ann != null) {
            final Class<? extends JsonDeserializer<?>> deserClass = ann.using();
            if (deserClass != JsonDeserializer.None.class) {
                return deserClass;
            }
        }
        return null;
    }
    
    @Override
    public Class<? extends KeyDeserializer> findKeyDeserializer(final Annotated a) {
        final JsonDeserialize ann = a.getAnnotation(JsonDeserialize.class);
        if (ann != null) {
            final Class<? extends KeyDeserializer> deserClass = ann.keyUsing();
            if (deserClass != KeyDeserializer.None.class) {
                return deserClass;
            }
        }
        return null;
    }
    
    @Override
    public Class<? extends JsonDeserializer<?>> findContentDeserializer(final Annotated a) {
        final JsonDeserialize ann = a.getAnnotation(JsonDeserialize.class);
        if (ann != null) {
            final Class<? extends JsonDeserializer<?>> deserClass = ann.contentUsing();
            if (deserClass != JsonDeserializer.None.class) {
                return deserClass;
            }
        }
        return null;
    }
    
    @Override
    public Class<?> findDeserializationType(final Annotated am, final JavaType baseType, final String propName) {
        final JsonDeserialize ann = am.getAnnotation(JsonDeserialize.class);
        if (ann != null) {
            final Class<?> cls = ann.as();
            if (cls != NoClass.class) {
                return cls;
            }
        }
        return null;
    }
    
    @Override
    public Class<?> findDeserializationKeyType(final Annotated am, final JavaType baseKeyType, final String propName) {
        final JsonDeserialize ann = am.getAnnotation(JsonDeserialize.class);
        if (ann != null) {
            final Class<?> cls = ann.keyAs();
            if (cls != NoClass.class) {
                return cls;
            }
        }
        return null;
    }
    
    @Override
    public Class<?> findDeserializationContentType(final Annotated am, final JavaType baseContentType, final String propName) {
        final JsonDeserialize ann = am.getAnnotation(JsonDeserialize.class);
        if (ann != null) {
            final Class<?> cls = ann.contentAs();
            if (cls != NoClass.class) {
                return cls;
            }
        }
        return null;
    }
    
    @Override
    public Object findValueInstantiator(final AnnotatedClass ac) {
        final JsonValueInstantiator ann = ac.getAnnotation(JsonValueInstantiator.class);
        return (ann == null) ? null : ann.value();
    }
    
    @Override
    public String findSettablePropertyName(final AnnotatedMethod am) {
        final JsonProperty pann = am.getAnnotation(JsonProperty.class);
        if (pann != null) {
            return pann.value();
        }
        final JsonSetter ann = am.getAnnotation(JsonSetter.class);
        if (ann != null) {
            return ann.value();
        }
        if (am.hasAnnotation(JsonDeserialize.class) || am.hasAnnotation(JsonView.class) || am.hasAnnotation(JsonBackReference.class) || am.hasAnnotation(JsonManagedReference.class)) {
            return "";
        }
        return null;
    }
    
    @Override
    public boolean hasAnySetterAnnotation(final AnnotatedMethod am) {
        return am.hasAnnotation(JsonAnySetter.class);
    }
    
    @Override
    public boolean hasAnyGetterAnnotation(final AnnotatedMethod am) {
        return am.hasAnnotation(JsonAnyGetter.class);
    }
    
    @Override
    public boolean hasCreatorAnnotation(final Annotated a) {
        return a.hasAnnotation(JsonCreator.class);
    }
    
    @Override
    public String findDeserializablePropertyName(final AnnotatedField af) {
        final JsonProperty pann = af.getAnnotation(JsonProperty.class);
        if (pann != null) {
            return pann.value();
        }
        if (af.hasAnnotation(JsonDeserialize.class) || af.hasAnnotation(JsonView.class) || af.hasAnnotation(JsonBackReference.class) || af.hasAnnotation(JsonManagedReference.class)) {
            return "";
        }
        return null;
    }
    
    @Override
    public String findPropertyNameForParam(final AnnotatedParameter param) {
        if (param != null) {
            final JsonProperty pann = param.getAnnotation(JsonProperty.class);
            if (pann != null) {
                return pann.value();
            }
        }
        return null;
    }
    
    protected boolean _isIgnorable(final Annotated a) {
        final JsonIgnore ann = a.getAnnotation(JsonIgnore.class);
        return ann != null && ann.value();
    }
    
    protected TypeResolverBuilder<?> _findTypeResolver(final MapperConfig<?> config, final Annotated ann, final JavaType baseType) {
        final JsonTypeInfo info = ann.getAnnotation(JsonTypeInfo.class);
        final JsonTypeResolver resAnn = ann.getAnnotation(JsonTypeResolver.class);
        TypeResolverBuilder<?> b;
        if (resAnn != null) {
            if (info == null) {
                return null;
            }
            b = config.typeResolverBuilderInstance(ann, resAnn.value());
        }
        else {
            if (info == null) {
                return null;
            }
            if (info.use() == JsonTypeInfo.Id.NONE) {
                return this._constructNoTypeResolverBuilder();
            }
            b = this._constructStdTypeResolverBuilder();
        }
        final JsonTypeIdResolver idResInfo = ann.getAnnotation(JsonTypeIdResolver.class);
        final TypeIdResolver idRes = (idResInfo == null) ? null : config.typeIdResolverInstance(ann, idResInfo.value());
        if (idRes != null) {
            idRes.init(baseType);
        }
        b = (TypeResolverBuilder<?>)b.init(info.use(), idRes);
        JsonTypeInfo.As inclusion = info.include();
        if (inclusion == JsonTypeInfo.As.EXTERNAL_PROPERTY && ann instanceof AnnotatedClass) {
            inclusion = JsonTypeInfo.As.PROPERTY;
        }
        b = (TypeResolverBuilder<?>)b.inclusion(inclusion);
        b = (TypeResolverBuilder<?>)b.typeProperty(info.property());
        final Class<?> defaultImpl = info.defaultImpl();
        if (defaultImpl != JsonTypeInfo.None.class) {
            b = (TypeResolverBuilder<?>)b.defaultImpl(defaultImpl);
        }
        return b;
    }
    
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
        return new StdTypeResolverBuilder();
    }
    
    protected StdTypeResolverBuilder _constructNoTypeResolverBuilder() {
        return StdTypeResolverBuilder.noTypeInfoBuilder();
    }
}
