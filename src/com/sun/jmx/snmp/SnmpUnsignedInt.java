/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public abstract class SnmpUnsignedInt extends SnmpInt{
    public static final long MAX_VALUE=0x0ffffffffL;
    // VARIABLES
    //----------
    final static String name="Unsigned32";

    // CONSTRUCTORS
    //-------------
    public SnmpUnsignedInt(int v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpUnsignedInt(Integer v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpUnsignedInt(long v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpUnsignedInt(Long v) throws IllegalArgumentException{
        super(v);
    }

    // PUBLIC METHODS
    //---------------
    public String getTypeName(){
        return name;
    }

    boolean isInitValueValid(int v){
        if((v<0)||(v>SnmpUnsignedInt.MAX_VALUE)){
            return false;
        }
        return true;
    }

    boolean isInitValueValid(long v){
        if((v<0)||(v>SnmpUnsignedInt.MAX_VALUE)){
            return false;
        }
        return true;
    }
}
