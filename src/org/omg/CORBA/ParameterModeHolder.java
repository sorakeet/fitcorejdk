package org.omg.CORBA;

public final class ParameterModeHolder implements org.omg.CORBA.portable.Streamable{
    public ParameterMode value=null;

    public ParameterModeHolder(){
    }

    public ParameterModeHolder(ParameterMode initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ParameterModeHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ParameterModeHelper.write(o,value);
    }

    public TypeCode _type(){
        return ParameterModeHelper.type();
    }
}
