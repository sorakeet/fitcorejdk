/**
 * Copyright (c) 2002, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote.rmi;

import com.sun.jmx.remote.internal.RMIExporter;
import com.sun.jmx.remote.util.EnvHelp;
import sun.reflect.misc.ReflectUtil;
import sun.rmi.server.DeserializationChecker;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;

import javax.security.auth.Subject;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RMIJRMPServerImpl extends RMIServerImpl{
    private final ExportedWrapper exportedWrapper;
    private final int port;
    private final RMIClientSocketFactory csf;
    private final RMIServerSocketFactory ssf;
    private final Map<String,?> env;

    public RMIJRMPServerImpl(int port,
                             RMIClientSocketFactory csf,
                             RMIServerSocketFactory ssf,
                             Map<String,?> env)
            throws IOException{
        super(env);
        if(port<0)
            throw new IllegalArgumentException("Negative port: "+port);
        this.port=port;
        this.csf=csf;
        this.ssf=ssf;
        this.env=(env==null)?Collections.<String,Object>emptyMap():env;
        String[] credentialsTypes
                =(String[])this.env.get(EnvHelp.CREDENTIAL_TYPES);
        List<String> types=null;
        if(credentialsTypes!=null){
            types=new ArrayList<>();
            for(String type : credentialsTypes){
                if(type==null){
                    throw new IllegalArgumentException("A credential type is null.");
                }
                ReflectUtil.checkPackageAccess(type);
                types.add(type);
            }
        }
        exportedWrapper=types!=null?
                new ExportedWrapper(this,types):
                null;
    }

    protected void export() throws IOException{
        if(exportedWrapper!=null){
            export(exportedWrapper);
        }else{
            export(this);
        }
    }

    private void export(Remote obj) throws RemoteException{
        final RMIExporter exporter=
                (RMIExporter)env.get(RMIExporter.EXPORTER_ATTRIBUTE);
        final boolean daemon=EnvHelp.isServerDaemon(env);
        if(daemon&&exporter!=null){
            throw new IllegalArgumentException("If "+EnvHelp.JMX_SERVER_DAEMON+
                    " is specified as true, "+RMIExporter.EXPORTER_ATTRIBUTE+
                    " cannot be used to specify an exporter!");
        }
        if(daemon){
            if(csf==null&&ssf==null){
                new UnicastServerRef(port).exportObject(obj,null,true);
            }else{
                new UnicastServerRef2(port,csf,ssf).exportObject(obj,null,true);
            }
        }else if(exporter!=null){
            exporter.exportObject(obj,port,csf,ssf);
        }else{
            UnicastRemoteObject.exportObject(obj,port,csf,ssf);
        }
    }

    public Remote toStub() throws IOException{
        if(exportedWrapper!=null){
            return RemoteObject.toStub(exportedWrapper);
        }else{
            return RemoteObject.toStub(this);
        }
    }

    protected RMIConnection makeClient(String connectionId,Subject subject)
            throws IOException{
        if(connectionId==null)
            throw new NullPointerException("Null connectionId");
        RMIConnection client=
                new RMIConnectionImpl(this,connectionId,getDefaultClassLoader(),
                        subject,env);
        export(client);
        return client;
    }

    protected void closeClient(RMIConnection client) throws IOException{
        unexport(client,true);
    }

    private void unexport(Remote obj,boolean force)
            throws NoSuchObjectException{
        RMIExporter exporter=
                (RMIExporter)env.get(RMIExporter.EXPORTER_ATTRIBUTE);
        if(exporter==null)
            UnicastRemoteObject.unexportObject(obj,force);
        else
            exporter.unexportObject(obj,force);
    }

    protected String getProtocol(){
        return "rmi";
    }

    protected void closeServer() throws IOException{
        if(exportedWrapper!=null){
            unexport(exportedWrapper,true);
        }else{
            unexport(this,true);
        }
    }

    private static class ExportedWrapper implements RMIServer, DeserializationChecker{
        private final RMIServer impl;
        private final List<String> allowedTypes;

        private ExportedWrapper(RMIServer impl,List<String> credentialsTypes){
            this.impl=impl;
            allowedTypes=credentialsTypes;
        }

        @Override
        public String getVersion() throws RemoteException{
            return impl.getVersion();
        }

        @Override
        public RMIConnection newClient(Object credentials) throws IOException{
            return impl.newClient(credentials);
        }

        @Override
        public void check(Method method,ObjectStreamClass descriptor,
                          int paramIndex,int callID){
            String type=descriptor.getName();
            if(!allowedTypes.contains(type)){
                throw new ClassCastException("Unsupported type: "+type);
            }
        }

        @Override
        public void checkProxyClass(Method method,String[] ifaces,
                                    int paramIndex,int callID){
            if(ifaces!=null&&ifaces.length>0){
                for(String iface : ifaces){
                    if(!allowedTypes.contains(iface)){
                        throw new ClassCastException("Unsupported type: "+iface);
                    }
                }
            }
        }
    }
}
