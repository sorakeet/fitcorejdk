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
 * $Id: DTMManagerDefault.java,v 1.2.4.1 2005/09/15 08:15:02 suresh_emailid Exp $
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
 * $Id: DTMManagerDefault.java,v 1.2.4.1 2005/09/15 08:15:02 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xalan.internal.utils.FactoryImpl;
import com.sun.org.apache.xml.internal.dtm.*;
import com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTM;
import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM;
import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2RTFDTM;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import com.sun.org.apache.xml.internal.utils.XMLReaderManager;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

public class DTMManagerDefault extends DTMManager{
    //static final boolean JKESS_XNI_EXPERIMENT=true;
    private static final boolean DUMPTREE=false;
    private static final boolean DEBUG=false;
    protected DTM m_dtms[]=new DTM[256];
    protected XMLReaderManager m_readerManager=null;
    protected DefaultHandler m_defaultHandler=new DefaultHandler();
    int m_dtm_offsets[]=new int[256];
    private ExpandedNameTable m_expandedNameTable=
            new ExpandedNameTable();

    public DTMManagerDefault(){
    }

    synchronized public void addDTM(DTM dtm,int id){
        addDTM(dtm,id,0);
    }    synchronized public int getFirstFreeDTMID(){
        int n=m_dtms.length;
        for(int i=1;i<n;i++){
            if(null==m_dtms[i]){
                return i;
            }
        }
        return n; // count on addDTM() to throw exception if out of range
    }

