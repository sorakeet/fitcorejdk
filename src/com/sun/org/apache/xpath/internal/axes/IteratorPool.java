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
 * $Id: IteratorPool.java,v 1.2.4.1 2005/09/14 19:45:19 jeffsuttor Exp $
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
 * $Id: IteratorPool.java,v 1.2.4.1 2005/09/14 19:45:19 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;

import java.util.ArrayList;

public final class IteratorPool implements java.io.Serializable{
    static final long serialVersionUID=-460927331149566998L;
    private final DTMIterator m_orig;
    private final ArrayList m_freeStack;

    public IteratorPool(DTMIterator original){
        m_orig=original;
        m_freeStack=new ArrayList();
    }

    public synchronized DTMIterator getInstanceOrThrow()
            throws CloneNotSupportedException{
        // Check if the pool is empty.
        if(m_freeStack.isEmpty()){
            // Create a new object if so.
            return (DTMIterator)m_orig.clone();
        }else{
            // Remove object from end of free pool.
            DTMIterator result=(DTMIterator)m_freeStack.remove(m_freeStack.size()-1);
            return result;
        }
    }

    public synchronized DTMIterator getInstance(){
        // Check if the pool is empty.
        if(m_freeStack.isEmpty()){
            // Create a new object if so.
            try{
                return (DTMIterator)m_orig.clone();
            }catch(Exception ex){
                throw new WrappedRuntimeException(ex);
            }
        }else{
            // Remove object from end of free pool.
            DTMIterator result=(DTMIterator)m_freeStack.remove(m_freeStack.size()-1);
            return result;
        }
    }

    public synchronized void freeInstance(DTMIterator obj){
        m_freeStack.add(obj);
    }
}
