// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.hostchooser;

public enum HostRequirement
{
    any {
        @Override
        public boolean allowConnectingTo(final HostStatus status) {
            return status != HostStatus.ConnectFail;
        }
    }, 
    @Deprecated
    master {
        @Override
        public boolean allowConnectingTo(final HostStatus status) {
            return HostRequirement$2.primary.allowConnectingTo(status);
        }
    }, 
    primary {
        @Override
        public boolean allowConnectingTo(final HostStatus status) {
            return status == HostStatus.Primary || status == HostStatus.ConnectOK;
        }
    }, 
    secondary {
        @Override
        public boolean allowConnectingTo(final HostStatus status) {
            return status == HostStatus.Secondary || status == HostStatus.ConnectOK;
        }
    }, 
    preferSecondary {
        @Override
        public boolean allowConnectingTo(final HostStatus status) {
            return status != HostStatus.ConnectFail;
        }
    };
    
    public abstract boolean allowConnectingTo(final HostStatus p0);
    
    public static HostRequirement getTargetServerType(final String targetServerType) {
        final String allowSlave = targetServerType.replace("lave", "econdary").replace("master", "primary");
        return valueOf(allowSlave);
    }
}
