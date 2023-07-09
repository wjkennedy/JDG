// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.sym;

import java.util.Arrays;
import org.codehaus.jackson.util.InternCache;
import java.util.concurrent.atomic.AtomicReference;

public final class BytesToNameCanonicalizer
{
    protected static final int DEFAULT_TABLE_SIZE = 64;
    protected static final int MAX_TABLE_SIZE = 65536;
    static final int MAX_ENTRIES_FOR_REUSE = 6000;
    static final int MAX_COLL_CHAIN_LENGTH = 255;
    static final int MAX_COLL_CHAIN_FOR_REUSE = 63;
    static final int MIN_HASH_SIZE = 16;
    static final int INITIAL_COLLISION_LEN = 32;
    static final int LAST_VALID_BUCKET = 254;
    protected final BytesToNameCanonicalizer _parent;
    protected final AtomicReference<TableInfo> _tableInfo;
    private final int _hashSeed;
    protected final boolean _intern;
    protected int _count;
    protected int _longestCollisionList;
    protected int _mainHashMask;
    protected int[] _mainHash;
    protected Name[] _mainNames;
    protected Bucket[] _collList;
    protected int _collCount;
    protected int _collEnd;
    private transient boolean _needRehash;
    private boolean _mainHashShared;
    private boolean _mainNamesShared;
    private boolean _collListShared;
    private static final int MULT = 33;
    private static final int MULT2 = 65599;
    private static final int MULT3 = 31;
    
    private BytesToNameCanonicalizer(int hashSize, final boolean intern, final int seed) {
        this._parent = null;
        this._hashSeed = seed;
        this._intern = intern;
        if (hashSize < 16) {
            hashSize = 16;
        }
        else if ((hashSize & hashSize - 1) != 0x0) {
            int curr;
            for (curr = 16; curr < hashSize; curr += curr) {}
            hashSize = curr;
        }
        this._tableInfo = new AtomicReference<TableInfo>(this.initTableInfo(hashSize));
    }
    
    private BytesToNameCanonicalizer(final BytesToNameCanonicalizer parent, final boolean intern, final int seed, final TableInfo state) {
        this._parent = parent;
        this._hashSeed = seed;
        this._intern = intern;
        this._tableInfo = null;
        this._count = state.count;
        this._mainHashMask = state.mainHashMask;
        this._mainHash = state.mainHash;
        this._mainNames = state.mainNames;
        this._collList = state.collList;
        this._collCount = state.collCount;
        this._collEnd = state.collEnd;
        this._longestCollisionList = state.longestCollisionList;
        this._needRehash = false;
        this._mainHashShared = true;
        this._mainNamesShared = true;
        this._collListShared = true;
    }
    
    private TableInfo initTableInfo(final int hashSize) {
        return new TableInfo(0, hashSize - 1, new int[hashSize], new Name[hashSize], null, 0, 0, 0);
    }
    
    public static BytesToNameCanonicalizer createRoot() {
        final long now = System.currentTimeMillis();
        final int seed = (int)now + ((int)now >>> 32) | 0x1;
        return createRoot(seed);
    }
    
    protected static BytesToNameCanonicalizer createRoot(final int hashSeed) {
        return new BytesToNameCanonicalizer(64, true, hashSeed);
    }
    
    public BytesToNameCanonicalizer makeChild(final boolean canonicalize, final boolean intern) {
        return new BytesToNameCanonicalizer(this, intern, this._hashSeed, this._tableInfo.get());
    }
    
    public void release() {
        if (this._parent != null && this.maybeDirty()) {
            this._parent.mergeChild(new TableInfo(this));
            this._mainHashShared = true;
            this._mainNamesShared = true;
            this._collListShared = true;
        }
    }
    
    private void mergeChild(TableInfo childState) {
        final int childCount = childState.count;
        final TableInfo currState = this._tableInfo.get();
        if (childCount <= currState.count) {
            return;
        }
        if (childCount > 6000 || childState.longestCollisionList > 63) {
            childState = this.initTableInfo(64);
        }
        this._tableInfo.compareAndSet(currState, childState);
    }
    
    public int size() {
        if (this._tableInfo != null) {
            return this._tableInfo.get().count;
        }
        return this._count;
    }
    
    public int bucketCount() {
        return this._mainHash.length;
    }
    
    public boolean maybeDirty() {
        return !this._mainHashShared;
    }
    
    public int hashSeed() {
        return this._hashSeed;
    }
    
    public int collisionCount() {
        return this._collCount;
    }
    
    public int maxCollisionLength() {
        return this._longestCollisionList;
    }
    
    public static Name getEmptyName() {
        return Name1.getEmptyName();
    }
    
