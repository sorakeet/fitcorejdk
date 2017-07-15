/**
 * Copyright (c) 2003, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.util;

import com.sun.jmx.mbeanserver.GetPropertyAction;
import com.sun.jmx.remote.security.NotificationAccessController;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServerFactory;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.util.*;

public class EnvHelp{
    public static final String CREDENTIAL_TYPES=
            "jmx.remote.rmi.server.credential.types";
    public static final String BUFFER_SIZE_PROPERTY=
            "jmx.remote.x.notification.buffer.size";
    public static final String MAX_FETCH_NOTIFS=
            "jmx.remote.x.notification.fetch.max";
    public static final String FETCH_TIMEOUT=
            "jmx.remote.x.notification.fetch.timeout";
    public static final String NOTIF_ACCESS_CONTROLLER=
            "com.sun.jmx.remote.notification.access.controller";
    public static final String DEFAULT_ORB="java.naming.corba.orb";
    public static final String HIDDEN_ATTRIBUTES=
            "jmx.remote.x.hidden.attributes";
    public static final String DEFAULT_HIDDEN_ATTRIBUTES=
            "java.naming.security.* "+
                    "jmx.remote.authenticator "+
                    "jmx.remote.context "+
                    "jmx.remote.default.class.loader "+
                    "jmx.remote.message.connection.server "+
                    "jmx.remote.object.wrapping "+
                    "jmx.remote.rmi.client.socket.factory "+
                    "jmx.remote.rmi.server.socket.factory "+
                    "jmx.remote.sasl.callback.handler "+
                    "jmx.remote.tls.socket.factory "+
                    "jmx.remote.x.access.file "+
                    "jmx.remote.x.password.file ";
    public static final String SERVER_CONNECTION_TIMEOUT=
            "jmx.remote.x.server.connection.timeout";
    public static final String CLIENT_CONNECTION_CHECK_PERIOD=
            "jmx.remote.x.client.connection.check.period";
    public static final String JMX_SERVER_DAEMON="jmx.remote.x.daemon";
    private static final String DEFAULT_CLASS_LOADER=
            JMXConnectorFactory.DEFAULT_CLASS_LOADER;
    private static final String DEFAULT_CLASS_LOADER_NAME=
            JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME;
    private static final SortedSet<String> defaultHiddenStrings=
            new TreeSet<String>();
    private static final SortedSet<String> defaultHiddenPrefixes=
            new TreeSet<String>();
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.misc","EnvHelp");

    public static ClassLoader resolveServerClassLoader(Map<String,?> env,
                                                       MBeanServer mbs)
            throws InstanceNotFoundException{
        if(env==null)
            return Thread.currentThread().getContextClassLoader();
        Object loader=env.get(DEFAULT_CLASS_LOADER);
        Object name=env.get(DEFAULT_CLASS_LOADER_NAME);
        if(loader!=null&&name!=null){
            final String msg="Only one of "+
                    DEFAULT_CLASS_LOADER+" or "+
                    DEFAULT_CLASS_LOADER_NAME+
                    " should be specified.";
            throw new IllegalArgumentException(msg);
        }
        if(loader==null&&name==null)
            return Thread.currentThread().getContextClassLoader();
        if(loader!=null){
            if(loader instanceof ClassLoader){
                return (ClassLoader)loader;
            }else{
                final String msg=
                        "ClassLoader object is not an instance of "+
                                ClassLoader.class.getName()+" : "+
                                loader.getClass().getName();
                throw new IllegalArgumentException(msg);
            }
        }
        ObjectName on;
        if(name instanceof ObjectName){
            on=(ObjectName)name;
        }else{
            final String msg=
                    "ClassLoader name is not an instance of "+
                            ObjectName.class.getName()+" : "+
                            name.getClass().getName();
            throw new IllegalArgumentException(msg);
        }
        if(mbs==null)
            throw new IllegalArgumentException("Null MBeanServer object");
        return mbs.getClassLoader(on);
    }

    public static ClassLoader resolveClientClassLoader(Map<String,?> env){
        if(env==null)
            return Thread.currentThread().getContextClassLoader();
        Object loader=env.get(DEFAULT_CLASS_LOADER);
        if(loader==null)
            return Thread.currentThread().getContextClassLoader();
        if(loader instanceof ClassLoader){
            return (ClassLoader)loader;
        }else{
            final String msg=
                    "ClassLoader object is not an instance of "+
                            ClassLoader.class.getName()+" : "+
                            loader.getClass().getName();
            throw new IllegalArgumentException(msg);
        }
    }

    public static <T extends Throwable> T initCause(T throwable,
                                                    Throwable cause){
        throwable.initCause(cause);
        return throwable;
    }

    public static Throwable getCause(Throwable t){
        Throwable ret=t;
        try{
            java.lang.reflect.Method getCause=
                    t.getClass().getMethod("getCause",(Class<?>[])null);
            ret=(Throwable)getCause.invoke(t,(Object[])null);
        }catch(Exception e){
            // OK.
            // it must be older than 1.4.
        }
        return (ret!=null)?ret:t;
    }

    public static int getNotifBufferSize(Map<String,?> env){
        int defaultQueueSize=1000; // default value
        // keep it for the compability for the fix:
        // 6174229: Environment parameter should be notification.buffer.size
        // instead of buffer.size
        final String oldP="jmx.remote.x.buffer.size";
        // the default value re-specified in the system
        try{
            GetPropertyAction act=new GetPropertyAction(BUFFER_SIZE_PROPERTY);
            String s=AccessController.doPrivileged(act);
            if(s!=null){
                defaultQueueSize=Integer.parseInt(s);
            }else{ // try the old one
                act=new GetPropertyAction(oldP);
                s=AccessController.doPrivileged(act);
                if(s!=null){
                    defaultQueueSize=Integer.parseInt(s);
                }
            }
        }catch(RuntimeException e){
            logger.warning("getNotifBufferSize",
                    "Can't use System property "+
                            BUFFER_SIZE_PROPERTY+": "+e);
            logger.debug("getNotifBufferSize",e);
        }
        int queueSize=defaultQueueSize;
        try{
            if(env.containsKey(BUFFER_SIZE_PROPERTY)){
                queueSize=(int)EnvHelp.getIntegerAttribute(env,BUFFER_SIZE_PROPERTY,
                        defaultQueueSize,0,
                        Integer.MAX_VALUE);
            }else{ // try the old one
                queueSize=(int)EnvHelp.getIntegerAttribute(env,oldP,
                        defaultQueueSize,0,
                        Integer.MAX_VALUE);
            }
        }catch(RuntimeException e){
            logger.warning("getNotifBufferSize",
                    "Can't determine queuesize (using default): "+
                            e);
            logger.debug("getNotifBufferSize",e);
        }
        return queueSize;
    }

    public static long getIntegerAttribute(Map<String,?> env,String name,
                                           long defaultValue,long minValue,
                                           long maxValue){
        final Object o;
        if(env==null||(o=env.get(name))==null)
            return defaultValue;
        final long result;
        if(o instanceof Number)
            result=((Number)o).longValue();
        else if(o instanceof String){
            result=Long.parseLong((String)o);
            /** May throw a NumberFormatException, which is an
             IllegalArgumentException.  */
        }else{
            final String msg=
                    "Attribute "+name+" value must be Integer or String: "+o;
            throw new IllegalArgumentException(msg);
        }
        if(result<minValue){
            final String msg=
                    "Attribute "+name+" value must be at least "+minValue+
                            ": "+result;
            throw new IllegalArgumentException(msg);
        }
        if(result>maxValue){
            final String msg=
                    "Attribute "+name+" value must be at most "+maxValue+
                            ": "+result;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    public static int getMaxFetchNotifNumber(Map<String,?> env){
        return (int)getIntegerAttribute(env,MAX_FETCH_NOTIFS,1000,1,
                Integer.MAX_VALUE);
    }

    public static long getFetchTimeout(Map<String,?> env){
        return getIntegerAttribute(env,FETCH_TIMEOUT,60000L,0,
                Long.MAX_VALUE);
    }

    public static NotificationAccessController getNotificationAccessController(
            Map<String,?> env){
        return (env==null)?null:
                (NotificationAccessController)env.get(NOTIF_ACCESS_CONTROLLER);
    }

    public static void checkAttributes(Map<?,?> attributes){
        for(Object key : attributes.keySet()){
            if(!(key instanceof String)){
                final String msg=
                        "Attributes contain key that is not a string: "+key;
                throw new IllegalArgumentException(msg);
            }
        }
    }

    public static <V> Map<String,V> filterAttributes(Map<String,V> attributes){
        if(logger.traceOn()){
            logger.trace("filterAttributes","starts");
        }
        SortedMap<String,V> map=new TreeMap<String,V>(attributes);
        purgeUnserializable(map.values());
        hideAttributes(map);
        return map;
    }

    private static void purgeUnserializable(Collection<?> objects){
        logger.trace("purgeUnserializable","starts");
        ObjectOutputStream oos=null;
        int i=0;
        for(Iterator<?> it=objects.iterator();it.hasNext();i++){
            Object v=it.next();
            if(v==null||v instanceof String){
                if(logger.traceOn()){
                    logger.trace("purgeUnserializable",
                            "Value trivially serializable: "+v);
                }
                continue;
            }
            try{
                if(oos==null)
                    oos=new ObjectOutputStream(new SinkOutputStream());
                oos.writeObject(v);
                if(logger.traceOn()){
                    logger.trace("purgeUnserializable",
                            "Value serializable: "+v);
                }
            }catch(IOException e){
                if(logger.traceOn()){
                    logger.trace("purgeUnserializable",
                            "Value not serializable: "+v+": "+
                                    e);
                }
                it.remove();
                oos=null; // ObjectOutputStream invalid after exception
            }
        }
    }

    private static void hideAttributes(SortedMap<String,?> map){
        if(map.isEmpty())
            return;
        final SortedSet<String> hiddenStrings;
        final SortedSet<String> hiddenPrefixes;
        String hide=(String)map.get(HIDDEN_ATTRIBUTES);
        if(hide!=null){
            if(hide.startsWith("="))
                hide=hide.substring(1);
            else
                hide+=" "+DEFAULT_HIDDEN_ATTRIBUTES;
            hiddenStrings=new TreeSet<String>();
            hiddenPrefixes=new TreeSet<String>();
            parseHiddenAttributes(hide,hiddenStrings,hiddenPrefixes);
        }else{
            hide=DEFAULT_HIDDEN_ATTRIBUTES;
            synchronized(defaultHiddenStrings){
                if(defaultHiddenStrings.isEmpty()){
                    parseHiddenAttributes(hide,
                            defaultHiddenStrings,
                            defaultHiddenPrefixes);
                }
                hiddenStrings=defaultHiddenStrings;
                hiddenPrefixes=defaultHiddenPrefixes;
            }
        }
        /** Construct a string that is greater than any key in the map.
         Setting a string-to-match or a prefix-to-match to this string
         guarantees that we will never call next() on the corresponding
         iterator.  */
        String sentinelKey=map.lastKey()+"X";
        Iterator<String> keyIterator=map.keySet().iterator();
        Iterator<String> stringIterator=hiddenStrings.iterator();
        Iterator<String> prefixIterator=hiddenPrefixes.iterator();
        String nextString;
        if(stringIterator.hasNext())
            nextString=stringIterator.next();
        else
            nextString=sentinelKey;
        String nextPrefix;
        if(prefixIterator.hasNext())
            nextPrefix=prefixIterator.next();
        else
            nextPrefix=sentinelKey;
        /** Read each key in sorted order and, if it matches a string
         or prefix, remove it. */
        keys:
        while(keyIterator.hasNext()){
            String key=keyIterator.next();
            /** Continue through string-match values until we find one
             that is either greater than the current key, or equal
             to it.  In the latter case, remove the key.  */
            int cmp=+1;
            while((cmp=nextString.compareTo(key))<0){
                if(stringIterator.hasNext())
                    nextString=stringIterator.next();
                else
                    nextString=sentinelKey;
            }
            if(cmp==0){
                keyIterator.remove();
                continue keys;
            }
            /** Continue through the prefix values until we find one
             that is either greater than the current key, or a
             prefix of it.  In the latter case, remove the key.  */
            while(nextPrefix.compareTo(key)<=0){
                if(key.startsWith(nextPrefix)){
                    keyIterator.remove();
                    continue keys;
                }
                if(prefixIterator.hasNext())
                    nextPrefix=prefixIterator.next();
                else
                    nextPrefix=sentinelKey;
            }
        }
    }

    private static void parseHiddenAttributes(String hide,
                                              SortedSet<String> hiddenStrings,
                                              SortedSet<String> hiddenPrefixes){
        final StringTokenizer tok=new StringTokenizer(hide);
        while(tok.hasMoreTokens()){
            String s=tok.nextToken();
            if(s.endsWith("*"))
                hiddenPrefixes.add(s.substring(0,s.length()-1));
            else
                hiddenStrings.add(s);
        }
    }

    public static long getServerConnectionTimeout(Map<String,?> env){
        return getIntegerAttribute(env,SERVER_CONNECTION_TIMEOUT,120000L,
                0,Long.MAX_VALUE);
    }

    public static long getConnectionCheckPeriod(Map<String,?> env){
        return getIntegerAttribute(env,CLIENT_CONNECTION_CHECK_PERIOD,60000L,
                0,Long.MAX_VALUE);
    }

    public static boolean computeBooleanFromString(String stringBoolean){
        // returns a default value of 'false' if no property is found...
        return computeBooleanFromString(stringBoolean,false);
    }

    public static boolean computeBooleanFromString(String stringBoolean,boolean defaultValue){
        if(stringBoolean==null)
            return defaultValue;
        else if(stringBoolean.equalsIgnoreCase("true"))
            return true;
        else if(stringBoolean.equalsIgnoreCase("false"))
            return false;
        else
            throw new IllegalArgumentException(
                    "Property value must be \"true\" or \"false\" instead of \""+
                            stringBoolean+"\"");
    }

    public static <K,V> Hashtable<K,V> mapToHashtable(Map<K,V> map){
        HashMap<K,V> m=new HashMap<K,V>(map);
        if(m.containsKey(null)) m.remove(null);
        for(Iterator<?> i=m.values().iterator();i.hasNext();)
            if(i.next()==null) i.remove();
        return new Hashtable<K,V>(m);
    }

    public static boolean isServerDaemon(Map<String,?> env){
        return (env!=null)&&
                ("true".equalsIgnoreCase((String)env.get(JMX_SERVER_DAEMON)));
    }

    private static final class SinkOutputStream extends OutputStream{
        public void write(int b){
        }

        public void write(byte[] b,int off,int len){
        }
    }
}
