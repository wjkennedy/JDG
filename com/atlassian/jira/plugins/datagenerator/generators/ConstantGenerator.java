// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import java.util.Map;
import com.atlassian.jira.entity.EntityUtils;
import com.google.common.collect.Maps;
import org.ofbiz.core.entity.GenericEntityException;
import org.apache.commons.lang.time.StopWatch;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.ofbiz.core.entity.DelegatorInterface;

public class ConstantGenerator
{
    private final ConstantGeneratorHelper constantGeneratorHelper;
    private final DelegatorInterface delegator;
    private final String entityName;
    
    public ConstantGenerator(final ConstantGeneratorHelper singleConstantGenerator, final DelegatorInterface delegator, final String entityName) {
        this.constantGeneratorHelper = singleConstantGenerator;
        this.delegator = delegator;
        this.entityName = entityName;
    }
    
    public void generate(final GeneratorContext context) throws GenericEntityException {
        final int constantCount = this.constantGeneratorHelper.constantCount(context);
        if (constantCount <= 0) {
            return;
        }
        final StopWatch stopWatch = new StopWatch();
        context.resetProgress(String.format("Generating additional %s (%d in total)", this.constantGeneratorHelper.getPluralName(), constantCount), constantCount);
        stopWatch.start();
        for (int i = 0; i < constantCount; ++i) {
            this.createRandomConstant();
            context.incProgress();
        }
        this.constantGeneratorHelper.refresh();
        stopWatch.stop();
        context.messages.add(String.format("Generated %d issue %s in %s", constantCount, this.constantGeneratorHelper.getPluralName(), stopWatch.toString()));
    }
    
    private void createRandomConstant() {
        final long sequenceId = this.delegator.getNextSeqId(this.entityName);
        final Map<String, Object> constantFields = Maps.newHashMap();
        constantFields.put("id", sequenceId);
        constantFields.put("name", this.constantGeneratorHelper.getConstantName() + " " + sequenceId);
        constantFields.put("description", "Autogenerated " + this.constantGeneratorHelper.getConstantName() + " " + sequenceId);
        this.constantGeneratorHelper.fillAdditionalData(constantFields);
        constantFields.put("sequence", sequenceId);
        EntityUtils.createValue(this.constantGeneratorHelper.getConstantName(), (Map)constantFields);
    }
}
