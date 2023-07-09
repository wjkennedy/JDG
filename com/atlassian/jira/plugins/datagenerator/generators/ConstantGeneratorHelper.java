// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.issue.IssueConstant;
import java.util.Collection;

public interface ConstantGeneratorHelper
{
    Collection<? extends IssueConstant> getConstants();
    
    int constantCount(final GeneratorContext p0);
    
    void refresh();
    
    String getPluralName();
    
    String getConstantName();
    
    void fillAdditionalData(final Map<String, Object> p0);
}
