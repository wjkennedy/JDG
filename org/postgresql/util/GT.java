// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Locale;
import org.checkerframework.dataflow.qual.Pure;
import java.util.ResourceBundle;

public class GT
{
    private static final GT _gt;
    private static final Object[] noargs;
    private ResourceBundle bundle;
    
    @Pure
    public static String tr(final String message, final Object... args) {
        return GT._gt.translate(message, args);
    }
    
    private GT() {
        try {
            this.bundle = ResourceBundle.getBundle("org.postgresql.translation.messages");
            this.bundle = ResourceBundle.getBundle("org.postgresql.translation.messages", Locale.getDefault(Locale.Category.DISPLAY));
        }
        catch (final MissingResourceException mre) {
            this.bundle = null;
        }
    }
    
    private String translate(String message, Object[] args) {
        if (this.bundle != null && message != null) {
            try {
                message = this.bundle.getString(message);
            }
            catch (final MissingResourceException ex) {}
        }
        if (args == null) {
            args = GT.noargs;
        }
        if (message != null) {
            message = MessageFormat.format(message, args);
        }
        return message;
    }
    
    static {
        _gt = new GT();
        noargs = new Object[0];
    }
}
