// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.TransformerException;
import org.xml.sax.ContentHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.Result;
import java.io.Writer;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import org.xml.sax.XMLReader;
import javax.xml.parsers.DocumentBuilder;
import org.postgresql.util.GT;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Source;
import java.io.StringReader;
import java.io.Reader;
import java.io.IOException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import org.postgresql.xml.DefaultPGXmlFactoryFactory;
import org.postgresql.xml.PGXmlFactoryFactory;
import javax.xml.transform.dom.DOMResult;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import org.postgresql.core.BaseConnection;
import java.sql.SQLXML;

public class PgSQLXML implements SQLXML
{
    private final BaseConnection conn;
    private String data;
    private boolean initialized;
    private boolean active;
    private boolean freed;
    private ByteArrayOutputStream byteArrayOutputStream;
    private StringWriter stringWriter;
    private DOMResult domResult;
    
    public PgSQLXML(final BaseConnection conn) {
        this(conn, null, false);
    }
    
    public PgSQLXML(final BaseConnection conn, final String data) {
        this(conn, data, true);
    }
    
    private PgSQLXML(final BaseConnection conn, final String data, final boolean initialized) {
        this.conn = conn;
        this.data = data;
        this.initialized = initialized;
        this.active = false;
        this.freed = false;
    }
    
    private PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException {
        if (this.conn != null) {
            return this.conn.getXmlFactoryFactory();
        }
        return DefaultPGXmlFactoryFactory.INSTANCE;
    }
    
    @Override
    public synchronized void free() {
        this.freed = true;
        this.data = null;
    }
    
