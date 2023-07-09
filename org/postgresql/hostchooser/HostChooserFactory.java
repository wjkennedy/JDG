// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.hostchooser;

import java.util.Properties;
import org.postgresql.util.HostSpec;

public class HostChooserFactory
{
    public static HostChooser createHostChooser(final HostSpec[] hostSpecs, final HostRequirement targetServerType, final Properties info) {
        if (hostSpecs.length == 1) {
            return new SingleHostChooser(hostSpecs[0], targetServerType);
        }
        return new MultiHostChooser(hostSpecs, targetServerType, info);
    }
}
