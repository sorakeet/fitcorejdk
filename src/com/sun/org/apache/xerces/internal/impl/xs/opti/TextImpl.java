/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * Copyright 2001-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.opti;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class TextImpl extends DefaultText{
    // Data
    String fData=null;
    SchemaDOM fSchemaDOM=null;
    int fRow;
    int fCol;

    public TextImpl(StringBuffer str,SchemaDOM sDOM,int row,int col){
        fData=str.toString();
        fSchemaDOM=sDOM;
        fRow=row;
        fCol=col;
        rawname=prefix=localpart=uri=null;
        nodeType=Node.TEXT_NODE;
    }
    //
    // org.w3c.dom.Node methods
    //

    public Node getParentNode(){
        return fSchemaDOM.relations[fRow][0];
    }

    public Node getPreviousSibling(){
        if(fCol==1){
            return null;
        }
        return fSchemaDOM.relations[fRow][fCol-1];
    }

    public Node getNextSibling(){
        if(fCol==fSchemaDOM.relations[fRow].length-1){
            return null;
        }
        return fSchemaDOM.relations[fRow][fCol+1];
    }
    // CharacterData methods

    public String getData()
            throws DOMException{
        return fData;
    }

    public int getLength(){
        if(fData==null) return 0;
        return fData.length();
    }

    public String substringData(int offset,
                                int count)
            throws DOMException{
        if(fData==null) return null;
        if(count<0||offset<0||offset>fData.length())
            throw new DOMException(DOMException.INDEX_SIZE_ERR,"parameter error");
        if(offset+count>=fData.length())
            return fData.substring(offset);
        return fData.substring(offset,offset+count);
    }
}
