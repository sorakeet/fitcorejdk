/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.io;

import sun.corba.Bridge;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ObjectStreamField implements Comparable{
    private static final Bridge bridge=
            (Bridge)AccessController.doPrivileged(
                    new PrivilegedAction(){
                        public Object run(){
                            return Bridge.get();
                        }
                    }
            );
    private String name;                // the name of the field
    private char type;                  // type first byte of the type signature
    private Field field;                // Reflected field
    private String typeString;          // iff object, typename
    private Class clazz;                // the type of this field, if has been resolved
    // the next 2 things are RMI-IIOP specific, it can be easily
    // removed, if we can figure out all place where there are dependencies
    // to this.  Signature is esentially equal to typestring. Then
    // essentially we can use the java.io.ObjectStreamField as such.
    private String signature;   // the signature of the field
    private long fieldID=Bridge.INVALID_FIELD_OFFSET;

    ObjectStreamField(Field field){
        this(field.getName(),field.getType());
        setField(field);
    }

    ObjectStreamField(String n,Class clazz){
        name=n;
        this.clazz=clazz;
        // Compute the typecode for easy switching
        if(clazz.isPrimitive()){
            if(clazz==Integer.TYPE){
                type='I';
            }else if(clazz==Byte.TYPE){
                type='B';
            }else if(clazz==Long.TYPE){
                type='J';
            }else if(clazz==Float.TYPE){
                type='F';
            }else if(clazz==Double.TYPE){
                type='D';
            }else if(clazz==Short.TYPE){
                type='S';
            }else if(clazz==Character.TYPE){
                type='C';
            }else if(clazz==Boolean.TYPE){
                type='Z';
            }
        }else if(clazz.isArray()){
            type='[';
            typeString=ObjectStreamClass.getSignature(clazz);
        }else{
            type='L';
            typeString=ObjectStreamClass.getSignature(clazz);
        }
        if(typeString!=null)
            signature=typeString;
        else
            signature=String.valueOf(type);
    }

    ObjectStreamField(String n,char t,Field f,String ts){
        name=n;
        type=t;
        setField(f);
        typeString=ts;
        if(typeString!=null)
            signature=typeString;
        else
            signature=String.valueOf(type);
    }

    ObjectStreamField(){
    }

    public String getName(){
        return name;
    }

    public Class getType(){
        if(clazz!=null)
            return clazz;
        switch(type){
            case 'B':
                clazz=Byte.TYPE;
                break;
            case 'C':
                clazz=Character.TYPE;
                break;
            case 'S':
                clazz=Short.TYPE;
                break;
            case 'I':
                clazz=Integer.TYPE;
                break;
            case 'J':
                clazz=Long.TYPE;
                break;
            case 'F':
                clazz=Float.TYPE;
                break;
            case 'D':
                clazz=Double.TYPE;
                break;
            case 'Z':
                clazz=Boolean.TYPE;
                break;
            case '[':
            case 'L':
                clazz=Object.class;
                break;
        }
        return clazz;
    }

    public char getTypeCode(){
        return type;
    }

    public String getTypeString(){
        return typeString;
    }

    Field getField(){
        return field;
    }

    void setField(Field field){
        this.field=field;
        this.fieldID=bridge.objectFieldOffset(field);
    }

    public boolean isPrimitive(){
        return (type!='['&&type!='L');
    }

    public int compareTo(Object o){
        ObjectStreamField f2=(ObjectStreamField)o;
        boolean thisprim=(this.typeString==null);
        boolean otherprim=(f2.typeString==null);
        if(thisprim!=otherprim){
            return (thisprim?-1:1);
        }
        return this.name.compareTo(f2.name);
    }

    public boolean typeEquals(ObjectStreamField other){
        if(other==null||type!=other.type)
            return false;
        /** Return true if the primitive types matched */
        if(typeString==null&&other.typeString==null)
            return true;
        return ObjectStreamClass.compareClassNames(typeString,
                other.typeString,
                '/');
    }

    public String getSignature(){
        return signature;
    }

    public String toString(){
        if(typeString!=null)
            return typeString+" "+name;
        else
            return type+" "+name;
    }

    public Class getClazz(){
        return clazz;
    }

    public long getFieldID(){
        return fieldID;
    }
}
