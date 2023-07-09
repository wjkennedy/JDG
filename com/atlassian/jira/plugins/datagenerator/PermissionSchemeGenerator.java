// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.roles.ProjectRole;
import java.util.Collections;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.permission.PermissionSchemeEntry;
import java.util.Collection;
import com.google.common.collect.Lists;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.permission.ProjectPermissions;
import java.util.Iterator;
import com.atlassian.jira.project.Project;
import java.util.Random;
import org.ofbiz.core.entity.GenericEntityException;
import java.util.List;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;
import java.util.ArrayList;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import java.util.Set;
import java.util.Map;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import org.springframework.stereotype.Component;

@Component
public class PermissionSchemeGenerator
{
    public static final String TYPE = "type";
    public static final String PARAMETER = "parameter";
    public static final String PERMISSION = "permission";
    private final PermissionSchemeManager permissionSchemeManager;
    private final SchemeFactory schemeFactory;
    private final ProjectRoleManager projectRoleManager;
    private final UserUtils userUtils;
    private Map<String, String> reporters;
    private GeneratorContext context;
    private Set<String> developerGroups;
    private Set<String> userGroups;
    private final Map<String, String> moduleUsers;
    
    @Autowired
    public PermissionSchemeGenerator(@ComponentImport final PermissionSchemeManager permissionSchemeManager, @ComponentImport final SchemeFactory schemeFactory, @ComponentImport final ProjectRoleManager projectRoleManager, final UserUtils userUtils) {
        this.developerGroups = Sets.newHashSet();
        this.userGroups = Sets.newHashSet();
        this.moduleUsers = Maps.newHashMap();
        this.permissionSchemeManager = permissionSchemeManager;
        this.schemeFactory = schemeFactory;
        this.projectRoleManager = projectRoleManager;
        this.userUtils = userUtils;
    }
    
    public void generate(final GeneratorContext context, final Map<String, String> moduleUsers) throws GenericEntityException {
        final int permissionsSchemesCount = context.generatorConfiguration.permissionSchemes.schemesCount;
        if (permissionsSchemesCount == 0) {
            return;
        }
        final StopWatch stopWatch = new StopWatch();
        context.resetProgress("Generating custom permission scheme", 1);
        stopWatch.start();
        this.context = context;
        this.prepareReporters();
        this.moduleUsers.clear();
        if (moduleUsers != null) {
            this.moduleUsers.putAll(moduleUsers);
        }
        final ApplicationUser restClientUser = this.userUtils.getUserByKey("jdg-rest-admin");
        if (restClientUser != null) {
            this.moduleUsers.put(restClientUser.getName(), restClientUser.getDisplayName());
        }
        final List<GenericValue> permissionSchemas = new ArrayList<GenericValue>();
        for (int i = 0; i < permissionsSchemesCount; ++i) {
            permissionSchemas.add(this.convertDefaultScheme());
        }
        this.applySchemasToGeneratedProjects(permissionSchemas);
        context.incProgress();
        stopWatch.stop();
        context.messages.add(String.format("Generated %d custom permission scheme in %s", permissionsSchemesCount, stopWatch.toString()));
    }
    
    private void applySchemasToGeneratedProjects(final List<GenericValue> schemas) {
        if (this.context.createdProjects != null) {
            final Random rand = new Random();
            for (final Project project : this.context.createdProjects) {
                this.permissionSchemeManager.removeSchemesFromProject(project);
                final GenericValue scheme = schemas.get(rand.nextInt(schemas.size()));
                this.permissionSchemeManager.addSchemeToProject(project, this.schemeFactory.getScheme(scheme));
            }
        }
    }
    
