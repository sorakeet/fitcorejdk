/**
 * Copyright (c) 2000, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orbutil;

import java.net.MalformedURLException;

public interface RepositoryIdInterface{
    Class getClassFromType() throws ClassNotFoundException;

    Class getClassFromType(String codebaseURL)
            throws ClassNotFoundException, MalformedURLException;

    Class getClassFromType(Class expectedType,
                           String codebaseURL)
            throws ClassNotFoundException, MalformedURLException;

    String getClassName();
}
