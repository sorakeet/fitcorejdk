/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({TYPE})
public @interface XmlType{
    String name() default "##default";

    String[] propOrder() default {""};

    String namespace() default "##default";

    Class factoryClass() default DEFAULT.class;

    String factoryMethod() default "";

    static final class DEFAULT{
    }
}
