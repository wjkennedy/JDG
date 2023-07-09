// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import java.util.ArrayList;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.jsontype.NamedType;
import java.util.List;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Collection;
import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;

public abstract class AnnotationIntrospector
{
    public static AnnotationIntrospector nopInstance() {
        return NopAnnotationIntrospector.instance;
    }
    
    public static AnnotationIntrospector pair(final AnnotationIntrospector a1, final AnnotationIntrospector a2) {
        return new Pair(a1, a2);
    }
    
    public Collection<AnnotationIntrospector> allIntrospectors() {
        return Collections.singletonList(this);
    }
    
    public Collection<AnnotationIntrospector> allIntrospectors(final Collection<AnnotationIntrospector> result) {
        result.add(this);
        return result;
    }
    
    public abstract boolean isHandled(final Annotation p0);
    
    public Boolean findCachability(final AnnotatedClass ac) {
        return null;
    }
    
    public abstract String findRootName(final AnnotatedClass p0);
    
    public abstract String[] findPropertiesToIgnore(final AnnotatedClass p0);
    
    public abstract Boolean findIgnoreUnknownProperties(final AnnotatedClass p0);
    
    public Boolean isIgnorableType(final AnnotatedClass ac) {
        return null;
    }
    
    public Object findFilterId(final AnnotatedClass ac) {
        return null;
    }
    
    public VisibilityChecker<?> findAutoDetectVisibility(final AnnotatedClass ac, final VisibilityChecker<?> checker) {
        return checker;
    }
    
    public TypeResolverBuilder<?> findTypeResolver(final MapperConfig<?> config, final AnnotatedClass ac, final JavaType baseType) {
        return null;
    }
    
    public TypeResolverBuilder<?> findPropertyTypeResolver(final MapperConfig<?> config, final AnnotatedMember am, final JavaType baseType) {
        return null;
    }
    
    public TypeResolverBuilder<?> findPropertyContentTypeResolver(final MapperConfig<?> config, final AnnotatedMember am, final JavaType containerType) {
        return null;
    }
    
    public List<NamedType> findSubtypes(final Annotated a) {
        return null;
    }
    
    public String findTypeName(final AnnotatedClass ac) {
        return null;
    }
    
    public ReferenceProperty findReferenceType(final AnnotatedMember member) {
        return null;
    }
    
    public Boolean shouldUnwrapProperty(final AnnotatedMember member) {
        return null;
    }
    
    public boolean hasIgnoreMarker(final AnnotatedMember m) {
        if (m instanceof AnnotatedMethod) {
            return this.isIgnorableMethod((AnnotatedMethod)m);
        }
        if (m instanceof AnnotatedField) {
            return this.isIgnorableField((AnnotatedField)m);
        }
        return m instanceof AnnotatedConstructor && this.isIgnorableConstructor((AnnotatedConstructor)m);
    }
    
    public Object findInjectableValueId(final AnnotatedMember m) {
        return null;
    }
    
    public abstract boolean isIgnorableMethod(final AnnotatedMethod p0);
    
    public abstract boolean isIgnorableConstructor(final AnnotatedConstructor p0);
    
    public abstract boolean isIgnorableField(final AnnotatedField p0);
    
    public abstract Object findSerializer(final Annotated p0);
    
    public Class<? extends JsonSerializer<?>> findKeySerializer(final Annotated am) {
        return null;
    }
    
    public Class<? extends JsonSerializer<?>> findContentSerializer(final Annotated am) {
        return null;
    }
    
    public JsonSerialize.Inclusion findSerializationInclusion(final Annotated a, final JsonSerialize.Inclusion defValue) {
        return defValue;
    }
    
    public abstract Class<?> findSerializationType(final Annotated p0);
    
    public Class<?> findSerializationKeyType(final Annotated am, final JavaType baseType) {
        return null;
    }
    
    public Class<?> findSerializationContentType(final Annotated am, final JavaType baseType) {
        return null;
    }
    
    public abstract JsonSerialize.Typing findSerializationTyping(final Annotated p0);
    
    public abstract Class<?>[] findSerializationViews(final Annotated p0);
    
    public abstract String[] findSerializationPropertyOrder(final AnnotatedClass p0);
    
    public abstract Boolean findSerializationSortAlphabetically(final AnnotatedClass p0);
    
    public abstract String findGettablePropertyName(final AnnotatedMethod p0);
    
