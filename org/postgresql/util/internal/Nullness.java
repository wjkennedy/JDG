// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util.internal;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.dataflow.qual.Pure;

public class Nullness
{
    @Pure
    @EnsuresNonNull({ "#1" })
    public static <T> T castNonNull(final T ref) {
        assert ref != null : "Misuse of castNonNull: called with a null argument";
        return ref;
    }
    
    @Pure
    @EnsuresNonNull({ "#1" })
    public static <T> T castNonNull(final T ref, final String message) {
        assert ref != null : "Misuse of castNonNull: called with a null argument " + message;
        return ref;
    }
}
