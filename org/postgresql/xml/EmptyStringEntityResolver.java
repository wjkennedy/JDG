// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xml;

import java.io.IOException;
import org.xml.sax.SAXException;
import java.io.Reader;
import java.io.StringReader;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;

public class EmptyStringEntityResolver implements EntityResolver
{
    public static final EmptyStringEntityResolver INSTANCE;
    
    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
        return new InputSource(new StringReader(""));
    }
    
    static {
        INSTANCE = new EmptyStringEntityResolver();
    }
}
