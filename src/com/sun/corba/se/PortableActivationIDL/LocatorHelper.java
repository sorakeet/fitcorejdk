package com.sun.corba.se.PortableActivationIDL;

abstract public class LocatorHelper{
    private static String _id="IDL:PortableActivationIDL/Locator:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,Locator that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(LocatorHelper.id(),"Locator");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,Locator value){
        ostream.write_Object((org.omg.CORBA.Object)value);
    }

    public static Locator extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static Locator read(org.omg.CORBA.portable.InputStream istream){
        return narrow(istream.read_Object(_LocatorStub.class));
    }

    public static Locator narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof Locator)
            return (Locator)obj;
        else if(!obj._is_a(id()))
            throw new org.omg.CORBA.BAD_PARAM();
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _LocatorStub stub=new _LocatorStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }

    public static Locator unchecked_narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof Locator)
            return (Locator)obj;
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _LocatorStub stub=new _LocatorStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }
}
