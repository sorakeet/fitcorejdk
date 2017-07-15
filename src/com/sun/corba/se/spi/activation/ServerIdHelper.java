package com.sun.corba.se.spi.activation;

abstract public class ServerIdHelper{
    private static String _id="IDL:activation/ServerId:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,int that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(ServerIdHelper.id(),"ServerId",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,int value){
        ostream.write_long(value);
    }

    public static int extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static int read(org.omg.CORBA.portable.InputStream istream){
        int value=(int)0;
        value=istream.read_long();
        return value;
    }
}
