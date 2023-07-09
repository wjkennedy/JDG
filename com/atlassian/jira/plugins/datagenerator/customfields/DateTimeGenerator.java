// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.apache.commons.lang.math.RandomUtils;
import java.sql.Timestamp;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.concurrent.TimeUnit;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;

public class DateTimeGenerator extends AbstractFieldValueGenerator
{
    private final long from;
    private final int periodInSeconds;
    public static final FieldValueGenerator.Factory factory;
    
    private DateTimeGenerator(final GeneratorConfiguration generatorConfiguration) {
        this.from = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(generatorConfiguration.period);
        this.periodInSeconds = (int)TimeUnit.DAYS.toSeconds(generatorConfiguration.period);
    }
    
    @Override
    public Timestamp generate(final IssueInfo issue, final CustomFieldInfo customFieldInfo) {
        return new Timestamp(this.from + TimeUnit.SECONDS.toMillis(RandomUtils.nextInt(this.periodInSeconds)));
    }
    
    @Override
    public String fieldType() {
        return "datevalue";
    }
    
    static {
        factory = new FieldValueGenerator.Factory() {
            @Override
            public FieldValueGenerator create(final GeneratorContext generatorContext) {
                return new DateTimeGenerator(generatorContext.generatorConfiguration, null);
            }
            
            @Override
            public boolean isEnabled(final GeneratorContext generatorContext) {
                return generatorContext.generatorConfiguration.customFields.dateTimeEnabled;
            }
        };
    }
}
