/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.presentation.rmi;

import java.lang.reflect.Method;

public interface IDLNameTranslator{
    Class[] getInterfaces();

    Method[] getMethods();

    Method getMethod(String idlName);

    String getIDLName(Method method);
}
