/**
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.*;
import sun.awt.dnd.SunDropTargetEvent;
import sun.misc.JavaSecurityAccess;
import sun.misc.SharedSecrets;
import sun.util.logging.PlatformLogger;

import java.awt.event.*;
import java.awt.peer.ComponentPeer;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class EventQueue{
    private static final AtomicInteger threadInitNumber=new AtomicInteger(0);
    private static final int LOW_PRIORITY=0;
    private static final int NORM_PRIORITY=1;
    private static final int HIGH_PRIORITY=2;
    private static final int ULTIMATE_PRIORITY=3;
    private static final int NUM_PRIORITIES=ULTIMATE_PRIORITY+1;
    private final static Runnable dummyRunnable=new Runnable(){
        public void run(){
        }
    };
    private static final int PAINT=0;
    private static final int UPDATE=1;
    private static final int MOVE=2;
    private static final int DRAG=3;
    private static final int PEER=4;
    private static final int CACHE_LENGTH=5;
    private static final JavaSecurityAccess javaSecurityAccess=
            SharedSecrets.getJavaSecurityAccess();
    private static volatile PlatformLogger eventLog;

    static{
        AWTAccessor.setEventQueueAccessor(
                new AWTAccessor.EventQueueAccessor(){
                    public Thread getDispatchThread(EventQueue eventQueue){
                        return eventQueue.getDispatchThread();
                    }

                    public boolean isDispatchThreadImpl(EventQueue eventQueue){
                        return eventQueue.isDispatchThreadImpl();
                    }

                    public void removeSourceEvents(EventQueue eventQueue,
                                                   Object source,
                                                   boolean removeAllEvents){
                        eventQueue.removeSourceEvents(source,removeAllEvents);
                    }

                    public boolean noEvents(EventQueue eventQueue){
                        return eventQueue.noEvents();
                    }

                    public void wakeup(EventQueue eventQueue,boolean isShutdown){
                        eventQueue.wakeup(isShutdown);
                    }

                    public void invokeAndWait(Object source,Runnable r)
                            throws InterruptedException, InvocationTargetException{
                        EventQueue.invokeAndWait(source,r);
                    }

                    public void setFwDispatcher(EventQueue eventQueue,
                                                FwDispatcher dispatcher){
                        eventQueue.setFwDispatcher(dispatcher);
                    }

                    @Override
                    public long getMostRecentEventTime(EventQueue eventQueue){
                        return eventQueue.getMostRecentEventTimeImpl();
                    }
                });
    }

    private final Lock pushPopLock;
    private final Condition pushPopCond;
    private final ThreadGroup threadGroup=
            Thread.currentThread().getThreadGroup();
    private final ClassLoader classLoader=
            Thread.currentThread().getContextClassLoader();
    private final AppContext appContext;
    private final String name="AWT-EventQueue-"+threadInitNumber.getAndIncrement();
    private Queue[] queues=new Queue[NUM_PRIORITIES];
    private EventQueue nextQueue;
    private EventQueue previousQueue;
    private EventDispatchThread dispatchThread;
    private long mostRecentEventTime=System.currentTimeMillis();
    private long mostRecentKeyEventTime=System.currentTimeMillis();
    private WeakReference<AWTEvent> currentEvent;
    private volatile int waitForID;
    private FwDispatcher fwDispatcher;

    public EventQueue(){
        for(int i=0;i<NUM_PRIORITIES;i++){
            queues[i]=new Queue();
        }
        /**
         * NOTE: if you ever have to start the associated event dispatch
         * thread at this point, be aware of the following problem:
         * If this EventQueue instance is created in
         * SunToolkit.createNewAppContext() the started dispatch thread
         * may call AppContext.getAppContext() before createNewAppContext()
         * completes thus causing mess in thread group to appcontext mapping.
         */
        appContext=AppContext.getAppContext();
        pushPopLock=(Lock)appContext.get(AppContext.EVENT_QUEUE_LOCK_KEY);
        pushPopCond=(Condition)appContext.get(AppContext.EVENT_QUEUE_COND_KEY);
    }

    public static long getMostRecentEventTime(){
        return Toolkit.getEventQueue().getMostRecentEventTimeImpl();
    }

    public static AWTEvent getCurrentEvent(){
        return Toolkit.getEventQueue().getCurrentEventImpl();
    }

    static void setCurrentEventAndMostRecentTime(AWTEvent e){
        Toolkit.getEventQueue().setCurrentEventAndMostRecentTimeImpl(e);
    }

    public static void invokeLater(Runnable runnable){
        Toolkit.getEventQueue().postEvent(
                new InvocationEvent(Toolkit.getDefaultToolkit(),runnable));
    }

    public static void invokeAndWait(Runnable runnable)
            throws InterruptedException, InvocationTargetException{
        invokeAndWait(Toolkit.getDefaultToolkit(),runnable);
    }

    static void invokeAndWait(Object source,Runnable runnable)
            throws InterruptedException, InvocationTargetException{
        if(EventQueue.isDispatchThread()){
            throw new Error("Cannot call invokeAndWait from the event dispatcher thread");
        }
        class AWTInvocationLock{
        }
        Object lock=new AWTInvocationLock();
        InvocationEvent event=
                new InvocationEvent(source,runnable,lock,true);
        synchronized(lock){
            Toolkit.getEventQueue().postEvent(event);
            while(!event.isDispatched()){
                lock.wait();
            }
        }
        Throwable eventThrowable=event.getThrowable();
        if(eventThrowable!=null){
            throw new InvocationTargetException(eventThrowable);
        }
    }

    public static boolean isDispatchThread(){
        EventQueue eq=Toolkit.getEventQueue();
        return eq.isDispatchThreadImpl();
    }

    public void postEvent(AWTEvent theEvent){
        SunToolkit.flushPendingEvents(appContext);
        postEventPrivate(theEvent);
    }

    private final void postEventPrivate(AWTEvent theEvent){
        theEvent.isPosted=true;
        pushPopLock.lock();
        try{
            if(nextQueue!=null){
                // Forward the event to the top of EventQueue stack
                nextQueue.postEventPrivate(theEvent);
                return;
            }
            if(dispatchThread==null){
                if(theEvent.getSource()==AWTAutoShutdown.getInstance()){
                    return;
                }else{
                    initDispatchThread();
                }
            }
            postEvent(theEvent,getPriority(theEvent));
        }finally{
            pushPopLock.unlock();
        }
    }

    private static int getPriority(AWTEvent theEvent){
        if(theEvent instanceof PeerEvent){
            PeerEvent peerEvent=(PeerEvent)theEvent;
            if((peerEvent.getFlags()&PeerEvent.ULTIMATE_PRIORITY_EVENT)!=0){
                return ULTIMATE_PRIORITY;
            }
            if((peerEvent.getFlags()&PeerEvent.PRIORITY_EVENT)!=0){
                return HIGH_PRIORITY;
            }
            if((peerEvent.getFlags()&PeerEvent.LOW_PRIORITY_EVENT)!=0){
                return LOW_PRIORITY;
            }
        }
        int id=theEvent.getID();
        if((id>=PaintEvent.PAINT_FIRST)&&(id<=PaintEvent.PAINT_LAST)){
            return LOW_PRIORITY;
        }
        return NORM_PRIORITY;
    }

    private void postEvent(AWTEvent theEvent,int priority){
        if(coalesceEvent(theEvent,priority)){
            return;
        }
        EventQueueItem newItem=new EventQueueItem(theEvent);
        cacheEQItem(newItem);
        boolean notifyID=(theEvent.getID()==this.waitForID);
        if(queues[priority].head==null){
            boolean shouldNotify=noEvents();
            queues[priority].head=queues[priority].tail=newItem;
            if(shouldNotify){
                if(theEvent.getSource()!=AWTAutoShutdown.getInstance()){
                    AWTAutoShutdown.getInstance().notifyThreadBusy(dispatchThread);
                }
                pushPopCond.signalAll();
            }else if(notifyID){
                pushPopCond.signalAll();
            }
        }else{
            // The event was not coalesced or has non-Component source.
            // Insert it at the end of the appropriate Queue.
            queues[priority].tail.next=newItem;
            queues[priority].tail=newItem;
            if(notifyID){
                pushPopCond.signalAll();
            }
        }
    }

    private boolean coalesceEvent(AWTEvent e,int priority){
        if(!(e.getSource() instanceof Component)){
            return false;
        }
        if(e instanceof PeerEvent){
            return coalescePeerEvent((PeerEvent)e);
        }
        // The worst case
        if(((Component)e.getSource()).isCoalescingEnabled()
                &&coalesceOtherEvent(e,priority)){
            return true;
        }
        if(e instanceof PaintEvent){
            return coalescePaintEvent((PaintEvent)e);
        }
        if(e instanceof MouseEvent){
            return coalesceMouseEvent((MouseEvent)e);
        }
        return false;
    }

    private boolean coalescePaintEvent(PaintEvent e){
        ComponentPeer sourcePeer=((Component)e.getSource()).peer;
        if(sourcePeer!=null){
            sourcePeer.coalescePaintEvent(e);
        }
        EventQueueItem[] cache=((Component)e.getSource()).eventCache;
        if(cache==null){
            return false;
        }
        int index=eventToCacheIndex(e);
        if(index!=-1&&cache[index]!=null){
            PaintEvent merged=mergePaintEvents(e,(PaintEvent)cache[index].event);
            if(merged!=null){
                cache[index].event=merged;
                return true;
            }
        }
        return false;
    }

    private PaintEvent mergePaintEvents(PaintEvent a,PaintEvent b){
        Rectangle aRect=a.getUpdateRect();
        Rectangle bRect=b.getUpdateRect();
        if(bRect.contains(aRect)){
            return b;
        }
        if(aRect.contains(bRect)){
            return a;
        }
        return null;
    }

    private boolean coalesceMouseEvent(MouseEvent e){
        EventQueueItem[] cache=((Component)e.getSource()).eventCache;
        if(cache==null){
            return false;
        }
        int index=eventToCacheIndex(e);
        if(index!=-1&&cache[index]!=null){
            cache[index].event=e;
            return true;
        }
        return false;
    }

    private boolean coalescePeerEvent(PeerEvent e){
        EventQueueItem[] cache=((Component)e.getSource()).eventCache;
        if(cache==null){
            return false;
        }
        int index=eventToCacheIndex(e);
        if(index!=-1&&cache[index]!=null){
            e=e.coalesceEvents((PeerEvent)cache[index].event);
            if(e!=null){
                cache[index].event=e;
                return true;
            }else{
                cache[index]=null;
            }
        }
        return false;
    }

    private static int eventToCacheIndex(AWTEvent e){
        switch(e.getID()){
            case PaintEvent.PAINT:
                return PAINT;
            case PaintEvent.UPDATE:
                return UPDATE;
            case MouseEvent.MOUSE_MOVED:
                return MOVE;
            case MouseEvent.MOUSE_DRAGGED:
                // Return -1 for SunDropTargetEvent since they are usually synchronous
                // and we don't want to skip them by coalescing with MouseEvent or other drag events
                return e instanceof SunDropTargetEvent?-1:DRAG;
            default:
                return e instanceof PeerEvent?PEER:-1;
        }
    }

    private boolean coalesceOtherEvent(AWTEvent e,int priority){
        int id=e.getID();
        Component source=(Component)e.getSource();
        for(EventQueueItem entry=queues[priority].head;
            entry!=null;entry=entry.next){
            // Give Component.coalesceEvents a chance
            if(entry.event.getSource()==source&&entry.event.getID()==id){
                AWTEvent coalescedEvent=source.coalesceEvents(
                        entry.event,e);
                if(coalescedEvent!=null){
                    entry.event=coalescedEvent;
                    return true;
                }
            }
        }
        return false;
    }

    private void cacheEQItem(EventQueueItem entry){
        int index=eventToCacheIndex(entry.event);
        if(index!=-1&&entry.event.getSource() instanceof Component){
            Component source=(Component)entry.event.getSource();
            if(source.eventCache==null){
                source.eventCache=new EventQueueItem[CACHE_LENGTH];
            }
            source.eventCache[index]=entry;
        }
    }

    private boolean noEvents(){
        for(int i=0;i<NUM_PRIORITIES;i++){
            if(queues[i].head!=null){
                return false;
            }
        }
        return true;
    }

    final void initDispatchThread(){
        pushPopLock.lock();
        try{
            if(dispatchThread==null&&!threadGroup.isDestroyed()&&!appContext.isDisposed()){
                dispatchThread=AccessController.doPrivileged(
                        new PrivilegedAction<EventDispatchThread>(){
                            public EventDispatchThread run(){
                                EventDispatchThread t=
                                        new EventDispatchThread(threadGroup,
                                                name,
                                                EventQueue.this);
                                t.setContextClassLoader(classLoader);
                                t.setPriority(Thread.NORM_PRIORITY+1);
                                t.setDaemon(false);
                                AWTAutoShutdown.getInstance().notifyThreadBusy(t);
                                return t;
                            }
                        }
                );
                dispatchThread.start();
            }
        }finally{
            pushPopLock.unlock();
        }
    }

    public AWTEvent getNextEvent() throws InterruptedException{
        do{
            /**
             * SunToolkit.flushPendingEvents must be called outside
             * of the synchronized block to avoid deadlock when
             * event queues are nested with push()/pop().
             */
            SunToolkit.flushPendingEvents(appContext);
            pushPopLock.lock();
            try{
                AWTEvent event=getNextEventPrivate();
                if(event!=null){
                    return event;
                }
                AWTAutoShutdown.getInstance().notifyThreadFree(dispatchThread);
                pushPopCond.await();
            }finally{
                pushPopLock.unlock();
            }
        }while(true);
    }

    AWTEvent getNextEventPrivate() throws InterruptedException{
        for(int i=NUM_PRIORITIES-1;i>=0;i--){
            if(queues[i].head!=null){
                EventQueueItem entry=queues[i].head;
                queues[i].head=entry.next;
                if(entry.next==null){
                    queues[i].tail=null;
                }
                uncacheEQItem(entry);
                return entry.event;
            }
        }
        return null;
    }

    private void uncacheEQItem(EventQueueItem entry){
        int index=eventToCacheIndex(entry.event);
        if(index!=-1&&entry.event.getSource() instanceof Component){
            Component source=(Component)entry.event.getSource();
            if(source.eventCache==null){
                return;
            }
            source.eventCache[index]=null;
        }
    }

    AWTEvent getNextEvent(int id) throws InterruptedException{
        do{
            /**
             * SunToolkit.flushPendingEvents must be called outside
             * of the synchronized block to avoid deadlock when
             * event queues are nested with push()/pop().
             */
            SunToolkit.flushPendingEvents(appContext);
            pushPopLock.lock();
            try{
                for(int i=0;i<NUM_PRIORITIES;i++){
                    for(EventQueueItem entry=queues[i].head, prev=null;
                        entry!=null;prev=entry,entry=entry.next){
                        if(entry.event.getID()==id){
                            if(prev==null){
                                queues[i].head=entry.next;
                            }else{
                                prev.next=entry.next;
                            }
                            if(queues[i].tail==entry){
                                queues[i].tail=prev;
                            }
                            uncacheEQItem(entry);
                            return entry.event;
                        }
                    }
                }
                waitForID=id;
                pushPopCond.await();
                waitForID=0;
            }finally{
                pushPopLock.unlock();
            }
        }while(true);
    }

    public AWTEvent peekEvent(int id){
        pushPopLock.lock();
        try{
            for(int i=NUM_PRIORITIES-1;i>=0;i--){
                EventQueueItem q=queues[i].head;
                for(;q!=null;q=q.next){
                    if(q.event.getID()==id){
                        return q.event;
                    }
                }
            }
        }finally{
            pushPopLock.unlock();
        }
        return null;
    }

    protected void dispatchEvent(final AWTEvent event){
        final Object src=event.getSource();
        final PrivilegedAction<Void> action=new PrivilegedAction<Void>(){
            public Void run(){
                // In case fwDispatcher is installed and we're already on the
                // dispatch thread (e.g. performing DefaultKeyboardFocusManager.sendMessage),
                // dispatch the event straight away.
                if(fwDispatcher==null||isDispatchThreadImpl()){
                    dispatchEventImpl(event,src);
                }else{
                    fwDispatcher.scheduleDispatch(new Runnable(){
                        @Override
                        public void run(){
                            dispatchEventImpl(event,src);
                        }
                    });
                }
                return null;
            }
        };
        final AccessControlContext stack=AccessController.getContext();
        final AccessControlContext srcAcc=getAccessControlContextFrom(src);
        final AccessControlContext eventAcc=event.getAccessControlContext();
        if(srcAcc==null){
            javaSecurityAccess.doIntersectionPrivilege(action,stack,eventAcc);
        }else{
            javaSecurityAccess.doIntersectionPrivilege(
                    new PrivilegedAction<Void>(){
                        public Void run(){
                            javaSecurityAccess.doIntersectionPrivilege(action,eventAcc);
                            return null;
                        }
                    },stack,srcAcc);
        }
    }

    private static AccessControlContext getAccessControlContextFrom(Object src){
        return src instanceof Component?
                ((Component)src).getAccessControlContext():
                src instanceof MenuComponent?
                        ((MenuComponent)src).getAccessControlContext():
                        src instanceof TrayIcon?
                                ((TrayIcon)src).getAccessControlContext():
                                null;
    }

    private void dispatchEventImpl(final AWTEvent event,final Object src){
        event.isPosted=true;
        if(event instanceof ActiveEvent){
            // This could become the sole method of dispatching in time.
            setCurrentEventAndMostRecentTimeImpl(event);
            ((ActiveEvent)event).dispatch();
        }else if(src instanceof Component){
            ((Component)src).dispatchEvent(event);
            event.dispatched();
        }else if(src instanceof MenuComponent){
            ((MenuComponent)src).dispatchEvent(event);
        }else if(src instanceof TrayIcon){
            ((TrayIcon)src).dispatchEvent(event);
        }else if(src instanceof AWTAutoShutdown){
            if(noEvents()){
                dispatchThread.stopDispatching();
            }
        }else{
            if(getEventLog().isLoggable(PlatformLogger.Level.FINE)){
                getEventLog().fine("Unable to dispatch event: "+event);
            }
        }
    }

    private static final PlatformLogger getEventLog(){
        if(eventLog==null){
            eventLog=PlatformLogger.getLogger("java.awt.event.EventQueue");
        }
        return eventLog;
    }

    private void setCurrentEventAndMostRecentTimeImpl(AWTEvent e){
        pushPopLock.lock();
        try{
            if(Thread.currentThread()!=dispatchThread){
                return;
            }
            currentEvent=new WeakReference<>(e);
            // This series of 'instanceof' checks should be replaced with a
            // polymorphic type (for example, an interface which declares a
            // getWhen() method). However, this would require us to make such
            // a type public, or to place it in sun.awt. Both of these approaches
            // have been frowned upon. So for now, we hack.
            //
            // In tiger, we will probably give timestamps to all events, so this
            // will no longer be an issue.
            long mostRecentEventTime2=Long.MIN_VALUE;
            if(e instanceof InputEvent){
                InputEvent ie=(InputEvent)e;
                mostRecentEventTime2=ie.getWhen();
                if(e instanceof KeyEvent){
                    mostRecentKeyEventTime=ie.getWhen();
                }
            }else if(e instanceof InputMethodEvent){
                InputMethodEvent ime=(InputMethodEvent)e;
                mostRecentEventTime2=ime.getWhen();
            }else if(e instanceof ActionEvent){
                ActionEvent ae=(ActionEvent)e;
                mostRecentEventTime2=ae.getWhen();
            }else if(e instanceof InvocationEvent){
                InvocationEvent ie=(InvocationEvent)e;
                mostRecentEventTime2=ie.getWhen();
            }
            mostRecentEventTime=Math.max(mostRecentEventTime,mostRecentEventTime2);
        }finally{
            pushPopLock.unlock();
        }
    }

    final boolean isDispatchThreadImpl(){
        EventQueue eq=this;
        pushPopLock.lock();
        try{
            EventQueue next=eq.nextQueue;
            while(next!=null){
                eq=next;
                next=eq.nextQueue;
            }
            if(eq.fwDispatcher!=null){
                return eq.fwDispatcher.isDispatchThread();
            }
            return (Thread.currentThread()==eq.dispatchThread);
        }finally{
            pushPopLock.unlock();
        }
    }

    private long getMostRecentEventTimeImpl(){
        pushPopLock.lock();
        try{
            return (Thread.currentThread()==dispatchThread)
                    ?mostRecentEventTime
                    :System.currentTimeMillis();
        }finally{
            pushPopLock.unlock();
        }
    }

    long getMostRecentEventTimeEx(){
        pushPopLock.lock();
        try{
            return mostRecentEventTime;
        }finally{
            pushPopLock.unlock();
        }
    }

    private AWTEvent getCurrentEventImpl(){
        pushPopLock.lock();
        try{
            return (Thread.currentThread()==dispatchThread)
                    ?currentEvent.get()
                    :null;
        }finally{
            pushPopLock.unlock();
        }
    }

    public void push(EventQueue newEventQueue){
        if(getEventLog().isLoggable(PlatformLogger.Level.FINE)){
            getEventLog().fine("EventQueue.push("+newEventQueue+")");
        }
        pushPopLock.lock();
        try{
            EventQueue topQueue=this;
            while(topQueue.nextQueue!=null){
                topQueue=topQueue.nextQueue;
            }
            if(topQueue.fwDispatcher!=null){
                throw new RuntimeException("push() to queue with fwDispatcher");
            }
            if((topQueue.dispatchThread!=null)&&
                    (topQueue.dispatchThread.getEventQueue()==this)){
                newEventQueue.dispatchThread=topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(newEventQueue);
            }
            // Transfer all events forward to new EventQueue.
            while(topQueue.peekEvent()!=null){
                try{
                    // Use getNextEventPrivate() as it doesn't call flushPendingEvents()
                    newEventQueue.postEventPrivate(topQueue.getNextEventPrivate());
                }catch(InterruptedException ie){
                    if(getEventLog().isLoggable(PlatformLogger.Level.FINE)){
                        getEventLog().fine("Interrupted push",ie);
                    }
                }
            }
            if(topQueue.dispatchThread!=null){
                // Wake up EDT waiting in getNextEvent(), so it can
                // pick up a new EventQueue. Post the waking event before
                // topQueue.nextQueue is assigned, otherwise the event would
                // go newEventQueue
                topQueue.postEventPrivate(new InvocationEvent(topQueue,dummyRunnable));
            }
            newEventQueue.previousQueue=topQueue;
            topQueue.nextQueue=newEventQueue;
            if(appContext.get(AppContext.EVENT_QUEUE_KEY)==topQueue){
                appContext.put(AppContext.EVENT_QUEUE_KEY,newEventQueue);
            }
            pushPopCond.signalAll();
        }finally{
            pushPopLock.unlock();
        }
    }

    protected void pop() throws EmptyStackException{
        if(getEventLog().isLoggable(PlatformLogger.Level.FINE)){
            getEventLog().fine("EventQueue.pop("+this+")");
        }
        pushPopLock.lock();
        try{
            EventQueue topQueue=this;
            while(topQueue.nextQueue!=null){
                topQueue=topQueue.nextQueue;
            }
            EventQueue prevQueue=topQueue.previousQueue;
            if(prevQueue==null){
                throw new EmptyStackException();
            }
            topQueue.previousQueue=null;
            prevQueue.nextQueue=null;
            // Transfer all events back to previous EventQueue.
            while(topQueue.peekEvent()!=null){
                try{
                    prevQueue.postEventPrivate(topQueue.getNextEventPrivate());
                }catch(InterruptedException ie){
                    if(getEventLog().isLoggable(PlatformLogger.Level.FINE)){
                        getEventLog().fine("Interrupted pop",ie);
                    }
                }
            }
            if((topQueue.dispatchThread!=null)&&
                    (topQueue.dispatchThread.getEventQueue()==this)){
                prevQueue.dispatchThread=topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(prevQueue);
            }
            if(appContext.get(AppContext.EVENT_QUEUE_KEY)==this){
                appContext.put(AppContext.EVENT_QUEUE_KEY,prevQueue);
            }
            // Wake up EDT waiting in getNextEvent(), so it can
            // pick up a new EventQueue
            topQueue.postEventPrivate(new InvocationEvent(topQueue,dummyRunnable));
            pushPopCond.signalAll();
        }finally{
            pushPopLock.unlock();
        }
    }

    public SecondaryLoop createSecondaryLoop(){
        return createSecondaryLoop(null,null,0);
    }

    SecondaryLoop createSecondaryLoop(Conditional cond,EventFilter filter,long interval){
        pushPopLock.lock();
        try{
            if(nextQueue!=null){
                // Forward the request to the top of EventQueue stack
                return nextQueue.createSecondaryLoop(cond,filter,interval);
            }
            if(fwDispatcher!=null){
                return fwDispatcher.createSecondaryLoop();
            }
            if(dispatchThread==null){
                initDispatchThread();
            }
            return new WaitDispatchSupport(dispatchThread,cond,filter,interval);
        }finally{
            pushPopLock.unlock();
        }
    }

    final void detachDispatchThread(EventDispatchThread edt){
        /**
         * Minimize discard possibility for non-posted events
         */
        SunToolkit.flushPendingEvents(appContext);
        /**
         * This synchronized block is to secure that the event dispatch
         * thread won't die in the middle of posting a new event to the
         * associated event queue. It is important because we notify
         * that the event dispatch thread is busy after posting a new event
         * to its queue, so the EventQueue.dispatchThread reference must
         * be valid at that point.
         */
        pushPopLock.lock();
        try{
            if(edt==dispatchThread){
                dispatchThread=null;
            }
            AWTAutoShutdown.getInstance().notifyThreadFree(edt);
            /**
             * Event was posted after EDT events pumping had stopped, so start
             * another EDT to handle this event
             */
            if(peekEvent()!=null){
                initDispatchThread();
            }
        }finally{
            pushPopLock.unlock();
        }
    }

    public AWTEvent peekEvent(){
        pushPopLock.lock();
        try{
            for(int i=NUM_PRIORITIES-1;i>=0;i--){
                if(queues[i].head!=null){
                    return queues[i].head.event;
                }
            }
        }finally{
            pushPopLock.unlock();
        }
        return null;
    }

    final EventDispatchThread getDispatchThread(){
        pushPopLock.lock();
        try{
            return dispatchThread;
        }finally{
            pushPopLock.unlock();
        }
    }

    final void removeSourceEvents(Object source,boolean removeAllEvents){
        SunToolkit.flushPendingEvents(appContext);
        pushPopLock.lock();
        try{
            for(int i=0;i<NUM_PRIORITIES;i++){
                EventQueueItem entry=queues[i].head;
                EventQueueItem prev=null;
                while(entry!=null){
                    if((entry.event.getSource()==source)
                            &&(removeAllEvents
                            ||!(entry.event instanceof SequencedEvent
                            ||entry.event instanceof SentEvent
                            ||entry.event instanceof FocusEvent
                            ||entry.event instanceof WindowEvent
                            ||entry.event instanceof KeyEvent
                            ||entry.event instanceof InputMethodEvent))){
                        if(entry.event instanceof SequencedEvent){
                            ((SequencedEvent)entry.event).dispose();
                        }
                        if(entry.event instanceof SentEvent){
                            ((SentEvent)entry.event).dispose();
                        }
                        if(entry.event instanceof InvocationEvent){
                            AWTAccessor.getInvocationEventAccessor()
                                    .dispose((InvocationEvent)entry.event);
                        }
                        if(prev==null){
                            queues[i].head=entry.next;
                        }else{
                            prev.next=entry.next;
                        }
                        uncacheEQItem(entry);
                    }else{
                        prev=entry;
                    }
                    entry=entry.next;
                }
                queues[i].tail=prev;
            }
        }finally{
            pushPopLock.unlock();
        }
    }

    synchronized long getMostRecentKeyEventTime(){
        pushPopLock.lock();
        try{
            return mostRecentKeyEventTime;
        }finally{
            pushPopLock.unlock();
        }
    }

    private void wakeup(boolean isShutdown){
        pushPopLock.lock();
        try{
            if(nextQueue!=null){
                // Forward call to the top of EventQueue stack.
                nextQueue.wakeup(isShutdown);
            }else if(dispatchThread!=null){
                pushPopCond.signalAll();
            }else if(!isShutdown){
                initDispatchThread();
            }
        }finally{
            pushPopLock.unlock();
        }
    }

    // The method is used by AWTAccessor for javafx/AWT single threaded mode.
    private void setFwDispatcher(FwDispatcher dispatcher){
        if(nextQueue!=null){
            nextQueue.setFwDispatcher(dispatcher);
        }else{
            fwDispatcher=dispatcher;
        }
    }
}

class Queue{
    EventQueueItem head;
    EventQueueItem tail;
}
