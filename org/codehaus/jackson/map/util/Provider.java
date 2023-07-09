// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.util.Collection;

public interface Provider<T>
{
    Collection<T> provide();
}
