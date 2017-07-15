/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpOid;

class SnmpEntryOid extends SnmpOid{
    private static final long serialVersionUID=9212653887791059564L;

    public SnmpEntryOid(long[] oid,int start){
        final int subLength=oid.length-start;
        final long[] subOid=new long[subLength];
        System.arraycopy(oid,start,subOid,0,subLength);
        components=subOid;
        componentCount=subLength;
    }
}
