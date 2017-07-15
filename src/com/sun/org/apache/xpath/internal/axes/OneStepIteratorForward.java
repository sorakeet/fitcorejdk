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
 * $Id: OneStepIteratorForward.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
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
 * $Id: OneStepIteratorForward.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.OpMap;

public class OneStepIteratorForward extends ChildTestIterator{
    static final long serialVersionUID=-1576936606178190566L;
    protected int m_axis=-1;

    OneStepIteratorForward(Compiler compiler,int opPos,int analysis)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis);
        int firstStepPos=OpMap.getFirstChildPos(opPos);
        m_axis=WalkerFactory.getAxisFromStep(compiler,firstStepPos);
    }

    public OneStepIteratorForward(int axis){
        super(null);
        m_axis=axis;
        int whatToShow=DTMFilter.SHOW_ALL;
        initNodeTest(whatToShow);
    }

    protected int getNextNode(){
        m_lastFetched=(DTM.NULL==m_lastFetched)
                ?m_traverser.first(m_context)
                :m_traverser.next(m_context,m_lastFetched);
        return m_lastFetched;
    }
//  /**
//   * Return the first node out of the nodeset, if this expression is
//   * a nodeset expression.  This is the default implementation for
//   * nodesets.
//   * <p>WARNING: Do not mutate this class from this function!</p>
//   * @param xctxt The XPath runtime context.
//   * @return the first node out of the nodeset, or DTM.NULL.
//   */
//  public int asNode(XPathContext xctxt)
//    throws javax.xml.transform.TransformerException
//  {
//    if(getPredicateCount() > 0)
//      return super.asNode(xctxt);
//
//    int current = xctxt.getCurrentNode();
//
//    DTM dtm = xctxt.getDTM(current);
//    DTMAxisTraverser traverser = dtm.getAxisTraverser(m_axis);
//
//    String localName = getLocalName();
//    String namespace = getNamespace();
//    int what = m_whatToShow;
//
//    // System.out.println("what: ");
//    // NodeTest.debugWhatToShow(what);
//    if(DTMFilter.SHOW_ALL == what
//       || ((DTMFilter.SHOW_ELEMENT & what) == 0)
//       || localName == NodeTest.WILD
//       || namespace == NodeTest.WILD)
//    {
//      return traverser.first(current);
//    }
//    else
//    {
//      int type = getNodeTypeTest(what);
//      int extendedType = dtm.getExpandedTypeID(namespace, localName, type);
//      return traverser.first(current, extendedType);
//    }
//  }

    public void setRoot(int context,Object environment){
        super.setRoot(context,environment);
        m_traverser=m_cdtm.getAxisTraverser(m_axis);
    }

    public int getAxis(){
        return m_axis;
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        if(m_axis!=((OneStepIteratorForward)expr).m_axis)
            return false;
        return true;
    }
}
