package com.sun.corba.se.spi.activation;

public final class InvalidORBidHolder implements org.omg.CORBA.portable.Streamable{
    public InvalidORBid value=null;

    public InvalidORBidHolder(){
    }

    public InvalidORBidHolder(InvalidORBid initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=InvalidORBidHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        InvalidORBidHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return InvalidORBidHelper.type();
    }
}
