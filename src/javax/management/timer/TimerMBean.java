/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.timer;
// java imports
//

import javax.management.InstanceNotFoundException;
import java.util.Date;
import java.util.Vector;
// NPCTE fix for bugId 4464388, esc 0,  MR , to be added after modification of jmx spec
//import java.io.Serializable;
// end of NPCTE fix for bugId 4464388
// jmx imports
//

public interface TimerMBean{
    public void start();

    public void stop();
    // NPCTE fix for bugId 4464388, esc 0,  MR, to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData,
//                                                Date date, long period, long nbOccurences)
// end of NPCTE fix for bugId 4464388

    public Integer addNotification(String type,String message,Object userData,
                                   Date date,long period,long nbOccurences,boolean fixedRate)
            throws IllegalArgumentException;
    // NPCTE fix for bugId 4464388, esc 0,  MR , to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData,
//                                              Date date, long period)
// end of NPCTE fix for bugId 4464388 */

    public Integer addNotification(String type,String message,Object userData,
                                   Date date,long period,long nbOccurences)
            throws IllegalArgumentException;
    // NPCTE fix for bugId 4464388, esc 0,  MR , to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData,
//                                              Date date, long period)
// end of NPCTE fix for bugId 4464388 */

    public Integer addNotification(String type,String message,Object userData,
                                   Date date,long period)
            throws IllegalArgumentException;
    // NPCTE fix for bugId 4464388, esc 0,  MR, to be added after modification of jmx spec
//  public synchronized Integer addNotification(String type, String message, Serializable userData, Date date)
//      throws java.lang.IllegalArgumentException {
// end of NPCTE fix for bugId 4464388

    public Integer addNotification(String type,String message,Object userData,Date date)
            throws IllegalArgumentException;

    public void removeNotification(Integer id) throws InstanceNotFoundException;

    public void removeNotifications(String type) throws InstanceNotFoundException;

    public void removeAllNotifications();
    // GETTERS AND SETTERS
    //--------------------

    public int getNbNotifications();

    public Vector<Integer> getAllNotificationIDs();

    public Vector<Integer> getNotificationIDs(String type);

    public String getNotificationType(Integer id);

    public String getNotificationMessage(Integer id);

    // NPCTE fix for bugId 4464388, esc 0 , MR , 03 sept 2001 , to be added after modification of jmx spec
    //public Serializable getNotificationUserData(Integer id);
    // end of NPCTE fix for bugId 4464388
    public Object getNotificationUserData(Integer id);

    public Date getDate(Integer id);

    public Long getPeriod(Integer id);

    public Long getNbOccurences(Integer id);

    public Boolean getFixedRate(Integer id);

    public boolean getSendPastNotifications();

    public void setSendPastNotifications(boolean value);

    public boolean isActive();

    public boolean isEmpty();
}
