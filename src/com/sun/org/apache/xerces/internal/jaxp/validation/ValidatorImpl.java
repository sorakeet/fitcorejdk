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
import com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import com.sun.org.apache.xerces.internal.util.Status;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import com.sun.org.apache.xerces.internal.xs.PSVIProvider;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.IOException;

final class ValidatorImpl extends Validator implements PSVIProvider{
    private static final String CURRENT_ELEMENT_NODE=Constants.XERCES_PROPERTY_PREFIX+Constants.CURRENT_ELEMENT_NODE_PROPERTY;
    //
    // Data
    //
    private XMLSchemaValidatorComponentManager fComponentManager;
    private ValidatorHandlerImpl fSAXValidatorHelper;
    private DOMValidatorHelper fDOMValidatorHelper;
    private StreamValidatorHelper fStreamValidatorHelper;
    private StAXValidatorHelper fStaxValidatorHelper;
    private boolean fConfigurationChanged=false;
    private boolean fErrorHandlerChanged=false;
    private boolean fResourceResolverChanged=false;

    public ValidatorImpl(XSGrammarPoolContainer grammarContainer){
        fComponentManager=new XMLSchemaValidatorComponentManager(grammarContainer);
        setErrorHandler(null);
        setResourceResolver(null);
    }

    public void reset(){
        // avoid resetting features and properties if the state the validator
        // is currently in, is the same as it will be after reset.
        if(fConfigurationChanged){
            fComponentManager.restoreInitialState();
            setErrorHandler(null);
            setResourceResolver(null);
            fConfigurationChanged=false;
            fErrorHandlerChanged=false;
            fResourceResolverChanged=false;
        }else{
            if(fErrorHandlerChanged){
                setErrorHandler(null);
                fErrorHandlerChanged=false;
            }
            if(fResourceResolverChanged){
                setResourceResolver(null);
                fResourceResolverChanged=false;
            }
        }
    }

    public void validate(Source source,Result result)
            throws SAXException, IOException{
        if(source instanceof SAXSource){
            // Hand off to SAX validator helper.
            if(fSAXValidatorHelper==null){
                fSAXValidatorHelper=new ValidatorHandlerImpl(fComponentManager);
            }
            fSAXValidatorHelper.validate(source,result);
        }else if(source instanceof DOMSource){
            // Hand off to DOM validator helper.
            if(fDOMValidatorHelper==null){
                fDOMValidatorHelper=new DOMValidatorHelper(fComponentManager);
            }
            fDOMValidatorHelper.validate(source,result);
        }else if(source instanceof StreamSource){
            // Hand off to stream validator helper.
            if(fStreamValidatorHelper==null){
                fStreamValidatorHelper=new StreamValidatorHelper(fComponentManager);
            }
            fStreamValidatorHelper.validate(source,result);
        }else if(source instanceof StAXSource){
            // Hand off to stax validator helper.
            if(fStaxValidatorHelper==null){
                fStaxValidatorHelper=new StAXValidatorHelper(fComponentManager);
            }
            fStaxValidatorHelper.validate(source,result);
        }
        // Source parameter cannot be null.
        else if(source==null){
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(),
                    "SourceParameterNull",null));
        }
        // Source parameter must be a SAXSource, DOMSource or StreamSource
        else{
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(),
                    "SourceNotAccepted",new Object[]{source.getClass().getName()}));
        }
    }    public void setErrorHandler(ErrorHandler errorHandler){
        fErrorHandlerChanged=(errorHandler!=null);
        fComponentManager.setErrorHandler(errorHandler);
    }

    public ElementPSVI getElementPSVI(){
        return (fSAXValidatorHelper!=null)?fSAXValidatorHelper.getElementPSVI():null;
    }    public ErrorHandler getErrorHandler(){
        return fComponentManager.getErrorHandler();
    }

    public AttributePSVI getAttributePSVI(int index){
        return (fSAXValidatorHelper!=null)?fSAXValidatorHelper.getAttributePSVI(index):null;
    }    public void setResourceResolver(LSResourceResolver resourceResolver){
        fResourceResolverChanged=(resourceResolver!=null);
        fComponentManager.setResourceResolver(resourceResolver);
    }

    public AttributePSVI getAttributePSVIByName(String uri,String localname){
        return (fSAXValidatorHelper!=null)?fSAXValidatorHelper.getAttributePSVIByName(uri,localname):null;
    }    public LSResourceResolver getResourceResolver(){
        return fComponentManager.getResourceResolver();
    }

    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        try{
            return fComponentManager.getFeature(name);
        }catch(XMLConfigurationException e){
            final String identifier=e.getIdentifier();
            final String key=e.getType()==Status.NOT_RECOGNIZED?
                    "feature-not-recognized":"feature-not-supported";
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(fComponentManager.getLocale(),
                            key,new Object[]{identifier}));
        }
    }

    public void setFeature(String name,boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        try{
            fComponentManager.setFeature(name,value);
        }catch(XMLConfigurationException e){
            final String identifier=e.getIdentifier();
            final String key;
            if(e.getType()==Status.NOT_ALLOWED){
                //for now, the identifier can only be (XMLConstants.FEATURE_SECURE_PROCESSING)
                throw new SAXNotSupportedException(
                        SAXMessageFormatter.formatMessage(fComponentManager.getLocale(),
                                "jaxp-secureprocessing-feature",null));
            }else if(e.getType()==Status.NOT_RECOGNIZED){
                key="feature-not-recognized";
            }else{
                key="feature-not-supported";
            }
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(fComponentManager.getLocale(),
                            key,new Object[]{identifier}));
        }
        fConfigurationChanged=true;
    }

    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        //Support current-element-node; return current node if DOMSource is used.
        if(CURRENT_ELEMENT_NODE.equals(name)){
            return (fDOMValidatorHelper!=null)?fDOMValidatorHelper.getCurrentElement():null;
        }
        try{
            return fComponentManager.getProperty(name);
        }catch(XMLConfigurationException e){
            final String identifier=e.getIdentifier();
            final String key=e.getType()==Status.NOT_RECOGNIZED?
                    "property-not-recognized":"property-not-supported";
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(fComponentManager.getLocale(),
                            key,new Object[]{identifier}));
        }
    }

    public void setProperty(String name,Object object)
            throws SAXNotRecognizedException, SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        try{
            fComponentManager.setProperty(name,object);
        }catch(XMLConfigurationException e){
            final String identifier=e.getIdentifier();
            final String key=e.getType()==Status.NOT_RECOGNIZED?
                    "property-not-recognized":"property-not-supported";
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(fComponentManager.getLocale(),
                            key,new Object[]{identifier}));
        }
        fConfigurationChanged=true;
    }








} // ValidatorImpl
