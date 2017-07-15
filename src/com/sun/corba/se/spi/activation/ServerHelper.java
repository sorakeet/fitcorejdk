package com.sun.corba.se.spi.activation;

abstract public class ServerHelper{
    private static String _id="IDL:activation/Server:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,Server that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(ServerHelper.id(),"Server");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,Server value){
        ostream.write_Object((org.omg.CORBA.Object)value);
    }

    public static Server extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static Server read(org.omg.CORBA.portable.InputStream istream){
        return narrow(istream.read_Object(_ServerStub.class));
    }

    public static Server narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof Server)
            return (Server)obj;
        else if(!obj._is_a(id()))
            throw new org.omg.CORBA.BAD_PARAM();
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _ServerStub stub=new _ServerStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }

    public static Server unchecked_narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof Server)
            return (Server)obj;
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _ServerStub stub=new _ServerStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }
}
