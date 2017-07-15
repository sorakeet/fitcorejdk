package com.sun.corba.se.spi.activation;

public final class ORBPortInfo implements org.omg.CORBA.portable.IDLEntity{
    public String orbId=null;
    public int port=(int)0;

    public ORBPortInfo(){
    } // ctor

    public ORBPortInfo(String _orbId,int _port){
        orbId=_orbId;
        port=_port;
    } // ctor
} // class ORBPortInfo
