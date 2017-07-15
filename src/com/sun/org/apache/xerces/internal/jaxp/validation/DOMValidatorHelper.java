/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * Copyright 2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.jaxp.validation;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.validation.EntityState;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.util.Enumeration;

final class DOMValidatorHelper implements ValidatorHelper, EntityState{
    //
    // Constants
    //
    private static final int CHUNK_SIZE=(1<<10);
    private static final int CHUNK_MASK=CHUNK_SIZE-1;
    // property identifiers
    private static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    private static final String NAMESPACE_CONTEXT=
            Constants.XERCES_PROPERTY_PREFIX+Constants.NAMESPACE_CONTEXT_PROPERTY;
    private static final String SCHEMA_VALIDATOR=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SCHEMA_VALIDATOR_PROPERTY;
    private static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    private static final String VALIDATION_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.VALIDATION_MANAGER_PROPERTY;
    final QName fElementQName=new QName();
    final QName fAttributeQName=new QName();
    final XMLAttributesImpl fAttributes=new XMLAttributesImpl();
    final XMLString fTempString=new XMLString();
    private final SimpleLocator fXMLLocator=new SimpleLocator(null,null,-1,-1,-1);
    private final DOMResultAugmentor fDOMResultAugmentor=new DOMResultAugmentor(this);
    private final DOMResultBuilder fDOMResultBuilder=new DOMResultBuilder();
    //
    // Data
    //
    private XMLErrorReporter fErrorReporter;
    private NamespaceSupport fNamespaceContext;
    private DOMNamespaceContext fDOMNamespaceContext=new DOMNamespaceContext();
    private XMLSchemaValidator fSchemaValidator;
    private SymbolTable fSymbolTable;
    private ValidationManager fValidationManager;
    private XMLSchemaValidatorComponentManager fComponentManager;
    private DOMDocumentHandler fDOMValidatorHandler;
    private NamedNodeMap fEntities=null;
    private char[] fCharBuffer=new char[CHUNK_SIZE];
    private Node fRoot;
    private Node fCurrentElement;

    public DOMValidatorHelper(XMLSchemaValidatorComponentManager componentManager){
        fComponentManager=componentManager;
        fErrorReporter=(XMLErrorReporter)fComponentManager.getProperty(ERROR_REPORTER);
        fNamespaceContext=(NamespaceSupport)fComponentManager.getProperty(NAMESPACE_CONTEXT);
        fSchemaValidator=(XMLSchemaValidator)fComponentManager.getProperty(SCHEMA_VALIDATOR);
        fSymbolTable=(SymbolTable)fComponentManager.getProperty(SYMBOL_TABLE);
        fValidationManager=(ValidationManager)fComponentManager.getProperty(VALIDATION_MANAGER);
    }

