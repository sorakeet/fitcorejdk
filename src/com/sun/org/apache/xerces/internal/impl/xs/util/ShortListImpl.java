/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002,2003-2004 The Apache Software Foundation.
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
 */
/**
 * Copyright 2002,2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.xs.util;

import com.sun.org.apache.xerces.internal.xs.ShortList;
import com.sun.org.apache.xerces.internal.xs.XSException;

import java.util.AbstractList;

public final class ShortListImpl extends AbstractList implements ShortList{
    public static final ShortListImpl EMPTY_LIST=new ShortListImpl(new short[0],0);
    // The array to hold all data
    private final short[] fArray;
    // Number of elements in this list
    private final int fLength;

    public ShortListImpl(short[] array,int length){
        fArray=array;
        fLength=length;
    }

    public Object get(int index){
        if(index>=0&&index<fLength){
            return new Short(fArray[index]);
        }
        throw new IndexOutOfBoundsException("Index: "+index);
    }

    public boolean equals(Object obj){
        if(obj==null||!(obj instanceof ShortList)){
            return false;
        }
        ShortList rhs=(ShortList)obj;
        if(fLength!=rhs.getLength()){
            return false;
        }
        for(int i=0;i<fLength;++i){
            if(fArray[i]!=rhs.item(i)){
                return false;
            }
        }
        return true;
    }

    public int size(){
        return getLength();
    }

    public int getLength(){
        return fLength;
    }

    public boolean contains(short item){
        for(int i=0;i<fLength;i++){
            if(fArray[i]==item){
                return true;
            }
        }
        return false;
    }

    public short item(int index) throws XSException{
        if(index<0||index>=fLength){
            throw new XSException(XSException.INDEX_SIZE_ERR,null);
        }
        return fArray[index];
    }
} // class ShortListImpl
