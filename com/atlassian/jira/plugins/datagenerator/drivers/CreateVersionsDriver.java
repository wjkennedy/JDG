// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.project.version.Version;
import java.util.Date;
import com.atlassian.jira.exception.CreateException;
import org.joda.time.DateTime;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.project.version.VersionManager;
import org.springframework.stereotype.Component;

@Component
public class CreateVersionsDriver
{
    private final VersionManager versionManager;
    
    @Autowired
    public CreateVersionsDriver(@ComponentImport final VersionManager versionManager) {
        this.versionManager = versionManager;
    }
    
    public void generate(final GeneratorContext context, final Project project, final int versionsCount) throws GenericEntityException, SQLException {
        Long scheduledAfter = null;
        for (int version = 1; version <= versionsCount; ++version) {
            final Date releaseDate = DateTime.now().plusWeeks(-1 * (versionsCount - version)).toDate();
            final Date startDate = DateTime.now().plusWeeks(-1 * (versionsCount - version + 1)).toDate();
            Version created = null;
            try {
                created = this.versionManager.createVersion(String.format("Version %d", version), startDate, releaseDate, "", project.getId(), scheduledAfter);
                context.incProgress();
            }
            catch (final CreateException e) {
                throw new RuntimeException((Throwable)e);
            }
            if (created != null) {
                scheduledAfter = created.getId();
            }
        }
    }
}
