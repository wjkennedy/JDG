// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

import com.atlassian.jira.plugins.datagenerator.PluginGeneratorConfiguration;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GeneratorConfiguration
{
    public Long[] projectIds;
    public int period;
    public int projectCount;
    public String projectTemplate;
    public Workflows workflows;
    public int additionalStatusesCount;
    public int additionalResolutionsCount;
    public int worklogsWithGroup;
    public int worklogsWithProjectRole;
    public int worklogsPerIssue;
    public int totalWorklogs;
    public int uniqueProjectRoles;
    public int uniqueGroups;
    public int groupsCount;
    public int rolesCount;
    public int assignRolesCount;
    public int developersCount;
    public int usersCount;
    public CustomFields customFields;
    public IssueTypes issueTypes;
    public int subtaskTypeCount;
    public boolean includeUserMentions;
    public boolean performReindex;
    public boolean assignSecurityLevelsToIssues;
    public Issues issues;
    public Transitions transitions;
    public System system;
    public PermissionSchemes permissionSchemes;
    public IssueSecuritySchemes issueSecuritySchemes;
    public int versionsCount;
    public int componentsCount;
    public int attachmentCount;
    public int kanbanBoards;
    public int scrumBoards;
    public int issuesPerBoard;
    public int issueWatchers;
    public int additionalComments;
    public String distribution;
    public int distributionTailTreshold;
    public double maxPercentagePerProject;
    public ChangeAuthor changesAuthor;
    public int assigneesCount;
    public int reportersCount;
    public int descriptionsCount;
    public PluginGeneratorConfiguration pluginConfiguration;
    
    public GeneratorConfiguration() {
        this.projectIds = new Long[0];
        this.period = 365;
        this.workflows = new Workflows();
        this.customFields = new CustomFields();
        this.issueTypes = new IssueTypes();
        this.includeUserMentions = true;
        this.performReindex = false;
        this.assignSecurityLevelsToIssues = false;
        this.issues = new Issues();
        this.transitions = new Transitions();
        this.system = new System();
        this.permissionSchemes = new PermissionSchemes();
        this.issueSecuritySchemes = new IssueSecuritySchemes();
        this.versionsCount = 0;
        this.componentsCount = 0;
        this.attachmentCount = 0;
        this.kanbanBoards = 0;
        this.scrumBoards = 0;
        this.issuesPerBoard = 0;
        this.issueWatchers = 0;
        this.additionalComments = 0;
        this.distributionTailTreshold = 1000;
        this.maxPercentagePerProject = 2.0;
        this.assigneesCount = 0;
        this.reportersCount = 0;
        this.descriptionsCount = 0;
        this.pluginConfiguration = new PluginGeneratorConfiguration();
    }
    
    public enum ChangeAuthor
    {
        CURRENT_USER, 
        DEVELOPERS, 
        ALL_USERS;
    }
}
