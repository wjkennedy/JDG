// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import org.codehaus.jackson.map.util.Annotations;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import java.util.Collections;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.ClassIntrospector;
import org.codehaus.jackson.map.AnnotationIntrospector;
import java.util.List;

public final class AnnotatedClass extends Annotated
{
    private static final AnnotationMap[] NO_ANNOTATION_MAPS;
    protected final Class<?> _class;
    protected final List<Class<?>> _superTypes;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final ClassIntrospector.MixInResolver _mixInResolver;
    protected final Class<?> _primaryMixIn;
    protected AnnotationMap _classAnnotations;
    protected AnnotatedConstructor _defaultConstructor;
    protected List<AnnotatedConstructor> _constructors;
    protected List<AnnotatedMethod> _creatorMethods;
    protected AnnotatedMethodMap _memberMethods;
    protected List<AnnotatedField> _fields;
    
    private AnnotatedClass(final Class<?> cls, final List<Class<?>> superTypes, final AnnotationIntrospector aintr, final ClassIntrospector.MixInResolver mir, final AnnotationMap classAnnotations) {
        this._class = cls;
        this._superTypes = superTypes;
        this._annotationIntrospector = aintr;
        this._mixInResolver = mir;
        this._primaryMixIn = ((this._mixInResolver == null) ? null : this._mixInResolver.findMixInClassFor(this._class));
        this._classAnnotations = classAnnotations;
    }
    
    @Override
    public AnnotatedClass withAnnotations(final AnnotationMap ann) {
        return new AnnotatedClass(this._class, this._superTypes, this._annotationIntrospector, this._mixInResolver, ann);
    }
    
    public static AnnotatedClass construct(final Class<?> cls, final AnnotationIntrospector aintr, final ClassIntrospector.MixInResolver mir) {
        final List<Class<?>> st = ClassUtil.findSuperTypes(cls, null);
        final AnnotatedClass ac = new AnnotatedClass(cls, st, aintr, mir, null);
        ac.resolveClassAnnotations();
        return ac;
    }
    
    public static AnnotatedClass constructWithoutSuperTypes(final Class<?> cls, final AnnotationIntrospector aintr, final ClassIntrospector.MixInResolver mir) {
        final List<Class<?>> empty = Collections.emptyList();
        final AnnotatedClass ac = new AnnotatedClass(cls, empty, aintr, mir, null);
        ac.resolveClassAnnotations();
        return ac;
    }
    
    @Override
    public Class<?> getAnnotated() {
        return this._class;
    }
    
    public int getModifiers() {
        return this._class.getModifiers();
    }
    
