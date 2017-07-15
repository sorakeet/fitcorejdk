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
 * $Id: Compiler.java,v 1.2.4.1 2005/09/14 19:47:10 jeffsuttor Exp $
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
 * $Id: Compiler.java,v 1.2.4.1 2005/09/14 19:47:10 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.compiler;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.QName;
import com.sun.org.apache.xml.internal.utils.SAXSourceLocator;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.axes.UnionPathIterator;
import com.sun.org.apache.xpath.internal.axes.WalkerFactory;
import com.sun.org.apache.xpath.internal.functions.FuncExtFunction;
import com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable;
import com.sun.org.apache.xpath.internal.functions.Function;
import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XString;
import com.sun.org.apache.xpath.internal.operations.*;
import com.sun.org.apache.xpath.internal.patterns.FunctionPattern;
import com.sun.org.apache.xpath.internal.patterns.NodeTest;
import com.sun.org.apache.xpath.internal.patterns.StepPattern;
import com.sun.org.apache.xpath.internal.patterns.UnionPattern;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import java.lang.String;

public class Compiler extends OpMap{
    private static final boolean DEBUG=false;
    // The current id for extension functions.
    private static long s_nextMethodId=0;
    ErrorListener m_errorHandler;
    SourceLocator m_locator;
    private int locPathDepth=-1;
    private PrefixResolver m_currentPrefixResolver=null;
    private FunctionTable m_functionTable;

    public Compiler(ErrorListener errorHandler,SourceLocator locator,
                    FunctionTable fTable){
        m_errorHandler=errorHandler;
        m_locator=locator;
        m_functionTable=fTable;
    }

    public Compiler(){
        m_errorHandler=null;
        m_locator=null;
    }

    public Expression compile(int opPos) throws TransformerException{
        int op=getOp(opPos);
        Expression expr=null;
        // System.out.println(getPatternString()+"op: "+op);
        switch(op){
            case OpCodes.OP_XPATH:
                expr=compile(opPos+2);
                break;
            case OpCodes.OP_OR:
                expr=or(opPos);
                break;
            case OpCodes.OP_AND:
                expr=and(opPos);
                break;
            case OpCodes.OP_NOTEQUALS:
                expr=notequals(opPos);
                break;
            case OpCodes.OP_EQUALS:
                expr=equals(opPos);
                break;
            case OpCodes.OP_LTE:
                expr=lte(opPos);
                break;
            case OpCodes.OP_LT:
                expr=lt(opPos);
                break;
            case OpCodes.OP_GTE:
                expr=gte(opPos);
                break;
            case OpCodes.OP_GT:
                expr=gt(opPos);
                break;
            case OpCodes.OP_PLUS:
                expr=plus(opPos);
                break;
            case OpCodes.OP_MINUS:
                expr=minus(opPos);
                break;
            case OpCodes.OP_MULT:
                expr=mult(opPos);
                break;
            case OpCodes.OP_DIV:
                expr=div(opPos);
                break;
            case OpCodes.OP_MOD:
                expr=mod(opPos);
                break;
//    case OpCodes.OP_QUO :
//      expr = quo(opPos); break;
            case OpCodes.OP_NEG:
                expr=neg(opPos);
                break;
            case OpCodes.OP_STRING:
                expr=string(opPos);
                break;
            case OpCodes.OP_BOOL:
                expr=bool(opPos);
                break;
            case OpCodes.OP_NUMBER:
                expr=number(opPos);
                break;
            case OpCodes.OP_UNION:
                expr=union(opPos);
                break;
            case OpCodes.OP_LITERAL:
                expr=literal(opPos);
                break;
            case OpCodes.OP_VARIABLE:
                expr=variable(opPos);
                break;
            case OpCodes.OP_GROUP:
                expr=group(opPos);
                break;
            case OpCodes.OP_NUMBERLIT:
                expr=numberlit(opPos);
                break;
            case OpCodes.OP_ARGUMENT:
                expr=arg(opPos);
                break;
            case OpCodes.OP_EXTFUNCTION:
                expr=compileExtension(opPos);
                break;
            case OpCodes.OP_FUNCTION:
                expr=compileFunction(opPos);
                break;
            case OpCodes.OP_LOCATIONPATH:
                expr=locationPath(opPos);
                break;
            case OpCodes.OP_PREDICATE:
                expr=null;
                break;  // should never hit this here.
            case OpCodes.OP_MATCHPATTERN:
                expr=matchPattern(opPos+2);
                break;
            case OpCodes.OP_LOCATIONPATHPATTERN:
                expr=locationPathPattern(opPos);
                break;
            case OpCodes.OP_QUO:
                error(XPATHErrorResources.ER_UNKNOWN_OPCODE,
                        new Object[]{"quo"});  //"ERROR! Unknown op code: "+m_opMap[opPos]);
                break;
            default:
                error(XPATHErrorResources.ER_UNKNOWN_OPCODE,
                        new Object[]{Integer.toString(getOp(opPos))});  //"ERROR! Unknown op code: "+m_opMap[opPos]);
        }
//    if(null != expr)
//      expr.setSourceLocator(m_locator);
        return expr;
    }

