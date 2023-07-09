// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

public class NativeQuery
{
    private static final String[] BIND_NAMES;
    private static final int[] NO_BINDS;
    public final String nativeSql;
    public final int[] bindPositions;
    public final SqlCommand command;
    public final boolean multiStatement;
    
    public NativeQuery(final String nativeSql, final SqlCommand dml) {
        this(nativeSql, NativeQuery.NO_BINDS, true, dml);
    }
    
    public NativeQuery(final String nativeSql, final int[] bindPositions, final boolean multiStatement, final SqlCommand dml) {
        this.nativeSql = nativeSql;
        this.bindPositions = ((bindPositions == null || bindPositions.length == 0) ? NativeQuery.NO_BINDS : bindPositions);
        this.multiStatement = multiStatement;
        this.command = dml;
    }
    
    public String toString(final ParameterList parameters) {
        if (this.bindPositions.length == 0) {
            return this.nativeSql;
        }
        int queryLength = this.nativeSql.length();
        final String[] params = new String[this.bindPositions.length];
        for (int i = 1; i <= this.bindPositions.length; ++i) {
            final String param = (parameters == null) ? "?" : parameters.toString(i, true);
            params[i - 1] = param;
            queryLength += param.length() - bindName(i).length();
        }
        final StringBuilder sbuf = new StringBuilder(queryLength);
        sbuf.append(this.nativeSql, 0, this.bindPositions[0]);
        for (int j = 1; j <= this.bindPositions.length; ++j) {
            sbuf.append(params[j - 1]);
            final int nextBind = (j < this.bindPositions.length) ? this.bindPositions[j] : this.nativeSql.length();
            sbuf.append(this.nativeSql, this.bindPositions[j - 1] + bindName(j).length(), nextBind);
        }
        return sbuf.toString();
    }
    
    public static String bindName(final int index) {
        return (index < NativeQuery.BIND_NAMES.length) ? NativeQuery.BIND_NAMES[index] : ("$" + index);
    }
    
    public static StringBuilder appendBindName(final StringBuilder sb, final int index) {
        if (index < NativeQuery.BIND_NAMES.length) {
            return sb.append(bindName(index));
        }
        sb.append('$');
        sb.append(index);
        return sb;
    }
    
    public static int calculateBindLength(int bindCount) {
        int res = 0;
        int numBinds;
        for (int bindLen = 2, maxBindsOfLen = 9; bindCount > 0; bindCount -= numBinds, res += bindLen * numBinds, ++bindLen, maxBindsOfLen *= 10) {
            numBinds = Math.min(maxBindsOfLen, bindCount);
        }
        return res;
    }
    
    public SqlCommand getCommand() {
        return this.command;
    }
    
    static {
        BIND_NAMES = new String[1280];
        NO_BINDS = new int[0];
        for (int i = 1; i < NativeQuery.BIND_NAMES.length; ++i) {
            NativeQuery.BIND_NAMES[i] = "$" + i;
        }
    }
}
