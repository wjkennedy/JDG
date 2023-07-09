// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import java.util.NoSuchElementException;
import java.util.Iterator;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import java.util.Collection;

public final class BeanPropertyMap
{
    private final Bucket[] _buckets;
    private final int _hashMask;
    private final int _size;
    
    public BeanPropertyMap(final Collection<SettableBeanProperty> properties) {
        this._size = properties.size();
        final int bucketCount = findSize(this._size);
        this._hashMask = bucketCount - 1;
        final Bucket[] buckets = new Bucket[bucketCount];
        for (final SettableBeanProperty property : properties) {
            final String key = property.getName();
            final int index = key.hashCode() & this._hashMask;
            buckets[index] = new Bucket(buckets[index], key, property);
        }
        this._buckets = buckets;
    }
    
    public void assignIndexes() {
        int index = 0;
        for (Bucket bucket : this._buckets) {
            while (bucket != null) {
                bucket.value.assignIndex(index++);
                bucket = bucket.next;
            }
        }
    }
    
    private static final int findSize(final int size) {
        int needed;
        int result;
        for (needed = ((size <= 32) ? (size + size) : (size + (size >> 2))), result = 2; result < needed; result += result) {}
        return result;
    }
    
    public int size() {
        return this._size;
    }
    
    public Iterator<SettableBeanProperty> allProperties() {
        return new IteratorImpl(this._buckets);
    }
    
    public SettableBeanProperty find(final String key) {
        final int index = key.hashCode() & this._hashMask;
        Bucket bucket = this._buckets[index];
        if (bucket == null) {
            return null;
        }
        if (bucket.key == key) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (bucket.key == key) {
                return bucket.value;
            }
        }
        return this._findWithEquals(key, index);
    }
    
    public void replace(final SettableBeanProperty property) {
        final String name = property.getName();
        final int index = name.hashCode() & this._buckets.length - 1;
        Bucket tail = null;
        boolean found = false;
        for (Bucket bucket = this._buckets[index]; bucket != null; bucket = bucket.next) {
            if (!found && bucket.key.equals(name)) {
                found = true;
            }
            else {
                tail = new Bucket(tail, bucket.key, bucket.value);
            }
        }
        if (!found) {
            throw new NoSuchElementException("No entry '" + property + "' found, can't replace");
        }
        this._buckets[index] = new Bucket(tail, name, property);
    }
    
    public void remove(final SettableBeanProperty property) {
        final String name = property.getName();
        final int index = name.hashCode() & this._buckets.length - 1;
        Bucket tail = null;
        boolean found = false;
        for (Bucket bucket = this._buckets[index]; bucket != null; bucket = bucket.next) {
            if (!found && bucket.key.equals(name)) {
                found = true;
            }
            else {
                tail = new Bucket(tail, bucket.key, bucket.value);
            }
        }
        if (!found) {
            throw new NoSuchElementException("No entry '" + property + "' found, can't remove");
        }
        this._buckets[index] = tail;
    }
    
    private SettableBeanProperty _findWithEquals(final String key, final int index) {
        for (Bucket bucket = this._buckets[index]; bucket != null; bucket = bucket.next) {
            if (key.equals(bucket.key)) {
                return bucket.value;
            }
        }
        return null;
    }
    
    private static final class Bucket
    {
        public final Bucket next;
        public final String key;
        public final SettableBeanProperty value;
        
        public Bucket(final Bucket next, final String key, final SettableBeanProperty value) {
            this.next = next;
            this.key = key;
            this.value = value;
        }
    }
    
    private static final class IteratorImpl implements Iterator<SettableBeanProperty>
    {
        private final Bucket[] _buckets;
        private Bucket _currentBucket;
        private int _nextBucketIndex;
        
        public IteratorImpl(final Bucket[] buckets) {
            this._buckets = buckets;
            int i = 0;
            final int len = this._buckets.length;
            while (i < len) {
                final Bucket b = this._buckets[i++];
                if (b != null) {
                    this._currentBucket = b;
                    break;
                }
            }
            this._nextBucketIndex = i;
        }
        
        public boolean hasNext() {
            return this._currentBucket != null;
        }
        
        public SettableBeanProperty next() {
            final Bucket curr = this._currentBucket;
            if (curr == null) {
                throw new NoSuchElementException();
            }
            Bucket b;
            for (b = curr.next; b == null && this._nextBucketIndex < this._buckets.length; b = this._buckets[this._nextBucketIndex++]) {}
            this._currentBucket = b;
            return curr.value;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
