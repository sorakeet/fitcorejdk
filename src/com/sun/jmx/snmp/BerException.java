/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class BerException extends Exception{
    public static final int BAD_VERSION=1;
    private static final long serialVersionUID=494709767137042951L;
    private int errorType=0;

    public BerException(){
        errorType=0;
    }

    public BerException(int x){
        errorType=x;
    }

    public boolean isInvalidSnmpVersion(){
        if(errorType==BAD_VERSION)
            return true;
        else
            return false;
    }
}
