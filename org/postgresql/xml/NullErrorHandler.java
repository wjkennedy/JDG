// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xml;

import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

public class NullErrorHandler implements ErrorHandler
{
    public static final NullErrorHandler INSTANCE;
    
    @Override
    public void error(final SAXParseException e) {
    }
    
    @Override
    public void fatalError(final SAXParseException e) {
    }
    
    @Override
    public void warning(final SAXParseException e) {
    }
    
    static {
        INSTANCE = new NullErrorHandler();
    }
}
