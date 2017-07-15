package org.omg.PortableServer;

public class _ServantActivatorStub extends org.omg.CORBA.portable.ObjectImpl implements ServantActivator{
    final public static Class _opsClass=ServantActivatorOperations.class;
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/PortableServer/ServantActivator:2.3",
            "IDL:omg.org/PortableServer/ServantManager:1.0"};

    public Servant incarnate(byte[] oid,POA adapter) throws ForwardRequest{
        org.omg.CORBA.portable.ServantObject $so=_servant_preinvoke("incarnate",_opsClass);
        ServantActivatorOperations $self=(ServantActivatorOperations)$so.servant;
        try{
            return $self.incarnate(oid,adapter);
        }finally{
            _servant_postinvoke($so);
        }
    } // incarnate

    public void etherealize(byte[] oid,POA adapter,Servant serv,boolean cleanup_in_progress,boolean remaining_activations){
        org.omg.CORBA.portable.ServantObject $so=_servant_preinvoke("etherealize",_opsClass);
        ServantActivatorOperations $self=(ServantActivatorOperations)$so.servant;
        try{
            $self.etherealize(oid,adapter,serv,cleanup_in_progress,remaining_activations);
        }finally{
            _servant_postinvoke($so);
        }
    } // etherealize

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
} // class _ServantActivatorStub
