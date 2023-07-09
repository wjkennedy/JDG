// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import org.codehaus.jackson.map.BeanPropertyDefinition;

public class POJOPropertyBuilder extends BeanPropertyDefinition implements Comparable<POJOPropertyBuilder>
{
    protected final String _name;
    protected final String _internalName;
    protected Node<AnnotatedField> _fields;
    protected Node<AnnotatedParameter> _ctorParameters;
    protected Node<AnnotatedMethod> _getters;
    protected Node<AnnotatedMethod> _setters;
    
    public POJOPropertyBuilder(final String internalName) {
        this._internalName = internalName;
        this._name = internalName;
    }
    
    public POJOPropertyBuilder(final POJOPropertyBuilder src, final String newName) {
        this._internalName = src._internalName;
        this._name = newName;
        this._fields = src._fields;
        this._ctorParameters = src._ctorParameters;
        this._getters = src._getters;
        this._setters = src._setters;
    }
    
    public POJOPropertyBuilder withName(final String newName) {
        return new POJOPropertyBuilder(this, newName);
    }
    
    public int compareTo(final POJOPropertyBuilder other) {
        if (this._ctorParameters != null) {
            if (other._ctorParameters == null) {
                return -1;
            }
        }
        else if (other._ctorParameters != null) {
            return 1;
        }
        return this.getName().compareTo(other.getName());
    }
    
    @Override
    public String getName() {
        return this._name;
    }
    
    @Override
    public String getInternalName() {
        return this._internalName;
    }
    
    @Override
    public boolean isExplicitlyIncluded() {
        return this.anyExplicitNames();
    }
    
    @Override
    public boolean hasGetter() {
        return this._getters != null;
    }
    
    @Override
    public boolean hasSetter() {
        return this._setters != null;
    }
    
    @Override
    public boolean hasField() {
        return this._fields != null;
    }
    
    @Override
    public boolean hasConstructorParameter() {
        return this._ctorParameters != null;
    }
    
    @Override
    public AnnotatedMember getAccessor() {
        AnnotatedMember m = this.getGetter();
        if (m == null) {
            m = this.getField();
        }
        return m;
    }
    
    @Override
    public AnnotatedMember getMutator() {
        AnnotatedMember m = this.getConstructorParameter();
        if (m == null) {
            m = this.getSetter();
            if (m == null) {
                m = this.getField();
            }
        }
        return m;
    }
    
    @Override
    public boolean couldSerialize() {
        return this._getters != null || this._fields != null;
    }
    
    @Override
    public AnnotatedMethod getGetter() {
        if (this._getters == null) {
            return null;
        }
        AnnotatedMethod getter = this._getters.value;
        Node<AnnotatedMethod> next = this._getters.next;
        while (next != null) {
            final AnnotatedMethod nextGetter = next.value;
            final Class<?> getterClass = getter.getDeclaringClass();
            final Class<?> nextClass = nextGetter.getDeclaringClass();
            if (getterClass != nextClass) {
                if (getterClass.isAssignableFrom(nextClass)) {
                    getter = nextGetter;
                }
                else if (!nextClass.isAssignableFrom(getterClass)) {
                    throw new IllegalArgumentException("Conflicting getter definitions for property \"" + this.getName() + "\": " + getter.getFullName() + " vs " + nextGetter.getFullName());
                }
                next = next.next;
                continue;
            }
            throw new IllegalArgumentException("Conflicting getter definitions for property \"" + this.getName() + "\": " + getter.getFullName() + " vs " + nextGetter.getFullName());
        }
        return getter;
    }
    
