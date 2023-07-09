// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.sym;

import java.util.Arrays;
import org.codehaus.jackson.util.InternCache;

public final class CharsToNameCanonicalizer
{
    public static final int HASH_MULT = 33;
    protected static final int DEFAULT_TABLE_SIZE = 64;
    protected static final int MAX_TABLE_SIZE = 65536;
    static final int MAX_ENTRIES_FOR_REUSE = 12000;
    static final int MAX_COLL_CHAIN_LENGTH = 255;
    static final int MAX_COLL_CHAIN_FOR_REUSE = 63;
    static final CharsToNameCanonicalizer sBootstrapSymbolTable;
    protected CharsToNameCanonicalizer _parent;
    private final int _hashSeed;
    protected final boolean _intern;
    protected final boolean _canonicalize;
    protected String[] _symbols;
    protected Bucket[] _buckets;
    protected int _size;
    protected int _sizeThreshold;
    protected int _indexMask;
    protected int _longestCollisionList;
    protected boolean _dirty;
    
    public static CharsToNameCanonicalizer createRoot() {
        final long now = System.currentTimeMillis();
        final int seed = (int)now + ((int)now >>> 32) | 0x1;
        return createRoot(seed);
    }
    
    protected static CharsToNameCanonicalizer createRoot(final int hashSeed) {
        return CharsToNameCanonicalizer.sBootstrapSymbolTable.makeOrphan(hashSeed);
    }
    
    private CharsToNameCanonicalizer() {
        this._canonicalize = true;
        this._intern = true;
        this._dirty = true;
        this._hashSeed = 0;
        this._longestCollisionList = 0;
        this.initTables(64);
    }
    
    private void initTables(final int initialSize) {
        this._symbols = new String[initialSize];
        this._buckets = new Bucket[initialSize >> 1];
        this._indexMask = initialSize - 1;
        this._size = 0;
        this._longestCollisionList = 0;
        this._sizeThreshold = _thresholdSize(initialSize);
    }
    
    private static final int _thresholdSize(final int hashAreaSize) {
        return hashAreaSize - (hashAreaSize >> 2);
    }
    
    private CharsToNameCanonicalizer(final CharsToNameCanonicalizer parent, final boolean canonicalize, final boolean intern, final String[] symbols, final Bucket[] buckets, final int size, final int hashSeed, final int longestColl) {
        this._parent = parent;
        this._canonicalize = canonicalize;
        this._intern = intern;
        this._symbols = symbols;
        this._buckets = buckets;
        this._size = size;
        this._hashSeed = hashSeed;
        final int arrayLen = symbols.length;
        this._sizeThreshold = _thresholdSize(arrayLen);
        this._indexMask = arrayLen - 1;
        this._longestCollisionList = longestColl;
        this._dirty = false;
    }
    
    public synchronized CharsToNameCanonicalizer makeChild(final boolean canonicalize, final boolean intern) {
        final String[] symbols;
        final Bucket[] buckets;
        final int size;
        final int hashSeed;
        final int longestCollisionList;
        synchronized (this) {
            symbols = this._symbols;
            buckets = this._buckets;
            size = this._size;
            hashSeed = this._hashSeed;
            longestCollisionList = this._longestCollisionList;
        }
        return new CharsToNameCanonicalizer(this, canonicalize, intern, symbols, buckets, size, hashSeed, longestCollisionList);
    }
    
    private CharsToNameCanonicalizer makeOrphan(final int seed) {
        return new CharsToNameCanonicalizer(null, true, true, this._symbols, this._buckets, this._size, seed, this._longestCollisionList);
    }
    
    private void mergeChild(final CharsToNameCanonicalizer child) {
        if (child.size() > 12000 || child._longestCollisionList > 63) {
            synchronized (this) {
                this.initTables(64);
                this._dirty = false;
            }
        }
        else {
            if (child.size() <= this.size()) {
                return;
            }
            synchronized (this) {
                this._symbols = child._symbols;
                this._buckets = child._buckets;
                this._size = child._size;
                this._sizeThreshold = child._sizeThreshold;
                this._indexMask = child._indexMask;
                this._longestCollisionList = child._longestCollisionList;
                this._dirty = false;
            }
        }
    }
    
    public void release() {
        if (!this.maybeDirty()) {
            return;
        }
        if (this._parent != null) {
            this._parent.mergeChild(this);
            this._dirty = false;
        }
    }
    
    public int size() {
        return this._size;
    }
    
    public int bucketCount() {
        return this._symbols.length;
    }
    
    public boolean maybeDirty() {
        return this._dirty;
    }
    
    public int hashSeed() {
        return this._hashSeed;
    }
    
    public int collisionCount() {
        int count = 0;
        for (final Bucket bucket : this._buckets) {
            if (bucket != null) {
                count += bucket.length();
            }
        }
        return count;
    }
    
    public int maxCollisionLength() {
        return this._longestCollisionList;
    }
    
