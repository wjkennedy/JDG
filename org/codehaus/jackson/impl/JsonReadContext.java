// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonStreamContext;

public final class JsonReadContext extends JsonStreamContext
{
    protected final JsonReadContext _parent;
    protected int _lineNr;
    protected int _columnNr;
    protected String _currentName;
    protected JsonReadContext _child;
    
    public JsonReadContext(final JsonReadContext parent, final int type, final int lineNr, final int colNr) {
        this._child = null;
        this._type = type;
        this._parent = parent;
        this._lineNr = lineNr;
        this._columnNr = colNr;
        this._index = -1;
    }
    
    protected final void reset(final int type, final int lineNr, final int colNr) {
        this._type = type;
        this._index = -1;
        this._lineNr = lineNr;
        this._columnNr = colNr;
        this._currentName = null;
    }
    
    public static JsonReadContext createRootContext(final int lineNr, final int colNr) {
        return new JsonReadContext(null, 0, lineNr, colNr);
    }
    
    public static JsonReadContext createRootContext() {
        return new JsonReadContext(null, 0, 1, 0);
    }
    
    public final JsonReadContext createChildArrayContext(final int lineNr, final int colNr) {
        JsonReadContext ctxt = this._child;
        if (ctxt == null) {
            ctxt = (this._child = new JsonReadContext(this, 1, lineNr, colNr));
            return ctxt;
        }
        ctxt.reset(1, lineNr, colNr);
        return ctxt;
    }
    
    public final JsonReadContext createChildObjectContext(final int lineNr, final int colNr) {
        JsonReadContext ctxt = this._child;
        if (ctxt == null) {
            ctxt = (this._child = new JsonReadContext(this, 2, lineNr, colNr));
            return ctxt;
        }
        ctxt.reset(2, lineNr, colNr);
        return ctxt;
    }
    
    @Override
    public final String getCurrentName() {
        return this._currentName;
    }
    
    @Override
    public final JsonReadContext getParent() {
        return this._parent;
    }
    
    public final JsonLocation getStartLocation(final Object srcRef) {
        final long totalChars = -1L;
        return new JsonLocation(srcRef, totalChars, this._lineNr, this._columnNr);
    }
    
    public final boolean expectComma() {
        final int ix = ++this._index;
        return this._type != 0 && ix > 0;
    }
    
    public void setCurrentName(final String name) {
        this._currentName = name;
    }
    
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(64);
        switch (this._type) {
            case 0: {
                sb.append("/");
                break;
            }
            case 1: {
                sb.append('[');
                sb.append(this.getCurrentIndex());
                sb.append(']');
                break;
            }
            case 2: {
                sb.append('{');
                if (this._currentName != null) {
                    sb.append('\"');
                    CharTypes.appendQuoted(sb, this._currentName);
                    sb.append('\"');
                }
                else {
                    sb.append('?');
                }
                sb.append('}');
                break;
            }
        }
        return sb.toString();
    }
}
