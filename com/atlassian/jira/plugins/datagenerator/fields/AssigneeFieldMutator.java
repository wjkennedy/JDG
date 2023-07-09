// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.fields;

import java.util.Iterator;
import com.google.common.collect.Maps;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class AssigneeFieldMutator implements FieldMutator
{
    private final UserUtils userUtils;
    private GeneratorConfiguration configuration;
    private Map<String, String> assignees;
    private List<String> assigneeNames;
    
    @Autowired
    public AssigneeFieldMutator(final UserUtils userUtils) {
        this.userUtils = userUtils;
    }
    
    @Override
    public String fieldType() {
        return "assignee";
    }
    
    @Override
    public void handle(final Map<Object, Object> sink) {
        if (RandomUtils.nextFloat() < this.configuration.transitions.reassignProbability) {
            final String assignee = Randomizer.randomItem(this.assigneeNames);
            sink.put("assignee", assignee);
        }
    }
    
    @Override
    public ChangeItemBean handle(final Entity issue, final Timestamp currentNow) {
        if (RandomUtils.nextFloat() < this.configuration.transitions.reassignProbability) {
            final String prevAssignee = (String)issue.get("assignee");
            boolean sameAssignee;
            String assignee;
            do {
                assignee = Randomizer.randomItem(this.assigneeNames);
                sameAssignee = StringUtils.equals(prevAssignee, assignee);
                if (sameAssignee && this.assigneeNames.size() <= 1) {
                    return null;
                }
            } while (sameAssignee);
            final String oldAssignee = (String)issue.get("assignee");
            final String oldAssigneeName = (oldAssignee == null) ? null : this.assignees.get(oldAssignee);
            final String assigneeName = this.assignees.get(assignee);
            issue.put("assignee", assignee);
            return new ChangeItemBean("jira", "assignee", oldAssignee, oldAssigneeName, assignee, assigneeName);
        }
        return null;
    }
    
    @Override
    public void init(final GeneratorContext generatorContext, final IssueCreationParameters issueCreationParameters) {
        this.configuration = generatorContext.generatorConfiguration;
        if (issueCreationParameters != null && !issueCreationParameters.getAssigneeUsernames().isEmpty()) {
            this.assigneeNames = (List<String>)ImmutableList.copyOf((Collection)issueCreationParameters.getAssigneeUsernames());
            this.assignees = Maps.newHashMap();
            for (final String username : this.assigneeNames) {
                this.assignees.put(username, this.userUtils.getDisplayName(username));
            }
        }
        else {
            this.assignees = generatorContext.assignees;
            this.assigneeNames = (List<String>)ImmutableList.copyOf((Collection)this.assignees.keySet());
        }
    }
}
