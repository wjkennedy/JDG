// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.regex.Matcher;
import org.postgresql.util.internal.Nullness;
import java.util.regex.Pattern;
import java.util.Properties;

public class ExpressionProperties extends Properties
{
    private static final Pattern EXPRESSION;
    private final Properties[] defaults;
    
    public ExpressionProperties(final Properties... defaults) {
        this.defaults = defaults;
    }
    
    @Override
    public String getProperty(final String key) {
        final String value = this.getRawPropertyValue(key);
        return this.replaceProperties(value);
    }
    
    @Override
    public String getProperty(final String key, final String defaultValue) {
        String value = this.getRawPropertyValue(key);
        if (value == null) {
            value = defaultValue;
        }
        return this.replaceProperties(value);
    }
    
    public String getRawPropertyValue(final String key) {
        String value = super.getProperty(key);
        if (value != null) {
            return value;
        }
        for (final Properties properties : this.defaults) {
            value = properties.getProperty(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
    
    private String replaceProperties(final String value) {
        if (value == null) {
            return null;
        }
        final Matcher matcher = ExpressionProperties.EXPRESSION.matcher(value);
        StringBuffer sb = null;
        while (matcher.find()) {
            if (sb == null) {
                sb = new StringBuffer();
            }
            String propValue = this.getProperty(Nullness.castNonNull(matcher.group(1)));
            if (propValue == null) {
                propValue = matcher.group();
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(propValue));
        }
        if (sb == null) {
            return value;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    static {
        EXPRESSION = Pattern.compile("\\$\\{([^}]+)\\}");
    }
}
