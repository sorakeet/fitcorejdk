/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp.daemon;

final class SnmpRequestCounter{
    int reqid=0;

    public SnmpRequestCounter(){
    }

    public synchronized int getNewId(){
        if(++reqid<0)
            reqid=1;
        return reqid;
    }
}
