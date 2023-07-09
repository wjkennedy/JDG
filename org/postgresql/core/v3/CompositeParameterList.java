// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

import org.postgresql.core.ParameterList;
import org.postgresql.util.ByteStreamWriter;
import java.io.InputStream;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;

class CompositeParameterList implements V3ParameterList
{
    private final int total;
    private final SimpleParameterList[] subparams;
    private final int[] offsets;
    
    CompositeParameterList(final SimpleParameterList[] subparams, final int[] offsets) {
        this.subparams = subparams;
        this.offsets = offsets;
        this.total = offsets[offsets.length - 1] + subparams[offsets.length - 1].getInParameterCount();
    }
    
    private int findSubParam(final int index) throws SQLException {
        if (index < 1 || index > this.total) {
            throw new PSQLException(GT.tr("The column index is out of range: {0}, number of columns: {1}.", index, this.total), PSQLState.INVALID_PARAMETER_VALUE);
        }
        for (int i = this.offsets.length - 1; i >= 0; --i) {
            if (this.offsets[i] < index) {
                return i;
            }
        }
        throw new IllegalArgumentException("I am confused; can't find a subparam for index " + index);
    }
    
    @Override
    public void registerOutParameter(final int index, final int sqlType) {
    }
    
    public int getDirection(final int i) {
        return 0;
    }
    
    @Override
    public int getParameterCount() {
        return this.total;
    }
    
    @Override
    public int getInParameterCount() {
        return this.total;
    }
    
    @Override
    public int getOutParameterCount() {
        return 0;
    }
    
    @Override
    public int[] getTypeOIDs() {
        final int[] oids = new int[this.total];
        for (int i = 0; i < this.offsets.length; ++i) {
            final int[] subOids = this.subparams[i].getTypeOIDs();
            System.arraycopy(subOids, 0, oids, this.offsets[i], subOids.length);
        }
        return oids;
    }
    
    @Override
    public void setIntParameter(final int index, final int value) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setIntParameter(index - this.offsets[sub], value);
    }
    
    @Override
    public void setLiteralParameter(final int index, final String value, final int oid) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setStringParameter(index - this.offsets[sub], value, oid);
    }
    
    @Override
    public void setStringParameter(final int index, final String value, final int oid) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setStringParameter(index - this.offsets[sub], value, oid);
    }
    
    @Override
    public void setBinaryParameter(final int index, final byte[] value, final int oid) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setBinaryParameter(index - this.offsets[sub], value, oid);
    }
    
    @Override
    public void setBytea(final int index, final byte[] data, final int offset, final int length) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], data, offset, length);
    }
    
    @Override
    public void setBytea(final int index, final InputStream stream, final int length) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], stream, length);
    }
    
    @Override
    public void setBytea(final int index, final InputStream stream) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], stream);
    }
    
    @Override
    public void setBytea(final int index, final ByteStreamWriter writer) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setBytea(index - this.offsets[sub], writer);
    }
    
    @Override
    public void setText(final int index, final InputStream stream) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setText(index - this.offsets[sub], stream);
    }
    
    @Override
    public void setNull(final int index, final int oid) throws SQLException {
        final int sub = this.findSubParam(index);
        this.subparams[sub].setNull(index - this.offsets[sub], oid);
    }
    
    @Override
    public String toString(final int index, final boolean standardConformingStrings) {
        try {
            final int sub = this.findSubParam(index);
            return this.subparams[sub].toString(index - this.offsets[sub], standardConformingStrings);
        }
        catch (final SQLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    
    @Override
    public ParameterList copy() {
        final SimpleParameterList[] copySub = new SimpleParameterList[this.subparams.length];
        for (int sub = 0; sub < this.subparams.length; ++sub) {
            copySub[sub] = (SimpleParameterList)this.subparams[sub].copy();
        }
        return new CompositeParameterList(copySub, this.offsets);
    }
    
    @Override
    public void clear() {
        for (final SimpleParameterList subparam : this.subparams) {
            subparam.clear();
        }
    }
    
    @Override
    public SimpleParameterList[] getSubparams() {
        return this.subparams;
    }
    
    @Override
    public void checkAllParametersSet() throws SQLException {
        for (final SimpleParameterList subparam : this.subparams) {
            subparam.checkAllParametersSet();
        }
    }
    
    @Override
    public byte[][] getEncoding() {
        return null;
    }
    
    @Override
    public byte[] getFlags() {
        return null;
    }
    
    @Override
    public int[] getParamTypes() {
        return null;
    }
    
    @Override
    public Object[] getValues() {
        return null;
    }
    
    @Override
    public void appendAll(final ParameterList list) throws SQLException {
    }
    
    @Override
    public void convertFunctionOutParameters() {
        for (final SimpleParameterList subparam : this.subparams) {
            subparam.convertFunctionOutParameters();
        }
    }
}
