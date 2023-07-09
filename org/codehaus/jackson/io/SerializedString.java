// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

import org.codehaus.jackson.SerializableString;

public class SerializedString implements SerializableString
{
    protected final String _value;
    protected byte[] _quotedUTF8Ref;
    protected byte[] _unquotedUTF8Ref;
    protected char[] _quotedChars;
    
    public SerializedString(final String v) {
        this._value = v;
    }
    
    public final String getValue() {
        return this._value;
    }
    
    public final int charLength() {
        return this._value.length();
    }
    
    public final char[] asQuotedChars() {
        char[] result = this._quotedChars;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsString(this._value);
            this._quotedChars = result;
        }
        return result;
    }
    
    public final byte[] asUnquotedUTF8() {
        byte[] result = this._unquotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().encodeAsUTF8(this._value);
            this._unquotedUTF8Ref = result;
        }
        return result;
    }
    
    public final byte[] asQuotedUTF8() {
        byte[] result = this._quotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsUTF8(this._value);
            this._quotedUTF8Ref = result;
        }
        return result;
    }
    
    @Override
    public final String toString() {
        return this._value;
    }
    
    @Override
    public final int hashCode() {
        return this._value.hashCode();
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        final SerializedString other = (SerializedString)o;
        return this._value.equals(other._value);
    }
}
