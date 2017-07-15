/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.soap;

import javax.xml.ws.soap.AddressingFeature.Responses;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebServiceFeatureAnnotation(id=AddressingFeature.ID, bean=AddressingFeature.class)
public @interface Addressing{
    boolean enabled() default true;

    boolean required() default false;

    Responses responses() default Responses.ALL;
}
