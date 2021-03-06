/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote.rmi;

import com.sun.jmx.mbeanserver.Util;
import com.sun.jmx.remote.internal.*;
import com.sun.jmx.remote.util.ClassLogger;
import com.sun.jmx.remote.util.EnvHelp;
import sun.reflect.misc.ReflectUtil;
import sun.rmi.server.UnicastRef2;
import sun.rmi.transport.LiveRef;

import javax.management.*;
import javax.management.remote.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.security.auth.Subject;
import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;

public class RMIConnector implements JMXConnector, Serializable, JMXAddressable{
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.rmi","RMIConnector");
    private static final long serialVersionUID=817323035842634473L;
    private static final String rmiServerImplStubClassName=
            RMIServer.class.getName()+"Impl_Stub";
    private static final Class<?> rmiServerImplStubClass;
    private static final String rmiConnectionImplStubClassName=
            RMIConnection.class.getName()+"Impl_Stub";
    private static final Class<?> rmiConnectionImplStubClass;
    private static final String pRefClassName=
            "com.sun.jmx.remote.internal.PRef";
    //--------------------------------------------------------------------
    // implements JMXConnector interface
    //--------------------------------------------------------------------
    private static final Constructor<?> proxyRefConstructor;
    private static final String iiopConnectionStubClassName=
            "org.omg.stub.javax.management.remote.rmi._RMIConnection_Stub";
    private static final String proxyStubClassName=
            "com.sun.jmx.remote.protocol.iiop.ProxyStub";
    private static final String ProxyInputStreamClassName=
            "com.sun.jmx.remote.protocol.iiop.ProxyInputStream";
    private static final String pInputStreamClassName=
            "com.sun.jmx.remote.protocol.iiop.PInputStream";
    private static final Class<?> proxyStubClass;
    private static final byte base64ToInt[]={
            -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,-1,-1,-1,-1,62,-1,-1,-1,63,52,53,54,
            55,56,57,58,59,60,61,-1,-1,-1,-1,-1,-1,-1,0,1,2,3,4,
            5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,
            24,25,-1,-1,-1,-1,-1,-1,26,27,28,29,30,31,32,33,34,
            35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51
    };
    private static volatile WeakReference<Object> orb=null;

    static{
        final String pRefByteCodeString=
                "\312\376\272\276\0\0\0.\0\27\12\0\5\0\15\11\0\4\0\16\13\0\17\0"+
                        "\20\7\0\21\7\0\22\1\0\6<init>\1\0\36(Ljava/rmi/server/RemoteRef;"+
                        ")V\1\0\4Code\1\0\6invoke\1\0S(Ljava/rmi/Remote;Ljava/lang/reflec"+
                        "t/Method;[Ljava/lang/Object;J)Ljava/lang/Object;\1\0\12Exception"+
                        "s\7\0\23\14\0\6\0\7\14\0\24\0\25\7\0\26\14\0\11\0\12\1\0\40com/"+
                        "sun/jmx/remote/internal/PRef\1\0$com/sun/jmx/remote/internal/Pr"+
                        "oxyRef\1\0\23java/lang/Exception\1\0\3ref\1\0\33Ljava/rmi/serve"+
                        "r/RemoteRef;\1\0\31java/rmi/server/RemoteRef\0!\0\4\0\5\0\0\0\0"+
                        "\0\2\0\1\0\6\0\7\0\1\0\10\0\0\0\22\0\2\0\2\0\0\0\6*+\267\0\1\261"+
                        "\0\0\0\0\0\1\0\11\0\12\0\2\0\10\0\0\0\33\0\6\0\6\0\0\0\17*\264\0"+
                        "\2+,-\26\4\271\0\3\6\0\260\0\0\0\0\0\13\0\0\0\4\0\1\0\14\0\0";
        final byte[] pRefByteCode=
                NoCallStackClassLoader.stringToBytes(pRefByteCodeString);
        PrivilegedExceptionAction<Constructor<?>> action=
                new PrivilegedExceptionAction<Constructor<?>>(){
                    public Constructor<?> run() throws Exception{
                        Class thisClass=RMIConnector.class;
                        ClassLoader thisLoader=thisClass.getClassLoader();
                        ProtectionDomain thisProtectionDomain=
                                thisClass.getProtectionDomain();
                        String[] otherClassNames={ProxyRef.class.getName()};
                        ClassLoader cl=
                                new NoCallStackClassLoader(pRefClassName,
                                        pRefByteCode,
                                        otherClassNames,
                                        thisLoader,
                                        thisProtectionDomain);
                        Class<?> c=cl.loadClass(pRefClassName);
                        return c.getConstructor(RemoteRef.class);
                    }
                };
        Class<?> serverStubClass;
        try{
            serverStubClass=Class.forName(rmiServerImplStubClassName);
        }catch(Exception e){
            logger.error("<clinit>",
                    "Failed to instantiate "+
                            rmiServerImplStubClassName+": "+e);
            logger.debug("<clinit>",e);
            serverStubClass=null;
        }
        rmiServerImplStubClass=serverStubClass;
        Class<?> stubClass;
        Constructor<?> constr;
        try{
            stubClass=Class.forName(rmiConnectionImplStubClassName);
            constr=(Constructor<?>)AccessController.doPrivileged(action);
        }catch(Exception e){
            logger.error("<clinit>",
                    "Failed to initialize proxy reference constructor "+
                            "for "+rmiConnectionImplStubClassName+": "+e);
            logger.debug("<clinit>",e);
            stubClass=null;
            constr=null;
        }
        rmiConnectionImplStubClass=stubClass;
        proxyRefConstructor=constr;
    }

