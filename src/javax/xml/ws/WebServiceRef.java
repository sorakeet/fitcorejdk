/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebServiceRef{
    String name() default "";

    Class<?> type() default Object.class;

    String mappedName() default "";

    // 2.1 has Class value() default Object.class;
    // Fixing this raw Class type correctly in 2.2 API. This shouldn't cause
    // any compatibility issues for applications.
    Class<? extends Service> value() default Service.class;

    String wsdlLocation() default "";

    String lookup() default "";
}
