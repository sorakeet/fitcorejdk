package org.omg.PortableInterceptor;

abstract public class ObjectReferenceTemplateHelper{
    private static String _id="IDL:omg.org/PortableInterceptor/ObjectReferenceTemplate:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,ObjectReferenceTemplate that){
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
                    org.omg.CORBA.ValueMember[] _members0=new org.omg.CORBA.ValueMember[0];
                    org.omg.CORBA.TypeCode _tcOf_members0=null;
                    __typeCode=org.omg.CORBA.ORB.init().create_value_tc(_id,"ObjectReferenceTemplate",org.omg.CORBA.VM_ABSTRACT.value,null,_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,ObjectReferenceTemplate value){
        ((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value(value,id());
    }

    public static String id(){
        return _id;
    }

    public static ObjectReferenceTemplate extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static ObjectReferenceTemplate read(org.omg.CORBA.portable.InputStream istream){
        return (ObjectReferenceTemplate)((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(id());
    }
}
