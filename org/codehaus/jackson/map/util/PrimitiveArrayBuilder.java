// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

public abstract class PrimitiveArrayBuilder<T>
{
    static final int INITIAL_CHUNK_SIZE = 12;
    static final int SMALL_CHUNK_SIZE = 16384;
    static final int MAX_CHUNK_SIZE = 262144;
    T _freeBuffer;
    Node<T> _bufferHead;
    Node<T> _bufferTail;
    int _bufferedEntryCount;
    
    protected PrimitiveArrayBuilder() {
    }
    
    public T resetAndStart() {
        this._reset();
        return (this._freeBuffer == null) ? this._constructArray(12) : this._freeBuffer;
    }
    
    public final T appendCompletedChunk(final T fullChunk, final int fullChunkLength) {
        final Node<T> next = new Node<T>(fullChunk, fullChunkLength);
        if (this._bufferHead == null) {
            final Node<T> node = next;
            this._bufferTail = node;
            this._bufferHead = node;
        }
        else {
            this._bufferTail.linkNext(next);
            this._bufferTail = next;
        }
        this._bufferedEntryCount += fullChunkLength;
        int nextLen = fullChunkLength;
        if (nextLen < 16384) {
            nextLen += nextLen;
        }
        else {
            nextLen += nextLen >> 2;
        }
        return this._constructArray(nextLen);
    }
    
    public T completeAndClearBuffer(final T lastChunk, final int lastChunkEntries) {
        final int totalSize = lastChunkEntries + this._bufferedEntryCount;
        final T resultArray = this._constructArray(totalSize);
        int ptr = 0;
        for (Node<T> n = this._bufferHead; n != null; n = n.next()) {
            ptr = n.copyData(resultArray, ptr);
        }
        System.arraycopy(lastChunk, 0, resultArray, ptr, lastChunkEntries);
        ptr += lastChunkEntries;
        if (ptr != totalSize) {
            throw new IllegalStateException("Should have gotten " + totalSize + " entries, got " + ptr);
        }
        return resultArray;
    }
    
    protected abstract T _constructArray(final int p0);
    
    protected void _reset() {
        if (this._bufferTail != null) {
            this._freeBuffer = this._bufferTail.getData();
        }
        final Node<T> node = null;
        this._bufferTail = node;
        this._bufferHead = node;
        this._bufferedEntryCount = 0;
    }
    
    static final class Node<T>
    {
        final T _data;
        final int _dataLength;
        Node<T> _next;
        
        public Node(final T data, final int dataLen) {
            this._data = data;
            this._dataLength = dataLen;
        }
        
        public T getData() {
            return this._data;
        }
        
        public int copyData(final T dst, int ptr) {
            System.arraycopy(this._data, 0, dst, ptr, this._dataLength);
            ptr += this._dataLength;
            return ptr;
        }
        
        public Node<T> next() {
            return this._next;
        }
        
        public void linkNext(final Node<T> next) {
            if (this._next != null) {
                throw new IllegalStateException();
            }
            this._next = next;
        }
    }
}
