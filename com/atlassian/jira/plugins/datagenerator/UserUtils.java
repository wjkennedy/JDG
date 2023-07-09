// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.user.ApplicationUser;
import java.util.List;
import java.util.Map;

public interface UserUtils
{
    Map<String, String> getDevelopers();
    
    Map<String, String> getUsers();
    
    List<ApplicationUser> getApplicationUsers();
    
    ApplicationUser createUser(final String p0, final String p1, final String p2, final boolean p3) throws Exception;
    
    void createAdmin(final String p0, final String p1, final String p2, final String p3) throws Exception;
    
    String getDisplayName(final String p0);
    
    void addUserToGroup(final String p0, final String p1) throws Exception;
    
    Group addGroup(final String p0) throws Exception;
    
    ApplicationUser getLoggedInUser();
    
    String getLoggedInUsername();
    
    boolean hasPermission(final int p0);
    
    ApplicationUser getUserByKey(final String p0);
    
    ApplicationUser getUserByName(final String p0);
}
