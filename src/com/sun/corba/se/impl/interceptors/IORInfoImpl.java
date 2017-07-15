/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.logging.InterceptorsSystemException;
import com.sun.corba.se.impl.logging.OMGSystemException;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.ior.TaggedComponentFactoryFinder;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate;
import com.sun.corba.se.spi.legacy.interceptor.IORInfoExt;
import com.sun.corba.se.spi.legacy.interceptor.UnknownType;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.oa.ObjectAdapter;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ObjectReferenceFactory;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

import java.util.Iterator;

public final class IORInfoImpl
        extends LocalObject
        implements IORInfo, IORInfoExt{
    // State values that determine which methods are allowed.
    // get_effective_policy, manager_id, and adapter_state are valid unless STATE_DONE
    // add_component, and add_component_to_profile are valid.
    private static final int STATE_INITIAL=0;
    // adapter_template, and R/W to current_factory are valid
    private static final int STATE_ESTABLISHED=1;
    // No methods are valid in this state
    private static final int STATE_DONE=2;
    // The current state of this object
    private int state=STATE_INITIAL;
    // The ObjectAdapter associated with this IORInfo object.
    private ObjectAdapter adapter;
    private ORB orb;
    private ORBUtilSystemException orbutilWrapper;
    private InterceptorsSystemException wrapper;
    private OMGSystemException omgWrapper;

    IORInfoImpl(ObjectAdapter adapter){
        this.orb=adapter.getORB();
        orbutilWrapper=ORBUtilSystemException.get(orb,
                CORBALogDomains.RPC_PROTOCOL);
        wrapper=InterceptorsSystemException.get(orb,
                CORBALogDomains.RPC_PROTOCOL);
        omgWrapper=OMGSystemException.get(orb,
                CORBALogDomains.RPC_PROTOCOL);
        this.adapter=adapter;
    }

    public Policy get_effective_policy(int type){
        checkState(STATE_INITIAL,STATE_ESTABLISHED);
        return adapter.getEffectivePolicy(type);
    }

    public void add_ior_component(TaggedComponent tagged_component){
        checkState(STATE_INITIAL);
        if(tagged_component==null) nullParam();
        addIORComponentToProfileInternal(tagged_component,
                adapter.getIORTemplate().iterator());
    }

    public void add_ior_component_to_profile(
            TaggedComponent tagged_component,int profile_id){
        checkState(STATE_INITIAL);
        if(tagged_component==null) nullParam();
        addIORComponentToProfileInternal(
                tagged_component,adapter.getIORTemplate().iteratorById(
                        profile_id));
    }

    public int manager_id(){
        checkState(STATE_INITIAL,STATE_ESTABLISHED);
        return adapter.getManagerId();
    }

    public short state(){
        checkState(STATE_INITIAL,STATE_ESTABLISHED);
        return adapter.getState();
    }

    public ObjectReferenceTemplate adapter_template(){
        checkState(STATE_ESTABLISHED);
        // At this point, the iortemp must contain only a single
        // IIOPProfileTemplate.  This is a restriction of our
        // implementation.  Also, note the the ObjectReferenceTemplate
        // is called when a certain POA is created in a certain ORB
        // in a certain server, so the server_id, orb_id, and
        // poa_id operations must be well-defined no matter what
        // kind of implementation is used: e.g., if a POA creates
        // IORs with multiple profiles, they must still all agree
        // about this information.  Thus, we are justified in
        // extracting the single IIOPProfileTemplate to create
        // an ObjectReferenceTemplate here.
        return adapter.getAdapterTemplate();
    }

    public ObjectReferenceFactory current_factory(){
        checkState(STATE_ESTABLISHED);
        return adapter.getCurrentFactory();
    }

    public void current_factory(ObjectReferenceFactory factory){
        checkState(STATE_ESTABLISHED);
        adapter.setCurrentFactory(factory);
    }

    private void addIORComponentToProfileInternal(
            TaggedComponent tagged_component,Iterator iterator){
        // Convert the given IOP::TaggedComponent into the appropriate
        // type for the TaggedProfileTemplate
        TaggedComponentFactoryFinder finder=
                orb.getTaggedComponentFactoryFinder();
        Object newTaggedComponent=finder.create(orb,tagged_component);
        // Iterate through TaggedProfileTemplates and add the given tagged
        // component to the appropriate one(s).
        boolean found=false;
        while(iterator.hasNext()){
            found=true;
            TaggedProfileTemplate taggedProfileTemplate=
                    (TaggedProfileTemplate)iterator.next();
            taggedProfileTemplate.add(newTaggedComponent);
        }
        // If no profile was found with the given id, throw a BAD_PARAM:
        // (See orbos/00-08-06, section 21.5.3.3.)
        if(!found){
            throw omgWrapper.invalidProfileId();
        }
    }

    private void nullParam(){
        throw orbutilWrapper.nullParam();
    }

    private void checkState(int expectedState){
        if(expectedState!=state)
            throw wrapper.badState1(new Integer(expectedState),new Integer(state));
    }

    private void checkState(int expectedState1,int expectedState2){
        if((expectedState1!=state)&&(expectedState2!=state))
            throw wrapper.badState2(new Integer(expectedState1),
                    new Integer(expectedState2),new Integer(state));
    }
    // REVISIT: add minor codes!

    public int getServerPort(String type)
            throws UnknownType{
        checkState(STATE_INITIAL,STATE_ESTABLISHED);
        int port=
                orb.getLegacyServerSocketManager()
                        .legacyGetTransientOrPersistentServerPort(type);
        if(port==-1){
            throw new UnknownType();
        }
        return port;
    }

    public ObjectAdapter getObjectAdapter(){
        return adapter;
    }

    void makeStateEstablished(){
        checkState(STATE_INITIAL);
        state=STATE_ESTABLISHED;
    }

    void makeStateDone(){
        checkState(STATE_ESTABLISHED);
        state=STATE_DONE;
    }
}
