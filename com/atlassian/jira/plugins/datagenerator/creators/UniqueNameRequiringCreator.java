// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators;

import java.util.List;

public interface UniqueNameRequiringCreator<T>
{
    T create(final String p0);
    
    List<T> create(final int p0);
}
