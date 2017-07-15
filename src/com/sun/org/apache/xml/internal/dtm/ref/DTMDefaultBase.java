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
 * $Id: DTMDefaultBase.java,v 1.3 2005/09/28 13:48:52 pvedula Exp $
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
 * $Id: DTMDefaultBase.java,v 1.3 2005/09/28 13:48:52 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.*;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import com.sun.org.apache.xml.internal.utils.BoolStack;
import com.sun.org.apache.xml.internal.utils.SuballocatedIntVector;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;

import javax.xml.transform.Source;
import java.io.*;
import java.util.Vector;

public abstract class DTMDefaultBase implements DTM{
    // This constant is likely to be removed in the future. Use the
    // getDocument() method instead of ROOTNODE to get at the root
    // node of a DTM.
    public static final int ROOTNODE=0;
    public static final int DEFAULT_BLOCKSIZE=512;  // favor small docs.
    public static final int DEFAULT_NUMBLOCKS=32;
    public static final int DEFAULT_NUMBLOCKS_SMALL=4;
    //protected final int m_blocksize;
    protected static final int NOTPROCESSED=DTM.NULL-1;
    static final boolean JJK_DEBUG=false;
    public DTMManager m_mgr;
    protected int m_size=0;
    protected SuballocatedIntVector m_exptype;
    protected SuballocatedIntVector m_firstch;
    protected SuballocatedIntVector m_nextsib;
    protected SuballocatedIntVector m_prevsib;
    protected SuballocatedIntVector m_parent;
    protected Vector m_namespaceDeclSets=null;
    protected SuballocatedIntVector m_namespaceDeclSetElements=null;
    protected int[][][] m_elemIndexes;
    protected DTMManagerDefault m_mgrDefault=null;
    protected SuballocatedIntVector m_dtmIdent;
    //protected final static int m_mask = DTMManager.IDENT_NODE_DEFAULT;
    protected String m_documentBaseURI;
    protected DTMWSFilter m_wsfilter;
    protected boolean m_shouldStripWS=false;
    protected BoolStack m_shouldStripWhitespaceStack;
    protected XMLStringFactory m_xstrf;
    protected ExpandedNameTable m_expandedNameTable;
    protected boolean m_indexing;
    protected DTMAxisTraverser[] m_traversers;
    private Vector m_namespaceLists=null;  // on demand

    public DTMDefaultBase(DTMManager mgr,Source source,int dtmIdentity,
                          DTMWSFilter whiteSpaceFilter,
                          XMLStringFactory xstringfactory,boolean doIndexing){
        this(mgr,source,dtmIdentity,whiteSpaceFilter,xstringfactory,
                doIndexing,DEFAULT_BLOCKSIZE,true,false);
    }

    public DTMDefaultBase(DTMManager mgr,Source source,int dtmIdentity,
                          DTMWSFilter whiteSpaceFilter,
                          XMLStringFactory xstringfactory,boolean doIndexing,
                          int blocksize,boolean usePrevsib,
                          boolean newNameTable){
        // Use smaller sizes for the internal node arrays if the block size
        // is small.
        int numblocks;
        if(blocksize<=64){
            numblocks=DEFAULT_NUMBLOCKS_SMALL;
            m_dtmIdent=new SuballocatedIntVector(4,1);
        }else{
            numblocks=DEFAULT_NUMBLOCKS;
            m_dtmIdent=new SuballocatedIntVector(32);
        }
        m_exptype=new SuballocatedIntVector(blocksize,numblocks);
        m_firstch=new SuballocatedIntVector(blocksize,numblocks);
        m_nextsib=new SuballocatedIntVector(blocksize,numblocks);
        m_parent=new SuballocatedIntVector(blocksize,numblocks);
        // Only create the m_prevsib array if the usePrevsib flag is true.
        // Some DTM implementations (e.g. SAXImpl) do not need this array.
        // We can save the time to build it in those cases.
        if(usePrevsib)
            m_prevsib=new SuballocatedIntVector(blocksize,numblocks);
        m_mgr=mgr;
        if(mgr instanceof DTMManagerDefault)
            m_mgrDefault=(DTMManagerDefault)mgr;
        m_documentBaseURI=(null!=source)?source.getSystemId():null;
        m_dtmIdent.setElementAt(dtmIdentity,0);
        m_wsfilter=whiteSpaceFilter;
        m_xstrf=xstringfactory;
        m_indexing=doIndexing;
        if(doIndexing){
            m_expandedNameTable=new ExpandedNameTable();
        }else{
            // Note that this fails if we aren't talking to an instance of
            // DTMManagerDefault
            m_expandedNameTable=m_mgrDefault.getExpandedNameTable(this);
        }
        if(null!=whiteSpaceFilter){
            m_shouldStripWhitespaceStack=new BoolStack();
            pushShouldStripWhitespace(false);
        }
    }

