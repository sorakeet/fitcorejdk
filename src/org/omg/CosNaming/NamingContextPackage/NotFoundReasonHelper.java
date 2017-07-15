package org.omg.CosNaming.NamingContextPackage;

abstract public class NotFoundReasonHelper{
    private static String _id="IDL:omg.org/CosNaming/NamingContext/NotFoundReason:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,NotFoundReason that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_enum_tc(NotFoundReasonHelper.id(),"NotFoundReason",new String[]{"missing_node","not_context","not_object"});
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,NotFoundReason value){
        ostream.write_long(value.value());
    }

    public static NotFoundReason extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static NotFoundReason read(org.omg.CORBA.portable.InputStream istream){
        return NotFoundReason.from_int(istream.read_long());
    }
}
