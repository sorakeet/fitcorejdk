package org.omg.DynamicAny;

abstract public class DynFixedHelper{
    private static String _id="IDL:omg.org/DynamicAny/DynFixed:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,DynFixed that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(DynFixedHelper.id(),"DynFixed");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,DynFixed value){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static DynFixed extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static DynFixed read(org.omg.CORBA.portable.InputStream istream){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static DynFixed narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof DynFixed)
            return (DynFixed)obj;
        else if(!obj._is_a(id()))
            throw new org.omg.CORBA.BAD_PARAM();
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _DynFixedStub stub=new _DynFixedStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }

    public static DynFixed unchecked_narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof DynFixed)
            return (DynFixed)obj;
        else{
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _DynFixedStub stub=new _DynFixedStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }
}
