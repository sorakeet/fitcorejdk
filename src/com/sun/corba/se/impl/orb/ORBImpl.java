/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orb;

import com.sun.corba.se.impl.copyobject.CopierManagerImpl;
import com.sun.corba.se.impl.corba.*;
import com.sun.corba.se.impl.encoding.CachedCodeBase;
import com.sun.corba.se.impl.interceptors.PIHandlerImpl;
import com.sun.corba.se.impl.interceptors.PINoOpHandlerImpl;
import com.sun.corba.se.impl.ior.TaggedComponentFactoryFinderImpl;
import com.sun.corba.se.impl.ior.TaggedProfileFactoryFinderImpl;
import com.sun.corba.se.impl.ior.TaggedProfileTemplateFactoryFinderImpl;
import com.sun.corba.se.impl.legacy.connection.LegacyServerSocketManagerImpl;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.se.impl.oa.poa.POAFactory;
import com.sun.corba.se.impl.oa.toa.TOAFactory;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.StackImpl;
import com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolManagerImpl;
import com.sun.corba.se.impl.protocol.CorbaInvocationInfo;
import com.sun.corba.se.impl.protocol.RequestDispatcherRegistryImpl;
import com.sun.corba.se.impl.transport.CorbaTransportManagerImpl;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.pept.protocol.ClientInvocationInfo;
import com.sun.corba.se.pept.transport.TransportManager;
import com.sun.corba.se.spi.copyobject.CopierManager;
import com.sun.corba.se.spi.ior.*;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager;
import com.sun.corba.se.spi.oa.OAInvocationInfo;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.orb.*;
import com.sun.corba.se.spi.orbutil.closure.ClosureFactory;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter;
import com.sun.corba.se.spi.protocol.ClientDelegateFactory;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import com.sun.corba.se.spi.protocol.PIHandler;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.se.spi.resolver.LocalResolver;
import com.sun.corba.se.spi.resolver.Resolver;
import com.sun.corba.se.spi.servicecontext.ServiceContextRegistry;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.org.omg.SendingContext.CodeBase;
import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.ValueFactory;

import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.ValueHandler;
import java.applet.Applet;
import java.io.IOException;
import java.lang.Object;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.*;

public class ORBImpl extends com.sun.corba.se.spi.orb.ORB{
    private static final byte STATUS_OPERATING=1;
    private static final byte STATUS_SHUTTING_DOWN=2;
    private static final byte STATUS_SHUTDOWN=3;
    private static final byte STATUS_DESTROYED=4;
    // pure java orb, caching the servant IOR per ORB
    private static IOR codeBaseIOR;
    private static String localHostString=null;
    private final Object urlOperationLock=new Object();
    // resolverLock must be used for all access to either resolver or
    // localResolver, since it is possible for the resolver to indirectly
    // refer to the localResolver.  Also used to protect access to
    // insNamingDelegate.
    private final Object resolverLock=new Object();
    protected TransportManager transportManager;
    protected LegacyServerSocketManager legacyServerSocketManager;
    private ThreadLocal OAInvocationInfoStack;
    private ThreadLocal clientInvocationInfoStack;
    // Vector holding deferred Requests
    private Vector dynamicRequests;
    private SynchVariable svResponseReceived;
    private Object runObj=new Object();
    private Object shutdownObj=new Object();
    private Object waitForCompletionObj=new Object();
    private byte status=STATUS_OPERATING;
    // XXX Should we move invocation tracking to the first level server dispatcher?
    private Object invocationObj=new Object();
    private int numInvocations=0;
    // thread local variable to store a boolean to detect deadlock in
    // ORB.shutdown(true).
    private ThreadLocal isProcessingInvocation=new ThreadLocal(){
        protected Object initialValue(){
            return Boolean.FALSE;
        }
    };
    // This map is caching TypeCodes created for a certain class (key)
    // and is used in Util.writeAny()
    private Map typeCodeForClassMap;
    // Cache to hold ValueFactories (Helper classes) keyed on repository ids
    private Hashtable valueFactoryCache=new Hashtable();
    // thread local variable to store the current ORB version.
    // default ORB version is the version of ORB with correct Rep-id
    // changes
    private ThreadLocal orbVersionThreadLocal;
    private RequestDispatcherRegistry requestDispatcherRegistry;
    private CopierManager copierManager;
    private int transientServerId;
    private ServiceContextRegistry serviceContextRegistry;
    // Needed here to implement connect/disconnect
    private TOAFactory toaFactory;
    // Needed here for set_delegate
    private POAFactory poaFactory;
    // The interceptor handler, which provides portable interceptor services for
    // subcontracts and object adapters.
    private PIHandler pihandler;
    private ORBData configData;
    private BadServerIdHandler badServerIdHandler;
    private ClientDelegateFactory clientDelegateFactory;
    private CorbaContactInfoListFactory corbaContactInfoListFactory;
    // All access to resolver, localResolver, and urlOperation must be protected using
    // resolverLock.  Do not hold the ORBImpl lock while accessing
    // resolver, or deadlocks may occur.
    // Note that we now have separate locks for each resolver type.  This is due
    // to bug 6980681 and 6238477, which was caused by a deadlock while resolving a
    // corbaname: URL that contained a reference to the same ORB as the
    // ORB making the call to string_to_object.  This caused a deadlock between the
    // client thread holding the single lock for access to the urlOperation,
    // and the server thread handling the client is_a request waiting on the
    // same lock to access the localResolver.
    // Used for resolver_initial_references and list_initial_services
    private Resolver resolver;
    // Used for register_initial_references
    private LocalResolver localResolver;
    // Converts strings to object references for resolvers and string_to_object
    private Operation urlOperation;
    private CorbaServerRequestDispatcher insNamingDelegate;
    private TaggedComponentFactoryFinder taggedComponentFactoryFinder;
    private IdentifiableFactoryFinder taggedProfileFactoryFinder;
    private IdentifiableFactoryFinder taggedProfileTemplateFactoryFinder;
    private ObjectKeyFactory objectKeyFactory;
    private boolean orbOwnsThreadPoolManager=false;
    private ThreadPoolManager threadpoolMgr;
    ////////////////////////////////////////////////////
    //
    // NOTE:
    //
    // Methods that are synchronized MUST stay synchronized.
    //
    // Methods that are NOT synchronized must stay that way to avoid deadlock.
    //
    //
    // REVISIT:
    //
    // checkShutDownState - lock on different object - and normalize usage.
    // starting/FinishDispatch and Shutdown
    //
    private Object badServerIdHandlerAccessLock=new Object();
    ////////////////////////////////////////////////////
    //
    //
    //
    private Object clientDelegateFactoryAccessorLock=new Object();
    private Object corbaContactInfoListFactoryAccessLock=new Object();
    private Object objectKeyFactoryAccessLock=new Object();
    private Object transportManagerAccessorLock=new Object();
    private Object legacyServerSocketManagerAccessLock=new Object();
    private Object threadPoolManagerAccessLock=new Object();

