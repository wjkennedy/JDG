// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import java.util.NoSuchElementException;
import java.io.IOException;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.JavaType;
import java.util.Iterator;

public class MappingIterator<T> implements Iterator<T>
{
    protected static final MappingIterator<?> EMPTY_ITERATOR;
    protected final JavaType _type;
    protected final DeserializationContext _context;
    protected final JsonDeserializer<T> _deserializer;
    protected JsonParser _parser;
    protected final boolean _closeParser;
    protected boolean _hasNextChecked;
    protected final T _updatedValue;
    
    protected MappingIterator(final JavaType type, final JsonParser jp, final DeserializationContext ctxt, final JsonDeserializer<?> deser) {
        this(type, jp, ctxt, deser, true, null);
    }
    
    protected MappingIterator(final JavaType type, final JsonParser jp, final DeserializationContext ctxt, final JsonDeserializer<?> deser, final boolean closeParser, final Object valueToUpdate) {
        this._type = type;
        this._parser = jp;
        this._context = ctxt;
        this._deserializer = (JsonDeserializer<T>)deser;
        if (jp != null && jp.getCurrentToken() == JsonToken.START_ARRAY) {
            final JsonStreamContext sc = jp.getParsingContext();
            if (!sc.inRoot()) {
                jp.clearCurrentToken();
            }
        }
        this._closeParser = closeParser;
        if (valueToUpdate == null) {
            this._updatedValue = null;
        }
        else {
            this._updatedValue = (T)valueToUpdate;
        }
    }
    
    protected static <T> MappingIterator<T> emptyIterator() {
        return (MappingIterator<T>)MappingIterator.EMPTY_ITERATOR;
    }
    
    public boolean hasNext() {
        try {
            return this.hasNextValue();
        }
        catch (final JsonMappingException e) {
            throw new RuntimeJsonMappingException(e.getMessage(), e);
        }
        catch (final IOException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }
    
    public T next() {
        try {
            return this.nextValue();
        }
        catch (final JsonMappingException e) {
            throw new RuntimeJsonMappingException(e.getMessage(), e);
        }
        catch (final IOException e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public boolean hasNextValue() throws IOException {
        if (this._parser == null) {
            return false;
        }
        if (!this._hasNextChecked) {
            JsonToken t = this._parser.getCurrentToken();
            this._hasNextChecked = true;
            if (t == null) {
                t = this._parser.nextToken();
                if (t == null) {
                    final JsonParser jp = this._parser;
                    this._parser = null;
                    if (this._closeParser) {
                        jp.close();
                    }
                    return false;
                }
                if (t == JsonToken.END_ARRAY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public T nextValue() throws IOException {
        if (!this._hasNextChecked && !this.hasNextValue()) {
            throw new NoSuchElementException();
        }
        if (this._parser == null) {
            throw new NoSuchElementException();
        }
        this._hasNextChecked = false;
        T result;
        if (this._updatedValue == null) {
            result = this._deserializer.deserialize(this._parser, this._context);
        }
        else {
            this._deserializer.deserialize(this._parser, this._context, this._updatedValue);
            result = this._updatedValue;
        }
        this._parser.clearCurrentToken();
        return result;
    }
    
    static {
        EMPTY_ITERATOR = new MappingIterator<Object>(null, null, null, null, false, null);
    }
}
