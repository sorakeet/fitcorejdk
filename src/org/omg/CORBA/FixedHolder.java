/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

public final class FixedHolder implements Streamable{
    public java.math.BigDecimal value;

    public FixedHolder(){
    }

    public FixedHolder(java.math.BigDecimal initial){
        value=initial;
    }

    public void _read(InputStream input){
        value=input.read_fixed();
    }

    public void _write(OutputStream output){
        output.write_fixed(value);
    }

    public TypeCode _type(){
        return ORB.init().get_primitive_tc(TCKind.tk_fixed);
    }
}
