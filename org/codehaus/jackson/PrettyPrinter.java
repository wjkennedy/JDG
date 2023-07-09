// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson;

import java.io.IOException;

public interface PrettyPrinter
{
    void writeRootValueSeparator(final JsonGenerator p0) throws IOException, JsonGenerationException;
    
    void writeStartObject(final JsonGenerator p0) throws IOException, JsonGenerationException;
    
    void writeEndObject(final JsonGenerator p0, final int p1) throws IOException, JsonGenerationException;
    
    void writeObjectEntrySeparator(final JsonGenerator p0) throws IOException, JsonGenerationException;
    
    void writeObjectFieldValueSeparator(final JsonGenerator p0) throws IOException, JsonGenerationException;
    
    void writeStartArray(final JsonGenerator p0) throws IOException, JsonGenerationException;
    
    void writeEndArray(final JsonGenerator p0, final int p1) throws IOException, JsonGenerationException;
    
    void writeArrayValueSeparator(final JsonGenerator p0) throws IOException, JsonGenerationException;
    
    void beforeArrayValues(final JsonGenerator p0) throws IOException, JsonGenerationException;
    
    void beforeObjectEntries(final JsonGenerator p0) throws IOException, JsonGenerationException;
}
