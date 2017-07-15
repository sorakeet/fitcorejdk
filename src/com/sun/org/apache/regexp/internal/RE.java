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
package com.sun.org.apache.regexp.internal;

import java.io.Serializable;
import java.util.Vector;

public class RE implements Serializable{
    public static final int MATCH_NORMAL=0x0000;
    public static final int MATCH_CASEINDEPENDENT=0x0001;
    public static final int MATCH_MULTILINE=0x0002;
    public static final int MATCH_SINGLELINE=0x0004;
    public static final int REPLACE_ALL=0x0000;
    public static final int REPLACE_FIRSTONLY=0x0001;
    public static final int REPLACE_BACKREFERENCES=0x0002;
    //   Opcode              Char       Opdata/Operand  Meaning
    //   ----------          ---------- --------------- --------------------------------------------------
    static final char OP_END='E';  //                 end of program
    static final char OP_BOL='^';  //                 match only if at beginning of line
    static final char OP_EOL='$';  //                 match only if at end of line
    static final char OP_ANY='.';  //                 match any single character except newline
    static final char OP_ANYOF='[';  // count/ranges    match any char in the list of ranges
    static final char OP_BRANCH='|';  // node            match this alternative or the next one
    static final char OP_ATOM='A';  // length/string   length of string followed by string itself
    static final char OP_STAR='*';  // node            kleene closure
    static final char OP_PLUS='+';  // node            positive closure
    static final char OP_MAYBE='?';  // node            optional closure
    static final char OP_ESCAPE='\\'; // escape          special escape code char class (escape is E_* code)
    static final char OP_OPEN='(';  // number          nth opening paren
    static final char OP_OPEN_CLUSTER='<';  //                 opening cluster
    static final char OP_CLOSE=')';  // number          nth closing paren
    static final char OP_CLOSE_CLUSTER='>';  //                 closing cluster
    static final char OP_BACKREF='#';  // number          reference nth already matched parenthesized string
    static final char OP_GOTO='G';  //                 nothing but a (back-)pointer
    static final char OP_NOTHING='N';  //                 match null string such as in '(a|)'
    static final char OP_RELUCTANTSTAR='8';  // none/expr       reluctant '*' (mnemonic for char is unshifted '*')
    static final char OP_RELUCTANTPLUS='=';  // none/expr       reluctant '+' (mnemonic for char is unshifted '+')
    static final char OP_RELUCTANTMAYBE='/';  // none/expr       reluctant '?' (mnemonic for char is unshifted '?')
    static final char OP_POSIXCLASS='P';  // classid         one of the posix character classes
    // Escape codes
    static final char E_ALNUM='w';  // Alphanumeric
    static final char E_NALNUM='W';  // Non-alphanumeric
    static final char E_BOUND='b';  // Word boundary
    static final char E_NBOUND='B';  // Non-word boundary
    static final char E_SPACE='s';  // Whitespace
    static final char E_NSPACE='S';  // Non-whitespace
    static final char E_DIGIT='d';  // Digit
    static final char E_NDIGIT='D';  // Non-digit
    // Posix character classes
    static final char POSIX_CLASS_ALNUM='w';  // Alphanumerics
    static final char POSIX_CLASS_ALPHA='a';  // Alphabetics
    static final char POSIX_CLASS_BLANK='b';  // Blanks
    static final char POSIX_CLASS_CNTRL='c';  // Control characters
    static final char POSIX_CLASS_DIGIT='d';  // Digits
    static final char POSIX_CLASS_GRAPH='g';  // Graphic characters
    static final char POSIX_CLASS_LOWER='l';  // Lowercase characters
    static final char POSIX_CLASS_PRINT='p';  // Printable characters
    static final char POSIX_CLASS_PUNCT='!';  // Punctuation
    static final char POSIX_CLASS_SPACE='s';  // Spaces
    static final char POSIX_CLASS_UPPER='u';  // Uppercase characters
    static final char POSIX_CLASS_XDIGIT='x';  // Hexadecimal digits
    static final char POSIX_CLASS_JSTART='j';  // Java identifier start
    static final char POSIX_CLASS_JPART='k';  // Java identifier part
    // Limits
    static final int maxNode=65536;            // Maximum number of nodes in a program
    static final int MAX_PAREN=16;              // Number of paren pairs (only 9 can be backrefs)
    // Node layout constants
    static final int offsetOpcode=0;            // Opcode offset (first character)
    static final int offsetOpdata=1;            // Opdata offset (second char)
    static final int offsetNext=2;            // Next index offset (third char)
    static final int nodeSize=3;            // Node size (in chars)
    // State of current program
    REProgram program;                            // Compiled regular expression 'program'
    transient CharacterIterator search;           // The string being matched against
    int matchFlags;                               // Match behaviour flags
    int maxParen=MAX_PAREN;
    // Parenthesized subexpressions
    transient int parenCount;                     // Number of subexpressions matched (num open parens + 1)
    transient int start0;                         // Cache of start[0]
    transient int end0;                           // Cache of start[0]
    transient int start1;                         // Cache of start[1]
    transient int end1;                           // Cache of start[1]
    transient int start2;                         // Cache of start[2]
    transient int end2;                           // Cache of start[2]
    transient int[] startn;                       // Lazy-alloced array of sub-expression starts
    transient int[] endn;                         // Lazy-alloced array of sub-expression ends
    // Backreferences
    transient int[] startBackref;                 // Lazy-alloced array of backref starts
    transient int[] endBackref;                   // Lazy-alloced array of backref ends