    private Expression compileOperation(Operation operation,int opPos)
            throws TransformerException{
        int leftPos=getFirstChildPos(opPos);
        int rightPos=getNextOpPos(leftPos);
        operation.setLeftRight(compile(leftPos),compile(rightPos));
        return operation;
    }

    private Expression compileUnary(UnaryOperation unary,int opPos)
            throws TransformerException{
        int rightPos=getFirstChildPos(opPos);
        unary.setRight(compile(rightPos));
        return unary;
    }

    protected Expression or(int opPos) throws TransformerException{
        return compileOperation(new Or(),opPos);
    }

    protected Expression and(int opPos) throws TransformerException{
        return compileOperation(new And(),opPos);
    }

    protected Expression notequals(int opPos) throws TransformerException{
        return compileOperation(new NotEquals(),opPos);
    }

    protected Expression equals(int opPos) throws TransformerException{
        return compileOperation(new Equals(),opPos);
    }

    protected Expression lte(int opPos) throws TransformerException{
        return compileOperation(new Lte(),opPos);
    }

    protected Expression lt(int opPos) throws TransformerException{
        return compileOperation(new Lt(),opPos);
    }
    //  protected Expression quo(int opPos) throws TransformerException
//  {
//    return compileOperation(new Quo(), opPos);
//  }

    protected Expression gte(int opPos) throws TransformerException{
        return compileOperation(new Gte(),opPos);
    }

    protected Expression gt(int opPos) throws TransformerException{
        return compileOperation(new Gt(),opPos);
    }

    protected Expression plus(int opPos) throws TransformerException{
        return compileOperation(new Plus(),opPos);
    }

    protected Expression minus(int opPos) throws TransformerException{
        return compileOperation(new Minus(),opPos);
    }

    protected Expression mult(int opPos) throws TransformerException{
        return compileOperation(new Mult(),opPos);
    }

    protected Expression div(int opPos) throws TransformerException{
        return compileOperation(new Div(),opPos);
    }

    protected Expression mod(int opPos) throws TransformerException{
        return compileOperation(new Mod(),opPos);
    }

    protected Expression neg(int opPos) throws TransformerException{
        return compileUnary(new Neg(),opPos);
    }

    protected Expression string(int opPos) throws TransformerException{
        return compileUnary(new com.sun.org.apache.xpath.internal.operations.String(),opPos);
    }

    protected Expression bool(int opPos) throws TransformerException{
        return compileUnary(new com.sun.org.apache.xpath.internal.operations.Bool(),opPos);
    }

    protected Expression number(int opPos) throws TransformerException{
        return compileUnary(new com.sun.org.apache.xpath.internal.operations.Number(),opPos);
    }

    protected Expression literal(int opPos){
        opPos=getFirstChildPos(opPos);
        return (XString)getTokenQueue().elementAt(getOp(opPos));
    }

    protected Expression numberlit(int opPos){
        opPos=getFirstChildPos(opPos);
        return (XNumber)getTokenQueue().elementAt(getOp(opPos));
    }

    protected Expression variable(int opPos) throws TransformerException{
        Variable var=new Variable();
        opPos=getFirstChildPos(opPos);
        int nsPos=getOp(opPos);
        String namespace
                =(OpCodes.EMPTY==nsPos)?null
                :(String)getTokenQueue().elementAt(nsPos);
        String localname
                =(String)getTokenQueue().elementAt(getOp(opPos+1));
        QName qname=new QName(namespace,localname);
        var.setQName(qname);
        return var;
    }

    protected Expression group(int opPos) throws TransformerException{
        // no-op
        return compile(opPos+2);
    }

    protected Expression arg(int opPos) throws TransformerException{
        // no-op
        return compile(opPos+2);
    }

    protected Expression union(int opPos) throws TransformerException{
        locPathDepth++;
        try{
            return UnionPathIterator.createUnionIterator(this,opPos);
        }finally{
            locPathDepth--;
        }
    }

    public int getLocationPathDepth(){
        return locPathDepth;
    }

    FunctionTable getFunctionTable(){
        return m_functionTable;
    }

