// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.util.MinimalPrettyPrinter;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.codehaus.jackson.io.SegmentedStringWriter;
import java.io.Writer;
import java.io.OutputStream;
import org.codehaus.jackson.JsonEncoding;
import java.io.File;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import java.io.Closeable;
import org.codehaus.jackson.JsonGenerator;
import java.text.DateFormat;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.util.VersionUtil;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.FormatSchema;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.Versioned;

public class ObjectWriter implements Versioned
{
    protected static final PrettyPrinter NULL_PRETTY_PRINTER;
    protected final SerializationConfig _config;
    protected final SerializerProvider _provider;
    protected final SerializerFactory _serializerFactory;
    protected final JsonFactory _jsonFactory;
    protected final JavaType _rootType;
    protected final PrettyPrinter _prettyPrinter;
    protected final FormatSchema _schema;
    
    protected ObjectWriter(final ObjectMapper mapper, final SerializationConfig config, final JavaType rootType, final PrettyPrinter pp) {
        this._config = config;
        this._provider = mapper._serializerProvider;
        this._serializerFactory = mapper._serializerFactory;
        this._jsonFactory = mapper._jsonFactory;
        this._rootType = rootType;
        this._prettyPrinter = pp;
        this._schema = null;
    }
    
    protected ObjectWriter(final ObjectMapper mapper, final SerializationConfig config) {
        this._config = config;
        this._provider = mapper._serializerProvider;
        this._serializerFactory = mapper._serializerFactory;
        this._jsonFactory = mapper._jsonFactory;
        this._rootType = null;
        this._prettyPrinter = null;
        this._schema = null;
    }
    
    protected ObjectWriter(final ObjectMapper mapper, final SerializationConfig config, final FormatSchema s) {
        this._config = config;
        this._provider = mapper._serializerProvider;
        this._serializerFactory = mapper._serializerFactory;
        this._jsonFactory = mapper._jsonFactory;
        this._rootType = null;
        this._prettyPrinter = null;
        this._schema = s;
    }
    
    protected ObjectWriter(final ObjectWriter base, final SerializationConfig config, final JavaType rootType, final PrettyPrinter pp, final FormatSchema s) {
        this._config = config;
        this._provider = base._provider;
        this._serializerFactory = base._serializerFactory;
        this._jsonFactory = base._jsonFactory;
        this._rootType = rootType;
        this._prettyPrinter = pp;
        this._schema = s;
    }
    
    protected ObjectWriter(final ObjectWriter base, final SerializationConfig config) {
        this._config = config;
        this._provider = base._provider;
        this._serializerFactory = base._serializerFactory;
        this._jsonFactory = base._jsonFactory;
        this._schema = base._schema;
        this._rootType = base._rootType;
        this._prettyPrinter = base._prettyPrinter;
    }
    
    public Version version() {
        return VersionUtil.versionFor(this.getClass());
    }
    
    public ObjectWriter withView(final Class<?> view) {
        if (view == this._config.getSerializationView()) {
            return this;
        }
        return new ObjectWriter(this, this._config.withView(view));
    }
    
    public ObjectWriter withType(final JavaType rootType) {
        if (rootType == this._rootType) {
            return this;
        }
        return new ObjectWriter(this, this._config, rootType, this._prettyPrinter, this._schema);
    }
    
    public ObjectWriter withType(final Class<?> rootType) {
        return this.withType(this._config.constructType(rootType));
    }
    
    public ObjectWriter withType(final TypeReference<?> rootType) {
        return this.withType(this._config.getTypeFactory().constructType(rootType.getType()));
    }
    
    public ObjectWriter withPrettyPrinter(PrettyPrinter pp) {
        if (pp == this._prettyPrinter) {
            return this;
        }
        if (pp == null) {
            pp = ObjectWriter.NULL_PRETTY_PRINTER;
        }
        return new ObjectWriter(this, this._config, this._rootType, pp, this._schema);
    }
    
    public ObjectWriter withDefaultPrettyPrinter() {
        return this.withPrettyPrinter(new DefaultPrettyPrinter());
    }
    
    public ObjectWriter withFilters(final FilterProvider filterProvider) {
        if (filterProvider == this._config.getFilterProvider()) {
            return this;
        }
        return new ObjectWriter(this, this._config.withFilters(filterProvider));
    }
    
    public ObjectWriter withSchema(final FormatSchema schema) {
        if (this._schema == schema) {
            return this;
        }
        return new ObjectWriter(this, this._config, this._rootType, this._prettyPrinter, schema);
    }
    
