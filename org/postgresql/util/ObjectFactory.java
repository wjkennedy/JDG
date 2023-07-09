// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.Properties;

public class ObjectFactory
{
    public static <T> T instantiate(final Class<T> expectedClass, final String classname, final Properties info, final boolean tryString, final String stringarg) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object[] args = { info };
        Constructor<? extends T> ctor = null;
        final Class<? extends T> cls = Class.forName(classname).asSubclass(expectedClass);
        try {
            ctor = cls.getConstructor(Properties.class);
        }
        catch (final NoSuchMethodException ex) {}
        if (tryString && ctor == null) {
            try {
                ctor = cls.getConstructor(String.class);
                args = new String[] { stringarg };
            }
            catch (final NoSuchMethodException ex2) {}
        }
        if (ctor == null) {
            ctor = cls.getConstructor((Class<?>[])new Class[0]);
            args = new Object[0];
        }
        return (T)ctor.newInstance(args);
    }
}
