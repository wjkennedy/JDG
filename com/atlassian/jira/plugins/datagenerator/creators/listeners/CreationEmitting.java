// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.listeners;

import java.util.function.Function;

public interface CreationEmitting
{
    void register(final Function<Integer, ?> p0);
    
    void emit(final int p0);
}
