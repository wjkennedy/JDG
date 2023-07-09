// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators;

import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.google.common.collect.Lists;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.creators.generator.RandomTextGenerator;

public abstract class UniqueNameRequiringCreatorImpl<T> implements UniqueNameRequiringCreator<T>
{
    private final RandomTextGenerator wordGenerator;
    
    public UniqueNameRequiringCreatorImpl() {
        this.wordGenerator = new RandomTextGenerator();
    }
    
    @Override
    public List<T> create(final int entities) {
        final List<T> generatedEntities = Lists.newArrayListWithCapacity(entities);
        final List<String> generatedEntitiesNames = Lists.newArrayListWithCapacity(entities);
        for (int i = 0; i < entities; ++i) {
            final String generatedEntityName = new RetryFunction<String>("generate unique name").execute(this.wordGenerator::generateWords, entityName -> entityName != null && !generatedEntitiesNames.contains(entityName));
            generatedEntitiesNames.add(generatedEntityName);
            generatedEntities.add(this.create(generatedEntityName));
        }
        return generatedEntities;
    }
}
