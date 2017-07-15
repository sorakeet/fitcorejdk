package org.omg.IOP;

public final class TaggedComponent implements org.omg.CORBA.portable.IDLEntity{
    public int tag=(int)0;
    public byte component_data[]=null;

    public TaggedComponent(){
    } // ctor

    public TaggedComponent(int _tag,byte[] _component_data){
        tag=_tag;
        component_data=_component_data;
    } // ctor
} // class TaggedComponent
