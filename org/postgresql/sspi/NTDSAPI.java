// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.sspi;

import com.sun.jna.Native;
import com.sun.jna.LastErrorException;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.WString;
import com.sun.jna.win32.StdCallLibrary;

interface NTDSAPI extends StdCallLibrary
{
    public static final NTDSAPI instance = (NTDSAPI)Native.loadLibrary("NTDSAPI", (Class)NTDSAPI.class);
    public static final int ERROR_SUCCESS = 0;
    public static final int ERROR_INVALID_PARAMETER = 87;
    public static final int ERROR_BUFFER_OVERFLOW = 111;
    
    int DsMakeSpnW(final WString p0, final WString p1, final WString p2, final short p3, final WString p4, final IntByReference p5, final char[] p6) throws LastErrorException;
}
