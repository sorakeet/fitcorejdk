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
 * $Id: OneStepIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
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
 * $Id: OneStepIterator.java,v 1.2.4.2 2005/09/14 19:45:22 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.OpMap;

public class OneStepIterator extends ChildTestIterator{
    static final long serialVersionUID=4623710779664998283L;
    protected int m_axis=-1;
    protected DTMAxisIterator m_iterator;

    OneStepIterator(Compiler compiler,int opPos,int analysis)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis);
        int firstStepPos=OpMap.getFirstChildPos(opPos);
        m_axis=WalkerFactory.getAxisFromStep(compiler,firstStepPos);
    }

    public OneStepIterator(DTMAxisIterator iterator,int axis)
            throws javax.xml.transform.TransformerException{
        super(null);
        m_iterator=iterator;
        m_axis=axis;
        int whatToShow=DTMFilter.SHOW_ALL;
        initNodeTest(whatToShow);
    }

    protected int getNextNode(){
        return m_lastFetched=m_iterator.next();
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException{
        OneStepIterator clone=(OneStepIterator)super.cloneWithReset();
        clone.m_iterator=m_iterator;
        return clone;
    }

    public void setRoot(int context,Object environment){
        super.setRoot(context,environment);
        if(m_axis>-1)
            m_iterator=m_cdtm.getAxisIterator(m_axis);
        m_iterator.setStartNode(m_context);
    }

    public int getAxis(){
        return m_axis;
    }    public Object clone() throws CloneNotSupportedException{
        // Do not access the location path itterator during this operation!
        OneStepIterator clone=(OneStepIterator)super.clone();
        if(m_iterator!=null){
            clone.m_iterator=m_iterator.cloneIterator();
        }
        return clone;
    }

    public void detach(){
        if(m_allowDetach){
            if(m_axis>-1)
                m_iterator=null;
            // Always call the superclass detach last!
            super.detach();
        }
    }

    public int getLength(){
        if(!isReverseAxes())
            return super.getLength();
        // Tell if this is being called from within a predicate.
        boolean isPredicateTest=(this==m_execContext.getSubContextList());
        // And get how many total predicates are part of this step.
        int predCount=getPredicateCount();
        // If we have already calculated the length, and the current predicate
        // is the first predicate, then return the length.  We don't cache
        // the anything but the length of the list to the first predicate.
        if(-1!=m_length&&isPredicateTest&&m_predicateIndex<1)
            return m_length;
        int count=0;
        XPathContext xctxt=getXPathContext();
        try{
            OneStepIterator clone=(OneStepIterator)this.cloneWithReset();
            int root=getRoot();
            xctxt.pushCurrentNode(root);
            clone.setRoot(root,xctxt);
            clone.m_predCount=m_predicateIndex;
            int next;
            while(DTM.NULL!=(next=clone.nextNode())){
                count++;
            }
        }catch(CloneNotSupportedException cnse){
            // can't happen
        }finally{
            xctxt.popCurrentNode();
        }
        if(isPredicateTest&&m_predicateIndex<1)
            m_length=count;
        return count;
    }    public boolean isReverseAxes(){
        return m_iterator.isReverse();
    }

    public void reset(){
        super.reset();
        if(null!=m_iterator)
            m_iterator.reset();
    }    protected int getProximityPosition(int predicateIndex){
        if(!isReverseAxes())
            return super.getProximityPosition(predicateIndex);
        // A negative predicate index seems to occur with
        // (preceding-sibling::*|following-sibling::*)/ancestor::*[position()]/**[position()]
        // -sb
        if(predicateIndex<0)
            return -1;
        if(m_proximityPositions[predicateIndex]<=0){
            XPathContext xctxt=getXPathContext();
            try{
                OneStepIterator clone=(OneStepIterator)this.clone();
                int root=getRoot();
                xctxt.pushCurrentNode(root);
                clone.setRoot(root,xctxt);
                // clone.setPredicateCount(predicateIndex);
                clone.m_predCount=predicateIndex;
                // Count 'em all
                int count=1;
                int next;
                while(DTM.NULL!=(next=clone.nextNode())){
                    count++;
                }
                m_proximityPositions[predicateIndex]+=count;
            }catch(CloneNotSupportedException cnse){
                // can't happen
            }finally{
                xctxt.popCurrentNode();
            }
        }
        return m_proximityPositions[predicateIndex];
    }



    protected void countProximityPosition(int i){
        if(!isReverseAxes())
            super.countProximityPosition(i);
        else if(i<m_proximityPositions.length)
            m_proximityPositions[i]--;
    }





    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        if(m_axis!=((OneStepIterator)expr).m_axis)
            return false;
        return true;
    }
}
