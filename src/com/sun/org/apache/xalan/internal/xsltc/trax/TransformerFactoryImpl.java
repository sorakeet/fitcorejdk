/**
 * Copyright (c) 2007, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: TransformerFactoryImpl.java,v 1.8 2007/04/09 21:30:41 joehw Exp $
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
 * $Id: TransformerFactoryImpl.java,v 1.8 2007/04/09 21:30:41 joehw Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.XalanConstants;
import com.sun.org.apache.xalan.internal.utils.*;
import com.sun.org.apache.xalan.internal.utils.FeaturePropertyBase.State;
import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xalan.internal.utils.XMLSecurityPropertyManager.Property;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader;
import com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import com.sun.org.apache.xml.internal.utils.StopParseException;
import com.sun.org.apache.xml.internal.utils.StylesheetPIHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.*;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TransformerFactoryImpl
        extends SAXTransformerFactory implements SourceLoader, ErrorListener{
    // Public constants for attributes supported by the XSLTC TransformerFactory.
    public final static String TRANSLET_NAME="translet-name";
    public final static String DESTINATION_DIRECTORY="destination-directory";
    public final static String PACKAGE_NAME="package-name";
    public final static String JAR_NAME="jar-name";
    public final static String GENERATE_TRANSLET="generate-translet";
    public final static String AUTO_TRANSLET="auto-translet";
    public final static String USE_CLASSPATH="use-classpath";
    public final static String DEBUG="debug";
    public final static String ENABLE_INLINING="enable-inlining";
    public final static String INDENT_NUMBER="indent-number";
    protected final static String DEFAULT_TRANSLET_NAME="GregorSamsa";
    private final FeatureManager _featureManager;
    private ErrorListener _errorListener=this;
    private URIResolver _uriResolver=null;
    private String _transletName=DEFAULT_TRANSLET_NAME;
    private String _destinationDirectory=null;
    private String _packageName=null;
    private String _jarFileName=null;
    private Map<Source,PIParamWrapper> _piParams=null;
    private boolean _debug=false;
    private boolean _enableInlining=false;
    private boolean _generateTranslet=false;
    private boolean _autoTranslet=false;
    private boolean _useClasspath=false;
    private int _indentNumber=-1;
    private boolean _isNotSecureProcessing=true;
    private boolean _isSecureMode=false;
    private boolean _useServicesMechanism;
    private String _accessExternalStylesheet=XalanConstants.EXTERNAL_ACCESS_DEFAULT;
    private String _accessExternalDTD=XalanConstants.EXTERNAL_ACCESS_DEFAULT;
    private XMLSecurityPropertyManager _xmlSecurityPropertyMgr;
    private XMLSecurityManager _xmlSecurityManager;
    private ClassLoader _extensionClassLoader=null;
    // Unmodifiable view of external extension function from xslt compiler
    // It will be populated by user-specified extension functions during the
    // type checking
    private Map<String,Class> _xsltcExtensionFunctions;
    public TransformerFactoryImpl(){
        this(true);
    }

    private TransformerFactoryImpl(boolean useServicesMechanism){
        this._useServicesMechanism=useServicesMechanism;
        _featureManager=new FeatureManager();
        if(System.getSecurityManager()!=null){
            _isSecureMode=true;
            _isNotSecureProcessing=false;
            _featureManager.setValue(FeatureManager.Feature.ORACLE_ENABLE_EXTENSION_FUNCTION,
                    State.FSP,XalanConstants.FEATURE_FALSE);
        }
        _xmlSecurityPropertyMgr=new XMLSecurityPropertyManager();
        _accessExternalDTD=_xmlSecurityPropertyMgr.getValue(
                Property.ACCESS_EXTERNAL_DTD);
        _accessExternalStylesheet=_xmlSecurityPropertyMgr.getValue(
                Property.ACCESS_EXTERNAL_STYLESHEET);
        //Parser's security manager
        _xmlSecurityManager=new XMLSecurityManager(true);
        //Unmodifiable hash map with loaded external extension functions
        _xsltcExtensionFunctions=null;
    }

    public static TransformerFactory newTransformerFactoryNoServiceLoader(){
        return new TransformerFactoryImpl(false);
    }

    public Map<String,Class> getExternalExtensionsMap(){
        return _xsltcExtensionFunctions;
    }

    public boolean useServicesMechnism(){
        return _useServicesMechanism;
    }

    public FeatureManager getFeatureManager(){
        return _featureManager;
    }    @Override
    public void setErrorListener(ErrorListener listener)
            throws IllegalArgumentException{
        if(listener==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.ERROR_LISTENER_NULL_ERR,
                    "TransformerFactory");
            throw new IllegalArgumentException(err.toString());
        }
        _errorListener=listener;
    }

    @Override
    public TransformerHandler newTransformerHandler(Source src)
            throws TransformerConfigurationException{
        final Transformer transformer=newTransformer(src);
        if(_uriResolver!=null){
            transformer.setURIResolver(_uriResolver);
        }
        return new TransformerHandlerImpl((TransformerImpl)transformer);
    }    @Override
    public ErrorListener getErrorListener(){
        return _errorListener;
    }

    @Override
    public Transformer newTransformer(Source source) throws
            TransformerConfigurationException{
        final Templates templates=newTemplates(source);
        final Transformer transformer=templates.newTransformer();
        if(_uriResolver!=null){
            transformer.setURIResolver(_uriResolver);
        }
        return (transformer);
    }    @Override
    public Object getAttribute(String name)
            throws IllegalArgumentException{
        // Return value for attribute 'translet-name'
        if(name.equals(TRANSLET_NAME)){
            return _transletName;
        }else if(name.equals(GENERATE_TRANSLET)){
            return new Boolean(_generateTranslet);
        }else if(name.equals(AUTO_TRANSLET)){
            return new Boolean(_autoTranslet);
        }else if(name.equals(ENABLE_INLINING)){
            if(_enableInlining)
                return Boolean.TRUE;
            else
                return Boolean.FALSE;
        }else if(name.equals(XalanConstants.SECURITY_MANAGER)){
            return _xmlSecurityManager;
        }else if(name.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)){
            return _extensionClassLoader;
        }
        /** Check to see if the property is managed by the security manager **/
        String propertyValue=(_xmlSecurityManager!=null)?
                _xmlSecurityManager.getLimitAsString(name):null;
        if(propertyValue!=null){
            return propertyValue;
        }else{
            propertyValue=(_xmlSecurityPropertyMgr!=null)?
                    _xmlSecurityPropertyMgr.getValue(name):null;
            if(propertyValue!=null){
                return propertyValue;
            }
        }
        // Throw an exception for all other attributes
        ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_INVALID_ATTR_ERR,name);
        throw new IllegalArgumentException(err.toString());
    }

    @Override
    public Transformer newTransformer()
            throws TransformerConfigurationException{
        TransformerImpl result=new TransformerImpl(new Properties(),
                _indentNumber,this);
        if(_uriResolver!=null){
            result.setURIResolver(_uriResolver);
        }
        if(!_isNotSecureProcessing){
            result.setSecureProcessing(true);
        }
        return result;
    }    @Override
    public void setAttribute(String name,Object value)
            throws IllegalArgumentException{
        // Set the default translet name (ie. class name), which will be used
        // for translets that cannot be given a name from their system-id.
        if(name.equals(TRANSLET_NAME)&&value instanceof String){
            _transletName=(String)value;
            return;
        }else if(name.equals(DESTINATION_DIRECTORY)&&value instanceof String){
            _destinationDirectory=(String)value;
            return;
        }else if(name.equals(PACKAGE_NAME)&&value instanceof String){
            _packageName=(String)value;
            return;
        }else if(name.equals(JAR_NAME)&&value instanceof String){
            _jarFileName=(String)value;
            return;
        }else if(name.equals(GENERATE_TRANSLET)){
            if(value instanceof Boolean){
                _generateTranslet=((Boolean)value).booleanValue();
                return;
            }else if(value instanceof String){
                _generateTranslet=((String)value).equalsIgnoreCase("true");
                return;
            }
        }else if(name.equals(AUTO_TRANSLET)){
            if(value instanceof Boolean){
                _autoTranslet=((Boolean)value).booleanValue();
                return;
            }else if(value instanceof String){
                _autoTranslet=((String)value).equalsIgnoreCase("true");
                return;
            }
        }else if(name.equals(USE_CLASSPATH)){
            if(value instanceof Boolean){
                _useClasspath=((Boolean)value).booleanValue();
                return;
            }else if(value instanceof String){
                _useClasspath=((String)value).equalsIgnoreCase("true");
                return;
            }
        }else if(name.equals(DEBUG)){
            if(value instanceof Boolean){
                _debug=((Boolean)value).booleanValue();
                return;
            }else if(value instanceof String){
                _debug=((String)value).equalsIgnoreCase("true");
                return;
            }
        }else if(name.equals(ENABLE_INLINING)){
            if(value instanceof Boolean){
                _enableInlining=((Boolean)value).booleanValue();
                return;
            }else if(value instanceof String){
                _enableInlining=((String)value).equalsIgnoreCase("true");
                return;
            }
        }else if(name.equals(INDENT_NUMBER)){
            if(value instanceof String){
                try{
                    _indentNumber=Integer.parseInt((String)value);
                    return;
                }catch(NumberFormatException e){
                    // Falls through
                }
            }else if(value instanceof Integer){
                _indentNumber=((Integer)value).intValue();
                return;
            }
        }else if(name.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)){
            if(value instanceof ClassLoader){
                _extensionClassLoader=(ClassLoader)value;
                return;
            }else{
                final ErrorMsg err
                        =new ErrorMsg(ErrorMsg.JAXP_INVALID_ATTR_VALUE_ERR,"Extension Functions ClassLoader");
                throw new IllegalArgumentException(err.toString());
            }
        }
        if(_xmlSecurityManager!=null&&
                _xmlSecurityManager.setLimit(name,XMLSecurityManager.State.APIPROPERTY,value)){
            return;
        }
        if(_xmlSecurityPropertyMgr!=null&&
                _xmlSecurityPropertyMgr.setValue(name,State.APIPROPERTY,value)){
            _accessExternalDTD=_xmlSecurityPropertyMgr.getValue(
                    Property.ACCESS_EXTERNAL_DTD);
            _accessExternalStylesheet=_xmlSecurityPropertyMgr.getValue(
                    Property.ACCESS_EXTERNAL_STYLESHEET);
            return;
        }
        // Throw an exception for all other attributes
        final ErrorMsg err
                =new ErrorMsg(ErrorMsg.JAXP_INVALID_ATTR_ERR,name);
        throw new IllegalArgumentException(err.toString());
    }

    @Override
    public Templates newTemplates(Source source)
            throws TransformerConfigurationException{
        // If the _useClasspath attribute is true, try to load the translet from
        // the CLASSPATH and create a template object using the loaded
        // translet.
        if(_useClasspath){
            String transletName=getTransletBaseName(source);
            if(_packageName!=null)
                transletName=_packageName+"."+transletName;
            try{
                final Class clazz=ObjectFactory.findProviderClass(transletName,true);
                resetTransientAttributes();
                return new TemplatesImpl(new Class[]{clazz},transletName,null,_indentNumber,this);
            }catch(ClassNotFoundException cnfe){
                ErrorMsg err=new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR,transletName);
                throw new TransformerConfigurationException(err.toString());
            }catch(Exception e){
                ErrorMsg err=new ErrorMsg(
                        new ErrorMsg(ErrorMsg.RUNTIME_ERROR_KEY)
                                +e.getMessage());
                throw new TransformerConfigurationException(err.toString());
            }
        }
        // If _autoTranslet is true, we will try to load the bytecodes
        // from the translet classes without compiling the stylesheet.
        if(_autoTranslet){
            byte[][] bytecodes;
            String transletClassName=getTransletBaseName(source);
            if(_packageName!=null)
                transletClassName=_packageName+"."+transletClassName;
            if(_jarFileName!=null)
                bytecodes=getBytecodesFromJar(source,transletClassName);
            else
                bytecodes=getBytecodesFromClasses(source,transletClassName);
            if(bytecodes!=null){
                if(_debug){
                    if(_jarFileName!=null)
                        System.err.println(new ErrorMsg(
                                ErrorMsg.TRANSFORM_WITH_JAR_STR,transletClassName,_jarFileName));
                    else
                        System.err.println(new ErrorMsg(
                                ErrorMsg.TRANSFORM_WITH_TRANSLET_STR,transletClassName));
                }
                // Reset the per-session attributes to their default values
                // after each newTemplates() call.
                resetTransientAttributes();
                return new TemplatesImpl(bytecodes,transletClassName,null,_indentNumber,this);
            }
        }
        // Create and initialize a stylesheet compiler
        final XSLTC xsltc=new XSLTC(_useServicesMechanism,_featureManager);
        if(_debug) xsltc.setDebug(true);
        if(_enableInlining)
            xsltc.setTemplateInlining(true);
        else
            xsltc.setTemplateInlining(false);
        if(!_isNotSecureProcessing) xsltc.setSecureProcessing(true);
        xsltc.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,_accessExternalStylesheet);
        xsltc.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,_accessExternalDTD);
        xsltc.setProperty(XalanConstants.SECURITY_MANAGER,_xmlSecurityManager);
        xsltc.setProperty(XalanConstants.JDK_EXTENSION_CLASSLOADER,_extensionClassLoader);
        xsltc.init();
        if(!_isNotSecureProcessing)
            _xsltcExtensionFunctions=xsltc.getExternalExtensionFunctions();
        // Set a document loader (for xsl:include/import) if defined
        if(_uriResolver!=null){
            xsltc.setSourceLoader(this);
        }
        // Pass parameters to the Parser to make sure it locates the correct
        // <?xml-stylesheet ...?> PI in an XML input document
        if((_piParams!=null)&&(_piParams.get(source)!=null)){
            // Get the parameters for this Source object
            PIParamWrapper p=_piParams.get(source);
            // Pass them on to the compiler (which will pass then to the parser)
            if(p!=null){
                xsltc.setPIParameters(p._media,p._title,p._charset);
            }
        }
        // Set the attributes for translet generation
        int outputType=XSLTC.BYTEARRAY_OUTPUT;
        if(_generateTranslet||_autoTranslet){
            // Set the translet name
            xsltc.setClassName(getTransletBaseName(source));
            if(_destinationDirectory!=null)
                xsltc.setDestDirectory(_destinationDirectory);
            else{
                String xslName=getStylesheetFileName(source);
                if(xslName!=null){
                    File xslFile=new File(xslName);
                    String xslDir=xslFile.getParent();
                    if(xslDir!=null)
                        xsltc.setDestDirectory(xslDir);
                }
            }
            if(_packageName!=null)
                xsltc.setPackageName(_packageName);
            if(_jarFileName!=null){
                xsltc.setJarFileName(_jarFileName);
                outputType=XSLTC.BYTEARRAY_AND_JAR_OUTPUT;
            }else
                outputType=XSLTC.BYTEARRAY_AND_FILE_OUTPUT;
        }
        // Compile the stylesheet
        final InputSource input=Util.getInputSource(xsltc,source);
        byte[][] bytecodes=xsltc.compile(null,input,outputType);
        final String transletName=xsltc.getClassName();
        // Output to the jar file if the jar file name is set.
        if((_generateTranslet||_autoTranslet)
                &&bytecodes!=null&&_jarFileName!=null){
            try{
                xsltc.outputToJar();
            }catch(IOException e){
            }
        }
        // Reset the per-session attributes to their default values
        // after each newTemplates() call.
        resetTransientAttributes();
        // Pass compiler warnings to the error listener
        if(_errorListener!=this){
            try{
                passWarningsToListener(xsltc.getWarnings());
            }catch(TransformerException e){
                throw new TransformerConfigurationException(e);
            }
        }else{
            xsltc.printWarnings();
        }
        // Check that the transformation went well before returning
        if(bytecodes==null){
            Vector errs=xsltc.getErrors();
            ErrorMsg err;
            if(errs!=null){
                err=(ErrorMsg)errs.elementAt(errs.size()-1);
            }else{
                err=new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR);
            }
            Throwable cause=err.getCause();
            TransformerConfigurationException exc;
            if(cause!=null){
                exc=new TransformerConfigurationException(cause.getMessage(),cause);
            }else{
                exc=new TransformerConfigurationException(err.toString());
            }
            // Pass compiler errors to the error listener
            if(_errorListener!=null){
                passErrorsToListener(xsltc.getErrors());
                // As required by TCK 1.2, send a fatalError to the
                // error listener because compilation of the stylesheet
                // failed and no further processing will be possible.
                try{
                    _errorListener.fatalError(exc);
                }catch(TransformerException te){
                    // well, we tried.
                }
            }else{
                xsltc.printErrors();
            }
            throw exc;
        }
        return new TemplatesImpl(bytecodes,transletName,
                xsltc.getOutputProperties(),_indentNumber,this);
    }    @Override
    public void setFeature(String name,boolean value)
            throws TransformerConfigurationException{
        // feature name cannot be null
        if(name==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_SET_FEATURE_NULL_NAME);
            throw new NullPointerException(err.toString());
        }
        // secure processing?
        else if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            if((_isSecureMode)&&(!value)){
                ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_SECUREPROCESSING_FEATURE);
                throw new TransformerConfigurationException(err.toString());
            }
            _isNotSecureProcessing=!value;
            _xmlSecurityManager.setSecureProcessing(value);
            // set external access restriction when FSP is explicitly set
            if(value&&XalanConstants.IS_JDK8_OR_ABOVE){
                _xmlSecurityPropertyMgr.setValue(Property.ACCESS_EXTERNAL_DTD,
                        State.FSP,XalanConstants.EXTERNAL_ACCESS_DEFAULT_FSP);
                _xmlSecurityPropertyMgr.setValue(Property.ACCESS_EXTERNAL_STYLESHEET,
                        State.FSP,XalanConstants.EXTERNAL_ACCESS_DEFAULT_FSP);
                _accessExternalDTD=_xmlSecurityPropertyMgr.getValue(
                        Property.ACCESS_EXTERNAL_DTD);
                _accessExternalStylesheet=_xmlSecurityPropertyMgr.getValue(
                        Property.ACCESS_EXTERNAL_STYLESHEET);
            }
            if(value&&_featureManager!=null){
                _featureManager.setValue(FeatureManager.Feature.ORACLE_ENABLE_EXTENSION_FUNCTION,
                        State.FSP,XalanConstants.FEATURE_FALSE);
            }
            return;
        }else if(name.equals(XalanConstants.ORACLE_FEATURE_SERVICE_MECHANISM)){
            //in secure mode, let _useServicesMechanism be determined by the constructor
            if(!_isSecureMode)
                _useServicesMechanism=value;
        }else{
            if(_featureManager!=null&&
                    _featureManager.setValue(name,State.APIPROPERTY,value)){
                return;
            }
            // unknown feature
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_UNSUPPORTED_FEATURE,name);
            throw new TransformerConfigurationException(err.toString());
        }
    }

    @Override
    public Source getAssociatedStylesheet(Source source,String media,
                                          String title,String charset)
            throws TransformerConfigurationException{
        String baseId;
        XMLReader reader;
        InputSource isource;
        /**
         * Fix for bugzilla bug 24187
         */
        StylesheetPIHandler _stylesheetPIHandler=new StylesheetPIHandler(null,media,title,charset);
        try{
            if(source instanceof DOMSource){
                final DOMSource domsrc=(DOMSource)source;
                baseId=domsrc.getSystemId();
                final org.w3c.dom.Node node=domsrc.getNode();
                final DOM2SAX dom2sax=new DOM2SAX(node);
                _stylesheetPIHandler.setBaseId(baseId);
                dom2sax.setContentHandler(_stylesheetPIHandler);
                dom2sax.parse();
            }else{
                isource=SAXSource.sourceToInputSource(source);
                baseId=isource.getSystemId();
                SAXParserFactory factory=FactoryImpl.getSAXFactory(_useServicesMechanism);
                factory.setNamespaceAware(true);
                if(!_isNotSecureProcessing){
                    try{
                        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
                    }catch(org.xml.sax.SAXException e){
                    }
                }
                SAXParser jaxpParser=factory.newSAXParser();
                reader=jaxpParser.getXMLReader();
                if(reader==null){
                    reader=XMLReaderFactory.createXMLReader();
                }
                _stylesheetPIHandler.setBaseId(baseId);
                reader.setContentHandler(_stylesheetPIHandler);
                reader.parse(isource);
            }
            if(_uriResolver!=null){
                _stylesheetPIHandler.setURIResolver(_uriResolver);
            }
        }catch(StopParseException e){
            // startElement encountered so do not parse further
        }catch(javax.xml.parsers.ParserConfigurationException e){
            throw new TransformerConfigurationException(
                    "getAssociatedStylesheets failed",e);
        }catch(org.xml.sax.SAXException se){
            throw new TransformerConfigurationException(
                    "getAssociatedStylesheets failed",se);
        }catch(IOException ioe){
            throw new TransformerConfigurationException(
                    "getAssociatedStylesheets failed",ioe);
        }
        return _stylesheetPIHandler.getAssociatedStylesheet();
    }    @Override
    public boolean getFeature(String name){
        // All supported features should be listed here
        String[] features={
                DOMSource.FEATURE,
                DOMResult.FEATURE,
                SAXSource.FEATURE,
                SAXResult.FEATURE,
                StAXSource.FEATURE,
                StAXResult.FEATURE,
                StreamSource.FEATURE,
                StreamResult.FEATURE,
                SAXTransformerFactory.FEATURE,
                SAXTransformerFactory.FEATURE_XMLFILTER,
                XalanConstants.ORACLE_FEATURE_SERVICE_MECHANISM
        };
        // feature name cannot be null
        if(name==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_GET_FEATURE_NULL_NAME);
            throw new NullPointerException(err.toString());
        }
        // Inefficient, but array is small
        for(int i=0;i<features.length;i++){
            if(name.equals(features[i])){
                return true;
            }
        }
        // secure processing?
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            return !_isNotSecureProcessing;
        }
        /** Check to see if the property is managed by the security manager **/
        String propertyValue=(_featureManager!=null)?
                _featureManager.getValueAsString(name):null;
        if(propertyValue!=null){
            return Boolean.parseBoolean(propertyValue);
        }
        // Feature not supported
        return false;
    }

    private void passWarningsToListener(Vector messages)
            throws TransformerException{
        if(_errorListener==null||messages==null){
            return;
        }
        // Pass messages to listener, one by one
        final int count=messages.size();
        for(int pos=0;pos<count;pos++){
            ErrorMsg msg=(ErrorMsg)messages.elementAt(pos);
            // Workaround for the TCK failure ErrorListener.errorTests.error001.
            if(msg.isWarningError())
                _errorListener.error(
                        new TransformerConfigurationException(msg.toString()));
            else
                _errorListener.warning(
                        new TransformerConfigurationException(msg.toString()));
        }
    }

    private void passErrorsToListener(Vector messages){
        try{
            if(_errorListener==null||messages==null){
                return;
            }
            // Pass messages to listener, one by one
            final int count=messages.size();
            for(int pos=0;pos<count;pos++){
                String message=messages.elementAt(pos).toString();
                _errorListener.error(new TransformerException(message));
            }
        }catch(TransformerException e){
            // nada
        }
    }

    private void resetTransientAttributes(){
        _transletName=DEFAULT_TRANSLET_NAME;
        _destinationDirectory=null;
        _packageName=null;
        _jarFileName=null;
    }    @Override
    public URIResolver getURIResolver(){
        return _uriResolver;
    }

    private byte[][] getBytecodesFromClasses(Source source,String fullClassName){
        if(fullClassName==null)
            return null;
        String xslFileName=getStylesheetFileName(source);
        File xslFile=null;
        if(xslFileName!=null)
            xslFile=new File(xslFileName);
        // Find the base name of the translet
        final String transletName;
        int lastDotIndex=fullClassName.lastIndexOf('.');
        if(lastDotIndex>0)
            transletName=fullClassName.substring(lastDotIndex+1);
        else
            transletName=fullClassName;
        // Construct the path name for the translet class file
        String transletPath=fullClassName.replace('.','/');
        if(_destinationDirectory!=null){
            transletPath=_destinationDirectory+"/"+transletPath+".class";
        }else{
            if(xslFile!=null&&xslFile.getParent()!=null)
                transletPath=xslFile.getParent()+"/"+transletPath+".class";
            else
                transletPath=transletPath+".class";
        }
        // Return null if the translet class file does not exist.
        File transletFile=new File(transletPath);
        if(!transletFile.exists())
            return null;
        // Compare the timestamps of the translet and the xsl file.
        // If the translet is older than the xsl file, return null
        // so that the xsl file is used for the transformation and
        // the translet is regenerated.
        if(xslFile!=null&&xslFile.exists()){
            long xslTimestamp=xslFile.lastModified();
            long transletTimestamp=transletFile.lastModified();
            if(transletTimestamp<xslTimestamp)
                return null;
        }
        // Load the translet into a bytecode array.
        Vector bytecodes=new Vector();
        int fileLength=(int)transletFile.length();
        if(fileLength>0){
            FileInputStream input;
            try{
                input=new FileInputStream(transletFile);
            }catch(FileNotFoundException e){
                return null;
            }
            byte[] bytes=new byte[fileLength];
            try{
                readFromInputStream(bytes,input,fileLength);
                input.close();
            }catch(IOException e){
                return null;
            }
            bytecodes.addElement(bytes);
        }else
            return null;
        // Find the parent directory of the translet.
        String transletParentDir=transletFile.getParent();
        if(transletParentDir==null)
            transletParentDir=SecuritySupport.getSystemProperty("user.dir");
        File transletParentFile=new File(transletParentDir);
        // Find all the auxiliary files which have a name pattern of "transletClass$nnn.class".
        final String transletAuxPrefix=transletName+"$";
        File[] auxfiles=transletParentFile.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir,String name){
                return (name.endsWith(".class")&&name.startsWith(transletAuxPrefix));
            }
        });
        // Load the auxiliary class files and add them to the bytecode array.
        for(int i=0;i<auxfiles.length;i++){
            File auxfile=auxfiles[i];
            int auxlength=(int)auxfile.length();
            if(auxlength>0){
                FileInputStream auxinput=null;
                try{
                    auxinput=new FileInputStream(auxfile);
                }catch(FileNotFoundException e){
                    continue;
                }
                byte[] bytes=new byte[auxlength];
                try{
                    readFromInputStream(bytes,auxinput,auxlength);
                    auxinput.close();
                }catch(IOException e){
                    continue;
                }
                bytecodes.addElement(bytes);
            }
        }
        // Convert the Vector of byte[] to byte[][].
        final int count=bytecodes.size();
        if(count>0){
            final byte[][] result=new byte[count][1];
            for(int i=0;i<count;i++){
                result[i]=(byte[])bytecodes.elementAt(i);
            }
            return result;
        }else
            return null;
    }    @Override
    public void setURIResolver(URIResolver resolver){
        _uriResolver=resolver;
    }

    private byte[][] getBytecodesFromJar(Source source,String fullClassName){
        String xslFileName=getStylesheetFileName(source);
        File xslFile=null;
        if(xslFileName!=null)
            xslFile=new File(xslFileName);
        // Construct the path for the jar file
        String jarPath;
        if(_destinationDirectory!=null)
            jarPath=_destinationDirectory+"/"+_jarFileName;
        else{
            if(xslFile!=null&&xslFile.getParent()!=null)
                jarPath=xslFile.getParent()+"/"+_jarFileName;
            else
                jarPath=_jarFileName;
        }
        // Return null if the jar file does not exist.
        File file=new File(jarPath);
        if(!file.exists())
            return null;
        // Compare the timestamps of the jar file and the xsl file. Return null
        // if the xsl file is newer than the jar file.
        if(xslFile!=null&&xslFile.exists()){
            long xslTimestamp=xslFile.lastModified();
            long transletTimestamp=file.lastModified();
            if(transletTimestamp<xslTimestamp)
                return null;
        }
        // Create a ZipFile object for the jar file
        ZipFile jarFile;
        try{
            jarFile=new ZipFile(file);
        }catch(IOException e){
            return null;
        }
        String transletPath=fullClassName.replace('.','/');
        String transletAuxPrefix=transletPath+"$";
        String transletFullName=transletPath+".class";
        Vector bytecodes=new Vector();
        // Iterate through all entries in the jar file to find the
        // translet and auxiliary classes.
        Enumeration entries=jarFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry=(ZipEntry)entries.nextElement();
            String entryName=entry.getName();
            if(entry.getSize()>0&&
                    (entryName.equals(transletFullName)||
                            (entryName.endsWith(".class")&&
                                    entryName.startsWith(transletAuxPrefix)))){
                try{
                    InputStream input=jarFile.getInputStream(entry);
                    int size=(int)entry.getSize();
                    byte[] bytes=new byte[size];
                    readFromInputStream(bytes,input,size);
                    input.close();
                    bytecodes.addElement(bytes);
                }catch(IOException e){
                    return null;
                }
            }
        }
        // Convert the Vector of byte[] to byte[][].
        final int count=bytecodes.size();
        if(count>0){
            final byte[][] result=new byte[count][1];
            for(int i=0;i<count;i++){
                result[i]=(byte[])bytecodes.elementAt(i);
            }
            return result;
        }else
            return null;
    }

    private void readFromInputStream(byte[] bytes,InputStream input,int size)
            throws IOException{
        int n=0;
        int offset=0;
        int length=size;
        while(length>0&&(n=input.read(bytes,offset,length))>0){
            offset=offset+n;
            length=length-n;
        }
    }

    private String getStylesheetFileName(Source source){
        String systemId=source.getSystemId();
        if(systemId!=null){
            File file=new File(systemId);
            if(file.exists())
                return systemId;
            else{
                URL url;
                try{
                    url=new URL(systemId);
                }catch(MalformedURLException e){
                    return null;
                }
                if("file".equals(url.getProtocol()))
                    return url.getFile();
                else
                    return null;
            }
        }else
            return null;
    }

    private String getTransletBaseName(Source source){
        String transletBaseName=null;
        if(!_transletName.equals(DEFAULT_TRANSLET_NAME))
            return _transletName;
        else{
            String systemId=source.getSystemId();
            if(systemId!=null){
                String baseName=Util.baseName(systemId);
                if(baseName!=null){
                    baseName=Util.noExtName(baseName);
                    transletBaseName=Util.toJavaName(baseName);
                }
            }
        }
        return (transletBaseName!=null)?transletBaseName:DEFAULT_TRANSLET_NAME;
    }

    @Override
    public TransformerHandler newTransformerHandler(Templates templates)
            throws TransformerConfigurationException{
        final Transformer transformer=templates.newTransformer();
        final TransformerImpl internal=(TransformerImpl)transformer;
        return new TransformerHandlerImpl(internal);
    }

    @Override
    public TransformerHandler newTransformerHandler()
            throws TransformerConfigurationException{
        final Transformer transformer=newTransformer();
        if(_uriResolver!=null){
            transformer.setURIResolver(_uriResolver);
        }
        return new TransformerHandlerImpl((TransformerImpl)transformer);
    }

    @Override
    public TemplatesHandler newTemplatesHandler()
            throws TransformerConfigurationException{
        final TemplatesHandlerImpl handler=
                new TemplatesHandlerImpl(_indentNumber,this);
        if(_uriResolver!=null){
            handler.setURIResolver(_uriResolver);
        }
        return handler;
    }

    @Override
    public XMLFilter newXMLFilter(Source src)
            throws TransformerConfigurationException{
        Templates templates=newTemplates(src);
        if(templates==null) return null;
        return newXMLFilter(templates);
    }

    @Override
    public XMLFilter newXMLFilter(Templates templates)
            throws TransformerConfigurationException{
        try{
            return new TrAXFilter(templates);
        }catch(TransformerConfigurationException e1){
            if(_errorListener!=null){
                try{
                    _errorListener.fatalError(e1);
                    return null;
                }catch(TransformerException e2){
                    new TransformerConfigurationException(e2);
                }
            }
            throw e1;
        }
    }

    @Override
    public void warning(TransformerException e)
            throws TransformerException{
        Throwable wrapped=e.getException();
        if(wrapped!=null){
            System.err.println(new ErrorMsg(ErrorMsg.WARNING_PLUS_WRAPPED_MSG,
                    e.getMessageAndLocation(),
                    wrapped.getMessage()));
        }else{
            System.err.println(new ErrorMsg(ErrorMsg.WARNING_MSG,
                    e.getMessageAndLocation()));
        }
    }

    @Override
    public void error(TransformerException e)
            throws TransformerException{
        Throwable wrapped=e.getException();
        if(wrapped!=null){
            System.err.println(new ErrorMsg(ErrorMsg.ERROR_PLUS_WRAPPED_MSG,
                    e.getMessageAndLocation(),
                    wrapped.getMessage()));
        }else{
            System.err.println(new ErrorMsg(ErrorMsg.ERROR_MSG,
                    e.getMessageAndLocation()));
        }
        throw e;
    }

    @Override
    public void fatalError(TransformerException e)
            throws TransformerException{
        Throwable wrapped=e.getException();
        if(wrapped!=null){
            System.err.println(new ErrorMsg(ErrorMsg.FATAL_ERR_PLUS_WRAPPED_MSG,
                    e.getMessageAndLocation(),
                    wrapped.getMessage()));
        }else{
            System.err.println(new ErrorMsg(ErrorMsg.FATAL_ERR_MSG,
                    e.getMessageAndLocation()));
        }
        throw e;
    }

    @Override
    public InputSource loadSource(String href,String context,XSLTC xsltc){
        try{
            if(_uriResolver!=null){
                final Source source=_uriResolver.resolve(href,context);
                if(source!=null){
                    return Util.getInputSource(xsltc,source);
                }
            }
        }catch(TransformerException e){
            // should catch it when the resolver explicitly throws the exception
            final ErrorMsg msg=new ErrorMsg(ErrorMsg.INVALID_URI_ERR,href+"\n"+e.getMessage(),this);
            xsltc.getParser().reportError(Constants.FATAL,msg);
        }
        return null;
    }

    protected final XSLTCDTMManager createNewDTMManagerInstance(){
        return XSLTCDTMManager.createNewDTMManagerInstance();
    }

    private static class PIParamWrapper{
        public String _media=null;
        public String _title=null;
        public String _charset=null;

        public PIParamWrapper(String media,String title,String charset){
            _media=media;
            _title=title;
            _charset=charset;
        }
    }
















}
