// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.user;

import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import org.springframework.stereotype.Component;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.plugins.datagenerator.creators.UniqueNameRequiringCreatorImpl;

@Component
public class UserCreator extends UniqueNameRequiringCreatorImpl<ApplicationUser>
{
    private final UserUtils userUtils;
    
    @Autowired
    public UserCreator(final UserUtils userUtils) {
        this.userUtils = userUtils;
    }
    
    @Override
    public ApplicationUser create(final String userName) {
        return new RetryFunction<ApplicationUser>().execute(() -> this.userUtils.createUser(userName, userName + "exampleemail.com", userName, true));
    }
}
