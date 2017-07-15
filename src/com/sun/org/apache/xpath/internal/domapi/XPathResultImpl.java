/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2005 The Apache Software Foundation.
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
 * <p>
 * $Id: XPathResultImpl.java,v 1.2.4.1 2005/09/10 04:18:54 jeffsuttor Exp $
 */
/**
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: XPathResultImpl.java,v 1.2.4.1 2005/09/10 04:18:54 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.domapi;

import com.sun.org.apache.xpath.internal.XPath;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import com.sun.org.apache.xpath.internal.res.XPATHMessages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathResult;

import javax.xml.transform.TransformerException;

class XPathResultImpl implements XPathResult, EventListener{
    final private XObject m_resultObj;
    final private XPath m_xpath;
    final private short m_resultType;
    final private Node m_contextNode;
    private boolean m_isInvalidIteratorState=false;
    private NodeIterator m_iterator=null;
    ;
    private NodeList m_list=null;

    XPathResultImpl(short type,XObject result,Node contextNode,XPath xpath){
        // Check that the type is valid
        if(!isValidType(type)){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INVALID_XPATH_TYPE,new Object[]{new Integer(type)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg); // Invalid XPath type argument: {0}
        }
        // Result object should never be null!
        if(null==result){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_EMPTY_XPATH_RESULT,null);
            throw new XPathException(XPathException.INVALID_EXPRESSION_ERR,fmsg); // Empty XPath result object
        }
        this.m_resultObj=result;
        this.m_contextNode=contextNode;
        this.m_xpath=xpath;
        // If specified result was ANY_TYPE, determine XObject type
        if(type==ANY_TYPE){
            this.m_resultType=getTypeFromXObject(result);
        }else{
            this.m_resultType=type;
        }
        // If the context node supports DOM Events and the type is one of the iterator
        // types register this result as an event listener
        if(((m_resultType==XPathResult.ORDERED_NODE_ITERATOR_TYPE)||
                (m_resultType==XPathResult.UNORDERED_NODE_ITERATOR_TYPE))){
            addEventListener();
        }// else can we handle iterator types if contextNode doesn't support EventTarget??
        // If this is an iterator type get the iterator
        if((m_resultType==ORDERED_NODE_ITERATOR_TYPE)||
                (m_resultType==UNORDERED_NODE_ITERATOR_TYPE)||
                (m_resultType==ANY_UNORDERED_NODE_TYPE)||
                (m_resultType==FIRST_ORDERED_NODE_TYPE)){
            try{
                m_iterator=m_resultObj.nodeset();
            }catch(TransformerException te){
                // probably not a node type
                String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INCOMPATIBLE_TYPES,new Object[]{m_xpath.getPatternString(),getTypeString(getTypeFromXObject(m_resultObj)),getTypeString(m_resultType)});
                throw new XPathException(XPathException.TYPE_ERR,fmsg);  // "The XPathResult of XPath expression {0} has an XPathResultType of {1} which cannot be coerced into the specified XPathResultType of {2}."},
            }
            // If user requested ordered nodeset and result is unordered
            // need to sort...TODO
            //            if ((m_resultType == ORDERED_NODE_ITERATOR_TYPE) &&
            //                (!(((DTMNodeIterator)m_iterator).getDTMIterator().isDocOrdered()))) {
            //
            //            }
            // If it's a snapshot type, get the nodelist
        }else if((m_resultType==UNORDERED_NODE_SNAPSHOT_TYPE)||
                (m_resultType==ORDERED_NODE_SNAPSHOT_TYPE)){
            try{
                m_list=m_resultObj.nodelist();
            }catch(TransformerException te){
                // probably not a node type
                String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INCOMPATIBLE_TYPES,new Object[]{m_xpath.getPatternString(),getTypeString(getTypeFromXObject(m_resultObj)),getTypeString(m_resultType)});
                throw new XPathException(XPathException.TYPE_ERR,fmsg); // "The XPathResult of XPath expression {0} has an XPathResultType of {1} which cannot be coerced into the specified XPathResultType of {2}."},
            }
        }
    }

    static boolean isValidType(short type){
        switch(type){
            case ANY_TYPE:
            case NUMBER_TYPE:
            case STRING_TYPE:
            case BOOLEAN_TYPE:
            case UNORDERED_NODE_ITERATOR_TYPE:
            case ORDERED_NODE_ITERATOR_TYPE:
            case UNORDERED_NODE_SNAPSHOT_TYPE:
            case ORDERED_NODE_SNAPSHOT_TYPE:
            case ANY_UNORDERED_NODE_TYPE:
            case FIRST_ORDERED_NODE_TYPE:
                return true;
            default:
                return false;
        }
    }    public short getResultType(){
        return m_resultType;
    }

    private String getTypeString(int type){
        switch(type){
            case ANY_TYPE:
                return "ANY_TYPE";
            case ANY_UNORDERED_NODE_TYPE:
                return "ANY_UNORDERED_NODE_TYPE";
            case BOOLEAN_TYPE:
                return "BOOLEAN";
            case FIRST_ORDERED_NODE_TYPE:
                return "FIRST_ORDERED_NODE_TYPE";
            case NUMBER_TYPE:
                return "NUMBER_TYPE";
            case ORDERED_NODE_ITERATOR_TYPE:
                return "ORDERED_NODE_ITERATOR_TYPE";
            case ORDERED_NODE_SNAPSHOT_TYPE:
                return "ORDERED_NODE_SNAPSHOT_TYPE";
            case STRING_TYPE:
                return "STRING_TYPE";
            case UNORDERED_NODE_ITERATOR_TYPE:
                return "UNORDERED_NODE_ITERATOR_TYPE";
            case UNORDERED_NODE_SNAPSHOT_TYPE:
                return "UNORDERED_NODE_SNAPSHOT_TYPE";
            default:
                return "#UNKNOWN";
        }
    }    public double getNumberValue() throws XPathException{
        if(getResultType()!=NUMBER_TYPE){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,new Object[]{m_xpath.getPatternString(),getTypeString(m_resultType)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg);
//              "The XPathResult of XPath expression {0} has an XPathResultType of {1} which cannot be converted to a number"
        }else{
            try{
                return m_resultObj.num();
            }catch(Exception e){
                // Type check above should prevent this exception from occurring.
                throw new XPathException(XPathException.TYPE_ERR,e.getMessage());
            }
        }
    }

    private short getTypeFromXObject(XObject object){
        switch(object.getType()){
            case XObject.CLASS_BOOLEAN:
                return BOOLEAN_TYPE;
            case XObject.CLASS_NODESET:
                return UNORDERED_NODE_ITERATOR_TYPE;
            case XObject.CLASS_NUMBER:
                return NUMBER_TYPE;
            case XObject.CLASS_STRING:
                return STRING_TYPE;
            // XPath 2.0 types
//          case XObject.CLASS_DATE:
//          case XObject.CLASS_DATETIME:
//          case XObject.CLASS_DTDURATION:
//          case XObject.CLASS_GDAY:
//          case XObject.CLASS_GMONTH:
//          case XObject.CLASS_GMONTHDAY:
//          case XObject.CLASS_GYEAR:
//          case XObject.CLASS_GYEARMONTH:
//          case XObject.CLASS_TIME:
//          case XObject.CLASS_YMDURATION: return STRING_TYPE; // treat all date types as strings?
            case XObject.CLASS_RTREEFRAG:
                return UNORDERED_NODE_ITERATOR_TYPE;
            case XObject.CLASS_NULL:
                return ANY_TYPE; // throw exception ?
            default:
                return ANY_TYPE; // throw exception ?
        }
    }    public String getStringValue() throws XPathException{
        if(getResultType()!=STRING_TYPE){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_TO_STRING,new Object[]{m_xpath.getPatternString(),m_resultObj.getTypeString()});
            throw new XPathException(XPathException.TYPE_ERR,fmsg);
//              "The XPathResult of XPath expression {0} has an XPathResultType of {1} which cannot be converted to a string."
        }else{
            try{
                return m_resultObj.str();
            }catch(Exception e){
                // Type check above should prevent this exception from occurring.
                throw new XPathException(XPathException.TYPE_ERR,e.getMessage());
            }
        }
    }

    private void addEventListener(){
        if(m_contextNode instanceof EventTarget)
            ((EventTarget)m_contextNode).addEventListener("DOMSubtreeModified",this,true);
    }    public boolean getBooleanValue() throws XPathException{
        if(getResultType()!=BOOLEAN_TYPE){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_TO_BOOLEAN,new Object[]{m_xpath.getPatternString(),getTypeString(m_resultType)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg);
//              "The XPathResult of XPath expression {0} has an XPathResultType of {1} which cannot be converted to a boolean."
        }else{
            try{
                return m_resultObj.bool();
            }catch(TransformerException e){
                // Type check above should prevent this exception from occurring.
                throw new XPathException(XPathException.TYPE_ERR,e.getMessage());
            }
        }
    }

    public void handleEvent(Event event){
        if(event.getType().equals("DOMSubtreeModified")){
            // invalidate the iterator
            m_isInvalidIteratorState=true;
            // deregister as a listener to reduce computational load
            removeEventListener();
        }
    }    public Node getSingleNodeValue() throws XPathException{
        if((m_resultType!=ANY_UNORDERED_NODE_TYPE)&&
                (m_resultType!=FIRST_ORDERED_NODE_TYPE)){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_CONVERT_TO_SINGLENODE,new Object[]{m_xpath.getPatternString(),getTypeString(m_resultType)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg);
//                              "The XPathResult of XPath expression {0} has an XPathResultType of {1} which cannot be converted to a single node.
//                               This method applies only to types ANY_UNORDERED_NODE_TYPE and FIRST_ORDERED_NODE_TYPE."
        }
        NodeIterator result=null;
        try{
            result=m_resultObj.nodeset();
        }catch(TransformerException te){
            throw new XPathException(XPathException.TYPE_ERR,te.getMessage());
        }
        if(null==result) return null;
        Node node=result.nextNode();
        // Wrap "namespace node" in an XPathNamespace
        if(isNamespaceNode(node)){
            return new XPathNamespaceImpl(node);
        }else{
            return node;
        }
    }

    public boolean getInvalidIteratorState(){
        return m_isInvalidIteratorState;
    }

    public int getSnapshotLength() throws XPathException{
        if((m_resultType!=UNORDERED_NODE_SNAPSHOT_TYPE)&&
                (m_resultType!=ORDERED_NODE_SNAPSHOT_TYPE)){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANT_GET_SNAPSHOT_LENGTH,new Object[]{m_xpath.getPatternString(),getTypeString(m_resultType)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg);
//                              "The method getSnapshotLength cannot be called on the XPathResult of XPath expression {0} because its XPathResultType is {1}.
        }
        return m_list.getLength();
    }

    public Node iterateNext() throws XPathException, DOMException{
        if((m_resultType!=UNORDERED_NODE_ITERATOR_TYPE)&&
                (m_resultType!=ORDERED_NODE_ITERATOR_TYPE)){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NON_ITERATOR_TYPE,new Object[]{m_xpath.getPatternString(),getTypeString(m_resultType)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg);
//                "The method iterateNext cannot be called on the XPathResult of XPath expression {0} because its XPathResultType is {1}.
//                This method applies only to types UNORDERED_NODE_ITERATOR_TYPE and ORDERED_NODE_ITERATOR_TYPE."},
        }
        if(getInvalidIteratorState()){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_DOC_MUTATED,null);
            throw new DOMException(DOMException.INVALID_STATE_ERR,fmsg);  // Document mutated since result was returned. Iterator is invalid.
        }
        Node node=m_iterator.nextNode();
        if(null==node)
            removeEventListener(); // JIRA 1673
        // Wrap "namespace node" in an XPathNamespace
        if(isNamespaceNode(node)){
            return new XPathNamespaceImpl(node);
        }else{
            return node;
        }
    }

    public Node snapshotItem(int index) throws XPathException{
        if((m_resultType!=UNORDERED_NODE_SNAPSHOT_TYPE)&&
                (m_resultType!=ORDERED_NODE_SNAPSHOT_TYPE)){
            String fmsg=XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NON_SNAPSHOT_TYPE,new Object[]{m_xpath.getPatternString(),getTypeString(m_resultType)});
            throw new XPathException(XPathException.TYPE_ERR,fmsg);
//              "The method snapshotItem cannot be called on the XPathResult of XPath expression {0} because its XPathResultType is {1}.
//              This method applies only to types UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE."},
        }
        Node node=m_list.item(index);
        // Wrap "namespace node" in an XPathNamespace
        if(isNamespaceNode(node)){
            return new XPathNamespaceImpl(node);
        }else{
            return node;
        }
    }









    private boolean isNamespaceNode(Node node){
        if((null!=node)&&
                (node.getNodeType()==Node.ATTRIBUTE_NODE)&&
                (node.getNodeName().startsWith("xmlns:")||node.getNodeName().equals("xmlns"))){
            return true;
        }else{
            return false;
        }
    }



    private void removeEventListener(){
        if(m_contextNode instanceof EventTarget)
            ((EventTarget)m_contextNode).removeEventListener("DOMSubtreeModified",this,true);
    }
}
