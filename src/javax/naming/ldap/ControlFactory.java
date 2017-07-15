/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import com.sun.naming.internal.FactoryEnumeration;
import com.sun.naming.internal.ResourceManager;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;

public abstract class ControlFactory{
    protected ControlFactory(){
    }

    public static Control getControlInstance(Control ctl,Context ctx,
                                             Hashtable<?,?> env)
            throws NamingException{
        // Get object factories list from environment properties or
        // provider resource file.
        FactoryEnumeration factories=ResourceManager.getFactories(
                LdapContext.CONTROL_FACTORIES,env,ctx);
        if(factories==null){
            return ctl;
        }
        // Try each factory until one succeeds
        Control answer=null;
        ControlFactory factory;
        while(answer==null&&factories.hasMore()){
            factory=(ControlFactory)factories.next();
            answer=factory.getControlInstance(ctl);
        }
        return (answer!=null)?answer:ctl;
    }

    public abstract Control getControlInstance(Control ctl) throws NamingException;
}
