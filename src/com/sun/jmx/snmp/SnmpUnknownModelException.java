/**
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpUnknownModelException extends Exception{
    private static final long serialVersionUID=-8667664269418048003L;

    public SnmpUnknownModelException(String msg){
        super(msg);
    }
}
