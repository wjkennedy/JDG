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
import org.xml.sax.EntityResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DefaultPGXmlFactoryFactory implements PGXmlFactoryFactory
{
    public static final DefaultPGXmlFactoryFactory INSTANCE;
    
    private DefaultPGXmlFactoryFactory() {
    }
    
    private DocumentBuilderFactory getDocumentBuilderFactory() {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        setFactoryProperties(factory);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }
    
    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilder builder = this.getDocumentBuilderFactory().newDocumentBuilder();
        builder.setEntityResolver(EmptyStringEntityResolver.INSTANCE);
        builder.setErrorHandler(NullErrorHandler.INSTANCE);
        return builder;
    }
    
    @Override
    public TransformerFactory newTransformerFactory() {
        final TransformerFactory factory = TransformerFactory.newInstance();
        setFactoryProperties(factory);
        return factory;
    }
    
    @Override
    public SAXTransformerFactory newSAXTransformerFactory() {
        final SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();
        setFactoryProperties(factory);
        return factory;
    }
    
    @Override
    public XMLInputFactory newXMLInputFactory() {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        setPropertyQuietly(factory, "javax.xml.stream.supportDTD", false);
        setPropertyQuietly(factory, "javax.xml.stream.isSupportingExternalEntities", false);
        return factory;
    }
    
    @Override
    public XMLOutputFactory newXMLOutputFactory() {
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        return factory;
    }
    
    @Override
    public XMLReader createXMLReader() throws SAXException {
        final XMLReader factory = XMLReaderFactory.createXMLReader();
        setFeatureQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeatureQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        setFeatureQuietly(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeatureQuietly(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setErrorHandler(NullErrorHandler.INSTANCE);
        return factory;
    }
    
    private static void setFeatureQuietly(final Object factory, final String name, final boolean value) {
        try {
            if (factory instanceof DocumentBuilderFactory) {
                ((DocumentBuilderFactory)factory).setFeature(name, value);
            }
            else if (factory instanceof TransformerFactory) {
                ((TransformerFactory)factory).setFeature(name, value);
            }
            else {
                if (!(factory instanceof XMLReader)) {
                    throw new Error("Invalid factory class: " + factory.getClass());
                }
                ((XMLReader)factory).setFeature(name, value);
            }
        }
        catch (final Exception ex) {}
    }
    
    private static void setAttributeQuietly(final Object factory, final String name, final Object value) {
        try {
            if (factory instanceof DocumentBuilderFactory) {
                ((DocumentBuilderFactory)factory).setAttribute(name, value);
            }
            else {
                if (!(factory instanceof TransformerFactory)) {
                    throw new Error("Invalid factory class: " + factory.getClass());
                }
                ((TransformerFactory)factory).setAttribute(name, value);
            }
        }
        catch (final Exception ex) {}
    }
    
    private static void setFactoryProperties(final Object factory) {
        setFeatureQuietly(factory, "http://javax.xml.XMLConstants/feature/secure-processing", true);
        setFeatureQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeatureQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        setFeatureQuietly(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeatureQuietly(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        setAttributeQuietly(factory, "http://javax.xml.XMLConstants/property/accessExternalDTD", "");
        setAttributeQuietly(factory, "http://javax.xml.XMLConstants/property/accessExternalSchema", "");
        setAttributeQuietly(factory, "http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
    }
    
    private static void setPropertyQuietly(final Object factory, final String name, final Object value) {
        try {
            if (factory instanceof XMLReader) {
                ((XMLReader)factory).setProperty(name, value);
            }
            else {
                if (!(factory instanceof XMLInputFactory)) {
                    throw new Error("Invalid factory class: " + factory.getClass());
                }
                ((XMLInputFactory)factory).setProperty(name, value);
            }
        }
        catch (final Exception ex) {}
    }
    
    static {
        INSTANCE = new DefaultPGXmlFactoryFactory();
    }
}
