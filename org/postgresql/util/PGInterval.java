// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.Date;
import java.util.Calendar;
import java.text.NumberFormat;
import java.util.Locale;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.sql.SQLException;
import java.io.Serializable;

public class PGInterval extends PGobject implements Serializable, Cloneable
{
    private static final int MICROS_IN_SECOND = 1000000;
    private int years;
    private int months;
    private int days;
    private int hours;
    private int minutes;
    private int wholeSeconds;
    private int microSeconds;
    private boolean isNull;
    
    public PGInterval() {
        this.type = "interval";
    }
    
    public PGInterval(final String value) throws SQLException {
        this();
        this.setValue(value);
    }
    
    private int lookAhead(final String value, final int position, final String find) {
        final char[] tokens = find.toCharArray();
        int found = -1;
        for (int i = 0; i < tokens.length; ++i) {
            found = value.indexOf(tokens[i], position);
            if (found > 0) {
                return found;
            }
        }
        return found;
    }
    
    private void parseISO8601Format(final String value) {
        int number = 0;
        String timeValue = null;
        final int hasTime = value.indexOf(84);
        String dateValue;
        if (hasTime > 0) {
            dateValue = value.substring(1, hasTime);
            timeValue = value.substring(hasTime + 1);
        }
        else {
            dateValue = value.substring(1);
        }
        for (int i = 0; i < dateValue.length(); ++i) {
            final int lookAhead = this.lookAhead(dateValue, i, "YMD");
            if (lookAhead > 0) {
                number = Integer.parseInt(dateValue.substring(i, lookAhead));
                if (dateValue.charAt(lookAhead) == 'Y') {
                    this.setYears(number);
                }
                else if (dateValue.charAt(lookAhead) == 'M') {
                    this.setMonths(number);
                }
                else if (dateValue.charAt(lookAhead) == 'D') {
                    this.setDays(number);
                }
                i = lookAhead;
            }
        }
        if (timeValue != null) {
            for (int i = 0; i < timeValue.length(); ++i) {
                final int lookAhead = this.lookAhead(timeValue, i, "HMS");
                if (lookAhead > 0) {
                    number = Integer.parseInt(timeValue.substring(i, lookAhead));
                    if (timeValue.charAt(lookAhead) == 'H') {
                        this.setHours(number);
                    }
                    else if (timeValue.charAt(lookAhead) == 'M') {
                        this.setMinutes(number);
                    }
                    else if (timeValue.charAt(lookAhead) == 'S') {
                        this.setSeconds(number);
                    }
                    i = lookAhead;
                }
            }
        }
    }
    
    public PGInterval(final int years, final int months, final int days, final int hours, final int minutes, final double seconds) {
        this();
        this.setValue(years, months, days, hours, minutes, seconds);
    }
    
