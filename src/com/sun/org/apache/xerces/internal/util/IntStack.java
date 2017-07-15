/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.util;

public final class IntStack{
    //
    // Data
    //
    private int fDepth;
    private int[] fData;
    //
    // Public methods
    //

    public int size(){
        return fDepth;
    }

    public void push(int value){
        ensureCapacity(fDepth+1);
        fData[fDepth++]=value;
    }

    private void ensureCapacity(int size){
        if(fData==null){
            fData=new int[32];
        }else if(fData.length<=size){
            int[] newdata=new int[fData.length*2];
            System.arraycopy(fData,0,newdata,0,fData.length);
            fData=newdata;
        }
    }

    public int peek(){
        return fData[fDepth-1];
    }

    public int elementAt(int depth){
        return fData[depth];
    }

    public int pop(){
        return fData[--fDepth];
    }
    // debugging

    public void clear(){
        fDepth=0;
    }
    //
    // Private methods
    //

    public void print(){
        System.out.print('(');
        System.out.print(fDepth);
        System.out.print(") {");
        for(int i=0;i<fDepth;i++){
            if(i==3){
                System.out.print(" ...");
                break;
            }
            System.out.print(' ');
            System.out.print(fData[i]);
            if(i<fDepth-1){
                System.out.print(',');
            }
        }
        System.out.print(" }");
        System.out.println();
    }
} // class IntStack
