/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpCounter64 extends SnmpValue{
    // VARIABLES
    //----------
    final static String name="Counter64";
    private static final long serialVersionUID=8784850650494679937L;
    private long value=0;

    public SnmpCounter64(Long v) throws IllegalArgumentException{
        this(v.longValue());
    }

    // CONSTRUCTORS
    //-------------
    public SnmpCounter64(long v) throws IllegalArgumentException{
        // NOTE:
        // The max value for a counter64 variable is 2^64 - 1.
        // The max value for a Long is 2^63 - 1.
        // All the allowed values for a conuter64 variable cannot be covered !!!
        //
        if((v<0)||(v>Long.MAX_VALUE)){
            throw new IllegalArgumentException();
        }
        value=v;
    }

    public static SnmpOid toOid(long[] index,int start) throws SnmpStatusException{
        try{
            return new SnmpOid(index[start]);
        }catch(IndexOutOfBoundsException e){
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }
    }

    public static int nextOid(long[] index,int start) throws SnmpStatusException{
        if(start>=index.length){
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }else{
            return start+1;
        }
    }

    public static void appendToOid(SnmpOid source,SnmpOid dest){
        if(source.getLength()!=1){
            throw new IllegalArgumentException();
        }
        dest.append(source);
    }

    // PUBLIC METHODS
    //---------------
    public long longValue(){
        return value;
    }

    public Long toLong(){
        return new Long(value);
    }

    public int intValue(){
        return (int)value;
    }

    public Integer toInteger(){
        return new Integer((int)value);
    }

    public SnmpOid toOid(){
        return new SnmpOid(value);
    }

    final public String getTypeName(){
        return name;
    }

    final synchronized public SnmpValue duplicate(){
        return (SnmpValue)clone();
    }

    final synchronized public Object clone(){
        SnmpCounter64 newclone=null;
        try{
            newclone=(SnmpCounter64)super.clone();
            newclone.value=value;
        }catch(CloneNotSupportedException e){
            throw new InternalError(e); // vm bug.
        }
        return newclone;
    }

    public String toString(){
        return String.valueOf(value);
    }
}
