// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonStreamContext;

public class JsonWriteContext extends JsonStreamContext
{
    public static final int STATUS_OK_AS_IS = 0;
    public static final int STATUS_OK_AFTER_COMMA = 1;
    public static final int STATUS_OK_AFTER_COLON = 2;
    public static final int STATUS_OK_AFTER_SPACE = 3;
    public static final int STATUS_EXPECT_VALUE = 4;
    public static final int STATUS_EXPECT_NAME = 5;
    protected final JsonWriteContext _parent;
    protected String _currentName;
    protected JsonWriteContext _child;
    
    protected JsonWriteContext(final int type, final JsonWriteContext parent) {
        this._child = null;
        this._type = type;
        this._parent = parent;
        this._index = -1;
    }
    
    public static JsonWriteContext createRootContext() {
        return new JsonWriteContext(0, null);
    }
    
    private final JsonWriteContext reset(final int type) {
        this._type = type;
        this._index = -1;
        this._currentName = null;
        return this;
    }
    
    public final JsonWriteContext createChildArrayContext() {
        JsonWriteContext ctxt = this._child;
        if (ctxt == null) {
            ctxt = (this._child = new JsonWriteContext(1, this));
            return ctxt;
        }
        return ctxt.reset(1);
    }
    
    public final JsonWriteContext createChildObjectContext() {
        JsonWriteContext ctxt = this._child;
        if (ctxt == null) {
            ctxt = (this._child = new JsonWriteContext(2, this));
            return ctxt;
        }
        return ctxt.reset(2);
    }
    
    @Override
    public final JsonWriteContext getParent() {
        return this._parent;
    }
    
    @Override
    public final String getCurrentName() {
        return this._currentName;
    }
    
    public final int writeFieldName(final String name) {
        if (this._type != 2) {
            return 4;
        }
        if (this._currentName != null) {
            return 4;
        }
        this._currentName = name;
        return (this._index >= 0) ? 1 : 0;
    }
    
    public final int writeValue() {
        if (this._type == 2) {
            if (this._currentName == null) {
                return 5;
            }
            this._currentName = null;
            ++this._index;
            return 2;
        }
        else {
            if (this._type == 1) {
                final int ix = this._index;
                ++this._index;
                return (ix >= 0) ? 1 : 0;
            }
            ++this._index;
            return (this._index == 0) ? 0 : 3;
        }
    }
    
    protected final void appendDesc(final StringBuilder sb) {
        if (this._type == 2) {
            sb.append('{');
            if (this._currentName != null) {
                sb.append('\"');
                sb.append(this._currentName);
                sb.append('\"');
            }
            else {
                sb.append('?');
            }
            sb.append('}');
        }
        else if (this._type == 1) {
            sb.append('[');
            sb.append(this.getCurrentIndex());
            sb.append(']');
        }
        else {
            sb.append("/");
        }
    }
    
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(64);
        this.appendDesc(sb);
        return sb.toString();
    }
}
