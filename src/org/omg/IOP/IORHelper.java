package org.omg.IOP;

abstract public class IORHelper{
    private static String _id="IDL:omg.org/IOP/IOR:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,IOR that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            synchronized(org.omg.CORBA.TypeCode.class){
                if(__typeCode==null){
                    if(__active){
                        return org.omg.CORBA.ORB.init().create_recursive_tc(_id);
                    }
                    __active=true;
                    org.omg.CORBA.StructMember[] _members0=new org.omg.CORBA.StructMember[2];
                    org.omg.CORBA.TypeCode _tcOf_members0=null;
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_string_tc(0);
                    _members0[0]=new org.omg.CORBA.StructMember(
                            "type_id",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=TaggedProfileHelper.type();
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_sequence_tc(0,_tcOf_members0);
                    _members0[1]=new org.omg.CORBA.StructMember(
                            "profiles",
                            _tcOf_members0,
                            null);
                    __typeCode=org.omg.CORBA.ORB.init().create_struct_tc(IORHelper.id(),"IOR",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,IOR value){
        ostream.write_string(value.type_id);
        ostream.write_long(value.profiles.length);
        for(int _i0=0;_i0<value.profiles.length;++_i0)
            TaggedProfileHelper.write(ostream,value.profiles[_i0]);
    }

    public static IOR extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static IOR read(org.omg.CORBA.portable.InputStream istream){
        IOR value=new IOR();
        value.type_id=istream.read_string();
        int _len0=istream.read_long();
        value.profiles=new TaggedProfile[_len0];
        for(int _o1=0;_o1<value.profiles.length;++_o1)
            value.profiles[_o1]=TaggedProfileHelper.read(istream);
        return value;
    }
}
