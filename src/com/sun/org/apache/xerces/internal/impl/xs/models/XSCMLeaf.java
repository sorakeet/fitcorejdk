/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.models;

import com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode;
import com.sun.org.apache.xerces.internal.impl.dtd.models.CMStateSet;

public class XSCMLeaf
        extends CMNode{
    //
    // Data
    //
    private Object fLeaf=null;
    private int fParticleId=-1;
    private int fPosition=-1;
    //
    // Constructors
    //

    public XSCMLeaf(int type,Object leaf,int id,int position){
        super(type);
        // Store the element index and position
        fLeaf=leaf;
        fParticleId=id;
        fPosition=position;
    }
    //
    // Package methods
    //

    final Object getLeaf(){
        return fLeaf;
    }

    final int getParticleId(){
        return fParticleId;
    }

    final int getPosition(){
        return fPosition;
    }

    final void setPosition(int newPosition){
        fPosition=newPosition;
    }
    //
    // CMNode methods
    //
    // package

    public boolean isNullable(){
        // Leaf nodes are never nullable unless its an epsilon node
        return (fPosition==-1);
    }

    protected void calcFirstPos(CMStateSet toSet){
        // If we are an epsilon node, then the first pos is an empty set
        if(fPosition==-1)
            toSet.zeroBits();
            // Otherwise, its just the one bit of our position
        else
            toSet.setBit(fPosition);
    }
    // protected

    protected void calcLastPos(CMStateSet toSet){
        // If we are an epsilon node, then the last pos is an empty set
        if(fPosition==-1)
            toSet.zeroBits();
            // Otherwise, its just the one bit of our position
        else
            toSet.setBit(fPosition);
    }

    public String toString(){
        StringBuffer strRet=new StringBuffer(fLeaf.toString());
        if(fPosition>=0){
            strRet.append
                    (
                            " (Pos:"
                                    +Integer.toString(fPosition)
                                    +")"
                    );
        }
        return strRet.toString();
    }
} // class XSCMLeaf