    public ObjectWriter withDateFormat(final DateFormat df) {
        final SerializationConfig newConfig = this._config.withDateFormat(df);
        if (newConfig == this._config) {
            return this;
        }
        return new ObjectWriter(this, newConfig);
    }
    
    public void writeValue(final JsonGenerator jgen, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        if (this._config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._writeCloseableValue(jgen, value, this._config);
        }
        else {
            if (this._rootType == null) {
                this._provider.serializeValue(this._config, jgen, value, this._serializerFactory);
            }
            else {
                this._provider.serializeValue(this._config, jgen, value, this._rootType, this._serializerFactory);
            }
            if (this._config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
        }
    }
    
    public void writeValue(final File resultFile, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(resultFile, JsonEncoding.UTF8), value);
    }
    
    public void writeValue(final OutputStream out, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8), value);
    }
    
    public void writeValue(final Writer w, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(w), value);
    }
    
    public String writeValueAsString(final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        final SegmentedStringWriter sw = new SegmentedStringWriter(this._jsonFactory._getBufferRecycler());
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(sw), value);
        return sw.getAndClear();
    }
    
    public byte[] writeValueAsBytes(final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        final ByteArrayBuilder bb = new ByteArrayBuilder(this._jsonFactory._getBufferRecycler());
        this._configAndWriteValue(this._jsonFactory.createJsonGenerator(bb, JsonEncoding.UTF8), value);
        final byte[] result = bb.toByteArray();
        bb.release();
        return result;
    }
    
    public boolean canSerialize(final Class<?> type) {
        return this._provider.hasSerializerFor(this._config, type, this._serializerFactory);
    }
    
    protected final void _configAndWriteValue(final JsonGenerator jgen, final Object value) throws IOException, JsonGenerationException, JsonMappingException {
        if (this._prettyPrinter != null) {
            final PrettyPrinter pp = this._prettyPrinter;
            jgen.setPrettyPrinter((pp == ObjectWriter.NULL_PRETTY_PRINTER) ? null : pp);
        }
        else if (this._config.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
            jgen.useDefaultPrettyPrinter();
        }
        if (this._schema != null) {
            jgen.setSchema(this._schema);
        }
        if (this._config.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._configAndWriteCloseable(jgen, value, this._config);
            return;
        }
        boolean closed = false;
        try {
            if (this._rootType == null) {
                this._provider.serializeValue(this._config, jgen, value, this._serializerFactory);
            }
            else {
                this._provider.serializeValue(this._config, jgen, value, this._rootType, this._serializerFactory);
            }
            closed = true;
            jgen.close();
        }
        finally {
            if (!closed) {
                try {
                    jgen.close();
                }
                catch (final IOException ex) {}
            }
        }
    }
    
    private final void _configAndWriteCloseable(JsonGenerator jgen, final Object value, final SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
        Closeable toClose = (Closeable)value;
        try {
            if (this._rootType == null) {
                this._provider.serializeValue(cfg, jgen, value, this._serializerFactory);
            }
            else {
                this._provider.serializeValue(cfg, jgen, value, this._rootType, this._serializerFactory);
            }
            if (this._schema != null) {
                jgen.setSchema(this._schema);
            }
            final JsonGenerator tmpJgen = jgen;
            jgen = null;
            tmpJgen.close();
            final Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        }
        finally {
            if (jgen != null) {
                try {
                    jgen.close();
                }
                catch (final IOException ex) {}
            }
            if (toClose != null) {
                try {
                    toClose.close();
                }
                catch (final IOException ex2) {}
            }
        }
    }
    
    private final void _writeCloseableValue(final JsonGenerator jgen, final Object value, final SerializationConfig cfg) throws IOException, JsonGenerationException, JsonMappingException {
        Closeable toClose = (Closeable)value;
        try {
            if (this._rootType == null) {
                this._provider.serializeValue(cfg, jgen, value, this._serializerFactory);
            }
            else {
                this._provider.serializeValue(cfg, jgen, value, this._rootType, this._serializerFactory);
            }
            if (this._config.isEnabled(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE)) {
                jgen.flush();
            }
            final Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        }
        finally {
            if (toClose != null) {
                try {
                    toClose.close();
                }
                catch (final IOException ex) {}
            }
        }
    }
    
    static {
        NULL_PRETTY_PRINTER = new MinimalPrettyPrinter();
    }
}
