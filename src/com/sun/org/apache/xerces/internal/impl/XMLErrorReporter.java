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
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;
import com.sun.org.apache.xerces.internal.util.ErrorHandlerProxy;
import com.sun.org.apache.xerces.internal.util.MessageFormatter;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.*;
import org.xml.sax.ErrorHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class XMLErrorReporter
        implements XMLComponent{
    //
    // Constants
    //
    // severity
    public static final short SEVERITY_WARNING=0;
    public static final short SEVERITY_ERROR=1;
    public static final short SEVERITY_FATAL_ERROR=2;
    // feature identifiers
    protected static final String CONTINUE_AFTER_FATAL_ERROR=
            Constants.XERCES_FEATURE_PREFIX+Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    // property identifiers
    protected static final String ERROR_HANDLER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_HANDLER_PROPERTY;
    // recognized features and properties
    private static final String[] RECOGNIZED_FEATURES={
            CONTINUE_AFTER_FATAL_ERROR,
    };
    private static final Boolean[] FEATURE_DEFAULTS={
            null,
    };
    private static final String[] RECOGNIZED_PROPERTIES={
            ERROR_HANDLER,
    };
    private static final Object[] PROPERTY_DEFAULTS={
            null,
    };
    //
    // Data
    //
    protected Locale fLocale;
    protected Map<String,MessageFormatter> fMessageFormatters;
    protected XMLErrorHandler fErrorHandler;
    protected XMLLocator fLocator;
    protected boolean fContinueAfterFatalError;
    protected XMLErrorHandler fDefaultErrorHandler;
    private ErrorHandler fSaxProxy=null;
    //
    // Constructors
    //

    public XMLErrorReporter(){
        // REVISIT: [Q] Should the locator be passed to the reportError
        //              method? Otherwise, there is no way for a parser
        //              component to store information about where an
        //              error occurred so as to report it later.
        //
        //              An example would be to record the location of
        //              IDREFs so that, at the end of the document, if
        //              there is no associated ID declared, the error
        //              could report the location information of the
        //              reference. -Ac
        //
        // NOTE: I added another reportError method that allows the
        //       caller to specify the location of the error being
        //       reported. -Ac
        fMessageFormatters=new HashMap<>();
    } // <init>()
    //
    // Methods
    //

    public Locale getLocale(){
        return fLocale;
    } // getLocale():  Locale

    public void setLocale(Locale locale){
        fLocale=locale;
    } // setLocale(Locale)

    public void setDocumentLocator(XMLLocator locator){
        fLocator=locator;
    } // setDocumentLocator(XMLLocator)

    public void putMessageFormatter(String domain,
                                    MessageFormatter messageFormatter){
        fMessageFormatters.put(domain,messageFormatter);
    } // putMessageFormatter(String,MessageFormatter)

    public MessageFormatter removeMessageFormatter(String domain){
        return fMessageFormatters.remove(domain);
    } // removeMessageFormatter(String):MessageFormatter

    public String reportError(String domain,String key,Object[] arguments,
                              short severity) throws XNIException{
        return reportError(fLocator,domain,key,arguments,severity);
    } // reportError(String,String,Object[],short):String

    public String reportError(XMLLocator location,
                              String domain,String key,Object[] arguments,
                              short severity) throws XNIException{
        return reportError(location,domain,key,arguments,severity,null);
    } // reportError(XMLLocator,String,String,Object[],short):String

    public String reportError(XMLLocator location,
                              String domain,String key,Object[] arguments,
                              short severity,Exception exception) throws XNIException{
        // REVISIT: [Q] Should we do anything about invalid severity
        //              parameter? -Ac
        // format error message and create parse exception
        MessageFormatter messageFormatter=getMessageFormatter(domain);
        String message;
        if(messageFormatter!=null){
            message=messageFormatter.formatMessage(fLocale,key,arguments);
        }else{
            StringBuffer str=new StringBuffer();
            str.append(domain);
            str.append('#');
            str.append(key);
            int argCount=arguments!=null?arguments.length:0;
            if(argCount>0){
                str.append('?');
                for(int i=0;i<argCount;i++){
                    str.append(arguments[i]);
                    if(i<argCount-1){
                        str.append('&');
                    }
                }
            }
            message=str.toString();
        }
        XMLParseException parseException=(exception!=null)?
                new XMLParseException(location,message,exception):
                new XMLParseException(location,message);
        // get error handler
        XMLErrorHandler errorHandler=fErrorHandler;
        if(errorHandler==null){
            if(fDefaultErrorHandler==null){
                fDefaultErrorHandler=new DefaultErrorHandler();
            }
            errorHandler=fDefaultErrorHandler;
        }
        // call error handler
        switch(severity){
            case SEVERITY_WARNING:{
                errorHandler.warning(domain,key,parseException);
                break;
            }
            case SEVERITY_ERROR:{
                errorHandler.error(domain,key,parseException);
                break;
            }
            case SEVERITY_FATAL_ERROR:{
                errorHandler.fatalError(domain,key,parseException);
                if(!fContinueAfterFatalError){
                    throw parseException;
                }
                break;
            }
        }
        return message;
    } // reportError(XMLLocator,String,String,Object[],short,Exception):String

    public MessageFormatter getMessageFormatter(String domain){
        return fMessageFormatters.get(domain);
    } // getMessageFormatter(String):MessageFormatter

    public String reportError(String domain,String key,Object[] arguments,
                              short severity,Exception exception) throws XNIException{
        return reportError(fLocator,domain,key,arguments,severity,exception);
    } // reportError(String,String,Object[],short,Exception):String
    //
    // XMLComponent methods
    //

    public void reset(XMLComponentManager componentManager)
            throws XNIException{
        // features
        fContinueAfterFatalError=componentManager.getFeature(CONTINUE_AFTER_FATAL_ERROR,false);
        // properties
        fErrorHandler=(XMLErrorHandler)componentManager.getProperty(ERROR_HANDLER);
    } // reset(XMLComponentManager)

    public String[] getRecognizedFeatures(){
        return (String[])(RECOGNIZED_FEATURES.clone());
    } // getRecognizedFeatures():String[]

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        //
        // Xerces features
        //
        if(featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)){
            final int suffixLength=featureId.length()-Constants.XERCES_FEATURE_PREFIX.length();
            //
            // http://apache.org/xml/features/continue-after-fatal-error
            //   Allows the parser to continue after a fatal error.
            //   Normally, a fatal error would stop the parse.
            //
            if(suffixLength==Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE.length()&&
                    featureId.endsWith(Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE)){
                fContinueAfterFatalError=state;
            }
        }
    } // setFeature(String,boolean)

    public String[] getRecognizedProperties(){
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        //
        // Xerces properties
        //
        if(propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.XERCES_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.ERROR_HANDLER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.ERROR_HANDLER_PROPERTY)){
                fErrorHandler=(XMLErrorHandler)value;
            }
        }
    } // setProperty(String,Object)

    public Boolean getFeatureDefault(String featureId){
        for(int i=0;i<RECOGNIZED_FEATURES.length;i++){
            if(RECOGNIZED_FEATURES[i].equals(featureId)){
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean

    public Object getPropertyDefault(String propertyId){
        for(int i=0;i<RECOGNIZED_PROPERTIES.length;i++){
            if(RECOGNIZED_PROPERTIES[i].equals(propertyId)){
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object

    // return state of given feature or false if unsupported.
    public boolean getFeature(String featureId)
            throws XMLConfigurationException{
        //
        // Xerces features
        //
        if(featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)){
            final int suffixLength=featureId.length()-Constants.XERCES_FEATURE_PREFIX.length();
            //
            // http://apache.org/xml/features/continue-after-fatal-error
            //   Allows the parser to continue after a fatal error.
            //   Normally, a fatal error would stop the parse.
            //
            if(suffixLength==Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE.length()&&
                    featureId.endsWith(Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE)){
                return fContinueAfterFatalError;
            }
        }
        return false;
    } // setFeature(String,boolean)

    public XMLErrorHandler getErrorHandler(){
        return fErrorHandler;
    }

    public ErrorHandler getSAXErrorHandler(){
        if(fSaxProxy==null){
            fSaxProxy=new ErrorHandlerProxy(){
                protected XMLErrorHandler getErrorHandler(){
                    return fErrorHandler;
                }
            };
        }
        return fSaxProxy;
    }
} // class XMLErrorReporter
