/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class ServiceInformationHolder
        implements org.omg.CORBA.portable.Streamable{
    public ServiceInformation value;

    public ServiceInformationHolder(){
        this(null);
    }

    public ServiceInformationHolder(ServiceInformation arg){
        value=arg;
    }

    public void _read(org.omg.CORBA.portable.InputStream in){
        value=ServiceInformationHelper.read(in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream out){
        ServiceInformationHelper.write(out,value);
    }

    public TypeCode _type(){
        return ServiceInformationHelper.type();
    }
}
