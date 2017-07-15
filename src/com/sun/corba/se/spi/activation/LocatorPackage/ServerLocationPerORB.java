package com.sun.corba.se.spi.activation.LocatorPackage;

public final class ServerLocationPerORB implements org.omg.CORBA.portable.IDLEntity{
    public String hostname=null;
    public com.sun.corba.se.spi.activation.EndPointInfo ports[]=null;

    public ServerLocationPerORB(){
    } // ctor

    public ServerLocationPerORB(String _hostname,com.sun.corba.se.spi.activation.EndPointInfo[] _ports){
        hostname=_hostname;
        ports=_ports;
    } // ctor
} // class ServerLocationPerORB
