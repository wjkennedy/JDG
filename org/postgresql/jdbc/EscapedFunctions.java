// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.Map;

@Deprecated
public class EscapedFunctions
{
    public static final String ABS = "abs";
    public static final String ACOS = "acos";
    public static final String ASIN = "asin";
    public static final String ATAN = "atan";
    public static final String ATAN2 = "atan2";
    public static final String CEILING = "ceiling";
    public static final String COS = "cos";
    public static final String COT = "cot";
    public static final String DEGREES = "degrees";
    public static final String EXP = "exp";
    public static final String FLOOR = "floor";
    public static final String LOG = "log";
    public static final String LOG10 = "log10";
    public static final String MOD = "mod";
    public static final String PI = "pi";
    public static final String POWER = "power";
    public static final String RADIANS = "radians";
    public static final String ROUND = "round";
    public static final String SIGN = "sign";
    public static final String SIN = "sin";
    public static final String SQRT = "sqrt";
    public static final String TAN = "tan";
    public static final String TRUNCATE = "truncate";
    public static final String ASCII = "ascii";
    public static final String CHAR = "char";
    public static final String CONCAT = "concat";
    public static final String INSERT = "insert";
    public static final String LCASE = "lcase";
    public static final String LEFT = "left";
    public static final String LENGTH = "length";
    public static final String LOCATE = "locate";
    public static final String LTRIM = "ltrim";
    public static final String REPEAT = "repeat";
    public static final String REPLACE = "replace";
    public static final String RIGHT = "right";
    public static final String RTRIM = "rtrim";
    public static final String SPACE = "space";
    public static final String SUBSTRING = "substring";
    public static final String UCASE = "ucase";
    public static final String CURDATE = "curdate";
    public static final String CURTIME = "curtime";
    public static final String DAYNAME = "dayname";
    public static final String DAYOFMONTH = "dayofmonth";
    public static final String DAYOFWEEK = "dayofweek";
    public static final String DAYOFYEAR = "dayofyear";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String MONTH = "month";
    public static final String MONTHNAME = "monthname";
    public static final String NOW = "now";
    public static final String QUARTER = "quarter";
    public static final String SECOND = "second";
    public static final String WEEK = "week";
    public static final String YEAR = "year";
    public static final String TIMESTAMPADD = "timestampadd";
    public static final String TIMESTAMPDIFF = "timestampdiff";
    public static final String SQL_TSI_ROOT = "SQL_TSI_";
    public static final String SQL_TSI_DAY = "DAY";
    public static final String SQL_TSI_FRAC_SECOND = "FRAC_SECOND";
    public static final String SQL_TSI_HOUR = "HOUR";
    public static final String SQL_TSI_MINUTE = "MINUTE";
    public static final String SQL_TSI_MONTH = "MONTH";
    public static final String SQL_TSI_QUARTER = "QUARTER";
    public static final String SQL_TSI_SECOND = "SECOND";
    public static final String SQL_TSI_WEEK = "WEEK";
    public static final String SQL_TSI_YEAR = "YEAR";
    public static final String DATABASE = "database";
    public static final String IFNULL = "ifnull";
    public static final String USER = "user";
    private static Map<String, Method> functionMap;
    
    private static Map<String, Method> createFunctionMap() {
        final Method[] arrayMeths = EscapedFunctions.class.getDeclaredMethods();
        final Map<String, Method> functionMap = new HashMap<String, Method>(arrayMeths.length * 2);
        for (final Method meth : arrayMeths) {
            if (meth.getName().startsWith("sql")) {
                functionMap.put(meth.getName().toLowerCase(Locale.US), meth);
            }
        }
        return functionMap;
    }
    
    public static Method getFunction(final String functionName) {
        return EscapedFunctions.functionMap.get("sql" + functionName.toLowerCase(Locale.US));
    }
    
    public static String sqlceiling(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("ceil(", "ceiling", parsedArgs);
    }
    
    public static String sqllog(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("ln(", "log", parsedArgs);
    }
    
    public static String sqllog10(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("log(", "log10", parsedArgs);
    }
    
    public static String sqlpower(final List<?> parsedArgs) throws SQLException {
        return twoArgumentsFunctionCall("pow(", "power", parsedArgs);
    }
    
    public static String sqltruncate(final List<?> parsedArgs) throws SQLException {
        return twoArgumentsFunctionCall("trunc(", "truncate", parsedArgs);
    }
    
    public static String sqlchar(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("chr(", "char", parsedArgs);
    }
    
    public static String sqlconcat(final List<?> parsedArgs) {
        final StringBuilder buf = new StringBuilder();
        buf.append('(');
        for (int iArg = 0; iArg < parsedArgs.size(); ++iArg) {
            buf.append(parsedArgs.get(iArg));
            if (iArg != parsedArgs.size() - 1) {
                buf.append(" || ");
            }
        }
        return buf.append(')').toString();
    }
    
