// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.exc;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.map.JsonMappingException;

public class UnrecognizedPropertyException extends JsonMappingException
{
    private static final long serialVersionUID = 1L;
    protected final Class<?> _referringClass;
    protected final String _unrecognizedPropertyName;
    
    public UnrecognizedPropertyException(final String msg, final JsonLocation loc, final Class<?> referringClass, final String propName) {
        super(msg, loc);
        this._referringClass = referringClass;
        this._unrecognizedPropertyName = propName;
    }
    
    public static UnrecognizedPropertyException from(final JsonParser jp, final Object fromObjectOrClass, final String propertyName) {
        if (fromObjectOrClass == null) {
            throw new IllegalArgumentException();
        }
        Class<?> ref;
        if (fromObjectOrClass instanceof Class) {
            ref = (Class)fromObjectOrClass;
        }
        else {
            ref = fromObjectOrClass.getClass();
        }
        final String msg = "Unrecognized field \"" + propertyName + "\" (Class " + ref.getName() + "), not marked as ignorable";
        final UnrecognizedPropertyException e = new UnrecognizedPropertyException(msg, jp.getCurrentLocation(), ref, propertyName);
        e.prependPath(fromObjectOrClass, propertyName);
        return e;
    }
    
    public Class<?> getReferringClass() {
        return this._referringClass;
    }
    
    public String getUnrecognizedPropertyName() {
        return this._unrecognizedPropertyName;
    }
}
