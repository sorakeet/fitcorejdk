package org.omg.IOP;

public final class IORHolder implements org.omg.CORBA.portable.Streamable{
    public IOR value=null;

    public IORHolder(){
    }

    public IORHolder(IOR initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=IORHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        IORHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return IORHelper.type();
    }
}
