package org.omg.IOP;

public final class TaggedComponentHolder implements org.omg.CORBA.portable.Streamable{
    public TaggedComponent value=null;

    public TaggedComponentHolder(){
    }

    public TaggedComponentHolder(TaggedComponent initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=TaggedComponentHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        TaggedComponentHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return TaggedComponentHelper.type();
    }
}
