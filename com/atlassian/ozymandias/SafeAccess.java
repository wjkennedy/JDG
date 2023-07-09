// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import java.util.concurrent.Callable;
import com.atlassian.ozymandias.error.ModuleAccessError;
import io.atlassian.fugue.Option;
import javax.annotation.Nullable;
import java.util.List;
import com.atlassian.plugin.ModuleDescriptor;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface SafeAccess
{
     <MT, RT, D extends ModuleDescriptor<MT>> List<RT> descriptors(final Iterable<D> p0, final PluginPointFunction<D, MT, RT> p1);
    
    @Nullable
     <MT, RT, D extends ModuleDescriptor<MT>> RT descriptor(final D p0, final PluginPointFunction<D, MT, RT> p1);
    
     <MT, RT, D extends ModuleDescriptor<MT>> List<RT> modules(final Iterable<MT> p0, final PluginPointFunction<D, MT, RT> p1);
    
    @Nullable
     <MT, RT, D extends ModuleDescriptor<MT>> RT module(final MT p0, final PluginPointFunction<D, MT, RT> p1);
    
     <MT, D extends ModuleDescriptor<MT>> List<Option<? extends ModuleAccessError>> descriptors(final Iterable<D> p0, final PluginPointVisitor<D, MT> p1);
    
     <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> descriptor(final D p0, final PluginPointVisitor<D, MT> p1);
    
     <RT, D extends ModuleDescriptor<?>> List<RT> descriptors(final Iterable<D> p0, final ModuleDescriptorFunction<D, RT> p1);
    
     <D extends ModuleDescriptor<?>> List<Option<? extends ModuleAccessError>> descriptors(final Iterable<D> p0, final ModuleDescriptorVisitor<D> p1);
    
    @Nullable
     <RT, D extends ModuleDescriptor<?>> RT descriptor(final D p0, final ModuleDescriptorFunction<D, RT> p1);
    
     <D extends ModuleDescriptor<?>> Option<? extends ModuleAccessError> descriptor(final D p0, final ModuleDescriptorVisitor<D> p1);
    
     <MT, D extends ModuleDescriptor<MT>> List<Option<? extends ModuleAccessError>> modules(final Iterable<MT> p0, final PluginPointVisitor<D, MT> p1);
    
     <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> module(final MT p0, final PluginPointVisitor<D, MT> p1);
    
     <RT> RT callable(final Callable<RT> p0);
    
    void runnable(final Runnable p0);
}
