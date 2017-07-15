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

public class DeferredTextImpl
        extends TextImpl
        implements DeferredNode{
    //
    // Constants
    //
    static final long serialVersionUID=2310613872100393425L;
    //
    // Data
    //
    protected transient int fNodeIndex;
    //
    // Constructors
    //

    DeferredTextImpl(DeferredDocumentImpl ownerDocument,int nodeIndex){
        super(ownerDocument,null);
        fNodeIndex=nodeIndex;
        needsSyncData(true);
    } // <init>(DeferredDocumentImpl,int)
    //
    // DeferredNode methods
    //

    public int getNodeIndex(){
        return fNodeIndex;
    }
    //
    // Protected methods
    //

    protected void synchronizeData(){
        // no need for future synchronizations
        needsSyncData(false);
        // get initial text value
        DeferredDocumentImpl ownerDocument=
                (DeferredDocumentImpl)this.ownerDocument();
        data=ownerDocument.getNodeValueString(fNodeIndex);
        // NOTE: We used to normalize adjacent text node values here.
        //       This code has moved to the DeferredDocumentImpl
        //       getNodeValueString() method. -Ac
        // ignorable whitespace
        isIgnorableWhitespace(ownerDocument.getNodeExtra(fNodeIndex)==1);
    } // synchronizeData()
} // class DeferredTextImpl
