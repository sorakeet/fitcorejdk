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
 * $Id: SAX2DTM.java,v 1.2.4.1 2005/09/15 08:15:11 suresh_emailid Exp $
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
 * $Id: SAX2DTM.java,v 1.2.4.1 2005/09/15 08:15:11 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref.sax2dtm;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMManager;
import com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import com.sun.org.apache.xml.internal.dtm.ref.*;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import com.sun.org.apache.xml.internal.utils.*;
import org.xml.sax.*;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SAX2DTM extends DTMDefaultBaseIterators
        implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler,
        DeclHandler, LexicalHandler{
    private static final boolean DEBUG=false;
    private static final String[] m_fixednames={null,
            null,  // nothing, Element
            null,"#text",  // Attr, Text
            "#cdata_section",null,  // CDATA, EntityReference
            null,null,  // Entity, PI
            "#comment","#document",  // Comment, Document
            null,"#document-fragment",  // Doctype, DocumentFragment
            null};  // Notation
    private static final int ENTITY_FIELD_PUBLICID=0;
    private static final int ENTITY_FIELD_SYSTEMID=1;
    private static final int ENTITY_FIELD_NOTATIONNAME=2;
    private static final int ENTITY_FIELD_NAME=3;
    private static final int ENTITY_FIELDS_PER=4;
    //private FastStringBuffer m_chars = new FastStringBuffer(13, 13);
    protected FastStringBuffer m_chars;
    protected SuballocatedIntVector m_data;
    transient protected IntStack m_parents;
    transient protected int m_previous=0;
    transient protected Vector m_prefixMappings=
            new Vector();
    transient protected IntStack m_contextIndexes;
    transient protected int m_textType=DTM.TEXT_NODE;
    transient protected int m_coalescedTextType=DTM.TEXT_NODE;
    transient protected Locator m_locator=null;
    transient protected boolean m_insideDTD=false;
    protected DTMTreeWalker m_walker=new DTMTreeWalker();
    protected DTMStringPool m_valuesOrPrefixes;
    protected boolean m_endDocumentOccured=false;
    protected SuballocatedIntVector m_dataOrQName;
    protected Map<String,Integer> m_idAttributes=new HashMap<>();
    protected int m_textPendingStart=-1;
    protected boolean m_useSourceLocationProperty=false;
    protected StringVector m_sourceSystemId;
    protected IntVector m_sourceLine;
    protected IntVector m_sourceColumn;
    boolean m_pastFirstElement=false;
    private IncrementalSAXSource m_incrementalSAXSource=null;
    transient private String m_systemId=null;
    private Vector m_entities=null;

    public SAX2DTM(DTMManager mgr,Source source,int dtmIdentity,
                   DTMWSFilter whiteSpaceFilter,
                   XMLStringFactory xstringfactory,
                   boolean doIndexing){
        this(mgr,source,dtmIdentity,whiteSpaceFilter,
                xstringfactory,doIndexing,DEFAULT_BLOCKSIZE,true,false);
    }

    public SAX2DTM(DTMManager mgr,Source source,int dtmIdentity,
                   DTMWSFilter whiteSpaceFilter,
                   XMLStringFactory xstringfactory,
                   boolean doIndexing,
                   int blocksize,
                   boolean usePrevsib,
                   boolean newNameTable){
        super(mgr,source,dtmIdentity,whiteSpaceFilter,
                xstringfactory,doIndexing,blocksize,usePrevsib,newNameTable);
        // %OPT% Use smaller sizes for all internal storage units when
        // the blocksize is small. This reduces the cost of creating an RTF.
        if(blocksize<=64){
            m_data=new SuballocatedIntVector(blocksize,DEFAULT_NUMBLOCKS_SMALL);
            m_dataOrQName=new SuballocatedIntVector(blocksize,DEFAULT_NUMBLOCKS_SMALL);
            m_valuesOrPrefixes=new DTMStringPool(16);
            m_chars=new FastStringBuffer(7,10);
            m_contextIndexes=new IntStack(4);
            m_parents=new IntStack(4);
        }else{
            m_data=new SuballocatedIntVector(blocksize,DEFAULT_NUMBLOCKS);
            m_dataOrQName=new SuballocatedIntVector(blocksize,DEFAULT_NUMBLOCKS);
            m_valuesOrPrefixes=new DTMStringPool();
            m_chars=new FastStringBuffer(10,13);
            m_contextIndexes=new IntStack();
            m_parents=new IntStack();
        }
        // %REVIEW%  Initial size pushed way down to reduce weight of RTFs
        // (I'm not entirely sure 0 would work, so I'm playing it safe for now.)
        //m_data = new SuballocatedIntVector(doIndexing ? (1024*2) : 512, 1024);
        //m_data = new SuballocatedIntVector(blocksize);
        m_data.addElement(0);   // Need placeholder in case index into here must be <0.
        //m_dataOrQName = new SuballocatedIntVector(blocksize);
        // m_useSourceLocationProperty=com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl.m_source_location;
        m_useSourceLocationProperty=mgr.getSource_location();
        m_sourceSystemId=(m_useSourceLocationProperty)?new StringVector():null;
        m_sourceLine=(m_useSourceLocationProperty)?new IntVector():null;
        m_sourceColumn=(m_useSourceLocationProperty)?new IntVector():null;
    }

    public void setUseSourceLocation(boolean useSourceLocation){
        m_useSourceLocationProperty=useSourceLocation;
    }

    public void setIncrementalSAXSource(IncrementalSAXSource incrementalSAXSource){
        // Establish coroutine link so we can request more data
        //
        // Note: It's possible that some versions of IncrementalSAXSource may
        // not actually use a CoroutineManager, and hence may not require
        // that we obtain an Application Coroutine ID. (This relies on the
        // coroutine transaction details having been encapsulated in the
        // IncrementalSAXSource.do...() methods.)
        m_incrementalSAXSource=incrementalSAXSource;
        // Establish SAX-stream link so we can receive the requested data
        incrementalSAXSource.setContentHandler(this);
        incrementalSAXSource.setLexicalHandler(this);
        incrementalSAXSource.setDTDHandler(this);
        // Are the following really needed? incrementalSAXSource doesn't yet
        // support them, and they're mostly no-ops here...
        //incrementalSAXSource.setErrorHandler(this);
        //incrementalSAXSource.setDeclHandler(this);
    }

    protected int getNextNodeIdentity(int identity){
        identity+=1;
        while(identity>=m_size){
            if(null==m_incrementalSAXSource)
                return DTM.NULL;
            nextNode();
        }
        return identity;
    }

    protected boolean nextNode(){
        if(null==m_incrementalSAXSource)
            return false;
        if(m_endDocumentOccured){
            clearCoRoutine();
            return false;
        }
        Object gotMore=m_incrementalSAXSource.deliverMoreNodes(true);
        // gotMore may be a Boolean (TRUE if still parsing, FALSE if
        // EOF) or an exception if IncrementalSAXSource malfunctioned
        // (code error rather than user error).
        //
        // %REVIEW% Currently the ErrorHandlers sketched herein are
        // no-ops, so I'm going to initially leave this also as a
        // no-op.
        if(!(gotMore instanceof Boolean)){
            if(gotMore instanceof RuntimeException){
                throw (RuntimeException)gotMore;
            }else if(gotMore instanceof Exception){
                throw new WrappedRuntimeException((Exception)gotMore);
            }
            // for now...
            clearCoRoutine();
            return false;
            // %TBD%
        }
        if(gotMore!=Boolean.TRUE){
            // EOF reached without satisfying the request
            clearCoRoutine();  // Drop connection, stop trying
            // %TBD% deregister as its listener?
        }
        return true;
    }

    public void clearCoRoutine(){
        clearCoRoutine(true);
    }

    public void clearCoRoutine(boolean callDoTerminate){
        if(null!=m_incrementalSAXSource){
            if(callDoTerminate)
                m_incrementalSAXSource.deliverMoreNodes(false);
            m_incrementalSAXSource=null;
        }
    }

    public int getNumberOfNodes(){
        return m_size;
    }

    public int getAttributeNode(int nodeHandle,String namespaceURI,
                                String name){
        for(int attrH=getFirstAttribute(nodeHandle);DTM.NULL!=attrH;
            attrH=getNextAttribute(attrH)){
            String attrNS=getNamespaceURI(attrH);
            String attrName=getLocalName(attrH);
            boolean nsMatch=namespaceURI==attrNS
                    ||(namespaceURI!=null
                    &&namespaceURI.equals(attrNS));
            if(nsMatch&&name.equals(attrName))
                return attrH;
        }
        return DTM.NULL;
    }

    public XMLString getStringValue(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int type;
        if(identity==DTM.NULL) // Separate lines because I wanted to breakpoint it
            type=DTM.NULL;
        else
            type=_type(identity);
        if(isTextType(type)){
            int dataIndex=_dataOrQName(identity);
            int offset=m_data.elementAt(dataIndex);
            int length=m_data.elementAt(dataIndex+1);
            return m_xstrf.newstr(m_chars,offset,length);
        }else{
            int firstChild=_firstch(identity);
            if(DTM.NULL!=firstChild){
                int offset=-1;
                int length=0;
                int startNode=identity;
                identity=firstChild;
                do{
                    type=_type(identity);
                    if(isTextType(type)){
                        int dataIndex=_dataOrQName(identity);
                        if(-1==offset){
                            offset=m_data.elementAt(dataIndex);
                        }
                        length+=m_data.elementAt(dataIndex+1);
                    }
                    identity=getNextNodeIdentity(identity);
                }while(DTM.NULL!=identity&&(_parent(identity)>=startNode));
                if(length>0){
                    return m_xstrf.newstr(m_chars,offset,length);
                }
            }else if(type!=DTM.ELEMENT_NODE){
                int dataIndex=_dataOrQName(identity);
                if(dataIndex<0){
                    dataIndex=-dataIndex;
                    dataIndex=m_data.elementAt(dataIndex+1);
                }
                return m_xstrf.newstr(m_valuesOrPrefixes.indexToString(dataIndex));
            }
        }
        return m_xstrf.emptystr();
    }

    public String getNodeName(int nodeHandle){
        int expandedTypeID=getExpandedTypeID(nodeHandle);
        // If just testing nonzero, no need to shift...
        int namespaceID=m_expandedNameTable.getNamespaceID(expandedTypeID);
        if(0==namespaceID){
            // Don't retrieve name until/unless needed
            // String name = m_expandedNameTable.getLocalName(expandedTypeID);
            int type=getNodeType(nodeHandle);
            if(type==DTM.NAMESPACE_NODE){
                if(null==m_expandedNameTable.getLocalName(expandedTypeID))
                    return "xmlns";
                else
                    return "xmlns:"+m_expandedNameTable.getLocalName(expandedTypeID);
            }else if(0==m_expandedNameTable.getLocalNameID(expandedTypeID)){
                return m_fixednames[type];
            }else
                return m_expandedNameTable.getLocalName(expandedTypeID);
        }else{
            int qnameIndex=m_dataOrQName.elementAt(makeNodeIdentity(nodeHandle));
            if(qnameIndex<0){
                qnameIndex=-qnameIndex;
                qnameIndex=m_data.elementAt(qnameIndex);
            }
            return m_valuesOrPrefixes.indexToString(qnameIndex);
        }
    }

    public String getNodeNameX(int nodeHandle){
        int expandedTypeID=getExpandedTypeID(nodeHandle);
        int namespaceID=m_expandedNameTable.getNamespaceID(expandedTypeID);
        if(0==namespaceID){
            String name=m_expandedNameTable.getLocalName(expandedTypeID);
            if(name==null)
                return "";
            else
                return name;
        }else{
            int qnameIndex=m_dataOrQName.elementAt(makeNodeIdentity(nodeHandle));
            if(qnameIndex<0){
                qnameIndex=-qnameIndex;
                qnameIndex=m_data.elementAt(qnameIndex);
            }
            return m_valuesOrPrefixes.indexToString(qnameIndex);
        }
    }

    public String getLocalName(int nodeHandle){
        return m_expandedNameTable.getLocalName(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public String getPrefix(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int type=_type(identity);
        if(DTM.ELEMENT_NODE==type){
            int prefixIndex=_dataOrQName(identity);
            if(0==prefixIndex)
                return "";
            else{
                String qname=m_valuesOrPrefixes.indexToString(prefixIndex);
                return getPrefix(qname,null);
            }
        }else if(DTM.ATTRIBUTE_NODE==type){
            int prefixIndex=_dataOrQName(identity);
            if(prefixIndex<0){
                prefixIndex=m_data.elementAt(-prefixIndex);
                String qname=m_valuesOrPrefixes.indexToString(prefixIndex);
                return getPrefix(qname,null);
            }
        }
        return "";
    }

    public String getNamespaceURI(int nodeHandle){
        return m_expandedNameTable.getNamespace(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public String getNodeValue(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int type=_type(identity);
        if(isTextType(type)){
            int dataIndex=_dataOrQName(identity);
            int offset=m_data.elementAt(dataIndex);
            int length=m_data.elementAt(dataIndex+1);
            // %OPT% We should cache this, I guess.
            return m_chars.getString(offset,length);
        }else if(DTM.ELEMENT_NODE==type||DTM.DOCUMENT_FRAGMENT_NODE==type
                ||DTM.DOCUMENT_NODE==type){
            return null;
        }else{
            int dataIndex=_dataOrQName(identity);
            if(dataIndex<0){
                dataIndex=-dataIndex;
                dataIndex=m_data.elementAt(dataIndex+1);
            }
            return m_valuesOrPrefixes.indexToString(dataIndex);
        }
    }

    protected int _dataOrQName(int identity){
        if(identity<m_size)
            return m_dataOrQName.elementAt(identity);
        // Check to see if the information requested has been processed, and,
        // if not, advance the iterator until we the information has been
        // processed.
        while(true){
            boolean isMore=nextNode();
            if(!isMore)
                return NULL;
            else if(identity<m_size)
                return m_dataOrQName.elementAt(identity);
        }
    }

    public String getDocumentTypeDeclarationSystemIdentifier(){
        /** @todo: implement this com.sun.org.apache.xml.internal.dtm.DTMDefaultBase abstract method */
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED,null));//"Not yet supported!");
        return null;
    }

    public String getDocumentTypeDeclarationPublicIdentifier(){
        /** @todo: implement this com.sun.org.apache.xml.internal.dtm.DTMDefaultBase abstract method */
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED,null));//"Not yet supported!");
        return null;
    }

    public int getElementById(String elementId){
        Integer intObj;
        boolean isMore=true;
        do{
            intObj=m_idAttributes.get(elementId);
            if(null!=intObj)
                return makeNodeHandle(intObj.intValue());
            if(!isMore||m_endDocumentOccured)
                break;
            isMore=nextNode();
        }
        while(null==intObj);
        return DTM.NULL;
    }

    public String getUnparsedEntityURI(String name){
        String url="";
        if(null==m_entities)
            return url;
        int n=m_entities.size();
        for(int i=0;i<n;i+=ENTITY_FIELDS_PER){
            String ename=(String)m_entities.elementAt(i+ENTITY_FIELD_NAME);
            if(null!=ename&&ename.equals(name)){
                String nname=(String)m_entities.elementAt(i
                        +ENTITY_FIELD_NOTATIONNAME);
                if(null!=nname){
                    // The draft says: "The XSLT processor may use the public
                    // identifier to generate a URI for the entity instead of the URI
                    // specified in the system identifier. If the XSLT processor does
                    // not use the public identifier to generate the URI, it must use
                    // the system identifier; if the system identifier is a relative
                    // URI, it must be resolved into an absolute URI using the URI of
                    // the resource containing the entity declaration as the base
                    // URI [RFC2396]."
                    // So I'm falling a bit short here.
                    url=(String)m_entities.elementAt(i+ENTITY_FIELD_SYSTEMID);
                    if(null==url){
                        url=(String)m_entities.elementAt(i+ENTITY_FIELD_PUBLICID);
                    }
                }
                break;
            }
        }
        return url;
    }

    public boolean isAttributeSpecified(int attributeHandle){
        // I'm not sure if I want to do anything with this...
        return true;  // ??
    }
//    /**
//     * Ensure that the size of the information arrays can hold another entry
//     * at the given index.
//     *
//     * @param on exit from this function, the information arrays sizes must be
//     * at least index+1.
//     *
//     * NEEDSDOC @param index
//     */
//    protected void ensureSize(int index)
//    {
//          // dataOrQName is an SuballocatedIntVector and hence self-sizing.
//          // But DTMDefaultBase may need fixup.
//        super.ensureSize(index);
//    }

    public void dispatchCharactersEvents(int nodeHandle,ContentHandler ch,
                                         boolean normalize)
            throws SAXException{
        int identity=makeNodeIdentity(nodeHandle);
        if(identity==DTM.NULL)
            return;
        int type=_type(identity);
        if(isTextType(type)){
            int dataIndex=m_dataOrQName.elementAt(identity);
            int offset=m_data.elementAt(dataIndex);
            int length=m_data.elementAt(dataIndex+1);
            if(normalize)
                m_chars.sendNormalizedSAXcharacters(ch,offset,length);
            else
                m_chars.sendSAXcharacters(ch,offset,length);
        }else{
            int firstChild=_firstch(identity);
            if(DTM.NULL!=firstChild){
                int offset=-1;
                int length=0;
                int startNode=identity;
                identity=firstChild;
                do{
                    type=_type(identity);
                    if(isTextType(type)){
                        int dataIndex=_dataOrQName(identity);
                        if(-1==offset){
                            offset=m_data.elementAt(dataIndex);
                        }
                        length+=m_data.elementAt(dataIndex+1);
                    }
                    identity=getNextNodeIdentity(identity);
                }while(DTM.NULL!=identity&&(_parent(identity)>=startNode));
                if(length>0){
                    if(normalize)
                        m_chars.sendNormalizedSAXcharacters(ch,offset,length);
                    else
                        m_chars.sendSAXcharacters(ch,offset,length);
                }
            }else if(type!=DTM.ELEMENT_NODE){
                int dataIndex=_dataOrQName(identity);
                if(dataIndex<0){
                    dataIndex=-dataIndex;
                    dataIndex=m_data.elementAt(dataIndex+1);
                }
                String str=m_valuesOrPrefixes.indexToString(dataIndex);
                if(normalize)
                    FastStringBuffer.sendNormalizedSAXcharacters(str.toCharArray(),
                            0,str.length(),ch);
                else
                    ch.characters(str.toCharArray(),0,str.length());
            }
        }
    }

    public void dispatchToEvents(int nodeHandle,ContentHandler ch)
            throws SAXException{
        DTMTreeWalker treeWalker=m_walker;
        ContentHandler prevCH=treeWalker.getcontentHandler();
        if(null!=prevCH){
            treeWalker=new DTMTreeWalker();
        }
        treeWalker.setcontentHandler(ch);
        treeWalker.setDTM(this);
        try{
            treeWalker.traverse(nodeHandle);
        }finally{
            treeWalker.setcontentHandler(null);
        }
    }

    public void migrateTo(DTMManager manager){
        super.migrateTo(manager);
        // We have to reset the information in m_dtmIdent and
        // register the DTM with the new manager.
        int numDTMs=m_dtmIdent.size();
        int dtmId=m_mgrDefault.getFirstFreeDTMID();
        int nodeIndex=0;
        for(int i=0;i<numDTMs;i++){
            m_dtmIdent.setElementAt(dtmId<<DTMManager.IDENT_DTM_NODE_BITS,i);
            m_mgrDefault.addDTM(this,dtmId,nodeIndex);
            dtmId++;
            nodeIndex+=(1<<DTMManager.IDENT_DTM_NODE_BITS);
        }
    }

    private final boolean isTextType(int type){
        return (DTM.TEXT_NODE==type||DTM.CDATA_SECTION_NODE==type);
    }

    public String getPrefix(String qname,String uri){
        String prefix;
        int uriIndex=-1;
        if(null!=uri&&uri.length()>0){
            do{
                uriIndex=m_prefixMappings.indexOf(uri,++uriIndex);
            }while((uriIndex&0x01)==0);
            if(uriIndex>=0){
                prefix=(String)m_prefixMappings.elementAt(uriIndex-1);
            }else if(null!=qname){
                int indexOfNSSep=qname.indexOf(':');
                if(qname.equals("xmlns"))
                    prefix="";
                else if(qname.startsWith("xmlns:"))
                    prefix=qname.substring(indexOfNSSep+1);
                else
                    prefix=(indexOfNSSep>0)
                            ?qname.substring(0,indexOfNSSep):null;
            }else{
                prefix=null;
            }
        }else if(null!=qname){
            int indexOfNSSep=qname.indexOf(':');
            if(indexOfNSSep>0){
                if(qname.startsWith("xmlns:"))
                    prefix=qname.substring(indexOfNSSep+1);
                else
                    prefix=qname.substring(0,indexOfNSSep);
            }else{
                if(qname.equals("xmlns"))
                    prefix="";
                else
                    prefix=null;
            }
        }else{
            prefix=null;
        }
        return prefix;
    }

    public boolean isWhitespace(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int type;
        if(identity==DTM.NULL) // Separate lines because I wanted to breakpoint it
            type=DTM.NULL;
        else
            type=_type(identity);
        if(isTextType(type)){
            int dataIndex=_dataOrQName(identity);
            int offset=m_data.elementAt(dataIndex);
            int length=m_data.elementAt(dataIndex+1);
            return m_chars.isWhitespace(offset,length);
        }
        return false;
    }

    public int getIdForNamespace(String uri){
        return m_valuesOrPrefixes.stringToIndex(uri);
    }

    public String getNamespaceURI(String prefix){
        String uri="";
        int prefixIndex=m_contextIndexes.peek()-1;
        if(null==prefix)
            prefix="";
        do{
            prefixIndex=m_prefixMappings.indexOf(prefix,++prefixIndex);
        }while((prefixIndex>=0)&&(prefixIndex&0x01)==0x01);
        if(prefixIndex>-1){
            uri=(String)m_prefixMappings.elementAt(prefixIndex+1);
        }
        return uri;
    }

    public void setIDAttribute(String id,int elem){
        m_idAttributes.put(id,elem);
    }

    public InputSource resolveEntity(String publicId,String systemId)
            throws SAXException{
        return null;
    }

    public void notationDecl(String name,String publicId,String systemId)
            throws SAXException{
        // no op
    }

    public void unparsedEntityDecl(
            String name,String publicId,String systemId,String notationName)
            throws SAXException{
        if(null==m_entities){
            m_entities=new Vector();
        }
        try{
            systemId=SystemIDResolver.getAbsoluteURI(systemId,
                    getDocumentBaseURI());
        }catch(Exception e){
            throw new SAXException(e);
        }
        //  private static final int ENTITY_FIELD_PUBLICID = 0;
        m_entities.addElement(publicId);
        //  private static final int ENTITY_FIELD_SYSTEMID = 1;
        m_entities.addElement(systemId);
        //  private static final int ENTITY_FIELD_NOTATIONNAME = 2;
        m_entities.addElement(notationName);
        //  private static final int ENTITY_FIELD_NAME = 3;
        m_entities.addElement(name);
    }

    public void setDocumentLocator(Locator locator){
        m_locator=locator;
        m_systemId=locator.getSystemId();
    }

    public void startDocument() throws SAXException{
        if(DEBUG)
            System.out.println("startDocument");
        int doc=addNode(DTM.DOCUMENT_NODE,
                m_expandedNameTable.getExpandedTypeID(DTM.DOCUMENT_NODE),
                DTM.NULL,DTM.NULL,0,true);
        m_parents.push(doc);
        m_previous=DTM.NULL;
        m_contextIndexes.push(m_prefixMappings.size());  // for the next element.
    }

    protected int addNode(int type,int expandedTypeID,
                          int parentIndex,int previousSibling,
                          int dataOrPrefix,boolean canHaveFirstChild){
        // Common to all nodes:
        int nodeIndex=m_size++;
        // Have we overflowed a DTM Identity's addressing range?
        if(m_dtmIdent.size()==(nodeIndex>>>DTMManager.IDENT_DTM_NODE_BITS)){
            addNewDTMID(nodeIndex);
        }
        m_firstch.addElement(canHaveFirstChild?NOTPROCESSED:DTM.NULL);
        m_nextsib.addElement(NOTPROCESSED);
        m_parent.addElement(parentIndex);
        m_exptype.addElement(expandedTypeID);
        m_dataOrQName.addElement(dataOrPrefix);
        if(m_prevsib!=null){
            m_prevsib.addElement(previousSibling);
        }
        if(DTM.NULL!=previousSibling){
            m_nextsib.setElementAt(nodeIndex,previousSibling);
        }
        if(m_locator!=null&&m_useSourceLocationProperty){
            setSourceLocation();
        }
        // Note that nextSibling is not processed until charactersFlush()
        // is called, to handle successive characters() events.
        // Special handling by type: Declare namespaces, attach first child
        switch(type){
            case DTM.NAMESPACE_NODE:
                declareNamespaceInContext(parentIndex,nodeIndex);
                break;
            case DTM.ATTRIBUTE_NODE:
                break;
            default:
                if(DTM.NULL==previousSibling&&DTM.NULL!=parentIndex){
                    m_firstch.setElementAt(nodeIndex,parentIndex);
                }
                break;
        }
        return nodeIndex;
    }

    protected void addNewDTMID(int nodeIndex){
        try{
            if(m_mgr==null)
                throw new ClassCastException();
            // Handle as Extended Addressing
            DTMManagerDefault mgrD=(DTMManagerDefault)m_mgr;
            int id=mgrD.getFirstFreeDTMID();
            mgrD.addDTM(this,id,nodeIndex);
            m_dtmIdent.addElement(id<<DTMManager.IDENT_DTM_NODE_BITS);
        }catch(ClassCastException e){
            // %REVIEW% Wrong error message, but I've been told we're trying
            // not to add messages right not for I18N reasons.
            // %REVIEW% Should this be a Fatal Error?
            error(XMLMessages.createXMLMessage(XMLErrorResources.ER_NO_DTMIDS_AVAIL,null));//"No more DTM IDs are available";
        }
    }

    protected void setSourceLocation(){
        m_sourceSystemId.addElement(m_locator.getSystemId());
        m_sourceLine.addElement(m_locator.getLineNumber());
        m_sourceColumn.addElement(m_locator.getColumnNumber());
        //%REVIEW% %BUG% Prevent this from arising in the first place
        // by not allowing the enabling conditions to change after we start
        // building the document.
        if(m_sourceSystemId.size()!=m_size){
            String msg="CODING ERROR in Source Location: "+m_size+" != "
                    +m_sourceSystemId.size();
            System.err.println(msg);
            throw new RuntimeException(msg);
        }
    }

    public void endDocument() throws SAXException{
        if(DEBUG)
            System.out.println("endDocument");
        charactersFlush();
        m_nextsib.setElementAt(NULL,0);
        if(m_firstch.elementAt(0)==NOTPROCESSED)
            m_firstch.setElementAt(NULL,0);
        if(DTM.NULL!=m_previous)
            m_nextsib.setElementAt(DTM.NULL,m_previous);
        m_parents=null;
        m_prefixMappings=null;
        m_contextIndexes=null;
        m_endDocumentOccured=true;
        // Bugzilla 4858: throw away m_locator. we cache m_systemId
        m_locator=null;
    }

    protected void charactersFlush(){
        if(m_textPendingStart>=0)  // -1 indicates no-text-in-progress
        {
            int length=m_chars.size()-m_textPendingStart;
            boolean doStrip=false;
            if(getShouldStripWhitespace()){
                doStrip=m_chars.isWhitespace(m_textPendingStart,length);
            }
            if(doStrip){
                m_chars.setLength(m_textPendingStart);  // Discard accumulated text
            }else{
                // Guard against characters/ignorableWhitespace events that
                // contained no characters.  They should not result in a node.
                if(length>0){
                    int exName=m_expandedNameTable.getExpandedTypeID(DTM.TEXT_NODE);
                    int dataIndex=m_data.size();
                    m_previous=addNode(m_coalescedTextType,exName,
                            m_parents.peek(),m_previous,dataIndex,false);
                    m_data.addElement(m_textPendingStart);
                    m_data.addElement(length);
                }
            }
            // Reset for next text block
            m_textPendingStart=-1;
            m_textType=m_coalescedTextType=DTM.TEXT_NODE;
        }
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of the EntityResolver interface.
    ////////////////////////////////////////////////////////////////////

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        if(DEBUG)
            System.out.println("startPrefixMapping: prefix: "+prefix+", uri: "
                    +uri);
        if(null==prefix)
            prefix="";
        m_prefixMappings.addElement(prefix);  // JDK 1.1.x compat -sc
        m_prefixMappings.addElement(uri);  // JDK 1.1.x compat -sc
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of DTDHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void endPrefixMapping(String prefix) throws SAXException{
        if(DEBUG)
            System.out.println("endPrefixMapping: prefix: "+prefix);
        if(null==prefix)
            prefix="";
        int index=m_contextIndexes.peek()-1;
        do{
            index=m_prefixMappings.indexOf(prefix,++index);
        }while((index>=0)&&((index&0x01)==0x01));
        if(index>-1){
            m_prefixMappings.setElementAt("%@$#^@#",index);
            m_prefixMappings.setElementAt("%@$#^@#",index+1);
        }
        // no op
    }

    public void startElement(
            String uri,String localName,String qName,Attributes attributes)
            throws SAXException{
        if(DEBUG){
            System.out.println("startElement: uri: "+uri+", localname: "
                    +localName+", qname: "+qName+", atts: "+attributes);
            boolean DEBUG_ATTRS=true;
            if(DEBUG_ATTRS&attributes!=null){
                int n=attributes.getLength();
                if(n==0)
                    System.out.println("\tempty attribute list");
                else for(int i=0;i<n;i++)
                    System.out.println("\t attr: uri: "+attributes.getURI(i)+
                            ", localname: "+attributes.getLocalName(i)+
                            ", qname: "+attributes.getQName(i)+
                            ", type: "+attributes.getType(i)+
                            ", value: "+attributes.getValue(i)
                    );
            }
        }
        charactersFlush();
        int exName=m_expandedNameTable.getExpandedTypeID(uri,localName,DTM.ELEMENT_NODE);
        String prefix=getPrefix(qName,uri);
        int prefixIndex=(null!=prefix)
                ?m_valuesOrPrefixes.stringToIndex(qName):0;
        int elemNode=addNode(DTM.ELEMENT_NODE,exName,
                m_parents.peek(),m_previous,prefixIndex,true);
        if(m_indexing)
            indexNode(exName,elemNode);
        m_parents.push(elemNode);
        int startDecls=m_contextIndexes.peek();
        int nDecls=m_prefixMappings.size();
        int prev=DTM.NULL;
        if(!m_pastFirstElement){
            // SPECIAL CASE: Implied declaration at root element
            prefix="xml";
            String declURL="http://www.w3.org/XML/1998/namespace";
            exName=m_expandedNameTable.getExpandedTypeID(null,prefix,DTM.NAMESPACE_NODE);
            int val=m_valuesOrPrefixes.stringToIndex(declURL);
            prev=addNode(DTM.NAMESPACE_NODE,exName,elemNode,
                    prev,val,false);
            m_pastFirstElement=true;
        }
        for(int i=startDecls;i<nDecls;i+=2){
            prefix=(String)m_prefixMappings.elementAt(i);
            if(prefix==null)
                continue;
            String declURL=(String)m_prefixMappings.elementAt(i+1);
            exName=m_expandedNameTable.getExpandedTypeID(null,prefix,DTM.NAMESPACE_NODE);
            int val=m_valuesOrPrefixes.stringToIndex(declURL);
            prev=addNode(DTM.NAMESPACE_NODE,exName,elemNode,
                    prev,val,false);
        }
        int n=attributes.getLength();
        for(int i=0;i<n;i++){
            String attrUri=attributes.getURI(i);
            String attrQName=attributes.getQName(i);
            String valString=attributes.getValue(i);
            prefix=getPrefix(attrQName,attrUri);
            int nodeType;
            String attrLocalName=attributes.getLocalName(i);
            if((null!=attrQName)
                    &&(attrQName.equals("xmlns")
                    ||attrQName.startsWith("xmlns:"))){
                if(declAlreadyDeclared(prefix))
                    continue;  // go to the next attribute.
                nodeType=DTM.NAMESPACE_NODE;
            }else{
                nodeType=DTM.ATTRIBUTE_NODE;
                if(attributes.getType(i).equalsIgnoreCase("ID"))
                    setIDAttribute(valString,elemNode);
            }
            // Bit of a hack... if somehow valString is null, stringToIndex will
            // return -1, which will make things very unhappy.
            if(null==valString)
                valString="";
            int val=m_valuesOrPrefixes.stringToIndex(valString);
            //String attrLocalName = attributes.getLocalName(i);
            if(null!=prefix){
                prefixIndex=m_valuesOrPrefixes.stringToIndex(attrQName);
                int dataIndex=m_data.size();
                m_data.addElement(prefixIndex);
                m_data.addElement(val);
                val=-dataIndex;
            }
            exName=m_expandedNameTable.getExpandedTypeID(attrUri,attrLocalName,nodeType);
            prev=addNode(nodeType,exName,elemNode,prev,val,
                    false);
        }
        if(DTM.NULL!=prev)
            m_nextsib.setElementAt(DTM.NULL,prev);
        if(null!=m_wsfilter){
            short wsv=m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode),this);
            boolean shouldStrip=(DTMWSFilter.INHERIT==wsv)
                    ?getShouldStripWhitespace()
                    :(DTMWSFilter.STRIP==wsv);
            pushShouldStripWhitespace(shouldStrip);
        }
        m_previous=DTM.NULL;
        m_contextIndexes.push(m_prefixMappings.size());  // for the children.
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of ContentHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void endElement(String uri,String localName,String qName)
            throws SAXException{
        if(DEBUG)
            System.out.println("endElement: uri: "+uri+", localname: "
                    +localName+", qname: "+qName);
        charactersFlush();
        // If no one noticed, startPrefixMapping is a drag.
        // Pop the context for the last child (the one pushed by startElement)
        m_contextIndexes.quickPop(1);
        // Do it again for this one (the one pushed by the last endElement).
        int topContextIndex=m_contextIndexes.peek();
        if(topContextIndex!=m_prefixMappings.size()){
            m_prefixMappings.setSize(topContextIndex);
        }
        int lastNode=m_previous;
        m_previous=m_parents.pop();
        // If lastNode is still DTM.NULL, this element had no children
        if(DTM.NULL==lastNode)
            m_firstch.setElementAt(DTM.NULL,m_previous);
        else
            m_nextsib.setElementAt(DTM.NULL,lastNode);
        popShouldStripWhitespace();
    }

    public void characters(char ch[],int start,int length) throws SAXException{
        if(m_textPendingStart==-1)  // First one in this block
        {
            m_textPendingStart=m_chars.size();
            m_coalescedTextType=m_textType;
        }
        // Type logic: If all adjacent text is CDATASections, the
        // concatentated text is treated as a single CDATASection (see
        // initialization above).  If any were ordinary Text, the whole
        // thing is treated as Text. This may be worth %REVIEW%ing.
        else if(m_textType==DTM.TEXT_NODE){
            m_coalescedTextType=DTM.TEXT_NODE;
        }
        m_chars.append(ch,start,length);
    }

    public void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException{
        // %OPT% We can probably take advantage of the fact that we know this
        // is whitespace.
        characters(ch,start,length);
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        if(DEBUG)
            System.out.println("processingInstruction: target: "+target+", data: "+data);
        charactersFlush();
        int exName=m_expandedNameTable.getExpandedTypeID(null,target,
                DTM.PROCESSING_INSTRUCTION_NODE);
        int dataIndex=m_valuesOrPrefixes.stringToIndex(data);
        m_previous=addNode(DTM.PROCESSING_INSTRUCTION_NODE,exName,
                m_parents.peek(),m_previous,
                dataIndex,false);
    }

    public void skippedEntity(String name) throws SAXException{
        // %REVIEW% What should be done here?
        // no op
    }

    protected boolean declAlreadyDeclared(String prefix){
        int startDecls=m_contextIndexes.peek();
        Vector prefixMappings=m_prefixMappings;
        int nDecls=prefixMappings.size();
        for(int i=startDecls;i<nDecls;i+=2){
            String prefixDecl=(String)prefixMappings.elementAt(i);
            if(prefixDecl==null)
                continue;
            if(prefixDecl.equals(prefix))
                return true;
        }
        return false;
    }

    public void warning(SAXParseException e) throws SAXException{
        // %REVIEW% Is there anyway to get the JAXP error listener here?
        System.err.println(e.getMessage());
    }

    public void error(SAXParseException e) throws SAXException{
        throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException{
        throw e;
    }

    public void elementDecl(String name,String model) throws SAXException{
        // no op
    }

    public void attributeDecl(
            String eName,String aName,String type,String valueDefault,String value)
            throws SAXException{
        // no op
    }

    public void internalEntityDecl(String name,String value)
            throws SAXException{
        // no op
    }

    public void externalEntityDecl(
            String name,String publicId,String systemId) throws SAXException{
        // no op
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of the ErrorHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void startDTD(String name,String publicId,String systemId)
            throws SAXException{
        m_insideDTD=true;
    }

    public void endDTD() throws SAXException{
        m_insideDTD=false;
    }

    public void startEntity(String name) throws SAXException{
        // no op
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of the DeclHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void endEntity(String name) throws SAXException{
        // no op
    }

    public void startCDATA() throws SAXException{
        m_textType=DTM.CDATA_SECTION_NODE;
    }

    public void endCDATA() throws SAXException{
        m_textType=DTM.TEXT_NODE;
    }

    public void comment(char ch[],int start,int length) throws SAXException{
        if(m_insideDTD)      // ignore comments if we're inside the DTD
            return;
        charactersFlush();
        int exName=m_expandedNameTable.getExpandedTypeID(DTM.COMMENT_NODE);
        // For now, treat comments as strings...  I guess we should do a
        // seperate FSB buffer instead.
        int dataIndex=m_valuesOrPrefixes.stringToIndex(new String(ch,start,
                length));
        m_previous=addNode(DTM.COMMENT_NODE,exName,
                m_parents.peek(),m_previous,dataIndex,false);
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of the LexicalHandler interface.
    ////////////////////////////////////////////////////////////////////

    public void setProperty(String property,Object value){
    }

    public boolean needsTwoThreads(){
        return null!=m_incrementalSAXSource;
    }

    public ContentHandler getContentHandler(){
        if(m_incrementalSAXSource.getClass()
                .getName().equals("com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Filter"))
            return (ContentHandler)m_incrementalSAXSource;
        else
            return this;
    }

    public LexicalHandler getLexicalHandler(){
        if(m_incrementalSAXSource.getClass()
                .getName().equals("com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Filter"))
            return (LexicalHandler)m_incrementalSAXSource;
        else
            return this;
    }

    public EntityResolver getEntityResolver(){
        return this;
    }

    public DTDHandler getDTDHandler(){
        return this;
    }

    public ErrorHandler getErrorHandler(){
        return this;
    }

    public DeclHandler getDeclHandler(){
        return this;
    }

    public SourceLocator getSourceLocatorFor(int node){
        if(m_useSourceLocationProperty){
            node=makeNodeIdentity(node);
            return new NodeLocator(null,
                    m_sourceSystemId.elementAt(node),
                    m_sourceLine.elementAt(node),
                    m_sourceColumn.elementAt(node));
        }else if(m_locator!=null){
            return new NodeLocator(null,m_locator.getSystemId(),-1,-1);
        }else if(m_systemId!=null){
            return new NodeLocator(null,m_systemId,-1,-1);
        }
        return null;
    }

    public String getFixedNames(int type){
        return m_fixednames[type];
    }
}
