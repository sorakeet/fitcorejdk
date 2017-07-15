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
 * $Id: TransformerHandlerImpl.java,v 1.2.4.1 2005/09/15 06:25:12 pvedula Exp $
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
 * $Id: TransformerHandlerImpl.java,v 1.2.4.1 2005/09/15 06:25:12 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter;
import com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.xml.sax.*;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.TransformerHandler;

public class TransformerHandlerImpl implements TransformerHandler, DeclHandler{
    private TransformerImpl _transformer;
    private AbstractTranslet _translet=null;
    private String _systemId;
    private SAXImpl _dom=null;
    private ContentHandler _handler=null;
    private LexicalHandler _lexHandler=null;
    private DTDHandler _dtdHandler=null;
    private DeclHandler _declHandler=null;
    private Result _result=null;
    private Locator _locator=null;
    private boolean _done=false; // Set in endDocument()
    private boolean _isIdentity=false;

    public TransformerHandlerImpl(TransformerImpl transformer){
        // Save the reference to the transformer
        _transformer=transformer;
        if(transformer.isIdentity()){
            // Set initial handler to the empty handler
            _handler=new DefaultHandler();
            _isIdentity=true;
        }else{
            // Get a reference to the translet wrapped inside the transformer
            _translet=_transformer.getTranslet();
        }
    }

