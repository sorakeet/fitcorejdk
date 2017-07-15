/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp.daemon;
// JAVA imports
//

import com.sun.jmx.snmp.*;

import java.net.InetAddress;
import java.util.Date;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_ADAPTOR_LOGGER;
// JMX imports
//

public class SnmpInformRequest implements SnmpDefinitions{
    final static private int stBase=1;
    final static public int stInProgress=stBase;
    final static public int stWaitingToSend=(stBase<<1)|stInProgress;
    final static public int stWaitingForReply=(stBase<<2)|stInProgress;
    final static public int stReceivedReply=(stBase<<3)|stInProgress;
    final static public int stAborted=(stBase<<4);
    final static public int stTimeout=(stBase<<5);
    final static public int stInternalError=(stBase<<6);
    final static public int stResultsAvailable=(stBase<<7);
    final static public int stNeverUsed=(stBase<<8);
    // VARIABLES
    //----------
    private static SnmpRequestCounter requestCounter=new SnmpRequestCounter();
    int errorStatus=0;
    int errorIndex=0;
    //private SnmpVarBind internalVarBind[] = null;
    SnmpVarBind internalVarBind[]=null;
    //private String reason = null;
    String reason=null;
    //private SnmpPduPacket requestPdu;
    SnmpPdu requestPdu;
    //private SnmpPduRequest responsePdu;
    SnmpPduRequestType responsePdu;
    private SnmpVarBindList varBindList=null;
    private transient SnmpAdaptorServer adaptor;
    private transient SnmpSession informSession;
    private SnmpInformHandler callback=null;
    private int numTries=0;
    private int timeout=3*1000; // 3 seconds.
    private int reqState=stNeverUsed;
    // Polling control parameters.
    private long prevPollTime=0;     // value of 0 means poll never happened.
    private long nextPollTime=0;
    private long waitTimeForResponse;
    private Date debugDate=new Date();
    private int requestId=0;
    private int port=0;
    private InetAddress address=null;
    private String communityString=null;
    // CONSTRUCTORS
    //-------------

    SnmpInformRequest(SnmpSession session,
                      SnmpAdaptorServer adp,
                      InetAddress addr,
                      String cs,
                      int p,
                      SnmpInformHandler requestCB)
            throws SnmpStatusException{
        informSession=session;
        adaptor=adp;
        address=addr;
        communityString=cs;
        port=p;
        callback=requestCB;
        informSession.addInformRequest(this);  // add to adaptor queue.
        setTimeout(adaptor.getTimeout());
    }
    // PUBLIC METHODS
    //---------------

    final synchronized void setTimeout(int value){
        timeout=value;
    }

    final public synchronized int getRequestId(){
        return requestId;
    }

    synchronized InetAddress getAddress(){
        return address;
    }

    final public synchronized int getRequestStatus(){
        return reqState;
    }

    final synchronized void setRequestStatus(int reqst){
        reqState=reqst;
    }

    final public synchronized boolean isAborted(){
        return ((reqState&stAborted)==stAborted);
    }

    final public synchronized boolean isResultAvailable(){
        return (reqState==stResultsAvailable);
    }

    final public synchronized int getNumTries(){
        return numTries;
    }

    final public synchronized long getAbsNextPollTime(){
        return nextPollTime;
    }

    final public synchronized long getAbsMaxTimeToWait(){
        if(prevPollTime==0){
            return System.currentTimeMillis();  // should never happen.
        }else{
            return waitTimeForResponse;
        }
    }

    public final synchronized SnmpVarBindList getResponseVarBindList(){
        if(inProgress())
            return null;
        return varBindList;
    }

    final public synchronized boolean inProgress(){
        return ((reqState&stInProgress)==stInProgress);
    }

    final public boolean waitForCompletion(long time){
        if(!inProgress())     // check if request is in progress.
            return true;
        if(informSession.thisSessionContext()){
            // We can manipulate callback safely as we are in session thread.
            //
            SnmpInformHandler savedCallback=callback;
            callback=null;
            informSession.waitForResponse(this,time);
            callback=savedCallback;
        }else{
            // This is being done from a different thread. So notifyClient will do the notification.
            //
            synchronized(this){
                SnmpInformHandler savedCallback=callback;
                try{
                    callback=null;
                    this.wait(time);
                }catch(InterruptedException e){
                }
                callback=savedCallback;
            }
        }
        return (!inProgress()); // true if request completed.
    }

