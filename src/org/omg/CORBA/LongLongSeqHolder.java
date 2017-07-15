/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class LongLongSeqHolder implements org.omg.CORBA.portable.Streamable{
    public long value[]=null;

    public LongLongSeqHolder(){
    }

    public LongLongSeqHolder(long[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=LongLongSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        LongLongSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return LongLongSeqHelper.type();
    }
}
