/**
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.oa.toa;

import com.sun.corba.se.impl.ior.JIDLObjectKeyTemplate;
import com.sun.corba.se.impl.oa.NullServantImpl;
import com.sun.corba.se.impl.oa.poa.Policies;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.protocol.JIDLLocalCRDImpl;
import com.sun.corba.se.pept.protocol.ClientDelegate;
import com.sun.corba.se.spi.copyobject.CopierManager;
import com.sun.corba.se.spi.copyobject.ObjectCopierFactory;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate;
import com.sun.corba.se.spi.oa.OADestroyed;
import com.sun.corba.se.spi.oa.OAInvocationInfo;
import com.sun.corba.se.spi.oa.ObjectAdapterBase;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import org.omg.CORBA.Policy;
import org.omg.PortableInterceptor.ACTIVE;
import org.omg.PortableInterceptor.ObjectReferenceFactory;

public class TOAImpl extends ObjectAdapterBase implements TOA{
    private TransientObjectManager servants;

    public TOAImpl(ORB orb,TransientObjectManager tom,String codebase){
        super(orb);
        servants=tom;
        // Make the object key template
        int serverid=((ORB)getORB()).getTransientServerId();
        int scid=ORBConstants.TOA_SCID;
        ObjectKeyTemplate oktemp=new JIDLObjectKeyTemplate(orb,scid,serverid);
        // REVISIT - POA specific
        Policies policies=Policies.defaultPolicies;
        // REVISIT - absorb codebase into a policy
        initializeTemplate(oktemp,true,
                policies,
                codebase,
                null, // manager id
                oktemp.getObjectAdapterId()
        );
    }
    // Methods required for dispatching requests

    // XXX For now, this does nothing.
    // This will need fixing once we support ORB and thread level policies,
    // but for now, there is no way to associate policies with the TOA, so
    // getEffectivePolicy must always return null.
    public Policy getEffectivePolicy(int type){
        return null;
    }

    public int getManagerId(){
        return -1;
    }

    public short getState(){
        return ACTIVE.value;
    }

    public org.omg.CORBA.Object getLocalServant(byte[] objectId){
        return (org.omg.CORBA.Object)(servants.lookupServant(objectId));
    }

    public void getInvocationServant(OAInvocationInfo info){
        Object servant=servants.lookupServant(info.id());
        if(servant==null)
            // This is expected to result in an RMI-IIOP NoSuchObjectException.
            // See bug 4973160.
            servant=new NullServantImpl(lifecycleWrapper().nullServant());
        info.setServant(servant);
    }

    public void returnServant(){
        // NO-OP
    }

    public void enter() throws OADestroyed{
    }

    public void exit(){
    }

    public ObjectCopierFactory getObjectCopierFactory(){
        CopierManager cm=getORB().getCopierManager();
        return cm.getDefaultObjectCopierFactory();
    }

    public String[] getInterfaces(Object servant,byte[] objectId){
        return StubAdapter.getTypeIds(servant);
    }
    // Methods unique to the TOA

    public void connect(org.omg.CORBA.Object objref){
        // Store the objref and get a userkey allocated by the transient
        // object manager.
        byte[] key=servants.storeServant(objref,null);
        // Find out the repository ID for this objref.
        String id=StubAdapter.getTypeIds(objref)[0];
        // Create the new objref
        ObjectReferenceFactory orf=getCurrentFactory();
        org.omg.CORBA.Object obj=orf.make_object(id,key);
        // Copy the delegate from the new objref to the argument
        // XXX handle the case of an attempt to connect a local object.
        org.omg.CORBA.portable.Delegate delegate=StubAdapter.getDelegate(
                obj);
        CorbaContactInfoList ccil=(CorbaContactInfoList)
                ((ClientDelegate)delegate).getContactInfoList();
        LocalClientRequestDispatcher lcs=
                ccil.getLocalClientRequestDispatcher();
        if(lcs instanceof JIDLLocalCRDImpl){
            JIDLLocalCRDImpl jlcs=(JIDLLocalCRDImpl)lcs;
            jlcs.setServant(objref);
        }else{
            throw new RuntimeException(
                    "TOAImpl.connect can not be called on "+lcs);
        }
        StubAdapter.setDelegate(objref,delegate);
    }

    public void disconnect(org.omg.CORBA.Object objref){
        // Get the delegate, then ior, then transientKey, then delete servant
        org.omg.CORBA.portable.Delegate del=StubAdapter.getDelegate(
                objref);
        CorbaContactInfoList ccil=(CorbaContactInfoList)
                ((ClientDelegate)del).getContactInfoList();
        LocalClientRequestDispatcher lcs=
                ccil.getLocalClientRequestDispatcher();
        if(lcs instanceof JIDLLocalCRDImpl){
            JIDLLocalCRDImpl jlcs=(JIDLLocalCRDImpl)lcs;
            byte[] oid=jlcs.getObjectId();
            servants.deleteServant(oid);
            jlcs.unexport();
        }else{
            throw new RuntimeException(
                    "TOAImpl.disconnect can not be called on "+lcs);
        }
    }
}
