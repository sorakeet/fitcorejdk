package org.omg.DynamicAny;

abstract public class DynStructHelper{
    private static String _id="IDL:omg.org/DynamicAny/DynStruct:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,DynStruct that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(DynStructHelper.id(),"DynStruct");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,DynStruct value){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static DynStruct extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static DynStruct read(org.omg.CORBA.portable.InputStream istream){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static DynStruct narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof DynStruct)
            return (DynStruct)obj;
        else if(!obj._is_a(id()))
            throw new org.omg.CORBA.BAD_PARAM();
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _DynStructStub stub=new _DynStructStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }

    public static DynStruct unchecked_narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof DynStruct)
            return (DynStruct)obj;
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _DynStructStub stub=new _DynStructStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }
}
