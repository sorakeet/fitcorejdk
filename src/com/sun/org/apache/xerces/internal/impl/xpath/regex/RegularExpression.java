/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
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
 */
/**
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.xpath.regex;

import com.sun.org.apache.xerces.internal.util.IntStack;

import java.text.CharacterIterator;
import java.util.Locale;
import java.util.Stack;

public class RegularExpression implements java.io.Serializable{
    static final boolean DEBUG=false;
    static final int IGNORE_CASE=1<<1;
    static final int SINGLE_LINE=1<<2;
    static final int MULTIPLE_LINES=1<<3;
//Public
    static final int EXTENDED_COMMENT=1<<4;
    static final int USE_UNICODE_CATEGORY=1<<5; // "u"
    static final int UNICODE_WORD_BOUNDARY=1<<6; // "w"
    static final int PROHIBIT_HEAD_CHARACTER_OPTIMIZATION=1<<7;
    static final int PROHIBIT_FIXED_STRING_OPTIMIZATION=1<<8;
    static final int XMLSCHEMA_MODE=1<<9;
    static final int SPECIAL_COMMA=1<<10;
    // ================================================================
    static final int LINE_FEED=0x000A;
    static final int CARRIAGE_RETURN=0x000D;
    static final int LINE_SEPARATOR=0x2028;
    static final int PARAGRAPH_SEPARATOR=0x2029;
    private static final long serialVersionUID=6242499334195006401L;
    // ================================================================
    private static final int WT_IGNORE=0;
    private static final int WT_LETTER=1;
    private static final int WT_OTHER=2;
    // ================================================================
    String regex;
    int options;
    int nofparen;
    Token tokentree;
    boolean hasBackReferences=false;
    transient int minlength;
    transient Op operations=null;
    transient int numberOfClosures;
    transient Context context=null;
    transient RangeToken firstChar=null;
    transient String fixedString=null;
    transient int fixedStringOptions;
    transient BMPattern fixedStringTable=null;
    transient boolean fixedStringOnly=false;

    public RegularExpression(String regex) throws ParseException{
        this(regex,null);
    }

    public RegularExpression(String regex,String options) throws ParseException{
        this.setPattern(regex,options);
    }

    public void setPattern(String newPattern,String options) throws ParseException{
        this.setPattern(newPattern,options,Locale.getDefault());
    }

    public void setPattern(String newPattern,String options,Locale locale) throws ParseException{
        this.setPattern(newPattern,REUtil.parseOptions(options),locale);
    }

    private void setPattern(String newPattern,int options,Locale locale) throws ParseException{
        this.regex=newPattern;
        this.options=options;
        RegexParser rp=RegularExpression.isSet(this.options,RegularExpression.XMLSCHEMA_MODE)
                ?new ParserForXMLSchema(locale):new RegexParser(locale);
        this.tokentree=rp.parse(this.regex,this.options);
        this.nofparen=rp.parennumber;
        this.hasBackReferences=rp.hasBackReferences;
        this.operations=null;
        this.context=null;
    }

    private static final boolean isSet(int options,int flag){
        return (options&flag)==flag;
    }

    public RegularExpression(String regex,String options,Locale locale) throws ParseException{
        this.setPattern(regex,options,locale);
    }

    RegularExpression(String regex,Token tok,int parens,boolean hasBackReferences,int options){
        this.regex=regex;
        this.tokentree=tok;
        this.nofparen=parens;
        this.options=options;
        this.hasBackReferences=hasBackReferences;
    }

    private static final int getPreviousWordType(ExpressionTarget target,int begin,int end,
                                                 int offset,int opts){
        int ret=getWordType(target,begin,end,--offset,opts);
        while(ret==WT_IGNORE)
            ret=getWordType(target,begin,end,--offset,opts);
        return ret;
    }

    private static final int getWordType(ExpressionTarget target,int begin,int end,
                                         int offset,int opts){
        if(offset<begin||offset>=end) return WT_OTHER;
        return getWordType0(target.charAt(offset),opts);
    }

    private static final int getWordType0(char ch,int opts){
        if(!isSet(opts,UNICODE_WORD_BOUNDARY)){
            if(isSet(opts,USE_UNICODE_CATEGORY)){
                return (Token.getRange("IsWord",true).match(ch))?WT_LETTER:WT_OTHER;
            }
            return isWordChar(ch)?WT_LETTER:WT_OTHER;
        }
        switch(Character.getType(ch)){
            case Character.UPPERCASE_LETTER:      // L
            case Character.LOWERCASE_LETTER:      // L
            case Character.TITLECASE_LETTER:      // L
            case Character.MODIFIER_LETTER:       // L
            case Character.OTHER_LETTER:          // L
            case Character.LETTER_NUMBER:         // N
            case Character.DECIMAL_DIGIT_NUMBER:  // N
            case Character.OTHER_NUMBER:          // N
            case Character.COMBINING_SPACING_MARK: // Mc
                return WT_LETTER;
            case Character.FORMAT:                // Cf
            case Character.NON_SPACING_MARK:      // Mn
            case Character.ENCLOSING_MARK:        // Mc
                return WT_IGNORE;
            case Character.CONTROL:               // Cc
                switch(ch){
                    case '\t':
                    case '\n':
                    case '\u000B':
                    case '\f':
                    case '\r':
                        return WT_OTHER;
                    default:
                        return WT_IGNORE;
                }
            default:
                return WT_OTHER;
        }
    }

    private static final boolean isEOLChar(int ch){
        return ch==LINE_FEED||ch==CARRIAGE_RETURN||ch==LINE_SEPARATOR
                ||ch==PARAGRAPH_SEPARATOR;
    }

    private static final boolean isWordChar(int ch){ // Legacy word characters
        if(ch=='_') return true;
        if(ch<'0') return false;
        if(ch>'z') return false;
        if(ch<='9') return true;
        if(ch<'A') return false;
        if(ch<='Z') return true;
        if(ch<'a') return false;
        return true;
    }

    private static final boolean matchIgnoreCase(int chardata,int ch){
        if(chardata==ch) return true;
        if(chardata>0xffff||ch>0xffff) return false;
        char uch1=Character.toUpperCase((char)chardata);
        char uch2=Character.toUpperCase((char)ch);
        if(uch1==uch2) return true;
        return Character.toLowerCase(uch1)==Character.toLowerCase(uch2);
    }

    private synchronized void compile(Token tok){
        if(this.operations!=null)
            return;
        this.numberOfClosures=0;
        this.operations=this.compile(tok,null,false);
    }

    private Op compile(Token tok,Op next,boolean reverse){
        Op ret;
        switch(tok.type){
            case Token.DOT:
                ret=Op.createDot();
                ret.next=next;
                break;
            case Token.CHAR:
                ret=Op.createChar(tok.getChar());
                ret.next=next;
                break;
            case Token.ANCHOR:
                ret=Op.createAnchor(tok.getChar());
                ret.next=next;
                break;
            case Token.RANGE:
            case Token.NRANGE:
                ret=Op.createRange(tok);
                ret.next=next;
                break;
            case Token.CONCAT:
                ret=next;
                if(!reverse){
                    for(int i=tok.size()-1;i>=0;i--){
                        ret=compile(tok.getChild(i),ret,false);
                    }
                }else{
                    for(int i=0;i<tok.size();i++){
                        ret=compile(tok.getChild(i),ret,true);
                    }
                }
                break;
            case Token.UNION:
                Op.UnionOp uni=Op.createUnion(tok.size());
                for(int i=0;i<tok.size();i++){
                    uni.addElement(compile(tok.getChild(i),next,reverse));
                }
                ret=uni;                          // ret.next is null.
                break;
            case Token.CLOSURE:
            case Token.NONGREEDYCLOSURE:
                Token child=tok.getChild(0);
                int min=tok.getMin();
                int max=tok.getMax();
                if(min>=0&&min==max){ // {n}
                    ret=next;
                    for(int i=0;i<min;i++){
                        ret=compile(child,ret,reverse);
                    }
                    break;
                }
                if(min>0&&max>0)
                    max-=min;
                if(max>0){
                    // X{2,6} -> XX(X(X(XX?)?)?)?
                    ret=next;
                    for(int i=0;i<max;i++){
                        Op.ChildOp q=Op.createQuestion(tok.type==Token.NONGREEDYCLOSURE);
                        q.next=next;
                        q.setChild(compile(child,ret,reverse));
                        ret=q;
                    }
                }else{
                    Op.ChildOp op;
                    if(tok.type==Token.NONGREEDYCLOSURE){
                        op=Op.createNonGreedyClosure();
                    }else{                        // Token.CLOSURE
                        op=Op.createClosure(this.numberOfClosures++);
                    }
                    op.next=next;
                    op.setChild(compile(child,op,reverse));
                    ret=op;
                }
                if(min>0){
                    for(int i=0;i<min;i++){
                        ret=compile(child,ret,reverse);
                    }
                }
                break;
            case Token.EMPTY:
                ret=next;
                break;
            case Token.STRING:
                ret=Op.createString(tok.getString());
                ret.next=next;
                break;
            case Token.BACKREFERENCE:
                ret=Op.createBackReference(tok.getReferenceNumber());
                ret.next=next;
                break;
            case Token.PAREN:
                if(tok.getParenNumber()==0){
                    ret=compile(tok.getChild(0),next,reverse);
                }else if(reverse){
                    next=Op.createCapture(tok.getParenNumber(),next);
                    next=compile(tok.getChild(0),next,reverse);
                    ret=Op.createCapture(-tok.getParenNumber(),next);
                }else{
                    next=Op.createCapture(-tok.getParenNumber(),next);
                    next=compile(tok.getChild(0),next,reverse);
                    ret=Op.createCapture(tok.getParenNumber(),next);
                }
                break;
            case Token.LOOKAHEAD:
                ret=Op.createLook(Op.LOOKAHEAD,next,compile(tok.getChild(0),null,false));
                break;
            case Token.NEGATIVELOOKAHEAD:
                ret=Op.createLook(Op.NEGATIVELOOKAHEAD,next,compile(tok.getChild(0),null,false));
                break;
            case Token.LOOKBEHIND:
                ret=Op.createLook(Op.LOOKBEHIND,next,compile(tok.getChild(0),null,true));
                break;
            case Token.NEGATIVELOOKBEHIND:
                ret=Op.createLook(Op.NEGATIVELOOKBEHIND,next,compile(tok.getChild(0),null,true));
                break;
            case Token.INDEPENDENT:
                ret=Op.createIndependent(next,compile(tok.getChild(0),null,reverse));
                break;
            case Token.MODIFIERGROUP:
                ret=Op.createModifier(next,compile(tok.getChild(0),null,reverse),
                        ((Token.ModifierToken)tok).getOptions(),
                        ((Token.ModifierToken)tok).getOptionsMask());
                break;
            case Token.CONDITION:
                Token.ConditionToken ctok=(Token.ConditionToken)tok;
                int ref=ctok.refNumber;
                Op condition=ctok.condition==null?null:compile(ctok.condition,null,reverse);
                Op yes=compile(ctok.yes,next,reverse);
                Op no=ctok.no==null?null:compile(ctok.no,next,reverse);
                ret=Op.createCondition(next,ref,condition,yes,no);
                break;
            default:
                throw new RuntimeException("Unknown token type: "+tok.type);
        } // switch (tok.type)
        return ret;
    }

    public boolean matches(char[] target){
        return this.matches(target,0,target.length,(Match)null);
    }

    public boolean matches(char[] target,int start,int end){
        return this.matches(target,start,end,(Match)null);
    }

    public boolean matches(char[] target,Match match){
        return this.matches(target,0,target.length,match);
    }

    public boolean matches(char[] target,int start,int end,Match match){
        synchronized(this){
            if(this.operations==null)
                this.prepare();
            if(this.context==null)
                this.context=new Context();
        }
        Context con=null;
        synchronized(this.context){
            con=this.context.inuse?new Context():this.context;
            con.reset(target,start,end,this.numberOfClosures);
        }
        if(match!=null){
            match.setNumberOfGroups(this.nofparen);
            match.setSource(target);
        }else if(this.hasBackReferences){
            match=new Match();
            match.setNumberOfGroups(this.nofparen);
            // Need not to call setSource() because
            // a caller can not access this match instance.
        }
        con.match=match;
        if(RegularExpression.isSet(this.options,XMLSCHEMA_MODE)){
            int matchEnd=this.match(con,this.operations,con.start,1,this.options);
            //System.err.println("DEBUG: matchEnd="+matchEnd);
            if(matchEnd==con.limit){
                if(con.match!=null){
                    con.match.setBeginning(0,con.start);
                    con.match.setEnd(0,matchEnd);
                }
                con.setInUse(false);
                return true;
            }
            return false;
        }
        /**
         * The pattern has only fixed string.
         * The engine uses Boyer-Moore.
         */
        if(this.fixedStringOnly){
            //System.err.println("DEBUG: fixed-only: "+this.fixedString);
            int o=this.fixedStringTable.matches(target,con.start,con.limit);
            if(o>=0){
                if(con.match!=null){
                    con.match.setBeginning(0,o);
                    con.match.setEnd(0,o+this.fixedString.length());
                }
                con.setInUse(false);
                return true;
            }
            con.setInUse(false);
            return false;
        }
        /**
         * The pattern contains a fixed string.
         * The engine checks with Boyer-Moore whether the text contains the fixed string or not.
         * If not, it return with false.
         */
        if(this.fixedString!=null){
            int o=this.fixedStringTable.matches(target,con.start,con.limit);
            if(o<0){
                //System.err.println("Non-match in fixed-string search.");
                con.setInUse(false);
                return false;
            }
        }
        int limit=con.limit-this.minlength;
        int matchStart;
        int matchEnd=-1;
        /**
         * Checks whether the expression starts with ".*".
         */
        if(this.operations!=null
                &&this.operations.type==Op.CLOSURE&&this.operations.getChild().type==Op.DOT){
            if(isSet(this.options,SINGLE_LINE)){
                matchStart=con.start;
                matchEnd=this.match(con,this.operations,con.start,1,this.options);
            }else{
                boolean previousIsEOL=true;
                for(matchStart=con.start;matchStart<=limit;matchStart++){
                    int ch=target[matchStart];
                    if(isEOLChar(ch)){
                        previousIsEOL=true;
                    }else{
                        if(previousIsEOL){
                            if(0<=(matchEnd=this.match(con,this.operations,
                                    matchStart,1,this.options)))
                                break;
                        }
                        previousIsEOL=false;
                    }
                }
            }
        }
        /**
         * Optimization against the first character.
         */
        else if(this.firstChar!=null){
            //System.err.println("DEBUG: with firstchar-matching: "+this.firstChar);
            RangeToken range=this.firstChar;
            for(matchStart=con.start;matchStart<=limit;matchStart++){
                int ch=target[matchStart];
                if(REUtil.isHighSurrogate(ch)&&matchStart+1<con.limit){
                    ch=REUtil.composeFromSurrogates(ch,target[matchStart+1]);
                }
                if(!range.match(ch)){
                    continue;
                }
                if(0<=(matchEnd=this.match(con,this.operations,
                        matchStart,1,this.options))){
                    break;
                }
            }
        }
        /**
         * Straightforward matching.
         */
        else{
            for(matchStart=con.start;matchStart<=limit;matchStart++){
                if(0<=(matchEnd=this.match(con,this.operations,matchStart,1,this.options)))
                    break;
            }
        }
        if(matchEnd>=0){
            if(con.match!=null){
                con.match.setBeginning(0,matchStart);
                con.match.setEnd(0,matchEnd);
            }
            con.setInUse(false);
            return true;
        }else{
            con.setInUse(false);
            return false;
        }
    }

    public boolean matches(String target){
        return this.matches(target,0,target.length(),(Match)null);
    }

    public boolean matches(String target,int start,int end){
        return this.matches(target,start,end,(Match)null);
    }

    public boolean matches(String target,Match match){
        return this.matches(target,0,target.length(),match);
    }

    public boolean matches(String target,int start,int end,Match match){
        synchronized(this){
            if(this.operations==null)
                this.prepare();
            if(this.context==null)
                this.context=new Context();
        }
        Context con=null;
        synchronized(this.context){
            con=this.context.inuse?new Context():this.context;
            con.reset(target,start,end,this.numberOfClosures);
        }
        if(match!=null){
            match.setNumberOfGroups(this.nofparen);
            match.setSource(target);
        }else if(this.hasBackReferences){
            match=new Match();
            match.setNumberOfGroups(this.nofparen);
            // Need not to call setSource() because
            // a caller can not access this match instance.
        }
        con.match=match;
        if(RegularExpression.isSet(this.options,XMLSCHEMA_MODE)){
            if(DEBUG){
                System.err.println("target string="+target);
            }
            int matchEnd=this.match(con,this.operations,con.start,1,this.options);
            if(DEBUG){
                System.err.println("matchEnd="+matchEnd);
                System.err.println("con.limit="+con.limit);
            }
            if(matchEnd==con.limit){
                if(con.match!=null){
                    con.match.setBeginning(0,con.start);
                    con.match.setEnd(0,matchEnd);
                }
                con.setInUse(false);
                return true;
            }
            return false;
        }
        /**
         * The pattern has only fixed string.
         * The engine uses Boyer-Moore.
         */
        if(this.fixedStringOnly){
            //System.err.println("DEBUG: fixed-only: "+this.fixedString);
            int o=this.fixedStringTable.matches(target,con.start,con.limit);
            if(o>=0){
                if(con.match!=null){
                    con.match.setBeginning(0,o);
                    con.match.setEnd(0,o+this.fixedString.length());
                }
                con.setInUse(false);
                return true;
            }
            con.setInUse(false);
            return false;
        }
        /**
         * The pattern contains a fixed string.
         * The engine checks with Boyer-Moore whether the text contains the fixed string or not.
         * If not, it return with false.
         */
        if(this.fixedString!=null){
            int o=this.fixedStringTable.matches(target,con.start,con.limit);
            if(o<0){
                //System.err.println("Non-match in fixed-string search.");
                con.setInUse(false);
                return false;
            }
        }
        int limit=con.limit-this.minlength;
        int matchStart;
        int matchEnd=-1;
        /**
         * Checks whether the expression starts with ".*".
         */
        if(this.operations!=null
                &&this.operations.type==Op.CLOSURE&&this.operations.getChild().type==Op.DOT){
            if(isSet(this.options,SINGLE_LINE)){
                matchStart=con.start;
                matchEnd=this.match(con,this.operations,con.start,1,this.options);
            }else{
                boolean previousIsEOL=true;
                for(matchStart=con.start;matchStart<=limit;matchStart++){
                    int ch=target.charAt(matchStart);
                    if(isEOLChar(ch)){
                        previousIsEOL=true;
                    }else{
                        if(previousIsEOL){
                            if(0<=(matchEnd=this.match(con,this.operations,
                                    matchStart,1,this.options)))
                                break;
                        }
                        previousIsEOL=false;
                    }
                }
            }
        }
        /**
         * Optimization against the first character.
         */
        else if(this.firstChar!=null){
            //System.err.println("DEBUG: with firstchar-matching: "+this.firstChar);
            RangeToken range=this.firstChar;
            for(matchStart=con.start;matchStart<=limit;matchStart++){
                int ch=target.charAt(matchStart);
                if(REUtil.isHighSurrogate(ch)&&matchStart+1<con.limit){
                    ch=REUtil.composeFromSurrogates(ch,target.charAt(matchStart+1));
                }
                if(!range.match(ch)){
                    continue;
                }
                if(0<=(matchEnd=this.match(con,this.operations,
                        matchStart,1,this.options))){
                    break;
                }
            }
        }
        /**
         * Straightforward matching.
         */
        else{
            for(matchStart=con.start;matchStart<=limit;matchStart++){
                if(0<=(matchEnd=this.match(con,this.operations,matchStart,1,this.options)))
                    break;
            }
        }
        if(matchEnd>=0){
            if(con.match!=null){
                con.match.setBeginning(0,matchStart);
                con.match.setEnd(0,matchEnd);
            }
            con.setInUse(false);
            return true;
        }else{
            con.setInUse(false);
            return false;
        }
    }

    private int match(Context con,Op op,int offset,int dx,int opts){
        final ExpressionTarget target=con.target;
        final Stack opStack=new Stack();
        final IntStack dataStack=new IntStack();
        final boolean isSetIgnoreCase=isSet(opts,IGNORE_CASE);
        int retValue=-1;
        boolean returned=false;
        for(;;){
            if(op==null||offset>con.limit||offset<con.start){
                if(op==null){
                    retValue=isSet(opts,XMLSCHEMA_MODE)&&offset!=con.limit?-1:offset;
                }else{
                    retValue=-1;
                }
                returned=true;
            }else{
                retValue=-1;
                // dx value is either 1 or -1
                switch(op.type){
                    case Op.CHAR:{
                        final int o1=(dx>0)?offset:offset-1;
                        if(o1>=con.limit||o1<0||!matchChar(op.getData(),target.charAt(o1),isSetIgnoreCase)){
                            returned=true;
                            break;
                        }
                        offset+=dx;
                        op=op.next;
                    }
                    break;
                    case Op.DOT:{
                        int o1=(dx>0)?offset:offset-1;
                        if(o1>=con.limit||o1<0){
                            returned=true;
                            break;
                        }
                        if(isSet(opts,SINGLE_LINE)){
                            if(REUtil.isHighSurrogate(target.charAt(o1))&&o1+dx>=0&&o1+dx<con.limit){
                                o1+=dx;
                            }
                        }else{
                            int ch=target.charAt(o1);
                            if(REUtil.isHighSurrogate(ch)&&o1+dx>=0&&o1+dx<con.limit){
                                o1+=dx;
                                ch=REUtil.composeFromSurrogates(ch,target.charAt(o1));
                            }
                            if(isEOLChar(ch)){
                                returned=true;
                                break;
                            }
                        }
                        offset=(dx>0)?o1+1:o1;
                        op=op.next;
                    }
                    break;
                    case Op.RANGE:
                    case Op.NRANGE:{
                        int o1=(dx>0)?offset:offset-1;
                        if(o1>=con.limit||o1<0){
                            returned=true;
                            break;
                        }
                        int ch=target.charAt(offset);
                        if(REUtil.isHighSurrogate(ch)&&o1+dx<con.limit&&o1+dx>=0){
                            o1+=dx;
                            ch=REUtil.composeFromSurrogates(ch,target.charAt(o1));
                        }
                        final RangeToken tok=op.getToken();
                        if(!tok.match(ch)){
                            returned=true;
                            break;
                        }
                        offset=(dx>0)?o1+1:o1;
                        op=op.next;
                    }
                    break;
                    case Op.ANCHOR:{
                        if(!matchAnchor(target,op,con,offset,opts)){
                            returned=true;
                            break;
                        }
                        op=op.next;
                    }
                    break;
                    case Op.BACKREFERENCE:{
                        int refno=op.getData();
                        if(refno<=0||refno>=this.nofparen){
                            throw new RuntimeException("Internal Error: Reference number must be more than zero: "+refno);
                        }
                        if(con.match.getBeginning(refno)<0||con.match.getEnd(refno)<0){
                            returned=true;
                            break;
                        }
                        int o2=con.match.getBeginning(refno);
                        int literallen=con.match.getEnd(refno)-o2;
                        if(dx>0){
                            if(!target.regionMatches(isSetIgnoreCase,offset,con.limit,o2,literallen)){
                                returned=true;
                                break;
                            }
                            offset+=literallen;
                        }else{
                            if(!target.regionMatches(isSetIgnoreCase,offset-literallen,con.limit,o2,literallen)){
                                returned=true;
                                break;
                            }
                            offset-=literallen;
                        }
                        op=op.next;
                    }
                    break;
                    case Op.STRING:{
                        String literal=op.getString();
                        int literallen=literal.length();
                        if(dx>0){
                            if(!target.regionMatches(isSetIgnoreCase,offset,con.limit,literal,literallen)){
                                returned=true;
                                break;
                            }
                            offset+=literallen;
                        }else{
                            if(!target.regionMatches(isSetIgnoreCase,offset-literallen,con.limit,literal,literallen)){
                                returned=true;
                                break;
                            }
                            offset-=literallen;
                        }
                        op=op.next;
                    }
                    break;
                    case Op.CLOSURE:{
                        // Saves current position to avoid zero-width repeats.
                        final int id=op.getData();
                        if(con.closureContexts[id].contains(offset)){
                            returned=true;
                            break;
                        }
                        con.closureContexts[id].addOffset(offset);
                    }
                    // fall through
                    case Op.QUESTION:{
                        opStack.push(op);
                        dataStack.push(offset);
                        op=op.getChild();
                    }
                    break;
                    case Op.NONGREEDYCLOSURE:
                    case Op.NONGREEDYQUESTION:{
                        opStack.push(op);
                        dataStack.push(offset);
                        op=op.next;
                    }
                    break;
                    case Op.UNION:
                        if(op.size()==0){
                            returned=true;
                        }else{
                            opStack.push(op);
                            dataStack.push(0);
                            dataStack.push(offset);
                            op=op.elementAt(0);
                        }
                        break;
                    case Op.CAPTURE:{
                        final int refno=op.getData();
                        if(con.match!=null){
                            if(refno>0){
                                dataStack.push(con.match.getBeginning(refno));
                                con.match.setBeginning(refno,offset);
                            }else{
                                final int index=-refno;
                                dataStack.push(con.match.getEnd(index));
                                con.match.setEnd(index,offset);
                            }
                            opStack.push(op);
                            dataStack.push(offset);
                        }
                        op=op.next;
                    }
                    break;
                    case Op.LOOKAHEAD:
                    case Op.NEGATIVELOOKAHEAD:
                    case Op.LOOKBEHIND:
                    case Op.NEGATIVELOOKBEHIND:{
                        opStack.push(op);
                        dataStack.push(dx);
                        dataStack.push(offset);
                        dx=(op.type==Op.LOOKAHEAD||op.type==Op.NEGATIVELOOKAHEAD)?1:-1;
                        op=op.getChild();
                    }
                    break;
                    case Op.INDEPENDENT:{
                        opStack.push(op);
                        dataStack.push(offset);
                        op=op.getChild();
                    }
                    break;
                    case Op.MODIFIER:{
                        int localopts=opts;
                        localopts|=op.getData();
                        localopts&=~op.getData2();
                        opStack.push(op);
                        dataStack.push(opts);
                        dataStack.push(offset);
                        opts=localopts;
                        op=op.getChild();
                    }
                    break;
                    case Op.CONDITION:{
                        Op.ConditionOp cop=(Op.ConditionOp)op;
                        if(cop.refNumber>0){
                            if(cop.refNumber>=this.nofparen){
                                throw new RuntimeException("Internal Error: Reference number must be more than zero: "+cop.refNumber);
                            }
                            if(con.match.getBeginning(cop.refNumber)>=0
                                    &&con.match.getEnd(cop.refNumber)>=0){
                                op=cop.yes;
                            }else if(cop.no!=null){
                                op=cop.no;
                            }else{
                                op=cop.next;
                            }
                        }else{
                            opStack.push(op);
                            dataStack.push(offset);
                            op=cop.condition;
                        }
                    }
                    break;
                    default:
                        throw new RuntimeException("Unknown operation type: "+op.type);
                }
            }
            // handle recursive operations
            while(returned){
                // exhausted all the operations
                if(opStack.isEmpty()){
                    return retValue;
                }
                op=(Op)opStack.pop();
                offset=dataStack.pop();
                switch(op.type){
                    case Op.CLOSURE:
                    case Op.QUESTION:
                        if(retValue<0){
                            op=op.next;
                            returned=false;
                        }
                        break;
                    case Op.NONGREEDYCLOSURE:
                    case Op.NONGREEDYQUESTION:
                        if(retValue<0){
                            op=op.getChild();
                            returned=false;
                        }
                        break;
                    case Op.UNION:{
                        int unionIndex=dataStack.pop();
                        if(DEBUG){
                            System.err.println("UNION: "+unionIndex+", ret="+retValue);
                        }
                        if(retValue<0){
                            if(++unionIndex<op.size()){
                                opStack.push(op);
                                dataStack.push(unionIndex);
                                dataStack.push(offset);
                                op=op.elementAt(unionIndex);
                                returned=false;
                            }else{
                                retValue=-1;
                            }
                        }
                    }
                    break;
                    case Op.CAPTURE:
                        final int refno=op.getData();
                        final int saved=dataStack.pop();
                        if(retValue<0){
                            if(refno>0){
                                con.match.setBeginning(refno,saved);
                            }else{
                                con.match.setEnd(-refno,saved);
                            }
                        }
                        break;
                    case Op.LOOKAHEAD:
                    case Op.LOOKBEHIND:{
                        dx=dataStack.pop();
                        if(0<=retValue){
                            op=op.next;
                            returned=false;
                        }
                        retValue=-1;
                    }
                    break;
                    case Op.NEGATIVELOOKAHEAD:
                    case Op.NEGATIVELOOKBEHIND:{
                        dx=dataStack.pop();
                        if(0>retValue){
                            op=op.next;
                            returned=false;
                        }
                        retValue=-1;
                    }
                    break;
                    case Op.MODIFIER:
                        opts=dataStack.pop();
                        // fall through
                    case Op.INDEPENDENT:
                        if(retValue>=0){
                            offset=retValue;
                            op=op.next;
                            returned=false;
                        }
                        break;
                    case Op.CONDITION:{
                        final Op.ConditionOp cop=(Op.ConditionOp)op;
                        if(0<=retValue){
                            op=cop.yes;
                        }else if(cop.no!=null){
                            op=cop.no;
                        }else{
                            op=cop.next;
                        }
                    }
                    returned=false;
                    break;
                    default:
                        break;
                }
            }
        }
    }

    private boolean matchChar(int ch,int other,boolean ignoreCase){
        return (ignoreCase)?matchIgnoreCase(ch,other):ch==other;
    }

    boolean matchAnchor(ExpressionTarget target,Op op,Context con,int offset,int opts){
        boolean go=false;
        switch(op.getData()){
            case '^':
                if(isSet(opts,MULTIPLE_LINES)){
                    if(!(offset==con.start
                            ||offset>con.start&&offset<con.limit&&isEOLChar(target.charAt(offset-1))))
                        return false;
                }else{
                    if(offset!=con.start)
                        return false;
                }
                break;
            case '@':                         // Internal use only.
                // The @ always matches line beginnings.
                if(!(offset==con.start
                        ||offset>con.start&&isEOLChar(target.charAt(offset-1))))
                    return false;
                break;
            case '$':
                if(isSet(opts,MULTIPLE_LINES)){
                    if(!(offset==con.limit
                            ||offset<con.limit&&isEOLChar(target.charAt(offset))))
                        return false;
                }else{
                    if(!(offset==con.limit
                            ||offset+1==con.limit&&isEOLChar(target.charAt(offset))
                            ||offset+2==con.limit&&target.charAt(offset)==CARRIAGE_RETURN
                            &&target.charAt(offset+1)==LINE_FEED))
                        return false;
                }
                break;
            case 'A':
                if(offset!=con.start) return false;
                break;
            case 'Z':
                if(!(offset==con.limit
                        ||offset+1==con.limit&&isEOLChar(target.charAt(offset))
                        ||offset+2==con.limit&&target.charAt(offset)==CARRIAGE_RETURN
                        &&target.charAt(offset+1)==LINE_FEED))
                    return false;
                break;
            case 'z':
                if(offset!=con.limit) return false;
                break;
            case 'b':
                if(con.length==0)
                    return false;
            {
                int after=getWordType(target,con.start,con.limit,offset,opts);
                if(after==WT_IGNORE) return false;
                int before=getPreviousWordType(target,con.start,con.limit,offset,opts);
                if(after==before) return false;
            }
            break;
            case 'B':
                if(con.length==0)
                    go=true;
                else{
                    int after=getWordType(target,con.start,con.limit,offset,opts);
                    go=after==WT_IGNORE
                            ||after==getPreviousWordType(target,con.start,con.limit,offset,opts);
                }
                if(!go) return false;
                break;
            case '<':
                if(con.length==0||offset==con.limit) return false;
                if(getWordType(target,con.start,con.limit,offset,opts)!=WT_LETTER
                        ||getPreviousWordType(target,con.start,con.limit,offset,opts)!=WT_OTHER)
                    return false;
                break;
            case '>':
                if(con.length==0||offset==con.start) return false;
                if(getWordType(target,con.start,con.limit,offset,opts)!=WT_OTHER
                        ||getPreviousWordType(target,con.start,con.limit,offset,opts)!=WT_LETTER)
                    return false;
                break;
        } // switch anchor type
        return true;
    }

    public boolean matches(CharacterIterator target){
        return this.matches(target,(Match)null);
    }

    public boolean matches(CharacterIterator target,Match match){
        int start=target.getBeginIndex();
        int end=target.getEndIndex();
        synchronized(this){
            if(this.operations==null)
                this.prepare();
            if(this.context==null)
                this.context=new Context();
        }
        Context con=null;
        synchronized(this.context){
            con=this.context.inuse?new Context():this.context;
            con.reset(target,start,end,this.numberOfClosures);
        }
        if(match!=null){
            match.setNumberOfGroups(this.nofparen);
            match.setSource(target);
        }else if(this.hasBackReferences){
            match=new Match();
            match.setNumberOfGroups(this.nofparen);
            // Need not to call setSource() because
            // a caller can not access this match instance.
        }
        con.match=match;
        if(RegularExpression.isSet(this.options,XMLSCHEMA_MODE)){
            int matchEnd=this.match(con,this.operations,con.start,1,this.options);
            //System.err.println("DEBUG: matchEnd="+matchEnd);
            if(matchEnd==con.limit){
                if(con.match!=null){
                    con.match.setBeginning(0,con.start);
                    con.match.setEnd(0,matchEnd);
                }
                con.setInUse(false);
                return true;
            }
            return false;
        }
        /**
         * The pattern has only fixed string.
         * The engine uses Boyer-Moore.
         */
        if(this.fixedStringOnly){
            //System.err.println("DEBUG: fixed-only: "+this.fixedString);
            int o=this.fixedStringTable.matches(target,con.start,con.limit);
            if(o>=0){
                if(con.match!=null){
                    con.match.setBeginning(0,o);
                    con.match.setEnd(0,o+this.fixedString.length());
                }
                con.setInUse(false);
                return true;
            }
            con.setInUse(false);
            return false;
        }
        /**
         * The pattern contains a fixed string.
         * The engine checks with Boyer-Moore whether the text contains the fixed string or not.
         * If not, it return with false.
         */
        if(this.fixedString!=null){
            int o=this.fixedStringTable.matches(target,con.start,con.limit);
            if(o<0){
                //System.err.println("Non-match in fixed-string search.");
                con.setInUse(false);
                return false;
            }
        }
        int limit=con.limit-this.minlength;
        int matchStart;
        int matchEnd=-1;
        /**
         * Checks whether the expression starts with ".*".
         */
        if(this.operations!=null
                &&this.operations.type==Op.CLOSURE&&this.operations.getChild().type==Op.DOT){
            if(isSet(this.options,SINGLE_LINE)){
                matchStart=con.start;
                matchEnd=this.match(con,this.operations,con.start,1,this.options);
            }else{
                boolean previousIsEOL=true;
                for(matchStart=con.start;matchStart<=limit;matchStart++){
                    int ch=target.setIndex(matchStart);
                    if(isEOLChar(ch)){
                        previousIsEOL=true;
                    }else{
                        if(previousIsEOL){
                            if(0<=(matchEnd=this.match(con,this.operations,
                                    matchStart,1,this.options)))
                                break;
                        }
                        previousIsEOL=false;
                    }
                }
            }
        }
        /**
         * Optimization against the first character.
         */
        else if(this.firstChar!=null){
            //System.err.println("DEBUG: with firstchar-matching: "+this.firstChar);
            RangeToken range=this.firstChar;
            for(matchStart=con.start;matchStart<=limit;matchStart++){
                int ch=target.setIndex(matchStart);
                if(REUtil.isHighSurrogate(ch)&&matchStart+1<con.limit){
                    ch=REUtil.composeFromSurrogates(ch,target.setIndex(matchStart+1));
                }
                if(!range.match(ch)){
                    continue;
                }
                if(0<=(matchEnd=this.match(con,this.operations,
                        matchStart,1,this.options))){
                    break;
                }
            }
        }
        /**
         * Straightforward matching.
         */
        else{
            for(matchStart=con.start;matchStart<=limit;matchStart++){
                if(0<=(matchEnd=this.match(con,this.operations,matchStart,1,this.options)))
                    break;
            }
        }
        if(matchEnd>=0){
            if(con.match!=null){
                con.match.setBeginning(0,matchStart);
                con.match.setEnd(0,matchEnd);
            }
            con.setInUse(false);
            return true;
        }else{
            con.setInUse(false);
            return false;
        }
    }

    void prepare(){
        if(Op.COUNT) Op.nofinstances=0;
        this.compile(this.tokentree);
        /**
         if  (this.operations.type == Op.CLOSURE && this.operations.getChild().type == Op.DOT) { // .*
         Op anchor = Op.createAnchor(isSet(this.options, SINGLE_LINE) ? 'A' : '@');
         anchor.next = this.operations;
         this.operations = anchor;
         }
         */
        if(Op.COUNT) System.err.println("DEBUG: The number of operations: "+Op.nofinstances);
        this.minlength=this.tokentree.getMinLength();
        this.firstChar=null;
        if(!isSet(this.options,PROHIBIT_HEAD_CHARACTER_OPTIMIZATION)
                &&!isSet(this.options,XMLSCHEMA_MODE)){
            RangeToken firstChar=Token.createRange();
            int fresult=this.tokentree.analyzeFirstCharacter(firstChar,this.options);
            if(fresult==Token.FC_TERMINAL){
                firstChar.compactRanges();
                this.firstChar=firstChar;
                if(DEBUG)
                    System.err.println("DEBUG: Use the first character optimization: "+firstChar);
            }
        }
        if(this.operations!=null
                &&(this.operations.type==Op.STRING||this.operations.type==Op.CHAR)
                &&this.operations.next==null){
            if(DEBUG)
                System.err.print(" *** Only fixed string! *** ");
            this.fixedStringOnly=true;
            if(this.operations.type==Op.STRING)
                this.fixedString=this.operations.getString();
            else if(this.operations.getData()>=0x10000){ // Op.CHAR
                this.fixedString=REUtil.decomposeToSurrogates(this.operations.getData());
            }else{
                char[] ac=new char[1];
                ac[0]=(char)this.operations.getData();
                this.fixedString=new String(ac);
            }
            this.fixedStringOptions=this.options;
            this.fixedStringTable=new BMPattern(this.fixedString,256,
                    isSet(this.fixedStringOptions,IGNORE_CASE));
        }else if(!isSet(this.options,PROHIBIT_FIXED_STRING_OPTIMIZATION)
                &&!isSet(this.options,XMLSCHEMA_MODE)){
            Token.FixedStringContainer container=new Token.FixedStringContainer();
            this.tokentree.findFixedString(container,this.options);
            this.fixedString=container.token==null?null:container.token.getString();
            this.fixedStringOptions=container.options;
            if(this.fixedString!=null&&this.fixedString.length()<2)
                this.fixedString=null;
            // This pattern has a fixed string of which length is more than one.
            if(this.fixedString!=null){
                this.fixedStringTable=new BMPattern(this.fixedString,256,
                        isSet(this.fixedStringOptions,IGNORE_CASE));
                if(DEBUG){
                    System.err.println("DEBUG: The longest fixed string: "+this.fixedString.length()
                            +"/" //+this.fixedString
                            +"/"+REUtil.createOptionString(this.fixedStringOptions));
                    System.err.print("String: ");
                    REUtil.dumpString(this.fixedString);
                }
            }
        }
    }

    public String getPattern(){
        return this.regex;
    }

    public void setPattern(String newPattern) throws ParseException{
        this.setPattern(newPattern,Locale.getDefault());
    }

    public void setPattern(String newPattern,Locale locale) throws ParseException{
        this.setPattern(newPattern,this.options,locale);
    }

    boolean equals(String pattern,int options){
        return this.regex.equals(pattern)&&this.options==options;
    }

    public int hashCode(){
        return (this.regex+"/"+this.getOptions()).hashCode();
    }

    public String getOptions(){
        return REUtil.createOptionString(this.options);
    }

    public boolean equals(Object obj){
        if(obj==null) return false;
        if(!(obj instanceof RegularExpression))
            return false;
        RegularExpression r=(RegularExpression)obj;
        return this.regex.equals(r.regex)&&this.options==r.options;
    }

    public String toString(){
        return this.tokentree.toString(this.options);
    }

    public int getNumberOfGroups(){
        return this.nofparen;
    }

    static abstract class ExpressionTarget{
        abstract char charAt(int index);

        abstract boolean regionMatches(boolean ignoreCase,int offset,int limit,String part,int partlen);

        abstract boolean regionMatches(boolean ignoreCase,int offset,int limit,int offset2,int partlen);
    }

    static final class StringTarget extends ExpressionTarget{
        private String target;

        StringTarget(String target){
            this.target=target;
        }

        final void resetTarget(String target){
            this.target=target;
        }

        final char charAt(int index){
            return target.charAt(index);
        }

        final boolean regionMatches(boolean ignoreCase,int offset,int limit,
                                    String part,int partlen){
            if(limit-offset<partlen){
                return false;
            }
            return (ignoreCase)?target.regionMatches(true,offset,part,0,partlen):target.regionMatches(offset,part,0,partlen);
        }

        final boolean regionMatches(boolean ignoreCase,int offset,int limit,
                                    int offset2,int partlen){
            if(limit-offset<partlen){
                return false;
            }
            return (ignoreCase)?target.regionMatches(true,offset,target,offset2,partlen)
                    :target.regionMatches(offset,target,offset2,partlen);
        }
    }

    static final class CharArrayTarget extends ExpressionTarget{
        char[] target;

        CharArrayTarget(char[] target){
            this.target=target;
        }

        final void resetTarget(char[] target){
            this.target=target;
        }

        char charAt(int index){
            return target[index];
        }

        final boolean regionMatches(boolean ignoreCase,int offset,int limit,
                                    String part,int partlen){
            if(offset<0||limit-offset<partlen){
                return false;
            }
            return (ignoreCase)?regionMatchesIgnoreCase(offset,limit,part,partlen)
                    :regionMatches(offset,limit,part,partlen);
        }

        private final boolean regionMatches(int offset,int limit,String part,int partlen){
            int i=0;
            while(partlen-->0){
                if(target[offset++]!=part.charAt(i++)){
                    return false;
                }
            }
            return true;
        }

        private final boolean regionMatchesIgnoreCase(int offset,int limit,String part,int partlen){
            int i=0;
            while(partlen-->0){
                final char ch1=target[offset++];
                final char ch2=part.charAt(i++);
                if(ch1==ch2){
                    continue;
                }
                final char uch1=Character.toUpperCase(ch1);
                final char uch2=Character.toUpperCase(ch2);
                if(uch1==uch2){
                    continue;
                }
                if(Character.toLowerCase(uch1)!=Character.toLowerCase(uch2)){
                    return false;
                }
            }
            return true;
        }

        final boolean regionMatches(boolean ignoreCase,int offset,int limit,int offset2,int partlen){
            if(offset<0||limit-offset<partlen){
                return false;
            }
            return (ignoreCase)?regionMatchesIgnoreCase(offset,limit,offset2,partlen)
                    :regionMatches(offset,limit,offset2,partlen);
        }

        private final boolean regionMatches(int offset,int limit,int offset2,int partlen){
            int i=offset2;
            while(partlen-->0){
                if(target[offset++]!=target[i++])
                    return false;
            }
            return true;
        }

        private final boolean regionMatchesIgnoreCase(int offset,int limit,int offset2,int partlen){
            int i=offset2;
            while(partlen-->0){
                final char ch1=target[offset++];
                final char ch2=target[i++];
                if(ch1==ch2){
                    continue;
                }
                final char uch1=Character.toUpperCase(ch1);
                final char uch2=Character.toUpperCase(ch2);
                if(uch1==uch2){
                    continue;
                }
                if(Character.toLowerCase(uch1)!=Character.toLowerCase(uch2)){
                    return false;
                }
            }
            return true;
        }
    }

    static final class CharacterIteratorTarget extends ExpressionTarget{
        CharacterIterator target;

        CharacterIteratorTarget(CharacterIterator target){
            this.target=target;
        }

        final void resetTarget(CharacterIterator target){
            this.target=target;
        }

        final char charAt(int index){
            return target.setIndex(index);
        }

        final boolean regionMatches(boolean ignoreCase,int offset,int limit,
                                    String part,int partlen){
            if(offset<0||limit-offset<partlen){
                return false;
            }
            return (ignoreCase)?regionMatchesIgnoreCase(offset,limit,part,partlen)
                    :regionMatches(offset,limit,part,partlen);
        }

        private final boolean regionMatches(int offset,int limit,String part,int partlen){
            int i=0;
            while(partlen-->0){
                if(target.setIndex(offset++)!=part.charAt(i++)){
                    return false;
                }
            }
            return true;
        }

        private final boolean regionMatchesIgnoreCase(int offset,int limit,String part,int partlen){
            int i=0;
            while(partlen-->0){
                final char ch1=target.setIndex(offset++);
                final char ch2=part.charAt(i++);
                if(ch1==ch2){
                    continue;
                }
                final char uch1=Character.toUpperCase(ch1);
                final char uch2=Character.toUpperCase(ch2);
                if(uch1==uch2){
                    continue;
                }
                if(Character.toLowerCase(uch1)!=Character.toLowerCase(uch2)){
                    return false;
                }
            }
            return true;
        }

        final boolean regionMatches(boolean ignoreCase,int offset,int limit,int offset2,int partlen){
            if(offset<0||limit-offset<partlen){
                return false;
            }
            return (ignoreCase)?regionMatchesIgnoreCase(offset,limit,offset2,partlen)
                    :regionMatches(offset,limit,offset2,partlen);
        }

        private final boolean regionMatches(int offset,int limit,int offset2,int partlen){
            int i=offset2;
            while(partlen-->0){
                if(target.setIndex(offset++)!=target.setIndex(i++)){
                    return false;
                }
            }
            return true;
        }

        private final boolean regionMatchesIgnoreCase(int offset,int limit,int offset2,int partlen){
            int i=offset2;
            while(partlen-->0){
                final char ch1=target.setIndex(offset++);
                final char ch2=target.setIndex(i++);
                if(ch1==ch2){
                    continue;
                }
                final char uch1=Character.toUpperCase(ch1);
                final char uch2=Character.toUpperCase(ch2);
                if(uch1==uch2){
                    continue;
                }
                if(Character.toLowerCase(uch1)!=Character.toLowerCase(uch2)){
                    return false;
                }
            }
            return true;
        }
    }

    static final class ClosureContext{
        int[] offsets=new int[4];
        int currentIndex=0;

        boolean contains(int offset){
            for(int i=0;i<currentIndex;++i){
                if(offsets[i]==offset){
                    return true;
                }
            }
            return false;
        }

        void reset(){
            currentIndex=0;
        }

        void addOffset(int offset){
            // We do not check for duplicates, caller is responsible for that
            if(currentIndex==offsets.length){
                offsets=expandOffsets();
            }
            offsets[currentIndex++]=offset;
        }

        private int[] expandOffsets(){
            final int len=offsets.length;
            final int newLen=len<<1;
            int[] newOffsets=new int[newLen];
            System.arraycopy(offsets,0,newOffsets,0,currentIndex);
            return newOffsets;
        }
    }

    static final class Context{
        int start;
        int limit;
        int length;
        Match match;
        boolean inuse=false;
        ClosureContext[] closureContexts;
        ExpressionTarget target;
        private StringTarget stringTarget;
        private CharArrayTarget charArrayTarget;
        private CharacterIteratorTarget characterIteratorTarget;

        Context(){
        }

        void reset(CharacterIterator target,int start,int limit,int nofclosures){
            if(characterIteratorTarget==null){
                characterIteratorTarget=new CharacterIteratorTarget(target);
            }else{
                characterIteratorTarget.resetTarget(target);
            }
            this.target=characterIteratorTarget;
            this.start=start;
            this.limit=limit;
            this.resetCommon(nofclosures);
        }

        private void resetCommon(int nofclosures){
            this.length=this.limit-this.start;
            setInUse(true);
            this.match=null;
            if(this.closureContexts==null||this.closureContexts.length!=nofclosures){
                this.closureContexts=new ClosureContext[nofclosures];
            }
            for(int i=0;i<nofclosures;i++){
                if(this.closureContexts[i]==null){
                    this.closureContexts[i]=new ClosureContext();
                }else{
                    this.closureContexts[i].reset();
                }
            }
        }

        synchronized void setInUse(boolean inUse){
            this.inuse=inUse;
        }

        void reset(String target,int start,int limit,int nofclosures){
            if(stringTarget==null){
                stringTarget=new StringTarget(target);
            }else{
                stringTarget.resetTarget(target);
            }
            this.target=stringTarget;
            this.start=start;
            this.limit=limit;
            this.resetCommon(nofclosures);
        }

        void reset(char[] target,int start,int limit,int nofclosures){
            if(charArrayTarget==null){
                charArrayTarget=new CharArrayTarget(target);
            }else{
                charArrayTarget.resetTarget(target);
            }
            this.target=charArrayTarget;
            this.start=start;
            this.limit=limit;
            this.resetCommon(nofclosures);
        }
    }
}