    @Override
    public AnnotatedMethod getSetter() {
        if (this._setters == null) {
            return null;
        }
        AnnotatedMethod setter = this._setters.value;
        Node<AnnotatedMethod> next = this._setters.next;
        while (next != null) {
            final AnnotatedMethod nextSetter = next.value;
            final Class<?> setterClass = setter.getDeclaringClass();
            final Class<?> nextClass = nextSetter.getDeclaringClass();
            if (setterClass != nextClass) {
                if (setterClass.isAssignableFrom(nextClass)) {
                    setter = nextSetter;
                }
                else if (!nextClass.isAssignableFrom(setterClass)) {
                    throw new IllegalArgumentException("Conflicting setter definitions for property \"" + this.getName() + "\": " + setter.getFullName() + " vs " + nextSetter.getFullName());
                }
                next = next.next;
                continue;
            }
            throw new IllegalArgumentException("Conflicting setter definitions for property \"" + this.getName() + "\": " + setter.getFullName() + " vs " + nextSetter.getFullName());
        }
        return setter;
    }
    
    @Override
    public AnnotatedField getField() {
        if (this._fields == null) {
            return null;
        }
        AnnotatedField field = this._fields.value;
        Node<AnnotatedField> next = this._fields.next;
        while (next != null) {
            final AnnotatedField nextField = next.value;
            final Class<?> fieldClass = field.getDeclaringClass();
            final Class<?> nextClass = nextField.getDeclaringClass();
            if (fieldClass != nextClass) {
                if (fieldClass.isAssignableFrom(nextClass)) {
                    field = nextField;
                }
                else if (!nextClass.isAssignableFrom(fieldClass)) {
                    throw new IllegalArgumentException("Multiple fields representing property \"" + this.getName() + "\": " + field.getFullName() + " vs " + nextField.getFullName());
                }
                next = next.next;
                continue;
            }
            throw new IllegalArgumentException("Multiple fields representing property \"" + this.getName() + "\": " + field.getFullName() + " vs " + nextField.getFullName());
        }
        return field;
    }
    
    @Override
    public AnnotatedParameter getConstructorParameter() {
        if (this._ctorParameters == null) {
            return null;
        }
        Node<AnnotatedParameter> curr = this._ctorParameters;
        while (!(curr.value.getOwner() instanceof AnnotatedConstructor)) {
            curr = curr.next;
            if (curr == null) {
                return this._ctorParameters.value;
            }
        }
        return curr.value;
    }
    
    public void addField(final AnnotatedField a, final String ename, final boolean visible, final boolean ignored) {
        this._fields = new Node<AnnotatedField>(a, this._fields, ename, visible, ignored);
    }
    
    public void addCtor(final AnnotatedParameter a, final String ename, final boolean visible, final boolean ignored) {
        this._ctorParameters = new Node<AnnotatedParameter>(a, this._ctorParameters, ename, visible, ignored);
    }
    
    public void addGetter(final AnnotatedMethod a, final String ename, final boolean visible, final boolean ignored) {
        this._getters = new Node<AnnotatedMethod>(a, this._getters, ename, visible, ignored);
    }
    
    public void addSetter(final AnnotatedMethod a, final String ename, final boolean visible, final boolean ignored) {
        this._setters = new Node<AnnotatedMethod>(a, this._setters, ename, visible, ignored);
    }
    
    public void addAll(final POJOPropertyBuilder src) {
        this._fields = merge(this._fields, src._fields);
        this._ctorParameters = merge(this._ctorParameters, src._ctorParameters);
        this._getters = merge(this._getters, src._getters);
        this._setters = merge(this._setters, src._setters);
    }
    
    private static <T> Node<T> merge(final Node<T> chain1, final Node<T> chain2) {
        if (chain1 == null) {
            return chain2;
        }
        if (chain2 == null) {
            return chain1;
        }
        return ((Node<Object>)chain1).append((Node<Object>)chain2);
    }
    
    public void removeIgnored() {
        this._fields = this._removeIgnored(this._fields);
        this._getters = this._removeIgnored(this._getters);
        this._setters = this._removeIgnored(this._setters);
        this._ctorParameters = this._removeIgnored(this._ctorParameters);
    }
    
    public void removeNonVisible() {
        this._getters = this._removeNonVisible(this._getters);
        this._ctorParameters = this._removeNonVisible(this._ctorParameters);
        if (this._getters == null) {
            this._fields = this._removeNonVisible(this._fields);
            this._setters = this._removeNonVisible(this._setters);
        }
    }
    
