// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.logging.Level;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

public class PGPropertyMaxResultBufferParser
{
    private static final Logger LOGGER;
    private static final String[] PERCENT_PHRASES;
    
    public static long parseProperty(final String value) throws PSQLException {
        long result = -1L;
        if (value != null) {
            if (checkIfValueContainsPercent(value)) {
                result = parseBytePercentValue(value);
            }
            else if (!value.isEmpty()) {
                result = parseByteValue(value);
            }
        }
        result = adjustResultSize(result);
        return result;
    }
    
    private static boolean checkIfValueContainsPercent(final String value) {
        return getPercentPhraseLengthIfContains(value) != -1;
    }
    
    private static long parseBytePercentValue(final String value) throws PSQLException {
        long result = -1L;
        if (!value.isEmpty()) {
            final int length = getPercentPhraseLengthIfContains(value);
            if (length == -1) {
                throwExceptionAboutParsingError("Received MaxResultBuffer parameter can't be parsed. Value received to parse: {0}", value);
            }
            result = calculatePercentOfMemory(value, length);
        }
        return result;
    }
    
    private static int getPercentPhraseLengthIfContains(final String valueToCheck) {
        int result = -1;
        for (final String phrase : PGPropertyMaxResultBufferParser.PERCENT_PHRASES) {
            final int indx = getPhraseLengthIfContains(valueToCheck, phrase);
            if (indx != -1) {
                result = indx;
            }
        }
        return result;
    }
    
    private static int getPhraseLengthIfContains(final String valueToCheck, final String phrase) {
        final int searchValueLength = phrase.length();
        if (valueToCheck.length() > searchValueLength) {
            final String subValue = valueToCheck.substring(valueToCheck.length() - searchValueLength);
            if (subValue.equals(phrase)) {
                return searchValueLength;
            }
        }
        return -1;
    }
    
    private static long calculatePercentOfMemory(final String value, final int percentPhraseLength) {
        final String realValue = value.substring(0, value.length() - percentPhraseLength);
        final double percent = Double.parseDouble(realValue) / 100.0;
        final long result = (long)(percent * ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        return result;
    }
    
    private static long parseByteValue(final String value) throws PSQLException {
        long result = -1L;
        long multiplier = 1L;
        final long mul = 1000L;
        final char sign = value.charAt(value.length() - 1);
        switch (sign) {
            case 'T':
            case 't': {
                multiplier *= mul;
            }
            case 'G':
            case 'g': {
                multiplier *= mul;
            }
            case 'M':
            case 'm': {
                multiplier *= mul;
            }
            case 'K':
            case 'k': {
                multiplier *= mul;
                final String realValue = value.substring(0, value.length() - 1);
                result = Integer.parseInt(realValue) * multiplier;
                break;
            }
            case '%': {
                return result;
            }
            default: {
                if (sign >= '0' && sign <= '9') {
                    result = Long.parseLong(value);
                    break;
                }
                throwExceptionAboutParsingError("Received MaxResultBuffer parameter can't be parsed. Value received to parse: {0}", value);
                break;
            }
        }
        return result;
    }
    
    private static long adjustResultSize(long value) {
        if (value > 0.9 * ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax()) {
            final long newResult = (long)(0.9 * ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
            PGPropertyMaxResultBufferParser.LOGGER.log(Level.WARNING, GT.tr("WARNING! Required to allocate {0} bytes, which exceeded possible heap memory size. Assigned {1} bytes as limit.", String.valueOf(value), String.valueOf(newResult)));
            value = newResult;
        }
        return value;
    }
    
    private static void throwExceptionAboutParsingError(final String message, final Object... values) throws PSQLException {
        throw new PSQLException(GT.tr(message, values), PSQLState.SYNTAX_ERROR);
    }
    
    static {
        LOGGER = Logger.getLogger(PGPropertyMaxResultBufferParser.class.getName());
        PERCENT_PHRASES = new String[] { "p", "pct", "percent" };
    }
}
