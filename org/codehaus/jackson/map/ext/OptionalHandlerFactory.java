// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ext;

import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.map.util.Provider;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.SerializationConfig;

public class OptionalHandlerFactory
{
    private static final String PACKAGE_PREFIX_JODA_DATETIME = "org.joda.time.";
    private static final String PACKAGE_PREFIX_JAVAX_XML = "javax.xml.";
    private static final String SERIALIZERS_FOR_JODA_DATETIME = "org.codehaus.jackson.map.ext.JodaSerializers";
    private static final String SERIALIZERS_FOR_JAVAX_XML = "org.codehaus.jackson.map.ext.CoreXMLSerializers";
    private static final String DESERIALIZERS_FOR_JODA_DATETIME = "org.codehaus.jackson.map.ext.JodaDeserializers";
    private static final String DESERIALIZERS_FOR_JAVAX_XML = "org.codehaus.jackson.map.ext.CoreXMLDeserializers";
    private static final String CLASS_NAME_DOM_NODE = "org.w3c.dom.Node";
    private static final String CLASS_NAME_DOM_DOCUMENT = "org.w3c.dom.Node";
    private static final String SERIALIZER_FOR_DOM_NODE = "org.codehaus.jackson.map.ext.DOMSerializer";
    private static final String DESERIALIZER_FOR_DOM_DOCUMENT = "org.codehaus.jackson.map.ext.DOMDeserializer$DocumentDeserializer";
    private static final String DESERIALIZER_FOR_DOM_NODE = "org.codehaus.jackson.map.ext.DOMDeserializer$NodeDeserializer";
    public static final OptionalHandlerFactory instance;
    
    protected OptionalHandlerFactory() {
    }
    
    public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type) {
        final Class<?> rawType = type.getRawClass();
        final String className = rawType.getName();
        String factoryName;
        if (className.startsWith("org.joda.time.")) {
            factoryName = "org.codehaus.jackson.map.ext.JodaSerializers";
        }
        else if (className.startsWith("javax.xml.") || this.hasSupertypeStartingWith(rawType, "javax.xml.")) {
            factoryName = "org.codehaus.jackson.map.ext.CoreXMLSerializers";
        }
        else {
            if (this.doesImplement(rawType, "org.w3c.dom.Node")) {
                return (JsonSerializer)this.instantiate("org.codehaus.jackson.map.ext.DOMSerializer");
            }
            return null;
        }
        final Object ob = this.instantiate(factoryName);
        if (ob == null) {
            return null;
        }
        final Provider<Map.Entry<Class<?>, JsonSerializer<?>>> prov = (Provider<Map.Entry<Class<?>, JsonSerializer<?>>>)ob;
        final Collection<Map.Entry<Class<?>, JsonSerializer<?>>> entries = prov.provide();
        for (final Map.Entry<Class<?>, JsonSerializer<?>> entry : entries) {
            if (rawType == entry.getKey()) {
                return entry.getValue();
            }
        }
        for (final Map.Entry<Class<?>, JsonSerializer<?>> entry : entries) {
            if (entry.getKey().isAssignableFrom(rawType)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public JsonDeserializer<?> findDeserializer(final JavaType type, final DeserializationConfig config, final DeserializerProvider p) {
        final Class<?> rawType = type.getRawClass();
        final String className = rawType.getName();
        String factoryName;
        if (className.startsWith("org.joda.time.")) {
            factoryName = "org.codehaus.jackson.map.ext.JodaDeserializers";
        }
        else if (className.startsWith("javax.xml.") || this.hasSupertypeStartingWith(rawType, "javax.xml.")) {
            factoryName = "org.codehaus.jackson.map.ext.CoreXMLDeserializers";
        }
        else {
            if (this.doesImplement(rawType, "org.w3c.dom.Node")) {
                return (JsonDeserializer)this.instantiate("org.codehaus.jackson.map.ext.DOMDeserializer$DocumentDeserializer");
            }
            if (this.doesImplement(rawType, "org.w3c.dom.Node")) {
                return (JsonDeserializer)this.instantiate("org.codehaus.jackson.map.ext.DOMDeserializer$NodeDeserializer");
            }
            return null;
        }
        final Object ob = this.instantiate(factoryName);
        if (ob == null) {
            return null;
        }
        final Provider<StdDeserializer<?>> prov = (Provider<StdDeserializer<?>>)ob;
        final Collection<StdDeserializer<?>> entries = prov.provide();
        for (final StdDeserializer<?> deser : entries) {
            if (rawType == deser.getValueClass()) {
                return deser;
            }
        }
        for (final StdDeserializer<?> deser : entries) {
            if (deser.getValueClass().isAssignableFrom(rawType)) {
                return deser;
            }
        }
        return null;
    }
    
    private Object instantiate(final String className) {
        try {
            return Class.forName(className).newInstance();
        }
        catch (final LinkageError linkageError) {}
        catch (final Exception ex) {}
        return null;
    }
    
    private boolean doesImplement(final Class<?> actualType, final String classNameToImplement) {
        for (Class<?> type = actualType; type != null; type = type.getSuperclass()) {
            if (type.getName().equals(classNameToImplement)) {
                return true;
            }
            if (this.hasInterface(type, classNameToImplement)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasInterface(final Class<?> type, final String interfaceToImplement) {
        final Class<?>[] interfaces2;
        final Class<?>[] interfaces = interfaces2 = type.getInterfaces();
        for (final Class<?> iface : interfaces2) {
            if (iface.getName().equals(interfaceToImplement)) {
                return true;
            }
        }
        for (final Class<?> iface : interfaces) {
            if (this.hasInterface(iface, interfaceToImplement)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasSupertypeStartingWith(final Class<?> rawType, final String prefix) {
        for (Class<?> supertype = rawType.getSuperclass(); supertype != null; supertype = supertype.getSuperclass()) {
            if (supertype.getName().startsWith(prefix)) {
                return true;
            }
        }
        for (Class<?> cls = rawType; cls != null; cls = cls.getSuperclass()) {
            if (this.hasInterfaceStartingWith(cls, prefix)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasInterfaceStartingWith(final Class<?> type, final String prefix) {
        final Class<?>[] interfaces2;
        final Class<?>[] interfaces = interfaces2 = type.getInterfaces();
        for (final Class<?> iface : interfaces2) {
            if (iface.getName().startsWith(prefix)) {
                return true;
            }
        }
        for (final Class<?> iface : interfaces) {
            if (this.hasInterfaceStartingWith(iface, prefix)) {
                return true;
            }
        }
        return false;
    }
    
    static {
        instance = new OptionalHandlerFactory();
    }
}
