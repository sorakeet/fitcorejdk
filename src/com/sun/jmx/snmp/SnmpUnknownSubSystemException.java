/**
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpUnknownSubSystemException extends Exception{
    private static final long serialVersionUID=4463202140045245052L;

    public SnmpUnknownSubSystemException(String msg){
        super(msg);
    }
}
