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
 * $Id: UnionChildIterator.java,v 1.2.4.1 2005/09/14 19:45:20 jeffsuttor Exp $
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
 * $Id: UnionChildIterator.java,v 1.2.4.1 2005/09/14 19:45:20 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.patterns.NodeTest;

public class UnionChildIterator extends ChildTestIterator{
    static final long serialVersionUID=3500298482193003495L;
    private PredicatedNodeTest[] m_nodeTests=null;

    public UnionChildIterator(){
        super(null);
    }

    public void addNodeTest(PredicatedNodeTest test){
        // Increase array size by only 1 at a time.  Fix this
        // if it looks to be a problem.
        if(null==m_nodeTests){
            m_nodeTests=new PredicatedNodeTest[1];
            m_nodeTests[0]=test;
        }else{
            PredicatedNodeTest[] tests=m_nodeTests;
            int len=m_nodeTests.length;
            m_nodeTests=new PredicatedNodeTest[len+1];
            System.arraycopy(tests,0,m_nodeTests,0,len);
            m_nodeTests[len]=test;
        }
        test.exprSetParent(this);
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        if(m_nodeTests!=null){
            for(int i=0;i<m_nodeTests.length;i++){
                m_nodeTests[i].fixupVariables(vars,globalsSize);
            }
        }
    }

    public short acceptNode(int n){
        XPathContext xctxt=getXPathContext();
        try{
            xctxt.pushCurrentNode(n);
            for(int i=0;i<m_nodeTests.length;i++){
                PredicatedNodeTest pnt=m_nodeTests[i];
                XObject score=pnt.execute(xctxt,n);
                if(score!=NodeTest.SCORE_NONE){
                    // Note that we are assuming there are no positional predicates!
                    if(pnt.getPredicateCount()>0){
                        if(pnt.executePredicates(n,xctxt))
                            return DTMIterator.FILTER_ACCEPT;
                    }else
                        return DTMIterator.FILTER_ACCEPT;
                }
            }
        }catch(javax.xml.transform.TransformerException se){
            // TODO: Fix this.
            throw new RuntimeException(se.getMessage());
        }finally{
            xctxt.popCurrentNode();
        }
        return DTMIterator.FILTER_SKIP;
    }
}
