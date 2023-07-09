// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db.postgres;

import org.slf4j.LoggerFactory;
import java.util.Map;
import java.sql.SQLException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.postgresql.copy.CopyIn;
import com.atlassian.jira.util.Supplier;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.entity.model.ModelEntity;
import org.slf4j.Logger;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;

public class PostgresEntityHandler implements EntityHandler
{
    private static final Logger LOG;
    private final ModelEntity modelEntity;
    private final ModelField[] fieldsToSave;
    private final ByteArrayOutputStream byteStream;
    private final PrintWriter out;
    private final Supplier<CopyIn> copyInSupplier;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    private CopyIn copyIn;
    
    public PostgresEntityHandler(final JiraSequenceIdGenerator sequenceIdGenerator, final ModelEntity modelEntity, final Supplier<CopyIn> copyInSupplier) {
        this.modelEntity = modelEntity;
        this.byteStream = new ByteArrayOutputStream();
        this.fieldsToSave = modelEntity.getFieldsCopy().toArray(new ModelField[modelEntity.getFieldsSize()]);
        this.sequenceIdGenerator = sequenceIdGenerator;
        this.copyInSupplier = copyInSupplier;
        try {
            this.out = new PrintWriter(new OutputStreamWriter(this.byteStream, "UTF-8"));
        }
        catch (final UnsupportedEncodingException uee) {
            throw new Error(uee);
        }
    }
    
    @Override
    public Long getNextSequenceId() {
        return this.sequenceIdGenerator.getNextSequenceId(this.modelEntity.getEntityName());
    }
    
    @Override
    public void close() throws SQLException {
        if (this.copyIn != null) {
            this.copyIn.endCopy();
        }
        this.copyIn = null;
    }
    
    @Override
    public void store(final Map<String, Object> params) throws SQLException {
        boolean needTab = false;
        for (final ModelField field : this.fieldsToSave) {
            if (needTab) {
                this.out.print('\t');
            }
            else {
                needTab = true;
            }
            final Object val = params.get(field.getName());
            this.out.print((val == null) ? "\\N" : val.toString());
        }
        this.out.print('\n');
        this.out.flush();
        if (this.copyIn == null) {
            this.copyIn = (CopyIn)this.copyInSupplier.get();
        }
        final byte[] bytes = this.byteStream.toByteArray();
        this.copyIn.writeToCopy(bytes, 0, bytes.length);
        this.byteStream.reset();
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)PostgresEntityHandler.class);
    }
}
