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
 * $Id: XPath.java,v 1.2.4.1 2005/09/15 01:41:57 jeffsuttor Exp $
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
 * $Id: XPath.java,v 1.2.4.1 2005/09/15 01:41:57 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.SAXSourceLocator;
import com.sun.org.apache.xpath.internal.compiler.Compiler;
import com.sun.org.apache.xpath.internal.compiler.FunctionTable;
import com.sun.org.apache.xpath.internal.compiler.XPathParser;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import java.io.Serializable;

public class XPath implements Serializable, ExpressionOwner{
    public static final int SELECT=0;
    public static final int MATCH=1;
    public static final double MATCH_SCORE_NONE=Double.NEGATIVE_INFINITY;
    public static final double MATCH_SCORE_QNAME=0.0;
    public static final double MATCH_SCORE_NSWILD=-0.25;
    public static final double MATCH_SCORE_NODETEST=-0.5;
    public static final double MATCH_SCORE_OTHER=0.5;
    static final long serialVersionUID=3976493477939110553L;
    private static final boolean DEBUG_MATCHES=false;
//  /**
//   * Set the SourceLocator on the expression object.
//   *
//   *
//   * @param l the SourceLocator on the expression object, which may be null.
//   */
//  public void setLocator(SourceLocator l)
//  {
//    // Note potential hazards -- l may not be serializable, or may be changed
//      // after being assigned here.
//    m_mainExp.setSourceLocator(l);
//  }
    String m_patternString;
    private Expression m_mainExp;
    private transient FunctionTable m_funcTable=null;

