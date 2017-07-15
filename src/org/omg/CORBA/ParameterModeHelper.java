package org.omg.CORBA;

abstract public class ParameterModeHelper{
    private static String _id="IDL:omg.org/CORBA/ParameterMode:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,ParameterMode that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().create_enum_tc(ParameterModeHelper.id(),"ParameterMode",new String[]{"PARAM_IN","PARAM_OUT","PARAM_INOUT"});
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,ParameterMode value){
        ostream.write_long(value.value());
    }

    public static ParameterMode extract(Any a){
        return read(a.create_input_stream());
    }

    public static ParameterMode read(org.omg.CORBA.portable.InputStream istream){
        return ParameterMode.from_int(istream.read_long());
    }
}
