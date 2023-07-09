// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.framework.qual;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum LiteralKind
{
    NULL, 
    INT, 
    LONG, 
    FLOAT, 
    DOUBLE, 
    BOOLEAN, 
    CHAR, 
    STRING, 
    ALL, 
    PRIMITIVE;
    
    public static List<LiteralKind> allLiteralKinds() {
        final List<LiteralKind> list = new ArrayList<LiteralKind>(Arrays.asList(values()));
        list.remove(LiteralKind.ALL);
        list.remove(LiteralKind.PRIMITIVE);
        return list;
    }
    
    public static List<LiteralKind> primitiveLiteralKinds() {
        return new ArrayList<LiteralKind>(Arrays.asList(LiteralKind.INT, LiteralKind.LONG, LiteralKind.FLOAT, LiteralKind.DOUBLE, LiteralKind.BOOLEAN, LiteralKind.CHAR));
    }
}
