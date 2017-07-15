/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.*;

public class LocalObject implements Object{
    private static String reason="This is a locally constrained object.";

    public LocalObject(){
    }

    public boolean _is_a(String repository_id){
        throw new NO_IMPLEMENT(reason);
    }

    public boolean _is_equivalent(Object that){
        return equals(that);
    }

    public boolean _non_existent(){
        return false;
    }

    public int _hash(int maximum){
        return hashCode();
    }

    public Object _duplicate(){
        throw new NO_IMPLEMENT(reason);
    }

    public void _release(){
        throw new NO_IMPLEMENT(reason);
    }

    public Object _get_interface_def(){
        // First try to call the delegate implementation class's
        // "Object get_interface_def(..)" method (will work for JDK1.2
        // ORBs).
        // Else call the delegate implementation class's
        // "InterfaceDef get_interface(..)" method using reflection
        // (will work for pre-JDK1.2 ORBs).
        throw new NO_IMPLEMENT(reason);
    }

    public Request _request(String operation){
        throw new NO_IMPLEMENT(reason);
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result){
        throw new NO_IMPLEMENT(reason);
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result,
                                   ExceptionList exceptions,
                                   ContextList contexts){
        throw new NO_IMPLEMENT(reason);
    }

    public Policy _get_policy(int policy_type){
        throw new NO_IMPLEMENT(reason);
    }

    public DomainManager[] _get_domain_managers(){
        throw new NO_IMPLEMENT(reason);
    }

    public Object
    _set_policy_override(Policy[] policies,
                         SetOverrideType set_add){
        throw new NO_IMPLEMENT(reason);
    }

    public Object _get_interface(){
        throw new NO_IMPLEMENT(reason);
    }

    public ORB _orb(){
        throw new NO_IMPLEMENT(reason);
    }

    public boolean _is_local(){
        throw new NO_IMPLEMENT(reason);
    }

    public ServantObject _servant_preinvoke(String operation,
                                            Class expectedType){
        throw new NO_IMPLEMENT(reason);
    }

    public void _servant_postinvoke(ServantObject servant){
        throw new NO_IMPLEMENT(reason);
    }

    public OutputStream _request(String operation,
                                 boolean responseExpected){
        throw new NO_IMPLEMENT(reason);
    }

    public InputStream _invoke(OutputStream output)
            throws ApplicationException, RemarshalException{
        throw new NO_IMPLEMENT(reason);
    }

    public void _releaseReply(InputStream input){
        throw new NO_IMPLEMENT(reason);
    }

    public boolean validate_connection(){
        throw new NO_IMPLEMENT(reason);
    }
}
