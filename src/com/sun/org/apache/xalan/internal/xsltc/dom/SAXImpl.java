/**
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xml.internal.dtm.*;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIterNodeList;
import com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import com.sun.org.apache.xml.internal.dtm.ref.EmptyIterator;
import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import com.sun.org.apache.xml.internal.serializer.ToXMLSAXHandler;
import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.util.HashMap;
import java.util.Map;

public final class SAXImpl extends SAX2DTM2
        implements DOMEnhancedForDTM, DOMBuilder{
    /** ------------------------------------------------------------------- */
    // private static final String XML_STRING = "xml:";
    private static final String XML_PREFIX="xml";
    private static final String XMLSPACE_STRING="xml:space";
    private static final String PRESERVE_STRING="preserve";
    // private static final String XMLNS_PREFIX = "xmlns";
    private static final String XML_URI="http://www.w3.org/XML/1998/namespace";
    /** DOMBuilder fields END                                               */
    // empty String for null attribute values
    private final static String EMPTYSTRING="";
    // empty iterator to be returned when there are no children
    private final static DTMAxisIterator EMPTYITERATOR=EmptyIterator.getInstance();
    // The URI to this document
    // private String _documentURI = null;
    static private int _documentURIIndex=0;
    /** DOMBuilder fields BEGIN                                             */
    // Namespace prefix-to-uri mapping stuff
    private int _uriCount=0;
    // private int       _prefixCount  = 0;
    // Stack used to keep track of what whitespace text nodes are protected
    // by xml:space="preserve" attributes and which nodes that are not.
    private int[] _xmlSpaceStack;
    private int _idx=1;
    private boolean _preserve=false;
    /** ------------------------------------------------------------------- */
    private boolean _escaping=true;
    private boolean _disableEscaping=false;
    private int _textNodeToProcess=DTM.NULL;
    // The number of expanded names
    private int _namesSize=-1;
    // Namespace related stuff
    private Map<Integer,Integer> _nsIndex=new HashMap<>();
    // The initial size of the text buffer
    private int _size=0;
    // Tracks which textnodes are not escaped
    private BitArray _dontEscape=null;
    // The owner Document when the input source is DOMSource.
    private Document _document;
    // The Map for org.w3c.dom.Node to node id mapping.
    // This is only used when the input is a DOMSource and the
    // buildIdIndex flag is true.
    private Map<Node,Integer> _node2Ids=null;
    // True if the input source is a DOMSource.
    private boolean _hasDOMSource=false;
    // The DTMManager
    private XSLTCDTMManager _dtmManager;
    // Support for access/navigation through org.w3c.dom API
    private Node[] _nodes;
    private NodeList[] _nodeLists;
    // private final static String XML_LANG_ATTRIBUTE = "http://www.w3.org/XML/1998/namespace:@lang";

    public SAXImpl(XSLTCDTMManager mgr,Source source,
                   int dtmIdentity,DTMWSFilter whiteSpaceFilter,
                   XMLStringFactory xstringfactory,
                   boolean doIndexing,boolean buildIdIndex){
        this(mgr,source,dtmIdentity,whiteSpaceFilter,xstringfactory,
                doIndexing,DEFAULT_BLOCKSIZE,buildIdIndex,false);
    }

    public SAXImpl(XSLTCDTMManager mgr,Source source,
                   int dtmIdentity,DTMWSFilter whiteSpaceFilter,
                   XMLStringFactory xstringfactory,
                   boolean doIndexing,int blocksize,
                   boolean buildIdIndex,
                   boolean newNameTable){
        super(mgr,source,dtmIdentity,whiteSpaceFilter,xstringfactory,
                doIndexing,blocksize,false,buildIdIndex,newNameTable);
        _dtmManager=mgr;
        _size=blocksize;
        // Use a smaller size for the space stack if the blocksize is small
        _xmlSpaceStack=new int[blocksize<=64?4:64];
        /** From DOMBuilder */
        _xmlSpaceStack[0]=DTMDefaultBase.ROOTNODE;
        // If the input source is DOMSource, set the _document field and
        // create the node2Ids table.
        if(source instanceof DOMSource){
            _hasDOMSource=true;
            DOMSource domsrc=(DOMSource)source;
            Node node=domsrc.getNode();
            if(node instanceof Document){
                _document=(Document)node;
            }else{
                _document=node.getOwnerDocument();
            }
            _node2Ids=new HashMap<>();
        }
    }

    public DTMAxisIterator getIterator(){
        return new SingletonIterator(getDocument(),true);
    }

    public DTMAxisIterator getChildren(final int node){
        return (new ChildrenIterator()).setStartNode(node);
    }

    public DTMAxisIterator getTypedChildren(final int type){
        return (new TypedChildrenIterator(type));
    }

    public DTMAxisIterator getNthDescendant(int type,int n,boolean includeself){
        return new NthDescendantIterator(n);
    }

    public DTMAxisIterator getNamespaceAxisIterator(int axis,int ns){
        if(ns==NO_TYPE){
            return EMPTYITERATOR;
        }else{
            switch(axis){
                case Axis.CHILD:
                    return new NamespaceChildrenIterator(ns);
                case Axis.ATTRIBUTE:
                    return new NamespaceAttributeIterator(ns);
                default:
                    return new NamespaceWildcardIterator(axis,ns);
            }
        }
    }

    public DTMAxisIterator getNodeValueIterator(DTMAxisIterator iterator,int type,
                                                String value,boolean op){
        return (DTMAxisIterator)(new NodeValueIterator(iterator,type,value,op));
    }

    public DTMAxisIterator orderNodes(DTMAxisIterator source,int node){
        return new DupFilterIterator(source);
    }

    public String getNamespaceName(final int node){
        if(node==DTM.NULL){
            return "";
        }
        String s;
        return (s=getNamespaceURI(node))==null?EMPTYSTRING:s;
    }

    public int getAttributeNode(final int type,final int element){
        for(int attr=getFirstAttribute(element);
            attr!=DTM.NULL;
            attr=getNextAttribute(attr)){
            if(getExpandedTypeID(attr)==type) return attr;
        }
        return DTM.NULL;
    }

    public void copy(final int node,SerializationHandler handler)
            throws TransletException{
        copy(node,handler,false);
    }

    public void copy(DTMAxisIterator nodes,SerializationHandler handler)
            throws TransletException{
        int node;
        while((node=nodes.next())!=DTM.NULL){
            copy(node,handler);
        }
    }

    public String shallowCopy(final int node,SerializationHandler handler)
            throws TransletException{
        int nodeID=makeNodeIdentity(node);
        int exptype=_exptype2(nodeID);
        int type=_exptype2Type(exptype);
        try{
            switch(type){
                case DTM.ELEMENT_NODE:
                    final String name=copyElement(nodeID,exptype,handler);
                    copyNS(nodeID,handler,true);
                    return name;
                case DTM.ROOT_NODE:
                case DTM.DOCUMENT_NODE:
                    return EMPTYSTRING;
                case DTM.TEXT_NODE:
                    copyTextNode(nodeID,handler);
                    return null;
                case DTM.PROCESSING_INSTRUCTION_NODE:
                    copyPI(node,handler);
                    return null;
                case DTM.COMMENT_NODE:
                    handler.comment(getStringValueX(node));
                    return null;
                case DTM.NAMESPACE_NODE:
                    handler.namespaceAfterStartElement(getNodeNameX(node),getNodeValue(node));
                    return null;
                case DTM.ATTRIBUTE_NODE:
                    copyAttribute(nodeID,exptype,handler);
                    return null;
                default:
                    final String uri1=getNamespaceName(node);
                    if(uri1.length()!=0){
                        final String prefix=getPrefix(node);
                        handler.namespaceAfterStartElement(prefix,uri1);
                    }
                    handler.addAttribute(getNodeName(node),getNodeValue(node));
                    return null;
            }
        }catch(Exception e){
            throw new TransletException(e);
        }
    }

    public boolean lessThan(int node1,int node2){
        if(node1==DTM.NULL){
            return false;
        }
        if(node2==DTM.NULL){
            return true;
        }
        return (node1<node2);
    }

    public void characters(final int node,SerializationHandler handler)
            throws TransletException{
        if(node!=DTM.NULL){
            try{
                dispatchCharactersEvents(node,handler,false);
            }catch(SAXException e){
                throw new TransletException(e);
            }
        }
    }

    public Node makeNode(int index){
        if(_nodes==null){
            _nodes=new Node[_namesSize];
        }
        int nodeID=makeNodeIdentity(index);
        if(nodeID<0){
            return null;
        }else if(nodeID<_nodes.length){
            return (_nodes[nodeID]!=null)?_nodes[nodeID]
                    :(_nodes[nodeID]=new DTMNodeProxy((DTM)this,index));
        }else{
            return new DTMNodeProxy((DTM)this,index);
        }
    }

    public Node makeNode(DTMAxisIterator iter){
        return makeNode(iter.next());
    }

    public NodeList makeNodeList(int index){
        if(_nodeLists==null){
            _nodeLists=new NodeList[_namesSize];
        }
        int nodeID=makeNodeIdentity(index);
        if(nodeID<0){
            return null;
        }else if(nodeID<_nodeLists.length){
            return (_nodeLists[nodeID]!=null)?_nodeLists[nodeID]
                    :(_nodeLists[nodeID]=new DTMAxisIterNodeList(this,
                    new SingletonIterator(index)));
        }else{
            return new DTMAxisIterNodeList(this,new SingletonIterator(index));
        }
    }

    public NodeList makeNodeList(DTMAxisIterator iter){
        return new DTMAxisIterNodeList(this,iter);
    }

    public String getLanguage(int node){
        int parent=node;
        while(DTM.NULL!=parent){
            if(DTM.ELEMENT_NODE==getNodeType(parent)){
                int langAttr=getAttributeNode(parent,"http://www.w3.org/XML/1998/namespace","lang");
                if(DTM.NULL!=langAttr){
                    return getNodeValue(langAttr);
                }
            }
            parent=getParent(parent);
        }
        return (null);
    }

    public int getSize(){
        return getNumberOfNodes();
    }

    public String getDocumentURI(int node){
        return getDocumentURI();
    }

    public void setFilter(StripFilter filter){
    }

    public void setupMapping(String[] names,String[] urisArray,
                             int[] typesArray,String[] namespaces){
        // This method only has a function in DOM adapters
    }

    public boolean isElement(final int node){
        return getNodeType(node)==DTM.ELEMENT_NODE;
    }

    public boolean isAttribute(final int node){
        return getNodeType(node)==DTM.ATTRIBUTE_NODE;
    }

    public String lookupNamespace(int node,String prefix)
            throws TransletException{
        int anode, nsnode;
        final AncestorIterator ancestors=new AncestorIterator();
        if(isElement(node)){
            ancestors.includeSelf();
        }
        ancestors.setStartNode(node);
        while((anode=ancestors.next())!=DTM.NULL){
            final NamespaceIterator namespaces=new NamespaceIterator();
            namespaces.setStartNode(anode);
            while((nsnode=namespaces.next())!=DTM.NULL){
                if(getLocalName(nsnode).equals(prefix)){
                    return getNodeValue(nsnode);
                }
            }
        }
        BasisLibrary.runTimeError(BasisLibrary.NAMESPACE_PREFIX_ERR,prefix);
        return null;
    }

    public DOM getResultTreeFrag(int initSize,int rtfType){
        return getResultTreeFrag(initSize,rtfType,true);
    }

    public DOM getResultTreeFrag(int initSize,int rtfType,boolean addToManager){
        if(rtfType==DOM.SIMPLE_RTF){
            if(addToManager){
                int dtmPos=_dtmManager.getFirstFreeDTMID();
                SimpleResultTreeImpl rtf=new SimpleResultTreeImpl(_dtmManager,
                        dtmPos<<DTMManager.IDENT_DTM_NODE_BITS);
                _dtmManager.addDTM(rtf,dtmPos,0);
                return rtf;
            }else{
                return new SimpleResultTreeImpl(_dtmManager,0);
            }
        }else if(rtfType==DOM.ADAPTIVE_RTF){
            if(addToManager){
                int dtmPos=_dtmManager.getFirstFreeDTMID();
                AdaptiveResultTreeImpl rtf=new AdaptiveResultTreeImpl(_dtmManager,
                        dtmPos<<DTMManager.IDENT_DTM_NODE_BITS,
                        m_wsfilter,initSize,m_buildIdIndex);
                _dtmManager.addDTM(rtf,dtmPos,0);
                return rtf;
            }else{
                return new AdaptiveResultTreeImpl(_dtmManager,0,
                        m_wsfilter,initSize,m_buildIdIndex);
            }
        }else{
            return (DOM)_dtmManager.getDTM(null,true,m_wsfilter,
                    true,false,false,
                    initSize,m_buildIdIndex);
        }
    }

    public SerializationHandler getOutputDomBuilder(){
        return new ToXMLSAXHandler(this,"UTF-8");
    }

    public int getNSType(int node){
        String s=getNamespaceURI(node);
        if(s==null){
            return 0;
        }
        int eType=getIdForNamespace(s);
        return _nsIndex.get(new Integer(eType));
    }

    public Map<String,Integer> getElementsWithIDs(){
        return m_idAttributes;
    }
    /**---------------------------------------------------------------------------*/

    private void copyPI(final int node,SerializationHandler handler)
            throws TransletException{
        final String target=getNodeName(node);
        final String value=getStringValueX(node);
        try{
            handler.processingInstruction(target,value);
        }catch(Exception e){
            throw new TransletException(e);
        }
    }

    public int getNamespaceType(final int node){
        return super.getNamespaceType(node);
    }

    protected boolean getShouldStripWhitespace(){
        return _preserve?false:super.getShouldStripWhitespace();
    }

    public short[] getMapping(String[] names,String[] uris,int[] types){
        // Delegate the work to getMapping2 if the document is not fully built.
        // Some of the processing has to be different in this case.
        if(_namesSize<0){
            return getMapping2(names,uris,types);
        }
        int i;
        final int namesLength=names.length;
        final int exLength=m_expandedNameTable.getSize();
        final short[] result=new short[exLength];
        // primitive types map to themselves
        for(i=0;i<DTM.NTYPES;i++){
            result[i]=(short)i;
        }
        for(i=NTYPES;i<exLength;i++){
            result[i]=m_expandedNameTable.getType(i);
        }
        // actual mapping of caller requested names
        for(i=0;i<namesLength;i++){
            int genType=m_expandedNameTable.getExpandedTypeID(uris[i],
                    names[i],
                    types[i],
                    true);
            if(genType>=0&&genType<exLength){
                result[genType]=(short)(i+DTM.NTYPES);
            }
        }
        return result;
    }

    public int[] getReverseMapping(String[] names,String[] uris,int[] types){
        int i;
        final int[] result=new int[names.length+DTM.NTYPES];
        // primitive types map to themselves
        for(i=0;i<DTM.NTYPES;i++){
            result[i]=i;
        }
        // caller's types map into appropriate dom types
        for(i=0;i<names.length;i++){
            int type=m_expandedNameTable.getExpandedTypeID(uris[i],names[i],types[i],true);
            result[i+DTM.NTYPES]=type;
        }
        return (result);
    }

    public short[] getNamespaceMapping(String[] namespaces){
        int i;
        final int nsLength=namespaces.length;
        final int mappingLength=_uriCount;
        final short[] result=new short[mappingLength];
        // Initialize all entries to -1
        for(i=0;i<mappingLength;i++){
            result[i]=(short)(-1);
        }
        for(i=0;i<nsLength;i++){
            int eType=getIdForNamespace(namespaces[i]);
            Integer type=_nsIndex.get(eType);
            if(type!=null){
                result[type]=(short)i;
            }
        }
        return (result);
    }

    public short[] getReverseNamespaceMapping(String[] namespaces){
        int i;
        final int length=namespaces.length;
        final short[] result=new short[length];
        for(i=0;i<length;i++){
            int eType=getIdForNamespace(namespaces[i]);
            Integer type=_nsIndex.get(eType);
            result[i]=(type==null)?-1:type.shortValue();
        }
        return result;
    }

    public String getDocumentURI(){
        String baseURI=getDocumentBaseURI();
        return (baseURI!=null)?baseURI:"rtf"+_documentURIIndex++;
    }

    public void setDocumentURI(String uri){
        if(uri!=null){
            setDocumentBaseURI(SystemIDResolver.getAbsoluteURI(uri));
        }
    }

    public boolean hasDOMSource(){
        return _hasDOMSource;
    }

    private short[] getMapping2(String[] names,String[] uris,int[] types){
        int i;
        final int namesLength=names.length;
        final int exLength=m_expandedNameTable.getSize();
        int[] generalizedTypes=null;
        if(namesLength>0){
            generalizedTypes=new int[namesLength];
        }
        int resultLength=exLength;
        for(i=0;i<namesLength;i++){
            // When the document is not fully built, the searchOnly
            // flag should be set to false. That means we should add
            // the type if it is not already in the expanded name table.
            //generalizedTypes[i] = getGeneralizedType(names[i], false);
            generalizedTypes[i]=
                    m_expandedNameTable.getExpandedTypeID(uris[i],
                            names[i],
                            types[i],
                            false);
            if(_namesSize<0&&generalizedTypes[i]>=resultLength){
                resultLength=generalizedTypes[i]+1;
            }
        }
        final short[] result=new short[resultLength];
        // primitive types map to themselves
        for(i=0;i<DTM.NTYPES;i++){
            result[i]=(short)i;
        }
        for(i=NTYPES;i<exLength;i++){
            result[i]=m_expandedNameTable.getType(i);
        }
        // actual mapping of caller requested names
        for(i=0;i<namesLength;i++){
            int genType=generalizedTypes[i];
            if(genType>=0&&genType<resultLength){
                result[genType]=(short)(i+DTM.NTYPES);
            }
        }
        return (result);
    }

    public void migrateTo(DTMManager manager){
        super.migrateTo(manager);
        if(manager instanceof XSLTCDTMManager){
            _dtmManager=(XSLTCDTMManager)manager;
        }
    }

    public String getUnparsedEntityURI(String name){
        // Special handling for DOM input
        if(_document!=null){
            String uri="";
            DocumentType doctype=_document.getDoctype();
            if(doctype!=null){
                NamedNodeMap entities=doctype.getEntities();
                if(entities==null){
                    return uri;
                }
                Entity entity=(Entity)entities.getNamedItem(name);
                if(entity==null){
                    return uri;
                }
                String notationName=entity.getNotationName();
                if(notationName!=null){
                    uri=entity.getSystemId();
                    if(uri==null){
                        uri=entity.getPublicId();
                    }
                }
            }
            return uri;
        }else{
            return super.getUnparsedEntityURI(name);
        }
    }

    public int getElementById(String idString){
        Node node=_document.getElementById(idString);
        if(node!=null){
            Integer id=_node2Ids.get(node);
            return (id!=null)?id:DTM.NULL;
        }else{
            return DTM.NULL;
        }
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        super.startPrefixMapping(prefix,uri);
        handleTextEscaping();
        definePrefixAndUri(prefix,uri);
    }

    /****************************************************************/
    public void characters(char[] ch,int start,int length) throws SAXException{
        super.characters(ch,start,length);
        _disableEscaping=!_escaping;
        _textNodeToProcess=getNumberOfNodes();
    }
    /**---------------------------------------------------------------------------*/

    public void ignorableWhitespace(char[] ch,int start,int length)
            throws SAXException{
        super.ignorableWhitespace(ch,start,length);
        _textNodeToProcess=getNumberOfNodes();
    }

    public void startElement(String uri,String localName,
                             String qname,Attributes attributes,
                             Node node)
            throws SAXException{
        this.startElement(uri,localName,qname,attributes);
        if(m_buildIdIndex){
            _node2Ids.put(node,new Integer(m_parents.peek()));
        }
    }

    public void startElement(String uri,String localName,
                             String qname,Attributes attributes)
            throws SAXException{
        super.startElement(uri,localName,qname,attributes);
        handleTextEscaping();
        if(m_wsfilter!=null){
            // Look for any xml:space attributes
            // Depending on the implementation of attributes, this
            // might be faster than looping through all attributes. ILENE
            final int index=attributes.getIndex(XMLSPACE_STRING);
            if(index>=0){
                xmlSpaceDefine(attributes.getValue(index),m_parents.peek());
            }
        }
    }

    /** DOMBuilder methods begin                                                  */
    private void xmlSpaceDefine(String val,final int node){
        final boolean setting=val.equals(PRESERVE_STRING);
        if(setting!=_preserve){
            _xmlSpaceStack[_idx++]=node;
            _preserve=setting;
        }
    }

    public void endElement(String namespaceURI,String localName,String qname)
            throws SAXException{
        super.endElement(namespaceURI,localName,qname);
        handleTextEscaping();
        // Revert to strip/preserve-space setting from before this element
        if(m_wsfilter!=null){
            xmlSpaceRevert(m_previous);
        }
    }

    private void xmlSpaceRevert(final int node){
        if(node==_xmlSpaceStack[_idx-1]){
            _idx--;
            _preserve=!_preserve;
        }
    }

    public void comment(char[] ch,int start,int length)
            throws SAXException{
        super.comment(ch,start,length);
        handleTextEscaping();
    }

    public void startDocument() throws SAXException{
        super.startDocument();
        _nsIndex.put(0,_uriCount++);
        definePrefixAndUri(XML_PREFIX,XML_URI);
    }

    public void endDocument() throws SAXException{
        super.endDocument();
        handleTextEscaping();
        _namesSize=m_expandedNameTable.getSize();
    }

    private void handleTextEscaping(){
        if(_disableEscaping&&_textNodeToProcess!=DTM.NULL
                &&_type(_textNodeToProcess)==DTM.TEXT_NODE){
            if(_dontEscape==null){
                _dontEscape=new BitArray(_size);
            }
            // Resize the _dontEscape BitArray if necessary.
            if(_textNodeToProcess>=_dontEscape.size()){
                _dontEscape.resize(_dontEscape.size()*2);
            }
            _dontEscape.setBit(_textNodeToProcess);
            _disableEscaping=false;
        }
        _textNodeToProcess=DTM.NULL;
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        super.processingInstruction(target,data);
        handleTextEscaping();
    }

    public String getNodeName(final int node){
        // Get the node type and make sure that it is within limits
        int nodeh=node;
        final short type=getNodeType(nodeh);
        switch(type){
            case DTM.ROOT_NODE:
            case DTM.DOCUMENT_NODE:
            case DTM.TEXT_NODE:
            case DTM.COMMENT_NODE:
                return EMPTYSTRING;
            case DTM.NAMESPACE_NODE:
                return this.getLocalName(nodeh);
            default:
                return super.getNodeName(nodeh);
        }
    }

    private void definePrefixAndUri(String prefix,String uri)
            throws SAXException{
        // Check if the URI already exists before pushing on stack
        Integer eType=new Integer(getIdForNamespace(uri));
        if(_nsIndex.get(eType)==null){
            _nsIndex.put(eType,_uriCount++);
        }
    }

    public boolean setEscaping(boolean value){
        final boolean temp=_escaping;
        _escaping=value;
        return temp;
    }

    /** DOMBuilder methods end                                                    */
    public void print(int node,int level){
        switch(getNodeType(node)){
            case DTM.ROOT_NODE:
            case DTM.DOCUMENT_NODE:
                print(getFirstChild(node),level);
                break;
            case DTM.TEXT_NODE:
            case DTM.COMMENT_NODE:
            case DTM.PROCESSING_INSTRUCTION_NODE:
                System.out.print(getStringValueX(node));
                break;
            default:
                final String name=getNodeName(node);
                System.out.print("<"+name);
                for(int a=getFirstAttribute(node);a!=DTM.NULL;a=getNextAttribute(a)){
                    System.out.print("\n"+getNodeName(a)+"=\""+getStringValueX(a)+"\"");
                }
                System.out.print('>');
                for(int child=getFirstChild(node);child!=DTM.NULL;
                    child=getNextSibling(child)){
                    print(child,level+1);
                }
                System.out.println("</"+name+'>');
                break;
        }
    }

    public String getAttributeValue(final String name,final int element){
        return getAttributeValue(getGeneralizedType(name),element);
    }

    public int getGeneralizedType(final String name){
        return getGeneralizedType(name,true);
    }

    public int getGeneralizedType(final String name,boolean searchOnly){
        String lName, ns=null;
        int index=-1;
        int code;
        // Is there a prefix?
        if((index=name.lastIndexOf(":"))>-1){
            ns=name.substring(0,index);
        }
        // Local part of name is after colon.  lastIndexOf returns -1 if
        // there is no colon, so lNameStartIdx will be zero in that case.
        int lNameStartIdx=index+1;
        // Distinguish attribute and element names.  Attribute has @ before
        // local part of name.
        if(name.charAt(lNameStartIdx)=='@'){
            code=DTM.ATTRIBUTE_NODE;
            lNameStartIdx++;
        }else{
            code=DTM.ELEMENT_NODE;
        }
        // Extract local name
        lName=(lNameStartIdx==0)?name:name.substring(lNameStartIdx);
        return m_expandedNameTable.getExpandedTypeID(ns,lName,code,searchOnly);
    }

    public String getAttributeValue(final int type,final int element){
        final int attr=getAttributeNode(type,element);
        return (attr!=DTM.NULL)?getStringValueX(attr):EMPTYSTRING;
    }

    public DTMAxisIterator getTypedAxisIterator(int axis,int type){
        // Most common case handled first
        if(axis==Axis.CHILD){
            return new TypedChildrenIterator(type);
        }
        if(type==NO_TYPE){
            return (EMPTYITERATOR);
        }
        switch(axis){
            case Axis.SELF:
                return new TypedSingletonIterator(type);
            case Axis.CHILD:
                return new TypedChildrenIterator(type);
            case Axis.PARENT:
                return new ParentIterator().setNodeType(type);
            case Axis.ANCESTOR:
                return new TypedAncestorIterator(type);
            case Axis.ANCESTORORSELF:
                return (new TypedAncestorIterator(type)).includeSelf();
            case Axis.ATTRIBUTE:
                return new TypedAttributeIterator(type);
            case Axis.DESCENDANT:
                return new TypedDescendantIterator(type);
            case Axis.DESCENDANTORSELF:
                return (new TypedDescendantIterator(type)).includeSelf();
            case Axis.FOLLOWING:
                return new TypedFollowingIterator(type);
            case Axis.PRECEDING:
                return new TypedPrecedingIterator(type);
            case Axis.FOLLOWINGSIBLING:
                return new TypedFollowingSiblingIterator(type);
            case Axis.PRECEDINGSIBLING:
                return new TypedPrecedingSiblingIterator(type);
            case Axis.NAMESPACE:
                return new TypedNamespaceIterator(type);
            case Axis.ROOT:
                return new TypedRootIterator(type);
            default:
                BasisLibrary.runTimeError(BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
                        Axis.getNames(axis));
        }
        return null;
    }

    public DTMAxisIterator getAxisIterator(final int axis){
        switch(axis){
            case Axis.SELF:
                return new SingletonIterator();
            case Axis.CHILD:
                return new ChildrenIterator();
            case Axis.PARENT:
                return new ParentIterator();
            case Axis.ANCESTOR:
                return new AncestorIterator();
            case Axis.ANCESTORORSELF:
                return (new AncestorIterator()).includeSelf();
            case Axis.ATTRIBUTE:
                return new AttributeIterator();
            case Axis.DESCENDANT:
                return new DescendantIterator();
            case Axis.DESCENDANTORSELF:
                return (new DescendantIterator()).includeSelf();
            case Axis.FOLLOWING:
                return new FollowingIterator();
            case Axis.PRECEDING:
                return new PrecedingIterator();
            case Axis.FOLLOWINGSIBLING:
                return new FollowingSiblingIterator();
            case Axis.PRECEDINGSIBLING:
                return new PrecedingSiblingIterator();
            case Axis.NAMESPACE:
                return new NamespaceIterator();
            case Axis.ROOT:
                return new RootIterator();
            default:
                BasisLibrary.runTimeError(BasisLibrary.AXIS_SUPPORT_ERR,
                        Axis.getNames(axis));
        }
        return null;
    }

    public DTMAxisIterator getTypedDescendantIterator(int type){
        return new TypedDescendantIterator(type);
    }

    public void copy(SerializationHandler handler) throws TransletException{
        copy(getDocument(),handler);
    }

    private final void copy(final int node,SerializationHandler handler,boolean isChild)
            throws TransletException{
        int nodeID=makeNodeIdentity(node);
        int eType=_exptype2(nodeID);
        int type=_exptype2Type(eType);
        try{
            switch(type){
                case DTM.ROOT_NODE:
                case DTM.DOCUMENT_NODE:
                    for(int c=_firstch2(nodeID);c!=DTM.NULL;c=_nextsib2(c)){
                        copy(makeNodeHandle(c),handler,true);
                    }
                    break;
                case DTM.PROCESSING_INSTRUCTION_NODE:
                    copyPI(node,handler);
                    break;
                case DTM.COMMENT_NODE:
                    handler.comment(getStringValueX(node));
                    break;
                case DTM.TEXT_NODE:
                    boolean oldEscapeSetting=false;
                    boolean escapeBit=false;
                    if(_dontEscape!=null){
                        escapeBit=_dontEscape.getBit(getNodeIdent(node));
                        if(escapeBit){
                            oldEscapeSetting=handler.setEscaping(false);
                        }
                    }
                    copyTextNode(nodeID,handler);
                    if(escapeBit){
                        handler.setEscaping(oldEscapeSetting);
                    }
                    break;
                case DTM.ATTRIBUTE_NODE:
                    copyAttribute(nodeID,eType,handler);
                    break;
                case DTM.NAMESPACE_NODE:
                    handler.namespaceAfterStartElement(getNodeNameX(node),getNodeValue(node));
                    break;
                default:
                    if(type==DTM.ELEMENT_NODE){
                        // Start element definition
                        final String name=copyElement(nodeID,eType,handler);
                        //if(isChild) => not to copy any namespaces  from parents
                        // else copy all namespaces in scope
                        copyNS(nodeID,handler,!isChild);
                        copyAttributes(nodeID,handler);
                        // Copy element children
                        for(int c=_firstch2(nodeID);c!=DTM.NULL;c=_nextsib2(c)){
                            copy(makeNodeHandle(c),handler,true);
                        }
                        // Close element definition
                        handler.endElement(name);
                    }
                    // Shallow copy of attribute to output handler
                    else{
                        final String uri=getNamespaceName(node);
                        if(uri.length()!=0){
                            final String prefix=getPrefix(node);
                            handler.namespaceAfterStartElement(prefix,uri);
                        }
                        handler.addAttribute(getNodeName(node),getNodeValue(node));
                    }
                    break;
            }
        }catch(Exception e){
            throw new TransletException(e);
        }
    }

    public DOMBuilder getBuilder(){
        return this;
    }

    public void release(){
        _dtmManager.release(this,true);
    }

    public class TypedNamespaceIterator extends NamespaceIterator{
        private String _nsPrefix;

        public TypedNamespaceIterator(int nodeType){
            super();
            if(m_expandedNameTable!=null){
                _nsPrefix=m_expandedNameTable.getLocalName(nodeType);
            }
        }

        public int next(){
            if((_nsPrefix==null)||(_nsPrefix.length()==0)){
                return (END);
            }
            int node=END;
            for(node=super.next();node!=END;node=super.next()){
                if(_nsPrefix.compareTo(getLocalName(node))==0){
                    return returnNode(node);
                }
            }
            return (END);
        }
    }  // end of TypedNamespaceIterator

    private final class NodeValueIterator extends InternalAxisIteratorBase{
        private final boolean _isReverse;
        private DTMAxisIterator _source;
        private String _value;
        private boolean _op;
        private int _returnType=RETURN_PARENT;

        public NodeValueIterator(DTMAxisIterator source,int returnType,
                                 String value,boolean op){
            _source=source;
            _returnType=returnType;
            _value=value;
            _op=op;
            _isReverse=source.isReverse();
        }

        public DTMAxisIterator reset(){
            _source.reset();
            return resetPosition();
        }

        public boolean isReverse(){
            return _isReverse;
        }        public DTMAxisIterator cloneIterator(){
            try{
                NodeValueIterator clone=(NodeValueIterator)super.clone();
                clone._isRestartable=false;
                clone._source=_source.cloneIterator();
                clone._value=_value;
                clone._op=_op;
                return clone.reset();
            }catch(CloneNotSupportedException e){
                BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                        e.toString());
                return null;
            }
        }

        public int next(){
            int node;
            while((node=_source.next())!=END){
                String val=getStringValueX(node);
                if(_value.equals(val)==_op){
                    if(_returnType==RETURN_CURRENT){
                        return returnNode(node);
                    }else{
                        return returnNode(getParent(node));
                    }
                }
            }
            return END;
        }        public void setRestartable(boolean isRestartable){
            _isRestartable=isRestartable;
            _source.setRestartable(isRestartable);
        }

        public void setMark(){
            _source.setMark();
        }



        public DTMAxisIterator setStartNode(int node){
            if(_isRestartable){
                _source.setStartNode(_startNode=node);
                return resetPosition();
            }
            return this;
        }



        public void gotoMark(){
            _source.gotoMark();
        }
    } // end NodeValueIterator

    public final class NamespaceWildcardIterator
            extends InternalAxisIteratorBase{
        protected int m_nsType;
        protected DTMAxisIterator m_baseIterator;

        public NamespaceWildcardIterator(int axis,int nsType){
            m_nsType=nsType;
            // Create a nested iterator that will select nodes of
            // the principal node kind for the selected axis.
            switch(axis){
                case Axis.ATTRIBUTE:{
                    // For "attribute::p:*", the principal node kind is
                    // attribute
                    m_baseIterator=getAxisIterator(axis);
                }
                case Axis.NAMESPACE:{
                    // This covers "namespace::p:*".  It is syntactically
                    // correct, though it doesn't make much sense.
                    m_baseIterator=getAxisIterator(axis);
                }
                default:{
                    // In all other cases, the principal node kind is
                    // element
                    m_baseIterator=getTypedAxisIterator(axis,
                            DTM.ELEMENT_NODE);
                }
            }
        }

        public DTMAxisIterator setStartNode(int node){
            if(_isRestartable){
                _startNode=node;
                m_baseIterator.setStartNode(node);
                resetPosition();
            }
            return this;
        }

        public int next(){
            int node;
            while((node=m_baseIterator.next())!=END){
                // Return only nodes that are in the selected namespace
                if(getNSType(node)==m_nsType){
                    return returnNode(node);
                }
            }
            return END;
        }

        public DTMAxisIterator cloneIterator(){
            try{
                DTMAxisIterator nestedClone=m_baseIterator.cloneIterator();
                NamespaceWildcardIterator clone=
                        (NamespaceWildcardIterator)super.clone();
                clone.m_baseIterator=nestedClone;
                clone.m_nsType=m_nsType;
                clone._isRestartable=false;
                return clone;
            }catch(CloneNotSupportedException e){
                BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                        e.toString());
                return null;
            }
        }

        public boolean isReverse(){
            return m_baseIterator.isReverse();
        }

        public void setMark(){
            m_baseIterator.setMark();
        }

        public void gotoMark(){
            m_baseIterator.gotoMark();
        }
    }

    public final class NamespaceChildrenIterator
            extends InternalAxisIteratorBase{
        private final int _nsType;

        public NamespaceChildrenIterator(final int type){
            _nsType=type;
        }

        public DTMAxisIterator setStartNode(int node){
            //%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
            if(node==DTMDefaultBase.ROOTNODE){
                node=getDocument();
            }
            if(_isRestartable){
                _startNode=node;
                _currentNode=(node==DTM.NULL)?DTM.NULL:NOTPROCESSED;
                return resetPosition();
            }
            return this;
        }

        public int next(){
            if(_currentNode!=DTM.NULL){
                for(int node=(NOTPROCESSED==_currentNode)
                        ?_firstch(makeNodeIdentity(_startNode))
                        :_nextsib(_currentNode);
                    node!=END;
                    node=_nextsib(node)){
                    int nodeHandle=makeNodeHandle(node);
                    if(getNSType(nodeHandle)==_nsType){
                        _currentNode=node;
                        return returnNode(nodeHandle);
                    }
                }
            }
            return END;
        }
    }  // end of NamespaceChildrenIterator

    public final class NamespaceAttributeIterator
            extends InternalAxisIteratorBase{
        private final int _nsType;

        public NamespaceAttributeIterator(int nsType){
            super();
            _nsType=nsType;
        }

        public DTMAxisIterator setStartNode(int node){
            //%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
            if(node==DTMDefaultBase.ROOTNODE){
                node=getDocument();
            }
            if(_isRestartable){
                int nsType=_nsType;
                _startNode=node;
                for(node=getFirstAttribute(node);
                    node!=END;
                    node=getNextAttribute(node)){
                    if(getNSType(node)==nsType){
                        break;
                    }
                }
                _currentNode=node;
                return resetPosition();
            }
            return this;
        }

        public int next(){
            int node=_currentNode;
            int nsType=_nsType;
            int nextNode;
            if(node==END){
                return END;
            }
            for(nextNode=getNextAttribute(node);
                nextNode!=END;
                nextNode=getNextAttribute(nextNode)){
                if(getNSType(nextNode)==nsType){
                    break;
                }
            }
            _currentNode=nextNode;
            return returnNode(node);
        }
    }  // end of NamespaceAttributeIterator
}
