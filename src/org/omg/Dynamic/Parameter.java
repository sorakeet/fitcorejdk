package org.omg.Dynamic;

public final class Parameter implements org.omg.CORBA.portable.IDLEntity{
    public org.omg.CORBA.Any argument=null;
    public org.omg.CORBA.ParameterMode mode=null;

    public Parameter(){
    } // ctor

    public Parameter(org.omg.CORBA.Any _argument,org.omg.CORBA.ParameterMode _mode){
        argument=_argument;
        mode=_mode;
    } // ctor
} // class Parameter
