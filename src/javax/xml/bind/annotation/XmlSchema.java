/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(PACKAGE)
public @interface XmlSchema{
    // the actual value is chosen because ## is not a valid
    // sequence in xs:anyURI.
    static final String NO_LOCATION="##generate";

    XmlNs[] xmlns() default {};

    String namespace() default "";

    XmlNsForm elementFormDefault() default XmlNsForm.UNSET;

    XmlNsForm attributeFormDefault() default XmlNsForm.UNSET;

    String location() default NO_LOCATION;
}
