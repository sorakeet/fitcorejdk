/**
 * Copyright (c) 2009, 2015, Oracle and/or its affiliates. All rights reserved.
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
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.org.apache.xerces.internal.util.XML11Char;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import org.w3c.dom.*;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class CoreDocumentImpl
        extends ParentNode implements Document{
    //
    // Constants
    //
    static final long serialVersionUID=0;
    private final static int[] kidOK;
    private static final ObjectStreamField[] serialPersistentFields=
            new ObjectStreamField[]{
                    new ObjectStreamField("docType",DocumentTypeImpl.class),
                    new ObjectStreamField("docElement",ElementImpl.class),
                    new ObjectStreamField("fFreeNLCache",NodeListCache.class),
                    new ObjectStreamField("encoding",String.class),
                    new ObjectStreamField("actualEncoding",String.class),
                    new ObjectStreamField("version",String.class),
                    new ObjectStreamField("standalone",boolean.class),
                    new ObjectStreamField("fDocumentURI",String.class),
                    new ObjectStreamField("userData",Hashtable.class),
                    new ObjectStreamField("identifiers",Hashtable.class),
                    new ObjectStreamField("changes",int.class),
                    new ObjectStreamField("allowGrammarAccess",boolean.class),
                    new ObjectStreamField("errorChecking",boolean.class),
                    new ObjectStreamField("ancestorChecking",boolean.class),
                    new ObjectStreamField("xmlVersionChanged",boolean.class),
                    new ObjectStreamField("documentNumber",int.class),
                    new ObjectStreamField("nodeCounter",int.class),
                    new ObjectStreamField("nodeTable",Hashtable.class),
                    new ObjectStreamField("xml11Version",boolean.class),
            };

    static{
        kidOK=new int[13];
        kidOK[DOCUMENT_NODE]=
                1<<ELEMENT_NODE|1<<PROCESSING_INSTRUCTION_NODE|
                        1<<COMMENT_NODE|1<<DOCUMENT_TYPE_NODE;
        kidOK[DOCUMENT_FRAGMENT_NODE]=
                kidOK[ENTITY_NODE]=
                        kidOK[ENTITY_REFERENCE_NODE]=
                                kidOK[ELEMENT_NODE]=
                                        1<<ELEMENT_NODE|1<<PROCESSING_INSTRUCTION_NODE|
                                                1<<COMMENT_NODE|1<<TEXT_NODE|
                                                1<<CDATA_SECTION_NODE|1<<ENTITY_REFERENCE_NODE;
        kidOK[ATTRIBUTE_NODE]=
                1<<TEXT_NODE|1<<ENTITY_REFERENCE_NODE;
        kidOK[DOCUMENT_TYPE_NODE]=
                kidOK[PROCESSING_INSTRUCTION_NODE]=
                        kidOK[COMMENT_NODE]=
                                kidOK[TEXT_NODE]=
                                        kidOK[CDATA_SECTION_NODE]=
                                                kidOK[NOTATION_NODE]=
                                                        0;
    } // static

    //
    // Data
    //
    // document information
    protected DocumentTypeImpl docType;
    protected ElementImpl docElement;
    protected String encoding;
    protected String actualEncoding;
    protected String version;
    protected boolean standalone;
    protected String fDocumentURI;
    protected Map<String,Node> identifiers;
    protected int changes=0;
    // experimental
    protected boolean allowGrammarAccess;
    protected boolean errorChecking=true;
    protected boolean ancestorChecking=true;
    //Did version change at any point when the document was created ?
    //this field helps us to optimize when normalizingDocument.
    protected boolean xmlVersionChanged=false;
    transient NodeListCache fFreeNLCache;
    // DOM Level 3: normalizeDocument
    transient DOMNormalizer domNormalizer=null;
    transient DOMConfigurationImpl fConfiguration=null;
    // support of XPath API
    transient Object fXPathEvaluator=null;
    //Revisit :: change to a better data structure.
    private Map<Node,Map<String,UserDataRecord>> nodeUserData;
    // Document number.   Documents are ordered across the implementation using
    // positive integer values.  Documents are assigned numbers on demand.
    private int documentNumber=0;
    // Node counter and table.  Used to assign numbers to nodes for this
    // document.  Node number values are negative integers.  Nodes are
    // assigned numbers on demand.
    private int nodeCounter=0;
    //
    // Static initialization
    //
    private Map<Node,Integer> nodeTable;
    private boolean xml11Version=false; //by default 1.0
    //
    // Constructors
    //

    public CoreDocumentImpl(){
        this(false);
    }

    public CoreDocumentImpl(boolean grammarAccess){
        super(null);
        ownerDocument=this;
        allowGrammarAccess=grammarAccess;
        String systemProp=SecuritySupport.getSystemProperty(Constants.SUN_DOM_PROPERTY_PREFIX+Constants.SUN_DOM_ANCESTOR_CHECCK);
        if(systemProp!=null){
            if(systemProp.equalsIgnoreCase("false")){
                ancestorChecking=false;
            }
        }
    }

    public CoreDocumentImpl(DocumentType doctype){
        this(doctype,false);
    }

    public CoreDocumentImpl(DocumentType doctype,boolean grammarAccess){
        this(grammarAccess);
        if(doctype!=null){
            DocumentTypeImpl doctypeImpl;
            try{
                doctypeImpl=(DocumentTypeImpl)doctype;
            }catch(ClassCastException e){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
            doctypeImpl.ownerDocument=this;
            appendChild(doctype);
        }
    }
    //
    // Node methods
    //

    public static final boolean isValidQName(String prefix,String local,boolean xml11Version){
        // check that both prefix and local part match NCName
        if(local==null) return false;
        boolean validNCName=false;
        if(!xml11Version){
            validNCName=(prefix==null||XMLChar.isValidNCName(prefix))
                    &&XMLChar.isValidNCName(local);
        }else{
            validNCName=(prefix==null||XML11Char.isXML11ValidNCName(prefix))
                    &&XML11Char.isXML11ValidNCName(local);
        }
        return validNCName;
    }

    public short getNodeType(){
        return Node.DOCUMENT_NODE;
    }

    public String getNodeName(){
        return "#document";
    }

    // other non-DOM methods
    protected int getNodeNumber(){
        if(documentNumber==0){
            CoreDOMImplementationImpl cd=(CoreDOMImplementationImpl)CoreDOMImplementationImpl.getDOMImplementation();
            documentNumber=cd.assignDocumentNumber();
        }
        return documentNumber;
    }

    public String getBaseURI(){
        if(fDocumentURI!=null&&fDocumentURI.length()!=0){// attribute value is always empty string
            try{
                return new URI(fDocumentURI).toString();
            }catch(URI.MalformedURIException e){
                // REVISIT: what should happen in this case?
                return null;
            }
        }
        return fDocumentURI;
    }

    public Object getFeature(String feature,String version){
        boolean anyVersion=version==null||version.length()==0;
        // if a plus sign "+" is prepended to any feature name, implementations
        // are considered in which the specified feature may not be directly
        // castable DOMImplementation.getFeature(feature, version). Without a
        // plus, only features whose interfaces are directly castable are
        // considered.
        if((feature.equalsIgnoreCase("+XPath"))
                &&(anyVersion||version.equals("3.0"))){
            // If an XPathEvaluator was created previously
            // return it otherwise create a new one.
            if(fXPathEvaluator!=null){
                return fXPathEvaluator;
            }
            try{
                Class xpathClass=ObjectFactory.findProviderClass(
                        "com.sun.org.apache.xpath.internal.domapi.XPathEvaluatorImpl",true);
                Constructor xpathClassConstr=
                        xpathClass.getConstructor(new Class[]{Document.class});
                // Check if the DOM XPath implementation implements
                // the interface org.w3c.dom.XPathEvaluator
                Class interfaces[]=xpathClass.getInterfaces();
                for(int i=0;i<interfaces.length;i++){
                    if(interfaces[i].getName().equals(
                            "org.w3c.dom.xpath.XPathEvaluator")){
                        fXPathEvaluator=xpathClassConstr.newInstance(new Object[]{this});
                        return fXPathEvaluator;
                    }
                }
                return null;
            }catch(Exception e){
                return null;
            }
        }
        return super.getFeature(feature,version);
    }

    protected void changed(){
        changes++;
    }

    protected int changes(){
        return changes;
    }

    public Node cloneNode(boolean deep){
        CoreDocumentImpl newdoc=new CoreDocumentImpl();
        callUserDataHandlers(this,newdoc,UserDataHandler.NODE_CLONED);
        cloneNode(newdoc,deep);
        return newdoc;
    } // cloneNode(boolean):Node

    // even though ownerDocument refers to this in this implementation
    // the DOM Level 2 spec says it must be null, so make it appear so
    final public Document getOwnerDocument(){
        return null;
    }

    public Node insertBefore(Node newChild,Node refChild)
            throws DOMException{
        // Only one such child permitted
        int type=newChild.getNodeType();
        if(errorChecking){
            if((type==Node.ELEMENT_NODE&&docElement!=null)||
                    (type==Node.DOCUMENT_TYPE_NODE&&docType!=null)){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null);
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,msg);
            }
        }
        // Adopt orphan doctypes
        if(newChild.getOwnerDocument()==null&&
                newChild instanceof DocumentTypeImpl){
            ((DocumentTypeImpl)newChild).ownerDocument=this;
        }
        super.insertBefore(newChild,refChild);
        // If insert succeeded, cache the kid appropriately
        if(type==Node.ELEMENT_NODE){
            docElement=(ElementImpl)newChild;
        }else if(type==Node.DOCUMENT_TYPE_NODE){
            docType=(DocumentTypeImpl)newChild;
        }
        return newChild;
    } // insertBefore(Node,Node):Node
    //
    // Document methods
    //
    // factory methods

    public Node removeChild(Node oldChild) throws DOMException{
        super.removeChild(oldChild);
        // If remove succeeded, un-cache the kid appropriately
        int type=oldChild.getNodeType();
        if(type==Node.ELEMENT_NODE){
            docElement=null;
        }else if(type==Node.DOCUMENT_TYPE_NODE){
            docType=null;
        }
        return oldChild;
    }   // removeChild(Node):Node

    public Node replaceChild(Node newChild,Node oldChild)
            throws DOMException{
        // Adopt orphan doctypes
        if(newChild.getOwnerDocument()==null&&
                newChild instanceof DocumentTypeImpl){
            ((DocumentTypeImpl)newChild).ownerDocument=this;
        }
        if(errorChecking&&((docType!=null&&
                oldChild.getNodeType()!=Node.DOCUMENT_TYPE_NODE&&
                newChild.getNodeType()==Node.DOCUMENT_TYPE_NODE)
                ||(docElement!=null&&
                oldChild.getNodeType()!=Node.ELEMENT_NODE&&
                newChild.getNodeType()==Node.ELEMENT_NODE))){
            throw new DOMException(
                    DOMException.HIERARCHY_REQUEST_ERR,
                    DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"HIERARCHY_REQUEST_ERR",null));
        }
        super.replaceChild(newChild,oldChild);
        int type=oldChild.getNodeType();
        if(type==Node.ELEMENT_NODE){
            docElement=(ElementImpl)newChild;
        }else if(type==Node.DOCUMENT_TYPE_NODE){
            docType=(DocumentTypeImpl)newChild;
        }
        return oldChild;
    }   // replaceChild(Node,Node):Node

    public String getTextContent() throws DOMException{
        return null;
    }

    public void setTextContent(String textContent)
            throws DOMException{
        // no-op
    }

    protected void cloneNode(CoreDocumentImpl newdoc,boolean deep){
        // clone the children by importing them
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        if(deep){
            Map<Node,String> reversedIdentifiers=null;
            if(identifiers!=null){
                // Build a reverse mapping from element to identifier.
                reversedIdentifiers=new HashMap<>(identifiers.size());
                for(String elementId : identifiers.keySet()){
                    reversedIdentifiers.put(identifiers.get(elementId),elementId);
                }
            }
            // Copy children into new document.
            for(ChildNode kid=firstChild;kid!=null;
                kid=kid.nextSibling){
                newdoc.appendChild(newdoc.importNode(kid,true,true,
                        reversedIdentifiers));
            }
        }
        // experimental
        newdoc.allowGrammarAccess=allowGrammarAccess;
        newdoc.errorChecking=errorChecking;
    } // cloneNode(CoreDocumentImpl,boolean):void

    void callUserDataHandlers(Node n,Node c,short operation){
        if(nodeUserData==null){
            return;
        }
        if(n instanceof NodeImpl){
            Map<String,UserDataRecord> t=((NodeImpl)n).getUserDataRecord();
            if(t==null||t.isEmpty()){
                return;
            }
            callUserDataHandlers(n,c,operation,t);
        }
    }

    void callUserDataHandlers(Node n,Node c,short operation,Map<String,UserDataRecord> userData){
        if(userData==null||userData.isEmpty()){
            return;
        }
        for(String key : userData.keySet()){
            UserDataRecord r=userData.get(key);
            if(r.fHandler!=null){
                r.fHandler.handle(operation,key,r.fData,n,c);
            }
        }
    }

    public DocumentType getDoctype(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return docType;
    }
    // other document methods

    public DOMImplementation getImplementation(){
        // Currently implemented as a singleton, since it's hardcoded
        // information anyway.
        return CoreDOMImplementationImpl.getDOMImplementation();
    }

    public Element getDocumentElement(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return docElement;
    }

    public Element createElement(String tagName)
            throws DOMException{
        if(errorChecking&&!isXMLName(tagName,xml11Version)){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        return new ElementImpl(this,tagName);
    } // createElement(String):Element

    public DocumentFragment createDocumentFragment(){
        return new DocumentFragmentImpl(this);
    }
    //
    // Public methods
    //
    // properties

    public Text createTextNode(String data){
        return new TextImpl(this,data);
    }

    public Comment createComment(String data){
        return new CommentImpl(this,data);
    }

    public CDATASection createCDATASection(String data)
            throws DOMException{
        return new CDATASectionImpl(this,data);
    }

    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data)
            throws DOMException{
        if(errorChecking&&!isXMLName(target,xml11Version)){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        return new ProcessingInstructionImpl(this,target,data);
    } // createProcessingInstruction(String,String):ProcessingInstruction

    public Attr createAttribute(String name)
            throws DOMException{
        if(errorChecking&&!isXMLName(name,xml11Version)){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "INVALID_CHARACTER_ERR",
                            null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        return new AttrImpl(this,name);
    } // createAttribute(String):Attr

    public EntityReference createEntityReference(String name)
            throws DOMException{
        if(errorChecking&&!isXMLName(name,xml11Version)){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        return new EntityReferenceImpl(this,name);
    } // createEntityReference(String):EntityReference

    public NodeList getElementsByTagName(String tagname){
        return new DeepNodeListImpl(this,tagname);
    }

    public Node importNode(Node source,boolean deep)
            throws DOMException{
        return importNode(source,deep,false,null);
    } // importNode(Node,boolean):Node

    //
    // DOM2: Namespace methods
    //
    public Element createElementNS(String namespaceURI,String qualifiedName)
            throws DOMException{
        return new ElementNSImpl(this,namespaceURI,qualifiedName);
    }

    public Attr createAttributeNS(String namespaceURI,String qualifiedName)
            throws DOMException{
        return new AttrNSImpl(this,namespaceURI,qualifiedName);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName){
        return new DeepNodeListImpl(this,namespaceURI,localName);
    }

    // identifier maintenence
    public Element getElementById(String elementId){
        return getIdentifier(elementId);
    }

    public String getInputEncoding(){
        return actualEncoding;
    }

    public void setInputEncoding(String value){
        actualEncoding=value;
    }

    public String getXmlEncoding(){
        return encoding;
    }

    public void setXmlEncoding(String value){
        encoding=value;
    }

    public boolean getXmlStandalone(){
        return standalone;
    }

    public void setXmlStandalone(boolean value)
            throws DOMException{
        standalone=value;
    }

    public String getXmlVersion(){
        return (version==null)?"1.0":version;
    }

    public void setXmlVersion(String value){
        if(value.equals("1.0")||value.equals("1.1")){
            //we need to change the flag value only --
            // when the version set is different than already set.
            if(!getXmlVersion().equals(value)){
                xmlVersionChanged=true;
                //change the normalization value back to false
                isNormalized(false);
                version=value;
            }
        }else{
            //NOT_SUPPORTED_ERR: Raised if the vesion is set to a value that is not supported by
            //this document
            //we dont support any other XML version
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
        if((getXmlVersion()).equals("1.1")){
            xml11Version=true;
        }else{
            xml11Version=false;
        }
    }

    public boolean getStrictErrorChecking(){
        return errorChecking;
    }

    public void setStrictErrorChecking(boolean check){
        errorChecking=check;
    }

    public String getDocumentURI(){
        return fDocumentURI;
    }

    public void setDocumentURI(String documentURI){
        fDocumentURI=documentURI;
    }

    public Node adoptNode(Node source){
        NodeImpl node;
        Map<String,UserDataRecord> userData;
        try{
            node=(NodeImpl)source;
        }catch(ClassCastException e){
            // source node comes from a different DOMImplementation
            return null;
        }
        // Return null if the source is null
        if(source==null){
            return null;
        }else if(source.getOwnerDocument()!=null){
            DOMImplementation thisImpl=this.getImplementation();
            DOMImplementation otherImpl=source.getOwnerDocument().getImplementation();
            // when the source node comes from a different implementation.
            if(thisImpl!=otherImpl){
                // Adopting from a DefferedDOM to DOM
                if(thisImpl instanceof DOMImplementationImpl&&
                        otherImpl instanceof DeferredDOMImplementationImpl){
                    // traverse the DOM and expand deffered nodes and then allow adoption
                    undeferChildren(node);
                }else if(thisImpl instanceof DeferredDOMImplementationImpl
                        &&otherImpl instanceof DOMImplementationImpl){
                    // Adopting from a DOM into a DefferedDOM, this should be okay
                }else{
                    // Adopting between two dissimilar DOM's is not allowed
                    return null;
                }
            }
        }
        switch(node.getNodeType()){
            case ATTRIBUTE_NODE:{
                AttrImpl attr=(AttrImpl)node;
                // remove node from wherever it is
                if(attr.getOwnerElement()!=null){
                    //1. owner element attribute is set to null
                    attr.getOwnerElement().removeAttributeNode(attr);
                }
                //2. specified flag is set to true
                attr.isSpecified(true);
                userData=node.getUserDataRecord();
                //3. change ownership
                attr.setOwnerDocument(this);
                if(userData!=null){
                    setUserDataTable(node,userData);
                }
                break;
            }
            //entity, notation nodes are read only nodes.. so they can't be adopted.
            //runtime will fall through to NOTATION_NODE
            case ENTITY_NODE:
            case NOTATION_NODE:{
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            //document, documentype nodes can't be adopted.
            //runtime will fall through to DocumentTypeNode
            case DOCUMENT_NODE:
            case DOCUMENT_TYPE_NODE:{
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
            }
            case ENTITY_REFERENCE_NODE:{
                userData=node.getUserDataRecord();
                // remove node from wherever it is
                Node parent=node.getParentNode();
                if(parent!=null){
                    parent.removeChild(source);
                }
                // discard its replacement value
                Node child;
                while((child=node.getFirstChild())!=null){
                    node.removeChild(child);
                }
                // change ownership
                node.setOwnerDocument(this);
                if(userData!=null){
                    setUserDataTable(node,userData);
                }
                // set its new replacement value if any
                if(docType==null){
                    break;
                }
                NamedNodeMap entities=docType.getEntities();
                Node entityNode=entities.getNamedItem(node.getNodeName());
                if(entityNode==null){
                    break;
                }
                for(child=entityNode.getFirstChild();
                    child!=null;child=child.getNextSibling()){
                    Node childClone=child.cloneNode(true);
                    node.appendChild(childClone);
                }
                break;
            }
            case ELEMENT_NODE:{
                userData=node.getUserDataRecord();
                // remove node from wherever it is
                Node parent=node.getParentNode();
                if(parent!=null){
                    parent.removeChild(source);
                }
                // change ownership
                node.setOwnerDocument(this);
                if(userData!=null){
                    setUserDataTable(node,userData);
                }
                // reconcile default attributes
                ((ElementImpl)node).reconcileDefaultAttributes();
                break;
            }
            default:{
                userData=node.getUserDataRecord();
                // remove node from wherever it is
                Node parent=node.getParentNode();
                if(parent!=null){
                    parent.removeChild(source);
                }
                // change ownership
                node.setOwnerDocument(this);
                if(userData!=null){
                    setUserDataTable(node,userData);
                }
            }
        }
        //DOM L3 Core CR
        //http://www.w3.org/TR/2003/CR-DOM-Level-3-Core-20031107/core.html#UserDataHandler-ADOPTED
        if(userData!=null){
            callUserDataHandlers(source,null,UserDataHandler.NODE_ADOPTED,userData);
        }
        return node;
    }

    public DOMConfiguration getDomConfig(){
        if(fConfiguration==null){
            fConfiguration=new DOMConfigurationImpl();
        }
        return fConfiguration;
    }

    public void normalizeDocument(){
        // No need to normalize if already normalized.
        if(isNormalized()&&!isNormalizeDocRequired()){
            return;
        }
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        if(domNormalizer==null){
            domNormalizer=new DOMNormalizer();
        }
        if(fConfiguration==null){
            fConfiguration=new DOMConfigurationImpl();
        }else{
            fConfiguration.reset();
        }
        domNormalizer.normalizeDocument(this,fConfiguration);
        isNormalized(true);
        //set the XMLversion changed value to false -- once we have finished
        //doing normalization
        xmlVersionChanged=false;
    }

    public Node renameNode(Node n,String namespaceURI,String name)
            throws DOMException{
        if(errorChecking&&n.getOwnerDocument()!=this&&n!=this){
            String msg=DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
        }
        switch(n.getNodeType()){
            case ELEMENT_NODE:{
                ElementImpl el=(ElementImpl)n;
                if(el instanceof ElementNSImpl){
                    ((ElementNSImpl)el).rename(namespaceURI,name);
                    // fire user data NODE_RENAMED event
                    callUserDataHandlers(el,null,UserDataHandler.NODE_RENAMED);
                }else{
                    if(namespaceURI==null){
                        if(errorChecking){
                            int colon1=name.indexOf(':');
                            if(colon1!=-1){
                                String msg=
                                        DOMMessageFormatter.formatMessage(
                                                DOMMessageFormatter.DOM_DOMAIN,
                                                "NAMESPACE_ERR",
                                                null);
                                throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                            }
                            if(!isXMLName(name,xml11Version)){
                                String msg=DOMMessageFormatter.formatMessage(
                                        DOMMessageFormatter.DOM_DOMAIN,
                                        "INVALID_CHARACTER_ERR",null);
                                throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
                                        msg);
                            }
                        }
                        el.rename(name);
                        // fire user data NODE_RENAMED event
                        callUserDataHandlers(el,null,
                                UserDataHandler.NODE_RENAMED);
                    }else{
                        // we need to create a new object
                        ElementNSImpl nel=
                                new ElementNSImpl(this,namespaceURI,name);
                        // register event listeners on new node
                        copyEventListeners(el,nel);
                        // remove user data from old node
                        Map<String,UserDataRecord> data=removeUserDataTable(el);
                        // remove old node from parent if any
                        Node parent=el.getParentNode();
                        Node nextSib=el.getNextSibling();
                        if(parent!=null){
                            parent.removeChild(el);
                        }
                        // move children to new node
                        Node child=el.getFirstChild();
                        while(child!=null){
                            el.removeChild(child);
                            nel.appendChild(child);
                            child=el.getFirstChild();
                        }
                        // move specified attributes to new node
                        nel.moveSpecifiedAttributes(el);
                        // attach user data to new node
                        setUserDataTable(nel,data);
                        // and fire user data NODE_RENAMED event
                        callUserDataHandlers(el,nel,
                                UserDataHandler.NODE_RENAMED);
                        // insert new node where old one was
                        if(parent!=null){
                            parent.insertBefore(nel,nextSib);
                        }
                        el=nel;
                    }
                }
                // fire ElementNameChanged event
                renamedElement((Element)n,el);
                return el;
            }
            case ATTRIBUTE_NODE:{
                AttrImpl at=(AttrImpl)n;
                // dettach attr from element
                Element el=at.getOwnerElement();
                if(el!=null){
                    el.removeAttributeNode(at);
                }
                if(n instanceof AttrNSImpl){
                    ((AttrNSImpl)at).rename(namespaceURI,name);
                    // reattach attr to element
                    if(el!=null){
                        el.setAttributeNodeNS(at);
                    }
                    // fire user data NODE_RENAMED event
                    callUserDataHandlers(at,null,UserDataHandler.NODE_RENAMED);
                }else{
                    if(namespaceURI==null){
                        at.rename(name);
                        // reattach attr to element
                        if(el!=null){
                            el.setAttributeNode(at);
                        }
                        // fire user data NODE_RENAMED event
                        callUserDataHandlers(at,null,UserDataHandler.NODE_RENAMED);
                    }else{
                        // we need to create a new object
                        AttrNSImpl nat=new AttrNSImpl(this,namespaceURI,name);
                        // register event listeners on new node
                        copyEventListeners(at,nat);
                        // remove user data from old node
                        Map<String,UserDataRecord> data=removeUserDataTable(at);
                        // move children to new node
                        Node child=at.getFirstChild();
                        while(child!=null){
                            at.removeChild(child);
                            nat.appendChild(child);
                            child=at.getFirstChild();
                        }
                        // attach user data to new node
                        setUserDataTable(nat,data);
                        // and fire user data NODE_RENAMED event
                        callUserDataHandlers(at,nat,UserDataHandler.NODE_RENAMED);
                        // reattach attr to element
                        if(el!=null){
                            el.setAttributeNode(nat);
                        }
                        at=nat;
                    }
                }
                // fire AttributeNameChanged event
                renamedAttrNode((Attr)n,at);
                return at;
            }
            default:{
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
            }
        }
    }

    public static final boolean isXMLName(String s,boolean xml11Version){
        if(s==null){
            return false;
        }
        if(!xml11Version)
            return XMLChar.isValidName(s);
        else
            return XML11Char.isXML11ValidName(s);
    } // isXMLName(String):boolean

    Map<String,UserDataRecord> removeUserDataTable(Node n){
        if(nodeUserData==null){
            return null;
        }
        return nodeUserData.get(n);
    }

    void setUserDataTable(Node n,Map<String,UserDataRecord> data){
        if(nodeUserData==null){
            nodeUserData=new HashMap<>();
        }
        if(data!=null){
            nodeUserData.put(n,data);
        }
    }

    protected void copyEventListeners(NodeImpl src,NodeImpl tgt){
        // does nothing by default - overidden in subclass
    }

    void renamedAttrNode(Attr oldAt,Attr newAt){
    }

    void renamedElement(Element oldEl,Element newEl){
    }

    boolean isNormalizeDocRequired(){
        // REVISIT: Implement to optimize when normalization
        // is required
        return true;
    }

    public Element getIdentifier(String idName){
        if(needsSyncData()){
            synchronizeData();
        }
        if(identifiers==null){
            return null;
        }
        Element elem=(Element)identifiers.get(idName);
        if(elem!=null){
            // check that the element is in the tree
            Node parent=elem.getParentNode();
            while(parent!=null){
                if(parent==this){
                    return elem;
                }
                parent=parent.getParentNode();
            }
        }
        return null;
    } // getIdentifier(String):Element

    public boolean getErrorChecking(){
        return errorChecking;
    }

    public void setErrorChecking(boolean check){
        errorChecking=check;
    }

    public String getEncoding(){
        return getXmlEncoding();
    }

    public void setEncoding(String value){
        setXmlEncoding(value);
    }

    public String getVersion(){
        return getXmlVersion();
    }

    public void setVersion(String value){
        setXmlVersion(value);
    }

    public boolean getStandalone(){
        return getXmlStandalone();
    }

    public void setStandalone(boolean value){
        setXmlStandalone(value);
    }

    //
    // DOM L3 LS
    //
    public boolean getAsync(){
        return false;
    }

    public void setAsync(boolean async){
        if(async){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
    }

    public void abort(){
    }

    public boolean load(String uri){
        return false;
    }

    public boolean loadXML(String source){
        return false;
    }

    public String saveXML(Node node)
            throws DOMException{
        if(errorChecking&&node!=null
                &&this!=node.getOwnerDocument()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
        }
        DOMImplementationLS domImplLS=(DOMImplementationLS)DOMImplementationImpl.getDOMImplementation();
        LSSerializer xmlWriter=domImplLS.createLSSerializer();
        if(node==null){
            node=this;
        }
        return xmlWriter.writeToString(node);
    }

    boolean getMutationEvents(){
        // does nothing by default - overriden in subclass
        return false;
    }

    void setMutationEvents(boolean set){
        // does nothing by default - overidden in subclass
    }
    //
    // Object methods
    //

    // non-DOM factory methods
    public DocumentType createDocumentType(String qualifiedName,
                                           String publicID,
                                           String systemID)
            throws DOMException{
        return new DocumentTypeImpl(this,qualifiedName,publicID,systemID);
    } // createDocumentType(String):DocumentType
    //
    // Public static methods
    //

    public Entity createEntity(String name)
            throws DOMException{
        if(errorChecking&&!isXMLName(name,xml11Version)){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        return new EntityImpl(this,name);
    } // createEntity(String):Entity

    public Notation createNotation(String name)
            throws DOMException{
        if(errorChecking&&!isXMLName(name,xml11Version)){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        return new NotationImpl(this,name);
    } // createNotation(String):Notation
    //
    // Protected methods
    //

    public ElementDefinitionImpl createElementDefinition(String name)
            throws DOMException{
        if(errorChecking&&!isXMLName(name,xml11Version)){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        return new ElementDefinitionImpl(this,name);
    } // createElementDefinition(String):ElementDefinitionImpl

    protected int getNodeNumber(Node node){
        // Check if the node is already in the hash
        // If so, retrieve the node number
        // If not, assign a number to the node
        // Node numbers are negative, from -1 to -n
        int num;
        if(nodeTable==null){
            nodeTable=new HashMap<>();
            num=--nodeCounter;
            nodeTable.put(node,new Integer(num));
        }else{
            Integer n=nodeTable.get(node);
            if(n==null){
                num=--nodeCounter;
                nodeTable.put(node,num);
            }else{
                num=n.intValue();
            }
        }
        return num;
    }

    private Node importNode(Node source,boolean deep,boolean cloningDoc,
                            Map<Node,String> reversedIdentifiers)
            throws DOMException{
        Node newnode=null;
        Map<String,UserDataRecord> userData=null;
        // Sigh. This doesn't work; too many nodes have private data that
        // would have to be manually tweaked. May be able to add local
        // shortcuts to each nodetype. Consider ?????
        // if(source instanceof NodeImpl &&
        //  !(source instanceof DocumentImpl))
        // {
        //  // Can't clone DocumentImpl since it invokes us...
        //  newnode=(NodeImpl)source.cloneNode(false);
        //  newnode.ownerDocument=this;
        // }
        // else
        if(source instanceof NodeImpl){
            userData=((NodeImpl)source).getUserDataRecord();
        }
        int type=source.getNodeType();
        switch(type){
            case ELEMENT_NODE:{
                Element newElement;
                boolean domLevel20=source.getOwnerDocument().getImplementation().hasFeature("XML","2.0");
                // Create element according to namespace support/qualification.
                if(domLevel20==false||source.getLocalName()==null)
                    newElement=createElement(source.getNodeName());
                else
                    newElement=createElementNS(source.getNamespaceURI(),
                            source.getNodeName());
                // Copy element's attributes, if any.
                NamedNodeMap sourceAttrs=source.getAttributes();
                if(sourceAttrs!=null){
                    int length=sourceAttrs.getLength();
                    for(int index=0;index<length;index++){
                        Attr attr=(Attr)sourceAttrs.item(index);
                        // NOTE: this methods is used for both importingNode
                        // and cloning the document node. In case of the
                        // clonning default attributes should be copied.
                        // But for importNode defaults should be ignored.
                        if(attr.getSpecified()||cloningDoc){
                            Attr newAttr=(Attr)importNode(attr,true,cloningDoc,
                                    reversedIdentifiers);
                            // Attach attribute according to namespace
                            // support/qualification.
                            if(domLevel20==false||
                                    attr.getLocalName()==null)
                                newElement.setAttributeNode(newAttr);
                            else
                                newElement.setAttributeNodeNS(newAttr);
                        }
                    }
                }
                // Register element identifier.
                if(reversedIdentifiers!=null){
                    // Does element have an associated identifier?
                    String elementId=reversedIdentifiers.get(source);
                    if(elementId!=null){
                        if(identifiers==null){
                            identifiers=new HashMap<>();
                        }
                        identifiers.put(elementId,newElement);
                    }
                }
                newnode=newElement;
                break;
            }
            case ATTRIBUTE_NODE:{
                if(source.getOwnerDocument().getImplementation().hasFeature("XML","2.0")){
                    if(source.getLocalName()==null){
                        newnode=createAttribute(source.getNodeName());
                    }else{
                        newnode=createAttributeNS(source.getNamespaceURI(),
                                source.getNodeName());
                    }
                }else{
                    newnode=createAttribute(source.getNodeName());
                }
                // if source is an AttrImpl from this very same implementation
                // avoid creating the child nodes if possible
                if(source instanceof AttrImpl){
                    AttrImpl attr=(AttrImpl)source;
                    if(attr.hasStringValue()){
                        AttrImpl newattr=(AttrImpl)newnode;
                        newattr.setValue(attr.getValue());
                        deep=false;
                    }else{
                        deep=true;
                    }
                }else{
                    // According to the DOM spec the kids carry the value.
                    // However, there are non compliant implementations out
                    // there that fail to do so. To avoid ending up with no
                    // value at all, in this case we simply copy the text value
                    // directly.
                    if(source.getFirstChild()==null){
                        newnode.setNodeValue(source.getNodeValue());
                        deep=false;
                    }else{
                        deep=true;
                    }
                }
                break;
            }
            case TEXT_NODE:{
                newnode=createTextNode(source.getNodeValue());
                break;
            }
            case CDATA_SECTION_NODE:{
                newnode=createCDATASection(source.getNodeValue());
                break;
            }
            case ENTITY_REFERENCE_NODE:{
                newnode=createEntityReference(source.getNodeName());
                // the subtree is created according to this doc by the method
                // above, so avoid carrying over original subtree
                deep=false;
                break;
            }
            case ENTITY_NODE:{
                Entity srcentity=(Entity)source;
                EntityImpl newentity=
                        (EntityImpl)createEntity(source.getNodeName());
                newentity.setPublicId(srcentity.getPublicId());
                newentity.setSystemId(srcentity.getSystemId());
                newentity.setNotationName(srcentity.getNotationName());
                // Kids carry additional value,
                // allow deep import temporarily
                newentity.isReadOnly(false);
                newnode=newentity;
                break;
            }
            case PROCESSING_INSTRUCTION_NODE:{
                newnode=createProcessingInstruction(source.getNodeName(),
                        source.getNodeValue());
                break;
            }
            case COMMENT_NODE:{
                newnode=createComment(source.getNodeValue());
                break;
            }
            case DOCUMENT_TYPE_NODE:{
                // unless this is used as part of cloning a Document
                // forbid it for the sake of being compliant to the DOM spec
                if(!cloningDoc){
                    String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
                }
                DocumentType srcdoctype=(DocumentType)source;
                DocumentTypeImpl newdoctype=(DocumentTypeImpl)
                        createDocumentType(srcdoctype.getNodeName(),
                                srcdoctype.getPublicId(),
                                srcdoctype.getSystemId());
                // Values are on NamedNodeMaps
                NamedNodeMap smap=srcdoctype.getEntities();
                NamedNodeMap tmap=newdoctype.getEntities();
                if(smap!=null){
                    for(int i=0;i<smap.getLength();i++){
                        tmap.setNamedItem(importNode(smap.item(i),true,true,
                                reversedIdentifiers));
                    }
                }
                smap=srcdoctype.getNotations();
                tmap=newdoctype.getNotations();
                if(smap!=null){
                    for(int i=0;i<smap.getLength();i++){
                        tmap.setNamedItem(importNode(smap.item(i),true,true,
                                reversedIdentifiers));
                    }
                }
                // NOTE: At this time, the DOM definition of DocumentType
                // doesn't cover Elements and their Attributes. domimpl's
                // extentions in that area will not be preserved, even if
                // copying from domimpl to domimpl. We could special-case
                // that here. Arguably we should. Consider. ?????
                newnode=newdoctype;
                break;
            }
            case DOCUMENT_FRAGMENT_NODE:{
                newnode=createDocumentFragment();
                // No name, kids carry value
                break;
            }
            case NOTATION_NODE:{
                Notation srcnotation=(Notation)source;
                NotationImpl newnotation=
                        (NotationImpl)createNotation(source.getNodeName());
                newnotation.setPublicId(srcnotation.getPublicId());
                newnotation.setSystemId(srcnotation.getSystemId());
                // Kids carry additional value
                newnode=newnotation;
                // No name, no value
                break;
            }
            case DOCUMENT_NODE: // Can't import document nodes
            default:{           // Unknown node type
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
            }
        }
        if(userData!=null)
            callUserDataHandlers(source,newnode,UserDataHandler.NODE_IMPORTED,userData);
        // If deep, replicate and attach the kids.
        if(deep){
            for(Node srckid=source.getFirstChild();
                srckid!=null;
                srckid=srckid.getNextSibling()){
                newnode.appendChild(importNode(srckid,true,cloningDoc,
                        reversedIdentifiers));
            }
        }
        if(newnode.getNodeType()==Node.ENTITY_NODE){
            ((NodeImpl)newnode).setReadOnly(true,true);
        }
        return newnode;
    } // importNode(Node,boolean,boolean,Map):Node
    //  NodeListCache pool

    protected void undeferChildren(Node node){
        Node top=node;
        while(null!=node){
            if(((NodeImpl)node).needsSyncData()){
                ((NodeImpl)node).synchronizeData();
            }
            NamedNodeMap attributes=node.getAttributes();
            if(attributes!=null){
                int length=attributes.getLength();
                for(int i=0;i<length;++i){
                    undeferChildren(attributes.item(i));
                }
            }
            Node nextNode=null;
            nextNode=node.getFirstChild();
            while(null==nextNode){
                if(top.equals(node))
                    break;
                nextNode=node.getNextSibling();
                if(null==nextNode){
                    node=node.getParentNode();
                    if((null==node)||(top.equals(node))){
                        nextNode=null;
                        break;
                    }
                }
            }
            node=nextNode;
        }
    }

    protected final void clearIdentifiers(){
        if(identifiers!=null){
            identifiers.clear();
        }
    }

    public void putIdentifier(String idName,Element element){
        if(element==null){
            removeIdentifier(idName);
            return;
        }
        if(needsSyncData()){
            synchronizeData();
        }
        if(identifiers==null){
            identifiers=new HashMap<>();
        }
        identifiers.put(idName,element);
    } // putIdentifier(String,Element)

    public void removeIdentifier(String idName){
        if(needsSyncData()){
            synchronizeData();
        }
        if(identifiers==null){
            return;
        }
        identifiers.remove(idName);
    } // removeIdentifier(String)

    public Element createElementNS(String namespaceURI,String qualifiedName,
                                   String localpart)
            throws DOMException{
        return new ElementNSImpl(this,namespaceURI,qualifiedName,localpart);
    }

    public Attr createAttributeNS(String namespaceURI,String qualifiedName,
                                  String localpart)
            throws DOMException{
        return new AttrNSImpl(this,namespaceURI,qualifiedName,localpart);
    }

    public Object clone() throws CloneNotSupportedException{
        CoreDocumentImpl newdoc=(CoreDocumentImpl)super.clone();
        newdoc.docType=null;
        newdoc.docElement=null;
        return newdoc;
    }

    protected boolean isKidOK(Node parent,Node child){
        if(allowGrammarAccess&&
                parent.getNodeType()==Node.DOCUMENT_TYPE_NODE){
            return child.getNodeType()==Node.ELEMENT_NODE;
        }
        return 0!=(kidOK[parent.getNodeType()]&1<<child.getNodeType());
    }

    NodeListCache getNodeListCache(ParentNode owner){
        if(fFreeNLCache==null){
            return new NodeListCache(owner);
        }
        NodeListCache c=fFreeNLCache;
        fFreeNLCache=fFreeNLCache.next;
        c.fChild=null;
        c.fChildIndex=-1;
        c.fLength=-1;
        // revoke previous ownership
        if(c.fOwner!=null){
            c.fOwner.fNodeListCache=null;
        }
        c.fOwner=owner;
        // c.next = null; not necessary, except for confused people...
        return c;
    }
    // Temporarily comment out this method, because
    // 1. It seems that finalizers are not guaranteed to be called, so the
    //    functionality is not implemented.
    // 2. It affects the performance greatly in multi-thread environment.
    // -SG

    void freeNodeListCache(NodeListCache c){
        c.next=fFreeNLCache;
        fFreeNLCache=c;
    }

    protected Map<String,UserDataRecord> getUserDataRecord(Node n){
        if(nodeUserData==null){
            return null;
        }
        Map<String,UserDataRecord> t=nodeUserData.get(n);
        if(t==null){
            return null;
        }
        return t;
    }

    protected final void checkNamespaceWF(String qname,int colon1,
                                          int colon2){
        if(!errorChecking){
            return;
        }
        // it is an error for NCName to have more than one ':'
        // check if it is valid QName [Namespace in XML production 6]
        // :camera , nikon:camera:minolta, camera:
        if(colon1==0||colon1==qname.length()-1||colon2!=colon1){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NAMESPACE_ERR",
                            null);
            throw new DOMException(DOMException.NAMESPACE_ERR,msg);
        }
    }

    protected final void checkDOMNSErr(String prefix,
                                       String namespace){
        if(errorChecking){
            if(namespace==null){
                String msg=
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "NAMESPACE_ERR",
                                null);
                throw new DOMException(DOMException.NAMESPACE_ERR,msg);
            }else if(prefix.equals("xml")
                    &&!namespace.equals(NamespaceContext.XML_URI)){
                String msg=
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "NAMESPACE_ERR",
                                null);
                throw new DOMException(DOMException.NAMESPACE_ERR,msg);
            }else if(
                    prefix.equals("xmlns")
                            &&!namespace.equals(NamespaceContext.XMLNS_URI)
                            ||(!prefix.equals("xmlns")
                            &&namespace.equals(NamespaceContext.XMLNS_URI))){
                String msg=
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "NAMESPACE_ERR",
                                null);
                throw new DOMException(DOMException.NAMESPACE_ERR,msg);
            }
        }
    }

    protected final void checkQName(String prefix,String local){
        if(!errorChecking){
            return;
        }
        // check that both prefix and local part match NCName
        boolean validNCName=false;
        if(!xml11Version){
            validNCName=(prefix==null||XMLChar.isValidNCName(prefix))
                    &&XMLChar.isValidNCName(local);
        }else{
            validNCName=(prefix==null||XML11Char.isXML11ValidNCName(prefix))
                    &&XML11Char.isXML11ValidNCName(local);
        }
        if(!validNCName){
            // REVISIT: add qname parameter to the message
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "INVALID_CHARACTER_ERR",
                            null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
    }

    boolean isXML11Version(){
        return xml11Version;
    }

    //we should be checking the (elements, attribute, entity etc.) names only when
    //version of the document is changed.
    boolean isXMLVersionChanged(){
        return xmlVersionChanged;
    }

    protected void setUserData(NodeImpl n,Object data){
        setUserData(n,"XERCES1DOMUSERDATA",data,null);
    }
    // Event related methods overidden in subclass

    public Object setUserData(Node n,String key,
                              Object data,UserDataHandler handler){
        if(data==null){
            if(nodeUserData!=null){
                Map<String,UserDataRecord> t=nodeUserData.get(n);
                if(t!=null){
                    UserDataRecord r=t.remove(key);
                    if(r!=null){
                        return r.fData;
                    }
                }
            }
            return null;
        }else{
            Map<String,UserDataRecord> t;
            if(nodeUserData==null){
                nodeUserData=new HashMap<>();
                t=new HashMap<>();
                nodeUserData.put(n,t);
            }else{
                t=nodeUserData.get(n);
                if(t==null){
                    t=new HashMap<>();
                    nodeUserData.put(n,t);
                }
            }
            UserDataRecord r=t.put(key,new UserDataRecord(data,handler));
            if(r!=null){
                return r.fData;
            }
            return null;
        }
    }

    protected Object getUserData(NodeImpl n){
        return getUserData(n,"XERCES1DOMUSERDATA");
    }

    public Object getUserData(Node n,String key){
        if(nodeUserData==null){
            return null;
        }
        Map<String,UserDataRecord> t=nodeUserData.get(n);
        if(t==null){
            return null;
        }
        UserDataRecord r=t.get(key);
        if(r!=null){
            return r.fData;
        }
        return null;
    }

    protected void addEventListener(NodeImpl node,String type,
                                    EventListener listener,
                                    boolean useCapture){
        // does nothing by default - overidden in subclass
    }
    // Notification methods overidden in subclasses

    protected void removeEventListener(NodeImpl node,String type,
                                       EventListener listener,
                                       boolean useCapture){
        // does nothing by default - overidden in subclass
    }

    protected boolean dispatchEvent(NodeImpl node,Event event){
        // does nothing by default - overidden in subclass
        return false;
    }

    void replacedText(NodeImpl node){
    }

    void deletedText(NodeImpl node,int offset,int count){
    }

    void insertedText(NodeImpl node,int offset,int count){
    }

    void modifyingCharacterData(NodeImpl node,boolean replace){
    }

    void modifiedCharacterData(NodeImpl node,String oldvalue,String value,boolean replace){
    }

    void insertingNode(NodeImpl node,boolean replace){
    }

    void insertedNode(NodeImpl node,NodeImpl newInternal,boolean replace){
    }

    void removingNode(NodeImpl node,NodeImpl oldChild,boolean replace){
    }

    void removedNode(NodeImpl node,boolean replace){
    }

    void replacingNode(NodeImpl node){
    }

    void replacedNode(NodeImpl node){
    }

    void replacingData(NodeImpl node){
    }

    void replacedCharacterData(NodeImpl node,String oldvalue,String value){
    }

    void modifiedAttrValue(AttrImpl attr,String oldvalue){
    }

    void setAttrNode(AttrImpl attr,AttrImpl previous){
    }

    void removedAttrNode(AttrImpl attr,NodeImpl oldOwner,String name){
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        // Convert Maps to Hashtables
        Hashtable<Node,Hashtable<String,UserDataRecord>> nud=null;
        if(nodeUserData!=null){
            nud=new Hashtable<>();
            for(Map.Entry<Node,Map<String,UserDataRecord>> e : nodeUserData.entrySet()){
                //e.getValue() will not be null since an entry is always put with a non-null value
                nud.put(e.getKey(),new Hashtable<>(e.getValue()));
            }
        }
        Hashtable<String,Node> ids=(identifiers==null)?null:new Hashtable<>(identifiers);
        Hashtable<Node,Integer> nt=(nodeTable==null)?null:new Hashtable<>(nodeTable);
        // Write serialized fields
        ObjectOutputStream.PutField pf=out.putFields();
        pf.put("docType",docType);
        pf.put("docElement",docElement);
        pf.put("fFreeNLCache",fFreeNLCache);
        pf.put("encoding",encoding);
        pf.put("actualEncoding",actualEncoding);
        pf.put("version",version);
        pf.put("standalone",standalone);
        pf.put("fDocumentURI",fDocumentURI);
        //userData is the original name. It has been changed to nodeUserData, refer to the corrsponding @serialField
        pf.put("userData",nud);
        pf.put("identifiers",ids);
        pf.put("changes",changes);
        pf.put("allowGrammarAccess",allowGrammarAccess);
        pf.put("errorChecking",errorChecking);
        pf.put("ancestorChecking",ancestorChecking);
        pf.put("xmlVersionChanged",xmlVersionChanged);
        pf.put("documentNumber",documentNumber);
        pf.put("nodeCounter",nodeCounter);
        pf.put("nodeTable",nt);
        pf.put("xml11Version",xml11Version);
        out.writeFields();
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        // We have to read serialized fields first.
        ObjectInputStream.GetField gf=in.readFields();
        docType=(DocumentTypeImpl)gf.get("docType",null);
        docElement=(ElementImpl)gf.get("docElement",null);
        fFreeNLCache=(NodeListCache)gf.get("fFreeNLCache",null);
        encoding=(String)gf.get("encoding",null);
        actualEncoding=(String)gf.get("actualEncoding",null);
        version=(String)gf.get("version",null);
        standalone=gf.get("standalone",false);
        fDocumentURI=(String)gf.get("fDocumentURI",null);
        //userData is the original name. It has been changed to nodeUserData, refer to the corrsponding @serialField
        Hashtable<Node,Hashtable<String,UserDataRecord>> nud=
                (Hashtable<Node,Hashtable<String,UserDataRecord>>)gf.get("userData",null);
        Hashtable<String,Node> ids=(Hashtable<String,Node>)gf.get("identifiers",null);
        changes=gf.get("changes",0);
        allowGrammarAccess=gf.get("allowGrammarAccess",false);
        errorChecking=gf.get("errorChecking",true);
        ancestorChecking=gf.get("ancestorChecking",true);
        xmlVersionChanged=gf.get("xmlVersionChanged",false);
        documentNumber=gf.get("documentNumber",0);
        nodeCounter=gf.get("nodeCounter",0);
        Hashtable<Node,Integer> nt=(Hashtable<Node,Integer>)gf.get("nodeTable",null);
        xml11Version=gf.get("xml11Version",false);
        //convert Hashtables back to HashMaps
        if(nud!=null){
            nodeUserData=new HashMap<>();
            for(Map.Entry<Node,Hashtable<String,UserDataRecord>> e : nud.entrySet()){
                nodeUserData.put(e.getKey(),new HashMap<>(e.getValue()));
            }
        }
        if(ids!=null) identifiers=new HashMap<>(ids);
        if(nt!=null) nodeTable=new HashMap<>(nt);
    }
} // class CoreDocumentImpl
