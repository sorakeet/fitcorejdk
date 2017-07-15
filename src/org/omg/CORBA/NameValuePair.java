/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class NameValuePair implements org.omg.CORBA.portable.IDLEntity{
    public String id;
    public Any value;

    public NameValuePair(){
    }

    public NameValuePair(String __id,Any __value){
        id=__id;
        value=__value;
    }
}
