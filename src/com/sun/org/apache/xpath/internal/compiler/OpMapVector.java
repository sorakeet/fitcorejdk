/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * $Id: OpMapVector.java,v 1.2.4.1 2005/09/10 03:57:14 jeffsuttor Exp $
 */
/**
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * $Id: OpMapVector.java,v 1.2.4.1 2005/09/10 03:57:14 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.compiler;

public class OpMapVector{
    protected int m_blocksize;
    protected int m_map[]; // IntStack is trying to see this directly
    protected int m_lengthPos=0;
    protected int m_mapSize;

    public OpMapVector(int blocksize,int increaseSize,int lengthPos){
        m_blocksize=increaseSize;
        m_mapSize=blocksize;
        m_lengthPos=lengthPos;
        m_map=new int[blocksize];
    }

    public final int elementAt(int i){
        return m_map[i];
    }

    public final void setElementAt(int value,int index){
        if(index>=m_mapSize){
            int oldSize=m_mapSize;
            m_mapSize+=m_blocksize;
            int newMap[]=new int[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,oldSize);
            m_map=newMap;
        }
        m_map[index]=value;
    }

    public final void setToSize(int size){
        int newMap[]=new int[size];
        System.arraycopy(m_map,0,newMap,0,m_map[m_lengthPos]);
        m_mapSize=size;
        m_map=newMap;
    }
}
