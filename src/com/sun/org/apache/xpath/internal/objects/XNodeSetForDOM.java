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
 * $Id: XNodeSetForDOM.java,v 1.2.4.1 2005/09/14 20:34:46 jeffsuttor Exp $
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
 * $Id: XNodeSetForDOM.java,v 1.2.4.1 2005/09/14 20:34:46 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xml.internal.dtm.DTMManager;
import com.sun.org.apache.xpath.internal.NodeSetDTM;
import com.sun.org.apache.xpath.internal.XPathContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public class XNodeSetForDOM extends XNodeSet{
    static final long serialVersionUID=-8396190713754624640L;
    Object m_origObj;

    public XNodeSetForDOM(Node node,DTMManager dtmMgr){
        m_dtmMgr=dtmMgr;
        m_origObj=node;
        int dtmHandle=dtmMgr.getDTMHandleFromNode(node);
        setObject(new NodeSetDTM(dtmMgr));
        ((NodeSetDTM)m_obj).addNode(dtmHandle);
    }

    public XNodeSetForDOM(XNodeSet val){
        super(val);
        if(val instanceof XNodeSetForDOM)
            m_origObj=((XNodeSetForDOM)val).m_origObj;
    }

    public XNodeSetForDOM(NodeList nodeList,XPathContext xctxt){
        m_dtmMgr=xctxt.getDTMManager();
        m_origObj=nodeList;
        // JKESS 20020514: Longer-term solution is to force
        // folks to request length through an accessor, so we can defer this
        // retrieval... but that requires an API change.
        // m_obj=new com.sun.org.apache.xpath.internal.NodeSetDTM(nodeList, xctxt);
        NodeSetDTM nsdtm=new NodeSetDTM(nodeList,xctxt);
        m_last=nsdtm.getLength();
        setObject(nsdtm);
    }

    public XNodeSetForDOM(NodeIterator nodeIter,XPathContext xctxt){
        m_dtmMgr=xctxt.getDTMManager();
        m_origObj=nodeIter;
        // JKESS 20020514: Longer-term solution is to force
        // folks to request length through an accessor, so we can defer this
        // retrieval... but that requires an API change.
        // m_obj = new com.sun.org.apache.xpath.internal.NodeSetDTM(nodeIter, xctxt);
        NodeSetDTM nsdtm=new NodeSetDTM(nodeIter,xctxt);
        m_last=nsdtm.getLength();
        setObject(nsdtm);
    }

    public Object object(){
        return m_origObj;
    }

    public NodeIterator nodeset() throws javax.xml.transform.TransformerException{
        return (m_origObj instanceof NodeIterator)
                ?(NodeIterator)m_origObj:super.nodeset();
    }

    public NodeList nodelist() throws javax.xml.transform.TransformerException{
        return (m_origObj instanceof NodeList)
                ?(NodeList)m_origObj:super.nodelist();
    }
}
