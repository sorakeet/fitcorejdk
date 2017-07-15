/**
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orbutil;

public class GetPropertyAction implements java.security.PrivilegedAction{
    private String theProp;
    private String defaultVal;

    public GetPropertyAction(String theProp){
        this.theProp=theProp;
    }

    public GetPropertyAction(String theProp,String defaultVal){
        this.theProp=theProp;
        this.defaultVal=defaultVal;
    }

    public Object run(){
        String value=System.getProperty(theProp);
        return (value==null)?defaultVal:value;
    }
}
