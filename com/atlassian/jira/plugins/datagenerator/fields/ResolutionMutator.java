// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.fields;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import java.util.Map;
import com.atlassian.jira.issue.resolution.Resolution;
import java.util.List;
import com.atlassian.jira.config.ConstantsManager;
import org.springframework.stereotype.Component;

@Component
public class ResolutionMutator implements FieldMutator
{
    private final ConstantsManager constantsManager;
    private List<Resolution> resolutions;
    private Map<String, Resolution> resolutionById;
    private GeneratorConfiguration configuration;
    
    @Autowired
    public ResolutionMutator(@ComponentImport final ConstantsManager constantsManager) {
        this.constantsManager = constantsManager;
    }
    
    @Override
    public String fieldType() {
        return "resolution";
    }
    
    @Override
    public void handle(final Map<Object, Object> sink) {
        if (RandomUtils.nextFloat() < this.configuration.transitions.randomizeResolutionProbability) {
            sink.put("resolution", Randomizer.randomItem(this.resolutions).getId());
        }
    }
    
    @Override
    public ChangeItemBean handle(final Entity issue, final Timestamp currentNow) {
        if (RandomUtils.nextFloat() < this.configuration.transitions.randomizeResolutionProbability) {
            final String prevResolution = (String)issue.get("resolution");
            boolean sameResolution;
            String resolutionId;
            do {
                resolutionId = Randomizer.randomItem(this.resolutions).getId();
                sameResolution = StringUtils.equals(prevResolution, resolutionId);
                if (sameResolution && this.resolutions.size() <= 1) {
                    return null;
                }
            } while (sameResolution);
            return this.updateResolution(issue, resolutionId, currentNow);
        }
        return null;
    }
    
    public ChangeItemBean updateResolution(final Entity issue, final String resolutionId, final Timestamp currentNow) {
        final String oldResolutionId = (String)issue.get("resolution");
        final String oldResolutionName = (oldResolutionId == null) ? null : this.resolutionById.get(oldResolutionId).getName();
        final String resolutionName = (resolutionId == null) ? null : this.resolutionById.get(resolutionId).getName();
        issue.put("resolution", resolutionId);
        issue.put("resolutiondate", currentNow);
        return new ChangeItemBean("jira", "resolution", oldResolutionId, oldResolutionName, resolutionId, resolutionName);
    }
    
    @Override
    public void init(final GeneratorContext generatorContext, final IssueCreationParameters issueCreationParameters) {
        this.configuration = generatorContext.generatorConfiguration;
        this.resolutions = (List<Resolution>)ImmutableList.copyOf(this.constantsManager.getResolutions());
        this.resolutionById = (Map<String, Resolution>)Maps.uniqueIndex((Iterable)this.resolutions, input -> input.getId());
    }
}
