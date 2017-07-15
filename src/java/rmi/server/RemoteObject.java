/**
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import sun.rmi.server.Util;

import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;

public abstract class RemoteObject implements Remote, java.io.Serializable{
    private static final long serialVersionUID=-3215090123894869218L;
    transient protected RemoteRef ref;

    protected RemoteObject(){
        ref=null;
    }

    protected RemoteObject(RemoteRef newref){
        ref=newref;
    }

    public static Remote toStub(Remote obj) throws NoSuchObjectException{
        if(obj instanceof RemoteStub||
                (obj!=null&&
                        Proxy.isProxyClass(obj.getClass())&&
                        Proxy.getInvocationHandler(obj) instanceof
                                RemoteObjectInvocationHandler)){
            return obj;
        }else{
            return sun.rmi.transport.ObjectTable.getStub(obj);
        }
    }

    public RemoteRef getRef(){
        return ref;
    }

    public int hashCode(){
        return (ref==null)?super.hashCode():ref.remoteHashCode();
    }

    public boolean equals(Object obj){
        if(obj instanceof RemoteObject){
            if(ref==null){
                return obj==this;
            }else{
                return ref.remoteEquals(((RemoteObject)obj).ref);
            }
        }else if(obj!=null){
            /**
             * Fix for 4099660: if object is not an instance of RemoteObject,
             * use the result of its equals method, to support symmetry is a
             * remote object implementation class that does not extend
             * RemoteObject wishes to support equality with its stub objects.
             */
            return obj.equals(this);
        }else{
            return false;
        }
    }

    public String toString(){
        String classname=Util.getUnqualifiedName(getClass());
        return (ref==null)?classname:
                classname+"["+ref.remoteToString()+"]";
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException, ClassNotFoundException{
        if(ref==null){
            throw new java.rmi.MarshalException("Invalid remote object");
        }else{
            String refClassName=ref.getRefClass(out);
            if(refClassName==null||refClassName.length()==0){
                /**
                 * No reference class name specified, so serialize
                 * remote reference.
                 */
                out.writeUTF("");
                out.writeObject(ref);
            }else{
                /**
                 * Built-in reference class specified, so delegate
                 * to reference to write out its external form.
                 */
                out.writeUTF(refClassName);
                ref.writeExternal(out);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException{
        String refClassName=in.readUTF();
        if(refClassName==null||refClassName.length()==0){
            /**
             * No reference class name specified, so construct
             * remote reference from its serialized form.
             */
            ref=(RemoteRef)in.readObject();
        }else{
            /**
             * Built-in reference class specified, so delegate to
             * internal reference class to initialize its fields from
             * its external form.
             */
            String internalRefClassName=
                    RemoteRef.packagePrefix+"."+refClassName;
            Class<?> refClass=Class.forName(internalRefClassName);
            try{
                ref=(RemoteRef)refClass.newInstance();
                /**
                 * If this step fails, assume we found an internal
                 * class that is not meant to be a serializable ref
                 * type.
                 */
            }catch(InstantiationException e){
                throw new ClassNotFoundException(internalRefClassName,e);
            }catch(IllegalAccessException e){
                throw new ClassNotFoundException(internalRefClassName,e);
            }catch(ClassCastException e){
                throw new ClassNotFoundException(internalRefClassName,e);
            }
            ref.readExternal(in);
        }
    }
}
