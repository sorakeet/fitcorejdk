/**
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

public final class ObjectHolder implements Streamable{
    public Object value;

    public ObjectHolder(){
    }

    public ObjectHolder(Object initial){
        value=initial;
    }

    public void _read(InputStream input){
        value=input.read_Object();
    }

    public void _write(OutputStream output){
        output.write_Object(value);
    }

    public TypeCode _type(){
        return ORB.init().get_primitive_tc(TCKind.tk_objref);
    }
}
