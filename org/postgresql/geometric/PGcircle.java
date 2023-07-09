// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.geometric;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.PGtokenizer;
import java.sql.SQLException;
import java.io.Serializable;
import org.postgresql.util.PGobject;

public class PGcircle extends PGobject implements Serializable, Cloneable
{
    public PGpoint center;
    public double radius;
    
    public PGcircle(final double x, final double y, final double r) {
        this(new PGpoint(x, y), r);
    }
    
    public PGcircle(final PGpoint c, final double r) {
        this();
        this.center = c;
        this.radius = r;
    }
    
    public PGcircle(final String s) throws SQLException {
        this();
        this.setValue(s);
    }
    
    public PGcircle() {
        this.type = "circle";
    }
    
    @Override
    public void setValue(final String s) throws SQLException {
        if (s == null) {
            this.center = null;
            return;
        }
        final PGtokenizer t = new PGtokenizer(PGtokenizer.removeAngle(s), ',');
        if (t.getSize() != 2) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s), PSQLState.DATA_TYPE_MISMATCH);
        }
        try {
            this.center = new PGpoint(t.getToken(0));
            this.radius = Double.parseDouble(t.getToken(1));
        }
        catch (final NumberFormatException e) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s), PSQLState.DATA_TYPE_MISMATCH, e);
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PGcircle)) {
            return false;
        }
        final PGcircle p = (PGcircle)obj;
        final PGpoint center = this.center;
        final PGpoint pCenter = p.center;
        if (center == null) {
            return pCenter == null;
        }
        return pCenter != null && p.radius == this.radius && PGobject.equals(pCenter, center);
    }
    
    @Override
    public int hashCode() {
        if (this.center == null) {
            return 0;
        }
        final long bits = Double.doubleToLongBits(this.radius);
        int v = (int)(bits ^ bits >>> 32);
        v = v * 31 + this.center.hashCode();
        return v;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        final PGcircle newPGcircle = (PGcircle)super.clone();
        if (newPGcircle.center != null) {
            newPGcircle.center = (PGpoint)newPGcircle.center.clone();
        }
        return newPGcircle;
    }
    
    @Override
    public String getValue() {
        return (this.center == null) ? null : ("<" + this.center + "," + this.radius + ">");
    }
}
