/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class PolicyListHolder implements org.omg.CORBA.portable.Streamable{
    public Policy value[]=null;

    public PolicyListHolder(){
    }

    public PolicyListHolder(Policy[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=PolicyListHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        PolicyListHelper.write(o,value);
    }

    public TypeCode _type(){
        return PolicyListHelper.type();
    }
}
