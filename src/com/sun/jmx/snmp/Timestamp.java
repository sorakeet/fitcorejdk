/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;
// java imports
//

import java.util.Date;

public class Timestamp implements java.io.Serializable{
    private static final long serialVersionUID=-242456119149401823L;
    // PRIVATE VARIABLES
    //------------------
    private long sysUpTime;
    private long crtime;
    private SnmpTimeticks uptimeCache=null;
    // CONSTRUCTORS
    //-------------

    public Timestamp(){
        crtime=System.currentTimeMillis();
    }

    public Timestamp(long uptime,long when){
        sysUpTime=uptime;
        crtime=when;
    }

    public Timestamp(long uptime){
        sysUpTime=uptime;
        crtime=System.currentTimeMillis();
    }
    // GETTER/SETTER
    //--------------

    final public synchronized SnmpTimeticks getTimeTicks(){
        if(uptimeCache==null)
            uptimeCache=new SnmpTimeticks((int)sysUpTime);
        return uptimeCache;
    }

    final public long getSysUpTime(){
        return sysUpTime;
    }

    final public long getDateTime(){
        return crtime;
    }

    final public String toString(){
        StringBuffer buf=new StringBuffer();
        buf.append("{SysUpTime = "+SnmpTimeticks.printTimeTicks(sysUpTime));
        buf.append("} {Timestamp = "+getDate().toString()+"}");
        return buf.toString();
    }

    final public synchronized Date getDate(){
        return new Date(crtime);
    }
}