    private GenericValue convertDefaultScheme() throws GenericEntityException {
        final Scheme defaultScheme = this.permissionSchemeManager.getDefaultSchemeObject();
        final Collection<PermissionSchemeEntry> browseProjectEntries = this.permissionSchemeManager.getPermissionSchemeEntries(defaultScheme, new ProjectPermissionKey(ProjectPermissions.BROWSE_PROJECTS.permissionKey()));
        final Collection<PermissionSchemeEntry> createIssueEntries = this.permissionSchemeManager.getPermissionSchemeEntries(defaultScheme, new ProjectPermissionKey(ProjectPermissions.CREATE_ISSUES.permissionKey()));
        final Collection<PermissionSchemeEntry> editIssueEntries = this.permissionSchemeManager.getPermissionSchemeEntries(defaultScheme, new ProjectPermissionKey(ProjectPermissions.EDIT_ISSUES.permissionKey()));
        final Collection<PermissionSchemeEntry> assignUserEntries = this.permissionSchemeManager.getPermissionSchemeEntries(defaultScheme, new ProjectPermissionKey(ProjectPermissions.ASSIGNABLE_USER.permissionKey()));
        final Collection<PermissionSchemeEntry> scheduleIssuesEntries = this.permissionSchemeManager.getPermissionSchemeEntries(defaultScheme, new ProjectPermissionKey(ProjectPermissions.SCHEDULE_ISSUES.permissionKey()));
        final Collection<PermissionSchemeEntry> addCommentsEntries = this.permissionSchemeManager.getPermissionSchemeEntries(defaultScheme, new ProjectPermissionKey(ProjectPermissions.ADD_COMMENTS.permissionKey()));
        final Collection<PermissionSchemeEntry> editAllCommentsEntries = this.permissionSchemeManager.getPermissionSchemeEntries(defaultScheme, new ProjectPermissionKey(ProjectPermissions.EDIT_ALL_COMMENTS.permissionKey()));
        final Collection<PermissionSchemeEntry> defaultEntities = Lists.newArrayList();
        defaultEntities.addAll(browseProjectEntries);
        defaultEntities.addAll(createIssueEntries);
        defaultEntities.addAll(editIssueEntries);
        defaultEntities.addAll(assignUserEntries);
        defaultEntities.addAll(scheduleIssuesEntries);
        defaultEntities.addAll(addCommentsEntries);
        defaultEntities.addAll(editAllCommentsEntries);
        final GenericValue scheme = this.permissionSchemeManager.createScheme("Auto-generated schema " + this.getMaxSequence(), "This is an auto-generated schema");
        for (final PermissionSchemeEntry defaultEntity : defaultEntities) {
            this.convertPermission(defaultEntity, scheme);
        }
        return scheme;
    }
    
    private void convertPermission(final PermissionSchemeEntry defaultEntity, final GenericValue scheme) throws GenericEntityException {
        if ("projectrole".equals(defaultEntity.getType())) {
            this.convertRolePermission(defaultEntity, scheme);
        }
        else {
            this.copyDefaultPermission(defaultEntity, scheme);
        }
    }
    
    private void convertRolePermission(final PermissionSchemeEntry defaultEntity, final GenericValue scheme) throws GenericEntityException {
        final String parameter = defaultEntity.getParameter();
        final ProjectRole projectRole = this.projectRoleManager.getProjectRole(Long.valueOf(parameter));
        if ("Developers".equals(projectRole.getName())) {
            if (this.context.assignees.size() >= this.context.generatorConfiguration.permissionSchemes.developerGroupsCount) {
                this.convertRoleToGroupsPermission(this.developerGroups, defaultEntity, scheme);
            }
            else {
                this.convertRoleToUsersPermission(defaultEntity, scheme, this.context.assignees);
                this.convertRoleToUsersPermission(defaultEntity, scheme, Collections.singletonMap(this.context.userName, this.userUtils.getDisplayName(this.context.userName)));
            }
            if (!this.moduleUsers.isEmpty()) {
                this.convertRoleToUsersPermission(defaultEntity, scheme, this.moduleUsers);
            }
        }
        else if ("Users".equals(projectRole.getName())) {
            if (this.reporters.size() >= this.context.generatorConfiguration.permissionSchemes.userGroupsCount) {
                this.convertRoleToGroupsPermission(this.userGroups, defaultEntity, scheme);
            }
            else {
                this.convertRoleToUsersPermission(defaultEntity, scheme, this.reporters);
                this.convertRoleToUsersPermission(defaultEntity, scheme, Collections.singletonMap(this.context.userName, this.userUtils.getDisplayName(this.context.userName)));
            }
            if (!this.moduleUsers.isEmpty()) {
                this.convertRoleToUsersPermission(defaultEntity, scheme, this.moduleUsers);
            }
        }
        else {
            this.copyDefaultPermission(defaultEntity, scheme);
        }
    }
    
