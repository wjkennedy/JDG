// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public class MinimalClassNameIdResolver extends ClassNameIdResolver
{
    protected final String _basePackageName;
    protected final String _basePackagePrefix;
    
    protected MinimalClassNameIdResolver(final JavaType baseType, final TypeFactory typeFactory) {
        super(baseType, typeFactory);
        final String base = baseType.getRawClass().getName();
        final int ix = base.lastIndexOf(46);
        if (ix < 0) {
            this._basePackageName = "";
            this._basePackagePrefix = ".";
        }
        else {
            this._basePackagePrefix = base.substring(0, ix + 1);
            this._basePackageName = base.substring(0, ix);
        }
    }
    
    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.MINIMAL_CLASS;
    }
    
    @Override
    public String idFromValue(final Object value) {
        final String n = value.getClass().getName();
        if (n.startsWith(this._basePackagePrefix)) {
            return n.substring(this._basePackagePrefix.length() - 1);
        }
        return n;
    }
    
    @Override
    public JavaType typeFromId(String id) {
        if (id.startsWith(".")) {
            final StringBuilder sb = new StringBuilder(id.length() + this._basePackageName.length());
            if (this._basePackageName.length() == 0) {
                sb.append(id.substring(1));
            }
            else {
                sb.append(this._basePackageName).append(id);
            }
            id = sb.toString();
        }
        return super.typeFromId(id);
    }
}