    public ORBImpl(){
        // All initialization is done through set_parameters().
    }

    private void dprint(String msg){
        ORBUtility.dprint(this,msg);
    }

    protected void set_parameters(String[] params,Properties props){
        preInit(params,props);
        DataCollector dataCollector=
                DataCollectorFactory.create(params,props,getLocalHostName());
        postInit(params,dataCollector);
    }

    protected void set_parameters(Applet app,Properties props){
        preInit(null,props);
        DataCollector dataCollector=
                DataCollectorFactory.create(app,props,getLocalHostName());
        postInit(null,dataCollector);
    }

    public synchronized void connect(org.omg.CORBA.Object servant){
        checkShutdownState();
        if(getTOAFactory()==null)
            throw wrapper.noToa();
        try{
            String codebase=Util.getCodebase(servant.getClass());
            getTOAFactory().getTOA(codebase).connect(servant);
        }catch(Exception ex){
            throw wrapper.orbConnectError(ex);
        }
    }

    private synchronized TOAFactory getTOAFactory(){
        if(toaFactory==null){
            toaFactory=(TOAFactory)requestDispatcherRegistry.getObjectAdapterFactory(
                    ORBConstants.TOA_SCID);
        }
        return toaFactory;
    }

    public synchronized void disconnect(org.omg.CORBA.Object obj){
        checkShutdownState();
        if(getTOAFactory()==null)
            throw wrapper.noToa();
        try{
            getTOAFactory().getTOA().disconnect(obj);
        }catch(Exception ex){
            throw wrapper.orbConnectError(ex);
        }
    }

    public String[] list_initial_services(){
        Resolver res;
        synchronized(this){
            checkShutdownState();
            res=resolver;
        }
        synchronized(resolverLock){
            Set keys=res.list();
            return (String[])keys.toArray(new String[keys.size()]);
        }
    }

    public org.omg.CORBA.Object resolve_initial_references(
            String identifier) throws InvalidName{
        Resolver res;
        synchronized(this){
            checkShutdownState();
            res=resolver;
        }
        synchronized(resolverLock){
            org.omg.CORBA.Object result=res.resolve(identifier);
            if(result==null)
                throw new InvalidName();
            else
                return result;
        }
    }

    public synchronized String object_to_string(org.omg.CORBA.Object obj){
        checkShutdownState();
        // Handle the null objref case
        if(obj==null){
            IOR nullIOR=IORFactories.makeIOR(this);
            return nullIOR.stringify();
        }
        IOR ior=null;
        try{
            ior=ORBUtility.connectAndGetIOR(this,obj);
        }catch(BAD_PARAM bp){
            // Throw MARSHAL instead if this is a LOCAL_OBJECT_NOT_ALLOWED error.
            if(bp.minor==ORBUtilSystemException.LOCAL_OBJECT_NOT_ALLOWED){
                throw omgWrapper.notAnObjectImpl(bp);
            }else
                // Not a local object problem: just rethrow the exception.
                // Do not wrap and log this, since it was already logged at its
                // point of origin.
                throw bp;
        }
        return ior.stringify();
    }

    public org.omg.CORBA.Object string_to_object(String str){
        Operation op;
        synchronized(this){
            checkShutdownState();
            op=urlOperation;
        }
        if(str==null)
            throw wrapper.nullParam();
        synchronized(urlOperationLock){
            org.omg.CORBA.Object obj=(org.omg.CORBA.Object)op.operate(str);
            return obj;
        }
    }

    public synchronized NVList create_list(int count){
        checkShutdownState();
        return new NVListImpl(this,count);
    }

    public synchronized NVList create_operation_list(org.omg.CORBA.Object oper){
        checkShutdownState();
        throw wrapper.genericNoImpl();
    }

