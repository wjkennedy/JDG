// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import java.io.Serializable;

public class JsonLocation implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final JsonLocation NA;
    final long _totalBytes;
    final long _totalChars;
    final int _lineNr;
    final int _columnNr;
    final Object _sourceRef;
    
    public JsonLocation(final Object srcRef, final long totalChars, final int lineNr, final int colNr) {
        this(srcRef, -1L, totalChars, lineNr, colNr);
    }
    
    @JsonCreator
    public JsonLocation(@JsonProperty("sourceRef") final Object sourceRef, @JsonProperty("byteOffset") final long totalBytes, @JsonProperty("charOffset") final long totalChars, @JsonProperty("lineNr") final int lineNr, @JsonProperty("columnNr") final int columnNr) {
        this._sourceRef = sourceRef;
        this._totalBytes = totalBytes;
        this._totalChars = totalChars;
        this._lineNr = lineNr;
        this._columnNr = columnNr;
    }
    
    public Object getSourceRef() {
        return this._sourceRef;
    }
    
    public int getLineNr() {
        return this._lineNr;
    }
    
    public int getColumnNr() {
        return this._columnNr;
    }
    
    public long getCharOffset() {
        return this._totalChars;
    }
    
    public long getByteOffset() {
        return this._totalBytes;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(80);
        sb.append("[Source: ");
        if (this._sourceRef == null) {
            sb.append("UNKNOWN");
        }
        else {
            sb.append(this._sourceRef.toString());
        }
        sb.append("; line: ");
        sb.append(this._lineNr);
        sb.append(", column: ");
        sb.append(this._columnNr);
        sb.append(']');
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        int hash = (this._sourceRef == null) ? 1 : this._sourceRef.hashCode();
        hash ^= this._lineNr;
        hash += this._columnNr;
        hash ^= (int)this._totalChars;
        hash += (int)this._totalBytes;
        return hash;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof JsonLocation)) {
            return false;
        }
        final JsonLocation otherLoc = (JsonLocation)other;
        if (this._sourceRef == null) {
            if (otherLoc._sourceRef != null) {
                return false;
            }
        }
        else if (!this._sourceRef.equals(otherLoc._sourceRef)) {
            return false;
        }
        return this._lineNr == otherLoc._lineNr && this._columnNr == otherLoc._columnNr && this._totalChars == otherLoc._totalChars && this.getByteOffset() == otherLoc.getByteOffset();
    }
    
    static {
        NA = new JsonLocation("N/A", -1L, -1L, -1, -1);
    }
}
