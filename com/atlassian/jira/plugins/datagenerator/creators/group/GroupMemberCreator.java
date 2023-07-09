// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.group;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.crowd.embedded.api.Group;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberCreator
{
    private final UserUtils userUtils;
    
    @Autowired
    public GroupMemberCreator(final UserUtils userUtils) {
        this.userUtils = userUtils;
    }
    
    public void create(final Group group, final ApplicationUser member) {
        new RetryFunction<Void>("add group member").execute(() -> {
            this.userUtils.addUserToGroup(group.getName(), member.getUsername());
            return null;
        });
    }
    
    public void create(final Group group, final Collection<ApplicationUser> members) {
        for (final ApplicationUser member : members) {
            this.create(group, member);
        }
    }
    
    public void create(final List<Group> groups, final Collection<ApplicationUser> members) {
        for (final Group group : groups) {
            this.create(group, members);
        }
    }
}
