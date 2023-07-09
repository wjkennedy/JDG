// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Method;

public interface MethodFilter
{
    boolean includeMethod(final Method p0);
}
