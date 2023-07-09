// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect;

public interface VisibilityChecker<T extends VisibilityChecker<T>>
{
    T with(final JsonAutoDetect p0);
    
    T with(final JsonAutoDetect.Visibility p0);
    
    T withVisibility(final JsonMethod p0, final JsonAutoDetect.Visibility p1);
    
    T withGetterVisibility(final JsonAutoDetect.Visibility p0);
    
    T withIsGetterVisibility(final JsonAutoDetect.Visibility p0);
    
    T withSetterVisibility(final JsonAutoDetect.Visibility p0);
    
    T withCreatorVisibility(final JsonAutoDetect.Visibility p0);
    
    T withFieldVisibility(final JsonAutoDetect.Visibility p0);
    
    boolean isGetterVisible(final Method p0);
    
    boolean isGetterVisible(final AnnotatedMethod p0);
    
    boolean isIsGetterVisible(final Method p0);
    
    boolean isIsGetterVisible(final AnnotatedMethod p0);
    
    boolean isSetterVisible(final Method p0);
    
    boolean isSetterVisible(final AnnotatedMethod p0);
    
    boolean isCreatorVisible(final Member p0);
    
    boolean isCreatorVisible(final AnnotatedMember p0);
    
    boolean isFieldVisible(final Field p0);
    
    boolean isFieldVisible(final AnnotatedField p0);
    
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, setterVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY, fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class Std implements VisibilityChecker<Std>
    {
        protected static final Std DEFAULT;
        protected final JsonAutoDetect.Visibility _getterMinLevel;
        protected final JsonAutoDetect.Visibility _isGetterMinLevel;
        protected final JsonAutoDetect.Visibility _setterMinLevel;
        protected final JsonAutoDetect.Visibility _creatorMinLevel;
        protected final JsonAutoDetect.Visibility _fieldMinLevel;
        
        public static Std defaultInstance() {
            return Std.DEFAULT;
        }
        
        public Std(final JsonAutoDetect ann) {
            final JsonMethod[] incl = ann.value();
            this._getterMinLevel = (hasMethod(incl, JsonMethod.GETTER) ? ann.getterVisibility() : JsonAutoDetect.Visibility.NONE);
            this._isGetterMinLevel = (hasMethod(incl, JsonMethod.IS_GETTER) ? ann.isGetterVisibility() : JsonAutoDetect.Visibility.NONE);
            this._setterMinLevel = (hasMethod(incl, JsonMethod.SETTER) ? ann.setterVisibility() : JsonAutoDetect.Visibility.NONE);
            this._creatorMinLevel = (hasMethod(incl, JsonMethod.CREATOR) ? ann.creatorVisibility() : JsonAutoDetect.Visibility.NONE);
            this._fieldMinLevel = (hasMethod(incl, JsonMethod.FIELD) ? ann.fieldVisibility() : JsonAutoDetect.Visibility.NONE);
        }
        
        public Std(final JsonAutoDetect.Visibility getter, final JsonAutoDetect.Visibility isGetter, final JsonAutoDetect.Visibility setter, final JsonAutoDetect.Visibility creator, final JsonAutoDetect.Visibility field) {
            this._getterMinLevel = getter;
            this._isGetterMinLevel = isGetter;
            this._setterMinLevel = setter;
            this._creatorMinLevel = creator;
            this._fieldMinLevel = field;
        }
        
        public Std(final JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                this._getterMinLevel = Std.DEFAULT._getterMinLevel;
                this._isGetterMinLevel = Std.DEFAULT._isGetterMinLevel;
                this._setterMinLevel = Std.DEFAULT._setterMinLevel;
                this._creatorMinLevel = Std.DEFAULT._creatorMinLevel;
                this._fieldMinLevel = Std.DEFAULT._fieldMinLevel;
            }
            else {
                this._getterMinLevel = v;
                this._isGetterMinLevel = v;
                this._setterMinLevel = v;
                this._creatorMinLevel = v;
                this._fieldMinLevel = v;
            }
        }
        
