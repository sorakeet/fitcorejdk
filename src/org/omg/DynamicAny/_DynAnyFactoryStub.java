package org.omg.DynamicAny;

public class _DynAnyFactoryStub extends org.omg.CORBA.portable.ObjectImpl implements DynAnyFactory{
    final public static Class _opsClass=DynAnyFactoryOperations.class;
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/DynamicAny/DynAnyFactory:1.0"};

    public DynAny create_dyn_any(org.omg.CORBA.Any value) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode{
        org.omg.CORBA.portable.ServantObject $so=_servant_preinvoke("create_dyn_any",_opsClass);
        DynAnyFactoryOperations $self=(DynAnyFactoryOperations)$so.servant;
        try{
            return $self.create_dyn_any(value);
        }finally{
            _servant_postinvoke($so);
        }
    } // create_dyn_any

    public DynAny create_dyn_any_from_type_code(org.omg.CORBA.TypeCode type) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode{
        org.omg.CORBA.portable.ServantObject $so=_servant_preinvoke("create_dyn_any_from_type_code",_opsClass);
        DynAnyFactoryOperations $self=(DynAnyFactoryOperations)$so.servant;
        try{
            return $self.create_dyn_any_from_type_code(type);
        }finally{
            _servant_postinvoke($so);
        }
    } // create_dyn_any_from_type_code

    public String[] _ids(){
        return (String[])__ids.clone();
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException{
        String str=s.readUTF();
        String[] args=null;
        java.util.Properties props=null;
        org.omg.CORBA.ORB orb=org.omg.CORBA.ORB.init(args,props);
        try{
            org.omg.CORBA.Object obj=orb.string_to_object(str);
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _set_delegate(delegate);
        }finally{
            orb.destroy();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        String[] args=null;
        java.util.Properties props=null;
        org.omg.CORBA.ORB orb=org.omg.CORBA.ORB.init(args,props);
        try{
            String str=orb.object_to_string(this);
            s.writeUTF(str);
        }finally{
            orb.destroy();
        }
    }
} // class _DynAnyFactoryStub
