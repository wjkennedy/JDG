// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import java.util.Iterator;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import java.util.ArrayList;
import org.ofbiz.core.entity.model.ModelReader;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import java.util.List;
import com.atlassian.jira.component.ComponentAccessor;
import org.ofbiz.core.entity.DelegatorInterface;
import org.springframework.stereotype.Component;

@Component
public class FieldDetector
{
    private final DelegatorInterface delegator;
    
    public FieldDetector() {
        this.delegator = (DelegatorInterface)ComponentAccessor.getComponent((Class)DelegatorInterface.class);
    }
    
    public boolean isFieldInEntity(final String entityName, final String fieldName) throws SQLException, GenericEntityException {
        final List<String> fieldNames = this.getFieldNames(entityName);
        return fieldNames.contains(fieldName);
    }
    
    private List<String> getFieldNames(final String entityName) throws GenericEntityException {
        final ModelReader modelReader = ModelReader.getModelReader(this.delegator.getDelegatorName());
        final ModelEntity modelEntity = modelReader.getModelEntity(entityName);
        final int fieldCount = modelEntity.getFieldsSize();
        final List<String> fieldNames = new ArrayList<String>(fieldCount);
        final List<ModelField> fieldsCopy = modelEntity.getFieldsCopy();
        for (final ModelField field : fieldsCopy) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }
}
