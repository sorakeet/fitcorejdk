/**
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote.rmi;

import com.sun.jmx.remote.internal.IIOPHelper;

import javax.security.auth.Subject;
import java.io.IOException;
import java.rmi.Remote;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Map;

public class RMIIIOPServerImpl extends RMIServerImpl{
    private final Map<String,?> env;
    private final AccessControlContext callerACC;

    public RMIIIOPServerImpl(Map<String,?> env)
            throws IOException{
        super(env);
        this.env=(env==null)?Collections.<String,Object>emptyMap():env;
        callerACC=AccessController.getContext();
    }

    protected void export() throws IOException{
        IIOPHelper.exportObject(this);
    }

    public Remote toStub() throws IOException{
        // javax.rmi.CORBA.Stub stub =
        //    (javax.rmi.CORBA.Stub) PortableRemoteObject.toStub(this);
        final Remote stub=IIOPHelper.toStub(this);
        // java.lang.System.out.println("NON CONNECTED STUB " + stub);
        // org.omg.CORBA.ORB orb =
        //    org.omg.CORBA.ORB.init((String[])null, (Properties)null);
        // stub.connect(orb);
        // java.lang.System.out.println("CONNECTED STUB " + stub);
        return stub;
    }

    @Override
    RMIConnection doNewClient(final Object credentials) throws IOException{
        if(callerACC==null){
            throw new SecurityException("AccessControlContext cannot be null");
        }
        try{
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<RMIConnection>(){
                        public RMIConnection run() throws IOException{
                            return superDoNewClient(credentials);
                        }
                    },callerACC);
        }catch(PrivilegedActionException pae){
            throw (IOException)pae.getCause();
        }
    }

    protected RMIConnection makeClient(String connectionId,Subject subject)
            throws IOException{
        if(connectionId==null)
            throw new NullPointerException("Null connectionId");
        RMIConnection client=
                new RMIConnectionImpl(this,connectionId,getDefaultClassLoader(),
                        subject,env);
        IIOPHelper.exportObject(client);
        return client;
    }

    protected void closeClient(RMIConnection client) throws IOException{
        IIOPHelper.unexportObject(client);
    }

    protected String getProtocol(){
        return "iiop";
    }

    protected void closeServer() throws IOException{
        IIOPHelper.unexportObject(this);
    }

    RMIConnection superDoNewClient(Object credentials) throws IOException{
        return super.doNewClient(credentials);
    }
}
