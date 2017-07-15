/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp.daemon;
// java imports
//

import com.sun.jmx.snmp.SnmpDefinitions;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpVarBindList;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Stack;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_ADAPTOR_LOGGER;
// jmx imports
//

class SnmpSession implements SnmpDefinitions, Runnable{
    // PRIVATE VARIABLES
    //------------------
    protected transient SnmpAdaptorServer adaptor;
    protected transient SnmpSocket informSocket=null;
    SnmpQManager snmpQman=null;
    private transient Hashtable<SnmpInformRequest,SnmpInformRequest> informRequestList=
            new Hashtable<>();
    private transient Stack<SnmpInformRequest> informRespq=
            new Stack<>();
    private transient Thread myThread=null;
    private transient SnmpInformRequest syncInformReq;
    private boolean isBeingCancelled=false;
    // PUBLIC CONSTRUCTORS
    //--------------------

    public SnmpSession(SnmpAdaptorServer adp) throws SocketException{
        adaptor=adp;
        snmpQman=new SnmpQManager();
        SnmpResponseHandler snmpRespHdlr=new SnmpResponseHandler(adp,snmpQman);
        initialize(adp,snmpRespHdlr);
    }

    // OTHER METHODS
    //--------------
    protected synchronized void initialize(SnmpAdaptorServer adp,
                                           SnmpResponseHandler snmpRespHdlr)
            throws SocketException{
        informSocket=new SnmpSocket(snmpRespHdlr,adp.getAddress(),adp.getBufferSize().intValue());
        myThread=new Thread(this,"SnmpSession");
        myThread.start();
    }

    public SnmpSession() throws SocketException{
    }

    SnmpSocket getSocket(){
        return informSocket;
    }

    SnmpQManager getSnmpQManager(){
        return snmpQman;
    }

    SnmpInformRequest makeAsyncRequest(InetAddress addr,String cs,
                                       SnmpInformHandler cb,
                                       SnmpVarBindList vblst,int port)
            throws SnmpStatusException{
        if(!isSessionActive()){
            throw new SnmpStatusException("SNMP adaptor server not ONLINE");
        }
        SnmpInformRequest snmpreq=new SnmpInformRequest(this,adaptor,addr,cs,port,cb);
        snmpreq.start(vblst);
        return snmpreq;
    }

    synchronized boolean isSessionActive(){
        //return ((myThread != null) && (myThread.isAlive()));
        return ((adaptor.isActive())&&(myThread!=null)&&(myThread.isAlive()));
    }

