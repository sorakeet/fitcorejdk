/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class ShortSeqHolder implements org.omg.CORBA.portable.Streamable{
    public short value[]=null;

    public ShortSeqHolder(){
    }

    public ShortSeqHolder(short[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ShortSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ShortSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return ShortSeqHelper.type();
    }
}
