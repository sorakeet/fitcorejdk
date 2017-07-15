package org.omg.PortableInterceptor;

abstract public class IORInterceptor_3_0Helper{
    private static String _id="IDL:omg.org/PortableInterceptor/IORInterceptor_3_0:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,IORInterceptor_3_0 that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(IORInterceptor_3_0Helper.id(),"IORInterceptor_3_0");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,IORInterceptor_3_0 value){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static IORInterceptor_3_0 extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static IORInterceptor_3_0 read(org.omg.CORBA.portable.InputStream istream){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static IORInterceptor_3_0 narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof IORInterceptor_3_0)
            return (IORInterceptor_3_0)obj;
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public static IORInterceptor_3_0 unchecked_narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof IORInterceptor_3_0)
            return (IORInterceptor_3_0)obj;
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }
}
