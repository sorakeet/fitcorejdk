/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class FloatSeqHolder implements org.omg.CORBA.portable.Streamable{
    public float value[]=null;

    public FloatSeqHolder(){
    }

    public FloatSeqHolder(float[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=FloatSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        FloatSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return FloatSeqHelper.type();
    }
}
