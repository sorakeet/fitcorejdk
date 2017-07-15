package org.omg.CosNaming.NamingContextPackage;

abstract public class NotFoundHelper{
    private static String _id="IDL:omg.org/CosNaming/NamingContext/NotFound:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,NotFound that){
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
                    _tcOf_members0=NotFoundReasonHelper.type();
                    _members0[0]=new org.omg.CORBA.StructMember(
                            "why",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=org.omg.CosNaming.NameComponentHelper.type();
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_sequence_tc(0,_tcOf_members0);
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_alias_tc(org.omg.CosNaming.NameHelper.id(),"Name",_tcOf_members0);
                    _members0[1]=new org.omg.CORBA.StructMember(
                            "rest_of_name",
                            _tcOf_members0,
                            null);
                    __typeCode=org.omg.CORBA.ORB.init().create_exception_tc(NotFoundHelper.id(),"NotFound",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,NotFound value){
        // write the repository ID
        ostream.write_string(id());
        NotFoundReasonHelper.write(ostream,value.why);
        org.omg.CosNaming.NameHelper.write(ostream,value.rest_of_name);
    }

    public static NotFound extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static NotFound read(org.omg.CORBA.portable.InputStream istream){
        NotFound value=new NotFound();
        // read and discard the repository ID
        istream.read_string();
        value.why=NotFoundReasonHelper.read(istream);
        value.rest_of_name=org.omg.CosNaming.NameHelper.read(istream);
        return value;
    }
}