    public XPath(
            String exprString,SourceLocator locator,
            PrefixResolver prefixResolver,int type,
            ErrorListener errorListener,FunctionTable aTable)
            throws TransformerException{
        m_funcTable=aTable;
        if(null==errorListener)
            errorListener=new com.sun.org.apache.xml.internal.utils.DefaultErrorHandler();
        m_patternString=exprString;
        XPathParser parser=new XPathParser(errorListener,locator);
        Compiler compiler=new Compiler(errorListener,locator,m_funcTable);
        if(SELECT==type)
            parser.initXPath(compiler,exprString,prefixResolver);
        else if(MATCH==type)
            parser.initMatchPattern(compiler,exprString,prefixResolver);
        else
            throw new RuntimeException(XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_CANNOT_DEAL_XPATH_TYPE,
                    new Object[]{Integer.toString(type)}));
        //"Can not deal with XPath type: " + type);
        // System.out.println("----------------");
        Expression expr=compiler.compile(0);
        // System.out.println("expr: "+expr);
        this.setExpression(expr);
        if((null!=locator)&&locator instanceof ExpressionNode){
            expr.exprSetParent((ExpressionNode)locator);
        }
    }

    public XPath(
            String exprString,SourceLocator locator,PrefixResolver prefixResolver,int type)
            throws TransformerException{
        this(exprString,locator,prefixResolver,type,null);
    }

    public XPath(
            String exprString,SourceLocator locator,PrefixResolver prefixResolver,int type,
            ErrorListener errorListener)
            throws TransformerException{
        initFunctionTable();
        if(null==errorListener)
            errorListener=new com.sun.org.apache.xml.internal.utils.DefaultErrorHandler();
        m_patternString=exprString;
        XPathParser parser=new XPathParser(errorListener,locator);
        Compiler compiler=new Compiler(errorListener,locator,m_funcTable);
        if(SELECT==type)
            parser.initXPath(compiler,exprString,prefixResolver);
        else if(MATCH==type)
            parser.initMatchPattern(compiler,exprString,prefixResolver);
        else
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CANNOT_DEAL_XPATH_TYPE,new Object[]{Integer.toString(type)})); //"Can not deal with XPath type: " + type);
        // System.out.println("----------------");
        Expression expr=compiler.compile(0);
        // System.out.println("expr: "+expr);
        this.setExpression(expr);
        if((null!=locator)&&locator instanceof ExpressionNode){
            expr.exprSetParent((ExpressionNode)locator);
        }
    }

    private void initFunctionTable(){
        m_funcTable=new FunctionTable();
    }

    public XPath(Expression expr){
        this.setExpression(expr);
        initFunctionTable();
    }

    public Expression getExpression(){
        return m_mainExp;
    }

    public void setExpression(Expression exp){
        if(null!=m_mainExp)
            exp.exprSetParent(m_mainExp.exprGetParent()); // a bit bogus
        m_mainExp=exp;
    }

    public void fixupVariables(java.util.Vector vars,int globalsSize){
        m_mainExp.fixupVariables(vars,globalsSize);
    }

    public XObject execute(
            XPathContext xctxt,org.w3c.dom.Node contextNode,
            PrefixResolver namespaceContext)
            throws TransformerException{
        return execute(
                xctxt,xctxt.getDTMHandleFromNode(contextNode),
                namespaceContext);
    }

    public XObject execute(
            XPathContext xctxt,int contextNode,PrefixResolver namespaceContext)
            throws TransformerException{
        xctxt.pushNamespaceContext(namespaceContext);
        xctxt.pushCurrentNodeAndExpression(contextNode,contextNode);
        XObject xobj=null;
        try{
            xobj=m_mainExp.execute(xctxt);
        }catch(TransformerException te){
            te.setLocator(this.getLocator());
            ErrorListener el=xctxt.getErrorListener();
            if(null!=el) // defensive, should never happen.
            {
                el.error(te);
            }else
                throw te;
        }catch(Exception e){
            while(e instanceof com.sun.org.apache.xml.internal.utils.WrappedRuntimeException){
                e=((com.sun.org.apache.xml.internal.utils.WrappedRuntimeException)e).getException();
            }
            // e.printStackTrace();
            String msg=e.getMessage();
            if(msg==null||msg.length()==0){
                msg=XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_XPATH_ERROR,null);
            }
            TransformerException te=new TransformerException(msg,
                    getLocator(),e);
            ErrorListener el=xctxt.getErrorListener();
            // te.printStackTrace();
            if(null!=el) // defensive, should never happen.
            {
                el.fatalError(te);
            }else
                throw te;
        }finally{
            xctxt.popNamespaceContext();
            xctxt.popCurrentNodeAndExpression();
        }
        return xobj;
    }

    public SourceLocator getLocator(){
        return m_mainExp;
    }

    public boolean bool(
            XPathContext xctxt,int contextNode,PrefixResolver namespaceContext)
            throws TransformerException{
        xctxt.pushNamespaceContext(namespaceContext);
        xctxt.pushCurrentNodeAndExpression(contextNode,contextNode);
        try{
            return m_mainExp.bool(xctxt);
        }catch(TransformerException te){
            te.setLocator(this.getLocator());
            ErrorListener el=xctxt.getErrorListener();
            if(null!=el) // defensive, should never happen.
            {
                el.error(te);
            }else
                throw te;
        }catch(Exception e){
            while(e instanceof com.sun.org.apache.xml.internal.utils.WrappedRuntimeException){
                e=((com.sun.org.apache.xml.internal.utils.WrappedRuntimeException)e).getException();
            }
            // e.printStackTrace();
            String msg=e.getMessage();
            if(msg==null||msg.length()==0){
                msg=XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_XPATH_ERROR,null);
            }
            TransformerException te=new TransformerException(msg,
                    getLocator(),e);
            ErrorListener el=xctxt.getErrorListener();
            // te.printStackTrace();
            if(null!=el) // defensive, should never happen.
            {
                el.fatalError(te);
            }else
                throw te;
        }finally{
            xctxt.popNamespaceContext();
            xctxt.popCurrentNodeAndExpression();
        }
        return false;
    }

    public double getMatchScore(XPathContext xctxt,int context)
            throws TransformerException{
        xctxt.pushCurrentNode(context);
        xctxt.pushCurrentExpressionNode(context);
        try{
            XObject score=m_mainExp.execute(xctxt);
            if(DEBUG_MATCHES){
                DTM dtm=xctxt.getDTM(context);
                System.out.println("score: "+score.num()+" for "
                        +dtm.getNodeName(context)+" for xpath "
                        +this.getPatternString());
            }
            return score.num();
        }finally{
            xctxt.popCurrentNode();
            xctxt.popCurrentExpressionNode();
        }
        // return XPath.MATCH_SCORE_NONE;
    }

    public String getPatternString(){
        return m_patternString;
    }

    public void warn(
            XPathContext xctxt,int sourceNode,String msg,Object[] args)
            throws TransformerException{
        String fmsg=XSLMessages.createXPATHWarning(msg,args);
        ErrorListener ehandler=xctxt.getErrorListener();
        if(null!=ehandler){
            // TO DO: Need to get stylesheet Locator from here.
            ehandler.warning(new TransformerException(fmsg,(SAXSourceLocator)xctxt.getSAXLocator()));
        }
    }

    public void assertion(boolean b,String msg){
        if(!b){
            String fMsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
                    new Object[]{msg});
            throw new RuntimeException(fMsg);
        }
    }

    public void error(
            XPathContext xctxt,int sourceNode,String msg,Object[] args)
            throws TransformerException{
        String fmsg=XSLMessages.createXPATHMessage(msg,args);
        ErrorListener ehandler=xctxt.getErrorListener();
        if(null!=ehandler){
            ehandler.fatalError(new TransformerException(fmsg,
                    (SAXSourceLocator)xctxt.getSAXLocator()));
        }else{
            SourceLocator slocator=xctxt.getSAXLocator();
            System.out.println(fmsg+"; file "+slocator.getSystemId()
                    +"; line "+slocator.getLineNumber()+"; column "
                    +slocator.getColumnNumber());
        }
    }

    public void callVisitors(ExpressionOwner owner,XPathVisitor visitor){
        m_mainExp.callVisitors(this,visitor);
    }
}
