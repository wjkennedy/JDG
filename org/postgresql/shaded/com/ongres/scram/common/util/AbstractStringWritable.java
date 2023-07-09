// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.util;

public abstract class AbstractStringWritable implements StringWritable
{
    @Override
    public String toString() {
        return this.writeTo(new StringBuffer()).toString();
    }
}
