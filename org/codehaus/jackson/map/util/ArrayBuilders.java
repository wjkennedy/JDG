// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.util.NoSuchElementException;
import java.util.Iterator;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

public final class ArrayBuilders
{
    BooleanBuilder _booleanBuilder;
    ByteBuilder _byteBuilder;
    ShortBuilder _shortBuilder;
    IntBuilder _intBuilder;
    LongBuilder _longBuilder;
    FloatBuilder _floatBuilder;
    DoubleBuilder _doubleBuilder;
    
    public ArrayBuilders() {
        this._booleanBuilder = null;
        this._byteBuilder = null;
        this._shortBuilder = null;
        this._intBuilder = null;
        this._longBuilder = null;
        this._floatBuilder = null;
        this._doubleBuilder = null;
    }
    
    public BooleanBuilder getBooleanBuilder() {
        if (this._booleanBuilder == null) {
            this._booleanBuilder = new BooleanBuilder();
        }
        return this._booleanBuilder;
    }
    
    public ByteBuilder getByteBuilder() {
        if (this._byteBuilder == null) {
            this._byteBuilder = new ByteBuilder();
        }
        return this._byteBuilder;
    }
    
    public ShortBuilder getShortBuilder() {
        if (this._shortBuilder == null) {
            this._shortBuilder = new ShortBuilder();
        }
        return this._shortBuilder;
    }
    
    public IntBuilder getIntBuilder() {
        if (this._intBuilder == null) {
            this._intBuilder = new IntBuilder();
        }
        return this._intBuilder;
    }
    
    public LongBuilder getLongBuilder() {
        if (this._longBuilder == null) {
            this._longBuilder = new LongBuilder();
        }
        return this._longBuilder;
    }
    
    public FloatBuilder getFloatBuilder() {
        if (this._floatBuilder == null) {
            this._floatBuilder = new FloatBuilder();
        }
        return this._floatBuilder;
    }
    
    public DoubleBuilder getDoubleBuilder() {
        if (this._doubleBuilder == null) {
            this._doubleBuilder = new DoubleBuilder();
        }
        return this._doubleBuilder;
    }
    
    public static <T> HashSet<T> arrayToSet(final T[] elements) {
        final HashSet<T> result = new HashSet<T>();
        if (elements != null) {
            for (final T elem : elements) {
                result.add(elem);
            }
        }
        return result;
    }
    
    public static <T> List<T> addToList(List<T> list, final T element) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        list.add(element);
        return list;
    }
    
    public static <T> T[] insertInList(final T[] array, final T element) {
        final int len = array.length;
        final T[] result = (T[])Array.newInstance(array.getClass().getComponentType(), len + 1);
        if (len > 0) {
            System.arraycopy(array, 0, result, 1, len);
        }
        result[0] = element;
        return result;
    }
    
    public static <T> T[] insertInListNoDup(final T[] array, final T element) {
        final int len = array.length;
        int ix = 0;
        while (ix < len) {
            if (array[ix] == element) {
                if (ix == 0) {
                    return array;
                }
                final T[] result = (T[])Array.newInstance(array.getClass().getComponentType(), len);
                System.arraycopy(array, 0, result, 1, ix);
                array[0] = element;
                return result;
            }
            else {
                ++ix;
            }
        }
        final T[] result2 = (T[])Array.newInstance(array.getClass().getComponentType(), len + 1);
        if (len > 0) {
            System.arraycopy(array, 0, result2, 1, len);
        }
        result2[0] = element;
        return result2;
    }
    
    public static <T> Iterator<T> arrayAsIterator(final T[] array) {
        return new ArrayIterator<T>(array);
    }
    
    public static <T> Iterable<T> arrayAsIterable(final T[] array) {
        return new ArrayIterator<T>(array);
    }
    
    public static final class BooleanBuilder extends PrimitiveArrayBuilder<boolean[]>
    {
        public final boolean[] _constructArray(final int len) {
            return new boolean[len];
        }
    }
    
    public static final class ByteBuilder extends PrimitiveArrayBuilder<byte[]>
    {
        public final byte[] _constructArray(final int len) {
            return new byte[len];
        }
    }
    
    public static final class ShortBuilder extends PrimitiveArrayBuilder<short[]>
    {
        public final short[] _constructArray(final int len) {
            return new short[len];
        }
    }
    
    public static final class IntBuilder extends PrimitiveArrayBuilder<int[]>
    {
        public final int[] _constructArray(final int len) {
            return new int[len];
        }
    }
    
    public static final class LongBuilder extends PrimitiveArrayBuilder<long[]>
    {
        public final long[] _constructArray(final int len) {
            return new long[len];
        }
    }
    
    public static final class FloatBuilder extends PrimitiveArrayBuilder<float[]>
    {
        public final float[] _constructArray(final int len) {
            return new float[len];
        }
    }
    
    public static final class DoubleBuilder extends PrimitiveArrayBuilder<double[]>
    {
        public final double[] _constructArray(final int len) {
            return new double[len];
        }
    }
    
    private static final class ArrayIterator<T> implements Iterator<T>, Iterable<T>
    {
        private final T[] _array;
        private int _index;
        
        public ArrayIterator(final T[] array) {
            this._array = array;
            this._index = 0;
        }
        
        public boolean hasNext() {
            return this._index < this._array.length;
        }
        
        public T next() {
            if (this._index >= this._array.length) {
                throw new NoSuchElementException();
            }
            return this._array[this._index++];
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        public Iterator<T> iterator() {
            return this;
        }
    }
}
