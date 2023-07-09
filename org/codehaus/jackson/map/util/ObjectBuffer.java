// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.util.List;
import java.lang.reflect.Array;

public final class ObjectBuffer
{
    static final int INITIAL_CHUNK_SIZE = 12;
    static final int SMALL_CHUNK_SIZE = 16384;
    static final int MAX_CHUNK_SIZE = 262144;
    private Node _bufferHead;
    private Node _bufferTail;
    private int _bufferedEntryCount;
    private Object[] _freeBuffer;
    
    public Object[] resetAndStart() {
        this._reset();
        if (this._freeBuffer == null) {
            return new Object[12];
        }
        return this._freeBuffer;
    }
    
    public Object[] appendCompletedChunk(final Object[] fullChunk) {
        final Node next = new Node(fullChunk);
        if (this._bufferHead == null) {
            final Node node = next;
            this._bufferTail = node;
            this._bufferHead = node;
        }
        else {
            this._bufferTail.linkNext(next);
            this._bufferTail = next;
        }
        int len = fullChunk.length;
        this._bufferedEntryCount += len;
        if (len < 16384) {
            len += len;
        }
        else {
            len += len >> 2;
        }
        return new Object[len];
    }
    
    public Object[] completeAndClearBuffer(final Object[] lastChunk, final int lastChunkEntries) {
        final int totalSize = lastChunkEntries + this._bufferedEntryCount;
        final Object[] result = new Object[totalSize];
        this._copyTo(result, totalSize, lastChunk, lastChunkEntries);
        return result;
    }
    
    public <T> T[] completeAndClearBuffer(final Object[] lastChunk, final int lastChunkEntries, final Class<T> componentType) {
        final int totalSize = lastChunkEntries + this._bufferedEntryCount;
        final T[] result = (T[])Array.newInstance(componentType, totalSize);
        this._copyTo(result, totalSize, lastChunk, lastChunkEntries);
        this._reset();
        return result;
    }
    
    public void completeAndClearBuffer(final Object[] lastChunk, final int lastChunkEntries, final List<Object> resultList) {
        for (Node n = this._bufferHead; n != null; n = n.next()) {
            final Object[] curr = n.getData();
            for (int i = 0, len = curr.length; i < len; ++i) {
                resultList.add(curr[i]);
            }
        }
        for (int j = 0; j < lastChunkEntries; ++j) {
            resultList.add(lastChunk[j]);
        }
    }
    
    public int initialCapacity() {
        return (this._freeBuffer == null) ? 0 : this._freeBuffer.length;
    }
    
    public int bufferedSize() {
        return this._bufferedEntryCount;
    }
    
    protected void _reset() {
        if (this._bufferTail != null) {
            this._freeBuffer = this._bufferTail.getData();
        }
        final Node node = null;
        this._bufferTail = node;
        this._bufferHead = node;
        this._bufferedEntryCount = 0;
    }
    
    protected final void _copyTo(final Object resultArray, final int totalSize, final Object[] lastChunk, final int lastChunkEntries) {
        int ptr = 0;
        for (Node n = this._bufferHead; n != null; n = n.next()) {
            final Object[] curr = n.getData();
            final int len = curr.length;
            System.arraycopy(curr, 0, resultArray, ptr, len);
            ptr += len;
        }
        System.arraycopy(lastChunk, 0, resultArray, ptr, lastChunkEntries);
        ptr += lastChunkEntries;
        if (ptr != totalSize) {
            throw new IllegalStateException("Should have gotten " + totalSize + " entries, got " + ptr);
        }
    }
    
    static final class Node
    {
        final Object[] _data;
        Node _next;
        
        public Node(final Object[] data) {
            this._data = data;
        }
        
        public Object[] getData() {
            return this._data;
        }
        
        public Node next() {
            return this._next;
        }
        
        public void linkNext(final Node next) {
            if (this._next != null) {
                throw new IllegalStateException();
            }
            this._next = next;
        }
    }
}
