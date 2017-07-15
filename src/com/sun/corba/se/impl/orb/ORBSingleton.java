/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.orb;

import com.sun.corba.se.impl.corba.*;
import com.sun.corba.se.impl.encoding.BufferManagerFactory;
import com.sun.corba.se.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.se.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.pept.protocol.ClientInvocationInfo;
import com.sun.corba.se.pept.transport.ConnectionCache;
import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.corba.se.pept.transport.Selector;
import com.sun.corba.se.pept.transport.TransportManager;
import com.sun.corba.se.spi.copyobject.CopierManager;
import com.sun.corba.se.spi.ior.*;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager;
import com.sun.corba.se.spi.oa.OAInvocationInfo;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.*;
import com.sun.corba.se.spi.orbutil.closure.Closure;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults;
import com.sun.corba.se.spi.presentation.rmi.PresentationManager;
import com.sun.corba.se.spi.protocol.ClientDelegateFactory;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import com.sun.corba.se.spi.protocol.PIHandler;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.se.spi.resolver.LocalResolver;
import com.sun.corba.se.spi.resolver.Resolver;
import com.sun.corba.se.spi.servicecontext.ServiceContextRegistry;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Object;
import org.omg.CORBA.portable.OutputStream;

import java.applet.Applet;
import java.net.URL;
import java.util.Properties;

public class ORBSingleton extends ORB{
    private static PresentationManager.StubFactoryFactory staticStubFactoryFactory=
            PresentationDefaults.getStaticStubFactoryFactory();
    // This is used to support read_Object.
    private ORB fullORB;

    protected void set_parameters(String params[],Properties props){
    }

    protected void set_parameters(Applet app,Properties props){
    }

