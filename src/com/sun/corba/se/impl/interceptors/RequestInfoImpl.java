/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.encoding.EncapsOutputStream;
import com.sun.corba.se.impl.logging.InterceptorsSystemException;
import com.sun.corba.se.impl.logging.OMGSystemException;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.util.RepositoryId;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.legacy.connection.Connection;
import com.sun.corba.se.spi.legacy.interceptor.RequestInfoExt;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;
import com.sun.corba.se.spi.servicecontext.UnknownServiceContext;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.Dynamic.Parameter;
import org.omg.IOP.ServiceContextHelper;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.RequestInfo;
import sun.corba.SharedSecrets;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class RequestInfoImpl
        extends LocalObject
        implements RequestInfo, RequestInfoExt{
    // Constant for an uninitizlied reply status.
    protected static final short UNINITIALIZED=-1;
    protected static final int EXECUTION_POINT_STARTING=0;
    protected static final int EXECUTION_POINT_INTERMEDIATE=1;
    protected static final int EXECUTION_POINT_ENDING=2;
    // Method IDs for all methods in RequestInfo.  This allows for a
    // convenient O(1) lookup for checkAccess().
    protected static final int MID_REQUEST_ID=0;
    protected static final int MID_OPERATION=1;
    protected static final int MID_ARGUMENTS=2;
    protected static final int MID_EXCEPTIONS=3;
    protected static final int MID_CONTEXTS=4;
    protected static final int MID_OPERATION_CONTEXT=5;
    protected static final int MID_RESULT=6;
    protected static final int MID_RESPONSE_EXPECTED=7;
    protected static final int MID_SYNC_SCOPE=8;
    protected static final int MID_REPLY_STATUS=9;
    protected static final int MID_FORWARD_REFERENCE=10;
    protected static final int MID_GET_SLOT=11;
    protected static final int MID_GET_REQUEST_SERVICE_CONTEXT=12;
    protected static final int MID_GET_REPLY_SERVICE_CONTEXT=13;
    // The last value from RequestInfo (be sure to update this):
    protected static final int MID_RI_LAST=13;
    //////////////////////////////////////////////////////////////////////
    //
    // NOTE: IF AN ATTRIBUTE IS ADDED, PLEASE UPDATE RESET();
    //
    //////////////////////////////////////////////////////////////////////
    // The ORB from which to get PICurrent and other info
    protected ORB myORB;
    //////////////////////////////////////////////////////////////////////
    //
    // NOTE: IF AN ATTRIBUTE IS ADDED, PLEASE UPDATE RESET();
    //
    //////////////////////////////////////////////////////////////////////
    protected InterceptorsSystemException wrapper;
    protected OMGSystemException stdWrapper;
    // The number of interceptors actually invoked for this client request.
    // See setFlowStackIndex for a detailed description.
    protected int flowStackIndex=0;
    // The type of starting point call to make to the interceptors
    // See ClientRequestInfoImpl and ServerRequestInfoImpl for a list of
    // appropriate constants.
    protected int startingPointCall;
    // The type of intermediate point call to make to the interceptors
    // See ServerRequestInfoImpl for a list of appropriate constants.
    // This does not currently apply to client request interceptors but is
    // here in case intermediate points are introduced in the future.
    protected int intermediatePointCall;
    // The type of ending point call to make to the interceptors
    // See ClientRequestInfoImpl and ServerRequestInfoImpl for a list of
    // appropriate constants.
    protected int endingPointCall;
    // The reply status to return in reply_status.  This is initialized
    // to UNINITIALIZED so that we can tell if this has been set or not.
    protected short replyStatus=UNINITIALIZED;
    // Which points we are currently executing (so we can implement the
    // validity table).
    protected int currentExecutionPoint;
    // Set to true if all interceptors have had all their points
    // executed.
    protected boolean alreadyExecuted;
    // Sources of request information
    protected Connection connection;
    protected ServiceContexts serviceContexts;
    // The ForwardRequest object if this request is being forwarded.
    // Either the forwardRequest or the forwardRequestIOR field is set.
    // When set, the other field is set to null initially.  If the other
    // field is queried, it is lazily calculated and cached.  These
    // two attributes are always kept in sync.
    protected ForwardRequest forwardRequest;
    protected IOR forwardRequestIOR;
    // PICurrent's  SlotTable
    protected SlotTable slotTable;
    // The exception to be returned by received_exception and
    // received_exception_id
    protected Exception exception;
    public RequestInfoImpl(ORB myORB){
        super();
        this.myORB=myORB;
        wrapper=InterceptorsSystemException.get(myORB,
                CORBALogDomains.RPC_PROTOCOL);
        stdWrapper=OMGSystemException.get(myORB,
                CORBALogDomains.RPC_PROTOCOL);
        // Capture the current TSC and make it the RSC of this request.
        PICurrent current=(PICurrent)(myORB.getPIHandler().getPICurrent());
        slotTable=current.getSlotTable();
    }

    void reset(){
        // Please keep these in the same order as declared above.
        flowStackIndex=0;
        startingPointCall=0;
        intermediatePointCall=0;
        endingPointCall=0;
        // 6763340
        setReplyStatus(UNINITIALIZED);
        currentExecutionPoint=EXECUTION_POINT_STARTING;
        alreadyExecuted=false;
        connection=null;
        serviceContexts=null;
        forwardRequest=null;
        forwardRequestIOR=null;
        exception=null;
        // We don't need to reset the Slots because they are
        // already in the clean state after recieve_<point> interceptor
        // are called.
    }

    abstract public int request_id();

    abstract public String operation();

    abstract public Parameter[] arguments();

    abstract public TypeCode[] exceptions();

    abstract public String[] contexts();

    abstract public String[] operation_context();

    abstract public Any result();

    abstract public boolean response_expected();

    public short sync_scope(){
        checkAccess(MID_SYNC_SCOPE);
        return SYNC_WITH_TRANSPORT.value; // REVISIT - get from MessageMediator
    }

    public short reply_status(){
        checkAccess(MID_REPLY_STATUS);
        return replyStatus;
    }

    abstract public Object forward_reference();

    public Any get_slot(int id)
            throws InvalidSlot{
        // access is currently valid for all states:
        //checkAccess( MID_GET_SLOT );
        // Delegate the call to the slotTable which was set when RequestInfo was
        // created.
        return slotTable.get_slot(id);
    }

    abstract public org.omg.IOP.ServiceContext
    get_request_service_context(int id);

    abstract public org.omg.IOP.ServiceContext
    get_reply_service_context(int id);
    // NOTE: When adding a method, be sure to:
    // 1. Add a MID_* constant for that method
    // 2. Call checkAccess at the start of the method
    // 3. Define entries in the validCall[][] table for interception points
    //    in both ClientRequestInfoImpl and ServerRequestInfoImpl.

    protected abstract void checkAccess(int methodID)
            throws BAD_INV_ORDER;

    public Connection connection(){
        return connection;
    }

    protected Parameter[] nvListToParameterArray(NVList parNVList){
        // _REVISIT_ This utility method should probably be doing a deep
        // copy so interceptor can't accidentally change the arguments.
        int count=parNVList.count();
        Parameter[] plist=new Parameter[count];
        try{
            for(int i=0;i<count;i++){
                Parameter p=new Parameter();
                plist[i]=p;
                NamedValue nv=parNVList.item(i);
                plist[i].argument=nv.value();
                // ParameterMode spec can be found in 99-10-07.pdf
                // Section:10.5.22
                // nv.flags spec can be found in 99-10-07.pdf
                // Section 7.1.1
                // nv.flags has ARG_IN as 1, ARG_OUT as 2 and ARG_INOUT as 3
                // To convert this into enum PARAM_IN, PARAM_OUT and
                // PARAM_INOUT the value is subtracted by 1.
                plist[i].mode=ParameterMode.from_int(nv.flags()-1);
            }
        }catch(Exception e){
            throw wrapper.exceptionInArguments(e);
        }
        return plist;
    }

    protected Any exceptionToAny(Exception exception){
        Any result=myORB.create_any();
        if(exception==null){
            // Note: exception should never be null here since we will throw
            // a BAD_INV_ORDER if this is not called from receive_exception.
            throw wrapper.exceptionWasNull2();
        }else if(exception instanceof SystemException){
            ORBUtility.insertSystemException(
                    (SystemException)exception,result);
        }else if(exception instanceof ApplicationException){
            // Use the Helper class for this exception to insert it into an
            // Any.
            try{
                // Insert the user exception inside the application exception
                // into the Any result:
                ApplicationException appException=
                        (ApplicationException)exception;
                insertApplicationException(appException,result);
            }catch(UNKNOWN e){
                // As per ptc/00-08-06, 21.3.13.4. if we cannot find the
                // appropriate class, then return an any containing UNKNOWN,
                // with a minor code of 1.  This is conveniently the same
                // exception that is returned from the
                // insertApplicationException utility method.
                ORBUtility.insertSystemException(e,result);
            }
        }else if(exception instanceof UserException){
            try{
                UserException userException=(UserException)exception;
                insertUserException(userException,result);
            }catch(UNKNOWN e){
                ORBUtility.insertSystemException(e,result);
            }
        }
        return result;
    }

    private void insertApplicationException(ApplicationException appException,
                                            Any result)
            throws UNKNOWN{
        try{
            // Extract the UserException from the ApplicationException.
            // Look up class name from repository id:
            RepositoryId repId=RepositoryId.cache.getId(
                    appException.getId());
            String className=repId.getClassName();
            // Find the read method on the helper class:
            String helperClassName=className+"Helper";
            Class<?> helperClass=
                    SharedSecrets.getJavaCorbaAccess().loadClass(helperClassName);
            Class[] readParams=new Class[1];
            readParams[0]=InputStream.class;
            Method readMethod=helperClass.getMethod("read",readParams);
            // Invoke the read method, passing in the input stream to
            // retrieve the user exception.  Mark and reset the stream
            // as to not disturb it.
            InputStream ueInputStream=appException.getInputStream();
            ueInputStream.mark(0);
            UserException userException=null;
            try{
                java.lang.Object[] readArguments=new java.lang.Object[1];
                readArguments[0]=ueInputStream;
                userException=(UserException)readMethod.invoke(
                        null,readArguments);
            }finally{
                try{
                    ueInputStream.reset();
                }catch(IOException e){
                    throw wrapper.markAndResetFailed(e);
                }
            }
            // Insert this UserException into the provided Any using the
            // helper class.
            insertUserException(userException,result);
        }catch(ClassNotFoundException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(NoSuchMethodException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(SecurityException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(IllegalAccessException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(IllegalArgumentException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(InvocationTargetException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }
    }

    private void insertUserException(UserException userException,Any result)
            throws UNKNOWN{
        try{
            // Insert this UserException into the provided Any using the
            // helper class.
            if(userException!=null){
                Class exceptionClass=userException.getClass();
                String className=exceptionClass.getName();
                String helperClassName=className+"Helper";
                Class<?> helperClass=
                        SharedSecrets.getJavaCorbaAccess().loadClass(helperClassName);
                // Find insert( Any, class ) method
                Class[] insertMethodParams=new Class[2];
                insertMethodParams[0]=Any.class;
                insertMethodParams[1]=exceptionClass;
                Method insertMethod=helperClass.getMethod(
                        "insert",insertMethodParams);
                // Call helper.insert( result, userException ):
                java.lang.Object[] insertMethodArguments=
                        new java.lang.Object[2];
                insertMethodArguments[0]=result;
                insertMethodArguments[1]=userException;
                insertMethod.invoke(null,insertMethodArguments);
            }
        }catch(ClassNotFoundException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(NoSuchMethodException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(SecurityException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(IllegalAccessException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(IllegalArgumentException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }catch(InvocationTargetException e){
            throw stdWrapper.unknownUserException(CompletionStatus.COMPLETED_MAYBE,e);
        }
    }

    protected org.omg.IOP.ServiceContext
    getServiceContext(HashMap cachedServiceContexts,
                      ServiceContexts serviceContexts,int id){
        org.omg.IOP.ServiceContext result=null;
        Integer integerId=new Integer(id);
        // Search cache first:
        result=(org.omg.IOP.ServiceContext)
                cachedServiceContexts.get(integerId);
        // null could normally mean that either we cached the value null
        // or it's not in the cache.  However, there is no way for us to
        // cache the value null in the following code.
        if(result==null){
            // Not in cache.  Find it and put in cache.
            // Get the desired "core" service context.
            com.sun.corba.se.spi.servicecontext.ServiceContext context=
                    serviceContexts.get(id);
            if(context==null)
                throw stdWrapper.invalidServiceContextId();
            // Convert the "core" service context to an
            // "IOP" ServiceContext by writing it to a
            // CDROutputStream and reading it back.
            EncapsOutputStream out=
                    sun.corba.OutputStreamFactory.newEncapsOutputStream(myORB);
            context.write(out,GIOPVersion.V1_2);
            InputStream inputStream=out.create_input_stream();
            result=ServiceContextHelper.read(inputStream);
            cachedServiceContexts.put(integerId,result);
        }
        // Good citizen: For increased efficiency, we assume that interceptors
        // will not modify the returned ServiceContext.  Otherwise, we would
        // have to make a deep copy.
        return result;
    }

    protected void addServiceContext(
            HashMap cachedServiceContexts,
            ServiceContexts serviceContexts,
            org.omg.IOP.ServiceContext service_context,
            boolean replace){
        int id=0;
        // Convert IOP.service_context to core.ServiceContext:
        EncapsOutputStream outputStream=
                sun.corba.OutputStreamFactory.newEncapsOutputStream(myORB);
        InputStream inputStream=null;
        UnknownServiceContext coreServiceContext=null;
        ServiceContextHelper.write(outputStream,service_context);
        inputStream=outputStream.create_input_stream();
        // Constructor expects id to already have been read from stream.
        coreServiceContext=new UnknownServiceContext(
                inputStream.read_long(),
                (org.omg.CORBA_2_3.portable.InputStream)inputStream);
        id=coreServiceContext.getId();
        if(serviceContexts.get(id)!=null)
            if(replace)
                serviceContexts.delete(id);
            else
                throw stdWrapper.serviceContextAddFailed(new Integer(id));
        serviceContexts.put(coreServiceContext);
        // Place IOP.ServiceContext in cache as well:
        cachedServiceContexts.put(new Integer(id),service_context);
    }

    protected int getFlowStackIndex(){
        return this.flowStackIndex;
    }

    protected void setFlowStackIndex(int num){
        this.flowStackIndex=num;
    }

    protected int getEndingPointCall(){
        return this.endingPointCall;
    }

    protected void setEndingPointCall(int call){
        this.endingPointCall=call;
    }

    protected int getIntermediatePointCall(){
        return this.intermediatePointCall;
    }

    protected void setIntermediatePointCall(int call){
        this.intermediatePointCall=call;
    }

    protected int getStartingPointCall(){
        return this.startingPointCall;
    }

    protected void setStartingPointCall(int call){
        this.startingPointCall=call;
    }

    protected boolean getAlreadyExecuted(){
        return this.alreadyExecuted;
    }

    protected void setAlreadyExecuted(boolean alreadyExecuted){
        this.alreadyExecuted=alreadyExecuted;
    }

    protected short getReplyStatus(){
        return this.replyStatus;
    }

    protected void setReplyStatus(short replyStatus){
        this.replyStatus=replyStatus;
    }

    protected void setForwardRequest(ForwardRequest forwardRequest){
        this.forwardRequest=forwardRequest;
        this.forwardRequestIOR=null;
    }

    protected void setForwardRequest(IOR ior){
        this.forwardRequestIOR=ior;
        this.forwardRequest=null;
    }

    protected ForwardRequest getForwardRequestException(){
        if(this.forwardRequest==null){
            if(this.forwardRequestIOR!=null){
                // Convert the internal IOR to a forward request exception
                // by creating an object reference.
                Object obj=iorToObject(this.forwardRequestIOR);
                this.forwardRequest=new ForwardRequest(obj);
            }
        }
        return this.forwardRequest;
    }

    protected Object iorToObject(IOR ior){
        return ORBUtility.makeObjectReference(ior);
    }

    protected IOR getForwardRequestIOR(){
        if(this.forwardRequestIOR==null){
            if(this.forwardRequest!=null){
                this.forwardRequestIOR=ORBUtility.getIOR(
                        this.forwardRequest.forward);
            }
        }
        return this.forwardRequestIOR;
    }

    Exception getException(){
        return this.exception;
    }

    protected void setException(Exception exception){
        this.exception=exception;
    }

    protected void setCurrentExecutionPoint(int executionPoint){
        this.currentExecutionPoint=executionPoint;
    }

    void setSlotTable(SlotTable slotTable){
        this.slotTable=slotTable;
    }
}
