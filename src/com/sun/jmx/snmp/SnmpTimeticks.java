/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;

public class SnmpTimeticks extends SnmpUnsignedInt{
    // VARIABLES
    //----------
    final static String name="TimeTicks";
    static final private long serialVersionUID=-5486435222360030630L;

    // CONSTRUCTORS
    //-------------
    public SnmpTimeticks(int v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpTimeticks(Integer v) throws IllegalArgumentException{
        super(v);
    }

    public SnmpTimeticks(Long v) throws IllegalArgumentException{
        this(v.longValue());
    }

    public SnmpTimeticks(long v) throws IllegalArgumentException{
        super(((v>0)?v&SnmpUnsignedInt.MAX_VALUE:v));
    }

    final public String toString(){
        return printTimeTicks(value);
    }

    // PUBLIC METHODS
    //---------------
    final static public String printTimeTicks(long timeticks){
        int seconds, minutes, hours, days;
        StringBuffer buf=new StringBuffer();
        timeticks/=100;
        days=(int)(timeticks/(60*60*24));
        timeticks%=(60*60*24);
        hours=(int)(timeticks/(60*60));
        timeticks%=(60*60);
        minutes=(int)(timeticks/60);
        seconds=(int)(timeticks%60);
        if(days==0){
            buf.append(hours+":"+minutes+":"+seconds);
            return buf.toString();
        }
        if(days==1){
            buf.append("1 day ");
        }else{
            buf.append(days+" days ");
        }
        buf.append(hours+":"+minutes+":"+seconds);
        return buf.toString();
    }

    final public String getTypeName(){
        return name;
    }
}
