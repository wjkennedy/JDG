// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;

public interface DataGenerator
{
    void generate(final GeneratorContext p0) throws GenericEntityException, SQLException;
}
