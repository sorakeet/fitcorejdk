/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpNull extends SnmpValue{
    // VARIABLES
    //----------
    final static String name="Null";
    private static final long serialVersionUID=1783782515994279177L;
    private int tag=5;

    public SnmpNull(String dummy){
        this();
    }

    // CONSTRUCTORS
    //-------------
    public SnmpNull(){
        tag=NullTag;
    }

    public SnmpNull(int t){
        tag=t;
    }

    // PUBLIC METHODS
    //---------------
    public int getTag(){
        return tag;
    }

    public SnmpOid toOid(){
        throw new IllegalArgumentException();
    }

    final public String getTypeName(){
        return name;
    }

    final synchronized public SnmpValue duplicate(){
        return (SnmpValue)clone();
    }

    final synchronized public Object clone(){
        SnmpNull newclone=null;
        try{
            newclone=(SnmpNull)super.clone();
            newclone.tag=tag;
        }catch(CloneNotSupportedException e){
            throw new InternalError(e); // vm bug.
        }
        return newclone;
    }

    public String toString(){
        String result="";
        if(tag!=5){
            result+="["+tag+"] ";
        }
        result+="NULL";
        switch(tag){
            case errNoSuchObjectTag:
                result+=" (noSuchObject)";
                break;
            case errNoSuchInstanceTag:
                result+=" (noSuchInstance)";
                break;
            case errEndOfMibViewTag:
                result+=" (endOfMibView)";
                break;
        }
        return result;
    }

    public boolean isNoSuchObjectValue(){
        return (tag==SnmpDataTypeEnums.errNoSuchObjectTag);
    }

    public boolean isNoSuchInstanceValue(){
        return (tag==SnmpDataTypeEnums.errNoSuchInstanceTag);
    }

    public boolean isEndOfMibViewValue(){
        return (tag==SnmpDataTypeEnums.errEndOfMibViewTag);
    }
}
