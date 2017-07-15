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
 * $Id: BoolStack.java,v 1.1.4.1 2005/09/08 11:03:08 suresh_emailid Exp $
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
 * $Id: BoolStack.java,v 1.1.4.1 2005/09/08 11:03:08 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer.utils;

public final class BoolStack{
    private boolean m_values[];
    private int m_allocatedSize;
    private int m_index;

    public BoolStack(){
        this(32);
    }

    public BoolStack(int size){
        m_allocatedSize=size;
        m_values=new boolean[size];
        m_index=-1;
    }

    public final int size(){
        return m_index+1;
    }

    public final void clear(){
        m_index=-1;
    }

    public final boolean push(boolean val){
        if(m_index==m_allocatedSize-1)
            grow();
        return (m_values[++m_index]=val);
    }

    private void grow(){
        m_allocatedSize*=2;
        boolean newVector[]=new boolean[m_allocatedSize];
        System.arraycopy(m_values,0,newVector,0,m_index+1);
        m_values=newVector;
    }

    public final boolean pop(){
        return m_values[m_index--];
    }

    public final boolean popAndTop(){
        m_index--;
        return (m_index>=0)?m_values[m_index]:false;
    }

    public final void setTop(boolean b){
        m_values[m_index]=b;
    }

    public final boolean peek(){
        return m_values[m_index];
    }

    public final boolean peekOrFalse(){
        return (m_index>-1)?m_values[m_index]:false;
    }

    public final boolean peekOrTrue(){
        return (m_index>-1)?m_values[m_index]:true;
    }

    public boolean isEmpty(){
        return (m_index==-1);
    }
}
