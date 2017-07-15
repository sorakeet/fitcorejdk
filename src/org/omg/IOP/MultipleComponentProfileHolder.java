package org.omg.IOP;

public final class MultipleComponentProfileHolder implements org.omg.CORBA.portable.Streamable{
    public TaggedComponent value[]=null;

    public MultipleComponentProfileHolder(){
    }

    public MultipleComponentProfileHolder(TaggedComponent[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=MultipleComponentProfileHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        MultipleComponentProfileHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return MultipleComponentProfileHelper.type();
    }
}