    public abstract boolean hasAsValueAnnotation(final AnnotatedMethod p0);
    
    public String findEnumValue(final Enum<?> value) {
        return value.name();
    }
    
    public abstract String findSerializablePropertyName(final AnnotatedField p0);
    
    public abstract Object findDeserializer(final Annotated p0);
    
    public abstract Class<? extends KeyDeserializer> findKeyDeserializer(final Annotated p0);
    
    public abstract Class<? extends JsonDeserializer<?>> findContentDeserializer(final Annotated p0);
    
    public abstract Class<?> findDeserializationType(final Annotated p0, final JavaType p1, final String p2);
    
    public abstract Class<?> findDeserializationKeyType(final Annotated p0, final JavaType p1, final String p2);
    
    public abstract Class<?> findDeserializationContentType(final Annotated p0, final JavaType p1, final String p2);
    
    public Object findValueInstantiator(final AnnotatedClass ac) {
        return null;
    }
    
    public abstract String findSettablePropertyName(final AnnotatedMethod p0);
    
    public boolean hasAnySetterAnnotation(final AnnotatedMethod am) {
        return false;
    }
    
    public boolean hasAnyGetterAnnotation(final AnnotatedMethod am) {
        return false;
    }
    
    public boolean hasCreatorAnnotation(final Annotated a) {
        return false;
    }
    
    public abstract String findDeserializablePropertyName(final AnnotatedField p0);
    
    public abstract String findPropertyNameForParam(final AnnotatedParameter p0);
    
    public static class ReferenceProperty
    {
        private final Type _type;
        private final String _name;
        
        public ReferenceProperty(final Type t, final String n) {
            this._type = t;
            this._name = n;
        }
        
        public static ReferenceProperty managed(final String name) {
            return new ReferenceProperty(Type.MANAGED_REFERENCE, name);
        }
        
        public static ReferenceProperty back(final String name) {
            return new ReferenceProperty(Type.BACK_REFERENCE, name);
        }
        
        public Type getType() {
            return this._type;
        }
        
        public String getName() {
            return this._name;
        }
        
        public boolean isManagedReference() {
            return this._type == Type.MANAGED_REFERENCE;
        }
        
        public boolean isBackReference() {
            return this._type == Type.BACK_REFERENCE;
        }
        
        public enum Type
        {
            MANAGED_REFERENCE, 
            BACK_REFERENCE;
        }
    }
    
    public static class Pair extends AnnotationIntrospector
    {
        protected final AnnotationIntrospector _primary;
        protected final AnnotationIntrospector _secondary;
        
        public Pair(final AnnotationIntrospector p, final AnnotationIntrospector s) {
            this._primary = p;
            this._secondary = s;
        }
        
        public static AnnotationIntrospector create(final AnnotationIntrospector primary, final AnnotationIntrospector secondary) {
            if (primary == null) {
                return secondary;
            }
            if (secondary == null) {
                return primary;
            }
            return new Pair(primary, secondary);
        }
        
        @Override
        public Collection<AnnotationIntrospector> allIntrospectors() {
            return this.allIntrospectors(new ArrayList<AnnotationIntrospector>());
        }
        
        @Override
        public Collection<AnnotationIntrospector> allIntrospectors(final Collection<AnnotationIntrospector> result) {
            this._primary.allIntrospectors(result);
            this._secondary.allIntrospectors(result);
            return result;
        }
        
        @Override
        public boolean isHandled(final Annotation ann) {
            return this._primary.isHandled(ann) || this._secondary.isHandled(ann);
        }
        
        @Override
        public Boolean findCachability(final AnnotatedClass ac) {
            Boolean result = this._primary.findCachability(ac);
            if (result == null) {
                result = this._secondary.findCachability(ac);
            }
            return result;
        }
        
        @Override
        public String findRootName(final AnnotatedClass ac) {
            final String name1 = this._primary.findRootName(ac);
            if (name1 == null) {
                return this._secondary.findRootName(ac);
            }
            if (name1.length() > 0) {
                return name1;
            }
            final String name2 = this._secondary.findRootName(ac);
            return (name2 == null) ? name1 : name2;
        }
        
        @Override
        public String[] findPropertiesToIgnore(final AnnotatedClass ac) {
            String[] result = this._primary.findPropertiesToIgnore(ac);
            if (result == null) {
                result = this._secondary.findPropertiesToIgnore(ac);
            }
            return result;
        }
        
