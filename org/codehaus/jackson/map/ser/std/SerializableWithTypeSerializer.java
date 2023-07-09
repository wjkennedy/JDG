// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;
import java.lang.annotation.Annotation;
import org.codehaus.jackson.schema.JsonSerializableSchema;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.JsonSerializableWithType;

@JacksonStdImpl
public class SerializableWithTypeSerializer extends SerializerBase<JsonSerializableWithType>
{
    public static final SerializableWithTypeSerializer instance;
    
    protected SerializableWithTypeSerializer() {
        super(JsonSerializableWithType.class);
    }
    
    @Override
    public void serialize(final JsonSerializableWithType value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        value.serialize(jgen, provider);
    }
    
    @Override
    public final void serializeWithType(final JsonSerializableWithType value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        value.serializeWithType(jgen, provider, typeSer);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        final ObjectNode objectNode = this.createObjectNode();
        String schemaType = "any";
        String objectProperties = null;
        String itemDefinition = null;
        if (typeHint != null) {
            final Class<?> rawClass = TypeFactory.rawClass(typeHint);
            if (rawClass.isAnnotationPresent(JsonSerializableSchema.class)) {
                final JsonSerializableSchema schemaInfo = rawClass.getAnnotation(JsonSerializableSchema.class);
                schemaType = schemaInfo.schemaType();
                if (!"##irrelevant".equals(schemaInfo.schemaObjectPropertiesDefinition())) {
                    objectProperties = schemaInfo.schemaObjectPropertiesDefinition();
                }
                if (!"##irrelevant".equals(schemaInfo.schemaItemDefinition())) {
                    itemDefinition = schemaInfo.schemaItemDefinition();
                }
            }
        }
        objectNode.put("type", schemaType);
        if (objectProperties != null) {
            try {
                objectNode.put("properties", new ObjectMapper().readValue(objectProperties, JsonNode.class));
            }
            catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        if (itemDefinition != null) {
            try {
                objectNode.put("items", new ObjectMapper().readValue(itemDefinition, JsonNode.class));
            }
            catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return objectNode;
    }
    
    static {
        instance = new SerializableWithTypeSerializer();
    }
}