    static{
        final String proxyStubByteCodeString=
                "\312\376\272\276\0\0\0\63\0+\12\0\14\0\30\7\0\31\12\0\14\0\32\12"+
                        "\0\2\0\33\7\0\34\12\0\5\0\35\12\0\5\0\36\12\0\5\0\37\12\0\2\0 "+
                        "\12\0\14\0!\7\0\"\7\0#\1\0\6<init>\1\0\3()V\1\0\4Code\1\0\7_in"+
                        "voke\1\0K(Lorg/omg/CORBA/portable/OutputStream;)Lorg/omg/CORBA"+
                        "/portable/InputStream;\1\0\15StackMapTable\7\0\34\1\0\12Except"+
                        "ions\7\0$\1\0\15_releaseReply\1\0'(Lorg/omg/CORBA/portable/Inp"+
                        "utStream;)V\14\0\15\0\16\1\0-com/sun/jmx/remote/protocol/iiop/"+
                        "PInputStream\14\0\20\0\21\14\0\15\0\27\1\0+org/omg/CORBA/porta"+
                        "ble/ApplicationException\14\0%\0&\14\0'\0(\14\0\15\0)\14\0*\0&"+
                        "\14\0\26\0\27\1\0*com/sun/jmx/remote/protocol/iiop/ProxyStub\1"+
                        "\0<org/omg/stub/javax/management/remote/rmi/_RMIConnection_Stu"+
                        "b\1\0)org/omg/CORBA/portable/RemarshalException\1\0\16getInput"+
                        "Stream\1\0&()Lorg/omg/CORBA/portable/InputStream;\1\0\5getId\1"+
                        "\0\24()Ljava/lang/String;\1\09(Ljava/lang/String;Lorg/omg/CORB"+
                        "A/portable/InputStream;)V\1\0\25getProxiedInputStream\0!\0\13\0"+
                        "\14\0\0\0\0\0\3\0\1\0\15\0\16\0\1\0\17\0\0\0\21\0\1\0\1\0\0\0\5"+
                        "*\267\0\1\261\0\0\0\0\0\1\0\20\0\21\0\2\0\17\0\0\0G\0\4\0\4\0\0"+
                        "\0'\273\0\2Y*+\267\0\3\267\0\4\260M\273\0\2Y,\266\0\6\267\0\4N"+
                        "\273\0\5Y,\266\0\7-\267\0\10\277\0\1\0\0\0\14\0\15\0\5\0\1\0\22"+
                        "\0\0\0\6\0\1M\7\0\23\0\24\0\0\0\6\0\2\0\5\0\25\0\1\0\26\0\27\0"+
                        "\1\0\17\0\0\0'\0\2\0\2\0\0\0\22+\306\0\13+\300\0\2\266\0\11L*+"+
                        "\267\0\12\261\0\0\0\1\0\22\0\0\0\3\0\1\14\0\0";
        final String pInputStreamByteCodeString=
                "\312\376\272\276\0\0\0\63\0\36\12\0\7\0\17\11\0\6\0\20\12\0\21"+
                        "\0\22\12\0\6\0\23\12\0\24\0\25\7\0\26\7\0\27\1\0\6<init>\1\0'("+
                        "Lorg/omg/CORBA/portable/InputStream;)V\1\0\4Code\1\0\10read_an"+
                        "y\1\0\25()Lorg/omg/CORBA/Any;\1\0\12read_value\1\0)(Ljava/lang"+
                        "/Class;)Ljava/io/Serializable;\14\0\10\0\11\14\0\30\0\31\7\0\32"+
                        "\14\0\13\0\14\14\0\33\0\34\7\0\35\14\0\15\0\16\1\0-com/sun/jmx"+
                        "/remote/protocol/iiop/PInputStream\1\0\61com/sun/jmx/remote/pr"+
                        "otocol/iiop/ProxyInputStream\1\0\2in\1\0$Lorg/omg/CORBA/portab"+
                        "le/InputStream;\1\0\"org/omg/CORBA/portable/InputStream\1\0\6n"+
                        "arrow\1\0*()Lorg/omg/CORBA_2_3/portable/InputStream;\1\0&org/o"+
                        "mg/CORBA_2_3/portable/InputStream\0!\0\6\0\7\0\0\0\0\0\3\0\1\0"+
                        "\10\0\11\0\1\0\12\0\0\0\22\0\2\0\2\0\0\0\6*+\267\0\1\261\0\0\0"+
                        "\0\0\1\0\13\0\14\0\1\0\12\0\0\0\24\0\1\0\1\0\0\0\10*\264\0\2\266"+
                        "\0\3\260\0\0\0\0\0\1\0\15\0\16\0\1\0\12\0\0\0\25\0\2\0\2\0\0\0"+
                        "\11*\266\0\4+\266\0\5\260\0\0\0\0\0\0";
        final byte[] proxyStubByteCode=
                NoCallStackClassLoader.stringToBytes(proxyStubByteCodeString);
        final byte[] pInputStreamByteCode=
                NoCallStackClassLoader.stringToBytes(pInputStreamByteCodeString);
        final String[] classNames={proxyStubClassName,pInputStreamClassName};
        final byte[][] byteCodes={proxyStubByteCode,pInputStreamByteCode};
        final String[] otherClassNames={
                iiopConnectionStubClassName,
                ProxyInputStreamClassName,
        };
        if(IIOPHelper.isAvailable()){
            PrivilegedExceptionAction<Class<?>> action=
                    new PrivilegedExceptionAction<Class<?>>(){
                        public Class<?> run() throws Exception{
                            Class thisClass=RMIConnector.class;
                            ClassLoader thisLoader=thisClass.getClassLoader();
                            ProtectionDomain thisProtectionDomain=
                                    thisClass.getProtectionDomain();
                            ClassLoader cl=
                                    new NoCallStackClassLoader(classNames,
                                            byteCodes,
                                            otherClassNames,
                                            thisLoader,
                                            thisProtectionDomain);
                            return cl.loadClass(proxyStubClassName);
                        }
                    };
            Class<?> stubClass;
            try{
                stubClass=AccessController.doPrivileged(action);
            }catch(Exception e){
                logger.error("<clinit>",
                        "Unexpected exception making shadow IIOP stub class: "+e);
                logger.debug("<clinit>",e);
                stubClass=null;
            }
            proxyStubClass=stubClass;
        }else{
            proxyStubClass=null;
        }
    }

    //--------------------------------------------------------------------
    // Private variables
    //--------------------------------------------------------------------
    private final RMIServer rmiServer;
    private final JMXServiceURL jmxServiceURL;
    // ---------------------------------------------------------
    // WARNING - WARNING - WARNING - WARNING - WARNING - WARNING
    // ---------------------------------------------------------
    // Any transient variable which needs to be initialized should
    // be initialized in the method initTransient()
    private transient Map<String,Object> env;
    private transient ClassLoader defaultClassLoader;
    private transient RMIConnection connection;
    private transient String connectionId;
    private transient long clientNotifSeqNo=0;
    private transient WeakHashMap<Subject,WeakReference<MBeanServerConnection>> rmbscMap;
    private transient WeakReference<MBeanServerConnection> nullSubjectConnRef=null;
    private transient RMINotifClient rmiNotifClient;
    // = new RMINotifClient(new Integer(0));
    private transient long clientNotifCounter=0;
    //--------------------------------------------------------------------
    // Private stuff - Check if stub can be trusted.
    //--------------------------------------------------------------------
    private transient boolean connected;
    //--------------------------------------------------------------------
    // Private stuff - RMIServer creation
    //--------------------------------------------------------------------
    // = false;
    private transient boolean terminated;
    // = false;
    private transient Exception closeException;
    private transient NotificationBroadcasterSupport connectionBroadcaster;
    private transient ClientCommunicatorAdmin communicatorAdmin;

    public RMIConnector(JMXServiceURL url,Map<String,?> environment){
        this(null,url,environment);
    }

    private RMIConnector(RMIServer rmiServer,JMXServiceURL address,
                         Map<String,?> environment){
        if(rmiServer==null&&address==null) throw new
                IllegalArgumentException("rmiServer and jmxServiceURL both null");
        initTransients();
        this.rmiServer=rmiServer;
        this.jmxServiceURL=address;
        if(environment==null){
            this.env=Collections.emptyMap();
        }else{
            EnvHelp.checkAttributes(environment);
            this.env=Collections.unmodifiableMap(environment);
        }
    }

    // Initialization of transient variables.
    private void initTransients(){
        rmbscMap=new WeakHashMap<Subject,WeakReference<MBeanServerConnection>>();
        connected=false;
        terminated=false;
        connectionBroadcaster=new NotificationBroadcasterSupport();
    }

    public RMIConnector(RMIServer rmiServer,Map<String,?> environment){
        this(rmiServer,null,environment);
    }

    private static String strings(final String[] strs){
        return objects(strs);
    }

    // TRACES & DEBUG
    //---------------
    private static String objects(final Object[] objs){
        if(objs==null)
            return "null";
        else
            return Arrays.asList(objs).toString();
    }

    static String getAttributesNames(AttributeList attributes){
        return attributes!=null?
                attributes.asList().stream()
                        .map(Attribute::getName)
                        .collect(Collectors.joining(", ","[","]"))
                :"[]";
    }

    public JMXServiceURL getAddress(){
        return jmxServiceURL;
    }

    public void connect() throws IOException{
        connect(null);
    }

