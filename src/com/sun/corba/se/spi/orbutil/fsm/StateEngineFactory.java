/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.fsm;

import com.sun.corba.se.impl.orbutil.fsm.StateEngineImpl;

public class StateEngineFactory{
    private StateEngineFactory(){
    }

    public static StateEngine create(){
        return new StateEngineImpl();
    }
}
