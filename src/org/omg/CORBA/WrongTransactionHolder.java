/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class WrongTransactionHolder implements org.omg.CORBA.portable.Streamable{
    public WrongTransaction value=null;

    public WrongTransactionHolder(){
    }

    public WrongTransactionHolder(WrongTransaction initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=WrongTransactionHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        WrongTransactionHelper.write(o,value);
    }

    public TypeCode _type(){
        return WrongTransactionHelper.type();
    }
}
