package com.sun.corba.se.PortableActivationIDL.LocatorPackage;

public final class ServerLocationPerORB implements org.omg.CORBA.portable.IDLEntity{
    public String hostname=null;
    public com.sun.corba.se.PortableActivationIDL.EndPointInfo ports[]=null;

    public ServerLocationPerORB(){
    } // ctor

    public ServerLocationPerORB(String _hostname,com.sun.corba.se.PortableActivationIDL.EndPointInfo[] _ports){
        hostname=_hostname;
        ports=_ports;
    } // ctor
} // class ServerLocationPerORB
