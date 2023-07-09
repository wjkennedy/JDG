// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import java.util.Map;
import org.postgresql.core.SqlCommand;
import org.postgresql.core.ParameterList;
import org.postgresql.core.NativeQuery;

public class BatchedQuery extends SimpleQuery
{
    private String sql;
    private final int valuesBraceOpenPosition;
    private final int valuesBraceClosePosition;
    private final int batchSize;
    private BatchedQuery[] blocks;
    
    public BatchedQuery(final NativeQuery query, final TypeTransferModeRegistry transferModeRegistry, final int valuesBraceOpenPosition, final int valuesBraceClosePosition, final boolean sanitiserDisabled) {
        super(query, transferModeRegistry, sanitiserDisabled);
        this.valuesBraceOpenPosition = valuesBraceOpenPosition;
        this.valuesBraceClosePosition = valuesBraceClosePosition;
        this.batchSize = 1;
    }
    
    private BatchedQuery(final BatchedQuery src, final int batchSize) {
        super(src);
        this.valuesBraceOpenPosition = src.valuesBraceOpenPosition;
        this.valuesBraceClosePosition = src.valuesBraceClosePosition;
        this.batchSize = batchSize;
    }
    
    public BatchedQuery deriveForMultiBatch(final int valueBlock) {
        if (this.getBatchSize() != 1) {
            throw new IllegalStateException("Only the original decorator can be derived.");
        }
        if (valueBlock == 1) {
            return this;
        }
        final int index = Integer.numberOfTrailingZeros(valueBlock) - 1;
        if (valueBlock > 128 || valueBlock != 1 << index + 1) {
            throw new IllegalArgumentException("Expected value block should be a power of 2 smaller or equal to 128. Actual block is " + valueBlock);
        }
        if (this.blocks == null) {
            this.blocks = new BatchedQuery[7];
        }
        BatchedQuery bq = this.blocks[index];
        if (bq == null) {
            bq = new BatchedQuery(this, valueBlock);
            this.blocks[index] = bq;
        }
        return bq;
    }
    
    @Override
    public int getBatchSize() {
        return this.batchSize;
    }
    
    @Override
    public String getNativeSql() {
        if (this.sql != null) {
            return this.sql;
        }
        return this.sql = this.buildNativeSql(null);
    }
    
    private String buildNativeSql(final ParameterList params) {
        String sql = null;
        final String nativeSql = super.getNativeSql();
        int batchSize = this.getBatchSize();
        if (batchSize < 2) {
            sql = nativeSql;
            return sql;
        }
        if (nativeSql == null) {
            sql = "";
            return sql;
        }
        int valuesBlockCharCount = 0;
        final int[] bindPositions = this.getNativeQuery().bindPositions;
        final int[] chunkStart = new int[1 + bindPositions.length];
        final int[] chunkEnd = new int[1 + bindPositions.length];
        chunkStart[0] = this.valuesBraceOpenPosition;
        if (bindPositions.length == 0) {
            valuesBlockCharCount = this.valuesBraceClosePosition - this.valuesBraceOpenPosition + 1;
            chunkEnd[0] = this.valuesBraceClosePosition + 1;
        }
        else {
            chunkEnd[0] = bindPositions[0];
            valuesBlockCharCount += chunkEnd[0] - chunkStart[0];
            for (int i = 0; i < bindPositions.length; ++i) {
                int startIndex;
                int endIndex;
                for (startIndex = bindPositions[i] + 2, endIndex = ((i < bindPositions.length - 1) ? bindPositions[i + 1] : (this.valuesBraceClosePosition + 1)); startIndex < endIndex && Character.isDigit(nativeSql.charAt(startIndex)); ++startIndex) {}
                chunkStart[i + 1] = startIndex;
                chunkEnd[i + 1] = endIndex;
                valuesBlockCharCount += chunkEnd[i + 1] - chunkStart[i + 1];
            }
        }
        int length = nativeSql.length();
        length += NativeQuery.calculateBindLength(bindPositions.length * batchSize);
        length -= NativeQuery.calculateBindLength(bindPositions.length);
        length += (valuesBlockCharCount + 1) * (batchSize - 1);
        final StringBuilder s = new StringBuilder(length);
        int pos;
        if (bindPositions.length > 0 && params == null) {
            s.append(nativeSql, 0, this.valuesBraceClosePosition + 1);
            pos = bindPositions.length + 1;
        }
        else {
            pos = 1;
            ++batchSize;
            s.append(nativeSql, 0, this.valuesBraceOpenPosition);
        }
        for (int j = 2; j <= batchSize; ++j) {
            if (j > 2 || pos != 1) {
                s.append(',');
            }
            s.append(nativeSql, chunkStart[0], chunkEnd[0]);
            for (int k = 1; k < chunkStart.length; ++k) {
                if (params == null) {
                    NativeQuery.appendBindName(s, pos++);
                }
                else {
                    s.append(params.toString(pos++, true));
                }
                s.append(nativeSql, chunkStart[k], chunkEnd[k]);
            }
        }
        s.append(nativeSql, this.valuesBraceClosePosition + 1, nativeSql.length());
        sql = s.toString();
        assert s.length() == length : "Predicted length != actual: " + length + " !=" + s.length();
        return sql;
    }
    
    @Override
    public String toString(final ParameterList params) {
        if (this.getBatchSize() < 2) {
            return super.toString(params);
        }
        return this.buildNativeSql(params);
    }
}
