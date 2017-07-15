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
 * $Id: TransformerImpl.java,v 1.10 2007/06/13 01:57:09 joehw Exp $
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
 * $Id: TransformerImpl.java,v 1.10 2007/06/13 01:57:09 joehw Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.XalanConstants;
import com.sun.org.apache.xalan.internal.utils.FactoryImpl;
import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xalan.internal.xsltc.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter;
import com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory;
import com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import com.sun.org.apache.xml.internal.utils.XMLReaderManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.*;

public final class TransformerImpl extends Transformer
        implements DOMCache, ErrorListener{
    private final static String LEXICAL_HANDLER_PROPERTY=
            "http://xml.org/sax/properties/lexical-handler";
    private static final String NAMESPACE_FEATURE=
            "http://xml.org/sax/features/namespaces";
    private static final String NAMESPACE_PREFIXES_FEATURE=
            "http://xml.org/sax/features/namespace-prefixes";
    private AbstractTranslet _translet=null;
    private String _method=null;
    private String _encoding=null;
    private String _sourceSystemId=null;
    private ErrorListener _errorListener=this;
    private URIResolver _uriResolver=null;
    private Properties _properties, _propertiesClone;
    private TransletOutputHandlerFactory _tohFactory=null;
    private DOM _dom=null;
    private int _indentNumber;
    private TransformerFactoryImpl _tfactory=null;
    private OutputStream _ostream=null;
    private XSLTCDTMManager _dtmManager=null;
    private XMLReaderManager _readerManager;
    //private boolean _isIncremental = false;
    private boolean _isIdentity=false;
    private boolean _isSecureProcessing=false;
    private boolean _useServicesMechanism;
    private String _accessExternalStylesheet=XalanConstants.EXTERNAL_ACCESS_DEFAULT;
    private String _accessExternalDTD=XalanConstants.EXTERNAL_ACCESS_DEFAULT;
    private XMLSecurityManager _securityManager;
    private Map<String,Object> _parameters=null;

    protected TransformerImpl(Properties outputProperties,int indentNumber,
                              TransformerFactoryImpl tfactory){
        this(null,outputProperties,indentNumber,tfactory);
        _isIdentity=true;
        // _properties.put(OutputKeys.METHOD, "xml");
    }

    protected TransformerImpl(Translet translet,Properties outputProperties,
                              int indentNumber,TransformerFactoryImpl tfactory){
        _translet=(AbstractTranslet)translet;
        _properties=createOutputProperties(outputProperties);
        _propertiesClone=(Properties)_properties.clone();
        _indentNumber=indentNumber;
        _tfactory=tfactory;
        _useServicesMechanism=_tfactory.useServicesMechnism();
        _accessExternalStylesheet=(String)_tfactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET);
        _accessExternalDTD=(String)_tfactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD);
        _securityManager=(XMLSecurityManager)_tfactory.getAttribute(XalanConstants.SECURITY_MANAGER);
        _readerManager=XMLReaderManager.getInstance(_useServicesMechanism);
        _readerManager.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,_accessExternalDTD);
        _readerManager.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,_isSecureProcessing);
        _readerManager.setProperty(XalanConstants.SECURITY_MANAGER,_securityManager);
        //_isIncremental = tfactory._incremental;
    }

    private Properties createOutputProperties(Properties outputProperties){
        final Properties defaults=new Properties();
        setDefaults(defaults,"xml");
        // Copy propeties set in stylesheet to base
        final Properties base=new Properties(defaults);
        if(outputProperties!=null){
            final Enumeration names=outputProperties.propertyNames();
            while(names.hasMoreElements()){
                final String name=(String)names.nextElement();
                base.setProperty(name,outputProperties.getProperty(name));
            }
        }else{
            base.setProperty(OutputKeys.ENCODING,_translet._encoding);
            if(_translet._method!=null)
                base.setProperty(OutputKeys.METHOD,_translet._method);
        }
        // Update defaults based on output method
        final String method=base.getProperty(OutputKeys.METHOD);
        if(method!=null){
            if(method.equals("html")){
                setDefaults(defaults,"html");
            }else if(method.equals("text")){
                setDefaults(defaults,"text");
            }
        }
        return base;
    }

    private void setDefaults(Properties props,String method){
        final Properties method_props=
                OutputPropertiesFactory.getDefaultMethodProperties(method);
        {
            final Enumeration names=method_props.propertyNames();
            while(names.hasMoreElements()){
                final String name=(String)names.nextElement();
                props.setProperty(name,method_props.getProperty(name));
            }
        }
    }

    public boolean isSecureProcessing(){
        return _isSecureProcessing;
    }

    public void setSecureProcessing(boolean flag){
        _isSecureProcessing=flag;
        _readerManager.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,_isSecureProcessing);
    }

    public boolean useServicesMechnism(){
        return _useServicesMechanism;
    }

    public void setServicesMechnism(boolean flag){
        _useServicesMechanism=flag;
    }

    protected AbstractTranslet getTranslet(){
        return _translet;
    }

    public boolean isIdentity(){
        return _isIdentity;
    }

    protected void setDOM(DOM dom){
        _dom=dom;
    }

    protected TransformerFactoryImpl getTransformerFactory(){
        return _tfactory;
    }

    protected TransletOutputHandlerFactory getTransletOutputHandlerFactory(){
        return _tohFactory;
    }

    private void postWarningToListener(String message){
        try{
            _errorListener.warning(new TransformerException(message));
        }catch(TransformerException e){
            // ignored - transformation cannot be continued
        }
    }

    @Override
    public DOM retrieveDocument(String baseURI,String href,Translet translet){
        try{
            // Argument to document function was: document('');
            if(href.length()==0){
                href=baseURI;
            }
            /**
             *  Fix for bug 24188
             *  Incase the _uriResolver.resolve(href,base) is null
             *  try to still  retrieve the document before returning null
             *  and throwing the FileNotFoundException in
             *  com.sun.org.apache.xalan.internal.xsltc.dom.LoadDocument
             *
             */
            Source resolvedSource=_uriResolver.resolve(href,baseURI);
            if(resolvedSource==null){
                StreamSource streamSource=new StreamSource(
                        SystemIDResolver.getAbsoluteURI(href,baseURI));
                return getDOM(streamSource);
            }
            return getDOM(resolvedSource);
        }catch(TransformerException e){
            if(_errorListener!=null)
                postErrorToListener("File not found: "+e.getMessage());
            return (null);
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
    }    @Override
    public ErrorListener getErrorListener(){
        return _errorListener;
    }

    @Override
    public void reset(){
        _method=null;
        _encoding=null;
        _sourceSystemId=null;
        _errorListener=this;
        _uriResolver=null;
        _dom=null;
        _parameters=null;
        _indentNumber=0;
        setOutputProperties(null);
        _tohFactory=null;
        _ostream=null;
    }    @Override
    public void setErrorListener(ErrorListener listener)
            throws IllegalArgumentException{
        if(listener==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.ERROR_LISTENER_NULL_ERR,
                    "Transformer");
            throw new IllegalArgumentException(err.toString());
        }
        _errorListener=listener;
        // Register a message handler to report xsl:messages
        if(_translet!=null)
            _translet.setMessageHandler(new MessageHandler(_errorListener));
    }

    @Override
    public void transform(Source source,Result result)
            throws TransformerException{
        if(!_isIdentity){
            if(_translet==null){
                ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_NO_TRANSLET_ERR);
                throw new TransformerException(err.toString());
            }
            // Pass output properties to the translet
            transferOutputProperties(_translet);
        }
        final SerializationHandler toHandler=getOutputHandler(result);
        if(toHandler==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_NO_HANDLER_ERR);
            throw new TransformerException(err.toString());
        }
        if(_uriResolver!=null&&!_isIdentity){
            _translet.setDOMCache(this);
        }
        // Pass output properties to handler if identity
        if(_isIdentity){
            transferOutputProperties(toHandler);
        }
        transform(source,toHandler,_encoding);
        try{
            if(result instanceof DOMResult){
                ((DOMResult)result).setNode(_tohFactory.getNode());
            }else if(result instanceof StAXResult){
                if(((StAXResult)result).getXMLEventWriter()!=null){
                    (_tohFactory.getXMLEventWriter()).flush();
                }else if(((StAXResult)result).getXMLStreamWriter()!=null){
                    (_tohFactory.getXMLStreamWriter()).flush();
                    //result = new StAXResult(_tohFactory.getXMLStreamWriter());
                }
            }
        }catch(Exception e){
            System.out.println("Result writing error");
        }
    }

    public SerializationHandler getOutputHandler(Result result)
            throws TransformerException{
        // Get output method using get() to ignore defaults
        _method=(String)_properties.get(OutputKeys.METHOD);
        // Get encoding using getProperty() to use defaults
        _encoding=(String)_properties.getProperty(OutputKeys.ENCODING);
        _tohFactory=TransletOutputHandlerFactory.newInstance(_useServicesMechanism);
        _tohFactory.setEncoding(_encoding);
        if(_method!=null){
            _tohFactory.setOutputMethod(_method);
        }
        // Set indentation number in the factory
        if(_indentNumber>=0){
            _tohFactory.setIndentNumber(_indentNumber);
        }
        // Return the content handler for this Result object
        try{
            // Result object could be SAXResult, DOMResult, or StreamResult
            if(result instanceof SAXResult){
                final SAXResult target=(SAXResult)result;
                final ContentHandler handler=target.getHandler();
                _tohFactory.setHandler(handler);
                /**
                 * Fix for bug 24414
                 * If the lexicalHandler is set then we need to get that
                 * for obtaining the lexical information
                 */
                LexicalHandler lexicalHandler=target.getLexicalHandler();
                if(lexicalHandler!=null){
                    _tohFactory.setLexicalHandler(lexicalHandler);
                }
                _tohFactory.setOutputType(TransletOutputHandlerFactory.SAX);
                return _tohFactory.getSerializationHandler();
            }else if(result instanceof StAXResult){
                if(((StAXResult)result).getXMLEventWriter()!=null)
                    _tohFactory.setXMLEventWriter(((StAXResult)result).getXMLEventWriter());
                else if(((StAXResult)result).getXMLStreamWriter()!=null)
                    _tohFactory.setXMLStreamWriter(((StAXResult)result).getXMLStreamWriter());
                _tohFactory.setOutputType(TransletOutputHandlerFactory.STAX);
                return _tohFactory.getSerializationHandler();
            }else if(result instanceof DOMResult){
                _tohFactory.setNode(((DOMResult)result).getNode());
                _tohFactory.setNextSibling(((DOMResult)result).getNextSibling());
                _tohFactory.setOutputType(TransletOutputHandlerFactory.DOM);
                return _tohFactory.getSerializationHandler();
            }else if(result instanceof StreamResult){
                // Get StreamResult
                final StreamResult target=(StreamResult)result;
                // StreamResult may have been created with a java.io.File,
                // java.io.Writer, java.io.OutputStream or just a String
                // systemId.
                _tohFactory.setOutputType(TransletOutputHandlerFactory.STREAM);
                // try to get a Writer from Result object
                final Writer writer=target.getWriter();
                if(writer!=null){
                    _tohFactory.setWriter(writer);
                    return _tohFactory.getSerializationHandler();
                }
                // or try to get an OutputStream from Result object
                final OutputStream ostream=target.getOutputStream();
                if(ostream!=null){
                    _tohFactory.setOutputStream(ostream);
                    return _tohFactory.getSerializationHandler();
                }
                // or try to get just a systemId string from Result object
                String systemId=result.getSystemId();
                if(systemId==null){
                    ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_NO_RESULT_ERR);
                    throw new TransformerException(err.toString());
                }
                // System Id may be in one of several forms, (1) a uri
                // that starts with 'file:', (2) uri that starts with 'http:'
                // or (3) just a filename on the local system.
                URL url;
                if(systemId.startsWith("file:")){
                    // if StreamResult(File) or setSystemID(File) was used,
                    // the systemId will be URI encoded as a result of File.toURI(),
                    // it must be decoded for use by URL
                    try{
                        URI uri=new URI(systemId);
                        systemId="file:";
                        String host=uri.getHost(); // decoded String
                        String path=uri.getPath(); //decoded String
                        if(path==null){
                            path="";
                        }
                        // if host (URI authority) then file:// + host + path
                        // else just path (may be absolute or relative)
                        if(host!=null){
                            systemId+="//"+host+path;
                        }else{
                            systemId+="//"+path;
                        }
                    }catch(Exception exception){
                        // URI exception which means nothing can be done so OK to ignore
                    }
                    url=new URL(systemId);
                    _ostream=new FileOutputStream(url.getFile());
                    _tohFactory.setOutputStream(_ostream);
                    return _tohFactory.getSerializationHandler();
                }else if(systemId.startsWith("http:")){
                    url=new URL(systemId);
                    final URLConnection connection=url.openConnection();
                    _tohFactory.setOutputStream(_ostream=connection.getOutputStream());
                    return _tohFactory.getSerializationHandler();
                }else{
                    // system id is just a filename
                    _tohFactory.setOutputStream(
                            _ostream=new FileOutputStream(new File(systemId)));
                    return _tohFactory.getSerializationHandler();
                }
            }
        }
        // If we cannot write to the location specified by the SystemId
        catch(UnknownServiceException e){
            throw new TransformerException(e);
        }catch(ParserConfigurationException e){
            throw new TransformerException(e);
        }
        // If we cannot create the file specified by the SystemId
        catch(IOException e){
            throw new TransformerException(e);
        }
        return null;
    }

    private void transform(Source source,SerializationHandler handler,
                           String encoding) throws TransformerException{
        try{
            /**
             * According to JAXP1.2, new SAXSource()/StreamSource()
             * should create an empty input tree, with a default root node.
             * new DOMSource()creates an empty document using DocumentBuilder.
             * newDocument(); Use DocumentBuilder.newDocument() for all 3
             * situations, since there is no clear spec. how to create
             * an empty tree when both SAXSource() and StreamSource() are used.
             */
            if((source instanceof StreamSource&&source.getSystemId()==null
                    &&((StreamSource)source).getInputStream()==null&&
                    ((StreamSource)source).getReader()==null)||
                    (source instanceof SAXSource&&
                            ((SAXSource)source).getInputSource()==null&&
                            ((SAXSource)source).getXMLReader()==null)||
                    (source instanceof DOMSource&&
                            ((DOMSource)source).getNode()==null)){
                DocumentBuilderFactory builderF=FactoryImpl.getDOMFactory(_useServicesMechanism);
                DocumentBuilder builder=builderF.newDocumentBuilder();
                String systemID=source.getSystemId();
                source=new DOMSource(builder.newDocument());
                // Copy system ID from original, empty Source to new
                if(systemID!=null){
                    source.setSystemId(systemID);
                }
            }
            if(_isIdentity){
                transformIdentity(source,handler);
            }else{
                _translet.transform(getDOM(source),handler);
            }
        }catch(TransletException e){
            if(_errorListener!=null) postErrorToListener(e.getMessage());
            throw new TransformerException(e);
        }catch(RuntimeException e){
            if(_errorListener!=null) postErrorToListener(e.getMessage());
            throw new TransformerException(e);
        }catch(Exception e){
            if(_errorListener!=null) postErrorToListener(e.getMessage());
            throw new TransformerException(e);
        }finally{
            _dtmManager=null;
        }
        // If we create an output stream for the Result, we need to close it after the transformation.
        if(_ostream!=null){
            try{
                _ostream.close();
            }catch(IOException e){
            }
            _ostream=null;
        }
    }    @Override
    public Properties getOutputProperties(){
        return (Properties)_properties.clone();
    }

    private DOM getDOM(Source source) throws TransformerException{
        try{
            DOM dom;
            if(source!=null){
                DTMWSFilter wsfilter;
                if(_translet!=null&&_translet instanceof StripFilter){
                    wsfilter=new DOMWSFilter(_translet);
                }else{
                    wsfilter=null;
                }
                boolean hasIdCall=(_translet!=null)?_translet.hasIdCall()
                        :false;
                if(_dtmManager==null){
                    _dtmManager=
                            _tfactory.createNewDTMManagerInstance();
                    _dtmManager.setServicesMechnism(_useServicesMechanism);
                }
                dom=(DOM)_dtmManager.getDTM(source,false,wsfilter,true,
                        false,false,0,hasIdCall);
            }else if(_dom!=null){
                dom=_dom;
                _dom=null;  // use only once, so reset to 'null'
            }else{
                return null;
            }
            if(!_isIdentity){
                // Give the translet the opportunity to make a prepass of
                // the document, in case it can extract useful information early
                _translet.prepassDocument(dom);
            }
            return dom;
        }catch(Exception e){
            if(_errorListener!=null){
                postErrorToListener(e.getMessage());
            }
            throw new TransformerException(e);
        }
    }    @Override
    public String getOutputProperty(String name)
            throws IllegalArgumentException{
        if(!validOutputProperty(name)){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR,name);
            throw new IllegalArgumentException(err.toString());
        }
        return _properties.getProperty(name);
    }

    private void postErrorToListener(String message){
        try{
            _errorListener.error(new TransformerException(message));
        }catch(TransformerException e){
            // ignored - transformation cannot be continued
        }
    }    @Override
    public void setOutputProperties(Properties properties)
            throws IllegalArgumentException{
        if(properties!=null){
            final Enumeration names=properties.propertyNames();
            while(names.hasMoreElements()){
                final String name=(String)names.nextElement();
                // Ignore lower layer properties
                if(isDefaultProperty(name,properties)) continue;
                if(validOutputProperty(name)){
                    _properties.setProperty(name,properties.getProperty(name));
                }else{
                    ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR,name);
                    throw new IllegalArgumentException(err.toString());
                }
            }
        }else{
            _properties=_propertiesClone;
        }
    }

    private void transformIdentity(Source source,SerializationHandler handler)
            throws Exception{
        // Get systemId from source
        if(source!=null){
            _sourceSystemId=source.getSystemId();
        }
        if(source instanceof StreamSource){
            final StreamSource stream=(StreamSource)source;
            final InputStream streamInput=stream.getInputStream();
            final Reader streamReader=stream.getReader();
            final XMLReader reader=_readerManager.getXMLReader();
            try{
                // Hook up reader and output handler
                try{
                    reader.setProperty(LEXICAL_HANDLER_PROPERTY,handler);
                    reader.setFeature(NAMESPACE_PREFIXES_FEATURE,true);
                }catch(SAXException e){
                    // Falls through
                }
                reader.setContentHandler(handler);
                // Create input source from source
                InputSource input;
                if(streamInput!=null){
                    input=new InputSource(streamInput);
                    input.setSystemId(_sourceSystemId);
                }else if(streamReader!=null){
                    input=new InputSource(streamReader);
                    input.setSystemId(_sourceSystemId);
                }else if(_sourceSystemId!=null){
                    input=new InputSource(_sourceSystemId);
                }else{
                    ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_NO_SOURCE_ERR);
                    throw new TransformerException(err.toString());
                }
                // Start pushing SAX events
                reader.parse(input);
            }finally{
                _readerManager.releaseXMLReader(reader);
            }
        }else if(source instanceof SAXSource){
            final SAXSource sax=(SAXSource)source;
            XMLReader reader=sax.getXMLReader();
            final InputSource input=sax.getInputSource();
            boolean userReader=true;
            try{
                // Create a reader if not set by user
                if(reader==null){
                    reader=_readerManager.getXMLReader();
                    userReader=false;
                }
                // Hook up reader and output handler
                try{
                    reader.setProperty(LEXICAL_HANDLER_PROPERTY,handler);
                    reader.setFeature(NAMESPACE_PREFIXES_FEATURE,true);
                }catch(SAXException e){
                    // Falls through
                }
                reader.setContentHandler(handler);
                // Start pushing SAX events
                reader.parse(input);
            }finally{
                if(!userReader){
                    _readerManager.releaseXMLReader(reader);
                }
            }
        }else if(source instanceof StAXSource){
            final StAXSource staxSource=(StAXSource)source;
            StAXEvent2SAX staxevent2sax;
            StAXStream2SAX staxStream2SAX;
            if(staxSource.getXMLEventReader()!=null){
                final XMLEventReader xmlEventReader=staxSource.getXMLEventReader();
                staxevent2sax=new StAXEvent2SAX(xmlEventReader);
                staxevent2sax.setContentHandler(handler);
                staxevent2sax.parse();
                handler.flushPending();
            }else if(staxSource.getXMLStreamReader()!=null){
                final XMLStreamReader xmlStreamReader=staxSource.getXMLStreamReader();
                staxStream2SAX=new StAXStream2SAX(xmlStreamReader);
                staxStream2SAX.setContentHandler(handler);
                staxStream2SAX.parse();
                handler.flushPending();
            }
        }else if(source instanceof DOMSource){
            final DOMSource domsrc=(DOMSource)source;
            new DOM2TO(domsrc.getNode(),handler).parse();
        }else if(source instanceof XSLTCSource){
            final DOM dom=((XSLTCSource)source).getDOM(null,_translet);
            ((SAXImpl)dom).copy(handler);
        }else{
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_NO_SOURCE_ERR);
            throw new TransformerException(err.toString());
        }
    }    @Override
    public void setOutputProperty(String name,String value)
            throws IllegalArgumentException{
        if(!validOutputProperty(name)){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR,name);
            throw new IllegalArgumentException(err.toString());
        }
        _properties.setProperty(name,value);
    }

    private void transferOutputProperties(AbstractTranslet translet){
        // Return right now if no properties are set
        if(_properties==null) return;
        // Get a list of all the defined properties
        Enumeration names=_properties.propertyNames();
        while(names.hasMoreElements()){
            // Note the use of get() instead of getProperty()
            String name=(String)names.nextElement();
            String value=(String)_properties.get(name);
            // Ignore default properties
            if(value==null) continue;
            // Pass property value to translet - override previous setting
            if(name.equals(OutputKeys.ENCODING)){
                translet._encoding=value;
            }else if(name.equals(OutputKeys.METHOD)){
                translet._method=value;
            }else if(name.equals(OutputKeys.DOCTYPE_PUBLIC)){
                translet._doctypePublic=value;
            }else if(name.equals(OutputKeys.DOCTYPE_SYSTEM)){
                translet._doctypeSystem=value;
            }else if(name.equals(OutputKeys.MEDIA_TYPE)){
                translet._mediaType=value;
            }else if(name.equals(OutputKeys.STANDALONE)){
                translet._standalone=value;
            }else if(name.equals(OutputKeys.VERSION)){
                translet._version=value;
            }else if(name.equals(OutputKeys.OMIT_XML_DECLARATION)){
                translet._omitHeader=
                        (value!=null&&value.toLowerCase().equals("yes"));
            }else if(name.equals(OutputKeys.INDENT)){
                translet._indent=
                        (value!=null&&value.toLowerCase().equals("yes"));
            }else if(name.equals(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL+"indent-amount")){
                if(value!=null){
                    translet._indentamount=Integer.parseInt(value);
                }
            }else if(name.equals(OutputPropertiesFactory.S_BUILTIN_EXTENSIONS_UNIVERSAL+"indent-amount")){
                if(value!=null){
                    translet._indentamount=Integer.parseInt(value);
                }
            }else if(name.equals(OutputKeys.CDATA_SECTION_ELEMENTS)){
                if(value!=null){
                    translet._cdata=null; // clear previous setting
                    StringTokenizer e=new StringTokenizer(value);
                    while(e.hasMoreTokens()){
                        translet.addCdataElement(e.nextToken());
                    }
                }
            }else if(name.equals(OutputPropertiesFactory.ORACLE_IS_STANDALONE)){
                if(value!=null&&value.equals("yes")){
                    translet._isStandalone=true;
                }
            }
        }
    }

    public void transferOutputProperties(SerializationHandler handler){
        // Return right now if no properties are set
        if(_properties==null) return;
        String doctypePublic=null;
        String doctypeSystem=null;
        // Get a list of all the defined properties
        Enumeration names=_properties.propertyNames();
        while(names.hasMoreElements()){
            // Note the use of get() instead of getProperty()
            String name=(String)names.nextElement();
            String value=(String)_properties.get(name);
            // Ignore default properties
            if(value==null) continue;
            // Pass property value to translet - override previous setting
            if(name.equals(OutputKeys.DOCTYPE_PUBLIC)){
                doctypePublic=value;
            }else if(name.equals(OutputKeys.DOCTYPE_SYSTEM)){
                doctypeSystem=value;
            }else if(name.equals(OutputKeys.MEDIA_TYPE)){
                handler.setMediaType(value);
            }else if(name.equals(OutputKeys.STANDALONE)){
                handler.setStandalone(value);
            }else if(name.equals(OutputKeys.VERSION)){
                handler.setVersion(value);
            }else if(name.equals(OutputKeys.OMIT_XML_DECLARATION)){
                handler.setOmitXMLDeclaration(
                        value!=null&&value.toLowerCase().equals("yes"));
            }else if(name.equals(OutputKeys.INDENT)){
                handler.setIndent(
                        value!=null&&value.toLowerCase().equals("yes"));
            }else if(name.equals(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL+"indent-amount")){
                if(value!=null){
                    handler.setIndentAmount(Integer.parseInt(value));
                }
            }else if(name.equals(OutputPropertiesFactory.S_BUILTIN_EXTENSIONS_UNIVERSAL+"indent-amount")){
                if(value!=null){
                    handler.setIndentAmount(Integer.parseInt(value));
                }
            }else if(name.equals(OutputPropertiesFactory.ORACLE_IS_STANDALONE)){
                if(value!=null&&value.equals("yes")){
                    handler.setIsStandalone(true);
                }
            }else if(name.equals(OutputKeys.CDATA_SECTION_ELEMENTS)){
                if(value!=null){
                    StringTokenizer e=new StringTokenizer(value);
                    Vector uriAndLocalNames=null;
                    while(e.hasMoreTokens()){
                        final String token=e.nextToken();
                        // look for the last colon, as the String may be
                        // something like "http://abc.com:local"
                        int lastcolon=token.lastIndexOf(':');
                        String uri;
                        String localName;
                        if(lastcolon>0){
                            uri=token.substring(0,lastcolon);
                            localName=token.substring(lastcolon+1);
                        }else{
                            // no colon at all, lets hope this is the
                            // local name itself then
                            uri=null;
                            localName=token;
                        }
                        if(uriAndLocalNames==null){
                            uriAndLocalNames=new Vector();
                        }
                        // add the uri/localName as a pair, in that order
                        uriAndLocalNames.addElement(uri);
                        uriAndLocalNames.addElement(localName);
                    }
                    handler.setCdataSectionElements(uriAndLocalNames);
                }
            }
        }
        // Call setDoctype() if needed
        if(doctypePublic!=null||doctypeSystem!=null){
            handler.setDoctype(doctypeSystem,doctypePublic);
        }
    }

    @Override
    public void setParameter(String name,Object value){
        if(value==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_INVALID_SET_PARAM_VALUE,name);
            throw new IllegalArgumentException(err.toString());
        }
        if(_isIdentity){
            if(_parameters==null){
                _parameters=new HashMap<>();
            }
            _parameters.put(name,value);
        }else{
            _translet.addParameter(name,value);
        }
    }

    @Override
    public final Object getParameter(String name){
        if(_isIdentity){
            return (_parameters!=null)?_parameters.get(name):null;
        }else{
            return _translet.getParameter(name);
        }
    }

    @Override
    public void clearParameters(){
        if(_isIdentity&&_parameters!=null){
            _parameters.clear();
        }else{
            _translet.clearParameters();
        }
    }    private boolean validOutputProperty(String name){
        return (name.equals(OutputKeys.ENCODING)||
                name.equals(OutputKeys.METHOD)||
                name.equals(OutputKeys.INDENT)||
                name.equals(OutputKeys.DOCTYPE_PUBLIC)||
                name.equals(OutputKeys.DOCTYPE_SYSTEM)||
                name.equals(OutputKeys.CDATA_SECTION_ELEMENTS)||
                name.equals(OutputKeys.MEDIA_TYPE)||
                name.equals(OutputKeys.OMIT_XML_DECLARATION)||
                name.equals(OutputKeys.STANDALONE)||
                name.equals(OutputKeys.VERSION)||
                name.equals(OutputPropertiesFactory.ORACLE_IS_STANDALONE)||
                name.charAt(0)=='{');
    }

    static class MessageHandler
            extends com.sun.org.apache.xalan.internal.xsltc.runtime.MessageHandler{
        private ErrorListener _errorListener;

        public MessageHandler(ErrorListener errorListener){
            _errorListener=errorListener;
        }

        @Override
        public void displayMessage(String msg){
            if(_errorListener==null){
                System.err.println(msg);
            }else{
                try{
                    _errorListener.warning(new TransformerException(msg));
                }catch(TransformerException e){
                    // ignored
                }
            }
        }
    }    private boolean isDefaultProperty(String name,Properties properties){
        return (properties.get(name)==null);
    }







    @Override
    public URIResolver getURIResolver(){
        return _uriResolver;
    }

    @Override
    public void setURIResolver(URIResolver resolver){
        _uriResolver=resolver;
    }










}