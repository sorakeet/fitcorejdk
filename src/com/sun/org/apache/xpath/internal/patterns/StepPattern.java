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
 * $Id: StepPattern.java,v 1.2.4.2 2005/09/15 00:21:16 jeffsuttor Exp $
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
 * $Id: StepPattern.java,v 1.2.4.2 2005/09/15 00:21:16 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.patterns;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.axes.SubContextList;
import com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class StepPattern extends NodeTest implements SubContextList, ExpressionOwner{
    static final long serialVersionUID=9071668960168152644L;
    private static final boolean DEBUG_MATCHES=false;
    protected int m_axis;
    String m_targetString;  // only calculate on head
    StepPattern m_relativePathPattern;
    Expression[] m_predicates;

    public StepPattern(int whatToShow,String namespace,String name,int axis,
                       int axisForPredicate){
        super(whatToShow,namespace,name);
        m_axis=axis;
    }

    public StepPattern(int whatToShow,int axis,int axisForPredicate){
        super(whatToShow);
        m_axis=axis;
    }

    public String getTargetString(){
        return m_targetString;
    }

    public StepPattern getRelativePathPattern(){
        return m_relativePathPattern;
    }

    public void setRelativePathPattern(StepPattern expr){
        m_relativePathPattern=expr;
        expr.exprSetParent(this);
        calcScore();
    }
    //  /**
    //   * Set the list of predicate expressions for this pattern step.
    //   * @param predicates List of expression objects.
    //   */
    //  public void setPredicates(Expression[] predicates)
    //  {
    //    m_predicates = predicates;
    //  }

    public Expression[] getPredicates(){
        return m_predicates;
    }

    public void setPredicates(Expression[] predicates){
        m_predicates=predicates;
        if(null!=predicates){
            for(int i=0;i<predicates.length;i++){
                predicates[i].exprSetParent(this);
            }
        }
        calcScore();
    }

    public boolean canTraverseOutsideSubtree(){
        int n=getPredicateCount();
        for(int i=0;i<n;i++){
            if(getPredicate(i).canTraverseOutsideSubtree())
                return true;
        }
        return false;
    }

    public Expression getPredicate(int i){
        return m_predicates[i];
    }

    public int getLastPos(XPathContext xctxt){
        return getProximityPosition(xctxt,xctxt.getPredicatePos(),true);
    }

    public int getProximityPosition(XPathContext xctxt){
        return getProximityPosition(xctxt,xctxt.getPredicatePos(),false);
    }

    private final int getProximityPosition(XPathContext xctxt,int predPos,
                                           boolean findLast){
        int pos=0;
        int context=xctxt.getCurrentNode();
        DTM dtm=xctxt.getDTM(context);
        int parent=dtm.getParent(context);
        try{
            DTMAxisTraverser traverser=dtm.getAxisTraverser(Axis.CHILD);
            for(int child=traverser.first(parent);DTM.NULL!=child;
                child=traverser.next(parent,child)){
                try{
                    xctxt.pushCurrentNode(child);
                    if(NodeTest.SCORE_NONE!=super.execute(xctxt,child)){
                        boolean pass=true;
                        try{
                            xctxt.pushSubContextList(this);
                            for(int i=0;i<predPos;i++){
                                xctxt.pushPredicatePos(i);
                                try{
                                    XObject pred=m_predicates[i].execute(xctxt);
                                    try{
                                        if(XObject.CLASS_NUMBER==pred.getType()){
                                            if((pos+1)!=(int)pred.numWithSideEffects()){
                                                pass=false;
                                                break;
                                            }
                                        }else if(!pred.boolWithSideEffects()){
                                            pass=false;
                                            break;
                                        }
                                    }finally{
                                        pred.detach();
                                    }
                                }finally{
                                    xctxt.popPredicatePos();
                                }
                            }
                        }finally{
                            xctxt.popSubContextList();
                        }
                        if(pass)
                            pos++;
                        if(!findLast&&child==context){
                            return pos;
                        }
                    }
                }finally{
                    xctxt.popCurrentNode();
                }
            }
        }catch(javax.xml.transform.TransformerException se){
            // TODO: should keep throw sax exception...
            throw new RuntimeException(se.getMessage());
        }
        return pos;
    }

    protected final XObject executeRelativePathPattern(
            XPathContext xctxt,DTM dtm,int currentNode)
            throws javax.xml.transform.TransformerException{
        XObject score=NodeTest.SCORE_NONE;
        int context=currentNode;
        DTMAxisTraverser traverser;
        traverser=dtm.getAxisTraverser(m_axis);
        for(int relative=traverser.first(context);DTM.NULL!=relative;
            relative=traverser.next(context,relative)){
            try{
                xctxt.pushCurrentNode(relative);
                score=execute(xctxt);
                if(score!=NodeTest.SCORE_NONE)
                    break;
            }finally{
                xctxt.popCurrentNode();
            }
        }
        return score;
    }

    public String toString(){
        StringBuffer buf=new StringBuffer();
        for(StepPattern pat=this;pat!=null;pat=pat.m_relativePathPattern){
            if(pat!=this)
                buf.append("/");
            buf.append(Axis.getNames(pat.m_axis));
            buf.append("::");
            if(0x000005000==pat.m_whatToShow){
                buf.append("doc()");
            }else if(DTMFilter.SHOW_BYFUNCTION==pat.m_whatToShow){
                buf.append("function()");
            }else if(DTMFilter.SHOW_ALL==pat.m_whatToShow){
                buf.append("node()");
            }else if(DTMFilter.SHOW_TEXT==pat.m_whatToShow){
                buf.append("text()");
            }else if(DTMFilter.SHOW_PROCESSING_INSTRUCTION==pat.m_whatToShow){
                buf.append("processing-instruction(");
                if(null!=pat.m_name){
                    buf.append(pat.m_name);
                }
                buf.append(")");
            }else if(DTMFilter.SHOW_COMMENT==pat.m_whatToShow){
                buf.append("comment()");
            }else if(null!=pat.m_name){
                if(DTMFilter.SHOW_ATTRIBUTE==pat.m_whatToShow){
                    buf.append("@");
                }
                if(null!=pat.m_namespace){
                    buf.append("{");
                    buf.append(pat.m_namespace);
                    buf.append("}");
                }
                buf.append(pat.m_name);
            }else if(DTMFilter.SHOW_ATTRIBUTE==pat.m_whatToShow){
                buf.append("@");
            }else if((DTMFilter.SHOW_DOCUMENT|DTMFilter.SHOW_DOCUMENT_FRAGMENT)
                    ==pat.m_whatToShow){
                buf.append("doc-root()");
            }else{
                buf.append('?').append(Integer.toHexString(pat.m_whatToShow));
            }
            if(null!=pat.m_predicates){
                for(int i=0;i<pat.m_predicates.length;i++){
                    buf.append("[");
                    buf.append(pat.m_predicates[i]);
                    buf.append("]");
                }
            }
        }
        return buf.toString();
    }

    public double getMatchScore(XPathContext xctxt,int context)
            throws javax.xml.transform.TransformerException{
        xctxt.pushCurrentNode(context);
        xctxt.pushCurrentExpressionNode(context);
        try{
            XObject score=execute(xctxt);
            return score.num();
        }finally{
            xctxt.popCurrentNode();
            xctxt.popCurrentExpressionNode();
        }
        // return XPath.MATCH_SCORE_NONE;
    }

    public int getAxis(){
        return m_axis;
    }

    public void setAxis(int axis){
        m_axis=axis;
    }

    public boolean deepEquals(Expression expr){
        if(!super.deepEquals(expr))
            return false;
        StepPattern sp=(StepPattern)expr;
        if(null!=m_predicates){
            int n=m_predicates.length;
            if((null==sp.m_predicates)||(sp.m_predicates.length!=n))
                return false;
            for(int i=0;i<n;i++){
                if(!m_predicates[i].deepEquals(sp.m_predicates[i]))
                    return false;
            }
        }else if(null!=sp.m_predicates)
            return false;
        if(null!=m_relativePathPattern){
            if(!m_relativePathPattern.deepEquals(sp.m_relativePathPattern))
                return false;
        }else if(sp.m_relativePathPattern!=null)
            return false;
        return true;
    }

    public void calcScore(){
        if((getPredicateCount()>0)||(null!=m_relativePathPattern)){
            m_score=SCORE_OTHER;
        }else
            super.calcScore();
        if(null==m_targetString)
            calcTargetString();
    }

    public void calcTargetString(){
        int whatToShow=getWhatToShow();
        switch(whatToShow){
            case DTMFilter.SHOW_COMMENT:
                m_targetString=PsuedoNames.PSEUDONAME_COMMENT;
                break;
            case DTMFilter.SHOW_TEXT:
            case DTMFilter.SHOW_CDATA_SECTION:
            case (DTMFilter.SHOW_TEXT|DTMFilter.SHOW_CDATA_SECTION):
                m_targetString=PsuedoNames.PSEUDONAME_TEXT;
                break;
            case DTMFilter.SHOW_ALL:
                m_targetString=PsuedoNames.PSEUDONAME_ANY;
                break;
            case DTMFilter.SHOW_DOCUMENT:
            case DTMFilter.SHOW_DOCUMENT|DTMFilter.SHOW_DOCUMENT_FRAGMENT:
                m_targetString=PsuedoNames.PSEUDONAME_ROOT;
                break;
            case DTMFilter.SHOW_ELEMENT:
                if(this.WILD==m_name)
                    m_targetString=PsuedoNames.PSEUDONAME_ANY;
                else
                    m_targetString=m_name;
                break;
            default:
                m_targetString=PsuedoNames.PSEUDONAME_ANY;
                break;
        }
    }

    public final int getPredicateCount(){
        return (null==m_predicates)?0:m_predicates.length;
    }

    public XObject execute(XPathContext xctxt,int currentNode)
            throws javax.xml.transform.TransformerException{
        DTM dtm=xctxt.getDTM(currentNode);
        if(dtm!=null){
            int expType=dtm.getExpandedTypeID(currentNode);
            return execute(xctxt,currentNode,dtm,expType);
        }
        return NodeTest.SCORE_NONE;
    }

    public XObject execute(
            XPathContext xctxt,int currentNode,DTM dtm,int expType)
            throws javax.xml.transform.TransformerException{
        if(m_whatToShow==NodeTest.SHOW_BYFUNCTION){
            if(null!=m_relativePathPattern){
                return m_relativePathPattern.execute(xctxt);
            }else
                return NodeTest.SCORE_NONE;
        }
        XObject score;
        score=super.execute(xctxt,currentNode,dtm,expType);
        if(score==NodeTest.SCORE_NONE)
            return NodeTest.SCORE_NONE;
        if(getPredicateCount()!=0){
            if(!executePredicates(xctxt,dtm,currentNode))
                return NodeTest.SCORE_NONE;
        }
        if(null!=m_relativePathPattern)
            return m_relativePathPattern.executeRelativePathPattern(xctxt,dtm,
                    currentNode);
        return score;
    }

    public XObject execute(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return execute(xctxt,xctxt.getCurrentNode());
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        super.fixupVariables(vars,globalsSize);
        if(null!=m_predicates){
            for(int i=0;i<m_predicates.length;i++){
                m_predicates[i].fixupVariables(vars,globalsSize);
            }
        }
        if(null!=m_relativePathPattern){
            m_relativePathPattern.fixupVariables(vars,globalsSize);
        }
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        if(visitor.visitMatchPattern(owner,this)){
            callSubtreeVisitors(visitor);
        }
    }

    protected void callSubtreeVisitors(XPathVisitor visitor){
        if(null!=m_predicates){
            int n=m_predicates.length;
            for(int i=0;i<n;i++){
                ExpressionOwner predOwner=new PredOwner(i);
                if(visitor.visitPredicate(predOwner,m_predicates[i])){
                    m_predicates[i].callVisitors(predOwner,visitor);
                }
            }
        }
        if(null!=m_relativePathPattern){
            m_relativePathPattern.callVisitors(this,visitor);
        }
    }

    protected final boolean executePredicates(
            XPathContext xctxt,DTM dtm,int currentNode)
            throws javax.xml.transform.TransformerException{
        boolean result=true;
        boolean positionAlreadySeen=false;
        int n=getPredicateCount();
        try{
            xctxt.pushSubContextList(this);
            for(int i=0;i<n;i++){
                xctxt.pushPredicatePos(i);
                try{
                    XObject pred=m_predicates[i].execute(xctxt);
                    try{
                        if(XObject.CLASS_NUMBER==pred.getType()){
                            int pos=(int)pred.num();
                            if(positionAlreadySeen){
                                result=(pos==1);
                                break;
                            }else{
                                positionAlreadySeen=true;
                                if(!checkProximityPosition(xctxt,i,dtm,currentNode,pos)){
                                    result=false;
                                    break;
                                }
                            }
                        }else if(!pred.boolWithSideEffects()){
                            result=false;
                            break;
                        }
                    }finally{
                        pred.detach();
                    }
                }finally{
                    xctxt.popPredicatePos();
                }
            }
        }finally{
            xctxt.popSubContextList();
        }
        return result;
    }

    private final boolean checkProximityPosition(XPathContext xctxt,
                                                 int predPos,DTM dtm,int context,int pos){
        try{
            DTMAxisTraverser traverser=
                    dtm.getAxisTraverser(Axis.PRECEDINGSIBLING);
            for(int child=traverser.first(context);DTM.NULL!=child;
                child=traverser.next(context,child)){
                try{
                    xctxt.pushCurrentNode(child);
                    if(NodeTest.SCORE_NONE!=super.execute(xctxt,child)){
                        boolean pass=true;
                        try{
                            xctxt.pushSubContextList(this);
                            for(int i=0;i<predPos;i++){
                                xctxt.pushPredicatePos(i);
                                try{
                                    XObject pred=m_predicates[i].execute(xctxt);
                                    try{
                                        if(XObject.CLASS_NUMBER==pred.getType()){
                                            throw new Error("Why: Should never have been called");
                                        }else if(!pred.boolWithSideEffects()){
                                            pass=false;
                                            break;
                                        }
                                    }finally{
                                        pred.detach();
                                    }
                                }finally{
                                    xctxt.popPredicatePos();
                                }
                            }
                        }finally{
                            xctxt.popSubContextList();
                        }
                        if(pass)
                            pos--;
                        if(pos<1)
                            return false;
                    }
                }finally{
                    xctxt.popCurrentNode();
                }
            }
        }catch(javax.xml.transform.TransformerException se){
            // TODO: should keep throw sax exception...
            throw new RuntimeException(se.getMessage());
        }
        return (pos==1);
    }

    class PredOwner implements ExpressionOwner{
        int m_index;

        PredOwner(int index){
            m_index=index;
        }

        public Expression getExpression(){
            return m_predicates[m_index];
        }

        public void setExpression(Expression exp){
            exp.exprSetParent(StepPattern.this);
            m_predicates[m_index]=exp;
        }
    }    public Expression getExpression(){
        return m_relativePathPattern;
    }

    public void setExpression(Expression exp){
        exp.exprSetParent(this);
        m_relativePathPattern=(StepPattern)exp;
    }


}
