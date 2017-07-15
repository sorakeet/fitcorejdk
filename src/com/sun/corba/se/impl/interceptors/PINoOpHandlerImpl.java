/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.corba.RequestImpl;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate;
import com.sun.corba.se.spi.oa.ObjectAdapter;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.protocol.PIHandler;
import org.omg.CORBA.Any;
import org.omg.CORBA.NVList;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.Interceptor;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import org.omg.PortableInterceptor.PolicyFactory;

public class PINoOpHandlerImpl implements PIHandler{
    public PINoOpHandlerImpl(){
    }

    public void close(){
    }

    public void initialize(){
    }

    public void destroyInterceptors(){
    }

    public void objectAdapterCreated(ObjectAdapter oa){
    }

    public void adapterManagerStateChanged(int managerId,
                                           short newState){
    }

    public void adapterStateChanged(ObjectReferenceTemplate[]
                                            templates,short newState){
    }

    public void disableInterceptorsThisThread(){
    }

    public void enableInterceptorsThisThread(){
    }

    public void invokeClientPIStartingPoint()
            throws RemarshalException{
    }

    public Exception invokeClientPIEndingPoint(
            int replyStatus,Exception exception){
        return null;
    }

    public Exception makeCompletedClientRequest(
            int replyStatus,Exception exception){
        return null;
    }

    public void initiateClientPIRequest(boolean diiRequest){
    }

    public void cleanupClientPIRequest(){
    }

    public void setClientPIInfo(RequestImpl requestImpl){
    }

    public void setClientPIInfo(CorbaMessageMediator messageMediator){
    }

    public void invokeServerPIStartingPoint(){
    }

    public void invokeServerPIIntermediatePoint(){
    }

    public void invokeServerPIEndingPoint(ReplyMessage replyMessage){
    }

    public void initializeServerPIInfo(CorbaMessageMediator request,
                                       ObjectAdapter oa,byte[] objectId,ObjectKeyTemplate oktemp){
    }

    public void setServerPIInfo(Object servant,
                                String targetMostDerivedInterface){
    }

    public void setServerPIInfo(Exception exception){
    }

    public void setServerPIInfo(NVList arguments){
    }

    public void setServerPIExceptionInfo(Any exception){
    }

    public void setServerPIInfo(Any result){
    }

    public void cleanupServerPIRequest(){
    }

    public org.omg.CORBA.Policy create_policy(int type,Any val)
            throws org.omg.CORBA.PolicyError{
        return null;
    }

    public void register_interceptor(Interceptor interceptor,int type)
            throws DuplicateName{
    }

    public Current getPICurrent(){
        return null;
    }

    public void registerPolicyFactory(int type,PolicyFactory factory){
    }

    public int allocateServerRequestId(){
        return 0;
    }

    final public void sendCancelRequestIfFinalFragmentNotSent(){
    }
}
