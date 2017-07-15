/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import javax.management.Notification;

public class JMXConnectionNotification extends Notification{
    public static final String OPENED="jmx.remote.connection.opened";
    public static final String CLOSED="jmx.remote.connection.closed";
    public static final String FAILED="jmx.remote.connection.failed";
    public static final String NOTIFS_LOST=
            "jmx.remote.connection.notifs.lost";
    private static final long serialVersionUID=-2331308725952627538L;
    private final String connectionId;

    public JMXConnectionNotification(String type,
                                     Object source,
                                     String connectionId,
                                     long sequenceNumber,
                                     String message,
                                     Object userData){
        /** We don't know whether the parent class (Notification) will
         throw an exception if the type or source is null, because
         JMX 1.2 doesn't specify that.  So we make sure it is not
         null, in case it would throw the wrong exception
         (e.g. IllegalArgumentException instead of
         NullPointerException).  Likewise for the sequence number.  */
        super((String)nonNull(type),
                nonNull(source),
                Math.max(0,sequenceNumber),
                System.currentTimeMillis(),
                message);
        if(type==null||source==null||connectionId==null)
            throw new NullPointerException("Illegal null argument");
        if(sequenceNumber<0)
            throw new IllegalArgumentException("Negative sequence number");
        this.connectionId=connectionId;
        setUserData(userData);
    }

    private static Object nonNull(Object arg){
        if(arg==null)
            return "";
        else
            return arg;
    }

    public String getConnectionId(){
        return connectionId;
    }
}
