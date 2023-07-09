// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.Calendar;
import java.sql.Timestamp;

public class PGTimestamp extends Timestamp
{
    private static final long serialVersionUID = -6245623465210738466L;
    private Calendar calendar;
    
    public PGTimestamp(final long time) {
        this(time, null);
    }
    
    public PGTimestamp(final long time, final Calendar calendar) {
        super(time);
        this.calendar = calendar;
    }
    
    public void setCalendar(final Calendar calendar) {
        this.calendar = calendar;
    }
    
    public Calendar getCalendar() {
        return this.calendar;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ((this.calendar == null) ? 0 : this.calendar.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final PGTimestamp that = (PGTimestamp)o;
        return (this.calendar != null) ? this.calendar.equals(that.calendar) : (that.calendar == null);
    }
    
    @Override
    public Object clone() {
        final PGTimestamp clone = (PGTimestamp)super.clone();
        final Calendar calendar = this.getCalendar();
        if (calendar != null) {
            clone.setCalendar((Calendar)calendar.clone());
        }
        return clone;
    }
}
