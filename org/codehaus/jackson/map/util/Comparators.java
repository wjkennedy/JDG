// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.lang.reflect.Array;

public class Comparators
{
    public static Object getArrayComparator(final Object defaultValue) {
        final int length = Array.getLength(defaultValue);
        return new Object() {
            @Override
            public boolean equals(final Object other) {
                if (other == this) {
                    return true;
                }
                if (other == null || other.getClass() != defaultValue.getClass()) {
                    return false;
                }
                if (Array.getLength(other) != length) {
                    return false;
                }
                for (int i = 0; i < length; ++i) {
                    final Object value1 = Array.get(defaultValue, i);
                    final Object value2 = Array.get(other, i);
                    if (value1 != value2) {
                        if (value1 != null && !value1.equals(value2)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };
    }
}
