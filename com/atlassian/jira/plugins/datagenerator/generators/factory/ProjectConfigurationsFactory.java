// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.factory;

import java.util.Iterator;
import com.google.common.collect.Maps;
import java.util.Map;
import com.atlassian.jira.project.Project;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Collections;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.plugins.access.DataPluginConfigurationAccessor;
import com.atlassian.jira.project.ProjectManager;
import org.springframework.stereotype.Component;

@Component
public class ProjectConfigurationsFactory
{
    private final ProjectManager projectManager;
    private final DataPluginConfigurationAccessor dataPluginConfigurationAccessor;
    
    @Autowired
    public ProjectConfigurationsFactory(@ComponentImport final ProjectManager projectManager, final DataPluginConfigurationAccessor dataPluginConfigurationAccessor) {
        this.projectManager = projectManager;
        this.dataPluginConfigurationAccessor = dataPluginConfigurationAccessor;
    }
    
    public Collection<ProjectConfiguration> build(final GeneratorConfiguration configuration) {
        final List<Project> projects = GeneratorConfigurationUtil.getProjects(configuration, this.projectManager);
        final Map<Project, Collection<String>> plugins = this.dataPluginConfigurationAccessor.getProjectsWithPlugins();
        return projects.stream().map(project -> {
            final Collection<String> pluginKeys = plugins.get(project);
            if (pluginKeys != null) {
                return new ProjectConfiguration(project, (Collection)pluginKeys);
            }
            else {
                return new ProjectConfiguration(project, (Collection)Collections.emptyList());
            }
        }).collect((Collector<? super Object, ?, Collection<ProjectConfiguration>>)Collectors.toList());
    }
    
    public static class ProjectConfigurations
    {
        public static Map<Project, Collection<String>> map(final Collection<ProjectConfiguration> configurations) {
            final Map<Project, Collection<String>> map = Maps.newHashMap();
            for (final ProjectConfiguration configuration : configurations) {
                map.put(configuration.getProject(), configuration.getPluginKeys());
            }
            return map;
        }
    }
    
    public class ProjectConfiguration
    {
        private final Project project;
        private final Collection<String> pluginKeys;
        
        public Collection<String> getPluginKeys() {
            return this.pluginKeys;
        }
        
        public Project getProject() {
            return this.project;
        }
        
        private ProjectConfiguration(final Project project, final Collection<String> pluginKeys) {
            this.project = project;
            this.pluginKeys = pluginKeys;
        }
    }
}
