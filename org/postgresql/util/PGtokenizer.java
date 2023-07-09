// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PGtokenizer
{
    private static final Map<Character, Character> CLOSING_TO_OPENING_CHARACTER;
    protected List<String> tokens;
    
    public PGtokenizer(final String string, final char delim) {
        this.tokens = new ArrayList<String>();
        this.tokenize(string, delim);
    }
    
    public int tokenize(final String string, final char delim) {
        this.tokens.clear();
        final Deque<Character> stack = new ArrayDeque<Character>();
        boolean skipChar = false;
        boolean nestedDoubleQuote = false;
        char c = '\0';
        int p = 0;
        int s = 0;
        while (p < string.length()) {
            c = string.charAt(p);
            if (c == '(' || c == '[' || c == '<' || (!nestedDoubleQuote && !skipChar && c == '\"')) {
                stack.push(c);
                if (c == '\"') {
                    nestedDoubleQuote = true;
                    skipChar = true;
                }
            }
            if (c == ')' || c == ']' || c == '>' || (nestedDoubleQuote && !skipChar && c == '\"')) {
                if (c == '\"') {
                    while (!stack.isEmpty() && !Character.valueOf('\"').equals(stack.peek())) {
                        stack.pop();
                    }
                    nestedDoubleQuote = false;
                    stack.pop();
                }
                else {
                    final Character ch = PGtokenizer.CLOSING_TO_OPENING_CHARACTER.get(c);
                    if (!stack.isEmpty() && ch != null && ch.equals(stack.peek())) {
                        stack.pop();
                    }
                }
            }
            skipChar = (c == '\\');
            if (stack.isEmpty() && c == delim) {
                this.tokens.add(string.substring(s, p));
                s = p + 1;
            }
            ++p;
        }
        if (s < string.length()) {
            this.tokens.add(string.substring(s));
        }
        if (s == string.length() && c == delim) {
            this.tokens.add("");
        }
        return this.tokens.size();
    }
    
    public int getSize() {
        return this.tokens.size();
    }
    
    public String getToken(final int n) {
        return this.tokens.get(n);
    }
    
    public PGtokenizer tokenizeToken(final int n, final char delim) {
        return new PGtokenizer(this.getToken(n), delim);
    }
    
    public static String remove(String s, final String l, final String t) {
        if (s.startsWith(l)) {
            s = s.substring(l.length());
        }
        if (s.endsWith(t)) {
            s = s.substring(0, s.length() - t.length());
        }
        return s;
    }
    
    public void remove(final String l, final String t) {
        for (int i = 0; i < this.tokens.size(); ++i) {
            this.tokens.set(i, remove(this.tokens.get(i), l, t));
        }
    }
    
    public static String removePara(final String s) {
        return remove(s, "(", ")");
    }
    
    public void removePara() {
        this.remove("(", ")");
    }
    
    public static String removeBox(final String s) {
        return remove(s, "[", "]");
    }
    
    public void removeBox() {
        this.remove("[", "]");
    }
    
    public static String removeAngle(final String s) {
        return remove(s, "<", ">");
    }
    
    public void removeAngle() {
        this.remove("<", ">");
    }
    
    public static String removeCurlyBrace(final String s) {
        return remove(s, "{", "}");
    }
    
    public void removeCurlyBrace() {
        this.remove("{", "}");
    }
    
    static {
        (CLOSING_TO_OPENING_CHARACTER = new HashMap<Character, Character>()).put(')', '(');
        PGtokenizer.CLOSING_TO_OPENING_CHARACTER.put(']', '[');
        PGtokenizer.CLOSING_TO_OPENING_CHARACTER.put('>', '<');
        PGtokenizer.CLOSING_TO_OPENING_CHARACTER.put('\"', '\"');
    }
}
