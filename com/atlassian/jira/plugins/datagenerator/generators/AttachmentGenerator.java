// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.google.common.collect.Lists;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Optional;
import com.atlassian.jira.issue.Issue;
import java.util.Iterator;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.plugins.datagenerator.util.PhasedTimer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.model.AttachmentFile;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.issue.AttachmentManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class AttachmentGenerator
{
    private static final Logger LOG;
    private final AttachmentManager attachmentManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final IssueUpdater issueUpdater;
    private final UserUtils userUtils;
    private List<AttachmentFile> attachmentFiles;
    private GeneratorContext context;
    
    @Autowired
    public AttachmentGenerator(@ComponentImport final AttachmentManager attachmentManager, @ComponentImport final ProjectManager projectManager, @ComponentImport final IssueManager issueManager, @ComponentImport final IssueUpdater issueUpdater, final UserUtils userUtils) {
        this.attachmentManager = attachmentManager;
        this.projectManager = projectManager;
        this.issueManager = issueManager;
        this.issueUpdater = issueUpdater;
        this.userUtils = userUtils;
    }
    
    public void prepare(final GeneratorContext context) throws Exception {
        this.context = context;
        this.attachmentFiles = AttachmentFile.createDefaultAttachments();
    }
    
    public void generate() throws Exception {
        int attachmentCount = this.context.generatorConfiguration.attachmentCount;
        final List<Project> allProjectsToGenerate = GeneratorConfigurationUtil.getProjects(this.context.generatorConfiguration, this.projectManager);
        int issuesCount = 0;
        for (final Project p : allProjectsToGenerate) {
            issuesCount += (int)this.issueManager.getIssueCountForProject(p.getId());
        }
        double meanAttachmentPerIssue = attachmentCount / (double)issuesCount;
        final PhasedTimer phasedTimer = new PhasedTimer();
        this.context.resetProgress(String.format("Generating %d attachments", attachmentCount), attachmentCount);
        phasedTimer.startPhase("generating attachments:");
        for (final Project p2 : allProjectsToGenerate) {
            final Collection<Long> issueIds = this.issueManager.getIssueIdsForProject(p2.getId());
            for (final Long issueId : issueIds) {
                if (attachmentCount == 0) {
                    break;
                }
                final Issue issue = (Issue)this.issueManager.getIssueObject(issueId);
                int attachmentsForIssue;
                if (issuesCount == 1) {
                    attachmentsForIssue = attachmentCount;
                }
                else {
                    attachmentsForIssue = Math.min(attachmentCount, Randomizer.randomLimitedGaussian(meanAttachmentPerIssue));
                    attachmentCount -= attachmentsForIssue;
                    --issuesCount;
                    meanAttachmentPerIssue = attachmentCount / (double)issuesCount;
                }
                this.addAttachmentsToIssue(issue, attachmentsForIssue);
                this.context.progress.addAndGet(attachmentsForIssue);
            }
        }
        phasedTimer.stop();
        this.context.messages.addAll(phasedTimer.toStrings());
    }
    
    public void cleanup() {
        AttachmentFile.closeAttachments(this.attachmentFiles);
    }
    
    private void addAttachmentsToIssue(final Issue issue, final int attachments) throws AttachmentException, URISyntaxException, IOException {
        final Optional<ApplicationUser> reporter = (Optional<ApplicationUser>)Optional.fromNullable((Object)issue.getReporter());
        final ApplicationUser user = (ApplicationUser)reporter.or((Object)this.userUtils.getLoggedInUser());
        final List<ChangeItemBean> changeItemBeans = Lists.newArrayList();
        for (int i = 0; i < attachments; ++i) {
            final AttachmentFile file = Randomizer.randomItem(this.attachmentFiles);
            final CreateAttachmentParamsBean attachmentParamsBean = new CreateAttachmentParamsBean.Builder(file.getFile(), file.getFileName(), file.getContentType(), user, issue).copySourceFile(Boolean.valueOf(true)).build();
            changeItemBeans.add(this.attachmentManager.createAttachment(attachmentParamsBean));
        }
        final IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue, issue, EventType.ISSUE_UPDATED_ID, user);
        issueUpdateBean.setChangeItems((Collection)changeItemBeans);
        this.issueUpdater.doUpdate(issueUpdateBean, true);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)AttachmentGenerator.class);
    }
}
