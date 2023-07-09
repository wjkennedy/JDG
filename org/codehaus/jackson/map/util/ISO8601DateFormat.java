// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.util.Date;
import java.text.NumberFormat;
import java.util.Calendar;
import java.text.DateFormat;

public class ISO8601DateFormat extends DateFormat
{
    private static final long serialVersionUID = 1L;
    private static Calendar CALENDAR;
    private static NumberFormat NUMBER_FORMAT;
    
    public ISO8601DateFormat() {
        this.numberFormat = ISO8601DateFormat.NUMBER_FORMAT;
        this.calendar = ISO8601DateFormat.CALENDAR;
    }
    
    @Override
    public StringBuffer format(final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition) {
        final String value = ISO8601Utils.format(date);
        toAppendTo.append(value);
        return toAppendTo;
    }
    
    @Override
    public Date parse(final String source, final ParsePosition pos) {
        pos.setIndex(source.length());
        return ISO8601Utils.parse(source);
    }
    
    @Override
    public Object clone() {
        return this;
    }
    
    static {
        ISO8601DateFormat.CALENDAR = new GregorianCalendar();
        ISO8601DateFormat.NUMBER_FORMAT = new DecimalFormat();
    }
}