    public Expression locationPath(int opPos) throws TransformerException{
        locPathDepth++;
        try{
            DTMIterator iter=WalkerFactory.newDTMIterator(this,opPos,(locPathDepth==0));
            return (Expression)iter; // cast OK, I guess.
        }finally{
            locPathDepth--;
        }
    }

    public Expression predicate(int opPos) throws TransformerException{
        return compile(opPos+2);
    }

    protected Expression matchPattern(int opPos) throws TransformerException{
        locPathDepth++;
        try{
            // First, count...
            int nextOpPos=opPos;
            int i;
            for(i=0;getOp(nextOpPos)==OpCodes.OP_LOCATIONPATHPATTERN;i++){
                nextOpPos=getNextOpPos(nextOpPos);
            }
            if(i==1)
                return compile(opPos);
            UnionPattern up=new UnionPattern();
            StepPattern[] patterns=new StepPattern[i];
            for(i=0;getOp(opPos)==OpCodes.OP_LOCATIONPATHPATTERN;i++){
                nextOpPos=getNextOpPos(opPos);
                patterns[i]=(StepPattern)compile(opPos);
                opPos=nextOpPos;
            }
            up.setPatterns(patterns);
            return up;
        }finally{
            locPathDepth--;
        }
    }

    public Expression locationPathPattern(int opPos)
            throws TransformerException{
        opPos=getFirstChildPos(opPos);
        return stepPattern(opPos,0,null);
    }

    public int getWhatToShow(int opPos){
        int axesType=getOp(opPos);
        int testType=getOp(opPos+3);
        // System.out.println("testType: "+testType);
        switch(testType){
            case OpCodes.NODETYPE_COMMENT:
                return DTMFilter.SHOW_COMMENT;
            case OpCodes.NODETYPE_TEXT:
//      return DTMFilter.SHOW_TEXT | DTMFilter.SHOW_COMMENT;
                return DTMFilter.SHOW_TEXT|DTMFilter.SHOW_CDATA_SECTION;
            case OpCodes.NODETYPE_PI:
                return DTMFilter.SHOW_PROCESSING_INSTRUCTION;
            case OpCodes.NODETYPE_NODE:
//      return DTMFilter.SHOW_ALL;
                switch(axesType){
                    case OpCodes.FROM_NAMESPACE:
                        return DTMFilter.SHOW_NAMESPACE;
                    case OpCodes.FROM_ATTRIBUTES:
                    case OpCodes.MATCH_ATTRIBUTE:
                        return DTMFilter.SHOW_ATTRIBUTE;
                    case OpCodes.FROM_SELF:
                    case OpCodes.FROM_ANCESTORS_OR_SELF:
                    case OpCodes.FROM_DESCENDANTS_OR_SELF:
                        return DTMFilter.SHOW_ALL;
                    default:
                        if(getOp(0)==OpCodes.OP_MATCHPATTERN)
                            return ~DTMFilter.SHOW_ATTRIBUTE
                                    &~DTMFilter.SHOW_DOCUMENT
                                    &~DTMFilter.SHOW_DOCUMENT_FRAGMENT;
                        else
                            return ~DTMFilter.SHOW_ATTRIBUTE;
                }
            case OpCodes.NODETYPE_ROOT:
                return DTMFilter.SHOW_DOCUMENT|DTMFilter.SHOW_DOCUMENT_FRAGMENT;
            case OpCodes.NODETYPE_FUNCTEST:
                return NodeTest.SHOW_BYFUNCTION;
            case OpCodes.NODENAME:
                switch(axesType){
                    case OpCodes.FROM_NAMESPACE:
                        return DTMFilter.SHOW_NAMESPACE;
                    case OpCodes.FROM_ATTRIBUTES:
                    case OpCodes.MATCH_ATTRIBUTE:
                        return DTMFilter.SHOW_ATTRIBUTE;
                    // break;
                    case OpCodes.MATCH_ANY_ANCESTOR:
                    case OpCodes.MATCH_IMMEDIATE_ANCESTOR:
                        return DTMFilter.SHOW_ELEMENT;
                    // break;
                    default:
                        return DTMFilter.SHOW_ELEMENT;
                }
            default:
                // System.err.println("We should never reach here.");
                return DTMFilter.SHOW_ALL;
        }
    }

