// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.codehaus.jackson.Version;
import java.util.regex.Pattern;

public class VersionUtil
{
    public static final String VERSION_FILE = "VERSION.txt";
    private static final Pattern VERSION_SEPARATOR;
    
    public static Version versionFor(final Class<?> cls) {
        Version version = null;
        try {
            final InputStream in = cls.getResourceAsStream("VERSION.txt");
            if (in != null) {
                try {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    version = parseVersion(br.readLine());
                }
                finally {
                    try {
                        in.close();
                    }
                    catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        catch (final IOException ex) {}
        return (version == null) ? Version.unknownVersion() : version;
    }
    
    public static Version parseVersion(String versionStr) {
        if (versionStr == null) {
            return null;
        }
        versionStr = versionStr.trim();
        if (versionStr.length() == 0) {
            return null;
        }
        final String[] parts = VersionUtil.VERSION_SEPARATOR.split(versionStr);
        if (parts.length < 2) {
            return null;
        }
        final int major = parseVersionPart(parts[0]);
        final int minor = parseVersionPart(parts[1]);
        final int patch = (parts.length > 2) ? parseVersionPart(parts[2]) : 0;
        final String snapshot = (parts.length > 3) ? parts[3] : null;
        return new Version(major, minor, patch, snapshot);
    }
    
    protected static int parseVersionPart(String partStr) {
        partStr = partStr.toString();
        final int len = partStr.length();
        int number = 0;
        for (int i = 0; i < len; ++i) {
            final char c = partStr.charAt(i);
            if (c > '9') {
                break;
            }
            if (c < '0') {
                break;
            }
            number = number * 10 + (c - '0');
        }
        return number;
    }
    
    static {
        VERSION_SEPARATOR = Pattern.compile("[-_./;:]");
    }
}
