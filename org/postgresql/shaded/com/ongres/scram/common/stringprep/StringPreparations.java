// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.stringprep;

import org.postgresql.shaded.com.ongres.saslprep.SaslPrep;
import org.postgresql.shaded.com.ongres.scram.common.util.UsAsciiUtils;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public enum StringPreparations implements StringPreparation
{
    NO_PREPARATION {
        @Override
        protected String doNormalize(final String value) throws IllegalArgumentException {
            return UsAsciiUtils.toPrintable(value);
        }
    }, 
    SASL_PREPARATION {
        @Override
        protected String doNormalize(final String value) throws IllegalArgumentException {
            return SaslPrep.saslPrep(value, true);
        }
    };
    
    protected abstract String doNormalize(final String p0) throws IllegalArgumentException;
    
    @Override
    public String normalize(final String value) throws IllegalArgumentException {
        Preconditions.checkNotEmpty(value, "value");
        final String normalized = this.doNormalize(value);
        if (null == normalized || normalized.isEmpty()) {
            throw new IllegalArgumentException("null or empty value after normalization");
        }
        return normalized;
    }
}
