// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.formatter;

import java.util.IllegalFormatConversionException;
import java.util.MissingFormatArgumentException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import org.checkerframework.checker.formatter.qual.ReturnsFormat;
import java.util.IllegalFormatException;
import org.checkerframework.checker.formatter.qual.ConversionCategory;
import java.util.regex.Pattern;

public class FormatUtil
{
    private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static Pattern fsPattern;
    
    @ReturnsFormat
    public static String asFormat(final String format, final ConversionCategory... cc) throws IllegalFormatException {
        final ConversionCategory[] fcc = formatParameterCategories(format);
        if (fcc.length != cc.length) {
            throw new ExcessiveOrMissingFormatArgumentException(cc.length, fcc.length);
        }
        for (int i = 0; i < cc.length; ++i) {
            if (cc[i] != fcc[i]) {
                throw new IllegalFormatConversionCategoryException(cc[i], fcc[i]);
            }
        }
        return format;
    }
    
    public static void tryFormatSatisfiability(final String format) throws IllegalFormatException {
        final String unused = String.format(format, (Object[])null);
    }
    
    public static ConversionCategory[] formatParameterCategories(final String format) throws IllegalFormatException {
        tryFormatSatisfiability(format);
        int last = -1;
        int lasto = -1;
        int maxindex = -1;
        final Conversion[] cs = parse(format);
        final Map<Integer, ConversionCategory> conv = new HashMap<Integer, ConversionCategory>();
        for (final Conversion c : cs) {
            final int index = c.index();
            switch (index) {
                case -1: {
                    break;
                }
                case 0: {
                    last = ++lasto;
                    break;
                }
                default: {
                    last = index - 1;
                    break;
                }
            }
            maxindex = Math.max(maxindex, last);
            conv.put(last, ConversionCategory.intersect(conv.containsKey(last) ? conv.get(last) : ConversionCategory.UNUSED, c.category()));
        }
        final ConversionCategory[] res = new ConversionCategory[maxindex + 1];
        for (int i = 0; i <= maxindex; ++i) {
            res[i] = (conv.containsKey(i) ? conv.get(i) : ConversionCategory.UNUSED);
        }
        return res;
    }
    
    private static int indexFromFormat(final Matcher m) {
        final String s = m.group(1);
        int index;
        if (s != null) {
            index = Integer.parseInt(s.substring(0, s.length() - 1));
        }
        else if (m.group(2) != null && m.group(2).contains(String.valueOf('<'))) {
            index = -1;
        }
        else {
            index = 0;
        }
        return index;
    }
    
    private static char conversionCharFromFormat(final Matcher m) {
        final String dt = m.group(5);
        if (dt == null) {
            return m.group(6).charAt(0);
        }
        return dt.charAt(0);
    }
    
    private static Conversion[] parse(final String format) {
        final ArrayList<Conversion> cs = new ArrayList<Conversion>();
        final Matcher m = FormatUtil.fsPattern.matcher(format);
        while (m.find()) {
            final char c = conversionCharFromFormat(m);
            switch (c) {
                case '%':
                case 'n': {
                    continue;
                }
                default: {
                    cs.add(new Conversion(c, indexFromFormat(m)));
                    continue;
                }
            }
        }
        return cs.toArray(new Conversion[cs.size()]);
    }
    
    static {
        FormatUtil.fsPattern = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");
    }
    
    private static class Conversion
    {
        private final int index;
        private final ConversionCategory cath;
        
        public Conversion(final char c, final int index) {
            this.index = index;
            this.cath = ConversionCategory.fromConversionChar(c);
        }
        
        int index() {
            return this.index;
        }
        
        ConversionCategory category() {
            return this.cath;
        }
    }
    
    public static class ExcessiveOrMissingFormatArgumentException extends MissingFormatArgumentException
    {
        private static final long serialVersionUID = 17000126L;
        private final int expected;
        private final int found;
        
        public ExcessiveOrMissingFormatArgumentException(final int expected, final int found) {
            super("-");
            this.expected = expected;
            this.found = found;
        }
        
        public int getExpected() {
            return this.expected;
        }
        
        public int getFound() {
            return this.found;
        }
        
        @Override
        public String getMessage() {
            return String.format("Expected %d arguments but found %d.", this.expected, this.found);
        }
    }
    
    public static class IllegalFormatConversionCategoryException extends IllegalFormatConversionException
    {
        private static final long serialVersionUID = 17000126L;
        private final ConversionCategory expected;
        private final ConversionCategory found;
        
        public IllegalFormatConversionCategoryException(final ConversionCategory expected, final ConversionCategory found) {
            super((expected.chars.length() == 0) ? '-' : expected.chars.charAt(0), (found.types == null) ? Object.class : found.types[0]);
            this.expected = expected;
            this.found = found;
        }
        
        public ConversionCategory getExpected() {
            return this.expected;
        }
        
        public ConversionCategory getFound() {
            return this.found;
        }
        
        @Override
        public String getMessage() {
            return String.format("Expected category %s but found %s.", this.expected, this.found);
        }
    }
}
