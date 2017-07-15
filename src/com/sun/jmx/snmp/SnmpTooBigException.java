/**
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpTooBigException extends Exception{
    private static final long serialVersionUID=4754796246674803969L;
    private int varBindCount;

    public SnmpTooBigException(){
        varBindCount=0;
    }

    public SnmpTooBigException(int n){
        varBindCount=n;
    }

    public int getVarBindCount(){
        return varBindCount;
    }
}
