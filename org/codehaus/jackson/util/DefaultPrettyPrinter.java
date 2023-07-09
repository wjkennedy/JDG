// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import java.util.Arrays;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.impl.Indenter;
import org.codehaus.jackson.PrettyPrinter;

public class DefaultPrettyPrinter implements PrettyPrinter
{
    protected Indenter _arrayIndenter;
    protected Indenter _objectIndenter;
    protected boolean _spacesInObjectEntries;
    protected int _nesting;
    
    public DefaultPrettyPrinter() {
        this._arrayIndenter = new FixedSpaceIndenter();
        this._objectIndenter = new Lf2SpacesIndenter();
        this._spacesInObjectEntries = true;
        this._nesting = 0;
    }
    
    public void indentArraysWith(final Indenter i) {
        this._arrayIndenter = ((i == null) ? new NopIndenter() : i);
    }
    
    public void indentObjectsWith(final Indenter i) {
        this._objectIndenter = ((i == null) ? new NopIndenter() : i);
    }
    
    public void spacesInObjectEntries(final boolean b) {
        this._spacesInObjectEntries = b;
    }
    
    public void writeRootValueSeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw(' ');
    }
    
    public void writeStartObject(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw('{');
        if (!this._objectIndenter.isInline()) {
            ++this._nesting;
        }
    }
    
    public void beforeObjectEntries(final JsonGenerator jg) throws IOException, JsonGenerationException {
        this._objectIndenter.writeIndentation(jg, this._nesting);
    }
    
    public void writeObjectFieldValueSeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        if (this._spacesInObjectEntries) {
            jg.writeRaw(" : ");
        }
        else {
            jg.writeRaw(':');
        }
    }
    
    public void writeObjectEntrySeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw(',');
        this._objectIndenter.writeIndentation(jg, this._nesting);
    }
    
    public void writeEndObject(final JsonGenerator jg, final int nrOfEntries) throws IOException, JsonGenerationException {
        if (!this._objectIndenter.isInline()) {
            --this._nesting;
        }
        if (nrOfEntries > 0) {
            this._objectIndenter.writeIndentation(jg, this._nesting);
        }
        else {
            jg.writeRaw(' ');
        }
        jg.writeRaw('}');
    }
    
    public void writeStartArray(final JsonGenerator jg) throws IOException, JsonGenerationException {
        if (!this._arrayIndenter.isInline()) {
            ++this._nesting;
        }
        jg.writeRaw('[');
    }
    
    public void beforeArrayValues(final JsonGenerator jg) throws IOException, JsonGenerationException {
        this._arrayIndenter.writeIndentation(jg, this._nesting);
    }
    
    public void writeArrayValueSeparator(final JsonGenerator jg) throws IOException, JsonGenerationException {
        jg.writeRaw(',');
        this._arrayIndenter.writeIndentation(jg, this._nesting);
    }
    
    public void writeEndArray(final JsonGenerator jg, final int nrOfValues) throws IOException, JsonGenerationException {
        if (!this._arrayIndenter.isInline()) {
            --this._nesting;
        }
        if (nrOfValues > 0) {
            this._arrayIndenter.writeIndentation(jg, this._nesting);
        }
        else {
            jg.writeRaw(' ');
        }
        jg.writeRaw(']');
    }
    
    public static class NopIndenter implements Indenter
    {
        public void writeIndentation(final JsonGenerator jg, final int level) {
        }
        
        public boolean isInline() {
            return true;
        }
    }
    
    public static class FixedSpaceIndenter implements Indenter
    {
        public void writeIndentation(final JsonGenerator jg, final int level) throws IOException, JsonGenerationException {
            jg.writeRaw(' ');
        }
        
        public boolean isInline() {
            return true;
        }
    }
    
    public static class Lf2SpacesIndenter implements Indenter
    {
        static final String SYSTEM_LINE_SEPARATOR;
        static final int SPACE_COUNT = 64;
        static final char[] SPACES;
        
        public boolean isInline() {
            return false;
        }
        
        public void writeIndentation(final JsonGenerator jg, int level) throws IOException, JsonGenerationException {
            jg.writeRaw(Lf2SpacesIndenter.SYSTEM_LINE_SEPARATOR);
            if (level > 0) {
                for (level += level; level > 64; level -= Lf2SpacesIndenter.SPACES.length) {
                    jg.writeRaw(Lf2SpacesIndenter.SPACES, 0, 64);
                }
                jg.writeRaw(Lf2SpacesIndenter.SPACES, 0, level);
            }
        }
        
        static {
            String lf = null;
            try {
                lf = System.getProperty("line.separator");
            }
            catch (final Throwable t) {}
            SYSTEM_LINE_SEPARATOR = ((lf == null) ? "\n" : lf);
            Arrays.fill(SPACES = new char[64], ' ');
        }
    }
}