    public Name findName(final int firstQuad) {
        final int hash = this.calcHash(firstQuad);
        final int ix = hash & this._mainHashMask;
        int val = this._mainHash[ix];
        if ((val >> 8 ^ hash) << 8 == 0) {
            final Name name = this._mainNames[ix];
            if (name == null) {
                return null;
            }
            if (name.equals(firstQuad)) {
                return name;
            }
        }
        else if (val == 0) {
            return null;
        }
        val &= 0xFF;
        if (val > 0) {
            --val;
            final Bucket bucket = this._collList[val];
            if (bucket != null) {
                return bucket.find(hash, firstQuad, 0);
            }
        }
        return null;
    }
    
    public Name findName(final int firstQuad, final int secondQuad) {
        final int hash = (secondQuad == 0) ? this.calcHash(firstQuad) : this.calcHash(firstQuad, secondQuad);
        final int ix = hash & this._mainHashMask;
        int val = this._mainHash[ix];
        if ((val >> 8 ^ hash) << 8 == 0) {
            final Name name = this._mainNames[ix];
            if (name == null) {
                return null;
            }
            if (name.equals(firstQuad, secondQuad)) {
                return name;
            }
        }
        else if (val == 0) {
            return null;
        }
        val &= 0xFF;
        if (val > 0) {
            --val;
            final Bucket bucket = this._collList[val];
            if (bucket != null) {
                return bucket.find(hash, firstQuad, secondQuad);
            }
        }
        return null;
    }
    
    public Name findName(final int[] quads, final int qlen) {
        if (qlen < 3) {
            return this.findName(quads[0], (qlen < 2) ? 0 : quads[1]);
        }
        final int hash = this.calcHash(quads, qlen);
        final int ix = hash & this._mainHashMask;
        int val = this._mainHash[ix];
        if ((val >> 8 ^ hash) << 8 == 0) {
            final Name name = this._mainNames[ix];
            if (name == null || name.equals(quads, qlen)) {
                return name;
            }
        }
        else if (val == 0) {
            return null;
        }
        val &= 0xFF;
        if (val > 0) {
            --val;
            final Bucket bucket = this._collList[val];
            if (bucket != null) {
                return bucket.find(hash, quads, qlen);
            }
        }
        return null;
    }
    
    public Name addName(String symbolStr, final int q1, final int q2) {
        if (this._intern) {
            symbolStr = InternCache.instance.intern(symbolStr);
        }
        final int hash = (q2 == 0) ? this.calcHash(q1) : this.calcHash(q1, q2);
        final Name symbol = constructName(hash, symbolStr, q1, q2);
        this._addSymbol(hash, symbol);
        return symbol;
    }
    
    public Name addName(String symbolStr, final int[] quads, final int qlen) {
        if (this._intern) {
            symbolStr = InternCache.instance.intern(symbolStr);
        }
        int hash;
        if (qlen < 3) {
            hash = ((qlen == 1) ? this.calcHash(quads[0]) : this.calcHash(quads[0], quads[1]));
        }
        else {
            hash = this.calcHash(quads, qlen);
        }
        final Name symbol = constructName(hash, symbolStr, quads, qlen);
        this._addSymbol(hash, symbol);
        return symbol;
    }
    
    public final int calcHash(final int firstQuad) {
        int hash = firstQuad ^ this._hashSeed;
        hash += hash >>> 15;
        hash ^= hash >>> 9;
        return hash;
    }
    
    public final int calcHash(final int firstQuad, final int secondQuad) {
        int hash = firstQuad;
        hash ^= hash >>> 15;
        hash += secondQuad * 33;
        hash ^= this._hashSeed;
        hash += hash >>> 7;
        return hash;
    }
    
    public final int calcHash(final int[] quads, final int qlen) {
        if (qlen < 3) {
            throw new IllegalArgumentException();
        }
        int hash = quads[0] ^ this._hashSeed;
        hash += hash >>> 9;
        hash *= 33;
        hash += quads[1];
        hash *= 65599;
        hash += hash >>> 15;
        hash ^= quads[2];
        hash += hash >>> 17;
        for (int i = 3; i < qlen; ++i) {
            hash = (hash * 31 ^ quads[i]);
            hash += hash >>> 3;
            hash ^= hash << 7;
        }
        hash += hash >>> 15;
        hash ^= hash << 9;
        return hash;
    }
    
    protected static int[] calcQuads(final byte[] wordBytes) {
        final int blen = wordBytes.length;
        final int[] result = new int[(blen + 3) / 4];
        for (int i = 0; i < blen; ++i) {
            int x = wordBytes[i] & 0xFF;
            if (++i < blen) {
                x = (x << 8 | (wordBytes[i] & 0xFF));
                if (++i < blen) {
                    x = (x << 8 | (wordBytes[i] & 0xFF));
                    if (++i < blen) {
                        x = (x << 8 | (wordBytes[i] & 0xFF));
                    }
                }
            }
            result[i >> 2] = x;
        }
        return result;
    }
    
