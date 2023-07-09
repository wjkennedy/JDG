// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import com.atlassian.jira.project.Project;

public interface ProjectUpdater
{
    void updateProjectCounters(final Project p0, final long p1);
    
    void updateCreatedProject(final Project p0);
}
