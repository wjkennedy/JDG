// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import java.lang.reflect.TypeVariable;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import org.codehaus.jackson.type.JavaType;

public class TypeBindings
{
    private static final JavaType[] NO_TYPES;
    public static final JavaType UNBOUND;
    protected final TypeFactory _typeFactory;
    protected final JavaType _contextType;
    protected final Class<?> _contextClass;
    protected Map<String, JavaType> _bindings;
    protected HashSet<String> _placeholders;
    private final TypeBindings _parentBindings;
    
    public TypeBindings(final TypeFactory typeFactory, final Class<?> cc) {
        this(typeFactory, null, cc, null);
    }
    
    public TypeBindings(final TypeFactory typeFactory, final JavaType type) {
        this(typeFactory, null, type.getRawClass(), type);
    }
    
    public TypeBindings childInstance() {
        return new TypeBindings(this._typeFactory, this, this._contextClass, this._contextType);
    }
    
    private TypeBindings(final TypeFactory tf, final TypeBindings parent, final Class<?> cc, final JavaType type) {
        this._typeFactory = tf;
        this._parentBindings = parent;
        this._contextClass = cc;
        this._contextType = type;
    }
    
    public JavaType resolveType(final Class<?> cls) {
        return this._typeFactory._constructType(cls, this);
    }
    
    public JavaType resolveType(final Type type) {
        return this._typeFactory._constructType(type, this);
    }
    
    public int getBindingCount() {
        if (this._bindings == null) {
            this._resolve();
        }
        return this._bindings.size();
    }
    
    public JavaType findType(final String name) {
        if (this._bindings == null) {
            this._resolve();
        }
        final JavaType t = this._bindings.get(name);
        if (t != null) {
            return t;
        }
        if (this._placeholders != null && this._placeholders.contains(name)) {
            return TypeBindings.UNBOUND;
        }
        if (this._parentBindings != null) {
            return this._parentBindings.findType(name);
        }
        if (this._contextClass != null) {
            final Class<?> enclosing = this._contextClass.getEnclosingClass();
            if (enclosing != null && !Modifier.isStatic(this._contextClass.getModifiers())) {
                return TypeBindings.UNBOUND;
            }
        }
        String className;
        if (this._contextClass != null) {
            className = this._contextClass.getName();
        }
        else if (this._contextType != null) {
            className = this._contextType.toString();
        }
        else {
            className = "UNKNOWN";
        }
        throw new IllegalArgumentException("Type variable '" + name + "' can not be resolved (with context of class " + className + ")");
    }
    
    public void addBinding(final String name, final JavaType type) {
        if (this._bindings == null || this._bindings.size() == 0) {
            this._bindings = new LinkedHashMap<String, JavaType>();
        }
        this._bindings.put(name, type);
    }
    
    public JavaType[] typesAsArray() {
        if (this._bindings == null) {
            this._resolve();
        }
        if (this._bindings.size() == 0) {
            return TypeBindings.NO_TYPES;
        }
        return this._bindings.values().toArray(new JavaType[this._bindings.size()]);
    }
    
    protected void _resolve() {
        this._resolveBindings(this._contextClass);
        if (this._contextType != null) {
            final int count = this._contextType.containedTypeCount();
            if (count > 0) {
                if (this._bindings == null) {
                    this._bindings = new LinkedHashMap<String, JavaType>();
                }
                for (int i = 0; i < count; ++i) {
                    final String name = this._contextType.containedTypeName(i);
                    final JavaType type = this._contextType.containedType(i);
                    this._bindings.put(name, type);
                }
            }
        }
        if (this._bindings == null) {
            this._bindings = Collections.emptyMap();
        }
    }
    
    public void _addPlaceholder(final String name) {
        if (this._placeholders == null) {
            this._placeholders = new HashSet<String>();
        }
        this._placeholders.add(name);
    }
    
    protected void _resolveBindings(final Type t) {
        if (t == null) {
            return;
        }
        Class<?> raw;
        if (t instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType)t;
            final Type[] args = pt.getActualTypeArguments();
            if (args != null && args.length > 0) {
                final Class<?> rawType = (Class<?>)pt.getRawType();
                final TypeVariable<?>[] vars = rawType.getTypeParameters();
                if (vars.length != args.length) {
                    throw new IllegalArgumentException("Strange parametrized type (in class " + rawType.getName() + "): number of type arguments != number of type parameters (" + args.length + " vs " + vars.length + ")");
                }
                for (int i = 0, len = args.length; i < len; ++i) {
                    final TypeVariable<?> var = vars[i];
                    final String name = var.getName();
                    if (this._bindings == null) {
                        this._bindings = new LinkedHashMap<String, JavaType>();
                    }
                    else if (this._bindings.containsKey(name)) {
                        continue;
                    }
                    this._addPlaceholder(name);
                    this._bindings.put(name, this._typeFactory._constructType(args[i], this));
                }
            }
            raw = (Class)pt.getRawType();
        }
        else {
            if (!(t instanceof Class)) {
                return;
            }
            raw = (Class)t;
            final Class<?> decl = raw.getDeclaringClass();
            if (decl != null && !decl.isAssignableFrom(raw)) {
                this._resolveBindings(raw.getDeclaringClass());
            }
            final TypeVariable<?>[] vars2 = raw.getTypeParameters();
            if (vars2 != null && vars2.length > 0) {
                JavaType[] typeParams = null;
                if (this._contextType != null && raw.isAssignableFrom(this._contextType.getRawClass())) {
                    typeParams = this._typeFactory.findTypeParameters(this._contextType, raw);
                }
                for (int j = 0; j < vars2.length; ++j) {
                    final TypeVariable<?> var2 = vars2[j];
                    final String name2 = var2.getName();
                    final Type varType = var2.getBounds()[0];
                    if (varType != null) {
                        if (this._bindings == null) {
                            this._bindings = new LinkedHashMap<String, JavaType>();
                        }
                        else if (this._bindings.containsKey(name2)) {
                            continue;
                        }
                        this._addPlaceholder(name2);
                        if (typeParams != null) {
                            this._bindings.put(name2, typeParams[j]);
                        }
                        else {
                            this._bindings.put(name2, this._typeFactory._constructType(varType, this));
                        }
                    }
                }
            }
        }
        this._resolveBindings(raw.getGenericSuperclass());
        for (final Type intType : raw.getGenericInterfaces()) {
            this._resolveBindings(intType);
        }
    }
    
    @Override
    public String toString() {
        if (this._bindings == null) {
            this._resolve();
        }
        final StringBuilder sb = new StringBuilder("[TypeBindings for ");
        if (this._contextType != null) {
            sb.append(this._contextType.toString());
        }
        else {
            sb.append(this._contextClass.getName());
        }
        sb.append(": ").append(this._bindings).append("]");
        return sb.toString();
    }
    
    static {
        NO_TYPES = new JavaType[0];
        UNBOUND = new SimpleType(Object.class);
    }
}
