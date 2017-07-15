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
// Sep 14, 2000:
//  Fixed serializer to report IO exception directly, instead at
//  the end of document processing.
//  Reported by Patrick Higgins <phiggins@transzap.com>
package com.sun.org.apache.xml.internal.serialize;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;

public class TextSerializer
        extends BaseMarkupSerializer{
    public TextSerializer(){
        super(new OutputFormat(Method.TEXT,null,false));
    }

    public void setOutputFormat(OutputFormat format){
        super.setOutputFormat(format!=null?format:new OutputFormat(Method.TEXT,null,false));
    }
    //-----------------------------------------//
    // SAX content handler serializing methods //
    //-----------------------------------------//

    public void characters(char[] chars,int start,int length)
            throws SAXException{
        ElementState state;
        try{
            state=content();
            state.doCData=state.inCData=false;
            printText(chars,start,length,true,true);
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void processingInstructionIO(String target,String code) throws IOException{
    }
    //------------------------------------------//
    // SAX document handler serializing methods //
    //------------------------------000---------//

    public void comment(char[] chars,int start,int length){
    }

    public void comment(String text){
    }

    protected void serializeNode(Node node)
            throws IOException{
        // Based on the node type call the suitable SAX handler.
        // Only comments entities and documents which are not
        // handled by SAX are serialized directly.
        switch(node.getNodeType()){
            case Node.TEXT_NODE:{
                String text;
                text=node.getNodeValue();
                if(text!=null)
                    characters(node.getNodeValue(),true);
                break;
            }
            case Node.CDATA_SECTION_NODE:{
                String text;
                text=node.getNodeValue();
                if(text!=null)
                    characters(node.getNodeValue(),true);
                break;
            }
            case Node.COMMENT_NODE:
                break;
            case Node.ENTITY_REFERENCE_NODE:
                // Ignore.
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                break;
            case Node.ELEMENT_NODE:
                serializeElement((Element)node);
                break;
            case Node.DOCUMENT_NODE:
                // !!! Fall through
            case Node.DOCUMENT_FRAGMENT_NODE:{
                Node child;
                // By definition this will happen if the node is a document,
                // document fragment, etc. Just serialize its contents. It will
                // work well for other nodes that we do not know how to serialize.
                child=node.getFirstChild();
                while(child!=null){
                    serializeNode(child);
                    child=child.getNextSibling();
                }
                break;
            }
            default:
                break;
        }
    }

    protected ElementState content(){
        ElementState state;
        state=getElementState();
        if(!isDocumentState()){
            // If this is the first content in the element,
            // change the state to not-empty.
            if(state.empty)
                state.empty=false;
            // Except for one content type, all of them
            // are not last element. That one content
            // type will take care of itself.
            state.afterElement=false;
        }
        return state;
    }

    protected String getEntityRef(int ch){
        return null;
    }

    protected void serializeElement(Element elem)
            throws IOException{
        Node child;
        ElementState state;
        boolean preserveSpace;
        String tagName;
        tagName=elem.getTagName();
        state=getElementState();
        if(isDocumentState()){
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.
            if(!_started)
                startDocument(tagName);
        }
        // For any other element, if first in parent, then
        // use the parnet's space preserving.
        preserveSpace=state.preserveSpace;
        // Do not change the current element state yet.
        // This only happens in endElement().
        // Ignore all other attributes of the element, only printing
        // its contents.
        // If element has children, then serialize them, otherwise
        // serialize en empty tag.
        if(elem.hasChildNodes()){
            // Enter an element state, and serialize the children
            // one by one. Finally, end the element.
            state=enterElementState(null,null,tagName,preserveSpace);
            child=elem.getFirstChild();
            while(child!=null){
                serializeNode(child);
                child=child.getNextSibling();
            }
            endElementIO(tagName);
        }else{
            if(!isDocumentState()){
                // After element but parent element is no longer empty.
                state.afterElement=true;
                state.empty=false;
            }
        }
    }

    public void startElement(String namespaceURI,String localName,
                             String rawName,Attributes attrs)
            throws SAXException{
        startElement(rawName==null?localName:rawName,null);
    }

    public void endElement(String namespaceURI,String localName,
                           String rawName)
            throws SAXException{
        endElement(rawName==null?localName:rawName);
    }
    //------------------------------------------//
    // Generic node serializing methods methods //
    //------------------------------------------//

    public void startElement(String tagName,AttributeList attrs)
            throws SAXException{
        boolean preserveSpace;
        ElementState state;
        try{
            state=getElementState();
            if(isDocumentState()){
                // If this is the root element handle it differently.
                // If the first root element in the document, serialize
                // the document's DOCTYPE. Space preserving defaults
                // to that of the output format.
                if(!_started)
                    startDocument(tagName);
            }
            // For any other element, if first in parent, then
            // use the parnet's space preserving.
            preserveSpace=state.preserveSpace;
            // Do not change the current element state yet.
            // This only happens in endElement().
            // Ignore all other attributes of the element, only printing
            // its contents.
            // Now it's time to enter a new element state
            // with the tag name and space preserving.
            // We still do not change the curent element state.
            state=enterElementState(null,null,tagName,preserveSpace);
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void endElement(String tagName)
            throws SAXException{
        try{
            endElementIO(tagName);
        }catch(IOException except){
            throw new SAXException(except);
        }
    }

    public void endElementIO(String tagName)
            throws IOException{
        ElementState state;
        // Works much like content() with additions for closing
        // an element. Note the different checks for the closed
        // element's state and the parent element's state.
        state=getElementState();
        // Leave the element state and update that of the parent
        // (if we're not root) to not empty and after element.
        state=leaveElementState();
        state.afterElement=true;
        state.empty=false;
        if(isDocumentState())
            _printer.flush();
    }

    protected void startDocument(String rootTagName)
            throws IOException{
        // Required to stop processing the DTD, even though the DTD
        // is not printed.
        _printer.leaveDTD();
        _started=true;
        // Always serialize these, even if not te first root element.
        serializePreRoot();
    }

    protected void characters(String text,boolean unescaped)
            throws IOException{
        ElementState state;
        state=content();
        state.doCData=state.inCData=false;
        printText(text,true,true);
    }
}
