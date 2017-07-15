/**
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpUnknownSecModelException extends SnmpUnknownModelException{
    private static final long serialVersionUID=-2173491650805292799L;

    public SnmpUnknownSecModelException(String msg){
        super(msg);
    }
}