    protected void pushShouldStripWhitespace(boolean shouldStrip){
        m_shouldStripWS=shouldStrip;
        if(null!=m_shouldStripWhitespaceStack)
            m_shouldStripWhitespaceStack.push(shouldStrip);
    }

    protected void indexNode(int expandedTypeID,int identity){
        ExpandedNameTable ent=m_expandedNameTable;
        short type=ent.getType(expandedTypeID);
        if(DTM.ELEMENT_NODE==type){
            int namespaceID=ent.getNamespaceID(expandedTypeID);
            int localNameID=ent.getLocalNameID(expandedTypeID);
            ensureSizeOfIndex(namespaceID,localNameID);
            int[] index=m_elemIndexes[namespaceID][localNameID];
            index[index[0]]=identity;
            index[0]++;
        }
    }

    protected void ensureSizeOfIndex(int namespaceID,int LocalNameID){
        if(null==m_elemIndexes){
            m_elemIndexes=new int[namespaceID+20][][];
        }else if(m_elemIndexes.length<=namespaceID){
            int[][][] indexes=m_elemIndexes;
            m_elemIndexes=new int[namespaceID+20][][];
            System.arraycopy(indexes,0,m_elemIndexes,0,indexes.length);
        }
        int[][] localNameIndex=m_elemIndexes[namespaceID];
        if(null==localNameIndex){
            localNameIndex=new int[LocalNameID+100][];
            m_elemIndexes[namespaceID]=localNameIndex;
        }else if(localNameIndex.length<=LocalNameID){
            int[][] indexes=localNameIndex;
            localNameIndex=new int[LocalNameID+100][];
            System.arraycopy(indexes,0,localNameIndex,0,indexes.length);
            m_elemIndexes[namespaceID]=localNameIndex;
        }
        int[] elemHandles=localNameIndex[LocalNameID];
        if(null==elemHandles){
            elemHandles=new int[128];
            localNameIndex[LocalNameID]=elemHandles;
            elemHandles[0]=1;
        }else if(elemHandles.length<=elemHandles[0]+1){
            int[] indexes=elemHandles;
            elemHandles=new int[elemHandles[0]+1024];
            System.arraycopy(indexes,0,elemHandles,0,indexes.length);
            localNameIndex[LocalNameID]=elemHandles;
        }
    }

    int findElementFromIndex(int nsIndex,int lnIndex,int firstPotential){
        int[][][] indexes=m_elemIndexes;
        if(null!=indexes&&nsIndex<indexes.length){
            int[][] lnIndexs=indexes[nsIndex];
            if(null!=lnIndexs&&lnIndex<lnIndexs.length){
                int[] elems=lnIndexs[lnIndex];
                if(null!=elems){
                    int pos=findGTE(elems,1,elems[0],firstPotential);
                    if(pos>-1){
                        return elems[pos];
                    }
                }
            }
        }
        return NOTPROCESSED;
    }

    protected int findGTE(int[] list,int start,int len,int value){
        int low=start;
        int high=start+(len-1);
        int end=high;
        while(low<=high){
            int mid=(low+high)/2;
            int c=list[mid];
            if(c>value)
                high=mid-1;
            else if(c<value)
                low=mid+1;
            else
                return mid;
        }
        return (low<=end&&list[low]>value)?low:-1;
    }

    protected abstract int getNumberOfNodes();
//    /**
//     * Ensure that the size of the information arrays can hold another entry
//     * at the given index.
//     *
//     * @param index On exit from this function, the information arrays sizes must be
//     * at least index+1.
//     */
//    protected void ensureSize(int index)
//    {
//        // We've cut over to Suballocated*Vector, which are self-sizing.
//    }

