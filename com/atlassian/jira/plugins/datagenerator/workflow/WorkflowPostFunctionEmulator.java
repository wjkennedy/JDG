// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.workflow;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.workflow.function.misc.CreateCommentFunction;
import java.util.Iterator;
import com.google.common.collect.Lists;
import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class WorkflowPostFunctionEmulator
{
    private static final Logger log;
    private static final String CLASS_NAME = "class.name";
    private final Map<String, WorkflowFunctionEmulator> emulatorMap;
    
    @Autowired
    public WorkflowPostFunctionEmulator(final List<WorkflowFunctionEmulator> emulators) {
        this.emulatorMap = (Map<String, WorkflowFunctionEmulator>)Maps.uniqueIndex((Iterable)emulators, WorkflowFunctionEmulator::className);
    }
    
    public Collection<ChangeItemBean> emulatePostFunctions(final Entity issue, final Collection<FunctionDescriptor> functions, final Timestamp currentNow) {
        final List<ChangeItemBean> cibs = Lists.newArrayList();
        for (final FunctionDescriptor function : functions) {
            final ChangeItemBean cib = this.emulatePostFunction(issue, function, currentNow);
            if (cib != null) {
                cibs.add(cib);
            }
        }
        return cibs;
    }
    
    public ChangeItemBean emulatePostFunction(final Entity issue, final FunctionDescriptor function, final Timestamp currentNow) {
        final String className = function.getArgs().get("class.name");
        final WorkflowFunctionEmulator emulator = this.emulatorMap.get(className);
        return (emulator == null) ? null : emulator.emulate(issue, function, currentNow);
    }
    
    public boolean processesComments(final Collection<FunctionDescriptor> functions) {
        return functions.stream().anyMatch(input -> CreateCommentFunction.class.getName().equals(input.getArgs().get("class.name")));
    }
    
    static {
        log = LoggerFactory.getLogger((Class)WorkflowPostFunctionEmulator.class);
    }
}
