// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.project.Project;
import org.apache.commons.lang.time.StopWatch;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.project.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.drivers.CreateRapidViewDriver;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class RapidViewsGenerator implements DataGenerator
{
    private static final Logger LOG;
    @Autowired
    private CreateRapidViewDriver driver;
    @Autowired
    private ProjectManager projectManager;
    
    @Override
    public void generate(final GeneratorContext context) {
        final GeneratorConfiguration configuration = context.generatorConfiguration;
        if (configuration.kanbanBoards == 0 && configuration.scrumBoards == 0) {
            return;
        }
        final List<Project> projects = GeneratorConfigurationUtil.getProjects(context.generatorConfiguration, this.projectManager);
        final int kanbanBoardsPerProject = configuration.kanbanBoards / projects.size();
        final int remainingKanbanBoards = configuration.kanbanBoards % projects.size();
        final int scrumBoardsPerProject = configuration.scrumBoards / projects.size();
        final int remainingScrumBoards = configuration.scrumBoards % projects.size();
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        context.resetProgress(String.format("Generating %d boards", configuration.kanbanBoards + configuration.scrumBoards), configuration.kanbanBoards + configuration.scrumBoards);
        for (int i = 0; i < projects.size(); ++i) {
            final Project project = projects.get(i);
            final int kanbanCount = (i < remainingKanbanBoards) ? (kanbanBoardsPerProject + 1) : kanbanBoardsPerProject;
            for (int j = 0; j < kanbanCount; ++j) {
                this.driver.generateKanbanBoard(context, project);
                context.incProgress();
            }
            final int scrumCount = (i < remainingScrumBoards) ? (scrumBoardsPerProject + 1) : scrumBoardsPerProject;
            for (int k = 0; k < scrumCount; ++k) {
                this.driver.generateScrumBoard(context, project);
                context.incProgress();
            }
            RapidViewsGenerator.LOG.info("Generated {} Kanban boards and {} Scrum boards for project {}, {} of {}", new Object[] { kanbanCount, scrumCount, project.getName(), i, projects.size() });
        }
        stopWatch.stop();
        final String message = String.format("Created %d boards in %s", configuration.kanbanBoards + configuration.scrumBoards, stopWatch.toString());
        context.messages.add(message);
        RapidViewsGenerator.LOG.info(message);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)RapidViewsGenerator.class);
    }
}
