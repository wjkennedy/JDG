// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import com.google.common.collect.Lists;
import com.atlassian.jira.project.Project;
import java.util.List;
import com.atlassian.jira.project.ProjectManager;
import javax.annotation.Nonnull;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;

public class GeneratorConfigurationUtil
{
    @Nonnull
    public static List<Project> getProjects(@Nonnull final GeneratorConfiguration generatorConfiguration, @Nonnull final ProjectManager projectManager) {
        final List<Project> projects = Lists.newArrayList();
        if (generatorConfiguration.projectIds != null) {
            for (final long id : generatorConfiguration.projectIds) {
                final Project project = projectManager.getProjectObj(Long.valueOf(id));
                if (project != null) {
                    projects.add(project);
                }
            }
        }
        return projects;
    }
}
