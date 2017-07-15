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
 * <p>
 * $Id: SmartTransformerFactoryImpl.java,v 1.2.4.1 2005/09/14 09:57:13 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: SmartTransformerFactoryImpl.java,v 1.2.4.1 2005/09/14 09:57:13 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import org.xml.sax.XMLFilter;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class SmartTransformerFactoryImpl extends SAXTransformerFactory{
    private static final String CLASS_NAME="SmartTransformerFactoryImpl";
    private SAXTransformerFactory _xsltcFactory=null;
    private SAXTransformerFactory _xalanFactory=null;
    private SAXTransformerFactory _currFactory=null;
    private ErrorListener _errorlistener=null;
    private URIResolver _uriresolver=null;
    private boolean featureSecureProcessing=false;

    public SmartTransformerFactoryImpl(){
    }

    public Transformer newTransformer(Source source) throws
            TransformerConfigurationException{
        if(_xalanFactory==null){
            createXalanTransformerFactory();
        }
        if(_errorlistener!=null){
            _xalanFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xalanFactory.setURIResolver(_uriresolver);
        }
        _currFactory=_xalanFactory;
        return _currFactory.newTransformer(source);
    }    private void createXSLTCTransformerFactory(){
        _xsltcFactory=new TransformerFactoryImpl();
        _currFactory=_xsltcFactory;
    }

    public Transformer newTransformer()
            throws TransformerConfigurationException{
        if(_xalanFactory==null){
            createXalanTransformerFactory();
        }
        if(_errorlistener!=null){
            _xalanFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xalanFactory.setURIResolver(_uriresolver);
        }
        _currFactory=_xalanFactory;
        return _currFactory.newTransformer();
    }    private void createXalanTransformerFactory(){
        final String xalanMessage=
                "com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl "+
                        "could not create an "+
                        "com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl.";
        // try to create instance of Xalan factory...
        try{
            Class xalanFactClass=ObjectFactory.findProviderClass(
                    "com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl",
                    true);
            _xalanFactory=(SAXTransformerFactory)
                    xalanFactClass.newInstance();
        }catch(ClassNotFoundException e){
            System.err.println(xalanMessage);
        }catch(InstantiationException e){
            System.err.println(xalanMessage);
        }catch(IllegalAccessException e){
            System.err.println(xalanMessage);
        }
        _currFactory=_xalanFactory;
    }

    public Templates newTemplates(Source source)
            throws TransformerConfigurationException{
        if(_xsltcFactory==null){
            createXSLTCTransformerFactory();
        }
        if(_errorlistener!=null){
            _xsltcFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xsltcFactory.setURIResolver(_uriresolver);
        }
        _currFactory=_xsltcFactory;
        return _currFactory.newTemplates(source);
    }    public void setErrorListener(ErrorListener listener)
            throws IllegalArgumentException{
        _errorlistener=listener;
    }

    public Source getAssociatedStylesheet(Source source,String media,
                                          String title,String charset)
            throws TransformerConfigurationException{
        if(_currFactory==null){
            createXSLTCTransformerFactory();
        }
        return _currFactory.getAssociatedStylesheet(source,media,
                title,charset);
    }    public ErrorListener getErrorListener(){
        return _errorlistener;
    }

    public TransformerHandler newTransformerHandler(Source src)
            throws TransformerConfigurationException{
        if(_xalanFactory==null){
            createXalanTransformerFactory();
        }
        if(_errorlistener!=null){
            _xalanFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xalanFactory.setURIResolver(_uriresolver);
        }
        return _xalanFactory.newTransformerHandler(src);
    }    public Object getAttribute(String name)
            throws IllegalArgumentException{
        // GTM: NB: 'debug' should change to something more unique...
        if((name.equals("translet-name"))||(name.equals("debug"))){
            if(_xsltcFactory==null){
                createXSLTCTransformerFactory();
            }
            return _xsltcFactory.getAttribute(name);
        }else{
            if(_xalanFactory==null){
                createXalanTransformerFactory();
            }
            return _xalanFactory.getAttribute(name);
        }
    }

    public TransformerHandler newTransformerHandler(Templates templates)
            throws TransformerConfigurationException{
        if(_xsltcFactory==null){
            createXSLTCTransformerFactory();
        }
        if(_errorlistener!=null){
            _xsltcFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xsltcFactory.setURIResolver(_uriresolver);
        }
        return _xsltcFactory.newTransformerHandler(templates);
    }    public void setAttribute(String name,Object value)
            throws IllegalArgumentException{
        // GTM: NB: 'debug' should change to something more unique...
        if((name.equals("translet-name"))||(name.equals("debug"))){
            if(_xsltcFactory==null){
                createXSLTCTransformerFactory();
            }
            _xsltcFactory.setAttribute(name,value);
        }else{
            if(_xalanFactory==null){
                createXalanTransformerFactory();
            }
            _xalanFactory.setAttribute(name,value);
        }
    }

    public TransformerHandler newTransformerHandler()
            throws TransformerConfigurationException{
        if(_xalanFactory==null){
            createXalanTransformerFactory();
        }
        if(_errorlistener!=null){
            _xalanFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xalanFactory.setURIResolver(_uriresolver);
        }
        return _xalanFactory.newTransformerHandler();
    }    public void setFeature(String name,boolean value)
            throws TransformerConfigurationException{
        // feature name cannot be null
        if(name==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_SET_FEATURE_NULL_NAME);
            throw new NullPointerException(err.toString());
        }
        // secure processing?
        else if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            featureSecureProcessing=value;
            // all done processing feature
            return;
        }else{
            // unknown feature
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_UNSUPPORTED_FEATURE,name);
            throw new TransformerConfigurationException(err.toString());
        }
    }

    public TemplatesHandler newTemplatesHandler()
            throws TransformerConfigurationException{
        if(_xsltcFactory==null){
            createXSLTCTransformerFactory();
        }
        if(_errorlistener!=null){
            _xsltcFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xsltcFactory.setURIResolver(_uriresolver);
        }
        return _xsltcFactory.newTemplatesHandler();
    }    public boolean getFeature(String name){
        // All supported features should be listed here
        String[] features={
                DOMSource.FEATURE,
                DOMResult.FEATURE,
                SAXSource.FEATURE,
                SAXResult.FEATURE,
                StreamSource.FEATURE,
                StreamResult.FEATURE
        };
        // feature name cannot be null
        if(name==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_GET_FEATURE_NULL_NAME);
            throw new NullPointerException(err.toString());
        }
        // Inefficient, but it really does not matter in a function like this
        for(int i=0;i<features.length;i++){
            if(name.equals(features[i]))
                return true;
        }
        // secure processing?
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            return featureSecureProcessing;
        }
        // unknown feature
        return false;
    }

    public XMLFilter newXMLFilter(Source src)
            throws TransformerConfigurationException{
        if(_xsltcFactory==null){
            createXSLTCTransformerFactory();
        }
        if(_errorlistener!=null){
            _xsltcFactory.setErrorListener(_errorlistener);
        }
        if(_uriresolver!=null){
            _xsltcFactory.setURIResolver(_uriresolver);
        }
        Templates templates=_xsltcFactory.newTemplates(src);
        if(templates==null) return null;
        return newXMLFilter(templates);
    }    public URIResolver getURIResolver(){
        return _uriresolver;
    }

    public XMLFilter newXMLFilter(Templates templates)
            throws TransformerConfigurationException{
        try{
            return new TrAXFilter(templates);
        }catch(TransformerConfigurationException e1){
            if(_xsltcFactory==null){
                createXSLTCTransformerFactory();
            }
            ErrorListener errorListener=_xsltcFactory.getErrorListener();
            if(errorListener!=null){
                try{
                    errorListener.fatalError(e1);
                    return null;
                }catch(TransformerException e2){
                    new TransformerConfigurationException(e2);
                }
            }
            throw e1;
        }
    }    public void setURIResolver(URIResolver resolver){
        _uriresolver=resolver;
    }




















}
