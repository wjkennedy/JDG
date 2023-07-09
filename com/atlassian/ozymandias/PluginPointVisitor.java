// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import com.atlassian.plugin.ModuleDescriptor;

@FunctionalInterface
public interface PluginPointVisitor<D extends ModuleDescriptor<MT>, MT>
{
    void visit(@Nonnull final D p0, @Nullable final MT p1);
}
