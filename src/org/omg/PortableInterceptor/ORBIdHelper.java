package org.omg.PortableInterceptor;

// This should actually be the CORBA::ORBid type once that is available
abstract public class ORBIdHelper{
    private static String _id="IDL:omg.org/PortableInterceptor/ORBId:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,String that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_string_tc(0);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(ORBIdHelper.id(),"ORBId",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,String value){
        ostream.write_string(value);
    }

    public static String extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static String read(org.omg.CORBA.portable.InputStream istream){
        String value=null;
        value=istream.read_string();
        return value;
    }
}
