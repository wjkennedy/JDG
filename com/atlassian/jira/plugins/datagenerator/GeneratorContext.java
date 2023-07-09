// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.plugins.datagenerator.rest.model.GeneratorStatus;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import com.atlassian.jira.project.Project;
import java.util.List;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import java.util.UUID;

public class GeneratorContext
{
    public final UUID id;
    public GeneratorConfiguration generatorConfiguration;
    public Map<String, String> assignees;
    public Map<String, String> reporters;
    public List<Long> createdIssuesIds;
    public List<Project> createdProjects;
    public List<CustomFieldInfo> createdCustomFields;
    public List<String> generatedUserKeys;
    public List<String> generatedGroupKeys;
    public String userName;
    public String phase;
    public final AtomicInteger progressMax;
    public final AtomicInteger progress;
    public final List<String> messages;
    public final AtomicReference<String> currentTask;
    public final AtomicBoolean finished;
    
    public GeneratorContext() {
        this.id = UUID.randomUUID();
        this.assignees = Collections.emptyMap();
        this.reporters = Collections.emptyMap();
        this.progressMax = new AtomicInteger(1);
        this.progress = new AtomicInteger();
        this.messages = Collections.synchronizedList((List<String>)Lists.newArrayList());
        this.currentTask = new AtomicReference<String>();
        this.finished = new AtomicBoolean();
    }
    
    public GeneratorStatus toRest() {
        final int progressMax = this.progressMax.get();
        final int normalizedProgress = (progressMax == 0) ? 100 : (100 * this.progress.get() / progressMax);
        return new GeneratorStatus(this.phase, (List<String>)ImmutableList.copyOf((Collection)this.messages), this.currentTask.get(), normalizedProgress, this.finished.get());
    }
    
    public void resetProgress(final String task, final int max) {
        this.currentTask.set(task);
        this.progressMax.set(max);
        this.progress.set(0);
    }
    
    public void incProgress() {
        this.progress.incrementAndGet();
    }
}
