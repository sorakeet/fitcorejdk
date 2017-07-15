/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.portable;

import org.omg.CORBA.*;

import java.lang.Object;

abstract public class ObjectImpl implements org.omg.CORBA.Object{
    private transient Delegate __delegate;

    public void _set_delegate(Delegate delegate){
        __delegate=delegate;
    }

    public abstract String[] _ids();

    public boolean _is_a(String repository_id){
        return _get_delegate().is_a(this,repository_id);
    }

    public boolean _is_equivalent(org.omg.CORBA.Object that){
        return _get_delegate().is_equivalent(this,that);
    }

    public boolean _non_existent(){
        return _get_delegate().non_existent(this);
    }

    public int _hash(int maximum){
        return _get_delegate().hash(this,maximum);
    }

    public org.omg.CORBA.Object _duplicate(){
        return _get_delegate().duplicate(this);
    }

    public void _release(){
        _get_delegate().release(this);
    }

    public org.omg.CORBA.Object _get_interface_def(){
        // First try to call the delegate implementation class's
        // "Object get_interface_def(..)" method (will work for JDK1.2 ORBs).
        // Else call the delegate implementation class's
        // "InterfaceDef get_interface(..)" method using reflection
        // (will work for pre-JDK1.2 ORBs).
        Delegate delegate=_get_delegate();
        try{
            // If the ORB's delegate class does not implement
            // "Object get_interface_def(..)", this will call
            // get_interface_def(..) on portable.Delegate.
            return delegate.get_interface_def(this);
        }catch(org.omg.CORBA.NO_IMPLEMENT ex){
            // Call "InterfaceDef get_interface(..)" method using reflection.
            try{
                Class[] argc={org.omg.CORBA.Object.class};
                java.lang.reflect.Method meth=
                        delegate.getClass().getMethod("get_interface",argc);
                Object[] argx={this};
                return (org.omg.CORBA.Object)meth.invoke(delegate,argx);
            }catch(java.lang.reflect.InvocationTargetException exs){
                Throwable t=exs.getTargetException();
                if(t instanceof Error){
                    throw (Error)t;
                }else if(t instanceof RuntimeException){
                    throw (RuntimeException)t;
                }else{
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                }
            }catch(RuntimeException rex){
                throw rex;
            }catch(Exception exr){
                throw new org.omg.CORBA.NO_IMPLEMENT();
            }
        }
    }

    public Request _request(String operation){
        return _get_delegate().request(this,operation);
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result){
        return _get_delegate().create_request(this,
                ctx,
                operation,
                arg_list,
                result);
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result,
                                   ExceptionList exceptions,
                                   ContextList contexts){
        return _get_delegate().create_request(this,
                ctx,
                operation,
                arg_list,
                result,
                exceptions,
                contexts);
    }

    public org.omg.CORBA.Policy _get_policy(int policy_type){
        return _get_delegate().get_policy(this,policy_type);
    }

    public org.omg.CORBA.DomainManager[] _get_domain_managers(){
        return _get_delegate().get_domain_managers(this);
    }

    public org.omg.CORBA.Object
    _set_policy_override(org.omg.CORBA.Policy[] policies,
                         org.omg.CORBA.SetOverrideType set_add){
        return _get_delegate().set_policy_override(this,policies,
                set_add);
    }

    public Delegate _get_delegate(){
        if(__delegate==null)
            throw new BAD_OPERATION("The delegate has not been set!");
        return __delegate;
    }

    public org.omg.CORBA.ORB _orb(){
        return _get_delegate().orb(this);
    }

    public boolean _is_local(){
        return _get_delegate().is_local(this);
    }

    public ServantObject _servant_preinvoke(String operation,
                                            Class expectedType){
        return _get_delegate().servant_preinvoke(this,operation,
                expectedType);
    }

    public void _servant_postinvoke(ServantObject servant){
        _get_delegate().servant_postinvoke(this,servant);
    }

    public OutputStream _request(String operation,
                                 boolean responseExpected){
        return _get_delegate().request(this,operation,responseExpected);
    }

    public InputStream _invoke(OutputStream output)
            throws ApplicationException, RemarshalException{
        return _get_delegate().invoke(this,output);
    }

    public void _releaseReply(InputStream input){
        _get_delegate().releaseReply(this,input);
    }

    public int hashCode(){
        if(__delegate!=null)
            return __delegate.hashCode(this);
        else
            return super.hashCode();
    }

    public boolean equals(Object obj){
        if(__delegate!=null)
            return __delegate.equals(this,obj);
        else
            return (this==obj);
    }

    public String toString(){
        if(__delegate!=null)
            return __delegate.toString(this);
        else
            return getClass().getName()+": no delegate set";
    }
}
