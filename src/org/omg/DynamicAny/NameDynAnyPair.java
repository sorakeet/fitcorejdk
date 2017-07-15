package org.omg.DynamicAny;

public final class NameDynAnyPair implements org.omg.CORBA.portable.IDLEntity{
    public String id=null;
    public DynAny value=null;

    public NameDynAnyPair(){
    } // ctor

    public NameDynAnyPair(String _id,DynAny _value){
        id=_id;
        value=_value;
    } // ctor
} // class NameDynAnyPair
