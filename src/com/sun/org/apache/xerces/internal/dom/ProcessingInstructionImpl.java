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
package com.sun.org.apache.xerces.internal.dom;

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class ProcessingInstructionImpl
        extends CharacterDataImpl
        implements ProcessingInstruction{
    //
    // Constants
    //
    static final long serialVersionUID=7554435174099981510L;
    //
    // Data
    //
    protected String target;
    //
    // Constructors
    //

    public ProcessingInstructionImpl(CoreDocumentImpl ownerDoc,
                                     String target,String data){
        super(ownerDoc,data);
        this.target=target;
    }
    //
    // Node methods
    //

    public short getNodeType(){
        return Node.PROCESSING_INSTRUCTION_NODE;
    }

    public String getNodeName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return target;
    }
    //
    // ProcessingInstruction methods
    //

    public String getBaseURI(){
        if(needsSyncData()){
            synchronizeData();
        }
        return ownerNode.getBaseURI();
    }

    public String getTarget(){
        if(needsSyncData()){
            synchronizeData();
        }
        return target;
    } // getTarget():String

    public String getData(){
        if(needsSyncData()){
            synchronizeData();
        }
        return data;
    } // getData():String

    public void setData(String data){
        // Hand off to setNodeValue for code-reuse reasons (mutation
        // events, readonly protection, synchronizing, etc.)
        setNodeValue(data);
    } // setData(String)
} // class ProcessingInstructionImpl