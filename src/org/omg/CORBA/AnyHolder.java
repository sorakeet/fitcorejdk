/**
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

public final class AnyHolder implements Streamable{
    public Any value;

    public AnyHolder(){
    }

    public AnyHolder(Any initial){
        value=initial;
    }

    public void _read(InputStream input){
        value=input.read_any();
    }

    public void _write(OutputStream output){
        output.write_any(value);
    }

    public TypeCode _type(){
        return ORB.init().get_primitive_tc(TCKind.tk_any);
    }
}
