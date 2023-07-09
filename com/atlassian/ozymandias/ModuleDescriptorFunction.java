// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import javax.annotation.Nonnull;
import com.atlassian.plugin.ModuleDescriptor;

@FunctionalInterface
public interface ModuleDescriptorFunction<D extends ModuleDescriptor<?>, RT>
{
    @Nonnull
    RT onModuleDescriptor(@Nonnull final D p0);
}
