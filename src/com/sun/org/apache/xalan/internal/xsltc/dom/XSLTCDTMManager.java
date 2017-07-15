/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: XSLTCDTMManager.java,v 1.2 2005/08/16 22:32:54 jeffsuttor Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: XSLTCDTMManager.java,v 1.2 2005/08/16 22:32:54 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.trax.DOM2SAX;
import com.sun.org.apache.xalan.internal.xsltc.trax.StAXEvent2SAX;
import com.sun.org.apache.xalan.internal.xsltc.trax.StAXStream2SAX;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMException;
import com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase;
import com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

public class XSLTCDTMManager extends DTMManagerDefault{
    private static final boolean DUMPTREE=false;
    private static final boolean DEBUG=false;

    public XSLTCDTMManager(){
        super();
    }

    public static XSLTCDTMManager createNewDTMManagerInstance(){
        return newInstance();
    }

    public static XSLTCDTMManager newInstance(){
        return new XSLTCDTMManager();
    }

    @Override
    public DTM getDTM(Source source,boolean unique,
                      DTMWSFilter whiteSpaceFilter,boolean incremental,
                      boolean doIndexing){
        return getDTM(source,unique,whiteSpaceFilter,incremental,
                doIndexing,false,0,true,false);
    }

    public DTM getDTM(Source source,boolean unique,
                      DTMWSFilter whiteSpaceFilter,boolean incremental,
                      boolean doIndexing,boolean hasUserReader,int size,
                      boolean buildIdIndex,boolean newNameTable){
        if(DEBUG&&null!=source){
            System.out.println("Starting "+
                    (unique?"UNIQUE":"shared")+
                    " source: "+source.getSystemId());
        }
        int dtmPos=getFirstFreeDTMID();
        int documentID=dtmPos<<IDENT_DTM_NODE_BITS;
        if((null!=source)&&source instanceof StAXSource){
            final StAXSource staxSource=(StAXSource)source;
            StAXEvent2SAX staxevent2sax=null;
            StAXStream2SAX staxStream2SAX=null;
            if(staxSource.getXMLEventReader()!=null){
                final XMLEventReader xmlEventReader=staxSource.getXMLEventReader();
                staxevent2sax=new StAXEvent2SAX(xmlEventReader);
            }else if(staxSource.getXMLStreamReader()!=null){
                final XMLStreamReader xmlStreamReader=staxSource.getXMLStreamReader();
                staxStream2SAX=new StAXStream2SAX(xmlStreamReader);
            }
            SAXImpl dtm;
            if(size<=0){
                dtm=new SAXImpl(this,source,documentID,
                        whiteSpaceFilter,null,doIndexing,
                        DTMDefaultBase.DEFAULT_BLOCKSIZE,
                        buildIdIndex,newNameTable);
            }else{
                dtm=new SAXImpl(this,source,documentID,
                        whiteSpaceFilter,null,doIndexing,
                        size,buildIdIndex,newNameTable);
            }
            dtm.setDocumentURI(source.getSystemId());
            addDTM(dtm,dtmPos,0);
            try{
                if(staxevent2sax!=null){
                    staxevent2sax.setContentHandler(dtm);
                    staxevent2sax.parse();
                }else if(staxStream2SAX!=null){
                    staxStream2SAX.setContentHandler(dtm);
                    staxStream2SAX.parse();
                }
            }catch(RuntimeException re){
                throw re;
            }catch(Exception e){
                throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
            }
            return dtm;
        }else if((null!=source)&&source instanceof DOMSource){
            final DOMSource domsrc=(DOMSource)source;
            final org.w3c.dom.Node node=domsrc.getNode();
            final DOM2SAX dom2sax=new DOM2SAX(node);
            SAXImpl dtm;
            if(size<=0){
                dtm=new SAXImpl(this,source,documentID,
                        whiteSpaceFilter,null,doIndexing,
                        DTMDefaultBase.DEFAULT_BLOCKSIZE,
                        buildIdIndex,newNameTable);
            }else{
                dtm=new SAXImpl(this,source,documentID,
                        whiteSpaceFilter,null,doIndexing,
                        size,buildIdIndex,newNameTable);
            }
            dtm.setDocumentURI(source.getSystemId());
            addDTM(dtm,dtmPos,0);
            dom2sax.setContentHandler(dtm);
            try{
                dom2sax.parse();
            }catch(RuntimeException re){
                throw re;
            }catch(Exception e){
                throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
            }
            return dtm;
        }else{
            boolean isSAXSource=(null!=source)
                    ?(source instanceof SAXSource):true;
            boolean isStreamSource=(null!=source)
                    ?(source instanceof StreamSource):false;
            if(isSAXSource||isStreamSource){
                XMLReader reader;
                InputSource xmlSource;
                if(null==source){
                    xmlSource=null;
                    reader=null;
                    hasUserReader=false;  // Make sure the user didn't lie
                }else{
                    reader=getXMLReader(source);
                    xmlSource=SAXSource.sourceToInputSource(source);
                    String urlOfSource=xmlSource.getSystemId();
                    if(null!=urlOfSource){
                        try{
                            urlOfSource=SystemIDResolver.getAbsoluteURI(urlOfSource);
                        }catch(Exception e){
                            // %REVIEW% Is there a better way to send a warning?
                            System.err.println("Can not absolutize URL: "+urlOfSource);
                        }
                        xmlSource.setSystemId(urlOfSource);
                    }
                }
                // Create the basic SAX2DTM.
                SAXImpl dtm;
                if(size<=0){
                    dtm=new SAXImpl(this,source,documentID,whiteSpaceFilter,
                            null,doIndexing,
                            DTMDefaultBase.DEFAULT_BLOCKSIZE,
                            buildIdIndex,newNameTable);
                }else{
                    dtm=new SAXImpl(this,source,documentID,whiteSpaceFilter,
                            null,doIndexing,size,buildIdIndex,newNameTable);
                }
                // Go ahead and add the DTM to the lookup table.  This needs to be
                // done before any parsing occurs. Note offset 0, since we've just
                // created a new DTM.
                addDTM(dtm,dtmPos,0);
                if(null==reader){
                    // Then the user will construct it themselves.
                    return dtm;
                }
                reader.setContentHandler(dtm.getBuilder());
                if(!hasUserReader||null==reader.getDTDHandler()){
                    reader.setDTDHandler(dtm);
                }
                if(!hasUserReader||null==reader.getErrorHandler()){
                    reader.setErrorHandler(dtm);
                }
                try{
                    reader.setProperty("http://xml.org/sax/properties/lexical-handler",dtm);
                }catch(SAXNotRecognizedException e){
                }catch(SAXNotSupportedException e){
                }
                try{
                    reader.parse(xmlSource);
                }catch(RuntimeException re){
                    throw re;
                }catch(Exception e){
                    throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
                }finally{
                    if(!hasUserReader){
                        releaseXMLReader(reader);
                    }
                }
                if(DUMPTREE){
                    System.out.println("Dumping SAX2DOM");
                    dtm.dumpDTM(System.err);
                }
                return dtm;
            }else{
                // It should have been handled by a derived class or the caller
                // made a mistake.
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NOT_SUPPORTED,new Object[]{source}));
            }
        }
    }

    public DTM getDTM(Source source,boolean unique,
                      DTMWSFilter whiteSpaceFilter,boolean incremental,
                      boolean doIndexing,boolean buildIdIndex){
        return getDTM(source,unique,whiteSpaceFilter,incremental,
                doIndexing,false,0,buildIdIndex,false);
    }

    public DTM getDTM(Source source,boolean unique,
                      DTMWSFilter whiteSpaceFilter,boolean incremental,
                      boolean doIndexing,boolean buildIdIndex,
                      boolean newNameTable){
        return getDTM(source,unique,whiteSpaceFilter,incremental,
                doIndexing,false,0,buildIdIndex,newNameTable);
    }

    public DTM getDTM(Source source,boolean unique,
                      DTMWSFilter whiteSpaceFilter,boolean incremental,
                      boolean doIndexing,boolean hasUserReader,int size,
                      boolean buildIdIndex){
        return getDTM(source,unique,whiteSpaceFilter,incremental,
                doIndexing,hasUserReader,size,
                buildIdIndex,false);
    }
}
