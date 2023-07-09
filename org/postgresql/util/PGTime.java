// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.Calendar;
import java.sql.Time;

public class PGTime extends Time
{
    private static final long serialVersionUID = 3592492258676494276L;
    private Calendar calendar;
    
    public PGTime(final long time) {
        this(time, null);
    }
    
    public PGTime(final long time, final Calendar calendar) {
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
        final PGTime pgTime = (PGTime)o;
        return (this.calendar != null) ? this.calendar.equals(pgTime.calendar) : (pgTime.calendar == null);
    }
    
    @Override
    public Object clone() {
        final PGTime clone = (PGTime)super.clone();
        final Calendar calendar = this.getCalendar();
        if (calendar != null) {
            clone.setCalendar((Calendar)calendar.clone());
        }
        return clone;
    }
}
