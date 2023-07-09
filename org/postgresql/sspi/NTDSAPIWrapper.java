// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.sspi;

import com.sun.jna.LastErrorException;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class NTDSAPIWrapper
{
    static final NTDSAPIWrapper instance;
    
    public String DsMakeSpn(final String serviceClass, final String serviceName, final String instanceName, final short instancePort, final String referrer) throws LastErrorException {
        final IntByReference spnLength = new IntByReference(2048);
        final char[] spn = new char[spnLength.getValue()];
        final int ret = NTDSAPI.instance.DsMakeSpnW(new WString(serviceClass), new WString(serviceName), (instanceName == null) ? null : new WString(instanceName), instancePort, (referrer == null) ? null : new WString(referrer), spnLength, spn);
        if (ret != 0) {
            throw new RuntimeException("NTDSAPI DsMakeSpn call failed with " + ret);
        }
        return new String(spn, 0, spnLength.getValue());
    }
    
    static {
        instance = new NTDSAPIWrapper();
    }
}
