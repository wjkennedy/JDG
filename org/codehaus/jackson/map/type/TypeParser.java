// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.type;

import java.util.StringTokenizer;
import org.codehaus.jackson.map.util.ClassUtil;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.type.JavaType;

public class TypeParser
{
    final TypeFactory _factory;
    
    public TypeParser(final TypeFactory f) {
        this._factory = f;
    }
    
    public JavaType parse(String canonical) throws IllegalArgumentException {
        canonical = canonical.trim();
        final MyTokenizer tokens = new MyTokenizer(canonical);
        final JavaType type = this.parseType(tokens);
        if (tokens.hasMoreTokens()) {
            throw this._problem(tokens, "Unexpected tokens after complete type");
        }
        return type;
    }
    
    protected JavaType parseType(final MyTokenizer tokens) throws IllegalArgumentException {
        if (!tokens.hasMoreTokens()) {
            throw this._problem(tokens, "Unexpected end-of-string");
        }
        final Class<?> base = this.findClass(tokens.nextToken(), tokens);
        if (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken();
            if ("<".equals(token)) {
                return this._factory._fromParameterizedClass(base, this.parseTypes(tokens));
            }
            tokens.pushBack(token);
        }
        return this._factory._fromClass(base, null);
    }
    
    protected List<JavaType> parseTypes(final MyTokenizer tokens) throws IllegalArgumentException {
        final ArrayList<JavaType> types = new ArrayList<JavaType>();
        while (tokens.hasMoreTokens()) {
            types.add(this.parseType(tokens));
            if (!tokens.hasMoreTokens()) {
                break;
            }
            final String token = tokens.nextToken();
            if (">".equals(token)) {
                return types;
            }
            if (!",".equals(token)) {
                throw this._problem(tokens, "Unexpected token '" + token + "', expected ',' or '>')");
            }
        }
        throw this._problem(tokens, "Unexpected end-of-string");
    }
    
    protected Class<?> findClass(final String className, final MyTokenizer tokens) {
        try {
            return ClassUtil.findClass(className);
        }
        catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw this._problem(tokens, "Can not locate class '" + className + "', problem: " + e.getMessage());
        }
    }
    
    protected IllegalArgumentException _problem(final MyTokenizer tokens, final String msg) {
        return new IllegalArgumentException("Failed to parse type '" + tokens.getAllInput() + "' (remaining: '" + tokens.getRemainingInput() + "'): " + msg);
    }
    
    static final class MyTokenizer extends StringTokenizer
    {
        protected final String _input;
        protected int _index;
        protected String _pushbackToken;
        
        public MyTokenizer(final String str) {
            super(str, "<,>", true);
            this._input = str;
        }
        
        @Override
        public boolean hasMoreTokens() {
            return this._pushbackToken != null || super.hasMoreTokens();
        }
        
        @Override
        public String nextToken() {
            String token;
            if (this._pushbackToken != null) {
                token = this._pushbackToken;
                this._pushbackToken = null;
            }
            else {
                token = super.nextToken();
            }
            this._index += token.length();
            return token;
        }
        
        public void pushBack(final String token) {
            this._pushbackToken = token;
            this._index -= token.length();
        }
        
        public String getAllInput() {
            return this._input;
        }
        
        public String getUsedInput() {
            return this._input.substring(0, this._index);
        }
        
        public String getRemainingInput() {
            return this._input.substring(this._index);
        }
    }
}
