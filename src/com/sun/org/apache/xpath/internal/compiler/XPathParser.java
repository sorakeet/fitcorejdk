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
 * $Id: XPathParser.java,v 1.2.4.1 2005/09/14 19:46:02 jeffsuttor Exp $
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
 * $Id: XPathParser.java,v 1.2.4.1 2005/09/14 19:46:02 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.compiler;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.XPathProcessorException;
import com.sun.org.apache.xpath.internal.domapi.XPathStylesheetDOM3Exception;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XString;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class XPathParser{
    // %REVIEW% Is there a better way of doing this?
    // Upside is minimum object churn. Downside is that we don't have a useful
    // backtrace in the exception itself -- but we don't expect to need one.
    static public final String CONTINUE_AFTER_FATAL_ERROR="CONTINUE_AFTER_FATAL_ERROR";
    protected final static int FILTER_MATCH_FAILED=0;
    protected final static int FILTER_MATCH_PRIMARY=1;
    protected final static int FILTER_MATCH_PREDICATES=2;
    transient String m_token;
    transient char m_tokenChar=0;
    int m_queueMark=0;
    PrefixResolver m_namespaceContext;
    javax.xml.transform.SourceLocator m_sourceLocator;
    private OpMap m_ops;
    private ErrorListener m_errorListener;
    private FunctionTable m_functionTable;

    public XPathParser(ErrorListener errorListener,javax.xml.transform.SourceLocator sourceLocator){
        m_errorListener=errorListener;
        m_sourceLocator=sourceLocator;
    }

    public void initXPath(
            Compiler compiler,String expression,PrefixResolver namespaceContext)
            throws TransformerException{
        m_ops=compiler;
        m_namespaceContext=namespaceContext;
        m_functionTable=compiler.getFunctionTable();
        Lexer lexer=new Lexer(compiler,namespaceContext,this);
        lexer.tokenize(expression);
        m_ops.setOp(0,OpCodes.OP_XPATH);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,2);
        // Patch for Christine's gripe. She wants her errorHandler to return from
        // a fatal error and continue trying to parse, rather than throwing an exception.
        // Without the patch, that put us into an endless loop.
        //
        // %REVIEW% Is there a better way of doing this?
        // %REVIEW% Are there any other cases which need the safety net?
        //      (and if so do we care right now, or should we rewrite the XPath
        //      grammar engine and can fix it at that time?)
        try{
            nextToken();
            Expr();
            if(null!=m_token){
                String extraTokens="";
                while(null!=m_token){
                    extraTokens+="'"+m_token+"'";
                    nextToken();
                    if(null!=m_token)
                        extraTokens+=", ";
                }
                error(XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS,
                        new Object[]{extraTokens});  //"Extra illegal tokens: "+extraTokens);
            }
        }catch(XPathProcessorException e){
            if(CONTINUE_AFTER_FATAL_ERROR.equals(e.getMessage())){
                // What I _want_ to do is null out this XPath.
                // I doubt this has the desired effect, but I'm not sure what else to do.
                // %REVIEW%!!!
                initXPath(compiler,"/..",namespaceContext);
            }else
                throw e;
        }
        compiler.shrink();
    }

    public void initMatchPattern(
            Compiler compiler,String expression,PrefixResolver namespaceContext)
            throws TransformerException{
        m_ops=compiler;
        m_namespaceContext=namespaceContext;
        m_functionTable=compiler.getFunctionTable();
        Lexer lexer=new Lexer(compiler,namespaceContext,this);
        lexer.tokenize(expression);
        m_ops.setOp(0,OpCodes.OP_MATCHPATTERN);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,2);
        nextToken();
        Pattern();
        if(null!=m_token){
            String extraTokens="";
            while(null!=m_token){
                extraTokens+="'"+m_token+"'";
                nextToken();
                if(null!=m_token)
                    extraTokens+=", ";
            }
            error(XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS,
                    new Object[]{extraTokens});  //"Extra illegal tokens: "+extraTokens);
        }
        // Terminate for safety.
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ENDOP);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        m_ops.shrink();
    }

    public void setErrorHandler(ErrorListener handler){
        m_errorListener=handler;
    }

    final boolean tokenIs(char c){
        return (m_token!=null)?(m_tokenChar==c):false;
    }

    final boolean lookahead(char c,int n){
        int pos=(m_queueMark+n);
        boolean b;
        if((pos<=m_ops.getTokenQueueSize())&&(pos>0)
                &&(m_ops.getTokenQueueSize()!=0)){
            String tok=((String)m_ops.m_tokenQueue.elementAt(pos-1));
            b=(tok.length()==1)?(tok.charAt(0)==c):false;
        }else{
            b=false;
        }
        return b;
    }

    private final boolean lookbehind(char c,int n){
        boolean isToken;
        int lookBehindPos=m_queueMark-(n+1);
        if(lookBehindPos>=0){
            String lookbehind=(String)m_ops.m_tokenQueue.elementAt(lookBehindPos);
            if(lookbehind.length()==1){
                char c0=(lookbehind==null)?'|':lookbehind.charAt(0);
                isToken=(c0=='|')?false:(c0==c);
            }else{
                isToken=false;
            }
        }else{
            isToken=false;
        }
        return isToken;
    }

    private final boolean lookbehindHasToken(int n){
        boolean hasToken;
        if((m_queueMark-n)>0){
            String lookbehind=(String)m_ops.m_tokenQueue.elementAt(m_queueMark-(n-1));
            char c0=(lookbehind==null)?'|':lookbehind.charAt(0);
            hasToken=(c0=='|')?false:true;
        }else{
            hasToken=false;
        }
        return hasToken;
    }

    private final boolean lookahead(String s,int n){
        boolean isToken;
        if((m_queueMark+n)<=m_ops.getTokenQueueSize()){
            String lookahead=(String)m_ops.m_tokenQueue.elementAt(m_queueMark+(n-1));
            isToken=(lookahead!=null)?lookahead.equals(s):(s==null);
        }else{
            isToken=(null==s);
        }
        return isToken;
    }

    private final String getTokenRelative(int i){
        String tok;
        int relative=m_queueMark+i;
        if((relative>0)&&(relative<m_ops.getTokenQueueSize())){
            tok=(String)m_ops.m_tokenQueue.elementAt(relative);
        }else{
            tok=null;
        }
        return tok;
    }

    private final void prevToken(){
        if(m_queueMark>0){
            m_queueMark--;
            m_token=(String)m_ops.m_tokenQueue.elementAt(m_queueMark);
            m_tokenChar=m_token.charAt(0);
        }else{
            m_token=null;
            m_tokenChar=0;
        }
    }

    private final void consumeExpected(String expected)
            throws TransformerException{
        if(tokenIs(expected)){
            nextToken();
        }else{
            error(XPATHErrorResources.ER_EXPECTED_BUT_FOUND,new Object[]{expected,
                    m_token});  //"Expected "+expected+", but found: "+m_token);
            // Patch for Christina's gripe. She wants her errorHandler to return from
            // this error and continue trying to parse, rather than throwing an exception.
            // Without the patch, that put us into an endless loop.
            throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
        }
    }

    final boolean tokenIs(String s){
        return (m_token!=null)?(m_token.equals(s)):(s==null);
    }

    private final void nextToken(){
        if(m_queueMark<m_ops.getTokenQueueSize()){
            m_token=(String)m_ops.m_tokenQueue.elementAt(m_queueMark++);
            m_tokenChar=m_token.charAt(0);
        }else{
            m_token=null;
            m_tokenChar=0;
        }
    }

    void error(String msg,Object[] args) throws TransformerException{
        String fmsg=XSLMessages.createXPATHMessage(msg,args);
        ErrorListener ehandler=this.getErrorListener();
        TransformerException te=new TransformerException(fmsg,m_sourceLocator);
        if(null!=ehandler){
            // TO DO: Need to get stylesheet Locator from here.
            ehandler.fatalError(te);
        }else{
            // System.err.println(fmsg);
            throw te;
        }
    }

    public ErrorListener getErrorListener(){
        return m_errorListener;
    }

    private final void consumeExpected(char expected)
            throws TransformerException{
        if(tokenIs(expected)){
            nextToken();
        }else{
            error(XPATHErrorResources.ER_EXPECTED_BUT_FOUND,
                    new Object[]{String.valueOf(expected),
                            m_token});  //"Expected "+expected+", but found: "+m_token);
            // Patch for Christina's gripe. She wants her errorHandler to return from
            // this error and continue trying to parse, rather than throwing an exception.
            // Without the patch, that put us into an endless loop.
            throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
        }
    }

    void warn(String msg,Object[] args) throws TransformerException{
        String fmsg=XSLMessages.createXPATHWarning(msg,args);
        ErrorListener ehandler=this.getErrorListener();
        if(null!=ehandler){
            // TO DO: Need to get stylesheet Locator from here.
            ehandler.warning(new TransformerException(fmsg,m_sourceLocator));
        }else{
            // Should never happen.
            System.err.println(fmsg);
        }
    }

    private void assertion(boolean b,String msg){
        if(!b){
            String fMsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
                    new Object[]{msg});
            throw new RuntimeException(fMsg);
        }
    }

    void errorForDOM3(String msg,Object[] args) throws TransformerException{
        String fmsg=XSLMessages.createXPATHMessage(msg,args);
        ErrorListener ehandler=this.getErrorListener();
        TransformerException te=new XPathStylesheetDOM3Exception(fmsg,m_sourceLocator);
        if(null!=ehandler){
            // TO DO: Need to get stylesheet Locator from here.
            ehandler.fatalError(te);
        }else{
            // System.err.println(fmsg);
            throw te;
        }
    }

    protected String dumpRemainingTokenQueue(){
        int q=m_queueMark;
        String returnMsg;
        if(q<m_ops.getTokenQueueSize()){
            String msg="\n Remaining tokens: (";
            while(q<m_ops.getTokenQueueSize()){
                String t=(String)m_ops.m_tokenQueue.elementAt(q++);
                msg+=(" '"+t+"'");
            }
            returnMsg=msg+")";
        }else{
            returnMsg="";
        }
        return returnMsg;
    }

    final int getFunctionToken(String key){
        int tok;
        Object id;
        try{
            // These are nodetests, xpathparser treats them as functions when parsing
            // a FilterExpr.
            id=Keywords.lookupNodeTest(key);
            if(null==id) id=m_functionTable.getFunctionID(key);
            tok=((Integer)id).intValue();
        }catch(NullPointerException npe){
            tok=-1;
        }catch(ClassCastException cce){
            tok=-1;
        }
        return tok;
    }

    void insertOp(int pos,int length,int op){
        int totalLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        for(int i=totalLen-1;i>=pos;i--){
            m_ops.setOp(i+length,m_ops.getOp(i));
        }
        m_ops.setOp(pos,op);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,totalLen+length);
    }

    void appendOp(int length,int op){
        int totalLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        m_ops.setOp(totalLen,op);
        m_ops.setOp(totalLen+OpMap.MAPINDEX_LENGTH,length);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,totalLen+length);
    }
    // ============= EXPRESSIONS FUNCTIONS =================

    protected void Expr() throws TransformerException{
        OrExpr();
    }

    protected void OrExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        AndExpr();
        if((null!=m_token)&&tokenIs("or")){
            nextToken();
            insertOp(opPos,2,OpCodes.OP_OR);
            OrExpr();
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
        }
    }

    protected void AndExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        EqualityExpr(-1);
        if((null!=m_token)&&tokenIs("and")){
            nextToken();
            insertOp(opPos,2,OpCodes.OP_AND);
            AndExpr();
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
        }
    }

    protected int EqualityExpr(int addPos) throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        if(-1==addPos)
            addPos=opPos;
        RelationalExpr(-1);
        if(null!=m_token){
            if(tokenIs('!')&&lookahead('=',1)){
                nextToken();
                nextToken();
                insertOp(addPos,2,OpCodes.OP_NOTEQUALS);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=EqualityExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }else if(tokenIs('=')){
                nextToken();
                insertOp(addPos,2,OpCodes.OP_EQUALS);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=EqualityExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }
        }
        return addPos;
    }

    protected int RelationalExpr(int addPos) throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        if(-1==addPos)
            addPos=opPos;
        AdditiveExpr(-1);
        if(null!=m_token){
            if(tokenIs('<')){
                nextToken();
                if(tokenIs('=')){
                    nextToken();
                    insertOp(addPos,2,OpCodes.OP_LTE);
                }else{
                    insertOp(addPos,2,OpCodes.OP_LT);
                }
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=RelationalExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }else if(tokenIs('>')){
                nextToken();
                if(tokenIs('=')){
                    nextToken();
                    insertOp(addPos,2,OpCodes.OP_GTE);
                }else{
                    insertOp(addPos,2,OpCodes.OP_GT);
                }
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=RelationalExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }
        }
        return addPos;
    }

    protected int AdditiveExpr(int addPos) throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        if(-1==addPos)
            addPos=opPos;
        MultiplicativeExpr(-1);
        if(null!=m_token){
            if(tokenIs('+')){
                nextToken();
                insertOp(addPos,2,OpCodes.OP_PLUS);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=AdditiveExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }else if(tokenIs('-')){
                nextToken();
                insertOp(addPos,2,OpCodes.OP_MINUS);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=AdditiveExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }
        }
        return addPos;
    }

    protected int MultiplicativeExpr(int addPos) throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        if(-1==addPos)
            addPos=opPos;
        UnaryExpr();
        if(null!=m_token){
            if(tokenIs('*')){
                nextToken();
                insertOp(addPos,2,OpCodes.OP_MULT);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=MultiplicativeExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }else if(tokenIs("div")){
                nextToken();
                insertOp(addPos,2,OpCodes.OP_DIV);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=MultiplicativeExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }else if(tokenIs("mod")){
                nextToken();
                insertOp(addPos,2,OpCodes.OP_MOD);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=MultiplicativeExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }else if(tokenIs("quo")){
                nextToken();
                insertOp(addPos,2,OpCodes.OP_QUO);
                int opPlusLeftHandLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-addPos;
                addPos=MultiplicativeExpr(addPos);
                m_ops.setOp(addPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(addPos+opPlusLeftHandLen+1)+opPlusLeftHandLen);
                addPos+=2;
            }
        }
        return addPos;
    }

    protected void UnaryExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        boolean isNeg=false;
        if(m_tokenChar=='-'){
            nextToken();
            appendOp(2,OpCodes.OP_NEG);
            isNeg=true;
        }
        UnionExpr();
        if(isNeg)
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected void StringExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        appendOp(2,OpCodes.OP_STRING);
        Expr();
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected void BooleanExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        appendOp(2,OpCodes.OP_BOOL);
        Expr();
        int opLen=m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos;
        if(opLen==2){
            error(XPATHErrorResources.ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,null);  //"boolean(...) argument is no longer optional with 19990709 XPath draft.");
        }
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,opLen);
    }

    protected void NumberExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        appendOp(2,OpCodes.OP_NUMBER);
        Expr();
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected void UnionExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        boolean continueOrLoop=true;
        boolean foundUnion=false;
        do{
            PathExpr();
            if(tokenIs('|')){
                if(false==foundUnion){
                    foundUnion=true;
                    insertOp(opPos,2,OpCodes.OP_UNION);
                }
                nextToken();
            }else{
                break;
            }
            // this.m_testForDocOrder = true;
        }
        while(continueOrLoop);
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected void PathExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        int filterExprMatch=FilterExpr();
        if(filterExprMatch!=FILTER_MATCH_FAILED){
            // If FilterExpr had Predicates, a OP_LOCATIONPATH opcode would already
            // have been inserted.
            boolean locationPathStarted=(filterExprMatch==FILTER_MATCH_PREDICATES);
            if(tokenIs('/')){
                nextToken();
                if(!locationPathStarted){
                    // int locationPathOpPos = opPos;
                    insertOp(opPos,2,OpCodes.OP_LOCATIONPATH);
                    locationPathStarted=true;
                }
                if(!RelativeLocationPath()){
                    // "Relative location path expected following '/' or '//'"
                    error(XPATHErrorResources.ER_EXPECTED_REL_LOC_PATH,null);
                }
            }
            // Terminate for safety.
            if(locationPathStarted){
                m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ENDOP);
                m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
                m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                        m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
            }
        }else{
            LocationPath();
        }
    }

    protected int FilterExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        int filterMatch;
        if(PrimaryExpr()){
            if(tokenIs('[')){
                // int locationPathOpPos = opPos;
                insertOp(opPos,2,OpCodes.OP_LOCATIONPATH);
                while(tokenIs('[')){
                    Predicate();
                }
                filterMatch=FILTER_MATCH_PREDICATES;
            }else{
                filterMatch=FILTER_MATCH_PRIMARY;
            }
        }else{
            filterMatch=FILTER_MATCH_FAILED;
        }
        return filterMatch;
        /**
         * if(tokenIs('['))
         * {
         *   Predicate();
         *   m_ops.m_opMap[opPos + OpMap.MAPINDEX_LENGTH] = m_ops.m_opMap[OpMap.MAPINDEX_LENGTH] - opPos;
         * }
         */
    }

    protected boolean PrimaryExpr() throws TransformerException{
        boolean matchFound;
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        if((m_tokenChar=='\'')||(m_tokenChar=='"')){
            appendOp(2,OpCodes.OP_LITERAL);
            Literal();
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
            matchFound=true;
        }else if(m_tokenChar=='$'){
            nextToken();  // consume '$'
            appendOp(2,OpCodes.OP_VARIABLE);
            QName();
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
            matchFound=true;
        }else if(m_tokenChar=='('){
            nextToken();
            appendOp(2,OpCodes.OP_GROUP);
            Expr();
            consumeExpected(')');
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
            matchFound=true;
        }else if((null!=m_token)&&((('.'==m_tokenChar)&&(m_token.length()>1)&&Character.isDigit(
                m_token.charAt(1)))||Character.isDigit(m_tokenChar))){
            appendOp(2,OpCodes.OP_NUMBERLIT);
            Number();
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
            matchFound=true;
        }else if(lookahead('(',1)||(lookahead(':',1)&&lookahead('(',3))){
            matchFound=FunctionCall();
        }else{
            matchFound=false;
        }
        return matchFound;
    }

    protected void Argument() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        appendOp(2,OpCodes.OP_ARGUMENT);
        Expr();
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected boolean FunctionCall() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        if(lookahead(':',1)){
            appendOp(4,OpCodes.OP_EXTFUNCTION);
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH+1,m_queueMark-1);
            nextToken();
            consumeExpected(':');
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH+2,m_queueMark-1);
            nextToken();
        }else{
            int funcTok=getFunctionToken(m_token);
            if(-1==funcTok){
                error(XPATHErrorResources.ER_COULDNOT_FIND_FUNCTION,
                        new Object[]{m_token});  //"Could not find function: "+m_token+"()");
            }
            switch(funcTok){
                case OpCodes.NODETYPE_PI:
                case OpCodes.NODETYPE_COMMENT:
                case OpCodes.NODETYPE_TEXT:
                case OpCodes.NODETYPE_NODE:
                    // Node type tests look like function calls, but they're not
                    return false;
                default:
                    appendOp(3,OpCodes.OP_FUNCTION);
                    m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH+1,funcTok);
            }
            nextToken();
        }
        consumeExpected('(');
        while(!tokenIs(')')&&m_token!=null){
            if(tokenIs(',')){
                error(XPATHErrorResources.ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,null);  //"Found ',' but no preceding argument!");
            }
            Argument();
            if(!tokenIs(')')){
                consumeExpected(',');
                if(tokenIs(')')){
                    error(XPATHErrorResources.ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
                            null);  //"Found ',' but no following argument!");
                }
            }
        }
        consumeExpected(')');
        // Terminate for safety.
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ENDOP);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
        return true;
    }
    // ============= GRAMMAR FUNCTIONS =================

    protected void LocationPath() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        // int locationPathOpPos = opPos;
        appendOp(2,OpCodes.OP_LOCATIONPATH);
        boolean seenSlash=tokenIs('/');
        if(seenSlash){
            appendOp(4,OpCodes.FROM_ROOT);
            // Tell how long the step is without the predicate
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-2,4);
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-1,OpCodes.NODETYPE_ROOT);
            nextToken();
        }else if(m_token==null){
            error(XPATHErrorResources.ER_EXPECTED_LOC_PATH_AT_END_EXPR,null);
        }
        if(m_token!=null){
            if(!RelativeLocationPath()&&!seenSlash){
                // Neither a '/' nor a RelativeLocationPath - i.e., matched nothing
                // "Location path expected, but found "+m_token+" was encountered."
                error(XPATHErrorResources.ER_EXPECTED_LOC_PATH,
                        new Object[]{m_token});
            }
        }
        // Terminate for safety.
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ENDOP);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected boolean RelativeLocationPath()
            throws TransformerException{
        if(!Step()){
            return false;
        }
        while(tokenIs('/')){
            nextToken();
            if(!Step()){
                // RelativeLocationPath can't end with a trailing '/'
                // "Location step expected following '/' or '//'"
                error(XPATHErrorResources.ER_EXPECTED_LOC_STEP,null);
            }
        }
        return true;
    }

    protected boolean Step() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        boolean doubleSlash=tokenIs('/');
        // At most a single '/' before each Step is consumed by caller; if the
        // first thing is a '/', that means we had '//' and the Step must not
        // be empty.
        if(doubleSlash){
            nextToken();
            appendOp(2,OpCodes.FROM_DESCENDANTS_OR_SELF);
            // Have to fix up for patterns such as '//@foo' or '//attribute::foo',
            // which translate to 'descendant-or-self::node()/attribute::foo'.
            // notice I leave the '/' on the queue, so the next will be processed
            // by a regular step pattern.
            // Make room for telling how long the step is without the predicate
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.NODETYPE_NODE);
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            // Tell how long the step is without the predicate
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH+1,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
            // Tell how long the step is with the predicate
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
            opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        }
        if(tokenIs(".")){
            nextToken();
            if(tokenIs('[')){
                error(XPATHErrorResources.ER_PREDICATE_ILLEGAL_SYNTAX,null);  //"'..[predicate]' or '.[predicate]' is illegal syntax.  Use 'self::node()[predicate]' instead.");
            }
            appendOp(4,OpCodes.FROM_SELF);
            // Tell how long the step is without the predicate
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-2,4);
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-1,OpCodes.NODETYPE_NODE);
        }else if(tokenIs("..")){
            nextToken();
            appendOp(4,OpCodes.FROM_PARENT);
            // Tell how long the step is without the predicate
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-2,4);
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-1,OpCodes.NODETYPE_NODE);
        }
        // There is probably a better way to test for this
        // transition... but it gets real hairy if you try
        // to do it in basis().
        else if(tokenIs('*')||tokenIs('@')||tokenIs('_')
                ||(m_token!=null&&Character.isLetter(m_token.charAt(0)))){
            Basis();
            while(tokenIs('[')){
                Predicate();
            }
            // Tell how long the entire step is.
            m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                    m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
        }else{
            // No Step matched - that's an error if previous thing was a '//'
            if(doubleSlash){
                // "Location step expected following '/' or '//'"
                error(XPATHErrorResources.ER_EXPECTED_LOC_STEP,null);
            }
            return false;
        }
        return true;
    }

    protected void Basis() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        int axesType;
        // The next blocks guarantee that a FROM_XXX will be added.
        if(lookahead("::",1)){
            axesType=AxisName();
            nextToken();
            nextToken();
        }else if(tokenIs('@')){
            axesType=OpCodes.FROM_ATTRIBUTES;
            appendOp(2,axesType);
            nextToken();
        }else{
            axesType=OpCodes.FROM_CHILDREN;
            appendOp(2,axesType);
        }
        // Make room for telling how long the step is without the predicate
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        NodeTest(axesType);
        // Tell how long the step is without the predicate
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH+1,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected int AxisName() throws TransformerException{
        Object val=Keywords.getAxisName(m_token);
        if(null==val){
            error(XPATHErrorResources.ER_ILLEGAL_AXIS_NAME,
                    new Object[]{m_token});  //"illegal axis name: "+m_token);
        }
        int axesType=((Integer)val).intValue();
        appendOp(2,axesType);
        return axesType;
    }

    protected void NodeTest(int axesType) throws TransformerException{
        if(lookahead('(',1)){
            Object nodeTestOp=Keywords.getNodeType(m_token);
            if(null==nodeTestOp){
                error(XPATHErrorResources.ER_UNKNOWN_NODETYPE,
                        new Object[]{m_token});  //"Unknown nodetype: "+m_token);
            }else{
                nextToken();
                int nt=((Integer)nodeTestOp).intValue();
                m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),nt);
                m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
                consumeExpected('(');
                if(OpCodes.NODETYPE_PI==nt){
                    if(!tokenIs(')')){
                        Literal();
                    }
                }
                consumeExpected(')');
            }
        }else{
            // Assume name of attribute or element.
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.NODENAME);
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            if(lookahead(':',1)){
                if(tokenIs('*')){
                    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ELEMWILDCARD);
                }else{
                    m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),m_queueMark-1);
                    // Minimalist check for an NCName - just check first character
                    // to distinguish from other possible tokens
                    if(!Character.isLetter(m_tokenChar)&&!tokenIs('_')){
                        // "Node test that matches either NCName:* or QName was expected."
                        error(XPATHErrorResources.ER_EXPECTED_NODE_TEST,null);
                    }
                }
                nextToken();
                consumeExpected(':');
            }else{
                m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.EMPTY);
            }
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            if(tokenIs('*')){
                m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ELEMWILDCARD);
            }else{
                m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),m_queueMark-1);
                // Minimalist check for an NCName - just check first character
                // to distinguish from other possible tokens
                if(!Character.isLetter(m_tokenChar)&&!tokenIs('_')){
                    // "Node test that matches either NCName:* or QName was expected."
                    error(XPATHErrorResources.ER_EXPECTED_NODE_TEST,null);
                }
            }
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            nextToken();
        }
    }

    protected void Predicate() throws TransformerException{
        if(tokenIs('[')){
            nextToken();
            PredicateExpr();
            consumeExpected(']');
        }
    }

    protected void PredicateExpr() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        appendOp(2,OpCodes.OP_PREDICATE);
        Expr();
        // Terminate for safety.
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ENDOP);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected void QName() throws TransformerException{
        // Namespace
        if(lookahead(':',1)){
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),m_queueMark-1);
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            nextToken();
            consumeExpected(':');
        }else{
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.EMPTY);
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        }
        // Local name
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),m_queueMark-1);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        nextToken();
    }

    protected void NCName(){
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),m_queueMark-1);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        nextToken();
    }

    protected void Literal() throws TransformerException{
        int last=m_token.length()-1;
        char c0=m_tokenChar;
        char cX=m_token.charAt(last);
        if(((c0=='\"')&&(cX=='\"'))||((c0=='\'')&&(cX=='\''))){
            // Mutate the token to remove the quotes and have the XString object
            // already made.
            int tokenQueuePos=m_queueMark-1;
            m_ops.m_tokenQueue.setElementAt(null,tokenQueuePos);
            Object obj=new XString(m_token.substring(1,last));
            m_ops.m_tokenQueue.setElementAt(obj,tokenQueuePos);
            // lit = m_token.substring(1, last);
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),tokenQueuePos);
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            nextToken();
        }else{
            error(XPATHErrorResources.ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
                    new Object[]{m_token});  //"Pattern literal ("+m_token+") needs to be quoted!");
        }
    }

    protected void Number() throws TransformerException{
        if(null!=m_token){
            // Mutate the token to remove the quotes and have the XNumber object
            // already made.
            double num;
            try{
                // XPath 1.0 does not support number in exp notation
                if((m_token.indexOf('e')>-1)||(m_token.indexOf('E')>-1))
                    throw new NumberFormatException();
                num=Double.valueOf(m_token).doubleValue();
            }catch(NumberFormatException nfe){
                num=0.0;  // to shut up compiler.
                error(XPATHErrorResources.ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
                        new Object[]{m_token});  //m_token+" could not be formatted to a number!");
            }
            m_ops.m_tokenQueue.setElementAt(new XNumber(num),m_queueMark-1);
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),m_queueMark-1);
            m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
            nextToken();
        }
    }
    // ============= PATTERN FUNCTIONS =================

    protected void Pattern() throws TransformerException{
        while(true){
            LocationPathPattern();
            if(tokenIs('|')){
                nextToken();
            }else{
                break;
            }
        }
    }

    protected void LocationPathPattern() throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        final int RELATIVE_PATH_NOT_PERMITTED=0;
        final int RELATIVE_PATH_PERMITTED=1;
        final int RELATIVE_PATH_REQUIRED=2;
        int relativePathStatus=RELATIVE_PATH_NOT_PERMITTED;
        appendOp(2,OpCodes.OP_LOCATIONPATHPATTERN);
        if(lookahead('(',1)
                &&(tokenIs(Keywords.FUNC_ID_STRING)
                ||tokenIs(Keywords.FUNC_KEY_STRING))){
            IdKeyPattern();
            if(tokenIs('/')){
                nextToken();
                if(tokenIs('/')){
                    appendOp(4,OpCodes.MATCH_ANY_ANCESTOR);
                    nextToken();
                }else{
                    appendOp(4,OpCodes.MATCH_IMMEDIATE_ANCESTOR);
                }
                // Tell how long the step is without the predicate
                m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-2,4);
                m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-1,OpCodes.NODETYPE_FUNCTEST);
                relativePathStatus=RELATIVE_PATH_REQUIRED;
            }
        }else if(tokenIs('/')){
            if(lookahead('/',1)){
                appendOp(4,OpCodes.MATCH_ANY_ANCESTOR);
                // Added this to fix bug reported by Myriam for match="//x/a"
                // patterns.  If you don't do this, the 'x' step will think it's part
                // of a '//' pattern, and so will cause 'a' to be matched when it has
                // any ancestor that is 'x'.
                nextToken();
                relativePathStatus=RELATIVE_PATH_REQUIRED;
            }else{
                appendOp(4,OpCodes.FROM_ROOT);
                relativePathStatus=RELATIVE_PATH_PERMITTED;
            }
            // Tell how long the step is without the predicate
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-2,4);
            m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH)-1,OpCodes.NODETYPE_ROOT);
            nextToken();
        }else{
            relativePathStatus=RELATIVE_PATH_REQUIRED;
        }
        if(relativePathStatus!=RELATIVE_PATH_NOT_PERMITTED){
            if(!tokenIs('|')&&(null!=m_token)){
                RelativePathPattern();
            }else if(relativePathStatus==RELATIVE_PATH_REQUIRED){
                // "A relative path pattern was expected."
                error(XPATHErrorResources.ER_EXPECTED_REL_PATH_PATTERN,null);
            }
        }
        // Terminate for safety.
        m_ops.setOp(m_ops.getOp(OpMap.MAPINDEX_LENGTH),OpCodes.ENDOP);
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
    }

    protected void IdKeyPattern() throws TransformerException{
        FunctionCall();
    }

    protected void RelativePathPattern()
            throws TransformerException{
        // Caller will have consumed any '/' or '//' preceding the
        // RelativePathPattern, so let StepPattern know it can't begin with a '/'
        boolean trailingSlashConsumed=StepPattern(false);
        while(tokenIs('/')){
            nextToken();
            // StepPattern() may consume first slash of pair in "a//b" while
            // processing StepPattern "a".  On next iteration, let StepPattern know
            // that happened, so it doesn't match ill-formed patterns like "a///b".
            trailingSlashConsumed=StepPattern(!trailingSlashConsumed);
        }
    }

    protected boolean StepPattern(boolean isLeadingSlashPermitted)
            throws TransformerException{
        return AbbreviatedNodeTestStep(isLeadingSlashPermitted);
    }

    protected boolean AbbreviatedNodeTestStep(boolean isLeadingSlashPermitted)
            throws TransformerException{
        int opPos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
        int axesType;
        // The next blocks guarantee that a MATCH_XXX will be added.
        int matchTypePos=-1;
        if(tokenIs('@')){
            axesType=OpCodes.MATCH_ATTRIBUTE;
            appendOp(2,axesType);
            nextToken();
        }else if(this.lookahead("::",1)){
            if(tokenIs("attribute")){
                axesType=OpCodes.MATCH_ATTRIBUTE;
                appendOp(2,axesType);
            }else if(tokenIs("child")){
                matchTypePos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
                axesType=OpCodes.MATCH_IMMEDIATE_ANCESTOR;
                appendOp(2,axesType);
            }else{
                axesType=-1;
                this.error(XPATHErrorResources.ER_AXES_NOT_ALLOWED,
                        new Object[]{this.m_token});
            }
            nextToken();
            nextToken();
        }else if(tokenIs('/')){
            if(!isLeadingSlashPermitted){
                // "A step was expected in the pattern, but '/' was encountered."
                error(XPATHErrorResources.ER_EXPECTED_STEP_PATTERN,null);
            }
            axesType=OpCodes.MATCH_ANY_ANCESTOR;
            appendOp(2,axesType);
            nextToken();
        }else{
            matchTypePos=m_ops.getOp(OpMap.MAPINDEX_LENGTH);
            axesType=OpCodes.MATCH_IMMEDIATE_ANCESTOR;
            appendOp(2,axesType);
        }
        // Make room for telling how long the step is without the predicate
        m_ops.setOp(OpMap.MAPINDEX_LENGTH,m_ops.getOp(OpMap.MAPINDEX_LENGTH)+1);
        NodeTest(axesType);
        // Tell how long the step is without the predicate
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH+1,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
        while(tokenIs('[')){
            Predicate();
        }
        boolean trailingSlashConsumed;
        // For "a//b", where "a" is current step, we need to mark operation of
        // current step as "MATCH_ANY_ANCESTOR".  Then we'll consume the first
        // slash and subsequent step will be treated as a MATCH_IMMEDIATE_ANCESTOR
        // (unless it too is followed by '//'.)
        //
        // %REVIEW%  Following is what happens today, but I'm not sure that's
        // %REVIEW%  correct behaviour.  Perhaps no valid case could be constructed
        // %REVIEW%  where it would matter?
        //
        // If current step is on the attribute axis (e.g., "@x//b"), we won't
        // change the current step, and let following step be marked as
        // MATCH_ANY_ANCESTOR on next call instead.
        if((matchTypePos>-1)&&tokenIs('/')&&lookahead('/',1)){
            m_ops.setOp(matchTypePos,OpCodes.MATCH_ANY_ANCESTOR);
            nextToken();
            trailingSlashConsumed=true;
        }else{
            trailingSlashConsumed=false;
        }
        // Tell how long the entire step is.
        m_ops.setOp(opPos+OpMap.MAPINDEX_LENGTH,
                m_ops.getOp(OpMap.MAPINDEX_LENGTH)-opPos);
        return trailingSlashConsumed;
    }
}
