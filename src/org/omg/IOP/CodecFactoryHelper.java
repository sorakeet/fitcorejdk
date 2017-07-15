package org.omg.IOP;

abstract public class CodecFactoryHelper{
    private static String _id="IDL:omg.org/IOP/CodecFactory:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,CodecFactory that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(CodecFactoryHelper.id(),"CodecFactory");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,CodecFactory value){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static CodecFactory extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static CodecFactory read(org.omg.CORBA.portable.InputStream istream){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static CodecFactory narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof CodecFactory)
            return (CodecFactory)obj;
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public static CodecFactory unchecked_narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof CodecFactory)
            return (CodecFactory)obj;
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }
}
