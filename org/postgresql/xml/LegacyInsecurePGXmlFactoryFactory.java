// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.ErrorHandler;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

public class LegacyInsecurePGXmlFactoryFactory implements PGXmlFactoryFactory
{
    public static final LegacyInsecurePGXmlFactoryFactory INSTANCE;
    
    private LegacyInsecurePGXmlFactoryFactory() {
    }
    
    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setErrorHandler(NullErrorHandler.INSTANCE);
        return builder;
    }
    
    @Override
    public TransformerFactory newTransformerFactory() {
        return TransformerFactory.newInstance();
    }
    
    @Override
    public SAXTransformerFactory newSAXTransformerFactory() {
        return (SAXTransformerFactory)TransformerFactory.newInstance();
    }
    
    @Override
    public XMLInputFactory newXMLInputFactory() {
        return XMLInputFactory.newInstance();
    }
    
    @Override
    public XMLOutputFactory newXMLOutputFactory() {
        return XMLOutputFactory.newInstance();
    }
    
    @Override
    public XMLReader createXMLReader() throws SAXException {
        return XMLReaderFactory.createXMLReader();
    }
    
    static {
        INSTANCE = new LegacyInsecurePGXmlFactoryFactory();
    }
}
