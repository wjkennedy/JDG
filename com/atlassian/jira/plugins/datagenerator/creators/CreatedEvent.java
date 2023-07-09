// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators;

import java.util.function.Function;

public interface CreatedEvent<T>
{
    T onCreated(final Function<Integer, ?> p0);
}
