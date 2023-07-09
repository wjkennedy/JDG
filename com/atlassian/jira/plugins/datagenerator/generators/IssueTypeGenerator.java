// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import com.atlassian.jira.issue.issuetype.IssueType;
import org.ofbiz.core.entity.GenericEntityException;
import org.apache.commons.lang.time.StopWatch;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.IssueTypeManagerHelper;
import org.springframework.stereotype.Component;

@Component
public class IssueTypeGenerator
{
    private final IssueTypeManagerHelper issueTypeManagerHelper;
    private int issueNameCounter;
    private List<String> iconsUrls;
    private Random random;
    
    @Autowired
    public IssueTypeGenerator(final IssueTypeManagerHelper issueTypeManagerHelper) {
        this.issueNameCounter = 1;
        this.iconsUrls = Arrays.asList("/images/icons/genericissue.gif", "/images/icons/bug.gif", "/images/icons/ico_epic.png", "/images/icons/exclamation.gif", "/images/icons/task.gif", "/images/icons/sales.gif", "/images/icons/improvement.gif");
        this.random = new Random();
        this.issueTypeManagerHelper = issueTypeManagerHelper;
    }
    
    public void generate(final GeneratorContext context) throws GenericEntityException {
        final int standardIssueTypesCount = context.generatorConfiguration.issueTypes.standardCount;
        if (standardIssueTypesCount <= 0) {
            return;
        }
        final StopWatch stopWatch = new StopWatch();
        context.resetProgress(String.format("Generating standard issue types (%d in total)", standardIssueTypesCount), standardIssueTypesCount);
        stopWatch.start();
        for (int i = 0; i < standardIssueTypesCount; ++i) {
            this.createRandomStandardIssue();
            context.incProgress();
        }
        stopWatch.stop();
        context.messages.add(String.format("Generated %d standard issue types in %s", standardIssueTypesCount, stopWatch.toString()));
    }
    
    private void createRandomStandardIssue() {
        final String issueTypeName = this.generateNewIssueTypeName();
        final String issueIconUrl = this.iconsUrls.get(this.random.nextInt(this.iconsUrls.size()));
        this.issueTypeManagerHelper.createIssueType(issueTypeName, "", issueIconUrl);
    }
    
    private boolean isIssueTypeNameTaken(final String issueTypeName) {
        return this.issueTypeManagerHelper.getIssueTypes().stream().anyMatch(issueType -> issueTypeName.equals(issueType.getName()));
    }
    
    private String generateNewIssueTypeNameWithoutCheck() {
        return "IssueType " + this.issueNameCounter++;
    }
    
    private String generateNewIssueTypeName() {
        String newName;
        for (newName = this.generateNewIssueTypeNameWithoutCheck(); this.isIssueTypeNameTaken(newName); newName = this.generateNewIssueTypeNameWithoutCheck()) {}
        return newName;
    }
}