    @Override
    public void setResult(Result result) throws IllegalArgumentException{
        _result=result;
        if(null==result){
            ErrorMsg err=new ErrorMsg(ErrorMsg.ER_RESULT_NULL);
            throw new IllegalArgumentException(err.toString()); //"result should not be null");
        }
        if(_isIdentity){
            try{
                // Connect this object with output system directly
                SerializationHandler outputHandler=
                        _transformer.getOutputHandler(result);
                _transformer.transferOutputProperties(outputHandler);
                _handler=outputHandler;
                _lexHandler=outputHandler;
            }catch(TransformerException e){
                _result=null;
            }
        }else if(_done){
            // Run the transformation now, if not already done
            try{
                _transformer.setDOM(_dom);
                _transformer.transform(null,_result);
            }catch(TransformerException e){
                // What the hell are we supposed to do with this???
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }    @Override
    public String getSystemId(){
        return _systemId;
    }

    @Override
    public void setDocumentLocator(Locator locator){
        _locator=locator;
        if(_handler!=null){
            _handler.setDocumentLocator(locator);
        }
    }    @Override
    public void setSystemId(String id){
        _systemId=id;
    }

    @Override
    public void startDocument() throws SAXException{
        // Make sure setResult() was called before the first SAX event
        if(_result==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.JAXP_SET_RESULT_ERR);
            throw new SAXException(err.toString());
        }
        if(!_isIdentity){
            boolean hasIdCall=(_translet!=null)?_translet.hasIdCall():false;
            XSLTCDTMManager dtmManager=null;
            // Create an internal DOM (not W3C) and get SAX2 input handler
            try{
                dtmManager=_transformer.getTransformerFactory()
                        .createNewDTMManagerInstance();
            }catch(Exception e){
                throw new SAXException(e);
            }
            DTMWSFilter wsFilter;
            if(_translet!=null&&_translet instanceof StripFilter){
                wsFilter=new DOMWSFilter(_translet);
            }else{
                wsFilter=null;
            }
            // Construct the DTM using the SAX events that come through
            _dom=(SAXImpl)dtmManager.getDTM(null,false,wsFilter,true,
                    false,hasIdCall);
            _handler=_dom.getBuilder();
            _lexHandler=(LexicalHandler)_handler;
            _dtdHandler=(DTDHandler)_handler;
            _declHandler=(DeclHandler)_handler;
            // Set document URI
            _dom.setDocumentURI(_systemId);
            if(_locator!=null){
                _handler.setDocumentLocator(_locator);
            }
        }
        // Proxy call
        _handler.startDocument();
    }    @Override
    public Transformer getTransformer(){
        return _transformer;
    }

    @Override
    public void endDocument() throws SAXException{
        // Signal to the DOMBuilder that the document is complete
        _handler.endDocument();
        if(!_isIdentity){
            // Run the transformation now if we have a reference to a Result object
            if(_result!=null){
                try{
                    _transformer.setDOM(_dom);
                    _transformer.transform(null,_result);
                }catch(TransformerException e){
                    throw new SAXException(e);
                }
            }
            // Signal that the internal DOM is built (see 'setResult()').
            _done=true;
            // Set this DOM as the transformer's DOM
            _transformer.setDOM(_dom);
        }
        if(_isIdentity&&_result instanceof DOMResult){
            ((DOMResult)_result).setNode(_transformer.getTransletOutputHandlerFactory().getNode());
        }
    }

    @Override
    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        _handler.startPrefixMapping(prefix,uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException{
        _handler.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri,String localName,
                             String qname,Attributes attributes)
            throws SAXException{
        _handler.startElement(uri,localName,qname,attributes);
    }

    @Override
    public void endElement(String namespaceURI,String localName,String qname)
            throws SAXException{
        _handler.endElement(namespaceURI,localName,qname);
    }

    @Override
    public void characters(char[] ch,int start,int length)
            throws SAXException{
        _handler.characters(ch,start,length);
    }

    @Override
    public void ignorableWhitespace(char[] ch,int start,int length)
            throws SAXException{
        _handler.ignorableWhitespace(ch,start,length);
    }

    @Override
    public void processingInstruction(String target,String data)
            throws SAXException{
        _handler.processingInstruction(target,data);
    }

    @Override
    public void skippedEntity(String name) throws SAXException{
        _handler.skippedEntity(name);
    }

    @Override
    public void startDTD(String name,String publicId,String systemId)
            throws SAXException{
        if(_lexHandler!=null){
            _lexHandler.startDTD(name,publicId,systemId);
        }
    }

    @Override
    public void endDTD() throws SAXException{
        if(_lexHandler!=null){
            _lexHandler.endDTD();
        }
    }

    @Override
    public void startEntity(String name) throws SAXException{
        if(_lexHandler!=null){
            _lexHandler.startEntity(name);
        }
    }

    @Override
    public void endEntity(String name) throws SAXException{
        if(_lexHandler!=null){
            _lexHandler.endEntity(name);
        }
    }

    @Override
    public void startCDATA() throws SAXException{
        if(_lexHandler!=null){
            _lexHandler.startCDATA();
        }
    }

    @Override
    public void endCDATA() throws SAXException{
        if(_lexHandler!=null){
            _lexHandler.endCDATA();
        }
    }

    @Override
    public void comment(char[] ch,int start,int length)
            throws SAXException{
        if(_lexHandler!=null){
            _lexHandler.comment(ch,start,length);
        }
    }

    @Override
    public void notationDecl(String name,String publicId,String systemId)
            throws SAXException{
        if(_dtdHandler!=null){
            _dtdHandler.notationDecl(name,publicId,systemId);
        }
    }

    @Override
    public void unparsedEntityDecl(String name,String publicId,
                                   String systemId,String notationName) throws SAXException{
        if(_dtdHandler!=null){
            _dtdHandler.unparsedEntityDecl(name,publicId,systemId,
                    notationName);
        }
    }

    @Override
    public void elementDecl(String name,String model)
            throws SAXException{
        if(_declHandler!=null){
            _declHandler.elementDecl(name,model);
        }
    }

    @Override
    public void attributeDecl(String eName,String aName,String type,
                              String valueDefault,String value) throws SAXException{
        if(_declHandler!=null){
            _declHandler.attributeDecl(eName,aName,type,valueDefault,value);
        }
    }

    @Override
    public void internalEntityDecl(String name,String value)
            throws SAXException{
        if(_declHandler!=null){
            _declHandler.internalEntityDecl(name,value);
        }
    }

    @Override
    public void externalEntityDecl(String name,String publicId,String systemId)
            throws SAXException{
        if(_declHandler!=null){
            _declHandler.externalEntityDecl(name,publicId,systemId);
        }
    }

    public void reset(){
        _systemId=null;
        _dom=null;
        _handler=null;
        _lexHandler=null;
        _dtdHandler=null;
        _declHandler=null;
        _result=null;
        _locator=null;
    }






}
