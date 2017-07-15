/**
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol;

import com.sun.corba.se.spi.ior.*;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import org.omg.CORBA.portable.ServantObject;

public abstract class LocalClientRequestDispatcherBase implements LocalClientRequestDispatcher{
    // If isNextIsLocalValid.get() == Boolean.TRUE,
    // the next call to isLocal should be valid
    private static final ThreadLocal isNextCallValid=new ThreadLocal(){
        protected synchronized Object initialValue(){
            return Boolean.TRUE;
        }
    };
    protected ORB orb;
    // Cached information needed for local dispatch
    protected boolean servantIsLocal;
    protected ObjectAdapterFactory oaf;
    protected ObjectAdapterId oaid;
    protected byte[] objectId;
    int scid;

    protected LocalClientRequestDispatcherBase(ORB orb,int scid,IOR ior){
        this.orb=orb;
        TaggedProfile prof=ior.getProfile();
        servantIsLocal=orb.getORBData().isLocalOptimizationAllowed()&&
                prof.isLocal();
        ObjectKeyTemplate oktemp=prof.getObjectKeyTemplate();
        this.scid=oktemp.getSubcontractId();
        RequestDispatcherRegistry sreg=orb.getRequestDispatcherRegistry();
        oaf=sreg.getObjectAdapterFactory(scid);
        oaid=oktemp.getObjectAdapterId();
        ObjectId oid=prof.getObjectId();
        objectId=oid.getId();
    }

    public byte[] getObjectId(){
        return objectId;
    }

    public boolean useLocalInvocation(org.omg.CORBA.Object self){
        if(isNextCallValid.get()==Boolean.TRUE)
            return servantIsLocal;
        else
            isNextCallValid.set(Boolean.TRUE);
        return false;
    }

    public boolean is_local(org.omg.CORBA.Object self){
        return false;
    }

    protected boolean checkForCompatibleServant(ServantObject so,
                                                Class expectedType){
        if(so==null)
            return false;
        // Normally, this test will never fail.  However, if the servant
        // and the stub were loaded in different class loaders, this test
        // will fail.
        if(!expectedType.isInstance(so.servant)){
            isNextCallValid.set(Boolean.FALSE);
            // When servant_preinvoke returns null, the stub will
            // recursively re-invoke itself.  Thus, the next call made from
            // the stub is another useLocalInvocation call.
            return false;
        }
        return true;
    }
}
// End of file.