    protected StepPattern stepPattern(
            int opPos,int stepCount,StepPattern ancestorPattern)
            throws TransformerException{
        int startOpPos=opPos;
        int stepType=getOp(opPos);
        if(OpCodes.ENDOP==stepType){
            return null;
        }
        boolean addMagicSelf=true;
        int endStep=getNextOpPos(opPos);
        // int nextStepType = getOpMap()[endStep];
        StepPattern pattern;
        // boolean isSimple = ((OpCodes.ENDOP == nextStepType) && (stepCount == 0));
        int argLen;
        switch(stepType){
            case OpCodes.OP_FUNCTION:
                if(DEBUG)
                    System.out.println("MATCH_FUNCTION: "+m_currentPattern);
                addMagicSelf=false;
                argLen=getOp(opPos+OpMap.MAPINDEX_LENGTH);
                pattern=new FunctionPattern(compileFunction(opPos),Axis.PARENT,Axis.CHILD);
                break;
            case OpCodes.FROM_ROOT:
                if(DEBUG)
                    System.out.println("FROM_ROOT, "+m_currentPattern);
                addMagicSelf=false;
                argLen=getArgLengthOfStep(opPos);
                opPos=getFirstChildPosOfStep(opPos);
                pattern=new StepPattern(DTMFilter.SHOW_DOCUMENT|
                        DTMFilter.SHOW_DOCUMENT_FRAGMENT,
                        Axis.PARENT,Axis.CHILD);
                break;
            case OpCodes.MATCH_ATTRIBUTE:
                if(DEBUG)
                    System.out.println("MATCH_ATTRIBUTE: "+getStepLocalName(startOpPos)+", "+m_currentPattern);
                argLen=getArgLengthOfStep(opPos);
                opPos=getFirstChildPosOfStep(opPos);
                pattern=new StepPattern(DTMFilter.SHOW_ATTRIBUTE,
                        getStepNS(startOpPos),
                        getStepLocalName(startOpPos),
                        Axis.PARENT,Axis.ATTRIBUTE);
                break;
            case OpCodes.MATCH_ANY_ANCESTOR:
                if(DEBUG)
                    System.out.println("MATCH_ANY_ANCESTOR: "+getStepLocalName(startOpPos)+", "+m_currentPattern);
                argLen=getArgLengthOfStep(opPos);
                opPos=getFirstChildPosOfStep(opPos);
                int what=getWhatToShow(startOpPos);
                // bit-o-hackery, but this code is due for the morgue anyway...
                if(0x00000500==what)
                    addMagicSelf=false;
                pattern=new StepPattern(getWhatToShow(startOpPos),
                        getStepNS(startOpPos),
                        getStepLocalName(startOpPos),
                        Axis.ANCESTOR,Axis.CHILD);
                break;
            case OpCodes.MATCH_IMMEDIATE_ANCESTOR:
                if(DEBUG)
                    System.out.println("MATCH_IMMEDIATE_ANCESTOR: "+getStepLocalName(startOpPos)+", "+m_currentPattern);
                argLen=getArgLengthOfStep(opPos);
                opPos=getFirstChildPosOfStep(opPos);
                pattern=new StepPattern(getWhatToShow(startOpPos),
                        getStepNS(startOpPos),
                        getStepLocalName(startOpPos),
                        Axis.PARENT,Axis.CHILD);
                break;
            default:
                error(XPATHErrorResources.ER_UNKNOWN_MATCH_OPERATION,null);  //"unknown match operation!");
                return null;
        }
        pattern.setPredicates(getCompiledPredicates(opPos+argLen));
        if(null==ancestorPattern){
            // This is the magic and invisible "." at the head of every
            // match pattern, and corresponds to the current node in the context
            // list, from where predicates are counted.
            // So, in order to calculate "foo[3]", it has to count from the
            // current node in the context list, so, from that current node,
            // the full pattern is really "self::node()/child::foo[3]".  If you
            // translate this to a select pattern from the node being tested,
            // which is really how we're treating match patterns, it works out to
            // self::foo/parent::node[child::foo[3]]", or close enough.
            /**      if(addMagicSelf && pattern.getPredicateCount() > 0)
             {
             StepPattern selfPattern = new StepPattern(DTMFilter.SHOW_ALL,
             Axis.PARENT, Axis.CHILD);
             // We need to keep the new nodetest from affecting the score...
             XNumber score = pattern.getStaticScore();
             pattern.setRelativePathPattern(selfPattern);
             pattern.setStaticScore(score);
             selfPattern.setStaticScore(score);
             }*/
        }else{
            // System.out.println("Setting "+ancestorPattern+" as relative to "+pattern);
            pattern.setRelativePathPattern(ancestorPattern);
        }
        StepPattern relativePathPattern=stepPattern(endStep,stepCount+1,
                pattern);
        return (null!=relativePathPattern)?relativePathPattern:pattern;
    }

