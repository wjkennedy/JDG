// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.apache.commons.lang.math.RandomUtils;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;

public class NumericGenerator extends AbstractFieldValueGenerator
{
    private static final NumericGenerator INSTANCE;
    public static final FieldValueGenerator.Factory factory;
    
    @Override
    public Double generate(final IssueInfo issue, final CustomFieldInfo customFieldInfo) {
        return RandomUtils.nextDouble() * 2.147483647E9;
    }
    
    @Override
    public String fieldType() {
        return "numbervalue";
    }
    
    static {
        INSTANCE = new NumericGenerator();
        factory = new FieldValueGenerator.Factory() {
            @Override
            public FieldValueGenerator create(final GeneratorContext generatorContext) {
                return NumericGenerator.INSTANCE;
            }
            
            @Override
            public boolean isEnabled(final GeneratorContext generatorContext) {
                return generatorContext.generatorConfiguration.customFields.numericEnabled;
            }
        };
    }
}
