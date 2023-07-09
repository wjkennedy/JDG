// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.atlassian.ozymandias.error.ThrowableLogger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Predicate;
import java.util.concurrent.Callable;
import com.atlassian.ozymandias.error.ModuleExceptionError;
import javax.annotation.Nullable;
import java.util.Iterator;
import com.google.common.collect.ImmutableList;
import com.atlassian.ozymandias.error.IncorrectModuleTypeError;
import com.atlassian.ozymandias.error.ModuleNotFoundError;
import io.atlassian.fugue.Option;
import com.atlassian.ozymandias.error.ModuleAccessError;
import io.atlassian.fugue.Either;
import java.util.List;
import com.atlassian.plugin.ModuleDescriptor;
import javax.annotation.Nonnull;
import com.atlassian.plugin.PluginAccessor;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SafePluginPointAccess implements SafeAccessViaPluginAccessor, SafeAccess
{
    private final PluginAccessor pluginAccessor;
    
    private SafePluginPointAccess() {
        this.pluginAccessor = null;
    }
    
    private SafePluginPointAccess(@Nonnull final PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }
    
    public static SafeAccessViaPluginAccessor to(@Nonnull final PluginAccessor pluginAccessor) {
        return new SafePluginPointAccess(pluginAccessor);
    }
    
    public static SafeAccess to() {
        return new SafePluginPointAccess();
    }
    
    @Override
    public <MT, RT, D extends ModuleDescriptor<MT>> List<RT> forType(final Class<D> moduleDescriptorClass, final PluginPointFunction<D, MT, RT> callback) {
        final List<D> moduleDescriptors = this.getModuleDescriptors(moduleDescriptorClass);
        return this.descriptors(moduleDescriptors, callback);
    }
    
    @Override
    public <MT, D extends ModuleDescriptor<MT>> void forType(final Class<D> moduleDescriptorClass, final PluginPointVisitor<D, MT> visitor) {
        final List<D> moduleDescriptors = this.getModuleDescriptors(moduleDescriptorClass);
        this.descriptors(moduleDescriptors, visitor);
    }
    
    @Override
    public <RT, D extends ModuleDescriptor<?>> List<RT> forType(final Class<D> moduleDescriptorClass, final ModuleDescriptorFunction<D, RT> callback) {
        final List<D> moduleDescriptors = this.getModuleDescriptors(moduleDescriptorClass);
        return this.descriptors(moduleDescriptors, callback);
    }
    
    @Override
    public <D extends ModuleDescriptor<?>> void forType(final Class<D> moduleDescriptorClass, final ModuleDescriptorVisitor<D> visitor) {
        final List<D> moduleDescriptors = this.getModuleDescriptors(moduleDescriptorClass);
        this.descriptors(moduleDescriptors, visitor);
    }
    
    @Override
    public <MT, RT, D extends ModuleDescriptor<MT>> Either<? extends ModuleAccessError, RT> forKey(final String moduleKey, final Class<D> moduleDescriptorClass, final PluginPointFunction<D, MT, RT> callback) {
        final AccumulatingVisitor<D, MT, RT> visitor = new AccumulatingVisitor<D, MT, RT>((PluginPointFunction)callback);
        final Option<? extends ModuleAccessError> result = this.forKey(moduleKey, moduleDescriptorClass, (PluginPointVisitor<D, Object>)visitor);
        if (result.isDefined()) {
            return (Either<? extends ModuleAccessError, RT>)Either.left(result.get());
        }
        return (Either<? extends ModuleAccessError, RT>)Either.right(((AccumulatingVisitor<ModuleDescriptor, Object, Object>)visitor).getResults().get(0));
    }
    
    @Override
    public <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> forKey(final String moduleKey, final Class<D> moduleDescriptorClass, final PluginPointVisitor<D, MT> visitor) {
        final ModuleDescriptor temp = this.pluginAccessor.getEnabledPluginModule(moduleKey);
        if (temp == null) {
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleNotFoundError());
        }
        if (moduleDescriptorClass.isAssignableFrom(temp.getClass())) {
            final D moduleDescriptor = moduleDescriptorClass.cast(temp);
            return this.descriptor(moduleDescriptor, visitor);
        }
        return (Option<? extends ModuleAccessError>)Option.some((Object)new IncorrectModuleTypeError());
    }
    
    @Override
    public <RT, D extends ModuleDescriptor<?>> Either<? extends ModuleAccessError, RT> forKey(final String moduleKey, final Class<D> moduleDescriptorClass, final ModuleDescriptorFunction<D, RT> callback) {
        final AccumulatingDescriptorVisitor<D, RT> visitor = new AccumulatingDescriptorVisitor<D, RT>((ModuleDescriptorFunction)callback);
        final Option<? extends ModuleAccessError> result = this.forKey(moduleKey, moduleDescriptorClass, visitor);
        if (result.isDefined()) {
            return (Either<? extends ModuleAccessError, RT>)Either.left(result.get());
        }
        return (Either<? extends ModuleAccessError, RT>)Either.right(((AccumulatingDescriptorVisitor<ModuleDescriptor, Object>)visitor).getResults().get(0));
    }
    
    @Override
    public <D extends ModuleDescriptor<?>> Option<? extends ModuleAccessError> forKey(final String moduleKey, final Class<D> moduleDescriptorClass, final ModuleDescriptorVisitor<D> visitor) {
        final ModuleDescriptor temp = this.pluginAccessor.getEnabledPluginModule(moduleKey);
        if (temp == null) {
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleNotFoundError());
        }
        if (moduleDescriptorClass.isAssignableFrom(temp.getClass())) {
            final D moduleDescriptor = moduleDescriptorClass.cast(temp);
            return this.descriptor(moduleDescriptor, visitor);
        }
        return (Option<? extends ModuleAccessError>)Option.some((Object)new IncorrectModuleTypeError());
    }
    
    @Override
    public <MT, RT, D extends ModuleDescriptor<MT>> List<RT> descriptors(final Iterable<D> moduleDescriptors, final PluginPointFunction<D, MT, RT> callback) {
        final AccumulatingVisitor<D, MT, RT> visitor = new AccumulatingVisitor<D, MT, RT>((PluginPointFunction)callback);
        this.descriptors(moduleDescriptors, (PluginPointVisitor<D, Object>)visitor);
        return ((AccumulatingVisitor<ModuleDescriptor, Object, Object>)visitor).getResults();
    }
    
    @Override
    public <MT, RT, D extends ModuleDescriptor<MT>> RT descriptor(final D moduleDescriptor, final PluginPointFunction<D, MT, RT> callback) {
        final AccumulatingVisitor<D, MT, RT> visitor = new AccumulatingVisitor<D, MT, RT>((PluginPointFunction)callback);
        this.visitPluginPointImpl(moduleDescriptor, (PluginPointVisitor<D, Object>)visitor);
        final List<RT> result = ((AccumulatingVisitor<ModuleDescriptor, Object, Object>)visitor).getResults();
        return result.isEmpty() ? null : result.get(0);
    }
    
    @Override
    public <MT, D extends ModuleDescriptor<MT>> List<Option<? extends ModuleAccessError>> descriptors(final Iterable<D> moduleDescriptors, final PluginPointVisitor<D, MT> visitor) {
        final ImmutableList.Builder<Option<? extends ModuleAccessError>> listBuilder = (ImmutableList.Builder<Option<? extends ModuleAccessError>>)ImmutableList.builder();
        for (final D moduleDescriptor : moduleDescriptors) {
            listBuilder.add((Object)this.visitPluginPointImpl(moduleDescriptor, visitor));
        }
        return (List<Option<? extends ModuleAccessError>>)listBuilder.build();
    }
    
    @Override
    public <RT, D extends ModuleDescriptor<?>> List<RT> descriptors(final Iterable<D> moduleDescriptors, final ModuleDescriptorFunction<D, RT> callback) {
        final AccumulatingDescriptorVisitor<D, RT> visitor = new AccumulatingDescriptorVisitor<D, RT>((ModuleDescriptorFunction)callback);
        this.descriptors(moduleDescriptors, visitor);
        return ((AccumulatingDescriptorVisitor<ModuleDescriptor, Object>)visitor).getResults();
    }
    
    @Override
    public <D extends ModuleDescriptor<?>> List<Option<? extends ModuleAccessError>> descriptors(final Iterable<D> moduleDescriptors, final ModuleDescriptorVisitor<D> visitor) {
        final ImmutableList.Builder<Option<? extends ModuleAccessError>> listBuilder = (ImmutableList.Builder<Option<? extends ModuleAccessError>>)ImmutableList.builder();
        for (final D moduleDescriptor : moduleDescriptors) {
            listBuilder.add((Object)this.visitDescriptorImpl(moduleDescriptor, visitor));
        }
        return (List<Option<? extends ModuleAccessError>>)listBuilder.build();
    }
    
    @Override
    public <D extends ModuleDescriptor<?>> Option<? extends ModuleAccessError> descriptor(final D moduleDescriptor, final ModuleDescriptorVisitor<D> visitor) {
        return this.visitDescriptorImpl(moduleDescriptor, (ModuleDescriptorVisitor<ModuleDescriptor>)visitor);
    }
    
    @Override
    public <RT, D extends ModuleDescriptor<?>> RT descriptor(final D moduleDescriptor, final ModuleDescriptorFunction<D, RT> callback) {
        final AccumulatingDescriptorVisitor<D, RT> visitor = new AccumulatingDescriptorVisitor<D, RT>((ModuleDescriptorFunction)callback);
        this.descriptor(moduleDescriptor, visitor);
        final List<RT> result = ((AccumulatingDescriptorVisitor<ModuleDescriptor, Object>)visitor).getResults();
        return result.isEmpty() ? null : result.get(0);
    }
    
    @Override
    public <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> descriptor(final D moduleDescriptor, final PluginPointVisitor<D, MT> visitor) {
        return this.visitPluginPointImpl(moduleDescriptor, (PluginPointVisitor<ModuleDescriptor, Object>)visitor);
    }
    
    @Override
    public <MT, RT, D extends ModuleDescriptor<MT>> List<RT> modules(final Iterable<MT> modules, final PluginPointFunction<D, MT, RT> callback) {
        final AccumulatingVisitor<D, MT, RT> visitor = new AccumulatingVisitor<D, MT, RT>((PluginPointFunction)callback);
        this.modules(modules, (PluginPointVisitor<ModuleDescriptor, MT>)visitor);
        return ((AccumulatingVisitor<ModuleDescriptor, Object, Object>)visitor).getResults();
    }
    
    @Override
    public <MT, RT, D extends ModuleDescriptor<MT>> RT module(final MT module, final PluginPointFunction<D, MT, RT> callback) {
        final AccumulatingVisitor<D, MT, RT> visitor = new AccumulatingVisitor<D, MT, RT>((PluginPointFunction)callback);
        this.visitModulesImpl(module, (PluginPointVisitor<ModuleDescriptor, MT>)visitor);
        final List<RT> result = ((AccumulatingVisitor<ModuleDescriptor, Object, Object>)visitor).getResults();
        return result.isEmpty() ? null : result.get(0);
    }
    
    @Override
    public <MT, D extends ModuleDescriptor<MT>> List<Option<? extends ModuleAccessError>> modules(final Iterable<MT> modules, final PluginPointVisitor<D, MT> visitor) {
        final ImmutableList.Builder<Option<? extends ModuleAccessError>> listBuilder = (ImmutableList.Builder<Option<? extends ModuleAccessError>>)ImmutableList.builder();
        for (final MT module : modules) {
            listBuilder.add((Object)this.visitModulesImpl(module, visitor));
        }
        return (List<Option<? extends ModuleAccessError>>)listBuilder.build();
    }
    
    @Override
    public <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> module(final MT module, final PluginPointVisitor<D, MT> visitor) {
        return this.visitModulesImpl(module, (PluginPointVisitor<ModuleDescriptor, Object>)visitor);
    }
    
    private <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> visitPluginPointImpl(@Nullable final D moduleDescriptor, final PluginPointVisitor<D, MT> visitor) {
        if (moduleDescriptor == null) {
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleNotFoundError());
        }
        MT module;
        try {
            module = (MT)moduleDescriptor.getModule();
        }
        catch (final ClassCastException e) {
            return (Option<? extends ModuleAccessError>)Option.some((Object)new IncorrectModuleTypeError());
        }
        catch (final Throwable t) {
            handleException(t, moduleDescriptor);
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleExceptionError(t));
        }
        return this.invokeModule(visitor, moduleDescriptor, module);
    }
    
    private <D extends ModuleDescriptor<?>> Option<? extends ModuleAccessError> visitDescriptorImpl(@Nullable final D moduleDescriptor, final ModuleDescriptorVisitor<D> visitor) {
        if (moduleDescriptor == null) {
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleNotFoundError());
        }
        try {
            visitor.visit(moduleDescriptor);
        }
        catch (final Throwable t) {
            handleException(t);
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleExceptionError(t));
        }
        return (Option<? extends ModuleAccessError>)Option.none();
    }
    
    private <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> visitModulesImpl(@Nullable final MT module, @Nonnull final PluginPointVisitor<D, MT> visitor) {
        if (module == null) {
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleNotFoundError());
        }
        return this.invokeModule(visitor, (D)null, module);
    }
    
    @Deprecated
    @Nullable
    @Override
    public <RT> RT callable(final Callable<RT> callable) {
        return (RT)call(callable).getOrNull();
    }
    
    public static <RT> Option<RT> call(final Callable<RT> callable) {
        try {
            return (Option<RT>)Option.option((Object)callable.call());
        }
        catch (final Throwable t) {
            handleException(t);
            return (Option<RT>)Option.none();
        }
    }
    
    @Override
    public void runnable(final Runnable runnable) {
        try {
            runnable.run();
        }
        catch (final Throwable t) {
            handleException(t);
        }
    }
    
    public static <T> Predicate<T> safe(final Predicate<T> base) {
        return input -> {
            try {
                return base.test(input);
            }
            catch (final Throwable t) {
                handleException(t);
                return false;
            }
        };
    }
    
    public static <T> Supplier<T> safe(final Supplier<T> base) {
        return (Supplier<T>)(() -> {
            try {
                return base.get();
            }
            catch (final Throwable t) {
                return handleException(t);
            }
        });
    }
    
    public static <F, T> Function<F, T> safe(final Function<F, T> base) {
        return (Function<F, T>)(from -> {
            try {
                return base.apply(from);
            }
            catch (final Throwable t) {
                return handleException(t);
            }
        });
    }
    
    @Nullable
    public static <T> T handleError(final Error e, @Nullable final ModuleDescriptor moduleDescriptor, @Nullable final Object module) throws Error {
        if (e instanceof LinkageError) {
            String msg;
            if (moduleDescriptor == null) {
                msg = String.format("%s Unable to run plugin code because of '%s - %s'.", "A LinkageError indicates that plugin code was compiled with outdated versions.", ThrowableLogger.getClassName(e), e.getMessage());
            }
            else if (module == null) {
                msg = String.format("%s This is for descriptor '%s' of class '%s' because of '%s - %s'.  Continuing...", "A LinkageError indicates that plugin code was compiled with outdated versions.", completeKey(moduleDescriptor), ThrowableLogger.getClassName(moduleDescriptor), ThrowableLogger.getClassName(e), e.getMessage());
            }
            else {
                msg = String.format("%s Unable to access module of type '%s' in descriptor '%s' of class '%s' because of '%s - %s'.  Continuing...", "A LinkageError indicates that plugin code was compiled with outdated versions.", ThrowableLogger.getClassName(module), completeKey(moduleDescriptor), ThrowableLogger.getClassName(moduleDescriptor), ThrowableLogger.getClassName(e), e.getMessage());
            }
            ThrowableLogger.logThrowable(msg, e, getLogger(moduleDescriptor, module));
            return null;
        }
        throw e;
    }
    
    @Nullable
    public static <T> T handleException(final Throwable e, @Nullable final ModuleDescriptor moduleDescriptor, @Nullable final Object module) throws Error {
        if (e instanceof Error) {
            return handleError((Error)e, moduleDescriptor, module);
        }
        String msg;
        if (moduleDescriptor == null) {
            msg = String.format("Unable to run plugin code because of '%s - %s'.", ThrowableLogger.getClassName(e), e.getMessage());
        }
        else if (module == null) {
            msg = String.format("Unable to access module for descriptor '%s' of class '%s' because of '%s - %s'.  Continuing...", completeKey(moduleDescriptor), ThrowableLogger.getClassName(moduleDescriptor), ThrowableLogger.getClassName(e), e.getMessage());
        }
        else {
            msg = String.format("Unable to access module of type '%s' in descriptor '%s' of class '%s' because of '%s - %s'.  Continuing...", ThrowableLogger.getClassName(module), completeKey(moduleDescriptor), ThrowableLogger.getClassName(moduleDescriptor), ThrowableLogger.getClassName(e), e.getMessage());
        }
        ThrowableLogger.logThrowable(msg, e, getLogger(moduleDescriptor, module));
        return null;
    }
    
    @Nullable
    public static <T> T handleError(final Error e) throws Error {
        return handleError(e, null, null);
    }
    
    @Nullable
    public static <T> T handleError(final Error e, @Nullable final ModuleDescriptor moduleDescriptor) throws Error {
        return handleError(e, moduleDescriptor, null);
    }
    
    @Nullable
    public static <T> T handleException(final Throwable e) throws Error {
        return handleException(e, null, null);
    }
    
    @Nullable
    public static <T> T handleException(final Throwable e, final ModuleDescriptor moduleDescriptor) throws Error {
        return handleException(e, moduleDescriptor, null);
    }
    
    private <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> invokeModule(final PluginPointVisitor<D, MT> visitor, final D moduleDescriptor, final MT module) {
        try {
            visitor.visit(moduleDescriptor, module);
            return (Option<? extends ModuleAccessError>)Option.none();
        }
        catch (final Throwable t) {
            handleException(t);
            return (Option<? extends ModuleAccessError>)Option.some((Object)new ModuleExceptionError(t));
        }
    }
    
    private <D extends ModuleDescriptor<?>> List<D> getModuleDescriptors(final Class<D> moduleDescriptorClass) {
        if (this.pluginAccessor == null) {
            throw new IllegalStateException("If you are going to call on the PluginAccessor then you must build this object with one!");
        }
        return this.pluginAccessor.getEnabledModuleDescriptorsByClass((Class)moduleDescriptorClass);
    }
    
    private static <D extends ModuleDescriptor<?>, MT> Logger getLogger(@Nullable final D moduleDescriptor, @Nullable final MT module) {
        Class logClass;
        if (module != null) {
            logClass = module.getClass();
        }
        else if (moduleDescriptor != null) {
            logClass = moduleDescriptor.getClass();
        }
        else {
            logClass = SafePluginPointAccess.class;
        }
        return LoggerFactory.getLogger(logClass);
    }
    
    private static <D extends ModuleDescriptor<?>> String completeKey(@Nullable final D moduleDescriptor) {
        if (moduleDescriptor == null) {
            return "NULL";
        }
        return moduleDescriptor.getCompleteKey();
    }
    
    private static class AccumulatingVisitor<D extends ModuleDescriptor<MT>, MT, RT> implements PluginPointVisitor<D, MT>
    {
        private final List<RT> results;
        private final PluginPointFunction<D, MT, RT> callback;
        
        private AccumulatingVisitor(final PluginPointFunction<D, MT, RT> callback) {
            this.results = Lists.newArrayList();
            this.callback = callback;
        }
        
        @Override
        public void visit(@Nonnull final D moduleDescriptor, @Nullable final MT module) {
            final RT result = this.callback.onModule(moduleDescriptor, module);
            this.results.add(result);
        }
        
        private List<RT> getResults() {
            return this.results;
        }
    }
    
    private static class AccumulatingDescriptorVisitor<D extends ModuleDescriptor<?>, RT> implements ModuleDescriptorVisitor<D>
    {
        private final List<RT> results;
        private final ModuleDescriptorFunction<D, RT> callback;
        
        private AccumulatingDescriptorVisitor(final ModuleDescriptorFunction<D, RT> callback) {
            this.results = Lists.newArrayList();
            this.callback = callback;
        }
        
        @Override
        public void visit(@Nonnull final D moduleDescriptor) {
            final RT result = this.callback.onModuleDescriptor(moduleDescriptor);
            this.results.add(result);
        }
        
        private List<RT> getResults() {
            return this.results;
        }
    }
}
