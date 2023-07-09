// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.listeners;

import java.util.Iterator;
import com.google.common.collect.Lists;
import java.util.function.Function;
import java.util.List;

public class CreationEmitter implements CreationEmitting
{
    private final List<Function<Integer, ?>> listeners;
    
    public CreationEmitter() {
        this.listeners = Lists.newArrayList();
    }
    
    @Override
    public void register(final Function<Integer, ?> lambda) {
        this.listeners.add(lambda);
    }
    
    @Override
    public void emit(final int created) {
        for (final Function<Integer, ?> listener : this.listeners) {
            listener.apply(created);
        }
    }
}
