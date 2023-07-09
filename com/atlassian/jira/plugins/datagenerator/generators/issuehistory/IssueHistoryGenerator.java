// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.issuehistory;

import com.atlassian.jira.issue.history.ChangeItemBean;
import java.util.Optional;
import javax.annotation.Nonnull;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;

public interface IssueHistoryGenerator
{
    @Nonnull
    Optional<ChangeItemBean> generate(@Nonnull final GeneratorContext p0, @Nonnull final Long p1);
}
