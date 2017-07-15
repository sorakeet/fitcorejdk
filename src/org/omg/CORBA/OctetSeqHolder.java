/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class OctetSeqHolder implements org.omg.CORBA.portable.Streamable{
    public byte value[]=null;

    public OctetSeqHolder(){
    }

    public OctetSeqHolder(byte[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=OctetSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        OctetSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return OctetSeqHelper.type();
    }
}
