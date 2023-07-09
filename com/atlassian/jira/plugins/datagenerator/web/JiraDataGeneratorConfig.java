// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.web;

import org.slf4j.LoggerFactory;
import webwork.action.ActionContext;
import com.atlassian.jira.web.action.ActionViewData;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.codehaus.jackson.map.SerializationConfig;
import com.atlassian.jira.permission.GlobalPermissionKey;
import java.io.IOException;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.ObjectMapper;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import org.apache.commons.lang.StringUtils;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.jira.plugins.datagenerator.drivers.DataGeneratorDriver;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.slf4j.Logger;
import com.atlassian.jira.security.request.RequestMethod;
import com.atlassian.jira.security.request.SupportedMethods;

@SupportedMethods({ RequestMethod.GET })
public class JiraDataGeneratorConfig extends StatusAwareGeneratorAction
{
    private static final Logger LOG;
    private static final String CONFIG_KEY;
    private String configText;
    protected final GeneratorContext context;
    boolean restoreDefaults;
    private boolean generate;
    
    public JiraDataGeneratorConfig(final DataGeneratorDriver dataGeneratorDriver, final PageBuilderService pageBuilderService) {
        super(dataGeneratorDriver, pageBuilderService);
        this.context = new GeneratorContext();
        this.configText = this.getConfigFromSession();
    }
    
    protected void doValidation() {
        if (this.generate) {
            if (StringUtils.isBlank(this.configText)) {
                this.addError("configText", "Please provide config text.");
                return;
            }
            try {
                this.context.generatorConfiguration = new ObjectMapper().readValue(this.configText, new TypeReference<GeneratorConfiguration>() {});
            }
            catch (final IOException e) {
                JiraDataGeneratorConfig.LOG.info("Error parsing config", (Throwable)e);
                this.addError("configText", "Error parsing config: " + e.getMessage());
            }
        }
    }
    
    @RequiresXsrfCheck
    @SupportedMethods({ RequestMethod.POST })
    public String doExecute() throws Exception {
        if (!this.hasGlobalPermission(GlobalPermissionKey.SYSTEM_ADMIN)) {
            return "permissionviolation";
        }
        this.validate();
        if (this.generate) {
            this.storeConfigInSession(this.configText);
            this.context.userName = this.getLoggedInUser().getName();
            this.dataGeneratorDriver.scheduleBothPhases(this.context);
            return this.getRedirect("JiraDataGeneratorConfig.jspa");
        }
        if (this.restoreDefaults) {
            this.storeConfigInSession(this.configText = new ObjectMapper().configure(SerializationConfig.Feature.INDENT_OUTPUT, true).writeValueAsString(new GeneratorConfiguration()));
            return this.getRedirect("JiraDataGeneratorConfig.jspa");
        }
        return "input";
    }
    
    @ActionViewData
    public Map<String, Object> getSoyData() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"configText", (Object)this.getConfigText()).put((Object)"errors", (Object)this.getErrorsMap()).put((Object)"xsrfToken", (Object)this.getXsrfToken()).build();
    }
    
    public String getConfigText() {
        return this.configText;
    }
    
    public void setConfigText(final String configText) {
        this.configText = configText;
    }
    
    public void setRestoreDefaults(final String s) {
        this.restoreDefaults = true;
    }
    
    public void setGenerate(final String s) {
        this.generate = true;
    }
    
    private void storeConfigInSession(final String config) {
        ActionContext.getSession().put(JiraDataGeneratorConfig.CONFIG_KEY, config);
    }
    
    private String getConfigFromSession() {
        final Object config = ActionContext.getSession().get(JiraDataGeneratorConfig.CONFIG_KEY);
        return (String)((config instanceof String) ? config : "");
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)JiraDataGeneratorConfig.class);
        CONFIG_KEY = JiraDataGeneratorConfig.class.getName() + ".generatorConfig";
    }
}
