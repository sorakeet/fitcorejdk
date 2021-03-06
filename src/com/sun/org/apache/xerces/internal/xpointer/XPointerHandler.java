/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * Copyright 2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xpointer;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler;
import com.sun.org.apache.xerces.internal.xinclude.XIncludeNamespaceSupport;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;

import java.util.Hashtable;
import java.util.Vector;

public final class XPointerHandler extends XIncludeHandler implements
        XPointerProcessor{
    // Supported schemes
    private final String ELEMENT_SCHEME_NAME="element";
    // Fields
    // A Vector of XPointerParts
    protected Vector fXPointerParts=null;
    // The current XPointerPart
    protected XPointerPart fXPointerPart=null;
    // Has the fXPointerPart resolved successfully
    protected boolean fFoundMatchingPtrPart=false;
    // The XPointer Error reporter
    protected XMLErrorReporter fXPointerErrorReporter;
    // The XPointer Error Handler
    protected XMLErrorHandler fErrorHandler;
    // XPointerFramework symbol table
    protected SymbolTable fSymbolTable=null;
    // Has the XPointer resolved the subresource
    protected boolean fIsXPointerResolved=false;
    // Fixup xml:base and xml:lang attributes
    protected boolean fFixupBase=false;
    protected boolean fFixupLang=false;
    // ************************************************************************
    // Constructors
    // ************************************************************************

    public XPointerHandler(){
        super();
        fXPointerParts=new Vector();
        fSymbolTable=new SymbolTable();
    }

    public XPointerHandler(SymbolTable symbolTable,
                           XMLErrorHandler errorHandler,XMLErrorReporter errorReporter){
        super();
        fXPointerParts=new Vector();
        fSymbolTable=symbolTable;
        fErrorHandler=errorHandler;
        fXPointerErrorReporter=errorReporter;
        //fErrorReporter = errorReporter; // The XInclude ErrorReporter
    }
    // ************************************************************************
    //  Implementation of the XPointerProcessor interface.
    // ************************************************************************

    public void parseXPointer(String xpointer) throws XNIException{
        // Initialize
        init();
        // tokens
        final Tokens tokens=new Tokens(fSymbolTable);
        // scanner
        Scanner scanner=new Scanner(fSymbolTable){
            protected void addToken(Tokens tokens,int token)
                    throws XNIException{
                if(token==Tokens.XPTRTOKEN_OPEN_PAREN
                        ||token==Tokens.XPTRTOKEN_CLOSE_PAREN
                        ||token==Tokens.XPTRTOKEN_SCHEMENAME
                        ||token==Tokens.XPTRTOKEN_SCHEMEDATA
                        ||token==Tokens.XPTRTOKEN_SHORTHAND){
                    super.addToken(tokens,token);
                    return;
                }
                reportError("InvalidXPointerToken",new Object[]{tokens
                        .getTokenString(token)});
            }
        };
        // scan the XPointer expression
        int length=xpointer.length();
        boolean success=scanner.scanExpr(fSymbolTable,tokens,xpointer,0,
                length);
        if(!success)
            reportError("InvalidXPointerExpression",new Object[]{xpointer});
        while(tokens.hasMore()){
            int token=tokens.nextToken();
            switch(token){
                case Tokens.XPTRTOKEN_SHORTHAND:{
                    // The shortHand name
                    token=tokens.nextToken();
                    String shortHandPointerName=tokens.getTokenString(token);
                    if(shortHandPointerName==null){
                        reportError("InvalidXPointerExpression",
                                new Object[]{xpointer});
                    }
                    XPointerPart shortHandPointer=new ShortHandPointer(
                            fSymbolTable);
                    shortHandPointer.setSchemeName(shortHandPointerName);
                    fXPointerParts.add(shortHandPointer);
                    break;
                }
                case Tokens.XPTRTOKEN_SCHEMENAME:{
                    // Retreive the local name and prefix to form the scheme name
                    token=tokens.nextToken();
                    String prefix=tokens.getTokenString(token);
                    token=tokens.nextToken();
                    String localName=tokens.getTokenString(token);
                    String schemeName=prefix+localName;
                    // The next character should be an open parenthesis
                    int openParenCount=0;
                    int closeParenCount=0;
                    token=tokens.nextToken();
                    String openParen=tokens.getTokenString(token);
                    if(openParen!="XPTRTOKEN_OPEN_PAREN"){
                        // can not have more than one ShortHand Pointer
                        if(token==Tokens.XPTRTOKEN_SHORTHAND){
                            reportError("MultipleShortHandPointers",
                                    new Object[]{xpointer});
                        }else{
                            reportError("InvalidXPointerExpression",
                                    new Object[]{xpointer});
                        }
                    }
                    openParenCount++;
                    // followed by zero or more ( and  the schemeData
                    String schemeData=null;
                    while(tokens.hasMore()){
                        token=tokens.nextToken();
                        schemeData=tokens.getTokenString(token);
                        if(schemeData!="XPTRTOKEN_OPEN_PAREN"){
                            break;
                        }
                        openParenCount++;
                    }
                    token=tokens.nextToken();
                    schemeData=tokens.getTokenString(token);
                    // followed by the same number of )
                    token=tokens.nextToken();
                    String closeParen=tokens.getTokenString(token);
                    if(closeParen!="XPTRTOKEN_CLOSE_PAREN"){
                        reportError("SchemeDataNotFollowedByCloseParenthesis",
                                new Object[]{xpointer});
                    }
                    closeParenCount++;
                    while(tokens.hasMore()){
                        if(tokens.getTokenString(tokens.peekToken())!="XPTRTOKEN_OPEN_PAREN"){
                            break;
                        }
                        closeParenCount++;
                    }
                    // check if the number of open parenthesis are equal to the number of close parenthesis
                    if(openParenCount!=closeParenCount){
                        reportError("UnbalancedParenthesisInXPointerExpression",
                                new Object[]{xpointer,
                                        new Integer(openParenCount),
                                        new Integer(closeParenCount)});
                    }
                    // Perform scheme specific parsing of the pointer part
                    if(schemeName.equals(ELEMENT_SCHEME_NAME)){
                        XPointerPart elementSchemePointer=new ElementSchemePointer(
                                fSymbolTable,fErrorReporter);
                        elementSchemePointer.setSchemeName(schemeName);
                        elementSchemePointer.setSchemeData(schemeData);
                        // If an exception occurs while parsing the element() scheme expression
                        // ignore it and move on to the next pointer part
                        try{
                            elementSchemePointer.parseXPointer(schemeData);
                            fXPointerParts.add(elementSchemePointer);
                        }catch(XNIException e){
                            // Re-throw the XPointer element() scheme syntax error.
                            throw new XNIException(e);
                        }
                    }else{
                        // ????
                        reportWarning("SchemeUnsupported",
                                new Object[]{schemeName});
                    }
                    break;
                }
                default:
                    reportError("InvalidXPointerExpression",
                            new Object[]{xpointer});
            }
        }
    }

    public boolean resolveXPointer(QName element,XMLAttributes attributes,
                                   Augmentations augs,int event) throws XNIException{
        boolean resolved=false;
        // The result of the first pointer part whose evaluation identifies
        // one or more subresources is reported by the XPointer processor as the
        // result of the pointer as a whole, and evaluation stops.
        // In our implementation, typically the first xpointer scheme that
        // matches an element is the document is considered.
        // If the pointer part resolved then use it, else search for the fragment
        // using next pointer part from lef-right.
        if(!fFoundMatchingPtrPart){
            // for each element, attempt to resolve it against each pointer part
            // in the XPointer expression until a matching element is found.
            for(int i=0;i<fXPointerParts.size();i++){
                fXPointerPart=(XPointerPart)fXPointerParts.get(i);
                if(fXPointerPart.resolveXPointer(element,attributes,augs,
                        event)){
                    fFoundMatchingPtrPart=true;
                    resolved=true;
                }
            }
        }else{
            if(fXPointerPart.resolveXPointer(element,attributes,augs,event)){
                resolved=true;
            }
        }
        if(!fIsXPointerResolved){
            fIsXPointerResolved=resolved;
        }
        return resolved;
    }

    public boolean isFragmentResolved() throws XNIException{
        boolean resolved=(fXPointerPart!=null)?fXPointerPart.isFragmentResolved()
                :false;
        if(!fIsXPointerResolved){
            fIsXPointerResolved=resolved;
        }
        return resolved;
    }

    public boolean isXPointerResolved() throws XNIException{
        return fIsXPointerResolved;
    }

    private void reportError(String key,Object[] arguments)
            throws XNIException{
        /**
         fXPointerErrorReporter.reportError(
         XPointerMessageFormatter.XPOINTER_DOMAIN, key, arguments,
         XMLErrorReporter.SEVERITY_ERROR);
         */
        throw new XNIException((fErrorReporter
                .getMessageFormatter(XPointerMessageFormatter.XPOINTER_DOMAIN))
                .formatMessage(fErrorReporter.getLocale(),key,arguments));
    }

    private void reportWarning(String key,Object[] arguments)
            throws XNIException{
        fXPointerErrorReporter.reportError(
                XPointerMessageFormatter.XPOINTER_DOMAIN,key,arguments,
                XMLErrorReporter.SEVERITY_WARNING);
    }

    protected void init(){
        fXPointerParts.clear();
        fXPointerPart=null;
        fFoundMatchingPtrPart=false;
        fIsXPointerResolved=false;
        //fFixupBase = false;
        //fFixupLang = false;
        initErrorReporter();
    }

    protected void initErrorReporter(){
        if(fXPointerErrorReporter==null){
            fXPointerErrorReporter=new XMLErrorReporter();
        }
        if(fErrorHandler==null){
            fErrorHandler=new XPointerErrorHandler();
        }
        /**
         fXPointerErrorReporter.setProperty(Constants.XERCES_PROPERTY_PREFIX
         + Constants.ERROR_HANDLER_PROPERTY, fErrorHandler);
         */
        fXPointerErrorReporter.putMessageFormatter(
                XPointerMessageFormatter.XPOINTER_DOMAIN,
                new XPointerMessageFormatter());
    }

    public XPointerPart getXPointerPart(){
        return fXPointerPart;
    }

    public Vector getPointerParts(){
        return fXPointerParts;
    }

    // ************************************************************************
    // Overridden XMLComponent methods
    // ************************************************************************
    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        // Error reporter
        if(propertyId==Constants.XERCES_PROPERTY_PREFIX
                +Constants.ERROR_REPORTER_PROPERTY){
            if(value!=null){
                fXPointerErrorReporter=(XMLErrorReporter)value;
            }else{
                fXPointerErrorReporter=null;
            }
        }
        // Error handler
        if(propertyId==Constants.XERCES_PROPERTY_PREFIX
                +Constants.ERROR_HANDLER_PROPERTY){
            if(value!=null){
                fErrorHandler=(XMLErrorHandler)value;
            }else{
                fErrorHandler=null;
            }
        }
        // xml:lang
        if(propertyId==Constants.XERCES_FEATURE_PREFIX
                +Constants.XINCLUDE_FIXUP_LANGUAGE_FEATURE){
            if(value!=null){
                fFixupLang=((Boolean)value).booleanValue();
            }else{
                fFixupLang=false;
            }
        }
        // xml:base
        if(propertyId==Constants.XERCES_FEATURE_PREFIX
                +Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE){
            if(value!=null){
                fFixupBase=((Boolean)value).booleanValue();
            }else{
                fFixupBase=false;
            }
        }
        //
        if(propertyId==Constants.XERCES_PROPERTY_PREFIX
                +Constants.NAMESPACE_CONTEXT_PROPERTY){
            fNamespaceContext=(XIncludeNamespaceSupport)value;
        }
        super.setProperty(propertyId,value);
    }

    // ************************************************************************
    //  Overridden XMLDocumentHandler methods
    // ************************************************************************
    public void comment(XMLString text,Augmentations augs) throws XNIException{
        if(!isChildFragmentResolved()){
            return;
        }
        super.comment(text,augs);
    }

    public boolean isChildFragmentResolved() throws XNIException{
        boolean resolved=(fXPointerPart!=null)?fXPointerPart
                .isChildFragmentResolved():false;
        return resolved;
    }

    public void processingInstruction(String target,XMLString data,
                                      Augmentations augs) throws XNIException{
        if(!isChildFragmentResolved()){
            return;
        }
        super.processingInstruction(target,data,augs);
    }

    public void startElement(QName element,XMLAttributes attributes,
                             Augmentations augs) throws XNIException{
        if(!resolveXPointer(element,attributes,augs,
                XPointerPart.EVENT_ELEMENT_START)){
            // xml:base and xml:lang processing
            if(fFixupBase){
                processXMLBaseAttributes(attributes);
            }
            if(fFixupLang){
                processXMLLangAttributes(attributes);
            }
            // set the context invalid if the element till an element from the result infoset is included
            fNamespaceContext.setContextInvalid();
            return;
        }
        super.startElement(element,attributes,augs);
    }

    public void emptyElement(QName element,XMLAttributes attributes,
                             Augmentations augs) throws XNIException{
        if(!resolveXPointer(element,attributes,augs,
                XPointerPart.EVENT_ELEMENT_EMPTY)){
            // xml:base and xml:lang processing
            if(fFixupBase){
                processXMLBaseAttributes(attributes);
            }
            if(fFixupLang){
                processXMLLangAttributes(attributes);
            }
            // no need to restore restoreBaseURI() for xml:base and xml:lang processing
            // set the context invalid if the element till an element from the result infoset is included
            fNamespaceContext.setContextInvalid();
            return;
        }
        super.emptyElement(element,attributes,augs);
    }

    public void endElement(QName element,Augmentations augs)
            throws XNIException{
        if(!resolveXPointer(element,null,augs,
                XPointerPart.EVENT_ELEMENT_END)){
            // no need to restore restoreBaseURI() for xml:base and xml:lang processing
            return;
        }
        super.endElement(element,augs);
    }

    public void characters(XMLString text,Augmentations augs)
            throws XNIException{
        if(!isChildFragmentResolved()){
            return;
        }
        super.characters(text,augs);
    }

    public void ignorableWhitespace(XMLString text,Augmentations augs)
            throws XNIException{
        if(!isChildFragmentResolved()){
            return;
        }
        super.ignorableWhitespace(text,augs);
    }

    public void startCDATA(Augmentations augs) throws XNIException{
        if(!isChildFragmentResolved()){
            return;
        }
        super.startCDATA(augs);
    }

    public void endCDATA(Augmentations augs) throws XNIException{
        if(!isChildFragmentResolved()){
            return;
        }
        super.endCDATA(augs);
    }

    private final class Tokens{
        private static final int XPTRTOKEN_OPEN_PAREN=0,
                XPTRTOKEN_CLOSE_PAREN=1, XPTRTOKEN_SHORTHAND=2,
                XPTRTOKEN_SCHEMENAME=3, XPTRTOKEN_SCHEMEDATA=4;
        // Token count
        private static final int INITIAL_TOKEN_COUNT=1<<8;
        // Token names
        private final String[] fgTokenNames={"XPTRTOKEN_OPEN_PAREN",
                "XPTRTOKEN_CLOSE_PAREN","XPTRTOKEN_SHORTHAND",
                "XPTRTOKEN_SCHEMENAME","XPTRTOKEN_SCHEMEDATA"};
        private int[] fTokens=new int[INITIAL_TOKEN_COUNT];
        private int fTokenCount=0;
        // Current token position
        private int fCurrentTokenIndex;
        private SymbolTable fSymbolTable;
        private Hashtable fTokenNames=new Hashtable();

        private Tokens(SymbolTable symbolTable){
            fSymbolTable=symbolTable;
            fTokenNames.put(new Integer(XPTRTOKEN_OPEN_PAREN),
                    "XPTRTOKEN_OPEN_PAREN");
            fTokenNames.put(new Integer(XPTRTOKEN_CLOSE_PAREN),
                    "XPTRTOKEN_CLOSE_PAREN");
            fTokenNames.put(new Integer(XPTRTOKEN_SHORTHAND),
                    "XPTRTOKEN_SHORTHAND");
            fTokenNames.put(new Integer(XPTRTOKEN_SCHEMENAME),
                    "XPTRTOKEN_SCHEMENAME");
            fTokenNames.put(new Integer(XPTRTOKEN_SCHEMEDATA),
                    "XPTRTOKEN_SCHEMEDATA");
        }

        private void addToken(String tokenStr){
            Integer tokenInt=(Integer)fTokenNames.get(tokenStr);
            if(tokenInt==null){
                tokenInt=new Integer(fTokenNames.size());
                fTokenNames.put(tokenInt,tokenStr);
            }
            addToken(tokenInt.intValue());
        }

        private void addToken(int token){
            try{
                fTokens[fTokenCount]=token;
            }catch(ArrayIndexOutOfBoundsException ex){
                int[] oldList=fTokens;
                fTokens=new int[fTokenCount<<1];
                System.arraycopy(oldList,0,fTokens,0,fTokenCount);
                fTokens[fTokenCount]=token;
            }
            fTokenCount++;
        }

        private void rewind(){
            fCurrentTokenIndex=0;
        }

        private boolean hasMore(){
            return fCurrentTokenIndex<fTokenCount;
        }

        private int peekToken() throws XNIException{
            if(fCurrentTokenIndex==fTokenCount){
                reportError("XPointerProcessingError",null);
            }
            return fTokens[fCurrentTokenIndex];
        }

        private String nextTokenAsString() throws XNIException{
            String tokenStrint=getTokenString(nextToken());
            if(tokenStrint==null){
                reportError("XPointerProcessingError",null);
            }
            return tokenStrint;
        }

        private String getTokenString(int token){
            return (String)fTokenNames.get(new Integer(token));
        }

        private int nextToken() throws XNIException{
            if(fCurrentTokenIndex==fTokenCount){
                reportError("XPointerProcessingError",null);
            }
            return fTokens[fCurrentTokenIndex++];
        }
    }

    private class Scanner{
        private static final byte CHARTYPE_INVALID=0, // invalid XML character
                CHARTYPE_OTHER=1, // not special - one of "#%&;?\`{}~" or DEL
                CHARTYPE_WHITESPACE=2, // one of "\t\n\r " (0x09, 0x0A, 0x0D, 0x20)
                CHARTYPE_CARRET=3, // ^
                CHARTYPE_OPEN_PAREN=4, // '(' (0x28)
                CHARTYPE_CLOSE_PAREN=5, // ')' (0x29)
                CHARTYPE_MINUS=6, // '-' (0x2D)
                CHARTYPE_PERIOD=7, // '.' (0x2E)
                CHARTYPE_SLASH=8, // '/' (0x2F)
                CHARTYPE_DIGIT=9, // '0'-'9' (0x30 to 0x39)
                CHARTYPE_COLON=10, // ':' (0x3A)
                CHARTYPE_EQUAL=11, // '=' (0x3D)
                CHARTYPE_LETTER=12, // 'A'-'Z' or 'a'-'z' (0x41 to 0x5A and 0x61 to 0x7A)
                CHARTYPE_UNDERSCORE=13, // '_' (0x5F)
                CHARTYPE_NONASCII=14; // Non-ASCII Unicode codepoint (>= 0x80)
        private final byte[] fASCIICharMap={0,0,0,0,0,0,0,0,0,2,2,
                0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                2,1,1,1,1,1,1,1,4,5,1,1,1,6,7,8,9,9,9,9,9,
                9,9,9,9,9,10,1,1,11,1,1,1,12,12,12,12,12,12,
                12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,
                12,12,12,12,1,1,1,3,13,1,12,12,12,12,12,12,12,
                12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,
                12,12,12,1,1,1,1,1};
        //
        // Data
        //
        private SymbolTable fSymbolTable;

        private Scanner(SymbolTable symbolTable){
            // save pool and tokens
            fSymbolTable=symbolTable;
        } // <init>(SymbolTable)

        private boolean scanExpr(SymbolTable symbolTable,Tokens tokens,
                                 String data,int currentOffset,int endOffset)
                throws XNIException{
            int ch;
            int openParen=0;
            int closeParen=0;
            int nameOffset, dataOffset;
            boolean isQName=false;
            String name=null;
            String prefix=null;
            String schemeData=null;
            StringBuffer schemeDataBuff=new StringBuffer();
            while(true){
                if(currentOffset==endOffset){
                    break;
                }
                ch=data.charAt(currentOffset);
                //
                while(ch==' '||ch==0x0A||ch==0x09||ch==0x0D){
                    if(++currentOffset==endOffset){
                        break;
                    }
                    ch=data.charAt(currentOffset);
                }
                if(currentOffset==endOffset){
                    break;
                }
                //
                // [1]    Pointer      ::=    Shorthand | SchemeBased
                // [2]    Shorthand    ::=    NCName
                // [3]    SchemeBased  ::=    PointerPart (S? PointerPart)*
                // [4]    PointerPart  ::=    SchemeName '(' SchemeData ')'
                // [5]    SchemeName   ::=    QName
                // [6]    SchemeData   ::=    EscapedData*
                // [7]    EscapedData  ::=    NormalChar | '^(' | '^)' | '^^' | '(' SchemeData ')'
                // [8]    NormalChar   ::=    UnicodeChar - [()^]
                // [9]    UnicodeChar  ::=    [#x0-#x10FFFF]
                // [?]    QName        ::=    (NCName ':')? NCName
                // [?]    NCName       ::=    (Letter | '_') (NCNameChar)*
                // [?]    NCNameChar   ::=    Letter | Digit | '.' | '-' | '_'  (ascii subset of 'NCNameChar')
                // [?]    Letter       ::=    [A-Za-z]                              (ascii subset of 'Letter')
                // [?]    Digit        ::=    [0-9]                                  (ascii subset of 'Digit')
                //
                byte chartype=(ch>=0x80)?CHARTYPE_NONASCII
                        :fASCIICharMap[ch];
                switch(chartype){
                    case CHARTYPE_OPEN_PAREN: // '('
                        addToken(tokens,Tokens.XPTRTOKEN_OPEN_PAREN);
                        openParen++;
                        ++currentOffset;
                        break;
                    case CHARTYPE_CLOSE_PAREN: // ')'
                        addToken(tokens,Tokens.XPTRTOKEN_CLOSE_PAREN);
                        closeParen++;
                        ++currentOffset;
                        break;
                    case CHARTYPE_CARRET:
                    case CHARTYPE_COLON:
                    case CHARTYPE_DIGIT:
                    case CHARTYPE_EQUAL:
                    case CHARTYPE_LETTER:
                    case CHARTYPE_MINUS:
                    case CHARTYPE_NONASCII:
                    case CHARTYPE_OTHER:
                    case CHARTYPE_PERIOD:
                    case CHARTYPE_SLASH:
                    case CHARTYPE_UNDERSCORE:
                    case CHARTYPE_WHITESPACE:
                        // Scanning SchemeName | Shorthand
                        if(openParen==0){
                            nameOffset=currentOffset;
                            currentOffset=scanNCName(data,endOffset,
                                    currentOffset);
                            if(currentOffset==nameOffset){
                                reportError("InvalidShortHandPointer",
                                        new Object[]{data});
                                return false;
                            }
                            if(currentOffset<endOffset){
                                ch=data.charAt(currentOffset);
                            }else{
                                ch=-1;
                            }
                            name=symbolTable.addSymbol(data.substring(nameOffset,
                                    currentOffset));
                            prefix=XMLSymbols.EMPTY_STRING;
                            // The name is a QName => a SchemeName
                            if(ch==':'){
                                if(++currentOffset==endOffset){
                                    return false;
                                }
                                ch=data.charAt(currentOffset);
                                prefix=name;
                                nameOffset=currentOffset;
                                currentOffset=scanNCName(data,endOffset,
                                        currentOffset);
                                if(currentOffset==nameOffset){
                                    return false;
                                }
                                if(currentOffset<endOffset){
                                    ch=data.charAt(currentOffset);
                                }else{
                                    ch=-1;
                                }
                                isQName=true;
                                name=symbolTable.addSymbol(data.substring(
                                        nameOffset,currentOffset));
                            }
                            // REVISIT:
                            if(currentOffset!=endOffset){
                                addToken(tokens,Tokens.XPTRTOKEN_SCHEMENAME);
                                tokens.addToken(prefix);
                                tokens.addToken(name);
                                isQName=false;
                            }else if(currentOffset==endOffset){
                                // NCName => Shorthand
                                addToken(tokens,Tokens.XPTRTOKEN_SHORTHAND);
                                tokens.addToken(name);
                                isQName=false;
                            }
                            // reset open/close paren for the next pointer part
                            closeParen=0;
                            break;
                        }else if(openParen>0&&closeParen==0&&name!=null){
                            // Scanning SchemeData
                            dataOffset=currentOffset;
                            currentOffset=scanData(data,schemeDataBuff,
                                    endOffset,currentOffset);
                            if(currentOffset==dataOffset){
                                reportError("InvalidSchemeDataInXPointer",
                                        new Object[]{data});
                                return false;
                            }
                            if(currentOffset<endOffset){
                                ch=data.charAt(currentOffset);
                            }else{
                                ch=-1;
                            }
                            schemeData=symbolTable.addSymbol(schemeDataBuff
                                    .toString());
                            addToken(tokens,Tokens.XPTRTOKEN_SCHEMEDATA);
                            tokens.addToken(schemeData);
                            // reset open/close paren for the next pointer part
                            openParen=0;
                            schemeDataBuff.delete(0,schemeDataBuff.length());
                        }else{
                            // ex. schemeName()
                            // Should we throw an exception with a more suitable message instead??
                            return false;
                        }
                }
            } // end while
            return true;
        }

        private int scanNCName(String data,int endOffset,int currentOffset){
            int ch=data.charAt(currentOffset);
            if(ch>=0x80){
                if(!XMLChar.isNameStart(ch)){
                    return currentOffset;
                }
            }else{
                byte chartype=fASCIICharMap[ch];
                if(chartype!=CHARTYPE_LETTER
                        &&chartype!=CHARTYPE_UNDERSCORE){
                    return currentOffset;
                }
            }
            //while (currentOffset++ < endOffset) {
            while(++currentOffset<endOffset){
                ch=data.charAt(currentOffset);
                if(ch>=0x80){
                    if(!XMLChar.isName(ch)){
                        break;
                    }
                }else{
                    byte chartype=fASCIICharMap[ch];
                    if(chartype!=CHARTYPE_LETTER
                            &&chartype!=CHARTYPE_DIGIT
                            &&chartype!=CHARTYPE_PERIOD
                            &&chartype!=CHARTYPE_MINUS
                            &&chartype!=CHARTYPE_UNDERSCORE){
                        break;
                    }
                }
            }
            return currentOffset;
        }

        private int scanData(String data,StringBuffer schemeData,
                             int endOffset,int currentOffset){
            while(true){
                if(currentOffset==endOffset){
                    break;
                }
                int ch=data.charAt(currentOffset);
                byte chartype=(ch>=0x80)?CHARTYPE_NONASCII
                        :fASCIICharMap[ch];
                if(chartype==CHARTYPE_OPEN_PAREN){
                    schemeData.append(ch);
                    //schemeData.append(Tokens.XPTRTOKEN_OPEN_PAREN);
                    currentOffset=scanData(data,schemeData,endOffset,
                            ++currentOffset);
                    if(currentOffset==endOffset){
                        return currentOffset;
                    }
                    ch=data.charAt(currentOffset);
                    chartype=(ch>=0x80)?CHARTYPE_NONASCII
                            :fASCIICharMap[ch];
                    if(chartype!=CHARTYPE_CLOSE_PAREN){
                        return endOffset;
                    }
                    schemeData.append((char)ch);
                    ++currentOffset;//
                }else if(chartype==CHARTYPE_CLOSE_PAREN){
                    return currentOffset;
                }else if(chartype==CHARTYPE_CARRET){
                    ch=data.charAt(++currentOffset);
                    chartype=(ch>=0x80)?CHARTYPE_NONASCII
                            :fASCIICharMap[ch];
                    if(chartype!=CHARTYPE_CARRET
                            &&chartype!=CHARTYPE_OPEN_PAREN
                            &&chartype!=CHARTYPE_CLOSE_PAREN){
                        break;
                    }
                    schemeData.append((char)ch);
                    ++currentOffset;
                }else{
                    schemeData.append((char)ch);
                    ++currentOffset;//
                }
            }
            return currentOffset;
        }
        //
        // Protected methods
        //

        protected void addToken(Tokens tokens,int token) throws XNIException{
            tokens.addToken(token);
        } // addToken(int)
    } // class Scanner
}
