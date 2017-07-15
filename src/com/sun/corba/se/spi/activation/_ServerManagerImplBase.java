package com.sun.corba.se.spi.activation;

public abstract class _ServerManagerImplBase extends org.omg.CORBA.portable.ObjectImpl
        implements ServerManager, org.omg.CORBA.portable.InvokeHandler{
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:activation/ServerManager:1.0",
            "IDL:activation/Activator:1.0",
            "IDL:activation/Locator:1.0"};

    static{
        _methods.put("active",new Integer(0));
        _methods.put("registerEndpoints",new Integer(1));
        _methods.put("getActiveServers",new Integer(2));
        _methods.put("activate",new Integer(3));
        _methods.put("shutdown",new Integer(4));
        _methods.put("install",new Integer(5));
        _methods.put("getORBNames",new Integer(6));
        _methods.put("uninstall",new Integer(7));
        _methods.put("locateServer",new Integer(8));
        _methods.put("locateServerForORB",new Integer(9));
        _methods.put("getEndpoint",new Integer(10));
        _methods.put("getServerPortForType",new Integer(11));
    }

    // Constructors
    public _ServerManagerImplBase(){
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh){
        org.omg.CORBA.portable.OutputStream out=null;
        Integer __method=(Integer)_methods.get($method);
        if(__method==null)
            throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        switch(__method.intValue()){
            // A new ORB started server registers itself with the Activator
            case 0:  // activation/Activator/active
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    Server serverObj=ServerHelper.read(in);
                    this.active(serverId,serverObj);
                    out=$rh.createReply();
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }
                break;
            }
            // Install a particular kind of endpoint
            case 1:  // activation/Activator/registerEndpoints
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    String orbId=ORBidHelper.read(in);
                    EndPointInfo endPointInfo[]=EndpointInfoListHelper.read(in);
                    this.registerEndpoints(serverId,orbId,endPointInfo);
                    out=$rh.createReply();
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }catch(NoSuchEndPoint $ex){
                    out=$rh.createExceptionReply();
                    NoSuchEndPointHelper.write(out,$ex);
                }catch(ORBAlreadyRegistered $ex){
                    out=$rh.createExceptionReply();
                    ORBAlreadyRegisteredHelper.write(out,$ex);
                }
                break;
            }
            // list active servers
            case 2:  // activation/Activator/getActiveServers
            {
                int $result[]=null;
                $result=this.getActiveServers();
                out=$rh.createReply();
                ServerIdsHelper.write(out,$result);
                break;
            }
            // If the server is not running, start it up.
            case 3:  // activation/Activator/activate
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    this.activate(serverId);
                    out=$rh.createReply();
                }catch(ServerAlreadyActive $ex){
                    out=$rh.createExceptionReply();
                    ServerAlreadyActiveHelper.write(out,$ex);
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }catch(ServerHeldDown $ex){
                    out=$rh.createExceptionReply();
                    ServerHeldDownHelper.write(out,$ex);
                }
                break;
            }
            // If the server is running, shut it down
            case 4:  // activation/Activator/shutdown
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    this.shutdown(serverId);
                    out=$rh.createReply();
                }catch(ServerNotActive $ex){
                    out=$rh.createExceptionReply();
                    ServerNotActiveHelper.write(out,$ex);
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }
                break;
            }
            // currently running, this method will activate it.
            case 5:  // activation/Activator/install
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    this.install(serverId);
                    out=$rh.createReply();
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }catch(ServerHeldDown $ex){
                    out=$rh.createExceptionReply();
                    ServerHeldDownHelper.write(out,$ex);
                }catch(ServerAlreadyInstalled $ex){
                    out=$rh.createExceptionReply();
                    ServerAlreadyInstalledHelper.write(out,$ex);
                }
                break;
            }
            // list all registered ORBs for a server
            case 6:  // activation/Activator/getORBNames
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    String $result[]=null;
                    $result=this.getORBNames(serverId);
                    out=$rh.createReply();
                    ORBidListHelper.write(out,$result);
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }
                break;
            }
            // After this hook completes, the server may still be running.
            case 7:  // activation/Activator/uninstall
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    this.uninstall(serverId);
                    out=$rh.createReply();
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }catch(ServerHeldDown $ex){
                    out=$rh.createExceptionReply();
                    ServerHeldDownHelper.write(out,$ex);
                }catch(ServerAlreadyUninstalled $ex){
                    out=$rh.createExceptionReply();
                    ServerAlreadyUninstalledHelper.write(out,$ex);
                }
                break;
            }
            // Starts the server if it is not already running.
            case 8:  // activation/Locator/locateServer
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    String endPoint=in.read_string();
                    com.sun.corba.se.spi.activation.LocatorPackage.ServerLocation $result=null;
                    $result=this.locateServer(serverId,endPoint);
                    out=$rh.createReply();
                    com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationHelper.write(out,$result);
                }catch(NoSuchEndPoint $ex){
                    out=$rh.createExceptionReply();
                    NoSuchEndPointHelper.write(out,$ex);
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }catch(ServerHeldDown $ex){
                    out=$rh.createExceptionReply();
                    ServerHeldDownHelper.write(out,$ex);
                }
                break;
            }
            // Starts the server if it is not already running.
            case 9:  // activation/Locator/locateServerForORB
            {
                try{
                    int serverId=ServerIdHelper.read(in);
                    String orbId=ORBidHelper.read(in);
                    com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB $result=null;
                    $result=this.locateServerForORB(serverId,orbId);
                    out=$rh.createReply();
                    com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORBHelper.write(out,$result);
                }catch(InvalidORBid $ex){
                    out=$rh.createExceptionReply();
                    InvalidORBidHelper.write(out,$ex);
                }catch(ServerNotRegistered $ex){
                    out=$rh.createExceptionReply();
                    ServerNotRegisteredHelper.write(out,$ex);
                }catch(ServerHeldDown $ex){
                    out=$rh.createExceptionReply();
                    ServerHeldDownHelper.write(out,$ex);
                }
                break;
            }
            // get the port for the endpoint of the locator
            case 10:  // activation/Locator/getEndpoint
            {
                try{
                    String endPointType=in.read_string();
                    int $result=(int)0;
                    $result=this.getEndpoint(endPointType);
                    out=$rh.createReply();
                    out.write_long($result);
                }catch(NoSuchEndPoint $ex){
                    out=$rh.createExceptionReply();
                    NoSuchEndPointHelper.write(out,$ex);
                }
                break;
            }
            // to pick a particular port type.
            case 11:  // activation/Locator/getServerPortForType
            {
                try{
                    com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB location=com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORBHelper.read(in);
                    String endPointType=in.read_string();
                    int $result=(int)0;
                    $result=this.getServerPortForType(location,endPointType);
                    out=$rh.createReply();
                    out.write_long($result);
                }catch(NoSuchEndPoint $ex){
                    out=$rh.createExceptionReply();
                    NoSuchEndPointHelper.write(out,$ex);
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
} // class _ServerManagerImplBase
