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
 * $Id: CoroutineManager.java,v 1.2.4.1 2005/09/15 08:14:58 suresh_emailid Exp $
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
 * $Id: CoroutineManager.java,v 1.2.4.1 2005/09/15 08:14:58 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;

import java.util.BitSet;

public class CoroutineManager{
    static final int m_unreasonableId=1024;
    // Expose???
    final static int NOBODY=-1;
    final static int ANYBODY=-1;
    BitSet m_activeIDs=new BitSet();
    Object m_yield=null;
    int m_nextCoroutine=NOBODY;

    public synchronized int co_joinCoroutineSet(int coroutineID){
        if(coroutineID>=0){
            if(coroutineID>=m_unreasonableId||m_activeIDs.get(coroutineID))
                return -1;
        }else{
            // What I want is "Find first clear bit". That doesn't exist.
            // JDK1.2 added "find last set bit", but that doesn't help now.
            coroutineID=0;
            while(coroutineID<m_unreasonableId){
                if(m_activeIDs.get(coroutineID))
                    ++coroutineID;
                else
                    break;
            }
            if(coroutineID>=m_unreasonableId)
                return -1;
        }
        m_activeIDs.set(coroutineID);
        return coroutineID;
    }

    public synchronized Object co_entry_pause(int thisCoroutine) throws NoSuchMethodException{
        if(!m_activeIDs.get(thisCoroutine))
            throw new NoSuchMethodException();
        while(m_nextCoroutine!=thisCoroutine){
            try{
                wait();
            }catch(InterruptedException e){
                // %TBD% -- Declare? Encapsulate? Ignore? Or
                // dance widdershins about the instruction cache?
            }
        }
        return m_yield;
    }

    public synchronized Object co_resume(Object arg_object,int thisCoroutine,int toCoroutine) throws NoSuchMethodException{
        if(!m_activeIDs.get(toCoroutine))
            throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_NOT_AVAIL,new Object[]{Integer.toString(toCoroutine)})); //"Coroutine not available, id="+toCoroutine);
        // We expect these values to be overwritten during the notify()/wait()
        // periods, as other coroutines in this set get their opportunity to run.
        m_yield=arg_object;
        m_nextCoroutine=toCoroutine;
        notify();
        while(m_nextCoroutine!=thisCoroutine||m_nextCoroutine==ANYBODY||m_nextCoroutine==NOBODY){
            try{
                // System.out.println("waiting...");
                wait();
            }catch(InterruptedException e){
                // %TBD% -- Declare? Encapsulate? Ignore? Or
                // dance deasil about the program counter?
            }
        }
        if(m_nextCoroutine==NOBODY){
            // Pass it along
            co_exit(thisCoroutine);
            // And inform this coroutine that its partners are Going Away
            // %REVIEW% Should this throw/return something more useful?
            throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_CO_EXIT,null)); //"CoroutineManager recieved co_exit() request");
        }
        return m_yield;
    }

    public synchronized void co_exit(int thisCoroutine){
        m_activeIDs.clear(thisCoroutine);
        m_nextCoroutine=NOBODY; // %REVIEW%
        notify();
    }

    public synchronized void co_exit_to(Object arg_object,int thisCoroutine,int toCoroutine) throws NoSuchMethodException{
        if(!m_activeIDs.get(toCoroutine))
            throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_NOT_AVAIL,new Object[]{Integer.toString(toCoroutine)})); //"Coroutine not available, id="+toCoroutine);
        // We expect these values to be overwritten during the notify()/wait()
        // periods, as other coroutines in this set get their opportunity to run.
        m_yield=arg_object;
        m_nextCoroutine=toCoroutine;
        m_activeIDs.clear(thisCoroutine);
        notify();
    }
}
