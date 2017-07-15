package org.omg.PortableServer;

public abstract class ServantActivatorPOA extends Servant
        implements ServantActivatorOperations, org.omg.CORBA.portable.InvokeHandler{
    // Constructors
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/PortableServer/ServantActivator:2.3",
            "IDL:omg.org/PortableServer/ServantManager:1.0"};

    static{
        _methods.put("incarnate",new Integer(0));
        _methods.put("etherealize",new Integer(1));
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh){
        throw new org.omg.CORBA.BAD_OPERATION();
    } // _invoke

    public String[] _all_interfaces(POA poa,byte[] objectId){
        return (String[])__ids.clone();
    }

    public ServantActivator _this(){
        return ServantActivatorHelper.narrow(
                super._this_object());
    }

    public ServantActivator _this(org.omg.CORBA.ORB orb){
        return ServantActivatorHelper.narrow(
                super._this_object(orb));
    }
} // class ServantActivatorPOA
