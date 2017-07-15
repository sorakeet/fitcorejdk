/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class CurrentHolder implements org.omg.CORBA.portable.Streamable{
    public Current value=null;

    public CurrentHolder(){
    }

    public CurrentHolder(Current initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=CurrentHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        CurrentHelper.write(o,value);
    }

    public TypeCode _type(){
        return CurrentHelper.type();
    }
}
