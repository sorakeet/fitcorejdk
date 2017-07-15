/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpCounter extends SnmpUnsignedInt{
    // VARIABLES
    //----------
    final static String name="Counter32";
    private static final long serialVersionUID=4655264728839396879L;

    // CONSTRUCTORS
    //-------------
    public SnmpCounter(int v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpCounter(Integer v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpCounter(long v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpCounter(Long v) throws IllegalArgumentException{
        super(v);
    }

    // PUBLIC METHODS
    //---------------
    final public String getTypeName(){
        return name;
    }
}
