/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**
 */
package com.sun.corba.se.impl.orbutil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Hashtable;

class LegacyHookGetFields extends ObjectInputStream.GetField{
    private Hashtable fields=null;

    LegacyHookGetFields(Hashtable fields){
        this.fields=fields;
    }

    public ObjectStreamClass getObjectStreamClass(){
        return null;
    }

    public boolean defaulted(String name)
            throws IOException, IllegalArgumentException{
        return (!fields.containsKey(name));
    }

    public boolean get(String name,boolean defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Boolean)fields.get(name)).booleanValue();
    }

    public byte get(String name,byte defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Byte)fields.get(name)).byteValue();
    }

    public char get(String name,char defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Character)fields.get(name)).charValue();
    }

    public short get(String name,short defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Short)fields.get(name)).shortValue();
    }

    public int get(String name,int defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Integer)fields.get(name)).intValue();
    }

    public long get(String name,long defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Long)fields.get(name)).longValue();
    }

    public float get(String name,float defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Float)fields.get(name)).floatValue();
    }

    public double get(String name,double defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return ((Double)fields.get(name)).doubleValue();
    }

    public Object get(String name,Object defvalue)
            throws IOException, IllegalArgumentException{
        if(defaulted(name))
            return defvalue;
        else return fields.get(name);
    }

    public String toString(){
        return fields.toString();
    }
}
