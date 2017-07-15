/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.MXBeanProxy;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.WeakHashMap;

public class MBeanServerInvocationHandler implements InvocationHandler{
    private static final WeakHashMap<Class<?>,WeakReference<MXBeanProxy>>
            mxbeanProxies=new WeakHashMap<Class<?>,WeakReference<MXBeanProxy>>();
    private final MBeanServerConnection connection;
    private final ObjectName objectName;
    private final boolean isMXBean;

    public MBeanServerInvocationHandler(MBeanServerConnection connection,
                                        ObjectName objectName){
        this(connection,objectName,false);
    }

    public MBeanServerInvocationHandler(MBeanServerConnection connection,
                                        ObjectName objectName,
                                        boolean isMXBean){
        if(connection==null){
            throw new IllegalArgumentException("Null connection");
        }
        if(Proxy.isProxyClass(connection.getClass())){
            if(MBeanServerInvocationHandler.class.isAssignableFrom(
                    Proxy.getInvocationHandler(connection).getClass())){
                throw new IllegalArgumentException("Wrapping MBeanServerInvocationHandler");
            }
        }
        if(objectName==null){
            throw new IllegalArgumentException("Null object name");
        }
        this.connection=connection;
        this.objectName=objectName;
        this.isMXBean=isMXBean;
    }

    public static <T> T newProxyInstance(MBeanServerConnection connection,
                                         ObjectName objectName,
                                         Class<T> interfaceClass,
                                         boolean notificationBroadcaster){
        return JMX.newMBeanProxy(connection,objectName,interfaceClass,notificationBroadcaster);
    }

    public MBeanServerConnection getMBeanServerConnection(){
        return connection;
    }

    public ObjectName getObjectName(){
        return objectName;
    }

    public Object invoke(Object proxy,Method method,Object[] args)
            throws Throwable{
        final Class<?> methodClass=method.getDeclaringClass();
        if(methodClass.equals(NotificationBroadcaster.class)
                ||methodClass.equals(NotificationEmitter.class))
            return invokeBroadcasterMethod(proxy,method,args);
        // local or not: equals, toString, hashCode
        if(shouldDoLocally(proxy,method))
            return doLocally(proxy,method,args);
        try{
            if(isMXBean()){
                MXBeanProxy p=findMXBeanProxy(methodClass);
                return p.invoke(connection,objectName,method,args);
            }else{
                final String methodName=method.getName();
                final Class<?>[] paramTypes=method.getParameterTypes();
                final Class<?> returnType=method.getReturnType();
                /** Inexplicably, InvocationHandler specifies that args is null
                 when the method takes no arguments rather than a
                 zero-length array.  */
                final int nargs=(args==null)?0:args.length;
                if(methodName.startsWith("get")
                        &&methodName.length()>3
                        &&nargs==0
                        &&!returnType.equals(Void.TYPE)){
                    return connection.getAttribute(objectName,
                            methodName.substring(3));
                }
                if(methodName.startsWith("is")
                        &&methodName.length()>2
                        &&nargs==0
                        &&(returnType.equals(Boolean.TYPE)
                        ||returnType.equals(Boolean.class))){
                    return connection.getAttribute(objectName,
                            methodName.substring(2));
                }
                if(methodName.startsWith("set")
                        &&methodName.length()>3
                        &&nargs==1
                        &&returnType.equals(Void.TYPE)){
                    Attribute attr=new Attribute(methodName.substring(3),args[0]);
                    connection.setAttribute(objectName,attr);
                    return null;
                }
                final String[] signature=new String[paramTypes.length];
                for(int i=0;i<paramTypes.length;i++)
                    signature[i]=paramTypes[i].getName();
                return connection.invoke(objectName,methodName,
                        args,signature);
            }
        }catch(MBeanException e){
            throw e.getTargetException();
        }catch(RuntimeMBeanException re){
            throw re.getTargetException();
        }catch(RuntimeErrorException rre){
            throw rre.getTargetError();
        }
        /** The invoke may fail because it can't get to the MBean, with
         one of the these exceptions declared by
         MBeanServerConnection.invoke:
         - RemoteException: can't talk to MBeanServer;
         - InstanceNotFoundException: objectName is not registered;
         - ReflectionException: objectName is registered but does not
         have the method being invoked.
         In all of these cases, the exception will be wrapped by the
         proxy mechanism in an UndeclaredThrowableException unless
         it happens to be declared in the "throws" clause of the
         method being invoked on the proxy.
         */
    }

