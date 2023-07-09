// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostSpec
{
    public static final String DEFAULT_NON_PROXY_HOSTS = "localhost|127.*|[::1]|0.0.0.0|[::0]";
    protected final String host;
    protected final int port;
    
    public HostSpec(final String host, final int port) {
        this.host = host;
        this.port = port;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public int getPort() {
        return this.port;
    }
    
    @Override
    public String toString() {
        return this.host + ":" + this.port;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof HostSpec && this.port == ((HostSpec)obj).port && this.host.equals(((HostSpec)obj).host);
    }
    
    @Override
    public int hashCode() {
        return this.port ^ this.host.hashCode();
    }
    
    public Boolean shouldResolve() {
        final String socksProxy = System.getProperty("socksProxyHost");
        if (socksProxy == null || socksProxy.trim().isEmpty()) {
            return true;
        }
        return this.matchesNonProxyHosts();
    }
    
    private Boolean matchesNonProxyHosts() {
        final String nonProxyHosts = System.getProperty("socksNonProxyHosts", "localhost|127.*|[::1]|0.0.0.0|[::0]");
        if (nonProxyHosts == null || this.host.isEmpty()) {
            return false;
        }
        final Pattern pattern = this.toPattern(nonProxyHosts);
        final Matcher matcher = (pattern == null) ? null : pattern.matcher(this.host);
        return matcher != null && matcher.matches();
    }
    
    private Pattern toPattern(final String mask) {
        final StringBuilder joiner = new StringBuilder();
        String separator = "";
        for (final String disjunct : mask.split("\\|")) {
            if (!disjunct.isEmpty()) {
                final String regex = this.disjunctToRegex(disjunct.toLowerCase());
                joiner.append(separator).append(regex);
                separator = "|";
            }
        }
        return (joiner.length() == 0) ? null : Pattern.compile(joiner.toString());
    }
    
    private String disjunctToRegex(final String disjunct) {
        String regex;
        if (disjunct.startsWith("*")) {
            regex = ".*" + Pattern.quote(disjunct.substring(1));
        }
        else if (disjunct.endsWith("*")) {
            regex = Pattern.quote(disjunct.substring(0, disjunct.length() - 1)) + ".*";
        }
        else {
            regex = Pattern.quote(disjunct);
        }
        return regex;
    }
}