    void waitForResponse(SnmpInformRequest req,long waitTime){
        if(!req.inProgress())
            return;
        setSyncMode(req);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSession.class.getName(),
                    "waitForResponse","Session switching to sync mode for inform request "+req.getRequestId());
        }
        long maxTime;
        if(waitTime<=0)
            maxTime=System.currentTimeMillis()+6000*1000;
        else
            maxTime=System.currentTimeMillis()+waitTime;
        while(req.inProgress()||syncInProgress()){
            waitTime=maxTime-System.currentTimeMillis();
            if(waitTime<=0)
                break;
            synchronized(this){
                if(!informRespq.removeElement(req)){
                    try{
                        this.wait(waitTime);
                    }catch(InterruptedException e){
                    }
                    continue;
                }
            }
            try{
                processResponse(req);
            }catch(Exception e){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSession.class.getName(),
                            "waitForResponse","Got unexpected exception",e);
                }
            }
        }
        resetSyncMode();
    }

    private synchronized boolean syncInProgress(){
        return syncInformReq!=null;
    }

    private synchronized void setSyncMode(SnmpInformRequest req){
        syncInformReq=req;
    }

    private synchronized void resetSyncMode(){
        if(syncInformReq==null)
            return;
        syncInformReq=null;
        if(thisSessionContext())
            return;
        this.notifyAll();
    }

    boolean thisSessionContext(){
        return (Thread.currentThread()==myThread);
    }

    private void processResponse(SnmpInformRequest reqc){
        while(reqc!=null&&myThread!=null){
            try{
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSession.class.getName(),
                            "processResponse","Processing response to req = "+reqc.getRequestId());
                }
                reqc.processResponse();  // Handles out of memory.
                reqc=null;  // finished processing.
            }catch(Exception e){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSession.class.getName(),
                            "processResponse","Got unexpected exception",e);
                }
                reqc=null;
            }catch(OutOfMemoryError ome){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSession.class.getName(),
                            "processResponse","Out of memory error in session thread",ome);
                }
                Thread.yield();
                continue;   // re-process the request.
            }
        }
    }

    @Override
    public void run(){
        myThread=Thread.currentThread();
        myThread.setPriority(Thread.NORM_PRIORITY);
        SnmpInformRequest reqc=null;
        while(myThread!=null){
            try{
                reqc=nextResponse();
                if(reqc!=null){
                    processResponse(reqc);
                }
            }catch(ThreadDeath d){
                myThread=null;
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSession.class.getName(),
                            "run","ThreadDeath, session thread unexpectedly shutting down");
                }
                throw d;
            }
        }
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSession.class.getName(),
                    "run","Session thread shutting down");
        }
        myThread=null;
    }
    // HANDLING INFORM REQUESTS LIST AND INFORM RESPONSES LIST
    //--------------------------------------------------------

    private synchronized SnmpInformRequest nextResponse(){
        if(informRespq.isEmpty()){
            try{
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSession.class.getName(),
                            "nextResponse","Blocking for response");
                }
                this.wait();
            }catch(InterruptedException e){
            }
        }
        if(informRespq.isEmpty())
            return null;
        SnmpInformRequest reqc=informRespq.firstElement();
        informRespq.removeElementAt(0);
        return reqc;
    }

    synchronized void addInformRequest(SnmpInformRequest snmpreq) throws SnmpStatusException{
        // If the adaptor is not ONLINE, stop adding requests.
        //
        if(!isSessionActive()){
            throw new SnmpStatusException("SNMP adaptor is not ONLINE or session is dead...");
        }
        informRequestList.put(snmpreq,snmpreq);
    }

    synchronized void removeInformRequest(SnmpInformRequest snmpreq){
        // deleteRequest can be called from destroySnmpSession.
        //In such a case remove is done in cancelAllRequest method.
        if(!isBeingCancelled)
            informRequestList.remove(snmpreq);
        if(syncInformReq!=null&&syncInformReq==snmpreq){
            resetSyncMode();
        }
    }

    void addResponse(SnmpInformRequest reqc){
        SnmpInformRequest snmpreq=reqc;
        if(isSessionActive()){
            synchronized(this){
                informRespq.push(reqc);
                this.notifyAll();
            }
        }else{
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpSession.class.getName(),
                        "addResponse","Adaptor not ONLINE or session thread dead, so inform response is dropped..."+reqc.getRequestId());
            }
        }
    }

    final void destroySession(){
        cancelAllRequests();
        cancelAllResponses();
        synchronized(this){
            informSocket.close();
            informSocket=null;
        }
        snmpQman.stopQThreads();
        snmpQman=null;
        killSessionThread();
    }

    private void cancelAllRequests(){
        final SnmpInformRequest[] list;
        synchronized(this){
            if(informRequestList.isEmpty()){
                return;
            }
            isBeingCancelled=true;
            list=new SnmpInformRequest[informRequestList.size()];
            java.util.Iterator<SnmpInformRequest> it=informRequestList.values().iterator();
            int i=0;
            while(it.hasNext()){
                SnmpInformRequest req=it.next();
                list[i++]=req;
                it.remove();
            }
            informRequestList.clear();
        }
        for(int i=0;i<list.length;i++)
            list[i].cancelRequest();
    }

    private synchronized void cancelAllResponses(){
        if(informRespq!=null){
            syncInformReq=null;
            informRespq.removeAllElements();
            this.notifyAll();
        }
    }

    private synchronized void killSessionThread(){
        if((myThread!=null)&&(myThread.isAlive())){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSession.class.getName(),
                        "killSessionThread","Destroying session");
            }
            if(!thisSessionContext()){
                myThread=null;
                this.notifyAll();
            }else
                myThread=null;
        }
    }

    @Override
    protected void finalize(){
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpSession.class.getName(),
                    "finalize","Shutting all servers");
        }
        if(informRespq!=null)
            informRespq.removeAllElements();
        informRespq=null;
        if(informSocket!=null)
            informSocket.close();
        informSocket=null;
        snmpQman=null;
    }
}
