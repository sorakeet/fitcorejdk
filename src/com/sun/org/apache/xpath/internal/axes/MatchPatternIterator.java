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
 * $Id: MatchPatternIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
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
 * $Id: MatchPatternIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.OpMap;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.patterns.NodeTest;
import com.sun.org.apache.xpath.internal.patterns.StepPattern;

public class MatchPatternIterator extends LocPathIterator{
    static final long serialVersionUID=-5201153767396296474L;
    private static final boolean DEBUG=false;
    protected StepPattern m_pattern;
    protected int m_superAxis=-1;
    protected DTMAxisTraverser m_traverser;
//  protected int m_nsElemBase = DTM.NULL;

    MatchPatternIterator(Compiler compiler,int opPos,int analysis)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis,false);
        int firstStepPos=OpMap.getFirstChildPos(opPos);
        m_pattern=WalkerFactory.loadSteps(this,compiler,firstStepPos,0);
        boolean fromRoot=false;
        boolean walkBack=false;
        boolean walkDescendants=false;
        boolean walkAttributes=false;
        if(0!=(analysis&(WalkerFactory.BIT_ROOT|
                WalkerFactory.BIT_ANY_DESCENDANT_FROM_ROOT)))
            fromRoot=true;
        if(0!=(analysis
                &(WalkerFactory.BIT_ANCESTOR
                |WalkerFactory.BIT_ANCESTOR_OR_SELF
                |WalkerFactory.BIT_PRECEDING
                |WalkerFactory.BIT_PRECEDING_SIBLING
                |WalkerFactory.BIT_FOLLOWING
                |WalkerFactory.BIT_FOLLOWING_SIBLING
                |WalkerFactory.BIT_PARENT|WalkerFactory.BIT_FILTER)))
            walkBack=true;
        if(0!=(analysis
                &(WalkerFactory.BIT_DESCENDANT_OR_SELF
                |WalkerFactory.BIT_DESCENDANT
                |WalkerFactory.BIT_CHILD)))
            walkDescendants=true;
        if(0!=(analysis
                &(WalkerFactory.BIT_ATTRIBUTE|WalkerFactory.BIT_NAMESPACE)))
            walkAttributes=true;
        if(false||DEBUG){
            System.out.print("analysis: "+Integer.toBinaryString(analysis));
            System.out.println(", "+WalkerFactory.getAnalysisString(analysis));
        }
        if(fromRoot||walkBack){
            if(walkAttributes){
                m_superAxis=Axis.ALL;
            }else{
                m_superAxis=Axis.DESCENDANTSFROMROOT;
            }
        }else if(walkDescendants){
            if(walkAttributes){
                m_superAxis=Axis.ALLFROMNODE;
            }else{
                m_superAxis=Axis.DESCENDANTORSELF;
            }
        }else{
            m_superAxis=Axis.ALL;
        }
        if(false||DEBUG){
            System.out.println("axis: "+Axis.getNames(m_superAxis));
        }
    }

    public void setRoot(int context,Object environment){
        super.setRoot(context,environment);
        m_traverser=m_cdtm.getAxisTraverser(m_superAxis);
    }

    public void detach(){
        if(m_allowDetach){
            m_traverser=null;
            // Always call the superclass detach last!
            super.detach();
        }
    }

    public int nextNode(){
        if(m_foundLast)
            return DTM.NULL;
        int next;
        com.sun.org.apache.xpath.internal.VariableStack vars;
        int savedStart;
        if(-1!=m_stackFrame){
            vars=m_execContext.getVarStack();
            // These three statements need to be combined into one operation.
            savedStart=vars.getStackFrame();
            vars.setStackFrame(m_stackFrame);
        }else{
            // Yuck.  Just to shut up the compiler!
            vars=null;
            savedStart=0;
        }
        try{
            if(DEBUG)
                System.out.println("m_pattern"+m_pattern.toString());
            do{
                next=getNextNode();
                if(DTM.NULL!=next){
                    if(DTMIterator.FILTER_ACCEPT==acceptNode(next,m_execContext))
                        break;
                    else
                        continue;
                }else
                    break;
            }
            while(next!=DTM.NULL);
            if(DTM.NULL!=next){
                if(DEBUG){
                    System.out.println("next: "+next);
                    System.out.println("name: "+m_cdtm.getNodeName(next));
                }
                incrementCurrentPos();
                return next;
            }else{
                m_foundLast=true;
                return DTM.NULL;
            }
        }finally{
            if(-1!=m_stackFrame){
                // These two statements need to be combined into one operation.
                vars.setStackFrame(savedStart);
            }
        }
    }

    protected int getNextNode(){
        m_lastFetched=(DTM.NULL==m_lastFetched)
                ?m_traverser.first(m_context)
                :m_traverser.next(m_context,m_lastFetched);
        return m_lastFetched;
    }

    public short acceptNode(int n,XPathContext xctxt){
        try{
            xctxt.pushCurrentNode(n);
            xctxt.pushIteratorRoot(m_context);
            if(DEBUG){
                System.out.println("traverser: "+m_traverser);
                System.out.print("node: "+n);
                System.out.println(", "+m_cdtm.getNodeName(n));
                // if(m_cdtm.getNodeName(n).equals("near-east"))
                System.out.println("pattern: "+m_pattern.toString());
                m_pattern.debugWhatToShow(m_pattern.getWhatToShow());
            }
            XObject score=m_pattern.execute(xctxt);
            if(DEBUG){
                // System.out.println("analysis: "+Integer.toBinaryString(m_analysis));
                System.out.println("score: "+score);
                System.out.println("skip: "+(score==NodeTest.SCORE_NONE));
            }
            // System.out.println("\n::acceptNode - score: "+score.num()+"::");
            return (score==NodeTest.SCORE_NONE)?DTMIterator.FILTER_SKIP
                    :DTMIterator.FILTER_ACCEPT;
        }catch(javax.xml.transform.TransformerException se){
            // TODO: Fix this.
            throw new RuntimeException(se.getMessage());
        }finally{
            xctxt.popCurrentNode();
            xctxt.popIteratorRoot();
        }
    }
}
