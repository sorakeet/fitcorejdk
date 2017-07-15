/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.fsm;

public interface State{
    void preAction(FSM fsm);

    void postAction(FSM fsm);
}
// end of State.java