    public void dumpDTM(OutputStream os){
        try{
            if(os==null){
                File f=new File("DTMDump"+((Object)this).hashCode()+".txt");
                System.err.println("Dumping... "+f.getAbsolutePath());
                os=new FileOutputStream(f);
            }
            PrintStream ps=new PrintStream(os);
            while(nextNode()){
            }
            int nRecords=m_size;
            ps.println("Total nodes: "+nRecords);
            for(int index=0;index<nRecords;++index){
                int i=makeNodeHandle(index);
                ps.println("=========== index="+index+" handle="+i+" ===========");
                ps.println("NodeName: "+getNodeName(i));
                ps.println("NodeNameX: "+getNodeNameX(i));
                ps.println("LocalName: "+getLocalName(i));
                ps.println("NamespaceURI: "+getNamespaceURI(i));
                ps.println("Prefix: "+getPrefix(i));
                int exTypeID=_exptype(index);
                ps.println("Expanded Type ID: "
                        +Integer.toHexString(exTypeID));
                int type=_type(index);
                String typestring;
                switch(type){
                    case DTM.ATTRIBUTE_NODE:
                        typestring="ATTRIBUTE_NODE";
                        break;
                    case DTM.CDATA_SECTION_NODE:
                        typestring="CDATA_SECTION_NODE";
                        break;
                    case DTM.COMMENT_NODE:
                        typestring="COMMENT_NODE";
                        break;
                    case DTM.DOCUMENT_FRAGMENT_NODE:
                        typestring="DOCUMENT_FRAGMENT_NODE";
                        break;
                    case DTM.DOCUMENT_NODE:
                        typestring="DOCUMENT_NODE";
                        break;
                    case DTM.DOCUMENT_TYPE_NODE:
                        typestring="DOCUMENT_NODE";
                        break;
                    case DTM.ELEMENT_NODE:
                        typestring="ELEMENT_NODE";
                        break;
                    case DTM.ENTITY_NODE:
                        typestring="ENTITY_NODE";
                        break;
                    case DTM.ENTITY_REFERENCE_NODE:
                        typestring="ENTITY_REFERENCE_NODE";
                        break;
                    case DTM.NAMESPACE_NODE:
                        typestring="NAMESPACE_NODE";
                        break;
                    case DTM.NOTATION_NODE:
                        typestring="NOTATION_NODE";
                        break;
                    case DTM.NULL:
                        typestring="NULL";
                        break;
                    case DTM.PROCESSING_INSTRUCTION_NODE:
                        typestring="PROCESSING_INSTRUCTION_NODE";
                        break;
                    case DTM.TEXT_NODE:
                        typestring="TEXT_NODE";
                        break;
                    default:
                        typestring="Unknown!";
                        break;
                }
                ps.println("Type: "+typestring);
                int firstChild=_firstch(index);
                if(DTM.NULL==firstChild)
                    ps.println("First child: DTM.NULL");
                else if(NOTPROCESSED==firstChild)
                    ps.println("First child: NOTPROCESSED");
                else
                    ps.println("First child: "+firstChild);
                if(m_prevsib!=null){
                    int prevSibling=_prevsib(index);
                    if(DTM.NULL==prevSibling)
                        ps.println("Prev sibling: DTM.NULL");
                    else if(NOTPROCESSED==prevSibling)
                        ps.println("Prev sibling: NOTPROCESSED");
                    else
                        ps.println("Prev sibling: "+prevSibling);
                }
                int nextSibling=_nextsib(index);
                if(DTM.NULL==nextSibling)
                    ps.println("Next sibling: DTM.NULL");
                else if(NOTPROCESSED==nextSibling)
                    ps.println("Next sibling: NOTPROCESSED");
                else
                    ps.println("Next sibling: "+nextSibling);
                int parent=_parent(index);
                if(DTM.NULL==parent)
                    ps.println("Parent: DTM.NULL");
                else if(NOTPROCESSED==parent)
                    ps.println("Parent: NOTPROCESSED");
                else
                    ps.println("Parent: "+parent);
                int level=_level(index);
                ps.println("Level: "+level);
                ps.println("Node Value: "+getNodeValue(i));
                ps.println("String Value: "+getStringValue(i));
            }
        }catch(IOException ioe){
            ioe.printStackTrace(System.err);
            throw new RuntimeException(ioe.getMessage());
        }
    }

    protected abstract boolean nextNode();

    protected short _type(int identity){
        int info=_exptype(identity);
        if(NULL!=info)
            return m_expandedNameTable.getType(info);
        else
            return NULL;
    }

    protected int _exptype(int identity){
        if(identity==DTM.NULL)
            return NULL;
        // Reorganized test and loop into single flow
        // Tiny performance improvement, saves a few bytes of code, clearer.
        // %OPT% Other internal getters could be treated simliarly
        while(identity>=m_size){
            if(!nextNode()&&identity>=m_size)
                return NULL;
        }
        return m_exptype.elementAt(identity);
    }

    protected int _level(int identity){
        while(identity>=m_size){
            boolean isMore=nextNode();
            if(!isMore&&identity>=m_size)
                return NULL;
        }
        int i=0;
        while(NULL!=(identity=_parent(identity)))
            ++i;
        return i;
    }

    protected int _firstch(int identity){
        // Boiler-plate code for each of the _xxx functions, except for the array.
        int info=(identity>=m_size)?NOTPROCESSED:m_firstch.elementAt(identity);
        // Check to see if the information requested has been processed, and,
        // if not, advance the iterator until we the information has been
        // processed.
        while(info==NOTPROCESSED){
            boolean isMore=nextNode();
            if(identity>=m_size&&!isMore)
                return NULL;
            else{
                info=m_firstch.elementAt(identity);
                if(info==NOTPROCESSED&&!isMore)
                    return NULL;
            }
        }
        return info;
    }

    protected int _nextsib(int identity){
        // Boiler-plate code for each of the _xxx functions, except for the array.
        int info=(identity>=m_size)?NOTPROCESSED:m_nextsib.elementAt(identity);
        // Check to see if the information requested has been processed, and,
        // if not, advance the iterator until we the information has been
        // processed.
        while(info==NOTPROCESSED){
            boolean isMore=nextNode();
            if(identity>=m_size&&!isMore)
                return NULL;
            else{
                info=m_nextsib.elementAt(identity);
                if(info==NOTPROCESSED&&!isMore)
                    return NULL;
            }
        }
        return info;
    }

