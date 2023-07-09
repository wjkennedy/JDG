// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.nullness;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import java.util.NoSuchElementException;

public final class Opt
{
    private Opt() {
        throw new AssertionError((Object)"shouldn't be instantiated");
    }
    
    public static <T> T get(final T primary) {
        if (primary == null) {
            throw new NoSuchElementException("No value present");
        }
        return primary;
    }
    
    @EnsuresNonNullIf(expression = { "#1" }, result = true)
    public static boolean isPresent(final Object primary) {
        return primary != null;
    }
    
    public static <T> void ifPresent(final T primary, final Consumer<? super T> consumer) {
        if (primary != null) {
            consumer.accept(primary);
        }
    }
    
    public static <T> T filter(final T primary, final Predicate<? super T> predicate) {
        if (primary == null) {
            return null;
        }
        return predicate.test(primary) ? primary : null;
    }
    
    public static <T, U> U map(final T primary, final Function<? super T, ? extends U> mapper) {
        if (primary == null) {
            return null;
        }
        return (U)mapper.apply(primary);
    }
    
    public static <T> T orElse(final T primary, final T other) {
        return (primary != null) ? primary : other;
    }
    
    public static <T> T orElseGet(final T primary, final Supplier<? extends T> other) {
        return (primary != null) ? primary : other.get();
    }
    
    public static <T, X extends Throwable> T orElseThrow(final T primary, final Supplier<? extends X> exceptionSupplier) throws X, Throwable {
        if (primary != null) {
            return primary;
        }
        throw (Throwable)exceptionSupplier.get();
    }
}
