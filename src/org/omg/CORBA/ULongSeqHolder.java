/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class ULongSeqHolder implements org.omg.CORBA.portable.Streamable{
    public int value[]=null;

    public ULongSeqHolder(){
    }

    public ULongSeqHolder(int[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ULongSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ULongSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return ULongSeqHelper.type();
    }
}
