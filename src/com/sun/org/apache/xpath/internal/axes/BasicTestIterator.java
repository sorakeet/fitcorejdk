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
 * $Id: BasicTestIterator.java,v 1.2.4.1 2005/09/14 19:45:20 jeffsuttor Exp $
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
 * $Id: BasicTestIterator.java,v 1.2.4.1 2005/09/14 19:45:20 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.OpMap;

public abstract class BasicTestIterator extends LocPathIterator{
    static final long serialVersionUID=3505378079378096623L;

    protected BasicTestIterator(){
    }

    protected BasicTestIterator(PrefixResolver nscontext){
        super(nscontext);
    }

    protected BasicTestIterator(Compiler compiler,int opPos,int analysis)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis,false);
        int firstStepPos=OpMap.getFirstChildPos(opPos);
        int whatToShow=compiler.getWhatToShow(firstStepPos);
        if((0==(whatToShow
                &(DTMFilter.SHOW_ATTRIBUTE
                |DTMFilter.SHOW_NAMESPACE
                |DTMFilter.SHOW_ELEMENT
                |DTMFilter.SHOW_PROCESSING_INSTRUCTION)))
                ||(whatToShow==DTMFilter.SHOW_ALL))
            initNodeTest(whatToShow);
        else{
            initNodeTest(whatToShow,compiler.getStepNS(firstStepPos),
                    compiler.getStepLocalName(firstStepPos));
        }
        initPredicateInfo(compiler,firstStepPos);
    }

    protected BasicTestIterator(
            Compiler compiler,int opPos,int analysis,boolean shouldLoadWalkers)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis,shouldLoadWalkers);
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException{
        ChildTestIterator clone=(ChildTestIterator)super.cloneWithReset();
        clone.resetProximityPositions();
        return clone;
    }

    public int nextNode(){
        if(m_foundLast){
            m_lastFetched=DTM.NULL;
            return DTM.NULL;
        }
        if(DTM.NULL==m_lastFetched){
            resetProximityPositions();
        }
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
            do{
                next=getNextNode();
                if(DTM.NULL!=next){
                    if(DTMIterator.FILTER_ACCEPT==acceptNode(next))
                        break;
                    else
                        continue;
                }else
                    break;
            }
            while(next!=DTM.NULL);
            if(DTM.NULL!=next){
                m_pos++;
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

    protected abstract int getNextNode();
}
