package com.sun.corba.se.spi.activation;

abstract public class ORBAlreadyRegisteredHelper{
    private static String _id="IDL:activation/ORBAlreadyRegistered:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,ORBAlreadyRegistered that){
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
                    org.omg.CORBA.StructMember[] _members0=new org.omg.CORBA.StructMember[1];
                    org.omg.CORBA.TypeCode _tcOf_members0=null;
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_string_tc(0);
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_alias_tc(ORBidHelper.id(),"ORBid",_tcOf_members0);
                    _members0[0]=new org.omg.CORBA.StructMember(
                            "orbId",
                            _tcOf_members0,
                            null);
                    __typeCode=org.omg.CORBA.ORB.init().create_exception_tc(ORBAlreadyRegisteredHelper.id(),"ORBAlreadyRegistered",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,ORBAlreadyRegistered value){
        // write the repository ID
        ostream.write_string(id());
        ostream.write_string(value.orbId);
    }

    public static ORBAlreadyRegistered extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static ORBAlreadyRegistered read(org.omg.CORBA.portable.InputStream istream){
        ORBAlreadyRegistered value=new ORBAlreadyRegistered();
        // read and discard the repository ID
        istream.read_string();
        value.orbId=istream.read_string();
        return value;
    }
}
