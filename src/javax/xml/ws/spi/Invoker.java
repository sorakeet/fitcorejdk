/**
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.spi;

import javax.xml.ws.WebServiceContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Invoker{
    public abstract void inject(WebServiceContext webServiceContext)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    public abstract Object invoke(Method m,Object... args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
