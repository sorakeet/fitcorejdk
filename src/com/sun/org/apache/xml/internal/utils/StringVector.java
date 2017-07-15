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
 * $Id: StringVector.java,v 1.2.4.1 2005/09/15 08:15:56 suresh_emailid Exp $
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
 * $Id: StringVector.java,v 1.2.4.1 2005/09/15 08:15:56 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class StringVector implements java.io.Serializable{
    static final long serialVersionUID=4995234972032919748L;
    protected int m_blocksize;
    protected String m_map[];
    protected int m_firstFree=0;
    protected int m_mapSize;

    public StringVector(){
        m_blocksize=8;
        m_mapSize=m_blocksize;
        m_map=new String[m_blocksize];
    }

    public StringVector(int blocksize){
        m_blocksize=blocksize;
        m_mapSize=blocksize;
        m_map=new String[blocksize];
    }

    public int getLength(){
        return m_firstFree;
    }

    public final int size(){
        return m_firstFree;
    }

    public final void addElement(String value){
        if((m_firstFree+1)>=m_mapSize){
            m_mapSize+=m_blocksize;
            String newMap[]=new String[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        m_map[m_firstFree]=value;
        m_firstFree++;
    }

    public final String elementAt(int i){
        return m_map[i];
    }

    public final boolean contains(String s){
        if(null==s)
            return false;
        for(int i=0;i<m_firstFree;i++){
            if(m_map[i].equals(s))
                return true;
        }
        return false;
    }

    public final boolean containsIgnoreCase(String s){
        if(null==s)
            return false;
        for(int i=0;i<m_firstFree;i++){
            if(m_map[i].equalsIgnoreCase(s))
                return true;
        }
        return false;
    }

    public final void push(String s){
        if((m_firstFree+1)>=m_mapSize){
            m_mapSize+=m_blocksize;
            String newMap[]=new String[m_mapSize];
            System.arraycopy(m_map,0,newMap,0,m_firstFree+1);
            m_map=newMap;
        }
        m_map[m_firstFree]=s;
        m_firstFree++;
    }

    public final String pop(){
        if(m_firstFree<=0)
            return null;
        m_firstFree--;
        String s=m_map[m_firstFree];
        m_map[m_firstFree]=null;
        return s;
    }

    public final String peek(){
        return (m_firstFree<=0)?null:m_map[m_firstFree-1];
    }
}
