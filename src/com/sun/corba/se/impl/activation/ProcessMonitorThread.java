/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.activation;

import com.sun.corba.se.impl.orbutil.ORBConstants;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

public class ProcessMonitorThread extends Thread{
    private static ProcessMonitorThread instance=null;
    private HashMap serverTable;
    private int sleepTime;

    private ProcessMonitorThread(HashMap ServerTable,int SleepTime){
        serverTable=ServerTable;
        sleepTime=SleepTime;
    }

    static void start(HashMap serverTable){
        int sleepTime=ORBConstants.DEFAULT_SERVER_POLLING_TIME;
        String pollingTime=System.getProperties().getProperty(
                ORBConstants.SERVER_POLLING_TIME);
        if(pollingTime!=null){
            try{
                sleepTime=Integer.parseInt(pollingTime);
            }catch(Exception e){
                // Too late to complain, Just use the default
                // sleepTime
            }
        }
        instance=new ProcessMonitorThread(serverTable,
                sleepTime);
        instance.setDaemon(true);
        instance.start();
    }

    static void interruptThread(){
        instance.interrupt();
    }

    public void run(){
        while(true){
            try{
                // Sleep's for a specified time, before checking
                // the Servers health. This will repeat as long as
                // the ServerManager (ORBD) is up and running.
                Thread.sleep(sleepTime);
            }catch(InterruptedException e){
                break;
            }
            Iterator serverList;
            synchronized(serverTable){
                // Check each ServerTableEntry to make sure that they
                // are in the right state.
                serverList=serverTable.values().iterator();
            }
            try{
                checkServerHealth(serverList);
            }catch(ConcurrentModificationException e){
                break;
            }
        }
    }

    private void checkServerHealth(Iterator serverList){
        if(serverList==null) return;
        while(serverList.hasNext()){
            ServerTableEntry entry=(ServerTableEntry)serverList.next();
            entry.checkProcessHealth();
        }
    }
}
