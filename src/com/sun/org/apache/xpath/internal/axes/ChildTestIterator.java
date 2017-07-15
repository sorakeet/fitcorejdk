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
 * $Id: ChildTestIterator.java,v 1.2.4.2 2005/09/14 19:45:20 jeffsuttor Exp $
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
 * $Id: ChildTestIterator.java,v 1.2.4.2 2005/09/14 19:45:20 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.compiler.Compiler;

public class ChildTestIterator extends BasicTestIterator{
    static final long serialVersionUID=-7936835957960705722L;
    transient protected DTMAxisTraverser m_traverser;
    //  protected int m_extendedTypeID;

    ChildTestIterator(Compiler compiler,int opPos,int analysis)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis);
    }

    public ChildTestIterator(DTMAxisTraverser traverser){
        super(null);
        m_traverser=traverser;
    }

    protected int getNextNode(){
        if(true /** 0 == m_extendedTypeID */){
            m_lastFetched=(DTM.NULL==m_lastFetched)
                    ?m_traverser.first(m_context)
                    :m_traverser.next(m_context,m_lastFetched);
        }
//    else
//    {
//      m_lastFetched = (DTM.NULL == m_lastFetched)
//                   ? m_traverser.first(m_context, m_extendedTypeID)
//                   : m_traverser.next(m_context, m_lastFetched,
//                                      m_extendedTypeID);
//    }
        return m_lastFetched;
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException{
        ChildTestIterator clone=(ChildTestIterator)super.cloneWithReset();
        clone.m_traverser=m_traverser;
        return clone;
    }

    public void setRoot(int context,Object environment){
        super.setRoot(context,environment);
        m_traverser=m_cdtm.getAxisTraverser(Axis.CHILD);
//    String localName = getLocalName();
//    String namespace = getNamespace();
//    int what = m_whatToShow;
//    // System.out.println("what: ");
//    // NodeTest.debugWhatToShow(what);
//    if(DTMFilter.SHOW_ALL == what ||
//       ((DTMFilter.SHOW_ELEMENT & what) == 0)
//       || localName == NodeTest.WILD
//       || namespace == NodeTest.WILD)
//    {
//      m_extendedTypeID = 0;
//    }
//    else
//    {
//      int type = getNodeTypeTest(what);
//      m_extendedTypeID = m_cdtm.getExpandedTypeID(namespace, localName, type);
//    }
    }

    public void detach(){
        if(m_allowDetach){
            m_traverser=null;
            // Always call the superclass detach last!
            super.detach();
        }
    }

    public int getAxis(){
        return Axis.CHILD;
    }
}
