package org.omg.CosNaming;

abstract public class BindingTypeHelper{
    private static String _id="IDL:omg.org/CosNaming/BindingType:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,BindingType that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_enum_tc(BindingTypeHelper.id(),"BindingType",new String[]{"nobject","ncontext"});
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,BindingType value){
        ostream.write_long(value.value());
    }

    public static BindingType extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static BindingType read(org.omg.CORBA.portable.InputStream istream){
        return BindingType.from_int(istream.read_long());
    }
}
