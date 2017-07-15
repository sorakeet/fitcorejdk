package org.omg.PortableInterceptor;

abstract public class ObjectIdHelper{
    private static String _id="IDL:omg.org/PortableInterceptor/ObjectId:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,byte[] that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_octet);
            __typeCode=org.omg.CORBA.ORB.init().create_sequence_tc(0,__typeCode);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(org.omg.CORBA.OctetSeqHelper.id(),"OctetSeq",__typeCode);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(ObjectIdHelper.id(),"ObjectId",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,byte[] value){
        org.omg.CORBA.OctetSeqHelper.write(ostream,value);
    }

    public static byte[] extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static byte[] read(org.omg.CORBA.portable.InputStream istream){
        byte value[]=null;
        value=org.omg.CORBA.OctetSeqHelper.read(istream);
        return value;
    }
}
