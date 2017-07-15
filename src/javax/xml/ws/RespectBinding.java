/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebServiceFeatureAnnotation(id=RespectBindingFeature.ID, bean=RespectBindingFeature.class)
public @interface RespectBinding{
    boolean enabled() default true;
}
