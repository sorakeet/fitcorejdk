/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class BooleanSeqHolder implements org.omg.CORBA.portable.Streamable{
    public boolean value[]=null;

    public BooleanSeqHolder(){
    }

    public BooleanSeqHolder(boolean[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=BooleanSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        BooleanSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return BooleanSeqHelper.type();
    }
}