    public String findSymbol(final char[] buffer, final int start, final int len, final int h) {
        if (len < 1) {
            return "";
        }
        if (!this._canonicalize) {
            return new String(buffer, start, len);
        }
        int index = this._hashToIndex(h);
        String sym = this._symbols[index];
        if (sym != null) {
            Label_0097: {
                if (sym.length() == len) {
                    int i = 0;
                    while (true) {
                        while (sym.charAt(i) == buffer[start + i]) {
                            if (++i >= len) {
                                if (i == len) {
                                    return sym;
                                }
                                break Label_0097;
                            }
                        }
                        continue;
                    }
                }
            }
            final Bucket b = this._buckets[index >> 1];
            if (b != null) {
                sym = b.find(buffer, start, len);
                if (sym != null) {
                    return sym;
                }
            }
        }
        if (!this._dirty) {
            this.copyArrays();
            this._dirty = true;
        }
        else if (this._size >= this._sizeThreshold) {
            this.rehash();
            index = this._hashToIndex(this.calcHash(buffer, start, len));
        }
        String newSymbol = new String(buffer, start, len);
        if (this._intern) {
            newSymbol = InternCache.instance.intern(newSymbol);
        }
        ++this._size;
        if (this._symbols[index] == null) {
            this._symbols[index] = newSymbol;
        }
        else {
            final int bix = index >> 1;
            final Bucket newB = new Bucket(newSymbol, this._buckets[bix]);
            this._buckets[bix] = newB;
            this._longestCollisionList = Math.max(newB.length(), this._longestCollisionList);
            if (this._longestCollisionList > 255) {
                this.reportTooManyCollisions(255);
            }
        }
        return newSymbol;
    }
    
    public final int _hashToIndex(int rawHash) {
        rawHash += rawHash >>> 15;
        return rawHash & this._indexMask;
    }
    
    public int calcHash(final char[] buffer, final int start, final int len) {
        int hash = this._hashSeed;
        for (int i = 0; i < len; ++i) {
            hash = hash * 33 + buffer[i];
        }
        return (hash == 0) ? 1 : hash;
    }
    
    public int calcHash(final String key) {
        final int len = key.length();
        int hash = this._hashSeed;
        for (int i = 0; i < len; ++i) {
            hash = hash * 33 + key.charAt(i);
        }
        return (hash == 0) ? 1 : hash;
    }
    
    private void copyArrays() {
        final String[] oldSyms = this._symbols;
        int size = oldSyms.length;
        System.arraycopy(oldSyms, 0, this._symbols = new String[size], 0, size);
        final Bucket[] oldBuckets = this._buckets;
        size = oldBuckets.length;
        System.arraycopy(oldBuckets, 0, this._buckets = new Bucket[size], 0, size);
    }
    
    private void rehash() {
        int size = this._symbols.length;
        final int newSize = size + size;
        if (newSize > 65536) {
            this._size = 0;
            Arrays.fill(this._symbols, null);
            Arrays.fill(this._buckets, null);
            this._dirty = true;
            return;
        }
        final String[] oldSyms = this._symbols;
        final Bucket[] oldBuckets = this._buckets;
        this._symbols = new String[newSize];
        this._buckets = new Bucket[newSize >> 1];
        this._indexMask = newSize - 1;
        this._sizeThreshold = _thresholdSize(newSize);
        int count = 0;
        int maxColl = 0;
        for (final String symbol : oldSyms) {
            if (symbol != null) {
                ++count;
                final int index = this._hashToIndex(this.calcHash(symbol));
                if (this._symbols[index] == null) {
                    this._symbols[index] = symbol;
                }
                else {
                    final int bix = index >> 1;
                    final Bucket newB = new Bucket(symbol, this._buckets[bix]);
                    this._buckets[bix] = newB;
                    maxColl = Math.max(maxColl, newB.length());
                }
            }
        }
        size >>= 1;
        for (Bucket b : oldBuckets) {
            while (b != null) {
                ++count;
                final String symbol2 = b.getSymbol();
                final int index2 = this._hashToIndex(this.calcHash(symbol2));
                if (this._symbols[index2] == null) {
                    this._symbols[index2] = symbol2;
                }
                else {
                    final int bix2 = index2 >> 1;
                    final Bucket newB2 = new Bucket(symbol2, this._buckets[bix2]);
                    this._buckets[bix2] = newB2;
                    maxColl = Math.max(maxColl, newB2.length());
                }
                b = b.getNext();
            }
        }
        this._longestCollisionList = maxColl;
        if (count != this._size) {
            throw new Error("Internal error on SymbolTable.rehash(): had " + this._size + " entries; now have " + count + ".");
        }
    }
    
    protected void reportTooManyCollisions(final int maxLen) {
        throw new IllegalStateException("Longest collision chain in symbol table (of size " + this._size + ") now exceeds maximum, " + maxLen + " -- suspect a DoS attack based on hash collisions");
    }
    
    static {
        sBootstrapSymbolTable = new CharsToNameCanonicalizer();
    }
    
    static final class Bucket
    {
        private final String _symbol;
        private final Bucket _next;
        private final int _length;
        
        public Bucket(final String symbol, final Bucket next) {
            this._symbol = symbol;
            this._next = next;
            this._length = ((next == null) ? 1 : (next._length + 1));
        }
        
        public String getSymbol() {
            return this._symbol;
        }
        
        public Bucket getNext() {
            return this._next;
        }
        
        public int length() {
            return this._length;
        }
        
        public String find(final char[] buf, final int start, final int len) {
            String sym = this._symbol;
            Bucket b = this._next;
            while (true) {
                Label_0061: {
                    if (sym.length() == len) {
                        int i = 0;
                        while (true) {
                            while (sym.charAt(i) == buf[start + i]) {
                                if (++i >= len) {
                                    if (i == len) {
                                        return sym;
                                    }
                                    break Label_0061;
                                }
                            }
                            continue;
                        }
                    }
                }
                if (b == null) {
                    return null;
                }
                sym = b.getSymbol();
                b = b.getNext();
            }
        }
    }
}
