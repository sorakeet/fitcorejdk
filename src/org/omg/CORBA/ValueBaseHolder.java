/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

public final class ValueBaseHolder implements Streamable{
    public java.io.Serializable value;

    public ValueBaseHolder(){
    }

    public ValueBaseHolder(java.io.Serializable initial){
        value=initial;
    }

    public void _read(InputStream input){
        value=((org.omg.CORBA_2_3.portable.InputStream)input).read_value();
    }

    public void _write(OutputStream output){
        ((org.omg.CORBA_2_3.portable.OutputStream)output).write_value(value);
    }

    public TypeCode _type(){
        return ORB.init().get_primitive_tc(TCKind.tk_value);
    }
}
