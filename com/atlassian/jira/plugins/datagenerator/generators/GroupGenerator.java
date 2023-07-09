// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.util.Iterator;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.google.common.base.Function;
import java.util.Collection;
import org.apache.commons.lang.time.StopWatch;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.text.DictionaryTextGenerator;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class GroupGenerator
{
    private static final Logger LOG;
    private static final int MAX_RETRIES = 20;
    private GeneratorContext generatorContext;
    private final UserUtils userUtils;
    private final DictionaryTextGenerator dictionaryTextGenerator;
    
    @Autowired
    public GroupGenerator(final UserUtils userUtils) {
        this.userUtils = userUtils;
        this.dictionaryTextGenerator = new DictionaryTextGenerator(0.5f, 1, 1);
    }
    
    private List<String> randomGroupNames(final int number) {
        final List<String> preparedGroups = Lists.newArrayListWithExpectedSize(number);
        for (int i = 0; i < number; ++i) {
            preparedGroups.add(this.generateGroupName() + i);
        }
        return preparedGroups;
    }
    
    private String generateGroupName() {
        return this.dictionaryTextGenerator.generateText(1);
    }
    
    public void generate(final GeneratorContext context) throws Exception {
        this.generatorContext = context;
        final int count = this.generatorContext.generatorConfiguration.groupsCount;
        if (count <= 0) {
            GroupGenerator.LOG.info("Group creation not requested, skipping.");
            this.generatorContext.generatedGroupKeys = Lists.newArrayList();
            return;
        }
        this.generatorContext.generatedGroupKeys = Lists.newArrayListWithCapacity(count);
        this.generatorContext.resetProgress(String.format("Generating groups (%d in total)", count), count);
        GroupGenerator.LOG.info("Generating {} groups", (Object)count);
        final Function<List<String>, List<String>> createGroups = (Function<List<String>, List<String>>)(groupNames -> {
            final List<String> createdGroups = Lists.newArrayListWithCapacity(groupNames.size());
            for (final String group : groupNames) {
                try {
                    this.userUtils.addGroup(group);
                    createdGroups.add(group);
                    this.generatorContext.incProgress();
                }
                catch (final InvalidGroupException e) {
                    GroupGenerator.LOG.info("Could not generate group '{}': {}", (Object)group, (Object)e.getMessage());
                }
                catch (final Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
            return createdGroups;
        });
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<String> groups;
        for (int groupsToCreate = count, retries = 0; groupsToCreate > 0 && retries++ < 20; groupsToCreate -= groups.size()) {
            groups = (List)createGroups.apply((Object)this.randomGroupNames(groupsToCreate));
            this.generatorContext.generatedGroupKeys.addAll(groups);
        }
        stopWatch.stop();
        final String message = String.format("Generated %d groups in %s", count, stopWatch.toString());
        this.generatorContext.messages.add(message);
        GroupGenerator.LOG.info(message);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)GroupGenerator.class);
    }
}
