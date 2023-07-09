// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.text.ParsePosition;
import java.text.NumberFormat;

public enum ServerVersion implements Version
{
    INVALID("0.0.0"), 
    v8_2("8.2.0"), 
    v8_3("8.3.0"), 
    v8_4("8.4.0"), 
    v9_0("9.0.0"), 
    v9_1("9.1.0"), 
    v9_2("9.2.0"), 
    v9_3("9.3.0"), 
    v9_4("9.4.0"), 
    v9_5("9.5.0"), 
    v9_6("9.6.0"), 
    v10("10"), 
    v11("11"), 
    v12("12"), 
    v13("13"), 
    v14("14");
    
    private final int version;
    
    private ServerVersion(final String version) {
        this.version = parseServerVersionStr(version);
    }
    
    @Override
    public int getVersionNum() {
        return this.version;
    }
    
    public static Version from(final String version) {
        final int versionNum = parseServerVersionStr(version);
        return new Version() {
            @Override
            public int getVersionNum() {
                return versionNum;
            }
            
            @Override
            public boolean equals(final Object obj) {
                return obj instanceof Version && this.getVersionNum() == ((Version)obj).getVersionNum();
            }
            
            @Override
            public int hashCode() {
                return this.getVersionNum();
            }
            
            @Override
            public String toString() {
                return Integer.toString(versionNum);
            }
        };
    }
    
    static int parseServerVersionStr(final String serverVersion) throws NumberFormatException {
        if (serverVersion == null) {
            return 0;
        }
        final NumberFormat numformat = NumberFormat.getIntegerInstance();
        numformat.setGroupingUsed(false);
        final ParsePosition parsepos = new ParsePosition(0);
        final int[] parts = new int[3];
        int versionParts;
        for (versionParts = 0; versionParts < 3; ++versionParts) {
            final Number part = (Number)numformat.parseObject(serverVersion, parsepos);
            if (part == null) {
                break;
            }
            parts[versionParts] = part.intValue();
            if (parsepos.getIndex() == serverVersion.length()) {
                break;
            }
            if (serverVersion.charAt(parsepos.getIndex()) != '.') {
                break;
            }
            parsepos.setIndex(parsepos.getIndex() + 1);
        }
        ++versionParts;
        if (parts[0] >= 10000) {
            if (parsepos.getIndex() == serverVersion.length() && versionParts == 1) {
                return parts[0];
            }
            throw new NumberFormatException("First major-version part equal to or greater than 10000 in invalid version string: " + serverVersion);
        }
        else if (versionParts >= 3) {
            if (parts[1] > 99) {
                throw new NumberFormatException("Unsupported second part of major version > 99 in invalid version string: " + serverVersion);
            }
            if (parts[2] > 99) {
                throw new NumberFormatException("Unsupported second part of minor version > 99 in invalid version string: " + serverVersion);
            }
            return (parts[0] * 100 + parts[1]) * 100 + parts[2];
        }
        else if (versionParts == 2) {
            if (parts[0] >= 10) {
                return parts[0] * 100 * 100 + parts[1];
            }
            if (parts[1] > 99) {
                throw new NumberFormatException("Unsupported second part of major version > 99 in invalid version string: " + serverVersion);
            }
            return (parts[0] * 100 + parts[1]) * 100;
        }
        else {
            if (versionParts == 1 && parts[0] >= 10) {
                return parts[0] * 100 * 100;
            }
            return 0;
        }
    }
}
