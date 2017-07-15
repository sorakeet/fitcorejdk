/**
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

abstract public class Enumerated implements Serializable{
    protected int value;

    public Enumerated() throws IllegalArgumentException{
        Enumeration<Integer> e=getIntTable().keys();
        if(e.hasMoreElements()){
            value=e.nextElement().intValue();
        }else{
            throw new IllegalArgumentException();
        }
    }

    protected abstract Hashtable<Integer,String> getIntTable();

    public Enumerated(int valueIndex) throws IllegalArgumentException{
        if(getIntTable().get(new Integer(valueIndex))==null){
            throw new IllegalArgumentException();
        }
        value=valueIndex;
    }

    public Enumerated(Integer valueIndex) throws IllegalArgumentException{
        if(getIntTable().get(valueIndex)==null){
            throw new IllegalArgumentException();
        }
        value=valueIndex.intValue();
    }

    public Enumerated(String valueString) throws IllegalArgumentException{
        Integer index=getStringTable().get(valueString);
        if(index==null){
            throw new IllegalArgumentException();
        }else{
            value=index.intValue();
        }
    }

    protected abstract Hashtable<String,Integer> getStringTable();

    public int intValue(){
        return value;
    }

    public Enumeration<Integer> valueIndexes(){
        return getIntTable().keys();
    }

    public Enumeration<String> valueStrings(){
        return getStringTable().keys();
    }

    @Override
    public int hashCode(){
        String hashString=getClass().getName()+String.valueOf(value);
        return hashString.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        return ((obj!=null)&&
                (getClass()==obj.getClass())&&
                (value==((Enumerated)obj).value));
    }

    @Override
    public String toString(){
        return getIntTable().get(new Integer(value));
    }
}
