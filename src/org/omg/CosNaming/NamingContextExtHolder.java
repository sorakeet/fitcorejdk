package org.omg.CosNaming;

public final class NamingContextExtHolder implements org.omg.CORBA.portable.Streamable{
    public NamingContextExt value=null;

    public NamingContextExtHolder(){
    }

    public NamingContextExtHolder(NamingContextExt initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NamingContextExtHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NamingContextExtHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NamingContextExtHelper.type();
    }
}
