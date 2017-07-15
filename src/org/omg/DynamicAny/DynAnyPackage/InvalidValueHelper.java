package org.omg.DynamicAny.DynAnyPackage;

abstract public class InvalidValueHelper{
    private static String _id="IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,InvalidValue that){
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
                    org.omg.CORBA.StructMember[] _members0=new org.omg.CORBA.StructMember[0];
                    org.omg.CORBA.TypeCode _tcOf_members0=null;
                    __typeCode=org.omg.CORBA.ORB.init().create_exception_tc(InvalidValueHelper.id(),"InvalidValue",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,InvalidValue value){
        // write the repository ID
        ostream.write_string(id());
    }

    public static InvalidValue extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static InvalidValue read(org.omg.CORBA.portable.InputStream istream){
        InvalidValue value=new InvalidValue();
        // read and discard the repository ID
        istream.read_string();
        return value;
    }
}
