package com.sun.corba.se.spi.activation;

public final class EndPointInfo implements org.omg.CORBA.portable.IDLEntity{
    public String endpointType=null;
    public int port=(int)0;

    public EndPointInfo(){
    } // ctor

    public EndPointInfo(String _endpointType,int _port){
        endpointType=_endpointType;
        port=_port;
    } // ctor
} // class EndPointInfo