        @Override
        public Boolean findIgnoreUnknownProperties(final AnnotatedClass ac) {
            Boolean result = this._primary.findIgnoreUnknownProperties(ac);
            if (result == null) {
                result = this._secondary.findIgnoreUnknownProperties(ac);
            }
            return result;
        }
        
        @Override
        public Boolean isIgnorableType(final AnnotatedClass ac) {
            Boolean result = this._primary.isIgnorableType(ac);
            if (result == null) {
                result = this._secondary.isIgnorableType(ac);
            }
            return result;
        }
        
        @Override
        public Object findFilterId(final AnnotatedClass ac) {
            Object id = this._primary.findFilterId(ac);
            if (id == null) {
                id = this._secondary.findFilterId(ac);
            }
            return id;
        }
        
        @Override
        public VisibilityChecker<?> findAutoDetectVisibility(final AnnotatedClass ac, VisibilityChecker<?> checker) {
            checker = this._secondary.findAutoDetectVisibility(ac, checker);
            return this._primary.findAutoDetectVisibility(ac, checker);
        }
        
        @Override
        public TypeResolverBuilder<?> findTypeResolver(final MapperConfig<?> config, final AnnotatedClass ac, final JavaType baseType) {
            TypeResolverBuilder<?> b = this._primary.findTypeResolver(config, ac, baseType);
            if (b == null) {
                b = this._secondary.findTypeResolver(config, ac, baseType);
            }
            return b;
        }
        
        @Override
        public TypeResolverBuilder<?> findPropertyTypeResolver(final MapperConfig<?> config, final AnnotatedMember am, final JavaType baseType) {
            TypeResolverBuilder<?> b = this._primary.findPropertyTypeResolver(config, am, baseType);
            if (b == null) {
                b = this._secondary.findPropertyTypeResolver(config, am, baseType);
            }
            return b;
        }
        
        @Override
        public TypeResolverBuilder<?> findPropertyContentTypeResolver(final MapperConfig<?> config, final AnnotatedMember am, final JavaType baseType) {
            TypeResolverBuilder<?> b = this._primary.findPropertyContentTypeResolver(config, am, baseType);
            if (b == null) {
                b = this._secondary.findPropertyContentTypeResolver(config, am, baseType);
            }
            return b;
        }
        
        @Override
        public List<NamedType> findSubtypes(final Annotated a) {
            final List<NamedType> types1 = this._primary.findSubtypes(a);
            final List<NamedType> types2 = this._secondary.findSubtypes(a);
            if (types1 == null || types1.isEmpty()) {
                return types2;
            }
            if (types2 == null || types2.isEmpty()) {
                return types1;
            }
            final ArrayList<NamedType> result = new ArrayList<NamedType>(types1.size() + types2.size());
            result.addAll(types1);
            result.addAll(types2);
            return result;
        }
        
        @Override
        public String findTypeName(final AnnotatedClass ac) {
            String name = this._primary.findTypeName(ac);
            if (name == null || name.length() == 0) {
                name = this._secondary.findTypeName(ac);
            }
            return name;
        }
        
        @Override
        public ReferenceProperty findReferenceType(final AnnotatedMember member) {
            ReferenceProperty ref = this._primary.findReferenceType(member);
            if (ref == null) {
                ref = this._secondary.findReferenceType(member);
            }
            return ref;
        }
        
        @Override
        public Boolean shouldUnwrapProperty(final AnnotatedMember member) {
            Boolean value = this._primary.shouldUnwrapProperty(member);
            if (value == null) {
                value = this._secondary.shouldUnwrapProperty(member);
            }
            return value;
        }
        
        @Override
        public Object findInjectableValueId(final AnnotatedMember m) {
            Object value = this._primary.findInjectableValueId(m);
            if (value == null) {
                value = this._secondary.findInjectableValueId(m);
            }
            return value;
        }
        
        @Override
        public boolean hasIgnoreMarker(final AnnotatedMember m) {
            return this._primary.hasIgnoreMarker(m) || this._secondary.hasIgnoreMarker(m);
        }
        
        @Override
        public boolean isIgnorableMethod(final AnnotatedMethod m) {
            return this._primary.isIgnorableMethod(m) || this._secondary.isIgnorableMethod(m);
        }
        
