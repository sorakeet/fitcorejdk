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
 * $Id: StringToStringTable.java,v 1.2.4.1 2005/09/15 08:15:56 suresh_emailid Exp $
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
 * $Id: StringToStringTable.java,v 1.2.4.1 2005/09/15 08:15:56 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class StringToStringTable{
    private int m_blocksize;
    private String m_map[];
    private int m_firstFree=0;
    private int m_mapSize;

    public StringToStringTable(){
        m_blocksize=16;
        m_mapSize=m_blocksize;
        m_map=new String[m_blocksize];
    }

    public StringToStringTable(int blocksize){
        m_blocksize=blocksize;
        m_mapSize=blocksize;
        m_map=new String[blocksize];
    }

    public final int getLength(){
        return m_firstFree;
    }

    public final void put(String key,String value){
        if((m_firstFree+2)>=m_mapSize){
            m_mapSize+=m_blocksize;
            String newMap[]=new String[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        m_map[m_firstFree]=key;
        m_firstFree++;
        m_map[m_firstFree]=value;
        m_firstFree++;
    }

    public final String get(String key){
        for(int i=0;i<m_firstFree;i+=2){
            if(m_map[i].equals(key))
                return m_map[i+1];
        }
        return null;
    }

    public final void remove(String key){
        for(int i=0;i<m_firstFree;i+=2){
            if(m_map[i].equals(key)){
                if((i+2)<m_firstFree)
                    System.arraycopy(m_map,i+2,m_map,i,m_firstFree-(i+2));
                m_firstFree-=2;
                m_map[m_firstFree]=null;
                m_map[m_firstFree+1]=null;
                break;
            }
        }
    }

    public final String getIgnoreCase(String key){
        if(null==key)
            return null;
        for(int i=0;i<m_firstFree;i+=2){
            if(m_map[i].equalsIgnoreCase(key))
                return m_map[i+1];
        }
        return null;
    }

    public final String getByValue(String val){
        for(int i=1;i<m_firstFree;i+=2){
            if(m_map[i].equals(val))
                return m_map[i-1];
        }
        return null;
    }

    public final String elementAt(int i){
        return m_map[i];
    }

    public final boolean contains(String key){
        for(int i=0;i<m_firstFree;i+=2){
            if(m_map[i].equals(key))
                return true;
        }
        return false;
    }

    public final boolean containsValue(String val){
        for(int i=1;i<m_firstFree;i+=2){
            if(m_map[i].equals(val))
                return true;
        }
        return false;
    }
}
