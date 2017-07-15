package org.omg.IOP;

public final class TaggedProfileHolder implements org.omg.CORBA.portable.Streamable{
    public TaggedProfile value=null;

    public TaggedProfileHolder(){
    }

    public TaggedProfileHolder(TaggedProfile initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=TaggedProfileHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        TaggedProfileHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return TaggedProfileHelper.type();
    }
}
