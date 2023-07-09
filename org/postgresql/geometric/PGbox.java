// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.geometric;

import org.postgresql.util.internal.Nullness;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.util.PGtokenizer;
import java.sql.SQLException;
import java.io.Serializable;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

public class PGbox extends PGobject implements PGBinaryObject, Serializable, Cloneable
{
    public PGpoint[] point;
    
    public PGbox(final double x1, final double y1, final double x2, final double y2) {
        this(new PGpoint(x1, y1), new PGpoint(x2, y2));
    }
    
    public PGbox(final PGpoint p1, final PGpoint p2) {
        this();
        this.point = new PGpoint[] { p1, p2 };
    }
    
    public PGbox(final String s) throws SQLException {
        this();
        this.setValue(s);
    }
    
    public PGbox() {
        this.type = "box";
    }
    
    @Override
    public void setValue(final String value) throws SQLException {
        if (value == null) {
            this.point = null;
            return;
        }
        final PGtokenizer t = new PGtokenizer(value, ',');
        if (t.getSize() != 2) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, value), PSQLState.DATA_TYPE_MISMATCH);
        }
        PGpoint[] point = this.point;
        if (point == null) {
            point = (this.point = new PGpoint[2]);
        }
        point[0] = new PGpoint(t.getToken(0));
        point[1] = new PGpoint(t.getToken(1));
    }
    
    @Override
    public void setByteValue(final byte[] b, final int offset) {
        PGpoint[] point = this.point;
        if (point == null) {
            point = (this.point = new PGpoint[2]);
        }
        (point[0] = new PGpoint()).setByteValue(b, offset);
        (point[1] = new PGpoint()).setByteValue(b, offset + point[0].lengthInBytes());
        this.point = point;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PGbox) {
            final PGbox p = (PGbox)obj;
            final PGpoint[] point = this.point;
            final PGpoint[] pPoint = p.point;
            if (point == null) {
                return pPoint == null;
            }
            if (pPoint == null) {
                return false;
            }
            if (pPoint[0].equals(point[0]) && pPoint[1].equals(point[1])) {
                return true;
            }
            if (pPoint[0].equals(point[1]) && pPoint[1].equals(point[0])) {
                return true;
            }
            if (pPoint[0].x == point[0].x && pPoint[0].y == point[1].y && pPoint[1].x == point[1].x && pPoint[1].y == point[0].y) {
                return true;
            }
            if (pPoint[0].x == point[1].x && pPoint[0].y == point[0].y && pPoint[1].x == point[0].x && pPoint[1].y == point[1].y) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        final PGpoint[] point = this.point;
        return (point == null) ? 0 : (point[0].hashCode() ^ point[1].hashCode());
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        final PGbox newPGbox = (PGbox)super.clone();
        if (newPGbox.point != null) {
            newPGbox.point = newPGbox.point.clone();
            for (int i = 0; i < newPGbox.point.length; ++i) {
                if (newPGbox.point[i] != null) {
                    newPGbox.point[i] = (PGpoint)newPGbox.point[i].clone();
                }
            }
        }
        return newPGbox;
    }
    
    @Override
    public String getValue() {
        final PGpoint[] point = this.point;
        return (point == null) ? null : (point[0].toString() + "," + point[1].toString());
    }
    
    @Override
    public int lengthInBytes() {
        final PGpoint[] point = this.point;
        if (point == null) {
            return 0;
        }
        return point[0].lengthInBytes() + point[1].lengthInBytes();
    }
    
    @Override
    public void toBytes(final byte[] bytes, final int offset) {
        final PGpoint[] point = Nullness.castNonNull(this.point);
        point[0].toBytes(bytes, offset);
        point[1].toBytes(bytes, offset + point[0].lengthInBytes());
    }
}
