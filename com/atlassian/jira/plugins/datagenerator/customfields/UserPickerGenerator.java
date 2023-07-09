// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import javax.annotation.Nullable;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.jira.component.ComponentAccessor;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Set;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.user.util.UserManager;
import java.util.List;

public class UserPickerGenerator extends AbstractFieldValueGenerator
{
    private final List<String> userNames;
    private final UserManager userManager;
    private final UserConverter userConverter;
    public static final FieldValueGenerator.Factory factory;
    
    private UserPickerGenerator(@Nonnull final Set<String> userNames) {
        this.userNames = (List<String>)ImmutableList.copyOf((Collection)userNames);
        this.userManager = ComponentAccessor.getUserManager();
        this.userConverter = (UserConverter)ComponentAccessor.getComponent((Class)UserConverter.class);
    }
    
    @Nullable
    @Override
    public ApplicationUser generate(@Nullable final IssueInfo issue, @Nullable final CustomFieldInfo customFieldInfo) {
        return this.userManager.getUserByName((String)Randomizer.randomItem(this.userNames));
    }
    
    @Nonnull
    @Override
    public String fieldType() {
        return "stringvalue";
    }
    
    @Nullable
    @Override
    public String convertToDbValue(@Nullable final Object generatedValue) {
        if (!(generatedValue instanceof ApplicationUser)) {
            return null;
        }
        return this.userConverter.getDbString((ApplicationUser)generatedValue);
    }
    
    static {
        factory = new FieldValueGenerator.Factory() {
            @Nonnull
            @Override
            public FieldValueGenerator create(@Nonnull final GeneratorContext generatorContext) {
                return new UserPickerGenerator(generatorContext.reporters.keySet(), null);
            }
            
            @Override
            public boolean isEnabled(@Nonnull final GeneratorContext generatorContext) {
                return generatorContext.generatorConfiguration.customFields.singleUserPickerEnabled;
            }
        };
    }
}