        public Std with(final JsonAutoDetect ann) {
            if (ann == null) {
                return this;
            }
            Std curr = this;
            final JsonMethod[] incl = ann.value();
            JsonAutoDetect.Visibility v = hasMethod(incl, JsonMethod.GETTER) ? ann.getterVisibility() : JsonAutoDetect.Visibility.NONE;
            curr = curr.withGetterVisibility(v);
            v = (hasMethod(incl, JsonMethod.IS_GETTER) ? ann.isGetterVisibility() : JsonAutoDetect.Visibility.NONE);
            curr = curr.withIsGetterVisibility(v);
            v = (hasMethod(incl, JsonMethod.SETTER) ? ann.setterVisibility() : JsonAutoDetect.Visibility.NONE);
            curr = curr.withSetterVisibility(v);
            v = (hasMethod(incl, JsonMethod.CREATOR) ? ann.creatorVisibility() : JsonAutoDetect.Visibility.NONE);
            curr = curr.withCreatorVisibility(v);
            v = (hasMethod(incl, JsonMethod.FIELD) ? ann.fieldVisibility() : JsonAutoDetect.Visibility.NONE);
            curr = curr.withFieldVisibility(v);
            return curr;
        }
        
        public Std with(final JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                return Std.DEFAULT;
            }
            return new Std(v);
        }
        
        public Std withVisibility(final JsonMethod method, final JsonAutoDetect.Visibility v) {
            switch (method) {
                case GETTER: {
                    return this.withGetterVisibility(v);
                }
                case SETTER: {
                    return this.withSetterVisibility(v);
                }
                case CREATOR: {
                    return this.withCreatorVisibility(v);
                }
                case FIELD: {
                    return this.withFieldVisibility(v);
                }
                case IS_GETTER: {
                    return this.withIsGetterVisibility(v);
                }
                case ALL: {
                    return this.with(v);
                }
                default: {
                    return this;
                }
            }
        }
        
        public Std withGetterVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._getterMinLevel;
            }
            if (this._getterMinLevel == v) {
                return this;
            }
            return new Std(v, this._isGetterMinLevel, this._setterMinLevel, this._creatorMinLevel, this._fieldMinLevel);
        }
        
        public Std withIsGetterVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._isGetterMinLevel;
            }
            if (this._isGetterMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, v, this._setterMinLevel, this._creatorMinLevel, this._fieldMinLevel);
        }
        
        public Std withSetterVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._setterMinLevel;
            }
            if (this._setterMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, this._isGetterMinLevel, v, this._creatorMinLevel, this._fieldMinLevel);
        }
        
        public Std withCreatorVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._creatorMinLevel;
            }
            if (this._creatorMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, this._isGetterMinLevel, this._setterMinLevel, v, this._fieldMinLevel);
        }
        
        public Std withFieldVisibility(JsonAutoDetect.Visibility v) {
            if (v == JsonAutoDetect.Visibility.DEFAULT) {
                v = Std.DEFAULT._fieldMinLevel;
            }
            if (this._fieldMinLevel == v) {
                return this;
            }
            return new Std(this._getterMinLevel, this._isGetterMinLevel, this._setterMinLevel, this._creatorMinLevel, v);
        }
        
        public boolean isCreatorVisible(final Member m) {
            return this._creatorMinLevel.isVisible(m);
        }
        
        public boolean isCreatorVisible(final AnnotatedMember m) {
            return this.isCreatorVisible(m.getMember());
        }
        
        public boolean isFieldVisible(final Field f) {
            return this._fieldMinLevel.isVisible(f);
        }
        
        public boolean isFieldVisible(final AnnotatedField f) {
            return this.isFieldVisible(f.getAnnotated());
        }
        
        public boolean isGetterVisible(final Method m) {
            return this._getterMinLevel.isVisible(m);
        }
        
        public boolean isGetterVisible(final AnnotatedMethod m) {
            return this.isGetterVisible(m.getAnnotated());
        }
        
        public boolean isIsGetterVisible(final Method m) {
            return this._isGetterMinLevel.isVisible(m);
        }
        
        public boolean isIsGetterVisible(final AnnotatedMethod m) {
            return this.isIsGetterVisible(m.getAnnotated());
        }
        
        public boolean isSetterVisible(final Method m) {
            return this._setterMinLevel.isVisible(m);
        }
        
        public boolean isSetterVisible(final AnnotatedMethod m) {
            return this.isSetterVisible(m.getAnnotated());
        }
        
        private static boolean hasMethod(final JsonMethod[] methods, final JsonMethod method) {
            for (final JsonMethod curr : methods) {
                if (curr == method || curr == JsonMethod.ALL) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public String toString() {
            return "[Visibility:" + " getter: " + this._getterMinLevel + ", isGetter: " + this._isGetterMinLevel + ", setter: " + this._setterMinLevel + ", creator: " + this._creatorMinLevel + ", field: " + this._fieldMinLevel + "]";
        }
        
        static {
            DEFAULT = new Std(Std.class.getAnnotation(JsonAutoDetect.class));
        }
    }
}
