// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

public final class Base64Variants
{
    static final String STD_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    public static final Base64Variant MIME;
    public static final Base64Variant MIME_NO_LINEFEEDS;
    public static final Base64Variant PEM;
    public static final Base64Variant MODIFIED_FOR_URL;
    
    public static Base64Variant getDefaultVariant() {
        return Base64Variants.MIME_NO_LINEFEEDS;
    }
    
    static {
        MIME = new Base64Variant("MIME", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", true, '=', 76);
        MIME_NO_LINEFEEDS = new Base64Variant(Base64Variants.MIME, "MIME-NO-LINEFEEDS", Integer.MAX_VALUE);
        PEM = new Base64Variant(Base64Variants.MIME, "PEM", true, '=', 64);
        final StringBuffer sb = new StringBuffer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
        sb.setCharAt(sb.indexOf("+"), '-');
        sb.setCharAt(sb.indexOf("/"), '_');
        MODIFIED_FOR_URL = new Base64Variant("MODIFIED-FOR-URL", sb.toString(), false, '\0', Integer.MAX_VALUE);
    }
}
