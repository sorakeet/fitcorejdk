package org.omg.CORBA;

public final class WStringSeqHolder implements org.omg.CORBA.portable.Streamable{
    public String value[]=null;

    public WStringSeqHolder(){
    }

    public WStringSeqHolder(String[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=WStringSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        WStringSeqHelper.write(o,value);
    }

    public TypeCode _type(){
        return WStringSeqHelper.type();
    }
}
