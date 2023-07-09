// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

import java.io.OutputStreamWriter;
import org.codehaus.jackson.io.UTF8Writer;
import org.codehaus.jackson.impl.WriterBasedGenerator;
import org.codehaus.jackson.impl.ReaderBasedParser;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Reader;
import java.net.URL;
import java.io.InputStream;
import org.codehaus.jackson.io.IOContext;
import java.io.FileInputStream;
import java.io.File;
import org.codehaus.jackson.util.VersionUtil;
import org.codehaus.jackson.impl.Utf8Generator;
import org.codehaus.jackson.impl.ByteSourceBootstrapper;
import java.io.IOException;
import org.codehaus.jackson.format.MatchStrength;
import org.codehaus.jackson.format.InputAccessor;
import org.codehaus.jackson.io.OutputDecorator;
import org.codehaus.jackson.io.InputDecorator;
import org.codehaus.jackson.io.CharacterEscapes;
import org.codehaus.jackson.sym.BytesToNameCanonicalizer;
import org.codehaus.jackson.sym.CharsToNameCanonicalizer;
import org.codehaus.jackson.util.BufferRecycler;
import java.lang.ref.SoftReference;

public class JsonFactory implements Versioned
{
    public static final String FORMAT_NAME_JSON = "JSON";
    static final int DEFAULT_PARSER_FEATURE_FLAGS;
    static final int DEFAULT_GENERATOR_FEATURE_FLAGS;
    protected static final ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef;
    protected CharsToNameCanonicalizer _rootCharSymbols;
    protected BytesToNameCanonicalizer _rootByteSymbols;
    protected ObjectCodec _objectCodec;
    protected int _parserFeatures;
    protected int _generatorFeatures;
    protected CharacterEscapes _characterEscapes;
    protected InputDecorator _inputDecorator;
    protected OutputDecorator _outputDecorator;
    
    public JsonFactory() {
        this(null);
    }
    
    public JsonFactory(final ObjectCodec oc) {
        this._rootCharSymbols = CharsToNameCanonicalizer.createRoot();
        this._rootByteSymbols = BytesToNameCanonicalizer.createRoot();
        this._parserFeatures = JsonFactory.DEFAULT_PARSER_FEATURE_FLAGS;
        this._generatorFeatures = JsonFactory.DEFAULT_GENERATOR_FEATURE_FLAGS;
        this._objectCodec = oc;
    }
    
    public String getFormatName() {
        if (this.getClass() == JsonFactory.class) {
            return "JSON";
        }
        return null;
    }
    
    public MatchStrength hasFormat(final InputAccessor acc) throws IOException {
        if (this.getClass() == JsonFactory.class) {
            return this.hasJSONFormat(acc);
        }
        return null;
    }
    
    protected MatchStrength hasJSONFormat(final InputAccessor acc) throws IOException {
        return ByteSourceBootstrapper.hasJSONFormat(acc);
    }
    
    public Version version() {
        return VersionUtil.versionFor(Utf8Generator.class);
    }
    
    public final JsonFactory configure(final JsonParser.Feature f, final boolean state) {
        if (state) {
            this.enable(f);
        }
        else {
            this.disable(f);
        }
        return this;
    }
    
    public JsonFactory enable(final JsonParser.Feature f) {
        this._parserFeatures |= f.getMask();
        return this;
    }
    
    public JsonFactory disable(final JsonParser.Feature f) {
        this._parserFeatures &= ~f.getMask();
        return this;
    }
    
    public final boolean isEnabled(final JsonParser.Feature f) {
        return (this._parserFeatures & f.getMask()) != 0x0;
    }
    
    @Deprecated
    public final void enableParserFeature(final JsonParser.Feature f) {
        this.enable(f);
    }
    
    @Deprecated
    public final void disableParserFeature(final JsonParser.Feature f) {
        this.disable(f);
    }
    
    @Deprecated
    public final void setParserFeature(final JsonParser.Feature f, final boolean state) {
        this.configure(f, state);
    }
    
    @Deprecated
    public final boolean isParserFeatureEnabled(final JsonParser.Feature f) {
        return (this._parserFeatures & f.getMask()) != 0x0;
    }
    
    public InputDecorator getInputDecorator() {
        return this._inputDecorator;
    }
    
    public JsonFactory setInputDecorator(final InputDecorator d) {
        this._inputDecorator = d;
        return this;
    }
    
    public final JsonFactory configure(final JsonGenerator.Feature f, final boolean state) {
        if (state) {
            this.enable(f);
        }
        else {
            this.disable(f);
        }
        return this;
    }
    