    public void trimByVisibility() {
        this._fields = this._trimByVisibility(this._fields);
        this._getters = this._trimByVisibility(this._getters);
        this._setters = this._trimByVisibility(this._setters);
        this._ctorParameters = this._trimByVisibility(this._ctorParameters);
    }
    
    public void mergeAnnotations(final boolean forSerialization) {
        if (forSerialization) {
            if (this._getters != null) {
                final AnnotationMap ann = this._mergeAnnotations(0, this._getters, this._fields, this._ctorParameters, this._setters);
                this._getters = this._getters.withValue(this._getters.value.withAnnotations(ann));
            }
            else if (this._fields != null) {
                final AnnotationMap ann = this._mergeAnnotations(0, this._fields, this._ctorParameters, this._setters);
                this._fields = this._fields.withValue(this._fields.value.withAnnotations(ann));
            }
        }
        else if (this._ctorParameters != null) {
            final AnnotationMap ann = this._mergeAnnotations(0, this._ctorParameters, this._setters, this._fields, this._getters);
            this._ctorParameters = this._ctorParameters.withValue(this._ctorParameters.value.withAnnotations(ann));
        }
        else if (this._setters != null) {
            final AnnotationMap ann = this._mergeAnnotations(0, this._setters, this._fields, this._getters);
            this._setters = this._setters.withValue(this._setters.value.withAnnotations(ann));
        }
        else if (this._fields != null) {
            final AnnotationMap ann = this._mergeAnnotations(0, this._fields, this._getters);
            this._fields = this._fields.withValue(this._fields.value.withAnnotations(ann));
        }
    }
    
    private AnnotationMap _mergeAnnotations(int index, final Node<? extends AnnotatedMember>... nodes) {
        final AnnotationMap ann = ((AnnotatedMember)nodes[index].value).getAllAnnotations();
        ++index;
        while (index < nodes.length) {
            if (nodes[index] != null) {
                return AnnotationMap.merge(ann, this._mergeAnnotations(index, nodes));
            }
            ++index;
        }
        return ann;
    }
    
    private <T> Node<T> _removeIgnored(final Node<T> node) {
        if (node == null) {
            return node;
        }
        return node.withoutIgnored();
    }
    
    private <T> Node<T> _removeNonVisible(final Node<T> node) {
        if (node == null) {
            return node;
        }
        return node.withoutNonVisible();
    }
    
    private <T> Node<T> _trimByVisibility(final Node<T> node) {
        if (node == null) {
            return node;
        }
        return node.trimByVisibility();
    }
    
    public boolean anyExplicitNames() {
        return this._anyExplicitNames(this._fields) || this._anyExplicitNames(this._getters) || this._anyExplicitNames(this._setters) || this._anyExplicitNames(this._ctorParameters);
    }
    
    private <T> boolean _anyExplicitNames(Node<T> n) {
        while (n != null) {
            if (n.explicitName != null && n.explicitName.length() > 0) {
                return true;
            }
            n = n.next;
        }
        return false;
    }
    
    public boolean anyVisible() {
        return this._anyVisible(this._fields) || this._anyVisible(this._getters) || this._anyVisible(this._setters) || this._anyVisible(this._ctorParameters);
    }
    
    private <T> boolean _anyVisible(Node<T> n) {
        while (n != null) {
            if (n.isVisible) {
                return true;
            }
            n = n.next;
        }
        return false;
    }
    
    public boolean anyIgnorals() {
        return this._anyIgnorals(this._fields) || this._anyIgnorals(this._getters) || this._anyIgnorals(this._setters) || this._anyIgnorals(this._ctorParameters);
    }
    
    public boolean anyDeserializeIgnorals() {
        return this._anyIgnorals(this._fields) || this._anyIgnorals(this._setters) || this._anyIgnorals(this._ctorParameters);
    }
    
    public boolean anySerializeIgnorals() {
        return this._anyIgnorals(this._fields) || this._anyIgnorals(this._getters);
    }
    
