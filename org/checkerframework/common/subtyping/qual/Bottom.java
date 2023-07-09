// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.subtyping.qual;

import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.TargetLocations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Documented;

@Documented
@SubtypeOf({})
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@TargetLocations({ TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND })
public @interface Bottom {
}
