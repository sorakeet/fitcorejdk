/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.security.auth.Subject;

public class ClientListenerInfo{
    private final ObjectName name;
    private final Integer listenerID;
    private final NotificationFilter filter;
    private final NotificationListener listener;
    private final Object handback;
    private final Subject delegationSubject;

    public ClientListenerInfo(Integer listenerID,
                              ObjectName name,
                              NotificationListener listener,
                              NotificationFilter filter,
                              Object handback,
                              Subject delegationSubject){
        this.listenerID=listenerID;
        this.name=name;
        this.listener=listener;
        this.filter=filter;
        this.handback=handback;
        this.delegationSubject=delegationSubject;
    }

    public Integer getListenerID(){
        return listenerID;
    }

    public Subject getDelegationSubject(){
        return delegationSubject;
    }

    public boolean sameAs(ObjectName name){
        return (getObjectName().equals(name));
    }

    public ObjectName getObjectName(){
        return name;
    }

    public boolean sameAs(ObjectName name,NotificationListener listener){
        return (getObjectName().equals(name)&&
                getListener()==listener);
    }

    public NotificationListener getListener(){
        return listener;
    }

    public boolean sameAs(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback){
        return (getObjectName().equals(name)&&
                getListener()==listener&&
                getNotificationFilter()==filter&&
                getHandback()==handback);
    }

    public NotificationFilter getNotificationFilter(){
        return filter;
    }

    public Object getHandback(){
        return handback;
    }
}
