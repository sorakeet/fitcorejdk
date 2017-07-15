/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SnmpString extends SnmpValue{
    // VARIABLES
    //----------
    final static String name="String";
    private static final long serialVersionUID=-7011986973225194188L;
    protected byte[] value=null;

    // CONSTRUCTORS
    //-------------
    public SnmpString(){
        value=new byte[0];
    }

    public SnmpString(byte[] v){
        value=v.clone();
    }

    public SnmpString(Byte[] v){
        value=new byte[v.length];
        for(int i=0;i<v.length;i++){
            value[i]=v[i].byteValue();
        }
    }
    // PUBLIC METHODS
    //---------------

    public SnmpString(String v){
        value=v.getBytes();
    }

    public SnmpString(InetAddress address){
        value=address.getAddress();
    }

    public static String BinToChar(String bin){
        char value[]=new char[bin.length()/8];
        int binLength=value.length;
        for(int i=0;i<binLength;i++)
            value[i]=(char)Integer.parseInt(bin.substring(8*i,8*i+8),2);
        return new String(value);
    }

    public static String HexToChar(String hex){
        char value[]=new char[hex.length()/2];
        int hexLength=value.length;
        for(int i=0;i<hexLength;i++)
            value[i]=(char)Integer.parseInt(hex.substring(2*i,2*i+2),16);
        return new String(value);
    }

    public static SnmpOid toOid(long[] index,int start) throws SnmpStatusException{
        try{
            if(index[start]>Integer.MAX_VALUE){
                throw new SnmpStatusException(SnmpStatusException.noSuchName);
            }
            int strLen=(int)index[start++];
            long[] ids=new long[strLen];
            for(int i=0;i<strLen;i++){
                ids[i]=index[start+i];
            }
            return new SnmpOid(ids);
        }catch(IndexOutOfBoundsException e){
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }
    }

    public static int nextOid(long[] index,int start) throws SnmpStatusException{
        try{
            if(index[start]>Integer.MAX_VALUE){
                throw new SnmpStatusException(SnmpStatusException.noSuchName);
            }
            int strLen=(int)index[start++];
            start+=strLen;
            if(start<=index.length){
                return start;
            }else{
                throw new SnmpStatusException(SnmpStatusException.noSuchName);
            }
        }catch(IndexOutOfBoundsException e){
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }
    }

    public static void appendToOid(SnmpOid source,SnmpOid dest){
        dest.append(source.getLength());
        dest.append(source);
    }

    public InetAddress inetAddressValue() throws UnknownHostException{
        return InetAddress.getByAddress(value);
    }

    public byte[] byteValue(){
        return value.clone();
    }

    public Byte[] toByte(){
        Byte[] result=new Byte[value.length];
        for(int i=0;i<value.length;i++){
            result[i]=new Byte(value[i]);
        }
        return result;
    }

    public SnmpOid toOid(){
        long[] ids=new long[value.length];
        for(int i=0;i<value.length;i++){
            ids[i]=(long)(value[i]&0xFF);
        }
        return new SnmpOid(ids);
    }

    public String getTypeName(){
        return name;
    }

    final synchronized public SnmpValue duplicate(){
        return (SnmpValue)clone();
    }

    synchronized public Object clone(){
        SnmpString newclone=null;
        try{
            newclone=(SnmpString)super.clone();
            newclone.value=new byte[value.length];
            System.arraycopy(value,0,newclone.value,0,value.length);
        }catch(CloneNotSupportedException e){
            throw new InternalError(e); // vm bug.
        }
        return newclone;
    }

    public String toString(){
        return new String(value);
    }
}
