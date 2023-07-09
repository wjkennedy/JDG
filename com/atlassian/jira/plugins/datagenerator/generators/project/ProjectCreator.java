// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.project;

import com.atlassian.jira.project.Project;

public interface ProjectCreator
{
    Project createProject(final String p0, final String p1) throws ProjectCreationException;
}
