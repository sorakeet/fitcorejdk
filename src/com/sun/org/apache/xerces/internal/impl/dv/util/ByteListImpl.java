/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004 The Apache Software Foundation.
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
 * Copyright 2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.dv.util;

import com.sun.org.apache.xerces.internal.xs.XSException;
import com.sun.org.apache.xerces.internal.xs.datatypes.ByteList;

import java.util.AbstractList;

public class ByteListImpl extends AbstractList implements ByteList{
    // actually data stored in a byte array
    protected final byte[] data;
    // canonical representation of the data
    protected String canonical;

    public ByteListImpl(byte[] data){
        this.data=data;
    }

    public Object get(int index){
        if(index>=0&&index<data.length){
            return new Byte(data[index]);
        }
        throw new IndexOutOfBoundsException("Index: "+index);
    }

    public int size(){
        return getLength();
    }

    public int getLength(){
        return data.length;
    }

    public boolean contains(byte item){
        for(int i=0;i<data.length;++i){
            if(data[i]==item){
                return true;
            }
        }
        return false;
    }

    public byte item(int index)
            throws XSException{
        if(index<0||index>data.length-1){
            throw new XSException(XSException.INDEX_SIZE_ERR,null);
        }
        return data[index];
    }
}
