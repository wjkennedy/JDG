// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.logging.Level;
import java.util.HashMap;
import org.postgresql.core.EncodingPredictor;
import java.util.Map;
import java.util.logging.Logger;
import java.io.Serializable;

public class ServerErrorMessage implements Serializable
{
    private static final Logger LOGGER;
    private static final Character SEVERITY;
    private static final Character MESSAGE;
    private static final Character DETAIL;
    private static final Character HINT;
    private static final Character POSITION;
    private static final Character WHERE;
    private static final Character FILE;
    private static final Character LINE;
    private static final Character ROUTINE;
    private static final Character SQLSTATE;
    private static final Character INTERNAL_POSITION;
    private static final Character INTERNAL_QUERY;
    private static final Character SCHEMA;
    private static final Character TABLE;
    private static final Character COLUMN;
    private static final Character DATATYPE;
    private static final Character CONSTRAINT;
    private final Map<Character, String> mesgParts;
    
    public ServerErrorMessage(final EncodingPredictor.DecodeResult serverError) {
        this(serverError.result);
        if (serverError.encoding != null) {
            this.mesgParts.put(ServerErrorMessage.MESSAGE, this.mesgParts.get(ServerErrorMessage.MESSAGE) + GT.tr(" (pgjdbc: autodetected server-encoding to be {0}, if the message is not readable, please check database logs and/or host, port, dbname, user, password, pg_hba.conf)", serverError.encoding));
        }
    }
    
    public ServerErrorMessage(final String serverError) {
        this.mesgParts = new HashMap<Character, String>();
        final char[] chars = serverError.toCharArray();
        for (int pos = 0, length = chars.length; pos < length; ++pos) {
            final char mesgType = chars[pos];
            if (mesgType != '\0') {
                int startString;
                for (startString = ++pos; pos < length && chars[pos] != '\0'; ++pos) {}
                final String mesgPart = new String(chars, startString, pos - startString);
                this.mesgParts.put(mesgType, mesgPart);
            }
        }
    }
    
    public String getSQLState() {
        return this.mesgParts.get(ServerErrorMessage.SQLSTATE);
    }
    
    public String getMessage() {
        return this.mesgParts.get(ServerErrorMessage.MESSAGE);
    }
    
    public String getSeverity() {
        return this.mesgParts.get(ServerErrorMessage.SEVERITY);
    }
    
    public String getDetail() {
        return this.mesgParts.get(ServerErrorMessage.DETAIL);
    }
    
    public String getHint() {
        return this.mesgParts.get(ServerErrorMessage.HINT);
    }
    
    public int getPosition() {
        return this.getIntegerPart(ServerErrorMessage.POSITION);
    }
    
    public String getWhere() {
        return this.mesgParts.get(ServerErrorMessage.WHERE);
    }
    
    public String getSchema() {
        return this.mesgParts.get(ServerErrorMessage.SCHEMA);
    }
    
    public String getTable() {
        return this.mesgParts.get(ServerErrorMessage.TABLE);
    }
    
    public String getColumn() {
        return this.mesgParts.get(ServerErrorMessage.COLUMN);
    }
    
    public String getDatatype() {
        return this.mesgParts.get(ServerErrorMessage.DATATYPE);
    }
    
    public String getConstraint() {
        return this.mesgParts.get(ServerErrorMessage.CONSTRAINT);
    }
    
    public String getFile() {
        return this.mesgParts.get(ServerErrorMessage.FILE);
    }
    
    public int getLine() {
        return this.getIntegerPart(ServerErrorMessage.LINE);
    }
    
    public String getRoutine() {
        return this.mesgParts.get(ServerErrorMessage.ROUTINE);
    }
    
    public String getInternalQuery() {
        return this.mesgParts.get(ServerErrorMessage.INTERNAL_QUERY);
    }
    
    public int getInternalPosition() {
        return this.getIntegerPart(ServerErrorMessage.INTERNAL_POSITION);
    }
    
    private int getIntegerPart(final Character c) {
        final String s = this.mesgParts.get(c);
        if (s == null) {
            return 0;
        }
        return Integer.parseInt(s);
    }
    
    String getNonSensitiveErrorMessage() {
        final StringBuilder totalMessage = new StringBuilder();
        String message = this.mesgParts.get(ServerErrorMessage.SEVERITY);
        if (message != null) {
            totalMessage.append(message).append(": ");
        }
        message = this.mesgParts.get(ServerErrorMessage.MESSAGE);
        if (message != null) {
            totalMessage.append(message);
        }
        return totalMessage.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder totalMessage = new StringBuilder();
        String message = this.mesgParts.get(ServerErrorMessage.SEVERITY);
        if (message != null) {
            totalMessage.append(message).append(": ");
        }
        message = this.mesgParts.get(ServerErrorMessage.MESSAGE);
        if (message != null) {
            totalMessage.append(message);
        }
        message = this.mesgParts.get(ServerErrorMessage.DETAIL);
        if (message != null) {
            totalMessage.append("\n  ").append(GT.tr("Detail: {0}", message));
        }
        message = this.mesgParts.get(ServerErrorMessage.HINT);
        if (message != null) {
            totalMessage.append("\n  ").append(GT.tr("Hint: {0}", message));
        }
        message = this.mesgParts.get(ServerErrorMessage.POSITION);
        if (message != null) {
            totalMessage.append("\n  ").append(GT.tr("Position: {0}", message));
        }
        message = this.mesgParts.get(ServerErrorMessage.WHERE);
        if (message != null) {
            totalMessage.append("\n  ").append(GT.tr("Where: {0}", message));
        }
        if (ServerErrorMessage.LOGGER.isLoggable(Level.FINEST)) {
            final String internalQuery = this.mesgParts.get(ServerErrorMessage.INTERNAL_QUERY);
            if (internalQuery != null) {
                totalMessage.append("\n  ").append(GT.tr("Internal Query: {0}", internalQuery));
            }
            final String internalPosition = this.mesgParts.get(ServerErrorMessage.INTERNAL_POSITION);
            if (internalPosition != null) {
                totalMessage.append("\n  ").append(GT.tr("Internal Position: {0}", internalPosition));
            }
            final String file = this.mesgParts.get(ServerErrorMessage.FILE);
            final String line = this.mesgParts.get(ServerErrorMessage.LINE);
            final String routine = this.mesgParts.get(ServerErrorMessage.ROUTINE);
            if (file != null || line != null || routine != null) {
                totalMessage.append("\n  ").append(GT.tr("Location: File: {0}, Routine: {1}, Line: {2}", file, routine, line));
            }
            message = this.mesgParts.get(ServerErrorMessage.SQLSTATE);
            if (message != null) {
                totalMessage.append("\n  ").append(GT.tr("Server SQLState: {0}", message));
            }
        }
        return totalMessage.toString();
    }
    
    static {
        LOGGER = Logger.getLogger(ServerErrorMessage.class.getName());
        SEVERITY = 'S';
        MESSAGE = 'M';
        DETAIL = 'D';
        HINT = 'H';
        POSITION = 'P';
        WHERE = 'W';
        FILE = 'F';
        LINE = 'L';
        ROUTINE = 'R';
        SQLSTATE = 'C';
        INTERNAL_POSITION = 'p';
        INTERNAL_QUERY = 'q';
        SCHEMA = 's';
        TABLE = 't';
        COLUMN = 'c';
        DATATYPE = 'd';
        CONSTRAINT = 'n';
    }
}