    @Override
    public String getName() {
        return this._class.getName();
    }
    
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> acls) {
        if (this._classAnnotations == null) {
            return null;
        }
        return this._classAnnotations.get(acls);
    }
    
    @Override
    public Type getGenericType() {
        return this._class;
    }
    
    @Override
    public Class<?> getRawType() {
        return this._class;
    }
    
    @Override
    protected AnnotationMap getAllAnnotations() {
        return this._classAnnotations;
    }
    
    public Annotations getAnnotations() {
        return this._classAnnotations;
    }
    
    public boolean hasAnnotations() {
        return this._classAnnotations.size() > 0;
    }
    
    public AnnotatedConstructor getDefaultConstructor() {
        return this._defaultConstructor;
    }
    
    public List<AnnotatedConstructor> getConstructors() {
        if (this._constructors == null) {
            return Collections.emptyList();
        }
        return this._constructors;
    }
    
    public List<AnnotatedMethod> getStaticMethods() {
        if (this._creatorMethods == null) {
            return Collections.emptyList();
        }
        return this._creatorMethods;
    }
    
    public Iterable<AnnotatedMethod> memberMethods() {
        return this._memberMethods;
    }
    
    public int getMemberMethodCount() {
        return this._memberMethods.size();
    }
    
    public AnnotatedMethod findMethod(final String name, final Class<?>[] paramTypes) {
        return this._memberMethods.find(name, paramTypes);
    }
    
    public int getFieldCount() {
        return (this._fields == null) ? 0 : this._fields.size();
    }
    
    public Iterable<AnnotatedField> fields() {
        if (this._fields == null) {
            return (Iterable<AnnotatedField>)Collections.emptyList();
        }
        return this._fields;
    }
    
    public void resolveClassAnnotations() {
        this._classAnnotations = new AnnotationMap();
        if (this._annotationIntrospector == null) {
            return;
        }
        if (this._primaryMixIn != null) {
            this._addClassMixIns(this._classAnnotations, this._class, this._primaryMixIn);
        }
        for (final Annotation a : this._class.getDeclaredAnnotations()) {
            if (this._annotationIntrospector.isHandled(a)) {
                this._classAnnotations.addIfNotPresent(a);
            }
        }
        for (final Class<?> cls : this._superTypes) {
            this._addClassMixIns(this._classAnnotations, cls);
            for (final Annotation a2 : cls.getDeclaredAnnotations()) {
                if (this._annotationIntrospector.isHandled(a2)) {
                    this._classAnnotations.addIfNotPresent(a2);
                }
            }
        }
        this._addClassMixIns(this._classAnnotations, Object.class);
    }
    
    public void resolveCreators(final boolean includeAll) {
        this._constructors = null;
        final Constructor<?>[] declaredConstructors;
        final Constructor<?>[] declaredCtors = declaredConstructors = this._class.getDeclaredConstructors();
        for (final Constructor<?> ctor : declaredConstructors) {
            if (ctor.getParameterTypes().length == 0) {
                this._defaultConstructor = this._constructConstructor(ctor, true);
            }
            else if (includeAll) {
                if (this._constructors == null) {
                    this._constructors = new ArrayList<AnnotatedConstructor>(Math.max(10, declaredCtors.length));
                }
                this._constructors.add(this._constructConstructor(ctor, false));
            }
        }
        if (this._primaryMixIn != null && (this._defaultConstructor != null || this._constructors != null)) {
            this._addConstructorMixIns(this._primaryMixIn);
        }
        if (this._annotationIntrospector != null) {
            if (this._defaultConstructor != null && this._annotationIntrospector.isIgnorableConstructor(this._defaultConstructor)) {
                this._defaultConstructor = null;
            }
            if (this._constructors != null) {
                int i = this._constructors.size();
                while (--i >= 0) {
                    if (this._annotationIntrospector.isIgnorableConstructor(this._constructors.get(i))) {
                        this._constructors.remove(i);
                    }
                }
            }
        }
        this._creatorMethods = null;
        if (includeAll) {
            for (final Method m : this._class.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) {
                    final int argCount = m.getParameterTypes().length;
                    if (argCount >= 1) {
                        if (this._creatorMethods == null) {
                            this._creatorMethods = new ArrayList<AnnotatedMethod>(8);
                        }
                        this._creatorMethods.add(this._constructCreatorMethod(m));
                    }
                }
            }
            if (this._primaryMixIn != null && this._creatorMethods != null) {
                this._addFactoryMixIns(this._primaryMixIn);
            }
            if (this._annotationIntrospector != null && this._creatorMethods != null) {
                int i = this._creatorMethods.size();
                while (--i >= 0) {
                    if (this._annotationIntrospector.isIgnorableMethod(this._creatorMethods.get(i))) {
                        this._creatorMethods.remove(i);
                    }
                }
            }
        }
    }
    
    public void resolveMemberMethods(final MethodFilter methodFilter) {
        this._memberMethods = new AnnotatedMethodMap();
        final AnnotatedMethodMap mixins = new AnnotatedMethodMap();
        this._addMemberMethods(this._class, methodFilter, this._memberMethods, this._primaryMixIn, mixins);
        for (final Class<?> cls : this._superTypes) {
            final Class<?> mixin = (this._mixInResolver == null) ? null : this._mixInResolver.findMixInClassFor(cls);
            this._addMemberMethods(cls, methodFilter, this._memberMethods, mixin, mixins);
        }
        if (this._mixInResolver != null) {
            final Class<?> mixin2 = this._mixInResolver.findMixInClassFor(Object.class);
            if (mixin2 != null) {
                this._addMethodMixIns(this._class, methodFilter, this._memberMethods, mixin2, mixins);
            }
        }
        if (this._annotationIntrospector != null && !mixins.isEmpty()) {
            for (final AnnotatedMethod mixIn : mixins) {
                try {
                    final Method m = Object.class.getDeclaredMethod(mixIn.getName(), mixIn.getParameterClasses());
                    if (m == null) {
                        continue;
                    }
                    final AnnotatedMethod am = this._constructMethod(m);
                    this._addMixOvers(mixIn.getAnnotated(), am, false);
                    this._memberMethods.add(am);
                }
                catch (final Exception ex) {}
            }
        }
    }
    
    public void resolveFields() {
        final LinkedHashMap<String, AnnotatedField> foundFields = new LinkedHashMap<String, AnnotatedField>();
        this._addFields(foundFields, this._class);
        if (foundFields.isEmpty()) {
            this._fields = Collections.emptyList();
        }
        else {
            (this._fields = new ArrayList<AnnotatedField>(foundFields.size())).addAll(foundFields.values());
        }
    }
    
    @Deprecated
    public void resolveMemberMethods(final MethodFilter methodFilter, final boolean collectIgnored) {
        this.resolveMemberMethods(methodFilter);
    }
    
    @Deprecated
    public void resolveFields(final boolean collectIgnored) {
        this.resolveFields();
    }
    
    protected void _addClassMixIns(final AnnotationMap annotations, final Class<?> toMask) {
        if (this._mixInResolver != null) {
            this._addClassMixIns(annotations, toMask, this._mixInResolver.findMixInClassFor(toMask));
        }
    }
    
    protected void _addClassMixIns(final AnnotationMap annotations, final Class<?> toMask, final Class<?> mixin) {
        if (mixin == null) {
            return;
        }
        for (final Annotation a : mixin.getDeclaredAnnotations()) {
            if (this._annotationIntrospector.isHandled(a)) {
                annotations.addIfNotPresent(a);
            }
        }
        for (final Class<?> parent : ClassUtil.findSuperTypes(mixin, toMask)) {
            for (final Annotation a2 : parent.getDeclaredAnnotations()) {
                if (this._annotationIntrospector.isHandled(a2)) {
                    annotations.addIfNotPresent(a2);
                }
            }
        }
    }
    
    protected void _addConstructorMixIns(final Class<?> mixin) {
        MemberKey[] ctorKeys = null;
        final int ctorCount = (this._constructors == null) ? 0 : this._constructors.size();
        for (final Constructor<?> ctor : mixin.getDeclaredConstructors()) {
            if (ctor.getParameterTypes().length == 0) {
                if (this._defaultConstructor != null) {
                    this._addMixOvers(ctor, this._defaultConstructor, false);
                }
            }
            else {
                if (ctorKeys == null) {
                    ctorKeys = new MemberKey[ctorCount];
                    for (int i = 0; i < ctorCount; ++i) {
                        ctorKeys[i] = new MemberKey(this._constructors.get(i).getAnnotated());
                    }
                }
                final MemberKey key = new MemberKey(ctor);
                for (int j = 0; j < ctorCount; ++j) {
                    if (key.equals(ctorKeys[j])) {
                        this._addMixOvers(ctor, this._constructors.get(j), true);
                        break;
                    }
                }
            }
        }
    }
    
    protected void _addFactoryMixIns(final Class<?> mixin) {
        MemberKey[] methodKeys = null;
        final int methodCount = this._creatorMethods.size();
        for (final Method m : mixin.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                if (m.getParameterTypes().length != 0) {
                    if (methodKeys == null) {
                        methodKeys = new MemberKey[methodCount];
                        for (int i = 0; i < methodCount; ++i) {
                            methodKeys[i] = new MemberKey(this._creatorMethods.get(i).getAnnotated());
                        }
                    }
                    final MemberKey key = new MemberKey(m);
                    for (int j = 0; j < methodCount; ++j) {
                        if (key.equals(methodKeys[j])) {
                            this._addMixOvers(m, this._creatorMethods.get(j), true);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    protected void _addMemberMethods(final Class<?> cls, final MethodFilter methodFilter, final AnnotatedMethodMap methods, final Class<?> mixInCls, final AnnotatedMethodMap mixIns) {
        if (mixInCls != null) {
            this._addMethodMixIns(cls, methodFilter, methods, mixInCls, mixIns);
        }
        if (cls == null) {
            return;
        }
        for (final Method m : cls.getDeclaredMethods()) {
            if (this._isIncludableMethod(m, methodFilter)) {
                AnnotatedMethod old = methods.find(m);
                if (old == null) {
                    final AnnotatedMethod newM = this._constructMethod(m);
                    methods.add(newM);
                    old = mixIns.remove(m);
                    if (old != null) {
                        this._addMixOvers(old.getAnnotated(), newM, false);
                    }
                }
                else {
                    this._addMixUnders(m, old);
                    if (old.getDeclaringClass().isInterface() && !m.getDeclaringClass().isInterface()) {
                        methods.add(old.withMethod(m));
                    }
                }
            }
        }
    }
    
    protected void _addMethodMixIns(final Class<?> targetClass, final MethodFilter methodFilter, final AnnotatedMethodMap methods, final Class<?> mixInCls, final AnnotatedMethodMap mixIns) {
        final List<Class<?>> parents = new ArrayList<Class<?>>();
        parents.add(mixInCls);
        ClassUtil.findSuperTypes(mixInCls, targetClass, parents);
        for (final Class<?> mixin : parents) {
            for (final Method m : mixin.getDeclaredMethods()) {
                if (this._isIncludableMethod(m, methodFilter)) {
                    final AnnotatedMethod am = methods.find(m);
                    if (am != null) {
                        this._addMixUnders(m, am);
                    }
                    else {
                        mixIns.add(this._constructMethod(m));
                    }
                }
            }
        }
    }
    
    protected void _addFields(final Map<String, AnnotatedField> fields, final Class<?> c) {
        final Class<?> parent = c.getSuperclass();
        if (parent != null) {
            this._addFields(fields, parent);
            for (final Field f : c.getDeclaredFields()) {
                if (this._isIncludableField(f)) {
                    fields.put(f.getName(), this._constructField(f));
                }
            }
            if (this._mixInResolver != null) {
                final Class<?> mixin = this._mixInResolver.findMixInClassFor(c);
                if (mixin != null) {
                    this._addFieldMixIns(parent, mixin, fields);
                }
            }
        }
    }
    
    protected void _addFieldMixIns(final Class<?> targetClass, final Class<?> mixInCls, final Map<String, AnnotatedField> fields) {
        final List<Class<?>> parents = new ArrayList<Class<?>>();
        parents.add(mixInCls);
        ClassUtil.findSuperTypes(mixInCls, targetClass, parents);
        for (final Class<?> mixin : parents) {
            for (final Field mixinField : mixin.getDeclaredFields()) {
                if (this._isIncludableField(mixinField)) {
                    final String name = mixinField.getName();
                    final AnnotatedField maskedField = fields.get(name);
                    if (maskedField != null) {
                        for (final Annotation a : mixinField.getDeclaredAnnotations()) {
                            if (this._annotationIntrospector.isHandled(a)) {
                                maskedField.addOrOverride(a);
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected AnnotatedMethod _constructMethod(final Method m) {
        if (this._annotationIntrospector == null) {
            return new AnnotatedMethod(m, this._emptyAnnotationMap(), null);
        }
        return new AnnotatedMethod(m, this._collectRelevantAnnotations(m.getDeclaredAnnotations()), null);
    }
    
    protected AnnotatedConstructor _constructConstructor(final Constructor<?> ctor, final boolean defaultCtor) {
        if (this._annotationIntrospector == null) {
            return new AnnotatedConstructor(ctor, this._emptyAnnotationMap(), this._emptyAnnotationMaps(ctor.getParameterTypes().length));
        }
        if (defaultCtor) {
            return new AnnotatedConstructor(ctor, this._collectRelevantAnnotations(ctor.getDeclaredAnnotations()), null);
        }
        Annotation[][] paramAnns = ctor.getParameterAnnotations();
        final int paramCount = ctor.getParameterTypes().length;
        AnnotationMap[] resolvedAnnotations = null;
        if (paramCount != paramAnns.length) {
            final Class<?> dc = ctor.getDeclaringClass();
            if (dc.isEnum() && paramCount == paramAnns.length + 2) {
                final Annotation[][] old = paramAnns;
                paramAnns = new Annotation[old.length + 2][];
                System.arraycopy(old, 0, paramAnns, 2, old.length);
                resolvedAnnotations = this._collectRelevantAnnotations(paramAnns);
            }
            else if (dc.isMemberClass() && paramCount == paramAnns.length + 1) {
                final Annotation[][] old = paramAnns;
                paramAnns = new Annotation[old.length + 1][];
                System.arraycopy(old, 0, paramAnns, 1, old.length);
                resolvedAnnotations = this._collectRelevantAnnotations(paramAnns);
            }
            if (resolvedAnnotations == null) {
                throw new IllegalStateException("Internal error: constructor for " + ctor.getDeclaringClass().getName() + " has mismatch: " + paramCount + " parameters; " + paramAnns.length + " sets of annotations");
            }
        }
        else {
            resolvedAnnotations = this._collectRelevantAnnotations(paramAnns);
        }
        return new AnnotatedConstructor(ctor, this._collectRelevantAnnotations(ctor.getDeclaredAnnotations()), resolvedAnnotations);
    }
    
    protected AnnotatedMethod _constructCreatorMethod(final Method m) {
        if (this._annotationIntrospector == null) {
            return new AnnotatedMethod(m, this._emptyAnnotationMap(), this._emptyAnnotationMaps(m.getParameterTypes().length));
        }
        return new AnnotatedMethod(m, this._collectRelevantAnnotations(m.getDeclaredAnnotations()), this._collectRelevantAnnotations(m.getParameterAnnotations()));
    }
    
    protected AnnotatedField _constructField(final Field f) {
        if (this._annotationIntrospector == null) {
            return new AnnotatedField(f, this._emptyAnnotationMap());
        }
        return new AnnotatedField(f, this._collectRelevantAnnotations(f.getDeclaredAnnotations()));
    }
    
    protected AnnotationMap[] _collectRelevantAnnotations(final Annotation[][] anns) {
        final int len = anns.length;
        final AnnotationMap[] result = new AnnotationMap[len];
        for (int i = 0; i < len; ++i) {
            result[i] = this._collectRelevantAnnotations(anns[i]);
        }
        return result;
    }
    
    protected AnnotationMap _collectRelevantAnnotations(final Annotation[] anns) {
        final AnnotationMap annMap = new AnnotationMap();
        if (anns != null) {
            for (final Annotation a : anns) {
                if (this._annotationIntrospector.isHandled(a)) {
                    annMap.add(a);
                }
            }
        }
        return annMap;
    }
    
    private AnnotationMap _emptyAnnotationMap() {
        return new AnnotationMap();
    }
    
    private AnnotationMap[] _emptyAnnotationMaps(final int count) {
        if (count == 0) {
            return AnnotatedClass.NO_ANNOTATION_MAPS;
        }
        final AnnotationMap[] maps = new AnnotationMap[count];
        for (int i = 0; i < count; ++i) {
            maps[i] = this._emptyAnnotationMap();
        }
        return maps;
    }
    
    protected boolean _isIncludableMethod(final Method m, final MethodFilter filter) {
        return (filter == null || filter.includeMethod(m)) && !m.isSynthetic() && !m.isBridge();
    }
    
    private boolean _isIncludableField(final Field f) {
        if (f.isSynthetic()) {
            return false;
        }
        final int mods = f.getModifiers();
        return !Modifier.isStatic(mods) && !Modifier.isTransient(mods);
    }
    
    protected void _addMixOvers(final Constructor<?> mixin, final AnnotatedConstructor target, final boolean addParamAnnotations) {
        for (final Annotation a : mixin.getDeclaredAnnotations()) {
            if (this._annotationIntrospector.isHandled(a)) {
                target.addOrOverride(a);
            }
        }
        if (addParamAnnotations) {
            final Annotation[][] pa = mixin.getParameterAnnotations();
            for (int i = 0, len = pa.length; i < len; ++i) {
                for (final Annotation a2 : pa[i]) {
                    target.addOrOverrideParam(i, a2);
                }
            }
        }
    }
    
    protected void _addMixOvers(final Method mixin, final AnnotatedMethod target, final boolean addParamAnnotations) {
        for (final Annotation a : mixin.getDeclaredAnnotations()) {
            if (this._annotationIntrospector.isHandled(a)) {
                target.addOrOverride(a);
            }
        }
        if (addParamAnnotations) {
            final Annotation[][] pa = mixin.getParameterAnnotations();
            for (int i = 0, len = pa.length; i < len; ++i) {
                for (final Annotation a2 : pa[i]) {
                    target.addOrOverrideParam(i, a2);
                }
            }
        }
    }
    
    protected void _addMixUnders(final Method src, final AnnotatedMethod target) {
        for (final Annotation a : src.getDeclaredAnnotations()) {
            if (this._annotationIntrospector.isHandled(a)) {
                target.addIfNotPresent(a);
            }
        }
    }
    
    @Override
    public String toString() {
        return "[AnnotedClass " + this._class.getName() + "]";
    }
    
    static {
        NO_ANNOTATION_MAPS = new AnnotationMap[0];
    }
}