    public synchronized NamedValue create_named_value(String s,Any any,int flags){
        checkShutdownState();
        return new NamedValueImpl(this,s,any,flags);
    }

    public synchronized ExceptionList create_exception_list(){
        checkShutdownState();
        return new ExceptionListImpl();
    }

    public synchronized ContextList create_context_list(){
        checkShutdownState();
        return new ContextListImpl(this);
    }

    public synchronized Context get_default_context(){
        checkShutdownState();
        throw wrapper.genericNoImpl();
    }

    public synchronized Environment create_environment(){
        checkShutdownState();
        return new EnvironmentImpl();
    }

    public synchronized org.omg.CORBA.portable.OutputStream create_output_stream(){
        checkShutdownState();
        return sun.corba.OutputStreamFactory.newEncapsOutputStream(this);
    }

    public synchronized void send_multiple_requests_oneway(Request[] req){
        checkShutdownState();
        // Invoke the send_oneway on each new Request
        for(int i=0;i<req.length;i++){
            req[i].send_oneway();
        }
    }

    public synchronized void send_multiple_requests_deferred(Request[] req){
        checkShutdownState();
        // add the new Requests to pending dynamic Requests
        for(int i=0;i<req.length;i++){
            dynamicRequests.addElement(req[i]);
        }
        // Invoke the send_deferred on each new Request
        for(int i=0;i<req.length;i++){
            AsynchInvoke invokeObject=new AsynchInvoke(this,
                    (RequestImpl)req[i],true);
            new Thread(invokeObject).start();
        }
    }

    public synchronized boolean poll_next_response(){
        checkShutdownState();
        Request currRequest;
        // poll on each pending request
        Enumeration ve=dynamicRequests.elements();
        while(ve.hasMoreElements()==true){
            currRequest=(Request)ve.nextElement();
            if(currRequest.poll_response()==true){
                return true;
            }
        }
        return false;
    }

    public Request get_next_response()
            throws org.omg.CORBA.WrongTransaction{
        synchronized(this){
            checkShutdownState();
        }
        while(true){
            // check if there already is a response
            synchronized(dynamicRequests){
                Enumeration elems=dynamicRequests.elements();
                while(elems.hasMoreElements()){
                    Request currRequest=(Request)elems.nextElement();
                    if(currRequest.poll_response()){
                        // get the response for this successfully polled Request
                        currRequest.get_response();
                        dynamicRequests.removeElement(currRequest);
                        return currRequest;
                    }
                }
            }
            // wait for a response
            synchronized(this.svResponseReceived){
                while(!this.svResponseReceived.value()){
                    try{
                        this.svResponseReceived.wait();
                    }catch(InterruptedException ex){
                        // NO-OP
                    }
                }
                // reinitialize the response flag
                this.svResponseReceived.reset();
            }
        }
    }

    public synchronized TypeCode get_primitive_tc(TCKind tcKind){
        checkShutdownState();
        return get_primitive_tc(tcKind.value());
    }

