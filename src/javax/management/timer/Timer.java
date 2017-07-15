/**
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.timer;

import javax.management.*;
import java.util.*;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.TIMER_LOGGER;
// jmx imports
//

public class Timer extends NotificationBroadcasterSupport
        implements TimerMBean, MBeanRegistration{
    public static final long ONE_SECOND=1000;
    public static final long ONE_MINUTE=60*ONE_SECOND;
    public static final long ONE_HOUR=60*ONE_MINUTE;
    public static final long ONE_DAY=24*ONE_HOUR;
    public static final long ONE_WEEK=7*ONE_DAY;
    // Flags needed to keep the indexes of the objects in the array.
    //
    private static final int TIMER_NOTIF_INDEX=0;
    private static final int TIMER_DATE_INDEX=1;
    private static final int TIMER_PERIOD_INDEX=2;
    private static final int TIMER_NB_OCCUR_INDEX=3;
    private static final int ALARM_CLOCK_INDEX=4;
    private static final int FIXED_RATE_INDEX=5;
    final private Map<Integer,Object[]> timerTable=
            new HashMap<>();
    private boolean sendPastNotifications=false;
    private transient boolean isActive=false;
    private transient long sequenceNumber=0;
    volatile private int counterID=0;
    private java.util.Timer timer;

    public Timer(){
    }

    public ObjectName preRegister(MBeanServer server,ObjectName name)
            throws Exception{
        return name;
    }

    public void postRegister(Boolean registrationDone){
    }

    public void preDeregister() throws Exception{
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "preDeregister","stop the timer");
        // Stop the timer.
        //
        stop();
    }

    public void postDeregister(){
    }

    public synchronized MBeanNotificationInfo[] getNotificationInfo(){
        Set<String> notifTypes=new TreeSet<String>();
        for(Object[] entry : timerTable.values()){
            TimerNotification notif=(TimerNotification)
                    entry[TIMER_NOTIF_INDEX];
            notifTypes.add(notif.getType());
        }
        String[] notifTypesArray=
                notifTypes.toArray(new String[0]);
        return new MBeanNotificationInfo[]{
                new MBeanNotificationInfo(notifTypesArray,
                        TimerNotification.class.getName(),
                        "Notification sent by Timer MBean")
        };
    }

    public synchronized void start(){
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "start","starting the timer");
        // Start the TimerAlarmClock.
        //
        if(isActive==false){
            timer=new java.util.Timer();
            TimerAlarmClock alarmClock;
            Date date;
            Date currentDate=new Date();
            // Send or not past notifications depending on the flag.
            // Update the date and the number of occurrences of past notifications
            // to make them later than the current date.
            //
            sendPastNotifications(currentDate,sendPastNotifications);
            // Update and start all the TimerAlarmClocks.
            // Here, all the notifications in the timer table are later than the current date.
            //
            for(Object[] obj : timerTable.values()){
                // Retrieve the date notification and the TimerAlarmClock.
                //
                date=(Date)obj[TIMER_DATE_INDEX];
                // Update all the TimerAlarmClock timeouts and start them.
                //
                boolean fixedRate=((Boolean)obj[FIXED_RATE_INDEX]).booleanValue();
                if(fixedRate){
                    alarmClock=new TimerAlarmClock(this,date);
                    obj[ALARM_CLOCK_INDEX]=(Object)alarmClock;
                    timer.schedule(alarmClock,alarmClock.next);
                }else{
                    alarmClock=new TimerAlarmClock(this,(date.getTime()-currentDate.getTime()));
                    obj[ALARM_CLOCK_INDEX]=(Object)alarmClock;
                    timer.schedule(alarmClock,alarmClock.timeout);
                }
            }
            // Set the state to ON.
            //
            isActive=true;
            TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                    "start","timer started");
        }else{
            TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                    "start","the timer is already activated");
        }
    }

    public synchronized void stop(){
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "stop","stopping the timer");
        // Stop the TimerAlarmClock.
        //
        if(isActive==true){
            for(Object[] obj : timerTable.values()){
                // Stop all the TimerAlarmClock.
                //
                TimerAlarmClock alarmClock=(TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
                if(alarmClock!=null){
//                     alarmClock.interrupt();
//                     try {
//                         // Wait until the thread die.
//                         //
//                         alarmClock.join();
//                     } catch (InterruptedException ex) {
//                         // Ignore...
//                     }
//                     // Remove the reference on the TimerAlarmClock.
//                     //
                    alarmClock.cancel();
                }
            }
            timer.cancel();
            // Set the state to OFF.
            //
            isActive=false;
            TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                    "stop","timer stopped");
        }else{
            TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                    "stop","the timer is already deactivated");
        }
    }
    // NPCTE fix for bugId 4464388, esc 0,  MR, to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData,
//                                                Date date, long period, long nbOccurences)
// end of NPCTE fix for bugId 4464388

    public synchronized Integer addNotification(String type,String message,Object userData,
                                                Date date,long period,long nbOccurences,boolean fixedRate)
            throws IllegalArgumentException{
        if(date==null){
            throw new IllegalArgumentException("Timer notification date cannot be null.");
        }
        // Check that all the timer notification attributes are valid.
        //
        // Invalid timer period value exception:
        // Check that the period and the nbOccurences are POSITIVE VALUES.
        //
        if((period<0)||(nbOccurences<0)){
            throw new IllegalArgumentException("Negative values for the periodicity");
        }
        Date currentDate=new Date();
        // Update the date if it is before the current date.
        //
        if(currentDate.after(date)){
            date.setTime(currentDate.getTime());
            if(TIMER_LOGGER.isLoggable(Level.FINER)){
                TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                        "addNotification",
                        "update timer notification to add with:"+
                                "\n\tNotification date = "+date);
            }
        }
        // Create and add the timer notification into the timer table.
        //
        Integer notifID=Integer.valueOf(++counterID);
        // The sequenceNumber and the timeStamp attributes are updated
        // when the notification is emitted by the timer.
        //
        TimerNotification notif=new TimerNotification(type,this,0,0,message,notifID);
        notif.setUserData(userData);
        Object[] obj=new Object[6];
        TimerAlarmClock alarmClock;
        if(fixedRate){
            alarmClock=new TimerAlarmClock(this,date);
        }else{
            alarmClock=new TimerAlarmClock(this,(date.getTime()-currentDate.getTime()));
        }
        // Fix bug 00417.B
        // The date registered into the timer is a clone from the date parameter.
        //
        Date d=new Date(date.getTime());
        obj[TIMER_NOTIF_INDEX]=(Object)notif;
        obj[TIMER_DATE_INDEX]=(Object)d;
        obj[TIMER_PERIOD_INDEX]=(Object)period;
        obj[TIMER_NB_OCCUR_INDEX]=(Object)nbOccurences;
        obj[ALARM_CLOCK_INDEX]=(Object)alarmClock;
        obj[FIXED_RATE_INDEX]=Boolean.valueOf(fixedRate);
        if(TIMER_LOGGER.isLoggable(Level.FINER)){
            StringBuilder strb=new StringBuilder()
                    .append("adding timer notification:\n\t")
                    .append("Notification source = ")
                    .append(notif.getSource())
                    .append("\n\tNotification type = ")
                    .append(notif.getType())
                    .append("\n\tNotification ID = ")
                    .append(notifID)
                    .append("\n\tNotification date = ")
                    .append(d)
                    .append("\n\tNotification period = ")
                    .append(period)
                    .append("\n\tNotification nb of occurrences = ")
                    .append(nbOccurences)
                    .append("\n\tNotification executes at fixed rate = ")
                    .append(fixedRate);
            TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                    "addNotification",strb.toString());
        }
        timerTable.put(notifID,obj);
        // Update and start the TimerAlarmClock.
        //
        if(isActive==true){
            if(fixedRate){
                timer.schedule(alarmClock,alarmClock.next);
            }else{
                timer.schedule(alarmClock,alarmClock.timeout);
            }
        }
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "addNotification","timer notification added");
        return notifID;
    }
    // NPCTE fix for bugId 4464388, esc 0,  MR , to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData,
//                                              Date date, long period)
// end of NPCTE fix for bugId 4464388 */

    public synchronized Integer addNotification(String type,String message,Object userData,
                                                Date date,long period,long nbOccurences)
            throws IllegalArgumentException{
        return addNotification(type,message,userData,date,period,nbOccurences,false);
    }
    // NPCTE fix for bugId 4464388, esc 0,  MR , to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData,
