package org.omg.DynamicAny;

abstract public class DynAnySeqHelper{
    private static String _id="IDL:omg.org/DynamicAny/DynAnySeq:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,DynAny[] that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=DynAnyHelper.type();
            __typeCode=org.omg.CORBA.ORB.init().create_sequence_tc(0,__typeCode);
            __typeCode=org.omg.CORBA.ORB.init().create_alias_tc(DynAnySeqHelper.id(),"DynAnySeq",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,DynAny[] value){
        ostream.write_long(value.length);
        for(int _i0=0;_i0<value.length;++_i0)
            DynAnyHelper.write(ostream,value[_i0]);
    }

    public static DynAny[] extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static DynAny[] read(org.omg.CORBA.portable.InputStream istream){
        DynAny value[]=null;
        int _len0=istream.read_long();
        value=new DynAny[_len0];
        for(int _o1=0;_o1<value.length;++_o1)
            value[_o1]=DynAnyHelper.read(istream);
        return value;
    }
}
