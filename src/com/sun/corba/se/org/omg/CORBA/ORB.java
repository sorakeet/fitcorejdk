/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.org.omg.CORBA;

import org.omg.CORBA.ORBPackage.InvalidName;

abstract public class ORB extends org.omg.CORBA_2_3.ORB{
    public void register_initial_reference(String id,
                                           org.omg.CORBA.Object obj)
            throws InvalidName{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