    public synchronized TypeCode create_struct_tc(String id,
                                                  String name,
                                                  StructMember[] members){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_struct,id,name,members);
    }

    public synchronized TypeCode create_union_tc(String id,
                                                 String name,
                                                 TypeCode discriminator_type,
                                                 UnionMember[] members){
        checkShutdownState();
        return new TypeCodeImpl(this,
                TCKind._tk_union,
                id,
                name,
                discriminator_type,
                members);
    }

    public synchronized TypeCode create_enum_tc(String id,
                                                String name,
                                                String[] members){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_enum,id,name,members);
    }

    public synchronized TypeCode create_alias_tc(String id,
                                                 String name,
                                                 TypeCode original_type){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_alias,id,name,original_type);
    }

    public synchronized TypeCode create_exception_tc(String id,
                                                     String name,
                                                     StructMember[] members){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_except,id,name,members);
    }

    public synchronized TypeCode create_interface_tc(String id,
                                                     String name){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_objref,id,name);
    }

    public synchronized TypeCode create_string_tc(int bound){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_string,bound);
    }

    public synchronized TypeCode create_wstring_tc(int bound){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_wstring,bound);
    }

    public synchronized TypeCode create_sequence_tc(int bound,
                                                    TypeCode element_type){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_sequence,bound,element_type);
    }

    public synchronized TypeCode create_recursive_sequence_tc(int bound,
                                                              int offset){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_sequence,bound,offset);
    }

    public synchronized TypeCode create_array_tc(int length,
                                                 TypeCode element_type){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_array,length,element_type);
    }

    public synchronized TypeCode create_native_tc(String id,
                                                  String name){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_native,id,name);
    }

    public synchronized TypeCode create_abstract_interface_tc(
            String id,
            String name){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_abstract_interface,id,name);
    }

    public synchronized TypeCode create_fixed_tc(short digits,short scale){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_fixed,digits,scale);
    }

    public synchronized TypeCode create_value_tc(String id,
                                                 String name,
                                                 short type_modifier,
                                                 TypeCode concrete_base,
                                                 ValueMember[] members){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_value,id,name,
                type_modifier,concrete_base,members);
    }

    public synchronized TypeCode create_recursive_tc(String id){
        checkShutdownState();
        return new TypeCodeImpl(this,id);
    }

    public synchronized TypeCode create_value_box_tc(String id,
                                                     String name,
                                                     TypeCode boxed_type){
        checkShutdownState();
        return new TypeCodeImpl(this,TCKind._tk_value_box,id,name,
                boxed_type);
    }

    public synchronized Any create_any(){
        checkShutdownState();
        return new AnyImpl(this);
    }

    public synchronized org.omg.CORBA.Current get_current(){
        checkShutdownState();
        /** _REVISIT_
         The implementation of get_current is not clear. How would
         ORB know whether the caller wants a Current for transactions
         or security ?? Or is it assumed that there is just one
         implementation for both ? If Current is thread-specific,
         then it should not be instantiated; so where does the
         ORB get a Current ?

         This should probably be deprecated. */
        throw wrapper.genericNoImpl();
    }
    // TypeCodeFactory interface methods.
    // Keeping track of type codes by repository id.
    // Keeping a cache of TypeCodes associated with the class
    // they got created from in Util.writeAny().

    public void run(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(runObj){
            try{
                runObj.wait();
            }catch(InterruptedException ex){
            }
        }
    }

    public void shutdown(boolean wait_for_completion){
        boolean wait=false;
        synchronized(this){
            checkShutdownState();
            // This is to avoid deadlock: don't allow a thread that is
            // processing a request to call shutdown( true ), because
            // the shutdown would block waiting for the request to complete,
            // while the request would block waiting for shutdown to complete.
            if(wait_for_completion&&
                    isProcessingInvocation.get()==Boolean.TRUE){
                throw omgWrapper.shutdownWaitForCompletionDeadlock();
            }
            if(status==STATUS_SHUTTING_DOWN){
                if(wait_for_completion){
                    wait=true;
                }else{
                    return;
                }
            }
            status=STATUS_SHUTTING_DOWN;
        }
        // Avoid more than one thread performing shutdown at a time.
        synchronized(shutdownObj){
            // At this point, the ORB status is certainly STATUS_SHUTTING_DOWN.
            // If wait is true, another thread already called shutdown( true ),
            // and so we wait for completion
            if(wait){
                while(true){
                    synchronized(this){
                        if(status==STATUS_SHUTDOWN)
                            break;
                    }
                    try{
                        shutdownObj.wait();
                    }catch(InterruptedException exc){
                        // NOP: just loop and wait until state is changed
                    }
                }
            }else{
                // perform the actual shutdown
                shutdownServants(wait_for_completion);
                if(wait_for_completion){
                    synchronized(waitForCompletionObj){
                        while(numInvocations>0){
                            try{
                                waitForCompletionObj.wait();
                            }catch(InterruptedException ex){
                            }
                        }
                    }
                }
                synchronized(runObj){
                    runObj.notifyAll();
                }
                status=STATUS_SHUTDOWN;
                shutdownObj.notifyAll();
            }
        }
    }

    // Cause all ObjectAdapaterFactories to clean up all of their internal state, which
    // may include activated objects that have associated state and callbacks that must
    // complete in order to shutdown.  This will cause new request to be rejected.
    protected void shutdownServants(boolean wait_for_completion){
        Set<ObjectAdapterFactory> oaset;
        synchronized(this){
            oaset=new HashSet<>(requestDispatcherRegistry.getObjectAdapterFactories());
        }
        for(ObjectAdapterFactory oaf : oaset)
            oaf.shutdown(wait_for_completion);
    }

    public synchronized boolean work_pending(){
        checkShutdownState();
        throw wrapper.genericNoImpl();
    }

    public synchronized void perform_work(){
        checkShutdownState();
        throw wrapper.genericNoImpl();
    }

    public synchronized org.omg.CORBA.Policy create_policy(int type,
                                                           Any val) throws org.omg.CORBA.PolicyError{
        checkShutdownState();
        return pihandler.create_policy(type,val);
    }

    public synchronized void setTypeCodeForClass(Class c,TypeCodeImpl tci){
        checkShutdownState();
        if(typeCodeForClassMap==null)
            typeCodeForClassMap=Collections.synchronizedMap(
                    new WeakHashMap(64));
        // Store only one TypeCode per class.
        if(!typeCodeForClassMap.containsKey(c))
            typeCodeForClassMap.put(c,tci);
    }

    public synchronized TypeCodeImpl getTypeCodeForClass(Class c){
        checkShutdownState();
        if(typeCodeForClassMap==null)
            return null;
        return (TypeCodeImpl)typeCodeForClassMap.get(c);
    }

    public void register_initial_reference(
            String id,org.omg.CORBA.Object obj) throws InvalidName{
        CorbaServerRequestDispatcher insnd;
        synchronized(this){
            checkShutdownState();
        }
        if((id==null)||(id.length()==0))
            throw new InvalidName();
        synchronized(this){
            checkShutdownState();
        }
        synchronized(resolverLock){
            insnd=insNamingDelegate;
            Object obj2=localResolver.resolve(id);
            if(obj2!=null)
                throw new InvalidName(id+" already registered");
            localResolver.register(id,ClosureFactory.makeConstant(obj));
        }
        synchronized(this){
            if(StubAdapter.isStub(obj))
                // Make all remote object references available for INS.
                requestDispatcherRegistry.registerServerRequestDispatcher(
                        insnd,id);
        }
    }

    public synchronized ValueFactory register_value_factory(String repositoryID,
                                                            ValueFactory factory){
        checkShutdownState();
        if((repositoryID==null)||(factory==null))
            throw omgWrapper.unableRegisterValueFactory();
        return (ValueFactory)valueFactoryCache.put(repositoryID,factory);
    }

    public synchronized void unregister_value_factory(String repositoryID){
        checkShutdownState();
        if(valueFactoryCache.remove(repositoryID)==null)
            throw wrapper.nullParam();
    }

    public synchronized ValueFactory lookup_value_factory(String repositoryID){
        checkShutdownState();
        ValueFactory factory=
                (ValueFactory)valueFactoryCache.get(repositoryID);
        if(factory==null){
            try{
                factory=Utility.getFactory(null,null,null,repositoryID);
            }catch(MARSHAL ex){
                throw wrapper.unableFindValueFactory(ex);
            }
        }
        return factory;
    }

    public synchronized void set_delegate(Object servant){
        checkShutdownState();
        POAFactory poaFactory=getPOAFactory();
        if(poaFactory!=null)
            ((org.omg.PortableServer.Servant)servant)
                    ._set_delegate(poaFactory.getDelegateImpl());
        else
            throw wrapper.noPoa();
    }

    private synchronized POAFactory getPOAFactory(){
        if(poaFactory==null){
            poaFactory=(POAFactory)requestDispatcherRegistry.getObjectAdapterFactory(
                    ORBConstants.TRANSIENT_SCID);
        }
        return poaFactory;
    }

    // XXX What about multi-homed host?
    public boolean isLocalHost(String hostName){
        synchronized(this){
            checkShutdownState();
        }
        return hostName.equals(configData.getORBServerHost())||
                hostName.equals(getLocalHostName());
    }

    public boolean isLocalServerId(int subcontractId,int serverId){
        synchronized(this){
            checkShutdownState();
        }
        if((subcontractId<ORBConstants.FIRST_POA_SCID)||
                (subcontractId>ORBConstants.MAX_POA_SCID))
            return serverId==getTransientServerId();
        // XXX isTransient info should be stored in subcontract registry
        if(ORBConstants.isTransient(subcontractId))
            return (serverId==getTransientServerId());
        else if(configData.getPersistentServerIdInitialized())
            return (serverId==configData.getPersistentServerId());
        else
            return false;
    }

    public OAInvocationInfo peekInvocationInfo(){
        synchronized(this){
            checkShutdownState();
        }
        StackImpl stack=(StackImpl)(OAInvocationInfoStack.get());
        return (OAInvocationInfo)(stack.peek());
    }

    public void pushInvocationInfo(OAInvocationInfo info){
        synchronized(this){
            checkShutdownState();
        }
        StackImpl stack=(StackImpl)(OAInvocationInfoStack.get());
        stack.push(info);
    }

    public OAInvocationInfo popInvocationInfo(){
        synchronized(this){
            checkShutdownState();
        }
        StackImpl stack=(StackImpl)(OAInvocationInfoStack.get());
        return (OAInvocationInfo)(stack.pop());
    }

    public CorbaTransportManager getCorbaTransportManager(){
        return (CorbaTransportManager)getTransportManager();
    }

    public LegacyServerSocketManager getLegacyServerSocketManager(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(legacyServerSocketManagerAccessLock){
            if(legacyServerSocketManager==null){
                legacyServerSocketManager=new LegacyServerSocketManagerImpl(this);
            }
            return legacyServerSocketManager;
        }
    }

    public void destroy(){
        boolean shutdownFirst=false;
        synchronized(this){
            shutdownFirst=(status==STATUS_OPERATING);
        }
        if(shutdownFirst){
            shutdown(true);
        }
        synchronized(this){
            if(status<STATUS_DESTROYED){
                getCorbaTransportManager().close();
                getPIHandler().destroyInterceptors();
                status=STATUS_DESTROYED;
            }
        }
        synchronized(threadPoolManagerAccessLock){
            if(orbOwnsThreadPoolManager){
                try{
                    threadpoolMgr.close();
                    threadpoolMgr=null;
                }catch(IOException exc){
                    wrapper.ioExceptionOnClose(exc);
                }
            }
        }
        try{
            monitoringManager.close();
            monitoringManager=null;
        }catch(IOException exc){
            wrapper.ioExceptionOnClose(exc);
        }
        CachedCodeBase.cleanCache(this);
        try{
            pihandler.close();
        }catch(IOException exc){
            wrapper.ioExceptionOnClose(exc);
        }
        super.destroy();
        badServerIdHandlerAccessLock=null;
        clientDelegateFactoryAccessorLock=null;
        corbaContactInfoListFactoryAccessLock=null;
        objectKeyFactoryAccessLock=null;
        legacyServerSocketManagerAccessLock=null;
        threadPoolManagerAccessLock=null;
        transportManager=null;
        legacyServerSocketManager=null;
        OAInvocationInfoStack=null;
        clientInvocationInfoStack=null;
        codeBaseIOR=null;
        dynamicRequests=null;
        svResponseReceived=null;
        runObj=null;
        shutdownObj=null;
        waitForCompletionObj=null;
        invocationObj=null;
        isProcessingInvocation=null;
        typeCodeForClassMap=null;
        valueFactoryCache=null;
        orbVersionThreadLocal=null;
        requestDispatcherRegistry=null;
        copierManager=null;
        toaFactory=null;
        poaFactory=null;
        pihandler=null;
        configData=null;
        badServerIdHandler=null;
        clientDelegateFactory=null;
        corbaContactInfoListFactory=null;
        resolver=null;
        localResolver=null;
        insNamingDelegate=null;
        urlOperation=null;
        taggedComponentFactoryFinder=null;
        taggedProfileFactoryFinder=null;
        taggedProfileTemplateFactoryFinder=null;
        objectKeyFactory=null;
    }

    public void set_parameters(Properties props){
        synchronized(this){
            checkShutdownState();
        }
        preInit(null,props);
        DataCollector dataCollector=
                DataCollectorFactory.create(props,getLocalHostName());
        postInit(null,dataCollector);
    }

    public ORBVersion getORBVersion(){
        synchronized(this){
            checkShutdownState();
        }
        return (ORBVersion)(orbVersionThreadLocal.get());
    }

    public void setORBVersion(ORBVersion verObj){
        synchronized(this){
            checkShutdownState();
        }
        orbVersionThreadLocal.set(verObj);
    }

    // pure java orb support, moved this method from FVDCodeBaseImpl.
    // Note that we connect this if we have not already done so.
    public synchronized IOR getFVDCodeBaseIOR(){
        checkShutdownState();
        if(codeBaseIOR!=null) // i.e. We are already connected to it
            return codeBaseIOR;
        // backward compatability 4365188
        CodeBase cb;
        ValueHandler vh=ORBUtility.createValueHandler();
        cb=(CodeBase)vh.getRunTimeCodeBase();
        return ORBUtility.connectAndGetIOR(this,cb);
    }

    public void handleBadServerId(ObjectKey okey){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(badServerIdHandlerAccessLock){
            if(badServerIdHandler==null)
                throw wrapper.badServerId();
            else
                badServerIdHandler.handle(okey);
        }
    }

    public void setBadServerIdHandler(BadServerIdHandler handler){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(badServerIdHandlerAccessLock){
            badServerIdHandler=handler;
        }
    }

    public void initBadServerIdHandler(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(badServerIdHandlerAccessLock){
            Class cls=configData.getBadServerIdHandler();
            if(cls!=null){
                try{
                    Class[] params=new Class[]{org.omg.CORBA.ORB.class};
                    Object[] args=new Object[]{this};
                    Constructor cons=cls.getConstructor(params);
                    badServerIdHandler=
                            (BadServerIdHandler)cons.newInstance(args);
                }catch(Exception e){
                    throw wrapper.errorInitBadserveridhandler(e);
                }
            }
        }
    }
    // XXX All of the isLocalXXX checking needs to be revisited.
    // First of all, all three of these methods are called from
    // only one place in impl.ior.IORImpl.  Second, we have problems
    // both with multi-homed hosts and with multi-profile IORs.
    // A possible strategy: like the LocalClientRequestDispatcher, we need
    // to determine this more abstractly at the ContactInfo level.
    // This level should probably just get the CorbaContactInfoList from
    // the IOR, then iterator over ContactInfo.  If any ContactInfo is
    // local, the IOR is local, and we can pick one to create the
    // LocalClientRequestDispatcher as well.  Bottom line: this code needs to move.

    public void notifyORB(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(this.svResponseReceived){
            this.svResponseReceived.set();
            this.svResponseReceived.notify();
        }
    }

    public PIHandler getPIHandler(){
        return pihandler;
    }

    // Note that the caller must hold the ORBImpl lock.
    public void checkShutdownState(){
        if(status==STATUS_DESTROYED){
            throw wrapper.orbDestroyed();
        }
        if(status==STATUS_SHUTDOWN){
            throw omgWrapper.badOperationAfterShutdown();
        }
    }

    public boolean isDuringDispatch(){
        synchronized(this){
            checkShutdownState();
        }
        Boolean value=(Boolean)(isProcessingInvocation.get());
        return value.booleanValue();
    }

    public void startingDispatch(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(invocationObj){
            isProcessingInvocation.set(Boolean.TRUE);
            numInvocations++;
        }
    }

    public void finishedDispatch(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(invocationObj){
            numInvocations--;
            isProcessingInvocation.set(false);
            if(numInvocations==0){
                synchronized(waitForCompletionObj){
                    waitForCompletionObj.notifyAll();
                }
            }else if(numInvocations<0){
                throw wrapper.numInvocationsAlreadyZero(
                        CompletionStatus.COMPLETED_YES);
            }
        }
    }

    public int getTransientServerId(){
        synchronized(this){
            checkShutdownState();
        }
        if(configData.getORBServerIdPropertySpecified()){
            // ORBServerId is specified then use that value
            return configData.getPersistentServerId();
        }
        return transientServerId;
    }

    public ServiceContextRegistry getServiceContextRegistry(){
        synchronized(this){
            checkShutdownState();
        }
        return serviceContextRegistry;
    }
    ////////////////////////////////////////////////////
    //
    // pept.broker.Broker
    //

    public RequestDispatcherRegistry getRequestDispatcherRegistry(){
        synchronized(this){
            checkShutdownState();
        }
        return requestDispatcherRegistry;
    }

    public ORBData getORBData(){
        return configData;
    }

    // preInit initializes all non-pluggable ORB data that is independent
    // of the property parsing.
    private void preInit(String[] params,Properties props){
        // Before ORBConfiguration we need to set a PINoOpHandlerImpl,
        // because PersisentServer Initialization inside configurator will
        // invoke orb.resolve_initial_references( ) which will result in a
        // check on piHandler to invoke Interceptors. We do not want any
        // Interceptors to be invoked before the complete ORB initialization.
        // piHandler will be replaced by a real PIHandler implementation at the
        // end of this method.
        pihandler=new PINoOpHandlerImpl();
        // This is the unique id of this server (JVM). Multiple incarnations
        // of this server will get different ids.
        // Compute transientServerId = milliseconds since Jan 1, 1970
        // Note: transientServerId will wrap in about 2^32 / 86400000 = 49.7 days.
        // If two ORBS are started at the same time then there is a possibility
        // of having the same transientServerId. This may result in collision
        // and may be a problem in ior.isLocal() check to see if the object
        // belongs to the current ORB. This problem is taken care of by checking
        // to see if the IOR port matches ORB server port in legacyIsLocalServerPort()
        // method.
        //
        // XXX need to move server ID to a string for CORBA 3.0.  At that point,
        // make this more unique (possibly use java.rmi.server.UID).
        transientServerId=(int)System.currentTimeMillis();
        orbVersionThreadLocal=new ThreadLocal(){
            protected Object initialValue(){
                // set default to version of the ORB with correct Rep-ids
                return ORBVersionFactory.getORBVersion();
            }
        };
        requestDispatcherRegistry=new RequestDispatcherRegistryImpl(
                this,ORBConstants.DEFAULT_SCID);
        copierManager=new CopierManagerImpl(this);
        taggedComponentFactoryFinder=
                new TaggedComponentFactoryFinderImpl(this);
        taggedProfileFactoryFinder=
                new TaggedProfileFactoryFinderImpl(this);
        taggedProfileTemplateFactoryFinder=
                new TaggedProfileTemplateFactoryFinderImpl(this);
        dynamicRequests=new Vector();
        svResponseReceived=new SynchVariable();
        OAInvocationInfoStack=
                new ThreadLocal(){
                    protected Object initialValue(){
                        return new StackImpl();
                    }
                };
        clientInvocationInfoStack=
                new ThreadLocal(){
                    protected Object initialValue(){
                        return new StackImpl();
                    }
                };
        serviceContextRegistry=new ServiceContextRegistry(this);
    }

    private void postInit(String[] params,DataCollector dataCollector){
        // First, create the standard ORB config data.
        // This must be initialized before the ORBConfigurator
        // is executed.
        configData=new ORBDataParserImpl(this,dataCollector);
        // Set the debug flags early so they can be used by other
        // parts of the initialization.
        setDebugFlags(configData.getORBDebugFlags());
        // REVISIT: this should go away after more transport init cleanup
        // and going to ORT based ORBD.
        getTransportManager();
        getLegacyServerSocketManager();
        // Create a parser to get the configured ORBConfigurator.
        ConfigParser parser=new ConfigParser();
        parser.init(dataCollector);
        ORBConfigurator configurator=null;
        try{
            configurator=
                    (ORBConfigurator)(parser.configurator.newInstance());
        }catch(Exception iexc){
            throw wrapper.badOrbConfigurator(iexc,parser.configurator.getName());
        }
        // Finally, run the configurator.  Note that the default implementation allows
        // other configurators with their own parsers to run,
        // using the same DataCollector.
        try{
            configurator.configure(dataCollector,this);
        }catch(Exception exc){
            throw wrapper.orbConfiguratorError(exc);
        }
        // Last of all, create the PIHandler and run the ORB initializers.
        pihandler=new PIHandlerImpl(this,params);
        pihandler.initialize();
        // Initialize the thread manager pool and byte buffer pool
        // so they may be initialized & accessed without synchronization
        getThreadPoolManager();
        super.getByteBufferPool();
    }

    protected void setDebugFlags(String[] args){
        for(int ctr=0;ctr<args.length;ctr++){
            String token=args[ctr];
            // If there is a public boolean data member in this class
            // named token + "DebugFlag", set it to true.
            try{
                Field fld=this.getClass().getField(token+"DebugFlag");
                int mod=fld.getModifiers();
                if(Modifier.isPublic(mod)&&!Modifier.isStatic(mod))
                    if(fld.getType()==boolean.class)
                        fld.setBoolean(this,true);
            }catch(Exception exc){
                // ignore it XXX log this as info
            }
        }
    }    public void setClientDelegateFactory(ClientDelegateFactory factory){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(clientDelegateFactoryAccessorLock){
            clientDelegateFactory=factory;
        }
    }

    private synchronized String getLocalHostName(){
        if(localHostString==null){
            try{
                localHostString=InetAddress.getLocalHost().getHostAddress();
            }catch(Exception ex){
                throw wrapper.getLocalHostFailed(ex);
            }
        }
        return localHostString;
    }    public ClientDelegateFactory getClientDelegateFactory(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(clientDelegateFactoryAccessorLock){
            return clientDelegateFactory;
        }
    }

    private String getHostName(String host)
            throws java.net.UnknownHostException{
        return InetAddress.getByName(host).getHostAddress();
    }

    public ClientInvocationInfo createOrIncrementInvocationInfo(){
        synchronized(this){
            checkShutdownState();
        }
        StackImpl invocationInfoStack=
                (StackImpl)clientInvocationInfoStack.get();
        ClientInvocationInfo clientInvocationInfo=null;
        if(!invocationInfoStack.empty()){
            clientInvocationInfo=
                    (ClientInvocationInfo)invocationInfoStack.peek();
        }
        if((clientInvocationInfo==null)||
                (!clientInvocationInfo.isRetryInvocation())){
            // This is a new call - not a retry.
            clientInvocationInfo=new CorbaInvocationInfo(this);
            startingDispatch();
            invocationInfoStack.push(clientInvocationInfo);
        }
        // Reset retry so recursive calls will get a new info object.
        clientInvocationInfo.setIsRetryInvocation(false);
        clientInvocationInfo.incrementEntryCount();
        return clientInvocationInfo;
    }    public void setCorbaContactInfoListFactory(CorbaContactInfoListFactory factory){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(corbaContactInfoListFactoryAccessLock){
            corbaContactInfoListFactory=factory;
        }
    }

    public ClientInvocationInfo getInvocationInfo(){
        synchronized(this){
            checkShutdownState();
        }
        StackImpl invocationInfoStack=
                (StackImpl)clientInvocationInfoStack.get();
        return (ClientInvocationInfo)invocationInfoStack.peek();
    }    public synchronized CorbaContactInfoListFactory getCorbaContactInfoListFactory(){
        checkShutdownState();
        return corbaContactInfoListFactory;
    }

    public void releaseOrDecrementInvocationInfo(){
        synchronized(this){
            checkShutdownState();
        }
        int entryCount=-1;
        ClientInvocationInfo clientInvocationInfo=null;
        StackImpl invocationInfoStack=
                (StackImpl)clientInvocationInfoStack.get();
        if(!invocationInfoStack.empty()){
            clientInvocationInfo=
                    (ClientInvocationInfo)invocationInfoStack.peek();
        }else{
            throw wrapper.invocationInfoStackEmpty();
        }
        clientInvocationInfo.decrementEntryCount();
        entryCount=clientInvocationInfo.getEntryCount();
        if(clientInvocationInfo.getEntryCount()==0){
            // 6763340: don't pop if this is a retry!
            if(!clientInvocationInfo.isRetryInvocation()){
                invocationInfoStack.pop();
            }
            finishedDispatch();
        }
    }    public void setResolver(Resolver resolver){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(resolverLock){
            this.resolver=resolver;
        }
    }

    public TransportManager getTransportManager(){
        synchronized(transportManagerAccessorLock){
            if(transportManager==null){
                transportManager=new CorbaTransportManagerImpl(this);
            }
            return transportManager;
        }
    }    public Resolver getResolver(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(resolverLock){
            return resolver;
        }
    }

    // Class that defines a parser that gets the name of the
    // ORBConfigurator class.
    private static class ConfigParser extends ParserImplBase{
        // The default here is the ORBConfiguratorImpl that we define,
        // but this can be replaced.
        public Class configurator=ORBConfiguratorImpl.class;

        public PropertyParser makeParser(){
            PropertyParser parser=new PropertyParser();
            parser.add(ORBConstants.SUN_PREFIX+"ORBConfigurator",
                    OperationFactory.classAction(),"configurator");
            return parser;
        }
    }    public void setLocalResolver(LocalResolver resolver){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(resolverLock){
            this.localResolver=resolver;
        }
    }

    public LocalResolver getLocalResolver(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(resolverLock){
            return localResolver;
        }
    }

    public void setURLOperation(Operation stringToObject){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(urlOperationLock){
            urlOperation=stringToObject;
        }
    }

    public Operation getURLOperation(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(urlOperationLock){
            return urlOperation;
        }
    }

    public void setINSDelegate(CorbaServerRequestDispatcher sdel){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(resolverLock){
            insNamingDelegate=sdel;
        }
    }

    public TaggedComponentFactoryFinder getTaggedComponentFactoryFinder(){
        synchronized(this){
            checkShutdownState();
        }
        return taggedComponentFactoryFinder;
    }

    public IdentifiableFactoryFinder getTaggedProfileFactoryFinder(){
        synchronized(this){
            checkShutdownState();
        }
        return taggedProfileFactoryFinder;
    }

    public IdentifiableFactoryFinder getTaggedProfileTemplateFactoryFinder(){
        synchronized(this){
            checkShutdownState();
        }
        return taggedProfileTemplateFactoryFinder;
    }



    public ObjectKeyFactory getObjectKeyFactory(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(objectKeyFactoryAccessLock){
            return objectKeyFactory;
        }
    }

    public void setObjectKeyFactory(ObjectKeyFactory factory){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(objectKeyFactoryAccessLock){
            objectKeyFactory=factory;
        }
    }













    public void setThreadPoolManager(ThreadPoolManager mgr){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(threadPoolManagerAccessLock){
            threadpoolMgr=mgr;
        }
    }

    public ThreadPoolManager getThreadPoolManager(){
        synchronized(this){
            checkShutdownState();
        }
        synchronized(threadPoolManagerAccessLock){
            if(threadpoolMgr==null){
                threadpoolMgr=new ThreadPoolManagerImpl();
                orbOwnsThreadPoolManager=true;
            }
            return threadpoolMgr;
        }
    }

    public CopierManager getCopierManager(){
        synchronized(this){
            checkShutdownState();
        }
        return copierManager;
    }
} // Class ORBImpl
////////////////////////////////////////////////////////////////////////
/// Helper class for a Synchronization Variable
////////////////////////////////////////////////////////////////////////

class SynchVariable{
    // Synchronization Variable
    public boolean _flag;

    // Constructor
    SynchVariable(){
        _flag=false;
    }

    // set Flag to true
    public void set(){
        _flag=true;
    }

    // get value
    public boolean value(){
        return _flag;
    }

    // reset Flag to true
    public void reset(){
        _flag=false;
    }
}
// End of file.
