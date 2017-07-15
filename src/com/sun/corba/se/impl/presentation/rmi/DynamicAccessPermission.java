/**
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.presentation.rmi;

import java.security.BasicPermission;

public final class DynamicAccessPermission extends BasicPermission{
    //private static final long serialVersionUID = -8343910153355041693L;

    public DynamicAccessPermission(String name){
        super(name);
    }

    public DynamicAccessPermission(String name,String actions){
        super(name,actions);
    }
}
