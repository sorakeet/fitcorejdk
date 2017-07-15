/**
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpBadSecurityLevelException extends Exception{
    private static final long serialVersionUID=8863728413063813053L;

    public SnmpBadSecurityLevelException(String msg){
        super(msg);
    }
}
