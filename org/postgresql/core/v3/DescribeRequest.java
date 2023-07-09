// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3;

class DescribeRequest
{
    public final SimpleQuery query;
    public final SimpleParameterList parameterList;
    public final boolean describeOnly;
    public final String statementName;
    
    DescribeRequest(final SimpleQuery query, final SimpleParameterList parameterList, final boolean describeOnly, final String statementName) {
        this.query = query;
        this.parameterList = parameterList;
        this.describeOnly = describeOnly;
        this.statementName = statementName;
    }
}
