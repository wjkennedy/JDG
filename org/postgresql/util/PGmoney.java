// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.sql.SQLException;
import java.io.Serializable;

public class PGmoney extends PGobject implements Serializable, Cloneable
{
    public double val;
    public boolean isNull;
    
    public PGmoney(final double value) {
        this();
        this.val = value;
    }
    
    public PGmoney(final String value) throws SQLException {
        this();
        this.setValue(value);
    }
    
    public PGmoney() {
        this.type = "money";
    }
    
    @Override
    public void setValue(final String s) throws SQLException {
        this.isNull = (s == null);
        if (s == null) {
            return;
        }
        try {
            final boolean negative = s.charAt(0) == '(';
            String s2 = PGtokenizer.removePara(s).substring(1);
            for (int pos = s2.indexOf(44); pos != -1; pos = s2.indexOf(44)) {
                s2 = s2.substring(0, pos) + s2.substring(pos + 1);
            }
            this.val = Double.parseDouble(s2);
            this.val = (negative ? (-this.val) : this.val);
        }
        catch (final NumberFormatException e) {
            throw new PSQLException(GT.tr("Conversion of money failed.", new Object[0]), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE, e);
        }
    }
    
    @Override
    public int hashCode() {
        if (this.isNull) {
            return 0;
        }
        final int prime = 31;
        int result = super.hashCode();
        final long temp = Double.doubleToLongBits(this.val);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PGmoney)) {
            return false;
        }
        final PGmoney p = (PGmoney)obj;
        if (this.isNull) {
            return p.isNull;
        }
        return !p.isNull && this.val == p.val;
    }
    
    @Override
    public String getValue() {
        if (this.isNull) {
            return null;
        }
        if (this.val < 0.0) {
            return "-$" + -this.val;
        }
        return "$" + this.val;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