    private <T> boolean _anyIgnorals(Node<T> n) {
        while (n != null) {
            if (n.isMarkedIgnored) {
                return true;
            }
            n = n.next;
        }
        return false;
    }
    
    public String findNewName() {
        Node<? extends AnnotatedMember> renamed = null;
        renamed = this.findRenamed(this._fields, renamed);
        renamed = this.findRenamed(this._getters, renamed);
        renamed = this.findRenamed(this._setters, renamed);
        renamed = this.findRenamed(this._ctorParameters, renamed);
        return (renamed == null) ? null : renamed.explicitName;
    }
    
    private Node<? extends AnnotatedMember> findRenamed(Node<? extends AnnotatedMember> node, Node<? extends AnnotatedMember> renamed) {
        while (node != null) {
            final String explName = node.explicitName;
            if (explName != null) {
                if (!explName.equals(this._name)) {
                    if (renamed == null) {
                        renamed = node;
                    }
                    else if (!explName.equals(renamed.explicitName)) {
                        throw new IllegalStateException("Conflicting property name definitions: '" + renamed.explicitName + "' (for " + renamed.value + ") vs '" + node.explicitName + "' (for " + node.value + ")");
                    }
                }
            }
            node = node.next;
        }
        return renamed;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[Property '").append(this._name).append("'; ctors: ").append(this._ctorParameters).append(", field(s): ").append(this._fields).append(", getter(s): ").append(this._getters).append(", setter(s): ").append(this._setters);
        sb.append("]");
        return sb.toString();
    }
    
    private static final class Node<T>
    {
        public final T value;
        public final Node<T> next;
        public final String explicitName;
        public final boolean isVisible;
        public final boolean isMarkedIgnored;
        
        public Node(final T v, final Node<T> n, final String explName, final boolean visible, final boolean ignored) {
            this.value = v;
            this.next = n;
            if (explName == null) {
                this.explicitName = null;
            }
            else {
                this.explicitName = ((explName.length() == 0) ? null : explName);
            }
            this.isVisible = visible;
            this.isMarkedIgnored = ignored;
        }
        
        public Node<T> withValue(final T newValue) {
            if (newValue == this.value) {
                return this;
            }
            return new Node<T>(newValue, this.next, this.explicitName, this.isVisible, this.isMarkedIgnored);
        }
        
        public Node<T> withNext(final Node<T> newNext) {
            if (newNext == this.next) {
                return this;
            }
            return new Node<T>(this.value, newNext, this.explicitName, this.isVisible, this.isMarkedIgnored);
        }
        
        public Node<T> withoutIgnored() {
            if (this.isMarkedIgnored) {
                return (this.next == null) ? null : this.next.withoutIgnored();
            }
            if (this.next != null) {
                final Node<T> newNext = this.next.withoutIgnored();
                if (newNext != this.next) {
                    return this.withNext(newNext);
                }
            }
            return this;
        }
        
        public Node<T> withoutNonVisible() {
            final Node<T> newNext = (this.next == null) ? null : this.next.withoutNonVisible();
            return this.isVisible ? this.withNext(newNext) : newNext;
        }
        
        private Node<T> append(final Node<T> appendable) {
            if (this.next == null) {
                return this.withNext(appendable);
            }
            return this.withNext(this.next.append(appendable));
        }
        
        public Node<T> trimByVisibility() {
            if (this.next == null) {
                return this;
            }
            final Node<T> newNext = this.next.trimByVisibility();
            if (this.explicitName != null) {
                if (newNext.explicitName == null) {
                    return this.withNext(null);
                }
                return this.withNext(newNext);
            }
            else {
                if (newNext.explicitName != null) {
                    return newNext;
                }
                if (this.isVisible == newNext.isVisible) {
                    return this.withNext(newNext);
                }
                return this.isVisible ? this.withNext(null) : newNext;
            }
        }
        
        @Override
        public String toString() {
            String msg = this.value.toString() + "[visible=" + this.isVisible + "]";
            if (this.next != null) {
                msg = msg + ", " + this.next.toString();
            }
            return msg;
        }
    }
}