    protected int _prevsib(int identity){
        if(identity<m_size)
            return m_prevsib.elementAt(identity);
        // Check to see if the information requested has been processed, and,
        // if not, advance the iterator until we the information has been
        // processed.
        while(true){
            boolean isMore=nextNode();
            if(identity>=m_size&&!isMore)
                return NULL;
            else if(identity<m_size)
                return m_prevsib.elementAt(identity);
        }
    }

    protected int _parent(int identity){
        if(identity<m_size)
            return m_parent.elementAt(identity);
        // Check to see if the information requested has been processed, and,
        // if not, advance the iterator until we the information has been
        // processed.
        while(true){
            boolean isMore=nextNode();
            if(identity>=m_size&&!isMore)
                return NULL;
            else if(identity<m_size)
                return m_parent.elementAt(identity);
        }
    }
    // ========= DTM Implementation Control Functions. ==============

    final public int makeNodeHandle(int nodeIdentity){
        if(NULL==nodeIdentity) return NULL;
        if(JJK_DEBUG&&nodeIdentity>DTMManager.IDENT_NODE_DEFAULT)
            System.err.println("GONK! (only useful in limited situations)");
        return m_dtmIdent.elementAt(nodeIdentity>>>DTMManager.IDENT_DTM_NODE_BITS)
                +(nodeIdentity&DTMManager.IDENT_NODE_DEFAULT);
    }
    // ========= Document Navigation Functions =========

    public String dumpNode(int nodeHandle){
        if(nodeHandle==DTM.NULL)
            return "[null]";
        String typestring;
        switch(getNodeType(nodeHandle)){
            case DTM.ATTRIBUTE_NODE:
                typestring="ATTR";
                break;
            case DTM.CDATA_SECTION_NODE:
                typestring="CDATA";
                break;
            case DTM.COMMENT_NODE:
                typestring="COMMENT";
                break;
            case DTM.DOCUMENT_FRAGMENT_NODE:
                typestring="DOC_FRAG";
                break;
            case DTM.DOCUMENT_NODE:
                typestring="DOC";
                break;
            case DTM.DOCUMENT_TYPE_NODE:
                typestring="DOC_TYPE";
                break;
            case DTM.ELEMENT_NODE:
                typestring="ELEMENT";
                break;
            case DTM.ENTITY_NODE:
                typestring="ENTITY";
                break;
            case DTM.ENTITY_REFERENCE_NODE:
                typestring="ENT_REF";
                break;
            case DTM.NAMESPACE_NODE:
                typestring="NAMESPACE";
                break;
            case DTM.NOTATION_NODE:
                typestring="NOTATION";
                break;
            case DTM.NULL:
                typestring="null";
                break;
            case DTM.PROCESSING_INSTRUCTION_NODE:
                typestring="PI";
                break;
            case DTM.TEXT_NODE:
                typestring="TEXT";
                break;
            default:
                typestring="Unknown!";
                break;
        }
        return "["+nodeHandle+": "+typestring+
                "(0x"+Integer.toHexString(getExpandedTypeID(nodeHandle))+") "+
                getNodeNameX(nodeHandle)+" {"+getNamespaceURI(nodeHandle)+"}"+
                "=\""+getNodeValue(nodeHandle)+"\"]";
    }

    public void setFeature(String featureId,boolean state){
    }

    public boolean hasChildNodes(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int firstChild=_firstch(identity);
        return firstChild!=DTM.NULL;
    }

    final public int makeNodeIdentity(int nodeHandle){
        if(NULL==nodeHandle) return NULL;
        if(m_mgrDefault!=null){
            // Optimization: use the DTMManagerDefault's fast DTMID-to-offsets
            // table.  I'm not wild about this solution but this operation
            // needs need extreme speed.
            int whichDTMindex=nodeHandle>>>DTMManager.IDENT_DTM_NODE_BITS;
            // %REVIEW% Wish I didn't have to perform the pre-test, but
            // someone is apparently asking DTMs whether they contain nodes
            // which really don't belong to them. That's probably a bug
            // which should be fixed, but until it is:
            if(m_mgrDefault.m_dtms[whichDTMindex]!=this)
                return NULL;
            else
                return
                        m_mgrDefault.m_dtm_offsets[whichDTMindex]
                                |(nodeHandle&DTMManager.IDENT_NODE_DEFAULT);
        }
        int whichDTMid=m_dtmIdent.indexOf(nodeHandle&DTMManager.IDENT_DTM_DEFAULT);
        return (whichDTMid==NULL)
                ?NULL
                :(whichDTMid<<DTMManager.IDENT_DTM_NODE_BITS)
                +(nodeHandle&DTMManager.IDENT_NODE_DEFAULT);
    }

