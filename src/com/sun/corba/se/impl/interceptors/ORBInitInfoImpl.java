/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.logging.InterceptorsSystemException;
import com.sun.corba.se.impl.logging.OMGSystemException;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.legacy.interceptor.ORBInitInfoExt;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.IOP.CodecFactory;
import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;

public final class ORBInitInfoImpl
        extends LocalObject
        implements ORBInitInfo, ORBInitInfoExt{
    // The pre-initialization stage (pre_init() being called)
    public static final int STAGE_PRE_INIT=0;
    // The post-initialization stage (post_init() being called)
    public static final int STAGE_POST_INIT=1;
    // Reject all calls - this object should no longer be around.
    public static final int STAGE_CLOSED=2;
    // The description for the OBJECT_NOT_EXIST exception in STAGE_CLOSED
    private static final String MESSAGE_ORBINITINFO_INVALID=
            "ORBInitInfo object is only valid during ORB_init";
    // The ORB we are initializing
    private ORB orb;
    private InterceptorsSystemException wrapper;
    private ORBUtilSystemException orbutilWrapper;
    private OMGSystemException omgWrapper;
    // The arguments passed to ORB_init
    private String[] args;
    // The ID of the ORB being initialized
    private String orbId;
    // The CodecFactory
    private CodecFactory codecFactory;
    // The current stage of initialization
    private int stage=STAGE_PRE_INIT;

    ORBInitInfoImpl(ORB orb,String[] args,
                    String orbId,CodecFactory codecFactory){
        this.orb=orb;
        wrapper=InterceptorsSystemException.get(orb,
                CORBALogDomains.RPC_PROTOCOL);
        orbutilWrapper=ORBUtilSystemException.get(orb,
                CORBALogDomains.RPC_PROTOCOL);
        omgWrapper=OMGSystemException.get(orb,
                CORBALogDomains.RPC_PROTOCOL);
        this.args=args;
        this.orbId=orbId;
        this.codecFactory=codecFactory;
    }

    public ORB getORB(){
        return orb;
    }

    void setStage(int stage){
        this.stage=stage;
    }

    public String[] arguments(){
        checkStage();
        return args;
    }

    private void checkStage(){
        if(stage==STAGE_CLOSED){
            throw wrapper.orbinitinfoInvalid();
        }
    }

    public String orb_id(){
        checkStage();
        return orbId;
    }

    public CodecFactory codec_factory(){
        checkStage();
        return codecFactory;
    }

    public void register_initial_reference(String id,
                                           org.omg.CORBA.Object obj)
            throws InvalidName{
        checkStage();
        if(id==null) nullParam();
        // As per CORBA 3.0 section 21.8.1,
        // if null is passed as the obj parameter,
        // throw BAD_PARAM with minor code OMGSystemException.RIR_WITH_NULL_OBJECT.
        // Though the spec is talking about IDL null, we will address both
        // Java null and IDL null:
        // Note: Local Objects can never be nil!
        if(obj==null){
            throw omgWrapper.rirWithNullObject();
        }
        // This check was made to determine that the objref is a
        // non-local objref that is fully
        // initialized: this was called only for its side-effects of
        // possibly throwing exceptions.  However, registering
        // local objects should be permitted!
        // XXX/Revisit?
        // IOR ior = ORBUtility.getIOR( obj ) ;
        // Delegate to ORB.  If ORB version throws InvalidName, convert to
        // equivalent Portable Interceptors InvalidName.
        try{
            orb.register_initial_reference(id,obj);
        }catch(org.omg.CORBA.ORBPackage.InvalidName e){
            InvalidName exc=new InvalidName(e.getMessage());
            exc.initCause(e);
            throw exc;
        }
    }

    public org.omg.CORBA.Object resolve_initial_references(String id)
            throws InvalidName{
        checkStage();
        if(id==null) nullParam();
        if(stage==STAGE_PRE_INIT){
            // Initializer is not allowed to invoke this method during
            // this stage.
            // _REVISIT_ Spec issue: What exception should really be
            // thrown here?
            throw wrapper.rirInvalidPreInit();
        }
        org.omg.CORBA.Object objRef=null;
        try{
            objRef=orb.resolve_initial_references(id);
        }catch(org.omg.CORBA.ORBPackage.InvalidName e){
            // Convert PIDL to IDL exception:
            throw new InvalidName();
        }
        return objRef;
    }

    public void add_client_request_interceptor(
            ClientRequestInterceptor interceptor)
            throws DuplicateName{
        checkStage();
        if(interceptor==null) nullParam();
        orb.getPIHandler().register_interceptor(interceptor,
                InterceptorList.INTERCEPTOR_TYPE_CLIENT);
    }

    public void add_server_request_interceptor(
            ServerRequestInterceptor interceptor)
            throws DuplicateName{
        checkStage();
        if(interceptor==null) nullParam();
        orb.getPIHandler().register_interceptor(interceptor,
                InterceptorList.INTERCEPTOR_TYPE_SERVER);
    }

    public void add_ior_interceptor(
            IORInterceptor interceptor)
            throws DuplicateName{
        checkStage();
        if(interceptor==null) nullParam();
        orb.getPIHandler().register_interceptor(interceptor,
                InterceptorList.INTERCEPTOR_TYPE_IOR);
    }

    public int allocate_slot_id(){
        checkStage();
        return ((PICurrent)orb.getPIHandler().getPICurrent()).allocateSlotId();
    }

    public void register_policy_factory(int type,
                                        PolicyFactory policy_factory){
        checkStage();
        if(policy_factory==null) nullParam();
        orb.getPIHandler().registerPolicyFactory(type,policy_factory);
    }

    private void nullParam()
            throws BAD_PARAM{
        throw orbutilWrapper.nullParam();
    }

    // New method from CORBA 3.1
    public void add_client_request_interceptor_with_policy(
            ClientRequestInterceptor interceptor,Policy[] policies)
            throws DuplicateName{
        // XXX ignore policies for now
        add_client_request_interceptor(interceptor);
    }

    // New method from CORBA 3.1
    public void add_server_request_interceptor_with_policy(
            ServerRequestInterceptor interceptor,Policy[] policies)
            throws DuplicateName, PolicyError{
        // XXX ignore policies for now
        add_server_request_interceptor(interceptor);
    }

    // New method from CORBA 3.1
    public void add_ior_interceptor_with_policy(
            IORInterceptor interceptor,Policy[] policies)
            throws DuplicateName, PolicyError{
        // XXX ignore policies for now
        add_ior_interceptor(interceptor);
    }
}