    public JsonFactory enable(final JsonGenerator.Feature f) {
        this._generatorFeatures |= f.getMask();
        return this;
    }
    
    public JsonFactory disable(final JsonGenerator.Feature f) {
        this._generatorFeatures &= ~f.getMask();
        return this;
    }
    
    public final boolean isEnabled(final JsonGenerator.Feature f) {
        return (this._generatorFeatures & f.getMask()) != 0x0;
    }
    
    @Deprecated
    public final void enableGeneratorFeature(final JsonGenerator.Feature f) {
        this.enable(f);
    }
    
    @Deprecated
    public final void disableGeneratorFeature(final JsonGenerator.Feature f) {
        this.disable(f);
    }
    
    @Deprecated
    public final void setGeneratorFeature(final JsonGenerator.Feature f, final boolean state) {
        this.configure(f, state);
    }
    
    @Deprecated
    public final boolean isGeneratorFeatureEnabled(final JsonGenerator.Feature f) {
        return this.isEnabled(f);
    }
    
    public CharacterEscapes getCharacterEscapes() {
        return this._characterEscapes;
    }
    
    public JsonFactory setCharacterEscapes(final CharacterEscapes esc) {
        this._characterEscapes = esc;
        return this;
    }
    
    public OutputDecorator getOutputDecorator() {
        return this._outputDecorator;
    }
    
    public JsonFactory setOutputDecorator(final OutputDecorator d) {
        this._outputDecorator = d;
        return this;
    }
    
    public JsonFactory setCodec(final ObjectCodec oc) {
        this._objectCodec = oc;
        return this;
    }
    
    public ObjectCodec getCodec() {
        return this._objectCodec;
    }
    
    public JsonParser createJsonParser(final File f) throws IOException, JsonParseException {
        final IOContext ctxt = this._createContext(f, true);
        InputStream in = new FileInputStream(f);
        if (this._inputDecorator != null) {
            in = this._inputDecorator.decorate(ctxt, in);
        }
        return this._createJsonParser(in, ctxt);
    }
    
    public JsonParser createJsonParser(final URL url) throws IOException, JsonParseException {
        final IOContext ctxt = this._createContext(url, true);
        InputStream in = this._optimizedStreamFromURL(url);
        if (this._inputDecorator != null) {
            in = this._inputDecorator.decorate(ctxt, in);
        }
        return this._createJsonParser(in, ctxt);
    }
    
    public JsonParser createJsonParser(InputStream in) throws IOException, JsonParseException {
        final IOContext ctxt = this._createContext(in, false);
        if (this._inputDecorator != null) {
            in = this._inputDecorator.decorate(ctxt, in);
        }
        return this._createJsonParser(in, ctxt);
    }
    
    public JsonParser createJsonParser(Reader r) throws IOException, JsonParseException {
        final IOContext ctxt = this._createContext(r, false);
        if (this._inputDecorator != null) {
            r = this._inputDecorator.decorate(ctxt, r);
        }
        return this._createJsonParser(r, ctxt);
    }
    
    public JsonParser createJsonParser(final byte[] data) throws IOException, JsonParseException {
        final IOContext ctxt = this._createContext(data, true);
        if (this._inputDecorator != null) {
            final InputStream in = this._inputDecorator.decorate(ctxt, data, 0, data.length);
            if (in != null) {
                return this._createJsonParser(in, ctxt);
            }
        }
        return this._createJsonParser(data, 0, data.length, ctxt);
    }
    
    public JsonParser createJsonParser(final byte[] data, final int offset, final int len) throws IOException, JsonParseException {
        final IOContext ctxt = this._createContext(data, true);
        if (this._inputDecorator != null) {
            final InputStream in = this._inputDecorator.decorate(ctxt, data, offset, len);
            if (in != null) {
                return this._createJsonParser(in, ctxt);
            }
        }
        return this._createJsonParser(data, offset, len, ctxt);
    }
    
    public JsonParser createJsonParser(final String content) throws IOException, JsonParseException {
        Reader r = new StringReader(content);
        final IOContext ctxt = this._createContext(r, true);
        if (this._inputDecorator != null) {
            r = this._inputDecorator.decorate(ctxt, r);
        }
        return this._createJsonParser(r, ctxt);
    }
    
    public JsonGenerator createJsonGenerator(OutputStream out, final JsonEncoding enc) throws IOException {
        final IOContext ctxt = this._createContext(out, false);
        ctxt.setEncoding(enc);
        if (enc == JsonEncoding.UTF8) {
            if (this._outputDecorator != null) {
                out = this._outputDecorator.decorate(ctxt, out);
            }
            return this._createUTF8JsonGenerator(out, ctxt);
        }
        Writer w = this._createWriter(out, enc, ctxt);
        if (this._outputDecorator != null) {
            w = this._outputDecorator.decorate(ctxt, w);
        }
        return this._createJsonGenerator(w, ctxt);
    }
    
