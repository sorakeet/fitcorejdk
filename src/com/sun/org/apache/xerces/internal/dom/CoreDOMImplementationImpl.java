/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * Copyright 1999-2005 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.RevalidationHandler;
import com.sun.org.apache.xerces.internal.parsers.DOMParserImpl;
import com.sun.org.apache.xerces.internal.parsers.DTDConfiguration;
import com.sun.org.apache.xerces.internal.parsers.XIncludeAwareParserConfiguration;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;

public class CoreDOMImplementationImpl
        implements DOMImplementation, DOMImplementationLS{
    //
    // Data
    //
    // validators pool
    private static final int SIZE=2;
    // static
    static CoreDOMImplementationImpl singleton=
            new CoreDOMImplementationImpl();
    private RevalidationHandler validators[]=new RevalidationHandler[SIZE];
    private RevalidationHandler dtdValidators[]=new RevalidationHandler[SIZE];
    private int freeValidatorIndex=-1;
    private int freeDTDValidatorIndex=-1;
    private int currentSize=SIZE;
    // Document and doctype counter.  Used to assign order to documents and
    // doctypes without owners, on an demand basis.   Used for
    // compareDocumentPosition
    private int docAndDoctypeCounter=0;

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
        boolean anyVersion=version==null||version.length()==0;
        // check if Xalan implementation is around and if yes report true for supporting
        // XPath API
        // if a plus sign "+" is prepended to any feature name, implementations
        // are considered in which the specified feature may not be directly
        // castable DOMImplementation.getFeature(feature, version). Without a
        // plus, only features whose interfaces are directly castable are considered.
        if((feature.equalsIgnoreCase("+XPath"))
                &&(anyVersion||version.equals("3.0"))){
            try{
                Class xpathClass=ObjectFactory.findProviderClass(
                        "com.sun.org.apache.xpath.internal.domapi.XPathEvaluatorImpl",true);
                // Check if the DOM XPath implementation implements
                // the interface org.w3c.dom.XPathEvaluator
                Class interfaces[]=xpathClass.getInterfaces();
                for(int i=0;i<interfaces.length;i++){
                    if(interfaces[i].getName().equals(
                            "org.w3c.dom.xpath.XPathEvaluator")){
                        return true;
                    }
                }
            }catch(Exception e){
                return false;
            }
            return true;
        }
        if(feature.startsWith("+")){
            feature=feature.substring(1);
        }
        return (
                feature.equalsIgnoreCase("Core")
                        &&(anyVersion
                        ||version.equals("1.0")
                        ||version.equals("2.0")
                        ||version.equals("3.0")))
                ||(feature.equalsIgnoreCase("XML")
                &&(anyVersion
                ||version.equals("1.0")
                ||version.equals("2.0")
                ||version.equals("3.0")))
                ||(feature.equalsIgnoreCase("LS")
                &&(anyVersion||version.equals("3.0")));
    } // hasFeature(String,String):boolean

    public DocumentType createDocumentType(String qualifiedName,
                                           String publicID,String systemID){
        // REVISIT: this might allow creation of invalid name for DOCTYPE
        //          xmlns prefix.
        //          also there is no way for a user to turn off error checking.
        checkQName(qualifiedName);
        return new DocumentTypeImpl(null,qualifiedName,publicID,systemID);
    }

    final void checkQName(String qname){
        int index=qname.indexOf(':');
        int lastIndex=qname.lastIndexOf(':');
        int length=qname.length();
        // it is an error for NCName to have more than one ':'
        // check if it is valid QName [Namespace in XML production 6]
        if(index==0||index==length-1||lastIndex!=index){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NAMESPACE_ERR",
                            null);
            throw new DOMException(DOMException.NAMESPACE_ERR,msg);
        }
        int start=0;
        // Namespace in XML production [6]
        if(index>0){
            // check that prefix is NCName
            if(!XMLChar.isNCNameStart(qname.charAt(start))){
                String msg=
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "INVALID_CHARACTER_ERR",
                                null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
            }
            for(int i=1;i<index;i++){
                if(!XMLChar.isNCName(qname.charAt(i))){
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "INVALID_CHARACTER_ERR",
                                    null);
                    throw new DOMException(
                            DOMException.INVALID_CHARACTER_ERR,
                            msg);
                }
            }
            start=index+1;
        }
        // check local part
        if(!XMLChar.isNCNameStart(qname.charAt(start))){
            // REVISIT: add qname parameter to the message
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "INVALID_CHARACTER_ERR",
                            null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
        }
        for(int i=start+1;i<length;i++){
            if(!XMLChar.isNCName(qname.charAt(i))){
                String msg=
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "INVALID_CHARACTER_ERR",
                                null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
            }
        }
    }

    public Document createDocument(
            String namespaceURI,
            String qualifiedName,
            DocumentType doctype)
            throws DOMException{
        if(doctype!=null&&doctype.getOwnerDocument()!=null){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "WRONG_DOCUMENT_ERR",
                            null);
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
        }
        CoreDocumentImpl doc=new CoreDocumentImpl(doctype);
        Element e=doc.createElementNS(namespaceURI,qualifiedName);
        doc.appendChild(e);
        return doc;
    }

    public Object getFeature(String feature,String version){
        if(singleton.hasFeature(feature,version)){
            if((feature.equalsIgnoreCase("+XPath"))){
                try{
                    Class xpathClass=ObjectFactory.findProviderClass(
                            "com.sun.org.apache.xpath.internal.domapi.XPathEvaluatorImpl",true);
                    // Check if the DOM XPath implementation implements
                    // the interface org.w3c.dom.XPathEvaluator
                    Class interfaces[]=xpathClass.getInterfaces();
                    for(int i=0;i<interfaces.length;i++){
                        if(interfaces[i].getName().equals(
                                "org.w3c.dom.xpath.XPathEvaluator")){
                            return xpathClass.newInstance();
                        }
                    }
                }catch(Exception e){
                    return null;
                }
            }else{
                return singleton;
            }
        }
        return null;
    }
    // DOM L3 LS

    public LSParser createLSParser(short mode,String schemaType)
            throws DOMException{
        if(mode!=DOMImplementationLS.MODE_SYNCHRONOUS||(schemaType!=null&&
                !"http://www.w3.org/2001/XMLSchema".equals(schemaType)&&
                !"http://www.w3.org/TR/REC-xml".equals(schemaType))){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NOT_SUPPORTED_ERR",
                            null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
        if(schemaType!=null
                &&schemaType.equals("http://www.w3.org/TR/REC-xml")){
            return new DOMParserImpl(new DTDConfiguration(),
                    schemaType);
        }else{
            // create default parser configuration validating against XMLSchemas
            return new DOMParserImpl(new XIncludeAwareParserConfiguration(),
                    schemaType);
        }
    }

    public LSSerializer createLSSerializer(){
        return new DOMSerializerImpl();
    }

    public LSInput createLSInput(){
        return new DOMInputImpl();
    }

    public LSOutput createLSOutput(){
        return new DOMOutputImpl();
    }

    //
    // Protected methods
    //
    synchronized RevalidationHandler getValidator(String schemaType){
        // REVISIT: implement retrieving DTD validator
        if(schemaType==XMLGrammarDescription.XML_SCHEMA){
            // create new validator - we should not attempt
            // to restrict the number of validation handlers being
            // requested
            if(freeValidatorIndex<0){
                return (RevalidationHandler)(ObjectFactory
                        .newInstance(
                                "com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator",
                                ObjectFactory.findClassLoader(),
                                true));
            }
            // return first available validator
            RevalidationHandler val=validators[freeValidatorIndex];
            validators[freeValidatorIndex--]=null;
            return val;
        }else if(schemaType==XMLGrammarDescription.XML_DTD){
            if(freeDTDValidatorIndex<0){
                return (RevalidationHandler)(ObjectFactory
                        .newInstance(
                                "com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator",
                                ObjectFactory.findClassLoader(),
                                true));
            }
            // return first available validator
            RevalidationHandler val=dtdValidators[freeDTDValidatorIndex];
            dtdValidators[freeDTDValidatorIndex--]=null;
            return val;
        }
        return null;
    }

    synchronized void releaseValidator(String schemaType,
                                       RevalidationHandler validator){
        // REVISIT: implement support for DTD validators as well
        if(schemaType==XMLGrammarDescription.XML_SCHEMA){
            ++freeValidatorIndex;
            if(validators.length==freeValidatorIndex){
                // resize size of the validators
                currentSize+=SIZE;
                RevalidationHandler newarray[]=new RevalidationHandler[currentSize];
                System.arraycopy(validators,0,newarray,0,validators.length);
                validators=newarray;
            }
            validators[freeValidatorIndex]=validator;
        }else if(schemaType==XMLGrammarDescription.XML_DTD){
            ++freeDTDValidatorIndex;
            if(dtdValidators.length==freeDTDValidatorIndex){
                // resize size of the validators
                currentSize+=SIZE;
                RevalidationHandler newarray[]=new RevalidationHandler[currentSize];
                System.arraycopy(dtdValidators,0,newarray,0,dtdValidators.length);
                dtdValidators=newarray;
            }
            dtdValidators[freeDTDValidatorIndex]=validator;
        }
    }

    protected synchronized int assignDocumentNumber(){
        return ++docAndDoctypeCounter;
    }

    protected synchronized int assignDocTypeNumber(){
        return ++docAndDoctypeCounter;
    }
} // class DOMImplementationImpl
