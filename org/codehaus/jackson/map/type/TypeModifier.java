// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import java.lang.reflect.Type;
import org.codehaus.jackson.type.JavaType;

public abstract class TypeModifier
{
    public abstract JavaType modifyType(final JavaType p0, final Type p1, final TypeBindings p2, final TypeFactory p3);
}