    public RE(String pattern) throws RESyntaxException{
        this(pattern,MATCH_NORMAL);
    }

    public RE(String pattern,int matchFlags) throws RESyntaxException{
        this(new RECompiler().compile(pattern));
        setMatchFlags(matchFlags);
    }

    public RE(REProgram program){
        this(program,MATCH_NORMAL);
    }

    public RE(REProgram program,int matchFlags){
        setProgram(program);
        setMatchFlags(matchFlags);
    }

    public RE(){
        this((REProgram)null,MATCH_NORMAL);
    }

    public static String simplePatternToFullRegularExpression(String pattern){
        StringBuffer buf=new StringBuffer();
        for(int i=0;i<pattern.length();i++){
            char c=pattern.charAt(i);
            switch(c){
                case '*':
                    buf.append(".*");
                    break;
                case '.':
                case '[':
                case ']':
                case '\\':
                case '+':
                case '?':
                case '{':
                case '}':
                case '$':
                case '^':
                case '|':
                case '(':
                case ')':
                    buf.append('\\');
                default:
                    buf.append(c);
                    break;
            }
        }
        return buf.toString();
    }

    public int getMatchFlags(){
        return matchFlags;
    }

    public void setMatchFlags(int matchFlags){
        this.matchFlags=matchFlags;
    }

    public REProgram getProgram(){
        return program;
    }

    public void setProgram(REProgram program){
        this.program=program;
        if(program!=null&&program.maxParens!=-1){
            this.maxParen=program.maxParens;
        }else{
            this.maxParen=MAX_PAREN;
        }
    }

    public int getParenCount(){
        return parenCount;
    }

    public String getParen(int which){
        int start;
        if(which<parenCount&&(start=getParenStart(which))>=0){
            return search.substring(start,getParenEnd(which));
        }
        return null;
    }

    public final int getParenLength(int which){
        if(which<parenCount){
            return getParenEnd(which)-getParenStart(which);
        }
        return -1;
    }

    public final int getParenStart(int which){
        if(which<parenCount){
            switch(which){
                case 0:
                    return start0;
                case 1:
                    return start1;
                case 2:
                    return start2;
                default:
                    if(startn==null){
                        allocParens();
                    }
                    return startn[which];
            }
        }
        return -1;
    }

    public final int getParenEnd(int which){
        if(which<parenCount){
            switch(which){
                case 0:
                    return end0;
                case 1:
                    return end1;
                case 2:
                    return end2;
                default:
                    if(endn==null){
                        allocParens();
                    }
                    return endn[which];
            }
        }
        return -1;
    }