    public static String sqlinsert(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 4) {
            throw new PSQLException(GT.tr("{0} function takes four and only four argument.", "insert"), PSQLState.SYNTAX_ERROR);
        }
        final StringBuilder buf = new StringBuilder();
        buf.append("overlay(");
        buf.append(parsedArgs.get(0)).append(" placing ").append(parsedArgs.get(3));
        buf.append(" from ").append(parsedArgs.get(1)).append(" for ").append(parsedArgs.get(2));
        return buf.append(')').toString();
    }
    
    public static String sqllcase(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("lower(", "lcase", parsedArgs);
    }
    
    public static String sqlleft(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", "left"), PSQLState.SYNTAX_ERROR);
        }
        final StringBuilder buf = new StringBuilder();
        buf.append("substring(");
        buf.append(parsedArgs.get(0)).append(" for ").append(parsedArgs.get(1));
        return buf.append(')').toString();
    }
    
    public static String sqllength(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "length"), PSQLState.SYNTAX_ERROR);
        }
        final StringBuilder buf = new StringBuilder();
        buf.append("length(trim(trailing from ");
        buf.append(parsedArgs.get(0));
        return buf.append("))").toString();
    }
    
    public static String sqllocate(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() == 2) {
            return "position(" + parsedArgs.get(0) + " in " + parsedArgs.get(1) + ")";
        }
        if (parsedArgs.size() == 3) {
            final String tmp = "position(" + parsedArgs.get(0) + " in substring(" + parsedArgs.get(1) + " from " + parsedArgs.get(2) + "))";
            return "(" + parsedArgs.get(2) + "*sign(" + tmp + ")+" + tmp + ")";
        }
        throw new PSQLException(GT.tr("{0} function takes two or three arguments.", "locate"), PSQLState.SYNTAX_ERROR);
    }
    
    public static String sqlltrim(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("trim(leading from ", "ltrim", parsedArgs);
    }
    
    public static String sqlright(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", "right"), PSQLState.SYNTAX_ERROR);
        }
        final StringBuilder buf = new StringBuilder();
        buf.append("substring(");
        buf.append(parsedArgs.get(0)).append(" from (length(").append(parsedArgs.get(0)).append(")+1-").append(parsedArgs.get(1));
        return buf.append("))").toString();
    }
    
    public static String sqlrtrim(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("trim(trailing from ", "rtrim", parsedArgs);
    }
    
    public static String sqlspace(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("repeat(' ',", "space", parsedArgs);
    }
    
    public static String sqlsubstring(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() == 2) {
            return "substr(" + parsedArgs.get(0) + "," + parsedArgs.get(1) + ")";
        }
        if (parsedArgs.size() == 3) {
            return "substr(" + parsedArgs.get(0) + "," + parsedArgs.get(1) + "," + parsedArgs.get(2) + ")";
        }
        throw new PSQLException(GT.tr("{0} function takes two or three arguments.", "substring"), PSQLState.SYNTAX_ERROR);
    }
    
    public static String sqlucase(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("upper(", "ucase", parsedArgs);
    }
    
    public static String sqlcurdate(final List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", "curdate"), PSQLState.SYNTAX_ERROR);
        }
        return "current_date";
    }
    
    public static String sqlcurtime(final List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", "curtime"), PSQLState.SYNTAX_ERROR);
        }
        return "current_time";
    }
    
    public static String sqldayname(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "dayname"), PSQLState.SYNTAX_ERROR);
        }
        return "to_char(" + parsedArgs.get(0) + ",'Day')";
    }
    
    public static String sqldayofmonth(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(day from ", "dayofmonth", parsedArgs);
    }
    
    public static String sqldayofweek(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "dayofweek"), PSQLState.SYNTAX_ERROR);
        }
        return "extract(dow from " + parsedArgs.get(0) + ")+1";
    }
    
    public static String sqldayofyear(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(doy from ", "dayofyear", parsedArgs);
    }
    
    public static String sqlhour(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(hour from ", "hour", parsedArgs);
    }
    
    public static String sqlminute(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(minute from ", "minute", parsedArgs);
    }
    
    public static String sqlmonth(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(month from ", "month", parsedArgs);
    }
    
    public static String sqlmonthname(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", "monthname"), PSQLState.SYNTAX_ERROR);
        }
        return "to_char(" + parsedArgs.get(0) + ",'Month')";
    }
    
    public static String sqlquarter(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(quarter from ", "quarter", parsedArgs);
    }
    
    public static String sqlsecond(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(second from ", "second", parsedArgs);
    }
    
    public static String sqlweek(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(week from ", "week", parsedArgs);
    }
    
    public static String sqlyear(final List<?> parsedArgs) throws SQLException {
        return singleArgumentFunctionCall("extract(year from ", "year", parsedArgs);
    }
    
    public static String sqltimestampadd(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 3) {
            throw new PSQLException(GT.tr("{0} function takes three and only three arguments.", "timestampadd"), PSQLState.SYNTAX_ERROR);
        }
        final String interval = constantToInterval(parsedArgs.get(0).toString(), parsedArgs.get(1).toString());
        final StringBuilder buf = new StringBuilder();
        buf.append("(").append(interval).append("+");
        buf.append(parsedArgs.get(2)).append(")");
        return buf.toString();
    }
    
    private static String constantToInterval(final String type, final String value) throws SQLException {
        if (!type.startsWith("SQL_TSI_")) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
        }
        final String shortType = type.substring("SQL_TSI_".length());
        if ("DAY".equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' day' as interval)";
        }
        if ("SECOND".equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' second' as interval)";
        }
        if ("HOUR".equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' hour' as interval)";
        }
        if ("MINUTE".equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' minute' as interval)";
        }
        if ("MONTH".equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' month' as interval)";
        }
        if ("QUARTER".equalsIgnoreCase(shortType)) {
            return "CAST((" + value + "::int * 3) || ' month' as interval)";
        }
        if ("WEEK".equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' week' as interval)";
        }
        if ("YEAR".equalsIgnoreCase(shortType)) {
            return "CAST(" + value + " || ' year' as interval)";
        }
        if ("FRAC_SECOND".equalsIgnoreCase(shortType)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", "SQL_TSI_FRAC_SECOND"), PSQLState.SYNTAX_ERROR);
        }
        throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
    }
    
    public static String sqltimestampdiff(final List<?> parsedArgs) throws SQLException {
        if (parsedArgs.size() != 3) {
            throw new PSQLException(GT.tr("{0} function takes three and only three arguments.", "timestampdiff"), PSQLState.SYNTAX_ERROR);
        }
        final String datePart = constantToDatePart(parsedArgs.get(0).toString());
        final StringBuilder buf = new StringBuilder();
        buf.append("extract( ").append(datePart).append(" from (").append(parsedArgs.get(2)).append("-").append(parsedArgs.get(1)).append("))");
        return buf.toString();
    }
    
    private static String constantToDatePart(final String type) throws SQLException {
        if (!type.startsWith("SQL_TSI_")) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
        }
        final String shortType = type.substring("SQL_TSI_".length());
        if ("DAY".equalsIgnoreCase(shortType)) {
            return "day";
        }
        if ("SECOND".equalsIgnoreCase(shortType)) {
            return "second";
        }
        if ("HOUR".equalsIgnoreCase(shortType)) {
            return "hour";
        }
        if ("MINUTE".equalsIgnoreCase(shortType)) {
            return "minute";
        }
        if ("FRAC_SECOND".equalsIgnoreCase(shortType)) {
            throw new PSQLException(GT.tr("Interval {0} not yet implemented", "SQL_TSI_FRAC_SECOND"), PSQLState.SYNTAX_ERROR);
        }
        throw new PSQLException(GT.tr("Interval {0} not yet implemented", type), PSQLState.SYNTAX_ERROR);
    }
    
    public static String sqldatabase(final List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", "database"), PSQLState.SYNTAX_ERROR);
        }
        return "current_database()";
    }
    
    public static String sqlifnull(final List<?> parsedArgs) throws SQLException {
        return twoArgumentsFunctionCall("coalesce(", "ifnull", parsedArgs);
    }
    
    public static String sqluser(final List<?> parsedArgs) throws SQLException {
        if (!parsedArgs.isEmpty()) {
            throw new PSQLException(GT.tr("{0} function doesn''t take any argument.", "user"), PSQLState.SYNTAX_ERROR);
        }
        return "user";
    }
    
    private static String singleArgumentFunctionCall(final String call, final String functionName, final List<?> parsedArgs) throws PSQLException {
        if (parsedArgs.size() != 1) {
            throw new PSQLException(GT.tr("{0} function takes one and only one argument.", functionName), PSQLState.SYNTAX_ERROR);
        }
        final StringBuilder buf = new StringBuilder();
        buf.append(call);
        buf.append(parsedArgs.get(0));
        return buf.append(')').toString();
    }
    
    private static String twoArgumentsFunctionCall(final String call, final String functionName, final List<?> parsedArgs) throws PSQLException {
        if (parsedArgs.size() != 2) {
            throw new PSQLException(GT.tr("{0} function takes two and only two arguments.", functionName), PSQLState.SYNTAX_ERROR);
        }
        final StringBuilder buf = new StringBuilder();
        buf.append(call);
        buf.append(parsedArgs.get(0)).append(',').append(parsedArgs.get(1));
        return buf.append(')').toString();
    }
    
    static {
        EscapedFunctions.functionMap = createFunctionMap();
    }
}
