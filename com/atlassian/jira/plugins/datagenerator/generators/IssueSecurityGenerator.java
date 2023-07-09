// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.util.Random;
import java.util.Collections;
import com.atlassian.jira.scheme.SchemeEntity;
import com.google.common.collect.Sets;
import com.google.common.collect.ArrayListMultimap;
import org.apache.commons.lang.time.StopWatch;
import java.util.Iterator;
import com.atlassian.jira.project.Project;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.RandomStringUtils;
import com.google.common.collect.Lists;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.google.common.collect.Multimap;
import com.atlassian.jira.scheme.Scheme;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.base.Suppliers;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.base.Supplier;
import com.atlassian.crowd.embedded.api.Group;
import java.util.Collection;
import com.atlassian.jira.user.ApplicationUser;
import java.util.Set;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class IssueSecurityGenerator implements DataGenerator
{
    private static final Logger LOG;
    private static final UserSearchParams STRONGLY_CONSISTENT_ACTIVE_AND_INACTIVE_USERS_ALLOW_EMPTY_QUERY;
    private static final String EMPTY_QUERY = "";
    private final UserSearchService userSearchService;
    private final GroupManager groupManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final ProjectManager projectManager;
    private Set<ApplicationUser> users;
    private Collection<Group> groups;
    private Set<SecurityLevelType> availableLevels;
    private Supplier<Iterable<ApplicationUser>> usersSupplier;
    private Supplier<Collection<Group>> groupsSupplier;
    
    @Autowired
    public IssueSecurityGenerator(@ComponentImport final IssueSecuritySchemeManager issueSecuritySchemeManager, @ComponentImport final IssueSecurityLevelManager issueSecurityLevelManager, @ComponentImport final UserSearchService userSearchService, @ComponentImport final GroupManager groupManager, @ComponentImport final ProjectManager projectManager) {
        this.usersSupplier = (Supplier<Iterable<ApplicationUser>>)Suppliers.memoize((Supplier)new Supplier<Iterable<ApplicationUser>>() {
            public Iterable<ApplicationUser> get() {
                return IssueSecurityGenerator.this.userSearchService.findUsers("", IssueSecurityGenerator.STRONGLY_CONSISTENT_ACTIVE_AND_INACTIVE_USERS_ALLOW_EMPTY_QUERY);
            }
        });
        this.groupsSupplier = (Supplier<Collection<Group>>)Suppliers.memoize((Supplier)new Supplier<Collection<Group>>() {
            public Collection<Group> get() {
                return IssueSecurityGenerator.this.groupManager.getAllGroups();
            }
        });
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.userSearchService = userSearchService;
        this.groupManager = groupManager;
        this.projectManager = projectManager;
    }
    
    @Override
    public void generate(final GeneratorContext context) throws GenericEntityException, SQLException {
        if (context.generatorConfiguration.issueSecuritySchemes.schemesCount == 0 || context.generatorConfiguration.issueSecuritySchemes.levelsPerScheme == 0 || context.generatorConfiguration.issueSecuritySchemes.permissionsPerLevel == 0) {
            IssueSecurityGenerator.LOG.debug("Skipping creating issue security schemas");
            return;
        }
        this.resetPermissionOptionSets();
        final List<Scheme> schemes = this.generateIssueSecuritySchemes(context.generatorConfiguration.issueSecuritySchemes.schemesCount);
        final Multimap<Long, IssueSecurityLevel> schemesAndLevels = this.generateIssueSecurityLevelsForSchemes(context, schemes);
        this.assignSchemesToProjects(schemesAndLevels, context);
    }
    
    private List<Scheme> generateIssueSecuritySchemes(final int schemesCount) {
        final List<Scheme> schemes = Lists.newArrayList();
        for (int i = 1; i <= schemesCount; ++i) {
            schemes.add(this.issueSecuritySchemeManager.createSchemeObject("Generated scheme " + RandomStringUtils.randomAlphanumeric(5), ""));
        }
        return schemes;
    }
    
    public void assignSchemesToProjects(final Multimap<Long, IssueSecurityLevel> schemesAndLevels, final GeneratorContext context) {
        final Iterator<Long> schemeKeys = Iterables.cycle((Iterable)schemesAndLevels.keySet()).iterator();
        final StopWatch stopWatch = this.startTimer();
        for (final Project project : this.projectManager.getProjectObjects()) {
            this.issueSecuritySchemeManager.setSchemeForProject(project, Long.valueOf(schemeKeys.next()));
        }
        this.stopAssignSchemeTimer(stopWatch, context);
    }
    
    private Multimap<Long, IssueSecurityLevel> generateIssueSecurityLevelsForSchemes(final GeneratorContext context, final List<Scheme> schemes) {
        final StopWatch stopWatch = this.startTimer();
        final Multimap<Long, IssueSecurityLevel> levelsFromSchemes = (Multimap<Long, IssueSecurityLevel>)ArrayListMultimap.create();
        for (final Scheme scheme : schemes) {
            for (int i = 1; i <= context.generatorConfiguration.issueSecuritySchemes.levelsPerScheme; ++i) {
                final IssueSecurityLevel issueSecurityLevel = this.issueSecurityLevelManager.createIssueSecurityLevel((long)scheme.getId(), scheme.getName() + "-Level-" + i, "");
                levelsFromSchemes.put((Object)scheme.getId(), (Object)issueSecurityLevel);
                for (int j = 1; j <= context.generatorConfiguration.issueSecuritySchemes.permissionsPerLevel; ++j) {
                    try {
                        this.generateSecurityLevelPermission(issueSecurityLevel);
                    }
                    catch (final Exception e) {
                        IssueSecurityGenerator.LOG.error("Error when creating security level permission, skipping ...", (Throwable)e);
                    }
                }
                this.resetPermissionOptionSets();
            }
        }
        this.stopSchemeTimer(stopWatch, context);
        return levelsFromSchemes;
    }
    
    private void resetPermissionOptionSets() {
        this.users = Sets.newHashSet((Iterable)this.usersSupplier.get());
        this.groups = Sets.newHashSet((Iterable)this.groupsSupplier.get());
        this.availableLevels = Sets.newHashSet((Object[])SecurityLevelType.values());
    }
    
    private void generateSecurityLevelPermission(final IssueSecurityLevel issueSecurityLevel) throws GenericEntityException {
        final SecurityLevelType levelType = this.getRandomType();
        if (levelType != null) {
            final SchemeEntity schemeEntity = new SchemeEntity(levelType.getName(), this.getParameterForType(levelType), (Object)issueSecurityLevel.getId());
            this.issueSecuritySchemeManager.createSchemeEntity(this.issueSecuritySchemeManager.getScheme(issueSecurityLevel.getSchemeId()), schemeEntity);
            if (levelType.isUnique()) {
                this.availableLevels.remove(levelType);
            }
        }
    }
    
    private String getParameterForType(final SecurityLevelType type) {
        switch (type) {
            case REPORTER:
            case CURRENT_ASSIGNEE:
            case PROJECT_LEAD: {
                return null;
            }
            case USER: {
                return this.getRandomUser().getName();
            }
            case GROUP: {
                return this.getRandomGroup().getName();
            }
            default: {
                throw new IllegalArgumentException("Invalid security level type");
            }
        }
    }
    
    private SecurityLevelType getRandomType() {
        if (this.availableLevels.isEmpty()) {
            return null;
        }
        final List<SecurityLevelType> levels = Lists.newArrayList((Iterable)this.availableLevels);
        Collections.shuffle(levels);
        return levels.get(0);
    }
    
    public ApplicationUser getRandomUser() {
        if (this.users.isEmpty()) {
            this.availableLevels.remove(SecurityLevelType.USER);
            throw new IllegalArgumentException("Invalid security level type");
        }
        final int index = new Random().nextInt(this.users.size());
        final ApplicationUser user = this.users.toArray(new ApplicationUser[this.users.size()])[index];
        this.users.remove(user);
        return user;
    }
    
    public Group getRandomGroup() {
        if (this.groups.isEmpty()) {
            this.availableLevels.remove(SecurityLevelType.GROUP);
            throw new IllegalArgumentException("Invalid security level type");
        }
        final int index = new Random().nextInt(this.groups.size());
        final Group group = this.groups.toArray(new Group[this.groups.size()])[index];
        this.groups.remove(group);
        return group;
    }
    
    private StopWatch startTimer() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }
    
    private void stopSchemeTimer(final StopWatch stopWatch, final GeneratorContext context) {
        stopWatch.stop();
        final String message = String.format("Generated %d schemes with %d levels and %d permissions on each level in %s", context.generatorConfiguration.issueSecuritySchemes.schemesCount, context.generatorConfiguration.issueSecuritySchemes.levelsPerScheme, context.generatorConfiguration.issueSecuritySchemes.permissionsPerLevel, stopWatch.toString());
        context.messages.add(message);
    }
    
    private void stopAssignSchemeTimer(final StopWatch stopWatch, final GeneratorContext context) {
        stopWatch.stop();
        final String message = String.format("Assigned issue security schemes to all projects in %s", stopWatch.toString());
        context.messages.add(message);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)IssueSecurityGenerator.class);
        STRONGLY_CONSISTENT_ACTIVE_AND_INACTIVE_USERS_ALLOW_EMPTY_QUERY = UserSearchParams.builder().allowEmptyQuery(true).forceStrongConsistency(true).includeActive(true).includeInactive(true).build();
    }
    
    enum SecurityLevelType
    {
        REPORTER("reporter", true), 
        GROUP("group", false), 
        USER("user", false), 
        PROJECT_LEAD("lead", true), 
        CURRENT_ASSIGNEE("assignee", true);
        
        private final String name;
        private final boolean unique;
        
        private SecurityLevelType(final String name, final boolean unique) {
            this.name = name;
            this.unique = unique;
        }
        
        boolean isUnique() {
            return this.unique;
        }
        
        String getName() {
            return this.name;
        }
    }
}
