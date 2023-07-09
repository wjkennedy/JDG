// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import org.codehaus.jackson.map.util.ClassUtil;
import java.lang.reflect.Member;

public abstract class AnnotatedMember extends Annotated
{
    protected final AnnotationMap _annotations;
    
    protected AnnotatedMember(final AnnotationMap annotations) {
        this._annotations = annotations;
    }
    
    public abstract Class<?> getDeclaringClass();
    
    public abstract Member getMember();
    
    @Override
    protected AnnotationMap getAllAnnotations() {
        return this._annotations;
    }
    
    public final void fixAccess() {
        ClassUtil.checkAndFixAccess(this.getMember());
    }
    
    public abstract void setValue(final Object p0, final Object p1) throws UnsupportedOperationException, IllegalArgumentException;
}