    public synchronized void connect(Map<String,?> environment)
            throws IOException{
        final boolean tracing=logger.traceOn();
        String idstr=(tracing?"["+this.toString()+"]":null);
        if(terminated){
            logger.trace("connect",idstr+" already closed.");
            throw new IOException("Connector closed");
        }
        if(connected){
            logger.trace("connect",idstr+" already connected.");
            return;
        }
        try{
            if(tracing) logger.trace("connect",idstr+" connecting...");
            final Map<String,Object> usemap=
                    new HashMap<String,Object>((this.env==null)?
                            Collections.<String,Object>emptyMap():this.env);
            if(environment!=null){
                EnvHelp.checkAttributes(environment);
                usemap.putAll(environment);
            }
            // Get RMIServer stub from directory or URL encoding if needed.
            if(tracing) logger.trace("connect",idstr+" finding stub...");
            RMIServer stub=(rmiServer!=null)?rmiServer:
                    findRMIServer(jmxServiceURL,usemap);
            // Check for secure RMIServer stub if the corresponding
            // client-side environment property is set to "true".
            //
            String stringBoolean=(String)usemap.get("jmx.remote.x.check.stub");
            boolean checkStub=EnvHelp.computeBooleanFromString(stringBoolean);
            if(checkStub) checkStub(stub,rmiServerImplStubClass);
            // Connect IIOP Stub if needed.
            if(tracing) logger.trace("connect",idstr+" connecting stub...");
            stub=connectStub(stub,usemap);
            idstr=(tracing?"["+this.toString()+"]":null);
            // Calling newClient on the RMIServer stub.
            if(tracing)
                logger.trace("connect",idstr+" getting connection...");
            Object credentials=usemap.get(CREDENTIALS);
            try{
                connection=getConnection(stub,credentials,checkStub);
            }catch(java.rmi.RemoteException re){
                if(jmxServiceURL!=null){
                    final String pro=jmxServiceURL.getProtocol();
                    final String path=jmxServiceURL.getURLPath();
                    if("rmi".equals(pro)&&
                            path.startsWith("/jndi/iiop:")){
                        MalformedURLException mfe=new MalformedURLException(
                                "Protocol is rmi but JNDI scheme is iiop: "+jmxServiceURL);
                        mfe.initCause(re);
                        throw mfe;
                    }
                }
                throw re;
            }
            // Always use one of:
            //   ClassLoader provided in Map at connect time,
            //   or contextClassLoader at connect time.
            if(tracing)
                logger.trace("connect",idstr+" getting class loader...");
            defaultClassLoader=EnvHelp.resolveClientClassLoader(usemap);
            usemap.put(JMXConnectorFactory.DEFAULT_CLASS_LOADER,
                    defaultClassLoader);
            rmiNotifClient=new RMINotifClient(defaultClassLoader,usemap);
            env=usemap;
            final long checkPeriod=EnvHelp.getConnectionCheckPeriod(usemap);
            communicatorAdmin=new RMIClientCommunicatorAdmin(checkPeriod);
            connected=true;
            // The connectionId variable is used in doStart(), when
            // reconnecting, to identify the "old" connection.
            //
            connectionId=getConnectionId();
            Notification connectedNotif=
                    new JMXConnectionNotification(JMXConnectionNotification.OPENED,
                            this,
                            connectionId,
                            clientNotifSeqNo++,
                            "Successful connection",
                            null);
            sendNotification(connectedNotif);
            if(tracing) logger.trace("connect",idstr+" done...");
        }catch(IOException e){
            if(tracing)
                logger.trace("connect",idstr+" failed to connect: "+e);
            throw e;
        }catch(RuntimeException e){
            if(tracing)
                logger.trace("connect",idstr+" failed to connect: "+e);
            throw e;
        }catch(NamingException e){
            final String msg="Failed to retrieve RMIServer stub: "+e;
            if(tracing) logger.trace("connect",idstr+" "+msg);
            throw EnvHelp.initCause(new IOException(msg),e);
        }
    }

    @Override
    public String toString(){
        final StringBuilder b=new StringBuilder(this.getClass().getName());
        b.append(":");
        if(rmiServer!=null){
            b.append(" rmiServer=").append(rmiServer.toString());
        }
        if(jmxServiceURL!=null){
            if(rmiServer!=null) b.append(",");
            b.append(" jmxServiceURL=").append(jmxServiceURL.toString());
        }
        return b.toString();
    }

    public synchronized MBeanServerConnection getMBeanServerConnection()
            throws IOException{
        return getMBeanServerConnection(null);
    }

    public synchronized MBeanServerConnection
    getMBeanServerConnection(Subject delegationSubject)
            throws IOException{
        if(terminated){
            if(logger.traceOn())
                logger.trace("getMBeanServerConnection","["+this.toString()+
                        "] already closed.");
            throw new IOException("Connection closed");
        }else if(!connected){
            if(logger.traceOn())
                logger.trace("getMBeanServerConnection","["+this.toString()+
                        "] is not connected.");
            throw new IOException("Not connected");
        }
        return getConnectionWithSubject(delegationSubject);
    }

    public synchronized void close() throws IOException{
        close(false);
    }

    public void
    addConnectionNotificationListener(NotificationListener listener,
                                      NotificationFilter filter,
                                      Object handback){
        if(listener==null)
            throw new NullPointerException("listener");
        connectionBroadcaster.addNotificationListener(listener,filter,
                handback);
    }

    public void
    removeConnectionNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException{
        if(listener==null)
            throw new NullPointerException("listener");
        connectionBroadcaster.removeNotificationListener(listener);
    }

    public void
    removeConnectionNotificationListener(NotificationListener listener,
                                         NotificationFilter filter,
                                         Object handback)
            throws ListenerNotFoundException{
        if(listener==null)
            throw new NullPointerException("listener");
        connectionBroadcaster.removeNotificationListener(listener,filter,
                handback);
    }

    public synchronized String getConnectionId() throws IOException{
        if(terminated||!connected){
            if(logger.traceOn())
                logger.trace("getConnectionId","["+this.toString()+
                        "] not connected.");
            throw new IOException("Not connected");
        }
        // we do a remote call to have an IOException if the connection is broken.
        // see the bug 4939578
        return connection.getConnectionId();
    }

    // allows to do close after setting the flag "terminated" to true.
    // It is necessary to avoid a deadlock, see 6296324
    private synchronized void close(boolean intern) throws IOException{
        final boolean tracing=logger.traceOn();
        final boolean debug=logger.debugOn();
        final String idstr=(tracing?"["+this.toString()+"]":null);
        if(!intern){
            // Return if already cleanly closed.
            //
            if(terminated){
                if(closeException==null){
                    if(tracing) logger.trace("close",idstr+" already closed.");
                    return;
                }
            }else{
                terminated=true;
            }
        }
        if(closeException!=null&&tracing){
            // Already closed, but not cleanly. Attempt again.
            //
            if(tracing){
                logger.trace("close",idstr+" had failed: "+closeException);
                logger.trace("close",idstr+" attempting to close again.");
            }
        }
        String savedConnectionId=null;
        if(connected){
            savedConnectionId=connectionId;
        }
        closeException=null;
        if(tracing) logger.trace("close",idstr+" closing.");
        if(communicatorAdmin!=null){
            communicatorAdmin.terminate();
        }
        if(rmiNotifClient!=null){
            try{
                rmiNotifClient.terminate();
                if(tracing) logger.trace("close",idstr+
                        " RMI Notification client terminated.");
            }catch(RuntimeException x){
                closeException=x;
                if(tracing) logger.trace("close",idstr+
                        " Failed to terminate RMI Notification client: "+x);
                if(debug) logger.debug("close",x);
            }
        }
        if(connection!=null){
            try{
                connection.close();
                if(tracing) logger.trace("close",idstr+" closed.");
            }catch(NoSuchObjectException nse){
                // OK, the server maybe closed itself.
            }catch(IOException e){
                closeException=e;
                if(tracing) logger.trace("close",idstr+
                        " Failed to close RMIServer: "+e);
                if(debug) logger.debug("close",e);
            }
        }
        // Clean up MBeanServerConnection table
        //
        rmbscMap.clear();
        /** Send notification of closure.  We don't do this if the user
         * never called connect() on the connector, because there's no
         * connection id in that case.  */
        if(savedConnectionId!=null){
            Notification closedNotif=
                    new JMXConnectionNotification(JMXConnectionNotification.CLOSED,
                            this,
                            savedConnectionId,
                            clientNotifSeqNo++,
                            "Client has been closed",
                            null);
            sendNotification(closedNotif);
        }
        // throw exception if needed
        //
        if(closeException!=null){
            if(tracing) logger.trace("close",idstr+" failed to close: "+
                    closeException);
            if(closeException instanceof IOException)
                throw (IOException)closeException;
            if(closeException instanceof RuntimeException)
                throw (RuntimeException)closeException;
            final IOException x=
                    new IOException("Failed to close: "+closeException);
            throw EnvHelp.initCause(x,closeException);
        }
    }

