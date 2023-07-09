// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

enum StatementCancelState
{
    IDLE, 
    IN_QUERY, 
    CANCELING, 
    CANCELLED;
}
