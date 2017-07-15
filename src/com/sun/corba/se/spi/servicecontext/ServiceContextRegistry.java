/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.servicecontext;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.BAD_PARAM;

import java.util.Enumeration;
import java.util.Vector;

public class ServiceContextRegistry{
    private ORB orb;
    private Vector scCollection;

    public ServiceContextRegistry(ORB orb){
        scCollection=new Vector();
        this.orb=orb;
    }

    public void register(Class cls){
        if(ORB.ORBInitDebug)
            dprint("Registering service context class "+cls);
        ServiceContextData scd=new ServiceContextData(cls);
        if(findServiceContextData(scd.getId())==null)
            scCollection.addElement(scd);
        else
            throw new BAD_PARAM("Tried to register duplicate service context");
    }

    private void dprint(String msg){
        ORBUtility.dprint(this,msg);
    }

    public ServiceContextData findServiceContextData(int scId){
        if(ORB.ORBInitDebug)
            dprint("Searching registry for service context id "+scId);
        Enumeration enumeration=scCollection.elements();
        while(enumeration.hasMoreElements()){
            ServiceContextData scd=
                    (ServiceContextData)(enumeration.nextElement());
            if(scd.getId()==scId){
                if(ORB.ORBInitDebug)
                    dprint("Service context data found: "+scd);
                return scd;
            }
        }
        if(ORB.ORBInitDebug)
            dprint("Service context data not found");
        return null;
    }
}
