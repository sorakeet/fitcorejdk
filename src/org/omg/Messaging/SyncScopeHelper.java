package org.omg.Messaging;

abstract public class SyncScopeHelper{
    private static String _id="IDL:omg.org/Messaging/SyncScope:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,short that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(SyncScopeHelper.id(),"SyncScope",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,short value){
        ostream.write_short(value);
    }

    public static short extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static short read(org.omg.CORBA.portable.InputStream istream){
        short value=(short)0;
        value=istream.read_short();
        return value;
    }
}
