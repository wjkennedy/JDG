// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import javax.annotation.Nullable;

public final class JiraStringUtils
{
    public static final int EXPECTED_ELEMENT_LENGTH = 8;
    
    private JiraStringUtils() {
    }
    
    public static String asString(@Nullable final Object... elements) {
        final int length = elements.length;
        if (length == 0) {
            return "";
        }
        if (length == 1) {
            asString(elements[0]);
        }
        final StringBuilder answer = new StringBuilder(length * 8);
        for (final Object elem : elements) {
            answer.append(asString(elem));
        }
        return answer.toString();
    }
    
    private static String asString(final Object obj) {
        return (obj != null) ? obj.toString() : "null";
    }
}
