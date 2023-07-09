// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ext;

import org.w3c.dom.Node;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Reader;
import org.xml.sax.InputSource;
import java.io.StringReader;
import org.w3c.dom.Document;
import org.codehaus.jackson.map.DeserializationContext;
import javax.xml.parsers.DocumentBuilderFactory;
import org.codehaus.jackson.map.deser.std.FromStringDeserializer;

public abstract class DOMDeserializer<T> extends FromStringDeserializer<T>
{
    static final DocumentBuilderFactory _parserFactory;
    
    protected DOMDeserializer(final Class<T> cls) {
        super(cls);
    }
    
    public abstract T _deserialize(final String p0, final DeserializationContext p1);
    
    protected final Document parse(final String value) throws IllegalArgumentException {
        try {
            return DOMDeserializer._parserFactory.newDocumentBuilder().parse(new InputSource(new StringReader(value)));
        }
        catch (final Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON String as XML: " + e.getMessage(), e);
        }
    }
    
    static {
        (_parserFactory = DocumentBuilderFactory.newInstance()).setNamespaceAware(true);
        DOMDeserializer._parserFactory.setExpandEntityReferences(false);
        try {
            DOMDeserializer._parserFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        }
        catch (final ParserConfigurationException pce) {
            System.err.println("[DOMDeserializer] Problem setting SECURE_PROCESSING_FEATURE: " + pce.toString());
        }
    }
    
    public static class NodeDeserializer extends DOMDeserializer<Node>
    {
        public NodeDeserializer() {
            super(Node.class);
        }
        
        @Override
        public Node _deserialize(final String value, final DeserializationContext ctxt) throws IllegalArgumentException {
            return this.parse(value);
        }
    }
    
    public static class DocumentDeserializer extends DOMDeserializer<Document>
    {
        public DocumentDeserializer() {
            super(Document.class);
        }
        
        @Override
        public Document _deserialize(final String value, final DeserializationContext ctxt) throws IllegalArgumentException {
            return this.parse(value);
        }
    }
}
