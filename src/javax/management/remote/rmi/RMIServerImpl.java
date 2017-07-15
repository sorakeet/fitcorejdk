/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote.rmi;

import com.sun.jmx.remote.internal.ArrayNotificationBuffer;
import com.sun.jmx.remote.internal.NotificationBuffer;
import com.sun.jmx.remote.security.JMXPluggableAuthenticator;
import com.sun.jmx.remote.util.ClassLogger;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.security.auth.Subject;
import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.Remote;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.security.Principal;
import java.util.*;

public abstract class RMIServerImpl implements Closeable, RMIServer{
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.rmi","RMIServerImpl");
    private static int connectionIdNumber;
    private final List<WeakReference<RMIConnection>> clientList=
            new ArrayList<WeakReference<RMIConnection>>();
    private final Map<String,?> env;
    private ClassLoader cl;
    private MBeanServer mbeanServer;
    private RMIConnectorServer connServer;
    private NotificationBuffer notifBuffer;

    public RMIServerImpl(Map<String,?> env){
        this.env=(env==null)?Collections.<String,Object>emptyMap():env;
    }

    void setRMIConnectorServer(RMIConnectorServer connServer)
            throws IOException{
        this.connServer=connServer;
    }

    protected abstract void export() throws IOException;

    public abstract Remote toStub() throws IOException;

    public synchronized ClassLoader getDefaultClassLoader(){
        return cl;
    }

    public synchronized void setDefaultClassLoader(ClassLoader cl){
        this.cl=cl;
    }

    public String getVersion(){
        // Expected format is: "protocol-version implementation-name"
        try{
            return "1.0 java_runtime_"+
                    System.getProperty("java.runtime.version");
        }catch(SecurityException e){
            return "1.0 ";
        }
    }

    public RMIConnection newClient(Object credentials) throws IOException{
        return doNewClient(credentials);
    }

    RMIConnection doNewClient(Object credentials) throws IOException{
        final boolean tracing=logger.traceOn();
        if(tracing) logger.trace("newClient","making new client");
        if(getMBeanServer()==null)
            throw new IllegalStateException("Not attached to an MBean server");
        Subject subject=null;
        JMXAuthenticator authenticator=
                (JMXAuthenticator)env.get(JMXConnectorServer.AUTHENTICATOR);
        if(authenticator==null){
            /**
             * Create the JAAS-based authenticator only if authentication
             * has been enabled
             */
            if(env.get("jmx.remote.x.password.file")!=null||
                    env.get("jmx.remote.x.login.config")!=null){
                authenticator=new JMXPluggableAuthenticator(env);
            }
        }
        if(authenticator!=null){
            if(tracing) logger.trace("newClient","got authenticator: "+
                    authenticator.getClass().getName());
            try{
                subject=authenticator.authenticate(credentials);
            }catch(SecurityException e){
                logger.trace("newClient","Authentication failed: "+e);
                throw e;
            }
        }
        if(tracing){
            if(subject!=null)
                logger.trace("newClient","subject is not null");
            else logger.trace("newClient","no subject");
        }
        final String connectionId=makeConnectionId(getProtocol(),subject);
        if(tracing)
            logger.trace("newClient","making new connection: "+connectionId);
        RMIConnection client=makeClient(connectionId,subject);
        dropDeadReferences();
        WeakReference<RMIConnection> wr=new WeakReference<RMIConnection>(client);
        synchronized(clientList){
            clientList.add(wr);
        }
        connServer.connectionOpened(connectionId,"Connection opened",null);
        synchronized(clientList){
            if(!clientList.contains(wr)){
                // can be removed only by a JMXConnectionNotification listener
                throw new IOException("The connection is refused.");
            }
        }
        if(tracing)
            logger.trace("newClient","new connection done: "+connectionId);
        return client;
    }

    public synchronized MBeanServer getMBeanServer(){
        return mbeanServer;
    }

    public synchronized void setMBeanServer(MBeanServer mbs){
        this.mbeanServer=mbs;
    }

    protected abstract RMIConnection makeClient(String connectionId,
                                                Subject subject)
            throws IOException;

    protected abstract String getProtocol();