    private void _addSymbol(final int hash, final Name symbol) {
        if (this._mainHashShared) {
            this.unshareMain();
        }
        if (this._needRehash) {
            this.rehash();
        }
        ++this._count;
        final int ix = hash & this._mainHashMask;
        if (this._mainNames[ix] == null) {
            this._mainHash[ix] = hash << 8;
            if (this._mainNamesShared) {
                this.unshareNames();
            }
            this._mainNames[ix] = symbol;
        }
        else {
            if (this._collListShared) {
                this.unshareCollision();
            }
            ++this._collCount;
            final int entryValue = this._mainHash[ix];
            int bucket = entryValue & 0xFF;
            if (bucket == 0) {
                if (this._collEnd <= 254) {
                    bucket = this._collEnd;
                    ++this._collEnd;
                    if (bucket >= this._collList.length) {
                        this.expandCollision();
                    }
                }
                else {
                    bucket = this.findBestBucket();
                }
                this._mainHash[ix] = ((entryValue & 0xFFFFFF00) | bucket + 1);
            }
            else {
                --bucket;
            }
            final Bucket newB = new Bucket(symbol, this._collList[bucket]);
            this._collList[bucket] = newB;
            this._longestCollisionList = Math.max(newB.length(), this._longestCollisionList);
            if (this._longestCollisionList > 255) {
                this.reportTooManyCollisions(255);
            }
        }
        final int hashSize = this._mainHash.length;
        if (this._count > hashSize >> 1) {
            final int hashQuarter = hashSize >> 2;
            if (this._count > hashSize - hashQuarter) {
                this._needRehash = true;
            }
            else if (this._collCount >= hashQuarter) {
                this._needRehash = true;
            }
        }
    }
    
    private void rehash() {
        this._needRehash = false;
        this._mainNamesShared = false;
        final int[] oldMainHash = this._mainHash;
        final int len = oldMainHash.length;
        final int newLen = len + len;
        if (newLen > 65536) {
            this.nukeSymbols();
            return;
        }
        this._mainHash = new int[newLen];
        this._mainHashMask = newLen - 1;
        final Name[] oldNames = this._mainNames;
        this._mainNames = new Name[newLen];
        int symbolsSeen = 0;
        for (final Name symbol : oldNames) {
            if (symbol != null) {
                ++symbolsSeen;
                final int hash = symbol.hashCode();
                final int ix = hash & this._mainHashMask;
                this._mainNames[ix] = symbol;
                this._mainHash[ix] = hash << 8;
            }
        }
        final int oldEnd = this._collEnd;
        if (oldEnd == 0) {
            this._longestCollisionList = 0;
            return;
        }
        this._collCount = 0;
        this._collEnd = 0;
        this._collListShared = false;
        int maxColl = 0;
        final Bucket[] oldBuckets = this._collList;
        this._collList = new Bucket[oldBuckets.length];
        for (Bucket curr : oldBuckets) {
            while (curr != null) {
                ++symbolsSeen;
                final Name symbol2 = curr._name;
                final int hash2 = symbol2.hashCode();
                final int ix2 = hash2 & this._mainHashMask;
                final int val = this._mainHash[ix2];
                if (this._mainNames[ix2] == null) {
                    this._mainHash[ix2] = hash2 << 8;
                    this._mainNames[ix2] = symbol2;
                }
                else {
                    ++this._collCount;
                    int bucket = val & 0xFF;
                    if (bucket == 0) {
                        if (this._collEnd <= 254) {
                            bucket = this._collEnd;
                            ++this._collEnd;
                            if (bucket >= this._collList.length) {
                                this.expandCollision();
                            }
                        }
                        else {
                            bucket = this.findBestBucket();
                        }
                        this._mainHash[ix2] = ((val & 0xFFFFFF00) | bucket + 1);
                    }
                    else {
                        --bucket;
                    }
                    final Bucket newB = new Bucket(symbol2, this._collList[bucket]);
                    this._collList[bucket] = newB;
                    maxColl = Math.max(maxColl, newB.length());
                }
                curr = curr._next;
            }
        }
        this._longestCollisionList = maxColl;
        if (symbolsSeen != this._count) {
            throw new RuntimeException("Internal error: count after rehash " + symbolsSeen + "; should be " + this._count);
        }
    }
    
    private void nukeSymbols() {
        this._count = 0;
        this._longestCollisionList = 0;
        Arrays.fill(this._mainHash, 0);
        Arrays.fill(this._mainNames, null);
        Arrays.fill(this._collList, null);
        this._collCount = 0;
        this._collEnd = 0;
    }
    
