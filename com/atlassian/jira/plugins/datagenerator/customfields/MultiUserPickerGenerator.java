// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
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
import java.util.List;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.user.util.UserManager;

public class MultiUserPickerGenerator extends AbstractFieldValueGenerator
{
    private final UserManager userManager;
    private final int maxUsersToGenerate;
    private final UserConverter userConverter;
    private final List<String> userNames;
    public static final FieldValueGenerator.Factory factory;
    
    private MultiUserPickerGenerator(@Nonnull final Set<String> userNames, final int maxUsersToGenerate) {
        this.userNames = (List<String>)ImmutableList.copyOf((Collection)userNames);
        this.maxUsersToGenerate = maxUsersToGenerate;
        this.userManager = ComponentAccessor.getUserManager();
        this.userConverter = (UserConverter)ComponentAccessor.getComponent((Class)UserConverter.class);
    }
    
    @Nonnull
    @Override
    public Collection<ApplicationUser> generate(@Nullable final IssueInfo issue, @Nullable final CustomFieldInfo customFieldInfo) {
        final int usersToGenerate = Randomizer.randomInt(this.maxUsersToGenerate + 1);
        return Randomizer.randomItems(usersToGenerate, this.userNames).stream().map((Function<? super Object, ?>)this.userManager::getUserByName).collect((Collector<? super Object, ?, Collection<ApplicationUser>>)Collectors.toList());
    }
    
    @Nonnull
    @Override
    public String fieldType() {
        return "stringvalue";
    }
    
    @Nonnull
    @Override
    public Collection convertToDbValue(@Nullable final Object generatedValue) {
        if (!(generatedValue instanceof Collection)) {
            return Collections.emptyList();
        }
        final Collection<Object> users = (Collection<Object>)generatedValue;
        return users.stream().map((Function<? super Object, ?>)this::objectToApplicationUser).filter(Optional::isPresent).map((Function<? super Object, ?>)Optional::get).map((Function<? super Object, ?>)this.userConverter::getDbString).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList());
    }
    
    @Nonnull
    private Optional<ApplicationUser> objectToApplicationUser(@Nullable final Object object) {
        if (object instanceof ApplicationUser) {
            return Optional.of(object);
        }
        return Optional.empty();
    }
    
    static {
        factory = new FieldValueGenerator.Factory() {
            @Nonnull
            @Override
            public FieldValueGenerator create(@Nonnull final GeneratorContext generatorContext) {
                return new MultiUserPickerGenerator(generatorContext.reporters.keySet(), generatorContext.generatorConfiguration.customFields.maxUsersToAssignInMultiUserPicker, null);
            }
            
            @Override
            public boolean isEnabled(@Nonnull final GeneratorContext generatorContext) {
                return generatorContext.generatorConfiguration.customFields.multiUserPickerEnabled;
            }
        };
    }
}
