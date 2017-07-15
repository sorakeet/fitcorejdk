/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: DOMHelper.java,v 1.2.4.1 2005/09/15 08:15:40 suresh_emailid Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * $Id: DOMHelper.java,v 1.2.4.1 2005/09/15 08:15:40 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DOMHelper{
    protected static final NSInfo m_NSInfoUnProcWithXMLNS=new NSInfo(false,
            true);
    protected static final NSInfo m_NSInfoUnProcWithoutXMLNS=new NSInfo(false,
            false);
    protected static final NSInfo m_NSInfoUnProcNoAncestorXMLNS=
            new NSInfo(false,false,NSInfo.ANCESTORNOXMLNS);
    protected static final NSInfo m_NSInfoNullWithXMLNS=new NSInfo(true,
            true);
    protected static final NSInfo m_NSInfoNullWithoutXMLNS=new NSInfo(true,
            false);
    protected static final NSInfo m_NSInfoNullNoAncestorXMLNS=
            new NSInfo(true,false,NSInfo.ANCESTORNOXMLNS);
    protected Vector m_candidateNoAncestorXMLNS=new Vector();
    //==========================================================
    // SECTION: Namespace resolution
    //==========================================================
    protected Document m_DOMFactory=null;
    Map<Node,NSInfo> m_NSInfos=new HashMap<>();

    public static boolean isNodeAfter(Node node1,Node node2){
        if(node1==node2||isNodeTheSame(node1,node2))
            return true;
        // Default return value, if there is no defined ordering
        boolean isNodeAfter=true;
        Node parent1=getParentOfNode(node1);
        Node parent2=getParentOfNode(node2);
        // Optimize for most common case
        if(parent1==parent2||isNodeTheSame(parent1,parent2))  // then we know they are siblings
        {
            if(null!=parent1)
                isNodeAfter=isNodeAfterSibling(parent1,node1,node2);
            else{
                // If both parents are null, ordering is not defined.
                // We're returning a value in lieu of throwing an exception.
                // Not a case we expect to arise in XPath, but beware if you
                // try to reuse this method.
                // We can just fall through in this case, which allows us
                // to hit the debugging code at the end of the function.
                //return isNodeAfter;
            }
        }else{
            // General strategy: Figure out the lengths of the two
            // ancestor chains, reconcile the lengths, and look for
            // the lowest common ancestor. If that ancestor is one of
            // the nodes being compared, it comes before the other.
            // Otherwise perform a sibling compare.
            //
            // NOTE: If no common ancestor is found, ordering is undefined
            // and we return the default value of isNodeAfter.
            // Count parents in each ancestor chain
            int nParents1=2, nParents2=2;  // include node & parent obtained above
            while(parent1!=null){
                nParents1++;
                parent1=getParentOfNode(parent1);
            }
            while(parent2!=null){
                nParents2++;
                parent2=getParentOfNode(parent2);
            }
            // Initially assume scan for common ancestor starts with
            // the input nodes.
            Node startNode1=node1, startNode2=node2;
            // If one ancestor chain is longer, adjust its start point
            // so we're comparing at the same depths
            if(nParents1<nParents2){
                // Adjust startNode2 to depth of startNode1
                int adjust=nParents2-nParents1;
                for(int i=0;i<adjust;i++){
                    startNode2=getParentOfNode(startNode2);
                }
            }else if(nParents1>nParents2){
                // adjust startNode1 to depth of startNode2
                int adjust=nParents1-nParents2;
                for(int i=0;i<adjust;i++){
                    startNode1=getParentOfNode(startNode1);
                }
            }
            Node prevChild1=null, prevChild2=null;  // so we can "back up"
            // Loop up the ancestor chain looking for common parent
            while(null!=startNode1){
                if(startNode1==startNode2||isNodeTheSame(startNode1,startNode2))  // common parent?
                {
                    if(null==prevChild1)  // first time in loop?
                    {
                        // Edge condition: one is the ancestor of the other.
                        isNodeAfter=(nParents1<nParents2)?true:false;
                        break;  // from while loop
                    }else{
                        // Compare ancestors below lowest-common as siblings
                        isNodeAfter=isNodeAfterSibling(startNode1,prevChild1,
                                prevChild2);
                        break;  // from while loop
                    }
                }  // end if(startNode1 == startNode2)
                // Move up one level and try again
                prevChild1=startNode1;
                startNode1=getParentOfNode(startNode1);
                prevChild2=startNode2;
                startNode2=getParentOfNode(startNode2);
            }  // end while(parents exist to examine)
        }  // end big else (not immediate siblings)
        // WARNING: The following diagnostic won't report the early
        // "same node" case. Fix if/when needed.
        /** -- please do not remove... very useful for diagnostics --
         System.out.println("node1 = "+node1.getNodeName()+"("+node1.getNodeType()+")"+
         ", node2 = "+node2.getNodeName()
         +"("+node2.getNodeType()+")"+
         ", isNodeAfter = "+isNodeAfter); */
        return isNodeAfter;
    }  // end isNodeAfter(Node node1, Node node2)

    public static boolean isNodeTheSame(Node node1,Node node2){
        if(node1 instanceof DTMNodeProxy&&node2 instanceof DTMNodeProxy)
            return ((DTMNodeProxy)node1).equals((DTMNodeProxy)node2);
        else
            return (node1==node2);
    }

    private static boolean isNodeAfterSibling(Node parent,Node child1,
                                              Node child2){
        boolean isNodeAfterSibling=false;
        short child1type=child1.getNodeType();
        short child2type=child2.getNodeType();
        if((Node.ATTRIBUTE_NODE!=child1type)
                &&(Node.ATTRIBUTE_NODE==child2type)){
            // always sort attributes before non-attributes.
            isNodeAfterSibling=false;
        }else if((Node.ATTRIBUTE_NODE==child1type)
                &&(Node.ATTRIBUTE_NODE!=child2type)){
            // always sort attributes before non-attributes.
            isNodeAfterSibling=true;
        }else if(Node.ATTRIBUTE_NODE==child1type){
            NamedNodeMap children=parent.getAttributes();
            int nNodes=children.getLength();
            boolean found1=false, found2=false;
            // Count from the start until we find one or the other.
            for(int i=0;i<nNodes;i++){
                Node child=children.item(i);
                if(child1==child||isNodeTheSame(child1,child)){
                    if(found2){
                        isNodeAfterSibling=false;
                        break;
                    }
                    found1=true;
                }else if(child2==child||isNodeTheSame(child2,child)){
                    if(found1){
                        isNodeAfterSibling=true;
                        break;
                    }
                    found2=true;
                }
            }
        }else{
            // TODO: Check performance of alternate solution:
            // There are two choices here: Count from the start of
            // the document until we find one or the other, or count
            // from one until we find or fail to find the other.
            // Either can wind up scanning all the siblings in the worst
            // case, which on a wide document can be a lot of work but
            // is more typically is a short list.
            // Scanning from the start involves two tests per iteration,
            // but it isn't clear that scanning from the middle doesn't
            // yield more iterations on average.
            // We should run some testcases.
            Node child=parent.getFirstChild();
            boolean found1=false, found2=false;
            while(null!=child){
                // Node child = children.item(i);
                if(child1==child||isNodeTheSame(child1,child)){
                    if(found2){
                        isNodeAfterSibling=false;
                        break;
                    }
                    found1=true;
                }else if(child2==child||isNodeTheSame(child2,child)){
                    if(found1){
                        isNodeAfterSibling=true;
                        break;
                    }
                    found2=true;
                }
                child=child.getNextSibling();
            }
        }
        return isNodeAfterSibling;
    }  // end isNodeAfterSibling(Node parent, Node child1, Node child2)

    public static Node getParentOfNode(Node node) throws RuntimeException{
        Node parent;
        short nodeType=node.getNodeType();
        if(Node.ATTRIBUTE_NODE==nodeType){
            Document doc=node.getOwnerDocument();
            /**
             TBD:
             if(null == doc)
             {
             throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CHILD_HAS_NO_OWNER_DOCUMENT, null));//"Attribute child does not have an owner document!");
             }
             */
            // Given how expensive the tree walk may be, we should first ask
            // whether this DOM can answer the question for us. The additional
            // test does slow down Level 1 DOMs slightly. DOMHelper2, which
            // is currently specialized for Xerces, assumes it can use the
            // Level 2 solution. We might want to have an intermediate stage,
            // which would assume DOM Level 2 but not assume Xerces.
            //
            // (Shouldn't have to check whether impl is null in a compliant DOM,
            // but let's be paranoid for a moment...)
            DOMImplementation impl=doc.getImplementation();
            if(impl!=null&&impl.hasFeature("Core","2.0")){
                parent=((Attr)node).getOwnerElement();
                return parent;
            }
            // DOM Level 1 solution, as fallback. Hugely expensive.
            Element rootElem=doc.getDocumentElement();
            if(null==rootElem){
                throw new RuntimeException(
                        XMLMessages.createXMLMessage(
                                XMLErrorResources.ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
                                null));  //"Attribute child does not have an owner document element!");
            }
            parent=locateAttrParent(rootElem,node);
        }else{
            parent=node.getParentNode();
            // if((Node.DOCUMENT_NODE != nodeType) && (null == parent))
            // {
            //   throw new RuntimeException("Child does not have parent!");
            // }
        }
        return parent;
    }

    private static Node locateAttrParent(Element elem,Node attr){
        Node parent=null;
        // This should only be called for Level 1 DOMs, so we don't have to
        // worry about namespace issues. In later levels, it's possible
        // for a DOM to have two Attrs with the same NodeName but
        // different namespaces, and we'd need to get getAttributeNodeNS...
        // but later levels also have Attr.getOwnerElement.
        Attr check=elem.getAttributeNode(attr.getNodeName());
        if(check==attr)
            parent=elem;
        if(null==parent){
            for(Node node=elem.getFirstChild();null!=node;
                node=node.getNextSibling()){
                if(Node.ELEMENT_NODE==node.getNodeType()){
                    parent=locateAttrParent((Element)node,attr);
                    if(null!=parent)
                        break;
                }
            }
        }
        return parent;
    }

    public static String getNodeData(Node node){
        FastStringBuffer buf=StringBufferPool.get();
        String s;
        try{
            getNodeData(node,buf);
            s=(buf.length()>0)?buf.toString():"";
        }finally{
            StringBufferPool.free(buf);
        }
        return s;
    }

    public static void getNodeData(Node node,FastStringBuffer buf){
        switch(node.getNodeType()){
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_NODE:
            case Node.ELEMENT_NODE:{
                for(Node child=node.getFirstChild();null!=child;
                    child=child.getNextSibling()){
                    getNodeData(child,buf);
                }
            }
            break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                buf.append(node.getNodeValue());
                break;
            case Node.ATTRIBUTE_NODE:
                buf.append(node.getNodeValue());
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                // warning(XPATHErrorResources.WG_PARSING_AND_PREPARING);
                break;
            default:
                // ignore
                break;
        }
    }

    public boolean shouldStripSourceNode(Node textNode)
            throws javax.xml.transform.TransformerException{
        // return (null == m_envSupport) ? false : m_envSupport.shouldStripSourceNode(textNode);
        return false;
    }

    public String getUniqueID(Node node){
        return "N"+Integer.toHexString(node.hashCode()).toUpperCase();
    }

    public short getLevel(Node n){
        short level=1;
        while(null!=(n=getParentOfNode(n))){
            level++;
        }
        return level;
    }

    public String getNamespaceForPrefix(String prefix,Element namespaceContext){
        int type;
        Node parent=namespaceContext;
        String namespace=null;
        if(prefix.equals("xml")){
            namespace=QName.S_XMLNAMESPACEURI; // Hardcoded, per Namespace spec
        }else if(prefix.equals("xmlns")){
            // Hardcoded in the DOM spec, expected to be adopted by
            // Namespace spec. NOTE: Namespace declarations _must_ use
            // the xmlns: prefix; other prefixes declared as belonging
            // to this namespace will not be recognized and should
            // probably be rejected by parsers as erroneous declarations.
            namespace="http://www.w3.org/2000/xmlns/";
        }else{
            // Attribute name for this prefix's declaration
            String declname=(prefix=="")
                    ?"xmlns"
                    :"xmlns:"+prefix;
            // Scan until we run out of Elements or have resolved the namespace
            while((null!=parent)&&(null==namespace)
                    &&(((type=parent.getNodeType())==Node.ELEMENT_NODE)
                    ||(type==Node.ENTITY_REFERENCE_NODE))){
                if(type==Node.ELEMENT_NODE){
                    // Look for the appropriate Namespace Declaration attribute,
                    // either "xmlns:prefix" or (if prefix is "") "xmlns".
                    // TODO: This does not handle "implicit declarations"
                    // which may be created when the DOM is edited. DOM Level
                    // 3 will define how those should be interpreted. But
                    // this issue won't arise in freshly-parsed DOMs.
                    // NOTE: declname is set earlier, outside the loop.
                    Attr attr=((Element)parent).getAttributeNode(declname);
                    if(attr!=null){
                        namespace=attr.getNodeValue();
                        break;
                    }
                }
                parent=getParentOfNode(parent);
            }
        }
        return namespace;
    }

    public String getNamespaceOfNode(Node n){
        String namespaceOfPrefix;
        boolean hasProcessedNS;
        NSInfo nsInfo;
        short ntype=n.getNodeType();
        if(Node.ATTRIBUTE_NODE!=ntype){
            nsInfo=m_NSInfos.get(n);
            hasProcessedNS=(nsInfo==null)?false:nsInfo.m_hasProcessedNS;
        }else{
            hasProcessedNS=false;
            nsInfo=null;
        }
        if(hasProcessedNS){
            namespaceOfPrefix=nsInfo.m_namespace;
        }else{
            namespaceOfPrefix=null;
            String nodeName=n.getNodeName();
            int indexOfNSSep=nodeName.indexOf(':');
            String prefix;
            if(Node.ATTRIBUTE_NODE==ntype){
                if(indexOfNSSep>0){
                    prefix=nodeName.substring(0,indexOfNSSep);
                }else{
                    // Attributes don't use the default namespace, so if
                    // there isn't a prefix, we're done.
                    return namespaceOfPrefix;
                }
            }else{
                prefix=(indexOfNSSep>=0)
                        ?nodeName.substring(0,indexOfNSSep):"";
            }
            boolean ancestorsHaveXMLNS=false;
            boolean nHasXMLNS=false;
            if(prefix.equals("xml")){
                namespaceOfPrefix=QName.S_XMLNAMESPACEURI;
            }else{
                int parentType;
                Node parent=n;
                while((null!=parent)&&(null==namespaceOfPrefix)){
                    if((null!=nsInfo)
                            &&(nsInfo.m_ancestorHasXMLNSAttrs
                            ==NSInfo.ANCESTORNOXMLNS)){
                        break;
                    }
                    parentType=parent.getNodeType();
                    if((null==nsInfo)||nsInfo.m_hasXMLNSAttrs){
                        boolean elementHasXMLNS=false;
                        if(parentType==Node.ELEMENT_NODE){
                            NamedNodeMap nnm=parent.getAttributes();
                            for(int i=0;i<nnm.getLength();i++){
                                Node attr=nnm.item(i);
                                String aname=attr.getNodeName();
                                if(aname.charAt(0)=='x'){
                                    boolean isPrefix=aname.startsWith("xmlns:");
                                    if(aname.equals("xmlns")||isPrefix){
                                        if(n==parent)
                                            nHasXMLNS=true;
                                        elementHasXMLNS=true;
                                        ancestorsHaveXMLNS=true;
                                        String p=isPrefix?aname.substring(6):"";
                                        if(p.equals(prefix)){
                                            namespaceOfPrefix=attr.getNodeValue();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if((Node.ATTRIBUTE_NODE!=parentType)&&(null==nsInfo)
                                &&(n!=parent)){
                            nsInfo=elementHasXMLNS
                                    ?m_NSInfoUnProcWithXMLNS:m_NSInfoUnProcWithoutXMLNS;
                            m_NSInfos.put(parent,nsInfo);
                        }
                    }
                    if(Node.ATTRIBUTE_NODE==parentType){
                        parent=getParentOfNode(parent);
                    }else{
                        m_candidateNoAncestorXMLNS.addElement(parent);
                        m_candidateNoAncestorXMLNS.addElement(nsInfo);
                        parent=parent.getParentNode();
                    }
                    if(null!=parent){
                        nsInfo=m_NSInfos.get(parent);
                    }
                }
                int nCandidates=m_candidateNoAncestorXMLNS.size();
                if(nCandidates>0){
                    if((false==ancestorsHaveXMLNS)&&(null==parent)){
                        for(int i=0;i<nCandidates;i+=2){
                            Object candidateInfo=m_candidateNoAncestorXMLNS.elementAt(i
                                    +1);
                            if(candidateInfo==m_NSInfoUnProcWithoutXMLNS){
                                m_NSInfos.put((Node)m_candidateNoAncestorXMLNS.elementAt(i),
                                        m_NSInfoUnProcNoAncestorXMLNS);
                            }else if(candidateInfo==m_NSInfoNullWithoutXMLNS){
                                m_NSInfos.put((Node)m_candidateNoAncestorXMLNS.elementAt(i),
                                        m_NSInfoNullNoAncestorXMLNS);
                            }
                        }
                    }
                    m_candidateNoAncestorXMLNS.removeAllElements();
                }
            }
            if(Node.ATTRIBUTE_NODE!=ntype){
                if(null==namespaceOfPrefix){
                    if(ancestorsHaveXMLNS){
                        if(nHasXMLNS)
                            m_NSInfos.put(n,m_NSInfoNullWithXMLNS);
                        else
                            m_NSInfos.put(n,m_NSInfoNullWithoutXMLNS);
                    }else{
                        m_NSInfos.put(n,m_NSInfoNullNoAncestorXMLNS);
                    }
                }else{
                    m_NSInfos.put(n,new NSInfo(namespaceOfPrefix,nHasXMLNS));
                }
            }
        }
        return namespaceOfPrefix;
    }
    //==========================================================
    // SECTION: DOM Helper Functions
    //==========================================================

    public String getLocalNameOfNode(Node n){
        String qname=n.getNodeName();
        int index=qname.indexOf(':');
        return (index<0)?qname:qname.substring(index+1);
    }

    public String getExpandedElementName(Element elem){
        String namespace=getNamespaceOfNode(elem);
        return (null!=namespace)
                ?namespace+":"+getLocalNameOfNode(elem)
                :getLocalNameOfNode(elem);
    }

    public String getExpandedAttributeName(Attr attr){
        String namespace=getNamespaceOfNode(attr);
        return (null!=namespace)
                ?namespace+":"+getLocalNameOfNode(attr)
                :getLocalNameOfNode(attr);
    }

    public boolean isIgnorableWhitespace(Text node){
        boolean isIgnorable=false;  // return value
        // TODO: I can probably do something to figure out if this
        // space is ignorable from just the information in
        // the DOM tree.
        // -- You need to be able to distinguish whitespace
        // that is #PCDATA from whitespace that isn't.  That requires
        // DTD support, which won't be standardized until DOM Level 3.
        return isIgnorable;
    }

    public Node getRoot(Node node){
        Node root=null;
        while(node!=null){
            root=node;
            node=getParentOfNode(node);
        }
        return root;
    }

    public Node getRootNode(Node n){
        int nt=n.getNodeType();
        return ((Node.DOCUMENT_NODE==nt)||(Node.DOCUMENT_FRAGMENT_NODE==nt))
                ?n:n.getOwnerDocument();
    }

    public boolean isNamespaceNode(Node n){
        if(Node.ATTRIBUTE_NODE==n.getNodeType()){
            String attrName=n.getNodeName();
            return (attrName.startsWith("xmlns:")||attrName.equals("xmlns"));
        }
        return false;
    }

    public Element getElementByID(String id,Document doc){
        return null;
    }

    public String getUnparsedEntityURI(String name,Document doc){
        String url="";
        DocumentType doctype=doc.getDoctype();
        if(null!=doctype){
            NamedNodeMap entities=doctype.getEntities();
            if(null==entities)
                return url;
            Entity entity=(Entity)entities.getNamedItem(name);
            if(null==entity)
                return url;
            String notationName=entity.getNotationName();
            if(null!=notationName)  // then it's unparsed
            {
                // The draft says: "The XSLT processor may use the public
                // identifier to generate a URI for the entity instead of the URI
                // specified in the system identifier. If the XSLT processor does
                // not use the public identifier to generate the URI, it must use
                // the system identifier; if the system identifier is a relative
                // URI, it must be resolved into an absolute URI using the URI of
                // the resource containing the entity declaration as the base
                // URI [RFC2396]."
                // So I'm falling a bit short here.
                url=entity.getSystemId();
                if(null==url){
                    url=entity.getPublicId();
                }else{
                    // This should be resolved to an absolute URL, but that's hard
                    // to do from here.
                }
            }
        }
        return url;
    }

    public Document getDOMFactory(){
        if(null==this.m_DOMFactory){
            this.m_DOMFactory=createDocument();
        }
        return this.m_DOMFactory;
    }

    public static Document createDocument(){
        return createDocument(false);
    }

    public static Document createDocument(boolean isSecureProcessing){
        try{
            // Use an implementation of the JAVA API for XML Parsing 1.0 to
            // create a DOM Document node to contain the result.
            DocumentBuilderFactory dfactory=DocumentBuilderFactory.newInstance();
            dfactory.setNamespaceAware(true);
            dfactory.setValidating(true);
            if(isSecureProcessing){
                try{
                    dfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
                }catch(ParserConfigurationException pce){
                }
            }
            DocumentBuilder docBuilder=dfactory.newDocumentBuilder();
            Document outNode=docBuilder.newDocument();
            return outNode;
        }catch(ParserConfigurationException pce){
            throw new RuntimeException(
                    XMLMessages.createXMLMessage(
                            XMLErrorResources.ER_CREATEDOCUMENT_NOT_SUPPORTED,null));  //"createDocument() not supported in XPathContext!");
            // return null;
        }
    }

    public void setDOMFactory(Document domFactory){
        this.m_DOMFactory=domFactory;
    }
}