    public void connect(Object servant){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void disconnect(Object obj){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public String[] list_initial_services(){
        throw wrapper.genericNoImpl();
    }

    public Object resolve_initial_references(String identifier)
            throws InvalidName{
        throw wrapper.genericNoImpl();
    }

    public String object_to_string(Object obj){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public Object string_to_object(String s){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public NVList create_list(int count){
        return new NVListImpl(this,count);
    }

    public NVList
    create_operation_list(Object oper){
        throw wrapper.genericNoImpl();
    }

    public NamedValue
    create_named_value(String s,Any any,int flags){
        return new NamedValueImpl(this,s,any,flags);
    }

    public ExceptionList create_exception_list(){
        return new ExceptionListImpl();
    }

    public ContextList create_context_list(){
        return new ContextListImpl(this);
    }

    public Context get_default_context(){
        throw wrapper.genericNoImpl();
    }

    public Environment create_environment(){
        return new EnvironmentImpl();
    }

    public OutputStream create_output_stream(){
        return sun.corba.OutputStreamFactory.newEncapsOutputStream(this);
    }

    public void send_multiple_requests_oneway(Request[] req){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void send_multiple_requests_deferred(Request[] req){
        throw new SecurityException("ORBSingleton: access denied");
    }
    // orbos 98-01-18: Objects By Value -- begin

    public boolean poll_next_response(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public Request get_next_response(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public TypeCode get_primitive_tc(TCKind tckind){
        return get_primitive_tc(tckind.value());
    }

    public TypeCode create_struct_tc(String id,
                                     String name,
                                     StructMember[] members){
        return new TypeCodeImpl(this,TCKind._tk_struct,id,name,members);
    }

    public TypeCode create_union_tc(String id,
                                    String name,
                                    TypeCode discriminator_type,
                                    UnionMember[] members){
        return new TypeCodeImpl(this,
                TCKind._tk_union,
                id,
                name,
                discriminator_type,
                members);
    }
    // TypeCodeFactory interface methods.
    // Keeping track of type codes by repository id.

    public TypeCode create_enum_tc(String id,
                                   String name,
                                   String[] members){
        return new TypeCodeImpl(this,TCKind._tk_enum,id,name,members);
    }

    public TypeCode create_alias_tc(String id,
                                    String name,
                                    TypeCode original_type){
        return new TypeCodeImpl(this,TCKind._tk_alias,id,name,original_type);
    }

    public TypeCode create_exception_tc(String id,
                                        String name,
                                        StructMember[] members){
        return new TypeCodeImpl(this,TCKind._tk_except,id,name,members);
    }

    public TypeCode create_interface_tc(String id,
                                        String name){
        return new TypeCodeImpl(this,TCKind._tk_objref,id,name);
    }

    public TypeCode create_string_tc(int bound){
        return new TypeCodeImpl(this,TCKind._tk_string,bound);
    }

    public TypeCode create_wstring_tc(int bound){
        return new TypeCodeImpl(this,TCKind._tk_wstring,bound);
    }

    public TypeCode create_sequence_tc(int bound,
                                       TypeCode element_type){
        return new TypeCodeImpl(this,TCKind._tk_sequence,bound,element_type);
    }

    public TypeCode create_recursive_sequence_tc(int bound,
                                                 int offset){
        return new TypeCodeImpl(this,TCKind._tk_sequence,bound,offset);
    }

    public TypeCode create_array_tc(int length,
                                    TypeCode element_type){
        return new TypeCodeImpl(this,TCKind._tk_array,length,element_type);
    }

    public TypeCode create_native_tc(String id,
                                     String name){
        return new TypeCodeImpl(this,TCKind._tk_native,id,name);
    }

    public TypeCode create_abstract_interface_tc(
            String id,
            String name){
        return new TypeCodeImpl(this,TCKind._tk_abstract_interface,id,name);
    }

    public TypeCode create_fixed_tc(short digits,short scale){
        return new TypeCodeImpl(this,TCKind._tk_fixed,digits,scale);
    }

    public TypeCode create_value_tc(String id,
                                    String name,
                                    short type_modifier,
                                    TypeCode concrete_base,
                                    ValueMember[] members){
        return new TypeCodeImpl(this,TCKind._tk_value,id,name,
                type_modifier,concrete_base,members);
    }

    public TypeCode create_recursive_tc(String id){
        return new TypeCodeImpl(this,id);
    }

    public TypeCode create_value_box_tc(String id,
                                        String name,
                                        TypeCode boxed_type){
        return new TypeCodeImpl(this,TCKind._tk_value_box,id,name,boxed_type);
    }

    public Any create_any(){
        return new AnyImpl(this);
    }

    public org.omg.CORBA.Current get_current(){
        throw wrapper.genericNoImpl();
    }

    public void run(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void shutdown(boolean wait_for_completion){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public boolean work_pending(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void perform_work(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public Policy create_policy(int type,Any val) throws PolicyError{
        throw new NO_IMPLEMENT();
    }

    public void register_initial_reference(
            String id,Object obj) throws InvalidName{
        throw wrapper.genericNoImpl();
    }

    public java.rmi.Remote string_to_remote(String s)
            throws java.rmi.RemoteException{
        throw new SecurityException("ORBSingleton: access denied");
    }

    protected void shutdownServants(boolean wait_for_completion){
        throw new SecurityException("ORBSingleton: access denied");
    }

    protected void destroyConnections(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public org.omg.CORBA.portable.ValueFactory register_value_factory(String repositoryID,
                                                                      org.omg.CORBA.portable.ValueFactory factory){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void unregister_value_factory(String repositoryID){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public org.omg.CORBA.portable.ValueFactory lookup_value_factory(String repositoryID){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getORBInitialPort(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public String getORBInitialHost(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public String getORBServerHost(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getORBServerPort(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public CodeSetComponentInfo getCodeSetComponentInfo(){
        return new CodeSetComponentInfo();
    }

    public boolean isLocalHost(String host){
        // To enable read_Object.
        return false;
    }

    public boolean isLocalServerId(int subcontractId,int serverId){
        // To enable read_Object.
        return false;
    }

    public OAInvocationInfo peekInvocationInfo(){
        return null;
    }

    public void pushInvocationInfo(OAInvocationInfo info){
    }

    public OAInvocationInfo popInvocationInfo(){
        return null;
    }

    public CorbaTransportManager getCorbaTransportManager(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public LegacyServerSocketManager getLegacyServerSocketManager(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void destroy(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void set_parameters(Properties props){
    }

    public ORBVersion getORBVersion(){
        // Always use our latest ORB version (latest fixes, etc)
        return ORBVersionFactory.getORBVersion();
    }

    public void setORBVersion(ORBVersion verObj){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public IOR getFVDCodeBaseIOR(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public void handleBadServerId(ObjectKey okey){
    }

    // NOTE: REMOVE THIS METHOD ONCE WE HAVE A ORT BASED ORBD
    public void setBadServerIdHandler(BadServerIdHandler handler){
    }

    // NOTE: REMOVE THIS METHOD ONCE WE HAVE A ORT BASED ORBD
    public void initBadServerIdHandler(){
    }

    public void notifyORB(){
    }

    public PIHandler getPIHandler(){
        return null;
    }

    public void checkShutdownState(){
    }

    public boolean isDuringDispatch(){
        return false;
    }

    public void startingDispatch(){
    }

    public void finishedDispatch(){
    }

    public int getTransientServerId(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public ServiceContextRegistry getServiceContextRegistry(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public RequestDispatcherRegistry getRequestDispatcherRegistry(){
        // To enable read_Object.
        return getFullORB().getRequestDispatcherRegistry();
    }

    private synchronized ORB getFullORB(){
        if(fullORB==null){
            Properties props=new Properties();
            fullORB=new ORBImpl();
            fullORB.set_parameters(props);
        }
        return fullORB;
    }

    public ORBData getORBData(){
        return getFullORB().getORBData();
    }

    public String getAppletHost(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public URL getAppletCodeBase(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getHighWaterMark(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getLowWaterMark(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getNumberToReclaim(){
        throw new SecurityException("ORBSingleton: access denied");
    }

    public int getGIOPFragmentSize(){
        return ORBConstants.GIOP_DEFAULT_BUFFER_SIZE;
    }

    public int getGIOPBuffMgrStrategy(GIOPVersion gv){
        return BufferManagerFactory.GROW;
    }

    public LegacyServerSocketEndPointInfo getServerEndpoint(){
        return null;
    }

    public void setPersistentServerId(int id){
    }    public void setClientDelegateFactory(ClientDelegateFactory factory){
    }

    public void setTypeCodeForClass(Class c,TypeCodeImpl tcimpl){
    }    public ClientDelegateFactory getClientDelegateFactory(){
        return getFullORB().getClientDelegateFactory();
    }

    public TypeCodeImpl getTypeCodeForClass(Class c){
        return null;
    }    public void setCorbaContactInfoListFactory(CorbaContactInfoListFactory factory){
    }

    public boolean alwaysSendCodeSetServiceContext(){
        return true;
    }    public CorbaContactInfoListFactory getCorbaContactInfoListFactory(){
        return getFullORB().getCorbaContactInfoListFactory();
    }

    public void registerInitialReference(String id,Closure closure){
    }    public Operation getURLOperation(){
        return null;
    }

    public ClientInvocationInfo createOrIncrementInvocationInfo(){
        return null;
    }    public void setINSDelegate(CorbaServerRequestDispatcher sdel){
    }

    public ClientInvocationInfo getInvocationInfo(){
        return null;
    }    public TaggedComponentFactoryFinder getTaggedComponentFactoryFinder(){
        return getFullORB().getTaggedComponentFactoryFinder();
    }

    public void releaseOrDecrementInvocationInfo(){
    }    public IdentifiableFactoryFinder getTaggedProfileFactoryFinder(){
        return getFullORB().getTaggedProfileFactoryFinder();
    }

    public TransportManager getTransportManager(){
        throw new SecurityException("ORBSingleton: access denied");
    }    public IdentifiableFactoryFinder getTaggedProfileTemplateFactoryFinder(){
        return getFullORB().getTaggedProfileTemplateFactoryFinder();
    }

    public ConnectionCache getConnectionCache(ContactInfo contactInfo){
        return null;
    }    public ObjectKeyFactory getObjectKeyFactory(){
        return getFullORB().getObjectKeyFactory();
    }

    public Selector getSelector(int x){
        return null;
    }    public void setObjectKeyFactory(ObjectKeyFactory factory){
        throw new SecurityException("ORBSingleton: access denied");
    }

















    public void setResolver(Resolver resolver){
    }

    public Resolver getResolver(){
        return null;
    }

    public void setLocalResolver(LocalResolver resolver){
    }

    public LocalResolver getLocalResolver(){
        return null;
    }

    public void setURLOperation(Operation stringToObject){
    }







    public void setThreadPoolManager(ThreadPoolManager mgr){
    }

    public ThreadPoolManager getThreadPoolManager(){
        return null;
    }

    public CopierManager getCopierManager(){
        return null;
    }
}
// End of file.