    public int getFirstChild(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int firstChild=_firstch(identity);
        return makeNodeHandle(firstChild);
    }

    public int getLastChild(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int child=_firstch(identity);
        int lastChild=DTM.NULL;
        while(child!=DTM.NULL){
            lastChild=child;
            child=_nextsib(child);
        }
        return makeNodeHandle(lastChild);
    }

    public abstract int getAttributeNode(int nodeHandle,String namespaceURI,
                                         String name);

    public int getFirstAttribute(int nodeHandle){
        int nodeID=makeNodeIdentity(nodeHandle);
        return makeNodeHandle(getFirstAttributeIdentity(nodeID));
    }

    protected int getFirstAttributeIdentity(int identity){
        int type=_type(identity);
        if(DTM.ELEMENT_NODE==type){
            // Assume that attributes and namespaces immediately follow the element.
            while(DTM.NULL!=(identity=getNextNodeIdentity(identity))){
                // Assume this can not be null.
                type=_type(identity);
                if(type==DTM.ATTRIBUTE_NODE){
                    return identity;
                }else if(DTM.NAMESPACE_NODE!=type){
                    break;
                }
            }
        }
        return DTM.NULL;
    }

    protected abstract int getNextNodeIdentity(int identity);

    public int getFirstNamespaceNode(int nodeHandle,boolean inScope){
        if(inScope){
            int identity=makeNodeIdentity(nodeHandle);
            if(_type(identity)==DTM.ELEMENT_NODE){
                SuballocatedIntVector nsContext=findNamespaceContext(identity);
                if(nsContext==null||nsContext.size()<1)
                    return NULL;
                return nsContext.elementAt(0);
            }else
                return NULL;
        }else{
            // Assume that attributes and namespaces immediately
            // follow the element.
            //
            // %OPT% Would things be faster if all NS nodes were built
            // before all Attr nodes? Some costs at build time for 2nd
            // pass...
            int identity=makeNodeIdentity(nodeHandle);
            if(_type(identity)==DTM.ELEMENT_NODE){
                while(DTM.NULL!=(identity=getNextNodeIdentity(identity))){
                    int type=_type(identity);
                    if(type==DTM.NAMESPACE_NODE)
                        return makeNodeHandle(identity);
                    else if(DTM.ATTRIBUTE_NODE!=type)
                        break;
                }
                return NULL;
            }else
                return NULL;
        }
    }

    public int getNextSibling(int nodeHandle){
        if(nodeHandle==DTM.NULL)
            return DTM.NULL;
        return makeNodeHandle(_nextsib(makeNodeIdentity(nodeHandle)));
    }

    public int getPreviousSibling(int nodeHandle){
        if(nodeHandle==DTM.NULL)
            return DTM.NULL;
        if(m_prevsib!=null)
            return makeNodeHandle(_prevsib(makeNodeIdentity(nodeHandle)));
        else{
            // If the previous sibling array is not built, we get at
            // the previous sibling using the parent, firstch and
            // nextsib arrays.
            int nodeID=makeNodeIdentity(nodeHandle);
            int parent=_parent(nodeID);
            int node=_firstch(parent);
            int result=DTM.NULL;
            while(node!=nodeID){
                result=node;
                node=_nextsib(node);
            }
            return makeNodeHandle(result);
        }
    }

    public int getNextAttribute(int nodeHandle){
        int nodeID=makeNodeIdentity(nodeHandle);
        if(_type(nodeID)==DTM.ATTRIBUTE_NODE){
            return makeNodeHandle(getNextAttributeIdentity(nodeID));
        }
        return DTM.NULL;
    }

    public int getNextNamespaceNode(int baseHandle,int nodeHandle,
                                    boolean inScope){
        if(inScope){
            //Since we've been given the base, try direct lookup
            //(could look from nodeHandle but this is at least one
            //comparison/get-parent faster)
            //SuballocatedIntVector nsContext=findNamespaceContext(nodeHandle & m_mask);
            SuballocatedIntVector nsContext=findNamespaceContext(makeNodeIdentity(baseHandle));
            if(nsContext==null)
                return NULL;
            int i=1+nsContext.indexOf(nodeHandle);
            if(i<=0||i==nsContext.size())
                return NULL;
            return nsContext.elementAt(i);
        }else{
            // Assume that attributes and namespace nodes immediately follow the element.
            int identity=makeNodeIdentity(nodeHandle);
            while(DTM.NULL!=(identity=getNextNodeIdentity(identity))){
                int type=_type(identity);
                if(type==DTM.NAMESPACE_NODE){
                    return makeNodeHandle(identity);
                }else if(type!=DTM.ATTRIBUTE_NODE){
                    break;
                }
            }
        }
        return DTM.NULL;
    }

    public int getParent(int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        if(identity>0)
            return makeNodeHandle(_parent(identity));
        else
            return DTM.NULL;
    }

