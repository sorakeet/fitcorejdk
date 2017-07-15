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
 * $Id: StringToStringTableVector.java,v 1.2.4.1 2005/09/15 08:15:56 suresh_emailid Exp $
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
 * $Id: StringToStringTableVector.java,v 1.2.4.1 2005/09/15 08:15:56 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class StringToStringTableVector{
    private int m_blocksize;
    private StringToStringTable m_map[];
    private int m_firstFree=0;
    private int m_mapSize;

    public StringToStringTableVector(){
        m_blocksize=8;
        m_mapSize=m_blocksize;
        m_map=new StringToStringTable[m_blocksize];
    }

    public StringToStringTableVector(int blocksize){
        m_blocksize=blocksize;
        m_mapSize=blocksize;
        m_map=new StringToStringTable[blocksize];
    }

    public final int getLength(){
        return m_firstFree;
    }

    public final int size(){
        return m_firstFree;
    }

    public final void addElement(StringToStringTable value){
        if((m_firstFree+1)>=m_mapSize){
            m_mapSize+=m_blocksize;
            StringToStringTable newMap[]=new StringToStringTable[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        m_map[m_firstFree]=value;
        m_firstFree++;
    }

    public final String get(String key){
        for(int i=m_firstFree-1;i>=0;--i){
            String nsuri=m_map[i].get(key);
            if(nsuri!=null)
                return nsuri;
        }
        return null;
    }

    public final boolean containsKey(String key){
        for(int i=m_firstFree-1;i>=0;--i){
            if(m_map[i].get(key)!=null)
                return true;
        }
        return false;
    }

    public final void removeLastElem(){
        if(m_firstFree>0){
            m_map[m_firstFree]=null;
            m_firstFree--;
        }
    }

    public final StringToStringTable elementAt(int i){
        return m_map[i];
    }

    public final boolean contains(StringToStringTable s){
        for(int i=0;i<m_firstFree;i++){
            if(m_map[i].equals(s))
                return true;
        }
        return false;
    }
}
