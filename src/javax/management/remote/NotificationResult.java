/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class NotificationResult implements Serializable{
    private static final long serialVersionUID=1191800228721395279L;
    private long earliestSequenceNumber;
    private long nextSequenceNumber;
    private TargetedNotification[] targetedNotifications;

    public NotificationResult(long earliestSequenceNumber,
                              long nextSequenceNumber,
                              TargetedNotification[] targetedNotifications){
        validate(targetedNotifications,earliestSequenceNumber,nextSequenceNumber);
        this.earliestSequenceNumber=earliestSequenceNumber;
        this.nextSequenceNumber=nextSequenceNumber;
        this.targetedNotifications=(targetedNotifications.length==0?targetedNotifications:targetedNotifications.clone());
    }

    private static void validate(TargetedNotification[] targetedNotifications,
                                 long earliestSequenceNumber,
                                 long nextSequenceNumber)
            throws IllegalArgumentException{
        if(targetedNotifications==null){
            final String msg="Notifications null";
            throw new IllegalArgumentException(msg);
        }
        if(earliestSequenceNumber<0||nextSequenceNumber<0)
            throw new IllegalArgumentException("Bad sequence numbers");
        /** We used to check nextSequenceNumber >= earliestSequenceNumber
         here.  But in fact the opposite can legitimately be true if
         notifications have been lost.  */
    }

    public String toString(){
        return "NotificationResult: earliest="+getEarliestSequenceNumber()+
                "; next="+getNextSequenceNumber()+"; nnotifs="+
                getTargetedNotifications().length;
    }

    public long getEarliestSequenceNumber(){
        return earliestSequenceNumber;
    }

    public long getNextSequenceNumber(){
        return nextSequenceNumber;
    }

    public TargetedNotification[] getTargetedNotifications(){
        return targetedNotifications.length==0?targetedNotifications:targetedNotifications.clone();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        ois.defaultReadObject();
        try{
            validate(
                    this.targetedNotifications,
                    this.earliestSequenceNumber,
                    this.nextSequenceNumber
            );
            this.targetedNotifications=this.targetedNotifications.length==0?
                    this.targetedNotifications:
                    this.targetedNotifications.clone();
        }catch(IllegalArgumentException e){
            throw new InvalidObjectException(e.getMessage());
        }
    }
}