    synchronized public void addDTM(DTM dtm,int id,int offset){
        if(id>=IDENT_MAX_DTMS){
            // TODO: %REVIEW% Not really the right error message.
            throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NO_DTMIDS_AVAIL,null)); //"No more DTM IDs are available!");
        }
        // We used to just allocate the array size to IDENT_MAX_DTMS.
        // But we expect to increase that to 16 bits, and I'm not willing
        // to allocate that much space unless needed. We could use one of our
        // handy-dandy Fast*Vectors, but this will do for now.
        // %REVIEW%
        int oldlen=m_dtms.length;
        if(oldlen<=id){
            // Various growth strategies are possible. I think we don't want
            // to over-allocate excessively, and I'm willing to reallocate
            // more often to get that. See also Fast*Vector classes.
            //
            // %REVIEW% Should throw a more diagnostic error if we go over the max...
            int newlen=Math.min((id+256),IDENT_MAX_DTMS);
            DTM new_m_dtms[]=new DTM[newlen];
            System.arraycopy(m_dtms,0,new_m_dtms,0,oldlen);
            m_dtms=new_m_dtms;
            int new_m_dtm_offsets[]=new int[newlen];
            System.arraycopy(m_dtm_offsets,0,new_m_dtm_offsets,0,oldlen);
            m_dtm_offsets=new_m_dtm_offsets;
        }
        m_dtms[id]=dtm;
        m_dtm_offsets[id]=offset;
        dtm.documentRegistration();
        // The DTM should have been told who its manager was when we created it.
        // Do we need to allow for adopting DTMs _not_ created by this manager?
    }

    public ExpandedNameTable getExpandedNameTable(DTM dtm){
        return m_expandedNameTable;
    }

    synchronized public DTM getDTM(Source source,boolean unique,
                                   DTMWSFilter whiteSpaceFilter,
                                   boolean incremental,boolean doIndexing){
        if(DEBUG&&null!=source)
            System.out.println("Starting "+
                    (unique?"UNIQUE":"shared")+
                    " source: "+source.getSystemId()
            );
        XMLStringFactory xstringFactory=m_xsf;
        int dtmPos=getFirstFreeDTMID();
        int documentID=dtmPos<<IDENT_DTM_NODE_BITS;
        if((null!=source)&&source instanceof DOMSource){
            DOM2DTM dtm=new DOM2DTM(this,(DOMSource)source,documentID,
                    whiteSpaceFilter,xstringFactory,doIndexing);
            addDTM(dtm,dtmPos,0);
            //      if (DUMPTREE)
            //      {
            //        dtm.dumpDTM();
            //      }
            return dtm;
        }else{
            boolean isSAXSource=(null!=source)
                    ?(source instanceof SAXSource):true;
            boolean isStreamSource=(null!=source)
                    ?(source instanceof StreamSource):false;
            if(isSAXSource||isStreamSource){
                XMLReader reader=null;
                SAX2DTM dtm;
                try{
                    InputSource xmlSource;
                    if(null==source){
                        xmlSource=null;
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
                    if(source==null&&unique&&!incremental&&!doIndexing){
                        // Special case to support RTF construction into shared DTM.
                        // It should actually still work for other uses,
                        // but may be slightly deoptimized relative to the base
                        // to allow it to deal with carrying multiple documents.
                        //
                        // %REVIEW% This is a sloppy way to request this mode;
                        // we need to consider architectural improvements.
                        dtm=new SAX2RTFDTM(this,source,documentID,whiteSpaceFilter,
                                xstringFactory,doIndexing);
                    }
                    /**************************************************************
                     // EXPERIMENTAL 3/22/02
                     else if(JKESS_XNI_EXPERIMENT && m_incremental) {
                     dtm = new XNI2DTM(this, source, documentID, whiteSpaceFilter,
                     xstringFactory, doIndexing);
                     }
                     **************************************************************/
                    // Create the basic SAX2DTM.
                    else{
                        dtm=new SAX2DTM(this,source,documentID,whiteSpaceFilter,
                                xstringFactory,doIndexing);
                    }
                    // Go ahead and add the DTM to the lookup table.  This needs to be
                    // done before any parsing occurs. Note offset 0, since we've just
                    // created a new DTM.
                    addDTM(dtm,dtmPos,0);
                    boolean haveXercesParser=
                            (null!=reader)
                                    &&(reader.getClass()
                                    .getName()
                                    .equals("com.sun.org.apache.xerces.internal.parsers.SAXParser"));
                    if(haveXercesParser){
                        incremental=true;  // No matter what.  %REVIEW%
                    }
                    // If the reader is null, but they still requested an incremental
                    // build, then we still want to set up the IncrementalSAXSource stuff.
                    if(m_incremental&&incremental
                    /** || ((null == reader) && incremental) */){
                        IncrementalSAXSource coParser=null;
                        if(haveXercesParser){
                            // IncrementalSAXSource_Xerces to avoid threading.
                            try{
                                coParser=(IncrementalSAXSource)
                                        Class.forName("com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Xerces").newInstance();
                            }catch(Exception ex){
                                ex.printStackTrace();
                                coParser=null;
                            }
                        }
                        if(coParser==null){
                            // Create a IncrementalSAXSource to run on the secondary thread.
                            if(null==reader){
                                coParser=new IncrementalSAXSource_Filter();
                            }else{
                                IncrementalSAXSource_Filter filter=
                                        new IncrementalSAXSource_Filter();
                                filter.setXMLReader(reader);
                                coParser=filter;
                            }
                        }
                        /**************************************************************
                         // EXPERIMENTAL 3/22/02
                         if (JKESS_XNI_EXPERIMENT && m_incremental &&
                         dtm instanceof XNI2DTM &&
                         coParser instanceof IncrementalSAXSource_Xerces) {
                         com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration xpc=
                         ((IncrementalSAXSource_Xerces)coParser)
                         .getXNIParserConfiguration();
                         if (xpc!=null) {
                         // Bypass SAX; listen to the XNI stream
                         ((XNI2DTM)dtm).setIncrementalXNISource(xpc);
                         } else {
                         // Listen to the SAX stream (will fail, diagnostically...)
                         dtm.setIncrementalSAXSource(coParser);
                         }
                         } else
                         ***************************************************************/
                        // Have the DTM set itself up as IncrementalSAXSource's listener.
                        dtm.setIncrementalSAXSource(coParser);
                        if(null==xmlSource){
                            // Then the user will construct it themselves.
                            return dtm;
                        }
                        if(null==reader.getErrorHandler()){
                            reader.setErrorHandler(dtm);
                        }
                        reader.setDTDHandler(dtm);
                        try{
                            // Launch parsing coroutine.  Launches a second thread,
                            // if we're using IncrementalSAXSource.filter().
                            coParser.startParse(xmlSource);
                        }catch(RuntimeException re){
                            dtm.clearCoRoutine();
                            throw re;
                        }catch(Exception e){
                            dtm.clearCoRoutine();
                            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
                        }
                    }else{
                        if(null==reader){
                            // Then the user will construct it themselves.
                            return dtm;
                        }
                        // not incremental
                        reader.setContentHandler(dtm);
                        reader.setDTDHandler(dtm);
                        if(null==reader.getErrorHandler()){
                            reader.setErrorHandler(dtm);
                        }
                        try{
                            reader.setProperty(
                                    "http://xml.org/sax/properties/lexical-handler",
                                    dtm);
                        }catch(SAXNotRecognizedException e){
                        }catch(SAXNotSupportedException e){
                        }
                        try{
                            reader.parse(xmlSource);
                        }catch(RuntimeException re){
                            dtm.clearCoRoutine();
                            throw re;
                        }catch(Exception e){
                            dtm.clearCoRoutine();
                            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
                        }
                    }
                    if(DUMPTREE){
                        System.out.println("Dumping SAX2DOM");
                        dtm.dumpDTM(System.err);
                    }
                    return dtm;
                }finally{
                    // Reset the ContentHandler, DTDHandler, ErrorHandler to the DefaultHandler
                    // after creating the DTM.
                    if(reader!=null&&!(m_incremental&&incremental)){
                        reader.setContentHandler(m_defaultHandler);
                        reader.setDTDHandler(m_defaultHandler);
                        reader.setErrorHandler(m_defaultHandler);
                        // Reset the LexicalHandler to null after creating the DTM.
                        try{
                            reader.setProperty("http://xml.org/sax/properties/lexical-handler",null);
                        }catch(Exception e){
                        }
                    }
                    releaseXMLReader(reader);
                }
            }else{
                // It should have been handled by a derived class or the caller
                // made a mistake.
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NOT_SUPPORTED,new Object[]{source})); //"Not supported: " + source);
            }
        }
    }

    synchronized public int getDTMHandleFromNode(Node node){
        if(null==node)
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NODE_NON_NULL,null)); //"node must be non-null for getDTMHandleFromNode!");
        if(node instanceof DTMNodeProxy)
            return ((DTMNodeProxy)node).getDTMNodeNumber();
        else{
            // Find the DOM2DTMs wrapped around this Document (if any)
            // and check whether they contain the Node in question.
            //
            // NOTE that since a DOM2DTM may represent a subtree rather
            // than a full document, we have to be prepared to check more
            // than one -- and there is no guarantee that we will find
            // one that contains ancestors or siblings of the node we're
            // seeking.
            //
            // %REVIEW% We could search for the one which contains this
            // node at the deepest level, and thus covers the widest
            // subtree, but that's going to entail additional work
            // checking more DTMs... and getHandleOfNode is not a
            // cheap operation in most implementations.
            //
            // TODO: %REVIEW% If overflow addressing, we may recheck a DTM
            // already examined. Ouch. But with the increased number of DTMs,
            // scanning back to check this is painful.
            // POSSIBLE SOLUTIONS:
            //   Generate a list of _unique_ DTM objects?
            //   Have each DTM cache last DOM node search?
            int max=m_dtms.length;
            for(int i=0;i<max;i++){
                DTM thisDTM=m_dtms[i];
                if((null!=thisDTM)&&thisDTM instanceof DOM2DTM){
                    int handle=((DOM2DTM)thisDTM).getHandleOfNode(node);
                    if(handle!=DTM.NULL) return handle;
                }
            }
            // Not found; generate a new DTM.
            //
            // %REVIEW% Is this really desirable, or should we return null
            // and make folks explicitly instantiate from a DOMSource? The
            // latter is more work but gives the caller the opportunity to
            // explicitly add the DTM to a DTMManager... and thus to know when
            // it can be discarded again, which is something we need to pay much
            // more attention to. (Especially since only DTMs which are assigned
            // to a manager can use the overflow addressing scheme.)
            //
            // %BUG% If the source node was a DOM2DTM$defaultNamespaceDeclarationNode
            // and the DTM wasn't registered with this DTMManager, we will create
            // a new DTM and _still_ not be able to find the node (since it will
            // be resynthesized). Another reason to push hard on making all DTMs
            // be managed DTMs.
            // Since the real root of our tree may be a DocumentFragment, we need to
            // use getParent to find the root, instead of getOwnerDocument.  Otherwise
            // DOM2DTM#getHandleOfNode will be very unhappy.
            Node root=node;
            Node p=(root.getNodeType()==Node.ATTRIBUTE_NODE)?((org.w3c.dom.Attr)root).getOwnerElement():root.getParentNode();
            for(;p!=null;p=p.getParentNode()){
                root=p;
            }
            DOM2DTM dtm=(DOM2DTM)getDTM(new DOMSource(root),
                    false,null,true,true);
            int handle;
            if(node instanceof com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTMdefaultNamespaceDeclarationNode){
                // Can't return the same node since it's unique to a specific DTM,
                // but can return the equivalent node -- find the corresponding
                // Document Element, then ask it for the xml: namespace decl.
                handle=dtm.getHandleOfNode(((org.w3c.dom.Attr)node).getOwnerElement());
                handle=dtm.getAttributeNode(handle,node.getNamespaceURI(),node.getLocalName());
            }else
                handle=((DOM2DTM)dtm).getHandleOfNode(node);
            if(DTM.NULL==handle)
                throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COULD_NOT_RESOLVE_NODE,null)); //"Could not resolve the node to a handle!");
            return handle;
        }
    }

    synchronized public XMLReader getXMLReader(Source inputSource){
        try{
            XMLReader reader=(inputSource instanceof SAXSource)
                    ?((SAXSource)inputSource).getXMLReader():null;
            // If user did not supply a reader, ask for one from the reader manager
            if(null==reader){
                if(m_readerManager==null){
                    m_readerManager=XMLReaderManager.getInstance(super.useServicesMechnism());
                }
                reader=m_readerManager.getXMLReader();
            }
            return reader;
        }catch(SAXException se){
            throw new DTMException(se.getMessage(),se);
        }
    }

    synchronized public void releaseXMLReader(XMLReader reader){
        if(m_readerManager!=null){
            m_readerManager.releaseXMLReader(reader);
        }
    }

    synchronized public DTM getDTM(int nodeHandle){
        try{
            // Performance critical function.
            return m_dtms[nodeHandle>>>IDENT_DTM_NODE_BITS];
        }catch(ArrayIndexOutOfBoundsException e){
            if(nodeHandle==DTM.NULL)
                return null;            // Accept as a special case.
            else
                throw e;                // Programming error; want to know about it.
        }
    }

    synchronized public int getDTMIdentity(DTM dtm){
        // Shortcut using DTMDefaultBase's extension hooks
        // %REVIEW% Should the lookup be part of the basic DTM API?
        if(dtm instanceof DTMDefaultBase){
            DTMDefaultBase dtmdb=(DTMDefaultBase)dtm;
            if(dtmdb.getManager()==this)
                return dtmdb.getDTMIDs().elementAt(0);
            else
                return -1;
        }
        int n=m_dtms.length;
        for(int i=0;i<n;i++){
            DTM tdtm=m_dtms[i];
            if(tdtm==dtm&&m_dtm_offsets[i]==0)
                return i<<IDENT_DTM_NODE_BITS;
        }
        return -1;
    }

    synchronized public boolean release(DTM dtm,boolean shouldHardDelete){
        if(DEBUG){
            System.out.println("Releasing "+
                    (shouldHardDelete?"HARD":"soft")+
                    " dtm="+
                    // Following shouldn't need a nodeHandle, but does...
                    // and doesn't seem to report the intended value
                    dtm.getDocumentBaseURI()
            );
        }
        if(dtm instanceof SAX2DTM){
            ((SAX2DTM)dtm).clearCoRoutine();
        }
        // Multiple DTM IDs may be assigned to a single DTM.
        // The Right Answer is to ask which (if it supports
        // extension, the DTM will need a list anyway). The
        // Wrong Answer, applied if the DTM can't help us,
        // is to linearly search them all; this may be very
        // painful.
        //
        // %REVIEW% Should the lookup move up into the basic DTM API?
        if(dtm instanceof DTMDefaultBase){
            com.sun.org.apache.xml.internal.utils.SuballocatedIntVector ids=((DTMDefaultBase)dtm).getDTMIDs();
            for(int i=ids.size()-1;i>=0;--i)
                m_dtms[ids.elementAt(i)>>>DTMManager.IDENT_DTM_NODE_BITS]=null;
        }else{
            int i=getDTMIdentity(dtm);
            if(i>=0){
                m_dtms[i>>>DTMManager.IDENT_DTM_NODE_BITS]=null;
            }
        }
        dtm.documentRelease();
        return true;
    }

    synchronized public DTM createDocumentFragment(){
        try{
            DocumentBuilderFactory dbf=FactoryImpl.getDOMFactory(super.useServicesMechnism());
            dbf.setNamespaceAware(true);
            DocumentBuilder db=dbf.newDocumentBuilder();
            Document doc=db.newDocument();
            Node df=doc.createDocumentFragment();
            return getDTM(new DOMSource(df),true,null,false,false);
        }catch(Exception e){
            throw new DTMException(e);
        }
    }

    synchronized public DTMIterator createDTMIterator(int whatToShow,DTMFilter filter,
                                                      boolean entityReferenceExpansion){
        /** @todo: implement this com.sun.org.apache.xml.internal.dtm.DTMManager abstract method */
        return null;
    }

    synchronized public DTMIterator createDTMIterator(String xpathString,
                                                      PrefixResolver presolver){
        /** @todo: implement this com.sun.org.apache.xml.internal.dtm.DTMManager abstract method */
        return null;
    }

    synchronized public DTMIterator createDTMIterator(int node){
        /** @todo: implement this com.sun.org.apache.xml.internal.dtm.DTMManager abstract method */
        return null;
    }

    synchronized public DTMIterator createDTMIterator(Object xpathCompiler,int pos){
        /** @todo: implement this com.sun.org.apache.xml.internal.dtm.DTMManager abstract method */
        return null;
    }


}
