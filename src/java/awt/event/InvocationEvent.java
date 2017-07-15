/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import sun.awt.AWTAccessor;

import java.awt.*;

public class InvocationEvent extends AWTEvent implements ActiveEvent{
    public static final int INVOCATION_FIRST=1200;
    public static final int INVOCATION_DEFAULT=INVOCATION_FIRST;
    public static final int INVOCATION_LAST=INVOCATION_DEFAULT;
    private static final long serialVersionUID=436056344909459450L;

    static{
        AWTAccessor.setInvocationEventAccessor(new AWTAccessor.InvocationEventAccessor(){
            @Override
            public void dispose(InvocationEvent invocationEvent){
                invocationEvent.finishedDispatching(false);
            }
        });
    }

    private final Runnable listener;
    protected Runnable runnable;
    protected volatile Object notifier;
    protected boolean catchExceptions;
    private volatile boolean dispatched=false;
    private Exception exception=null;
    private Throwable throwable=null;
    private long when;

    public InvocationEvent(Object source,Runnable runnable){
        this(source,INVOCATION_DEFAULT,runnable,null,null,false);
    }

    private InvocationEvent(Object source,int id,Runnable runnable,
                            Object notifier,Runnable listener,boolean catchThrowables){
        super(source,id);
        this.runnable=runnable;
        this.notifier=notifier;
        this.listener=listener;
        this.catchExceptions=catchThrowables;
        this.when=System.currentTimeMillis();
    }

    public InvocationEvent(Object source,Runnable runnable,Object notifier,
                           boolean catchThrowables){
        this(source,INVOCATION_DEFAULT,runnable,notifier,null,catchThrowables);
    }

    public InvocationEvent(Object source,Runnable runnable,Runnable listener,
                           boolean catchThrowables){
        this(source,INVOCATION_DEFAULT,runnable,null,listener,catchThrowables);
    }

    protected InvocationEvent(Object source,int id,Runnable runnable,
                              Object notifier,boolean catchThrowables){
        this(source,id,runnable,notifier,null,catchThrowables);
    }

    public void dispatch(){
        try{
            if(catchExceptions){
                try{
                    runnable.run();
                }catch(Throwable t){
                    if(t instanceof Exception){
                        exception=(Exception)t;
                    }
                    throwable=t;
                }
            }else{
                runnable.run();
            }
        }finally{
            finishedDispatching(true);
        }
    }

    private void finishedDispatching(boolean dispatched){
        this.dispatched=dispatched;
        if(notifier!=null){
            synchronized(notifier){
                notifier.notifyAll();
            }
        }
        if(listener!=null){
            listener.run();
        }
    }

    public Exception getException(){
        return (catchExceptions)?exception:null;
    }

    public Throwable getThrowable(){
        return (catchExceptions)?throwable:null;
    }

    public long getWhen(){
        return when;
    }

    public boolean isDispatched(){
        return dispatched;
    }

    public String paramString(){
        String typeStr;
        switch(id){
            case INVOCATION_DEFAULT:
                typeStr="INVOCATION_DEFAULT";
                break;
            default:
                typeStr="unknown type";
        }
        return typeStr+",runnable="+runnable+",notifier="+notifier+
                ",catchExceptions="+catchExceptions+",when="+when;
    }
}
