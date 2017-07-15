package org.omg.PortableServer;

public abstract class ServantLocatorPOA extends Servant
        implements ServantLocatorOperations, org.omg.CORBA.portable.InvokeHandler{
    // Constructors
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/PortableServer/ServantLocator:1.0",
            "IDL:omg.org/PortableServer/ServantManager:1.0"};

    static{
        _methods.put("preinvoke",new Integer(0));
        _methods.put("postinvoke",new Integer(1));
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh){
        throw new org.omg.CORBA.BAD_OPERATION();
    } // _invoke

    public String[] _all_interfaces(POA poa,byte[] objectId){
        return (String[])__ids.clone();
    }

    public ServantLocator _this(){
        return ServantLocatorHelper.narrow(
                super._this_object());
    }

    public ServantLocator _this(org.omg.CORBA.ORB orb){
        return ServantLocatorHelper.narrow(
                super._this_object(orb));
    }
} // class ServantLocatorPOA
