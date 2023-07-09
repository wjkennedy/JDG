// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.atlassian.jira.user.ApplicationUser;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import com.google.common.collect.Lists;
import java.util.function.Predicate;
import java.util.Objects;
import java.util.function.Function;
import java.util.Optional;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.event.ClearCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.ImmutableList;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@Component
public class DefaultUserUtils implements UserUtils, InitializingBean, DisposableBean
{
    private static final Logger log;
    private static final List<String> blacklistedUsers;
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final UserService userService;
    private final CrowdService crowdService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final EventPublisher eventPublisher;
    private final GroupManager groupManager;
    private final ResettableLazyReference<List<Group>> developerGroups;
    private final ResettableLazyReference<List<Group>> userGroups;
    private final ResettableLazyReference<List<Group>> adminGroups;
    
    @Autowired
    public DefaultUserUtils(@ComponentImport final UserUtil userUtil, @ComponentImport final UserManager userManager, @ComponentImport final UserService userService, @ComponentImport final CrowdService crowdService, @ComponentImport final JiraAuthenticationContext jiraAuthenticationContext, @ComponentImport final PermissionManager permissionManager, @ComponentImport final EventPublisher eventPublisher, @ComponentImport final GroupManager groupManager) {
        this.developerGroups = new ResettableLazyReference<List<Group>>() {
            protected List<Group> create() throws Exception {
                return DefaultUserUtils.this.getOrCreateGroup((List)ImmutableList.of((Object)"jira-developers", (Object)"developers"));
            }
        };
        this.userGroups = new ResettableLazyReference<List<Group>>() {
            protected List<Group> create() throws Exception {
                return DefaultUserUtils.this.getOrCreateGroup((List)ImmutableList.of((Object)"jira-users", (Object)"users"));
            }
        };
        this.adminGroups = new ResettableLazyReference<List<Group>>() {
            protected List<Group> create() throws Exception {
                return DefaultUserUtils.this.getOrCreateGroup((List)ImmutableList.of((Object)"jira-administrators", (Object)"administrators"));
            }
        };
        this.userUtil = userUtil;
        this.userManager = userManager;
        this.userService = userService;
        this.crowdService = crowdService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.eventPublisher = eventPublisher;
        this.groupManager = groupManager;
    }
    
    public void afterPropertiesSet() throws Exception {
        this.eventPublisher.register((Object)this);
    }
    
    public void destroy() throws Exception {
        this.eventPublisher.unregister((Object)this);
    }
    
    @EventListener
    public void onClearCache(final ClearCacheEvent event) {
        this.userGroups.reset();
        this.developerGroups.reset();
    }
    
    private Optional<Group> getFirstExistingGroup(final List<String> candidateNames) {
        return candidateNames.stream().map((Function<? super Object, ? extends Group>)this.groupManager::getGroup).filter(Objects::nonNull).findFirst();
    }
    
    private List<Group> getOrCreateGroup(final List<String> candidateNames) {
        final Optional<Group> existingGroup = this.getFirstExistingGroup(candidateNames);
        if (!existingGroup.isPresent() && !candidateNames.isEmpty()) {
            final String groupName = candidateNames.get(0);
            try {
                final Group createdGroup = this.addGroup(groupName);
                DefaultUserUtils.log.info(String.format("Created group: %s", groupName));
                return (List<Group>)ImmutableList.of((Object)createdGroup);
            }
            catch (final Exception e) {
                DefaultUserUtils.log.warn(String.format("Unable to create group: %s", groupName));
            }
        }
        return Lists.newArrayList((Object[])new Group[] { existingGroup.get() });
    }
    
    @Override
    public Map<String, String> getDevelopers() {
        return this.getUsers(this.userUtil.getAllUsersInGroups((Collection)this.developerGroups.get()));
    }
    
    @Override
    public Map<String, String> getUsers() {
        return this.getUsers(this.userUtil.getAllUsersInGroups((Collection)this.userGroups.get()));
    }
    
    @Override
    public List<ApplicationUser> getApplicationUsers() {
        return this.userManager.getAllApplicationUsers().stream().filter(user -> !DefaultUserUtils.blacklistedUsers.contains(user.getUsername()) && user.isActive()).collect((Collector<? super Object, ?, List<ApplicationUser>>)Collectors.toList());
    }
    
    @Override
    public ApplicationUser createUser(final String username, final String email, final String displayName, final boolean addToDevs) throws Exception {
        return this.createUser(username, null, email, displayName, addToDevs, false);
    }
    
    @Override
    public void createAdmin(final String username, final String password, final String email, final String displayName) throws Exception {
        this.createUser(username, password, email, displayName, true, true);
    }
    
    private ApplicationUser createUser(final String username, final String password, final String email, final String displayName, final boolean addToDevs, final boolean addToAdmins) throws Exception {
        final ApplicationUser user = this.userService.createUser(this.userService.validateCreateUser(UserService.CreateUserRequest.withUserDetails((ApplicationUser)null, username, password, email, displayName).inDirectory((Long)null).sendNotification(false).withApplicationAccess((Set)null).withEventUserEvent(1).skipValidation()));
        for (final Group userGroup : (List)this.userGroups.get()) {
            this.userUtil.addUserToGroup(userGroup, user);
        }
        if (addToDevs) {
            for (final Group developerGroup : (List)this.developerGroups.get()) {
                this.userUtil.addUserToGroup(developerGroup, user);
            }
        }
        if (addToAdmins) {
            for (final Group adminGroup : (List)this.adminGroups.get()) {
                this.userUtil.addUserToGroup(adminGroup, user);
            }
        }
        return user;
    }
    
    @Override
    public String getDisplayName(final String username) {
        final String displayName = this.userUtil.getDisplayableNameSafely(this.userManager.getUserByName(username));
        return StringUtils.defaultString(displayName, username);
    }
    
    private Map<String, String> getUsers(final Set<ApplicationUser> users) {
        final ImmutableMap.Builder<String, String> builder = (ImmutableMap.Builder<String, String>)ImmutableMap.builder();
        for (final ApplicationUser user : users) {
            builder.put((Object)user.getName(), (Object)user.getDisplayName());
        }
        return (Map<String, String>)builder.build();
    }
    
    @Override
    public void addUserToGroup(final String groupName, final String userName) throws Exception {
        Group group = this.groupManager.getGroup(groupName);
        if (group == null) {
            group = this.crowdService.addGroup((Group)new ImmutableGroup(groupName));
        }
        this.userUtil.addUserToGroup(group, this.userManager.getUserByName(userName));
    }
    
    @Override
    public Group addGroup(final String groupName) throws Exception {
        return this.crowdService.addGroup((Group)new ImmutableGroup(groupName));
    }
    
    @Override
    public ApplicationUser getLoggedInUser() {
        return this.jiraAuthenticationContext.getLoggedInUser();
    }
    
    @Override
    public String getLoggedInUsername() {
        return this.getLoggedInUser().getName();
    }
    
    @Override
    public boolean hasPermission(final int permission) {
        return this.permissionManager.hasPermission(permission, this.getLoggedInUser());
    }
    
    @Override
    public ApplicationUser getUserByKey(final String userKey) {
        return this.userManager.getUserByKey(userKey);
    }
    
    @Override
    public ApplicationUser getUserByName(final String userName) {
        return this.userManager.getUserByName(userName);
    }
    
    static {
        log = LoggerFactory.getLogger((Class)DefaultUserUtils.class);
        (blacklistedUsers = new ArrayList<String>()).add("jdg-rest-admin");
        DefaultUserUtils.blacklistedUsers.add("sysadmin");
    }
}
