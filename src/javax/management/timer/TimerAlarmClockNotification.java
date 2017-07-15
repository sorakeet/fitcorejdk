/**
 * Copyright (c) 2002, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.timer;

class TimerAlarmClockNotification
        extends javax.management.Notification{
    private static final long serialVersionUID=-4841061275673620641L;

    public TimerAlarmClockNotification(TimerAlarmClock source){
        super("",source,0);
    }
}
