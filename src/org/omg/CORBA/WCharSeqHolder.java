/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class WCharSeqHolder implements org.omg.CORBA.portable.Streamable{
    public char value[]=null;

    public WCharSeqHolder(){
    }

    public WCharSeqHolder(char[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=WCharSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        WCharSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return WCharSeqHelper.type();
    }
}
