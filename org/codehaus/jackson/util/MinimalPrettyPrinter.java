// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.PrettyPrinter;

public class MinimalPrettyPrinter implements PrettyPrinter
{
    public static final String DEFAULT_ROOT_VALUE_SEPARATOR = " ";
    protected String _rootValueSeparator;
    
    public MinimalPrettyPrinter() {
        this(" ");
    }
    
    public MinimalPrettyPrinter(final String rootValueSeparator) {
        this._rootValueSeparator = " ";
        this._rootValueSeparator = rootValueSeparator;
    }
    
    public void setRootValueSeparator(final String sep) {
        this._rootValueSeparator = sep;
    }
    
    public void writeRootValueSeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        if (this._rootValueSeparator != null) {
            jg.writeRaw(this._rootValueSeparator);
        }
    }
    
    public void writeStartObject(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw('{');
    }
    
    public void beforeObjectEntries(final JsonGenerator jg) throws IOException, JsonGenerationException {
    }
    
    public void writeObjectFieldValueSeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw(':');
    }
    
    public void writeObjectEntrySeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw(',');
    }
    
    public void writeEndObject(final JsonGenerator jg, final int nrOfEntries) throws IOException, JsonGenerationException {
        jg.writeRaw('}');
    }
    
    public void writeStartArray(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw('[');
    }
    
    public void beforeArrayValues(final JsonGenerator jg) throws IOException, JsonGenerationException {
    }
    
    public void writeArrayValueSeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw(',');
    }
    
    public void writeEndArray(final JsonGenerator jg, final int nrOfValues) throws IOException, JsonGenerationException {
        jg.writeRaw(']');
    }
}
