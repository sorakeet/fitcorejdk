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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

public class XSAnnotationImpl implements XSAnnotation{
    // Data
    // the content of the annotation node, including all children, along
    // with any non-schema attributes from its parent
    private String fData=null;
    // the grammar which owns this annotation; we get parsers
    // from here when we need them
    private SchemaGrammar fGrammar=null;

    // constructors
    public XSAnnotationImpl(String contents,SchemaGrammar grammar){
        fData=contents;
        fGrammar=grammar;
    }

    public boolean writeAnnotation(Object target,
                                   short targetType){
        if(targetType==XSAnnotation.W3C_DOM_ELEMENT||targetType==XSAnnotation.W3C_DOM_DOCUMENT){
            writeToDOM((Node)target,targetType);
            return true;
        }else if(targetType==SAX_CONTENTHANDLER){
            writeToSAX((ContentHandler)target);
            return true;
        }
        return false;
    }

    public String getAnnotationString(){
        return fData;
    }
    // XSObject methods

    // private methods
    private synchronized void writeToSAX(ContentHandler handler){
        // nothing must go wrong with this parse...
        SAXParser parser=fGrammar.getSAXParser();
        StringReader aReader=new StringReader(fData);
        InputSource aSource=new InputSource(aReader);
        parser.setContentHandler(handler);
        try{
            parser.parse(aSource);
        }catch(SAXException e){
            // this should never happen!
            // REVISIT:  what to do with this?; should really not
            // eat it...
        }catch(IOException i){
            // ditto with above
        }
        // Release the reference to the user's ContentHandler.
        parser.setContentHandler(null);
    }

    // this creates the new Annotation element as the first child
    // of the Node
    private synchronized void writeToDOM(Node target,short type){
        Document futureOwner=(type==XSAnnotation.W3C_DOM_ELEMENT)?
                target.getOwnerDocument():(Document)target;
        DOMParser parser=fGrammar.getDOMParser();
        StringReader aReader=new StringReader(fData);
        InputSource aSource=new InputSource(aReader);
        try{
            parser.parse(aSource);
        }catch(SAXException e){
            // this should never happen!
            // REVISIT:  what to do with this?; should really not
            // eat it...
        }catch(IOException i){
            // ditto with above
        }
        Document aDocument=parser.getDocument();
        parser.dropDocumentReferences();
        Element annotation=aDocument.getDocumentElement();
        Node newElem=null;
        if(futureOwner instanceof CoreDocumentImpl){
            newElem=futureOwner.adoptNode(annotation);
            // adoptNode will return null when the DOM implementations are not compatible.
            if(newElem==null){
                newElem=futureOwner.importNode(annotation,true);
            }
        }else{
            newElem=futureOwner.importNode(annotation,true);
        }
        target.insertBefore(newElem,target.getFirstChild());
    }

    public short getType(){
        return XSConstants.ANNOTATION;
    }

    public String getName(){
        return null;
    }

    public String getNamespace(){
        return null;
    }

    public XSNamespaceItem getNamespaceItem(){
        return null;
    }
}
