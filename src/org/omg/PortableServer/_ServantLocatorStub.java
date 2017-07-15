package org.omg.PortableServer;

public class _ServantLocatorStub extends org.omg.CORBA.portable.ObjectImpl implements ServantLocator{
    final public static Class _opsClass=ServantLocatorOperations.class;
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/PortableServer/ServantLocator:1.0",
            "IDL:omg.org/PortableServer/ServantManager:1.0"};

    public Servant preinvoke(byte[] oid,POA adapter,String operation,org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie) throws ForwardRequest{
        org.omg.CORBA.portable.ServantObject $so=_servant_preinvoke("preinvoke",_opsClass);
        ServantLocatorOperations $self=(ServantLocatorOperations)$so.servant;
        try{
            return $self.preinvoke(oid,adapter,operation,the_cookie);
        }finally{
            _servant_postinvoke($so);
        }
    } // preinvoke

    public void postinvoke(byte[] oid,POA adapter,String operation,Object the_cookie,Servant the_servant){
        org.omg.CORBA.portable.ServantObject $so=_servant_preinvoke("postinvoke",_opsClass);
        ServantLocatorOperations $self=(ServantLocatorOperations)$so.servant;
        try{
            $self.postinvoke(oid,adapter,operation,the_cookie,the_servant);
        }finally{
            _servant_postinvoke($so);
        }
    } // postinvoke

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
} // class _ServantLocatorStub
