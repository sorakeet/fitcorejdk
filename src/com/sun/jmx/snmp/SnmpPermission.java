/**
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

import java.security.BasicPermission;

public class SnmpPermission extends BasicPermission{
    public SnmpPermission(String name){
        super(name);
    }

    public SnmpPermission(String name,String actions){
        super(name,actions);
    }
}
