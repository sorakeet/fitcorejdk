package org.omg.DynamicAny;

public final class NameValuePair implements org.omg.CORBA.portable.IDLEntity{
    public String id=null;
    public org.omg.CORBA.Any value=null;

    public NameValuePair(){
    } // ctor

    public NameValuePair(String _id,org.omg.CORBA.Any _value){
        id=_id;
        value=_value;
    } // ctor
} // class NameValuePair
