/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

import org.w3c.dom.DOMLocator;
import org.w3c.dom.Node;

public class DOMLocatorImpl implements DOMLocator{
    //
    // Data
    //
    public int fColumnNumber=-1;
    public int fLineNumber=-1;
    public Node fRelatedNode=null;
    public String fUri=null;
    public int fByteOffset=-1;
    public int fUtf16Offset=-1;
    //
    // Constructors
    //

    public DOMLocatorImpl(){
    }

    public DOMLocatorImpl(int lineNumber,int columnNumber,String uri){
        fLineNumber=lineNumber;
        fColumnNumber=columnNumber;
        fUri=uri;
    } // DOMLocatorImpl (int lineNumber, int columnNumber, String uri )

    public DOMLocatorImpl(int lineNumber,int columnNumber,int utf16Offset,String uri){
        fLineNumber=lineNumber;
        fColumnNumber=columnNumber;
        fUri=uri;
        fUtf16Offset=utf16Offset;
    } // DOMLocatorImpl (int lineNumber, int columnNumber, int utf16Offset, String uri )

    public DOMLocatorImpl(int lineNumber,int columnNumber,int byteoffset,Node relatedData,String uri){
        fLineNumber=lineNumber;
        fColumnNumber=columnNumber;
        fByteOffset=byteoffset;
        fRelatedNode=relatedData;
        fUri=uri;
    } // DOMLocatorImpl (int lineNumber, int columnNumber, int offset, Node errorNode, String uri )

    public DOMLocatorImpl(int lineNumber,int columnNumber,int byteoffset,Node relatedData,String uri,int utf16Offset){
        fLineNumber=lineNumber;
        fColumnNumber=columnNumber;
        fByteOffset=byteoffset;
        fRelatedNode=relatedData;
        fUri=uri;
        fUtf16Offset=utf16Offset;
    } // DOMLocatorImpl (int lineNumber, int columnNumber, int offset, Node errorNode, String uri )

    public int getLineNumber(){
        return fLineNumber;
    }

    public int getColumnNumber(){
        return fColumnNumber;
    }

    public int getByteOffset(){
        return fByteOffset;
    }

    public int getUtf16Offset(){
        return fUtf16Offset;
    }

    public Node getRelatedNode(){
        return fRelatedNode;
    }

    public String getUri(){
        return fUri;
    }
}// class DOMLocatorImpl
