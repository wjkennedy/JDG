// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonLocation;
import java.util.LinkedList;
import org.codehaus.jackson.JsonProcessingException;

public class JsonMappingException extends JsonProcessingException
{
    private static final long serialVersionUID = 1L;
    static final int MAX_REFS_TO_LIST = 1000;
    protected LinkedList<Reference> _path;
    
    public JsonMappingException(final String msg) {
        super(msg);
    }
    
    public JsonMappingException(final String msg, final Throwable rootCause) {
        super(msg, rootCause);
    }
    
    public JsonMappingException(final String msg, final JsonLocation loc) {
        super(msg, loc);
    }
    
    public JsonMappingException(final String msg, final JsonLocation loc, final Throwable rootCause) {
        super(msg, loc, rootCause);
    }
    
    public static JsonMappingException from(final JsonParser jp, final String msg) {
        return new JsonMappingException(msg, jp.getTokenLocation());
    }
    
    public static JsonMappingException from(final JsonParser jp, final String msg, final Throwable problem) {
        return new JsonMappingException(msg, jp.getTokenLocation(), problem);
    }
    
    public static JsonMappingException wrapWithPath(final Throwable src, final Object refFrom, final String refFieldName) {
        return wrapWithPath(src, new Reference(refFrom, refFieldName));
    }
    
    public static JsonMappingException wrapWithPath(final Throwable src, final Object refFrom, final int index) {
        return wrapWithPath(src, new Reference(refFrom, index));
    }
    
    public static JsonMappingException wrapWithPath(final Throwable src, final Reference ref) {
        JsonMappingException jme;
        if (src instanceof JsonMappingException) {
            jme = (JsonMappingException)src;
        }
        else {
            String msg = src.getMessage();
            if (msg == null || msg.length() == 0) {
                msg = "(was " + src.getClass().getName() + ")";
            }
            jme = new JsonMappingException(msg, null, src);
        }
        jme.prependPath(ref);
        return jme;
    }
    
    public List<Reference> getPath() {
        if (this._path == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList((List<? extends Reference>)this._path);
    }
    
    public void prependPath(final Object referrer, final String fieldName) {
        final Reference ref = new Reference(referrer, fieldName);
        this.prependPath(ref);
    }
    
    public void prependPath(final Object referrer, final int index) {
        final Reference ref = new Reference(referrer, index);
        this.prependPath(ref);
    }
    
    public void prependPath(final Reference r) {
        if (this._path == null) {
            this._path = new LinkedList<Reference>();
        }
        if (this._path.size() < 1000) {
            this._path.addFirst(r);
        }
    }
    
    @Override
    public String getMessage() {
        final String msg = super.getMessage();
        if (this._path == null) {
            return msg;
        }
        final StringBuilder sb = (msg == null) ? new StringBuilder() : new StringBuilder(msg);
        sb.append(" (through reference chain: ");
        this._appendPathDesc(sb);
        sb.append(')');
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + ": " + this.getMessage();
    }
    
    protected void _appendPathDesc(final StringBuilder sb) {
        final Iterator<Reference> it = this._path.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append("->");
            }
        }
    }
    
    public static class Reference implements Serializable
    {
        private static final long serialVersionUID = 1L;
        protected Object _from;
        protected String _fieldName;
        protected int _index;
        
        protected Reference() {
            this._index = -1;
        }
        
        public Reference(final Object from) {
            this._index = -1;
            this._from = from;
        }
        
        public Reference(final Object from, final String fieldName) {
            this._index = -1;
            this._from = from;
            if (fieldName == null) {
                throw new NullPointerException("Can not pass null fieldName");
            }
            this._fieldName = fieldName;
        }
        
        public Reference(final Object from, final int index) {
            this._index = -1;
            this._from = from;
            this._index = index;
        }
        
        public void setFrom(final Object o) {
            this._from = o;
        }
        
        public void setFieldName(final String n) {
            this._fieldName = n;
        }
        
        public void setIndex(final int ix) {
            this._index = ix;
        }
        
        public Object getFrom() {
            return this._from;
        }
        
        public String getFieldName() {
            return this._fieldName;
        }
        
        public int getIndex() {
            return this._index;
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            final Class<?> cls = (this._from instanceof Class) ? ((Class)this._from) : this._from.getClass();
            final Package pkg = cls.getPackage();
            if (pkg != null) {
                sb.append(pkg.getName());
                sb.append('.');
            }
            sb.append(cls.getSimpleName());
            sb.append('[');
            if (this._fieldName != null) {
                sb.append('\"');
                sb.append(this._fieldName);
                sb.append('\"');
            }
            else if (this._index >= 0) {
                sb.append(this._index);
            }
            else {
                sb.append('?');
            }
            sb.append(']');
            return sb.toString();
        }
    }
}
