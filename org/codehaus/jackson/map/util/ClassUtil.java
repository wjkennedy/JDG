// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.EnumSet;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public final class ClassUtil
{
    public static List<Class<?>> findSuperTypes(final Class<?> cls, final Class<?> endBefore) {
        return findSuperTypes(cls, endBefore, new ArrayList<Class<?>>(8));
    }
    
    public static List<Class<?>> findSuperTypes(final Class<?> cls, final Class<?> endBefore, final List<Class<?>> result) {
        _addSuperTypes(cls, endBefore, result, false);
        return result;
    }
    
    private static void _addSuperTypes(final Class<?> cls, final Class<?> endBefore, final Collection<Class<?>> result, final boolean addClassItself) {
        if (cls == endBefore || cls == null || cls == Object.class) {
            return;
        }
        if (addClassItself) {
            if (result.contains(cls)) {
                return;
            }
            result.add(cls);
        }
        for (final Class<?> intCls : cls.getInterfaces()) {
            _addSuperTypes(intCls, endBefore, result, true);
        }
        _addSuperTypes(cls.getSuperclass(), endBefore, result, true);
    }
    
    public static String canBeABeanType(final Class<?> type) {
        if (type.isAnnotation()) {
            return "annotation";
        }
        if (type.isArray()) {
            return "array";
        }
        if (type.isEnum()) {
            return "enum";
        }
        if (type.isPrimitive()) {
            return "primitive";
        }
        return null;
    }
    
    @Deprecated
    public static String isLocalType(final Class<?> type) {
        return isLocalType(type, false);
    }
    
    public static String isLocalType(final Class<?> type, final boolean allowNonStatic) {
        try {
            if (type.getEnclosingMethod() != null) {
                return "local/anonymous";
            }
            if (!allowNonStatic && type.getEnclosingClass() != null && !Modifier.isStatic(type.getModifiers())) {
                return "non-static member class";
            }
        }
        catch (final SecurityException ex) {}
        catch (final NullPointerException ex2) {}
        return null;
    }
    
    public static Class<?> getOuterClass(final Class<?> type) {
        try {
            if (type.getEnclosingMethod() != null) {
                return null;
            }
            if (!Modifier.isStatic(type.getModifiers())) {
                return type.getEnclosingClass();
            }
        }
        catch (final SecurityException ex) {}
        catch (final NullPointerException ex2) {}
        return null;
    }
    
    public static boolean isProxyType(final Class<?> type) {
        final String name = type.getName();
        return name.startsWith("net.sf.cglib.proxy.") || name.startsWith("org.hibernate.proxy.");
    }
    
    public static boolean isConcrete(final Class<?> type) {
        final int mod = type.getModifiers();
        return (mod & 0x600) == 0x0;
    }
    
    public static boolean isConcrete(final Member member) {
        final int mod = member.getModifiers();
        return (mod & 0x600) == 0x0;
    }
    
    public static boolean isCollectionMapOrArray(final Class<?> type) {
        return type.isArray() || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }
    
    public static String getClassDescription(final Object classOrInstance) {
        if (classOrInstance == null) {
            return "unknown";
        }
        final Class<?> cls = (classOrInstance instanceof Class) ? ((Class)classOrInstance) : classOrInstance.getClass();
        return cls.getName();
    }
    
    public static Class<?> findClass(final String className) throws ClassNotFoundException {
        if (className.indexOf(46) < 0) {
            if ("int".equals(className)) {
                return Integer.TYPE;
            }
            if ("long".equals(className)) {
                return Long.TYPE;
            }
            if ("float".equals(className)) {
                return Float.TYPE;
            }
            if ("double".equals(className)) {
                return Double.TYPE;
            }
            if ("boolean".equals(className)) {
                return Boolean.TYPE;
            }
            if ("byte".equals(className)) {
                return Byte.TYPE;
            }
            if ("char".equals(className)) {
                return Character.TYPE;
            }
            if ("short".equals(className)) {
                return Short.TYPE;
            }
            if ("void".equals(className)) {
                return Void.TYPE;
            }
        }
        Throwable prob = null;
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                return Class.forName(className, true, loader);
            }
            catch (final Exception e) {
                prob = getRootCause(e);
            }
        }
        try {
            return Class.forName(className);
        }
        catch (final Exception e) {
            if (prob == null) {
                prob = getRootCause(e);
            }
            if (prob instanceof RuntimeException) {
                throw (RuntimeException)prob;
            }
            throw new ClassNotFoundException(prob.getMessage(), prob);
        }
    }
    
    public static boolean hasGetterSignature(final Method m) {
        if (Modifier.isStatic(m.getModifiers())) {
            return false;
        }
        final Class<?>[] pts = m.getParameterTypes();
        return (pts == null || pts.length == 0) && Void.TYPE != m.getReturnType();
    }
    
    public static Throwable getRootCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
    
    public static void throwRootCause(Throwable t) throws Exception {
        t = getRootCause(t);
        if (t instanceof Exception) {
            throw (Exception)t;
        }
        throw (Error)t;
    }
    
    public static void throwAsIAE(final Throwable t) {
        throwAsIAE(t, t.getMessage());
    }
    
    public static void throwAsIAE(final Throwable t, final String msg) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        throw new IllegalArgumentException(msg, t);
    }
    
    public static void unwrapAndThrowAsIAE(final Throwable t) {
        throwAsIAE(getRootCause(t));
    }
    
    public static void unwrapAndThrowAsIAE(final Throwable t, final String msg) {
        throwAsIAE(getRootCause(t), msg);
    }
    
    public static <T> T createInstance(final Class<T> cls, final boolean canFixAccess) throws IllegalArgumentException {
        final Constructor<T> ctor = findConstructor(cls, canFixAccess);
        if (ctor == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " has no default (no arg) constructor");
        }
        try {
            return ctor.newInstance(new Object[0]);
        }
        catch (final Exception e) {
            unwrapAndThrowAsIAE(e, "Failed to instantiate class " + cls.getName() + ", problem: " + e.getMessage());
            return null;
        }
    }
    
    public static <T> Constructor<T> findConstructor(final Class<T> cls, final boolean canFixAccess) throws IllegalArgumentException {
        try {
            final Constructor<T> ctor = cls.getDeclaredConstructor((Class<?>[])new Class[0]);
            if (canFixAccess) {
                checkAndFixAccess(ctor);
            }
            else if (!Modifier.isPublic(ctor.getModifiers())) {
                throw new IllegalArgumentException("Default constructor for " + cls.getName() + " is not accessible (non-public?): not allowed to try modify access via Reflection: can not instantiate type");
            }
            return ctor;
        }
        catch (final NoSuchMethodException ex) {}
        catch (final Exception e) {
            unwrapAndThrowAsIAE(e, "Failed to find default constructor of class " + cls.getName() + ", problem: " + e.getMessage());
        }
        return null;
    }
    
    public static Object defaultValue(final Class<?> cls) {
        if (cls == Integer.TYPE) {
            return 0;
        }
        if (cls == Long.TYPE) {
            return 0L;
        }
        if (cls == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        if (cls == Double.TYPE) {
            return 0.0;
        }
        if (cls == Float.TYPE) {
            return 0.0f;
        }
        if (cls == Byte.TYPE) {
            return 0;
        }
        if (cls == Short.TYPE) {
            return 0;
        }
        if (cls == Character.TYPE) {
            return '\0';
        }
        throw new IllegalArgumentException("Class " + cls.getName() + " is not a primitive type");
    }
    
    public static Class<?> wrapperType(final Class<?> primitiveType) {
        if (primitiveType == Integer.TYPE) {
            return Integer.class;
        }
        if (primitiveType == Long.TYPE) {
            return Long.class;
        }
        if (primitiveType == Boolean.TYPE) {
            return Boolean.class;
        }
        if (primitiveType == Double.TYPE) {
            return Double.class;
        }
        if (primitiveType == Float.TYPE) {
            return Float.class;
        }
        if (primitiveType == Byte.TYPE) {
            return Byte.class;
        }
        if (primitiveType == Short.TYPE) {
            return Short.class;
        }
        if (primitiveType == Character.TYPE) {
            return Character.class;
        }
        throw new IllegalArgumentException("Class " + primitiveType.getName() + " is not a primitive type");
    }
    
    public static void checkAndFixAccess(final Member member) {
        final AccessibleObject ao = (AccessibleObject)member;
        try {
            ao.setAccessible(true);
        }
        catch (final SecurityException se) {
            if (!ao.isAccessible()) {
                final Class<?> declClass = member.getDeclaringClass();
                throw new IllegalArgumentException("Can not access " + member + " (from class " + declClass.getName() + "; failed to set access: " + se.getMessage());
            }
        }
    }
    
    public static Class<? extends Enum<?>> findEnumType(final EnumSet<?> s) {
        if (!s.isEmpty()) {
            return findEnumType(s.iterator().next());
        }
        return EnumTypeLocator.instance.enumTypeFor(s);
    }
    
    public static Class<? extends Enum<?>> findEnumType(final EnumMap<?, ?> m) {
        if (!m.isEmpty()) {
            return findEnumType((Enum<?>)m.keySet().iterator().next());
        }
        return EnumTypeLocator.instance.enumTypeFor(m);
    }
    
    public static Class<? extends Enum<?>> findEnumType(final Enum<?> en) {
        Class<?> ec = en.getClass();
        if (ec.getSuperclass() != Enum.class) {
            ec = ec.getSuperclass();
        }
        return (Class<? extends Enum<?>>)ec;
    }
    
    public static Class<? extends Enum<?>> findEnumType(Class<?> cls) {
        if (cls.getSuperclass() != Enum.class) {
            cls = cls.getSuperclass();
        }
        return (Class<? extends Enum<?>>)cls;
    }
    
    private static class EnumTypeLocator
    {
        static final EnumTypeLocator instance;
        private final Field enumSetTypeField;
        private final Field enumMapTypeField;
        
        private EnumTypeLocator() {
            this.enumSetTypeField = locateField(EnumSet.class, "elementType", Class.class);
            this.enumMapTypeField = locateField(EnumMap.class, "elementType", Class.class);
        }
        
        public Class<? extends Enum<?>> enumTypeFor(final EnumSet<?> set) {
            if (this.enumSetTypeField != null) {
                return (Class)this.get(set, this.enumSetTypeField);
            }
            throw new IllegalStateException("Can not figure out type for EnumSet (odd JDK platform?)");
        }
        
        public Class<? extends Enum<?>> enumTypeFor(final EnumMap<?, ?> set) {
            if (this.enumMapTypeField != null) {
                return (Class)this.get(set, this.enumMapTypeField);
            }
            throw new IllegalStateException("Can not figure out type for EnumMap (odd JDK platform?)");
        }
        
        private Object get(final Object bean, final Field field) {
            try {
                return field.get(bean);
            }
            catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        
        private static Field locateField(final Class<?> fromClass, final String expectedName, final Class<?> type) {
            Field found = null;
            final Field[] declaredFields;
            final Field[] fields = declaredFields = fromClass.getDeclaredFields();
            for (final Field f : declaredFields) {
                if (expectedName.equals(f.getName()) && f.getType() == type) {
                    found = f;
                    break;
                }
            }
            if (found == null) {
                for (final Field f : fields) {
                    if (f.getType() == type) {
                        if (found != null) {
                            return null;
                        }
                        found = f;
                    }
                }
            }
            if (found != null) {
                try {
                    found.setAccessible(true);
                }
                catch (final Throwable t) {}
            }
            return found;
        }
        
        static {
            instance = new EnumTypeLocator();
        }
    }
}
