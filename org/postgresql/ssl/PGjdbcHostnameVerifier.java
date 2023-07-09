// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.ssl;

import java.util.Iterator;
import java.util.Collection;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.util.List;
import java.security.cert.CertificateParsingException;
import java.util.Collections;
import java.net.IDN;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.postgresql.util.GT;
import java.util.logging.Level;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import java.util.Comparator;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;

public class PGjdbcHostnameVerifier implements HostnameVerifier
{
    private static final Logger LOGGER;
    public static final PGjdbcHostnameVerifier INSTANCE;
    private static final int TYPE_DNS_NAME = 2;
    private static final int TYPE_IP_ADDRESS = 7;
    public static final Comparator<String> HOSTNAME_PATTERN_COMPARATOR;
    
    @Override
    public boolean verify(final String hostname, final SSLSession session) {
        X509Certificate[] peerCerts;
        try {
            peerCerts = (X509Certificate[])session.getPeerCertificates();
        }
        catch (final SSLPeerUnverifiedException e) {
            PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("Unable to parse X509Certificate for hostname {0}", hostname), e);
            return false;
        }
        if (peerCerts == null || peerCerts.length == 0) {
            PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("No certificates found for hostname {0}", hostname));
            return false;
        }
        String canonicalHostname;
        if (hostname.startsWith("[") && hostname.endsWith("]")) {
            canonicalHostname = hostname.substring(1, hostname.length() - 1);
        }
        else {
            try {
                canonicalHostname = IDN.toASCII(hostname);
                if (PGjdbcHostnameVerifier.LOGGER.isLoggable(Level.FINEST)) {
                    PGjdbcHostnameVerifier.LOGGER.log(Level.FINEST, "Canonical host name for {0} is {1}", new Object[] { hostname, canonicalHostname });
                }
            }
            catch (final IllegalArgumentException e2) {
                PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("Hostname {0} is invalid", hostname), e2);
                return false;
            }
        }
        final X509Certificate serverCert = peerCerts[0];
        Collection<List<?>> subjectAltNames;
        try {
            subjectAltNames = serverCert.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                subjectAltNames = (Collection<List<?>>)Collections.emptyList();
            }
        }
        catch (final CertificateParsingException e3) {
            PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("Unable to parse certificates for hostname {0}", hostname), e3);
            return false;
        }
        boolean anyDnsSan = false;
        for (final List<?> sanItem : subjectAltNames) {
            if (sanItem.size() != 2) {
                continue;
            }
            final Integer sanType = (Integer)sanItem.get(0);
            if (sanType == null) {
                continue;
            }
            if (sanType != 7 && sanType != 2) {
                continue;
            }
            final String san = (String)sanItem.get(1);
            if (sanType == 7 && san != null && san.startsWith("*")) {
                continue;
            }
            anyDnsSan |= (sanType == 2);
            if (this.verifyHostName(canonicalHostname, san)) {
                if (PGjdbcHostnameVerifier.LOGGER.isLoggable(Level.FINEST)) {
                    PGjdbcHostnameVerifier.LOGGER.log(Level.FINEST, GT.tr("Server name validation pass for {0}, subjectAltName {1}", hostname, san));
                }
                return true;
            }
        }
        if (anyDnsSan) {
            PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: certificate for host {0} dNSName entries subjectAltName, but none of them match. Assuming server name validation failed", hostname));
            return false;
        }
        LdapName dn;
        try {
            dn = new LdapName(serverCert.getSubjectX500Principal().getName("RFC2253"));
        }
        catch (final InvalidNameException e4) {
            PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: unable to extract common name from X509Certificate for hostname {0}", hostname), e4);
            return false;
        }
        final List<String> commonNames = new ArrayList<String>(1);
        for (final Rdn rdn : dn.getRdns()) {
            if ("CN".equals(rdn.getType())) {
                commonNames.add((String)rdn.getValue());
            }
        }
        if (commonNames.isEmpty()) {
            PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: certificate for hostname {0} has no DNS subjectAltNames, and it CommonName is missing as well", hostname));
            return false;
        }
        if (commonNames.size() > 1) {
            Collections.sort(commonNames, PGjdbcHostnameVerifier.HOSTNAME_PATTERN_COMPARATOR);
        }
        final String commonName = commonNames.get(commonNames.size() - 1);
        final boolean result = this.verifyHostName(canonicalHostname, commonName);
        if (!result) {
            PGjdbcHostnameVerifier.LOGGER.log(Level.SEVERE, GT.tr("Server name validation failed: hostname {0} does not match common name {1}", hostname, commonName));
        }
        return result;
    }
    
    public boolean verifyHostName(final String hostname, final String pattern) {
        if (hostname == null || pattern == null) {
            return false;
        }
        final int lastStar = pattern.lastIndexOf(42);
        if (lastStar == -1) {
            return hostname.equalsIgnoreCase(pattern);
        }
        if (lastStar > 0) {
            return false;
        }
        if (pattern.indexOf(46) == -1) {
            return false;
        }
        if (hostname.length() < pattern.length() - 1) {
            return false;
        }
        final boolean ignoreCase = true;
        final int toffset = hostname.length() - pattern.length() + 1;
        return hostname.lastIndexOf(46, toffset - 1) < 0 && hostname.regionMatches(true, toffset, pattern, 1, pattern.length() - 1);
    }
    
    static {
        LOGGER = Logger.getLogger(PGjdbcHostnameVerifier.class.getName());
        INSTANCE = new PGjdbcHostnameVerifier();
        HOSTNAME_PATTERN_COMPARATOR = new Comparator<String>() {
            private int countChars(final String value, final char ch) {
                int count = 0;
                int pos = -1;
                while (true) {
                    pos = value.indexOf(ch, pos + 1);
                    if (pos == -1) {
                        break;
                    }
                    ++count;
                }
                return count;
            }
            
            @Override
            public int compare(final String o1, final String o2) {
                final int d1 = this.countChars(o1, '.');
                final int d2 = this.countChars(o2, '.');
                if (d1 != d2) {
                    return (d1 > d2) ? 1 : -1;
                }
                final int s1 = this.countChars(o1, '*');
                final int s2 = this.countChars(o2, '*');
                if (s1 != s2) {
                    return (s1 < s2) ? 1 : -1;
                }
                final int l1 = o1.length();
                final int l2 = o2.length();
                if (l1 != l2) {
                    return (l1 > l2) ? 1 : -1;
                }
                return 0;
            }
        };
    }
}
