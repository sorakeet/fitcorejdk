/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class DoubleSeqHolder implements org.omg.CORBA.portable.Streamable{
    public double value[]=null;

    public DoubleSeqHolder(){
    }

    public DoubleSeqHolder(double[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=DoubleSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        DoubleSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return DoubleSeqHelper.type();
    }
}
