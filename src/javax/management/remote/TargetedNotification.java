/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import javax.management.Notification;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class TargetedNotification implements Serializable{
    private static final long serialVersionUID=7676132089779300926L;
// If we replace Integer with int...
//     /**
//      * <p>Constructs a <code>TargetedNotification</code> object.  The
//      * object contains a pair (Notification, Listener ID).
//      * The Listener ID identifies the client listener to which that
//      * notification is targeted. The client listener ID is one
//      * previously returned by the connector server in response to an
//      * <code>addNotificationListener</code> request.</p>
//      * @param notification Notification emitted from the MBean server.
//      * @param listenerID   The ID of the listener to which this
//      *        notification is targeted.
//      */
//     public TargetedNotification(Notification notification,
//                              int listenerID) {
//      this.notif = notification;
//      this.id = listenerID;
//     }
    private Notification notif;
    private Integer id;

    public TargetedNotification(Notification notification,
                                Integer listenerID){
        validate(notification,listenerID);
        // If we replace integer with int...
        // this(notification,intValue(listenerID));
        this.notif=notification;
        this.id=listenerID;
    }

    private static void validate(Notification notif,Integer id) throws IllegalArgumentException{
        if(notif==null){
            throw new IllegalArgumentException("Invalid notification: null");
        }
        if(id==null){
            throw new IllegalArgumentException("Invalid listener ID: null");
        }
    }

    public Notification getNotification(){
        return notif;
    }

    public Integer getListenerID(){
        return id;
    }
    //private final int id;
// Needed if we use int instead of Integer...
//     private static int intValue(Integer id) {
//      if (id == null) throw new
//          IllegalArgumentException("Invalid listener ID: null");
//      return id.intValue();
//     }

    public String toString(){
        return "{"+notif+", "+id+"}";
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        ois.defaultReadObject();
        try{
            validate(this.notif,this.id);
        }catch(IllegalArgumentException e){
            throw new InvalidObjectException(e.getMessage());
        }
    }
}
