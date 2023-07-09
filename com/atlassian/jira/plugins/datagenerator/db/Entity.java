// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import java.util.Iterator;
import java.sql.SQLException;
import com.atlassian.plugin.util.Assertions;
import com.google.common.collect.Maps;
import java.util.Map;

public class Entity
{
    private static final String ID = "id";
    private final Map<String, Object> data;
    private final Map<String, Entity> externalRefs;
    
    public Entity() {
        this.data = Maps.newHashMap();
        this.externalRefs = Maps.newHashMap();
    }
    
    public Entity put(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }
    
    public Object get(final String key) {
        return this.data.get(key);
    }
    
    public Entity setRef(final String name, final Entity external) {
        this.externalRefs.put(name, external);
        return this;
    }
    
    public Long getId() {
        final Long id = this.data.get("id");
        Assertions.notNull("Referenced entity must be stored before referencing entity can retrieve the id", (Object)id);
        return id;
    }
    
    public void store(final EntityHandler entityHandler) throws SQLException {
        this.store(entityHandler, entityHandler);
    }
    
    public void store(final EntityHandler entityHandler, final EntityHandler idProvider) throws SQLException {
        for (final Map.Entry<String, Entity> ref : this.externalRefs.entrySet()) {
            this.data.put(ref.getKey(), ref.getValue().getId());
        }
        if (this.data.isEmpty()) {
            throw new IllegalStateException("Trying to store an uninitialized entity");
        }
        if (!this.data.containsKey("id")) {
            this.data.put("id", idProvider.getNextSequenceId());
        }
        entityHandler.store(this.data);
    }
}
