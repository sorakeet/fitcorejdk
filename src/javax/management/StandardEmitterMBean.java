/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class StandardEmitterMBean extends StandardMBean
        implements NotificationEmitter{
    private static final MBeanNotificationInfo[] NO_NOTIFICATION_INFO=
            new MBeanNotificationInfo[0];
    private final NotificationEmitter emitter;
    private final MBeanNotificationInfo[] notificationInfo;

    public <T> StandardEmitterMBean(T implementation,Class<T> mbeanInterface,
                                    NotificationEmitter emitter){
        this(implementation,mbeanInterface,false,emitter);
    }

    public <T> StandardEmitterMBean(T implementation,Class<T> mbeanInterface,
                                    boolean isMXBean,
                                    NotificationEmitter emitter){
        super(implementation,mbeanInterface,isMXBean);
        if(emitter==null)
            throw new IllegalArgumentException("Null emitter");
        this.emitter=emitter;
        MBeanNotificationInfo[] infos=emitter.getNotificationInfo();
        if(infos==null||infos.length==0){
            this.notificationInfo=NO_NOTIFICATION_INFO;
        }else{
            this.notificationInfo=infos.clone();
        }
    }

    protected StandardEmitterMBean(Class<?> mbeanInterface,
                                   NotificationEmitter emitter){
        this(mbeanInterface,false,emitter);
    }

    protected StandardEmitterMBean(Class<?> mbeanInterface,boolean isMXBean,
                                   NotificationEmitter emitter){
        super(mbeanInterface,isMXBean);
        if(emitter==null)
            throw new IllegalArgumentException("Null emitter");
        this.emitter=emitter;
        MBeanNotificationInfo[] infos=emitter.getNotificationInfo();
        if(infos==null||infos.length==0){
            this.notificationInfo=NO_NOTIFICATION_INFO;
        }else{
            this.notificationInfo=infos.clone();
        }
    }

    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws ListenerNotFoundException{
        emitter.removeNotificationListener(listener,filter,handback);
    }

    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback){
        emitter.addNotificationListener(listener,filter,handback);
    }

    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException{
        emitter.removeNotificationListener(listener);
    }

    public MBeanNotificationInfo[] getNotificationInfo(){
        // this getter might get called from the super constructor
        // when the notificationInfo has not been properly set yet
        if(notificationInfo==null){
            return NO_NOTIFICATION_INFO;
        }
        if(notificationInfo.length==0){
            return notificationInfo;
        }else{
            return notificationInfo.clone();
        }
    }

    public void sendNotification(Notification n){
        if(emitter instanceof NotificationBroadcasterSupport)
            ((NotificationBroadcasterSupport)emitter).sendNotification(n);
        else{
            final String msg=
                    "Cannot sendNotification when emitter is not an "+
                            "instance of NotificationBroadcasterSupport: "+
                            emitter.getClass().getName();
            throw new ClassCastException(msg);
        }
    }

    @Override
    MBeanNotificationInfo[] getNotifications(MBeanInfo info){
        return getNotificationInfo();
    }
}