    final public void cancelRequest(){
        errorStatus=snmpReqAborted;
        stopRequest();
        deleteRequest();
        notifyClient();
    }

    final public synchronized void notifyClient(){
        this.notifyAll();
    }

    private void stopRequest(){
        // Remove the clause synchronized of the stopRequest method.
        // Synchronization is isolated as possible to avoid thread lock.
        // Note: the method removeRequest from SendQ is synchronized.
        // fix bug jaw.00392.B
        //
        synchronized(this){
            setRequestStatus(stAborted);
        }
        informSession.getSnmpQManager().removeRequest(this);
        synchronized(this){
            requestId=0;
        }
    }

    final synchronized void deleteRequest(){
        informSession.removeInformRequest(this);
    }

    synchronized void start(SnmpVarBindList vblst) throws SnmpStatusException{
        if(inProgress())
            throw new SnmpStatusException("Inform request already in progress.");
        setVarBindList(vblst);
        initializeAndFire();
    }

    private synchronized void initializeAndFire(){
        requestPdu=null;
        responsePdu=null;
        reason=null;
        startRequest(System.currentTimeMillis());
        setErrorStatusAndIndex(0,0);
    }
    // PRIVATE AND PACKAGE METHODS
    //----------------------------

    private synchronized void startRequest(long starttime){
        nextPollTime=starttime;
        prevPollTime=0;
        schedulePoll();
    }

    private void schedulePoll(){
        numTries=0;
        initNewRequest();
        setRequestStatus(stWaitingToSend);
        informSession.getSnmpQManager().addRequest(this);
    }

    final synchronized void initNewRequest(){
        requestId=requestCounter.getNewId();
    }

    final synchronized void setErrorStatusAndIndex(int stat,int idx){
        errorStatus=stat;
        errorIndex=idx;
    }

