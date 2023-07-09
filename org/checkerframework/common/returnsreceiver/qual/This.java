// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.returnsreceiver.qual;

import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.PolymorphicQualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@PolymorphicQualifier
@TargetLocations({ TypeUseLocation.RECEIVER, TypeUseLocation.RETURN })
public @interface This {
}
