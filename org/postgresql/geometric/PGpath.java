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

public class PGpath extends PGobject implements Serializable, Cloneable
{
    public boolean open;
    public PGpoint[] points;
    
    public PGpath(final PGpoint[] points, final boolean open) {
        this();
        this.points = points;
        this.open = open;
    }
    
    public PGpath() {
        this.type = "path";
    }
    
    public PGpath(final String s) throws SQLException {
        this();
        this.setValue(s);
    }
    
    @Override
    public void setValue(String s) throws SQLException {
        if (s == null) {
            this.points = null;
            return;
        }
        if (s.startsWith("[") && s.endsWith("]")) {
            this.open = true;
            s = PGtokenizer.removeBox(s);
        }
        else {
            if (!s.startsWith("(") || !s.endsWith(")")) {
                throw new PSQLException(GT.tr("Cannot tell if path is open or closed: {0}.", s), PSQLState.DATA_TYPE_MISMATCH);
            }
            this.open = false;
            s = PGtokenizer.removePara(s);
        }
        final PGtokenizer t = new PGtokenizer(s, ',');
        final int npoints = t.getSize();
        final PGpoint[] points = new PGpoint[npoints];
        this.points = points;
        for (int p = 0; p < npoints; ++p) {
            points[p] = new PGpoint(t.getToken(p));
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PGpath)) {
            return false;
        }
        final PGpath p = (PGpath)obj;
        final PGpoint[] points = this.points;
        final PGpoint[] pPoints = p.points;
        if (points == null) {
            return pPoints == null;
        }
        if (pPoints == null) {
            return false;
        }
        if (p.open != this.open) {
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
        final PGpoint[] points = this.points;
        if (points == null) {
            return 0;
        }
        int hash = this.open ? 1231 : 1237;
        for (int i = 0; i < points.length && i < 5; ++i) {
            hash = hash * 31 + points[i].hashCode();
        }
        return hash;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        final PGpath newPGpath = (PGpath)super.clone();
        if (newPGpath.points != null) {
            final PGpoint[] newPoints = newPGpath.points.clone();
            newPGpath.points = newPoints;
            for (int i = 0; i < newPGpath.points.length; ++i) {
                newPoints[i] = (PGpoint)newPGpath.points[i].clone();
            }
        }
        return newPGpath;
    }
    
    @Override
    public String getValue() {
        final PGpoint[] points = this.points;
        if (points == null) {
            return null;
        }
        final StringBuilder b = new StringBuilder(this.open ? "[" : "(");
        for (int p = 0; p < points.length; ++p) {
            if (p > 0) {
                b.append(",");
            }
            b.append(points[p].toString());
        }
        b.append(this.open ? "]" : ")");
        return b.toString();
    }
    
    public boolean isOpen() {
        return this.open && this.points != null;
    }
    
    public boolean isClosed() {
        return !this.open && this.points != null;
    }
    
    public void closePath() {
        this.open = false;
    }
    
    public void openPath() {
        this.open = true;
    }
}
