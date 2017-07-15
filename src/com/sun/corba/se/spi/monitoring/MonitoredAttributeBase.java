/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public abstract class MonitoredAttributeBase implements MonitoredAttribute{
    String name;
    MonitoredAttributeInfo attributeInfo;

    public MonitoredAttributeBase(String name,MonitoredAttributeInfo info){
        this.name=name;
        this.attributeInfo=info;
    }

    MonitoredAttributeBase(String name){
        this.name=name;
    }

    void setMonitoredAttributeInfo(MonitoredAttributeInfo info){
        this.attributeInfo=info;
    }

    public MonitoredAttributeInfo getAttributeInfo(){
        return attributeInfo;
    }    public void clearState(){
    }

    public abstract Object getValue();

    public void setValue(Object value){
        if(!attributeInfo.isWritable()){
            throw new IllegalStateException(
                    "The Attribute "+name+" is not Writable...");
        }
        throw new IllegalStateException(
                "The method implementation is not provided for the attribute "+
                        name);
    }



    public String getName(){
        return name;
    }
} // end MonitoredAttributeBase
