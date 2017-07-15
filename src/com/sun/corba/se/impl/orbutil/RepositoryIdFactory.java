/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orbutil;

public abstract class RepositoryIdFactory{
    private static final RepIdDelegator currentDelegator
            =new RepIdDelegator();

    public static RepositoryIdStrings getRepIdStringsFactory(){
        return currentDelegator;
    }

    public static RepositoryIdUtility getRepIdUtility(){
        return currentDelegator;
    }
}
