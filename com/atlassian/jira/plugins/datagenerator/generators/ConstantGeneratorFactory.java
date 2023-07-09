// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.config.ConstantsManager;
import java.util.HashMap;
import com.atlassian.jira.component.ComponentAccessor;
import java.util.Map;
import org.ofbiz.core.entity.DelegatorInterface;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConstantGeneratorFactory
{
    private final List<ConstantGeneratorHelper> helpers;
    private final DelegatorInterface delegator;
    private final Map<String, String> enititiesMap;
    
    @Autowired
    public ConstantGeneratorFactory(final List<ConstantGeneratorHelper> helpers) {
        this.helpers = helpers;
        this.delegator = (DelegatorInterface)ComponentAccessor.getComponent((Class)DelegatorInterface.class);
        (this.enititiesMap = new HashMap<String, String>()).put(ConstantsManager.CONSTANT_TYPE.STATUS.getType(), "Status");
        this.enititiesMap.put(ConstantsManager.CONSTANT_TYPE.RESOLUTION.getType(), "Resolution");
    }
    
    public ConstantGenerator createGenerator(final String constantName) {
        final ConstantGeneratorHelper generatorHelper = this.helpers.stream().filter(helper -> helper.getConstantName().equals(constantName)).findFirst().get();
        return new ConstantGenerator(generatorHelper, this.delegator, this.enititiesMap.get(constantName));
    }
}