        @Override
        public boolean isIgnorableConstructor(final AnnotatedConstructor c) {
            return this._primary.isIgnorableConstructor(c) || this._secondary.isIgnorableConstructor(c);
        }
        
        @Override
        public boolean isIgnorableField(final AnnotatedField f) {
            return this._primary.isIgnorableField(f) || this._secondary.isIgnorableField(f);
        }
        
        @Override
        public Object findSerializer(final Annotated am) {
            Object result = this._primary.findSerializer(am);
            if (result == null) {
                result = this._secondary.findSerializer(am);
            }
            return result;
        }
        
        @Override
        public Class<? extends JsonSerializer<?>> findKeySerializer(final Annotated a) {
            Class<? extends JsonSerializer<?>> result = this._primary.findKeySerializer(a);
            if (result == null || result == JsonSerializer.None.class) {
                result = this._secondary.findKeySerializer(a);
            }
            return result;
        }
        
        @Override
        public Class<? extends JsonSerializer<?>> findContentSerializer(final Annotated a) {
            Class<? extends JsonSerializer<?>> result = this._primary.findContentSerializer(a);
            if (result == null || result == JsonSerializer.None.class) {
                result = this._secondary.findContentSerializer(a);
            }
            return result;
        }
        
        @Override
        public JsonSerialize.Inclusion findSerializationInclusion(final Annotated a, JsonSerialize.Inclusion defValue) {
            defValue = this._secondary.findSerializationInclusion(a, defValue);
            defValue = this._primary.findSerializationInclusion(a, defValue);
            return defValue;
        }
        
        @Override
        public Class<?> findSerializationType(final Annotated a) {
            Class<?> result = this._primary.findSerializationType(a);
            if (result == null) {
                result = this._secondary.findSerializationType(a);
            }
            return result;
        }
        
        @Override
        public Class<?> findSerializationKeyType(final Annotated am, final JavaType baseType) {
            Class<?> result = this._primary.findSerializationKeyType(am, baseType);
            if (result == null) {
                result = this._secondary.findSerializationKeyType(am, baseType);
            }
            return result;
        }
        
        @Override
        public Class<?> findSerializationContentType(final Annotated am, final JavaType baseType) {
            Class<?> result = this._primary.findSerializationContentType(am, baseType);
            if (result == null) {
                result = this._secondary.findSerializationContentType(am, baseType);
            }
            return result;
        }
        
        @Override
        public JsonSerialize.Typing findSerializationTyping(final Annotated a) {
            JsonSerialize.Typing result = this._primary.findSerializationTyping(a);
            if (result == null) {
                result = this._secondary.findSerializationTyping(a);
            }
            return result;
        }
        
        @Override
        public Class<?>[] findSerializationViews(final Annotated a) {
            Class<?>[] result = this._primary.findSerializationViews(a);
            if (result == null) {
                result = this._secondary.findSerializationViews(a);
            }
            return result;
        }
        
        @Override
        public String[] findSerializationPropertyOrder(final AnnotatedClass ac) {
            String[] result = this._primary.findSerializationPropertyOrder(ac);
            if (result == null) {
                result = this._secondary.findSerializationPropertyOrder(ac);
            }
            return result;
        }
        
        @Override
        public Boolean findSerializationSortAlphabetically(final AnnotatedClass ac) {
            Boolean result = this._primary.findSerializationSortAlphabetically(ac);
            if (result == null) {
                result = this._secondary.findSerializationSortAlphabetically(ac);
            }
            return result;
        }
        
        @Override
        public String findGettablePropertyName(final AnnotatedMethod am) {
            String result = this._primary.findGettablePropertyName(am);
            if (result == null) {
                result = this._secondary.findGettablePropertyName(am);
            }
            else if (result.length() == 0) {
                final String str2 = this._secondary.findGettablePropertyName(am);
                if (str2 != null) {
                    result = str2;
                }
            }
            return result;
        }
        
        @Override
        public boolean hasAsValueAnnotation(final AnnotatedMethod am) {
            return this._primary.hasAsValueAnnotation(am) || this._secondary.hasAsValueAnnotation(am);
        }
        
        @Override
        public String findEnumValue(final Enum<?> value) {
            String result = this._primary.findEnumValue(value);
            if (result == null) {
                result = this._secondary.findEnumValue(value);
            }
            return result;
        }
        
