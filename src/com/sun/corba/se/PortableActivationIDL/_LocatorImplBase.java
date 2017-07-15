package com.sun.corba.se.PortableActivationIDL;

public abstract class _LocatorImplBase extends org.omg.CORBA.portable.ObjectImpl
        implements Locator, org.omg.CORBA.portable.InvokeHandler{
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:PortableActivationIDL/Locator:1.0"};

    static{
        _methods.put("locateServer",new Integer(0));
        _methods.put("locateServerForORB",new Integer(1));
        _methods.put("getEndpoint",new Integer(2));
        _methods.put("getServerPortForType",new Integer(3));
    }

    // Constructors
    public _LocatorImplBase(){
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh){
        org.omg.CORBA.portable.OutputStream out=null;
        Integer __method=(Integer)_methods.get($method);
        if(__method==null)
            throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        switch(__method.intValue()){
            /** locate server - returns the port with a specific type for all registered
             * ORBs of an active server.
             * Starts the server if it is not already running.
             */
            case 0:  // PortableActivationIDL/Locator/locateServer
            {
                try{
                    String serverId=org.omg.PortableInterceptor.ServerIdHelper.read(in);
                    String endPoint=in.read_string();
                    com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerType $result=null;
                    $result=this.locateServer(serverId,endPoint);
                    out=$rh.createReply();
                    com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerTypeHelper.write(out,$result);
                }catch(NoSuchEndPoint $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.NoSuchEndPointHelper.write(out,$ex);
                }catch(com.sun.corba.se.PortableActivationIDL.ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.write(out,$ex);
                }catch(ServerHeldDown $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.ServerHeldDownHelper.write(out,$ex);
                }
                break;
            }
            /** locate server - returns all ports registered with a specified ORB for
             * an active server
             * Starts the server if it is not already running.
             */
            case 1:  // PortableActivationIDL/Locator/locateServerForORB
            {
                try{
                    String serverId=org.omg.PortableInterceptor.ServerIdHelper.read(in);
                    String orbId=org.omg.PortableInterceptor.ORBIdHelper.read(in);
                    com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORB $result=null;
                    $result=this.locateServerForORB(serverId,orbId);
                    out=$rh.createReply();
                    com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORBHelper.write(out,$result);
                }catch(InvalidORBid $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.InvalidORBidHelper.write(out,$ex);
                }catch(com.sun.corba.se.PortableActivationIDL.ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.write(out,$ex);
                }catch(ServerHeldDown $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.ServerHeldDownHelper.write(out,$ex);
                }
                break;
            }
            /** get the port for the endpoint of the locator
             */
            case 2:  // PortableActivationIDL/Locator/getEndpoint
            {
                try{
                    String endPointType=in.read_string();
                    int $result=(int)0;
                    $result=this.getEndpoint(endPointType);
                    out=$rh.createReply();
                    out.write_long($result);
                }catch(NoSuchEndPoint $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.NoSuchEndPointHelper.write(out,$ex);
                }
                break;
            }
            /** Useful from external BadServerIdHandlers which need
             * to pick a particular port type.
             */
            case 3:  // PortableActivationIDL/Locator/getServerPortForType
            {
                try{
                    com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORB location=com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORBHelper.read(in);
                    String endPointType=in.read_string();
                    int $result=(int)0;
                    $result=this.getServerPortForType(location,endPointType);
                    out=$rh.createReply();
                    out.write_long($result);
                }catch(NoSuchEndPoint $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.PortableActivationIDL.NoSuchEndPointHelper.write(out,$ex);
                }
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
} // class _LocatorImplBase
