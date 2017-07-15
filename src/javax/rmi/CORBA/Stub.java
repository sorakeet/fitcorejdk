/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package javax.rmi.CORBA;

import com.sun.corba.se.impl.orbutil.GetPropertyAction;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;
import org.omg.CORBA_2_3.portable.ObjectImpl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClassLoader;
import java.security.AccessController;
import java.util.Properties;

public abstract class Stub extends ObjectImpl
        implements java.io.Serializable{
    private static final long serialVersionUID=1087775603798577179L;
    private static final String StubClassKey="javax.rmi.CORBA.StubClass";
    private static Class stubDelegateClass=null;

    static{
        Object stubDelegateInstance=createDelegate(StubClassKey);
        if(stubDelegateInstance!=null)
            stubDelegateClass=stubDelegateInstance.getClass();
    }

    // This can only be set at object construction time (no sync necessary).
    private transient StubDelegate stubDelegate=null;

    // Same code as in PortableRemoteObject. Can not be shared because they
    // are in different packages and the visibility needs to be package for
    // security reasons. If you know a better solution how to share this code
    // then remove it from PortableRemoteObject. Also in Util.java
    private static Object createDelegate(String classKey){
        String className=(String)
                AccessController.doPrivileged(new GetPropertyAction(classKey));
        if(className==null){
            Properties props=getORBPropertiesFile();
            if(props!=null){
                className=props.getProperty(classKey);
            }
        }
        if(className==null){
            return new com.sun.corba.se.impl.javax.rmi.CORBA.StubDelegateImpl();
        }
        try{
            return loadDelegateClass(className).newInstance();
        }catch(ClassNotFoundException ex){
            INITIALIZE exc=new INITIALIZE("Cannot instantiate "+className);
            exc.initCause(ex);
            throw exc;
        }catch(Exception ex){
            INITIALIZE exc=new INITIALIZE("Error while instantiating"+className);
            exc.initCause(ex);
            throw exc;
        }
    }

    private static Class loadDelegateClass(String className) throws ClassNotFoundException{
        try{
            ClassLoader loader=Thread.currentThread().getContextClassLoader();
            return Class.forName(className,false,loader);
        }catch(ClassNotFoundException e){
            // ignore, then try RMIClassLoader
        }
        try{
            return RMIClassLoader.loadClass(className);
        }catch(MalformedURLException e){
            String msg="Could not load "+className+": "+e.toString();
            ClassNotFoundException exc=new ClassNotFoundException(msg);
            throw exc;
        }
    }

    private static Properties getORBPropertiesFile(){
        return (Properties)AccessController.doPrivileged(new GetORBPropertiesFileAction());
    }

    public String toString(){
        if(stubDelegate==null){
            setDefaultDelegate();
        }
        String ior;
        if(stubDelegate!=null){
            ior=stubDelegate.toString(this);
            if(ior==null){
                return super.toString();
            }else{
                return ior;
            }
        }
        return super.toString();
    }

    public int hashCode(){
        if(stubDelegate==null){
            setDefaultDelegate();
        }
        if(stubDelegate!=null){
            return stubDelegate.hashCode(this);
        }
        return 0;
    }

    public boolean equals(Object obj){
        if(stubDelegate==null){
            setDefaultDelegate();
        }
        if(stubDelegate!=null){
            return stubDelegate.equals(this,obj);
        }
        return false;
    }

    private void setDefaultDelegate(){
        if(stubDelegateClass!=null){
            try{
                stubDelegate=(StubDelegate)stubDelegateClass.newInstance();
            }catch(Exception ex){
                // what kind of exception to throw
                // delegate not set therefore it is null and will return default
                // values
            }
        }
    }

    public void connect(ORB orb) throws RemoteException{
        if(stubDelegate==null){
            setDefaultDelegate();
        }
        if(stubDelegate!=null){
            stubDelegate.connect(this,orb);
        }
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException{
        if(stubDelegate==null){
            setDefaultDelegate();
        }
        if(stubDelegate!=null){
            stubDelegate.readObject(this,stream);
        }
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException{
        if(stubDelegate==null){
            setDefaultDelegate();
        }
        if(stubDelegate!=null){
            stubDelegate.writeObject(this,stream);
        }
    }
}