    public Expression[] getCompiledPredicates(int opPos)
            throws TransformerException{
        int count=countPredicates(opPos);
        if(count>0){
            Expression[] predicates=new Expression[count];
            compilePredicates(opPos,predicates);
            return predicates;
        }
        return null;
    }

    public int countPredicates(int opPos) throws TransformerException{
        int count=0;
        while(OpCodes.OP_PREDICATE==getOp(opPos)){
            count++;
            opPos=getNextOpPos(opPos);
        }
        return count;
    }

    private void compilePredicates(int opPos,Expression[] predicates)
            throws TransformerException{
        for(int i=0;OpCodes.OP_PREDICATE==getOp(opPos);i++){
            predicates[i]=predicate(opPos);
            opPos=getNextOpPos(opPos);
        }
    }

    Expression compileFunction(int opPos) throws TransformerException{
        int endFunc=opPos+getOp(opPos+1)-1;
        opPos=getFirstChildPos(opPos);
        int funcID=getOp(opPos);
        opPos++;
        if(-1!=funcID){
            Function func=m_functionTable.getFunction(funcID);
            /**
             * It is a trick for function-available. Since the function table is an
             * instance field, insert this table at compilation time for later usage
             */
            if(func instanceof FuncExtFunctionAvailable)
                ((FuncExtFunctionAvailable)func).setFunctionTable(m_functionTable);
            func.postCompileStep(this);
            try{
                int i=0;
                for(int p=opPos;p<endFunc;p=getNextOpPos(p),i++){
                    // System.out.println("argPos: "+ p);
                    // System.out.println("argCode: "+ m_opMap[p]);
                    func.setArg(compile(p),i);
                }
                func.checkNumberArgs(i);
            }catch(WrongNumberArgsException wnae){
                String name=m_functionTable.getFunctionName(funcID);
                m_errorHandler.fatalError(new TransformerException(
                        XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ONLY_ALLOWS,
                                new Object[]{name,wnae.getMessage()}),m_locator));
                //"name + " only allows " + wnae.getMessage() + " arguments", m_locator));
            }
            return func;
        }else{
            error(XPATHErrorResources.ER_FUNCTION_TOKEN_NOT_FOUND,null);  //"function token not found.");
            return null;
        }
    }

    synchronized private long getNextMethodId(){
        if(s_nextMethodId==Long.MAX_VALUE)
            s_nextMethodId=0;
        return s_nextMethodId++;
    }

    private Expression compileExtension(int opPos)
            throws TransformerException{
        int endExtFunc=opPos+getOp(opPos+1)-1;
        opPos=getFirstChildPos(opPos);
        String ns=(String)getTokenQueue().elementAt(getOp(opPos));
        opPos++;
        String funcName=
                (String)getTokenQueue().elementAt(getOp(opPos));
        opPos++;
        // We create a method key to uniquely identify this function so that we
        // can cache the object needed to invoke it.  This way, we only pay the
        // reflection overhead on the first call.
        Function extension=new FuncExtFunction(ns,funcName,String.valueOf(getNextMethodId()));
        try{
            int i=0;
            while(opPos<endExtFunc){
                int nextOpPos=getNextOpPos(opPos);
                extension.setArg(this.compile(opPos),i);
                opPos=nextOpPos;
                i++;
            }
        }catch(WrongNumberArgsException wnae){
            ;  // should never happen
        }
        return extension;
    }

    public void warn(String msg,Object[] args) throws TransformerException{
        String fmsg=XSLMessages.createXPATHWarning(msg,args);
        if(null!=m_errorHandler){
            m_errorHandler.warning(new TransformerException(fmsg,m_locator));
        }else{
            System.out.println(fmsg
                    +"; file "+m_locator.getSystemId()
                    +"; line "+m_locator.getLineNumber()
                    +"; column "+m_locator.getColumnNumber());
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

    public void error(String msg,Object[] args) throws TransformerException{
        String fmsg=XSLMessages.createXPATHMessage(msg,args);
        if(null!=m_errorHandler){
            m_errorHandler.fatalError(new TransformerException(fmsg,m_locator));
        }else{
            // System.out.println(te.getMessage()
            //                    +"; file "+te.getSystemId()
            //                    +"; line "+te.getLineNumber()
            //                    +"; column "+te.getColumnNumber());
            throw new TransformerException(fmsg,(SAXSourceLocator)m_locator);
        }
    }

    public PrefixResolver getNamespaceContext(){
        return m_currentPrefixResolver;
    }

    public void setNamespaceContext(PrefixResolver pr){
        m_currentPrefixResolver=pr;
    }
}
