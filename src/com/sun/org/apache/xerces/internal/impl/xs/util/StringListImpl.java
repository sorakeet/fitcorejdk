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

import com.sun.org.apache.xerces.internal.xs.StringList;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Vector;

public final class StringListImpl extends AbstractList implements StringList{
    public static final StringListImpl EMPTY_LIST=new StringListImpl(new String[0],0);
    // The array to hold all data
    private final String[] fArray;
    // Number of elements in this list
    private final int fLength;
    // REVISIT: this is temp solution. In general we need to use this class
    //          instead of the Vector.
    private final Vector fVector;

    public StringListImpl(Vector v){
        fVector=v;
        fLength=(v==null)?0:v.size();
        fArray=null;
    }

    public StringListImpl(String[] array,int length){
        fArray=array;
        fLength=length;
        fVector=null;
    }

    public Object get(int index){
        if(index>=0&&index<fLength){
            if(fVector!=null){
                return fVector.elementAt(index);
            }
            return fArray[index];
        }
        throw new IndexOutOfBoundsException("Index: "+index);
    }

    public int size(){
        return getLength();
    }

    public int getLength(){
        return fLength;
    }

    public boolean contains(String item){
        if(fVector!=null){
            return fVector.contains(item);
        }
        if(item==null){
            for(int i=0;i<fLength;i++){
                if(fArray[i]==null)
                    return true;
            }
        }else{
            for(int i=0;i<fLength;i++){
                if(item.equals(fArray[i]))
                    return true;
            }
        }
        return false;
    }

    public String item(int index){
        if(index<0||index>=fLength){
            return null;
        }
        if(fVector!=null){
            return (String)fVector.elementAt(index);
        }
        return fArray[index];
    }

    public Object[] toArray(){
        if(fVector!=null){
            return fVector.toArray();
        }
        Object[] a=new Object[fLength];
        toArray0(a);
        return a;
    }

    public Object[] toArray(Object[] a){
        if(fVector!=null){
            return fVector.toArray(a);
        }
        if(a.length<fLength){
            Class arrayClass=a.getClass();
            Class componentType=arrayClass.getComponentType();
            a=(Object[])Array.newInstance(componentType,fLength);
        }
        toArray0(a);
        if(a.length>fLength){
            a[fLength]=null;
        }
        return a;
    }

    private void toArray0(Object[] a){
        if(fLength>0){
            System.arraycopy(fArray,0,a,0,fLength);
        }
    }
} // class StringListImpl