    private static synchronized String makeConnectionId(String protocol,
                                                        Subject subject){
        connectionIdNumber++;
        String clientHost="";
        try{
            clientHost=RemoteServer.getClientHost();
            /**
             * According to the rules specified in the javax.management.remote
             * package description, a numeric IPv6 address (detected by the
             * presence of otherwise forbidden ":" character) forming a part
             * of the connection id must be enclosed in square brackets.
             */
            if(clientHost.contains(":")){
                clientHost="["+clientHost+"]";
            }
        }catch(ServerNotActiveException e){
            logger.trace("makeConnectionId","getClientHost",e);
        }
        final StringBuilder buf=new StringBuilder();
        buf.append(protocol).append(":");
        if(clientHost.length()>0)
            buf.append("//").append(clientHost);
        buf.append(" ");
        if(subject!=null){
            Set<Principal> principals=subject.getPrincipals();
            String sep="";
            for(Iterator<Principal> it=principals.iterator();it.hasNext();){
                Principal p=it.next();
                String name=p.getName().replace(' ','_').replace(';',':');
                buf.append(sep).append(name);
                sep=";";
            }
        }
        buf.append(" ").append(connectionIdNumber);
        if(logger.traceOn())
            logger.trace("newConnectionId","connectionId="+buf);
        return buf.toString();
    }

    private void dropDeadReferences(){
        synchronized(clientList){
            for(Iterator<WeakReference<RMIConnection>> it=clientList.iterator();
                it.hasNext();){
                WeakReference<RMIConnection> wr=it.next();
                if(wr.get()==null)
                    it.remove();
            }
        }
    }

    protected void clientClosed(RMIConnection client) throws IOException{
        final boolean debug=logger.debugOn();
        if(debug) logger.trace("clientClosed","client="+client);
        if(client==null)
            throw new NullPointerException("Null client");
        synchronized(clientList){
            dropDeadReferences();
            for(Iterator<WeakReference<RMIConnection>> it=clientList.iterator();
                it.hasNext();){
                WeakReference<RMIConnection> wr=it.next();
                if(wr.get()==client){
                    it.remove();
                    break;
                }
            }
            /** It is not a bug for this loop not to find the client.  In
             our close() method, we remove a client from the list before
             calling its close() method.  */
        }
        if(debug) logger.trace("clientClosed","closing client.");
        closeClient(client);
        if(debug) logger.trace("clientClosed","sending notif");
        connServer.connectionClosed(client.getConnectionId(),
                "Client connection closed",null);
        if(debug) logger.trace("clientClosed","done");
    }

    protected abstract void closeClient(RMIConnection client)
            throws IOException;

    public synchronized void close() throws IOException{
        final boolean tracing=logger.traceOn();
        final boolean debug=logger.debugOn();
        if(tracing) logger.trace("close","closing");
        IOException ioException=null;
        try{
            if(debug) logger.debug("close","closing Server");
            closeServer();
        }catch(IOException e){
            if(tracing) logger.trace("close","Failed to close server: "+e);
            if(debug) logger.debug("close",e);
            ioException=e;
        }
        if(debug) logger.debug("close","closing Clients");
        // Loop to close all clients
        while(true){
            synchronized(clientList){
                if(debug) logger.debug("close","droping dead references");
                dropDeadReferences();
                if(debug) logger.debug("close","client count: "+clientList.size());
                if(clientList.size()==0)
                    break;
                /** Loop until we find a non-null client.  Because we called
                 dropDeadReferences(), this will usually be the first
                 element of the list, but a garbage collection could have
                 happened in between.  */
                for(Iterator<WeakReference<RMIConnection>> it=clientList.iterator();
                    it.hasNext();){
                    WeakReference<RMIConnection> wr=it.next();
                    RMIConnection client=wr.get();
                    it.remove();
                    if(client!=null){
                        try{
                            client.close();
                        }catch(IOException e){
                            if(tracing)
                                logger.trace("close","Failed to close client: "+e);
                            if(debug) logger.debug("close",e);
                            if(ioException==null)
                                ioException=e;
                        }
                        break;
                    }
                }
            }
        }
        if(notifBuffer!=null)
            notifBuffer.dispose();
        if(ioException!=null){
            if(tracing) logger.trace("close","close failed.");
            throw ioException;
        }
        if(tracing) logger.trace("close","closed.");
    }

    protected abstract void closeServer() throws IOException;

    synchronized NotificationBuffer getNotifBuffer(){
        //Notification buffer is lazily created when the first client connects
        if(notifBuffer==null)
            notifBuffer=
                    ArrayNotificationBuffer.getNotificationBuffer(mbeanServer,
                            env);
        return notifBuffer;
    }
}
