// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config.module;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import com.google.common.base.Preconditions;

public abstract class ConfigurationField
{
    private final ConfigFieldType fieldType;
    private final String label;
    private final String id;
    private final String title;
    private final String defaultValue;
    private String description;
    
    public ConfigurationField(final ConfigFieldType fieldType, final String label, final String id, final String title) {
        this(fieldType, label, id, title, null);
    }
    
    public ConfigurationField(final ConfigFieldType fieldType, final String label, final String id, final String title, final String defaultValue) {
        Preconditions.checkArgument(fieldType != null, (Object)"fieldType is required");
        Preconditions.checkArgument(StringUtils.isNotBlank(id), (Object)"id is required");
        this.fieldType = fieldType;
        this.label = label;
        this.id = id;
        this.title = title;
        this.defaultValue = defaultValue;
    }
    
    public ConfigFieldType getFieldType() {
        return this.fieldType;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String getDefaultValue() {
        return this.defaultValue;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public abstract static class ConfigFieldType
    {
        protected final String type;
        
        protected ConfigFieldType(final String type) {
            this.type = type;
        }
        
        public String getType() {
            return this.type;
        }
        
        public abstract Object parseValue(final String p0);
    }
    
    public static class NumberConfigFieldType extends ConfigFieldType
    {
        public NumberConfigFieldType() {
            super("text");
        }
        
        @Override
        public Object parseValue(final String value) {
            return NumberUtils.isDigits(value) ? Integer.parseInt(value) : 0;
        }
    }
    
    public static class DecimalConfigFieldType extends ConfigFieldType
    {
        public DecimalConfigFieldType() {
            super("text");
        }
        
        @Override
        public Object parseValue(final String value) {
            return NumberUtils.isNumber(value) ? Float.parseFloat(value) : 0.0f;
        }
    }
    
    public static class CheckboxConfigFieldType extends ConfigFieldType
    {
        public CheckboxConfigFieldType() {
            super("checkbox");
        }
        
        @Override
        public Object parseValue(final String value) {
            return StringUtils.isNotBlank(value) && Boolean.parseBoolean(value);
        }
    }
    
    public static class NumberConfigurationField extends ConfigurationField
    {
        public NumberConfigurationField(final String label, final String id, final String title) {
            this(label, id, title, 0);
        }
        
        public NumberConfigurationField(final String label, final String id, final String title, final int defaultValue) {
            super(new NumberConfigFieldType(), label, id, title, String.valueOf(defaultValue));
        }
    }
    
    public static class DecimalConfigurationField extends ConfigurationField
    {
        public DecimalConfigurationField(final String label, final String id, final String title) {
            this(label, id, title, 0.0f);
        }
        
        public DecimalConfigurationField(final String label, final String id, final String title, final float defaultValue) {
            super(new DecimalConfigFieldType(), label, id, title, String.valueOf(defaultValue));
        }
    }
    
    public static class CheckboxConfigurationField extends ConfigurationField
    {
        public CheckboxConfigurationField(final String label, final String id, final String title) {
            this(label, id, title, false);
        }
        
        public CheckboxConfigurationField(final String label, final String id, final String title, final boolean defaultValue) {
            super(new CheckboxConfigFieldType(), label, id, title, String.valueOf(defaultValue));
        }
    }
}
