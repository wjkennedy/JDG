// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.annotate;

public enum JsonMethod
{
    GETTER, 
    SETTER, 
    CREATOR, 
    FIELD, 
    IS_GETTER, 
    NONE, 
    ALL;
    
    public boolean creatorEnabled() {
        return this == JsonMethod.CREATOR || this == JsonMethod.ALL;
    }
    
    public boolean getterEnabled() {
        return this == JsonMethod.GETTER || this == JsonMethod.ALL;
    }
    
    public boolean isGetterEnabled() {
        return this == JsonMethod.IS_GETTER || this == JsonMethod.ALL;
    }
    
    public boolean setterEnabled() {
        return this == JsonMethod.SETTER || this == JsonMethod.ALL;
    }
    
    public boolean fieldEnabled() {
        return this == JsonMethod.FIELD || this == JsonMethod.ALL;
    }
}
