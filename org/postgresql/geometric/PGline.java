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

public class PGline extends PGobject implements Serializable, Cloneable
{
    public double a;
    public double b;
    public double c;
    private boolean isNull;
    
    public PGline(final double a, final double b, final double c) {
        this();
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    public PGline(final double x1, final double y1, final double x2, final double y2) {
        this();
        this.setValue(x1, y1, x2, y2);
    }
    
    public PGline(final PGpoint p1, final PGpoint p2) {
        this();
        this.setValue(p1, p2);
    }
    
    public PGline(final PGlseg lseg) {
        this();
        if (lseg == null) {
            this.isNull = true;
            return;
        }
        final PGpoint[] point = lseg.point;
        if (point == null) {
            this.isNull = true;
            return;
        }
        this.setValue(point[0], point[1]);
    }
    
    private void setValue(final PGpoint p1, final PGpoint p2) {
        if (p1 == null || p2 == null) {
            this.isNull = true;
        }
        else {
            this.setValue(p1.x, p1.y, p2.x, p2.y);
        }
    }
    
    private void setValue(final double x1, final double y1, final double x2, final double y2) {
        if (x1 == x2) {
            this.a = -1.0;
            this.b = 0.0;
        }
        else {
            this.a = (y2 - y1) / (x2 - x1);
            this.b = -1.0;
        }
        this.c = y1 - this.a * x1;
    }
    
    public PGline(final String s) throws SQLException {
        this();
        this.setValue(s);
    }
    
    public PGline() {
        this.type = "line";
    }
    
    @Override
    public void setValue(final String s) throws SQLException {
        this.isNull = (s == null);
        if (s == null) {
            return;
        }
        if (s.trim().startsWith("{")) {
            final PGtokenizer t = new PGtokenizer(PGtokenizer.removeCurlyBrace(s), ',');
            if (t.getSize() != 3) {
                throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s), PSQLState.DATA_TYPE_MISMATCH);
            }
            this.a = Double.parseDouble(t.getToken(0));
            this.b = Double.parseDouble(t.getToken(1));
            this.c = Double.parseDouble(t.getToken(2));
        }
        else if (s.trim().startsWith("[")) {
            final PGtokenizer t = new PGtokenizer(PGtokenizer.removeBox(s), ',');
            if (t.getSize() != 2) {
                throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s), PSQLState.DATA_TYPE_MISMATCH);
            }
            final PGpoint point1 = new PGpoint(t.getToken(0));
            final PGpoint point2 = new PGpoint(t.getToken(1));
            this.a = point2.x - point1.x;
            this.b = point2.y - point1.y;
            this.c = point1.y;
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final PGline pGline = (PGline)obj;
        if (this.isNull) {
            return pGline.isNull;
        }
        return !pGline.isNull && Double.compare(pGline.a, this.a) == 0 && Double.compare(pGline.b, this.b) == 0 && Double.compare(pGline.c, this.c) == 0;
    }
    
    @Override
    public int hashCode() {
        if (this.isNull) {
            return 0;
        }
        int result = super.hashCode();
        long temp = Double.doubleToLongBits(this.a);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.b);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.c);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }
    
    @Override
    public String getValue() {
        return this.isNull ? null : ("{" + this.a + "," + this.b + "," + this.c + "}");
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
