/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpInt extends SnmpValue{
    // VARIABLES
    //----------
    final static String name="Integer32";
    private static final long serialVersionUID=-7163624758070343373L;
    protected long value=0;

    public SnmpInt(Integer v) throws IllegalArgumentException{
        this(v.intValue());
    }

    // CONSTRUCTORS
    //-------------
    public SnmpInt(int v) throws IllegalArgumentException{
        if(isInitValueValid(v)==false){
            throw new IllegalArgumentException();
        }
        value=(long)v;
    }

    boolean isInitValueValid(int v){
        if((v<Integer.MIN_VALUE)||(v>Integer.MAX_VALUE)){
            return false;
        }
        return true;
    }

    public SnmpInt(Long v) throws IllegalArgumentException{
        this(v.longValue());
    }

    public SnmpInt(long v) throws IllegalArgumentException{
        if(isInitValueValid(v)==false){
            throw new IllegalArgumentException();
        }
        value=v;
    }

    boolean isInitValueValid(long v){
        if((v<Integer.MIN_VALUE)||(v>Integer.MAX_VALUE)){
            return false;
        }
        return true;
    }

    public SnmpInt(Enumerated v) throws IllegalArgumentException{
        this(v.intValue());
    }

    public SnmpInt(boolean v){
        value=v?1:2;
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

    public String getTypeName(){
        return name;
    }

    final synchronized public SnmpValue duplicate(){
        return (SnmpValue)clone();
    }

    final synchronized public Object clone(){
        SnmpInt newclone=null;
        try{
            newclone=(SnmpInt)super.clone();
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
