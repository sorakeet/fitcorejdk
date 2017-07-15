/**
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.timer;

import java.util.Date;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.TIMER_LOGGER;

class TimerAlarmClock extends java.util.TimerTask{
    Timer listener=null;
    long timeout=10000;
    Date next=null;

    public TimerAlarmClock(Timer listener,long timeout){
        this.listener=listener;
        this.timeout=Math.max(0L,timeout);
    }

    public TimerAlarmClock(Timer listener,Date next){
        this.listener=listener;
        this.next=next;
    }

    public void run(){
        try{
            //this.sleep(timeout);
            TimerAlarmClockNotification notif=new TimerAlarmClockNotification(this);
            listener.notifyAlarmClock(notif);
        }catch(Exception e){
            TIMER_LOGGER.logp(Level.FINEST,Timer.class.getName(),"run",
                    "Got unexpected exception when sending a notification",e);
        }
    }
}
