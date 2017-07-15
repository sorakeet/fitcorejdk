package org.omg.PortableInterceptor;

abstract public class AdapterNameHelper{
    private static String _id="IDL:omg.org/PortableInterceptor/AdapterName:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,String[] that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_string_tc(0);
            __typeCode=org.omg.CORBA.ORB.init().create_sequence_tc(0,__typeCode);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(org.omg.CORBA.StringSeqHelper.id(),"StringSeq",__typeCode);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(AdapterNameHelper.id(),"AdapterName",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,String[] value){
        org.omg.CORBA.StringSeqHelper.write(ostream,value);
    }

    public static String[] extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static String[] read(org.omg.CORBA.portable.InputStream istream){
        String value[]=null;
        value=org.omg.CORBA.StringSeqHelper.read(istream);
        return value;
    }
}
