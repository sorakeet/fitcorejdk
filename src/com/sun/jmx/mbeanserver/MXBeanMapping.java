/**
 * Copyright (c) 2007, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.io.InvalidObjectException;
import java.lang.reflect.Type;

public abstract class MXBeanMapping{
    private final Type javaType;
    private final OpenType<?> openType;
    private final Class<?> openClass;

    protected MXBeanMapping(Type javaType,OpenType<?> openType){
        if(javaType==null||openType==null)
            throw new NullPointerException("Null argument");
        this.javaType=javaType;
        this.openType=openType;
        this.openClass=makeOpenClass(javaType,openType);
    }

    private static Class<?> makeOpenClass(Type javaType,OpenType<?> openType){
        if(javaType instanceof Class<?>&&((Class<?>)javaType).isPrimitive())
            return (Class<?>)javaType;
        try{
            String className=openType.getClassName();
            return Class.forName(className,false,MXBeanMapping.class.getClassLoader());
        }catch(ClassNotFoundException e){
            throw new RuntimeException(e);  // should not happen
        }
    }

    public final Type getJavaType(){
        return javaType;
    }

    public final OpenType<?> getOpenType(){
        return openType;
    }

    public final Class<?> getOpenClass(){
        return openClass;
    }

    public abstract Object fromOpenValue(Object openValue)
            throws InvalidObjectException;

    public abstract Object toOpenValue(Object javaValue)
            throws OpenDataException;

    public void checkReconstructible() throws InvalidObjectException{
    }
}
