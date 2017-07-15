/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.transport;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ReaderThread;
import com.sun.corba.se.pept.transport.Selector;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.Work;

public class ReaderThreadImpl
        implements
        ReaderThread,
        Work{
    private ORB orb;
    private Connection connection;
    private Selector selector;
    private boolean keepRunning;
    private long enqueueTime;

    public ReaderThreadImpl(ORB orb,
                            Connection connection,Selector selector){
        this.orb=orb;
        this.connection=connection;
        this.selector=selector;
        keepRunning=true;
    }
    ////////////////////////////////////////////////////
    //
    // ReaderThread methods.
    //

    // REVISIT - this needs alot more from previous ReaderThread.
    public void doWork(){
        try{
            if(orb.transportDebugFlag){
                dprint(".doWork: Start ReaderThread: "+connection);
            }
            while(keepRunning){
                try{
                    if(orb.transportDebugFlag){
                        dprint(".doWork: Start ReaderThread cycle: "
                                +connection);
                    }
                    if(connection.read()){
                        // REVISIT - put in pool;
                        return;
                    }
                    if(orb.transportDebugFlag){
                        dprint(".doWork: End ReaderThread cycle: "
                                +connection);
                    }
                }catch(Throwable t){
                    if(orb.transportDebugFlag){
                        dprint(".doWork: exception in read: "+connection,t);
                    }
                    orb.getTransportManager().getSelector(0)
                            .unregisterForEvent(getConnection().getEventHandler());
                    getConnection().close();
                }
            }
        }finally{
            if(orb.transportDebugFlag){
                dprint(".doWork: Terminated ReaderThread: "+connection);
            }
        }
    }

    public Connection getConnection(){
        return connection;
    }
    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    public void close(){
        if(orb.transportDebugFlag){
            dprint(".close: "+connection);
        }
        keepRunning=false;
    }

    private void dprint(String msg){
        ORBUtility.dprint("ReaderThreadImpl",msg);
    }    public void setEnqueueTime(long timeInMillis){
        enqueueTime=timeInMillis;
    }

    protected void dprint(String msg,Throwable t){
        dprint(msg);
        t.printStackTrace(System.out);
    }    public long getEnqueueTime(){
        return enqueueTime;
    }

    public String getName(){
        return "ReaderThread";
    }
    ////////////////////////////////////////////////////
    //
    // Implementation.
    //




}
// End of file.
