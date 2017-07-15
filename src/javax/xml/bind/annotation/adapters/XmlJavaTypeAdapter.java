/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation.adapters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({PACKAGE,FIELD,METHOD,TYPE,PARAMETER})
public @interface XmlJavaTypeAdapter{
    Class<? extends XmlAdapter> value();

    Class type() default DEFAULT.class;

    static final class DEFAULT{
    }
}
