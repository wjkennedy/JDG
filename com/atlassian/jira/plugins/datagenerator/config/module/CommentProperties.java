// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import com.atlassian.jira.util.json.JSONObject;
import java.util.Map;

public class CommentProperties
{
    private final String entityName;
    private final Map<String, JSONObject> entityProperties;
    
    public CommentProperties(final String entityName, final Map<String, JSONObject> entityProperties) {
        Preconditions.checkArgument(StringUtils.isNotBlank((CharSequence)entityName), (Object)"entityName is required");
        this.entityName = entityName;
        final ImmutableMap.Builder<String, JSONObject> entityPropertiesBuilder = (ImmutableMap.Builder<String, JSONObject>)ImmutableMap.builder();
        if (entityProperties != null) {
            entityPropertiesBuilder.putAll((Map)entityProperties);
        }
        this.entityProperties = (Map<String, JSONObject>)entityPropertiesBuilder.build();
    }
    
    public String getEntityName() {
        return this.entityName;
    }
    
    public Map<String, JSONObject> getEntityProperties() {
        return this.entityProperties;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommentProperties)) {
            return false;
        }
        final CommentProperties that = (CommentProperties)o;
        return this.entityName.equals(that.entityName) && this.entityProperties.equals(that.entityProperties);
    }
    
    @Override
    public int hashCode() {
        int result = this.entityName.hashCode();
        result = 31 * result + this.entityProperties.hashCode();
        return result;
    }
}
