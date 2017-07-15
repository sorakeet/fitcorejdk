/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class UnknownUserExceptionHolder implements org.omg.CORBA.portable.Streamable{
    public UnknownUserException value=null;

    public UnknownUserExceptionHolder(){
    }

    public UnknownUserExceptionHolder(UnknownUserException initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=UnknownUserExceptionHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        UnknownUserExceptionHelper.write(o,value);
    }

    public TypeCode _type(){
        return UnknownUserExceptionHelper.type();
    }
}
