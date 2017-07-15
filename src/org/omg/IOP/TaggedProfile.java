package org.omg.IOP;

public final class TaggedProfile implements org.omg.CORBA.portable.IDLEntity{
    public int tag=(int)0;
    public byte profile_data[]=null;

    public TaggedProfile(){
    } // ctor

    public TaggedProfile(int _tag,byte[] _profile_data){
        tag=_tag;
        profile_data=_profile_data;
    } // ctor
} // class TaggedProfile
