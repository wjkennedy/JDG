// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias;

import javax.annotation.Nonnull;
import com.atlassian.plugin.ModuleDescriptor;

@FunctionalInterface
public interface ModuleDescriptorVisitor<D extends ModuleDescriptor<?>>
{
    void visit(@Nonnull final D p0);
}
