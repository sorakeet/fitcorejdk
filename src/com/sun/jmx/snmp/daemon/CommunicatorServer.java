/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.daemon;
// java import
//

import javax.management.*;
import javax.management.remote.MBeanServerForwarder;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_ADAPTOR_LOGGER;
// jmx import
//
// JSR 160 import
//
// XXX Revisit:
//   used to import com.sun.jmx.snmp.MBeanServerForwarder
// Now using JSR 160 instead. => this is an additional
// dependency to JSR 160.
//

public abstract class CommunicatorServer
        implements Runnable, MBeanRegistration, NotificationBroadcaster,
        CommunicatorServerMBean{
    //
    // States of a CommunicatorServer
    //
    public static final int ONLINE=0;
    public static final int OFFLINE=1;
    public static final int STOPPING=2;
    public static final int STARTING=3;
    //
    // Types of connectors.
    //
    /**
     * Indicates that it is an RMI connector type.
     */
    //public static final int RMI_TYPE = 1 ;
    //public static final int HTTP_TYPE = 2 ;
    //public static final int HTML_TYPE = 3 ;
    public static final int SNMP_TYPE=4;
    //public static final int HTTPS_TYPE = 5 ;
    //
    // Package variables
    //
    transient volatile int state=OFFLINE;
    ObjectName objectName;
    MBeanServer topMBS;
    MBeanServer bottomMBS;
    transient String dbgTag=null;
    int maxActiveClientCount=1;
    transient int servedClientCount=0;
    String host=null;
    int port=-1;
    //
    // Private fields
    //
    private transient Object stateLock=new Object();
    private transient Vector<ClientHandler>
            clientHandlerVector=new Vector<>();
    private transient Thread mainThread=null;
    private volatile boolean stopRequested=false;
    private boolean interrupted=false;
    private transient Exception startException=null;
    // Notifs count, broadcaster and info
    private transient long notifCount=0;
    private transient NotificationBroadcasterSupport notifBroadcaster=
            new NotificationBroadcasterSupport();
    private transient MBeanNotificationInfo[] notifInfos=null;

    public CommunicatorServer(int connectorType)
            throws IllegalArgumentException{
        switch(connectorType){
            case SNMP_TYPE:
                //No op. int Type deciding debugging removed.
                break;
            default:
                throw new IllegalArgumentException("Invalid connector Type");
        }
        dbgTag=makeDebugTag();
    }

    String makeDebugTag(){
        return "CommunicatorServer["+getProtocol()+":"+getPort()+"]";
    }

    protected Thread createMainThread(){
        return new Thread(this,makeThreadName());
    }

    public void start(long timeout)
            throws CommunicationException, InterruptedException{
        boolean start;
        synchronized(stateLock){
            if(state==STOPPING){
                // Fix for bug 4352451:
                //     "java.net.BindException: Address in use".
                waitState(OFFLINE,60000);
            }
            start=(state==OFFLINE);
            if(start){
                changeState(STARTING);
                stopRequested=false;
                interrupted=false;
                startException=null;
            }
        }
        if(!start){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "start","Connector is not OFFLINE");
            }
            return;
        }
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                    "start","--> Start connector ");
        }
        mainThread=createMainThread();
        mainThread.start();
        if(timeout>0) waitForStart(timeout);
    }

    @Override
    public void start(){
        try{
            start(0);
        }catch(InterruptedException x){
            // cannot happen because of `0'
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "start","interrupted",x);
            }
        }
    }

    @Override
    public void stop(){
        synchronized(stateLock){
            if(state==OFFLINE||state==STOPPING){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                            "stop","Connector is not ONLINE");
                }
                return;
            }
            changeState(STOPPING);
            //
            // Stop the connector thread
            //
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "stop","Interrupt main thread");
            }
            stopRequested=true;
            if(!interrupted){
                interrupted=true;
                mainThread.interrupt();
            }
        }
        //
        // Call terminate on each active client handler
        //
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                    "stop","terminateAllClient");
        }
        terminateAllClient();
        // ----------------------
        // changeState
        // ----------------------
        synchronized(stateLock){
            if(state==STARTING)
                changeState(OFFLINE);
        }
    }

    @Override
    public boolean isActive(){
        synchronized(stateLock){
            return (state==ONLINE);
        }
    }

    @Override
    public boolean waitState(int wantedState,long timeOut){
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                    "waitState",wantedState+"(0on,1off,2st) TO="+timeOut+
                            " ; current state = "+getStateString());
        }
        long endTime=0;
        if(timeOut>0)
            endTime=System.currentTimeMillis()+timeOut;
        synchronized(stateLock){
            while(state!=wantedState){
                if(timeOut<0){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                                "waitState","timeOut < 0, return without wait");
                    }
                    return false;
                }else{
                    try{
                        if(timeOut>0){
                            long toWait=endTime-System.currentTimeMillis();
                            if(toWait<=0){
                                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                                            "waitState","timed out");
                                }
                                return false;
                            }
                            stateLock.wait(toWait);
                        }else{  // timeOut == 0
                            stateLock.wait();
                        }
                    }catch(InterruptedException e){
                        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                                    "waitState","wait interrupted");
                        }
                        return (state==wantedState);
                    }
                }
            }
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "waitState","returning in desired state");
            }
            return true;
        }
    }

    @Override
    public int getState(){
        synchronized(stateLock){
            return state;
        }
    }

    @Override
    public String getStateString(){
        return getStringForState(state);
    }

    @Override
    public String getHost(){
        try{
            host=InetAddress.getLocalHost().getHostName();
        }catch(Exception e){
            host="Unknown host";
        }
        return host;
    }

    @Override
    public int getPort(){
        synchronized(stateLock){
            return port;
        }
    }

    @Override
    public void setPort(int port) throws IllegalStateException{
        synchronized(stateLock){
            if((state==ONLINE)||(state==STARTING))
                throw new IllegalStateException("Stop server before "+
                        "carrying out this operation");
            this.port=port;
            dbgTag=makeDebugTag();
        }
    }

    @Override
    public abstract String getProtocol();

    private void terminateAllClient(){
        final int s=clientHandlerVector.size();
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            if(s>=1){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "terminateAllClient","Interrupting "+s+" clients");
            }
        }
        // The ClientHandler will remove themselves from the
        // clientHandlerVector at the end of their run() method, by
        // calling notifyClientHandlerDeleted().
        // Since the clientHandlerVector is modified by the ClientHandler
        // threads we must avoid using Enumeration or Iterator to loop
        // over this array.
        // We cannot use the same logic here than in waitClientTermination()
        // because there is no guarantee that calling interrupt() on the
        // ClientHandler will actually terminate the ClientHandler.
        // Since we do not want to wait for the actual ClientHandler
        // termination, we cannot simply loop over the array until it is
        // empty (this might result in calling interrupt() endlessly on
        // the same client handler. So what we do is simply take a snapshot
        // copy of the vector and loop over the copy.
        // What we *MUST NOT DO* is locking the clientHandlerVector, because
        // this would most probably cause a deadlock.
        //
        final ClientHandler[] handlers=
                clientHandlerVector.toArray(new ClientHandler[0]);
        for(ClientHandler h : handlers){
            try{
                h.interrupt();
            }catch(Exception x){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                            "terminateAllClient",
                            "Failed to interrupt pending request. "+
                                    "Ignore the exception.",x);
                }
            }
        }
    }

    private void waitForStart(long timeout)
            throws CommunicationException, InterruptedException{
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                    "waitForStart","Timeout="+timeout+
                            " ; current state = "+getStateString());
        }
        final long startTime=System.currentTimeMillis();
        synchronized(stateLock){
            while(state==STARTING){
                // Time elapsed since startTime...
                //
                final long elapsed=System.currentTimeMillis()-startTime;
                // wait for timeout - elapsed.
                // A timeout of Long.MAX_VALUE is equivalent to something
                // like 292271023 years - which is pretty close to
                // forever as far as we are concerned ;-)
                //
                final long remainingTime=timeout-elapsed;
                // If remainingTime is negative, the timeout has elapsed.
                //
                if(remainingTime<0){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                                "waitForStart","timeout < 0, return without wait");
                    }
                    throw new InterruptedException("Timeout expired");
                }
                // We're going to wait until someone notifies on the
                // the stateLock object, or until the timeout expires,
                // or until the thread is interrupted.
                //
                try{
                    stateLock.wait(remainingTime);
                }catch(InterruptedException e){
                    if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                                "waitForStart","wait interrupted");
                    }
                    // If we are now ONLINE, then no need to rethrow the
                    // exception... we're simply going to exit the while
                    // loop. Otherwise, throw the InterruptedException.
                    //
                    if(state!=ONLINE) throw e;
                }
            }
            // We're no longer in STARTING state
            //
            if(state==ONLINE){
                // OK, we're started, everything went fine, just return
                //
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                            "waitForStart","started");
                }
                return;
            }else if(startException instanceof CommunicationException){
                // There was some exception during the starting phase.
                // Cast and throw...
                //
                throw (CommunicationException)startException;
            }else if(startException instanceof InterruptedException){
                // There was some exception during the starting phase.
                // Cast and throw...
                //
                throw (InterruptedException)startException;
            }else if(startException!=null){
                // There was some exception during the starting phase.
                // Wrap and throw...
                //
                throw new CommunicationException(startException,
                        "Failed to start: "+
                                startException);
            }else{
                // We're not ONLINE, and there's no exception...
                // Something went wrong but we don't know what...
                //
                throw new CommunicationException("Failed to start: state is "+
                        getStringForState(state));
            }
        }
    }

    int getServedClientCount(){
        return servedClientCount;
    }

    int getMaxActiveClientCount(){
        return maxActiveClientCount;
    }

    void setMaxActiveClientCount(int c)
            throws IllegalStateException{
        synchronized(stateLock){
            if((state==ONLINE)||(state==STARTING)){
                throw new IllegalStateException(
                        "Stop server before carrying out this operation");
            }
            maxActiveClientCount=c;
        }
    }

    void notifyClientHandlerCreated(ClientHandler h){
        clientHandlerVector.addElement(h);
    }

    synchronized void notifyClientHandlerDeleted(ClientHandler h){
        clientHandlerVector.removeElement(h);
        notifyAll();
    }

    @Override
    public void run(){
        // Fix jaw.00667.B
        // It seems that the init of "i" and "success"
        // need to be done outside the "try" clause...
        // A bug in Java 2 production release ?
        //
        int i=0;
        boolean success=false;
        // ----------------------
        // Bind
        // ----------------------
        try{
            // Fix for bug 4352451: "java.net.BindException: Address in use".
            //
            final int bindRetries=getBindTries();
            final long sleepTime=getBindSleepTime();
            while(i<bindRetries&&!success){
                try{
                    // Try socket connection.
                    //
                    doBind();
                    success=true;
                }catch(CommunicationException ce){
                    i++;
                    try{
                        Thread.sleep(sleepTime);
                    }catch(InterruptedException ie){
                        throw ie;
                    }
                }
            }
            // Retry last time to get correct exception.
            //
            if(!success){
                // Try socket connection.
                //
                doBind();
            }
        }catch(Exception x){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,dbgTag,
                        "run","Got unexpected exception",x);
            }
            synchronized(stateLock){
                startException=x;
                changeState(OFFLINE);
            }
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "run","State is OFFLINE");
            }
            doError(x);
            return;
        }
        try{
            // ----------------------
            // State change
            // ----------------------
            changeState(ONLINE);
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "run","State is ONLINE");
            }
            // ----------------------
            // Main loop
            // ----------------------
            while(!stopRequested){
                servedClientCount++;
                doReceive();
                waitIfTooManyClients();
                doProcess();
            }
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "run","Stop has been requested");
            }
        }catch(InterruptedException x){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,dbgTag,
                        "run","Interrupt caught");
            }
            changeState(STOPPING);
        }catch(Exception x){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,dbgTag,
                        "run","Got unexpected exception",x);
            }
            changeState(STOPPING);
        }finally{
            synchronized(stateLock){
                interrupted=true;
                Thread.interrupted();
            }
            // ----------------------
            // unBind
            // ----------------------
            try{
                doUnbind();
                waitClientTermination();
                changeState(OFFLINE);
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                            "run","State is OFFLINE");
                }
            }catch(Exception x){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,dbgTag,
                            "run","Got unexpected exception",x);
                }
                changeState(OFFLINE);
            }
        }
    }

    protected int getBindTries(){
        return 50;
    }

    protected long getBindSleepTime(){
        return 100;
    }
    //
    // To be defined by the subclass.
    //
    // Each method below is called by run() and must be subclassed.
    // If the method sends an exception (Communication or Interrupt), this
    // will end up the run() method and switch the connector offline.
    //
    // If it is a CommunicationException, run() will call
    //       Debug.printException().
    //
    // All these methods should propagate the InterruptedException to inform
    // run() that the connector must be switch OFFLINE.
    //
    //
    //
    // doBind() should do all what is needed before calling doReceive().
    // If doBind() throws an exception, doUnbind() is not to be called
    // and run() ends up.
    //

    protected abstract void doError(Exception e) throws CommunicationException;

    protected abstract void doBind()
            throws CommunicationException, InterruptedException;

    protected abstract void doReceive()
            throws CommunicationException, InterruptedException;

    protected abstract void doProcess()
            throws CommunicationException, InterruptedException;

    protected abstract void doUnbind()
            throws CommunicationException, InterruptedException;

    void changeState(int newState){
        int oldState;
        synchronized(stateLock){
            if(state==newState)
                return;
            oldState=state;
            state=newState;
            stateLock.notifyAll();
        }
        sendStateChangeNotification(oldState,newState);
    }

    private void sendStateChangeNotification(int oldState,int newState){
        String oldStateString=getStringForState(oldState);
        String newStateString=getStringForState(newState);
        String message=new StringBuffer().append(dbgTag)
                .append(" The value of attribute State has changed from ")
                .append(oldState).append(" (").append(oldStateString)
                .append(") to ").append(newState).append(" (")
                .append(newStateString).append(").").toString();
        notifCount++;
        AttributeChangeNotification notif=
                new AttributeChangeNotification(this,    // source
                        notifCount,                 // sequence number
                        System.currentTimeMillis(), // time stamp
                        message,                    // message
                        "State",                    // attribute name
                        "int",                      // attribute type
                        new Integer(oldState),      // old value
                        new Integer(newState));    // new value
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,dbgTag,
                    "sendStateChangeNotification","Sending AttributeChangeNotification #"
                            +notifCount+" with message: "+message);
        }
        notifBroadcaster.sendNotification(notif);
    }

    private static String getStringForState(int s){
        switch(s){
            case ONLINE:
                return "ONLINE";
            case STARTING:
                return "STARTING";
            case OFFLINE:
                return "OFFLINE";
            case STOPPING:
                return "STOPPING";
            default:
                return "UNDEFINED";
        }
    }

    private synchronized void waitIfTooManyClients()
            throws InterruptedException{
        while(getActiveClientCount()>=maxActiveClientCount){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "waitIfTooManyClients","Waiting for a client to terminate");
            }
            wait();
        }
    }

    int getActiveClientCount(){
        int result=clientHandlerVector.size();
        return result;
    }

    private void waitClientTermination(){
        int s=clientHandlerVector.size();
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            if(s>=1){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "waitClientTermination","waiting for "+
                                s+" clients to terminate");
            }
        }
        // The ClientHandler will remove themselves from the
        // clientHandlerVector at the end of their run() method, by
        // calling notifyClientHandlerDeleted().
        // Since the clientHandlerVector is modified by the ClientHandler
        // threads we must avoid using Enumeration or Iterator to loop
        // over this array. We must also take care of NoSuchElementException
        // which could be thrown if the last ClientHandler removes itself
        // between the call to clientHandlerVector.isEmpty() and the call
        // to clientHandlerVector.firstElement().
        // What we *MUST NOT DO* is locking the clientHandlerVector, because
        // this would most probably cause a deadlock.
        //
        while(!clientHandlerVector.isEmpty()){
            try{
                clientHandlerVector.firstElement().join();
            }catch(NoSuchElementException x){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                            "waitClientTermination","No elements left",x);
                }
            }
        }
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            if(s>=1){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,dbgTag,
                        "waitClientTermination","Ok, let's go...");
            }
        }
    }

    public synchronized MBeanServer getMBeanServer(){
        return topMBS;
    }

    public synchronized void setMBeanServer(MBeanServer newMBS)
            throws IllegalArgumentException, IllegalStateException{
        synchronized(stateLock){
            if(state==ONLINE||state==STARTING)
                throw new IllegalStateException("Stop server before "+
                        "carrying out this operation");
        }
        final String error=
                "MBeanServer argument must be MBean server where this "+
                        "server is registered, or an MBeanServerForwarder "+
                        "leading to that server";
        Vector<MBeanServer> seenMBS=new Vector<>();
        for(MBeanServer mbs=newMBS;
            mbs!=bottomMBS;
            mbs=((MBeanServerForwarder)mbs).getMBeanServer()){
            if(!(mbs instanceof MBeanServerForwarder))
                throw new IllegalArgumentException(error);
            if(seenMBS.contains(mbs))
                throw new IllegalArgumentException("MBeanServerForwarder "+
                        "loop");
            seenMBS.addElement(mbs);
        }
        topMBS=newMBS;
    }

    //
    // To be called by the subclass if needed
    //
    ObjectName getObjectName(){
        return objectName;
    }
    //
    // NotificationBroadcaster
    //

    String makeThreadName(){
        String result;
        if(objectName==null)
            result="CommunicatorServer";
        else
            result=objectName.toString();
        return result;
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException{
        // Call the default deserialization of the object.
        //
        stream.defaultReadObject();
        // Call the specific initialization for the CommunicatorServer service.
        // This is for transient structures to be initialized to specific
        // default values.
        //
        stateLock=new Object();
        state=OFFLINE;
        stopRequested=false;
        servedClientCount=0;
        clientHandlerVector=new Vector<>();
        mainThread=null;
        notifCount=0;
        notifInfos=null;
        notifBroadcaster=new NotificationBroadcasterSupport();
        dbgTag=makeDebugTag();
    }

    @Override
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws IllegalArgumentException{
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,dbgTag,
                    "addNotificationListener","Adding listener "+listener+
                            " with filter "+filter+" and handback "+handback);
        }
        notifBroadcaster.addNotificationListener(listener,filter,handback);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException{
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,dbgTag,
                    "removeNotificationListener","Removing listener "+listener);
        }
        notifBroadcaster.removeNotificationListener(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo(){
        // Initialize notifInfos on first call to getNotificationInfo()
        //
        if(notifInfos==null){
            notifInfos=new MBeanNotificationInfo[1];
            String[] notifTypes={
                    AttributeChangeNotification.ATTRIBUTE_CHANGE};
            notifInfos[0]=new MBeanNotificationInfo(notifTypes,
                    AttributeChangeNotification.class.getName(),
                    "Sent to notify that the value of the State attribute "+
                            "of this CommunicatorServer instance has changed.");
        }
        return notifInfos.clone();
    }
    //
    // MBeanRegistration
    //

    @Override
    public ObjectName preRegister(MBeanServer server,ObjectName name)
            throws Exception{
        objectName=name;
        synchronized(this){
            if(bottomMBS!=null){
                throw new IllegalArgumentException("connector already "+
                        "registered in an MBean "+
                        "server");
            }
            topMBS=bottomMBS=server;
        }
        dbgTag=makeDebugTag();
        return name;
    }

    @Override
    public void postRegister(Boolean registrationDone){
        if(!registrationDone.booleanValue()){
            synchronized(this){
                topMBS=bottomMBS=null;
            }
        }
    }

    @Override
    public void preDeregister() throws Exception{
        synchronized(this){
            topMBS=bottomMBS=null;
        }
        objectName=null;
        final int cstate=getState();
        if((cstate==ONLINE)||(cstate==STARTING)){
            stop();
        }
    }

    @Override
    public void postDeregister(){
    }
}
