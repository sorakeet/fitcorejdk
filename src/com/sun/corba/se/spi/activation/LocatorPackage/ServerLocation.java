package com.sun.corba.se.spi.activation.LocatorPackage;

public final class ServerLocation implements org.omg.CORBA.portable.IDLEntity{
    public String hostname=null;
    public com.sun.corba.se.spi.activation.ORBPortInfo ports[]=null;

    public ServerLocation(){
    } // ctor

    public ServerLocation(String _hostname,com.sun.corba.se.spi.activation.ORBPortInfo[] _ports){
        hostname=_hostname;
        ports=_ports;
    } // ctor
} // class ServerLocation
