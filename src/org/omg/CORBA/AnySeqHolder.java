/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class AnySeqHolder implements org.omg.CORBA.portable.Streamable{
    public Any value[]=null;

    public AnySeqHolder(){
    }

    public AnySeqHolder(Any[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=AnySeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        AnySeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return AnySeqHelper.type();
    }
}
