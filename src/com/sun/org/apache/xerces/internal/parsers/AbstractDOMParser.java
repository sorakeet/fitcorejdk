/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * Copyright 2001-2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.dom.*;
import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import org.w3c.dom.*;
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.traversal.NodeFilter;

import java.util.Locale;
import java.util.Stack;

public class AbstractDOMParser extends AbstractXMLDocumentParser{
    //
    // Constants
    //
    // feature ids
    protected static final String NAMESPACES=
            Constants.SAX_FEATURE_PREFIX+Constants.NAMESPACES_FEATURE;
    protected static final String CREATE_ENTITY_REF_NODES=
            Constants.XERCES_FEATURE_PREFIX+Constants.CREATE_ENTITY_REF_NODES_FEATURE;
    protected static final String INCLUDE_COMMENTS_FEATURE=
            Constants.XERCES_FEATURE_PREFIX+Constants.INCLUDE_COMMENTS_FEATURE;
    protected static final String CREATE_CDATA_NODES_FEATURE=
            Constants.XERCES_FEATURE_PREFIX+Constants.CREATE_CDATA_NODES_FEATURE;
    protected static final String INCLUDE_IGNORABLE_WHITESPACE=
            Constants.XERCES_FEATURE_PREFIX+Constants.INCLUDE_IGNORABLE_WHITESPACE;
    protected static final String DEFER_NODE_EXPANSION=
            Constants.XERCES_FEATURE_PREFIX+Constants.DEFER_NODE_EXPANSION_FEATURE;
    // property ids
    protected static final String DOCUMENT_CLASS_NAME=
            Constants.XERCES_PROPERTY_PREFIX+Constants.DOCUMENT_CLASS_NAME_PROPERTY;
    protected static final String CURRENT_ELEMENT_NODE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.CURRENT_ELEMENT_NODE_PROPERTY;
    // other
    protected static final String DEFAULT_DOCUMENT_CLASS_NAME=
            "com.sun.org.apache.xerces.internal.dom.DocumentImpl";
    protected static final String CORE_DOCUMENT_CLASS_NAME=
            "com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl";
    protected static final String PSVI_DOCUMENT_CLASS_NAME=
            "com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl";
    private static final String[] RECOGNIZED_FEATURES={
            NAMESPACES,
            CREATE_ENTITY_REF_NODES,
            INCLUDE_COMMENTS_FEATURE,
            CREATE_CDATA_NODES_FEATURE,
            INCLUDE_IGNORABLE_WHITESPACE,
            DEFER_NODE_EXPANSION
    };
    // protected static final String GRAMMAR_POOL =
    // Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String[] RECOGNIZED_PROPERTIES={
            DOCUMENT_CLASS_NAME,
            CURRENT_ELEMENT_NODE,
    };
    // debugging
    private static final boolean DEBUG_EVENTS=false;
    private static final boolean DEBUG_BASEURI=false;
    protected final StringBuilder fStringBuilder=new StringBuilder(50);
    // data
    protected final Stack fBaseURIStack=new Stack();
    private final QName fAttrQName=new QName();
    //
    // Data
    //
    protected DOMErrorHandlerWrapper fErrorHandler=null;
    protected boolean fInDTD;
    // features
    protected boolean fCreateEntityRefNodes;
    protected boolean fIncludeIgnorableWhitespace;
    protected boolean fIncludeComments;
    protected boolean fCreateCDATANodes;
    // dom information
    protected Document fDocument;
    protected CoreDocumentImpl fDocumentImpl;
    protected boolean fStorePSVI;
    protected String fDocumentClassName;
    protected DocumentType fDocumentType;
    protected Node fCurrentNode;
    protected CDATASection fCurrentCDATASection;
    protected EntityImpl fCurrentEntityDecl;
    protected int fDeferredEntityDecl;
    // internal subset
    protected StringBuilder fInternalSubset;
    // deferred expansion data
    protected boolean fDeferNodeExpansion;
    protected boolean fNamespaceAware;
    protected DeferredDocumentImpl fDeferredDocumentImpl;
    protected int fDocumentIndex;
    protected int fDocumentTypeIndex;
    protected int fCurrentNodeIndex;
    protected int fCurrentCDATASectionIndex;
    // state
    protected boolean fInDTDExternalSubset;
    protected Node fRoot;
    protected boolean fInCDATASection;
    protected boolean fFirstChunk=false;
    protected boolean fFilterReject=false;
    protected int fRejectedElementDepth=0;
    protected Stack fSkippedElemStack=null;
    protected boolean fInEntityRef=false;
    // handlers
    protected LSParserFilter fDOMFilter=null;
    private XMLLocator fLocator;
    protected AbstractDOMParser(XMLParserConfiguration config){
        super(config);
        // add recognized features
        fConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
        // set default values
        fConfiguration.setFeature(CREATE_ENTITY_REF_NODES,true);
        fConfiguration.setFeature(INCLUDE_IGNORABLE_WHITESPACE,true);
        fConfiguration.setFeature(DEFER_NODE_EXPANSION,true);
        fConfiguration.setFeature(INCLUDE_COMMENTS_FEATURE,true);
        fConfiguration.setFeature(CREATE_CDATA_NODES_FEATURE,true);
        // add recognized properties
        fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        // set default values
        fConfiguration.setProperty(DOCUMENT_CLASS_NAME,
                DEFAULT_DOCUMENT_CLASS_NAME);
    } // <init>(XMLParserConfiguration)
    //
    // Constructors
    //

    protected String getDocumentClassName(){
        return fDocumentClassName;
    }

    protected void setDocumentClassName(String documentClassName){
        // normalize class name
        if(documentClassName==null){
            documentClassName=DEFAULT_DOCUMENT_CLASS_NAME;
        }
        if(!documentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)&&
                !documentClassName.equals(PSVI_DOCUMENT_CLASS_NAME)){
            // verify that this class exists and is of the right type
            try{
                Class _class=ObjectFactory.findProviderClass(documentClassName,true);
                //if (!_class.isAssignableFrom(Document.class)) {
                if(!Document.class.isAssignableFrom(_class)){
                    throw new IllegalArgumentException(
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "InvalidDocumentClassName",new Object[]{documentClassName}));
                }
            }catch(ClassNotFoundException e){
                throw new IllegalArgumentException(
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "MissingDocumentClassName",new Object[]{documentClassName}));
            }
        }
        // set document class name
        fDocumentClassName=documentClassName;
        if(!documentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)){
            fDeferNodeExpansion=false;
        }
    } // setDocumentClassName(String)

    public Document getDocument(){
        return fDocument;
    } // getDocument():Document
    //
    // Public methods
    //

    public final void dropDocumentReferences(){
        fDocument=null;
        fDocumentImpl=null;
        fDeferredDocumentImpl=null;
        fDocumentType=null;
        fCurrentNode=null;
        fCurrentCDATASection=null;
        fCurrentEntityDecl=null;
        fRoot=null;
    } // dropDocumentReferences()

    public void setLocale(Locale locale){
        fConfiguration.setLocale(locale);
    } // setLocale(Locale)
    //
    // XMLDocumentParser methods
    //

    public void startDocument(XMLLocator locator,String encoding,
                              NamespaceContext namespaceContext,Augmentations augs)
            throws XNIException{
        fLocator=locator;
        if(!fDeferNodeExpansion){
            if(fDocumentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)){
                fDocument=new DocumentImpl();
                fDocumentImpl=(CoreDocumentImpl)fDocument;
                // REVISIT: when DOM Level 3 is REC rely on Document.support
                //          instead of specific class
                // set DOM error checking off
                fDocumentImpl.setStrictErrorChecking(false);
                // set actual encoding
                fDocumentImpl.setInputEncoding(encoding);
                // set documentURI
                fDocumentImpl.setDocumentURI(locator.getExpandedSystemId());
            }else if(fDocumentClassName.equals(PSVI_DOCUMENT_CLASS_NAME)){
                fDocument=new PSVIDocumentImpl();
                fDocumentImpl=(CoreDocumentImpl)fDocument;
                fStorePSVI=true;
                // REVISIT: when DOM Level 3 is REC rely on Document.support
                //          instead of specific class
                // set DOM error checking off
                fDocumentImpl.setStrictErrorChecking(false);
                // set actual encoding
                fDocumentImpl.setInputEncoding(encoding);
                // set documentURI
                fDocumentImpl.setDocumentURI(locator.getExpandedSystemId());
            }else{
                // use specified document class
                try{
                    Class documentClass=ObjectFactory.findProviderClass(fDocumentClassName,true);
                    fDocument=(Document)documentClass.newInstance();
                    // if subclass of our own class that's cool too
                    Class defaultDocClass=
                            ObjectFactory.findProviderClass(CORE_DOCUMENT_CLASS_NAME,true);
                    if(defaultDocClass.isAssignableFrom(documentClass)){
                        fDocumentImpl=(CoreDocumentImpl)fDocument;
                        Class psviDocClass=ObjectFactory.findProviderClass(PSVI_DOCUMENT_CLASS_NAME,true);
                        if(psviDocClass.isAssignableFrom(documentClass)){
                            fStorePSVI=true;
                        }
                        // REVISIT: when DOM Level 3 is REC rely on
                        //          Document.support instead of specific class
                        // set DOM error checking off
                        fDocumentImpl.setStrictErrorChecking(false);
                        // set actual encoding
                        fDocumentImpl.setInputEncoding(encoding);
                        // set documentURI
                        if(locator!=null){
                            fDocumentImpl.setDocumentURI(locator.getExpandedSystemId());
                        }
                    }
                }catch(ClassNotFoundException e){
                    // won't happen we already checked that earlier
                }catch(Exception e){
                    throw new RuntimeException(
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "CannotCreateDocumentClass",
                                    new Object[]{fDocumentClassName}));
                }
            }
            fCurrentNode=fDocument;
        }else{
            fDeferredDocumentImpl=new DeferredDocumentImpl(fNamespaceAware);
            fDocument=fDeferredDocumentImpl;
            fDocumentIndex=fDeferredDocumentImpl.createDeferredDocument();
            // REVISIT: strict error checking is not implemented in deferred dom.
            //          Document.support instead of specific class
            // set actual encoding
            fDeferredDocumentImpl.setInputEncoding(encoding);
            // set documentURI
            fDeferredDocumentImpl.setDocumentURI(locator.getExpandedSystemId());
            fCurrentNodeIndex=fDocumentIndex;
        }
    } // startDocument(String,String)

    public void xmlDecl(String version,String encoding,String standalone,
                        Augmentations augs)
            throws XNIException{
        if(!fDeferNodeExpansion){
            // REVISIT: when DOM Level 3 is REC rely on Document.support
            //          instead of specific class
            if(fDocumentImpl!=null){
                if(version!=null)
                    fDocumentImpl.setXmlVersion(version);
                fDocumentImpl.setXmlEncoding(encoding);
                fDocumentImpl.setXmlStandalone("yes".equals(standalone));
            }
        }else{
            if(version!=null)
                fDeferredDocumentImpl.setXmlVersion(version);
            fDeferredDocumentImpl.setXmlEncoding(encoding);
            fDeferredDocumentImpl.setXmlStandalone("yes".equals(standalone));
        }
    } // xmlDecl(String,String,String)
    //
    // XMLDocumentHandler methods
    //

    public void doctypeDecl(String rootElement,
                            String publicId,String systemId,Augmentations augs)
            throws XNIException{
        if(!fDeferNodeExpansion){
            if(fDocumentImpl!=null){
                fDocumentType=fDocumentImpl.createDocumentType(
                        rootElement,publicId,systemId);
                fCurrentNode.appendChild(fDocumentType);
            }
        }else{
            fDocumentTypeIndex=fDeferredDocumentImpl.
                    createDeferredDocumentType(rootElement,publicId,systemId);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,fDocumentTypeIndex);
        }
    } // doctypeDecl(String,String,String)

    public void startElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>startElement ("+element.rawname+")");
        }
        if(!fDeferNodeExpansion){
            if(fFilterReject){
                ++fRejectedElementDepth;
                return;
            }
            Element el=createElementNode(element);
            int attrCount=attributes.getLength();
            boolean seenSchemaDefault=false;
            for(int i=0;i<attrCount;i++){
                attributes.getName(i,fAttrQName);
                Attr attr=createAttrNode(fAttrQName);
                String attrValue=attributes.getValue(i);
                AttributePSVI attrPSVI=(AttributePSVI)attributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
                if(fStorePSVI&&attrPSVI!=null){
                    ((PSVIAttrNSImpl)attr).setPSVI(attrPSVI);
                }
                attr.setValue(attrValue);
                boolean specified=attributes.isSpecified(i);
                // Take special care of schema defaulted attributes. Calling the
                // non-namespace aware setAttributeNode() method could overwrite
                // another attribute with the same local name.
                if(!specified&&(seenSchemaDefault||(fAttrQName.uri!=null&&
                        fAttrQName.uri!=NamespaceContext.XMLNS_URI&&fAttrQName.prefix==null))){
                    el.setAttributeNodeNS(attr);
                    seenSchemaDefault=true;
                }else{
                    el.setAttributeNode(attr);
                }
                // NOTE: The specified value MUST be set after you set
                //       the node value because that turns the "specified"
                //       flag to "true" which may overwrite a "false"
                //       value from the attribute list. -Ac
                if(fDocumentImpl!=null){
                    AttrImpl attrImpl=(AttrImpl)attr;
                    Object type=null;
                    boolean id=false;
                    // REVISIT: currently it is possible that someone turns off
                    // namespaces and turns on xml schema validation
                    // To avoid classcast exception in AttrImpl check for namespaces
                    // however the correct solution should probably disallow setting
                    // namespaces to false when schema processing is turned on.
                    if(attrPSVI!=null&&fNamespaceAware){
                        // XML Schema
                        type=attrPSVI.getMemberTypeDefinition();
                        if(type==null){
                            type=attrPSVI.getTypeDefinition();
                            if(type!=null){
                                id=((XSSimpleType)type).isIDType();
                                attrImpl.setType(type);
                            }
                        }else{
                            id=((XSSimpleType)type).isIDType();
                            attrImpl.setType(type);
                        }
                    }else{
                        // DTD
                        boolean isDeclared=Boolean.TRUE.equals(attributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_DECLARED));
                        // For DOM Level 3 TypeInfo, the type name must
                        // be null if this attribute has not been declared
                        // in the DTD.
                        if(isDeclared){
                            type=attributes.getType(i);
                            id="ID".equals(type);
                        }
                        attrImpl.setType(type);
                    }
                    if(id){
                        ((ElementImpl)el).setIdAttributeNode(attr,true);
                    }
                    attrImpl.setSpecified(specified);
                    // REVISIT: Handle entities in attribute value.
                }
            }
            setCharacterData(false);
            if(augs!=null){
                ElementPSVI elementPSVI=(ElementPSVI)augs.getItem(Constants.ELEMENT_PSVI);
                if(elementPSVI!=null&&fNamespaceAware){
                    XSTypeDefinition type=elementPSVI.getMemberTypeDefinition();
                    if(type==null){
                        type=elementPSVI.getTypeDefinition();
                    }
                    ((ElementNSImpl)el).setType(type);
                }
            }
            // filter nodes
            if(fDOMFilter!=null&&!fInEntityRef){
                if(fRoot==null){
                    // fill value of the root element
                    fRoot=el;
                }else{
                    short code=fDOMFilter.startElement(el);
                    switch(code){
                        case LSParserFilter.FILTER_INTERRUPT:{
                            throw Abort.INSTANCE;
                        }
                        case LSParserFilter.FILTER_REJECT:{
                            fFilterReject=true;
                            fRejectedElementDepth=0;
                            return;
                        }
                        case LSParserFilter.FILTER_SKIP:{
                            // make sure that if any char data is available
                            // the fFirstChunk is true, so that if the next event
                            // is characters(), and the last node is text, we will copy
                            // the value already in the text node to fStringBuffer
                            // (not to lose it).
                            fFirstChunk=true;
                            fSkippedElemStack.push(Boolean.TRUE);
                            return;
                        }
                        default:{
                            if(!fSkippedElemStack.isEmpty()){
                                fSkippedElemStack.push(Boolean.FALSE);
                            }
                        }
                    }
                }
            }
            fCurrentNode.appendChild(el);
            fCurrentNode=el;
        }else{
            int el=fDeferredDocumentImpl.createDeferredElement(fNamespaceAware?
                    element.uri:null,element.rawname);
            Object type=null;
            int attrCount=attributes.getLength();
            // Need to loop in reverse order so that the attributes
            // are processed in document order when the DOM is expanded.
            for(int i=attrCount-1;i>=0;--i){
                // set type information
                AttributePSVI attrPSVI=(AttributePSVI)attributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
                boolean id=false;
                // REVISIT: currently it is possible that someone turns off
                // namespaces and turns on xml schema validation
                // To avoid classcast exception in AttrImpl check for namespaces
                // however the correct solution should probably disallow setting
                // namespaces to false when schema processing is turned on.
                if(attrPSVI!=null&&fNamespaceAware){
                    // XML Schema
                    type=attrPSVI.getMemberTypeDefinition();
                    if(type==null){
                        type=attrPSVI.getTypeDefinition();
                        if(type!=null){
                            id=((XSSimpleType)type).isIDType();
                        }
                    }else{
                        id=((XSSimpleType)type).isIDType();
                    }
                }else{
                    // DTD
                    boolean isDeclared=Boolean.TRUE.equals(attributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_DECLARED));
                    // For DOM Level 3 TypeInfo, the type name must
                    // be null if this attribute has not been declared
                    // in the DTD.
                    if(isDeclared){
                        type=attributes.getType(i);
                        id="ID".equals(type);
                    }
                }
                // create attribute
                fDeferredDocumentImpl.setDeferredAttribute(
                        el,
                        attributes.getQName(i),
                        attributes.getURI(i),
                        attributes.getValue(i),
                        attributes.isSpecified(i),
                        id,
                        type);
            }
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,el);
            fCurrentNodeIndex=el;
        }
    } // startElement(QName,XMLAttributes)

    public void emptyElement(QName element,XMLAttributes attributes,Augmentations augs)
            throws XNIException{
        startElement(element,attributes,augs);
        endElement(element,augs);
    } // emptyElement(QName,XMLAttributes)

    public void characters(XMLString text,Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>characters(): "+text.toString());
        }
        if(!fDeferNodeExpansion){
            if(fFilterReject){
                return;
            }
            if(fInCDATASection&&fCreateCDATANodes){
                if(fCurrentCDATASection==null){
                    fCurrentCDATASection=
                            fDocument.createCDATASection(text.toString());
                    fCurrentNode.appendChild(fCurrentCDATASection);
                    fCurrentNode=fCurrentCDATASection;
                }else{
                    fCurrentCDATASection.appendData(text.toString());
                }
            }else if(!fInDTD){
                // if type is union (XML Schema) it is possible that we receive
                // character call with empty data
                if(text.length==0){
                    return;
                }
                Node child=fCurrentNode.getLastChild();
                if(child!=null&&child.getNodeType()==Node.TEXT_NODE){
                    // collect all the data into the string buffer.
                    if(fFirstChunk){
                        if(fDocumentImpl!=null){
                            fStringBuilder.append(((TextImpl)child).removeData());
                        }else{
                            fStringBuilder.append(((Text)child).getData());
                            ((Text)child).setNodeValue(null);
                        }
                        fFirstChunk=false;
                    }
                    if(text.length>0){
                        fStringBuilder.append(text.ch,text.offset,text.length);
                    }
                }else{
                    fFirstChunk=true;
                    Text textNode=fDocument.createTextNode(text.toString());
                    fCurrentNode.appendChild(textNode);
                }
            }
        }else{
            // The Text and CDATASection normalization is taken care of within
            // the DOM in the deferred case.
            if(fInCDATASection&&fCreateCDATANodes){
                if(fCurrentCDATASectionIndex==-1){
                    int cs=fDeferredDocumentImpl.
                            createDeferredCDATASection(text.toString());
                    fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,cs);
                    fCurrentCDATASectionIndex=cs;
                    fCurrentNodeIndex=cs;
                }else{
                    int txt=fDeferredDocumentImpl.
                            createDeferredTextNode(text.toString(),false);
                    fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,txt);
                }
            }else if(!fInDTD){
                // if type is union (XML Schema) it is possible that we receive
                // character call with empty data
                if(text.length==0){
                    return;
                }
                String value=text.toString();
                int txt=fDeferredDocumentImpl.
                        createDeferredTextNode(value,false);
                fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,txt);
            }
        }
    } // characters(XMLString)

    public void ignorableWhitespace(XMLString text,Augmentations augs) throws XNIException{
        if(!fIncludeIgnorableWhitespace||fFilterReject){
            return;
        }
        if(!fDeferNodeExpansion){
            Node child=fCurrentNode.getLastChild();
            if(child!=null&&child.getNodeType()==Node.TEXT_NODE){
                Text textNode=(Text)child;
                textNode.appendData(text.toString());
            }else{
                Text textNode=fDocument.createTextNode(text.toString());
                if(fDocumentImpl!=null){
                    TextImpl textNodeImpl=(TextImpl)textNode;
                    textNodeImpl.setIgnorableWhitespace(true);
                }
                fCurrentNode.appendChild(textNode);
            }
        }else{
            // The Text normalization is taken care of within the DOM in the
            // deferred case.
            int txt=fDeferredDocumentImpl.
                    createDeferredTextNode(text.toString(),true);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,txt);
        }
    } // ignorableWhitespace(XMLString)

    public void endElement(QName element,Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>endElement ("+element.rawname+")");
        }
        if(!fDeferNodeExpansion){
            // REVISIT: Should this happen after we call the filter?
            if(augs!=null&&fDocumentImpl!=null&&(fNamespaceAware||fStorePSVI)){
                ElementPSVI elementPSVI=(ElementPSVI)augs.getItem(Constants.ELEMENT_PSVI);
                if(elementPSVI!=null){
                    // Updating TypeInfo. If the declared type is a union the
                    // [member type definition] will only be available at the
                    // end of an element.
                    if(fNamespaceAware){
                        XSTypeDefinition type=elementPSVI.getMemberTypeDefinition();
                        if(type==null){
                            type=elementPSVI.getTypeDefinition();
                        }
                        ((ElementNSImpl)fCurrentNode).setType(type);
                    }
                    if(fStorePSVI){
                        ((PSVIElementNSImpl)fCurrentNode).setPSVI(elementPSVI);
                    }
                }
            }
            if(fDOMFilter!=null){
                if(fFilterReject){
                    if(fRejectedElementDepth--==0){
                        fFilterReject=false;
                    }
                    return;
                }
                if(!fSkippedElemStack.isEmpty()){
                    if(fSkippedElemStack.pop()==Boolean.TRUE){
                        return;
                    }
                }
                setCharacterData(false);
                if((fCurrentNode!=fRoot)&&!fInEntityRef&&(fDOMFilter.getWhatToShow()&NodeFilter.SHOW_ELEMENT)!=0){
                    short code=fDOMFilter.acceptNode(fCurrentNode);
                    switch(code){
                        case LSParserFilter.FILTER_INTERRUPT:{
                            throw Abort.INSTANCE;
                        }
                        case LSParserFilter.FILTER_REJECT:{
                            Node parent=fCurrentNode.getParentNode();
                            parent.removeChild(fCurrentNode);
                            fCurrentNode=parent;
                            return;
                        }
                        case LSParserFilter.FILTER_SKIP:{
                            // make sure that if any char data is available
                            // the fFirstChunk is true, so that if the next event
                            // is characters(), and the last node is text, we will copy
                            // the value already in the text node to fStringBuffer
                            // (not to lose it).
                            fFirstChunk=true;
                            // replace children
                            Node parent=fCurrentNode.getParentNode();
                            NodeList ls=fCurrentNode.getChildNodes();
                            int length=ls.getLength();
                            for(int i=0;i<length;i++){
                                parent.appendChild(ls.item(0));
                            }
                            parent.removeChild(fCurrentNode);
                            fCurrentNode=parent;
                            return;
                        }
                        default:{
                        }
                    }
                }
                fCurrentNode=fCurrentNode.getParentNode();
            } // end-if DOMFilter
            else{
                setCharacterData(false);
                fCurrentNode=fCurrentNode.getParentNode();
            }
        }else{
            if(augs!=null){
                ElementPSVI elementPSVI=(ElementPSVI)augs.getItem(Constants.ELEMENT_PSVI);
                if(elementPSVI!=null){
                    // Setting TypeInfo. If the declared type is a union the
                    // [member type definition] will only be available at the
                    // end of an element.
                    XSTypeDefinition type=elementPSVI.getMemberTypeDefinition();
                    if(type==null){
                        type=elementPSVI.getTypeDefinition();
                    }
                    fDeferredDocumentImpl.setTypeInfo(fCurrentNodeIndex,type);
                }
            }
            fCurrentNodeIndex=
                    fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex,false);
        }
    } // endElement(QName)

    public void startCDATA(Augmentations augs) throws XNIException{
        fInCDATASection=true;
        if(!fDeferNodeExpansion){
            if(fFilterReject){
                return;
            }
            if(fCreateCDATANodes){
                setCharacterData(false);
            }
        }
    } // startCDATA()

    public void endCDATA(Augmentations augs) throws XNIException{
        fInCDATASection=false;
        if(!fDeferNodeExpansion){
            if(fFilterReject){
                return;
            }
            if(fCurrentCDATASection!=null){
                if(fDOMFilter!=null&&!fInEntityRef&&
                        (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_CDATA_SECTION)!=0){
                    short code=fDOMFilter.acceptNode(fCurrentCDATASection);
                    switch(code){
                        case LSParserFilter.FILTER_INTERRUPT:{
                            throw Abort.INSTANCE;
                        }
                        case LSParserFilter.FILTER_REJECT:{
                            // fall through to SKIP since CDATA section has no children.
                        }
                        case LSParserFilter.FILTER_SKIP:{
                            Node parent=fCurrentNode.getParentNode();
                            parent.removeChild(fCurrentCDATASection);
                            fCurrentNode=parent;
                            return;
                        }
                        default:{
                            // accept node
                        }
                    }
                }
                fCurrentNode=fCurrentNode.getParentNode();
                fCurrentCDATASection=null;
            }
        }else{
            if(fCurrentCDATASectionIndex!=-1){
                fCurrentNodeIndex=
                        fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex,false);
                fCurrentCDATASectionIndex=-1;
            }
        }
    } // endCDATA()

    public void endDocument(Augmentations augs) throws XNIException{
        if(!fDeferNodeExpansion){
            // REVISIT: when DOM Level 3 is REC rely on Document.support
            //          instead of specific class
            // set the actual encoding and set DOM error checking back on
            if(fDocumentImpl!=null){
                if(fLocator!=null){
                    if(fLocator.getEncoding()!=null)
                        fDocumentImpl.setInputEncoding(fLocator.getEncoding());
                }
                fDocumentImpl.setStrictErrorChecking(true);
            }
            fCurrentNode=null;
        }else{
            // set the actual encoding
            if(fLocator!=null){
                if(fLocator.getEncoding()!=null)
                    fDeferredDocumentImpl.setInputEncoding(fLocator.getEncoding());
            }
            fCurrentNodeIndex=-1;
        }
    } // endDocument()

    public void startGeneralEntity(String name,
                                   XMLResourceIdentifier identifier,
                                   String encoding,Augmentations augs)
            throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>startGeneralEntity ("+name+")");
            if(DEBUG_BASEURI){
                System.out.println("   expandedSystemId( **baseURI): "+identifier.getExpandedSystemId());
                System.out.println("   baseURI:"+identifier.getBaseSystemId());
            }
        }
        // Always create entity reference nodes to be able to recreate
        // entity as a part of doctype
        if(!fDeferNodeExpansion){
            if(fFilterReject){
                return;
            }
            setCharacterData(true);
            EntityReference er=fDocument.createEntityReference(name);
            if(fDocumentImpl!=null){
                // REVISIT: baseURI/actualEncoding
                //         remove dependency on our implementation when DOM L3 is REC
                //
                EntityReferenceImpl erImpl=(EntityReferenceImpl)er;
                // set base uri
                erImpl.setBaseURI(identifier.getExpandedSystemId());
                if(fDocumentType!=null){
                    // set actual encoding
                    NamedNodeMap entities=fDocumentType.getEntities();
                    fCurrentEntityDecl=(EntityImpl)entities.getNamedItem(name);
                    if(fCurrentEntityDecl!=null){
                        fCurrentEntityDecl.setInputEncoding(encoding);
                    }
                }
                // we don't need synchronization now, because entity ref will be
                // expanded anyway. Synch only needed when user creates entityRef node
                erImpl.needsSyncChildren(false);
            }
            fInEntityRef=true;
            fCurrentNode.appendChild(er);
            fCurrentNode=er;
        }else{
            int er=
                    fDeferredDocumentImpl.createDeferredEntityReference(name,identifier.getExpandedSystemId());
            if(fDocumentTypeIndex!=-1){
                // find corresponding Entity decl
                int node=fDeferredDocumentImpl.getLastChild(fDocumentTypeIndex,false);
                while(node!=-1){
                    short nodeType=fDeferredDocumentImpl.getNodeType(node,false);
                    if(nodeType==Node.ENTITY_NODE){
                        String nodeName=
                                fDeferredDocumentImpl.getNodeName(node,false);
                        if(nodeName.equals(name)){
                            fDeferredEntityDecl=node;
                            fDeferredDocumentImpl.setInputEncoding(node,encoding);
                            break;
                        }
                    }
                    node=fDeferredDocumentImpl.getRealPrevSibling(node,false);
                }
            }
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,er);
            fCurrentNodeIndex=er;
        }
    } // startGeneralEntity(String,XMLResourceIdentifier, Augmentations)

    public void textDecl(String version,String encoding,Augmentations augs) throws XNIException{
        if(fInDTD){
            return;
        }
        if(!fDeferNodeExpansion){
            if(fCurrentEntityDecl!=null&&!fFilterReject){
                fCurrentEntityDecl.setXmlEncoding(encoding);
                if(version!=null)
                    fCurrentEntityDecl.setXmlVersion(version);
            }
        }else{
            if(fDeferredEntityDecl!=-1){
                fDeferredDocumentImpl.setEntityInfo(fDeferredEntityDecl,version,encoding);
            }
        }
    } // textDecl(String,String)

    public void endGeneralEntity(String name,Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>endGeneralEntity: ("+name+")");
        }
        if(!fDeferNodeExpansion){
            if(fFilterReject){
                return;
            }
            setCharacterData(true);
            if(fDocumentType!=null){
                // get current entity declaration
                NamedNodeMap entities=fDocumentType.getEntities();
                fCurrentEntityDecl=(EntityImpl)entities.getNamedItem(name);
                if(fCurrentEntityDecl!=null){
                    if(fCurrentEntityDecl!=null&&fCurrentEntityDecl.getFirstChild()==null){
                        fCurrentEntityDecl.setReadOnly(false,true);
                        Node child=fCurrentNode.getFirstChild();
                        while(child!=null){
                            Node copy=child.cloneNode(true);
                            fCurrentEntityDecl.appendChild(copy);
                            child=child.getNextSibling();
                        }
                        fCurrentEntityDecl.setReadOnly(true,true);
                        //entities.setNamedItem(fCurrentEntityDecl);
                    }
                    fCurrentEntityDecl=null;
                }
            }
            fInEntityRef=false;
            boolean removeEntityRef=false;
            if(fCreateEntityRefNodes){
                if(fDocumentImpl!=null){
                    // Make entity ref node read only
                    ((NodeImpl)fCurrentNode).setReadOnly(true,true);
                }
                if(fDOMFilter!=null&&
                        (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_ENTITY_REFERENCE)!=0){
                    short code=fDOMFilter.acceptNode(fCurrentNode);
                    switch(code){
                        case LSParserFilter.FILTER_INTERRUPT:{
                            throw Abort.INSTANCE;
                        }
                        case LSParserFilter.FILTER_REJECT:{
                            Node parent=fCurrentNode.getParentNode();
                            parent.removeChild(fCurrentNode);
                            fCurrentNode=parent;
                            return;
                        }
                        case LSParserFilter.FILTER_SKIP:{
                            // make sure we don't loose chars if next event is characters()
                            fFirstChunk=true;
                            removeEntityRef=true;
                            break;
                        }
                        default:{
                            fCurrentNode=fCurrentNode.getParentNode();
                        }
                    }
                }else{
                    fCurrentNode=fCurrentNode.getParentNode();
                }
            }
            if(!fCreateEntityRefNodes||removeEntityRef){
                // move entity reference children to the list of
                // siblings of its parent and remove entity reference
                NodeList children=fCurrentNode.getChildNodes();
                Node parent=fCurrentNode.getParentNode();
                int length=children.getLength();
                if(length>0){
                    // get previous sibling of the entity reference
                    Node node=fCurrentNode.getPreviousSibling();
                    // normalize text nodes
                    Node child=children.item(0);
                    if(node!=null&&node.getNodeType()==Node.TEXT_NODE&&
                            child.getNodeType()==Node.TEXT_NODE){
                        ((Text)node).appendData(child.getNodeValue());
                        fCurrentNode.removeChild(child);
                    }else{
                        node=parent.insertBefore(child,fCurrentNode);
                        handleBaseURI(node);
                    }
                    for(int i=1;i<length;i++){
                        node=parent.insertBefore(children.item(0),fCurrentNode);
                        handleBaseURI(node);
                    }
                } // length > 0
                parent.removeChild(fCurrentNode);
                fCurrentNode=parent;
            }
        }else{
            if(fDocumentTypeIndex!=-1){
                // find corresponding Entity decl
                int node=fDeferredDocumentImpl.getLastChild(fDocumentTypeIndex,false);
                while(node!=-1){
                    short nodeType=fDeferredDocumentImpl.getNodeType(node,false);
                    if(nodeType==Node.ENTITY_NODE){
                        String nodeName=
                                fDeferredDocumentImpl.getNodeName(node,false);
                        if(nodeName.equals(name)){
                            fDeferredEntityDecl=node;
                            break;
                        }
                    }
                    node=fDeferredDocumentImpl.getRealPrevSibling(node,false);
                }
            }
            if(fDeferredEntityDecl!=-1&&
                    fDeferredDocumentImpl.getLastChild(fDeferredEntityDecl,false)==-1){
                // entity definition exists and it does not have any children
                int prevIndex=-1;
                int childIndex=fDeferredDocumentImpl.getLastChild(fCurrentNodeIndex,false);
                while(childIndex!=-1){
                    int cloneIndex=fDeferredDocumentImpl.cloneNode(childIndex,true);
                    fDeferredDocumentImpl.insertBefore(fDeferredEntityDecl,cloneIndex,prevIndex);
                    prevIndex=cloneIndex;
                    childIndex=fDeferredDocumentImpl.getRealPrevSibling(childIndex,false);
                }
            }
            if(fCreateEntityRefNodes){
                fCurrentNodeIndex=
                        fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex,
                                false);
            }else{ //!fCreateEntityRefNodes
                // move children of entity ref before the entity ref.
                // remove entity ref.
                // holds a child of entity ref
                int childIndex=fDeferredDocumentImpl.getLastChild(fCurrentNodeIndex,false);
                int parentIndex=
                        fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex,
                                false);
                int prevIndex=fCurrentNodeIndex;
                int lastChild=childIndex;
                int sibling=-1;
                while(childIndex!=-1){
                    handleBaseURI(childIndex);
                    sibling=fDeferredDocumentImpl.getRealPrevSibling(childIndex,false);
                    fDeferredDocumentImpl.insertBefore(parentIndex,childIndex,prevIndex);
                    prevIndex=childIndex;
                    childIndex=sibling;
                }
                if(lastChild!=-1)
                    fDeferredDocumentImpl.setAsLastChild(parentIndex,lastChild);
                else{
                    sibling=fDeferredDocumentImpl.getRealPrevSibling(prevIndex,false);
                    fDeferredDocumentImpl.setAsLastChild(parentIndex,sibling);
                }
                fCurrentNodeIndex=parentIndex;
            }
            fDeferredEntityDecl=-1;
        }
    } // endGeneralEntity(String, Augmentations)

    public void comment(XMLString text,Augmentations augs) throws XNIException{
        if(fInDTD){
            if(fInternalSubset!=null&&!fInDTDExternalSubset){
                fInternalSubset.append("<!--");
                if(text.length>0){
                    fInternalSubset.append(text.ch,text.offset,text.length);
                }
                fInternalSubset.append("-->");
            }
            return;
        }
        if(!fIncludeComments||fFilterReject){
            return;
        }
        if(!fDeferNodeExpansion){
            Comment comment=fDocument.createComment(text.toString());
            setCharacterData(false);
            fCurrentNode.appendChild(comment);
            if(fDOMFilter!=null&&!fInEntityRef&&
                    (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_COMMENT)!=0){
                short code=fDOMFilter.acceptNode(comment);
                switch(code){
                    case LSParserFilter.FILTER_INTERRUPT:{
                        throw Abort.INSTANCE;
                    }
                    case LSParserFilter.FILTER_REJECT:{
                        // REVISIT: the constant FILTER_REJECT should be changed when new
                        // DOM LS specs gets published
                        // fall through to SKIP since comment has no children.
                    }
                    case LSParserFilter.FILTER_SKIP:{
                        // REVISIT: the constant FILTER_SKIP should be changed when new
                        // DOM LS specs gets published
                        fCurrentNode.removeChild(comment);
                        // make sure we don't loose chars if next event is characters()
                        fFirstChunk=true;
                        return;
                    }
                    default:{
                        // accept node
                    }
                }
            }
        }else{
            int comment=
                    fDeferredDocumentImpl.createDeferredComment(text.toString());
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,comment);
        }
    } // comment(XMLString)

    public void processingInstruction(String target,XMLString data,Augmentations augs)
            throws XNIException{
        if(fInDTD){
            if(fInternalSubset!=null&&!fInDTDExternalSubset){
                fInternalSubset.append("<?");
                fInternalSubset.append(target);
                if(data.length>0){
                    fInternalSubset.append(' ').append(data.ch,data.offset,data.length);
                }
                fInternalSubset.append("?>");
            }
            return;
        }
        if(DEBUG_EVENTS){
            System.out.println("==>processingInstruction ("+target+")");
        }
        if(!fDeferNodeExpansion){
            if(fFilterReject){
                return;
            }
            ProcessingInstruction pi=
                    fDocument.createProcessingInstruction(target,data.toString());
            setCharacterData(false);
            fCurrentNode.appendChild(pi);
            if(fDOMFilter!=null&&!fInEntityRef&&
                    (fDOMFilter.getWhatToShow()&NodeFilter.SHOW_PROCESSING_INSTRUCTION)!=0){
                short code=fDOMFilter.acceptNode(pi);
                switch(code){
                    case LSParserFilter.FILTER_INTERRUPT:{
                        throw Abort.INSTANCE;
                    }
                    case LSParserFilter.FILTER_REJECT:{
                        // fall through to SKIP since PI has no children.
                    }
                    case LSParserFilter.FILTER_SKIP:{
                        fCurrentNode.removeChild(pi);
                        // fFirstChunk must be set to true so that data
                        // won't be lost in the case where the child before PI is
                        // a text node and the next event is characters.
                        fFirstChunk=true;
                        return;
                    }
                    default:{
                    }
                }
            }
        }else{
            int pi=fDeferredDocumentImpl.
                    createDeferredProcessingInstruction(target,data.toString());
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex,pi);
        }
    } // processingInstruction(String,XMLString)

    public void startDTD(XMLLocator locator,Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>startDTD");
            if(DEBUG_BASEURI){
                System.out.println("   expandedSystemId: "+locator.getExpandedSystemId());
                System.out.println("   baseURI:"+locator.getBaseSystemId());
            }
        }
        fInDTD=true;
        if(locator!=null){
            fBaseURIStack.push(locator.getBaseSystemId());
        }
        if(fDeferNodeExpansion||fDocumentImpl!=null){
            fInternalSubset=new StringBuilder(1024);
        }
    } // startDTD(XMLLocator)

    public void startExternalSubset(XMLResourceIdentifier identifier,
                                    Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>startExternalSubset");
            if(DEBUG_BASEURI){
                System.out.println("   expandedSystemId: "+identifier.getExpandedSystemId());
                System.out.println("   baseURI:"+identifier.getBaseSystemId());
            }
        }
        fBaseURIStack.push(identifier.getBaseSystemId());
        fInDTDExternalSubset=true;
    } // startExternalSubset(Augmentations)

    public void endExternalSubset(Augmentations augs) throws XNIException{
        fInDTDExternalSubset=false;
        fBaseURIStack.pop();
    } // endExternalSubset(Augmentations)

    public void startParameterEntity(String name,
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>startParameterEntity: "+name);
            if(DEBUG_BASEURI){
                System.out.println("   expandedSystemId: "+identifier.getExpandedSystemId());
                System.out.println("   baseURI:"+identifier.getBaseSystemId());
            }
        }
        if(augs!=null&&fInternalSubset!=null&&
                !fInDTDExternalSubset&&
                Boolean.TRUE.equals(augs.getItem(Constants.ENTITY_SKIPPED))){
            fInternalSubset.append(name).append(";\n");
        }
        fBaseURIStack.push(identifier.getExpandedSystemId());
    }
    //
    // XMLDTDHandler methods
    //

    public void endParameterEntity(String name,Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>endParameterEntity: "+name);
        }
        fBaseURIStack.pop();
    }

    public void ignoredCharacters(XMLString text,Augmentations augs) throws XNIException{
    } // ignoredCharacters(XMLString, Augmentations)

    public void elementDecl(String name,String contentModel,Augmentations augs)
            throws XNIException{
        // internal subset string
        if(fInternalSubset!=null&&!fInDTDExternalSubset){
            fInternalSubset.append("<!ELEMENT ");
            fInternalSubset.append(name);
            fInternalSubset.append(' ');
            fInternalSubset.append(contentModel);
            fInternalSubset.append(">\n");
        }
    } // elementDecl(String,String)

    public void startAttlist(String elementName,Augmentations augs) throws XNIException{
    } // startAttlist(String)

    public void attributeDecl(String elementName,String attributeName,
                              String type,String[] enumeration,
                              String defaultType,XMLString defaultValue,
                              XMLString nonNormalizedDefaultValue,Augmentations augs) throws XNIException{
        // internal subset string
        if(fInternalSubset!=null&&!fInDTDExternalSubset){
            fInternalSubset.append("<!ATTLIST ");
            fInternalSubset.append(elementName);
            fInternalSubset.append(' ');
            fInternalSubset.append(attributeName);
            fInternalSubset.append(' ');
            if(type.equals("ENUMERATION")){
                fInternalSubset.append('(');
                for(int i=0;i<enumeration.length;i++){
                    if(i>0){
                        fInternalSubset.append('|');
                    }
                    fInternalSubset.append(enumeration[i]);
                }
                fInternalSubset.append(')');
            }else{
                fInternalSubset.append(type);
            }
            if(defaultType!=null){
                fInternalSubset.append(' ');
                fInternalSubset.append(defaultType);
            }
            if(defaultValue!=null){
                fInternalSubset.append(" '");
                for(int i=0;i<defaultValue.length;i++){
                    char c=defaultValue.ch[defaultValue.offset+i];
                    if(c=='\''){
                        fInternalSubset.append("&apos;");
                    }else{
                        fInternalSubset.append(c);
                    }
                }
                fInternalSubset.append('\'');
            }
            fInternalSubset.append(">\n");
        }
        // REVISIT: This code applies to the support of domx/grammar-access
        // feature in Xerces 1
        // deferred expansion
        if(fDeferredDocumentImpl!=null){
            // get the default value
            if(defaultValue!=null){
                // get element definition
                int elementDefIndex=fDeferredDocumentImpl.lookupElementDefinition(elementName);
                // create element definition if not already there
                if(elementDefIndex==-1){
                    elementDefIndex=fDeferredDocumentImpl.createDeferredElementDefinition(elementName);
                    fDeferredDocumentImpl.appendChild(fDocumentTypeIndex,elementDefIndex);
                }
                // add default attribute
                boolean nsEnabled=fNamespaceAware;
                String namespaceURI=null;
                if(nsEnabled){
                    // DOM Level 2 wants all namespace declaration attributes
                    // to be bound to "http://www.w3.org/2000/xmlns/"
                    // So as long as the XML parser doesn't do it, it needs to
                    // done here.
                    if(attributeName.startsWith("xmlns:")||
                            attributeName.equals("xmlns")){
                        namespaceURI=NamespaceContext.XMLNS_URI;
                    }else if(attributeName.startsWith("xml:")){
                        namespaceURI=NamespaceContext.XML_URI;
                    }
                }
                int attrIndex=fDeferredDocumentImpl.createDeferredAttribute(
                        attributeName,namespaceURI,defaultValue.toString(),false);
                if("ID".equals(type)){
                    fDeferredDocumentImpl.setIdAttribute(attrIndex);
                }
                // REVISIT: set ID type correctly
                fDeferredDocumentImpl.appendChild(elementDefIndex,attrIndex);
            }
        } // if deferred
        // full expansion
        else if(fDocumentImpl!=null){
            // get the default value
            if(defaultValue!=null){
                // get element definition node
                NamedNodeMap elements=((DocumentTypeImpl)fDocumentType).getElements();
                ElementDefinitionImpl elementDef=(ElementDefinitionImpl)elements.getNamedItem(elementName);
                if(elementDef==null){
                    elementDef=fDocumentImpl.createElementDefinition(elementName);
                    ((DocumentTypeImpl)fDocumentType).getElements().setNamedItem(elementDef);
                }
                // REVISIT: Check for uniqueness of element name? -Ac
                // create attribute and set properties
                boolean nsEnabled=fNamespaceAware;
                AttrImpl attr;
                if(nsEnabled){
                    String namespaceURI=null;
                    // DOM Level 2 wants all namespace declaration attributes
                    // to be bound to "http://www.w3.org/2000/xmlns/"
                    // So as long as the XML parser doesn't do it, it needs to
                    // done here.
                    if(attributeName.startsWith("xmlns:")||
                            attributeName.equals("xmlns")){
                        namespaceURI=NamespaceContext.XMLNS_URI;
                    }else if(attributeName.startsWith("xml:")){
                        namespaceURI=NamespaceContext.XML_URI;
                    }
                    attr=(AttrImpl)fDocumentImpl.createAttributeNS(namespaceURI,
                            attributeName);
                }else{
                    attr=(AttrImpl)fDocumentImpl.createAttribute(attributeName);
                }
                attr.setValue(defaultValue.toString());
                attr.setSpecified(false);
                attr.setIdAttribute("ID".equals(type));
                // add default attribute to element definition
                if(nsEnabled){
                    elementDef.getAttributes().setNamedItemNS(attr);
                }else{
                    elementDef.getAttributes().setNamedItem(attr);
                }
            }
        } // if NOT defer-node-expansion
    } // attributeDecl(String,String,String,String[],String,XMLString, XMLString, Augmentations)

    public void endAttlist(Augmentations augs) throws XNIException{
    } // endAttlist()

    public void internalEntityDecl(String name,XMLString text,
                                   XMLString nonNormalizedText,
                                   Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>internalEntityDecl: "+name);
            if(DEBUG_BASEURI){
                System.out.println("   baseURI:"+(String)fBaseURIStack.peek());
            }
        }
        // internal subset string
        if(fInternalSubset!=null&&!fInDTDExternalSubset){
            fInternalSubset.append("<!ENTITY ");
            if(name.startsWith("%")){
                fInternalSubset.append("% ");
                fInternalSubset.append(name.substring(1));
            }else{
                fInternalSubset.append(name);
            }
            fInternalSubset.append(' ');
            String value=nonNormalizedText.toString();
            boolean singleQuote=value.indexOf('\'')==-1;
            fInternalSubset.append(singleQuote?'\'':'"');
            fInternalSubset.append(value);
            fInternalSubset.append(singleQuote?'\'':'"');
            fInternalSubset.append(">\n");
        }
        // NOTE: We only know how to create these nodes for the Xerces
        //       DOM implementation because DOM Level 2 does not specify
        //       that functionality. -Ac
        // create full node
        // don't add parameter entities!
        if(name.startsWith("%"))
            return;
        if(fDocumentType!=null){
            NamedNodeMap entities=fDocumentType.getEntities();
            EntityImpl entity=(EntityImpl)entities.getNamedItem(name);
            if(entity==null){
                entity=(EntityImpl)fDocumentImpl.createEntity(name);
                entity.setBaseURI((String)fBaseURIStack.peek());
                entities.setNamedItem(entity);
            }
        }
        // create deferred node
        if(fDocumentTypeIndex!=-1){
            boolean found=false;
            int node=fDeferredDocumentImpl.getLastChild(fDocumentTypeIndex,false);
            while(node!=-1){
                short nodeType=fDeferredDocumentImpl.getNodeType(node,false);
                if(nodeType==Node.ENTITY_NODE){
                    String nodeName=fDeferredDocumentImpl.getNodeName(node,false);
                    if(nodeName.equals(name)){
                        found=true;
                        break;
                    }
                }
                node=fDeferredDocumentImpl.getRealPrevSibling(node,false);
            }
            if(!found){
                int entityIndex=
                        fDeferredDocumentImpl.createDeferredEntity(name,null,null,null,(String)fBaseURIStack.peek());
                fDeferredDocumentImpl.appendChild(fDocumentTypeIndex,entityIndex);
            }
        }
    } // internalEntityDecl(String,XMLString,XMLString)

    public void externalEntityDecl(String name,XMLResourceIdentifier identifier,
                                   Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>externalEntityDecl: "+name);
            if(DEBUG_BASEURI){
                System.out.println("   expandedSystemId:"+identifier.getExpandedSystemId());
                System.out.println("   baseURI:"+identifier.getBaseSystemId());
            }
        }
        // internal subset string
        String publicId=identifier.getPublicId();
        String literalSystemId=identifier.getLiteralSystemId();
        if(fInternalSubset!=null&&!fInDTDExternalSubset){
            fInternalSubset.append("<!ENTITY ");
            if(name.startsWith("%")){
                fInternalSubset.append("% ");
                fInternalSubset.append(name.substring(1));
            }else{
                fInternalSubset.append(name);
            }
            fInternalSubset.append(' ');
            if(publicId!=null){
                fInternalSubset.append("PUBLIC '");
                fInternalSubset.append(publicId);
                fInternalSubset.append("' '");
            }else{
                fInternalSubset.append("SYSTEM '");
            }
            fInternalSubset.append(literalSystemId);
            fInternalSubset.append("'>\n");
        }
        // NOTE: We only know how to create these nodes for the Xerces
        //       DOM implementation because DOM Level 2 does not specify
        //       that functionality. -Ac
        // create full node
        // don't add parameter entities!
        if(name.startsWith("%"))
            return;
        if(fDocumentType!=null){
            NamedNodeMap entities=fDocumentType.getEntities();
            EntityImpl entity=(EntityImpl)entities.getNamedItem(name);
            if(entity==null){
                entity=(EntityImpl)fDocumentImpl.createEntity(name);
                entity.setPublicId(publicId);
                entity.setSystemId(literalSystemId);
                entity.setBaseURI(identifier.getBaseSystemId());
                entities.setNamedItem(entity);
            }
        }
        // create deferred node
        if(fDocumentTypeIndex!=-1){
            boolean found=false;
            int nodeIndex=fDeferredDocumentImpl.getLastChild(fDocumentTypeIndex,false);
            while(nodeIndex!=-1){
                short nodeType=fDeferredDocumentImpl.getNodeType(nodeIndex,false);
                if(nodeType==Node.ENTITY_NODE){
                    String nodeName=fDeferredDocumentImpl.getNodeName(nodeIndex,false);
                    if(nodeName.equals(name)){
                        found=true;
                        break;
                    }
                }
                nodeIndex=fDeferredDocumentImpl.getRealPrevSibling(nodeIndex,false);
            }
            if(!found){
                int entityIndex=fDeferredDocumentImpl.createDeferredEntity(
                        name,publicId,literalSystemId,null,identifier.getBaseSystemId());
                fDeferredDocumentImpl.appendChild(fDocumentTypeIndex,entityIndex);
            }
        }
    } // externalEntityDecl(String,XMLResourceIdentifier, Augmentations)

    public void unparsedEntityDecl(String name,XMLResourceIdentifier identifier,
                                   String notation,Augmentations augs)
            throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>unparsedEntityDecl: "+name);
            if(DEBUG_BASEURI){
                System.out.println("   expandedSystemId:"+identifier.getExpandedSystemId());
                System.out.println("   baseURI:"+identifier.getBaseSystemId());
            }
        }
        // internal subset string
        String publicId=identifier.getPublicId();
        String literalSystemId=identifier.getLiteralSystemId();
        if(fInternalSubset!=null&&!fInDTDExternalSubset){
            fInternalSubset.append("<!ENTITY ");
            fInternalSubset.append(name);
            fInternalSubset.append(' ');
            if(publicId!=null){
                fInternalSubset.append("PUBLIC '");
                fInternalSubset.append(publicId);
                if(literalSystemId!=null){
                    fInternalSubset.append("' '");
                    fInternalSubset.append(literalSystemId);
                }
            }else{
                fInternalSubset.append("SYSTEM '");
                fInternalSubset.append(literalSystemId);
            }
            fInternalSubset.append("' NDATA ");
            fInternalSubset.append(notation);
            fInternalSubset.append(">\n");
        }
        // NOTE: We only know how to create these nodes for the Xerces
        //       DOM implementation because DOM Level 2 does not specify
        //       that functionality. -Ac
        // create full node
        if(fDocumentType!=null){
            NamedNodeMap entities=fDocumentType.getEntities();
            EntityImpl entity=(EntityImpl)entities.getNamedItem(name);
            if(entity==null){
                entity=(EntityImpl)fDocumentImpl.createEntity(name);
                entity.setPublicId(publicId);
                entity.setSystemId(literalSystemId);
                entity.setNotationName(notation);
                entity.setBaseURI(identifier.getBaseSystemId());
                entities.setNamedItem(entity);
            }
        }
        // create deferred node
        if(fDocumentTypeIndex!=-1){
            boolean found=false;
            int nodeIndex=fDeferredDocumentImpl.getLastChild(fDocumentTypeIndex,false);
            while(nodeIndex!=-1){
                short nodeType=fDeferredDocumentImpl.getNodeType(nodeIndex,false);
                if(nodeType==Node.ENTITY_NODE){
                    String nodeName=fDeferredDocumentImpl.getNodeName(nodeIndex,false);
                    if(nodeName.equals(name)){
                        found=true;
                        break;
                    }
                }
                nodeIndex=fDeferredDocumentImpl.getRealPrevSibling(nodeIndex,false);
            }
            if(!found){
                int entityIndex=fDeferredDocumentImpl.createDeferredEntity(
                        name,publicId,literalSystemId,notation,identifier.getBaseSystemId());
                fDeferredDocumentImpl.appendChild(fDocumentTypeIndex,entityIndex);
            }
        }
    } // unparsedEntityDecl(String,XMLResourceIdentifier, String, Augmentations)

    public void notationDecl(String name,XMLResourceIdentifier identifier,
                             Augmentations augs) throws XNIException{
        // internal subset string
        String publicId=identifier.getPublicId();
        String literalSystemId=identifier.getLiteralSystemId();
        if(fInternalSubset!=null&&!fInDTDExternalSubset){
            fInternalSubset.append("<!NOTATION ");
            fInternalSubset.append(name);
            if(publicId!=null){
                fInternalSubset.append(" PUBLIC '");
                fInternalSubset.append(publicId);
                if(literalSystemId!=null){
                    fInternalSubset.append("' '");
                    fInternalSubset.append(literalSystemId);
                }
            }else{
                fInternalSubset.append(" SYSTEM '");
                fInternalSubset.append(literalSystemId);
            }
            fInternalSubset.append("'>\n");
        }
        // NOTE: We only know how to create these nodes for the Xerces
        //       DOM implementation because DOM Level 2 does not specify
        //       that functionality. -Ac
        // create full node
        if(fDocumentImpl!=null&&fDocumentType!=null){
            NamedNodeMap notations=fDocumentType.getNotations();
            if(notations.getNamedItem(name)==null){
                NotationImpl notation=(NotationImpl)fDocumentImpl.createNotation(name);
                notation.setPublicId(publicId);
                notation.setSystemId(literalSystemId);
                notation.setBaseURI(identifier.getBaseSystemId());
                notations.setNamedItem(notation);
            }
        }
        // create deferred node
        if(fDocumentTypeIndex!=-1){
            boolean found=false;
            int nodeIndex=fDeferredDocumentImpl.getLastChild(fDocumentTypeIndex,false);
            while(nodeIndex!=-1){
                short nodeType=fDeferredDocumentImpl.getNodeType(nodeIndex,false);
                if(nodeType==Node.NOTATION_NODE){
                    String nodeName=fDeferredDocumentImpl.getNodeName(nodeIndex,false);
                    if(nodeName.equals(name)){
                        found=true;
                        break;
                    }
                }
                nodeIndex=fDeferredDocumentImpl.getPrevSibling(nodeIndex,false);
            }
            if(!found){
                int notationIndex=fDeferredDocumentImpl.createDeferredNotation(
                        name,publicId,literalSystemId,identifier.getBaseSystemId());
                fDeferredDocumentImpl.appendChild(fDocumentTypeIndex,notationIndex);
            }
        }
    } // notationDecl(String,XMLResourceIdentifier, Augmentations)

    public void startConditional(short type,Augmentations augs) throws XNIException{
    } // startConditional(short)

    public void endConditional(Augmentations augs) throws XNIException{
    } // endConditional()

    public void endDTD(Augmentations augs) throws XNIException{
        if(DEBUG_EVENTS){
            System.out.println("==>endDTD()");
        }
        fInDTD=false;
        if(!fBaseURIStack.isEmpty()){
            fBaseURIStack.pop();
        }
        String internalSubset=fInternalSubset!=null&&fInternalSubset.length()>0
                ?fInternalSubset.toString():null;
        if(fDeferNodeExpansion){
            if(internalSubset!=null){
                fDeferredDocumentImpl.setInternalSubset(fDocumentTypeIndex,internalSubset);
            }
        }else if(fDocumentImpl!=null){
            if(internalSubset!=null){
                ((DocumentTypeImpl)fDocumentType).setInternalSubset(internalSubset);
            }
        }
    } // endDTD()

    public void reset() throws XNIException{
        super.reset();
        // get feature state
        fCreateEntityRefNodes=
                fConfiguration.getFeature(CREATE_ENTITY_REF_NODES);
        fIncludeIgnorableWhitespace=
                fConfiguration.getFeature(INCLUDE_IGNORABLE_WHITESPACE);
        fDeferNodeExpansion=
                fConfiguration.getFeature(DEFER_NODE_EXPANSION);
        fNamespaceAware=fConfiguration.getFeature(NAMESPACES);
        fIncludeComments=fConfiguration.getFeature(INCLUDE_COMMENTS_FEATURE);
        fCreateCDATANodes=fConfiguration.getFeature(CREATE_CDATA_NODES_FEATURE);
        // get property
        setDocumentClassName((String)
                fConfiguration.getProperty(DOCUMENT_CLASS_NAME));
        // reset dom information
        fDocument=null;
        fDocumentImpl=null;
        fStorePSVI=false;
        fDocumentType=null;
        fDocumentTypeIndex=-1;
        fDeferredDocumentImpl=null;
        fCurrentNode=null;
        // reset string buffer
        fStringBuilder.setLength(0);
        // reset state information
        fRoot=null;
        fInDTD=false;
        fInDTDExternalSubset=false;
        fInCDATASection=false;
        fFirstChunk=false;
        fCurrentCDATASection=null;
        fCurrentCDATASectionIndex=-1;
        fBaseURIStack.removeAllElements();
    } // reset()

    protected final void handleBaseURI(Node node){
        if(fDocumentImpl!=null){
            // REVISIT: remove dependency on our implementation when
            //          DOM L3 becomes REC
            String baseURI=null;
            short nodeType=node.getNodeType();
            if(nodeType==Node.ELEMENT_NODE){
                // if an element already has xml:base attribute
                // do nothing
                if(fNamespaceAware){
                    if(((Element)node).getAttributeNodeNS("http://www.w3.org/XML/1998/namespace","base")!=null){
                        return;
                    }
                }else if(((Element)node).getAttributeNode("xml:base")!=null){
                    return;
                }
                // retrive the baseURI from the entity reference
                baseURI=((EntityReferenceImpl)fCurrentNode).getBaseURI();
                if(baseURI!=null&&!baseURI.equals(fDocumentImpl.getDocumentURI())){
                    if(fNamespaceAware){
                        ((Element)node).setAttributeNS("http://www.w3.org/XML/1998/namespace","xml:base",baseURI);
                    }else{
                        ((Element)node).setAttribute("xml:base",baseURI);
                    }
                }
            }else if(nodeType==Node.PROCESSING_INSTRUCTION_NODE){
                baseURI=((EntityReferenceImpl)fCurrentNode).getBaseURI();
                if(baseURI!=null&&fErrorHandler!=null){
                    DOMErrorImpl error=new DOMErrorImpl();
                    error.fType="pi-base-uri-not-preserved";
                    error.fRelatedData=baseURI;
                    error.fSeverity=DOMError.SEVERITY_WARNING;
                    fErrorHandler.getErrorHandler().handleError(error);
                }
            }
        }
    }

    protected final void handleBaseURI(int node){
        short nodeType=fDeferredDocumentImpl.getNodeType(node,false);
        if(nodeType==Node.ELEMENT_NODE){
            String baseURI=fDeferredDocumentImpl.getNodeValueString(fCurrentNodeIndex,false);
            if(baseURI==null){
                baseURI=fDeferredDocumentImpl.getDeferredEntityBaseURI(fDeferredEntityDecl);
            }
            if(baseURI!=null&&!baseURI.equals(fDeferredDocumentImpl.getDocumentURI())){
                fDeferredDocumentImpl.setDeferredAttribute(node,
                        "xml:base",
                        "http://www.w3.org/XML/1998/namespace",
                        baseURI,
                        true);
            }
        }else if(nodeType==Node.PROCESSING_INSTRUCTION_NODE){
            // retrieve baseURI from the entity reference
            String baseURI=fDeferredDocumentImpl.getNodeValueString(fCurrentNodeIndex,false);
            if(baseURI==null){
                // try baseURI of the entity declaration
                baseURI=fDeferredDocumentImpl.getDeferredEntityBaseURI(fDeferredEntityDecl);
            }
            if(baseURI!=null&&fErrorHandler!=null){
                DOMErrorImpl error=new DOMErrorImpl();
                error.fType="pi-base-uri-not-preserved";
                error.fRelatedData=baseURI;
                error.fSeverity=DOMError.SEVERITY_WARNING;
                fErrorHandler.getErrorHandler().handleError(error);
            }
        }
    }

    protected void setCharacterData(boolean sawChars){
        // handle character data
        fFirstChunk=sawChars;
        // if we have data in the buffer we must have created
        // a text node already.
        Node child=fCurrentNode.getLastChild();
        if(child!=null){
            if(fStringBuilder.length()>0){
                // REVISIT: should this check be performed?
                if(child.getNodeType()==Node.TEXT_NODE){
                    if(fDocumentImpl!=null){
                        ((TextImpl)child).replaceData(fStringBuilder.toString());
                    }else{
                        ((Text)child).setData(fStringBuilder.toString());
                    }
                }
                // reset string buffer
                fStringBuilder.setLength(0);
            }
            if(fDOMFilter!=null&&!fInEntityRef){
                if((child.getNodeType()==Node.TEXT_NODE)&&
                        ((fDOMFilter.getWhatToShow()&NodeFilter.SHOW_TEXT)!=0)){
                    short code=fDOMFilter.acceptNode(child);
                    switch(code){
                        case LSParserFilter.FILTER_INTERRUPT:{
                            throw Abort.INSTANCE;
                        }
                        case LSParserFilter.FILTER_REJECT:{
                            // fall through to SKIP since Comment has no children.
                        }
                        case LSParserFilter.FILTER_SKIP:{
                            fCurrentNode.removeChild(child);
                            return;
                        }
                        default:{
                            // accept node -- do nothing
                        }
                    }
                }
            }   // end-if fDOMFilter !=null
        } // end-if child !=null
    }

    // method to create an element node.
    // subclasses can override this method to create element nodes in other ways.
    protected Element createElementNode(QName element){
        Element el=null;
        if(fNamespaceAware){
            // if we are using xerces DOM implementation, call our
            // own constructor to reuse the strings we have here.
            if(fDocumentImpl!=null){
                el=fDocumentImpl.createElementNS(element.uri,element.rawname,
                        element.localpart);
            }else{
                el=fDocument.createElementNS(element.uri,element.rawname);
            }
        }else{
            el=fDocument.createElement(element.rawname);
        }
        return el;
    }

    // method to create an attribute node.
    // subclasses can override this method to create attribute nodes in other ways.
    protected Attr createAttrNode(QName attrQName){
        Attr attr=null;
        if(fNamespaceAware){
            if(fDocumentImpl!=null){
                // if we are using xerces DOM implementation, call our
                // own constructor to reuse the strings we have here.
                attr=fDocumentImpl.createAttributeNS(attrQName.uri,
                        attrQName.rawname,
                        attrQName.localpart);
            }else{
                attr=fDocument.createAttributeNS(attrQName.uri,
                        attrQName.rawname);
            }
        }else{
            attr=fDocument.createAttribute(attrQName.rawname);
        }
        return attr;
    }

    public void abort(){
        throw Abort.INSTANCE;
    }

    static final class Abort extends RuntimeException{
        static final Abort INSTANCE=new Abort();
        private static final long serialVersionUID=1687848994976808490L;

        private Abort(){
        }

        public Throwable fillInStackTrace(){
            return this;
        }
    }
} // class AbstractDOMParser