    private MBeanServerConnection getConnectionWithSubject(Subject delegationSubject){
        MBeanServerConnection conn=null;
        if(delegationSubject==null){
            if(nullSubjectConnRef==null
                    ||(conn=nullSubjectConnRef.get())==null){
                conn=new RemoteMBeanServerConnection(null);
                nullSubjectConnRef=new WeakReference(conn);
            }
        }else{
            WeakReference<MBeanServerConnection> wr=rmbscMap.get(delegationSubject);
            if(wr==null||(conn=wr.get())==null){
                conn=new RemoteMBeanServerConnection(delegationSubject);
                rmbscMap.put(delegationSubject,new WeakReference(conn));
            }
        }
        return conn;
    }

    private void sendNotification(Notification n){
        connectionBroadcaster.sendNotification(n);
    }

    //--------------------------------------------------------------------
    // Private stuff - Serialization
    //--------------------------------------------------------------------
    static RMIServer connectStub(RMIServer rmiServer,
                                 Map<String,?> environment)
            throws IOException{
        if(IIOPHelper.isStub(rmiServer)){
            try{
                IIOPHelper.getOrb(rmiServer);
            }catch(UnsupportedOperationException x){
                // BAD_OPERATION
                IIOPHelper.connect(rmiServer,resolveOrb(environment));
            }
        }
        return rmiServer;
    }

    private static void checkStub(Remote stub,
                                  Class<?> stubClass){
        // Check remote stub is from the expected class.
        //
        if(stub.getClass()!=stubClass){
            if(!Proxy.isProxyClass(stub.getClass())){
                throw new SecurityException(
                        "Expecting a "+stubClass.getName()+" stub!");
            }else{
                InvocationHandler handler=Proxy.getInvocationHandler(stub);
                if(handler.getClass()!=RemoteObjectInvocationHandler.class)
                    throw new SecurityException(
                            "Expecting a dynamic proxy instance with a "+
                                    RemoteObjectInvocationHandler.class.getName()+
                                    " invocation handler!");
                else
                    stub=(Remote)handler;
            }
        }
        // Check RemoteRef in stub is from the expected class
        // "sun.rmi.server.UnicastRef2".
        //
        RemoteRef ref=((RemoteObject)stub).getRef();
        if(ref.getClass()!=UnicastRef2.class)
            throw new SecurityException(
                    "Expecting a "+UnicastRef2.class.getName()+
                            " remote reference in stub!");
        // Check RMIClientSocketFactory in stub is from the expected class
        // "javax.rmi.ssl.SslRMIClientSocketFactory".
        //
        LiveRef liveRef=((UnicastRef2)ref).getLiveRef();
        RMIClientSocketFactory csf=liveRef.getClientSocketFactory();
        if(csf==null||csf.getClass()!=SslRMIClientSocketFactory.class)
            throw new SecurityException(
                    "Expecting a "+SslRMIClientSocketFactory.class.getName()+
                            " RMI client socket factory in stub!");
    }

    private RMIServer findRMIServer(JMXServiceURL directoryURL,
                                    Map<String,Object> environment)
            throws NamingException, IOException{
        final boolean isIiop=RMIConnectorServer.isIiopURL(directoryURL,true);
        if(isIiop){
            // Make sure java.naming.corba.orb is in the Map.
            environment.put(EnvHelp.DEFAULT_ORB,resolveOrb(environment));
        }
        String path=directoryURL.getURLPath();
        int end=path.indexOf(';');
        if(end<0) end=path.length();
        if(path.startsWith("/jndi/"))
            return findRMIServerJNDI(path.substring(6,end),environment,isIiop);
        else if(path.startsWith("/stub/"))
            return findRMIServerJRMP(path.substring(6,end),environment,isIiop);
        else if(path.startsWith("/ior/")){
            if(!IIOPHelper.isAvailable())
                throw new IOException("iiop protocol not available");
            return findRMIServerIIOP(path.substring(5,end),environment,isIiop);
        }else{
            final String msg="URL path must begin with /jndi/ or /stub/ "+
                    "or /ior/: "+path;
            throw new MalformedURLException(msg);
        }
    }

    static Object resolveOrb(Map<String,?> environment)
            throws IOException{
        if(environment!=null){
            final Object orb=environment.get(EnvHelp.DEFAULT_ORB);
            if(orb!=null&&!(IIOPHelper.isOrb(orb)))
                throw new IllegalArgumentException(EnvHelp.DEFAULT_ORB+
                        " must be an instance of org.omg.CORBA.ORB.");
            if(orb!=null) return orb;
        }
        final Object orb=
                (RMIConnector.orb==null)?null:RMIConnector.orb.get();
        if(orb!=null) return orb;
        final Object newOrb=
                IIOPHelper.createOrb((String[])null,(Properties)null);
        RMIConnector.orb=new WeakReference<Object>(newOrb);
        return newOrb;
    }

    private RMIServer findRMIServerJNDI(String jndiURL,Map<String,?> env,
                                        boolean isIiop)
            throws NamingException{
        InitialContext ctx=new InitialContext(EnvHelp.mapToHashtable(env));
        Object objref=ctx.lookup(jndiURL);
        ctx.close();
        if(isIiop)
            return narrowIIOPServer(objref);
        else
            return narrowJRMPServer(objref);
    }

    private static RMIServer narrowJRMPServer(Object objref){
        return (RMIServer)objref;
    }

    private static RMIServer narrowIIOPServer(Object objref){
        try{
            return IIOPHelper.narrow(objref,RMIServer.class);
        }catch(ClassCastException e){
            if(logger.traceOn())
                logger.trace("narrowIIOPServer","Failed to narrow objref="+
                        objref+": "+e);
            if(logger.debugOn()) logger.debug("narrowIIOPServer",e);
            return null;
        }
    }

    private RMIServer findRMIServerIIOP(String ior,Map<String,?> env,boolean isIiop){
        // could forbid "rmi:" URL here -- but do we need to?
        final Object orb=env.get(EnvHelp.DEFAULT_ORB);
        final Object stub=IIOPHelper.stringToObject(orb,ior);
        return IIOPHelper.narrow(stub,RMIServer.class);
    }

    private RMIServer findRMIServerJRMP(String base64,Map<String,?> env,boolean isIiop)
            throws IOException{
        // could forbid "iiop:" URL here -- but do we need to?
        final byte[] serialized;
        try{
            serialized=base64ToByteArray(base64);
        }catch(IllegalArgumentException e){
            throw new MalformedURLException("Bad BASE64 encoding: "+
                    e.getMessage());
        }
        final ByteArrayInputStream bin=new ByteArrayInputStream(serialized);
        final ClassLoader loader=EnvHelp.resolveClientClassLoader(env);
        final ObjectInputStream oin=
                (loader==null)?
                        new ObjectInputStream(bin):
                        new ObjectInputStreamWithLoader(bin,loader);
        final Object stub;
        try{
            stub=oin.readObject();
        }catch(ClassNotFoundException e){
            throw new MalformedURLException("Class not found: "+e);
        }
        return (RMIServer)stub;
    }

