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
 * $Id: DTMDocumentImpl.java,v 1.2.4.1 2005/09/15 08:15:01 suresh_emailid Exp $
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
 * $Id: DTMDocumentImpl.java,v 1.2.4.1 2005/09/15 08:15:01 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.*;
import com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.SourceLocator;

public class DTMDocumentImpl
        implements DTM, ContentHandler, LexicalHandler{
    // Number of lower bits used to represent node index.
    protected static final byte DOCHANDLE_SHIFT=22;
    // Masks the lower order of node handle.
    // Same as {@link DTMConstructor.IDENT_NODE_DEFAULT}
    protected static final int NODEHANDLE_MASK=(1<<(DOCHANDLE_SHIFT+1))-1;
    // Masks the higher order Document handle
    // Same as {@link DTMConstructor.IDENT_DOC_DEFAULT}
    protected static final int DOCHANDLE_MASK=-1-NODEHANDLE_MASK;
    private static final String[] fixednames=
            {
                    null,null,                                                      // nothing, Element
                    null,"#text",                                           // Attr, Text
                    "#cdata_section",null,  // CDATA, EntityReference
                    null,null,                                                      // Entity, PI
                    "#comment","#document", // Comment, Document
                    null,"#document-fragment", // Doctype, DocumentFragment
                    null};                                                                  // Notation
    private final boolean DEBUG=false;
    protected int m_currentNode=-1;               // current node
    protected String m_documentBaseURI;
    int m_docHandle=NULL;          // masked document handle for this dtm document
    int m_docElement=NULL;         // nodeHandle to the root of the actual dtm doc content
    // Context for parse-and-append operations
    int currentParent=0;                  // current parent - default is document root
    int previousSibling=0;                // previous sibling - no previous sibling
    // Local cache for record-at-a-time fetch
    int gotslot[]=new int[4];
    boolean m_isError=false;
    // ========= DTM data structure declarations. ==============
    // nodes array: integer array blocks to hold the first level reference of the nodes,
    // each reference slot is addressed by a nodeHandle index value.
    // Assumes indices are not larger than {@link NODEHANDLE_MASK}
    // ({@link DOCHANDLE_SHIFT} bits).
    ChunkedIntArray nodes=new ChunkedIntArray(4);
    // The tree under construction can itself be used as
    // the element stack, so m_elemStack isn't needed.
    //protected Stack m_elemStack = new Stack();     // element stack
    private boolean previousSiblingWasParent=false;
    // endDocument recieved?
    private boolean done=false;
    private IncrementalSAXSource m_incrSAXSource=null;
    // text/comment table: string buffer to hold the text string values of the document,
    // each of which is addressed by the absolute offset and length in the buffer
    private FastStringBuffer m_char=new FastStringBuffer();
    // Start of string currently being accumulated into m_char;
    // needed because the string may be appended in several chunks.
    private int m_char_current_start=0;
    // %TBD% INITIALIZATION/STARTUP ISSUES
    // -- Should we really be creating these, or should they be
    // passed in from outside? Scott want to be able to share
    // pools across multiple documents, so setting them here is
    // probably not the right default.
    private DTMStringPool m_localNames=new DTMStringPool();
    private DTMStringPool m_nsNames=new DTMStringPool();
    private DTMStringPool m_prefixNames=new DTMStringPool();
    // %TBD% If we use the current ExpandedNameTable mapper, it
    // needs to be bound to the NS and local name pools. Which
    // means it needs to attach to them AFTER we've resolved their
    // startup. Or it needs to attach to this document and
    // retrieve them each time. Or this needs to be
    // an interface _implemented_ by this class... which might be simplest!
    private ExpandedNameTable m_expandedNames=
            new ExpandedNameTable();
    private XMLStringFactory m_xsf;

    public DTMDocumentImpl(DTMManager mgr,int documentNumber,
                           DTMWSFilter whiteSpaceFilter,
                           XMLStringFactory xstringfactory){
        initDocument(documentNumber);    // clear nodes and document handle
        m_xsf=xstringfactory;
    }

    final void initDocument(int documentNumber){
        // save masked DTM document handle
        m_docHandle=documentNumber<<DOCHANDLE_SHIFT;
        // Initialize the doc -- no parent, no next-sib
        nodes.writeSlot(0,DOCUMENT_NODE,-1,-1,0);
        // wait for the first startElement to create the doc root node
        done=false;
    }
    // ========= DTM Implementation Control Functions. ==============

    public void setIncrementalSAXSource(IncrementalSAXSource source){
        m_incrSAXSource=source;
        // Establish SAX-stream link so we can receive the requested data
        source.setContentHandler(this);
        source.setLexicalHandler(this);
        // Are the following really needed? IncrementalSAXSource doesn't yet
        // support them, and they're mostly no-ops here...
        //source.setErrorHandler(this);
        //source.setDTDHandler(this);
        //source.setDeclHandler(this);
    }

    ;

    public void setFeature(String featureId,boolean state){
    }

    public void setProperty(String property,Object value){
    }

    public DTMAxisTraverser getAxisTraverser(final int axis){
        return null;
    }

    public DTMAxisIterator getAxisIterator(final int axis){
        // %TBD%
        return null;
    }

    public DTMAxisIterator getTypedAxisIterator(final int axis,final int type){
        // %TBD%
        return null;
    }

    public boolean hasChildNodes(int nodeHandle){
        return (getFirstChild(nodeHandle)!=NULL);
    }

    public int getFirstChild(int nodeHandle){
        // ###shs worry about tracing/debug later
        nodeHandle&=NODEHANDLE_MASK;
        // Read node into variable
        nodes.readSlot(nodeHandle,gotslot);
        // type is the last half of first slot
        short type=(short)(gotslot[0]&0xFFFF);
        // Check to see if Element or Document node
        if((type==ELEMENT_NODE)||(type==DOCUMENT_NODE)||
                (type==ENTITY_REFERENCE_NODE)){
            // In case when Document root is given
            //      if (nodeHandle == 0) nodeHandle = 1;
            // %TBD% Probably was a mistake.
            // If someone explicitly asks for first child
            // of Document, I would expect them to want
            // that and only that.
            int kid=nodeHandle+1;
            nodes.readSlot(kid,gotslot);
            while(ATTRIBUTE_NODE==(gotslot[0]&0xFFFF)){
                // points to next sibling
                kid=gotslot[2];
                // Return NULL if node has only attributes
                if(kid==NULL) return NULL;
                nodes.readSlot(kid,gotslot);
            }
            // If parent slot matches given parent, return kid
            if(gotslot[1]==nodeHandle){
                int firstChild=kid|m_docHandle;
                return firstChild;
            }
        }
        // No child found
        return NULL;
    }

    public int getLastChild(int nodeHandle){
        // ###shs put trace/debug later
        nodeHandle&=NODEHANDLE_MASK;
        // do not need to test node type since getFirstChild does that
        int lastChild=NULL;
        for(int nextkid=getFirstChild(nodeHandle);nextkid!=NULL;
            nextkid=getNextSibling(nextkid)){
            lastChild=nextkid;
        }
        return lastChild|m_docHandle;
    }

    public int getAttributeNode(int nodeHandle,String namespaceURI,String name){
        int nsIndex=m_nsNames.stringToIndex(namespaceURI),
                nameIndex=m_localNames.stringToIndex(name);
        nodeHandle&=NODEHANDLE_MASK;
        nodes.readSlot(nodeHandle,gotslot);
        short type=(short)(gotslot[0]&0xFFFF);
        // If nodeHandle points to element next slot would be first attribute
        if(type==ELEMENT_NODE)
            nodeHandle++;
        // Iterate through Attribute Nodes
        while(type==ATTRIBUTE_NODE){
            if((nsIndex==(gotslot[0]<<16))&&(gotslot[3]==nameIndex))
                return nodeHandle|m_docHandle;
            // Goto next sibling
            nodeHandle=gotslot[2];
            nodes.readSlot(nodeHandle,gotslot);
        }
        return NULL;
    }

    public int getFirstAttribute(int nodeHandle){
        nodeHandle&=NODEHANDLE_MASK;
        // %REVIEW% jjk: Just a quick observation: If you're going to
        // call readEntry repeatedly on the same node, it may be
        // more efficiently to do a readSlot to get the data locally,
        // reducing the addressing and call-and-return overhead.
        // Should we check if handle is element (do we want sanity checks?)
        if(ELEMENT_NODE!=(nodes.readEntry(nodeHandle,0)&0xFFFF))
            return NULL;
        // First Attribute (if any) should be at next position in table
        nodeHandle++;
        return (ATTRIBUTE_NODE==(nodes.readEntry(nodeHandle,0)&0xFFFF))?
                nodeHandle|m_docHandle:NULL;
    }

    public int getFirstNamespaceNode(int nodeHandle,boolean inScope){
        return NULL;
    }

    public int getNextSibling(int nodeHandle){
        nodeHandle&=NODEHANDLE_MASK;
        // Document root has no next sibling
        if(nodeHandle==0)
            return NULL;
        short type=(short)(nodes.readEntry(nodeHandle,0)&0xFFFF);
        if((type==ELEMENT_NODE)||(type==ATTRIBUTE_NODE)||
                (type==ENTITY_REFERENCE_NODE)){
            int nextSib=nodes.readEntry(nodeHandle,2);
            if(nextSib==NULL)
                return NULL;
            if(nextSib!=0)
                return (m_docHandle|nextSib);
            // ###shs should cycle/wait if nextSib is 0? Working on threading next
        }
        // Next Sibling is in the next position if it shares the same parent
        int thisParent=nodes.readEntry(nodeHandle,1);
        if(nodes.readEntry(++nodeHandle,1)==thisParent)
            return (m_docHandle|nodeHandle);
        return NULL;
    }

    public int getPreviousSibling(int nodeHandle){
        nodeHandle&=NODEHANDLE_MASK;
        // Document root has no previous sibling
        if(nodeHandle==0)
            return NULL;
        int parent=nodes.readEntry(nodeHandle,1);
        int kid=NULL;
        for(int nextkid=getFirstChild(parent);nextkid!=nodeHandle;
            nextkid=getNextSibling(nextkid)){
            kid=nextkid;
        }
        return kid|m_docHandle;
    }

    public int getNextAttribute(int nodeHandle){
        nodeHandle&=NODEHANDLE_MASK;
        nodes.readSlot(nodeHandle,gotslot);
        //%REVIEW% Why are we using short here? There's no storage
        //reduction for an automatic variable, especially one used
        //so briefly, and it typically costs more cycles to process
        //than an int would.
        short type=(short)(gotslot[0]&0xFFFF);
        if(type==ELEMENT_NODE){
            return getFirstAttribute(nodeHandle);
        }else if(type==ATTRIBUTE_NODE){
            if(gotslot[2]!=NULL)
                return (m_docHandle|gotslot[2]);
        }
        return NULL;
    }

    public int getNextNamespaceNode(int baseHandle,int namespaceHandle,boolean inScope){
        // ###shs need to work on namespace
        return NULL;
    }
    //================================================================
    // ========= SAX2 ContentHandler methods =========
    // Accept SAX events, use them to build/extend the DTM tree.
    // Replaces the deprecated DocumentHandler interface.

    public int getParent(int nodeHandle){
        // Should check to see within range?
        // Document Root should not have to be handled differently
        return (m_docHandle|nodes.readEntry(nodeHandle,1));
    }

    public int getDocument(){
        return m_docHandle;
    }

    public int getOwnerDocument(int nodeHandle){
        // Assumption that Document Node is always in 0 slot
        if((nodeHandle&NODEHANDLE_MASK)==0)
            return NULL;
        return (nodeHandle&DOCHANDLE_MASK);
    }

    public int getDocumentRoot(int nodeHandle){
        // Assumption that Document Node is always in 0 slot
        if((nodeHandle&NODEHANDLE_MASK)==0)
            return NULL;
        return (nodeHandle&DOCHANDLE_MASK);
    }

    public XMLString getStringValue(int nodeHandle){
        // ###zaj - researching
        nodes.readSlot(nodeHandle,gotslot);
        int nodetype=gotslot[0]&0xFF;
        String value=null;
        switch(nodetype){
            case TEXT_NODE:
            case COMMENT_NODE:
            case CDATA_SECTION_NODE:
                value=m_char.getString(gotslot[2],gotslot[3]);
                break;
            case PROCESSING_INSTRUCTION_NODE:
            case ATTRIBUTE_NODE:
            case ELEMENT_NODE:
            case ENTITY_REFERENCE_NODE:
            default:
                break;
        }
        return m_xsf.newstr(value);
    }

    //###zaj - tbd
    public int getStringValueChunkCount(int nodeHandle){
        //###zaj    return value
        return 0;
    }

    //###zaj - tbd
    public char[] getStringValueChunk(int nodeHandle,int chunkIndex,
                                      int[] startAndLen){
        return new char[0];
    }

    public int getExpandedTypeID(int nodeHandle){
        nodes.readSlot(nodeHandle,gotslot);
        String qName=m_localNames.indexToString(gotslot[3]);
        // Remove prefix from qName
        // %TBD% jjk This is assuming the elementName is the qName.
        int colonpos=qName.indexOf(":");
        String localName=qName.substring(colonpos+1);
        // Get NS
        String namespace=m_nsNames.indexToString(gotslot[0]<<16);
        // Create expanded name
        String expandedName=namespace+":"+localName;
        int expandedNameID=m_nsNames.stringToIndex(expandedName);
        return expandedNameID;
    }

    public int getExpandedTypeID(String namespace,String localName,int type){
        // Create expanded name
        // %TBD% jjk Expanded name is bitfield-encoded as
        // typeID[6]nsuriID[10]localID[16]. Switch to that form, and to
        // accessing the ns/local via their tables rather than confusing
        // nsnames and expandednames.
        String expandedName=namespace+":"+localName;
        int expandedNameID=m_nsNames.stringToIndex(expandedName);
        return expandedNameID;
    }

    public String getLocalNameFromExpandedNameID(int ExpandedNameID){
        // Get expanded name
        String expandedName=m_localNames.indexToString(ExpandedNameID);
        // Remove prefix from expanded name
        int colonpos=expandedName.indexOf(":");
        String localName=expandedName.substring(colonpos+1);
        return localName;
    }

    public String getNamespaceFromExpandedNameID(int ExpandedNameID){
        String expandedName=m_localNames.indexToString(ExpandedNameID);
        // Remove local name from expanded name
        int colonpos=expandedName.indexOf(":");
        String nsName=expandedName.substring(0,colonpos);
        return nsName;
    }

    public String getNodeName(int nodeHandle){
        nodes.readSlot(nodeHandle,gotslot);
        short type=(short)(gotslot[0]&0xFFFF);
        String name=fixednames[type];
        if(null==name){
            int i=gotslot[3];
                  /**/
            System.out.println("got i="+i+" "+(i>>16)+"/"+(i&0xffff));
            name=m_localNames.indexToString(i&0xFFFF);
            String prefix=m_prefixNames.indexToString(i>>16);
            if(prefix!=null&&prefix.length()>0)
                name=prefix+":"+name;
        }
        return name;
    }

    public String getNodeNameX(int nodeHandle){
        return null;
    }

    public String getLocalName(int nodeHandle){
        nodes.readSlot(nodeHandle,gotslot);
        short type=(short)(gotslot[0]&0xFFFF);
        String name="";
        if((type==ELEMENT_NODE)||(type==ATTRIBUTE_NODE)){
            int i=gotslot[3];
            name=m_localNames.indexToString(i&0xFFFF);
            if(name==null) name="";
        }
        return name;
    }

    public String getPrefix(int nodeHandle){
        nodes.readSlot(nodeHandle,gotslot);
        short type=(short)(gotslot[0]&0xFFFF);
        String name="";
        if((type==ELEMENT_NODE)||(type==ATTRIBUTE_NODE)){
            int i=gotslot[3];
            name=m_prefixNames.indexToString(i>>16);
            if(name==null) name="";
        }
        return name;
    }

    public String getNamespaceURI(int nodeHandle){
        return null;
    }

    public String getNodeValue(int nodeHandle){
        nodes.readSlot(nodeHandle,gotslot);
        int nodetype=gotslot[0]&0xFF;         // ###zaj use mask to get node type
        String value=null;
        switch(nodetype){                     // ###zaj todo - document nodetypes
            case ATTRIBUTE_NODE:
                nodes.readSlot(nodeHandle+1,gotslot);
            case TEXT_NODE:
            case COMMENT_NODE:
            case CDATA_SECTION_NODE:
                value=m_char.getString(gotslot[2],gotslot[3]);         //###zaj
                break;
            case PROCESSING_INSTRUCTION_NODE:
            case ELEMENT_NODE:
            case ENTITY_REFERENCE_NODE:
            default:
                break;
        }
        return value;
    }

    public short getNodeType(int nodeHandle){
        return (short)(nodes.readEntry(nodeHandle,0)&0xFFFF);
    }

    public short getLevel(int nodeHandle){
        short count=0;
        while(nodeHandle!=0){
            count++;
            nodeHandle=nodes.readEntry(nodeHandle,1);
        }
        return count;
    }
    //================================================================
    // ========= Document Handler Functions =========
    // %REVIEW% jjk -- DocumentHandler is  SAX Level 1, and deprecated....
    // and this wasn't a fully compliant or declared implementation of that API
    // in any case. Phase out in favor of SAX2 ContentHandler/LexicalHandler

    public boolean isSupported(String feature,String version){
        return false;
    }
//      /**
//       * Receive hint of the end of a document.
//       *
//       * <p>The content handler will invoke this method only once, and it will
//       * be the last method invoked during the parse.  The handler shall not
//       * not invoke this method until it has either abandoned parsing
//       * (because of an unrecoverable error) or reached the end of
//       * input.</p>
//       */
//      public void documentEnd()
//      {
//              done = true;
//              // %TBD% may need to notice the last slot number and slot count to avoid
//              // residual data from provious use of this DTM
//      }
//      /**
//       * Receive notification of the beginning of a document.
//       *
//       * <p>The SAX parser will invoke this method only once, before any
//       * other methods in this interface.</p>
//       */
//      public void reset()
//      {
//              // %TBD% reset slot 0 to indicate ChunkedIntArray reuse or wait for
//              //       the next initDocument().
//              m_docElement = NULL;     // reset nodeHandle to the root of the actual dtm doc content
//              initDocument(0);
//      }
//      /**
//       * Factory method; creates an Element node in this document.
//       *
//       * The node created will be chained according to its natural order of request
//       * received.  %TBD% It can be rechained later via the optional DTM writable interface.
//       *
//       * <p>The XML content handler will invoke endElement() method after all
//       * of the element's content are processed in order to give DTM the indication
//       * to prepare and patch up parent and sibling node pointers.</p>
//       *
//       * <p>The following interface for createElement will use an index value corresponds
//       * to the symbol entry in the DTMDStringPool based symbol tables.</p>
//       *
//       * @param nsIndex The namespace of the node
//       * @param nameIndex The element name.
//       * @see #endElement
//       * @see org.xml.sax.Attributes
//       * @return nodeHandle int of the element created
//       */
//      public int createElement(int nsIndex, int nameIndex, Attributes atts)
//      {
//              // do document root node creation here on the first element, create nodes for
//              // this element and its attributes, store the element, namespace, and attritute
//              // name indexes to the nodes array, keep track of the current node and parent
//              // element used
//              // W0  High:  Namespace  Low:  Node Type
//              int w0 = (nsIndex << 16) | ELEMENT_NODE;
//              // W1: Parent
//              int w1 = currentParent;
//              // W2: Next  (initialized as 0)
//              int w2 = 0;
//              // W3: Tagname
//              int w3 = nameIndex;
//              //int ourslot = nodes.appendSlot(w0, w1, w2, w3);
//              int ourslot = appendNode(w0, w1, w2, w3);
//              currentParent = ourslot;
//              previousSibling = 0;
//              setAttributes(atts);
//              // set the root element pointer when creating the first element node
//              if (m_docElement == NULL)
//                      m_docElement = ourslot;
//              return (m_docHandle | ourslot);
//      }
//      // Factory method to create an Element node not associated with a given name space
//      // using String value parameters passed in from a content handler or application
//      /**
//       * Factory method; creates an Element node not associated with a given name space in this document.
//       *
//       * The node created will be chained according to its natural order of request
//       * received.  %TBD% It can be rechained later via the optional DTM writable interface.
//       *
//       * <p>The XML content handler or application will invoke endElement() method after all
//       * of the element's content are processed in order to give DTM the indication
//       * to prepare and patch up parent and sibling node pointers.</p>
//       *
//       * <p>The following parameters for createElement contains raw string values for name
//       * symbols used in an Element node.</p>
//       *
//       * @param name String the element name, including the prefix if any.
//       * @param atts The attributes attached to the element, if any.
//       * @see #endElement
//       * @see org.xml.sax.Attributes
//       */
//      public int createElement(String name, Attributes atts)
//      {
//              // This method wraps around the index valued interface of the createElement interface.
//              // The raw string values are stored into the current DTM name symbol tables.  The method
//              // method will then use the index values returned to invoke the other createElement()
//              // onverted to index values modified to match a
//              // method.
//              int nsIndex = NULL;
//              int nameIndex = m_localNames.stringToIndex(name);
//              // note - there should be no prefix separator in the name because it is not associated
//              // with a name space
//              return createElement(nsIndex, nameIndex, atts);
//      }
//      // Factory method to create an Element node associated with a given name space
//      // using String value parameters passed in from a content handler or application
//      /**
//       * Factory method; creates an Element node associated with a given name space in this document.
//       *
//       * The node created will be chained according to its natural order of request
//       * received.  %TBD% It can be rechained later via the optional DTM writable interface.
//       *
//       * <p>The XML content handler or application will invoke endElement() method after all
//       * of the element's content are processed in order to give DTM the indication
//       * to prepare and patch up parent and sibling node pointers.</p>
//       *
//       * <p>The following parameters for createElementNS contains raw string values for name
//       * symbols used in an Element node.</p>
//       *
//       * @param ns String the namespace of the node
//       * @param name String the element name, including the prefix if any.
//       * @param atts The attributes attached to the element, if any.
//       * @see #endElement
//       * @see org.xml.sax.Attributes
//       */
//      public int createElementNS(String ns, String name, Attributes atts)
//      {
//              // This method wraps around the index valued interface of the createElement interface.
//              // The raw string values are stored into the current DTM name symbol tables.  The method
//              // method will then use the index values returned to invoke the other createElement()
//              // onverted to index values modified to match a
//              // method.
//              int nsIndex = m_nsNames.stringToIndex(ns);
//              int nameIndex = m_localNames.stringToIndex(name);
//              // The prefixIndex is not needed by the indexed interface of the createElement method
//              int prefixSep = name.indexOf(":");
//              int prefixIndex = m_prefixNames.stringToIndex(name.substring(0, prefixSep));
//              return createElement(nsIndex, nameIndex, atts);
//      }
//      /**
//       * Receive an indication for the end of an element.
//       *
//       * <p>The XML content handler will invoke this method at the end of every
//       * element in the XML document to give hint its time to pop up the current
//       * element and parent and patch up parent and sibling pointers if necessary
//       *
//       * <p>%tbd% The following interface may need to be modified to match a
//       * coordinated access to the DTMDStringPool based symbol tables.</p>
//               *
//       * @param ns the namespace of the element
//       * @param name The element name
//       */
//      public void endElement(String ns, String name)
//      {
//              // pop up the stacks
//              //
//              if (previousSiblingWasParent)
//                      nodes.writeEntry(previousSibling, 2, NULL);
//              // Pop parentage
//              previousSibling = currentParent;
//              nodes.readSlot(currentParent, gotslot);
//              currentParent = gotslot[1] & 0xFFFF;
//              // The element just being finished will be
//              // the previous sibling for the next operation
//              previousSiblingWasParent = true;
//              // Pop a level of namespace table
//              // namespaceTable.removeLastElem();
//      }
//      /**
//       * Creates attributes for the current node.
//       *
//       * @param atts Attributes to be created.
//       */
//      void setAttributes(Attributes atts) {
//              int atLength = (null == atts) ? 0 : atts.getLength();
//              for (int i=0; i < atLength; i++) {
//                      String qname = atts.getQName(i);
//                      createAttribute(atts.getQName(i), atts.getValue(i));
//              }
//      }
//      /**
//       * Appends an attribute to the document.
//       * @param qname Qualified Name of the attribute
//       * @param value Value of the attribute
//       * @return Handle of node
//       */
//      public int createAttribute(String qname, String value) {
//              int colonpos = qname.indexOf(":");
//              String attName = qname.substring(colonpos+1);
//              int w0 = 0;
//              if (colonpos > 0) {
//                      String prefix = qname.substring(0, colonpos);
//                      if (prefix.equals("xml")) {
//                              //w0 = ATTRIBUTE_NODE |
//                              //      (com.sun.org.apache.xalan.internal.templates.Constants.S_XMLNAMESPACEURI << 16);
//                      } else {
//                              //w0 = ATTRIBUTE_NODE |
//                      }
//              } else {
//                      w0 = ATTRIBUTE_NODE;
//              }
//              // W1:  Parent
//              int w1 = currentParent;
//              // W2:  Next (not yet resolved)
//              int w2 = 0;
//              // W3:  Tag name
//              int w3 = m_localNames.stringToIndex(attName);
//              // Add node
//              int ourslot = appendNode(w0, w1, w2, w3);
//              previousSibling = ourslot;      // Should attributes be previous siblings
//              // W0: Node Type
//              w0 = TEXT_NODE;
//              // W1: Parent
//              w1 = ourslot;
//              // W2: Start Position within buffer
//              w2 = m_char.length();
//              m_char.append(value);
//              // W3: Length
//              w3 = m_char.length() - w2;
//              appendNode(w0, w1, w2, w3);
//              charStringStart=m_char.length();
//              charStringLength = 0;
//              //previousSibling = ourslot;
//              // Attrs are Parents
//              previousSiblingWasParent = true;
//              return (m_docHandle | ourslot);
//      }
//      /**
//       * Factory method; creates a Text node in this document.
//       *
//       * The node created will be chained according to its natural order of request
//       * received.  %TBD% It can be rechained later via the optional DTM writable interface.
//       *
//       * @param text String The characters text string from the XML document.
//       * @return int DTM node-number of the text node created
//       */
//      public int createTextNode(String text)
//      throws DTMException
//      {
//              // wraps around the index value based createTextNode method
//              return createTextNode(text.toCharArray(), 0, text.length());
//      }
//      /**
//       * Factory method; creates a Text node in this document.
//       *
//       * The node created will be chained according to its natural order of request
//       * received.  %TBD% It can be rechained later via the optional DTM writable interface.
//       *
//       * %REVIEW% for text normalization issues, unless we are willing to
//       * insist that all adjacent text must be merged before this method
//       * is called.
//       *
//       * @param ch The characters from the XML document.
//       * @param start The start position in the array.
//       * @param length The number of characters to read from the array.
//       */
//      public int createTextNode(char ch[], int start, int length)
//      throws DTMException
//      {
//              m_char.append(ch, start, length);               // store the chunk to the text/comment string table
//              // create a Text Node
//              // %TBD% may be possible to combine with appendNode()to replace the next chunk of code
//              int w0 = TEXT_NODE;
//              // W1: Parent
//              int w1 = currentParent;
//              // W2: Start position within m_char
//              int w2 = charStringStart;
//              // W3: Length of the full string
//              int w3 = length;
//              int ourslot = appendNode(w0, w1, w2, w3);
//              previousSibling = ourslot;
//              charStringStart=m_char.length();
//              charStringLength = 0;
//              return (m_docHandle | ourslot);
//      }
//      /**
//       * Factory method; creates a Comment node in this document.
//       *
//       * The node created will be chained according to its natural order of request
//       * received.  %TBD% It can be rechained later via the optional DTM writable interface.
//       *
//       * @param text String The characters text string from the XML document.
//       * @return int DTM node-number of the text node created
//       */
//      public int createComment(String text)
//      throws DTMException
//      {
//              // wraps around the index value based createTextNode method
//              return createComment(text.toCharArray(), 0, text.length());
//      }
//      /**
//       * Factory method; creates a Comment node in this document.
//       *
//       * The node created will be chained according to its natural order of request
//       * received.  %TBD% It can be rechained later via the optional DTM writable interface.
//       *
//       * @param ch An array holding the characters in the comment.
//       * @param start The starting position in the array.
//       * @param length The number of characters to use from the array.
//       * @see DTMException
//       */
//      public int createComment(char ch[], int start, int length)
//      throws DTMException
//      {
//              m_char.append(ch, start, length);               // store the comment string to the text/comment string table
//              // create a Comment Node
//              // %TBD% may be possible to combine with appendNode()to replace the next chunk of code
//              int w0 = COMMENT_NODE;
//              // W1: Parent
//              int w1 = currentParent;
//              // W2: Start position within m_char
//              int w2 = charStringStart;
//              // W3: Length of the full string
//              int w3 = length;
//              int ourslot = appendNode(w0, w1, w2, w3);
//              previousSibling = ourslot;
//              charStringStart=m_char.length();
//              charStringLength = 0;
//              return (m_docHandle | ourslot);
//      }
//      // Counters to keep track of the current text string being accumulated with respect
//      // to the text/comment string table: charStringStart should point to the starting
//      // offset of the string in the table and charStringLength the acccumulated length when
//      // appendAccumulatedText starts, and reset to the end of the table and 0 at the end
//      // of appendAccumulatedText for the next set of characters receives
//      int charStringStart=0,charStringLength=0;
    // ========= Document Navigation Functions =========

    public String getDocumentBaseURI(){
        return m_documentBaseURI;
    }

    public void setDocumentBaseURI(String baseURI){
        m_documentBaseURI=baseURI;
    }

    public String getDocumentSystemIdentifier(int nodeHandle){
        return null;
    }

    public String getDocumentEncoding(int nodeHandle){
        return null;
    }

    public String getDocumentStandalone(int nodeHandle){
        return null;
    }

    public String getDocumentVersion(int documentHandle){
        return null;
    }

    public boolean getDocumentAllDeclarationsProcessed(){
        return false;
    }

    public String getDocumentTypeDeclarationSystemIdentifier(){
        return null;
    }

    public String getDocumentTypeDeclarationPublicIdentifier(){
        return null;
    }

    public int getElementById(String elementId){
        return 0;
    }

    public String getUnparsedEntityURI(String name){
        return null;
    }

    public boolean supportsPreStripping(){
        return false;
    }

    public boolean isNodeAfter(int nodeHandle1,int nodeHandle2){
        return false;
    }

    public boolean isCharacterElementContentWhitespace(int nodeHandle){
        return false;
    }

    public boolean isDocumentAllDeclarationsProcessed(int documentHandle){
        return false;
    }

    public boolean isAttributeSpecified(int attributeHandle){
        return false;
    }

    public void dispatchCharactersEvents(
            int nodeHandle,ContentHandler ch,boolean normalize)
            throws org.xml.sax.SAXException{
    }

    public void dispatchToEvents(int nodeHandle,ContentHandler ch)
            throws org.xml.sax.SAXException{
    }

    public org.w3c.dom.Node getNode(int nodeHandle){
        return null;
    }

    public boolean needsTwoThreads(){
        return null!=m_incrSAXSource;
    }

    public ContentHandler getContentHandler(){
        if(m_incrSAXSource instanceof IncrementalSAXSource_Filter)
            return (ContentHandler)m_incrSAXSource;
        else
            return this;
    }

    public LexicalHandler getLexicalHandler(){
        if(m_incrSAXSource instanceof IncrementalSAXSource_Filter)
            return (LexicalHandler)m_incrSAXSource;
        else
            return this;
    }

    public org.xml.sax.EntityResolver getEntityResolver(){
        return null;
    }

    public org.xml.sax.DTDHandler getDTDHandler(){
        return null;
    }

    public org.xml.sax.ErrorHandler getErrorHandler(){
        return null;
    }

    public org.xml.sax.ext.DeclHandler getDeclHandler(){
        return null;
    }

    public void appendChild(int newChild,boolean clone,boolean cloneDepth){
        boolean sameDoc=((newChild&DOCHANDLE_MASK)==m_docHandle);
        if(clone||!sameDoc){
        }else{
        }
    }

    public void appendTextChild(String str){
        // ###shs Think more about how this differs from createTextNode
        //%TBD%
    }

    public SourceLocator getSourceLocatorFor(int node){
        return null;
    }

    public void documentRegistration(){
    }

    public void documentRelease(){
    }

    public void migrateTo(DTMManager manager){
    }

    public DTMStringPool getLocalNameTable(){
        return m_localNames;
    }

    public void setLocalNameTable(DTMStringPool poolRef){
        m_localNames=poolRef;
    }
    // ============== Document query functions ==============

    public DTMStringPool getNsNameTable(){
        return m_nsNames;
    }

    public void setNsNameTable(DTMStringPool poolRef){
        m_nsNames=poolRef;
    }

    public DTMStringPool getPrefixNameTable(){
        return m_prefixNames;
    }

    public void setPrefixNameTable(DTMStringPool poolRef){
        m_prefixNames=poolRef;
    }

    FastStringBuffer getContentBuffer(){
        return m_char;
    }

    void setContentBuffer(FastStringBuffer buffer){
        m_char=buffer;
    }

    public void setDocumentLocator(Locator locator){
        // No-op for DTM
    }

    public void startDocument()
            throws org.xml.sax.SAXException{
        appendStartDocument();
    }

    public void endDocument()
            throws org.xml.sax.SAXException{
        // May need to tell the low-level builder code to pop up a level.
        // There _should't_ be any significant pending text at this point.
        appendEndDocument();
    }

    public void startPrefixMapping(String prefix,String uri)
            throws org.xml.sax.SAXException{
        // No-op in DTM, handled during element/attr processing?
    }

    public void endPrefixMapping(String prefix)
            throws org.xml.sax.SAXException{
        // No-op
    }

    public void startElement(String namespaceURI,String localName,
                             String qName,Attributes atts)
            throws org.xml.sax.SAXException{
        processAccumulatedText();
        // %TBD% Split prefix off qname
        String prefix=null;
        int colon=qName.indexOf(':');
        if(colon>0)
            prefix=qName.substring(0,colon);
        // %TBD% Where do we pool expandedName, or is it just the union, or...
    /**/
        System.out.println("Prefix="+prefix+" index="+m_prefixNames.stringToIndex(prefix));
        appendStartElement(m_nsNames.stringToIndex(namespaceURI),
                m_localNames.stringToIndex(localName),
                m_prefixNames.stringToIndex(prefix)); /////// %TBD%
        // %TBD% I'm assuming that DTM will require resequencing of
        // NS decls before other attrs, hence two passes are taken.
        // %TBD% Is there an easier way to test for NSDecl?
        int nAtts=(atts==null)?0:atts.getLength();
        // %TBD% Countdown is more efficient if nobody cares about sequence.
        for(int i=nAtts-1;i>=0;--i){
            qName=atts.getQName(i);
            if(qName.startsWith("xmlns:")||"xmlns".equals(qName)){
                prefix=null;
                colon=qName.indexOf(':');
                if(colon>0){
                    prefix=qName.substring(0,colon);
                }else{
                    // %REVEIW% Null or ""?
                    prefix=null; // Default prefix
                }
                appendNSDeclaration(
                        m_prefixNames.stringToIndex(prefix),
                        m_nsNames.stringToIndex(atts.getValue(i)),
                        atts.getType(i).equalsIgnoreCase("ID"));
            }
        }
        for(int i=nAtts-1;i>=0;--i){
            qName=atts.getQName(i);
            if(!(qName.startsWith("xmlns:")||"xmlns".equals(qName))){
                // %TBD% I hate having to extract the prefix into a new
                // string when we may never use it. Consider pooling whole
                // qNames, which are already strings?
                prefix=null;
                colon=qName.indexOf(':');
                if(colon>0){
                    prefix=qName.substring(0,colon);
                    localName=qName.substring(colon+1);
                }else{
                    prefix=""; // Default prefix
                    localName=qName;
                }
                m_char.append(atts.getValue(i)); // Single-string value
                int contentEnd=m_char.length();
                if(!("xmlns".equals(prefix)||"xmlns".equals(qName)))
                    appendAttribute(m_nsNames.stringToIndex(atts.getURI(i)),
                            m_localNames.stringToIndex(localName),
                            m_prefixNames.stringToIndex(prefix),
                            atts.getType(i).equalsIgnoreCase("ID"),
                            m_char_current_start,contentEnd-m_char_current_start);
                m_char_current_start=contentEnd;
            }
        }
    }
    // ============== Boolean methods ================

    public void endElement(String namespaceURI,String localName,
                           String qName)
            throws org.xml.sax.SAXException{
        processAccumulatedText();
        // No args but we do need to tell the low-level builder code to
        // pop up a level.
        appendEndElement();
    }

    public void characters(char[] ch,int start,int length)
            throws org.xml.sax.SAXException{
        // Actually creating the text node is handled by
        // processAccumulatedText(); here we just accumulate the
        // characters into the buffer.
        m_char.append(ch,start,length);
    }

    public void ignorableWhitespace(char[] ch,int start,int length)
            throws org.xml.sax.SAXException{
        // %TBD% I believe ignorable text isn't part of the DTM model...?
    }

    public void processingInstruction(String target,String data)
            throws org.xml.sax.SAXException{
        processAccumulatedText();
        // %TBD% Which pools do target and data go into?
    }

    public void skippedEntity(String name)
            throws org.xml.sax.SAXException{
        processAccumulatedText();
        //%TBD%
    }
    // ========== Direct SAX Dispatch, for optimization purposes ========

    // Flush string accumulation into a text node
    private void processAccumulatedText(){
        int len=m_char.length();
        if(len!=m_char_current_start){
            // The FastStringBuffer has been previously agreed upon
            appendTextChild(m_char_current_start,len-m_char_current_start);
            m_char_current_start=len;
        }
    }

    void appendTextChild(int m_char_current_start,int contentLength){
        // create a Text Node
        // %TBD% may be possible to combine with appendNode()to replace the next chunk of code
        int w0=TEXT_NODE;
        // W1: Parent
        int w1=currentParent;
        // W2: Start position within m_char
        int w2=m_char_current_start;
        // W3: Length of the full string
        int w3=contentLength;
        int ourslot=appendNode(w0,w1,w2,w3);
        previousSibling=ourslot;
    }

    private final int appendNode(int w0,int w1,int w2,int w3){
        // A decent compiler may inline this.
        int slotnumber=nodes.appendSlot(w0,w1,w2,w3);
        if(DEBUG) System.out.println(slotnumber+": "+w0+" "+w1+" "+w2+" "+w3);
        if(previousSiblingWasParent)
            nodes.writeEntry(previousSibling,2,slotnumber);
        previousSiblingWasParent=false;       // Set the default; endElement overrides
        return slotnumber;
    }
    // ==== Construction methods (may not be supported by some implementations!) =====
    // %REVIEW% jjk: These probably aren't the right API. At the very least
    // they need to deal with current-insertion-location and end-element
    // issues.

    void appendEndElement(){
        // pop up the stacks
        if(previousSiblingWasParent)
            nodes.writeEntry(previousSibling,2,NULL);
        // Pop parentage
        previousSibling=currentParent;
        nodes.readSlot(currentParent,gotslot);
        currentParent=gotslot[1]&0xFFFF;
        // The element just being finished will be
        // the previous sibling for the next operation
        previousSiblingWasParent=true;
        // Pop a level of namespace table
        // namespaceTable.removeLastElem();
    }

    void appendStartElement(int namespaceIndex,int localNameIndex,int prefixIndex){
        // do document root node creation here on the first element, create nodes for
        // this element and its attributes, store the element, namespace, and attritute
        // name indexes to the nodes array, keep track of the current node and parent
        // element used
        // W0  High:  Namespace  Low:  Node Type
        int w0=(namespaceIndex<<16)|ELEMENT_NODE;
        // W1: Parent
        int w1=currentParent;
        // W2: Next  (initialized as 0)
        int w2=0;
        // W3: Tagname high: prefix Low: local name
        int w3=localNameIndex|prefixIndex<<16;
                /**/
        System.out.println("set w3="+w3+" "+(w3>>16)+"/"+(w3&0xffff));
        //int ourslot = nodes.appendSlot(w0, w1, w2, w3);
        int ourslot=appendNode(w0,w1,w2,w3);
        currentParent=ourslot;
        previousSibling=0;
        // set the root element pointer when creating the first element node
        if(m_docElement==NULL)
            m_docElement=ourslot;
    }
    //================================================================
    // ==== BUILDER methods ====
    // %TBD% jjk: SHOULD PROBABLY BE INLINED, unless we want to support
    // both SAX1 and SAX2 and share this logic between them.

    void appendNSDeclaration(int prefixIndex,int namespaceIndex,
                             boolean isID){
        // %REVIEW% I'm assigning this node the "namespace for namespaces"
        // which the DOM defined. It is expected that the Namespace spec will
        // adopt this as official. It isn't strictly needed since it's implied
        // by the nodetype, but for now...
        // %REVIEW% Prefix need not be recorded; it's implied too. But
        // recording it might simplify the design.
        // %TBD% isID is not currently honored.
        final int namespaceForNamespaces=m_nsNames.stringToIndex("http://www.w3.org/2000/xmlns/");
        // W0  High:  Namespace  Low:  Node Type
        int w0=NAMESPACE_NODE|(m_nsNames.stringToIndex("http://www.w3.org/2000/xmlns/")<<16);
        // W1:  Parent
        int w1=currentParent;
        // W2:  CURRENTLY UNUSED -- It's next-sib in attrs, but we have no kids.
        int w2=0;
        // W3:  namespace name
        int w3=namespaceIndex;
        // Add node
        int ourslot=appendNode(w0,w1,w2,w3);
        previousSibling=ourslot;  // Should attributes be previous siblings
        previousSiblingWasParent=false;
        return;//(m_docHandle | ourslot);
    }

    void appendAttribute(int namespaceIndex,int localNameIndex,int prefixIndex,
                         boolean isID,
                         int m_char_current_start,int contentLength){
        // %TBD% isID is not currently honored.
        // W0  High:  Namespace  Low:  Node Type
        int w0=ATTRIBUTE_NODE|namespaceIndex<<16;
        // W1:  Parent
        int w1=currentParent;
        // W2:  Next (not yet resolved)
        int w2=0;
        // W3:  Tagname high: prefix Low: local name
        int w3=localNameIndex|prefixIndex<<16;
    /**/
        System.out.println("set w3="+w3+" "+(w3>>16)+"/"+(w3&0xffff));
        // Add node
        int ourslot=appendNode(w0,w1,w2,w3);
        previousSibling=ourslot;  // Should attributes be previous siblings
        // Attribute's content is currently appended as a Text Node
        // W0: Node Type
        w0=TEXT_NODE;
        // W1: Parent
        w1=ourslot;
        // W2: Start Position within buffer
        w2=m_char_current_start;
        // W3: Length
        w3=contentLength;
        appendNode(w0,w1,w2,w3);
        // Attrs are Parents
        previousSiblingWasParent=true;
        return;//(m_docHandle | ourslot);
    }

    void appendEndDocument(){
        done=true;
        // %TBD% may need to notice the last slot number and slot count to avoid
        // residual data from provious use of this DTM
    }

    void appendStartDocument(){
        // %TBD% reset slot 0 to indicate ChunkedIntArray reuse or wait for
        //       the next initDocument().
        m_docElement=NULL;         // reset nodeHandle to the root of the actual dtm doc content
        initDocument(0);
    }

    public void startDTD(String name,String publicId,
                         String systemId)
            throws org.xml.sax.SAXException{
        // No-op in DTM
    }

    public void endDTD()
            throws org.xml.sax.SAXException{
        // No-op in DTM
    }

    public void startEntity(String name)
            throws org.xml.sax.SAXException{
        // No-op in DTM
    }

    public void endEntity(String name)
            throws org.xml.sax.SAXException{
        // No-op in DTM
    }

    public void startCDATA()
            throws org.xml.sax.SAXException{
        // No-op in DTM
    }

    public void endCDATA()
            throws org.xml.sax.SAXException{
        // No-op in DTM
    }

    //
    // LexicalHandler support. Not all SAX2 parsers support these events
    // but we may want to pass them through when they exist...
    //
    public void comment(char[] ch,int start,int length)
            throws org.xml.sax.SAXException{
        processAccumulatedText();
        m_char.append(ch,start,length); // Single-string value
        appendComment(m_char_current_start,length);
        m_char_current_start+=length;
    }

    void appendComment(int m_char_current_start,int contentLength){
        // create a Comment Node
        // %TBD% may be possible to combine with appendNode()to replace the next chunk of code
        int w0=COMMENT_NODE;
        // W1: Parent
        int w1=currentParent;
        // W2: Start position within m_char
        int w2=m_char_current_start;
        // W3: Length of the full string
        int w3=contentLength;
        int ourslot=appendNode(w0,w1,w2,w3);
        previousSibling=ourslot;
    }

    public int getNextDescendant(int subtreeRootHandle,int nodeHandle){
        subtreeRootHandle&=NODEHANDLE_MASK;
        nodeHandle&=NODEHANDLE_MASK;
        // Document root [Document Node? -- jjk] - no next-sib
        if(nodeHandle==0)
            return NULL;
        while(!m_isError){
            // Document done and node out of bounds
            if(done&&(nodeHandle>nodes.slotsUsed()))
                break;
            if(nodeHandle>subtreeRootHandle){
                nodes.readSlot(nodeHandle+1,gotslot);
                if(gotslot[2]!=0){
                    short type=(short)(gotslot[0]&0xFFFF);
                    if(type==ATTRIBUTE_NODE){
                        nodeHandle+=2;
                    }else{
                        int nextParentPos=gotslot[1];
                        if(nextParentPos>=subtreeRootHandle)
                            return (m_docHandle|(nodeHandle+1));
                        else
                            break;
                    }
                }else if(!done){
                    // Add wait logic here
                }else
                    break;
            }else{
                nodeHandle++;
            }
        }
        // Probably should throw error here like original instead of returning
        return NULL;
    }

    public int getNextFollowing(int axisContextHandle,int nodeHandle){
        //###shs still working on
        return NULL;
    }

    public int getNextPreceding(int axisContextHandle,int nodeHandle){
        // ###shs copied from Xalan 1, what is this suppose to do?
        nodeHandle&=NODEHANDLE_MASK;
        while(nodeHandle>1){
            nodeHandle--;
            if(ATTRIBUTE_NODE==(nodes.readEntry(nodeHandle,0)&0xFFFF))
                continue;
            // if nodeHandle is _not_ an ancestor of
            // axisContextHandle, specialFind will return it.
            // If it _is_ an ancestor, specialFind will return -1
            // %REVIEW% unconditional return defeats the
            // purpose of the while loop -- does this
            // logic make any sense?
            return (m_docHandle|nodes.specialFind(axisContextHandle,nodeHandle));
        }
        return NULL;
    }

    public int getDocumentRoot(){
        return (m_docHandle|m_docElement);
    }
}