    @Override
    public void setValue(String value) throws SQLException {
        this.isNull = (value == null);
        if (value == null) {
            this.setValue(0, 0, 0, 0, 0, 0.0);
            this.isNull = true;
            return;
        }
        final boolean PostgresFormat = !value.startsWith("@");
        if (value.startsWith("P")) {
            this.parseISO8601Format(value);
            return;
        }
        if (!PostgresFormat && value.length() == 3 && value.charAt(2) == '0') {
            this.setValue(0, 0, 0, 0, 0, 0.0);
            return;
        }
        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        double seconds = 0.0;
        try {
            String valueToken = null;
            value = value.replace('+', ' ').replace('@', ' ');
            final StringTokenizer st = new StringTokenizer(value);
            int i = 1;
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                if ((i & 0x1) == 0x1) {
                    final int endHours = token.indexOf(58);
                    if (endHours == -1) {
                        valueToken = token;
                    }
                    else {
                        final int offset = (token.charAt(0) == '-') ? 1 : 0;
                        hours = nullSafeIntGet(token.substring(offset + 0, endHours));
                        minutes = nullSafeIntGet(token.substring(endHours + 1, endHours + 3));
                        final int endMinutes = token.indexOf(58, endHours + 1);
                        if (endMinutes != -1) {
                            seconds = nullSafeDoubleGet(token.substring(endMinutes + 1));
                        }
                        if (offset == 1) {
                            hours = -hours;
                            minutes = -minutes;
                            seconds = -seconds;
                        }
                        valueToken = null;
                    }
                }
                else if (token.startsWith("year")) {
                    years = nullSafeIntGet(valueToken);
                }
                else if (token.startsWith("mon")) {
                    months = nullSafeIntGet(valueToken);
                }
                else if (token.startsWith("day")) {
                    days = nullSafeIntGet(valueToken);
                }
                else if (token.startsWith("hour")) {
                    hours = nullSafeIntGet(valueToken);
                }
                else if (token.startsWith("min")) {
                    minutes = nullSafeIntGet(valueToken);
                }
                else if (token.startsWith("sec")) {
                    seconds = nullSafeDoubleGet(valueToken);
                }
                ++i;
            }
        }
        catch (final NumberFormatException e) {
            throw new PSQLException(GT.tr("Conversion of interval failed", new Object[0]), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE, e);
        }
        if (!PostgresFormat && value.endsWith("ago")) {
            this.setValue(-years, -months, -days, -hours, -minutes, -seconds);
        }
        else {
            this.setValue(years, months, days, hours, minutes, seconds);
        }
    }
    
    public void setValue(final int years, final int months, final int days, final int hours, final int minutes, final double seconds) {
        this.setYears(years);
        this.setMonths(months);
        this.setDays(days);
        this.setHours(hours);
        this.setMinutes(minutes);
        this.setSeconds(seconds);
    }
    
    @Override
    public String getValue() {
        if (this.isNull) {
            return null;
        }
        final DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(Locale.US);
        df.applyPattern("0.0#####");
        return String.format(Locale.ROOT, "%d years %d mons %d days %d hours %d mins %s secs", this.years, this.months, this.days, this.hours, this.minutes, df.format(this.getSeconds()));
    }
    
    public int getYears() {
        return this.years;
    }
    
    public void setYears(final int years) {
        this.isNull = false;
        this.years = years;
    }
    
    public int getMonths() {
        return this.months;
    }
    
    public void setMonths(final int months) {
        this.isNull = false;
        this.months = months;
    }
    
    public int getDays() {
        return this.days;
    }
    
    public void setDays(final int days) {
        this.isNull = false;
        this.days = days;
    }
    
    public int getHours() {
        return this.hours;
    }
    
    public void setHours(final int hours) {
        this.isNull = false;
        this.hours = hours;
    }
    
    public int getMinutes() {
        return this.minutes;
    }
    
    public void setMinutes(final int minutes) {
        this.isNull = false;
        this.minutes = minutes;
    }
    
    public double getSeconds() {
        return this.wholeSeconds + this.microSeconds / 1000000.0;
    }
    
    public int getWholeSeconds() {
        return this.wholeSeconds;
    }
    
    public int getMicroSeconds() {
        return this.microSeconds;
    }
    
    public void setSeconds(final double seconds) {
        this.isNull = false;
        this.wholeSeconds = (int)seconds;
        this.microSeconds = (int)Math.round((seconds - this.wholeSeconds) * 1000000.0);
    }
    
    public void add(final Calendar cal) {
        if (this.isNull) {
            return;
        }
        final int milliseconds = (this.microSeconds + ((this.microSeconds < 0) ? -500 : 500)) / 1000 + this.wholeSeconds * 1000;
        cal.add(14, milliseconds);
        cal.add(12, this.getMinutes());
        cal.add(10, this.getHours());
        cal.add(5, this.getDays());
        cal.add(2, this.getMonths());
        cal.add(1, this.getYears());
    }
    
    public void add(final Date date) {
        if (this.isNull) {
            return;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        this.add(cal);
        date.setTime(cal.getTime().getTime());
    }
    
    public void add(final PGInterval interval) {
        if (this.isNull || interval.isNull) {
            return;
        }
        interval.setYears(interval.getYears() + this.getYears());
        interval.setMonths(interval.getMonths() + this.getMonths());
        interval.setDays(interval.getDays() + this.getDays());
        interval.setHours(interval.getHours() + this.getHours());
        interval.setMinutes(interval.getMinutes() + this.getMinutes());
        interval.setSeconds(interval.getSeconds() + this.getSeconds());
    }
    
    public void scale(final int factor) {
        if (this.isNull) {
            return;
        }
        this.setYears(factor * this.getYears());
        this.setMonths(factor * this.getMonths());
        this.setDays(factor * this.getDays());
        this.setHours(factor * this.getHours());
        this.setMinutes(factor * this.getMinutes());
        this.setSeconds(factor * this.getSeconds());
    }
    
    private static int nullSafeIntGet(final String value) throws NumberFormatException {
        return (value == null) ? 0 : Integer.parseInt(value);
    }
    
    private static double nullSafeDoubleGet(final String value) throws NumberFormatException {
        return (value == null) ? 0.0 : Double.parseDouble(value);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PGInterval)) {
            return false;
        }
        final PGInterval pgi = (PGInterval)obj;
        if (this.isNull) {
            return pgi.isNull;
        }
        return !pgi.isNull && pgi.years == this.years && pgi.months == this.months && pgi.days == this.days && pgi.hours == this.hours && pgi.minutes == this.minutes && pgi.wholeSeconds == this.wholeSeconds && pgi.microSeconds == this.microSeconds;
    }
    
    @Override
    public int hashCode() {
        if (this.isNull) {
            return 0;
        }
        return (((((((248 + this.microSeconds) * 31 + this.wholeSeconds) * 31 + this.minutes) * 31 + this.hours) * 31 + this.days) * 31 + this.months) * 31 + this.years) * 31;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