    private void convertRoleToGroupsPermission(final Set<String> groups, final PermissionSchemeEntry defaultEntity, final GenericValue scheme) throws GenericEntityException {
        for (final String group : groups) {
            this.permissionSchemeManager.createSchemeEntity(scheme, new SchemeEntity("group", group, (Object)defaultEntity.getPermissionKey()));
        }
    }
    
    private void convertRoleToUsersPermission(final PermissionSchemeEntry defaultEntity, final GenericValue scheme, final Map<String, String> users) throws GenericEntityException {
        if (users != null && !users.isEmpty()) {
            for (final String user : users.keySet()) {
                this.permissionSchemeManager.createSchemeEntity(scheme, new SchemeEntity("user", user, (Object)defaultEntity.getPermissionKey()));
            }
        }
        else {
            this.copyDefaultPermission(defaultEntity, scheme);
        }
    }
    
    private void prepareReporters() {
        this.reporters = this.context.reporters;
        if (this.reporters.isEmpty()) {
            final Map<String, String> users = this.userUtils.getUsers();
            this.reporters = (users.isEmpty() ? this.context.assignees : users);
        }
        int userCount = 0;
        int maxUserCount = 0;
        if (this.context.assignees.size() >= this.context.generatorConfiguration.permissionSchemes.developerGroupsCount) {
            maxUserCount = this.context.generatorConfiguration.permissionSchemes.developerGroupsCount;
            for (final String username : this.context.assignees.keySet()) {
                try {
                    final String groupName = "developers_" + userCount % maxUserCount;
                    this.userUtils.addUserToGroup(groupName, username);
                    this.developerGroups.add(groupName);
                    ++userCount;
                }
                catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
            this.developerGroups.add("jira-developers");
        }
        if (this.reporters.size() >= this.context.generatorConfiguration.permissionSchemes.userGroupsCount) {
            maxUserCount = this.context.generatorConfiguration.permissionSchemes.userGroupsCount;
            userCount = 0;
            for (final String username : this.reporters.keySet()) {
                try {
                    final String groupName = "users_" + userCount % maxUserCount;
                    this.userUtils.addUserToGroup(groupName, username);
                    this.userGroups.add(groupName);
                    ++userCount;
                }
                catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
            this.userGroups.add("jira-developers");
        }
    }
    
    private void copyDefaultPermission(final PermissionSchemeEntry defaultEntity, final GenericValue scheme) throws GenericEntityException {
        final SchemeEntity permissionDuplicate = new SchemeEntity(defaultEntity.getType(), defaultEntity.getParameter(), (Object)defaultEntity.getPermissionKey());
        this.permissionSchemeManager.createSchemeEntity(scheme, permissionDuplicate);
    }
    
    private long getMaxSequence() throws GenericEntityException {
        long maxSequence = 0L;
        final List<GenericValue> permissionSchemes = this.permissionSchemeManager.getSchemes();
        for (final GenericValue permissionScheme : permissionSchemes) {
            final long sequence = permissionScheme.getLong("id");
            if (sequence > maxSequence) {
                maxSequence = sequence;
            }
        }
        return maxSequence;
    }
}
