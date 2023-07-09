// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ext;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.GregorianCalendar;
import org.codehaus.jackson.JsonParser;
import javax.xml.datatype.XMLGregorianCalendar;
import org.codehaus.jackson.map.deser.std.StdScalarDeserializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import javax.xml.datatype.Duration;
import org.codehaus.jackson.map.deser.std.FromStringDeserializer;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.datatype.DatatypeFactory;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.util.Provider;

public class CoreXMLDeserializers implements Provider<StdDeserializer<?>>
{
    static final DatatypeFactory _dataTypeFactory;
    
    public Collection<StdDeserializer<?>> provide() {
        return (Collection<StdDeserializer<?>>)Arrays.asList(new DurationDeserializer(), new GregorianCalendarDeserializer(), new QNameDeserializer());
    }
    
    static {
        try {
            _dataTypeFactory = DatatypeFactory.newInstance();
        }
        catch (final DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class DurationDeserializer extends FromStringDeserializer<Duration>
    {
        public DurationDeserializer() {
            super(Duration.class);
        }
        
        @Override
        protected Duration _deserialize(final String value, final DeserializationContext ctxt) throws IllegalArgumentException {
            return CoreXMLDeserializers._dataTypeFactory.newDuration(value);
        }
    }
    
    public static class GregorianCalendarDeserializer extends StdScalarDeserializer<XMLGregorianCalendar>
    {
        public GregorianCalendarDeserializer() {
            super(XMLGregorianCalendar.class);
        }
        
        @Override
        public XMLGregorianCalendar deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final Date d = this._parseDate(jp, ctxt);
            if (d == null) {
                return null;
            }
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(d);
            return CoreXMLDeserializers._dataTypeFactory.newXMLGregorianCalendar(calendar);
        }
    }
    
    public static class QNameDeserializer extends FromStringDeserializer<QName>
    {
        public QNameDeserializer() {
            super(QName.class);
        }
        
        @Override
        protected QName _deserialize(final String value, final DeserializationContext ctxt) throws IllegalArgumentException {
            return QName.valueOf(value);
        }
    }
}
