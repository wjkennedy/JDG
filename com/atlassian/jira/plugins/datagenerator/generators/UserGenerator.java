// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.util.Iterator;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang.RandomStringUtils;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.collect.Lists;
import java.util.Collections;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class UserGenerator
{
    private static final Logger LOG;
    private Map<String, String> preparedUsers;
    private Map<String, String> preparedDevelopers;
    private GeneratorContext generatorContext;
    private final UserUtils userUtils;
    
    @Autowired
    public UserGenerator(final UserUtils userUtils) {
        this.userUtils = userUtils;
    }
    
    public void prepare(final GeneratorContext generatorContext) {
        this.generatorContext = generatorContext;
        this.preparedUsers = Maps.newHashMapWithExpectedSize(generatorContext.generatorConfiguration.usersCount);
        this.preparedDevelopers = Maps.newHashMapWithExpectedSize(generatorContext.generatorConfiguration.developersCount);
        final Map<String, String> possibleReporters = Maps.newHashMapWithExpectedSize(generatorContext.generatorConfiguration.usersCount + generatorContext.generatorConfiguration.developersCount);
        for (int i = 0; i < generatorContext.generatorConfiguration.usersCount; ++i) {
            final Pair<String, String> user = this.generateUser();
            this.preparedUsers.put((String)user.first(), (String)user.second());
            possibleReporters.put((String)user.first(), (String)user.second());
        }
        for (int i = 0; i < generatorContext.generatorConfiguration.developersCount; ++i) {
            final Pair<String, String> user = this.generateUser();
            this.preparedDevelopers.put((String)user.first(), (String)user.second());
            possibleReporters.put((String)user.first(), (String)user.second());
        }
        generatorContext.assignees = (this.preparedDevelopers.isEmpty() ? Collections.emptyMap() : this.preparedDevelopers);
        generatorContext.reporters = (possibleReporters.isEmpty() ? Collections.emptyMap() : possibleReporters);
        generatorContext.generatedUserKeys = Lists.newArrayListWithCapacity(generatorContext.generatorConfiguration.usersCount);
    }
    
    private Pair<String, String> generateUser() {
        final String initials = RandomStringUtils.randomAlphabetic(2).toLowerCase();
        final String given = RandomStringUtils.randomAlphabetic(7).toLowerCase();
        final String userName = initials + given;
        final String fullName = initials.substring(0, 1).toUpperCase() + RandomStringUtils.randomAlphabetic(7).toLowerCase() + " " + initials.substring(1).toUpperCase() + given;
        return (Pair<String, String>)Pair.of((Object)userName, (Object)fullName);
    }
    
    public void generate() throws Exception {
        final int totalCount = this.generatorContext.generatorConfiguration.usersCount + this.generatorContext.generatorConfiguration.developersCount;
        if (totalCount <= 0) {
            UserGenerator.LOG.info("User creation not requested, skipping.");
            return;
        }
        this.generatorContext.resetProgress(String.format("Generating users (%d in total)", totalCount), totalCount);
        UserGenerator.LOG.info("Generating users: {} jira-developers and {} plain jira-users.", (Object)this.generatorContext.generatorConfiguration.developersCount, (Object)this.generatorContext.generatorConfiguration.usersCount);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (final Map.Entry<String, String> entry : this.preparedUsers.entrySet()) {
            final String email = entry.getKey() + "@localdomain.com";
            this.userUtils.createUser(entry.getKey(), email, entry.getValue(), false);
            this.generatorContext.generatedUserKeys.add(entry.getKey());
            this.generatorContext.incProgress();
        }
        for (final Map.Entry<String, String> entry : this.preparedDevelopers.entrySet()) {
            final String email = entry.getKey() + "@localdomain.com";
            this.userUtils.createUser(entry.getKey(), email, entry.getValue(), true);
            this.generatorContext.incProgress();
        }
        stopWatch.stop();
        final String message = String.format("Generated %d users in %s", totalCount, stopWatch.toString());
        this.generatorContext.messages.add(message);
        UserGenerator.LOG.info(message);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)UserGenerator.class);
    }
}
