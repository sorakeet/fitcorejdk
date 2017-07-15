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
 * $Id: SAX2RTFDTM.java,v 1.2.4.1 2005/09/15 08:15:13 suresh_emailid Exp $
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
 * $Id: SAX2RTFDTM.java,v 1.2.4.1 2005/09/15 08:15:13 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref.sax2dtm;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMManager;
import com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import com.sun.org.apache.xml.internal.utils.IntStack;
import com.sun.org.apache.xml.internal.utils.IntVector;
import com.sun.org.apache.xml.internal.utils.StringVector;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;

public class SAX2RTFDTM extends SAX2DTM{
    private static final boolean DEBUG=false;
    IntStack mark_size=new IntStack();
    IntStack mark_data_size=new IntStack();
    IntStack mark_char_size=new IntStack();
    IntStack mark_doq_size=new IntStack();
    IntStack mark_nsdeclset_size=new IntStack();
    IntStack mark_nsdeclelem_size=new IntStack();
    int m_emptyNodeCount;
    int m_emptyNSDeclSetCount;
    int m_emptyNSDeclSetElemsCount;
    int m_emptyDataCount;
    int m_emptyCharsCount;
    int m_emptyDataQNCount;
    private int m_currentDocumentNode=NULL;

    public SAX2RTFDTM(DTMManager mgr,Source source,int dtmIdentity,
                      DTMWSFilter whiteSpaceFilter,
                      XMLStringFactory xstringfactory,
                      boolean doIndexing){
        super(mgr,source,dtmIdentity,whiteSpaceFilter,
                xstringfactory,doIndexing);
        // NEVER track source locators for RTFs; they aren't meaningful. I think.
        // (If we did track them, we'd need to tail-prune these too.)
        //com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl.m_source_location;
        m_useSourceLocationProperty=false;
        m_sourceSystemId=(m_useSourceLocationProperty)?new StringVector()
                :null;
        m_sourceLine=(m_useSourceLocationProperty)?new IntVector():null;
        m_sourceColumn=(m_useSourceLocationProperty)?new IntVector():null;
        // Record initial sizes of fields that are pushed and restored
        // for RTF tail-pruning.  More entries can be popped than pushed, so
        // we need this to mark the primordial state of the DTM.
        m_emptyNodeCount=m_size;
        m_emptyNSDeclSetCount=(m_namespaceDeclSets==null)
                ?0:m_namespaceDeclSets.size();
        m_emptyNSDeclSetElemsCount=(m_namespaceDeclSetElements==null)
                ?0:m_namespaceDeclSetElements.size();
        m_emptyDataCount=m_data.size();
        m_emptyCharsCount=m_chars.size();
        m_emptyDataQNCount=m_dataOrQName.size();
    }

    public int getDocument(){
        return makeNodeHandle(m_currentDocumentNode);
    }

    public int getDocumentRoot(int nodeHandle){
        for(int id=makeNodeIdentity(nodeHandle);id!=NULL;id=_parent(id)){
            if(_type(id)==DTM.DOCUMENT_NODE){
                return makeNodeHandle(id);
            }
        }
        return DTM.NULL; // Safety net; should never happen
    }

    protected int _documentRoot(int nodeIdentifier){
        if(nodeIdentifier==NULL) return NULL;
        for(int parent=_parent(nodeIdentifier);
            parent!=NULL;
            nodeIdentifier=parent,parent=_parent(nodeIdentifier))
            ;
        return nodeIdentifier;
    }

    public void startDocument() throws SAXException{
        // Re-initialize the tree append process
        m_endDocumentOccured=false;
        m_prefixMappings=new java.util.Vector();
        m_contextIndexes=new IntStack();
        m_parents=new IntStack();
        m_currentDocumentNode=m_size;
        super.startDocument();
    }

    public void endDocument() throws SAXException{
        charactersFlush();
        m_nextsib.setElementAt(NULL,m_currentDocumentNode);
        if(m_firstch.elementAt(m_currentDocumentNode)==NOTPROCESSED)
            m_firstch.setElementAt(NULL,m_currentDocumentNode);
        if(DTM.NULL!=m_previous)
            m_nextsib.setElementAt(DTM.NULL,m_previous);
        m_parents=null;
        m_prefixMappings=null;
        m_contextIndexes=null;
        m_currentDocumentNode=NULL; // no longer open
        m_endDocumentOccured=true;
    }

    public void pushRewindMark(){
        if(m_indexing||m_elemIndexes!=null)
            throw new NullPointerException("Coding error; Don't try to mark/rewind an indexed DTM");
        // Values from DTMDefaultBase
        // %REVIEW% Can the namespace stack sizes ever differ? If not, save space!
        mark_size.push(m_size);
        mark_nsdeclset_size.push((m_namespaceDeclSets==null)
                ?0
                :m_namespaceDeclSets.size());
        mark_nsdeclelem_size.push((m_namespaceDeclSetElements==null)
                ?0
                :m_namespaceDeclSetElements.size());
        // Values from SAX2DTM
        mark_data_size.push(m_data.size());
        mark_char_size.push(m_chars.size());
        mark_doq_size.push(m_dataOrQName.size());
    }

    public boolean popRewindMark(){
        boolean top=mark_size.empty();
        m_size=top?m_emptyNodeCount:mark_size.pop();
        m_exptype.setSize(m_size);
        m_firstch.setSize(m_size);
        m_nextsib.setSize(m_size);
        m_prevsib.setSize(m_size);
        m_parent.setSize(m_size);
        m_elemIndexes=null;
        int ds=top?m_emptyNSDeclSetCount:mark_nsdeclset_size.pop();
        if(m_namespaceDeclSets!=null){
            m_namespaceDeclSets.setSize(ds);
        }
        int ds1=top?m_emptyNSDeclSetElemsCount:mark_nsdeclelem_size.pop();
        if(m_namespaceDeclSetElements!=null){
            m_namespaceDeclSetElements.setSize(ds1);
        }
        // Values from SAX2DTM - m_data always has a reserved entry
        m_data.setSize(top?m_emptyDataCount:mark_data_size.pop());
        m_chars.setLength(top?m_emptyCharsCount:mark_char_size.pop());
        m_dataOrQName.setSize(top?m_emptyDataQNCount:mark_doq_size.pop());
        // Return true iff DTM now empty
        return m_size==0;
    }

    public boolean isTreeIncomplete(){
        return !m_endDocumentOccured;
    }
}
