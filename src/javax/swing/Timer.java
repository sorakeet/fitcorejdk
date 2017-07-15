/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EventListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class Timer implements Serializable{
    private static volatile boolean logTimers;
    // The following field strives to maintain the following:
    //    If coalesce is true, only allow one Runnable to be queued on the
    //    EventQueue and be pending (ie in the process of notifying the
    //    ActionListener). If we didn't do this it would allow for a
    //    situation where the app is taking too long to process the
    //    actionPerformed, and thus we'ld end up queing a bunch of Runnables
    //    and the app would never return: not good. This of course implies
    //    you can get dropped events, but such is life.
    // notify is used to indicate if the ActionListener can be notified, when
    // the Runnable is processed if this is true it will notify the listeners.
    // notify is set to true when the Timer fires and the Runnable is queued.
    // It will be set to false after notifying the listeners (if coalesce is
    // true) or if the developer invokes stop.
    private transient final AtomicBoolean notify=new AtomicBoolean(false);
    private transient final Runnable doPostEvent;
    private transient final Lock lock=new ReentrantLock();
    protected EventListenerList listenerList=new EventListenerList();
    // This field is maintained by TimerQueue.
    // eventQueued can also be reset by the TimerQueue, but will only ever
    // happen in applet case when TimerQueues thread is destroyed.
    // access to this field is synchronized on getLock() lock.
    transient TimerQueue.DelayedTimer delayedTimer=null;
    private volatile int initialDelay, delay;
    private volatile boolean repeats=true, coalesce=true;
    private volatile String actionCommand;
    private transient volatile AccessControlContext acc=
            AccessController.getContext();

    public Timer(int delay,ActionListener listener){
        super();
        this.delay=delay;
        this.initialDelay=delay;
        doPostEvent=new DoPostEvent();
        if(listener!=null){
            addActionListener(listener);
        }
    }

    public void addActionListener(ActionListener listener){
        listenerList.add(ActionListener.class,listener);
    }

    public static boolean getLogTimers(){
        return logTimers;
    }

    public static void setLogTimers(boolean flag){
        logTimers=flag;
    }

    public void removeActionListener(ActionListener listener){
        listenerList.remove(ActionListener.class,listener);
    }

    public ActionListener[] getActionListeners(){
        return listenerList.getListeners(ActionListener.class);
    }

    protected void fireActionPerformed(ActionEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ActionListener.class){
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }

    public boolean isRepeats(){
        return repeats;
    }

    public void setRepeats(boolean flag){
        repeats=flag;
    }

    public boolean isCoalesce(){
        return coalesce;
    }

    public void setCoalesce(boolean flag){
        boolean old=coalesce;
        coalesce=flag;
        if(!old&&coalesce){
            // We must do this as otherwise if the Timer once notified
            // in !coalese mode notify will be stuck to true and never
            // become false.
            cancelEvent();
        }
    }

    void cancelEvent(){
        notify.set(false);
    }

    public String getActionCommand(){
        return actionCommand;
    }

    public void setActionCommand(String command){
        this.actionCommand=command;
    }

    public boolean isRunning(){
        return timerQueue().containsTimer(this);
    }

    private TimerQueue timerQueue(){
        return TimerQueue.sharedInstance();
    }

    public void restart(){
        getLock().lock();
        try{
            stop();
            start();
        }finally{
            getLock().unlock();
        }
    }

    public void start(){
        timerQueue().addTimer(this,getInitialDelay());
    }

    public int getInitialDelay(){
        return initialDelay;
    }

    public void setInitialDelay(int initialDelay){
        if(initialDelay<0){
            throw new IllegalArgumentException("Invalid initial delay: "+
                    initialDelay);
        }else{
            this.initialDelay=initialDelay;
        }
    }

    public void stop(){
        getLock().lock();
        try{
            cancelEvent();
            timerQueue().removeTimer(this);
        }finally{
            getLock().unlock();
        }
    }

    Lock getLock(){
        return lock;
    }

    void post(){
        if(notify.compareAndSet(false,true)||!coalesce){
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run(){
                    SwingUtilities.invokeLater(doPostEvent);
                    return null;
                }
            },getAccessControlContext());
        }
    }

    final AccessControlContext getAccessControlContext(){
        if(acc==null){
            throw new SecurityException(
                    "Timer is missing AccessControlContext");
        }
        return acc;
    }

    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException{
        this.acc=AccessController.getContext();
        in.defaultReadObject();
    }

    private Object readResolve(){
        Timer timer=new Timer(getDelay(),null);
        timer.listenerList=listenerList;
        timer.initialDelay=initialDelay;
        timer.delay=delay;
        timer.repeats=repeats;
        timer.coalesce=coalesce;
        timer.actionCommand=actionCommand;
        return timer;
    }

    public int getDelay(){
        return delay;
    }

    public void setDelay(int delay){
        if(delay<0){
            throw new IllegalArgumentException("Invalid delay: "+delay);
        }else{
            this.delay=delay;
        }
    }

    class DoPostEvent implements Runnable{
        public void run(){
            if(logTimers){
                System.out.println("Timer ringing: "+Timer.this);
            }
            if(notify.get()){
                fireActionPerformed(new ActionEvent(Timer.this,0,getActionCommand(),
                        System.currentTimeMillis(),
                        0));
                if(coalesce){
                    cancelEvent();
                }
            }
        }

        Timer getTimer(){
            return Timer.this;
        }
    }
}
