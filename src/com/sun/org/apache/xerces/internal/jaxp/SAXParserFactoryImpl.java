/**
 * Copyright (c) 2005, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.org.apache.xerces.internal.jaxp;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import java.util.HashMap;
import java.util.Map;

public class SAXParserFactoryImpl extends SAXParserFactory{
    private static final String VALIDATION_FEATURE=
            Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
    private static final String NAMESPACES_FEATURE=
            Constants.SAX_FEATURE_PREFIX+Constants.NAMESPACES_FEATURE;
    private static final String XINCLUDE_FEATURE=
            Constants.XERCES_FEATURE_PREFIX+Constants.XINCLUDE_FEATURE;
    private Map<String,Boolean> features;
    private Schema grammar;
    private boolean isXIncludeAware;
    private boolean fSecureProcess=true;

    public SAXParser newSAXParser()
            throws ParserConfigurationException{
        SAXParser saxParserImpl;
        try{
            saxParserImpl=new SAXParserImpl(this,features,fSecureProcess);
        }catch(SAXException se){
            // Translate to ParserConfigurationException
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }

    private SAXParserImpl newSAXParserImpl()
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException{
        SAXParserImpl saxParserImpl;
        try{
            saxParserImpl=new SAXParserImpl(this,features);
        }catch(SAXNotSupportedException e){
            throw e;
        }catch(SAXNotRecognizedException e){
            throw e;
        }catch(SAXException se){
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }

    public void setFeature(String name,boolean value)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        // If this is the secure processing feature, save it then return.
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            if(System.getSecurityManager()!=null&&(!value)){
                throw new ParserConfigurationException(
                        SAXMessageFormatter.formatMessage(null,
                                "jaxp-secureprocessing-feature",null));
            }
            fSecureProcess=value;
            putInFeatures(name,value);
            return;
        }
        // XXX This is ugly.  We have to collect the features and then
        // later create an XMLReader to verify the features.
        putInFeatures(name,value);
        // Test the feature by possibly throwing SAX exceptions
        try{
            newSAXParserImpl();
        }catch(SAXNotSupportedException e){
            features.remove(name);
            throw e;
        }catch(SAXNotRecognizedException e){
            features.remove(name);
            throw e;
        }
    }

    public boolean getFeature(String name)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException{
        if(name==null){
            throw new NullPointerException();
        }
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            return fSecureProcess;
        }
        // Check for valid name by creating a dummy XMLReader to get
        // feature value
        return newSAXParserImpl().getXMLReader().getFeature(name);
    }

    public Schema getSchema(){
        return grammar;
    }

    public void setSchema(Schema grammar){
        this.grammar=grammar;
    }

    public boolean isXIncludeAware(){
        return getFromFeatures(XINCLUDE_FEATURE);
    }

    public void setXIncludeAware(boolean state){
        putInFeatures(XINCLUDE_FEATURE,state);
    }

    public void setValidating(boolean validating){
        putInFeatures(VALIDATION_FEATURE,validating);
    }

    public boolean isValidating(){
        return getFromFeatures(VALIDATION_FEATURE);
    }

    private void putInFeatures(String name,boolean value){
        if(features==null){
            features=new HashMap<>();
        }
        features.put(name,value?Boolean.TRUE:Boolean.FALSE);
    }

    private boolean getFromFeatures(String name){
        if(features==null){
            return false;
        }else{
            Boolean value=features.get(name);
            return (value==null)?false:value;
        }
    }

    public boolean isNamespaceAware(){
        return getFromFeatures(NAMESPACES_FEATURE);
    }

    public void setNamespaceAware(boolean awareness){
        putInFeatures(NAMESPACES_FEATURE,awareness);
    }
}
