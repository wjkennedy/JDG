// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ext;

import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.w3c.dom.ls.LSSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.Node;
import org.codehaus.jackson.map.ser.std.SerializerBase;

public class DOMSerializer extends SerializerBase<Node>
{
    protected final DOMImplementationLS _domImpl;
    
    public DOMSerializer() {
        super(Node.class);
        DOMImplementationRegistry registry;
        try {
            registry = DOMImplementationRegistry.newInstance();
        }
        catch (final Exception e) {
            throw new IllegalStateException("Could not instantiate DOMImplementationRegistry: " + e.getMessage(), e);
        }
        this._domImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
    }
    
    @Override
    public void serialize(final Node value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        if (this._domImpl == null) {
            throw new IllegalStateException("Could not find DOM LS");
        }
        final LSSerializer writer = this._domImpl.createLSSerializer();
        jgen.writeString(writer.writeToString(value));
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
        return this.createSchemaNode("string", true);
    }
}
