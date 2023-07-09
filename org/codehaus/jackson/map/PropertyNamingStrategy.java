// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.introspect.AnnotatedParameter;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedField;

public abstract class PropertyNamingStrategy
{
    public static final PropertyNamingStrategy CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
    
    public String nameForField(final MapperConfig<?> config, final AnnotatedField field, final String defaultName) {
        return defaultName;
    }
    
    public String nameForGetterMethod(final MapperConfig<?> config, final AnnotatedMethod method, final String defaultName) {
        return defaultName;
    }
    
    public String nameForSetterMethod(final MapperConfig<?> config, final AnnotatedMethod method, final String defaultName) {
        return defaultName;
    }
    
    public String nameForConstructorParameter(final MapperConfig<?> config, final AnnotatedParameter ctorParam, final String defaultName) {
        return defaultName;
    }
    
    static {
        CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES = new LowerCaseWithUnderscoresStrategy();
    }
    
    public abstract static class PropertyNamingStrategyBase extends PropertyNamingStrategy
    {
        @Override
        public String nameForField(final MapperConfig<?> config, final AnnotatedField field, final String defaultName) {
            return this.translate(defaultName);
        }
        
        @Override
        public String nameForGetterMethod(final MapperConfig<?> config, final AnnotatedMethod method, final String defaultName) {
            return this.translate(defaultName);
        }
        
        @Override
        public String nameForSetterMethod(final MapperConfig<?> config, final AnnotatedMethod method, final String defaultName) {
            return this.translate(defaultName);
        }
        
        @Override
        public String nameForConstructorParameter(final MapperConfig<?> config, final AnnotatedParameter ctorParam, final String defaultName) {
            return this.translate(defaultName);
        }
        
        public abstract String translate(final String p0);
    }
    
    public static class LowerCaseWithUnderscoresStrategy extends PropertyNamingStrategyBase
    {
        @Override
        public String translate(final String input) {
            if (input == null) {
                return input;
            }
            final int length = input.length();
            final StringBuilder result = new StringBuilder(length * 2);
            int resultLength = 0;
            boolean wasPrevTranslated = false;
            for (int i = 0; i < length; ++i) {
                char c = input.charAt(i);
                if (i > 0 || c != '_') {
                    if (Character.isUpperCase(c)) {
                        if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != '_') {
                            result.append('_');
                            ++resultLength;
                        }
                        c = Character.toLowerCase(c);
                        wasPrevTranslated = true;
                    }
                    else {
                        wasPrevTranslated = false;
                    }
                    result.append(c);
                    ++resultLength;
                }
            }
            return (resultLength > 0) ? result.toString() : input;
        }
    }
}
