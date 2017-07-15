/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class ULongLongSeqHolder implements org.omg.CORBA.portable.Streamable{
    public long value[]=null;

    public ULongLongSeqHolder(){
    }

    public ULongLongSeqHolder(long[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ULongLongSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ULongLongSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return ULongLongSeqHelper.type();
    }
}
