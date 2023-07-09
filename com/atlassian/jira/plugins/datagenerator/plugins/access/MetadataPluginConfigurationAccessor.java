// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.plugins.access;

import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.plugins.datagenerator.config.module.MetadataPluginConfiguration;
import com.atlassian.jira.plugins.datagenerator.config.module.ConfigurationField;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.google.common.collect.Maps;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.plugin.Plugin;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import com.atlassian.plugin.PluginState;
import com.atlassian.jira.plugins.datagenerator.descriptor.MetadataFactoryModuleDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.google.common.base.Preconditions;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import java.util.Map;
import com.google.common.base.Predicate;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.ozymandias.SafeAccessViaPluginAccessor;
import org.springframework.stereotype.Component;

@Component
public class MetadataPluginConfigurationAccessor
{
    private final SafeAccessViaPluginAccessor safeAccessor;
    private final PluginAccessor pluginAccessor;
    private static final Predicate<Map.Entry<String, String>> USER_MAP_FILTER;
    
    @Autowired
    public MetadataPluginConfigurationAccessor(@ComponentImport final PluginAccessor pluginAccessor) {
        Preconditions.checkArgument(pluginAccessor != null, (Object)"pluginAccessor is required");
        this.safeAccessor = SafePluginPointAccess.to(pluginAccessor);
        this.pluginAccessor = pluginAccessor;
    }
    
    public Map<String, Boolean> getEnabledStates() {
        final List<Map<String, Boolean>> results = this.safeAccessor.forType(MetadataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> {
            final String pluginKey = module.getPluginKey();
            final Plugin plugin = this.pluginAccessor.getPlugin(pluginKey);
            return Collections.singletonMap(pluginKey, plugin != null && plugin.getPluginState().equals((Object)PluginState.ENABLED));
        });
        final ImmutableMap.Builder<String, Boolean> builder = (ImmutableMap.Builder<String, Boolean>)ImmutableMap.builder();
        for (final Map<String, Boolean> value : results) {
            builder.putAll((Map)value);
        }
        return (Map<String, Boolean>)builder.build();
    }
    
    public List<MetadataPluginConfigurationValidationData> validateModules(final GeneratorConfiguration generatorConfiguration) {
        return (List<MetadataPluginConfigurationValidationData>)ImmutableList.copyOf((Collection)this.safeAccessor.forType(MetadataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> {
            final String pluginName = module.getPluginName();
            final String pluginKey = module.getPluginKey();
            final ErrorCollection errors = module.validate(generatorConfiguration);
            if (errors != null) {
                new MetadataPluginConfigurationValidationData(pluginKey, pluginName, (ErrorCollection)new ImmutableErrorCollection(errors));
                return;
            }
            else {
                return new MetadataPluginConfigurationValidationData(pluginKey, pluginName, errors);
            }
        }));
    }
    
    public Map<String, String> prepareModuleUsersForExecution() {
        final List<Map<String, String>> results = this.safeAccessor.forType(MetadataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> {
            final Map<String, String> map = module.prepareUsersForExecution();
            if (map == null) {
                return (Map<String, String>)Collections.emptyMap();
            }
            else {
                return Maps.newHashMap((Map)map);
            }
        });
        final Map<String, String> userMap = Maps.newHashMap();
        for (final Map<String, String> value : results) {
            userMap.putAll(Maps.filterEntries((Map)value, (Predicate)MetadataPluginConfigurationAccessor.USER_MAP_FILTER));
        }
        return (Map<String, String>)ImmutableMap.copyOf((Map)userMap);
    }
    
    public void executeModules(final GeneratorContext generatorContext) {
        this.safeAccessor.forType(MetadataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> module.execute(generatorContext));
    }
    
    public List<MetadataPluginConfigurationFormData> getFormGenerationValues() {
        return (List<MetadataPluginConfigurationFormData>)ImmutableList.copyOf((Collection)this.safeAccessor.forType(MetadataFactoryModuleDescriptor.class, (moduleDescriptor, module) -> {
            final Collection<ConfigurationField> fields = module.getConfigurationFields();
            final String pluginName = module.getPluginName();
            final String pluginKey = module.getPluginKey();
            return new MetadataPluginConfigurationFormData(pluginKey, pluginName, fields);
        }));
    }
    
    static {
        USER_MAP_FILTER = (input -> StringUtils.isNotBlank((String)input.getKey()) && StringUtils.isNotBlank((String)input.getValue()));
    }
}