    @Override
    public synchronized InputStream getBinaryStream() throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        if (this.data == null) {
            return null;
        }
        try {
            return new ByteArrayInputStream(this.conn.getEncoding().encode(this.data));
        }
        catch (final IOException ioe) {
            throw new PSQLException("Failed to re-encode xml data.", PSQLState.DATA_ERROR, ioe);
        }
    }
    
    @Override
    public synchronized Reader getCharacterStream() throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        if (this.data == null) {
            return null;
        }
        return new StringReader(this.data);
    }
    
    @Override
    public synchronized <T extends Source> T getSource(final Class<T> sourceClass) throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        final String data = this.data;
        if (data == null) {
            return null;
        }
        try {
            if (sourceClass == null || DOMSource.class.equals(sourceClass)) {
                final DocumentBuilder builder = this.getXmlFactoryFactory().newDocumentBuilder();
                final InputSource input = new InputSource(new StringReader(data));
                final DOMSource domSource = new DOMSource(builder.parse(input));
                return (T)domSource;
            }
            if (SAXSource.class.equals(sourceClass)) {
                final XMLReader reader = this.getXmlFactoryFactory().createXMLReader();
                final InputSource is = new InputSource(new StringReader(data));
                return sourceClass.cast(new SAXSource(reader, is));
            }
            if (StreamSource.class.equals(sourceClass)) {
                return sourceClass.cast(new StreamSource(new StringReader(data)));
            }
            if (StAXSource.class.equals(sourceClass)) {
                final XMLInputFactory xif = this.getXmlFactoryFactory().newXMLInputFactory();
                final XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(data));
                return sourceClass.cast(new StAXSource(xsr));
            }
        }
        catch (final Exception e) {
            throw new PSQLException(GT.tr("Unable to decode xml data.", new Object[0]), PSQLState.DATA_ERROR, e);
        }
        throw new PSQLException(GT.tr("Unknown XML Source class: {0}", sourceClass), PSQLState.INVALID_PARAMETER_TYPE);
    }
    
    @Override
    public synchronized String getString() throws SQLException {
        this.checkFreed();
        this.ensureInitialized();
        return this.data;
    }
    
    @Override
    public synchronized OutputStream setBinaryStream() throws SQLException {
        this.checkFreed();
        this.initialize();
        this.active = true;
        return this.byteArrayOutputStream = new ByteArrayOutputStream();
    }
    
    @Override
    public synchronized Writer setCharacterStream() throws SQLException {
        this.checkFreed();
        this.initialize();
        this.active = true;
        return this.stringWriter = new StringWriter();
    }
    
    @Override
    public synchronized <T extends Result> T setResult(final Class<T> resultClass) throws SQLException {
        this.checkFreed();
        this.initialize();
        if (resultClass == null || DOMResult.class.equals(resultClass)) {
            this.domResult = new DOMResult();
            this.active = true;
            return (T)this.domResult;
        }
        if (SAXResult.class.equals(resultClass)) {
            try {
                final SAXTransformerFactory transformerFactory = this.getXmlFactoryFactory().newSAXTransformerFactory();
                final TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
                this.stringWriter = new StringWriter();
                transformerHandler.setResult(new StreamResult(this.stringWriter));
                this.active = true;
                return resultClass.cast(new SAXResult(transformerHandler));
            }
            catch (final TransformerException te) {
                throw new PSQLException(GT.tr("Unable to create SAXResult for SQLXML.", new Object[0]), PSQLState.UNEXPECTED_ERROR, te);
            }
        }
        if (StreamResult.class.equals(resultClass)) {
            this.stringWriter = new StringWriter();
            this.active = true;
            return resultClass.cast(new StreamResult(this.stringWriter));
        }
        if (StAXResult.class.equals(resultClass)) {
            final StringWriter stringWriter = new StringWriter();
            this.stringWriter = stringWriter;
            try {
                final XMLOutputFactory xof = this.getXmlFactoryFactory().newXMLOutputFactory();
                final XMLStreamWriter xsw = xof.createXMLStreamWriter(stringWriter);
                this.active = true;
                return resultClass.cast(new StAXResult(xsw));
            }
            catch (final XMLStreamException xse) {
                throw new PSQLException(GT.tr("Unable to create StAXResult for SQLXML", new Object[0]), PSQLState.UNEXPECTED_ERROR, xse);
            }
        }
        throw new PSQLException(GT.tr("Unknown XML Result class: {0}", resultClass), PSQLState.INVALID_PARAMETER_TYPE);
    }
    
    @Override
    public synchronized void setString(final String value) throws SQLException {
        this.checkFreed();
        this.initialize();
        this.data = value;
    }
    
    private void checkFreed() throws SQLException {
        if (this.freed) {
            throw new PSQLException(GT.tr("This SQLXML object has already been freed.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
    }
    
    private void ensureInitialized() throws SQLException {
        if (!this.initialized) {
            throw new PSQLException(GT.tr("This SQLXML object has not been initialized, so you cannot retrieve data from it.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        if (!this.active) {
            return;
        }
        if (this.byteArrayOutputStream != null) {
            try {
                this.data = this.conn.getEncoding().decode(this.byteArrayOutputStream.toByteArray());
            }
            catch (final IOException ioe) {
                throw new PSQLException(GT.tr("Failed to convert binary xml data to encoding: {0}.", this.conn.getEncoding().name()), PSQLState.DATA_ERROR, ioe);
            }
            finally {
                this.byteArrayOutputStream = null;
                this.active = false;
            }
        }
        else if (this.stringWriter != null) {
            this.data = this.stringWriter.toString();
            this.stringWriter = null;
            this.active = false;
        }
        else if (this.domResult != null) {
            DOMResult domResult = this.domResult;
            try {
                final TransformerFactory factory = this.getXmlFactoryFactory().newTransformerFactory();
                final Transformer transformer = factory.newTransformer();
                final DOMSource domSource = new DOMSource(domResult.getNode());
                final StringWriter stringWriter = new StringWriter();
                final StreamResult streamResult = new StreamResult(stringWriter);
                transformer.transform(domSource, streamResult);
                this.data = stringWriter.toString();
            }
            catch (final TransformerException te) {
                throw new PSQLException(GT.tr("Unable to convert DOMResult SQLXML data to a string.", new Object[0]), PSQLState.DATA_ERROR, te);
            }
            finally {
                domResult = null;
                this.active = false;
            }
        }
    }
    
    private void initialize() throws SQLException {
        if (this.initialized) {
            throw new PSQLException(GT.tr("This SQLXML object has already been initialized, so you cannot manipulate it further.", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
        }
        this.initialized = true;
    }
}
