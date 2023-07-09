// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.map.type.ClassKey;

public class RootNameLookup
{
    protected LRUMap<ClassKey, SerializedString> _rootNames;
    
    public SerializedString findRootName(final JavaType rootType, final MapperConfig<?> config) {
        return this.findRootName(rootType.getRawClass(), config);
    }
    
    public synchronized SerializedString findRootName(final Class<?> rootType, final MapperConfig<?> config) {
        final ClassKey key = new ClassKey(rootType);
        if (this._rootNames == null) {
            this._rootNames = new LRUMap<ClassKey, SerializedString>(20, 200);
        }
        else {
            final SerializedString name = this._rootNames.get(key);
            if (name != null) {
                return name;
            }
        }
        final BasicBeanDescription beanDesc = config.introspectClassAnnotations(rootType);
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        final AnnotatedClass ac = beanDesc.getClassInfo();
        String nameStr = intr.findRootName(ac);
        if (nameStr == null) {
            nameStr = rootType.getSimpleName();
        }
        final SerializedString name2 = new SerializedString(nameStr);
        this._rootNames.put(key, name2);
        return name2;
    }
}
