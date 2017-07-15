/**
 * Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at

 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.*;
import com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar;

import java.io.EOFException;
import java.io.IOException;

public class XMLDTDScannerImpl
        extends XMLScanner
        implements XMLDTDScanner, XMLComponent, XMLEntityHandler{
    //
    // Constants
    //
    // scanner states
    protected static final int SCANNER_STATE_END_OF_INPUT=0;
    protected static final int SCANNER_STATE_TEXT_DECL=1;
    protected static final int SCANNER_STATE_MARKUP_DECL=2;
    // recognized features and properties
    private static final String[] RECOGNIZED_FEATURES={
            VALIDATION,
            NOTIFY_CHAR_REFS,
    };
    private static final Boolean[] FEATURE_DEFAULTS={
            null,
            Boolean.FALSE,
    };
    private static final String[] RECOGNIZED_PROPERTIES={
            SYMBOL_TABLE,
            ERROR_REPORTER,
            ENTITY_MANAGER,
    };
    private static final Object[] PROPERTY_DEFAULTS={
            null,
            null,
            null,
    };
    // debugging
    private static final boolean DEBUG_SCANNER_STATE=false;
    //
    // Data
    //
    // handlers
    public XMLDTDHandler fDTDHandler=null;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    // state
    protected int fScannerState;
    protected boolean fStandalone;
    protected boolean fSeenExternalDTD;
    protected boolean fSeenExternalPE;
    DTDGrammar nvGrammarInfo=null;
    boolean nonValidatingMode=false;
    // private data
    private boolean fStartDTDCalled;
    private XMLAttributesImpl fAttributes=new XMLAttributesImpl();
    private int[] fContentStack=new int[5];
    private int fContentDepth;
    private int[] fPEStack=new int[5];
    private boolean[] fPEReport=new boolean[5];
    private int fPEDepth;
    private int fMarkUpDepth;
    private int fExtEntityDepth;
    private int fIncludeSectDepth;
    // temporary variables
    private String[] fStrings=new String[3];
    private XMLString fString=new XMLString();
    private XMLStringBuffer fStringBuffer=new XMLStringBuffer();
    private XMLStringBuffer fStringBuffer2=new XMLStringBuffer();
    private XMLString fLiteral=new XMLString();
    private XMLString fLiteral2=new XMLString();
    private String[] fEnumeration=new String[5];
    private int fEnumerationCount;
    private XMLStringBuffer fIgnoreConditionalBuffer=new XMLStringBuffer(128);
    //
    // Constructors
    //

    public XMLDTDScannerImpl(){
    } // <init>()

    public XMLDTDScannerImpl(SymbolTable symbolTable,
                             XMLErrorReporter errorReporter,XMLEntityManager entityManager){
        fSymbolTable=symbolTable;
        fErrorReporter=errorReporter;
        fEntityManager=entityManager;
        entityManager.setProperty(SYMBOL_TABLE,fSymbolTable);
    }
    //
    // XMLDTDScanner methods
    //

    public void setInputSource(XMLInputSource inputSource) throws IOException{
        if(inputSource==null){
            // no system id was available
            if(fDTDHandler!=null){
                fDTDHandler.startDTD(null,null);
                fDTDHandler.endDTD(null);
            }
            if(nonValidatingMode){
                nvGrammarInfo.startDTD(null,null);
                nvGrammarInfo.endDTD(null);
            }
            return;
        }
        fEntityManager.setEntityHandler(this);
        fEntityManager.startDTDEntity(inputSource);
    } // setInputSource(XMLInputSource)

    public boolean scanDTDInternalSubset(boolean complete,boolean standalone,
                                         boolean hasExternalSubset)
            throws IOException, XNIException{
        // reset entity scanner
        //xxx:stax getText() is supposed to return only DTD internal subset
        //shouldn't we record position here before we go ahead ??
        fEntityScanner=(XMLEntityScanner)fEntityManager.getEntityScanner();
        fEntityManager.setEntityHandler(this);
        fStandalone=standalone;
        //System.out.println("state"+fScannerState);
        if(fScannerState==SCANNER_STATE_TEXT_DECL){
            // call handler
            if(fDTDHandler!=null){
                fDTDHandler.startDTD(fEntityScanner,null);
                fStartDTDCalled=true;
            }
            if(nonValidatingMode){
                fStartDTDCalled=true;
                nvGrammarInfo.startDTD(fEntityScanner,null);
            }
            // set starting state for internal subset
            setScannerState(SCANNER_STATE_MARKUP_DECL);
        }
        // keep dispatching "events"
        do{
            if(!scanDecls(complete)){
                // call handler
                if(fDTDHandler!=null&&hasExternalSubset==false){
                    fDTDHandler.endDTD(null);
                }
                if(nonValidatingMode&&hasExternalSubset==false){
                    nvGrammarInfo.endDTD(null);
                }
                // we're done, set starting state for external subset
                setScannerState(SCANNER_STATE_TEXT_DECL);
                // we're done scanning DTD.
                fLimitAnalyzer.reset(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT);
                fLimitAnalyzer.reset(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT);
                return false;
            }
        }while(complete);
        // return that there is more to scan
        return true;
    } // scanDTDInternalSubset(boolean,boolean,boolean):boolean

    public boolean scanDTDExternalSubset(boolean complete)
            throws IOException, XNIException{
        fEntityManager.setEntityHandler(this);
        if(fScannerState==SCANNER_STATE_TEXT_DECL){
            fSeenExternalDTD=true;
            boolean textDecl=scanTextDecl();
            if(fScannerState==SCANNER_STATE_END_OF_INPUT){
                return false;
            }else{
                // next state is markup decls regardless of whether there
                // is a TextDecl or not
                setScannerState(SCANNER_STATE_MARKUP_DECL);
                if(textDecl&&!complete){
                    return true;
                }
            }
        }
        // keep dispatching "events"
        do{
            if(!scanDecls(complete)){
                return false;
            }
        }while(complete);
        // return that there is more to scan
        return true;
    } // scanDTDExternalSubset(boolean):boolean

    @Override
    public boolean skipDTD(boolean supportDTD) throws IOException{
        if(supportDTD)
            return false;
        fStringBuffer.clear();
        while(fEntityScanner.scanData("]",fStringBuffer)){
            int c=fEntityScanner.peekChar();
            if(c!=-1){
                if(XMLChar.isHighSurrogate(c)){
                    scanSurrogates(fStringBuffer);
                }
                if(isInvalidLiteral(c)){
                    reportFatalError("InvalidCharInDTD",
                            new Object[]{Integer.toHexString(c)});
                    fEntityScanner.scanChar(null);
                }
            }
        }
        fEntityScanner.fCurrentEntity.position--;
        return true;
    }

    public void setLimitAnalyzer(XMLLimitAnalyzer limitAnalyzer){
        fLimitAnalyzer=limitAnalyzer;
    }
    //
    // XMLComponent methods
    //

    public void reset(XMLComponentManager componentManager)
            throws XMLConfigurationException{
        super.reset(componentManager);
        init();
    } // reset(XMLComponentManager)

    // this is made for something like XMLDTDLoader--XMLComponentManager-free operation...
    public void reset(){
        super.reset();
        init();
    }

    public void reset(PropertyManager props){
        setPropertyManager(props);
        super.reset(props);
        init();
        nonValidatingMode=true;
        //Revisit : Create new grammar until we implement GrammarPool.
        nvGrammarInfo=new DTDGrammar(fSymbolTable);
    }

    public void startEntity(String name,
                            XMLResourceIdentifier identifier,
                            String encoding,Augmentations augs) throws XNIException{
        super.startEntity(name,identifier,encoding,augs);
        boolean dtdEntity=name.equals("[dtd]");
        if(dtdEntity){
            // call handler
            if(fDTDHandler!=null&&!fStartDTDCalled){
                fDTDHandler.startDTD(fEntityScanner,null);
            }
            if(fDTDHandler!=null){
                fDTDHandler.startExternalSubset(identifier,null);
            }
            fEntityManager.startExternalSubset();
            fEntityStore.startExternalSubset();
            fExtEntityDepth++;
        }else if(name.charAt(0)=='%'){
            pushPEStack(fMarkUpDepth,fReportEntity);
            if(fEntityScanner.isExternal()){
                fExtEntityDepth++;
            }
        }
        // call handler
        if(fDTDHandler!=null&&!dtdEntity&&fReportEntity){
            fDTDHandler.startParameterEntity(name,identifier,encoding,null);
        }
    } // startEntity(String,XMLResourceIdentifier,String)

    public void endEntity(String name,Augmentations augs)
            throws XNIException, IOException{
        super.endEntity(name,augs);
        // if there is no data after the doctype
        //
        if(fScannerState==SCANNER_STATE_END_OF_INPUT)
            return;
        // Handle end of PE
        boolean reportEntity=fReportEntity;
        if(name.startsWith("%")){
            reportEntity=peekReportEntity();
            // check well-formedness of the enity
            int startMarkUpDepth=popPEStack();
            // throw fatalError if this entity was incomplete and
            // was a freestanding decl
            if(startMarkUpDepth==0&&
                    startMarkUpDepth<fMarkUpDepth){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "ILL_FORMED_PARAMETER_ENTITY_WHEN_USED_IN_DECL",
                        new Object[]{fEntityManager.fCurrentEntity.name},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
            if(startMarkUpDepth!=fMarkUpDepth){
                reportEntity=false;
                if(fValidation){
                    // Proper nesting of parameter entities is a Validity Constraint
                    // and must not be enforced when validation is off
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                            "ImproperDeclarationNesting",
                            new Object[]{name},
                            XMLErrorReporter.SEVERITY_ERROR);
                }
            }
            if(fEntityScanner.isExternal()){
                fExtEntityDepth--;
            }
        }
        // call handler
        boolean dtdEntity=name.equals("[dtd]");
        if(fDTDHandler!=null&&!dtdEntity&&reportEntity){
            fDTDHandler.endParameterEntity(name,null);
        }
        // end DTD
        if(dtdEntity){
            if(fIncludeSectDepth!=0){
                reportFatalError("IncludeSectUnterminated",null);
            }
            fScannerState=SCANNER_STATE_END_OF_INPUT;
            // call handler
            fEntityManager.endExternalSubset();
            fEntityStore.endExternalSubset();
            if(fDTDHandler!=null){
                fDTDHandler.endExternalSubset(null);
                fDTDHandler.endDTD(null);
            }
            fExtEntityDepth--;
        }
        //XML (Document Entity) is the last opened entity, however
        //if for some reason DTD Scanner receives this callback
        //there is something wrong (probably invalid XML), throw exception.
        //or
        //For standalone DTD loader, it might be the last opened entity
        //and if this is the last opened entity and fMarkUpDepth != 0 or
        //fIncludeSectDepth != 0 or fExtEntityDepth != 0 throw Exception
        if(augs!=null&&Boolean.TRUE.equals(augs.getItem(Constants.LAST_ENTITY))
                &&(fMarkUpDepth!=0||fExtEntityDepth!=0||fIncludeSectDepth!=0)){
            throw new EOFException();
        }
    } // endEntity(String)

    private final int popPEStack(){
        return fPEStack[--fPEDepth];
    }

    private final boolean peekReportEntity(){
        return fPEReport[fPEDepth-1];
    }
    //
    // XMLDTDSource methods
    //

    private final void pushPEStack(int depth,boolean report){
        if(fPEStack.length==fPEDepth){
            int[] newIntStack=new int[fPEDepth*2];
            System.arraycopy(fPEStack,0,newIntStack,0,fPEDepth);
            fPEStack=newIntStack;
            // report end/start calls
            boolean[] newBooleanStack=new boolean[fPEDepth*2];
            System.arraycopy(fPEReport,0,newBooleanStack,0,fPEDepth);
            fPEReport=newBooleanStack;
        }
        fPEReport[fPEDepth]=report;
        fPEStack[fPEDepth++]=depth;
    }    public void setDTDHandler(XMLDTDHandler dtdHandler){
        fDTDHandler=dtdHandler;
    } // setDTDHandler(XMLDTDHandler)

    // private methods
    private void init(){
        // reset state related data
        fStartDTDCalled=false;
        fExtEntityDepth=0;
        fIncludeSectDepth=0;
        fMarkUpDepth=0;
        fPEDepth=0;
        fStandalone=false;
        fSeenExternalDTD=false;
        fSeenExternalPE=false;
        // set starting state
        setScannerState(SCANNER_STATE_TEXT_DECL);
        //new SymbolTable());
        fLimitAnalyzer=fEntityManager.fLimitAnalyzer;
        fSecurityManager=fEntityManager.fSecurityManager;
    }    public XMLDTDHandler getDTDHandler(){
        return fDTDHandler;
    } // getDTDHandler():  XMLDTDHandler
    //
    // XMLDTDContentModelSource methods
    //

    protected final void setScannerState(int state){
        fScannerState=state;
        if(DEBUG_SCANNER_STATE){
            System.out.print("### setScannerState: ");
            System.out.print(getScannerStateName(state));
            //System.out.println();
        }
    } // setScannerState(int)    public void setDTDContentModelHandler(XMLDTDContentModelHandler
                                                  dtdContentModelHandler){
        fDTDContentModelHandler=dtdContentModelHandler;
    } // setDTDContentModelHandler

    private static String getScannerStateName(int state){
        if(DEBUG_SCANNER_STATE){
            switch(state){
                case SCANNER_STATE_END_OF_INPUT:
                    return "SCANNER_STATE_END_OF_INPUT";
                case SCANNER_STATE_TEXT_DECL:
                    return "SCANNER_STATE_TEXT_DECL";
                case SCANNER_STATE_MARKUP_DECL:
                    return "SCANNER_STATE_MARKUP_DECL";
            }
        }
        return "??? ("+state+')';
    } // getScannerStateName(int):String    public XMLDTDContentModelHandler getDTDContentModelHandler(){
        return fDTDContentModelHandler;
    } // setDTDContentModelHandler
    //
    // XMLEntityHandler methods
    //

    public String[] getRecognizedFeatures(){
        return (String[])(RECOGNIZED_FEATURES.clone());
    } // getRecognizedFeatures():String[]

    public String[] getRecognizedProperties(){
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]
    // helper methods

    public Boolean getFeatureDefault(String featureId){
        for(int i=0;i<RECOGNIZED_FEATURES.length;i++){
            if(RECOGNIZED_FEATURES[i].equals(featureId)){
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean
    //
    // Private methods
    //

    public Object getPropertyDefault(String propertyId){
        for(int i=0;i<RECOGNIZED_PROPERTIES.length;i++){
            if(RECOGNIZED_PROPERTIES[i].equals(propertyId)){
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object

    protected final boolean scanningInternalSubset(){
        return fExtEntityDepth==0;
    }

    protected void startPE(String name,boolean literal)
            throws IOException, XNIException{
        int depth=fPEDepth;
        String pName="%"+name;
        if(fValidation&&!fEntityStore.isDeclaredEntity(pName)){
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,"EntityNotDeclared",
                    new Object[]{name},XMLErrorReporter.SEVERITY_ERROR);
        }
        fEntityManager.startEntity(false,fSymbolTable.addSymbol(pName),
                literal);
        // if we actually got a new entity and it's external
        // parse text decl if there is any
        if(depth!=fPEDepth&&fEntityScanner.isExternal()){
            scanTextDecl();
        }
    }

    protected final boolean scanTextDecl()
            throws IOException, XNIException{
        // scan XMLDecl
        boolean textDecl=false;
        if(fEntityScanner.skipString("<?xml")){
            fMarkUpDepth++;
            // NOTE: special case where document starts with a PI
            //       whose name starts with "xml" (e.g. "xmlfoo")
            if(isValidNameChar(fEntityScanner.peekChar())){
                fStringBuffer.clear();
                fStringBuffer.append("xml");
                while(isValidNameChar(fEntityScanner.peekChar())){
                    fStringBuffer.append((char)fEntityScanner.scanChar(null));
                }
                String target=
                        fSymbolTable.addSymbol(fStringBuffer.ch,
                                fStringBuffer.offset,
                                fStringBuffer.length);
                scanPIData(target,fString);
            }
            // standard Text declaration
            else{
                // pseudo-attribute values
                String version=null;
                String encoding=null;
                scanXMLDeclOrTextDecl(true,fStrings);
                textDecl=true;
                fMarkUpDepth--;
                version=fStrings[0];
                encoding=fStrings[1];
                fEntityScanner.setEncoding(encoding);
                // call handler
                if(fDTDHandler!=null){
                    fDTDHandler.textDecl(version,encoding,null);
                }
            }
        }
        fEntityManager.fCurrentEntity.mayReadChunks=true;
        return textDecl;
    } // scanTextDecl(boolean):boolean

    protected final void scanPIData(String target,XMLString data)
            throws IOException, XNIException{
        //Venu REVISIT
        //      super.scanPIData(target, data);
        fMarkUpDepth--;
        // call handler
        if(fDTDHandler!=null){
            fDTDHandler.processingInstruction(target,data,null);
        }
    } // scanPIData(String)

    protected final void scanComment() throws IOException, XNIException{
        fReportEntity=false;
        scanComment(fStringBuffer);
        fMarkUpDepth--;
        // call handler
        if(fDTDHandler!=null){
            fDTDHandler.comment(fStringBuffer,null);
        }
        fReportEntity=true;
    } // scanComment()

    protected final void scanElementDecl() throws IOException, XNIException{
        // spaces
        fReportEntity=false;
        if(!skipSeparator(true,!scanningInternalSubset())){
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL",
                    null);
        }
        // element name
        String name=fEntityScanner.scanName(NameType.ELEMENTSTART);
        if(name==null){
            reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL",
                    null);
        }
        // spaces
        if(!skipSeparator(true,!scanningInternalSubset())){
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL",
                    new Object[]{name});
        }
        // content model
        if(fDTDContentModelHandler!=null){
            fDTDContentModelHandler.startContentModel(name,null);
        }
        String contentModel=null;
        fReportEntity=true;
        if(fEntityScanner.skipString("EMPTY")){
            contentModel="EMPTY";
            // call handler
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.empty(null);
            }
        }else if(fEntityScanner.skipString("ANY")){
            contentModel="ANY";
            // call handler
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.any(null);
            }
        }else{
            if(!fEntityScanner.skipChar('(',null)){
                reportFatalError("MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN",
                        new Object[]{name});
            }
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.startGroup(null);
            }
            fStringBuffer.clear();
            fStringBuffer.append('(');
            fMarkUpDepth++;
            skipSeparator(false,!scanningInternalSubset());
            // Mixed content model
            if(fEntityScanner.skipString("#PCDATA")){
                scanMixed(name);
            }else{              // children content
                scanChildren(name);
            }
            contentModel=fStringBuffer.toString();
        }
        // call handler
        if(fDTDContentModelHandler!=null){
            fDTDContentModelHandler.endContentModel(null);
        }
        fReportEntity=false;
        skipSeparator(false,!scanningInternalSubset());
        // end
        if(!fEntityScanner.skipChar('>',null)){
            reportFatalError("ElementDeclUnterminated",new Object[]{name});
        }
        fReportEntity=true;
        fMarkUpDepth--;
        // call handler
        if(fDTDHandler!=null){
            fDTDHandler.elementDecl(name,contentModel,null);
        }
        if(nonValidatingMode) nvGrammarInfo.elementDecl(name,contentModel,null);
    } // scanElementDecl()

    private final void scanMixed(String elName)
            throws IOException, XNIException{
        String childName=null;
        fStringBuffer.append("#PCDATA");
        // call handler
        if(fDTDContentModelHandler!=null){
            fDTDContentModelHandler.pcdata(null);
        }
        skipSeparator(false,!scanningInternalSubset());
        while(fEntityScanner.skipChar('|',null)){
            fStringBuffer.append('|');
            // call handler
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.separator(XMLDTDContentModelHandler.SEPARATOR_CHOICE,
                        null);
            }
            skipSeparator(false,!scanningInternalSubset());
            childName=fEntityScanner.scanName(NameType.ENTITY);
            if(childName==null){
                reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT",
                        new Object[]{elName});
            }
            fStringBuffer.append(childName);
            // call handler
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.element(childName,null);
            }
            skipSeparator(false,!scanningInternalSubset());
        }
        // The following check must be done in a single call (as opposed to one
        // for ')' and then one for '*') to guarantee that callbacks are
        // properly nested. We do not want to trigger endEntity too early in
        // case we cross the boundary of an entity between the two characters.
        if(fEntityScanner.skipString(")*")){
            fStringBuffer.append(")*");
            // call handler
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.endGroup(null);
                fDTDContentModelHandler.occurrence(XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE,
                        null);
            }
        }else if(childName!=null){
            reportFatalError("MixedContentUnterminated",
                    new Object[]{elName});
        }else if(fEntityScanner.skipChar(')',null)){
            fStringBuffer.append(')');
            // call handler
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.endGroup(null);
            }
        }else{
            reportFatalError("MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN",
                    new Object[]{elName});
        }
        fMarkUpDepth--;
        // we are done
    }

    private final void scanChildren(String elName)
            throws IOException, XNIException{
        fContentDepth=0;
        pushContentStack(0);
        int currentOp=0;
        int c;
        while(true){
            if(fEntityScanner.skipChar('(',null)){
                fMarkUpDepth++;
                fStringBuffer.append('(');
                // call handler
                if(fDTDContentModelHandler!=null){
                    fDTDContentModelHandler.startGroup(null);
                }
                // push current op on stack and reset it
                pushContentStack(currentOp);
                currentOp=0;
                skipSeparator(false,!scanningInternalSubset());
                continue;
            }
            skipSeparator(false,!scanningInternalSubset());
            String childName=fEntityScanner.scanName(NameType.ELEMENTSTART);
            if(childName==null){
                reportFatalError("MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN",
                        new Object[]{elName});
                return;
            }
            // call handler
            if(fDTDContentModelHandler!=null){
                fDTDContentModelHandler.element(childName,null);
            }
            fStringBuffer.append(childName);
            c=fEntityScanner.peekChar();
            if(c=='?'||c=='*'||c=='+'){
                // call handler
                if(fDTDContentModelHandler!=null){
                    short oc;
                    if(c=='?'){
                        oc=XMLDTDContentModelHandler.OCCURS_ZERO_OR_ONE;
                    }else if(c=='*'){
                        oc=XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE;
                    }else{
                        oc=XMLDTDContentModelHandler.OCCURS_ONE_OR_MORE;
                    }
                    fDTDContentModelHandler.occurrence(oc,null);
                }
                fEntityScanner.scanChar(null);
                fStringBuffer.append((char)c);
            }
            while(true){
                skipSeparator(false,!scanningInternalSubset());
                c=fEntityScanner.peekChar();
                if(c==','&&currentOp!='|'){
                    currentOp=c;
                    // call handler
                    if(fDTDContentModelHandler!=null){
                        fDTDContentModelHandler.separator(XMLDTDContentModelHandler.SEPARATOR_SEQUENCE,
                                null);
                    }
                    fEntityScanner.scanChar(null);
                    fStringBuffer.append(',');
                    break;
                }else if(c=='|'&&currentOp!=','){
                    currentOp=c;
                    // call handler
                    if(fDTDContentModelHandler!=null){
                        fDTDContentModelHandler.separator(XMLDTDContentModelHandler.SEPARATOR_CHOICE,
                                null);
                    }
                    fEntityScanner.scanChar(null);
                    fStringBuffer.append('|');
                    break;
                }else if(c!=')'){
                    reportFatalError("MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN",
                            new Object[]{elName});
                }
                // call handler
                if(fDTDContentModelHandler!=null){
                    fDTDContentModelHandler.endGroup(null);
                }
                // restore previous op
                currentOp=popContentStack();
                short oc;
                // The following checks must be done in a single call (as
                // opposed to one for ')' and then one for '?', '*', and '+')
                // to guarantee that callbacks are properly nested. We do not
                // want to trigger endEntity too early in case we cross the
                // boundary of an entity between the two characters.
                if(fEntityScanner.skipString(")?")){
                    fStringBuffer.append(")?");
                    // call handler
                    if(fDTDContentModelHandler!=null){
                        oc=XMLDTDContentModelHandler.OCCURS_ZERO_OR_ONE;
                        fDTDContentModelHandler.occurrence(oc,null);
                    }
                }else if(fEntityScanner.skipString(")+")){
                    fStringBuffer.append(")+");
                    // call handler
                    if(fDTDContentModelHandler!=null){
                        oc=XMLDTDContentModelHandler.OCCURS_ONE_OR_MORE;
                        fDTDContentModelHandler.occurrence(oc,null);
                    }
                }else if(fEntityScanner.skipString(")*")){
                    fStringBuffer.append(")*");
                    // call handler
                    if(fDTDContentModelHandler!=null){
                        oc=XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE;
                        fDTDContentModelHandler.occurrence(oc,null);
                    }
                }else{
                    // no occurrence specified
                    fEntityScanner.scanChar(null);
                    fStringBuffer.append(')');
                }
                fMarkUpDepth--;
                if(fContentDepth==0){
                    return;
                }
            }
            skipSeparator(false,!scanningInternalSubset());
        }
    }

    protected final void scanAttlistDecl() throws IOException, XNIException{
        // spaces
        fReportEntity=false;
        if(!skipSeparator(true,!scanningInternalSubset())){
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL",
                    null);
        }
        // element name
        String elName=fEntityScanner.scanName(NameType.ELEMENTSTART);
        if(elName==null){
            reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL",
                    null);
        }
        // call handler
        if(fDTDHandler!=null){
            fDTDHandler.startAttlist(elName,null);
        }
        // spaces
        if(!skipSeparator(true,!scanningInternalSubset())){
            // no space, is it the end yet?
            if(fEntityScanner.skipChar('>',null)){
                // yes, stop here
                // call handler
                if(fDTDHandler!=null){
                    fDTDHandler.endAttlist(null);
                }
                fMarkUpDepth--;
                return;
            }else{
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF",
                        new Object[]{elName});
            }
        }
        // definitions
        while(!fEntityScanner.skipChar('>',null)){
            String name=fEntityScanner.scanName(NameType.ATTRIBUTENAME);
            if(name==null){
                reportFatalError("AttNameRequiredInAttDef",
                        new Object[]{elName});
            }
            // spaces
            if(!skipSeparator(true,!scanningInternalSubset())){
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF",
                        new Object[]{elName,name});
            }
            // type
            String type=scanAttType(elName,name);
            // spaces
            if(!skipSeparator(true,!scanningInternalSubset())){
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF",
                        new Object[]{elName,name});
            }
            // default decl
            String defaultType=scanAttDefaultDecl(elName,name,
                    type,
                    fLiteral,fLiteral2);
            // REVISIT: Should we do anything with the non-normalized
            //          default attribute value? -Ac
            // yes--according to bug 5073.  - neilg
            String[] enumr=null;
            if(fDTDHandler!=null||nonValidatingMode){
                if(fEnumerationCount!=0){
                    enumr=new String[fEnumerationCount];
                    System.arraycopy(fEnumeration,0,enumr,
                            0,fEnumerationCount);
                }
            }
            // call handler
            // Determine whether the default value to be passed should be null.
            // REVISIT: should probably check whether fLiteral.ch is null instead. LM.
            if(defaultType!=null&&(defaultType.equals("#REQUIRED")||
                    defaultType.equals("#IMPLIED"))){
                if(fDTDHandler!=null){
                    fDTDHandler.attributeDecl(elName,name,type,enumr,
                            defaultType,null,null,null);
                }
                if(nonValidatingMode){
                    nvGrammarInfo.attributeDecl(elName,name,type,enumr,
                            defaultType,null,null,null);
                }
            }else{
                if(fDTDHandler!=null){
                    fDTDHandler.attributeDecl(elName,name,type,enumr,
                            defaultType,fLiteral,fLiteral2,null);
                }
                if(nonValidatingMode){
                    nvGrammarInfo.attributeDecl(elName,name,type,enumr,
                            defaultType,fLiteral,fLiteral2,null);
                }
            }
            skipSeparator(false,!scanningInternalSubset());
        }
        // call handler
        if(fDTDHandler!=null){
            fDTDHandler.endAttlist(null);
        }
        fMarkUpDepth--;
        fReportEntity=true;
    } // scanAttlistDecl()

    private final String scanAttType(String elName,String atName)
            throws IOException, XNIException{
        String type=null;
        fEnumerationCount=0;
        /**
         * Watchout: the order here is important: when a string happens to
         * be a substring of another string, the longer one needs to be
         * looked for first!!
         */
        if(fEntityScanner.skipString("CDATA")){
            type="CDATA";
        }else if(fEntityScanner.skipString("IDREFS")){
            type="IDREFS";
        }else if(fEntityScanner.skipString("IDREF")){
            type="IDREF";
        }else if(fEntityScanner.skipString("ID")){
            type="ID";
        }else if(fEntityScanner.skipString("ENTITY")){
            type="ENTITY";
        }else if(fEntityScanner.skipString("ENTITIES")){
            type="ENTITIES";
        }else if(fEntityScanner.skipString("NMTOKENS")){
            type="NMTOKENS";
        }else if(fEntityScanner.skipString("NMTOKEN")){
            type="NMTOKEN";
        }else if(fEntityScanner.skipString("NOTATION")){
            type="NOTATION";
            // spaces
            if(!skipSeparator(true,!scanningInternalSubset())){
                reportFatalError("MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE",
                        new Object[]{elName,atName});
            }
            // open paren
            int c=fEntityScanner.scanChar(null);
            if(c!='('){
                reportFatalError("MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE",
                        new Object[]{elName,atName});
            }
            fMarkUpDepth++;
            do{
                skipSeparator(false,!scanningInternalSubset());
                String aName=fEntityScanner.scanName(NameType.ATTRIBUTENAME);
                if(aName==null){
                    reportFatalError("MSG_NAME_REQUIRED_IN_NOTATIONTYPE",
                            new Object[]{elName,atName});
                }
                ensureEnumerationSize(fEnumerationCount+1);
                fEnumeration[fEnumerationCount++]=aName;
                skipSeparator(false,!scanningInternalSubset());
                c=fEntityScanner.scanChar(null);
            }while(c=='|');
            if(c!=')'){
                reportFatalError("NotationTypeUnterminated",
                        new Object[]{elName,atName});
            }
            fMarkUpDepth--;
        }else{              // Enumeration
            type="ENUMERATION";
            // open paren
            int c=fEntityScanner.scanChar(null);
            if(c!='('){
                //                       "OPEN_PAREN_REQUIRED_BEFORE_ENUMERATION_IN_ATTRDECL",
                reportFatalError("AttTypeRequiredInAttDef",
                        new Object[]{elName,atName});
            }
            fMarkUpDepth++;
            do{
                skipSeparator(false,!scanningInternalSubset());
                String token=fEntityScanner.scanNmtoken();
                if(token==null){
                    reportFatalError("MSG_NMTOKEN_REQUIRED_IN_ENUMERATION",
                            new Object[]{elName,atName});
                }
                ensureEnumerationSize(fEnumerationCount+1);
                fEnumeration[fEnumerationCount++]=token;
                skipSeparator(false,!scanningInternalSubset());
                c=fEntityScanner.scanChar(null);
            }while(c=='|');
            if(c!=')'){
                reportFatalError("EnumerationUnterminated",
                        new Object[]{elName,atName});
            }
            fMarkUpDepth--;
        }
        return type;
    } // scanAttType():String

    protected final String scanAttDefaultDecl(String elName,String atName,
                                              String type,
                                              XMLString defaultVal,
                                              XMLString nonNormalizedDefaultVal)
            throws IOException, XNIException{
        String defaultType=null;
        fString.clear();
        defaultVal.clear();
        if(fEntityScanner.skipString("#REQUIRED")){
            defaultType="#REQUIRED";
        }else if(fEntityScanner.skipString("#IMPLIED")){
            defaultType="#IMPLIED";
        }else{
            if(fEntityScanner.skipString("#FIXED")){
                defaultType="#FIXED";
                // spaces
                if(!skipSeparator(true,!scanningInternalSubset())){
                    reportFatalError("MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL",
                            new Object[]{elName,atName});
                }
            }
            // AttValue
            boolean isVC=!fStandalone&&(fSeenExternalDTD||fSeenExternalPE);
            scanAttributeValue(defaultVal,nonNormalizedDefaultVal,atName,
                    fAttributes,0,isVC,elName,false);
        }
        return defaultType;
    } // ScanAttDefaultDecl

    private final void scanEntityDecl() throws IOException, XNIException{
        boolean isPEDecl=false;
        boolean sawPERef=false;
        fReportEntity=false;
        if(fEntityScanner.skipSpaces()){
            if(!fEntityScanner.skipChar('%',NameType.REFERENCE)){
                isPEDecl=false; // <!ENTITY x "x">
            }else if(skipSeparator(true,!scanningInternalSubset())){
                // <!ENTITY % x "x">
                isPEDecl=true;
            }else if(scanningInternalSubset()){
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL",
                        null);
                isPEDecl=true;
            }else if(fEntityScanner.peekChar()=='%'){
                // <!ENTITY %%x; "x"> is legal
                skipSeparator(false,!scanningInternalSubset());
                isPEDecl=true;
            }else{
                sawPERef=true;
            }
        }else if(scanningInternalSubset()||!fEntityScanner.skipChar('%',NameType.REFERENCE)){
            // <!ENTITY[^ ]...> or <!ENTITY[^ %]...>
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL",
                    null);
            isPEDecl=false;
        }else if(fEntityScanner.skipSpaces()){
            // <!ENTITY% ...>
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL",
                    null);
            isPEDecl=false;
        }else{
            sawPERef=true;
        }
        if(sawPERef){
            while(true){
                String peName=fEntityScanner.scanName(NameType.REFERENCE);
                if(peName==null){
                    reportFatalError("NameRequiredInPEReference",null);
                }else if(!fEntityScanner.skipChar(';',NameType.REFERENCE)){
                    reportFatalError("SemicolonRequiredInPEReference",
                            new Object[]{peName});
                }else{
                    startPE(peName,false);
                }
                fEntityScanner.skipSpaces();
                if(!fEntityScanner.skipChar('%',NameType.REFERENCE))
                    break;
                if(!isPEDecl){
                    if(skipSeparator(true,!scanningInternalSubset())){
                        isPEDecl=true;
                        break;
                    }
                    isPEDecl=fEntityScanner.skipChar('%',NameType.REFERENCE);
                }
            }
        }
        // name
        String name=fEntityScanner.scanName(NameType.ENTITY);
        if(name==null){
            reportFatalError("MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL",null);
        }
        // spaces
        if(!skipSeparator(true,!scanningInternalSubset())){
            reportFatalError("MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL",
                    new Object[]{name});
        }
        // external id
        scanExternalID(fStrings,false);
        String systemId=fStrings[0];
        String publicId=fStrings[1];
        if(isPEDecl&&systemId!=null){
            fSeenExternalPE=true;
        }
        String notation=null;
        // NDATA
        boolean sawSpace=skipSeparator(true,!scanningInternalSubset());
        if(!isPEDecl&&fEntityScanner.skipString("NDATA")){
            // check whether there was space before NDATA
            if(!sawSpace){
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NDATA_IN_UNPARSED_ENTITYDECL",
                        new Object[]{name});
            }
            // spaces
            if(!skipSeparator(true,!scanningInternalSubset())){
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL",
                        new Object[]{name});
            }
            notation=fEntityScanner.scanName(NameType.NOTATION);
            if(notation==null){
                reportFatalError("MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL",
                        new Object[]{name});
            }
        }
        // internal entity
        if(systemId==null){
            scanEntityValue(name,isPEDecl,fLiteral,fLiteral2);
            // since we need it's value anyway, let's snag it so it doesn't get corrupted
            // if a new load takes place before we store the entity values
            fStringBuffer.clear();
            fStringBuffer2.clear();
            fStringBuffer.append(fLiteral.ch,fLiteral.offset,fLiteral.length);
            fStringBuffer2.append(fLiteral2.ch,fLiteral2.offset,fLiteral2.length);
        }
        // skip possible trailing space
        skipSeparator(false,!scanningInternalSubset());
        // end
        if(!fEntityScanner.skipChar('>',null)){
            reportFatalError("EntityDeclUnterminated",new Object[]{name});
        }
        fMarkUpDepth--;
        // register entity and make callback
        if(isPEDecl){
            name="%"+name;
        }
        if(systemId!=null){
            String baseSystemId=fEntityScanner.getBaseSystemId();
            if(notation!=null){
                fEntityStore.addUnparsedEntity(name,publicId,systemId,baseSystemId,notation);
            }else{
                fEntityStore.addExternalEntity(name,publicId,systemId,
                        baseSystemId);
            }
            if(fDTDHandler!=null){
                //Venu Revisit : why false has been removed in expandSYstem
                fResourceIdentifier.setValues(publicId,systemId,baseSystemId,XMLEntityManager.expandSystemId(systemId,baseSystemId));
                if(notation!=null){
                    fDTDHandler.unparsedEntityDecl(name,fResourceIdentifier,
                            notation,null);
                }else{
                    fDTDHandler.externalEntityDecl(name,fResourceIdentifier,null);
                }
            }
        }else{
            fEntityStore.addInternalEntity(name,fStringBuffer.toString());
            if(fDTDHandler!=null){
                fDTDHandler.internalEntityDecl(name,fStringBuffer,fStringBuffer2,null);
            }
        }
        fReportEntity=true;
    } // scanEntityDecl()

    protected final void scanEntityValue(String entityName,boolean isPEDecl,XMLString value,
                                         XMLString nonNormalizedValue)
            throws IOException, XNIException{
        int quote=fEntityScanner.scanChar(null);
        if(quote!='\''&&quote!='"'){
            reportFatalError("OpenQuoteMissingInDecl",null);
        }
        // store at which depth of entities we start
        int entityDepth=fEntityDepth;
        XMLString literal=fString;
        XMLString literal2=fString;
        int countChar=0;
        if(fLimitAnalyzer==null){
            fLimitAnalyzer=fEntityManager.fLimitAnalyzer;
        }
        fLimitAnalyzer.startEntity(entityName);
        if(fEntityScanner.scanLiteral(quote,fString,false)!=quote){
            fStringBuffer.clear();
            fStringBuffer2.clear();
            int offset;
            do{
                countChar=0;
                offset=fStringBuffer.length;
                fStringBuffer.append(fString);
                fStringBuffer2.append(fString);
                if(fEntityScanner.skipChar('&',NameType.REFERENCE)){
                    if(fEntityScanner.skipChar('#',NameType.REFERENCE)){
                        fStringBuffer2.append("&#");
                        scanCharReferenceValue(fStringBuffer,fStringBuffer2);
                    }else{
                        fStringBuffer.append('&');
                        fStringBuffer2.append('&');
                        String eName=fEntityScanner.scanName(NameType.REFERENCE);
                        if(eName==null){
                            reportFatalError("NameRequiredInReference",
                                    null);
                        }else{
                            fStringBuffer.append(eName);
                            fStringBuffer2.append(eName);
                        }
                        if(!fEntityScanner.skipChar(';',NameType.REFERENCE)){
                            reportFatalError("SemicolonRequiredInReference",
                                    new Object[]{eName});
                        }else{
                            fStringBuffer.append(';');
                            fStringBuffer2.append(';');
                        }
                    }
                }else if(fEntityScanner.skipChar('%',NameType.REFERENCE)){
                    while(true){
                        fStringBuffer2.append('%');
                        String peName=fEntityScanner.scanName(NameType.REFERENCE);
                        if(peName==null){
                            reportFatalError("NameRequiredInPEReference",
                                    null);
                        }else if(!fEntityScanner.skipChar(';',NameType.REFERENCE)){
                            reportFatalError("SemicolonRequiredInPEReference",
                                    new Object[]{peName});
                        }else{
                            if(scanningInternalSubset()){
                                reportFatalError("PEReferenceWithinMarkup",
                                        new Object[]{peName});
                            }
                            fStringBuffer2.append(peName);
                            fStringBuffer2.append(';');
                        }
                        startPE(peName,true);
                        // REVISIT: [Q] Why do we skip spaces here? -Ac
                        // REVISIT: This will make returning the non-
                        //          normalized value harder. -Ac
                        fEntityScanner.skipSpaces();
                        if(!fEntityScanner.skipChar('%',NameType.REFERENCE))
                            break;
                    }
                }else{
                    int c=fEntityScanner.peekChar();
                    if(XMLChar.isHighSurrogate(c)){
                        countChar++;
                        scanSurrogates(fStringBuffer2);
                    }else if(isInvalidLiteral(c)){
                        reportFatalError("InvalidCharInLiteral",
                                new Object[]{Integer.toHexString(c)});
                        fEntityScanner.scanChar(null);
                    }
                    // if it's not the delimiting quote or if it is but from a
                    // different entity than the one this literal started from,
                    // simply append the character to our buffer
                    else if(c!=quote||entityDepth!=fEntityDepth){
                        fStringBuffer.append((char)c);
                        fStringBuffer2.append((char)c);
                        fEntityScanner.scanChar(null);
                    }
                }
                checkEntityLimit(isPEDecl,entityName,fStringBuffer.length-offset+countChar);
            }while(fEntityScanner.scanLiteral(quote,fString,false)!=quote);
            checkEntityLimit(isPEDecl,entityName,fString.length);
            fStringBuffer.append(fString);
            fStringBuffer2.append(fString);
            literal=fStringBuffer;
            literal2=fStringBuffer2;
        }else{
            checkEntityLimit(isPEDecl,entityName,literal);
        }
        value.setValues(literal);
        nonNormalizedValue.setValues(literal2);
        if(fLimitAnalyzer!=null){
            if(isPEDecl){
                fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT,entityName);
            }else{
                fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT,entityName);
            }
        }
        if(!fEntityScanner.skipChar(quote,null)){
            reportFatalError("CloseQuoteMissingInDecl",null);
        }
    } // scanEntityValue(XMLString,XMLString):void

    private final void scanNotationDecl() throws IOException, XNIException{
        // spaces
        fReportEntity=false;
        if(!skipSeparator(true,!scanningInternalSubset())){
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL",
                    null);
        }
        // notation name
        String name=fEntityScanner.scanName(NameType.NOTATION);
        if(name==null){
            reportFatalError("MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL",
                    null);
        }
        // spaces
        if(!skipSeparator(true,!scanningInternalSubset())){
            reportFatalError("MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL",
                    new Object[]{name});
        }
        // external id
        scanExternalID(fStrings,true);
        String systemId=fStrings[0];
        String publicId=fStrings[1];
        String baseSystemId=fEntityScanner.getBaseSystemId();
        if(systemId==null&&publicId==null){
            reportFatalError("ExternalIDorPublicIDRequired",
                    new Object[]{name});
        }
        // skip possible trailing space
        skipSeparator(false,!scanningInternalSubset());
        // end
        if(!fEntityScanner.skipChar('>',null)){
            reportFatalError("NotationDeclUnterminated",new Object[]{name});
        }
        fMarkUpDepth--;
        fResourceIdentifier.setValues(publicId,systemId,baseSystemId,XMLEntityManager.expandSystemId(systemId,baseSystemId));
        if(nonValidatingMode) nvGrammarInfo.notationDecl(name,fResourceIdentifier,null);
        // call handler
        if(fDTDHandler!=null){
            //Venu Revisit wby false has been removed.
            //fResourceIdentifier.setValues(publicId, systemId, baseSystemId, XMLEntityManager.expandSystemId(systemId, baseSystemId, false));
            fDTDHandler.notationDecl(name,fResourceIdentifier,null);
        }
        fReportEntity=true;
    } // scanNotationDecl()

    private final void scanConditionalSect(int currPEDepth)
            throws IOException, XNIException{
        fReportEntity=false;
        skipSeparator(false,!scanningInternalSubset());
        if(fEntityScanner.skipString("INCLUDE")){
            skipSeparator(false,!scanningInternalSubset());
            if(currPEDepth!=fPEDepth&&fValidation){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "INVALID_PE_IN_CONDITIONAL",
                        new Object[]{fEntityManager.fCurrentEntity.name},
                        XMLErrorReporter.SEVERITY_ERROR);
            }
            // call handler
            if(!fEntityScanner.skipChar('[',null)){
                reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",null);
            }
            if(fDTDHandler!=null){
                fDTDHandler.startConditional(XMLDTDHandler.CONDITIONAL_INCLUDE,
                        null);
            }
            fIncludeSectDepth++;
            // just stop there and go back to the main loop
            fReportEntity=true;
        }else if(fEntityScanner.skipString("IGNORE")){
            skipSeparator(false,!scanningInternalSubset());
            if(currPEDepth!=fPEDepth&&fValidation){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "INVALID_PE_IN_CONDITIONAL",
                        new Object[]{fEntityManager.fCurrentEntity.name},
                        XMLErrorReporter.SEVERITY_ERROR);
            }
            // call handler
            if(fDTDHandler!=null){
                fDTDHandler.startConditional(XMLDTDHandler.CONDITIONAL_IGNORE,
                        null);
            }
            if(!fEntityScanner.skipChar('[',null)){
                reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",null);
            }
            fReportEntity=true;
            int initialDepth=++fIncludeSectDepth;
            if(fDTDHandler!=null){
                fIgnoreConditionalBuffer.clear();
            }
            while(true){
                if(fEntityScanner.skipChar('<',null)){
                    if(fDTDHandler!=null){
                        fIgnoreConditionalBuffer.append('<');
                    }
                    //
                    // These tests are split so that we handle cases like
                    // '<<![' and '<!<![' which we might otherwise miss.
                    //
                    if(fEntityScanner.skipChar('!',null)){
                        if(fEntityScanner.skipChar('[',null)){
                            if(fDTDHandler!=null){
                                fIgnoreConditionalBuffer.append("![");
                            }
                            fIncludeSectDepth++;
                        }else{
                            if(fDTDHandler!=null){
                                fIgnoreConditionalBuffer.append("!");
                            }
                        }
                    }
                }else if(fEntityScanner.skipChar(']',null)){
                    if(fDTDHandler!=null){
                        fIgnoreConditionalBuffer.append(']');
                    }
                    //
                    // The same thing goes for ']<![' and '<]]>', etc.
                    //
                    if(fEntityScanner.skipChar(']',null)){
                        if(fDTDHandler!=null){
                            fIgnoreConditionalBuffer.append(']');
                        }
                        while(fEntityScanner.skipChar(']',null)){
                            /** empty loop body */
                            if(fDTDHandler!=null){
                                fIgnoreConditionalBuffer.append(']');
                            }
                        }
                        if(fEntityScanner.skipChar('>',null)){
                            if(fIncludeSectDepth--==initialDepth){
                                fMarkUpDepth--;
                                // call handler
                                if(fDTDHandler!=null){
                                    fLiteral.setValues(fIgnoreConditionalBuffer.ch,0,
                                            fIgnoreConditionalBuffer.length-2);
                                    fDTDHandler.ignoredCharacters(fLiteral,null);
                                    fDTDHandler.endConditional(null);
                                }
                                return;
                            }else if(fDTDHandler!=null){
                                fIgnoreConditionalBuffer.append('>');
                            }
                        }
                    }
                }else{
                    int c=fEntityScanner.scanChar(null);
                    if(fScannerState==SCANNER_STATE_END_OF_INPUT){
                        reportFatalError("IgnoreSectUnterminated",null);
                        return;
                    }
                    if(fDTDHandler!=null){
                        fIgnoreConditionalBuffer.append((char)c);
                    }
                }
            }
        }else{
            reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",null);
        }
    } // scanConditionalSect()

    protected final boolean scanDecls(boolean complete)
            throws IOException, XNIException{
        skipSeparator(false,true);
        boolean again=true;
        //System.out.println("scanDecls"+fScannerState);
        while(again&&fScannerState==SCANNER_STATE_MARKUP_DECL){
            again=complete;
            if(fEntityScanner.skipChar('<',null)){
                fMarkUpDepth++;
                if(fEntityScanner.skipChar('?',null)){
                    fStringBuffer.clear();
                    scanPI(fStringBuffer);
                    fMarkUpDepth--; // we're done with this decl
                }else if(fEntityScanner.skipChar('!',null)){
                    if(fEntityScanner.skipChar('-',null)){
                        if(!fEntityScanner.skipChar('-',null)){
                            reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",
                                    null);
                        }else{
                            scanComment();
                        }
                    }else if(fEntityScanner.skipString("ELEMENT")){
                        scanElementDecl();
                    }else if(fEntityScanner.skipString("ATTLIST")){
                        scanAttlistDecl();
                    }else if(fEntityScanner.skipString("ENTITY")){
                        scanEntityDecl();
                    }else if(fEntityScanner.skipString("NOTATION")){
                        scanNotationDecl();
                    }else if(fEntityScanner.skipChar('[',null)&&
                            !scanningInternalSubset()){
                        scanConditionalSect(fPEDepth);
                    }else{
                        fMarkUpDepth--;
                        reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",
                                null);
                    }
                }else{
                    fMarkUpDepth--;
                    reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",null);
                }
            }else if(fIncludeSectDepth>0&&fEntityScanner.skipChar(']',null)){
                // end of conditional section?
                if(!fEntityScanner.skipChar(']',null)
                        ||!fEntityScanner.skipChar('>',null)){
                    reportFatalError("IncludeSectUnterminated",null);
                }
                // call handler
                if(fDTDHandler!=null){
                    fDTDHandler.endConditional(null);
                }
                // decreaseMarkupDepth();
                fIncludeSectDepth--;
                fMarkUpDepth--;
            }else if(scanningInternalSubset()&&
                    fEntityScanner.peekChar()==']'){
                // this is the end of the internal subset, let's stop here
                return false;
            }else if(fEntityScanner.skipSpaces()){
                // simply skip
            }else{
                reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",null);
            }
            skipSeparator(false,true);
        }
        return fScannerState!=SCANNER_STATE_END_OF_INPUT;
    }

    private boolean skipSeparator(boolean spaceRequired,boolean lookForPERefs)
            throws IOException, XNIException{
        int depth=fPEDepth;
        boolean sawSpace=fEntityScanner.skipSpaces();
        if(!lookForPERefs||!fEntityScanner.skipChar('%',NameType.REFERENCE)){
            return !spaceRequired||sawSpace||(depth!=fPEDepth);
        }
        while(true){
            String name=fEntityScanner.scanName(NameType.ENTITY);
            if(name==null){
                reportFatalError("NameRequiredInPEReference",null);
            }else if(!fEntityScanner.skipChar(';',NameType.REFERENCE)){
                reportFatalError("SemicolonRequiredInPEReference",
                        new Object[]{name});
            }
            startPE(name,false);
            fEntityScanner.skipSpaces();
            if(!fEntityScanner.skipChar('%',NameType.REFERENCE))
                return true;
        }
    }

    private final void pushContentStack(int c){
        if(fContentStack.length==fContentDepth){
            int[] newStack=new int[fContentDepth*2];
            System.arraycopy(fContentStack,0,newStack,0,fContentDepth);
            fContentStack=newStack;
        }
        fContentStack[fContentDepth++]=c;
    }

    private final int popContentStack(){
        return fContentStack[--fContentDepth];
    }

    private final void ensureEnumerationSize(int size){
        if(fEnumeration.length==size){
            String[] newEnum=new String[size*2];
            System.arraycopy(fEnumeration,0,newEnum,0,size);
            fEnumeration=newEnum;
        }
    }

    public DTDGrammar getGrammar(){
        return nvGrammarInfo;
    }








} // class XMLDTDScannerImpl
