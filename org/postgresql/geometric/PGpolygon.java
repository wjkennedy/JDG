// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.geometric;

import org.postgresql.util.PGtokenizer;
import java.sql.SQLException;
import java.io.Serializable;
import org.postgresql.util.PGobject;

public class PGpolygon extends PGobject implements Serializable, Cloneable
{
    public PGpoint[] points;
    
    public PGpolygon(final PGpoint[] points) {
        this();
        this.points = points;
    }
    
    public PGpolygon(final String s) throws SQLException {
        this();
        this.setValue(s);
    }
    
    public PGpolygon() {
        this.type = "polygon";
    }
    
    @Override
    public void setValue(final String s) throws SQLException {
        if (s == null) {
            this.points = null;
            return;
        }
        final PGtokenizer t = new PGtokenizer(PGtokenizer.removePara(s), ',');
        final int npoints = t.getSize();
        PGpoint[] points = this.points;
        if (points == null || points.length != npoints) {
            points = (this.points = new PGpoint[npoints]);
        }
        for (int p = 0; p < npoints; ++p) {
            points[p] = new PGpoint(t.getToken(p));
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PGpolygon)) {
            return false;
        }
        final PGpolygon p = (PGpolygon)obj;
        final PGpoint[] points = this.points;
        final PGpoint[] pPoints = p.points;
        if (points == null) {
            return pPoints == null;
        }
        if (pPoints == null) {
            return false;
        }
        if (pPoints.length != points.length) {
            return false;
        }
        for (int i = 0; i < points.length; ++i) {
            if (!points[i].equals(pPoints[i])) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        final PGpoint[] points = this.points;
        if (points == null) {
            return hash;
        }
        for (int i = 0; i < points.length && i < 5; ++i) {
            hash = hash * 31 + points[i].hashCode();
        }
        return hash;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        final PGpolygon newPGpolygon = (PGpolygon)super.clone();
        if (newPGpolygon.points != null) {
            final PGpoint[] newPoints = newPGpolygon.points.clone();
            newPGpolygon.points = newPoints;
            for (int i = 0; i < newPGpolygon.points.length; ++i) {
                if (newPGpolygon.points[i] != null) {
                    newPoints[i] = (PGpoint)newPGpolygon.points[i].clone();
                }
            }
        }
        return newPGpolygon;
    }
    
    @Override
    public String getValue() {
        final PGpoint[] points = this.points;
        if (points == null) {
            return null;
        }
        final StringBuilder b = new StringBuilder();
        b.append("(");
        for (int p = 0; p < points.length; ++p) {
            if (p > 0) {
                b.append(",");
            }
            b.append(points[p].toString());
        }
        b.append(")");
        return b.toString();
    }
}
