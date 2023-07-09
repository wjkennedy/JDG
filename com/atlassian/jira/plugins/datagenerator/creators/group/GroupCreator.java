// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.group;

import java.util.function.Function;
import java.util.List;
import com.atlassian.jira.user.ApplicationUser;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.creators.listeners.CreationEmitter;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import org.springframework.stereotype.Component;
import com.atlassian.jira.plugins.datagenerator.creators.CreatedEvent;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.plugins.datagenerator.creators.UniqueNameRequiringCreatorImpl;

@Component
public class GroupCreator extends UniqueNameRequiringCreatorImpl<Group> implements CreatedEvent<GroupCreator>
{
    private final UserUtils userUtils;
    private final GroupMemberCreator groupMemberCreator;
    private final CreationEmitter emitter;
    
    @Autowired
    public GroupCreator(final UserUtils userUtils, final GroupMemberCreator groupMemberCreator) {
        this.emitter = new CreationEmitter();
        this.userUtils = userUtils;
        this.groupMemberCreator = groupMemberCreator;
    }
    
    @Override
    public Group create(final String groupName) {
        return new RetryFunction<Group>("create group").execute(() -> {
            final Group group = this.userUtils.addGroup(groupName);
            this.emitter.emit(1);
            return group;
        });
    }
    
    public List<Group> createWithMembers(final int groups, final Collection<ApplicationUser> members) {
        final List<Group> generated = this.create(groups);
        generated.forEach(group -> this.groupMemberCreator.create(group, members));
        return generated;
    }
    
    @Override
    public GroupCreator onCreated(final Function<Integer, ?> lambda) {
        this.emitter.register(lambda);
        return this;
    }
}
