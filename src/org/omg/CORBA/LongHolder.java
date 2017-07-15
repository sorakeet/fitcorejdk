/**
 * Copyright (c) 1995, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

public final class LongHolder implements Streamable{
    public long value;

    public LongHolder(){
    }

    public LongHolder(long initial){
        value=initial;
    }

    public void _read(InputStream input){
        value=input.read_longlong();
    }

    public void _write(OutputStream output){
        output.write_longlong(value);
    }

    public TypeCode _type(){
        return ORB.init().get_primitive_tc(TCKind.tk_longlong);
    }
}
