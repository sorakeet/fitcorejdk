/**
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public interface LinkedInvocationHandler extends InvocationHandler{
    Proxy getProxy();

    void setProxy(Proxy proxy);
}
