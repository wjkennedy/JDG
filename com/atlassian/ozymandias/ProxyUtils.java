// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public final class ProxyUtils
{
    @Nonnull
    public static <T> T safeProxyWithReturnValue(@Nonnull final Class<T> classToProxy, @Nonnull final T objectToProxy, @Nullable final Object returnValue) {
        return safeProxyWithReturnValueSupplier(classToProxy, objectToProxy, new ExceptionSafeInvocationHandler.ReturnValueSupplier() {
            @Nullable
            @Override
            public Object get(@Nonnull final Method method, @Nonnull final Object... args) {
                return returnValue;
            }
        });
    }
    
    @Nonnull
    public static <T> T safeProxyWithReturnValueSupplier(@Nonnull final Class<T> classToProxy, @Nonnull final T objectToProxy, @Nullable final ExceptionSafeInvocationHandler.ReturnValueSupplier returnValueSupplier) {
        return (T)Proxy.newProxyInstance(classToProxy.getClassLoader(), new Class[] { classToProxy }, new ExceptionSafeInvocationHandler(objectToProxy, returnValueSupplier));
    }
    
    private ProxyUtils() {
    }
}
