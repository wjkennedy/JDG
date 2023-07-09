// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.i18nformatter;

import java.text.ChoiceFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.i18nformatter.qual.I18nValidFormat;
import org.checkerframework.checker.i18nformatter.qual.I18nChecksFormat;
import java.util.Map;
import java.util.HashMap;
import org.checkerframework.checker.i18nformatter.qual.I18nConversionCategory;
import java.util.IllegalFormatException;
import java.text.MessageFormat;

public class I18nFormatUtil
{
    public static void tryFormatSatisfiability(final String format) throws IllegalFormatException {
        MessageFormat.format(format, (Object[])null);
    }
    
    public static I18nConversionCategory[] formatParameterCategories(final String format) throws IllegalFormatException {
        tryFormatSatisfiability(format);
        final I18nConversion[] cs = MessageFormatParser.parse(format);
        int maxIndex = -1;
        final Map<Integer, I18nConversionCategory> conv = new HashMap<Integer, I18nConversionCategory>();
        for (final I18nConversion c : cs) {
            final int index = c.index;
            conv.put(index, I18nConversionCategory.intersect(c.category, conv.containsKey(index) ? conv.get(index) : I18nConversionCategory.UNUSED));
            maxIndex = Math.max(maxIndex, index);
        }
        final I18nConversionCategory[] res = new I18nConversionCategory[maxIndex + 1];
        for (int i = 0; i <= maxIndex; ++i) {
            res[i] = (conv.containsKey(i) ? conv.get(i) : I18nConversionCategory.UNUSED);
        }
        return res;
    }
    
    @I18nChecksFormat
    public static boolean hasFormat(final String format, final I18nConversionCategory... cc) {
        final I18nConversionCategory[] fcc = formatParameterCategories(format);
        if (fcc.length != cc.length) {
            return false;
        }
        for (int i = 0; i < cc.length; ++i) {
            if (!I18nConversionCategory.isSubsetOf(cc[i], fcc[i])) {
                return false;
            }
        }
        return true;
    }
    
    @I18nValidFormat
    public static boolean isFormat(final String format) {
        try {
            formatParameterCategories(format);
        }
        catch (final Exception e) {
            return false;
        }
        return true;
    }
    
    private static class I18nConversion
    {
        public int index;
        public I18nConversionCategory category;
        
        public I18nConversion(final int index, final I18nConversionCategory category) {
            this.index = index;
            this.category = category;
        }
        
        @Override
        public String toString() {
            return this.category.toString() + "(index: " + this.index + ")";
        }
    }
    
    private static class MessageFormatParser
    {
        public static int maxOffset;
        private static Locale locale;
        private static List<I18nConversionCategory> categories;
        private static List<Integer> argumentIndices;
        private static int numFormat;
        private static final int SEG_RAW = 0;
        private static final int SEG_INDEX = 1;
        private static final int SEG_TYPE = 2;
        private static final int SEG_MODIFIER = 3;
        private static final int TYPE_NULL = 0;
        private static final int TYPE_NUMBER = 1;
        private static final int TYPE_DATE = 2;
        private static final int TYPE_TIME = 3;
        private static final int TYPE_CHOICE = 4;
        private static final String[] TYPE_KEYWORDS;
        private static final int MODIFIER_DEFAULT = 0;
        private static final int MODIFIER_CURRENCY = 1;
        private static final int MODIFIER_PERCENT = 2;
        private static final int MODIFIER_INTEGER = 3;
        private static final String[] NUMBER_MODIFIER_KEYWORDS;
        private static final String[] DATE_TIME_MODIFIER_KEYWORDS;
        
        public static I18nConversion[] parse(final String pattern) {
            MessageFormatParser.categories = new ArrayList<I18nConversionCategory>();
            MessageFormatParser.argumentIndices = new ArrayList<Integer>();
            MessageFormatParser.locale = Locale.getDefault(Locale.Category.FORMAT);
            applyPattern(pattern);
            final I18nConversion[] ret = new I18nConversion[MessageFormatParser.numFormat];
            for (int i = 0; i < MessageFormatParser.numFormat; ++i) {
                ret[i] = new I18nConversion(MessageFormatParser.argumentIndices.get(i), MessageFormatParser.categories.get(i));
            }
            return ret;
        }
        
