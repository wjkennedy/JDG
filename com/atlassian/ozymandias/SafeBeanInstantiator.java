// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import org.slf4j.LoggerFactory;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.ozymandias.error.ErrorUtils;
import com.atlassian.ozymandias.error.ThrowableLogger;
import java.util.Objects;
import io.atlassian.fugue.Option;
import com.atlassian.plugin.Plugin;
import org.slf4j.Logger;

public final class SafeBeanInstantiator
{
    private static final Logger log;
    
    public static <T> Option<T> load(final Class<T> beanClass, final Plugin toInstantiateFrom) {
        Objects.requireNonNull(beanClass, "beanClass is required");
        Objects.requireNonNull(toInstantiateFrom, "toInstantiateFrom is required");
        SafeBeanInstantiator.log.debug("Attempting to instantiate a bean of type '{}' from plugin with key '{}'", (Object)beanClass, (Object)toInstantiateFrom.getKey());
        if (isNotContainerManagedPlugin(toInstantiateFrom)) {
            return (Option<T>)Option.none();
        }
        try {
            return (Option<T>)Option.some((Object)loadBean(beanClass, toInstantiateFrom));
        }
        catch (final Throwable t) {
            ErrorUtils.handleThrowable(t, String.format("Unable to instantiate a bean of type '%s' from plugin with key '%s' because of '%s - %s'.", beanClass, toInstantiateFrom.getKey(), ThrowableLogger.getClassName(t), t.getMessage()), SafeBeanInstantiator.log);
            return (Option<T>)Option.none();
        }
    }
    
    public static <T> Option<T> load(final String className, final Class<T> expectedType, final Class<?> callingClass, final Plugin toInstantiateFrom) {
        Objects.requireNonNull(className, "beanClass is required");
        Objects.requireNonNull(expectedType, "expectedType is required");
        Objects.requireNonNull(callingClass, "callingClass is required");
        Objects.requireNonNull(toInstantiateFrom, "toInstantiateFrom is required");
        SafeBeanInstantiator.log.debug("Attempting to instantiate a bean with class name '" + className + "' with expected supertype '{}' from plugin with key '{}'", (Object)expectedType, (Object)toInstantiateFrom.getKey());
        if (isNotContainerManagedPlugin(toInstantiateFrom)) {
            return (Option<T>)Option.none();
        }
        try {
            final Class<?> actualClass = toInstantiateFrom.loadClass(className, (Class)callingClass);
            final Object actualBean = loadBean(actualClass, toInstantiateFrom);
            if (!expectedType.isAssignableFrom(actualBean.getClass())) {
                SafeBeanInstantiator.log.warn("Instantiated bean of type '{}', but expected type was '{}'. Returning absent result.", (Object)actualBean.getClass(), (Object)expectedType);
                return (Option<T>)Option.none();
            }
            return (Option<T>)Option.some(actualBean);
        }
        catch (final Throwable t) {
            ErrorUtils.handleThrowable(t, String.format("Unable to instantiate a bean with class name '%s' with expected supertype '%s' from plugin with key '%s' because of '%s - %s'.", className, expectedType, toInstantiateFrom.getKey(), ThrowableLogger.getClassName(t), t.getMessage()), SafeBeanInstantiator.log);
            return (Option<T>)Option.none();
        }
    }
    
    private static boolean isNotContainerManagedPlugin(final Plugin plugin) {
        final boolean isNot = !(plugin instanceof ContainerManagedPlugin);
        if (isNot) {
            SafeBeanInstantiator.log.warn("Cannot instantiate bean for plugin with key '{}'. Plugin is not an instance of ContainerManagedPlugin", (Object)plugin.getKey());
        }
        return isNot;
    }
    
    private static <T> T loadBean(final Class<T> beanClass, final Plugin toLoadFrom) {
        return (T)((ContainerManagedPlugin)toLoadFrom).getContainerAccessor().createBean((Class)beanClass);
    }
    
    static {
        log = LoggerFactory.getLogger((Class)SafeBeanInstantiator.class);
    }
}
