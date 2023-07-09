// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import java.util.Map;
import java.util.Iterator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonStreamContext;

abstract class NodeCursor extends JsonStreamContext
{
    final NodeCursor _parent;
    
    public NodeCursor(final int contextType, final NodeCursor p) {
        this._type = contextType;
        this._index = -1;
        this._parent = p;
    }
    
    @Override
    public final NodeCursor getParent() {
        return this._parent;
    }
    
    @Override
    public abstract String getCurrentName();
    
    public abstract JsonToken nextToken();
    
    public abstract JsonToken nextValue();
    
    public abstract JsonToken endToken();
    
    public abstract JsonNode currentNode();
    
    public abstract boolean currentHasChildren();
    
    public final NodeCursor iterateChildren() {
        final JsonNode n = this.currentNode();
        if (n == null) {
            throw new IllegalStateException("No current node");
        }
        if (n.isArray()) {
            return new Array(n, this);
        }
        if (n.isObject()) {
            return new Object(n, this);
        }
        throw new IllegalStateException("Current node of type " + n.getClass().getName());
    }
    
    protected static final class RootValue extends NodeCursor
    {
        JsonNode _node;
        protected boolean _done;
        
        public RootValue(final JsonNode n, final NodeCursor p) {
            super(0, p);
            this._done = false;
            this._node = n;
        }
        
        @Override
        public String getCurrentName() {
            return null;
        }
        
        @Override
        public JsonToken nextToken() {
            if (!this._done) {
                this._done = true;
                return this._node.asToken();
            }
            this._node = null;
            return null;
        }
        
        @Override
        public JsonToken nextValue() {
            return this.nextToken();
        }
        
        @Override
        public JsonToken endToken() {
            return null;
        }
        
        @Override
        public JsonNode currentNode() {
            return this._node;
        }
        
        @Override
        public boolean currentHasChildren() {
            return false;
        }
    }
    
    protected static final class Array extends NodeCursor
    {
        Iterator<JsonNode> _contents;
        JsonNode _currentNode;
        
        public Array(final JsonNode n, final NodeCursor p) {
            super(1, p);
            this._contents = n.getElements();
        }
        
        @Override
        public String getCurrentName() {
            return null;
        }
        
        @Override
        public JsonToken nextToken() {
            if (!this._contents.hasNext()) {
                this._currentNode = null;
                return null;
            }
            this._currentNode = this._contents.next();
            return this._currentNode.asToken();
        }
        
        @Override
        public JsonToken nextValue() {
            return this.nextToken();
        }
        
        @Override
        public JsonToken endToken() {
            return JsonToken.END_ARRAY;
        }
        
        @Override
        public JsonNode currentNode() {
            return this._currentNode;
        }
        
        @Override
        public boolean currentHasChildren() {
            return ((ContainerNode)this.currentNode()).size() > 0;
        }
    }
    
    protected static final class Object extends NodeCursor
    {
        Iterator<Map.Entry<String, JsonNode>> _contents;
        Map.Entry<String, JsonNode> _current;
        boolean _needEntry;
        
        public Object(final JsonNode n, final NodeCursor p) {
            super(2, p);
            this._contents = ((ObjectNode)n).getFields();
            this._needEntry = true;
        }
        
        @Override
        public String getCurrentName() {
            return (this._current == null) ? null : this._current.getKey();
        }
        
        @Override
        public JsonToken nextToken() {
            if (!this._needEntry) {
                this._needEntry = true;
                return this._current.getValue().asToken();
            }
            if (!this._contents.hasNext()) {
                this._current = null;
                return null;
            }
            this._needEntry = false;
            this._current = this._contents.next();
            return JsonToken.FIELD_NAME;
        }
        
        @Override
        public JsonToken nextValue() {
            JsonToken t = this.nextToken();
            if (t == JsonToken.FIELD_NAME) {
                t = this.nextToken();
            }
            return t;
        }
        
        @Override
        public JsonToken endToken() {
            return JsonToken.END_OBJECT;
        }
        
        @Override
        public JsonNode currentNode() {
            return (this._current == null) ? null : this._current.getValue();
        }
        
        @Override
        public boolean currentHasChildren() {
            return ((ContainerNode)this.currentNode()).size() > 0;
        }
    }
}