    private int findBestBucket() {
        final Bucket[] buckets = this._collList;
        int bestCount = Integer.MAX_VALUE;
        int bestIx = -1;
        for (int i = 0, len = this._collEnd; i < len; ++i) {
            final int count = buckets[i].length();
            if (count < bestCount) {
                if (count == 1) {
                    return i;
                }
                bestCount = count;
                bestIx = i;
            }
        }
        return bestIx;
    }
    
    private void unshareMain() {
        final int[] old = this._mainHash;
        final int len = this._mainHash.length;
        System.arraycopy(old, 0, this._mainHash = new int[len], 0, len);
        this._mainHashShared = false;
    }
    
    private void unshareCollision() {
        final Bucket[] old = this._collList;
        if (old == null) {
            this._collList = new Bucket[32];
        }
        else {
            final int len = old.length;
            System.arraycopy(old, 0, this._collList = new Bucket[len], 0, len);
        }
        this._collListShared = false;
    }
    
    private void unshareNames() {
        final Name[] old = this._mainNames;
        final int len = old.length;
        System.arraycopy(old, 0, this._mainNames = new Name[len], 0, len);
        this._mainNamesShared = false;
    }
    
    private void expandCollision() {
        final Bucket[] old = this._collList;
        final int len = old.length;
        System.arraycopy(old, 0, this._collList = new Bucket[len + len], 0, len);
    }
    
    private static Name constructName(final int hash, final String name, final int q1, final int q2) {
        if (q2 == 0) {
            return new Name1(name, hash, q1);
        }
        return new Name2(name, hash, q1, q2);
    }
    
    private static Name constructName(final int hash, final String name, final int[] quads, final int qlen) {
        if (qlen < 4) {
            switch (qlen) {
                case 1: {
                    return new Name1(name, hash, quads[0]);
                }
                case 2: {
                    return new Name2(name, hash, quads[0], quads[1]);
                }
                case 3: {
                    return new Name3(name, hash, quads[0], quads[1], quads[2]);
                }
            }
        }
        final int[] buf = new int[qlen];
        for (int i = 0; i < qlen; ++i) {
            buf[i] = quads[i];
        }
        return new NameN(name, hash, buf, qlen);
    }
    
    protected void reportTooManyCollisions(final int maxLen) {
        throw new IllegalStateException("Longest collision chain in symbol table (of size " + this._count + ") now exceeds maximum, " + maxLen + " -- suspect a DoS attack based on hash collisions");
    }
    
    private static final class TableInfo
    {
        public final int count;
        public final int mainHashMask;
        public final int[] mainHash;
        public final Name[] mainNames;
        public final Bucket[] collList;
        public final int collCount;
        public final int collEnd;
        public final int longestCollisionList;
        
        public TableInfo(final int count, final int mainHashMask, final int[] mainHash, final Name[] mainNames, final Bucket[] collList, final int collCount, final int collEnd, final int longestCollisionList) {
            this.count = count;
            this.mainHashMask = mainHashMask;
            this.mainHash = mainHash;
            this.mainNames = mainNames;
            this.collList = collList;
            this.collCount = collCount;
            this.collEnd = collEnd;
            this.longestCollisionList = longestCollisionList;
        }
        
        public TableInfo(final BytesToNameCanonicalizer src) {
            this.count = src._count;
            this.mainHashMask = src._mainHashMask;
            this.mainHash = src._mainHash;
            this.mainNames = src._mainNames;
            this.collList = src._collList;
            this.collCount = src._collCount;
            this.collEnd = src._collEnd;
            this.longestCollisionList = src._longestCollisionList;
        }
    }
    
    static final class Bucket
    {
        protected final Name _name;
        protected final Bucket _next;
        private final int _length;
        
        Bucket(final Name name, final Bucket next) {
            this._name = name;
            this._next = next;
            this._length = ((next == null) ? 1 : (next._length + 1));
        }
        
        public int length() {
            return this._length;
        }
        
        public Name find(final int hash, final int firstQuad, final int secondQuad) {
            if (this._name.hashCode() == hash && this._name.equals(firstQuad, secondQuad)) {
                return this._name;
            }
            for (Bucket curr = this._next; curr != null; curr = curr._next) {
                final Name currName = curr._name;
                if (currName.hashCode() == hash && currName.equals(firstQuad, secondQuad)) {
                    return currName;
                }
            }
            return null;
        }
        
        public Name find(final int hash, final int[] quads, final int qlen) {
            if (this._name.hashCode() == hash && this._name.equals(quads, qlen)) {
                return this._name;
            }
            for (Bucket curr = this._next; curr != null; curr = curr._next) {
                final Name currName = curr._name;
                if (currName.hashCode() == hash && currName.equals(quads, qlen)) {
                    return currName;
                }
            }
            return null;
        }
    }
}
