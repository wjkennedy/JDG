// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import java.util.logging.Level;
import java.util.logging.Logger;

class BooleanTypeUtil
{
    private static final Logger LOGGER;
    
    private BooleanTypeUtil() {
    }
    
    static boolean castToBoolean(final Object in) throws PSQLException {
        if (BooleanTypeUtil.LOGGER.isLoggable(Level.FINE)) {
            BooleanTypeUtil.LOGGER.log(Level.FINE, "Cast to boolean: \"{0}\"", String.valueOf(in));
        }
        if (in instanceof Boolean) {
            return (boolean)in;
        }
        if (in instanceof String) {
            return fromString((String)in);
        }
        if (in instanceof Character) {
            return fromCharacter((Character)in);
        }
        if (in instanceof Number) {
            return fromNumber((Number)in);
        }
        throw new PSQLException("Cannot cast to boolean", PSQLState.CANNOT_COERCE);
    }
    
    static boolean fromString(final String strval) throws PSQLException {
        final String val = strval.trim();
        if ("1".equals(val) || "true".equalsIgnoreCase(val) || "t".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val) || "y".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val)) {
            return true;
        }
        if ("0".equals(val) || "false".equalsIgnoreCase(val) || "f".equalsIgnoreCase(val) || "no".equalsIgnoreCase(val) || "n".equalsIgnoreCase(val) || "off".equalsIgnoreCase(val)) {
            return false;
        }
        throw cannotCoerceException(strval);
    }
    
    private static boolean fromCharacter(final Character charval) throws PSQLException {
        if ('1' == charval || 't' == charval || 'T' == charval || 'y' == charval || 'Y' == charval) {
            return true;
        }
        if ('0' == charval || 'f' == charval || 'F' == charval || 'n' == charval || 'N' == charval) {
            return false;
        }
        throw cannotCoerceException(charval);
    }
    
    private static boolean fromNumber(final Number numval) throws PSQLException {
        final double value = numval.doubleValue();
        if (value == 1.0) {
            return true;
        }
        if (value == 0.0) {
            return false;
        }
        throw cannotCoerceException(numval);
    }
    
    private static PSQLException cannotCoerceException(final Object value) {
        if (BooleanTypeUtil.LOGGER.isLoggable(Level.FINE)) {
            BooleanTypeUtil.LOGGER.log(Level.FINE, "Cannot cast to boolean: \"{0}\"", String.valueOf(value));
        }
        return new PSQLException(GT.tr("Cannot cast to boolean: \"{0}\"", String.valueOf(value)), PSQLState.CANNOT_COERCE);
    }
    
    static {
        LOGGER = Logger.getLogger(BooleanTypeUtil.class.getName());
    }
}
