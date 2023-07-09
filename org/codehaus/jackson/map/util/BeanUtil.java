// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import org.codehaus.jackson.map.introspect.AnnotatedMethod;

public class BeanUtil
{
    public static String okNameForGetter(final AnnotatedMethod am) {
        final String name = am.getName();
        String str = okNameForIsGetter(am, name);
        if (str == null) {
            str = okNameForRegularGetter(am, name);
        }
        return str;
    }
    
    public static String okNameForRegularGetter(final AnnotatedMethod am, final String name) {
        if (name.startsWith("get")) {
            if ("getCallbacks".equals(name)) {
                if (isCglibGetCallbacks(am)) {
                    return null;
                }
            }
            else if ("getMetaClass".equals(name) && isGroovyMetaClassGetter(am)) {
                return null;
            }
            return manglePropertyName(name.substring(3));
        }
        return null;
    }
    
    public static String okNameForIsGetter(final AnnotatedMethod am, final String name) {
        if (!name.startsWith("is")) {
            return null;
        }
        final Class<?> rt = am.getRawType();
        if (rt != Boolean.class && rt != Boolean.TYPE) {
            return null;
        }
        return manglePropertyName(name.substring(2));
    }
    
    public static String okNameForSetter(final AnnotatedMethod am) {
        String name = am.getName();
        if (!name.startsWith("set")) {
            return null;
        }
        name = manglePropertyName(name.substring(3));
        if (name == null) {
            return null;
        }
        if ("metaClass".equals(name) && isGroovyMetaClassSetter(am)) {
            return null;
        }
        return name;
    }
    
    protected static boolean isCglibGetCallbacks(final AnnotatedMethod am) {
        final Class<?> rt = am.getRawType();
        if (rt == null || !rt.isArray()) {
            return false;
        }
        final Class<?> compType = rt.getComponentType();
        final Package pkg = compType.getPackage();
        if (pkg != null) {
            final String pname = pkg.getName();
            if (pname.startsWith("net.sf.cglib") || pname.startsWith("org.hibernate.repackage.cglib")) {
                return true;
            }
        }
        return false;
    }
    
    protected static boolean isGroovyMetaClassSetter(final AnnotatedMethod am) {
        final Class<?> argType = am.getParameterClass(0);
        final Package pkg = argType.getPackage();
        return pkg != null && pkg.getName().startsWith("groovy.lang");
    }
    
    protected static boolean isGroovyMetaClassGetter(final AnnotatedMethod am) {
        final Class<?> rt = am.getRawType();
        if (rt == null || rt.isArray()) {
            return false;
        }
        final Package pkg = rt.getPackage();
        return pkg != null && pkg.getName().startsWith("groovy.lang");
    }
    
    protected static String manglePropertyName(final String basename) {
        final int len = basename.length();
        if (len == 0) {
            return null;
        }
        StringBuilder sb = null;
        for (int i = 0; i < len; ++i) {
            final char upper = basename.charAt(i);
            final char lower = Character.toLowerCase(upper);
            if (upper == lower) {
                break;
            }
            if (sb == null) {
                sb = new StringBuilder(basename);
            }
            sb.setCharAt(i, lower);
        }
        return (sb == null) ? basename : sb.toString();
    }
}
