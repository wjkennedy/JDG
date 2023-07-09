// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import com.atlassian.plugin.ModuleDescriptor;

@FunctionalInterface
public interface PluginPointFunction<D extends ModuleDescriptor<MT>, MT, RT>
{
    @Nullable
    RT onModule(@Nonnull final D p0, @Nullable final MT p1);
}