    void action(){
        if(inProgress()==false)
            return;
        while(true){
            try{
                if(numTries==0){
                    invokeOnReady();
                }else if(numTries<getMaxTries()){
                    invokeOnRetry();
                }else{
                    invokeOnTimeout();
                }
                return;
            }catch(OutOfMemoryError omerr){
                // Consider it as a try !
                //
                numTries++;
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                            "action","Inform request hit out of memory situation...");
                }
                Thread.yield();
            }
        }
    }

    private void invokeOnReady(){
        if(requestPdu==null){
            requestPdu=constructPduPacket();
        }
        if(requestPdu!=null){
            if(sendPdu()==false)
                queueResponse();
        }
    }

    private void invokeOnRetry(){
        invokeOnReady();
    }

    private void invokeOnTimeout(){
        errorStatus=snmpReqTimeout;
        queueResponse();
    }

    synchronized SnmpPdu constructPduPacket(){
        SnmpPduPacket reqpdu=null;
        Exception excep=null;
        try{
            reqpdu=new SnmpPduRequest();
            reqpdu.port=port;
            reqpdu.type=pduInformRequestPdu;
            reqpdu.version=snmpVersionTwo;
            reqpdu.community=communityString.getBytes("8859_1");
            reqpdu.requestId=getRequestId();
            reqpdu.varBindList=internalVarBind;
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpInformRequest.class.getName(),
                        "constructPduPacket","Packet built");
            }
        }catch(Exception e){
            excep=e;
            errorStatus=snmpReqUnknownError;
            reason=e.getMessage();
        }
        if(excep!=null){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "constructPduPacket","Got unexpected exception",excep);
            }
            reqpdu=null;
            queueResponse();
        }
        return reqpdu;
    }

    boolean sendPdu(){
        try{
            responsePdu=null;
            SnmpPduFactory pduFactory=adaptor.getPduFactory();
            SnmpMessage msg=(SnmpMessage)pduFactory.encodeSnmpPdu((SnmpPduPacket)requestPdu,adaptor.getBufferSize().intValue());
            if(msg==null){
                if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                            "sendPdu","pdu factory returned a null value");
                }
                throw new SnmpStatusException(snmpReqUnknownError);
                // This exception will caught hereafter and reported as an snmpReqUnknownError
                // FIXME: may be it's not the best behaviour ?
            }
            int maxPktSize=adaptor.getBufferSize().intValue();
            byte[] encoding=new byte[maxPktSize];
            int encodingLength=msg.encodeMessage(encoding);
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpInformRequest.class.getName(),
                        "sendPdu","Dump : \n"+msg.printMessage());
            }
            sendPduPacket(encoding,encodingLength);
            return true;
        }catch(SnmpTooBigException ar){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "sendPdu","Got unexpected exception",ar);
            }
            setErrorStatusAndIndex(snmpReqPacketOverflow,ar.getVarBindCount());
            requestPdu=null;
            reason=ar.getMessage();
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "sendPdu","Packet Overflow while building inform request");
            }
        }catch(java.io.IOException ioe){
            setErrorStatusAndIndex(snmpReqSocketIOError,0);
            reason=ioe.getMessage();
        }catch(Exception e){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "sendPdu","Got unexpected exception",e);
            }
            setErrorStatusAndIndex(snmpReqUnknownError,0);
            reason=e.getMessage();
        }
        return false;
    }

    final void sendPduPacket(byte[] buffer,int length) throws java.io.IOException{
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpInformRequest.class.getName(),
                    "sendPduPacket","Send to peer. Peer/Port : "+address.getHostName()+
                            "/"+port+". Length = "+length+"\nDump : \n"+
                            SnmpMessage.dumpHexBuffer(buffer,0,length));
        }
        SnmpSocket theSocket=informSession.getSocket();
        synchronized(theSocket){
            theSocket.sendPacket(buffer,length,address,port);
            setRequestSentTime(System.currentTimeMillis());
        }
    }

    final void processResponse(){
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpInformRequest.class.getName(),
                    "processResponse","errstatus = "+errorStatus);
        }
        if(inProgress()==false){  // check if this request is still alive.
            responsePdu=null;
            return;  // the request may have  cancelled.
        }
        if(errorStatus>=snmpReqInternalError){
            handleInternalError("Internal Error...");
            return;
        }
        try{
            parsePduPacket(responsePdu);
            //responsePdu = null;
            // At this point the errorIndex is rationalized to start with 0.
            switch(errorStatus){
                case snmpRspNoError:
                    handleSuccess();
                    return;
                case snmpReqTimeout:
                    handleTimeout();
                    return;
                case snmpReqInternalError:
                    handleInternalError("Unknown internal error.  deal with it later!");
                    return;
                case snmpReqHandleTooBig:
                    setErrorStatusAndIndex(snmpRspTooBig,0);
                    handleError("Cannot handle too-big situation...");
                    return;
                case snmpReqRefireAfterVbFix:
                    // Refire request after fixing varbindlist.
                    initializeAndFire();
                    return;
                default:
                    handleError("Error status set in packet...!!");
                    return;
            }
        }catch(Exception e){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "processResponse","Got unexpected exception",e);
            }
            reason=e.getMessage();
        }
        handleInternalError(reason);
    }

    synchronized void parsePduPacket(SnmpPduRequestType rpdu){
        if(rpdu==null)
            return;
        errorStatus=rpdu.getErrorStatus();
        errorIndex=rpdu.getErrorIndex();
        if(errorStatus==snmpRspNoError){
            updateInternalVarBindWithResult(((SnmpPdu)rpdu).varBindList);
            return;
        }
        if(errorStatus!=snmpRspNoError)
            --errorIndex;  // rationalize for index to start with 0.
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpInformRequest.class.getName(),
                    "parsePduPacket","received inform response. ErrorStatus/ErrorIndex = "
                            +errorStatus+"/"+errorIndex);
        }
    }

    void updateInternalVarBindWithResult(SnmpVarBind[] list){
        if((list==null)||(list.length==0))
            return;
        int idx=0;
        for(int i=0;i<internalVarBind.length&&idx<list.length;i++){
            SnmpVarBind avar=internalVarBind[i];
            if(avar==null)
                continue;
            SnmpVarBind res=list[idx];
            avar.setSnmpValue(res.getSnmpValue());
            idx++;
        }
    }

    private void handleSuccess(){
        setRequestStatus(stResultsAvailable);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpInformRequest.class.getName(),
                    "handleSuccess","Invoking user defined callback...");
        }
        deleteRequest();  // delete only non-poll request.
        notifyClient();
        requestPdu=null;
        //responsePdu = null;
        internalVarBind=null;
        try{  // catch all user exception which may happen in callback.
            if(callback!=null)
                callback.processSnmpPollData(this,errorStatus,errorIndex,getVarBindList());
        }catch(Exception e){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleSuccess","Exception generated by user callback",e);
            }
        }catch(OutOfMemoryError ome){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleSuccess","OutOfMemory Error generated by user callback",ome);
            }
            Thread.yield();
        }
    }

    private void handleTimeout(){
        setRequestStatus(stTimeout);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                    "handleTimeout","Snmp error/index = "+snmpErrorToString(errorStatus)
                            +"/"+errorIndex+". Invoking timeout user defined callback...");
        }
        deleteRequest();
        notifyClient();
        requestPdu=null;
        responsePdu=null;
        internalVarBind=null;
        try{
            if(callback!=null)
                callback.processSnmpPollTimeout(this);
        }catch(Exception e){  // catch any exception a user might not handle.
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleTimeout","Exception generated by user callback",e);
            }
        }catch(OutOfMemoryError ome){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleTimeout","OutOfMemory Error generated by user callback",ome);
            }
            Thread.yield();
        }
    }

    private void handleError(String msg){
        setRequestStatus(stResultsAvailable);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                    "handleError","Snmp error/index = "+snmpErrorToString(errorStatus)+"/"+
                            errorIndex+". Invoking error user defined callback...\n"+getVarBindList());
        }
        deleteRequest();
        notifyClient();
        requestPdu=null;
        responsePdu=null;
        internalVarBind=null;
        try{
            if(callback!=null)
                callback.processSnmpPollData(this,getErrorStatus(),getErrorIndex(),getVarBindList());
        }catch(Exception e){  // catch any exception a user might not handle.
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleError","Exception generated by user callback",e);
            }
        }catch(OutOfMemoryError ome){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleError","OutOfMemory Error generated by user callback",ome);
            }
            Thread.yield();
        }
    }

    final public synchronized int getErrorStatus(){
        return errorStatus;
    }

    final public synchronized int getErrorIndex(){
        return errorIndex;
    }

    private void handleInternalError(String msg){
        setRequestStatus(stInternalError);
        if(reason==null)
            reason=msg;
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                    "handleInternalError","Snmp error/index = "+snmpErrorToString(errorStatus)+
                            "/"+errorIndex+". Invoking internal error user defined callback...\n"+
                            getVarBindList());
        }
        deleteRequest();
        notifyClient();
        requestPdu=null;
        responsePdu=null;
        internalVarBind=null;
        try{
            if(callback!=null)
                callback.processSnmpInternalError(this,reason);
        }catch(Exception e){  // catch any exception a user might not handle.
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleInternalError","Exception generated by user callback",e);
            }
        }catch(OutOfMemoryError ome){
            if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)){
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,SnmpInformRequest.class.getName(),
                        "handleInternalError","OutOfMemory Error generated by user callback",ome);
            }
            Thread.yield();
        }
    }

    public static String snmpErrorToString(int errcode){
        switch(errcode){
            case snmpRspNoError:
                return "noError";
            case snmpRspTooBig:
                return "tooBig";
            case snmpRspNoSuchName:
                return "noSuchName";
            case snmpRspBadValue:
                return "badValue";
            case snmpRspReadOnly:
                return "readOnly";
            case snmpRspGenErr:
                return "genErr";
            case snmpRspNoAccess:
                return "noAccess";
            case snmpRspWrongType:
                return "wrongType";
            case snmpRspWrongLength:
                return "wrongLength";
            case snmpRspWrongEncoding:
                return "wrongEncoding";
            case snmpRspWrongValue:
                return "wrongValue";
            case snmpRspNoCreation:
                return "noCreation";
            case snmpRspInconsistentValue:
                return "inconsistentValue";
            case snmpRspResourceUnavailable:
                return "resourceUnavailable";
            case snmpRspCommitFailed:
                return "commitFailed";
            case snmpRspUndoFailed:
                return "undoFailed";
            case snmpRspAuthorizationError:
                return "authorizationError";
            case snmpRspNotWritable:
                return "notWritable";
            case snmpRspInconsistentName:
                return "inconsistentName";
            case snmpReqTimeout:
                return "reqTimeout";
            case snmpReqAborted:
                return "reqAborted";
            case snmpRspDecodingError:
                return "rspDecodingError";
            case snmpReqEncodingError:
                return "reqEncodingError";
            case snmpReqPacketOverflow:
                return "reqPacketOverflow";
            case snmpRspEndOfTable:
                return "rspEndOfTable";
            case snmpReqRefireAfterVbFix:
                return "reqRefireAfterVbFix";
            case snmpReqHandleTooBig:
                return "reqHandleTooBig";
            case snmpReqTooBigImpossible:
                return "reqTooBigImpossible";
            case snmpReqInternalError:
                return "reqInternalError";
            case snmpReqSocketIOError:
                return "reqSocketIOError";
            case snmpReqUnknownError:
                return "reqUnknownError";
            case snmpWrongSnmpVersion:
                return "wrongSnmpVersion";
            case snmpUnknownPrincipal:
                return "snmpUnknownPrincipal";
            case snmpAuthNotSupported:
                return "snmpAuthNotSupported";
            case snmpPrivNotSupported:
                return "snmpPrivNotSupported";
            case snmpBadSecurityLevel:
                return "snmpBadSecurityLevel";
            case snmpUsmBadEngineId:
                return "snmpUsmBadEngineId";
            case snmpUsmInvalidTimeliness:
                return "snmpUsmInvalidTimeliness";
        }
        return "Unknown Error = "+errcode;
    }

    final synchronized SnmpVarBindList getVarBindList(){
        return varBindList;
    }

    final synchronized void setVarBindList(SnmpVarBindList newvblst){
        varBindList=newvblst;
        if(internalVarBind==null||internalVarBind.length!=varBindList.size()){
            internalVarBind=new SnmpVarBind[varBindList.size()];
        }
        varBindList.copyInto(internalVarBind);
    }

    final void invokeOnResponse(Object resp){
        if(resp!=null){
            if(resp instanceof SnmpPduRequestType)
                responsePdu=(SnmpPduRequestType)resp;
            else
                return;
        }
        setRequestStatus(stReceivedReply);
        queueResponse();
    }

    private void queueResponse(){
        informSession.addResponse(this);
    }

    final synchronized void setPrevPollTime(long prev){
        prevPollTime=prev;
    }

    final void setRequestSentTime(long sendtime){
        numTries++;
        setPrevPollTime(sendtime);
        waitTimeForResponse=prevPollTime+timeout*numTries;
        setRequestStatus(stWaitingForReply);
        if(SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)){
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER,SnmpInformRequest.class.getName(),
                    "setRequestSentTime","Inform request Successfully sent");
        }
        informSession.getSnmpQManager().addWaiting(this);
    }

    @Override
    public synchronized String toString(){
        StringBuffer s=new StringBuffer(300);
        s.append(tostring());
        s.append("\nPeer/Port : "+address.getHostName()+"/"+port);
        return s.toString();
    }

    @Override
    protected void finalize(){
        callback=null;
        varBindList=null;
        internalVarBind=null;
        adaptor=null;
        informSession=null;
        requestPdu=null;
        responsePdu=null;
    }

    private synchronized String tostring(){
        StringBuffer s=new StringBuffer("InformRequestId = "+requestId);
        s.append("   "+"Status = "+statusDescription(reqState));
        s.append("  Timeout/MaxTries/NumTries = "+timeout*numTries+"/"+
                +getMaxTries()+"/"+numTries);
        if(prevPollTime>0){
            debugDate.setTime(prevPollTime);
            s.append("\nPrevPolled = "+debugDate.toString());
        }else
            s.append("\nNeverPolled");
        s.append(" / RemainingTime(millis) = "+
                timeRemainingForAction(System.currentTimeMillis()));
        return s.toString();
    }

    final public int getMaxTries(){
        return adaptor.getMaxTries();
    }

    long timeRemainingForAction(long currtime){
        switch(reqState){
            case stWaitingToSend:
                return nextPollTime-currtime;
            case stWaitingForReply:
                return waitTimeForResponse-currtime;
            default:
                return -1;
        }
    }

    static String statusDescription(int state){
        switch(state){
            case stWaitingToSend:
                return "Waiting to send.";
            case stWaitingForReply:
                return "Waiting for reply.";
            case stReceivedReply:
                return "Response arrived.";
            case stAborted:
                return "Aborted by user.";
            case stTimeout:
                return "Timeout Occured.";
            case stInternalError:
                return "Internal error.";
            case stResultsAvailable:
                return "Results available";
            case stNeverUsed:
                return "Inform request in createAndWait state";
        }
        return "Unknown inform request state.";
    }
}
