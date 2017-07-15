/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.PortableServer;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.portable.Delegate;

abstract public class Servant{
    private transient Delegate _delegate=null;

    final public void _set_delegate(Delegate delegate){
        _delegate=delegate;
    }

    final public org.omg.CORBA.Object _this_object(ORB orb){
        try{
            ((org.omg.CORBA_2_3.ORB)orb).set_delegate(this);
        }catch(ClassCastException e){
            throw
                    new
                            org.omg.CORBA.BAD_PARAM
                            ("POA Servant requires an instance of org.omg.CORBA_2_3.ORB");
        }
        return _this_object();
    }

    final public org.omg.CORBA.Object _this_object(){
        return _get_delegate().this_object(this);
    }

    final public Delegate _get_delegate(){
        if(_delegate==null){
            throw
                    new
                            org.omg.CORBA.BAD_INV_ORDER
                            ("The Servant has not been associated with an ORB instance");
        }
        return _delegate;
    }

    final public ORB _orb(){
        return _get_delegate().orb(this);
    }

    final public POA _poa(){
        return _get_delegate().poa(this);
    }

    final public byte[] _object_id(){
        return _get_delegate().object_id(this);
    }

    public POA _default_POA(){
        return _get_delegate().default_POA(this);
    }

    public boolean _is_a(String repository_id){
        return _get_delegate().is_a(this,repository_id);
    }

    public boolean _non_existent(){
        return _get_delegate().non_existent(this);
    }
    // Ken and Simon will ask about editorial changes
    // needed in IDL to Java mapping to the following
    // signature.

    // _get_interface_def() replaces the _get_interface() method
    public org.omg.CORBA.Object _get_interface_def(){
        // First try to call the delegate implementation class's
        // "Object get_interface_def(..)" method (will work for ORBs
        // whose delegates implement this method).
        // Else call the delegate implementation class's
        // "InterfaceDef get_interface(..)" method using reflection
        // (will work for ORBs that were built using an older version
        // of the Delegate interface with a get_interface method
        // but not a get_interface_def method).
        Delegate delegate=_get_delegate();
        try{
            // If the ORB's delegate class does not implement
            // "Object get_interface_def(..)", this will throw
            // an AbstractMethodError.
            return delegate.get_interface_def(this);
        }catch(AbstractMethodError aex){
            // Call "InterfaceDef get_interface(..)" method using reflection.
            try{
                Class[] argc={Servant.class};
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

    // methods for which the user must provide an
    // implementation
    abstract public String[] _all_interfaces(POA poa,byte[] objectId);
}
