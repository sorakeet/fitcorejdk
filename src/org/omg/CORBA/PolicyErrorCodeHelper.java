package org.omg.CORBA;

abstract public class PolicyErrorCodeHelper{
    private static String _id="IDL:omg.org/CORBA/PolicyErrorCode:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,short that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().get_primitive_tc(TCKind.tk_short);
            __typeCode=ORB.init().create_alias_tc(PolicyErrorCodeHelper.id(),"PolicyErrorCode",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,short value){
        ostream.write_short(value);
    }

    public static short extract(Any a){
        return read(a.create_input_stream());
    }

    public static short read(org.omg.CORBA.portable.InputStream istream){
        short value=(short)0;
        value=istream.read_short();
        return value;
    }
}
