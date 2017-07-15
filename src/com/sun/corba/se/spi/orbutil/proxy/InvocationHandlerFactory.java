/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.proxy;

import java.lang.reflect.InvocationHandler;

public interface InvocationHandlerFactory{
    InvocationHandler getInvocationHandler();

    Class[] getProxyInterfaces();
}
