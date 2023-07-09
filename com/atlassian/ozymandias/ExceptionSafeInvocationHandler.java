// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.commons.lang3.StringUtils;
import com.atlassian.ozymandias.error.ErrorUtils;
import java.lang.reflect.Method;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;

public class ExceptionSafeInvocationHandler implements InvocationHandler
{
    @Nonnull
    private final Object proxiedObject;
    private final ReturnValueSupplier returnValueSupplier;
    
    public ExceptionSafeInvocationHandler(@Nonnull final Object proxiedObject, @Nullable final ReturnValueSupplier returnValueSupplier) {
        this.proxiedObject = Objects.requireNonNull(proxiedObject);
        this.returnValueSupplier = returnValueSupplier;
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(this.proxiedObject, args);
        }
        catch (final LinkageError e) {
            ErrorUtils.handleThrowable(e, e.getMessage(), this.getLogger());
            return this.getFailureValue(method, args);
        }
        catch (final Exception e2) {
            final String message = (String)StringUtils.defaultIfEmpty((CharSequence)e2.getMessage(), (CharSequence)("Exception in " + method));
            ErrorUtils.handleThrowable(e2, message, this.getLogger());
            return this.getFailureValue(method, args);
        }
    }
    
    @Nullable
    private Object getFailureValue(@Nonnull final Method method, @Nonnull final Object[] args) {
        return (this.returnValueSupplier == null) ? null : this.returnValueSupplier.get(method, args);
    }
    
    private Logger getLogger() {
        return LoggerFactory.getLogger((Class)this.proxiedObject.getClass());
    }
    
    public interface ReturnValueSupplier
    {
        @Nullable
        Object get(@Nonnull final Method p0, @Nonnull final Object... p1);
    }
}