    private static byte[] base64ToByteArray(String s){
        int sLen=s.length();
        int numGroups=sLen/4;
        if(4*numGroups!=sLen)
            throw new IllegalArgumentException(
                    "String length must be a multiple of four.");
        int missingBytesInLastGroup=0;
        int numFullGroups=numGroups;
        if(sLen!=0){
            if(s.charAt(sLen-1)=='='){
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if(s.charAt(sLen-2)=='=')
                missingBytesInLastGroup++;
        }
        byte[] result=new byte[3*numGroups-missingBytesInLastGroup];
        // Translate all full groups from base64 to byte array elements
        int inCursor=0, outCursor=0;
        for(int i=0;i<numFullGroups;i++){
            int ch0=base64toInt(s.charAt(inCursor++));
            int ch1=base64toInt(s.charAt(inCursor++));
            int ch2=base64toInt(s.charAt(inCursor++));
            int ch3=base64toInt(s.charAt(inCursor++));
            result[outCursor++]=(byte)((ch0<<2)|(ch1>>4));
            result[outCursor++]=(byte)((ch1<<4)|(ch2>>2));
            result[outCursor++]=(byte)((ch2<<6)|ch3);
        }
        // Translate partial group, if present
        if(missingBytesInLastGroup!=0){
            int ch0=base64toInt(s.charAt(inCursor++));
            int ch1=base64toInt(s.charAt(inCursor++));
            result[outCursor++]=(byte)((ch0<<2)|(ch1>>4));
            if(missingBytesInLastGroup==1){
                int ch2=base64toInt(s.charAt(inCursor++));
                result[outCursor++]=(byte)((ch1<<4)|(ch2>>2));
            }
        }
        // assert inCursor == s.length()-missingBytesInLastGroup;
        // assert outCursor == result.length;
        return result;
    }

    private static int base64toInt(char c){
        int result;
        if(c>=base64ToInt.length)
            result=-1;
        else
            result=base64ToInt[c];
        if(result<0)
            throw new IllegalArgumentException("Illegal character "+c);
        return result;
    }

    private static RMIConnection getConnection(RMIServer server,
                                               Object credentials,
                                               boolean checkStub)
            throws IOException{
        RMIConnection c=server.newClient(credentials);
        if(checkStub) checkStub(c,rmiConnectionImplStubClass);
        try{
            if(c.getClass()==rmiConnectionImplStubClass)
                return shadowJrmpStub((RemoteObject)c);
            if(c.getClass().getName().equals(iiopConnectionStubClassName))
                return shadowIiopStub(c);
            logger.trace("getConnection",
                    "Did not wrap "+c.getClass()+" to foil "+
                            "stack search for classes: class loading semantics "+
                            "may be incorrect");
        }catch(Exception e){
            logger.error("getConnection",
                    "Could not wrap "+c.getClass()+" to foil "+
                            "stack search for classes: class loading semantics "+
                            "may be incorrect: "+e);
            logger.debug("getConnection",e);
            // so just return the original stub, which will work for all
            // but the most exotic class loading situations
        }
        return c;
    }

    private static RMIConnection shadowJrmpStub(RemoteObject stub)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, ClassNotFoundException,
            NoSuchMethodException{
        RemoteRef ref=stub.getRef();
        RemoteRef proxyRef=(RemoteRef)
                proxyRefConstructor.newInstance(new Object[]{ref});
        final Constructor<?> rmiConnectionImplStubConstructor=
                rmiConnectionImplStubClass.getConstructor(RemoteRef.class);
        Object[] args={proxyRef};
        RMIConnection proxyStub=(RMIConnection)
                rmiConnectionImplStubConstructor.newInstance(args);
        return proxyStub;
    }

    private static RMIConnection shadowIiopStub(Object stub)
            throws InstantiationException, IllegalAccessException{
        Object proxyStub=null;
        try{
            proxyStub=AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){
                public Object run() throws Exception{
                    return proxyStubClass.newInstance();
                }
            });
        }catch(PrivilegedActionException e){
            throw new InternalError();
        }
        IIOPHelper.setDelegate(proxyStub,IIOPHelper.getDelegate(stub));
        return (RMIConnection)proxyStub;
    }

    // added for re-connection
    private Integer addListenerWithSubject(ObjectName name,
                                           MarshalledObject<NotificationFilter> filter,
                                           Subject delegationSubject,
                                           boolean reconnect)
            throws InstanceNotFoundException, IOException{
        final boolean debug=logger.debugOn();
        if(debug)
            logger.debug("addListenerWithSubject",
                    "(ObjectName,MarshalledObject,Subject)");
        final ObjectName[] names=new ObjectName[]{name};
        final MarshalledObject<NotificationFilter>[] filters=
                Util.cast(new MarshalledObject<?>[]{filter});
        final Subject[] delegationSubjects=new Subject[]{
                delegationSubject
        };
        final Integer[] listenerIDs=
                addListenersWithSubjects(names,filters,delegationSubjects,
                        reconnect);
        if(debug) logger.debug("addListenerWithSubject","listenerID="
                +listenerIDs[0]);
        return listenerIDs[0];
    }

    // added for re-connection
    private Integer[] addListenersWithSubjects(ObjectName[] names,
                                               MarshalledObject<NotificationFilter>[] filters,
                                               Subject[] delegationSubjects,
                                               boolean reconnect)
            throws InstanceNotFoundException, IOException{
        final boolean debug=logger.debugOn();
        if(debug)
            logger.debug("addListenersWithSubjects",
                    "(ObjectName[],MarshalledObject[],Subject[])");
        final ClassLoader old=pushDefaultClassLoader();
        Integer[] listenerIDs=null;
        try{
            listenerIDs=connection.addNotificationListeners(names,
                    filters,
                    delegationSubjects);
        }catch(NoSuchObjectException noe){
            // maybe reconnect
            if(reconnect){
                communicatorAdmin.gotIOException(noe);
                listenerIDs=connection.addNotificationListeners(names,
                        filters,
                        delegationSubjects);
            }else{
                throw noe;
            }
        }catch(IOException ioe){
            // send a failed notif if necessary
            communicatorAdmin.gotIOException(ioe);
        }finally{
            popDefaultClassLoader(old);
        }
        if(debug) logger.debug("addListenersWithSubjects","registered "
                +((listenerIDs==null)?0:listenerIDs.length)
                +" listener(s)");
        return listenerIDs;
    }

    //--------------------------------------------------------------------
    // Private stuff - Find / Set default class loader
    //--------------------------------------------------------------------
    private ClassLoader pushDefaultClassLoader(){
        final Thread t=Thread.currentThread();
        final ClassLoader old=t.getContextClassLoader();
        if(defaultClassLoader!=null)
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run(){
                    t.setContextClassLoader(defaultClassLoader);
                    return null;
                }
            });
        return old;
    }

    private void popDefaultClassLoader(final ClassLoader old){
        AccessController.doPrivileged(new PrivilegedAction<Void>(){
            public Void run(){
                Thread.currentThread().setContextClassLoader(old);
                return null;
            }
        });
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        if(rmiServer==null&&jmxServiceURL==null) throw new
                InvalidObjectException("rmiServer and jmxServiceURL both null");
        initTransients();
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException{
        if(rmiServer==null&&jmxServiceURL==null) throw new
                InvalidObjectException("rmiServer and jmxServiceURL both null.");
        connectStub(this.rmiServer,env);
        s.defaultWriteObject();
    }

    private static final class ObjectInputStreamWithLoader
            extends ObjectInputStream{
        private final ClassLoader loader;

        ObjectInputStreamWithLoader(InputStream in,ClassLoader cl)
                throws IOException{
            super(in);
            this.loader=cl;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass classDesc)
                throws IOException, ClassNotFoundException{
            String name=classDesc.getName();
            ReflectUtil.checkPackageAccess(name);
            return Class.forName(name,false,loader);
        }
    }

    //--------------------------------------------------------------------
    // Implementation of MBeanServerConnection
    //--------------------------------------------------------------------
    private class RemoteMBeanServerConnection implements MBeanServerConnection{
        private Subject delegationSubject;

        public RemoteMBeanServerConnection(){
            this(null);
        }

        public RemoteMBeanServerConnection(Subject delegationSubject){
            this.delegationSubject=delegationSubject;
        }

        public ObjectInstance createMBean(String className,
                                          ObjectName name)
                throws ReflectionException,
                InstanceAlreadyExistsException,
                MBeanRegistrationException,
                MBeanException,
                NotCompliantMBeanException,
                IOException{
            if(logger.debugOn())
                logger.debug("createMBean(String,ObjectName)",
                        "className="+className+", name="+
                                name);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.createMBean(className,
                        name,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.createMBean(className,
                        name,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public ObjectInstance createMBean(String className,
                                          ObjectName name,
                                          ObjectName loaderName)
                throws ReflectionException,
                InstanceAlreadyExistsException,
                MBeanRegistrationException,
                MBeanException,
                NotCompliantMBeanException,
                InstanceNotFoundException,
                IOException{
            if(logger.debugOn())
                logger.debug("createMBean(String,ObjectName,ObjectName)",
                        "className="+className+", name="
                                +name+", loaderName="
                                +loaderName+")");
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.createMBean(className,
                        name,
                        loaderName,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.createMBean(className,
                        name,
                        loaderName,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public ObjectInstance createMBean(String className,
                                          ObjectName name,
                                          Object params[],
                                          String signature[])
                throws ReflectionException,
                InstanceAlreadyExistsException,
                MBeanRegistrationException,
                MBeanException,
                NotCompliantMBeanException,
                IOException{
            if(logger.debugOn())
                logger.debug("createMBean(String,ObjectName,Object[],String[])",
                        "className="+className+", name="
                                +name+", signature="+strings(signature));
            final MarshalledObject<Object[]> sParams=
                    new MarshalledObject<Object[]>(params);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.createMBean(className,
                        name,
                        sParams,
                        signature,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.createMBean(className,
                        name,
                        sParams,
                        signature,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public ObjectInstance createMBean(String className,
                                          ObjectName name,
                                          ObjectName loaderName,
                                          Object params[],
                                          String signature[])
                throws ReflectionException,
                InstanceAlreadyExistsException,
                MBeanRegistrationException,
                MBeanException,
                NotCompliantMBeanException,
                InstanceNotFoundException,
                IOException{
            if(logger.debugOn()) logger.debug(
                    "createMBean(String,ObjectName,ObjectName,Object[],String[])",
                    "className="+className+", name="+name+", loaderName="
                            +loaderName+", signature="+strings(signature));
            final MarshalledObject<Object[]> sParams=
                    new MarshalledObject<Object[]>(params);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.createMBean(className,
                        name,
                        loaderName,
                        sParams,
                        signature,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.createMBean(className,
                        name,
                        loaderName,
                        sParams,
                        signature,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public void unregisterMBean(ObjectName name)
                throws InstanceNotFoundException,
                MBeanRegistrationException,
                IOException{
            if(logger.debugOn())
                logger.debug("unregisterMBean","name="+name);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                connection.unregisterMBean(name,delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.unregisterMBean(name,delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public ObjectInstance getObjectInstance(ObjectName name)
                throws InstanceNotFoundException,
                IOException{
            if(logger.debugOn())
                logger.debug("getObjectInstance","name="+name);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.getObjectInstance(name,delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.getObjectInstance(name,delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public Set<ObjectInstance> queryMBeans(ObjectName name,
                                               QueryExp query)
                throws IOException{
            if(logger.debugOn()) logger.debug("queryMBeans",
                    "name="+name+", query="+query);
            final MarshalledObject<QueryExp> sQuery=
                    new MarshalledObject<QueryExp>(query);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.queryMBeans(name,sQuery,delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.queryMBeans(name,sQuery,delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public Set<ObjectName> queryNames(ObjectName name,
                                          QueryExp query)
                throws IOException{
            if(logger.debugOn()) logger.debug("queryNames",
                    "name="+name+", query="+query);
            final MarshalledObject<QueryExp> sQuery=
                    new MarshalledObject<QueryExp>(query);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.queryNames(name,sQuery,delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.queryNames(name,sQuery,delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public boolean isRegistered(ObjectName name)
                throws IOException{
            if(logger.debugOn())
                logger.debug("isRegistered","name="+name);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.isRegistered(name,delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.isRegistered(name,delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public Integer getMBeanCount()
                throws IOException{
            if(logger.debugOn()) logger.debug("getMBeanCount","");
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.getMBeanCount(delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.getMBeanCount(delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public Object getAttribute(ObjectName name,
                                   String attribute)
                throws MBeanException,
                AttributeNotFoundException,
                InstanceNotFoundException,
                ReflectionException,
                IOException{
            if(logger.debugOn()) logger.debug("getAttribute",
                    "name="+name+", attribute="
                            +attribute);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.getAttribute(name,
                        attribute,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.getAttribute(name,
                        attribute,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public AttributeList getAttributes(ObjectName name,
                                           String[] attributes)
                throws InstanceNotFoundException,
                ReflectionException,
                IOException{
            if(logger.debugOn()) logger.debug("getAttributes",
                    "name="+name+", attributes="
                            +strings(attributes));
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.getAttributes(name,
                        attributes,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.getAttributes(name,
                        attributes,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public void setAttribute(ObjectName name,
                                 Attribute attribute)
                throws InstanceNotFoundException,
                AttributeNotFoundException,
                InvalidAttributeValueException,
                MBeanException,
                ReflectionException,
                IOException{
            if(logger.debugOn()) logger.debug("setAttribute",
                    "name="+name+", attribute name="
                            +attribute.getName());
            final MarshalledObject<Attribute> sAttribute=
                    new MarshalledObject<Attribute>(attribute);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                connection.setAttribute(name,sAttribute,delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.setAttribute(name,sAttribute,delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public AttributeList setAttributes(ObjectName name,
                                           AttributeList attributes)
                throws InstanceNotFoundException,
                ReflectionException,
                IOException{
            if(logger.debugOn()){
                logger.debug("setAttributes",
                        "name="+name+", attribute names="
                                +getAttributesNames(attributes));
            }
            final MarshalledObject<AttributeList> sAttributes=
                    new MarshalledObject<AttributeList>(attributes);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.setAttributes(name,
                        sAttributes,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.setAttributes(name,
                        sAttributes,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public Object invoke(ObjectName name,
                             String operationName,
                             Object params[],
                             String signature[])
                throws InstanceNotFoundException,
                MBeanException,
                ReflectionException,
                IOException{
            if(logger.debugOn()) logger.debug("invoke",
                    "name="+name
                            +", operationName="+operationName
                            +", signature="+strings(signature));
            final MarshalledObject<Object[]> sParams=
                    new MarshalledObject<Object[]>(params);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.invoke(name,
                        operationName,
                        sParams,
                        signature,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.invoke(name,
                        operationName,
                        sParams,
                        signature,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public String getDefaultDomain()
                throws IOException{
            if(logger.debugOn()) logger.debug("getDefaultDomain","");
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.getDefaultDomain(delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.getDefaultDomain(delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public String[] getDomains() throws IOException{
            if(logger.debugOn()) logger.debug("getDomains","");
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.getDomains(delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.getDomains(delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public void addNotificationListener(ObjectName name,
                                            NotificationListener listener,
                                            NotificationFilter filter,
                                            Object handback)
                throws InstanceNotFoundException,
                IOException{
            final boolean debug=logger.debugOn();
            if(debug)
                logger.debug("addNotificationListener"+
                                "(ObjectName,NotificationListener,"+
                                "NotificationFilter,Object)",
                        "name="+name
                                +", listener="+listener
                                +", filter="+filter
                                +", handback="+handback);
            final Integer listenerID=
                    addListenerWithSubject(name,
                            new MarshalledObject<NotificationFilter>(filter),
                            delegationSubject,true);
            rmiNotifClient.addNotificationListener(listenerID,name,listener,
                    filter,handback,
                    delegationSubject);
        }

        public void addNotificationListener(ObjectName name,
                                            ObjectName listener,
                                            NotificationFilter filter,
                                            Object handback)
                throws InstanceNotFoundException,
                IOException{
            if(logger.debugOn())
                logger.debug("addNotificationListener"+
                                "(ObjectName,ObjectName,NotificationFilter,Object)",
                        "name="+name+", listener="+listener
                                +", filter="+filter+", handback="+handback);
            final MarshalledObject<NotificationFilter> sFilter=
                    new MarshalledObject<NotificationFilter>(filter);
            final MarshalledObject<Object> sHandback=
                    new MarshalledObject<Object>(handback);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                connection.addNotificationListener(name,
                        listener,
                        sFilter,
                        sHandback,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.addNotificationListener(name,
                        listener,
                        sFilter,
                        sHandback,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public void removeNotificationListener(ObjectName name,
                                               ObjectName listener)
                throws InstanceNotFoundException,
                ListenerNotFoundException,
                IOException{
            if(logger.debugOn()) logger.debug("removeNotificationListener"+
                            "(ObjectName,ObjectName)",
                    "name="+name
                            +", listener="+listener);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                connection.removeNotificationListener(name,
                        listener,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.removeNotificationListener(name,
                        listener,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public void removeNotificationListener(ObjectName name,
                                               ObjectName listener,
                                               NotificationFilter filter,
                                               Object handback)
                throws InstanceNotFoundException,
                ListenerNotFoundException,
                IOException{
            if(logger.debugOn())
                logger.debug("removeNotificationListener"+
                                "(ObjectName,ObjectName,NotificationFilter,Object)",
                        "name="+name
                                +", listener="+listener
                                +", filter="+filter
                                +", handback="+handback);
            final MarshalledObject<NotificationFilter> sFilter=
                    new MarshalledObject<NotificationFilter>(filter);
            final MarshalledObject<Object> sHandback=
                    new MarshalledObject<Object>(handback);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                connection.removeNotificationListener(name,
                        listener,
                        sFilter,
                        sHandback,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.removeNotificationListener(name,
                        listener,
                        sFilter,
                        sHandback,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public void removeNotificationListener(ObjectName name,
                                               NotificationListener listener)
                throws InstanceNotFoundException,
                ListenerNotFoundException,
                IOException{
            final boolean debug=logger.debugOn();
            if(debug) logger.debug("removeNotificationListener"+
                            "(ObjectName,NotificationListener)",
                    "name="+name
                            +", listener="+listener);
            final Integer[] ret=
                    rmiNotifClient.removeNotificationListener(name,listener);
            if(debug) logger.debug("removeNotificationListener",
                    "listenerIDs="+objects(ret));
            final ClassLoader old=pushDefaultClassLoader();
            try{
                connection.removeNotificationListeners(name,
                        ret,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.removeNotificationListeners(name,
                        ret,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }
        // Specific Notification Handle ----------------------------------

        public void removeNotificationListener(ObjectName name,
                                               NotificationListener listener,
                                               NotificationFilter filter,
                                               Object handback)
                throws InstanceNotFoundException,
                ListenerNotFoundException,
                IOException{
            final boolean debug=logger.debugOn();
            if(debug)
                logger.debug("removeNotificationListener"+
                                "(ObjectName,NotificationListener,"+
                                "NotificationFilter,Object)",
                        "name="+name
                                +", listener="+listener
                                +", filter="+filter
                                +", handback="+handback);
            final Integer ret=
                    rmiNotifClient.removeNotificationListener(name,listener,
                            filter,handback);
            if(debug) logger.debug("removeNotificationListener",
                    "listenerID="+ret);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                connection.removeNotificationListeners(name,
                        new Integer[]{ret},
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.removeNotificationListeners(name,
                        new Integer[]{ret},
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public MBeanInfo getMBeanInfo(ObjectName name)
                throws InstanceNotFoundException,
                IntrospectionException,
                ReflectionException,
                IOException{
            if(logger.debugOn()) logger.debug("getMBeanInfo","name="+name);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.getMBeanInfo(name,delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.getMBeanInfo(name,delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }

        public boolean isInstanceOf(ObjectName name,
                                    String className)
                throws InstanceNotFoundException,
                IOException{
            if(logger.debugOn())
                logger.debug("isInstanceOf","name="+name+
                        ", className="+className);
            final ClassLoader old=pushDefaultClassLoader();
            try{
                return connection.isInstanceOf(name,
                        className,
                        delegationSubject);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                return connection.isInstanceOf(name,
                        className,
                        delegationSubject);
            }finally{
                popDefaultClassLoader(old);
            }
        }
    }

    //--------------------------------------------------------------------
    private class RMINotifClient extends ClientNotifForwarder{
        public RMINotifClient(ClassLoader cl,Map<String,?> env){
            super(cl,env);
        }

        protected NotificationResult fetchNotifs(long clientSequenceNumber,
                                                 int maxNotifications,
                                                 long timeout)
                throws IOException, ClassNotFoundException{
            boolean retried=false;
            while(true){ // used for a successful re-connection
                // or a transient network problem
                try{
                    return connection.fetchNotifications(clientSequenceNumber,
                            maxNotifications,
                            timeout); // return normally
                }catch(IOException ioe){
                    // Examine the chain of exceptions to determine whether this
                    // is a deserialization issue. If so - we propagate the
                    // appropriate exception to the caller, who will then
                    // proceed with fetching notifications one by one
                    rethrowDeserializationException(ioe);
                    try{
                        communicatorAdmin.gotIOException(ioe);
                        // reconnection OK, back to "while" to do again
                    }catch(IOException ee){
                        boolean toClose=false;
                        synchronized(this){
                            if(terminated){
                                // the connection is closed.
                                throw ioe;
                            }else if(retried){
                                toClose=true;
                            }
                        }
                        if(toClose){
                            // JDK-8049303
                            // We received an IOException - but the communicatorAdmin
                            // did not close the connection - possibly because
                            // the original exception was raised by a transient network
                            // problem?
                            // We already know that this exception is not due to a deserialization
                            // issue as we already took care of that before involving the
                            // communicatorAdmin. Moreover - we already made one retry attempt
                            // at fetching the same batch of notifications - and the
                            // problem persisted.
                            // Since trying again doesn't seem to solve the issue, we will now
                            // close the connection. Doing otherwise might cause the
                            // NotifFetcher thread to die silently.
                            final Notification failedNotif=
                                    new JMXConnectionNotification(
                                            JMXConnectionNotification.FAILED,
                                            this,
                                            connectionId,
                                            clientNotifSeqNo++,
                                            "Failed to communicate with the server: "+ioe.toString(),
                                            ioe);
                            sendNotification(failedNotif);
                            try{
                                close(true);
                            }catch(Exception e){
                                // OK.
                                // We are closing
                            }
                            throw ioe; // the connection is closed here.
                        }else{
                            // JDK-8049303 possible transient network problem,
                            // let's try one more time
                            retried=true;
                        }
                    }
                }
            }
        }

        private void rethrowDeserializationException(IOException ioe)
                throws ClassNotFoundException, IOException{
            // specially treating for an UnmarshalException
            if(ioe instanceof UnmarshalException){
                throw ioe; // the fix of 6937053 made ClientNotifForwarder.fetchNotifs
                // fetch one by one with UnmarshalException
            }else if(ioe instanceof MarshalException){
                // IIOP will throw MarshalException wrapping a NotSerializableException
                // when a server fails to serialize a response.
                MarshalException me=(MarshalException)ioe;
                if(me.detail instanceof NotSerializableException){
                    throw (NotSerializableException)me.detail;
                }
            }
            // Not serialization problem, return.
        }

        protected Integer addListenerForMBeanRemovedNotif()
                throws IOException, InstanceNotFoundException{
            NotificationFilterSupport clientFilter=
                    new NotificationFilterSupport();
            clientFilter.enableType(
                    MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
            MarshalledObject<NotificationFilter> sFilter=
                    new MarshalledObject<NotificationFilter>(clientFilter);
            Integer[] listenerIDs;
            final ObjectName[] names=
                    new ObjectName[]{MBeanServerDelegate.DELEGATE_NAME};
            final MarshalledObject<NotificationFilter>[] filters=
                    Util.cast(new MarshalledObject<?>[]{sFilter});
            final Subject[] subjects=new Subject[]{null};
            try{
                listenerIDs=
                        connection.addNotificationListeners(names,
                                filters,
                                subjects);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                listenerIDs=
                        connection.addNotificationListeners(names,
                                filters,
                                subjects);
            }
            return listenerIDs[0];
        }

        protected void removeListenerForMBeanRemovedNotif(Integer id)
                throws IOException, InstanceNotFoundException,
                ListenerNotFoundException{
            try{
                connection.removeNotificationListeners(
                        MBeanServerDelegate.DELEGATE_NAME,
                        new Integer[]{id},
                        null);
            }catch(IOException ioe){
                communicatorAdmin.gotIOException(ioe);
                connection.removeNotificationListeners(
                        MBeanServerDelegate.DELEGATE_NAME,
                        new Integer[]{id},
                        null);
            }
        }

        protected void lostNotifs(String message,long number){
            final String notifType=JMXConnectionNotification.NOTIFS_LOST;
            final JMXConnectionNotification n=
                    new JMXConnectionNotification(notifType,
                            RMIConnector.this,
                            connectionId,
                            clientNotifCounter++,
                            message,
                            Long.valueOf(number));
            sendNotification(n);
        }
    }

    private class RMIClientCommunicatorAdmin extends ClientCommunicatorAdmin{
        public RMIClientCommunicatorAdmin(long period){
            super(period);
        }

        @Override
        public void gotIOException(IOException ioe) throws IOException{
            if(ioe instanceof NoSuchObjectException){
                // need to restart
                super.gotIOException(ioe);
                return;
            }
            // check if the connection is broken
            try{
                connection.getDefaultDomain(null);
            }catch(IOException ioexc){
                boolean toClose=false;
                synchronized(this){
                    if(!terminated){
                        terminated=true;
                        toClose=true;
                    }
                }
                if(toClose){
                    // we should close the connection,
                    // but send a failed notif at first
                    final Notification failedNotif=
                            new JMXConnectionNotification(
                                    JMXConnectionNotification.FAILED,
                                    this,
                                    connectionId,
                                    clientNotifSeqNo++,
                                    "Failed to communicate with the server: "+ioe.toString(),
                                    ioe);
                    sendNotification(failedNotif);
                    try{
                        close(true);
                    }catch(Exception e){
                        // OK.
                        // We are closing
                    }
                }
            }
            // forward the exception
            if(ioe instanceof ServerException){
                /** Need to unwrap the exception.
                 Some user-thrown exception at server side will be wrapped by
                 rmi into a ServerException.
                 For example, a RMIConnnectorServer will wrap a
                 ClassNotFoundException into a UnmarshalException, and rmi
                 will throw a ServerException at client side which wraps this
                 UnmarshalException.
                 No failed notif here.
                 */
                Throwable tt=((ServerException)ioe).detail;
                if(tt instanceof IOException){
                    throw (IOException)tt;
                }else if(tt instanceof RuntimeException){
                    throw (RuntimeException)tt;
                }
            }
            throw ioe;
        }

        protected void checkConnection() throws IOException{
            if(logger.debugOn())
                logger.debug("RMIClientCommunicatorAdmin-checkConnection",
                        "Calling the method getDefaultDomain.");
            connection.getDefaultDomain(null);
        }

        protected void doStart() throws IOException{
            // Get RMIServer stub from directory or URL encoding if needed.
            RMIServer stub;
            try{
                stub=(rmiServer!=null)?rmiServer:
                        findRMIServer(jmxServiceURL,env);
            }catch(NamingException ne){
                throw new IOException("Failed to get a RMI stub: "+ne);
            }
            // Connect IIOP Stub if needed.
            stub=connectStub(stub,env);
            // Calling newClient on the RMIServer stub.
            Object credentials=env.get(CREDENTIALS);
            connection=stub.newClient(credentials);
            // notif issues
            final ClientListenerInfo[] old=rmiNotifClient.preReconnection();
            reconnectNotificationListeners(old);
            connectionId=getConnectionId();
            Notification reconnectedNotif=
                    new JMXConnectionNotification(JMXConnectionNotification.OPENED,
                            this,
                            connectionId,
                            clientNotifSeqNo++,
                            "Reconnected to server",
                            null);
            sendNotification(reconnectedNotif);
        }

        public void reconnectNotificationListeners(ClientListenerInfo[] old) throws IOException{
            final int len=old.length;
            int i;
            ClientListenerInfo[] clis=new ClientListenerInfo[len];
            final Subject[] subjects=new Subject[len];
            final ObjectName[] names=new ObjectName[len];
            final NotificationListener[] listeners=new NotificationListener[len];
            final NotificationFilter[] filters=new NotificationFilter[len];
            final MarshalledObject<NotificationFilter>[] mFilters=
                    Util.cast(new MarshalledObject<?>[len]);
            final Object[] handbacks=new Object[len];
            for(i=0;i<len;i++){
                subjects[i]=old[i].getDelegationSubject();
                names[i]=old[i].getObjectName();
                listeners[i]=old[i].getListener();
                filters[i]=old[i].getNotificationFilter();
                mFilters[i]=new MarshalledObject<NotificationFilter>(filters[i]);
                handbacks[i]=old[i].getHandback();
            }
            try{
                Integer[] ids=addListenersWithSubjects(names,mFilters,subjects,false);
                for(i=0;i<len;i++){
                    clis[i]=new ClientListenerInfo(ids[i],
                            names[i],
                            listeners[i],
                            filters[i],
                            handbacks[i],
                            subjects[i]);
                }
                rmiNotifClient.postReconnection(clis);
                return;
            }catch(InstanceNotFoundException infe){
                // OK, we will do one by one
            }
            int j=0;
            for(i=0;i<len;i++){
                try{
                    Integer id=addListenerWithSubject(names[i],
                            new MarshalledObject<NotificationFilter>(filters[i]),
                            subjects[i],
                            false);
                    clis[j++]=new ClientListenerInfo(id,
                            names[i],
                            listeners[i],
                            filters[i],
                            handbacks[i],
                            subjects[i]);
                }catch(InstanceNotFoundException infe){
                    logger.warning("reconnectNotificationListeners",
                            "Can't reconnect listener for "+
                                    names[i]);
                }
            }
            if(j!=len){
                ClientListenerInfo[] tmp=clis;
                clis=new ClientListenerInfo[j];
                System.arraycopy(tmp,0,clis,0,j);
            }
            rmiNotifClient.postReconnection(clis);
        }

        protected void doStop(){
            try{
                close();
            }catch(IOException ioe){
                logger.warning("RMIClientCommunicatorAdmin-doStop",
                        "Failed to call the method close():"+ioe);
                logger.debug("RMIClientCommunicatorAdmin-doStop",ioe);
            }
        }
    }
}
