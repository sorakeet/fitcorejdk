/**
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.tasks;

import java.util.ArrayList;

public class ThreadService implements TaskServer{
    private static long counter=0;
// public methods
// --------------
    // protected or private variables
// ------------------------------
    private ArrayList<Runnable> jobList=new ArrayList<Runnable>(0);
    private ExecutorThread[] threadList;
    private int minThreads=1;
    private int currThreds=0;
    private int idle=0;
// private classes
// ---------------
    private boolean terminated=false;
    private int priority;
    private ThreadGroup threadGroup=new ThreadGroup("ThreadService");
    private ClassLoader cloader;
    private int addedJobs=1;
    private int doneJobs=1;
    public ThreadService(int threadNumber){
        if(threadNumber<=0){
            throw new IllegalArgumentException("The thread number should bigger than zero.");
        }
        minThreads=threadNumber;
        threadList=new ExecutorThread[threadNumber];
        priority=Thread.currentThread().getPriority();
        cloader=Thread.currentThread().getContextClassLoader();
    }

    public void submitTask(Task task) throws IllegalArgumentException{
        submitTask((Runnable)task);
    }

    public void submitTask(Runnable task) throws IllegalArgumentException{
        stateCheck();
        if(task==null){
            throw new IllegalArgumentException("No task specified.");
        }
        synchronized(jobList){
            jobList.add(jobList.size(),task);
            jobList.notify();
        }
        createThread();
    }

    // private methods
    private void stateCheck() throws IllegalStateException{
        if(terminated){
            throw new IllegalStateException("The thread service has been terminated.");
        }
    }

    private void createThread(){
        if(idle<1){
            synchronized(threadList){
                if(jobList.size()>0&&currThreds<minThreads){
                    ExecutorThread et=new ExecutorThread();
                    et.start();
                    threadList[currThreds++]=et;
                }
            }
        }
    }

    public Runnable removeTask(Runnable task){
        stateCheck();
        Runnable removed=null;
        synchronized(jobList){
            int lg=jobList.indexOf(task);
            if(lg>=0){
                removed=jobList.remove(lg);
            }
        }
        if(removed!=null&&removed instanceof Task)
            ((Task)removed).cancel();
        return removed;
    }

    // to terminate
    public void terminate(){
        if(terminated==true){
            return;
        }
        terminated=true;
        synchronized(jobList){
            jobList.notifyAll();
        }
        removeAll();
        for(int i=0;i<currThreds;i++){
            try{
                threadList[i].interrupt();
            }catch(Exception e){
                // TODO
            }
        }
        threadList=null;
    }

    public void removeAll(){
        stateCheck();
        final Object[] jobs;
        synchronized(jobList){
            jobs=jobList.toArray();
            jobList.clear();
        }
        final int len=jobs.length;
        for(int i=0;i<len;i++){
            final Object o=jobs[i];
            if(o!=null&&o instanceof Task) ((Task)o).cancel();
        }
    }

    // A thread used to execute jobs
    //
    private class ExecutorThread extends Thread{
        public ExecutorThread(){
            super(threadGroup,"ThreadService-"+counter++);
            setDaemon(true);
            // init
            this.setPriority(priority);
            this.setContextClassLoader(cloader);
            idle++;
        }

        public void run(){
            while(!terminated){
                Runnable job=null;
                synchronized(jobList){
                    if(jobList.size()>0){
                        job=jobList.remove(0);
                        if(jobList.size()>0){
                            jobList.notify();
                        }
                    }else{
                        try{
                            jobList.wait();
                        }catch(InterruptedException ie){
                            // terminated ?
                        }finally{
                        }
                        continue;
                    }
                }
                if(job!=null){
                    try{
                        idle--;
                        job.run();
                    }catch(Exception e){
                        // TODO
                        e.printStackTrace();
                    }finally{
                        idle++;
                    }
                }
                // re-init
                this.setPriority(priority);
                Thread.interrupted();
                this.setContextClassLoader(cloader);
            }
        }
    }
}