    public int getDocument(){
        return m_dtmIdent.elementAt(0); // makeNodeHandle(0)
    }

    public int getOwnerDocument(int nodeHandle){
        if(DTM.DOCUMENT_NODE==getNodeType(nodeHandle))
            return DTM.NULL;
        return getDocumentRoot(nodeHandle);
    }

    public int getDocumentRoot(int nodeHandle){
        return getManager().getDTM(nodeHandle).getDocument();
    }

    public abstract XMLString getStringValue(int nodeHandle);

    public int getStringValueChunkCount(int nodeHandle){
        // %TBD%
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED,null));//("getStringValueChunkCount not yet supported!");
        return 0;
    }

    public char[] getStringValueChunk(int nodeHandle,int chunkIndex,
                                      int[] startAndLen){
        // %TBD%
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED,null));//"getStringValueChunk not yet supported!");
        return null;
    }

    public int getExpandedTypeID(int nodeHandle){
        // %REVIEW% This _should_ only be null if someone asked the wrong DTM about the node...
        // which one would hope would never happen...
        int id=makeNodeIdentity(nodeHandle);
        if(id==NULL)
            return NULL;
        return _exptype(id);
    }

    public int getExpandedTypeID(String namespace,String localName,int type){
        ExpandedNameTable ent=m_expandedNameTable;
        return ent.getExpandedTypeID(namespace,localName,type);
    }

    public String getLocalNameFromExpandedNameID(int expandedNameID){
        return m_expandedNameTable.getLocalName(expandedNameID);
    }

    public String getNamespaceFromExpandedNameID(int expandedNameID){
        return m_expandedNameTable.getNamespace(expandedNameID);
    }

    public abstract String getNodeName(int nodeHandle);

    public String getNodeNameX(int nodeHandle){
        /** @todo: implement this com.sun.org.apache.xml.internal.dtm.DTMDefaultBase abstract method */
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED,null));//"Not yet supported!");
        return null;
    }

    public abstract String getLocalName(int nodeHandle);

    public abstract String getPrefix(int nodeHandle);

    public abstract String getNamespaceURI(int nodeHandle);

    public abstract String getNodeValue(int nodeHandle);

    public short getNodeType(int nodeHandle){
        if(nodeHandle==DTM.NULL)
            return DTM.NULL;
        return m_expandedNameTable.getType(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public short getLevel(int nodeHandle){
        // Apparently, the axis walker stuff requires levels to count from 1.
        int identity=makeNodeIdentity(nodeHandle);
        return (short)(_level(identity)+1);
    }

    public boolean isSupported(String feature,String version){
        // %TBD%
        return false;
    }

    public String getDocumentBaseURI(){
        return m_documentBaseURI;
    }

    public void setDocumentBaseURI(String baseURI){
        m_documentBaseURI=baseURI;
    }

    public String getDocumentSystemIdentifier(int nodeHandle){
        // %REVIEW%  OK? -sb
        return m_documentBaseURI;
    }

    public String getDocumentEncoding(int nodeHandle){
        // %REVIEW%  OK??  -sb
        return "UTF-8";
    }

    public String getDocumentStandalone(int nodeHandle){
        return null;
    }

    public String getDocumentVersion(int documentHandle){
        return null;
    }

    public boolean getDocumentAllDeclarationsProcessed(){
        // %REVIEW% OK?
        return true;
    }

    public abstract String getDocumentTypeDeclarationSystemIdentifier();
    // ============== Document query functions ==============

    public abstract String getDocumentTypeDeclarationPublicIdentifier();

    public abstract int getElementById(String elementId);

    public abstract String getUnparsedEntityURI(String name);

    public boolean supportsPreStripping(){
        return true;
    }

    public boolean isNodeAfter(int nodeHandle1,int nodeHandle2){
        // These return NULL if the node doesn't belong to this document.
        int index1=makeNodeIdentity(nodeHandle1);
        int index2=makeNodeIdentity(nodeHandle2);
        return index1!=NULL&&index2!=NULL&&index1<=index2;
    }

    public boolean isCharacterElementContentWhitespace(int nodeHandle){
        // %TBD%
        return false;
    }

    public boolean isDocumentAllDeclarationsProcessed(int documentHandle){
        return true;
    }

    public abstract boolean isAttributeSpecified(int attributeHandle);

    public abstract void dispatchCharactersEvents(
            int nodeHandle,org.xml.sax.ContentHandler ch,boolean normalize)
            throws org.xml.sax.SAXException;

    public abstract void dispatchToEvents(
            int nodeHandle,org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException;

    public org.w3c.dom.Node getNode(int nodeHandle){
        return new DTMNodeProxy(this,nodeHandle);
    }

    public void appendChild(int newChild,boolean clone,boolean cloneDepth){
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED,null));//"appendChild not yet supported!");
    }
    // ============== Boolean methods ================

    public void appendTextChild(String str){
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED,null));//"appendTextChild not yet supported!");
    }

    public void documentRegistration(){
    }

    public void documentRelease(){
    }

    public void migrateTo(DTMManager mgr){
        m_mgr=mgr;
        if(mgr instanceof DTMManagerDefault)
            m_mgrDefault=(DTMManagerDefault)mgr;
    }

    protected void error(String msg){
        throw new DTMException(msg);
    }
    // ========== Direct SAX Dispatch, for optimization purposes ========

    public DTMManager getManager(){
        return m_mgr;
    }

    public int getTypedFirstChild(int nodeHandle,int nodeType){
        int firstChild, eType;
        if(nodeType<DTM.NTYPES){
            for(firstChild=_firstch(makeNodeIdentity(nodeHandle));
                firstChild!=DTM.NULL;
                firstChild=_nextsib(firstChild)){
                eType=_exptype(firstChild);
                if(eType==nodeType
                        ||(eType>=DTM.NTYPES
                        &&m_expandedNameTable.getType(eType)==nodeType)){
                    return makeNodeHandle(firstChild);
                }
            }
        }else{
            for(firstChild=_firstch(makeNodeIdentity(nodeHandle));
                firstChild!=DTM.NULL;
                firstChild=_nextsib(firstChild)){
                if(_exptype(firstChild)==nodeType){
                    return makeNodeHandle(firstChild);
                }
            }
        }
        return DTM.NULL;
    }

    protected int getTypedAttribute(int nodeHandle,int attType){
        int type=getNodeType(nodeHandle);
        if(DTM.ELEMENT_NODE==type){
            int identity=makeNodeIdentity(nodeHandle);
            while(DTM.NULL!=(identity=getNextNodeIdentity(identity))){
                type=_type(identity);
                if(type==DTM.ATTRIBUTE_NODE){
                    if(_exptype(identity)==attType) return makeNodeHandle(identity);
                }else if(DTM.NAMESPACE_NODE!=type){
                    break;
                }
            }
        }
        return DTM.NULL;
    }
    // ==== Construction methods (may not be supported by some implementations!) =====

    public int getTypedNextSibling(int nodeHandle,int nodeType){
        if(nodeHandle==DTM.NULL)
            return DTM.NULL;
        int node=makeNodeIdentity(nodeHandle);
        int eType;
        while((node=_nextsib(node))!=DTM.NULL&&
                ((eType=_exptype(node))!=nodeType&&
                        m_expandedNameTable.getType(eType)!=nodeType)) ;
        //_type(node) != nodeType));
        return (node==DTM.NULL?DTM.NULL:makeNodeHandle(node));
    }

    protected int getNextAttributeIdentity(int identity){
        // Assume that attributes and namespace nodes immediately follow the element
        while(DTM.NULL!=(identity=getNextNodeIdentity(identity))){
            int type=_type(identity);
            if(type==DTM.ATTRIBUTE_NODE){
                return identity;
            }else if(type!=DTM.NAMESPACE_NODE){
                break;
            }
        }
        return DTM.NULL;
    }

    protected void declareNamespaceInContext(int elementNodeIndex,int namespaceNodeIndex){
        SuballocatedIntVector nsList=null;
        if(m_namespaceDeclSets==null){
            // First
            m_namespaceDeclSetElements=new SuballocatedIntVector(32);
            m_namespaceDeclSetElements.addElement(elementNodeIndex);
            m_namespaceDeclSets=new Vector();
            nsList=new SuballocatedIntVector(32);
            m_namespaceDeclSets.addElement(nsList);
        }else{
            // Most recent. May be -1 (none) if DTM was pruned.
            // %OPT% Is there a lastElement() method? Should there be?
            int last=m_namespaceDeclSetElements.size()-1;
            if(last>=0&&elementNodeIndex==m_namespaceDeclSetElements.elementAt(last)){
                nsList=(SuballocatedIntVector)m_namespaceDeclSets.elementAt(last);
            }
        }
        if(nsList==null){
            m_namespaceDeclSetElements.addElement(elementNodeIndex);
            SuballocatedIntVector inherited=
                    findNamespaceContext(_parent(elementNodeIndex));
            if(inherited!=null){
                // %OPT% Count-down might be faster, but debuggability may
                // be better this way, and if we ever decide we want to
                // keep this ordered by expanded-type...
                int isize=inherited.size();
                // Base the size of a new namespace list on the
                // size of the inherited list - but within reason!
                nsList=new SuballocatedIntVector(Math.max(Math.min(isize+16,2048),
                        32));
                for(int i=0;i<isize;++i){
                    nsList.addElement(inherited.elementAt(i));
                }
            }else{
                nsList=new SuballocatedIntVector(32);
            }
            m_namespaceDeclSets.addElement(nsList);
        }
        // Handle overwriting inherited.
        // %OPT% Keep sorted? (By expanded-name rather than by doc order...)
        // Downside: Would require insertElementAt if not found,
        // which has recopying costs. But these are generally short lists...
        int newEType=_exptype(namespaceNodeIndex);
        for(int i=nsList.size()-1;i>=0;--i){
            if(newEType==getExpandedTypeID(nsList.elementAt(i))){
                nsList.setElementAt(makeNodeHandle(namespaceNodeIndex),i);
                return;
            }
        }
        nsList.addElement(makeNodeHandle(namespaceNodeIndex));
    }

    protected SuballocatedIntVector findNamespaceContext(int elementNodeIndex){
        if(null!=m_namespaceDeclSetElements){
            // %OPT% Is binary-search really saving us a lot versus linear?
            // (... It may be, in large docs with many NS decls.)
            int wouldBeAt=findInSortedSuballocatedIntVector(m_namespaceDeclSetElements,
                    elementNodeIndex);
            if(wouldBeAt>=0) // Found it
                return (SuballocatedIntVector)m_namespaceDeclSets.elementAt(wouldBeAt);
            if(wouldBeAt==-1) // -1-wouldbeat == 0
                return null; // Not after anything; definitely not found
            // Not found, but we know where it should have been.
            // Search back until we find an ancestor or run out.
            wouldBeAt=-1-wouldBeAt;
            // Decrement wouldBeAt to find last possible ancestor
            int candidate=m_namespaceDeclSetElements.elementAt(--wouldBeAt);
            int ancestor=_parent(elementNodeIndex);
            // Special case: if the candidate is before the given node, and
            // is in the earliest possible position in the document, it
            // must have the namespace declarations we're interested in.
            if(wouldBeAt==0&&candidate<ancestor){
                int rootHandle=getDocumentRoot(makeNodeHandle(elementNodeIndex));
                int rootID=makeNodeIdentity(rootHandle);
                int uppermostNSCandidateID;
                if(getNodeType(rootHandle)==DTM.DOCUMENT_NODE){
                    int ch=_firstch(rootID);
                    uppermostNSCandidateID=(ch!=DTM.NULL)?ch:rootID;
                }else{
                    uppermostNSCandidateID=rootID;
                }
                if(candidate==uppermostNSCandidateID){
                    return (SuballocatedIntVector)m_namespaceDeclSets.elementAt(wouldBeAt);
                }
            }
            while(wouldBeAt>=0&&ancestor>0){
                if(candidate==ancestor){
                    // Found ancestor in list
                    return (SuballocatedIntVector)m_namespaceDeclSets.elementAt(wouldBeAt);
                }else if(candidate<ancestor){
                    // Too deep in tree
                    do{
                        ancestor=_parent(ancestor);
                    }while(candidate<ancestor);
                }else if(wouldBeAt>0){
                    // Too late in list
                    candidate=m_namespaceDeclSetElements.elementAt(--wouldBeAt);
                }else
                    break;
            }
        }
        return null; // No namespaces known at this node
    }

    protected int findInSortedSuballocatedIntVector(SuballocatedIntVector vector,int lookfor){
        // Binary search
        int i=0;
        if(vector!=null){
            int first=0;
            int last=vector.size()-1;
            while(first<=last){
                i=(first+last)/2;
                int test=lookfor-vector.elementAt(i);
                if(test==0){
                    return i; // Name found
                }else if(test<0){
                    last=i-1; // looked too late
                }else{
                    first=i+1; // looked ot early
                }
            }
            if(first>i){
                i=first; // Clean up at loop end
            }
        }
        return -1-i; // not-found has to be encoded.
    }

    public int getNamespaceType(final int nodeHandle){
        int identity=makeNodeIdentity(nodeHandle);
        int expandedNameID=_exptype(identity);
        return m_expandedNameTable.getNamespaceID(expandedNameID);
    }

    public int getNodeIdent(int nodeHandle){
        /**if (nodeHandle != DTM.NULL)
         return nodeHandle & m_mask;
         else
         return DTM.NULL;*/
        return makeNodeIdentity(nodeHandle);
    }

    public int getNodeHandle(int nodeId){
        /**if (nodeId != DTM.NULL)
         return nodeId | m_dtmIdent;
         else
         return DTM.NULL;*/
        return makeNodeHandle(nodeId);
    }

    protected boolean getShouldStripWhitespace(){
        return m_shouldStripWS;
    }

    protected void setShouldStripWhitespace(boolean shouldStrip){
        m_shouldStripWS=shouldStrip;
        if(null!=m_shouldStripWhitespaceStack)
            m_shouldStripWhitespaceStack.setTop(shouldStrip);
    }

    protected void popShouldStripWhitespace(){
        if(null!=m_shouldStripWhitespaceStack)
            m_shouldStripWS=m_shouldStripWhitespaceStack.popAndTop();
    }

    public SuballocatedIntVector getDTMIDs(){
        if(m_mgr==null) return null;
        return m_dtmIdent;
    }
}
