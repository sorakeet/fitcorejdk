package com.sun.corba.se.spi.activation.LocatorPackage;

abstract public class ServerLocationPerORBHelper{
    private static String _id="IDL:activation/Locator/ServerLocationPerORB:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,ServerLocationPerORB that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            synchronized(org.omg.CORBA.TypeCode.class){
                if(__typeCode==null){
                    if(__active){
                        return org.omg.CORBA.ORB.init().create_recursive_tc(_id);
                    }
                    __active=true;
                    org.omg.CORBA.StructMember[] _members0=new org.omg.CORBA.StructMember[2];
                    org.omg.CORBA.TypeCode _tcOf_members0=null;
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_string_tc(0);
                    _members0[0]=new org.omg.CORBA.StructMember(
                            "hostname",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=com.sun.corba.se.spi.activation.EndPointInfoHelper.type();
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_sequence_tc(0,_tcOf_members0);
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_alias_tc(com.sun.corba.se.spi.activation.EndpointInfoListHelper.id(),"EndpointInfoList",_tcOf_members0);
                    _members0[1]=new org.omg.CORBA.StructMember(
                            "ports",
                            _tcOf_members0,
                            null);
                    __typeCode=org.omg.CORBA.ORB.init().create_struct_tc(ServerLocationPerORBHelper.id(),"ServerLocationPerORB",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,ServerLocationPerORB value){
        ostream.write_string(value.hostname);
        com.sun.corba.se.spi.activation.EndpointInfoListHelper.write(ostream,value.ports);
    }

    public static ServerLocationPerORB extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static ServerLocationPerORB read(org.omg.CORBA.portable.InputStream istream){
        ServerLocationPerORB value=new ServerLocationPerORB();
        value.hostname=istream.read_string();
        value.ports=com.sun.corba.se.spi.activation.EndpointInfoListHelper.read(istream);
        return value;
    }
}
