/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpGauge extends SnmpUnsignedInt{
    // VARIABLES
    //----------
    final static String name="Gauge32";
    private static final long serialVersionUID=-8366622742122792945L;

    // CONSTRUCTORS
    //-------------
    public SnmpGauge(int v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpGauge(Integer v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpGauge(long v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpGauge(Long v) throws IllegalArgumentException{
        super(v);
    }

    // PUBLIC METHODS
    //---------------
    final public String getTypeName(){
        return name;
    }
}
