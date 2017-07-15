/**
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AppContext;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TimerQueue implements Runnable{
    private static final Object sharedInstanceKey=
            new StringBuffer("TimerQueue.sharedInstanceKey");
    private static final Object expiredTimersKey=
            new StringBuffer("TimerQueue.expiredTimersKey");
    private static final Object classLock=new Object();
    private static final long NANO_ORIGIN=System.nanoTime();
    private final DelayQueue<DelayedTimer> queue;
    private final Lock runningLock;
    private volatile boolean running;

    public TimerQueue(){
        super();
        queue=new DelayQueue<DelayedTimer>();
        // Now start the TimerQueue thread.
        runningLock=new ReentrantLock();
        startIfNeeded();
    }

    void startIfNeeded(){
        if(!running){
            runningLock.lock();
            if(running){
                return;
            }
            try{
                final ThreadGroup threadGroup=
                        AppContext.getAppContext().getThreadGroup();
                java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction<Object>(){
                            public Object run(){
                                Thread timerThread=new Thread(threadGroup,TimerQueue.this,
                                        "TimerQueue");
                                timerThread.setDaemon(true);
                                timerThread.setPriority(Thread.NORM_PRIORITY);
                                timerThread.start();
                                return null;
                            }
                        });
                running=true;
            }finally{
                runningLock.unlock();
            }
        }
    }

    public static TimerQueue sharedInstance(){
        synchronized(classLock){
            TimerQueue sharedInst=(TimerQueue)
                    SwingUtilities.appContextGet(
                            sharedInstanceKey);
            if(sharedInst==null){
                sharedInst=new TimerQueue();
                SwingUtilities.appContextPut(sharedInstanceKey,sharedInst);
            }
            return sharedInst;
        }
    }

    void addTimer(Timer timer,long delayMillis){
        timer.getLock().lock();
        try{
            // If the Timer is already in the queue, then ignore the add.
            if(!containsTimer(timer)){
                addTimer(new DelayedTimer(timer,
                        TimeUnit.MILLISECONDS.toNanos(delayMillis)
                                +now()));
            }
        }finally{
            timer.getLock().unlock();
        }
    }

    private void addTimer(DelayedTimer delayedTimer){
        assert delayedTimer!=null&&!containsTimer(delayedTimer.getTimer());
        Timer timer=delayedTimer.getTimer();
        timer.getLock().lock();
        try{
            timer.delayedTimer=delayedTimer;
            queue.add(delayedTimer);
        }finally{
            timer.getLock().unlock();
        }
    }

    boolean containsTimer(Timer timer){
        timer.getLock().lock();
        try{
            return timer.delayedTimer!=null;
        }finally{
            timer.getLock().unlock();
        }
    }

    private static long now(){
        return System.nanoTime()-NANO_ORIGIN;
    }

    void removeTimer(Timer timer){
        timer.getLock().lock();
        try{
            if(timer.delayedTimer!=null){
                queue.remove(timer.delayedTimer);
                timer.delayedTimer=null;
            }
        }finally{
            timer.getLock().unlock();
        }
    }

    public void run(){
        runningLock.lock();
        try{
            while(running){
                try{
                    DelayedTimer runningTimer=queue.take();
                    Timer timer=runningTimer.getTimer();
                    timer.getLock().lock();
                    try{
                        DelayedTimer delayedTimer=timer.delayedTimer;
                        if(delayedTimer==runningTimer){
                            /**
                             * Timer is not removed (delayedTimer != null)
                             * or not removed and added (runningTimer == delayedTimer)
                             * after we get it from the queue and before the
                             * lock on the timer is acquired
                             */
                            timer.post(); // have timer post an event
                            timer.delayedTimer=null;
                            if(timer.isRepeats()){
                                delayedTimer.setTime(now()
                                        +TimeUnit.MILLISECONDS.toNanos(
                                        timer.getDelay()));
                                addTimer(delayedTimer);
                            }
                        }
                        // Allow run other threads on systems without kernel threads
                        timer.getLock().newCondition().awaitNanos(1);
                    }catch(SecurityException ignore){
                    }finally{
                        timer.getLock().unlock();
                    }
                }catch(InterruptedException ie){
                    // Shouldn't ignore InterruptedExceptions here, so AppContext
                    // is disposed gracefully, see 6799345 for details
                    if(AppContext.getAppContext().isDisposed()){
                        break;
                    }
                }
            }
        }catch(ThreadDeath td){
            // Mark all the timers we contain as not being queued.
            for(DelayedTimer delayedTimer : queue){
                delayedTimer.getTimer().cancelEvent();
            }
            throw td;
        }finally{
            running=false;
            runningLock.unlock();
        }
    }

    public String toString(){
        StringBuilder buf=new StringBuilder();
        buf.append("TimerQueue (");
        boolean isFirst=true;
        for(DelayedTimer delayedTimer : queue){
            if(!isFirst){
                buf.append(", ");
            }
            buf.append(delayedTimer.getTimer().toString());
            isFirst=false;
        }
        buf.append(")");
        return buf.toString();
    }

    static class DelayedTimer implements Delayed{
        // most of it copied from
        // java.util.concurrent.ScheduledThreadPoolExecutor
        private static final AtomicLong sequencer=new AtomicLong(0);
        private final long sequenceNumber;
        private final Timer timer;
        private volatile long time;

        DelayedTimer(Timer timer,long nanos){
            this.timer=timer;
            time=nanos;
            sequenceNumber=sequencer.getAndIncrement();
        }

        final void setTime(long nanos){
            time=nanos;
        }

        final Timer getTimer(){
            return timer;
        }

        public int compareTo(Delayed other){
            if(other==this){ // compare zero ONLY if same object
                return 0;
            }
            if(other instanceof DelayedTimer){
                DelayedTimer x=(DelayedTimer)other;
                long diff=time-x.time;
                if(diff<0){
                    return -1;
                }else if(diff>0){
                    return 1;
                }else if(sequenceNumber<x.sequenceNumber){
                    return -1;
                }else{
                    return 1;
                }
            }
            long d=(getDelay(TimeUnit.NANOSECONDS)-
                    other.getDelay(TimeUnit.NANOSECONDS));
            return (d==0)?0:((d<0)?-1:1);
        }

        final public long getDelay(TimeUnit unit){
            return unit.convert(time-now(),TimeUnit.NANOSECONDS);
        }
    }
}
