/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.remote.util.ClassLogger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

public class NotificationBroadcasterSupport implements NotificationEmitter{
    private final static Executor defaultExecutor=new Executor(){
        // DirectExecutor using caller thread
        public void execute(Runnable r){
            r.run();
        }
    };
    private static final MBeanNotificationInfo[] NO_NOTIFICATION_INFO=
            new MBeanNotificationInfo[0];
    private static final ClassLogger logger=
            new ClassLogger("javax.management","NotificationBroadcasterSupport");
    // since 1.6
    private final Executor executor;
    private final MBeanNotificationInfo[] notifInfo;
    private List<ListenerInfo> listenerList=
            new CopyOnWriteArrayList<ListenerInfo>();

    public NotificationBroadcasterSupport(){
        this(null,(MBeanNotificationInfo[])null);
    }

    public NotificationBroadcasterSupport(Executor executor,
                                          MBeanNotificationInfo... info){
        this.executor=(executor!=null)?executor:defaultExecutor;
        notifInfo=info==null?NO_NOTIFICATION_INFO:info.clone();
    }

    public NotificationBroadcasterSupport(Executor executor){
        this(executor,(MBeanNotificationInfo[])null);
    }

    public NotificationBroadcasterSupport(MBeanNotificationInfo... info){
        this(null,info);
    }

    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback){
        if(listener==null){
            throw new IllegalArgumentException("Listener can't be null");
        }
        listenerList.add(new ListenerInfo(listener,filter,handback));
    }

    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException{
        ListenerInfo wildcard=new WildcardListenerInfo(listener);
        boolean removed=
                listenerList.removeAll(Collections.singleton(wildcard));
        if(!removed)
            throw new ListenerNotFoundException("Listener not registered");
    }

    public MBeanNotificationInfo[] getNotificationInfo(){
        if(notifInfo.length==0)
            return notifInfo;
        else
            return notifInfo.clone();
    }

    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws ListenerNotFoundException{
        ListenerInfo li=new ListenerInfo(listener,filter,handback);
        boolean removed=listenerList.remove(li);
        if(!removed){
            throw new ListenerNotFoundException("Listener not registered "+
                    "(with this filter and "+
                    "handback)");
            // or perhaps not registered at all
        }
    }

    public void sendNotification(Notification notification){
        if(notification==null){
            return;
        }
        boolean enabled;
        for(ListenerInfo li : listenerList){
            try{
                enabled=li.filter==null||
                        li.filter.isNotificationEnabled(notification);
            }catch(Exception e){
                if(logger.debugOn()){
                    logger.debug("sendNotification",e);
                }
                continue;
            }
            if(enabled){
                executor.execute(new SendNotifJob(notification,li));
            }
        }
    }

    protected void handleNotification(NotificationListener listener,
                                      Notification notif,Object handback){
        listener.handleNotification(notif,handback);
    }

    // private stuff
    private static class ListenerInfo{
        NotificationListener listener;
        NotificationFilter filter;
        Object handback;

        ListenerInfo(NotificationListener listener,
                     NotificationFilter filter,
                     Object handback){
            this.listener=listener;
            this.filter=filter;
            this.handback=handback;
        }

        @Override
        public int hashCode(){
            return Objects.hashCode(listener);
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof ListenerInfo))
                return false;
            ListenerInfo li=(ListenerInfo)o;
            if(li instanceof WildcardListenerInfo)
                return (li.listener==listener);
            else
                return (li.listener==listener&&li.filter==filter
                        &&li.handback==handback);
        }
    }

    private static class WildcardListenerInfo extends ListenerInfo{
        WildcardListenerInfo(NotificationListener listener){
            super(listener,null,null);
        }

        @Override
        public boolean equals(Object o){
            assert (!(o instanceof WildcardListenerInfo));
            return o.equals(this);
        }

        @Override
        public int hashCode(){
            return super.hashCode();
        }
    }

    private class SendNotifJob implements Runnable{
        private final Notification notif;
        private final ListenerInfo listenerInfo;

        public SendNotifJob(Notification notif,ListenerInfo listenerInfo){
            this.notif=notif;
            this.listenerInfo=listenerInfo;
        }

        public void run(){
            try{
                handleNotification(listenerInfo.listener,
                        notif,listenerInfo.handback);
            }catch(Exception e){
                if(logger.debugOn()){
                    logger.debug("SendNotifJob-run",e);
                }
            }
        }
    }
}
