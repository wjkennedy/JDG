// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.util.Named;

public abstract class BeanPropertyDefinition implements Named
{
    public abstract String getName();
    
    public abstract String getInternalName();
    
    public abstract boolean isExplicitlyIncluded();
    
    public abstract boolean hasGetter();
    
    public abstract boolean hasSetter();
    
    public abstract boolean hasField();
    
    public abstract boolean hasConstructorParameter();
    
    public boolean couldDeserialize() {
        return this.getMutator() != null;
    }
    
    public boolean couldSerialize() {
        return this.getAccessor() != null;
    }
    
    public abstract AnnotatedMethod getGetter();
    
    public abstract AnnotatedMethod getSetter();
    
    public abstract AnnotatedField getField();
    
    public abstract AnnotatedParameter getConstructorParameter();
    
    public abstract AnnotatedMember getAccessor();
    
    public abstract AnnotatedMember getMutator();
}
