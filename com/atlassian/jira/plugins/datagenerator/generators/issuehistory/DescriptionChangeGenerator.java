// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.issuehistory;

import java.util.List;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import com.google.common.collect.Lists;
import com.atlassian.jira.issue.history.ChangeItemBean;
import java.util.Optional;
import javax.annotation.Nonnull;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.stereotype.Component;

@Component
public class DescriptionChangeGenerator implements IssueHistoryGenerator
{
    @Nonnull
    @Override
    public Optional<ChangeItemBean> generate(@Nonnull final GeneratorContext context, @Nonnull final Long issueId) {
        final List<String> userNames = Lists.newArrayList((Iterable)context.reporters.keySet());
        final TextGenerator textGenerator = TextGenerator.getTextGenerator(context.generatorConfiguration, userNames);
        final String oldContent = textGenerator.generateText();
        final String newContent = textGenerator.generateText();
        return Optional.of(new ChangeItemBean("jira", "description", oldContent, newContent));
    }
}
