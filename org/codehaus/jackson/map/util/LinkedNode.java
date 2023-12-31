// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

public final class LinkedNode<T>
{
    final T _value;
    final LinkedNode<T> _next;
    
    public LinkedNode(final T value, final LinkedNode<T> next) {
        this._value = value;
        this._next = next;
    }
    
    public LinkedNode<T> next() {
        return this._next;
    }
    
    public T value() {
        return this._value;
    }
    
    public static <ST> boolean contains(LinkedNode<ST> node, final ST value) {
        while (node != null) {
            if (node.value() == value) {
                return true;
            }
            node = node.next();
        }
        return false;
    }
}
