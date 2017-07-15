/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import com.sun.jmx.remote.util.ClassLogger;
import com.sun.jmx.remote.util.EnvHelp;

import javax.management.*;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

public class ArrayNotificationBuffer implements NotificationBuffer{
    static final String broadcasterClass=
            NotificationBroadcaster.class.getName();
    // FACTORY STUFF, INCLUDING SHARING
    private static final Object globalLock=new Object();
    private static final
    HashMap<MBeanServer,ArrayNotificationBuffer> mbsToBuffer=
            new HashMap<MBeanServer,ArrayNotificationBuffer>(1);
    private static final QueryExp broadcasterQuery=new BroadcasterQuery();
    private static final NotificationFilter creationFilter;
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.misc",
                    "ArrayNotificationBuffer");

    static{
        NotificationFilterSupport nfs=new NotificationFilterSupport();
        nfs.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
        creationFilter=nfs;
    }

    private final Collection<ShareBuffer> sharers=new HashSet<ShareBuffer>(1);
    private final NotificationListener bufferListener=new BufferListener();
    private final MBeanServer mBeanServer;
    // ARRAYNOTIFICATIONBUFFER IMPLEMENTATION
    private final ArrayQueue<NamedNotification> queue;
    private boolean disposed=false;    private synchronized boolean isDisposed(){
        return disposed;
    }
    private int queueSize;    // We no longer support calling this method from outside.
    // The JDK doesn't contain any such calls and users are not
    // supposed to be accessing this class.
    public void dispose(){
        throw new UnsupportedOperationException();
    }
    private long earliestSequenceNumber;    public NotificationResult
    fetchNotifications(NotificationBufferFilter filter,
                       long startSequenceNumber,
                       long timeout,
                       int maxNotifications)
            throws InterruptedException{
        logger.trace("fetchNotifications","starts");
        if(startSequenceNumber<0||isDisposed()){
            synchronized(this){
                return new NotificationResult(earliestSequenceNumber(),
                        nextSequenceNumber(),
                        new TargetedNotification[0]);
            }
        }
        // Check arg validity
        if(filter==null
                ||startSequenceNumber<0||timeout<0
                ||maxNotifications<0){
            logger.trace("fetchNotifications","Bad args");
            throw new IllegalArgumentException("Bad args to fetch");
        }
        if(logger.debugOn()){
            logger.trace("fetchNotifications",
                    "filter="+filter+"; startSeq="+
                            startSequenceNumber+"; timeout="+timeout+
                            "; max="+maxNotifications);
        }
        if(startSequenceNumber>nextSequenceNumber()){
            final String msg="Start sequence number too big: "+
                    startSequenceNumber+" > "+nextSequenceNumber();
            logger.trace("fetchNotifications",msg);
            throw new IllegalArgumentException(msg);
        }
        /** Determine the end time corresponding to the timeout value.
         Caller may legitimately supply Long.MAX_VALUE to indicate no
         timeout.  In that case the addition will overflow and produce
         a negative end time.  Set end time to Long.MAX_VALUE in that
         case.  We assume System.currentTimeMillis() is positive.  */
        long endTime=System.currentTimeMillis()+timeout;
        if(endTime<0) // overflow
            endTime=Long.MAX_VALUE;
        if(logger.debugOn())
            logger.debug("fetchNotifications","endTime="+endTime);
        /** We set earliestSeq the first time through the loop.  If we
         set it here, notifications could be dropped before we
         started examining them, so earliestSeq might not correspond
         to the earliest notification we examined.  */
        long earliestSeq=-1;
        long nextSeq=startSequenceNumber;
        List<TargetedNotification> notifs=
                new ArrayList<TargetedNotification>();
        /** On exit from this loop, notifs, earliestSeq, and nextSeq must
         all be correct values for the returned NotificationResult.  */
        while(true){
            logger.debug("fetchNotifications","main loop starts");
            NamedNotification candidate;
            /** Get the next available notification regardless of filters,
             or wait for one to arrive if there is none.  */
            synchronized(this){
                /** First time through.  The current earliestSequenceNumber
                 is the first one we could have examined.  */
                if(earliestSeq<0){
                    earliestSeq=earliestSequenceNumber();
                    if(logger.debugOn()){
                        logger.debug("fetchNotifications",
                                "earliestSeq="+earliestSeq);
                    }
                    if(nextSeq<earliestSeq){
                        nextSeq=earliestSeq;
                        logger.debug("fetchNotifications",
                                "nextSeq=earliestSeq");
                    }
                }else
                    earliestSeq=earliestSequenceNumber();
                /** If many notifications have been dropped since the
                 last time through, nextSeq could now be earlier
                 than the current earliest.  If so, notifications
                 may have been lost and we return now so the caller
                 can see this next time it calls.  */
                if(nextSeq<earliestSeq){
                    logger.trace("fetchNotifications",
                            "nextSeq="+nextSeq+" < "+"earliestSeq="+
                                    earliestSeq+" so may have lost notifs");
                    break;
                }
                if(nextSeq<nextSequenceNumber()){
                    candidate=notificationAt(nextSeq);
                    // Skip security check if NotificationBufferFilter is not overloaded
                    if(!(filter instanceof ServerNotifForwarder.NotifForwarderBufferFilter)){
                        try{
                            ServerNotifForwarder.checkMBeanPermission(this.mBeanServer,
                                    candidate.getObjectName(),"addNotificationListener");
                        }catch(InstanceNotFoundException|SecurityException e){
                            if(logger.debugOn()){
                                logger.debug("fetchNotifications","candidate: "+candidate+" skipped. exception "+e);
                            }
                            ++nextSeq;
                            continue;
                        }
                    }
                    if(logger.debugOn()){
                        logger.debug("fetchNotifications","candidate: "+
                                candidate);
                        logger.debug("fetchNotifications","nextSeq now "+
                                nextSeq);
                    }
                }else{
                    /** nextSeq is the largest sequence number.  If we
                     already got notifications, return them now.
                     Otherwise wait for some to arrive, with
                     timeout.  */
                    if(notifs.size()>0){
                        logger.debug("fetchNotifications",
                                "no more notifs but have some so don't wait");
                        break;
                    }
                    long toWait=endTime-System.currentTimeMillis();
                    if(toWait<=0){
                        logger.debug("fetchNotifications","timeout");
                        break;
                    }
                    /** dispose called */
                    if(isDisposed()){
                        if(logger.debugOn())
                            logger.debug("fetchNotifications",
                                    "dispose callled, no wait");
                        return new NotificationResult(earliestSequenceNumber(),
                                nextSequenceNumber(),
                                new TargetedNotification[0]);
                    }
                    if(logger.debugOn())
                        logger.debug("fetchNotifications",
                                "wait("+toWait+")");
                    wait(toWait);
                    continue;
                }
            }
            /** We have a candidate notification.  See if it matches
             our filters.  We do this outside the synchronized block
             so we don't hold up everyone accessing the buffer
             (including notification senders) while we evaluate
             potentially slow filters.  */
            ObjectName name=candidate.getObjectName();
            Notification notif=candidate.getNotification();
            List<TargetedNotification> matchedNotifs=
                    new ArrayList<TargetedNotification>();
            logger.debug("fetchNotifications",
                    "applying filter to candidate");
            filter.apply(matchedNotifs,name,notif);
            if(matchedNotifs.size()>0){
                /** We only check the max size now, so that our
                 returned nextSeq is as large as possible.  This
                 prevents the caller from thinking it missed
                 interesting notifications when in fact we knew they
                 weren't.  */
                if(maxNotifications<=0){
                    logger.debug("fetchNotifications",
                            "reached maxNotifications");
                    break;
                }
                --maxNotifications;
                if(logger.debugOn())
                    logger.debug("fetchNotifications","add: "+
                            matchedNotifs);
                notifs.addAll(matchedNotifs);
            }
            ++nextSeq;
        } // end while
        /** Construct and return the result.  */
        int nnotifs=notifs.size();
        TargetedNotification[] resultNotifs=
                new TargetedNotification[nnotifs];
        notifs.toArray(resultNotifs);
        NotificationResult nr=
                new NotificationResult(earliestSeq,nextSeq,resultNotifs);
        if(logger.debugOn())
            logger.debug("fetchNotifications",nr.toString());
        logger.trace("fetchNotifications","ends");
        return nr;
    }
    private long nextSequenceNumber;    synchronized long earliestSequenceNumber(){
        return earliestSequenceNumber;
    }
    private Set<ObjectName> createdDuringQuery;    synchronized long nextSequenceNumber(){
        return nextSequenceNumber;
    }
    private final NotificationListener creationListener=
            new NotificationListener(){
                public void handleNotification(Notification notif,
                                               Object handback){
                    logger.debug("creationListener","handleNotification called");
                    createdNotification((MBeanServerNotification)notif);
                }
            };

    private ArrayNotificationBuffer(MBeanServer mbs,int queueSize){
        if(logger.traceOn())
            logger.trace("Constructor","queueSize="+queueSize);
        if(mbs==null||queueSize<1)
            throw new IllegalArgumentException("Bad args");
        this.mBeanServer=mbs;
        this.queueSize=queueSize;
        this.queue=new ArrayQueue<NamedNotification>(queueSize);
        this.earliestSequenceNumber=System.currentTimeMillis();
        this.nextSequenceNumber=this.earliestSequenceNumber;
        logger.trace("Constructor","ends");
    }

    public static NotificationBuffer getNotificationBuffer(
            MBeanServer mbs,Map<String,?> env){
        if(env==null)
            env=Collections.emptyMap();
        //Find out queue size
        int queueSize=EnvHelp.getNotifBufferSize(env);
        ArrayNotificationBuffer buf;
        boolean create;
        NotificationBuffer sharer;
        synchronized(globalLock){
            buf=mbsToBuffer.get(mbs);
            create=(buf==null);
            if(create){
                buf=new ArrayNotificationBuffer(mbs,queueSize);
                mbsToBuffer.put(mbs,buf);
            }
            sharer=buf.new ShareBuffer(queueSize);
        }
        /** We avoid holding any locks while calling createListeners.
         * This prevents possible deadlocks involving user code, but
         * does mean that a second ConnectorServer created and started
         * in this window will return before all the listeners are ready,
         * which could lead to surprising behaviour.  The alternative
         * would be to block the second ConnectorServer until the first
         * one has finished adding all the listeners, but that would then
         * be subject to deadlock.
         */
        if(create)
            buf.createListeners();
        return sharer;
    }    synchronized NamedNotification notificationAt(long seqNo){
        long index=seqNo-earliestSequenceNumber;
        if(index<0||index>Integer.MAX_VALUE){
            final String msg="Bad sequence number: "+seqNo+" (earliest "
                    +earliestSequenceNumber+")";
            logger.trace("notificationAt",msg);
            throw new IllegalArgumentException(msg);
        }
        return queue.get((int)index);
    }

    void addSharer(ShareBuffer sharer){
        synchronized(globalLock){
            synchronized(this){
                if(sharer.getSize()>queueSize)
                    resize(sharer.getSize());
            }
            sharers.add(sharer);
        }
    }

    private synchronized void resize(int newSize){
        if(newSize==queueSize)
            return;
        while(queue.size()>newSize)
            dropNotification();
        queue.resize(newSize);
        queueSize=newSize;
    }

    private void dropNotification(){
        queue.remove(0);
        earliestSequenceNumber++;
    }

    private void removeSharer(ShareBuffer sharer){
        boolean empty;
        synchronized(globalLock){
            sharers.remove(sharer);
            empty=sharers.isEmpty();
            if(empty)
                removeNotificationBuffer(mBeanServer);
            else{
                int max=0;
                for(ShareBuffer buf : sharers){
                    int bufsize=buf.getSize();
                    if(bufsize>max)
                        max=bufsize;
                }
                if(max<queueSize)
                    resize(max);
            }
        }
        if(empty){
            synchronized(this){
                disposed=true;
                // Notify potential waiting fetchNotification call
                notifyAll();
            }
            destroyListeners();
        }
    }

    static void removeNotificationBuffer(MBeanServer mbs){
        synchronized(globalLock){
            mbsToBuffer.remove(mbs);
        }
    }

    private void destroyListeners(){
        checkNoLocks();
        logger.debug("destroyListeners","starts");
        try{
            removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME,
                    creationListener);
        }catch(Exception e){
            logger.warning("remove listener from MBeanServer delegate",e);
        }
        Set<ObjectName> names=queryNames(null,broadcasterQuery);
        for(final ObjectName name : names){
            if(logger.debugOn())
                logger.debug("destroyListeners",
                        "remove listener from "+name);
            removeBufferListener(name);
        }
        logger.debug("destroyListeners","ends");
    }

    private void removeBufferListener(ObjectName name){
        checkNoLocks();
        if(logger.debugOn())
            logger.debug("removeBufferListener",name.toString());
        try{
            removeNotificationListener(name,bufferListener);
        }catch(Exception e){
            logger.trace("removeBufferListener",e);
        }
    }

    private void removeNotificationListener(final ObjectName name,
                                            final NotificationListener listener)
            throws Exception{
        try{
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(){
                public Void run() throws Exception{
                    mBeanServer.removeNotificationListener(name,listener);
                    return null;
                }
            });
        }catch(Exception e){
            throw extractException(e);
        }
    }

    private static Exception extractException(Exception e){
        while(e instanceof PrivilegedActionException){
            e=((PrivilegedActionException)e).getException();
        }
        return e;
    }

    private Set<ObjectName> queryNames(final ObjectName name,
                                       final QueryExp query){
        PrivilegedAction<Set<ObjectName>> act=
                new PrivilegedAction<Set<ObjectName>>(){
                    public Set<ObjectName> run(){
                        return mBeanServer.queryNames(name,query);
                    }
                };
        try{
            return AccessController.doPrivileged(act);
        }catch(RuntimeException e){
            logger.fine("queryNames","Failed to query names: "+e);
            logger.debug("queryNames",e);
            throw e;
        }
    }

    private void checkNoLocks(){
        if(Thread.holdsLock(this)||Thread.holdsLock(globalLock))
            logger.warning("checkNoLocks","lock protocol violation");
    }

    synchronized void addNotification(NamedNotification notif){
        if(logger.traceOn())
            logger.trace("addNotification",notif.toString());
        while(queue.size()>=queueSize){
            dropNotification();
            if(logger.debugOn()){
                logger.debug("addNotification",
                        "dropped oldest notif, earliestSeq="+
                                earliestSequenceNumber);
            }
        }
        queue.add(notif);
        nextSequenceNumber++;
        if(logger.debugOn())
            logger.debug("addNotification","nextSeq="+nextSequenceNumber);
        notifyAll();
    }

    private void createListeners(){
        logger.debug("createListeners","starts");
        synchronized(this){
            createdDuringQuery=new HashSet<ObjectName>();
        }
        try{
            addNotificationListener(MBeanServerDelegate.DELEGATE_NAME,
                    creationListener,creationFilter,null);
            logger.debug("createListeners","added creationListener");
        }catch(Exception e){
            final String msg="Can't add listener to MBean server delegate: ";
            RuntimeException re=new IllegalArgumentException(msg+e);
            EnvHelp.initCause(re,e);
            logger.fine("createListeners",msg+e);
            logger.debug("createListeners",e);
            throw re;
        }
        /** Spec doesn't say whether Set returned by QueryNames can be modified
         so we clone it. */
        Set<ObjectName> names=queryNames(null,broadcasterQuery);
        names=new HashSet<ObjectName>(names);
        synchronized(this){
            names.addAll(createdDuringQuery);
            createdDuringQuery=null;
        }
        for(ObjectName name : names)
            addBufferListener(name);
        logger.debug("createListeners","ends");
    }

    private void addBufferListener(ObjectName name){
        checkNoLocks();
        if(logger.debugOn())
            logger.debug("addBufferListener",name.toString());
        try{
            addNotificationListener(name,bufferListener,null,name);
        }catch(Exception e){
            logger.trace("addBufferListener",e);
            /** This can happen if the MBean was unregistered just
             after the query.  Or user NotificationBroadcaster might
             throw unexpected exception.  */
        }
    }

    private void addNotificationListener(final ObjectName name,
                                         final NotificationListener listener,
                                         final NotificationFilter filter,
                                         final Object handback)
            throws Exception{
        try{
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(){
                public Void run() throws InstanceNotFoundException{
                    mBeanServer.addNotificationListener(name,
                            listener,
                            filter,
                            handback);
                    return null;
                }
            });
        }catch(Exception e){
            throw extractException(e);
        }
    }

    private void createdNotification(MBeanServerNotification n){
        final String shouldEqual=
                MBeanServerNotification.REGISTRATION_NOTIFICATION;
        if(!n.getType().equals(shouldEqual)){
            logger.warning("createNotification","bad type: "+n.getType());
            return;
        }
        ObjectName name=n.getMBeanName();
        if(logger.debugOn())
            logger.debug("createdNotification","for: "+name);
        synchronized(this){
            if(createdDuringQuery!=null){
                createdDuringQuery.add(name);
                return;
            }
        }
        if(isInstanceOf(mBeanServer,name,broadcasterClass)){
            addBufferListener(name);
            if(isDisposed())
                removeBufferListener(name);
        }
    }

    private static boolean isInstanceOf(final MBeanServer mbs,
                                        final ObjectName name,
                                        final String className){
        PrivilegedExceptionAction<Boolean> act=
                new PrivilegedExceptionAction<Boolean>(){
                    public Boolean run() throws InstanceNotFoundException{
                        return mbs.isInstanceOf(name,className);
                    }
                };
        try{
            return AccessController.doPrivileged(act);
        }catch(Exception e){
            logger.fine("isInstanceOf","failed: "+e);
            logger.debug("isInstanceOf",e);
            return false;
        }
    }

    private static class NamedNotification{
        private final ObjectName sender;
        private final Notification notification;

        NamedNotification(ObjectName sender,Notification notif){
            this.sender=sender;
            this.notification=notif;
        }

        ObjectName getObjectName(){
            return sender;
        }

        Notification getNotification(){
            return notification;
        }

        public String toString(){
            return "NamedNotification("+sender+", "+notification+")";
        }
    }

    private static class BroadcasterQuery
            extends QueryEval implements QueryExp{
        private static final long serialVersionUID=7378487660587592048L;

        public boolean apply(final ObjectName name){
            final MBeanServer mbs=QueryEval.getMBeanServer();
            return isInstanceOf(mbs,name,broadcasterClass);
        }
    }

    private class ShareBuffer implements NotificationBuffer{
        private final int size;

        ShareBuffer(int size){
            this.size=size;
            addSharer(this);
        }

        public NotificationResult
        fetchNotifications(NotificationBufferFilter filter,
                           long startSequenceNumber,
                           long timeout,
                           int maxNotifications)
                throws InterruptedException{
            NotificationBuffer buf=ArrayNotificationBuffer.this;
            return buf.fetchNotifications(filter,startSequenceNumber,
                    timeout,maxNotifications);
        }        public void dispose(){
            ArrayNotificationBuffer.this.removeSharer(this);
        }

        int getSize(){
            return size;
        }


    }

    private class BufferListener implements NotificationListener{
        public void handleNotification(Notification notif,Object handback){
            if(logger.debugOn()){
                logger.debug("BufferListener.handleNotification",
                        "notif="+notif+"; handback="+handback);
            }
            ObjectName name=(ObjectName)handback;
            addNotification(new NamedNotification(name,notif));
        }
    }






}
