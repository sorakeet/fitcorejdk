/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: ThreadControllerWrapper.java,v 1.2.4.1 2005/09/15 08:15:59 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: ThreadControllerWrapper.java,v 1.2.4.1 2005/09/15 08:15:59 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class ThreadControllerWrapper{
    private static ThreadController m_tpool=new ThreadController();

    public static Thread runThread(Runnable runnable,int priority){
        return m_tpool.run(runnable,priority);
    }

    public static void waitThread(Thread worker,Runnable task)
            throws InterruptedException{
        m_tpool.waitThread(worker,task);
    }

    public static class ThreadController{
        public Thread run(Runnable task,int priority){
            Thread t=new SafeThread(task);
            t.start();
            //       if( priority > 0 )
            //      t.setPriority( priority );
            return t;
        }

        public void waitThread(Thread worker,Runnable task)
                throws InterruptedException{
            // This should wait until the transformThread is considered not alive.
            worker.join();
        }

        final class SafeThread extends Thread{
            private volatile boolean ran=false;

            public SafeThread(Runnable target){
                super(target);
            }

            public final void run(){
                if(Thread.currentThread()!=this){
                    throw new IllegalStateException("The run() method in a"
                            +" SafeThread cannot be called from another thread.");
                }
                synchronized(this){
                    if(!ran){
                        ran=true;
                    }else{
                        throw new IllegalStateException("The run() method in a"
                                +" SafeThread cannot be called more than once.");
                    }
                }
                super.run();
            }
        }
    }
}
