package org.omg.CosNaming;

abstract public class BindingHelper{
    private static String _id="IDL:omg.org/CosNaming/Binding:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,Binding that){
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
                    _tcOf_members0=NameComponentHelper.type();
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_sequence_tc(0,_tcOf_members0);
                    _tcOf_members0=org.omg.CORBA.ORB.init().create_alias_tc(NameHelper.id(),"Name",_tcOf_members0);
                    _members0[0]=new org.omg.CORBA.StructMember(
                            "binding_name",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=BindingTypeHelper.type();
                    _members0[1]=new org.omg.CORBA.StructMember(
                            "binding_type",
                            _tcOf_members0,
                            null);
                    __typeCode=org.omg.CORBA.ORB.init().create_struct_tc(BindingHelper.id(),"Binding",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,Binding value){
        NameHelper.write(ostream,value.binding_name);
        BindingTypeHelper.write(ostream,value.binding_type);
    }

    public static Binding extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static Binding read(org.omg.CORBA.portable.InputStream istream){
        Binding value=new Binding();
        value.binding_name=NameHelper.read(istream);
        value.binding_type=BindingTypeHelper.read(istream);
        return value;
    }
}
