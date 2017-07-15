package org.omg.DynamicAny;

abstract public class DynArrayHelper{
    private static String _id="IDL:omg.org/DynamicAny/DynArray:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,DynArray that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(DynArrayHelper.id(),"DynArray");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,DynArray value){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static DynArray extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static DynArray read(org.omg.CORBA.portable.InputStream istream){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static DynArray narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof DynArray)
            return (DynArray)obj;
        else if(!obj._is_a(id()))
            throw new org.omg.CORBA.BAD_PARAM();
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _DynArrayStub stub=new _DynArrayStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }

    public static DynArray unchecked_narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof DynArray)
            return (DynArray)obj;
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _DynArrayStub stub=new _DynArrayStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }
}
