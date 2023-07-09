// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.issuehistory;

import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.issue.history.ChangeItemBean;
import java.util.Optional;
import javax.annotation.Nonnull;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.user.UserKeyService;
import org.springframework.stereotype.Component;

@Component
public class AssigneeChangeGenerator implements IssueHistoryGenerator
{
    private final UserKeyService userKeyService;
    
    @Autowired
    public AssigneeChangeGenerator(@ComponentImport final UserKeyService userKeyService) {
        this.userKeyService = userKeyService;
    }
    
    @Nonnull
    @Override
    public Optional<ChangeItemBean> generate(@Nonnull final GeneratorContext context, @Nonnull final Long issueId) {
        final String oldUserName = Randomizer.randomKey(context.assignees);
        final String newUserName = Randomizer.randomKey(context.assignees);
        if (oldUserName == null || newUserName == null) {
            return Optional.empty();
        }
        final String oldDisplayName = context.assignees.get(oldUserName);
        final String newDisplayName = context.assignees.get(newUserName);
        return Optional.of(new ChangeItemBean("jira", "assignee", this.userKeyService.getKeyForUsername(oldUserName), oldDisplayName, this.userKeyService.getKeyForUsername(newUserName), newDisplayName));
    }
}