//                                              Date date, long period)
// end of NPCTE fix for bugId 4464388 */

    public synchronized Integer addNotification(String type,String message,Object userData,
                                                Date date,long period)
            throws IllegalArgumentException{
        return (addNotification(type,message,userData,date,period,0));
    }
    // NPCTE fix for bugId 4464388, esc 0,  MR, to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData, Date date)
//      throws java.lang.IllegalArgumentException {
// end of NPCTE fix for bugId 4464388

    public synchronized Integer addNotification(String type,String message,Object userData,Date date)
            throws IllegalArgumentException{
        return (addNotification(type,message,userData,date,0,0));
    }

    public synchronized void removeNotification(Integer id) throws InstanceNotFoundException{
        // Check that the notification to remove is effectively in the timer table.
        //
        if(timerTable.containsKey(id)==false){
            throw new InstanceNotFoundException("Timer notification to remove not in the list of notifications");
        }
        // Stop the TimerAlarmClock.
        //
        Object[] obj=timerTable.get(id);
        TimerAlarmClock alarmClock=(TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
        if(alarmClock!=null){
//             alarmClock.interrupt();
//             try {
//                 // Wait until the thread die.
//                 //
//                 alarmClock.join();
//             } catch (InterruptedException e) {
//                 // Ignore...
//             }
//             // Remove the reference on the TimerAlarmClock.
//             //
            alarmClock.cancel();
        }
        // Remove the timer notification from the timer table.
        //
        if(TIMER_LOGGER.isLoggable(Level.FINER)){
            StringBuilder strb=new StringBuilder()
                    .append("removing timer notification:")
                    .append("\n\tNotification source = ")
                    .append(((TimerNotification)obj[TIMER_NOTIF_INDEX]).getSource())
                    .append("\n\tNotification type = ")
                    .append(((TimerNotification)obj[TIMER_NOTIF_INDEX]).getType())
                    .append("\n\tNotification ID = ")
                    .append(((TimerNotification)obj[TIMER_NOTIF_INDEX]).getNotificationID())
                    .append("\n\tNotification date = ")
                    .append(obj[TIMER_DATE_INDEX])
                    .append("\n\tNotification period = ")
                    .append(obj[TIMER_PERIOD_INDEX])
                    .append("\n\tNotification nb of occurrences = ")
                    .append(obj[TIMER_NB_OCCUR_INDEX])
                    .append("\n\tNotification executes at fixed rate = ")
                    .append(obj[FIXED_RATE_INDEX]);
            TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                    "removeNotification",strb.toString());
        }
        timerTable.remove(id);
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "removeNotification","timer notification removed");
    }

    public synchronized void removeNotifications(String type) throws InstanceNotFoundException{
        Vector<Integer> v=getNotificationIDs(type);
        if(v.isEmpty())
            throw new InstanceNotFoundException("Timer notifications to remove not in the list of notifications");
        for(Integer i : v)
            removeNotification(i);
    }

    public synchronized void removeAllNotifications(){
        TimerAlarmClock alarmClock;
        for(Object[] obj : timerTable.values()){
            // Stop the TimerAlarmClock.
            //
            alarmClock=(TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
//             if (alarmClock != null) {
//                 alarmClock.interrupt();
//                 try {
//                     // Wait until the thread die.
//                     //
//                     alarmClock.join();
//                 } catch (InterruptedException ex) {
//                     // Ignore...
//                 }
            // Remove the reference on the TimerAlarmClock.
            //
//             }
            alarmClock.cancel();
        }
        // Remove all the timer notifications from the timer table.
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "removeAllNotifications","removing all timer notifications");
        timerTable.clear();
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "removeAllNotifications","all timer notifications removed");
        // Reset the counterID.
        //
        counterID=0;
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "removeAllNotifications","timer notification counter ID reset");
    }
    // GETTERS AND SETTERS
    //--------------------

    public synchronized int getNbNotifications(){
        return timerTable.size();
    }

    public synchronized Vector<Integer> getAllNotificationIDs(){
        return new Vector<Integer>(timerTable.keySet());
    }

    public synchronized Vector<Integer> getNotificationIDs(String type){
        String s;
        Vector<Integer> v=new Vector<Integer>();
        for(Map.Entry<Integer,Object[]> entry : timerTable.entrySet()){
            Object[] obj=entry.getValue();
            s=((TimerNotification)obj[TIMER_NOTIF_INDEX]).getType();
            if((type==null)?s==null:type.equals(s))
                v.addElement(entry.getKey());
        }
        return v;
    }
    // 5089997: return is Vector<Integer> not Vector<TimerNotification>

    public synchronized String getNotificationType(Integer id){
        Object[] obj=timerTable.get(id);
        if(obj!=null){
            return (((TimerNotification)obj[TIMER_NOTIF_INDEX]).getType());
        }
        return null;
    }

    public synchronized String getNotificationMessage(Integer id){
        Object[] obj=timerTable.get(id);
        if(obj!=null){
            return (((TimerNotification)obj[TIMER_NOTIF_INDEX]).getMessage());
        }
        return null;
    }
    // NPCTE fix for bugId 4464388, esc 0, MR, 03 sept 2001, to be added after modification of jmx spec
    //public Serializable getNotificationUserData(Integer id) {
    // end of NPCTE fix for bugId 4464388

    public synchronized Object getNotificationUserData(Integer id){
        Object[] obj=timerTable.get(id);
        if(obj!=null){
            return (((TimerNotification)obj[TIMER_NOTIF_INDEX]).getUserData());
        }
        return null;
    }

    public synchronized Date getDate(Integer id){
        Object[] obj=timerTable.get(id);
        if(obj!=null){
            Date date=(Date)obj[TIMER_DATE_INDEX];
            return (new Date(date.getTime()));
        }
        return null;
    }

    public synchronized Long getPeriod(Integer id){
        Object[] obj=timerTable.get(id);
        if(obj!=null){
            return (Long)obj[TIMER_PERIOD_INDEX];
        }
        return null;
    }

    public synchronized Long getNbOccurences(Integer id){
        Object[] obj=timerTable.get(id);
        if(obj!=null){
            return (Long)obj[TIMER_NB_OCCUR_INDEX];
        }
        return null;
    }

    public synchronized Boolean getFixedRate(Integer id){
        Object[] obj=timerTable.get(id);
        if(obj!=null){
            Boolean fixedRate=(Boolean)obj[FIXED_RATE_INDEX];
            return (Boolean.valueOf(fixedRate.booleanValue()));
        }
        return null;
    }

    public boolean getSendPastNotifications(){
        return sendPastNotifications;
    }

    public void setSendPastNotifications(boolean value){
        sendPastNotifications=value;
    }

    public boolean isActive(){
        return isActive;
    }

    public synchronized boolean isEmpty(){
        return (timerTable.isEmpty());
    }

    private synchronized void sendPastNotifications(Date currentDate,boolean currentFlag){
        TimerNotification notif;
        Integer notifID;
        Date date;
        ArrayList<Object[]> values=
                new ArrayList<Object[]>(timerTable.values());
        for(Object[] obj : values){
            // Retrieve the timer notification and the date notification.
            //
            notif=(TimerNotification)obj[TIMER_NOTIF_INDEX];
            notifID=notif.getNotificationID();
            date=(Date)obj[TIMER_DATE_INDEX];
            // Update the timer notification while:
            //  - the timer notification date is earlier than the current date
            //  - the timer notification has not been removed from the timer table.
            //
            while((currentDate.after(date))&&(timerTable.containsKey(notifID))){
                if(currentFlag==true){
                    if(TIMER_LOGGER.isLoggable(Level.FINER)){
                        StringBuilder strb=new StringBuilder()
                                .append("sending past timer notification:")
                                .append("\n\tNotification source = ")
                                .append(notif.getSource())
                                .append("\n\tNotification type = ")
                                .append(notif.getType())
                                .append("\n\tNotification ID = ")
                                .append(notif.getNotificationID())
                                .append("\n\tNotification date = ")
                                .append(date)
                                .append("\n\tNotification period = ")
                                .append(obj[TIMER_PERIOD_INDEX])
                                .append("\n\tNotification nb of occurrences = ")
                                .append(obj[TIMER_NB_OCCUR_INDEX])
                                .append("\n\tNotification executes at fixed rate = ")
                                .append(obj[FIXED_RATE_INDEX]);
                        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                                "sendPastNotifications",strb.toString());
                    }
                    sendNotification(date,notif);
                    TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                            "sendPastNotifications","past timer notification sent");
                }
                // Update the date and the number of occurrences of the timer notification.
                //
                updateTimerTable(notif.getNotificationID());
            }
        }
    }

    private synchronized void updateTimerTable(Integer notifID){
        // Retrieve the timer notification and the TimerAlarmClock.
        //
        Object[] obj=timerTable.get(notifID);
        Date date=(Date)obj[TIMER_DATE_INDEX];
        Long period=(Long)obj[TIMER_PERIOD_INDEX];
        Long nbOccurences=(Long)obj[TIMER_NB_OCCUR_INDEX];
        Boolean fixedRate=(Boolean)obj[FIXED_RATE_INDEX];
        TimerAlarmClock alarmClock=(TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
        if(period.longValue()!=0){
            // Update the date and the number of occurrences of the timer notification
            // and the TimerAlarmClock time out.
            // NOTES :
            //   nbOccurences = 0 notifies an infinite periodicity.
            //   nbOccurences = 1 notifies a finite periodicity that has reached its end.
            //   nbOccurences > 1 notifies a finite periodicity that has not yet reached its end.
            //
            if((nbOccurences.longValue()==0)||(nbOccurences.longValue()>1)){
                date.setTime(date.getTime()+period.longValue());
                obj[TIMER_NB_OCCUR_INDEX]=Long.valueOf(Math.max(0L,(nbOccurences.longValue()-1)));
                nbOccurences=(Long)obj[TIMER_NB_OCCUR_INDEX];
                if(isActive==true){
                    if(fixedRate.booleanValue()){
                        alarmClock=new TimerAlarmClock(this,date);
                        obj[ALARM_CLOCK_INDEX]=(Object)alarmClock;
                        timer.schedule(alarmClock,alarmClock.next);
                    }else{
                        alarmClock=new TimerAlarmClock(this,period.longValue());
                        obj[ALARM_CLOCK_INDEX]=(Object)alarmClock;
                        timer.schedule(alarmClock,alarmClock.timeout);
                    }
                }
                if(TIMER_LOGGER.isLoggable(Level.FINER)){
                    TimerNotification notif=(TimerNotification)obj[TIMER_NOTIF_INDEX];
                    StringBuilder strb=new StringBuilder()
                            .append("update timer notification with:")
                            .append("\n\tNotification source = ")
                            .append(notif.getSource())
                            .append("\n\tNotification type = ")
                            .append(notif.getType())
                            .append("\n\tNotification ID = ")
                            .append(notifID)
                            .append("\n\tNotification date = ")
                            .append(date)
                            .append("\n\tNotification period = ")
                            .append(period)
                            .append("\n\tNotification nb of occurrences = ")
                            .append(nbOccurences)
                            .append("\n\tNotification executes at fixed rate = ")
                            .append(fixedRate);
                    TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                            "updateTimerTable",strb.toString());
                }
            }else{
                if(alarmClock!=null){
//                     alarmClock.interrupt();
//                     try {
//                         // Wait until the thread die.
//                         //
//                         alarmClock.join();
//                     } catch (InterruptedException e) {
//                         // Ignore...
//                     }
                    alarmClock.cancel();
                }
                timerTable.remove(notifID);
            }
        }else{
            if(alarmClock!=null){
//                 alarmClock.interrupt();
//                 try {
//                     // Wait until the thread die.
//                     //
//                     alarmClock.join();
//                 } catch (InterruptedException e) {
//                     // Ignore...
//                 }
                alarmClock.cancel();
            }
            timerTable.remove(notifID);
        }
    }

    void sendNotification(Date timeStamp,TimerNotification notification){
        if(TIMER_LOGGER.isLoggable(Level.FINER)){
            StringBuilder strb=new StringBuilder()
                    .append("sending timer notification:")
                    .append("\n\tNotification source = ")
                    .append(notification.getSource())
                    .append("\n\tNotification type = ")
                    .append(notification.getType())
                    .append("\n\tNotification ID = ")
                    .append(notification.getNotificationID())
                    .append("\n\tNotification date = ")
                    .append(timeStamp);
            TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                    "sendNotification",strb.toString());
        }
        long curSeqNumber;
        synchronized(this){
            sequenceNumber=sequenceNumber+1;
            curSeqNumber=sequenceNumber;
        }
        synchronized(notification){
            notification.setTimeStamp(timeStamp.getTime());
            notification.setSequenceNumber(curSeqNumber);
            this.sendNotification((TimerNotification)notification.cloneTimerNotification());
        }
        TIMER_LOGGER.logp(Level.FINER,Timer.class.getName(),
                "sendNotification","timer notification sent");
    }

    @SuppressWarnings("deprecation")
    void notifyAlarmClock(TimerAlarmClockNotification notification){
        TimerNotification timerNotification=null;
        Date timerDate=null;
        // Retrieve the timer notification associated to the alarm-clock.
        //
        TimerAlarmClock alarmClock=(TimerAlarmClock)notification.getSource();
        synchronized(Timer.this){
            for(Object[] obj : timerTable.values()){
                if(obj[ALARM_CLOCK_INDEX]==alarmClock){
                    timerNotification=(TimerNotification)obj[TIMER_NOTIF_INDEX];
                    timerDate=(Date)obj[TIMER_DATE_INDEX];
                    break;
                }
            }
        }
        // Notify the timer.
        //
        sendNotification(timerDate,timerNotification);
        // Update the notification and the TimerAlarmClock timeout.
        //
        updateTimerTable(timerNotification.getNotificationID());
    }
}
