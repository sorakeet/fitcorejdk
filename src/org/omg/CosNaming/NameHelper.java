package org.omg.CosNaming;

abstract public class NameHelper{
    private static String _id="IDL:omg.org/CosNaming/Name:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,NameComponent[] that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=NameComponentHelper.type();
            __typeCode=org.omg.CORBA.ORB.init().create_sequence_tc(0,__typeCode);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(NameHelper.id(),"Name",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,NameComponent[] value){
        ostream.write_long(value.length);
        for(int _i0=0;_i0<value.length;++_i0)
            NameComponentHelper.write(ostream,value[_i0]);
    }

    public static NameComponent[] extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static NameComponent[] read(org.omg.CORBA.portable.InputStream istream){
        NameComponent value[]=null;
        int _len0=istream.read_long();
        value=new NameComponent[_len0];
        for(int _o1=0;_o1<value.length;++_o1)
            value[_o1]=NameComponentHelper.read(istream);
        return value;
    }
}
