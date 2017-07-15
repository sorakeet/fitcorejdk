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
 * $Id: TemplatesHandlerImpl.java,v 1.2.4.1 2005/09/06 12:09:03 pvedula Exp $
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
 * $Id: TemplatesHandlerImpl.java,v 1.2.4.1 2005/09/06 12:09:03 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.XalanConstants;
import com.sun.org.apache.xalan.internal.xsltc.compiler.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Parser;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TemplatesHandler;
import java.util.Vector;

public class TemplatesHandlerImpl
        implements ContentHandler, TemplatesHandler, SourceLoader{
    private String _systemId;
    private int _indentNumber;
    private URIResolver _uriResolver=null;
    private TransformerFactoryImpl _tfactory=null;
    private Parser _parser=null;
    private TemplatesImpl _templates=null;

    protected TemplatesHandlerImpl(int indentNumber,
                                   TransformerFactoryImpl tfactory){
        _indentNumber=indentNumber;
        _tfactory=tfactory;
        // Instantiate XSLTC and get reference to parser object
        XSLTC xsltc=new XSLTC(tfactory.useServicesMechnism(),tfactory.getFeatureManager());
        if(tfactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING))
            xsltc.setSecureProcessing(true);
        xsltc.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,
                (String)tfactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
        xsltc.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,
                (String)tfactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
        xsltc.setProperty(XalanConstants.SECURITY_MANAGER,
                tfactory.getAttribute(XalanConstants.SECURITY_MANAGER));
        if("true".equals(tfactory.getAttribute(TransformerFactoryImpl.ENABLE_INLINING)))
            xsltc.setTemplateInlining(true);
        else
            xsltc.setTemplateInlining(false);
        _parser=xsltc.getParser();
    }

    public void setURIResolver(URIResolver resolver){
        _uriResolver=resolver;
    }    public String getSystemId(){
        return _systemId;
    }

    public Templates getTemplates(){
        return _templates;
    }    public void setSystemId(String id){
        _systemId=id;
    }

    public InputSource loadSource(String href,String context,XSLTC xsltc){
        try{
            // A _uriResolver must be set if this method is called
            final Source source=_uriResolver.resolve(href,context);
            if(source!=null){
                return Util.getInputSource(xsltc,source);
            }
        }catch(TransformerException e){
            // Falls through
        }
        return null;
    }

    public void setDocumentLocator(Locator locator){
        setSystemId(locator.getSystemId());
        _parser.setDocumentLocator(locator);
    }

    public void startDocument(){
        XSLTC xsltc=_parser.getXSLTC();
        xsltc.init();   // calls _parser.init()
        xsltc.setOutputType(XSLTC.BYTEARRAY_OUTPUT);
        _parser.startDocument();
    }
    // -- ContentHandler --------------------------------------------------

    public void endDocument() throws SAXException{
        _parser.endDocument();
        // create the templates
        try{
            XSLTC xsltc=_parser.getXSLTC();
            // Set the translet class name if not already set
            String transletName;
            if(_systemId!=null){
                transletName=Util.baseName(_systemId);
            }else{
                transletName=(String)_tfactory.getAttribute("translet-name");
            }
            xsltc.setClassName(transletName);
            // Get java-legal class name from XSLTC module
            transletName=xsltc.getClassName();
            Stylesheet stylesheet=null;
            SyntaxTreeNode root=_parser.getDocumentRoot();
            // Compile the translet - this is where the work is done!
            if(!_parser.errorsFound()&&root!=null){
                // Create a Stylesheet element from the root node
                stylesheet=_parser.makeStylesheet(root);
                stylesheet.setSystemId(_systemId);
                stylesheet.setParentStylesheet(null);
                if(xsltc.getTemplateInlining())
                    stylesheet.setTemplateInlining(true);
                else
                    stylesheet.setTemplateInlining(false);
                // Set a document loader (for xsl:include/import) if defined
                if(_uriResolver!=null){
                    stylesheet.setSourceLoader(this);
                }
                _parser.setCurrentStylesheet(stylesheet);
                // Set it as top-level in the XSLTC object
                xsltc.setStylesheet(stylesheet);
                // Create AST under the Stylesheet element
                _parser.createAST(stylesheet);
            }
            // Generate the bytecodes and output the translet class(es)
            if(!_parser.errorsFound()&&stylesheet!=null){
                stylesheet.setMultiDocument(xsltc.isMultiDocument());
                stylesheet.setHasIdCall(xsltc.hasIdCall());
                // Class synchronization is needed for BCEL
                synchronized(xsltc.getClass()){
                    stylesheet.translate();
                }
            }
            if(!_parser.errorsFound()){
                // Check that the transformation went well before returning
                final byte[][] bytecodes=xsltc.getBytecodes();
                if(bytecodes!=null){
                    _templates=
                            new TemplatesImpl(xsltc.getBytecodes(),transletName,
                                    _parser.getOutputProperties(),_indentNumber,_tfactory);
                    // Set URIResolver on templates object
                    if(_uriResolver!=null){
                        _templates.setURIResolver(_uriResolver);
                    }
                }
            }else{
                StringBuffer errorMessage=new StringBuffer();
                Vector errors=_parser.getErrors();
                final int count=errors.size();
                for(int i=0;i<count;i++){
                    if(errorMessage.length()>0)
                        errorMessage.append('\n');
                    errorMessage.append(errors.elementAt(i).toString());
                }
                throw new SAXException(ErrorMsg.JAXP_COMPILE_ERR,new TransformerException(errorMessage.toString()));
            }
        }catch(CompilerException e){
            throw new SAXException(ErrorMsg.JAXP_COMPILE_ERR,e);
        }
    }

    public void startPrefixMapping(String prefix,String uri){
        _parser.startPrefixMapping(prefix,uri);
    }

    public void endPrefixMapping(String prefix){
        _parser.endPrefixMapping(prefix);
    }

    public void startElement(String uri,String localname,String qname,
                             Attributes attributes) throws SAXException{
        _parser.startElement(uri,localname,qname,attributes);
    }

    public void endElement(String uri,String localname,String qname){
        _parser.endElement(uri,localname,qname);
    }

    public void characters(char[] ch,int start,int length){
        _parser.characters(ch,start,length);
    }

    public void ignorableWhitespace(char[] ch,int start,int length){
        _parser.ignorableWhitespace(ch,start,length);
    }

    public void processingInstruction(String name,String value){
        _parser.processingInstruction(name,value);
    }

    public void skippedEntity(String name){
        _parser.skippedEntity(name);
    }




}
