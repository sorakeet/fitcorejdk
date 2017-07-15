/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD,METHOD,PACKAGE})
public @interface XmlSchemaType{
    String name();

    String namespace() default "http://www.w3.org/2001/XMLSchema";

    Class type() default DEFAULT.class;

    static final class DEFAULT{
    }
}
