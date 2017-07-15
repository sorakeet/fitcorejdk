/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Documented
@Retention(SOURCE)
@Target({PACKAGE,TYPE,ANNOTATION_TYPE,METHOD,CONSTRUCTOR,FIELD,
        LOCAL_VARIABLE,PARAMETER})
public @interface Generated{
    String[] value();

    String date() default "";

    String comments() default "";
}
