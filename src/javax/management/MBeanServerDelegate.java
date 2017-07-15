/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.defaults.JmxProperties;
import com.sun.jmx.defaults.ServiceName;
import com.sun.jmx.mbeanserver.Util;

public class MBeanServerDelegate implements MBeanServerDelegateMBean,
        NotificationEmitter{
    public static final ObjectName DELEGATE_NAME=
            Util.newObjectName("JMImplementation:type=MBeanServerDelegate");
    private static final MBeanNotificationInfo[] notifsInfo;
    private static long oldStamp=0;

    static{
        final String[] types={
                MBeanServerNotification.UNREGISTRATION_NOTIFICATION,
                MBeanServerNotification.REGISTRATION_NOTIFICATION
        };
        notifsInfo=new MBeanNotificationInfo[1];
        notifsInfo[0]=
                new MBeanNotificationInfo(types,
                        "javax.management.MBeanServerNotification",
                        "Notifications sent by the MBeanServerDelegate MBean");
    }

    private final NotificationBroadcasterSupport broadcaster;
    private final long stamp;
    private String mbeanServerId;
    private long sequenceNumber=1;

    public MBeanServerDelegate(){
        stamp=getStamp();
        broadcaster=new NotificationBroadcasterSupport();
    }

    private static synchronized long getStamp(){
        long s=System.currentTimeMillis();
        if(oldStamp>=s){
            s=oldStamp+1;
        }
        oldStamp=s;
        return s;
    }

    public synchronized String getMBeanServerId(){
        if(mbeanServerId==null){
            String localHost;
            try{
                localHost=java.net.InetAddress.getLocalHost().getHostName();
            }catch(java.net.UnknownHostException e){
                JmxProperties.MISC_LOGGER.finest("Can't get local host name, "+
                        "using \"localhost\" instead. Cause is: "+e);
                localHost="localhost";
            }
            mbeanServerId=localHost+"_"+stamp;
        }
        return mbeanServerId;
    }

    public String getSpecificationName(){
        return ServiceName.JMX_SPEC_NAME;
    }

    public String getSpecificationVersion(){
        return ServiceName.JMX_SPEC_VERSION;
    }

    public String getSpecificationVendor(){
        return ServiceName.JMX_SPEC_VENDOR;
    }

    public String getImplementationName(){
        return ServiceName.JMX_IMPL_NAME;
    }

    public String getImplementationVersion(){
        try{
            return System.getProperty("java.runtime.version");
        }catch(SecurityException e){
            return "";
        }
    }

    public String getImplementationVendor(){
        return ServiceName.JMX_IMPL_VENDOR;
    }

    // From NotificationEmitter extends NotificationBroacaster
    //
    public synchronized void addNotificationListener(NotificationListener listener,
                                                     NotificationFilter filter,
                                                     Object handback)
            throws IllegalArgumentException{
        broadcaster.addNotificationListener(listener,filter,handback);
    }

    // From NotificationEmitter extends NotificationBroacaster
    //
    public synchronized void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException{
        broadcaster.removeNotificationListener(listener);
    }

    // From NotificationEmitter extends NotificationBroacaster
    //
    public MBeanNotificationInfo[] getNotificationInfo(){
        final int len=MBeanServerDelegate.notifsInfo.length;
        final MBeanNotificationInfo[] infos=
                new MBeanNotificationInfo[len];
        System.arraycopy(MBeanServerDelegate.notifsInfo,0,infos,0,len);
        return infos;
    }

    // From NotificationEmitter extends NotificationBroacaster
    //
    public synchronized void removeNotificationListener(NotificationListener listener,
                                                        NotificationFilter filter,
                                                        Object handback)
            throws ListenerNotFoundException{
        broadcaster.removeNotificationListener(listener,filter,handback);
    }

    public void sendNotification(Notification notification){
        if(notification.getSequenceNumber()<1){
            synchronized(this){
                notification.setSequenceNumber(this.sequenceNumber++);
            }
        }
        broadcaster.sendNotification(notification);
    }
}
