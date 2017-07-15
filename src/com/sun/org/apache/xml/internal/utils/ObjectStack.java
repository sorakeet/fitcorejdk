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
 * $Id: ObjectStack.java,v 1.2.4.1 2005/09/15 08:15:51 suresh_emailid Exp $
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
 * $Id: ObjectStack.java,v 1.2.4.1 2005/09/15 08:15:51 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import java.util.EmptyStackException;

public class ObjectStack extends ObjectVector{
    public ObjectStack(){
        super();
    }

    public ObjectStack(int blocksize){
        super(blocksize);
    }

    public ObjectStack(ObjectStack v){
        super(v);
    }

    public Object push(Object i){
        if((m_firstFree+1)>=m_mapSize){
            m_mapSize+=m_blocksize;
            Object newMap[]=new Object[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        m_map[m_firstFree]=i;
        m_firstFree++;
        return i;
    }

    public Object pop(){
        Object val=m_map[--m_firstFree];
        m_map[m_firstFree]=null;
        return val;
    }

    public void quickPop(int n){
        m_firstFree-=n;
    }

    public Object peek(){
        try{
            return m_map[m_firstFree-1];
        }catch(ArrayIndexOutOfBoundsException e){
            throw new EmptyStackException();
        }
    }

    public Object peek(int n){
        try{
            return m_map[m_firstFree-(1+n)];
        }catch(ArrayIndexOutOfBoundsException e){
            throw new EmptyStackException();
        }
    }

    public void setTop(Object val){
        try{
            m_map[m_firstFree-1]=val;
        }catch(ArrayIndexOutOfBoundsException e){
            throw new EmptyStackException();
        }
    }

    public boolean empty(){
        return m_firstFree==0;
    }

    public int search(Object o){
        int i=lastIndexOf(o);
        if(i>=0){
            return size()-i;
        }
        return -1;
    }

    public Object clone()
            throws CloneNotSupportedException{
        return (ObjectStack)super.clone();
    }
}
