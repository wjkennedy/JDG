// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import io.atlassian.fugue.Option;
import com.atlassian.ozymandias.error.ModuleAccessError;
import io.atlassian.fugue.Either;
import java.util.List;
import com.atlassian.plugin.ModuleDescriptor;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface SafeAccessViaPluginAccessor
{
     <MT, RT, D extends ModuleDescriptor<MT>> List<RT> forType(final Class<D> p0, final PluginPointFunction<D, MT, RT> p1);
    
     <MT, D extends ModuleDescriptor<MT>> void forType(final Class<D> p0, final PluginPointVisitor<D, MT> p1);
    
     <MT, RT, D extends ModuleDescriptor<MT>> Either<? extends ModuleAccessError, RT> forKey(final String p0, final Class<D> p1, final PluginPointFunction<D, MT, RT> p2);
    
     <MT, D extends ModuleDescriptor<MT>> Option<? extends ModuleAccessError> forKey(final String p0, final Class<D> p1, final PluginPointVisitor<D, MT> p2);
    
     <RT, D extends ModuleDescriptor<?>> List<RT> forType(final Class<D> p0, final ModuleDescriptorFunction<D, RT> p1);
    
     <D extends ModuleDescriptor<?>> void forType(final Class<D> p0, final ModuleDescriptorVisitor<D> p1);
    
     <RT, D extends ModuleDescriptor<?>> Either<? extends ModuleAccessError, RT> forKey(final String p0, final Class<D> p1, final ModuleDescriptorFunction<D, RT> p2);
    
     <D extends ModuleDescriptor<?>> Option<? extends ModuleAccessError> forKey(final String p0, final Class<D> p1, final ModuleDescriptorVisitor<D> p2);
}
