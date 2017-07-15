/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpOpaque extends SnmpString{
    // VARIABLES
    //----------
    final static String name="Opaque";
    private static final long serialVersionUID=380952213936036664L;

    // CONSTRUCTORS
    //-------------
    public SnmpOpaque(byte[] v){
        super(v);
    }

    public SnmpOpaque(Byte[] v){
        super(v);
    }

    public SnmpOpaque(String v){
        super(v);
    }

    // PUBLIC METHODS
    //---------------
    public String toString(){
        StringBuffer result=new StringBuffer();
        for(int i=0;i<value.length;i++){
            byte b=value[i];
            int n=(b>=0)?b:b+256;
            result.append(Character.forDigit(n/16,16));
            result.append(Character.forDigit(n%16,16));
        }
        return result.toString();
    }

    final public String getTypeName(){
        return name;
    }
}
