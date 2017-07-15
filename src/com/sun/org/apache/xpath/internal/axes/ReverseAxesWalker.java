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
 * $Id: ReverseAxesWalker.java,v 1.2.4.1 2005/09/14 19:45:21 jeffsuttor Exp $
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
 * $Id: ReverseAxesWalker.java,v 1.2.4.1 2005/09/14 19:45:21 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xpath.internal.XPathContext;

public class ReverseAxesWalker extends AxesWalker{
    static final long serialVersionUID=2847007647832768941L;
    protected DTMAxisIterator m_iterator;

    ReverseAxesWalker(LocPathIterator locPathIterator,int axis){
        super(locPathIterator,axis);
    }

    public void detach(){
        m_iterator=null;
        super.detach();
    }

    public void setRoot(int root){
        super.setRoot(root);
        m_iterator=getDTM(root).getAxisIterator(m_axis);
        m_iterator.setStartNode(root);
    }

    protected int getNextNode(){
        if(m_foundLast)
            return DTM.NULL;
        int next=m_iterator.next();
        if(m_isFresh)
            m_isFresh=false;
        if(DTM.NULL==next)
            this.m_foundLast=true;
        return next;
    }
//  /**
//   *  Set the root node of the TreeWalker.
//   *
//   * @param root The context node of this step.
//   */
//  public void setRoot(int root)
//  {
//    super.setRoot(root);
//  }

    public int getLastPos(XPathContext xctxt){
        int count=0;
        AxesWalker savedWalker=wi().getLastUsedWalker();
        try{
            ReverseAxesWalker clone=(ReverseAxesWalker)this.clone();
            clone.setRoot(this.getRoot());
            clone.setPredicateCount(this.getPredicateCount()-1);
            clone.setPrevWalker(null);
            clone.setNextWalker(null);
            wi().setLastUsedWalker(clone);
            // Count 'em all
            // count = 1;
            int next;
            while(DTM.NULL!=(next=clone.nextNode())){
                count++;
            }
        }catch(CloneNotSupportedException cnse){
            // can't happen
        }finally{
            wi().setLastUsedWalker(savedWalker);
        }
        return count;
    }

    public boolean isDocOrdered(){
        return false;  // I think.
    }

    protected int getProximityPosition(int predicateIndex){
        // A negative predicate index seems to occur with
        // (preceding-sibling::*|following-sibling::*)/ancestor::*[position()]/**[position()]
        // -sb
        if(predicateIndex<0)
            return -1;
        int count=m_proximityPositions[predicateIndex];
        if(count<=0){
            AxesWalker savedWalker=wi().getLastUsedWalker();
            try{
                ReverseAxesWalker clone=(ReverseAxesWalker)this.clone();
                clone.setRoot(this.getRoot());
                clone.setPredicateCount(predicateIndex);
                clone.setPrevWalker(null);
                clone.setNextWalker(null);
                wi().setLastUsedWalker(clone);
                // Count 'em all
                count++;
                int next;
                while(DTM.NULL!=(next=clone.nextNode())){
                    count++;
                }
                m_proximityPositions[predicateIndex]=count;
            }catch(CloneNotSupportedException cnse){
                // can't happen
            }finally{
                wi().setLastUsedWalker(savedWalker);
            }
        }
        return count;
    }

    protected void countProximityPosition(int i){
        if(i<m_proximityPositions.length)
            m_proximityPositions[i]--;
    }

    public boolean isReverseAxes(){
        return true;
    }
}
