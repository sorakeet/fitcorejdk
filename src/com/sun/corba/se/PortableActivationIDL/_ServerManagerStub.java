package com.sun.corba.se.PortableActivationIDL;

public class _ServerManagerStub extends org.omg.CORBA.portable.ObjectImpl implements ServerManager{
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:PortableActivationIDL/ServerManager:1.0",
            "IDL:PortableActivationIDL/Activator:1.0",
            "IDL:PortableActivationIDL/Locator:1.0"};

    public void registerServer(String serverId,ServerProxy serverObj) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("registerServer",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            ServerProxyHelper.write($out,serverObj);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            registerServer(serverId,serverObj);
        }finally{
            _releaseReply($in);
        }
    } // registerServer

    public void serverGoingDown(String serverId){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("serverGoingDown",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            serverGoingDown(serverId);
        }finally{
            _releaseReply($in);
        }
    } // serverGoingDown

    public void registerORB(String serverId,String orbId,ORBProxy orb,EndPointInfo[] endPointInfo) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, NoSuchEndPoint, com.sun.corba.se.PortableActivationIDL.ORBAlreadyRegistered{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("registerORB",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            org.omg.PortableInterceptor.ORBIdHelper.write($out,orbId);
            ORBProxyHelper.write($out,orb);
            com.sun.corba.se.PortableActivationIDL.EndpointInfoListHelper.write($out,endPointInfo);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/NoSuchEndPoint:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.NoSuchEndPointHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ORBAlreadyRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ORBAlreadyRegisteredHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            registerORB(serverId,orbId,orb,endPointInfo);
        }finally{
            _releaseReply($in);
        }
    } // registerORB

    public org.omg.PortableInterceptor.ObjectReferenceTemplate registerPOA(String serverId,String orbId,org.omg.PortableInterceptor.ObjectReferenceTemplate poaTemplate){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("registerPOA",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            org.omg.PortableInterceptor.ORBIdHelper.write($out,orbId);
            org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.write($out,poaTemplate);
            $in=_invoke($out);
            org.omg.PortableInterceptor.ObjectReferenceTemplate $result=org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return registerPOA(serverId,orbId,poaTemplate);
        }finally{
            _releaseReply($in);
        }
    } // registerPOA

    public void poaDestroyed(String serverId,String orbId,org.omg.PortableInterceptor.ObjectReferenceTemplate poaTemplate){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("poaDestroyed",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            org.omg.PortableInterceptor.ORBIdHelper.write($out,orbId);
            org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.write($out,poaTemplate);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            poaDestroyed(serverId,orbId,poaTemplate);
        }finally{
            _releaseReply($in);
        }
    } // poaDestroyed

    public void activate(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerAlreadyActive, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("activate",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/ServerAlreadyActive:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerAlreadyActiveHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerHeldDown:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerHeldDownHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            activate(serverId);
        }finally{
            _releaseReply($in);
        }
    } // activate

    public void shutdown(String serverId) throws ServerNotActive, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("shutdown",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/ServerNotActive:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotActiveHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            shutdown(serverId);
        }finally{
            _releaseReply($in);
        }
    } // shutdown

    public void install(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown, com.sun.corba.se.PortableActivationIDL.ServerAlreadyInstalled{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("install",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerHeldDown:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerHeldDownHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerAlreadyInstalled:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerAlreadyInstalledHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            install(serverId);
        }finally{
            _releaseReply($in);
        }
    } // install

    public void uninstall(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown, com.sun.corba.se.PortableActivationIDL.ServerAlreadyUninstalled{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("uninstall",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerHeldDown:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerHeldDownHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerAlreadyUninstalled:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerAlreadyUninstalledHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            uninstall(serverId);
        }finally{
            _releaseReply($in);
        }
    } // uninstall

    public String[] getActiveServers(){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("getActiveServers",true);
            $in=_invoke($out);
            String $result[]=ServerIdsHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return getActiveServers();
        }finally{
            _releaseReply($in);
        }
    } // getActiveServers

    public String[] getORBNames(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("getORBNames",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            $in=_invoke($out);
            String $result[]=ORBidListHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return getORBNames(serverId);
        }finally{
            _releaseReply($in);
        }
    } // getORBNames

    public org.omg.PortableInterceptor.ObjectReferenceTemplate lookupPOATemplate(String serverId,String orbId,String[] orbAdapterName){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("lookupPOATemplate",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            org.omg.PortableInterceptor.ORBIdHelper.write($out,orbId);
            org.omg.PortableInterceptor.AdapterNameHelper.write($out,orbAdapterName);
            $in=_invoke($out);
            org.omg.PortableInterceptor.ObjectReferenceTemplate $result=org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return lookupPOATemplate(serverId,orbId,orbAdapterName);
        }finally{
            _releaseReply($in);
        }
    } // lookupPOATemplate

    public com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerType locateServer(String serverId,String endPoint) throws NoSuchEndPoint, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("locateServer",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            $out.write_string(endPoint);
            $in=_invoke($out);
            com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerType $result=com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerTypeHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/NoSuchEndPoint:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.NoSuchEndPointHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerHeldDown:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerHeldDownHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return locateServer(serverId,endPoint);
        }finally{
            _releaseReply($in);
        }
    } // locateServer

    public com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORB locateServerForORB(String serverId,String orbId) throws InvalidORBid, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("locateServerForORB",true);
            org.omg.PortableInterceptor.ServerIdHelper.write($out,serverId);
            org.omg.PortableInterceptor.ORBIdHelper.write($out,orbId);
            $in=_invoke($out);
            com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORB $result=com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORBHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/InvalidORBid:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.InvalidORBidHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerNotRegistered:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerNotRegisteredHelper.read($in);
            else if(_id.equals("IDL:PortableActivationIDL/ServerHeldDown:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.ServerHeldDownHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return locateServerForORB(serverId,orbId);
        }finally{
            _releaseReply($in);
        }
    } // locateServerForORB

    public int getEndpoint(String endPointType) throws NoSuchEndPoint{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("getEndpoint",true);
            $out.write_string(endPointType);
            $in=_invoke($out);
            int $result=TCPPortHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/NoSuchEndPoint:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.NoSuchEndPointHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return getEndpoint(endPointType);
        }finally{
            _releaseReply($in);
        }
    } // getEndpoint

    public int getServerPortForType(com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORB location,String endPointType) throws NoSuchEndPoint{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("getServerPortForType",true);
            com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORBHelper.write($out,location);
            $out.write_string(endPointType);
            $in=_invoke($out);
            int $result=TCPPortHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:PortableActivationIDL/NoSuchEndPoint:1.0"))
                throw com.sun.corba.se.PortableActivationIDL.NoSuchEndPointHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return getServerPortForType(location,endPointType);
        }finally{
            _releaseReply($in);
        }
    } // getServerPortForType

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
} // class _ServerManagerStub
