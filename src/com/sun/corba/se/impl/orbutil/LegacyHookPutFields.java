/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**
 */
package com.sun.corba.se.impl.orbutil;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

class LegacyHookPutFields extends ObjectOutputStream.PutField{
    private Hashtable fields=new Hashtable();

    public void put(String name,boolean value){
        fields.put(name,new Boolean(value));
    }

    public void put(String name,byte value){
        fields.put(name,new Byte(value));
    }

    public void put(String name,char value){
        fields.put(name,new Character(value));
    }

    public void put(String name,short value){
        fields.put(name,new Short(value));
    }

    public void put(String name,int value){
        fields.put(name,new Integer(value));
    }

    public void put(String name,long value){
        fields.put(name,new Long(value));
    }

    public void put(String name,float value){
        fields.put(name,new Float(value));
    }

    public void put(String name,double value){
        fields.put(name,new Double(value));
    }

    public void put(String name,Object value){
        fields.put(name,value);
    }

    public void write(ObjectOutput out) throws IOException{
        out.writeObject(fields);
    }
}
