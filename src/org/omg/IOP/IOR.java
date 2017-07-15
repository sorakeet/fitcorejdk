package org.omg.IOP;

public final class IOR implements org.omg.CORBA.portable.IDLEntity{
    public String type_id=null;
    public TaggedProfile profiles[]=null;

    public IOR(){
    } // ctor

    public IOR(String _type_id,TaggedProfile[] _profiles){
        type_id=_type_id;
        profiles=_profiles;
    } // ctor
} // class IOR
