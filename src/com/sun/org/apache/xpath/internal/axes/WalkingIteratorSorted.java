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
 * $Id: WalkingIteratorSorted.java,v 1.2.4.1 2005/09/14 19:45:23 jeffsuttor Exp $
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
 * $Id: WalkingIteratorSorted.java,v 1.2.4.1 2005/09/14 19:45:23 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.compiler.Compiler;

public class WalkingIteratorSorted extends WalkingIterator{
    static final long serialVersionUID=-4512512007542368213L;
//  /** True if the nodes will be found in document order */
//  protected boolean m_inNaturalOrder = false;
    protected boolean m_inNaturalOrderStatic=false;

    public WalkingIteratorSorted(PrefixResolver nscontext){
        super(nscontext);
    }

    WalkingIteratorSorted(
            Compiler compiler,int opPos,int analysis,boolean shouldLoadWalkers)
            throws javax.xml.transform.TransformerException{
        super(compiler,opPos,analysis,shouldLoadWalkers);
    }

    public boolean isDocOrdered(){
        return m_inNaturalOrderStatic;
    }

    boolean canBeWalkedInNaturalDocOrderStatic(){
        if(null!=m_firstWalker){
            AxesWalker walker=m_firstWalker;
            int prevAxis=-1;
            boolean prevIsSimpleDownAxis=true;
            for(int i=0;null!=walker;i++){
                int axis=walker.getAxis();
                if(walker.isDocOrdered()){
                    boolean isSimpleDownAxis=((axis==Axis.CHILD)
                            ||(axis==Axis.SELF)
                            ||(axis==Axis.ROOT));
                    // Catching the filtered list here is only OK because
                    // FilterExprWalker#isDocOrdered() did the right thing.
                    if(isSimpleDownAxis||(axis==-1))
                        walker=walker.getNextWalker();
                    else{
                        boolean isLastWalker=(null==walker.getNextWalker());
                        if(isLastWalker){
                            if(walker.isDocOrdered()&&(axis==Axis.DESCENDANT||
                                    axis==Axis.DESCENDANTORSELF||axis==Axis.DESCENDANTSFROMROOT
                                    ||axis==Axis.DESCENDANTSORSELFFROMROOT)||(axis==Axis.ATTRIBUTE))
                                return true;
                        }
                        return false;
                    }
                }else
                    return false;
            }
            return true;
        }
        return false;
    }
//  /**
//   * NEEDSDOC Method canBeWalkedInNaturalDocOrder
//   *
//   *
//   * NEEDSDOC (canBeWalkedInNaturalDocOrder) @return
//   */
//  boolean canBeWalkedInNaturalDocOrder()
//  {
//
//    if (null != m_firstWalker)
//    {
//      AxesWalker walker = m_firstWalker;
//      int prevAxis = -1;
//      boolean prevIsSimpleDownAxis = true;
//
//      for(int i = 0; null != walker; i++)
//      {
//        int axis = walker.getAxis();
//
//        if(walker.isDocOrdered())
//        {
//          boolean isSimpleDownAxis = ((axis == Axis.CHILD)
//                                   || (axis == Axis.SELF)
//                                   || (axis == Axis.ROOT));
//          // Catching the filtered list here is only OK because
//          // FilterExprWalker#isDocOrdered() did the right thing.
//          if(isSimpleDownAxis || (axis == -1))
//            walker = walker.getNextWalker();
//          else
//          {
//            boolean isLastWalker = (null == walker.getNextWalker());
//            if(isLastWalker)
//            {
//              if(walker.isDocOrdered() && (axis == Axis.DESCENDANT ||
//                 axis == Axis.DESCENDANTORSELF || axis == Axis.DESCENDANTSFROMROOT
//                 || axis == Axis.DESCENDANTSORSELFFROMROOT) || (axis == Axis.ATTRIBUTE))
//                return true;
//            }
//            return false;
//          }
//        }
//        else
//          return false;
//      }
//      return true;
//    }
//    return false;
//  }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        int analysis=getAnalysisBits();
        if(WalkerFactory.isNaturalDocOrder(analysis)){
            m_inNaturalOrderStatic=true;
        }else{
            m_inNaturalOrderStatic=false;
            // System.out.println("Setting natural doc order to false: "+
            //    WalkerFactory.getAnalysisString(analysis));
        }
    }
}
