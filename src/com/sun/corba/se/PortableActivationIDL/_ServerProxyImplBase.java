package com.sun.corba.se.PortableActivationIDL;

public abstract class _ServerProxyImplBase extends org.omg.CORBA.portable.ObjectImpl
        implements ServerProxy, org.omg.CORBA.portable.InvokeHandler{
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:PortableActivationIDL/ServerProxy:1.0"};

    static{
        _methods.put("shutdown",new Integer(0));
        _methods.put("install",new Integer(1));
        _methods.put("uninstall",new Integer(2));
    }

    // Constructors
    public _ServerProxyImplBase(){
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh){
        org.omg.CORBA.portable.OutputStream out=null;
        Integer __method=(Integer)_methods.get($method);
        if(__method==null)
            throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        switch(__method.intValue()){
            /** Shutdown this server.  Returns after orb.shutdown() completes.
             */
            case 0:  // PortableActivationIDL/ServerProxy/shutdown
            {
                this.shutdown();
                out=$rh.createReply();
                break;
            }
            /** Install the server.  Returns after the install hook completes
             * execution in the server.
             */
            case 1:  // PortableActivationIDL/ServerProxy/install
            {
                this.install();
                out=$rh.createReply();
                break;
            }
            /** Uninstall the server.  Returns after the uninstall hook
             * completes execution.
             */
            case 2:  // PortableActivationIDL/ServerProxy/uninstall
            {
                this.uninstall();
                out=$rh.createReply();
                break;
            }
            default:
                throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }
        return out;
    } // _invoke

    public String[] _ids(){
        return (String[])__ids.clone();
    }
} // class _ServerProxyImplBase
