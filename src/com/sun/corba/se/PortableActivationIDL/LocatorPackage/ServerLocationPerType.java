package com.sun.corba.se.PortableActivationIDL.LocatorPackage;

public final class ServerLocationPerType implements org.omg.CORBA.portable.IDLEntity{
    public String hostname=null;
    public com.sun.corba.se.PortableActivationIDL.ORBPortInfo ports[]=null;

    public ServerLocationPerType(){
    } // ctor

    public ServerLocationPerType(String _hostname,com.sun.corba.se.PortableActivationIDL.ORBPortInfo[] _ports){
        hostname=_hostname;
        ports=_ports;
    } // ctor
} // class ServerLocationPerType