    public void validate(Source source,Result result)
            throws SAXException, IOException{
        if(result instanceof DOMResult||result==null){
            final DOMSource domSource=(DOMSource)source;
            final DOMResult domResult=(DOMResult)result;
            Node node=domSource.getNode();
            fRoot=node;
            if(node!=null){
                fComponentManager.reset();
                fValidationManager.setEntityState(this);
                fDOMNamespaceContext.reset();
                String systemId=domSource.getSystemId();
                fXMLLocator.setLiteralSystemId(systemId);
                fXMLLocator.setExpandedSystemId(systemId);
                fErrorReporter.setDocumentLocator(fXMLLocator);
                try{
                    // regardless of what type of node this is, fire start and end document events
                    setupEntityMap((node.getNodeType()==Node.DOCUMENT_NODE)?(Document)node:node.getOwnerDocument());
                    setupDOMResultHandler(domSource,domResult);
                    fSchemaValidator.startDocument(fXMLLocator,null,fDOMNamespaceContext,null);
                    validate(node);
                    fSchemaValidator.endDocument(null);
                }catch(XMLParseException e){
                    throw Util.toSAXParseException(e);
                }catch(XNIException e){
                    throw Util.toSAXException(e);
                }finally{
                    // Release references to application objects
                    fRoot=null;
                    //fCurrentElement = null; -- keep the reference to support current-element-node property
                    fEntities=null;
                    if(fDOMValidatorHandler!=null){
                        fDOMValidatorHandler.setDOMResult(null);
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(),
                "SourceResultMismatch",
                new Object[]{source.getClass().getName(),result.getClass().getName()}));
    }

    private void validate(Node node){
        final Node top=node;
        // Performs a non-recursive traversal of the DOM. This
        // will avoid a stack overflow for DOMs with high depth.
        while(node!=null){
            beginNode(node);
            Node next=node.getFirstChild();
            while(next==null){
                finishNode(node);
                if(top==node){
                    break;
                }
                next=node.getNextSibling();
                if(next==null){
                    node=node.getParentNode();
                    if(node==null||top==node){
                        if(node!=null){
                            finishNode(node);
                        }
                        next=null;
                        break;
                    }
                }
            }
            node=next;
        }
    }

    private void beginNode(Node node){
        switch(node.getNodeType()){
            case Node.ELEMENT_NODE:
                fCurrentElement=node;
                // push namespace context
                fNamespaceContext.pushContext();
                // start element
                fillQName(fElementQName,node);
                processAttributes(node.getAttributes());
                fSchemaValidator.startElement(fElementQName,fAttributes,null);
                break;
            case Node.TEXT_NODE:
                if(fDOMValidatorHandler!=null){
                    fDOMValidatorHandler.setIgnoringCharacters(true);
                    sendCharactersToValidator(node.getNodeValue());
                    fDOMValidatorHandler.setIgnoringCharacters(false);
                    fDOMValidatorHandler.characters((Text)node);
                }else{
                    sendCharactersToValidator(node.getNodeValue());
                }
                break;
            case Node.CDATA_SECTION_NODE:
                if(fDOMValidatorHandler!=null){
                    fDOMValidatorHandler.setIgnoringCharacters(true);
                    fSchemaValidator.startCDATA(null);
                    sendCharactersToValidator(node.getNodeValue());
                    fSchemaValidator.endCDATA(null);
                    fDOMValidatorHandler.setIgnoringCharacters(false);
                    fDOMValidatorHandler.cdata((CDATASection)node);
                }else{
                    fSchemaValidator.startCDATA(null);
                    sendCharactersToValidator(node.getNodeValue());
                    fSchemaValidator.endCDATA(null);
                }
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                /**
                 * The validator does nothing with processing instructions so bypass it.
                 * Send the ProcessingInstruction node directly to the result builder.
                 */
                if(fDOMValidatorHandler!=null){
                    fDOMValidatorHandler.processingInstruction((ProcessingInstruction)node);
                }
                break;
            case Node.COMMENT_NODE:
                /**
                 * The validator does nothing with comments so bypass it.
                 * Send the Comment node directly to the result builder.
                 */
                if(fDOMValidatorHandler!=null){
                    fDOMValidatorHandler.comment((Comment)node);
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                /**
                 * Send the DocumentType node directly to the result builder.
                 */
                if(fDOMValidatorHandler!=null){
                    fDOMValidatorHandler.doctypeDecl((DocumentType)node);
                }
                break;
            default: // Ignore other node types.
                break;
        }
    }

    private void fillQName(QName toFill,Node node){
        final String prefix=node.getPrefix();
        final String localName=node.getLocalName();
        final String rawName=node.getNodeName();
        final String namespace=node.getNamespaceURI();
        toFill.uri=(namespace!=null&&namespace.length()>0)?fSymbolTable.addSymbol(namespace):null;
        toFill.rawname=(rawName!=null)?fSymbolTable.addSymbol(rawName):XMLSymbols.EMPTY_STRING;
        // Is this a DOM level1 document?
        if(localName==null){
            int k=rawName.indexOf(':');
            if(k>0){
                toFill.prefix=fSymbolTable.addSymbol(rawName.substring(0,k));
                toFill.localpart=fSymbolTable.addSymbol(rawName.substring(k+1));
            }else{
                toFill.prefix=XMLSymbols.EMPTY_STRING;
                toFill.localpart=toFill.rawname;
            }
        }else{
            toFill.prefix=(prefix!=null)?fSymbolTable.addSymbol(prefix):XMLSymbols.EMPTY_STRING;
            toFill.localpart=(localName!=null)?fSymbolTable.addSymbol(localName):XMLSymbols.EMPTY_STRING;
        }
    }

    private void processAttributes(NamedNodeMap attrMap){
        final int attrCount=attrMap.getLength();
        fAttributes.removeAllAttributes();
        for(int i=0;i<attrCount;++i){
            Attr attr=(Attr)attrMap.item(i);
            String value=attr.getValue();
            if(value==null){
                value=XMLSymbols.EMPTY_STRING;
            }
            fillQName(fAttributeQName,attr);
            // REVISIT: Assuming all attributes are of type CDATA. The actual type may not matter. -- mrglavas
            fAttributes.addAttributeNS(fAttributeQName,XMLSymbols.fCDATASymbol,value);
            fAttributes.setSpecified(i,attr.getSpecified());
            // REVISIT: Should we be looking at non-namespace attributes
            // for additional mappings? Should we detect illegal namespace
            // declarations and exclude them from the context? -- mrglavas
            if(fAttributeQName.uri==NamespaceContext.XMLNS_URI){
                // process namespace attribute
                if(fAttributeQName.prefix==XMLSymbols.PREFIX_XMLNS){
                    fNamespaceContext.declarePrefix(fAttributeQName.localpart,value.length()!=0?fSymbolTable.addSymbol(value):null);
                }else{
                    fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING,value.length()!=0?fSymbolTable.addSymbol(value):null);
                }
            }
        }
    }

    private void sendCharactersToValidator(String str){
        if(str!=null){
            final int length=str.length();
            final int remainder=length&CHUNK_MASK;
            if(remainder>0){
                str.getChars(0,remainder,fCharBuffer,0);
                fTempString.setValues(fCharBuffer,0,remainder);
                fSchemaValidator.characters(fTempString,null);
            }
            int i=remainder;
            while(i<length){
                str.getChars(i,i+=CHUNK_SIZE,fCharBuffer,0);
                fTempString.setValues(fCharBuffer,0,CHUNK_SIZE);
                fSchemaValidator.characters(fTempString,null);
            }
        }
    }

    private void finishNode(Node node){
        if(node.getNodeType()==Node.ELEMENT_NODE){
            fCurrentElement=node;
            // end element
            fillQName(fElementQName,node);
            fSchemaValidator.endElement(fElementQName,null);
            // pop namespace context
            fNamespaceContext.popContext();
        }
    }

    private void setupEntityMap(Document doc){
        if(doc!=null){
            DocumentType docType=doc.getDoctype();
            if(docType!=null){
                fEntities=docType.getEntities();
                return;
            }
        }
        fEntities=null;
    }

    private void setupDOMResultHandler(DOMSource source,DOMResult result) throws SAXException{
        // If there's no DOMResult, unset the validator handler
        if(result==null){
            fDOMValidatorHandler=null;
            fSchemaValidator.setDocumentHandler(null);
            return;
        }
        final Node nodeResult=result.getNode();
        // If the source node and result node are the same use the DOMResultAugmentor.
        // Otherwise use the DOMResultBuilder.
        if(source.getNode()==nodeResult){
            fDOMValidatorHandler=fDOMResultAugmentor;
            fDOMResultAugmentor.setDOMResult(result);
            fSchemaValidator.setDocumentHandler(fDOMResultAugmentor);
            return;
        }
        if(result.getNode()==null){
            try{
                DocumentBuilderFactory factory=fComponentManager.getFeature(Constants.ORACLE_FEATURE_SERVICE_MECHANISM)?
                        DocumentBuilderFactory.newInstance():new DocumentBuilderFactoryImpl();
                factory.setNamespaceAware(true);
                DocumentBuilder builder=factory.newDocumentBuilder();
                result.setNode(builder.newDocument());
            }catch(ParserConfigurationException e){
                throw new SAXException(e);
            }
        }
        fDOMValidatorHandler=fDOMResultBuilder;
        fDOMResultBuilder.setDOMResult(result);
        fSchemaValidator.setDocumentHandler(fDOMResultBuilder);
    }

    public boolean isEntityDeclared(String name){
        return false;
    }

    public boolean isEntityUnparsed(String name){
        if(fEntities!=null){
            Entity entity=(Entity)fEntities.getNamedItem(name);
            if(entity!=null){
                return (entity.getNotationName()!=null);
            }
        }
        return false;
    }

    Node getCurrentElement(){
        return fCurrentElement;
    }

    final class DOMNamespaceContext implements NamespaceContext{
        //
        // Data
        //
        protected String[] fNamespace=new String[16*2];
        protected int fNamespaceSize=0;
        protected boolean fDOMContextBuilt=false;
        //
        // Methods
        //

        public void pushContext(){
            fNamespaceContext.pushContext();
        }

        public void popContext(){
            fNamespaceContext.popContext();
        }

        public boolean declarePrefix(String prefix,String uri){
            return fNamespaceContext.declarePrefix(prefix,uri);
        }

        public String getURI(String prefix){
            String uri=fNamespaceContext.getURI(prefix);
            if(uri==null){
                if(!fDOMContextBuilt){
                    fillNamespaceContext();
                    fDOMContextBuilt=true;
                }
                if(fNamespaceSize>0&&
                        !fNamespaceContext.containsPrefix(prefix)){
                    uri=getURI0(prefix);
                }
            }
            return uri;
        }

        public String getPrefix(String uri){
            return fNamespaceContext.getPrefix(uri);
        }

        public int getDeclaredPrefixCount(){
            return fNamespaceContext.getDeclaredPrefixCount();
        }

        public String getDeclaredPrefixAt(int index){
            return fNamespaceContext.getDeclaredPrefixAt(index);
        }

        public Enumeration getAllPrefixes(){
            return fNamespaceContext.getAllPrefixes();
        }

        public void reset(){
            fDOMContextBuilt=false;
            fNamespaceSize=0;
        }

        private void fillNamespaceContext(){
            if(fRoot!=null){
                Node currentNode=fRoot.getParentNode();
                while(currentNode!=null){
                    if(Node.ELEMENT_NODE==currentNode.getNodeType()){
                        NamedNodeMap attributes=currentNode.getAttributes();
                        final int attrCount=attributes.getLength();
                        for(int i=0;i<attrCount;++i){
                            Attr attr=(Attr)attributes.item(i);
                            String value=attr.getValue();
                            if(value==null){
                                value=XMLSymbols.EMPTY_STRING;
                            }
                            fillQName(fAttributeQName,attr);
                            // REVISIT: Should we be looking at non-namespace attributes
                            // for additional mappings? Should we detect illegal namespace
                            // declarations and exclude them from the context? -- mrglavas
                            if(fAttributeQName.uri==NamespaceContext.XMLNS_URI){
                                // process namespace attribute
                                if(fAttributeQName.prefix==XMLSymbols.PREFIX_XMLNS){
                                    declarePrefix0(fAttributeQName.localpart,value.length()!=0?fSymbolTable.addSymbol(value):null);
                                }else{
                                    declarePrefix0(XMLSymbols.EMPTY_STRING,value.length()!=0?fSymbolTable.addSymbol(value):null);
                                }
                            }
                        }
                    }
                    currentNode=currentNode.getParentNode();
                }
            }
        }

        private void declarePrefix0(String prefix,String uri){
            // resize array, if needed
            if(fNamespaceSize==fNamespace.length){
                String[] namespacearray=new String[fNamespaceSize*2];
                System.arraycopy(fNamespace,0,namespacearray,0,fNamespaceSize);
                fNamespace=namespacearray;
            }
            // bind prefix to uri in current context
            fNamespace[fNamespaceSize++]=prefix;
            fNamespace[fNamespaceSize++]=uri;
        }

        private String getURI0(String prefix){
            // find prefix in the DOM context
            for(int i=0;i<fNamespaceSize;i+=2){
                if(fNamespace[i]==prefix){
                    return fNamespace[i+1];
                }
            }
            // prefix not found
            return null;
        }
    }
} // DOMValidatorHelper
