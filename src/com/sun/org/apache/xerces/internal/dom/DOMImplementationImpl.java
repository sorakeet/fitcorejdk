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

import org.w3c.dom.*;

public class DOMImplementationImpl extends CoreDOMImplementationImpl
        implements DOMImplementation{
    //
    // Data
    //
    // static
    static DOMImplementationImpl singleton=new DOMImplementationImpl();
    //
    // Public methods
    //

    public static DOMImplementation getDOMImplementation(){
        return singleton;
    }
    //
    // DOMImplementation methods
    //

    public boolean hasFeature(String feature,String version){
        boolean result=super.hasFeature(feature,version);
        if(!result){
            boolean anyVersion=version==null||version.length()==0;
            if(feature.startsWith("+")){
                feature=feature.substring(1);
            }
            return (
                    (feature.equalsIgnoreCase("Events")
                            &&(anyVersion||version.equals("2.0")))
                            ||(feature.equalsIgnoreCase("MutationEvents")
                            &&(anyVersion||version.equals("2.0")))
                            ||(feature.equalsIgnoreCase("Traversal")
                            &&(anyVersion||version.equals("2.0")))
                            ||(feature.equalsIgnoreCase("Range")
                            &&(anyVersion||version.equals("2.0")))
                            ||(feature.equalsIgnoreCase("MutationEvents")
                            &&(anyVersion||version.equals("2.0"))));
        }
        return result;
    } // hasFeature(String,String):boolean

    public Document createDocument(String namespaceURI,
                                   String qualifiedName,
                                   DocumentType doctype)
            throws DOMException{
        if(namespaceURI==null&&qualifiedName==null&&doctype==null){
            //if namespaceURI, qualifiedName and doctype are null, returned document is empty with
            //no document element
            return new DocumentImpl();
        }else if(doctype!=null&&doctype.getOwnerDocument()!=null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
        }
        DocumentImpl doc=new DocumentImpl(doctype);
        Element e=doc.createElementNS(namespaceURI,qualifiedName);
        doc.appendChild(e);
        return doc;
    }
} // class DOMImplementationImpl
