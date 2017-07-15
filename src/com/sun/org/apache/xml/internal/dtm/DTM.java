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
 * $Id: DTM.java,v 1.2.4.1 2005/09/15 08:14:51 suresh_emailid Exp $
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
 * $Id: DTM.java,v 1.2.4.1 2005/09/15 08:14:51 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm;

import com.sun.org.apache.xml.internal.utils.XMLString;

import javax.xml.transform.SourceLocator;

public interface DTM{
    public static final int NULL=-1;
    // These nodeType mnemonics and values are deliberately the same as those
    // used by the DOM, for convenient mapping
    //
    // %REVIEW% Should we actually define these as initialized to,
    // eg. org.w3c.dom.Document.ELEMENT_NODE?
    public static final short ROOT_NODE=0;
    public static final short ELEMENT_NODE=1;
    public static final short ATTRIBUTE_NODE=2;
    public static final short TEXT_NODE=3;
    public static final short CDATA_SECTION_NODE=4;
    public static final short ENTITY_REFERENCE_NODE=5;
    public static final short ENTITY_NODE=6;
    public static final short PROCESSING_INSTRUCTION_NODE=7;
    public static final short COMMENT_NODE=8;
    public static final short DOCUMENT_NODE=9;
    public static final short DOCUMENT_TYPE_NODE=10;
    public static final short DOCUMENT_FRAGMENT_NODE=11;
    public static final short NOTATION_NODE=12;
    public static final short NAMESPACE_NODE=13;
    public static final short NTYPES=14;
    // ========= DTM Implementation Control Functions. ==============
    // %TBD% RETIRED -- do via setFeature if needed. Remove from impls.
    // public void setParseBlockSize(int blockSizeSuggestion);

    public void setFeature(String featureId,boolean state);

    public void setProperty(String property,Object value);
    // ========= Document Navigation Functions =========

    public DTMAxisTraverser getAxisTraverser(final int axis);

    public DTMAxisIterator getAxisIterator(final int axis);

    public DTMAxisIterator getTypedAxisIterator(final int axis,final int type);

    public boolean hasChildNodes(int nodeHandle);

    public int getFirstChild(int nodeHandle);

    public int getLastChild(int nodeHandle);

    public int getAttributeNode(int elementHandle,String namespaceURI,
                                String name);

    public int getFirstAttribute(int nodeHandle);

    public int getFirstNamespaceNode(int nodeHandle,boolean inScope);

    public int getNextSibling(int nodeHandle);

    public int getPreviousSibling(int nodeHandle);

    public int getNextAttribute(int nodeHandle);

    public int getNextNamespaceNode(int baseHandle,int namespaceHandle,
                                    boolean inScope);

    public int getParent(int nodeHandle);

    public int getDocument();

    public int getOwnerDocument(int nodeHandle);

    public int getDocumentRoot(int nodeHandle);

    public XMLString getStringValue(int nodeHandle);

    public int getStringValueChunkCount(int nodeHandle);

    public char[] getStringValueChunk(int nodeHandle,int chunkIndex,
                                      int[] startAndLen);

    public int getExpandedTypeID(int nodeHandle);

    public int getExpandedTypeID(String namespace,String localName,int type);

    public String getLocalNameFromExpandedNameID(int ExpandedNameID);

    public String getNamespaceFromExpandedNameID(int ExpandedNameID);

    public String getNodeName(int nodeHandle);

    public String getNodeNameX(int nodeHandle);

    public String getLocalName(int nodeHandle);

    public String getPrefix(int nodeHandle);

    public String getNamespaceURI(int nodeHandle);

    public String getNodeValue(int nodeHandle);

    public short getNodeType(int nodeHandle);

    public short getLevel(int nodeHandle);
    // ============== Document query functions ==============

    public boolean isSupported(String feature,String version);

    public String getDocumentBaseURI();

    public void setDocumentBaseURI(String baseURI);

    public String getDocumentSystemIdentifier(int nodeHandle);

    public String getDocumentEncoding(int nodeHandle);

    public String getDocumentStandalone(int nodeHandle);

    public String getDocumentVersion(int documentHandle);

    public boolean getDocumentAllDeclarationsProcessed();

    public String getDocumentTypeDeclarationSystemIdentifier();

    public String getDocumentTypeDeclarationPublicIdentifier();

    public int getElementById(String elementId);

    public String getUnparsedEntityURI(String name);
    // ============== Boolean methods ================

    public boolean supportsPreStripping();

    public boolean isNodeAfter(int firstNodeHandle,int secondNodeHandle);

    public boolean isCharacterElementContentWhitespace(int nodeHandle);

    public boolean isDocumentAllDeclarationsProcessed(int documentHandle);

    public boolean isAttributeSpecified(int attributeHandle);
    // ========== Direct SAX Dispatch, for optimization purposes ========

    public void dispatchCharactersEvents(
            int nodeHandle,org.xml.sax.ContentHandler ch,boolean normalize)
            throws org.xml.sax.SAXException;

    public void dispatchToEvents(int nodeHandle,org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException;

    public org.w3c.dom.Node getNode(int nodeHandle);
    // ==== Construction methods (may not be supported by some implementations!) =====
    // %REVIEW% What response occurs if not supported?

    public boolean needsTwoThreads();
    // %REVIEW% Do these appends make any sense, should we support a
    // wider set of methods (like the "append" methods in the
    // current DTMDocumentImpl draft), or should we just support SAX
    // listener interfaces?  Should it be a separate interface to
    // make that distinction explicit?

    public org.xml.sax.ContentHandler getContentHandler();

    public org.xml.sax.ext.LexicalHandler getLexicalHandler();

    public org.xml.sax.EntityResolver getEntityResolver();

    public org.xml.sax.DTDHandler getDTDHandler();

    public org.xml.sax.ErrorHandler getErrorHandler();

    public org.xml.sax.ext.DeclHandler getDeclHandler();

    public void appendChild(int newChild,boolean clone,boolean cloneDepth);

    public void appendTextChild(String str);

    public SourceLocator getSourceLocatorFor(int node);

    public void documentRegistration();

    public void documentRelease();

    public void migrateTo(DTMManager manager);
}
