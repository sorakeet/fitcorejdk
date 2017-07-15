/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * Copyright 2002-2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PSVIDocumentImpl extends DocumentImpl{
    static final long serialVersionUID=-8822220250676434522L;

    public PSVIDocumentImpl(){
        super();
    }

    public PSVIDocumentImpl(DocumentType doctype){
        super(doctype);
    }

    public Node cloneNode(boolean deep){
        PSVIDocumentImpl newdoc=new PSVIDocumentImpl();
        callUserDataHandlers(this,newdoc,UserDataHandler.NODE_CLONED);
        cloneNode(newdoc,deep);
        // experimental
        newdoc.mutationEvents=mutationEvents;
        return newdoc;
    } // cloneNode(boolean):Node

    public DOMImplementation getImplementation(){
        // Currently implemented as a singleton, since it's hardcoded
        // information anyway.
        return PSVIDOMImplementationImpl.getDOMImplementation();
    }

    public DOMConfiguration getDomConfig(){
        super.getDomConfig();
        return fConfiguration;
    }

    public Element createElementNS(String namespaceURI,String qualifiedName)
            throws DOMException{
        return new PSVIElementNSImpl(this,namespaceURI,qualifiedName);
    }

    public Element createElementNS(String namespaceURI,String qualifiedName,
                                   String localpart) throws DOMException{
        return new PSVIElementNSImpl(this,namespaceURI,qualifiedName,localpart);
    }

    public Attr createAttributeNS(String namespaceURI,String qualifiedName)
            throws DOMException{
        return new PSVIAttrNSImpl(this,namespaceURI,qualifiedName);
    }

    public Attr createAttributeNS(String namespaceURI,String qualifiedName,
                                  String localName) throws DOMException{
        return new PSVIAttrNSImpl(this,namespaceURI,qualifiedName,localName);
    }
    // REVISIT: Forbid serialization of PSVI DOM until
    // we support object serialization of grammars -- mrglavas

    private void writeObject(ObjectOutputStream out)
            throws IOException{
        throw new NotSerializableException(getClass().getName());
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        throw new NotSerializableException(getClass().getName());
    }
} // class PSVIDocumentImpl
