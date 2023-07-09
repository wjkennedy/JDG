// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.common.value.qual;

import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({})
@SubtypeOf({ UnknownVal.class })
public @interface IntRangeFromGTENegativeOne {
}
