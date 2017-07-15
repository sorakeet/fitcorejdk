/**
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

public class UnsolicitedNotificationEvent extends java.util.EventObject{
    private static final long serialVersionUID=-2382603380799883705L;
    private UnsolicitedNotification notice;

    public UnsolicitedNotificationEvent(Object src,
                                        UnsolicitedNotification notice){
        super(src);
        this.notice=notice;
    }

    public UnsolicitedNotification getNotification(){
        return notice;
    }

    public void dispatch(UnsolicitedNotificationListener listener){
        listener.notificationReceived(this);
    }
}
