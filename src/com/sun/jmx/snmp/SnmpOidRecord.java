/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;

public class SnmpOidRecord{
    // PRIVATE VARIABLES
    private String name;
    private String oid;
    private String type;

    public SnmpOidRecord(String name,String oid,String type){
        this.name=name;
        this.oid=oid;
        this.type=type;
    }

    public String getName(){
        return name;
    }

    public String getOid(){
        return oid;
    }

    public String getType(){
        return type;
    }
}
