// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.postgresql.jdbc.EscapedFunctions2;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.jdbc.EscapeSyntaxCallMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parser
{
    private static final int[] NO_BINDS;
    private static final char[] QUOTE_OR_ALPHABETIC_MARKER;
    private static final char[] QUOTE_OR_ALPHABETIC_MARKER_OR_PARENTHESIS;
    private static final char[] SINGLE_QUOTE;
    
    public static List<NativeQuery> parseJdbcSql(final String query, final boolean standardConformingStrings, final boolean withParameters, final boolean splitStatements, final boolean isBatchedReWriteConfigured, final String... returningColumnNames) throws SQLException {
        if (!withParameters && !splitStatements && returningColumnNames != null && returningColumnNames.length == 0) {
            return Collections.singletonList(new NativeQuery(query, SqlCommand.createStatementTypeInfo(SqlCommandType.BLANK)));
        }
        int fragmentStart = 0;
        int inParen = 0;
        final char[] aChars = query.toCharArray();
        final StringBuilder nativeSql = new StringBuilder(query.length() + 10);
        List<Integer> bindPositions = null;
        List<NativeQuery> nativeQueries = null;
        boolean isCurrentReWriteCompatible = false;
        boolean isValuesFound = false;
        int valuesBraceOpenPosition = -1;
        int valuesBraceClosePosition = -1;
        boolean valuesBraceCloseFound = false;
        boolean isInsertPresent = false;
        boolean isReturningPresent = false;
        boolean isReturningPresentPrev = false;
        SqlCommandType currentCommandType = SqlCommandType.BLANK;
        SqlCommandType prevCommandType = SqlCommandType.BLANK;
        int numberOfStatements = 0;
        boolean whitespaceOnly = true;
        int keyWordCount = 0;
        int keywordStart = -1;
        int keywordEnd = -1;
        for (int i = 0; i < aChars.length; ++i) {
            final char aChar = aChars[i];
            boolean isKeyWordChar = false;
            whitespaceOnly &= (aChar == ';' || Character.isWhitespace(aChar));
            keywordEnd = i;
            switch (aChar) {
                case '\'': {
                    i = parseSingleQuotes(aChars, i, standardConformingStrings);
                    break;
                }
                case '\"': {
                    i = parseDoubleQuotes(aChars, i);
                    break;
                }
                case '-': {
                    i = parseLineComment(aChars, i);
                    break;
                }
                case '/': {
                    i = parseBlockComment(aChars, i);
                    break;
                }
                case '$': {
                    i = parseDollarQuotes(aChars, i);
                    break;
                }
                case ')': {
                    if (--inParen == 0 && isValuesFound && !valuesBraceCloseFound) {
                        valuesBraceClosePosition = nativeSql.length() + i - fragmentStart;
                        break;
                    }
                    break;
                }
                case '?': {
                    nativeSql.append(aChars, fragmentStart, i - fragmentStart);
                    if (i + 1 < aChars.length && aChars[i + 1] == '?') {
                        nativeSql.append('?');
                        ++i;
                    }
                    else if (!withParameters) {
                        nativeSql.append('?');
                    }
                    else {
                        if (bindPositions == null) {
                            bindPositions = new ArrayList<Integer>();
                        }
                        bindPositions.add(nativeSql.length());
                        final int bindIndex = bindPositions.size();
                        nativeSql.append(NativeQuery.bindName(bindIndex));
                    }
                    fragmentStart = i + 1;
                    break;
                }
                case ';': {
                    if (inParen != 0) {
                        break;
                    }
                    if (!whitespaceOnly) {
                        ++numberOfStatements;
                        nativeSql.append(aChars, fragmentStart, i - fragmentStart);
                        whitespaceOnly = true;
                    }
                    fragmentStart = i + 1;
                    if (nativeSql.length() > 0) {
                        if (addReturning(nativeSql, currentCommandType, returningColumnNames, isReturningPresent)) {
                            isReturningPresent = true;
                        }
                        if (splitStatements) {
                            if (nativeQueries == null) {
                                nativeQueries = new ArrayList<NativeQuery>();
                            }
                            if (!isValuesFound || !isCurrentReWriteCompatible || valuesBraceClosePosition == -1 || (bindPositions != null && valuesBraceClosePosition < bindPositions.get(bindPositions.size() - 1))) {
                                valuesBraceOpenPosition = -1;
                                valuesBraceClosePosition = -1;
                            }
                            nativeQueries.add(new NativeQuery(nativeSql.toString(), toIntArray(bindPositions), false, SqlCommand.createStatementTypeInfo(currentCommandType, isBatchedReWriteConfigured, valuesBraceOpenPosition, valuesBraceClosePosition, isReturningPresent, nativeQueries.size())));
                        }
                    }
                    prevCommandType = currentCommandType;
                    isReturningPresentPrev = isReturningPresent;
                    currentCommandType = SqlCommandType.BLANK;
                    isReturningPresent = false;
                    if (splitStatements) {
                        if (bindPositions != null) {
                            bindPositions.clear();
                        }
                        nativeSql.setLength(0);
                        isValuesFound = false;
                        isCurrentReWriteCompatible = false;
                        valuesBraceOpenPosition = -1;
                        valuesBraceClosePosition = -1;
                        valuesBraceCloseFound = false;
                        break;
                    }
                    break;
                }
                default: {
                    if (keywordStart >= 0) {
                        isKeyWordChar = isIdentifierContChar(aChar);
                        break;
                    }
                    isKeyWordChar = isIdentifierStartChar(aChar);
                    if (!isKeyWordChar) {
                        break;
                    }
                    keywordStart = i;
                    if (valuesBraceOpenPosition != -1 && inParen == 0) {
                        valuesBraceCloseFound = true;
                        break;
                    }
                    break;
                }
            }
            if (keywordStart >= 0 && (i == aChars.length - 1 || !isKeyWordChar)) {
                final int wordLength = (isKeyWordChar ? (i + 1) : keywordEnd) - keywordStart;
                if (currentCommandType == SqlCommandType.BLANK) {
                    if (wordLength == 6 && parseUpdateKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.UPDATE;
                    }
                    else if (wordLength == 6 && parseDeleteKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.DELETE;
                    }
                    else if (wordLength == 4 && parseMoveKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.MOVE;
                    }
                    else if (wordLength == 6 && parseSelectKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.SELECT;
                    }
                    else if (wordLength == 4 && parseWithKeyword(aChars, keywordStart)) {
                        currentCommandType = SqlCommandType.WITH;
                    }
                    else if (wordLength == 6 && parseInsertKeyword(aChars, keywordStart)) {
                        if (!isInsertPresent && (nativeQueries == null || nativeQueries.isEmpty())) {
                            isCurrentReWriteCompatible = (keyWordCount == 0);
                            isInsertPresent = true;
                            currentCommandType = SqlCommandType.INSERT;
                        }
                        else {
                            isCurrentReWriteCompatible = false;
                        }
                    }
                }
                else if (currentCommandType == SqlCommandType.WITH && inParen == 0) {
                    final SqlCommandType command = parseWithCommandType(aChars, i, keywordStart, wordLength);
                    if (command != null) {
                        currentCommandType = command;
                    }
                }
                if (inParen == 0) {
                    if (aChar != ')') {
                        if (wordLength == 9 && parseReturningKeyword(aChars, keywordStart)) {
                            isReturningPresent = true;
                        }
                        else if (wordLength == 6 && parseValuesKeyword(aChars, keywordStart)) {
                            isValuesFound = true;
                        }
                    }
                }
                keywordStart = -1;
                ++keyWordCount;
            }
            if (aChar == '(' && ++inParen == 1 && isValuesFound && valuesBraceOpenPosition == -1) {
                valuesBraceOpenPosition = nativeSql.length() + i - fragmentStart;
            }
        }
        if (!isValuesFound || !isCurrentReWriteCompatible || valuesBraceClosePosition == -1 || (bindPositions != null && valuesBraceClosePosition < bindPositions.get(bindPositions.size() - 1))) {
            valuesBraceOpenPosition = -1;
            valuesBraceClosePosition = -1;
        }
        if (fragmentStart < aChars.length && !whitespaceOnly) {
            nativeSql.append(aChars, fragmentStart, aChars.length - fragmentStart);
        }
        else if (numberOfStatements > 1) {
            isReturningPresent = false;
            currentCommandType = SqlCommandType.BLANK;
        }
        else if (numberOfStatements == 1) {
            isReturningPresent = isReturningPresentPrev;
            currentCommandType = prevCommandType;
        }
        if (nativeSql.length() == 0) {
            return (nativeQueries != null) ? nativeQueries : Collections.emptyList();
        }
        if (addReturning(nativeSql, currentCommandType, returningColumnNames, isReturningPresent)) {
            isReturningPresent = true;
        }
        final NativeQuery lastQuery = new NativeQuery(nativeSql.toString(), toIntArray(bindPositions), !splitStatements, SqlCommand.createStatementTypeInfo(currentCommandType, isBatchedReWriteConfigured, valuesBraceOpenPosition, valuesBraceClosePosition, isReturningPresent, (nativeQueries == null) ? 0 : nativeQueries.size()));
        if (nativeQueries == null) {
            return Collections.singletonList(lastQuery);
        }
        if (!whitespaceOnly) {
            nativeQueries.add(lastQuery);
        }
        return nativeQueries;
    }
    
    private static SqlCommandType parseWithCommandType(final char[] aChars, final int i, final int keywordStart, final int wordLength) {
        SqlCommandType command;
        if (wordLength == 6 && parseUpdateKeyword(aChars, keywordStart)) {
            command = SqlCommandType.UPDATE;
        }
        else if (wordLength == 6 && parseDeleteKeyword(aChars, keywordStart)) {
            command = SqlCommandType.DELETE;
        }
        else if (wordLength == 6 && parseInsertKeyword(aChars, keywordStart)) {
            command = SqlCommandType.INSERT;
        }
        else {
            if (wordLength != 6 || !parseSelectKeyword(aChars, keywordStart)) {
                return null;
            }
            command = SqlCommandType.SELECT;
        }
        int nextInd;
        for (nextInd = i; nextInd < aChars.length; ++nextInd) {
            final char nextChar = aChars[nextInd];
            if (nextChar == '-') {
                nextInd = parseLineComment(aChars, nextInd);
            }
            else if (nextChar == '/') {
                nextInd = parseBlockComment(aChars, nextInd);
            }
            else if (!Character.isWhitespace(nextChar)) {
                break;
            }
        }
        if (nextInd + 2 >= aChars.length || !parseAsKeyword(aChars, nextInd) || isIdentifierContChar(aChars[nextInd + 2])) {
            return command;
        }
        return null;
    }
    
    private static boolean addReturning(final StringBuilder nativeSql, final SqlCommandType currentCommandType, final String[] returningColumnNames, final boolean isReturningPresent) throws SQLException {
        if (isReturningPresent || returningColumnNames.length == 0) {
            return false;
        }
        if (currentCommandType != SqlCommandType.INSERT && currentCommandType != SqlCommandType.UPDATE && currentCommandType != SqlCommandType.DELETE && currentCommandType != SqlCommandType.WITH) {
            return false;
        }
        nativeSql.append("\nRETURNING ");
        if (returningColumnNames.length == 1 && returningColumnNames[0].charAt(0) == '*') {
            nativeSql.append('*');
            return true;
        }
        for (int col = 0; col < returningColumnNames.length; ++col) {
            final String columnName = returningColumnNames[col];
            if (col > 0) {
                nativeSql.append(", ");
            }
            Utils.escapeIdentifier(nativeSql, columnName);
        }
        return true;
    }
    
    private static int[] toIntArray(final List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return Parser.NO_BINDS;
        }
        final int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            res[i] = list.get(i);
        }
        return res;
    }
    
    public static int parseSingleQuotes(final char[] query, int offset, boolean standardConformingStrings) {
        if (standardConformingStrings && offset >= 2 && (query[offset - 1] == 'e' || query[offset - 1] == 'E') && charTerminatesIdentifier(query[offset - 2])) {
            standardConformingStrings = false;
        }
        if (standardConformingStrings) {
            while (++offset < query.length) {
                switch (query[offset]) {
                    case '\'': {
                        return offset;
                    }
                    default: {
                        continue;
                    }
                }
            }
        }
        else {
            while (++offset < query.length) {
                switch (query[offset]) {
                    case '\\': {
                        ++offset;
                        continue;
                    }
                    case '\'': {
                        return offset;
                    }
                    default: {
                        continue;
                    }
                }
            }
        }
        return query.length;
    }
    
    public static int parseDoubleQuotes(final char[] query, int offset) {
        while (++offset < query.length && query[offset] != '\"') {}
        return offset;
    }
    
    public static int parseDollarQuotes(final char[] query, int offset) {
        if (offset + 1 < query.length && (offset == 0 || !isIdentifierContChar(query[offset - 1]))) {
            int endIdx = -1;
            if (query[offset + 1] == '$') {
                endIdx = offset + 1;
            }
            else if (isDollarQuoteStartChar(query[offset + 1])) {
                for (int d = offset + 2; d < query.length; ++d) {
                    if (query[d] == '$') {
                        endIdx = d;
                        break;
                    }
                    if (!isDollarQuoteContChar(query[d])) {
                        break;
                    }
                }
            }
            if (endIdx > 0) {
                final int tagIdx = offset;
                final int tagLen = endIdx - offset + 1;
                offset = endIdx;
                ++offset;
                while (offset < query.length) {
                    if (query[offset] == '$' && subArraysEqual(query, tagIdx, offset, tagLen)) {
                        offset += tagLen - 1;
                        break;
                    }
                    ++offset;
                }
            }
        }
        return offset;
    }
    
    public static int parseLineComment(final char[] query, int offset) {
        if (offset + 1 < query.length && query[offset + 1] == '-') {
            while (offset + 1 < query.length) {
                ++offset;
                if (query[offset] == '\r' || query[offset] == '\n') {
                    break;
                }
            }
        }
        return offset;
    }
    
    public static int parseBlockComment(final char[] query, int offset) {
        if (offset + 1 < query.length && query[offset + 1] == '*') {
            int level = 1;
            for (offset += 2; offset < query.length; ++offset) {
                switch (query[offset - 1]) {
                    case '*': {
                        if (query[offset] == '/') {
                            --level;
                            ++offset;
                            break;
                        }
                        break;
                    }
                    case '/': {
                        if (query[offset] == '*') {
                            ++level;
                            ++offset;
                            break;
                        }
                        break;
                    }
                }
                if (level == 0) {
                    --offset;
                    break;
                }
            }
        }
        return offset;
    }
    
    public static boolean parseDeleteKeyword(final char[] query, final int offset) {
        return query.length >= offset + 6 && (query[offset] | ' ') == 0x64 && (query[offset + 1] | ' ') == 0x65 && (query[offset + 2] | ' ') == 0x6C && (query[offset + 3] | ' ') == 0x65 && (query[offset + 4] | ' ') == 0x74 && (query[offset + 5] | ' ') == 0x65;
    }
    
    public static boolean parseInsertKeyword(final char[] query, final int offset) {
        return query.length >= offset + 7 && (query[offset] | ' ') == 0x69 && (query[offset + 1] | ' ') == 0x6E && (query[offset + 2] | ' ') == 0x73 && (query[offset + 3] | ' ') == 0x65 && (query[offset + 4] | ' ') == 0x72 && (query[offset + 5] | ' ') == 0x74;
    }
    
    public static boolean parseMoveKeyword(final char[] query, final int offset) {
        return query.length >= offset + 4 && (query[offset] | ' ') == 0x6D && (query[offset + 1] | ' ') == 0x6F && (query[offset + 2] | ' ') == 0x76 && (query[offset + 3] | ' ') == 0x65;
    }
    
    public static boolean parseReturningKeyword(final char[] query, final int offset) {
        return query.length >= offset + 9 && (query[offset] | ' ') == 0x72 && (query[offset + 1] | ' ') == 0x65 && (query[offset + 2] | ' ') == 0x74 && (query[offset + 3] | ' ') == 0x75 && (query[offset + 4] | ' ') == 0x72 && (query[offset + 5] | ' ') == 0x6E && (query[offset + 6] | ' ') == 0x69 && (query[offset + 7] | ' ') == 0x6E && (query[offset + 8] | ' ') == 0x67;
    }
    
    public static boolean parseSelectKeyword(final char[] query, final int offset) {
        return query.length >= offset + 6 && (query[offset] | ' ') == 0x73 && (query[offset + 1] | ' ') == 0x65 && (query[offset + 2] | ' ') == 0x6C && (query[offset + 3] | ' ') == 0x65 && (query[offset + 4] | ' ') == 0x63 && (query[offset + 5] | ' ') == 0x74;
    }
    
    public static boolean parseUpdateKeyword(final char[] query, final int offset) {
        return query.length >= offset + 6 && (query[offset] | ' ') == 0x75 && (query[offset + 1] | ' ') == 0x70 && (query[offset + 2] | ' ') == 0x64 && (query[offset + 3] | ' ') == 0x61 && (query[offset + 4] | ' ') == 0x74 && (query[offset + 5] | ' ') == 0x65;
    }
    
    public static boolean parseValuesKeyword(final char[] query, final int offset) {
        return query.length >= offset + 6 && (query[offset] | ' ') == 0x76 && (query[offset + 1] | ' ') == 0x61 && (query[offset + 2] | ' ') == 0x6C && (query[offset + 3] | ' ') == 0x75 && (query[offset + 4] | ' ') == 0x65 && (query[offset + 5] | ' ') == 0x73;
    }
    
    public static long parseLong(final String s, int beginIndex, final int endIndex) {
        if (endIndex - beginIndex > 16) {
            return Long.parseLong(s.substring(beginIndex, endIndex));
        }
        long res = digitAt(s, beginIndex);
        ++beginIndex;
        while (beginIndex < endIndex) {
            res = res * 10L + digitAt(s, beginIndex);
            ++beginIndex;
        }
        return res;
    }
    
    public static boolean parseWithKeyword(final char[] query, final int offset) {
        return query.length >= offset + 4 && (query[offset] | ' ') == 0x77 && (query[offset + 1] | ' ') == 0x69 && (query[offset + 2] | ' ') == 0x74 && (query[offset + 3] | ' ') == 0x68;
    }
    
    public static boolean parseAsKeyword(final char[] query, final int offset) {
        return query.length >= offset + 2 && (query[offset] | ' ') == 0x61 && (query[offset + 1] | ' ') == 0x73;
    }
    
    public static boolean isDigitAt(final String s, final int pos) {
        return pos > 0 && pos < s.length() && Character.isDigit(s.charAt(pos));
    }
    
    public static int digitAt(final String s, final int pos) {
        final int c = s.charAt(pos) - '0';
        if (c < 0 || c > 9) {
            throw new NumberFormatException("Input string: \"" + s + "\", position: " + pos);
        }
        return c;
    }
    
    public static boolean isSpace(final char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
    }
    
    public static boolean isArrayWhiteSpace(final char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000b';
    }
    
    public static boolean isOperatorChar(final char c) {
        return ",()[].;:+-*/%^<>=~!@#&|`?".indexOf(c) != -1;
    }
    
    public static boolean isIdentifierStartChar(final char c) {
        return Character.isJavaIdentifierStart(c);
    }
    
    public static boolean isIdentifierContChar(final char c) {
        return Character.isJavaIdentifierPart(c);
    }
    
    public static boolean charTerminatesIdentifier(final char c) {
        return c == '\"' || isSpace(c) || isOperatorChar(c);
    }
    
    public static boolean isDollarQuoteStartChar(final char c) {
        return c != '$' && isIdentifierStartChar(c);
    }
    
    public static boolean isDollarQuoteContChar(final char c) {
        return c != '$' && isIdentifierContChar(c);
    }
    
    private static boolean subArraysEqual(final char[] arr, final int offA, final int offB, final int len) {
        if (offA < 0 || offB < 0 || offA >= arr.length || offB >= arr.length || offA + len > arr.length || offB + len > arr.length) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (arr[offA + i] != arr[offB + i]) {
                return false;
            }
        }
        return true;
    }
    
    public static JdbcCallParseInfo modifyJdbcCall(final String jdbcSql, final boolean stdStrings, final int serverVersion, final int protocolVersion, final EscapeSyntaxCallMode escapeSyntaxCallMode) throws SQLException {
        String sql = jdbcSql;
        boolean isFunction = false;
        boolean outParamBeforeFunc = false;
        final int len = jdbcSql.length();
        int state = 1;
        boolean inQuotes = false;
        boolean inEscape = false;
        int startIndex = -1;
        int endIndex = -1;
        boolean syntaxError = false;
        int i = 0;
        while (i < len && !syntaxError) {
            final char ch = jdbcSql.charAt(i);
            switch (state) {
                case 1: {
                    if (ch == '{') {
                        ++i;
                        ++state;
                        continue;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue;
                    }
                    i = len;
                    continue;
                }
                case 2: {
                    if (ch == '?') {
                        isFunction = (outParamBeforeFunc = true);
                        ++i;
                        ++state;
                        continue;
                    }
                    if (ch == 'c' || ch == 'C') {
                        state += 3;
                        continue;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue;
                    }
                    syntaxError = true;
                    continue;
                }
                case 3: {
                    if (ch == '=') {
                        ++i;
                        ++state;
                        continue;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue;
                    }
                    syntaxError = true;
                    continue;
                }
                case 4: {
                    if (ch == 'c' || ch == 'C') {
                        ++state;
                        continue;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue;
                    }
                    syntaxError = true;
                    continue;
                }
                case 5: {
                    if ((ch == 'c' || ch == 'C') && i + 4 <= len && jdbcSql.substring(i, i + 4).equalsIgnoreCase("call")) {
                        isFunction = true;
                        i += 4;
                        ++state;
                        continue;
                    }
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue;
                    }
                    syntaxError = true;
                    continue;
                }
                case 6: {
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        ++state;
                        startIndex = i;
                        continue;
                    }
                    syntaxError = true;
                    continue;
                }
                case 7: {
                    if (ch == '\'') {
                        inQuotes = !inQuotes;
                        ++i;
                        continue;
                    }
                    if (inQuotes && ch == '\\' && !stdStrings) {
                        i += 2;
                        continue;
                    }
                    if (!inQuotes && ch == '{') {
                        inEscape = !inEscape;
                        ++i;
                        continue;
                    }
                    if (!inQuotes && ch == '}') {
                        if (!inEscape) {
                            endIndex = i;
                            ++i;
                            ++state;
                            continue;
                        }
                        inEscape = false;
                        continue;
                    }
                    else {
                        if (!inQuotes && ch == ';') {
                            syntaxError = true;
                            continue;
                        }
                        ++i;
                        continue;
                    }
                    break;
                }
                case 8: {
                    if (Character.isWhitespace(ch)) {
                        ++i;
                        continue;
                    }
                    syntaxError = true;
                    continue;
                }
                default: {
                    throw new IllegalStateException("somehow got into bad state " + state);
                }
            }
        }
        if (i == len && !syntaxError) {
            if (state == 1) {
                for (i = 0; i < len && Character.isWhitespace(jdbcSql.charAt(i)); ++i) {}
                if (i < len - 5) {
                    final char ch = jdbcSql.charAt(i);
                    if ((ch == 'c' || ch == 'C') && jdbcSql.substring(i, i + 4).equalsIgnoreCase("call") && Character.isWhitespace(jdbcSql.charAt(i + 4))) {
                        isFunction = true;
                    }
                }
                return new JdbcCallParseInfo(sql, isFunction);
            }
            if (state != 8) {
                syntaxError = true;
            }
        }
        if (syntaxError) {
            throw new PSQLException(GT.tr("Malformed function or procedure escape syntax at offset {0}.", i), PSQLState.STATEMENT_NOT_ALLOWED_IN_FUNCTION_CALL);
        }
        String prefix;
        String suffix;
        if (escapeSyntaxCallMode == EscapeSyntaxCallMode.SELECT || serverVersion < 110000 || (outParamBeforeFunc && escapeSyntaxCallMode == EscapeSyntaxCallMode.CALL_IF_NO_RETURN)) {
            prefix = "select * from ";
            suffix = " as result";
        }
        else {
            prefix = "call ";
            suffix = "";
        }
        final String s = jdbcSql.substring(startIndex, endIndex);
        final int prefixLength = prefix.length();
        final StringBuilder sb = new StringBuilder(prefixLength + jdbcSql.length() + suffix.length() + 10);
        sb.append(prefix);
        sb.append(s);
        final int opening = s.indexOf(40) + 1;
        if (opening == 0) {
            sb.append(outParamBeforeFunc ? "(?)" : "()");
        }
        else if (outParamBeforeFunc) {
            boolean needComma = false;
            for (int j = opening + prefixLength; j < sb.length(); ++j) {
                final char c = sb.charAt(j);
                if (c == ')') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    needComma = true;
                    break;
                }
            }
            if (needComma) {
                sb.insert(opening + prefixLength, "?,");
            }
            else {
                sb.insert(opening + prefixLength, "?");
            }
        }
        if (!suffix.isEmpty()) {
            sql = sb.append(suffix).toString();
        }
        else {
            sql = sb.toString();
        }
        return new JdbcCallParseInfo(sql, isFunction);
    }
    
    public static String replaceProcessing(final String sql, final boolean replaceProcessingEnabled, final boolean standardConformingStrings) throws SQLException {
        if (replaceProcessingEnabled) {
            final int len = sql.length();
            final char[] chars = sql.toCharArray();
            final StringBuilder newsql = new StringBuilder(len);
            for (int i = 0; i < len; ++i) {
                i = parseSql(chars, i, newsql, false, standardConformingStrings);
                if (i < len) {
                    newsql.append(chars[i]);
                }
            }
            return newsql.toString();
        }
        return sql;
    }
    
    private static int parseSql(final char[] sql, int i, final StringBuilder newsql, final boolean stopOnComma, final boolean stdStrings) throws SQLException {
        SqlParseState state = SqlParseState.IN_SQLCODE;
        final int len = sql.length;
        int nestedParenthesis = 0;
        boolean endOfNested = false;
        --i;
    Label_0501:
        while (!endOfNested && ++i < len) {
            final char c = sql[i];
            switch (state) {
                case IN_SQLCODE: {
                    if (c == '$') {
                        final int i2 = i;
                        i = parseDollarQuotes(sql, i);
                        checkParsePosition(i, len, i2, sql, "Unterminated dollar quote started at position {0} in SQL {1}. Expected terminating $$");
                        newsql.append(sql, i2, i - i2 + 1);
                        continue;
                    }
                    if (c == '\'') {
                        final int i2 = i;
                        i = parseSingleQuotes(sql, i, stdStrings);
                        checkParsePosition(i, len, i2, sql, "Unterminated string literal started at position {0} in SQL {1}. Expected ' char");
                        newsql.append(sql, i2, i - i2 + 1);
                        continue;
                    }
                    if (c == '\"') {
                        final int i2 = i;
                        i = parseDoubleQuotes(sql, i);
                        checkParsePosition(i, len, i2, sql, "Unterminated identifier started at position {0} in SQL {1}. Expected \" char");
                        newsql.append(sql, i2, i - i2 + 1);
                        continue;
                    }
                    if (c == '/') {
                        final int i2 = i;
                        i = parseBlockComment(sql, i);
                        checkParsePosition(i, len, i2, sql, "Unterminated block comment started at position {0} in SQL {1}. Expected */ sequence");
                        newsql.append(sql, i2, i - i2 + 1);
                        continue;
                    }
                    if (c == '-') {
                        final int i2 = i;
                        i = parseLineComment(sql, i);
                        newsql.append(sql, i2, i - i2 + 1);
                        continue;
                    }
                    if (c == '(') {
                        ++nestedParenthesis;
                    }
                    else if (c == ')') {
                        if (--nestedParenthesis < 0) {
                            endOfNested = true;
                            continue;
                        }
                    }
                    else {
                        if (stopOnComma && c == ',' && nestedParenthesis == 0) {
                            endOfNested = true;
                            continue;
                        }
                        if (c == '{' && i + 1 < len) {
                            final SqlParseState[] availableStates = SqlParseState.VALUES;
                            for (int j = 1; j < availableStates.length; ++j) {
                                final SqlParseState availableState = availableStates[j];
                                final int matchedPosition = availableState.getMatchedPosition(sql, i + 1);
                                if (matchedPosition != 0) {
                                    i += matchedPosition;
                                    if (availableState.replacementKeyword != null) {
                                        newsql.append(availableState.replacementKeyword);
                                    }
                                    state = availableState;
                                    continue Label_0501;
                                }
                            }
                        }
                    }
                    newsql.append(c);
                    continue;
                }
                case ESC_FUNCTION: {
                    i = escapeFunction(sql, i, newsql, stdStrings);
                    state = SqlParseState.IN_SQLCODE;
                    continue;
                }
                case ESC_DATE:
                case ESC_TIME:
                case ESC_TIMESTAMP:
                case ESC_OUTERJOIN:
                case ESC_ESCAPECHAR: {
                    if (c == '}') {
                        state = SqlParseState.IN_SQLCODE;
                        continue;
                    }
                    newsql.append(c);
                    continue;
                }
            }
        }
        return i;
    }
    
    private static int findOpenBrace(final char[] sql, final int i) {
        int posArgs;
        for (posArgs = i; posArgs < sql.length && sql[posArgs] != '('; ++posArgs) {}
        return posArgs;
    }
    
    private static void checkParsePosition(final int i, final int len, final int i0, final char[] sql, final String message) throws PSQLException {
        if (i < len) {
            return;
        }
        throw new PSQLException(GT.tr(message, i0, new String(sql)), PSQLState.SYNTAX_ERROR);
    }
    
    private static int escapeFunction(final char[] sql, int i, final StringBuilder newsql, final boolean stdStrings) throws SQLException {
        final int argPos = findOpenBrace(sql, i);
        if (argPos < sql.length) {
            final String functionName = new String(sql, i, argPos - i).trim();
            i = argPos + 1;
            i = escapeFunctionArguments(newsql, functionName, sql, i, stdStrings);
        }
        ++i;
        while (i < sql.length && sql[i] != '}') {
            newsql.append(sql[i++]);
        }
        return i;
    }
    
    private static int escapeFunctionArguments(final StringBuilder newsql, final String functionName, final char[] sql, int i, final boolean stdStrings) throws SQLException {
        final List<CharSequence> parsedArgs = new ArrayList<CharSequence>(3);
        while (true) {
            final StringBuilder arg = new StringBuilder();
            final int lastPos = i;
            i = parseSql(sql, i, arg, true, stdStrings);
            if (i != lastPos) {
                parsedArgs.add(arg);
            }
            if (i >= sql.length || sql[i] != ',') {
                break;
            }
            ++i;
        }
        final Method method = EscapedFunctions2.getFunction(functionName);
        if (method == null) {
            newsql.append(functionName);
            EscapedFunctions2.appendCall(newsql, "(", ",", ")", parsedArgs);
            return i;
        }
        try {
            method.invoke(null, newsql, parsedArgs);
        }
        catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof SQLException) {
                throw (SQLException)targetException;
            }
            final String message = (targetException == null) ? "no message" : targetException.getMessage();
            throw new PSQLException(message, PSQLState.SYSTEM_ERROR);
        }
        catch (final IllegalAccessException e2) {
            throw new PSQLException(e2.getMessage(), PSQLState.SYSTEM_ERROR);
        }
        return i;
    }
    
    static {
        NO_BINDS = new int[0];
        QUOTE_OR_ALPHABETIC_MARKER = new char[] { '\"', '0' };
        QUOTE_OR_ALPHABETIC_MARKER_OR_PARENTHESIS = new char[] { '\"', '0', '(' };
        SINGLE_QUOTE = new char[] { '\'' };
    }
    
    private enum SqlParseState
    {
        IN_SQLCODE, 
        ESC_DATE("d", Parser.SINGLE_QUOTE, "DATE "), 
        ESC_TIME("t", Parser.SINGLE_QUOTE, "TIME "), 
        ESC_TIMESTAMP("ts", Parser.SINGLE_QUOTE, "TIMESTAMP "), 
        ESC_FUNCTION("fn", Parser.QUOTE_OR_ALPHABETIC_MARKER, (String)null), 
        ESC_OUTERJOIN("oj", Parser.QUOTE_OR_ALPHABETIC_MARKER_OR_PARENTHESIS, (String)null), 
        ESC_ESCAPECHAR("escape", Parser.SINGLE_QUOTE, "ESCAPE ");
        
        private static final SqlParseState[] VALUES;
        private final char[] escapeKeyword;
        private final char[] allowedValues;
        private final String replacementKeyword;
        
        private SqlParseState() {
            this("", new char[0], null);
        }
        
        private SqlParseState(final String escapeKeyword, final char[] allowedValues, final String replacementKeyword) {
            this.escapeKeyword = escapeKeyword.toCharArray();
            this.allowedValues = allowedValues;
            this.replacementKeyword = replacementKeyword;
        }
        
        private boolean startMatches(final char[] sql, int pos) {
            for (final char c : this.escapeKeyword) {
                if (pos >= sql.length) {
                    return false;
                }
                final char curr = sql[pos++];
                if (curr != c && curr != Character.toUpperCase(c)) {
                    return false;
                }
            }
            return pos < sql.length;
        }
        
        private int getMatchedPosition(final char[] sql, final int pos) {
            if (!this.startMatches(sql, pos)) {
                return 0;
            }
            int newPos;
            char curr;
            for (newPos = pos + this.escapeKeyword.length, curr = sql[newPos]; curr == ' '; curr = sql[newPos]) {
                if (++newPos >= sql.length) {
                    return 0;
                }
            }
            for (final char c : this.allowedValues) {
                if (curr == c || (c == '0' && Character.isLetter(curr))) {
                    return newPos - pos;
                }
            }
            return 0;
        }
        
        static {
            VALUES = values();
        }
    }
}
