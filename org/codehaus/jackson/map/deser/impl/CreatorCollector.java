// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.impl;

import org.codehaus.jackson.map.util.ClassUtil;
import java.lang.reflect.Member;
import java.util.HashMap;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.deser.std.StdValueInstantiator;
import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.introspect.AnnotatedWithParams;
import org.codehaus.jackson.map.introspect.AnnotatedConstructor;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;

public class CreatorCollector
{
    final BasicBeanDescription _beanDesc;
    final boolean _canFixAccess;
    protected AnnotatedConstructor _defaultConstructor;
    protected AnnotatedWithParams _stringCreator;
    protected AnnotatedWithParams _intCreator;
    protected AnnotatedWithParams _longCreator;
    protected AnnotatedWithParams _doubleCreator;
    protected AnnotatedWithParams _booleanCreator;
    protected AnnotatedWithParams _delegateCreator;
    protected AnnotatedWithParams _propertyBasedCreator;
    protected CreatorProperty[] _propertyBasedArgs;
    
    public CreatorCollector(final BasicBeanDescription beanDesc, final boolean canFixAccess) {
        this._propertyBasedArgs = null;
        this._beanDesc = beanDesc;
        this._canFixAccess = canFixAccess;
    }
    
    public ValueInstantiator constructValueInstantiator(final DeserializationConfig config) {
        final StdValueInstantiator inst = new StdValueInstantiator(config, this._beanDesc.getType());
        JavaType delegateType;
        if (this._delegateCreator == null) {
            delegateType = null;
        }
        else {
            final TypeBindings bindings = this._beanDesc.bindingsForBeanType();
            delegateType = bindings.resolveType(this._delegateCreator.getParameterType(0));
        }
        inst.configureFromObjectSettings(this._defaultConstructor, this._delegateCreator, delegateType, this._propertyBasedCreator, this._propertyBasedArgs);
        inst.configureFromStringCreator(this._stringCreator);
        inst.configureFromIntCreator(this._intCreator);
        inst.configureFromLongCreator(this._longCreator);
        inst.configureFromDoubleCreator(this._doubleCreator);
        inst.configureFromBooleanCreator(this._booleanCreator);
        return inst;
    }
    
    public void setDefaultConstructor(final AnnotatedConstructor ctor) {
        this._defaultConstructor = ctor;
    }
    
    public void addStringCreator(final AnnotatedWithParams creator) {
        this._stringCreator = this.verifyNonDup(creator, this._stringCreator, "String");
    }
    
    public void addIntCreator(final AnnotatedWithParams creator) {
        this._intCreator = this.verifyNonDup(creator, this._intCreator, "int");
    }
    
    public void addLongCreator(final AnnotatedWithParams creator) {
        this._longCreator = this.verifyNonDup(creator, this._longCreator, "long");
    }
    
    public void addDoubleCreator(final AnnotatedWithParams creator) {
        this._doubleCreator = this.verifyNonDup(creator, this._doubleCreator, "double");
    }
    
    public void addBooleanCreator(final AnnotatedWithParams creator) {
        this._booleanCreator = this.verifyNonDup(creator, this._booleanCreator, "boolean");
    }
    
    public void addDelegatingCreator(final AnnotatedWithParams creator) {
        this._delegateCreator = this.verifyNonDup(creator, this._delegateCreator, "delegate");
    }
    
    public void addPropertyCreator(final AnnotatedWithParams creator, final CreatorProperty[] properties) {
        this._propertyBasedCreator = this.verifyNonDup(creator, this._propertyBasedCreator, "property-based");
        if (properties.length > 1) {
            final HashMap<String, Integer> names = new HashMap<String, Integer>();
            for (int i = 0, len = properties.length; i < len; ++i) {
                final String name = properties[i].getName();
                if (name.length() != 0 || properties[i].getInjectableValueId() == null) {
                    final Integer old = names.put(name, i);
                    if (old != null) {
                        throw new IllegalArgumentException("Duplicate creator property \"" + name + "\" (index " + old + " vs " + i + ")");
                    }
                }
            }
        }
        this._propertyBasedArgs = properties;
    }
    
    protected AnnotatedWithParams verifyNonDup(final AnnotatedWithParams newOne, final AnnotatedWithParams oldOne, final String type) {
        if (oldOne != null && oldOne.getClass() == newOne.getClass()) {
            throw new IllegalArgumentException("Conflicting " + type + " creators: already had " + oldOne + ", encountered " + newOne);
        }
        if (this._canFixAccess) {
            ClassUtil.checkAndFixAccess((Member)newOne.getAnnotated());
        }
        return newOne;
    }
}