        @Override
        public String findSerializablePropertyName(final AnnotatedField af) {
            String result = this._primary.findSerializablePropertyName(af);
            if (result == null) {
                result = this._secondary.findSerializablePropertyName(af);
            }
            else if (result.length() == 0) {
                final String str2 = this._secondary.findSerializablePropertyName(af);
                if (str2 != null) {
                    result = str2;
                }
            }
            return result;
        }
        
        @Override
        public Object findDeserializer(final Annotated am) {
            Object result = this._primary.findDeserializer(am);
            if (result == null) {
                result = this._secondary.findDeserializer(am);
            }
            return result;
        }
        
        @Override
        public Class<? extends KeyDeserializer> findKeyDeserializer(final Annotated am) {
            Class<? extends KeyDeserializer> result = this._primary.findKeyDeserializer(am);
            if (result == null || result == KeyDeserializer.None.class) {
                result = this._secondary.findKeyDeserializer(am);
            }
            return result;
        }
        
        @Override
        public Class<? extends JsonDeserializer<?>> findContentDeserializer(final Annotated am) {
            Class<? extends JsonDeserializer<?>> result = this._primary.findContentDeserializer(am);
            if (result == null || result == JsonDeserializer.None.class) {
                result = this._secondary.findContentDeserializer(am);
            }
            return result;
        }
        
        @Override
        public Class<?> findDeserializationType(final Annotated am, final JavaType baseType, final String propName) {
            Class<?> result = this._primary.findDeserializationType(am, baseType, propName);
            if (result == null) {
                result = this._secondary.findDeserializationType(am, baseType, propName);
            }
            return result;
        }
        
        @Override
        public Class<?> findDeserializationKeyType(final Annotated am, final JavaType baseKeyType, final String propName) {
            Class<?> result = this._primary.findDeserializationKeyType(am, baseKeyType, propName);
            if (result == null) {
                result = this._secondary.findDeserializationKeyType(am, baseKeyType, propName);
            }
            return result;
        }
        
        @Override
        public Class<?> findDeserializationContentType(final Annotated am, final JavaType baseContentType, final String propName) {
            Class<?> result = this._primary.findDeserializationContentType(am, baseContentType, propName);
            if (result == null) {
                result = this._secondary.findDeserializationContentType(am, baseContentType, propName);
            }
            return result;
        }
        
        @Override
        public Object findValueInstantiator(final AnnotatedClass ac) {
            Object result = this._primary.findValueInstantiator(ac);
            if (result == null) {
                result = this._secondary.findValueInstantiator(ac);
            }
            return result;
        }
        
        @Override
        public String findSettablePropertyName(final AnnotatedMethod am) {
            String result = this._primary.findSettablePropertyName(am);
            if (result == null) {
                result = this._secondary.findSettablePropertyName(am);
            }
            else if (result.length() == 0) {
                final String str2 = this._secondary.findSettablePropertyName(am);
                if (str2 != null) {
                    result = str2;
                }
            }
            return result;
        }
        
        @Override
        public boolean hasAnySetterAnnotation(final AnnotatedMethod am) {
            return this._primary.hasAnySetterAnnotation(am) || this._secondary.hasAnySetterAnnotation(am);
        }
        
        @Override
        public boolean hasAnyGetterAnnotation(final AnnotatedMethod am) {
            return this._primary.hasAnyGetterAnnotation(am) || this._secondary.hasAnyGetterAnnotation(am);
        }
        
        @Override
        public boolean hasCreatorAnnotation(final Annotated a) {
            return this._primary.hasCreatorAnnotation(a) || this._secondary.hasCreatorAnnotation(a);
        }
        
        @Override
        public String findDeserializablePropertyName(final AnnotatedField af) {
            String result = this._primary.findDeserializablePropertyName(af);
            if (result == null) {
                result = this._secondary.findDeserializablePropertyName(af);
            }
            else if (result.length() == 0) {
                final String str2 = this._secondary.findDeserializablePropertyName(af);
                if (str2 != null) {
                    result = str2;
                }
            }
            return result;
        }
        
        @Override
        public String findPropertyNameForParam(final AnnotatedParameter param) {
            String result = this._primary.findPropertyNameForParam(param);
            if (result == null) {
                result = this._secondary.findPropertyNameForParam(param);
            }
            return result;
        }
    }
}
