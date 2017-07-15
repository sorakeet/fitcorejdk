package com.sun.corba.se.spi.activation;

abstract public class EndPointInfoHelper{
    private static String _id="IDL:activation/EndPointInfo:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,EndPointInfo that){
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
                            "endpointType",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_alias_tc(TCPPortHelper.id(),"TCPPort",_tcOf_members0);
                    _members0[1]=new org.omg.CORBA.StructMember(
                            "port",
                            _tcOf_members0,
                            null);
                    __typeCode=org.omg.CORBA.ORB.init().create_struct_tc(EndPointInfoHelper.id(),"EndPointInfo",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,EndPointInfo value){
        ostream.write_string(value.endpointType);
        ostream.write_long(value.port);
    }

    public static EndPointInfo extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static EndPointInfo read(org.omg.CORBA.portable.InputStream istream){
        EndPointInfo value=new EndPointInfo();
        value.endpointType=istream.read_string();
        value.port=istream.read_long();
        return value;
    }
}