    private final void allocParens(){
        // Allocate arrays for subexpressions
        startn=new int[maxParen];
        endn=new int[maxParen];
        // Set sub-expression pointers to invalid values
        for(int i=0;i<maxParen;i++){
            startn[i]=-1;
            endn[i]=-1;
        }
    }

    protected final void setParenStart(int which,int i){
        if(which<parenCount){
            switch(which){
                case 0:
                    start0=i;
                    break;
                case 1:
                    start1=i;
                    break;
                case 2:
                    start2=i;
                    break;
                default:
                    if(startn==null){
                        allocParens();
                    }
                    startn[which]=i;
                    break;
            }
        }
    }

    protected final void setParenEnd(int which,int i){
        if(which<parenCount){
            switch(which){
                case 0:
                    end0=i;
                    break;
                case 1:
                    end1=i;
                    break;
                case 2:
                    end2=i;
                    break;
                default:
                    if(endn==null){
                        allocParens();
                    }
                    endn[which]=i;
                    break;
            }
        }
    }

    protected void internalError(String s) throws Error{
        throw new Error("RE internal error: "+s);
    }

    protected int matchNodes(int firstNode,int lastNode,int idxStart){
        // Our current place in the string
        int idx=idxStart;
        // Loop while node is valid
        int next, opcode, opdata;
        int idxNew;
        char[] instruction=program.instruction;
        for(int node=firstNode;node<lastNode;){
            opcode=instruction[node+offsetOpcode];
            next=node+(short)instruction[node+offsetNext];
            opdata=instruction[node+offsetOpdata];
            switch(opcode){
                case OP_RELUCTANTMAYBE:{
                    int once=0;
                    do{
                        // Try to match the rest without using the reluctant subexpr
                        if((idxNew=matchNodes(next,maxNode,idx))!=-1){
                            return idxNew;
                        }
                    }
                    while((once++==0)&&(idx=matchNodes(node+nodeSize,next,idx))!=-1);
                    return -1;
                }
                case OP_RELUCTANTPLUS:
                    while((idx=matchNodes(node+nodeSize,next,idx))!=-1){
                        // Try to match the rest without using the reluctant subexpr
                        if((idxNew=matchNodes(next,maxNode,idx))!=-1){
                            return idxNew;
                        }
                    }
                    return -1;
                case OP_RELUCTANTSTAR:
                    do{
                        // Try to match the rest without using the reluctant subexpr
                        if((idxNew=matchNodes(next,maxNode,idx))!=-1){
                            return idxNew;
                        }
                    }
                    while((idx=matchNodes(node+nodeSize,next,idx))!=-1);
                    return -1;
                case OP_OPEN:
                    // Match subexpression
                    if((program.flags&REProgram.OPT_HASBACKREFS)!=0){
                        startBackref[opdata]=idx;
                    }
                    if((idxNew=matchNodes(next,maxNode,idx))!=-1){
                        // Increase valid paren count
                        if((opdata+1)>parenCount){
                            parenCount=opdata+1;
                        }
                        // Don't set paren if already set later on
                        if(getParenStart(opdata)==-1){
                            setParenStart(opdata,idx);
                        }
                    }
                    return idxNew;
                case OP_CLOSE:
                    // Done matching subexpression
                    if((program.flags&REProgram.OPT_HASBACKREFS)!=0){
                        endBackref[opdata]=idx;
                    }
                    if((idxNew=matchNodes(next,maxNode,idx))!=-1){
                        // Increase valid paren count
                        if((opdata+1)>parenCount){
                            parenCount=opdata+1;
                        }
                        // Don't set paren if already set later on
                        if(getParenEnd(opdata)==-1){
                            setParenEnd(opdata,idx);
                        }
                    }
                    return idxNew;
                case OP_OPEN_CLUSTER:
                case OP_CLOSE_CLUSTER:
                    // starting or ending the matching of a subexpression which has no backref.
                    return matchNodes(next,maxNode,idx);
                case OP_BACKREF:{
                    // Get the start and end of the backref
                    int s=startBackref[opdata];
                    int e=endBackref[opdata];
                    // We don't know the backref yet
                    if(s==-1||e==-1){
                        return -1;
                    }
                    // The backref is empty size
                    if(s==e){
                        break;
                    }
                    // Get the length of the backref
                    int l=e-s;
                    // If there's not enough input left, give up.
                    if(search.isEnd(idx+l-1)){
                        return -1;
                    }
                    // Case fold the backref?
                    final boolean caseFold=
                            ((matchFlags&MATCH_CASEINDEPENDENT)!=0);
                    // Compare backref to input
                    for(int i=0;i<l;i++){
                        if(compareChars(search.charAt(idx++),search.charAt(s+i),caseFold)!=0){
                            return -1;
                        }
                    }
                }
                break;
                case OP_BOL:
                    // Fail if we're not at the start of the string
                    if(idx!=0){
                        // If we're multiline matching, we could still be at the start of a line
                        if((matchFlags&MATCH_MULTILINE)==MATCH_MULTILINE){
                            // If not at start of line, give up
                            if(idx<=0||!isNewline(idx-1)){
                                return -1;
                            }else{
                                break;
                            }
                        }
                        return -1;
                    }
                    break;
                case OP_EOL:
                    // If we're not at the end of string
                    if(!search.isEnd(0)&&!search.isEnd(idx)){
                        // If we're multi-line matching
                        if((matchFlags&MATCH_MULTILINE)==MATCH_MULTILINE){
                            // Give up if we're not at the end of a line
                            if(!isNewline(idx)){
                                return -1;
                            }else{
                                break;
                            }
                        }
                        return -1;
                    }
                    break;
                case OP_ESCAPE:
                    // Which escape?
                    switch(opdata){
                        // Word boundary match
                        case E_NBOUND:
                        case E_BOUND:{
                            char cLast=((idx==0)?'\n':search.charAt(idx-1));
                            char cNext=((search.isEnd(idx))?'\n':search.charAt(idx));
                            if((Character.isLetterOrDigit(cLast)==Character.isLetterOrDigit(cNext))==(opdata==E_BOUND)){
                                return -1;
                            }
                        }
                        break;
                        // Alpha-numeric, digit, space, javaLetter, javaLetterOrDigit
                        case E_ALNUM:
                        case E_NALNUM:
                        case E_DIGIT:
                        case E_NDIGIT:
                        case E_SPACE:
                        case E_NSPACE:
                            // Give up if out of input
                            if(search.isEnd(idx)){
                                return -1;
                            }
                            char c=search.charAt(idx);
                            // Switch on escape
                            switch(opdata){
                                case E_ALNUM:
                                case E_NALNUM:
                                    if(!((Character.isLetterOrDigit(c)||c=='_')==(opdata==E_ALNUM))){
                                        return -1;
                                    }
                                    break;
                                case E_DIGIT:
                                case E_NDIGIT:
                                    if(!(Character.isDigit(c)==(opdata==E_DIGIT))){
                                        return -1;
                                    }
                                    break;
                                case E_SPACE:
                                case E_NSPACE:
                                    if(!(Character.isWhitespace(c)==(opdata==E_SPACE))){
                                        return -1;
                                    }
                                    break;
                            }
                            idx++;
                            break;
                        default:
                            internalError("Unrecognized escape '"+opdata+"'");
                    }
                    break;
                case OP_ANY:
                    if((matchFlags&MATCH_SINGLELINE)==MATCH_SINGLELINE){
                        // Match anything
                        if(search.isEnd(idx)){
                            return -1;
                        }
                    }else{
                        // Match anything but a newline
                        if(search.isEnd(idx)||isNewline(idx)){
                            return -1;
                        }
                    }
                    idx++;
                    break;
                case OP_ATOM:{
                    // Match an atom value
                    if(search.isEnd(idx)){
                        return -1;
                    }
                    // Get length of atom and starting index
                    int lenAtom=opdata;
                    int startAtom=node+nodeSize;
                    // Give up if not enough input remains to have a match
                    if(search.isEnd(lenAtom+idx-1)){
                        return -1;
                    }
                    // Match atom differently depending on casefolding flag
                    final boolean caseFold=
                            ((matchFlags&MATCH_CASEINDEPENDENT)!=0);
                    for(int i=0;i<lenAtom;i++){
                        if(compareChars(search.charAt(idx++),instruction[startAtom+i],caseFold)!=0){
                            return -1;
                        }
                    }
                }
                break;
                case OP_POSIXCLASS:{
                    // Out of input?
                    if(search.isEnd(idx)){
                        return -1;
                    }
                    switch(opdata){
                        case POSIX_CLASS_ALNUM:
                            if(!Character.isLetterOrDigit(search.charAt(idx))){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_ALPHA:
                            if(!Character.isLetter(search.charAt(idx))){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_DIGIT:
                            if(!Character.isDigit(search.charAt(idx))){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_BLANK: // JWL - bugbug: is this right??
                            if(!Character.isSpaceChar(search.charAt(idx))){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_SPACE:
                            if(!Character.isWhitespace(search.charAt(idx))){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_CNTRL:
                            if(Character.getType(search.charAt(idx))!=Character.CONTROL){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_GRAPH: // JWL - bugbug???
                            switch(Character.getType(search.charAt(idx))){
                                case Character.MATH_SYMBOL:
                                case Character.CURRENCY_SYMBOL:
                                case Character.MODIFIER_SYMBOL:
                                case Character.OTHER_SYMBOL:
                                    break;
                                default:
                                    return -1;
                            }
                            break;
                        case POSIX_CLASS_LOWER:
                            if(Character.getType(search.charAt(idx))!=Character.LOWERCASE_LETTER){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_UPPER:
                            if(Character.getType(search.charAt(idx))!=Character.UPPERCASE_LETTER){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_PRINT:
                            if(Character.getType(search.charAt(idx))==Character.CONTROL){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_PUNCT:{
                            int type=Character.getType(search.charAt(idx));
                            switch(type){
                                case Character.DASH_PUNCTUATION:
                                case Character.START_PUNCTUATION:
                                case Character.END_PUNCTUATION:
                                case Character.CONNECTOR_PUNCTUATION:
                                case Character.OTHER_PUNCTUATION:
                                    break;
                                default:
                                    return -1;
                            }
                        }
                        break;
                        case POSIX_CLASS_XDIGIT: // JWL - bugbug??
                        {
                            boolean isXDigit=((search.charAt(idx)>='0'&&search.charAt(idx)<='9')||
                                    (search.charAt(idx)>='a'&&search.charAt(idx)<='f')||
                                    (search.charAt(idx)>='A'&&search.charAt(idx)<='F'));
                            if(!isXDigit){
                                return -1;
                            }
                        }
                        break;
                        case POSIX_CLASS_JSTART:
                            if(!Character.isJavaIdentifierStart(search.charAt(idx))){
                                return -1;
                            }
                            break;
                        case POSIX_CLASS_JPART:
                            if(!Character.isJavaIdentifierPart(search.charAt(idx))){
                                return -1;
                            }
                            break;
                        default:
                            internalError("Bad posix class");
                            break;
                    }
                    // Matched.
                    idx++;
                }
                break;
                case OP_ANYOF:{
                    // Out of input?
                    if(search.isEnd(idx)){
                        return -1;
                    }
                    // Get character to match against character class and maybe casefold
                    char c=search.charAt(idx);
                    boolean caseFold=(matchFlags&MATCH_CASEINDEPENDENT)!=0;
                    // Loop through character class checking our match character
                    int idxRange=node+nodeSize;
                    int idxEnd=idxRange+(opdata*2);
                    boolean match=false;
                    for(int i=idxRange;!match&&i<idxEnd;){
                        // Get start, end and match characters
                        char s=instruction[i++];
                        char e=instruction[i++];
                        match=((compareChars(c,s,caseFold)>=0)
                                &&(compareChars(c,e,caseFold)<=0));
                    }
                    // Fail if we didn't match the character class
                    if(!match){
                        return -1;
                    }
                    idx++;
                }
                break;
                case OP_BRANCH:{
                    // Check for choices
                    if(instruction[next+offsetOpcode]!=OP_BRANCH){
                        // If there aren't any other choices, just evaluate this branch.
                        node+=nodeSize;
                        continue;
                    }
                    // Try all available branches
                    short nextBranch;
                    do{
                        // Try matching the branch against the string
                        if((idxNew=matchNodes(node+nodeSize,maxNode,idx))!=-1){
                            return idxNew;
                        }
                        // Go to next branch (if any)
                        nextBranch=(short)instruction[node+offsetNext];
                        node+=nextBranch;
                    }
                    while(nextBranch!=0&&(instruction[node+offsetOpcode]==OP_BRANCH));
                    // Failed to match any branch!
                    return -1;
                }
                case OP_NOTHING:
                case OP_GOTO:
                    // Just advance to the next node without doing anything
                    break;
                case OP_END:
                    // Match has succeeded!
                    setParenEnd(0,idx);
                    return idx;
                default:
                    // Corrupt program
                    internalError("Invalid opcode '"+opcode+"'");
            }
            // Advance to the next node in the program
            node=next;
        }
        // We "should" never end up here
        internalError("Corrupt program");
        return -1;
    }

    protected boolean matchAt(int i){
        // Initialize start pointer, paren cache and paren count
        start0=-1;
        end0=-1;
        start1=-1;
        end1=-1;
        start2=-1;
        end2=-1;
        startn=null;
        endn=null;
        parenCount=1;
        setParenStart(0,i);
        // Allocate backref arrays (unless optimizations indicate otherwise)
        if((program.flags&REProgram.OPT_HASBACKREFS)!=0){
            startBackref=new int[maxParen];
            endBackref=new int[maxParen];
        }
        // Match against string
        int idx;
        if((idx=matchNodes(0,maxNode,i))!=-1){
            setParenEnd(0,idx);
            return true;
        }
        // Didn't match
        parenCount=0;
        return false;
    }

    public boolean match(String search,int i){
        return match(new StringCharacterIterator(search),i);
    }

    public boolean match(CharacterIterator search,int i){
        // There is no compiled program to search with!
        if(program==null){
            // This should be uncommon enough to be an error case rather
            // than an exception (which would have to be handled everywhere)
            internalError("No RE program to run!");
        }
        // Save string to search
        this.search=search;
        // Can we optimize the search by looking for a prefix string?
        if(program.prefix==null){
            // Unprefixed matching must try for a match at each character
            for(;!search.isEnd(i-1);i++){
                // Try a match at index i
                if(matchAt(i)){
                    return true;
                }
            }
            return false;
        }else{
            // Prefix-anchored matching is possible
            boolean caseIndependent=(matchFlags&MATCH_CASEINDEPENDENT)!=0;
            char[] prefix=program.prefix;
            for(;!search.isEnd(i+prefix.length-1);i++){
                int j=i;
                int k=0;
                boolean match;
                do{
                    // If there's a mismatch of any character in the prefix, give up
                    match=(compareChars(search.charAt(j++),prefix[k++],caseIndependent)==0);
                }while(match&&k<prefix.length);
                // See if the whole prefix string matched
                if(k==prefix.length){
                    // We matched the full prefix at firstChar, so try it
                    if(matchAt(i)){
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean match(String search){
        return match(search,0);
    }

    public String[] split(String s){
        // Create new vector
        Vector v=new Vector();
        // Start at position 0 and search the whole string
        int pos=0;
        int len=s.length();
        // Try a match at each position
        while(pos<len&&match(s,pos)){
            // Get start of match
            int start=getParenStart(0);
            // Get end of match
            int newpos=getParenEnd(0);
            // Check if no progress was made
            if(newpos==pos){
                v.addElement(s.substring(pos,start+1));
                newpos++;
            }else{
                v.addElement(s.substring(pos,start));
            }
            // Move to new position
            pos=newpos;
        }
        // Push remainder if it's not empty
        String remainder=s.substring(pos);
        if(remainder.length()!=0){
            v.addElement(remainder);
        }
        // Return vector as an array of strings
        String[] ret=new String[v.size()];
        v.copyInto(ret);
        return ret;
    }

    public String subst(String substituteIn,String substitution){
        return subst(substituteIn,substitution,REPLACE_ALL);
    }

    public String subst(String substituteIn,String substitution,int flags){
        // String to return
        StringBuffer ret=new StringBuffer();
        // Start at position 0 and search the whole string
        int pos=0;
        int len=substituteIn.length();
        // Try a match at each position
        while(pos<len&&match(substituteIn,pos)){
            // Append string before match
            ret.append(substituteIn.substring(pos,getParenStart(0)));
            if((flags&REPLACE_BACKREFERENCES)!=0){
                // Process backreferences
                int lCurrentPosition=0;
                int lLastPosition=-2;
                int lLength=substitution.length();
                boolean bAddedPrefix=false;
                while((lCurrentPosition=substitution.indexOf("$",lCurrentPosition))>=0){
                    if((lCurrentPosition==0||substitution.charAt(lCurrentPosition-1)!='\\')
                            &&lCurrentPosition+1<lLength){
                        char c=substitution.charAt(lCurrentPosition+1);
                        if(c>='0'&&c<='9'){
                            if(bAddedPrefix==false){
                                // Append everything between the beginning of the
                                // substitution string and the current $ sign
                                ret.append(substitution.substring(0,lCurrentPosition));
                                bAddedPrefix=true;
                            }else{
                                // Append everything between the last and the current $ sign
                                ret.append(substitution.substring(lLastPosition+2,lCurrentPosition));
                            }
                            // Append the parenthesized expression
                            // Note: if a parenthesized expression of the requested
                            // index is not available "null" is added to the string
                            ret.append(getParen(c-'0'));
                            lLastPosition=lCurrentPosition;
                        }
                    }
                    // Move forward, skipping past match
                    lCurrentPosition++;
                }
                // Append everything after the last $ sign
                ret.append(substitution.substring(lLastPosition+2,lLength));
            }else{
                // Append substitution without processing backreferences
                ret.append(substitution);
            }
            // Move forward, skipping past match
            int newpos=getParenEnd(0);
            // We always want to make progress!
            if(newpos==pos){
                newpos++;
            }
            // Try new position
            pos=newpos;
            // Break out if we're only supposed to replace one occurrence
            if((flags&REPLACE_FIRSTONLY)!=0){
                break;
            }
        }
        // If there's remaining input, append it
        if(pos<len){
            ret.append(substituteIn.substring(pos));
        }
        // Return string buffer as string
        return ret.toString();
    }

    public String[] grep(Object[] search){
        // Create new vector to hold return items
        Vector v=new Vector();
        // Traverse array of objects
        for(int i=0;i<search.length;i++){
            // Get next object as a string
            String s=search[i].toString();
            // If it matches this regexp, add it to the list
            if(match(s)){
                v.addElement(s);
            }
        }
        // Return vector as an array of strings
        String[] ret=new String[v.size()];
        v.copyInto(ret);
        return ret;
    }

    private boolean isNewline(int i){
        char nextChar=search.charAt(i);
        if(nextChar=='\n'||nextChar=='\r'||nextChar=='\u0085'
                ||nextChar=='\u2028'||nextChar=='\u2029'){
            return true;
        }
        return false;
    }

    private int compareChars(char c1,char c2,boolean caseIndependent){
        if(caseIndependent){
            c1=Character.toLowerCase(c1);
            c2=Character.toLowerCase(c2);
        }
        return ((int)c1-(int)c2);
    }
}
