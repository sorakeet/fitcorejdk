/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orbutil.threadpool;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchThreadPoolException;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolChooser;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolManagerImpl implements ThreadPoolManager{
    private static final ORBUtilSystemException wrapper=
            ORBUtilSystemException.get(CORBALogDomains.RPC_TRANSPORT);
    private static AtomicInteger tgCount=new AtomicInteger();
    private ThreadPool threadPool;
    private ThreadGroup threadGroup;

    public ThreadPoolManagerImpl(){
        threadGroup=getThreadGroup();
        threadPool=new ThreadPoolImpl(threadGroup,
                ORBConstants.THREADPOOL_DEFAULT_NAME);
    }

    private ThreadGroup getThreadGroup(){
        ThreadGroup tg;
        // See bugs 4916766 and 4936203
        // We intend to create new threads in a reliable thread group.
        // This avoids problems if the application/applet
        // creates a thread group, makes JavaIDL calls which create a new
        // connection and ReaderThread, and then destroys the thread
        // group. If our ReaderThreads were to be part of such destroyed thread
        // group then it might get killed and cause other invoking threads
        // sharing the same connection to get a non-restartable
        // CommunicationFailure. We'd like to avoid that.
        //
        // Our solution is to create all of our threads in the highest thread
        // group that we have access to, given our own security clearance.
        //
        try{
            // try to get a thread group that's as high in the threadgroup
            // parent-child hierarchy, as we can get to.
            // this will prevent an ORB thread created during applet-init from
            // being killed when an applet dies.
            tg=AccessController.doPrivileged(
                    new PrivilegedAction<ThreadGroup>(){
                        public ThreadGroup run(){
                            ThreadGroup tg=Thread.currentThread().getThreadGroup();
                            ThreadGroup ptg=tg;
                            try{
                                while(ptg!=null){
                                    tg=ptg;
                                    ptg=tg.getParent();
                                }
                            }catch(SecurityException se){
                                // Discontinue going higher on a security exception.
                            }
                            return new ThreadGroup(tg,"ORB ThreadGroup "+tgCount.getAndIncrement());
                        }
                    }
            );
        }catch(SecurityException e){
            // something wrong, we go back to the original code
            tg=Thread.currentThread().getThreadGroup();
        }
        return tg;
    }

    public void close(){
        try{
            threadPool.close();
        }catch(IOException exc){
            wrapper.threadPoolCloseError();
        }
        try{
            boolean isDestroyed=threadGroup.isDestroyed();
            int numThreads=threadGroup.activeCount();
            int numGroups=threadGroup.activeGroupCount();
            if(isDestroyed){
                wrapper.threadGroupIsDestroyed(threadGroup);
            }else{
                if(numThreads>0)
                    wrapper.threadGroupHasActiveThreadsInClose(threadGroup,numThreads);
                if(numGroups>0)
                    wrapper.threadGroupHasSubGroupsInClose(threadGroup,numGroups);
                threadGroup.destroy();
            }
        }catch(IllegalThreadStateException exc){
            wrapper.threadGroupDestroyFailed(exc,threadGroup);
        }
        threadGroup=null;
    }

    public ThreadPool getThreadPool(String threadpoolId)
            throws NoSuchThreadPoolException{
        return threadPool;
    }

    public ThreadPool getThreadPool(int numericIdForThreadpool)
            throws NoSuchThreadPoolException{
        return threadPool;
    }

    public int getThreadPoolNumericId(String threadpoolId){
        return 0;
    }

    public String getThreadPoolStringId(int numericIdForThreadpool){
        return "";
    }

    public ThreadPool getDefaultThreadPool(){
        return threadPool;
    }

    public ThreadPoolChooser getThreadPoolChooser(String componentId){
        //FIXME: This method is not used, but should be fixed once
        //nio select starts working and we start using ThreadPoolChooser
        return null;
    }

    public ThreadPoolChooser getThreadPoolChooser(int componentIndex){
        //FIXME: This method is not used, but should be fixed once
        //nio select starts working and we start using ThreadPoolChooser
        return null;
    }

    public void setThreadPoolChooser(String componentId,ThreadPoolChooser aThreadPoolChooser){
        //FIXME: This method is not used, but should be fixed once
        //nio select starts working and we start using ThreadPoolChooser
    }

    public int getThreadPoolChooserNumericId(String componentId){
        //FIXME: This method is not used, but should be fixed once
        //nio select starts working and we start using ThreadPoolChooser
        return 0;
    }
}
// End of file.