    private static MXBeanProxy findMXBeanProxy(Class<?> mxbeanInterface){
        synchronized(mxbeanProxies){
            WeakReference<MXBeanProxy> proxyRef=
                    mxbeanProxies.get(mxbeanInterface);
            MXBeanProxy p=(proxyRef==null)?null:proxyRef.get();
            if(p==null){
                try{
                    p=new MXBeanProxy(mxbeanInterface);
                }catch(IllegalArgumentException e){
                    String msg="Cannot make MXBean proxy for "+
                            mxbeanInterface.getName()+": "+e.getMessage();
                    IllegalArgumentException iae=
                            new IllegalArgumentException(msg,e.getCause());
                    iae.setStackTrace(e.getStackTrace());
                    throw iae;
                }
                mxbeanProxies.put(mxbeanInterface,
                        new WeakReference<MXBeanProxy>(p));
            }
            return p;
        }
    }

    private Object invokeBroadcasterMethod(Object proxy,Method method,
                                           Object[] args) throws Exception{
        final String methodName=method.getName();
        final int nargs=(args==null)?0:args.length;
        if(methodName.equals("addNotificationListener")){
            /** The various throws of IllegalArgumentException here
             should not happen, since we know what the methods in
             NotificationBroadcaster and NotificationEmitter
             are.  */
            if(nargs!=3){
                final String msg=
                        "Bad arg count to addNotificationListener: "+nargs;
                throw new IllegalArgumentException(msg);
            }
            /** Other inconsistencies will produce ClassCastException
             below.  */
            NotificationListener listener=(NotificationListener)args[0];
            NotificationFilter filter=(NotificationFilter)args[1];
            Object handback=args[2];
            connection.addNotificationListener(objectName,
                    listener,
                    filter,
                    handback);
            return null;
        }else if(methodName.equals("removeNotificationListener")){
            /** NullPointerException if method with no args, but that
             shouldn't happen because removeNL does have args.  */
            NotificationListener listener=(NotificationListener)args[0];
            switch(nargs){
                case 1:
                    connection.removeNotificationListener(objectName,listener);
                    return null;
                case 3:
                    NotificationFilter filter=(NotificationFilter)args[1];
                    Object handback=args[2];
                    connection.removeNotificationListener(objectName,
                            listener,
                            filter,
                            handback);
                    return null;
                default:
                    final String msg=
                            "Bad arg count to removeNotificationListener: "+nargs;
                    throw new IllegalArgumentException(msg);
            }
        }else if(methodName.equals("getNotificationInfo")){
            if(args!=null){
                throw new IllegalArgumentException("getNotificationInfo has "+
                        "args");
            }
            MBeanInfo info=connection.getMBeanInfo(objectName);
            return info.getNotifications();
        }else{
            throw new IllegalArgumentException("Bad method name: "+
                    methodName);
        }
    }

    private boolean shouldDoLocally(Object proxy,Method method){
        final String methodName=method.getName();
        if((methodName.equals("hashCode")||methodName.equals("toString"))
                &&method.getParameterTypes().length==0
                &&isLocal(proxy,method))
            return true;
        if(methodName.equals("equals")
                &&Arrays.equals(method.getParameterTypes(),
                new Class<?>[]{Object.class})
                &&isLocal(proxy,method))
            return true;
        if(methodName.equals("finalize")
                &&method.getParameterTypes().length==0){
            return true;
        }
        return false;
    }

    private static boolean isLocal(Object proxy,Method method){
        final Class<?>[] interfaces=proxy.getClass().getInterfaces();
        if(interfaces==null){
            return true;
        }
        final String methodName=method.getName();
        final Class<?>[] params=method.getParameterTypes();
        for(Class<?> intf : interfaces){
            try{
                intf.getMethod(methodName,params);
                return false; // found method in one of our interfaces
            }catch(NoSuchMethodException nsme){
                // OK.
            }
        }
        return true;  // did not find in any interface
    }

    private Object doLocally(Object proxy,Method method,Object[] args){
        final String methodName=method.getName();
        if(methodName.equals("equals")){
            if(this==args[0]){
                return true;
            }
            if(!(args[0] instanceof Proxy)){
                return false;
            }
            final InvocationHandler ihandler=
                    Proxy.getInvocationHandler(args[0]);
            if(ihandler==null||
                    !(ihandler instanceof MBeanServerInvocationHandler)){
                return false;
            }
            final MBeanServerInvocationHandler handler=
                    (MBeanServerInvocationHandler)ihandler;
            return connection.equals(handler.connection)&&
                    objectName.equals(handler.objectName)&&
                    proxy.getClass().equals(args[0].getClass());
        }else if(methodName.equals("toString")){
            return (isMXBean()?"MX":"M")+"BeanProxy("+
                    connection+"["+objectName+"])";
        }else if(methodName.equals("hashCode")){
            return objectName.hashCode()+connection.hashCode();
        }else if(methodName.equals("finalize")){
            // ignore the finalizer invocation via proxy
            return null;
        }
        throw new RuntimeException("Unexpected method name: "+methodName);
    }

    public boolean isMXBean(){
        return isMXBean;
    }
}
