// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.util.LinkedNode;
import org.codehaus.jackson.map.DeserializationProblemHandler;
import org.codehaus.jackson.map.JsonDeserializer;
import java.util.Calendar;
import java.text.ParseException;
import java.util.Date;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import java.text.DateFormat;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.map.InjectableValues;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;

public class StdDeserializationContext extends DeserializationContext
{
    static final int MAX_ERROR_STR_LEN = 500;
    protected JsonParser _parser;
    protected final DeserializerProvider _deserProvider;
    protected final InjectableValues _injectableValues;
    protected ArrayBuilders _arrayBuilders;
    protected ObjectBuffer _objectBuffer;
    protected DateFormat _dateFormat;
    
    public StdDeserializationContext(final DeserializationConfig config, final JsonParser jp, final DeserializerProvider prov, final InjectableValues injectableValues) {
        super(config);
        this._parser = jp;
        this._deserProvider = prov;
        this._injectableValues = injectableValues;
    }
    
    @Override
    public DeserializerProvider getDeserializerProvider() {
        return this._deserProvider;
    }
    
    @Override
    public JsonParser getParser() {
        return this._parser;
    }
    
    @Override
    public Object findInjectableValue(final Object valueId, final BeanProperty forProperty, final Object beanInstance) {
        if (this._injectableValues == null) {
            throw new IllegalStateException("No 'injectableValues' configured, can not inject value with id [" + valueId + "]");
        }
        return this._injectableValues.findInjectableValue(valueId, this, forProperty, beanInstance);
    }
    
    @Override
    public final ObjectBuffer leaseObjectBuffer() {
        ObjectBuffer buf = this._objectBuffer;
        if (buf == null) {
            buf = new ObjectBuffer();
        }
        else {
            this._objectBuffer = null;
        }
        return buf;
    }
    
    @Override
    public final void returnObjectBuffer(final ObjectBuffer buf) {
        if (this._objectBuffer == null || buf.initialCapacity() >= this._objectBuffer.initialCapacity()) {
            this._objectBuffer = buf;
        }
    }
    
    @Override
    public final ArrayBuilders getArrayBuilders() {
        if (this._arrayBuilders == null) {
            this._arrayBuilders = new ArrayBuilders();
        }
        return this._arrayBuilders;
    }
    
    @Override
    public Date parseDate(final String dateStr) throws IllegalArgumentException {
        try {
            return this.getDateFormat().parse(dateStr);
        }
        catch (final ParseException pex) {
            throw new IllegalArgumentException(pex.getMessage());
        }
    }
    
    @Override
    public Calendar constructCalendar(final Date d) {
        final Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }
    
    @Override
    public boolean handleUnknownProperty(final JsonParser jp, final JsonDeserializer<?> deser, final Object instanceOrClass, final String propName) throws IOException, JsonProcessingException {
        LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers();
        if (h != null) {
            final JsonParser oldParser = this._parser;
            this._parser = jp;
            try {
                while (h != null) {
                    if (h.value().handleUnknownProperty(this, deser, instanceOrClass, propName)) {
                        return true;
                    }
                    h = h.next();
                }
            }
            finally {
                this._parser = oldParser;
            }
        }
        return false;
    }
    
    @Override
    public JsonMappingException mappingException(final Class<?> targetClass) {
        return this.mappingException(targetClass, this._parser.getCurrentToken());
    }
    
    @Override
    public JsonMappingException mappingException(final Class<?> targetClass, final JsonToken token) {
        final String clsName = this._calcName(targetClass);
        return JsonMappingException.from(this._parser, "Can not deserialize instance of " + clsName + " out of " + token + " token");
    }
    
    @Override
    public JsonMappingException instantiationException(final Class<?> instClass, final Throwable t) {
        return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + ", problem: " + t.getMessage(), t);
    }
    
    @Override
    public JsonMappingException instantiationException(final Class<?> instClass, final String msg) {
        return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + ", problem: " + msg);
    }
    
    @Override
    public JsonMappingException weirdStringException(final Class<?> instClass, final String msg) {
        return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + " from String value '" + this._valueDesc() + "': " + msg);
    }
    
    @Override
    public JsonMappingException weirdNumberException(final Class<?> instClass, final String msg) {
        return JsonMappingException.from(this._parser, "Can not construct instance of " + instClass.getName() + " from number value (" + this._valueDesc() + "): " + msg);
    }
    
    @Override
    public JsonMappingException weirdKeyException(final Class<?> keyClass, final String keyValue, final String msg) {
        return JsonMappingException.from(this._parser, "Can not construct Map key of type " + keyClass.getName() + " from String \"" + this._desc(keyValue) + "\": " + msg);
    }
    
    @Override
    public JsonMappingException wrongTokenException(final JsonParser jp, final JsonToken expToken, final String msg) {
        return JsonMappingException.from(jp, "Unexpected token (" + jp.getCurrentToken() + "), expected " + expToken + ": " + msg);
    }
    
    @Override
    public JsonMappingException unknownFieldException(final Object instanceOrClass, final String fieldName) {
        return UnrecognizedPropertyException.from(this._parser, instanceOrClass, fieldName);
    }
    
    @Override
    public JsonMappingException unknownTypeException(final JavaType type, final String id) {
        return JsonMappingException.from(this._parser, "Could not resolve type id '" + id + "' into a subtype of " + type);
    }
    
    protected DateFormat getDateFormat() {
        if (this._dateFormat == null) {
            this._dateFormat = (DateFormat)this._config.getDateFormat().clone();
        }
        return this._dateFormat;
    }
    
    protected String determineClassName(final Object instance) {
        return ClassUtil.getClassDescription(instance);
    }
    
    protected String _calcName(final Class<?> cls) {
        if (cls.isArray()) {
            return this._calcName(cls.getComponentType()) + "[]";
        }
        return cls.getName();
    }
    
    protected String _valueDesc() {
        try {
            return this._desc(this._parser.getText());
        }
        catch (final Exception e) {
            return "[N/A]";
        }
    }
    
    protected String _desc(String desc) {
        if (desc.length() > 500) {
            desc = desc.substring(0, 500) + "]...[" + desc.substring(desc.length() - 500);
        }
        return desc;
    }
}
