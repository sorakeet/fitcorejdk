/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

import java.io.Serializable;
import java.util.Hashtable;

public class EnumRowStatus extends Enumerated implements Serializable{
    public final static int unspecified=0;
    public final static int active=1;
    public final static int notInService=2;
    public final static int notReady=3;
    public final static int createAndGo=4;
    public final static int createAndWait=5;
    public final static int destroy=6;
    // Initialize the mapping tables.
    //
    final static Hashtable<Integer,String> intTable=new Hashtable<>();
    final static Hashtable<String,Integer> stringTable=new Hashtable<>();
    private static final long serialVersionUID=8966519271130162420L;

    static{
        intTable.put(new Integer(0),"unspecified");
        intTable.put(new Integer(3),"notReady");
        intTable.put(new Integer(6),"destroy");
        intTable.put(new Integer(2),"notInService");
        intTable.put(new Integer(5),"createAndWait");
        intTable.put(new Integer(1),"active");
        intTable.put(new Integer(4),"createAndGo");
        stringTable.put("unspecified",new Integer(0));
        stringTable.put("notReady",new Integer(3));
        stringTable.put("destroy",new Integer(6));
        stringTable.put("notInService",new Integer(2));
        stringTable.put("createAndWait",new Integer(5));
        stringTable.put("active",new Integer(1));
        stringTable.put("createAndGo",new Integer(4));
    }

    public EnumRowStatus(Enumerated valueIndex)
            throws IllegalArgumentException{
        this(valueIndex.intValue());
    }

    public EnumRowStatus(int valueIndex)
            throws IllegalArgumentException{
        super(valueIndex);
    }

    public EnumRowStatus(Integer valueIndex)
            throws IllegalArgumentException{
        super(valueIndex);
    }

    public EnumRowStatus(Long valueIndex)
            throws IllegalArgumentException{
        this(valueIndex.longValue());
    }

    public EnumRowStatus(long valueIndex)
            throws IllegalArgumentException{
        this((int)valueIndex);
    }

    public EnumRowStatus()
            throws IllegalArgumentException{
        this(unspecified);
    }

    public EnumRowStatus(String x)
            throws IllegalArgumentException{
        super(x);
    }

    public EnumRowStatus(SnmpInt valueIndex)
            throws IllegalArgumentException{
        this(valueIndex.intValue());
    }

    static public boolean isValidValue(int value){
        if(value<0) return false;
        if(value>6) return false;
        return true;
    }

    public SnmpInt toSnmpValue()
            throws IllegalArgumentException{
        if(value==unspecified)
            throw new
                    IllegalArgumentException("`unspecified' is not a valid SNMP value.");
        return new SnmpInt(value);
    }

    // Documented in Enumerated
    //
    @Override
    protected Hashtable<Integer,String> getIntTable(){
        return EnumRowStatus.getRSIntTable();
    }

    // Documented in Enumerated
    //
    @Override
    protected Hashtable<String,Integer> getStringTable(){
        return EnumRowStatus.getRSStringTable();
    }

    static Hashtable<String,Integer> getRSStringTable(){
        return stringTable;
    }

    static Hashtable<Integer,String> getRSIntTable(){
        return intTable;
    }
}
