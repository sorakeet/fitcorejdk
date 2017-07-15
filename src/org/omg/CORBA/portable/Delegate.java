/**
 * Copyright (c) 1997, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.portable;

import org.omg.CORBA.*;

import java.lang.Object;

public abstract class Delegate{
    public abstract org.omg.CORBA.Object get_interface_def(
            org.omg.CORBA.Object self);

    public abstract org.omg.CORBA.Object duplicate(org.omg.CORBA.Object obj);

    public abstract void release(org.omg.CORBA.Object obj);

    public abstract boolean is_a(org.omg.CORBA.Object obj,String repository_id);

    public abstract boolean non_existent(org.omg.CORBA.Object obj);

    public abstract boolean is_equivalent(org.omg.CORBA.Object obj,
                                          org.omg.CORBA.Object other);

    public abstract int hash(org.omg.CORBA.Object obj,int max);

    public abstract Request request(org.omg.CORBA.Object obj,String operation);

    public abstract Request create_request(org.omg.CORBA.Object obj,
                                           Context ctx,
                                           String operation,
                                           NVList arg_list,
                                           NamedValue result);

    public abstract Request create_request(org.omg.CORBA.Object obj,
                                           Context ctx,
                                           String operation,
                                           NVList arg_list,
                                           NamedValue result,
                                           ExceptionList exclist,
                                           ContextList ctxlist);

    public org.omg.CORBA.ORB orb(org.omg.CORBA.Object obj){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object self,
                                           int policy_type){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.DomainManager[] get_domain_managers(
            org.omg.CORBA.Object
                    self){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object set_policy_override(org.omg.CORBA.Object self,
                                                    org.omg.CORBA.Policy[] policies,
                                                    org.omg.CORBA.SetOverrideType set_add){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean is_local(org.omg.CORBA.Object self){
        return false;
    }

    public ServantObject servant_preinvoke(org.omg.CORBA.Object self,
                                           String operation,
                                           Class expectedType){
        return null;
    }

    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servant){
    }

    public OutputStream request(org.omg.CORBA.Object self,
                                String operation,
                                boolean responseExpected){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public InputStream invoke(org.omg.CORBA.Object self,
                              OutputStream output)
            throws ApplicationException, RemarshalException{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void releaseReply(org.omg.CORBA.Object self,
                             InputStream input){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public String toString(org.omg.CORBA.Object self){
        return self.getClass().getName()+":"+this.toString();
    }

    public int hashCode(org.omg.CORBA.Object self){
        return System.identityHashCode(self);
    }

    public boolean equals(org.omg.CORBA.Object self,Object obj){
        return (self==obj);
    }
}