    public JsonGenerator createJsonGenerator(Writer out) throws IOException {
        final IOContext ctxt = this._createContext(out, false);
        if (this._outputDecorator != null) {
            out = this._outputDecorator.decorate(ctxt, out);
        }
        return this._createJsonGenerator(out, ctxt);
    }
    
    public JsonGenerator createJsonGenerator(final OutputStream out) throws IOException {
        return this.createJsonGenerator(out, JsonEncoding.UTF8);
    }
    
    public JsonGenerator createJsonGenerator(final File f, final JsonEncoding enc) throws IOException {
        OutputStream out = new FileOutputStream(f);
        final IOContext ctxt = this._createContext(out, true);
        ctxt.setEncoding(enc);
        if (enc == JsonEncoding.UTF8) {
            if (this._outputDecorator != null) {
                out = this._outputDecorator.decorate(ctxt, out);
            }
            return this._createUTF8JsonGenerator(out, ctxt);
        }
        Writer w = this._createWriter(out, enc, ctxt);
        if (this._outputDecorator != null) {
            w = this._outputDecorator.decorate(ctxt, w);
        }
        return this._createJsonGenerator(w, ctxt);
    }
    
    protected JsonParser _createJsonParser(final InputStream in, final IOContext ctxt) throws IOException, JsonParseException {
        return new ByteSourceBootstrapper(ctxt, in).constructParser(this._parserFeatures, this._objectCodec, this._rootByteSymbols, this._rootCharSymbols);
    }
    
    protected JsonParser _createJsonParser(final Reader r, final IOContext ctxt) throws IOException, JsonParseException {
        return new ReaderBasedParser(ctxt, this._parserFeatures, r, this._objectCodec, this._rootCharSymbols.makeChild(this.isEnabled(JsonParser.Feature.CANONICALIZE_FIELD_NAMES), this.isEnabled(JsonParser.Feature.INTERN_FIELD_NAMES)));
    }
    
    protected JsonParser _createJsonParser(final byte[] data, final int offset, final int len, final IOContext ctxt) throws IOException, JsonParseException {
        return new ByteSourceBootstrapper(ctxt, data, offset, len).constructParser(this._parserFeatures, this._objectCodec, this._rootByteSymbols, this._rootCharSymbols);
    }
    
    protected JsonGenerator _createJsonGenerator(final Writer out, final IOContext ctxt) throws IOException {
        final WriterBasedGenerator gen = new WriterBasedGenerator(ctxt, this._generatorFeatures, this._objectCodec, out);
        if (this._characterEscapes != null) {
            gen.setCharacterEscapes(this._characterEscapes);
        }
        return gen;
    }
    
    protected JsonGenerator _createUTF8JsonGenerator(final OutputStream out, final IOContext ctxt) throws IOException {
        final Utf8Generator gen = new Utf8Generator(ctxt, this._generatorFeatures, this._objectCodec, out);
        if (this._characterEscapes != null) {
            gen.setCharacterEscapes(this._characterEscapes);
        }
        return gen;
    }
    
    protected Writer _createWriter(final OutputStream out, final JsonEncoding enc, final IOContext ctxt) throws IOException {
        if (enc == JsonEncoding.UTF8) {
            return new UTF8Writer(ctxt, out);
        }
        return new OutputStreamWriter(out, enc.getJavaName());
    }
    
    protected IOContext _createContext(final Object srcRef, final boolean resourceManaged) {
        return new IOContext(this._getBufferRecycler(), srcRef, resourceManaged);
    }
    
    public BufferRecycler _getBufferRecycler() {
        final SoftReference<BufferRecycler> ref = JsonFactory._recyclerRef.get();
        BufferRecycler br = (ref == null) ? null : ref.get();
        if (br == null) {
            br = new BufferRecycler();
            JsonFactory._recyclerRef.set(new SoftReference<BufferRecycler>(br));
        }
        return br;
    }
    
    protected InputStream _optimizedStreamFromURL(final URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            final String host = url.getHost();
            if (host == null || host.length() == 0) {
                return new FileInputStream(url.getPath());
            }
        }
        return url.openStream();
    }
    
    static {
        DEFAULT_PARSER_FEATURE_FLAGS = JsonParser.Feature.collectDefaults();
        DEFAULT_GENERATOR_FEATURE_FLAGS = JsonGenerator.Feature.collectDefaults();
        _recyclerRef = new ThreadLocal<SoftReference<BufferRecycler>>();
    }
}
