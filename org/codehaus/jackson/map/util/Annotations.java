// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.lang.annotation.Annotation;

public interface Annotations
{
     <A extends Annotation> A get(final Class<A> p0);
    
    int size();
}
