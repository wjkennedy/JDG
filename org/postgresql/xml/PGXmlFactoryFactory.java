// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.xml;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

public interface PGXmlFactoryFactory
{
    DocumentBuilder newDocumentBuilder() throws ParserConfigurationException;
    
    TransformerFactory newTransformerFactory();
    
    SAXTransformerFactory newSAXTransformerFactory();
    
    XMLInputFactory newXMLInputFactory();
    
    XMLOutputFactory newXMLOutputFactory();
    
    XMLReader createXMLReader() throws SAXException;
}
