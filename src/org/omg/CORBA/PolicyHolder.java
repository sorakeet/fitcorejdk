/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class PolicyHolder implements org.omg.CORBA.portable.Streamable{
    public Policy value=null;

    public PolicyHolder(){
    }

    public PolicyHolder(Policy initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=PolicyHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        PolicyHelper.write(o,value);
    }

    public TypeCode _type(){
        return PolicyHelper.type();
    }
}
