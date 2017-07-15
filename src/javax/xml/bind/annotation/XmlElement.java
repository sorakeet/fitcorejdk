/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD,METHOD,PARAMETER})
public @interface XmlElement{
    String name() default "##default";

    boolean nillable() default false;

    boolean required() default false;

    String namespace() default "##default";

    String defaultValue() default "\u0000";

    Class type() default DEFAULT.class;

    static final class DEFAULT{
    }
}
