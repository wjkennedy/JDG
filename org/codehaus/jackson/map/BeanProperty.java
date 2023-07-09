// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.util.Annotations;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import java.lang.annotation.Annotation;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.util.Named;

public interface BeanProperty extends Named
{
    String getName();
    
    JavaType getType();
    
     <A extends Annotation> A getAnnotation(final Class<A> p0);
    
     <A extends Annotation> A getContextAnnotation(final Class<A> p0);
    
    AnnotatedMember getMember();
    
    public static class Std implements BeanProperty
    {
        protected final String _name;
        protected final JavaType _type;
        protected final AnnotatedMember _member;
        protected final Annotations _contextAnnotations;
        
        public Std(final String name, final JavaType type, final Annotations contextAnnotations, final AnnotatedMember member) {
            this._name = name;
            this._type = type;
            this._member = member;
            this._contextAnnotations = contextAnnotations;
        }
        
        public Std withType(final JavaType type) {
            return new Std(this._name, type, this._contextAnnotations, this._member);
        }
        
        public <A extends Annotation> A getAnnotation(final Class<A> acls) {
            return this._member.getAnnotation(acls);
        }
        
        public <A extends Annotation> A getContextAnnotation(final Class<A> acls) {
            return (A)((this._contextAnnotations == null) ? null : this._contextAnnotations.get(acls));
        }
        
        public String getName() {
            return this._name;
        }
        
        public JavaType getType() {
            return this._type;
        }
        
        public AnnotatedMember getMember() {
            return this._member;
        }
    }
}
