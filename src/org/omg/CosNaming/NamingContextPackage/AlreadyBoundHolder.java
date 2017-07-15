package org.omg.CosNaming.NamingContextPackage;

public final class AlreadyBoundHolder implements org.omg.CORBA.portable.Streamable{
    public AlreadyBound value=null;

    public AlreadyBoundHolder(){
    }

    public AlreadyBoundHolder(AlreadyBound initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=AlreadyBoundHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        AlreadyBoundHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return AlreadyBoundHelper.type();
    }
}
