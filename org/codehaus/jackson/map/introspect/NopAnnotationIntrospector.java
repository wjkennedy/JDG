// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import java.lang.annotation.Annotation;
import org.codehaus.jackson.map.AnnotationIntrospector;

public class NopAnnotationIntrospector extends AnnotationIntrospector
{
    public static final NopAnnotationIntrospector instance;
    
    @Override
    public boolean isHandled(final Annotation ann) {
        return false;
    }
    
    @Override
    public String findEnumValue(final Enum<?> value) {
        return value.name();
    }
    
    @Override
    public String findRootName(final AnnotatedClass ac) {
        return null;
    }
    
    @Override
    public String[] findPropertiesToIgnore(final AnnotatedClass ac) {
        return null;
    }
    
    @Override
    public Boolean findIgnoreUnknownProperties(final AnnotatedClass ac) {
        return null;
    }
    
    @Override
    public boolean hasIgnoreMarker(final AnnotatedMember member) {
        return false;
    }
    
    @Override
    public boolean isIgnorableConstructor(final AnnotatedConstructor c) {
        return false;
    }
    
    @Override
    public boolean isIgnorableMethod(final AnnotatedMethod m) {
        return false;
    }
    
    @Override
    public boolean isIgnorableField(final AnnotatedField f) {
        return false;
    }
    
    @Override
    public Object findSerializer(final Annotated am) {
        return null;
    }
    
    @Override
    public Class<?> findSerializationType(final Annotated a) {
        return null;
    }
    
    @Override
    public JsonSerialize.Typing findSerializationTyping(final Annotated a) {
        return null;
    }
    
    @Override
    public Class<?>[] findSerializationViews(final Annotated a) {
        return null;
    }
    
    @Override
    public String[] findSerializationPropertyOrder(final AnnotatedClass ac) {
        return null;
    }
    
    @Override
    public Boolean findSerializationSortAlphabetically(final AnnotatedClass ac) {
        return null;
    }
    
    @Override
    public String findGettablePropertyName(final AnnotatedMethod am) {
        return null;
    }
    
    @Override
    public boolean hasAsValueAnnotation(final AnnotatedMethod am) {
        return false;
    }
    
    @Override
    public String findDeserializablePropertyName(final AnnotatedField af) {
        return null;
    }
    
    @Override
    public Class<?> findDeserializationContentType(final Annotated am, final JavaType t, final String propName) {
        return null;
    }
    
    @Override
    public Class<?> findDeserializationKeyType(final Annotated am, final JavaType t, final String propName) {
        return null;
    }
    
    @Override
    public Class<?> findDeserializationType(final Annotated am, final JavaType t, final String propName) {
        return null;
    }
    
    @Override
    public Object findDeserializer(final Annotated am) {
        return null;
    }
    
    @Override
    public Class<KeyDeserializer> findKeyDeserializer(final Annotated am) {
        return null;
    }
    
    @Override
    public Class<JsonDeserializer<?>> findContentDeserializer(final Annotated am) {
        return null;
    }
    
    @Override
    public String findPropertyNameForParam(final AnnotatedParameter param) {
        return null;
    }
    
    @Override
    public String findSerializablePropertyName(final AnnotatedField af) {
        return null;
    }
    
    @Override
    public String findSettablePropertyName(final AnnotatedMethod am) {
        return null;
    }
    
    static {
        instance = new NopAnnotationIntrospector();
    }
}
