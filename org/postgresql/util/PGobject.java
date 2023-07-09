// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import org.postgresql.util.internal.Nullness;
import java.sql.SQLException;
import java.io.Serializable;

public class PGobject implements Serializable, Cloneable
{
    protected String type;
    protected String value;
    
    public final void setType(final String type) {
        this.type = type;
    }
    
    public void setValue(final String value) throws SQLException {
        this.value = value;
    }
    
    public final String getType() {
        return Nullness.castNonNull(this.type, "PGobject#type is uninitialized. Please call setType(String)");
    }
    
    public String getValue() {
        return this.value;
    }
    
    public boolean isNull() {
        return this.getValue() != null;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PGobject)) {
            return false;
        }
        final Object otherValue = ((PGobject)obj).getValue();
        if (otherValue == null) {
            return this.getValue() == null;
        }
        return otherValue.equals(this.getValue());
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public String toString() {
        return this.getValue();
    }
    
    @Override
    public int hashCode() {
        final String value = this.getValue();
        return (value != null) ? value.hashCode() : 0;
    }
    
    protected static boolean equals(final Object a, final Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
