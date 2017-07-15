package org.omg.CosNaming.NamingContextExtPackage;

public final class InvalidAddressHolder implements org.omg.CORBA.portable.Streamable{
    public InvalidAddress value=null;

    public InvalidAddressHolder(){
    }

    public InvalidAddressHolder(InvalidAddress initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=InvalidAddressHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        InvalidAddressHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return InvalidAddressHelper.type();
    }
}
