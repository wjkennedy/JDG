// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype;

public final class NamedType
{
    protected final Class<?> _class;
    protected final int _hashCode;
    protected String _name;
    
    public NamedType(final Class<?> c) {
        this(c, null);
    }
    
    public NamedType(final Class<?> c, final String name) {
        this._class = c;
        this._hashCode = c.getName().hashCode();
        this.setName(name);
    }
    
    public Class<?> getType() {
        return this._class;
    }
    
    public String getName() {
        return this._name;
    }
    
    public void setName(final String name) {
        this._name = ((name == null || name.length() == 0) ? null : name);
    }
    
    public boolean hasName() {
        return this._name != null;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == this.getClass() && this._class == ((NamedType)o)._class);
    }
    
    @Override
    public int hashCode() {
        return this._hashCode;
    }
    
    @Override
    public String toString() {
        return "[NamedType, class " + this._class.getName() + ", name: " + ((this._name == null) ? "null" : ("'" + this._name + "'")) + "]";
    }
}