        private static void applyPattern(final String pattern) {
            final StringBuilder[] segments = new StringBuilder[4];
            segments[0] = new StringBuilder();
            int part = 0;
            MessageFormatParser.numFormat = 0;
            boolean inQuote = false;
            int braceStack = 0;
            MessageFormatParser.maxOffset = -1;
            for (int i = 0; i < pattern.length(); ++i) {
                final char ch = pattern.charAt(i);
                if (part == 0) {
                    if (ch == '\'') {
                        if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
                            segments[part].append(ch);
                            ++i;
                        }
                        else {
                            inQuote = !inQuote;
                        }
                    }
                    else if (ch == '{' && !inQuote) {
                        part = 1;
                        if (segments[1] == null) {
                            segments[1] = new StringBuilder();
                        }
                    }
                    else {
                        segments[part].append(ch);
                    }
                }
                else if (inQuote) {
                    segments[part].append(ch);
                    if (ch == '\'') {
                        inQuote = false;
                    }
                }
                else {
                    switch (ch) {
                        case ',': {
                            if (part >= 3) {
                                segments[part].append(ch);
                                break;
                            }
                            if (segments[++part] == null) {
                                segments[part] = new StringBuilder();
                                break;
                            }
                            break;
                        }
                        case '{': {
                            ++braceStack;
                            segments[part].append(ch);
                            break;
                        }
                        case '}': {
                            if (braceStack == 0) {
                                part = 0;
                                makeFormat(MessageFormatParser.numFormat, segments);
                                ++MessageFormatParser.numFormat;
                                segments[1] = null;
                                segments[3] = (segments[2] = null);
                                break;
                            }
                            --braceStack;
                            segments[part].append(ch);
                            break;
                        }
                        case ' ': {
                            if (part != 2 || segments[2].length() > 0) {
                                segments[part].append(ch);
                                break;
                            }
                            break;
                        }
                        case '\'': {
                            inQuote = true;
                            segments[part].append(ch);
                            break;
                        }
                        default: {
                            segments[part].append(ch);
                            break;
                        }
                    }
                }
            }
            if (braceStack == 0 && part != 0) {
                MessageFormatParser.maxOffset = -1;
                throw new IllegalArgumentException("Unmatched braces in the pattern");
            }
        }
        
        private static void makeFormat(final int offsetNumber, final StringBuilder[] textSegments) {
            final String[] segments = new String[textSegments.length];
            for (int i = 0; i < textSegments.length; ++i) {
                final StringBuilder oneseg = textSegments[i];
                segments[i] = ((oneseg != null) ? oneseg.toString() : "");
            }
            int argumentNumber;
            try {
                argumentNumber = Integer.parseInt(segments[1]);
            }
            catch (final NumberFormatException e) {
                throw new IllegalArgumentException("can't parse argument number: " + segments[1], e);
            }
            if (argumentNumber < 0) {
                throw new IllegalArgumentException("negative argument number: " + argumentNumber);
            }
            final int oldMaxOffset = MessageFormatParser.maxOffset;
            MessageFormatParser.maxOffset = offsetNumber;
            MessageFormatParser.argumentIndices.add(argumentNumber);
            I18nConversionCategory category = null;
            if (segments[2].length() != 0) {
                final int type = findKeyword(segments[2], MessageFormatParser.TYPE_KEYWORDS);
                switch (type) {
                    case 0: {
                        category = I18nConversionCategory.GENERAL;
                        break;
                    }
                    case 1: {
                        switch (findKeyword(segments[3], MessageFormatParser.NUMBER_MODIFIER_KEYWORDS)) {
                            case 0:
                            case 1:
                            case 2:
                            case 3: {
                                break;
                            }
                            default: {
                                try {
                                    new DecimalFormat(segments[3], DecimalFormatSymbols.getInstance(MessageFormatParser.locale));
                                }
                                catch (final IllegalArgumentException e2) {
                                    MessageFormatParser.maxOffset = oldMaxOffset;
                                    throw e2;
                                }
                                break;
                            }
                        }
                        category = I18nConversionCategory.NUMBER;
                        break;
                    }
                    case 2:
                    case 3: {
                        final int mod = findKeyword(segments[3], MessageFormatParser.DATE_TIME_MODIFIER_KEYWORDS);
                        if (mod < 0 || mod >= MessageFormatParser.DATE_TIME_MODIFIER_KEYWORDS.length) {
                            try {
                                new SimpleDateFormat(segments[3], MessageFormatParser.locale);
                            }
                            catch (final IllegalArgumentException e3) {
                                MessageFormatParser.maxOffset = oldMaxOffset;
                                throw e3;
                            }
                        }
                        category = I18nConversionCategory.DATE;
                        break;
                    }
                    case 4: {
                        if (segments[3].length() == 0) {
                            throw new IllegalArgumentException("Choice Pattern requires Subformat Pattern: " + segments[3]);
                        }
                        try {
                            new ChoiceFormat(segments[3]);
                        }
                        catch (final Exception e4) {
                            MessageFormatParser.maxOffset = oldMaxOffset;
                            throw new IllegalArgumentException("Choice Pattern incorrect: " + segments[3], e4);
                        }
                        category = I18nConversionCategory.NUMBER;
                        break;
                    }
                    default: {
                        MessageFormatParser.maxOffset = oldMaxOffset;
                        throw new IllegalArgumentException("unknown format type: " + segments[2]);
                    }
                }
            }
            else {
                category = I18nConversionCategory.GENERAL;
            }
            MessageFormatParser.categories.add(category);
        }
        
        private static final int findKeyword(final String s, final String[] list) {
            for (int i = 0; i < list.length; ++i) {
                if (s.equals(list[i])) {
                    return i;
                }
            }
            final String ls = s.trim().toLowerCase(Locale.ROOT);
            if (ls != s) {
                for (int j = 0; j < list.length; ++j) {
                    if (ls.equals(list[j])) {
                        return j;
                    }
                }
            }
            return -1;
        }
        
        static {
            TYPE_KEYWORDS = new String[] { "", "number", "date", "time", "choice" };
            NUMBER_MODIFIER_KEYWORDS = new String[] { "", "currency", "percent", "integer" };
            DATE_TIME_MODIFIER_KEYWORDS = new String[] { "", "short", "medium", "long", "full" };
        }
    }
}
