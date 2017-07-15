/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2005 The Apache Software Foundation.
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
 * Copyright 2003-2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xinclude;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.util.URI.MalformedURIException;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.*;
import com.sun.org.apache.xerces.internal.xpointer.XPointerHandler;
import com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor;

import java.io.CharConversionException;
import java.io.IOException;
import java.util.*;

public class XIncludeHandler
        implements XMLComponent, XMLDocumentFilter, XMLDTDFilter{
    public final static String XINCLUDE_DEFAULT_CONFIGURATION=
            "com.sun.org.apache.xerces.internal.parsers.XIncludeParserConfiguration";
    public final static String HTTP_ACCEPT="Accept";
    public final static String HTTP_ACCEPT_LANGUAGE="Accept-Language";
    public final static String XPOINTER="xpointer";
    public final static String XINCLUDE_NS_URI=
            "http://www.w3.org/2001/XInclude".intern();
    public final static String XINCLUDE_INCLUDE="include".intern();
    public final static String XINCLUDE_FALLBACK="fallback".intern();
    public final static String XINCLUDE_PARSE_XML="xml".intern();
    public final static String XINCLUDE_PARSE_TEXT="text".intern();
    public final static String XINCLUDE_ATTR_HREF="href".intern();
    public final static String XINCLUDE_ATTR_PARSE="parse".intern();
    public final static String XINCLUDE_ATTR_ENCODING="encoding".intern();
    public final static String XINCLUDE_ATTR_ACCEPT="accept".intern();
    public final static String XINCLUDE_ATTR_ACCEPT_LANGUAGE="accept-language".intern();
    // Top Level Information Items have [included] property in infoset
    public final static String XINCLUDE_INCLUDED="[included]".intern();
    public final static String CURRENT_BASE_URI="currentBaseURI";
    // used for adding [base URI] attributes
    public final static String XINCLUDE_BASE="base".intern();
    public final static QName XML_BASE_QNAME=
            new QName(
                    XMLSymbols.PREFIX_XML,
                    XINCLUDE_BASE,
                    (XMLSymbols.PREFIX_XML+":"+XINCLUDE_BASE).intern(),
                    NamespaceContext.XML_URI);
    // used for adding [language] attributes
    public final static String XINCLUDE_LANG="lang".intern();
    public final static QName XML_LANG_QNAME=
            new QName(
                    XMLSymbols.PREFIX_XML,
                    XINCLUDE_LANG,
                    (XMLSymbols.PREFIX_XML+":"+XINCLUDE_LANG).intern(),
                    NamespaceContext.XML_URI);
    public final static QName NEW_NS_ATTR_QNAME=
            new QName(
                    XMLSymbols.PREFIX_XMLNS,
                    "",
                    XMLSymbols.PREFIX_XMLNS+":",
                    NamespaceContext.XMLNS_URI);
    public static final String BUFFER_SIZE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.BUFFER_SIZE_PROPERTY;
    // recognized features and properties
    protected static final String VALIDATION=
            Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
    protected static final String SCHEMA_VALIDATION=
            Constants.XERCES_FEATURE_PREFIX+Constants.SCHEMA_VALIDATION_FEATURE;
    protected static final String DYNAMIC_VALIDATION=
            Constants.XERCES_FEATURE_PREFIX+Constants.DYNAMIC_VALIDATION_FEATURE;
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS=
            Constants.SAX_FEATURE_PREFIX
                    +Constants.ALLOW_DTD_EVENTS_AFTER_ENDDTD_FEATURE;
    protected static final String XINCLUDE_FIXUP_BASE_URIS=
            Constants.XERCES_FEATURE_PREFIX+Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE;
    protected static final String XINCLUDE_FIXUP_LANGUAGE=
            Constants.XERCES_FEATURE_PREFIX+Constants.XINCLUDE_FIXUP_LANGUAGE_FEATURE;
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String SECURITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SECURITY_MANAGER_PROPERTY;
    protected static final String PARSER_SETTINGS=
            Constants.XERCES_FEATURE_PREFIX+Constants.PARSER_SETTINGS;
    protected static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    // Processing States
    private final static int STATE_NORMAL_PROCESSING=1;
    // we go into this state after a successful include (thus we ignore the children
    // of the include) or after a fallback
    private final static int STATE_IGNORE=2;
    // we go into this state after a failed include.  If we don't encounter a fallback
    // before we reach the end include tag, it's a fatal error
    private final static int STATE_EXPECT_FALLBACK=3;
    private static final String[] RECOGNIZED_FEATURES=
            {ALLOW_UE_AND_NOTATION_EVENTS,XINCLUDE_FIXUP_BASE_URIS,XINCLUDE_FIXUP_LANGUAGE};
    private static final Boolean[] FEATURE_DEFAULTS={Boolean.TRUE,Boolean.TRUE,Boolean.TRUE};
    private static final String[] RECOGNIZED_PROPERTIES=
            {ERROR_REPORTER,ENTITY_RESOLVER,SECURITY_MANAGER,BUFFER_SIZE};
    private static final Object[] PROPERTY_DEFAULTS={null,null,null,new Integer(XMLEntityManager.DEFAULT_BUFFER_SIZE)};
    // this value must be at least 1
    private static final int INITIAL_SIZE=8;
    // which ASCII characters need to be escaped
    private static final boolean gNeedEscaping[]=new boolean[128];
    // the first hex character if a character needs to be escaped
    private static final char gAfterEscaping1[]=new char[128];
    // the second hex character if a character needs to be escaped
    private static final char gAfterEscaping2[]=new char[128];
    private static final char[] gHexChs={'0','1','2','3','4','5','6','7',
            '8','9','A','B','C','D','E','F'};

    // initialize the above 3 arrays
    static{
        char[] escChs={' ','<','>','"','{','}','|','\\','^','`'};
        int len=escChs.length;
        char ch;
        for(int i=0;i<len;i++){
            ch=escChs[i];
            gNeedEscaping[ch]=true;
            gAfterEscaping1[ch]=gHexChs[ch>>4];
            gAfterEscaping2[ch]=gHexChs[ch&0xf];
        }
    }

    // instance variables
    // for XMLDocumentFilter
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;
    // for XMLDTDFilter
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDSource fDTDSource;
    // for XIncludeHandler
    protected XIncludeHandler fParentXIncludeHandler;
    // for buffer size in XIncludeTextReader
    protected int fBufferSize=XMLEntityManager.DEFAULT_BUFFER_SIZE;
    // It "feels wrong" to store this value here.  However,
    // calculating it can be time consuming, so we cache it.
    // It's never going to change in the lifetime of this XIncludeHandler
    protected String fParentRelativeURI;
    // we cache the child parser configuration, so we don't have to re-create
    // the objects when the parser is re-used
    protected XMLParserConfiguration fChildConfig;
    // The cached child parser configuration, may contain a
    // XInclude or XPointer Handler.  Cache both these
    protected XMLParserConfiguration fXIncludeChildConfig;
    protected XMLParserConfiguration fXPointerChildConfig;
    // The XPointerProcessor
    protected XPointerProcessor fXPtrProcessor=null;
    protected XMLLocator fDocLocation;
    protected XIncludeMessageFormatter fXIncludeMessageFormatter=new XIncludeMessageFormatter();
    protected XIncludeNamespaceSupport fNamespaceContext;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityResolver fEntityResolver;
    protected XMLSecurityManager fSecurityManager;
    protected XMLSecurityPropertyManager fSecurityPropertyMgr;
    // these are needed for text include processing
    protected XIncludeTextReader fXInclude10TextReader;
    protected XIncludeTextReader fXInclude11TextReader;
    // these are needed for XML Base processing
    protected XMLResourceIdentifier fCurrentBaseURI;
    protected IntStack fBaseURIScope;
    protected Stack fBaseURI;
    protected Stack fLiteralSystemID;
    protected Stack fExpandedSystemID;
    // these are needed for Language Fixup
    protected IntStack fLanguageScope;
    protected Stack fLanguageStack;
    protected String fCurrentLanguage;
    // used for passing features on to child XIncludeHandler objects
    protected ParserConfigurationSettings fSettings;
    // The current element depth.  We start at depth 0 (before we've reached any elements).
    // The first element is at depth 1.
    private int fDepth;
    // The current element depth of the result infoset.
    private int fResultDepth;
    // Used to ensure that fallbacks are always children of include elements,
    // and that include elements are never children of other include elements.
    // An index contains true if the ancestor of the current element which resides
    // at that depth was an include element.
    private boolean[] fSawInclude=new boolean[INITIAL_SIZE];
    // Ensures that only one fallback element can be at a single depth.
    // An index contains true if we have seen any fallback elements at that depth,
    // and it is only reset to false when the end tag of the parent is encountered.
    private boolean[] fSawFallback=new boolean[INITIAL_SIZE];
    // The state of the processor at each given depth.
    private int[] fState=new int[INITIAL_SIZE];
    // buffering the necessary DTD events
    private ArrayList fNotations;
    private ArrayList fUnparsedEntities;
    // flags which control whether base URI or language fixup is performed.
    private boolean fFixupBaseURIs=true;
    private boolean fFixupLanguage=true;
    // Constructors
    // for SAX compatibility.
    // Has the value of the ALLOW_UE_AND_NOTATION_EVENTS feature
    private boolean fSendUEAndNotationEvents;
    // XMLComponent methods
    // track the version of the document being parsed
    private boolean fIsXML11;
    // track whether a DTD is being parsed
    private boolean fInDTD;
    // track whether the root element of the result infoset has been processed
    private boolean fSeenRootElement;
    // track whether the child config needs its features refreshed
    private boolean fNeedCopyFeatures=true;

    public XIncludeHandler(){
        fDepth=0;
        fSawFallback[fDepth]=false;
        fSawInclude[fDepth]=false;
        fState[fDepth]=STATE_NORMAL_PROCESSING;
        fNotations=new ArrayList();
        fUnparsedEntities=new ArrayList();
        fBaseURIScope=new IntStack();
        fBaseURI=new Stack();
        fLiteralSystemID=new Stack();
        fExpandedSystemID=new Stack();
        fCurrentBaseURI=new XMLResourceIdentifierImpl();
        fLanguageScope=new IntStack();
        fLanguageStack=new Stack();
        fCurrentLanguage=null;
    }

    @Override
    public void reset(XMLComponentManager componentManager)
            throws XNIException{
        fNamespaceContext=null;
        fDepth=0;
        fResultDepth=isRootDocument()?0:fParentXIncludeHandler.getResultDepth();
        fNotations.clear();
        fUnparsedEntities.clear();
        fParentRelativeURI=null;
        fIsXML11=false;
        fInDTD=false;
        fSeenRootElement=false;
        fBaseURIScope.clear();
        fBaseURI.clear();
        fLiteralSystemID.clear();
        fExpandedSystemID.clear();
        fLanguageScope.clear();
        fLanguageStack.clear();
        // REVISIT: Find a better method for maintaining
        // the state of the XInclude processor. These arrays
        // can potentially grow quite large. Cleaning them
        // out on reset may be very time consuming. -- mrglavas
        //
        // clear the previous settings from the arrays
        for(int i=0;i<fState.length;++i){
            fState[i]=STATE_NORMAL_PROCESSING;
        }
        for(int i=0;i<fSawFallback.length;++i){
            fSawFallback[i]=false;
        }
        for(int i=0;i<fSawInclude.length;++i){
            fSawInclude[i]=false;
        }
        try{
            if(!componentManager.getFeature(PARSER_SETTINGS)){
                // if parser settings have not changed return.
                return;
            }
        }catch(XMLConfigurationException e){
        }
        // parser settings changed. Need to refresh features on child config.
        fNeedCopyFeatures=true;
        try{
            fSendUEAndNotationEvents=
                    componentManager.getFeature(ALLOW_UE_AND_NOTATION_EVENTS);
            if(fChildConfig!=null){
                fChildConfig.setFeature(
                        ALLOW_UE_AND_NOTATION_EVENTS,
                        fSendUEAndNotationEvents);
            }
        }catch(XMLConfigurationException e){
        }
        try{
            fFixupBaseURIs=
                    componentManager.getFeature(XINCLUDE_FIXUP_BASE_URIS);
            if(fChildConfig!=null){
                fChildConfig.setFeature(
                        XINCLUDE_FIXUP_BASE_URIS,
                        fFixupBaseURIs);
            }
        }catch(XMLConfigurationException e){
            fFixupBaseURIs=true;
        }
        try{
            fFixupLanguage=
                    componentManager.getFeature(XINCLUDE_FIXUP_LANGUAGE);
            if(fChildConfig!=null){
                fChildConfig.setFeature(
                        XINCLUDE_FIXUP_LANGUAGE,
                        fFixupLanguage);
            }
        }catch(XMLConfigurationException e){
            fFixupLanguage=true;
        }
        // Get symbol table.
        try{
            SymbolTable value=
                    (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
            if(value!=null){
                fSymbolTable=value;
                if(fChildConfig!=null){
                    fChildConfig.setProperty(SYMBOL_TABLE,value);
                }
            }
        }catch(XMLConfigurationException e){
            fSymbolTable=null;
        }
        // Get error reporter.
        try{
            XMLErrorReporter value=
                    (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
            if(value!=null){
                setErrorReporter(value);
                if(fChildConfig!=null){
                    fChildConfig.setProperty(ERROR_REPORTER,value);
                }
            }
        }catch(XMLConfigurationException e){
            fErrorReporter=null;
        }
        // Get entity resolver.
        try{
            XMLEntityResolver value=
                    (XMLEntityResolver)componentManager.getProperty(
                            ENTITY_RESOLVER);
            if(value!=null){
                fEntityResolver=value;
                if(fChildConfig!=null){
                    fChildConfig.setProperty(ENTITY_RESOLVER,value);
                }
            }
        }catch(XMLConfigurationException e){
            fEntityResolver=null;
        }
        // Get security manager.
        try{
            XMLSecurityManager value=
                    (XMLSecurityManager)componentManager.getProperty(
                            SECURITY_MANAGER);
            if(value!=null){
                fSecurityManager=value;
                if(fChildConfig!=null){
                    fChildConfig.setProperty(SECURITY_MANAGER,value);
                }
            }
        }catch(XMLConfigurationException e){
            fSecurityManager=null;
        }
        fSecurityPropertyMgr=(XMLSecurityPropertyManager)
                componentManager.getProperty(Constants.XML_SECURITY_PROPERTY_MANAGER);
        // Get buffer size.
        try{
            Integer value=
                    (Integer)componentManager.getProperty(
                            BUFFER_SIZE);
            if(value!=null&&value.intValue()>0){
                fBufferSize=value.intValue();
                if(fChildConfig!=null){
                    fChildConfig.setProperty(BUFFER_SIZE,value);
                }
            }else{
                fBufferSize=((Integer)getPropertyDefault(BUFFER_SIZE)).intValue();
            }
        }catch(XMLConfigurationException e){
            fBufferSize=((Integer)getPropertyDefault(BUFFER_SIZE)).intValue();
        }
        // Reset XML 1.0 text reader.
        if(fXInclude10TextReader!=null){
            fXInclude10TextReader.setBufferSize(fBufferSize);
        }
        // Reset XML 1.1 text reader.
        if(fXInclude11TextReader!=null){
            fXInclude11TextReader.setBufferSize(fBufferSize);
        }
        fSettings=new ParserConfigurationSettings();
        copyFeatures(componentManager,fSettings);
        // We don't want a schema validator on the new pipeline,
        // so if it was enabled, we set the feature to false. If
        // the validation feature was also enabled we turn on
        // dynamic validation, so that DTD validation is performed
        // on the included documents only if they have a DOCTYPE.
        // This is consistent with the behaviour on the main pipeline.
        try{
            if(componentManager.getFeature(SCHEMA_VALIDATION)){
                fSettings.setFeature(SCHEMA_VALIDATION,false);
                if(componentManager.getFeature(VALIDATION)){
                    fSettings.setFeature(DYNAMIC_VALIDATION,true);
                }
            }
        }catch(XMLConfigurationException e){
        }
        // Don't reset fChildConfig -- we don't want it to share the same components.
        // It will be reset when it is actually used to parse something.
    } // reset(XMLComponentManager)

    @Override
    public String[] getRecognizedFeatures(){
        return (String[])(RECOGNIZED_FEATURES.clone());
    } // getRecognizedFeatures():String[]

    @Override
    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        if(featureId.equals(ALLOW_UE_AND_NOTATION_EVENTS)){
            fSendUEAndNotationEvents=state;
        }
        if(fSettings!=null){
            fNeedCopyFeatures=true;
            fSettings.setFeature(featureId,state);
        }
    } // setFeature(String,boolean)    @Override
    public void setDocumentHandler(XMLDocumentHandler handler){
        fDocumentHandler=handler;
    }

    @Override
    public String[] getRecognizedProperties(){
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]    @Override
    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    }
    // XMLDocumentHandler methods

    @Override
    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        if(propertyId.equals(SYMBOL_TABLE)){
            fSymbolTable=(SymbolTable)value;
            if(fChildConfig!=null){
                fChildConfig.setProperty(propertyId,value);
            }
            return;
        }
        if(propertyId.equals(ERROR_REPORTER)){
            setErrorReporter((XMLErrorReporter)value);
            if(fChildConfig!=null){
                fChildConfig.setProperty(propertyId,value);
            }
            return;
        }
        if(propertyId.equals(ENTITY_RESOLVER)){
            fEntityResolver=(XMLEntityResolver)value;
            if(fChildConfig!=null){
                fChildConfig.setProperty(propertyId,value);
            }
            return;
        }
        if(propertyId.equals(SECURITY_MANAGER)){
            fSecurityManager=(XMLSecurityManager)value;
            if(fChildConfig!=null){
                fChildConfig.setProperty(propertyId,value);
            }
            return;
        }
        if(propertyId.equals(XML_SECURITY_PROPERTY_MANAGER)){
            fSecurityPropertyMgr=(XMLSecurityPropertyManager)value;
            if(fChildConfig!=null){
                fChildConfig.setProperty(XML_SECURITY_PROPERTY_MANAGER,value);
            }
            return;
        }
        if(propertyId.equals(BUFFER_SIZE)){
            Integer bufferSize=(Integer)value;
            if(fChildConfig!=null){
                fChildConfig.setProperty(propertyId,value);
            }
            if(bufferSize!=null&&bufferSize.intValue()>0){
                fBufferSize=bufferSize.intValue();
                // Reset XML 1.0 text reader.
                if(fXInclude10TextReader!=null){
                    fXInclude10TextReader.setBufferSize(fBufferSize);
                }
                // Reset XML 1.1 text reader.
                if(fXInclude11TextReader!=null){
                    fXInclude11TextReader.setBufferSize(fBufferSize);
                }
            }
            return;
        }
    } // setProperty(String,Object)

    @Override
    public Boolean getFeatureDefault(String featureId){
        for(int i=0;i<RECOGNIZED_FEATURES.length;i++){
            if(RECOGNIZED_FEATURES[i].equals(featureId)){
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean

    @Override
    public Object getPropertyDefault(String propertyId){
        for(int i=0;i<RECOGNIZED_PROPERTIES.length;i++){
            if(RECOGNIZED_PROPERTIES[i].equals(propertyId)){
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object

    private void setErrorReporter(XMLErrorReporter reporter){
        fErrorReporter=reporter;
        if(fErrorReporter!=null){
            fErrorReporter.putMessageFormatter(
                    XIncludeMessageFormatter.XINCLUDE_DOMAIN,fXIncludeMessageFormatter);
            // this ensures the proper location is displayed in error messages
            if(fDocLocation!=null){
                fErrorReporter.setDocumentLocator(fDocLocation);
            }
        }
    }

    // used to know whether to pass declarations to the document handler
    protected boolean isRootDocument(){
        return fParentXIncludeHandler==null;
    }

    // It would be nice if we didn't have to repeat code like this, but there's no interface that has
    // setFeature() and addRecognizedFeatures() that the objects have in common.
    protected void copyFeatures(
            XMLComponentManager from,
            ParserConfigurationSettings to){
        Enumeration features=Constants.getXercesFeatures();
        copyFeatures1(features,Constants.XERCES_FEATURE_PREFIX,from,to);
        features=Constants.getSAXFeatures();
        copyFeatures1(features,Constants.SAX_FEATURE_PREFIX,from,to);
    }

    private void copyFeatures1(
            Enumeration features,
            String featurePrefix,
            XMLComponentManager from,
            ParserConfigurationSettings to){
        while(features.hasMoreElements()){
            String featureId=featurePrefix+(String)features.nextElement();
            to.addRecognizedFeatures(new String[]{featureId});
            try{
                to.setFeature(featureId,from.getFeature(featureId));
            }catch(XMLConfigurationException e){
                // componentManager doesn't support this feature,
                // so we won't worry about it
            }
        }
    }

    @Override
    public void startDocument(
            XMLLocator locator,
            String encoding,
            NamespaceContext namespaceContext,
            Augmentations augs)
            throws XNIException{
        // we do this to ensure that the proper location is reported in errors
        // otherwise, the locator from the root document would always be used
        fErrorReporter.setDocumentLocator(locator);
        if(!isRootDocument()
                &&fParentXIncludeHandler.searchForRecursiveIncludes(locator)){
            reportFatalError(
                    "RecursiveInclude",
                    new Object[]{locator.getExpandedSystemId()});
        }
        if(!(namespaceContext instanceof XIncludeNamespaceSupport)){
            reportFatalError("IncompatibleNamespaceContext");
        }
        fNamespaceContext=(XIncludeNamespaceSupport)namespaceContext;
        fDocLocation=locator;
        // initialize the current base URI
        fCurrentBaseURI.setBaseSystemId(locator.getBaseSystemId());
        fCurrentBaseURI.setExpandedSystemId(locator.getExpandedSystemId());
        fCurrentBaseURI.setLiteralSystemId(locator.getLiteralSystemId());
        saveBaseURI();
        if(augs==null){
            augs=new AugmentationsImpl();
        }
        augs.putItem(CURRENT_BASE_URI,fCurrentBaseURI);
        // initialize the current language
        fCurrentLanguage=XMLSymbols.EMPTY_STRING;
        saveLanguage(fCurrentLanguage);
        if(isRootDocument()&&fDocumentHandler!=null){
            fDocumentHandler.startDocument(
                    locator,
                    encoding,
                    namespaceContext,
                    augs);
        }
    }

    @Override
    public void xmlDecl(
            String version,
            String encoding,
            String standalone,
            Augmentations augs)
            throws XNIException{
        fIsXML11="1.1".equals(version);
        if(isRootDocument()&&fDocumentHandler!=null){
            fDocumentHandler.xmlDecl(version,encoding,standalone,augs);
        }
    }

    @Override
    public void doctypeDecl(
            String rootElement,
            String publicId,
            String systemId,
            Augmentations augs)
            throws XNIException{
        if(isRootDocument()&&fDocumentHandler!=null){
            fDocumentHandler.doctypeDecl(rootElement,publicId,systemId,augs);
        }
    }

    @Override
    public void comment(XMLString text,Augmentations augs)
            throws XNIException{
        if(!fInDTD){
            if(fDocumentHandler!=null
                    &&getState()==STATE_NORMAL_PROCESSING){
                fDepth++;
                augs=modifyAugmentations(augs);
                fDocumentHandler.comment(text,augs);
                fDepth--;
            }
        }else if(fDTDHandler!=null){
            fDTDHandler.comment(text,augs);
        }
    }

    @Override
    public void processingInstruction(
            String target,
            XMLString data,
            Augmentations augs)
            throws XNIException{
        if(!fInDTD){
            if(fDocumentHandler!=null
                    &&getState()==STATE_NORMAL_PROCESSING){
                // we need to change the depth like this so that modifyAugmentations() works
                fDepth++;
                augs=modifyAugmentations(augs);
                fDocumentHandler.processingInstruction(target,data,augs);
                fDepth--;
            }
        }else if(fDTDHandler!=null){
            fDTDHandler.processingInstruction(target,data,augs);
        }
    }

    @Override
    public void startElement(
            QName element,
            XMLAttributes attributes,
            Augmentations augs)
            throws XNIException{
        fDepth++;
        int lastState=getState(fDepth-1);
        // If the last two states were fallback then this must be a descendant of an include
        // child which isn't a fallback. The specification says we should ignore such elements
        // and their children.
        if(lastState==STATE_EXPECT_FALLBACK&&getState(fDepth-2)==STATE_EXPECT_FALLBACK){
            setState(STATE_IGNORE);
        }else{
            setState(lastState);
        }
        // we process the xml:base and xml:lang attributes regardless
        // of what type of element it is.
        processXMLBaseAttributes(attributes);
        if(fFixupLanguage){
            processXMLLangAttributes(attributes);
        }
        if(isIncludeElement(element)){
            boolean success=this.handleIncludeElement(attributes);
            if(success){
                setState(STATE_IGNORE);
            }else{
                setState(STATE_EXPECT_FALLBACK);
            }
        }else if(isFallbackElement(element)){
            this.handleFallbackElement();
        }else if(hasXIncludeNamespace(element)){
            if(getSawInclude(fDepth-1)){
                reportFatalError(
                        "IncludeChild",
                        new Object[]{element.rawname});
            }
            if(getSawFallback(fDepth-1)){
                reportFatalError(
                        "FallbackChild",
                        new Object[]{element.rawname});
            }
            if(getState()==STATE_NORMAL_PROCESSING){
                if(fResultDepth++==0){
                    checkMultipleRootElements();
                }
                if(fDocumentHandler!=null){
                    augs=modifyAugmentations(augs);
                    attributes=processAttributes(attributes);
                    fDocumentHandler.startElement(element,attributes,augs);
                }
            }
        }else if(getState()==STATE_NORMAL_PROCESSING){
            if(fResultDepth++==0){
                checkMultipleRootElements();
            }
            if(fDocumentHandler!=null){
                augs=modifyAugmentations(augs);
                attributes=processAttributes(attributes);
                fDocumentHandler.startElement(element,attributes,augs);
            }
        }
    }

    @Override
    public void emptyElement(
            QName element,
            XMLAttributes attributes,
            Augmentations augs)
            throws XNIException{
        fDepth++;
        int lastState=getState(fDepth-1);
        // If the last two states were fallback then this must be a descendant of an include
        // child which isn't a fallback. The specification says we should ignore such elements
        // and their children.
        if(lastState==STATE_EXPECT_FALLBACK&&getState(fDepth-2)==STATE_EXPECT_FALLBACK){
            setState(STATE_IGNORE);
        }else{
            setState(lastState);
        }
        // we process the xml:base and xml:lang attributes regardless
        // of what type of element it is.
        processXMLBaseAttributes(attributes);
        if(fFixupLanguage){
            processXMLLangAttributes(attributes);
        }
        if(isIncludeElement(element)){
            boolean success=this.handleIncludeElement(attributes);
            if(success){
                setState(STATE_IGNORE);
            }else{
                reportFatalError("NoFallback",
                        new Object[]{attributes.getValue(null,"href")});
            }
        }else if(isFallbackElement(element)){
            this.handleFallbackElement();
        }else if(hasXIncludeNamespace(element)){
            if(getSawInclude(fDepth-1)){
                reportFatalError(
                        "IncludeChild",
                        new Object[]{element.rawname});
            }
            if(getSawFallback(fDepth-1)){
                reportFatalError(
                        "FallbackChild",
                        new Object[]{element.rawname});
            }
            if(getState()==STATE_NORMAL_PROCESSING){
                if(fResultDepth==0){
                    checkMultipleRootElements();
                }
                if(fDocumentHandler!=null){
                    augs=modifyAugmentations(augs);
                    attributes=processAttributes(attributes);
                    fDocumentHandler.emptyElement(element,attributes,augs);
                }
            }
        }else if(getState()==STATE_NORMAL_PROCESSING){
            if(fResultDepth==0){
                checkMultipleRootElements();
            }
            if(fDocumentHandler!=null){
                augs=modifyAugmentations(augs);
                attributes=processAttributes(attributes);
                fDocumentHandler.emptyElement(element,attributes,augs);
            }
        }
        // reset the out of scope stack elements
        setSawFallback(fDepth+1,false);
        setSawInclude(fDepth,false);
        // check if an xml:base has gone out of scope
        if(fBaseURIScope.size()>0&&fDepth==fBaseURIScope.peek()){
            // pop the values from the stack
            restoreBaseURI();
        }
        fDepth--;
    }

    @Override
    public void startGeneralEntity(
            String name,
            XMLResourceIdentifier resId,
            String encoding,
            Augmentations augs)
            throws XNIException{
        if(getState()==STATE_NORMAL_PROCESSING){
            if(fResultDepth==0){
                if(augs!=null&&Boolean.TRUE.equals(augs.getItem(Constants.ENTITY_SKIPPED))){
                    reportFatalError("UnexpandedEntityReferenceIllegal");
                }
            }else if(fDocumentHandler!=null){
                fDocumentHandler.startGeneralEntity(name,resId,encoding,augs);
            }
        }
    }

    @Override
    public void textDecl(String version,String encoding,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null
                &&getState()==STATE_NORMAL_PROCESSING){
            fDocumentHandler.textDecl(version,encoding,augs);
        }
    }

    @Override
    public void endGeneralEntity(String name,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null
                &&getState()==STATE_NORMAL_PROCESSING
                &&fResultDepth!=0){
            fDocumentHandler.endGeneralEntity(name,augs);
        }
    }    @Override
    public void setDocumentSource(XMLDocumentSource source){
        fDocumentSource=source;
    }

    @Override
    public void characters(XMLString text,Augmentations augs)
            throws XNIException{
        if(getState()==STATE_NORMAL_PROCESSING){
            if(fResultDepth==0){
                checkWhitespace(text);
            }else if(fDocumentHandler!=null){
                // we need to change the depth like this so that modifyAugmentations() works
                fDepth++;
                augs=modifyAugmentations(augs);
                fDocumentHandler.characters(text,augs);
                fDepth--;
            }
        }
    }    @Override
    public XMLDocumentSource getDocumentSource(){
        return fDocumentSource;
    }
    // DTDHandler methods
    // We are only interested in the notation and unparsed entity declarations,
    // the rest we just pass on

    @Override
    public void ignorableWhitespace(XMLString text,Augmentations augs)
            throws XNIException{
        if(fDocumentHandler!=null
                &&getState()==STATE_NORMAL_PROCESSING
                &&fResultDepth!=0){
            fDocumentHandler.ignorableWhitespace(text,augs);
        }
    }

    @Override
    public void endElement(QName element,Augmentations augs)
            throws XNIException{
        if(isIncludeElement(element)){
            // if we're ending an include element, and we were expecting a fallback
            // we check to see if the children of this include element contained a fallback
            if(getState()==STATE_EXPECT_FALLBACK
                    &&!getSawFallback(fDepth+1)){
                reportFatalError("NoFallback",
                        new Object[]{"unknown"});
            }
        }
        if(isFallbackElement(element)){
            // the state would have been set to normal processing if we were expecting the fallback element
            // now that we're done processing it, we should ignore all the other children of the include element
            if(getState()==STATE_NORMAL_PROCESSING){
                setState(STATE_IGNORE);
            }
        }else if(getState()==STATE_NORMAL_PROCESSING){
            --fResultDepth;
            if(fDocumentHandler!=null){
                fDocumentHandler.endElement(element,augs);
            }
        }
        // reset the out of scope stack elements
        setSawFallback(fDepth+1,false);
        setSawInclude(fDepth,false);
        // check if an xml:base has gone out of scope
        if(fBaseURIScope.size()>0&&fDepth==fBaseURIScope.peek()){
            // pop the values from the stack
            restoreBaseURI();
        }
        // check if an xml:lang has gone out of scope
        if(fLanguageScope.size()>0&&fDepth==fLanguageScope.peek()){
            // pop the language from the stack
            fCurrentLanguage=restoreLanguage();
        }
        fDepth--;
    }

    @Override
    public void startCDATA(Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null
                &&getState()==STATE_NORMAL_PROCESSING
                &&fResultDepth!=0){
            fDocumentHandler.startCDATA(augs);
        }
    }

    @Override
    public void endCDATA(Augmentations augs) throws XNIException{
        if(fDocumentHandler!=null
                &&getState()==STATE_NORMAL_PROCESSING
                &&fResultDepth!=0){
            fDocumentHandler.endCDATA(augs);
        }
    }

    @Override
    public void endDocument(Augmentations augs) throws XNIException{
        if(isRootDocument()){
            if(!fSeenRootElement){
                reportFatalError("RootElementRequired");
            }
            if(fDocumentHandler!=null){
                fDocumentHandler.endDocument(augs);
            }
        }
    }

    protected boolean isIncludeElement(QName element){
        return element.localpart.equals(XINCLUDE_INCLUDE)&&
                hasXIncludeNamespace(element);
    }

    protected boolean hasXIncludeNamespace(QName element){
        // REVISIT: The namespace of this element should be bound
        // already. Why are we looking it up from the namespace
        // context? -- mrglavas
        return element.uri==XINCLUDE_NS_URI
                ||fNamespaceContext.getURI(element.prefix)==XINCLUDE_NS_URI;
    }

    protected boolean isFallbackElement(QName element){
        return element.localpart.equals(XINCLUDE_FALLBACK)&&
                hasXIncludeNamespace(element);
    }

    protected void setSawFallback(int depth,boolean val){
        if(depth>=fSawFallback.length){
            boolean[] newarray=new boolean[depth*2];
            System.arraycopy(fSawFallback,0,newarray,0,fSawFallback.length);
            fSawFallback=newarray;
        }
        fSawFallback[depth]=val;
    }    @Override
    public XMLDTDSource getDTDSource(){
        return fDTDSource;
    }

    protected void setSawInclude(int depth,boolean val){
        if(depth>=fSawInclude.length){
            boolean[] newarray=new boolean[depth*2];
            System.arraycopy(fSawInclude,0,newarray,0,fSawInclude.length);
            fSawInclude=newarray;
        }
        fSawInclude[depth]=val;
    }

    protected void restoreBaseURI(){
        fBaseURI.pop();
        fLiteralSystemID.pop();
        fExpandedSystemID.pop();
        fBaseURIScope.pop();
        fCurrentBaseURI.setBaseSystemId((String)fBaseURI.peek());
        fCurrentBaseURI.setLiteralSystemId((String)fLiteralSystemID.peek());
        fCurrentBaseURI.setExpandedSystemId((String)fExpandedSystemID.peek());
    }

    public String restoreLanguage(){
        fLanguageStack.pop();
        fLanguageScope.pop();
        return (String)fLanguageStack.peek();
    }

    private void checkWhitespace(XMLString value){
        int end=value.offset+value.length;
        for(int i=value.offset;i<end;++i){
            if(!XMLChar.isSpace(value.ch[i])){
                reportFatalError("ContentIllegalAtTopLevel");
                return;
            }
        }
    }    @Override
    public void setDTDSource(XMLDTDSource source){
        fDTDSource=source;
    }

    protected Augmentations modifyAugmentations(Augmentations augs){
        return modifyAugmentations(augs,false);
    }

    protected Augmentations modifyAugmentations(
            Augmentations augs,
            boolean force){
        if(force||isTopLevelIncludedItem()){
            if(augs==null){
                augs=new AugmentationsImpl();
            }
            augs.putItem(XINCLUDE_INCLUDED,Boolean.TRUE);
        }
        return augs;
    }

    protected boolean isTopLevelIncludedItem(){
        return isTopLevelIncludedItemViaInclude()
                ||isTopLevelIncludedItemViaFallback();
    }

    protected boolean isTopLevelIncludedItemViaInclude(){
        return fDepth==1&&!isRootDocument();
    }

    protected boolean isTopLevelIncludedItemViaFallback(){
        // Technically, this doesn't check if the parent was a fallback, it also
        // would return true if any of the parent's sibling elements were fallbacks.
        // However, this doesn't matter, since we will always be ignoring elements
        // whose parent's siblings were fallbacks.
        return getSawFallback(fDepth-1);
    }

    protected boolean getSawFallback(int depth){
        if(depth>=fSawFallback.length){
            return false;
        }
        return fSawFallback[depth];
    }

    protected int getState(){
        return fState[fDepth];
    }    @Override
    public XMLDTDHandler getDTDHandler(){
        return fDTDHandler;
    }

    protected void setState(int state){
        if(fDepth>=fState.length){
            int[] newarray=new int[fDepth*2];
            System.arraycopy(fState,0,newarray,0,fState.length);
            fState=newarray;
        }
        fState[fDepth]=state;
    }    @Override
    public void setDTDHandler(XMLDTDHandler handler){
        fDTDHandler=handler;
    }
    // XIncludeHandler methods

    protected void reportFatalError(String key){
        this.reportFatalError(key,null);
    }

    protected void reportFatalError(String key,Object[] args){
        this.reportError(key,args,XMLErrorReporter.SEVERITY_FATAL_ERROR);
    }

    private void reportError(String key,Object[] args,short severity){
        if(fErrorReporter!=null){
            fErrorReporter.reportError(
                    XIncludeMessageFormatter.XINCLUDE_DOMAIN,
                    key,
                    args,
                    severity);
        }
        // we won't worry about when error reporter is null, since there should always be
        // at least the default error reporter
    }

    protected void saveBaseURI(){
        fBaseURIScope.push(fDepth);
        fBaseURI.push(fCurrentBaseURI.getBaseSystemId());
        fLiteralSystemID.push(fCurrentBaseURI.getLiteralSystemId());
        fExpandedSystemID.push(fCurrentBaseURI.getExpandedSystemId());
    }

    protected void saveLanguage(String language){
        fLanguageScope.push(fDepth);
        fLanguageStack.push(language);
    }

    @Override
    public void startDTD(XMLLocator locator,Augmentations augmentations)
            throws XNIException{
        fInDTD=true;
        if(fDTDHandler!=null){
            fDTDHandler.startDTD(locator,augmentations);
        }
    }

    @Override
    public void startParameterEntity(
            String name,
            XMLResourceIdentifier identifier,
            String encoding,
            Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.startParameterEntity(
                    name,
                    identifier,
                    encoding,
                    augmentations);
        }
    }

    @Override
    public void endParameterEntity(String name,Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.endParameterEntity(name,augmentations);
        }
    }

    @Override
    public void startExternalSubset(
            XMLResourceIdentifier identifier,
            Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.startExternalSubset(identifier,augmentations);
        }
    }

    @Override
    public void endExternalSubset(Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.endExternalSubset(augmentations);
        }
    }

    @Override
    public void elementDecl(
            String name,
            String contentModel,
            Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.elementDecl(name,contentModel,augmentations);
        }
    }

    @Override
    public void startAttlist(String elementName,Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.startAttlist(elementName,augmentations);
        }
    }

    @Override
    public void attributeDecl(
            String elementName,
            String attributeName,
            String type,
            String[] enumeration,
            String defaultType,
            XMLString defaultValue,
            XMLString nonNormalizedDefaultValue,
            Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.attributeDecl(
                    elementName,
                    attributeName,
                    type,
                    enumeration,
                    defaultType,
                    defaultValue,
                    nonNormalizedDefaultValue,
                    augmentations);
        }
    }

    @Override
    public void endAttlist(Augmentations augmentations) throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.endAttlist(augmentations);
        }
    }

    @Override
    public void internalEntityDecl(
            String name,
            XMLString text,
            XMLString nonNormalizedText,
            Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.internalEntityDecl(
                    name,
                    text,
                    nonNormalizedText,
                    augmentations);
        }
    }

    @Override
    public void externalEntityDecl(
            String name,
            XMLResourceIdentifier identifier,
            Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.externalEntityDecl(name,identifier,augmentations);
        }
    }

    @Override
    public void unparsedEntityDecl(
            String name,
            XMLResourceIdentifier identifier,
            String notation,
            Augmentations augmentations)
            throws XNIException{
        this.addUnparsedEntity(name,identifier,notation,augmentations);
        if(fDTDHandler!=null){
            fDTDHandler.unparsedEntityDecl(
                    name,
                    identifier,
                    notation,
                    augmentations);
        }
    }

    @Override
    public void notationDecl(
            String name,
            XMLResourceIdentifier identifier,
            Augmentations augmentations)
            throws XNIException{
        this.addNotation(name,identifier,augmentations);
        if(fDTDHandler!=null){
            fDTDHandler.notationDecl(name,identifier,augmentations);
        }
    }

    @Override
    public void startConditional(short type,Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.startConditional(type,augmentations);
        }
    }

    @Override
    public void ignoredCharacters(XMLString text,Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.ignoredCharacters(text,augmentations);
        }
    }

    @Override
    public void endConditional(Augmentations augmentations)
            throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.endConditional(augmentations);
        }
    }

    @Override
    public void endDTD(Augmentations augmentations) throws XNIException{
        if(fDTDHandler!=null){
            fDTDHandler.endDTD(augmentations);
        }
        fInDTD=false;
    }

    protected void addNotation(
            String name,
            XMLResourceIdentifier identifier,
            Augmentations augmentations){
        Notation not=new Notation();
        not.name=name;
        not.systemId=identifier.getLiteralSystemId();
        not.publicId=identifier.getPublicId();
        not.baseURI=identifier.getBaseSystemId();
        not.expandedSystemId=identifier.getExpandedSystemId();
        not.augmentations=augmentations;
        fNotations.add(not);
    }

    protected void addUnparsedEntity(
            String name,
            XMLResourceIdentifier identifier,
            String notation,
            Augmentations augmentations){
        UnparsedEntity ent=new UnparsedEntity();
        ent.name=name;
        ent.systemId=identifier.getLiteralSystemId();
        ent.publicId=identifier.getPublicId();
        ent.baseURI=identifier.getBaseSystemId();
        ent.expandedSystemId=identifier.getExpandedSystemId();
        ent.notation=notation;
        ent.augmentations=augmentations;
        fUnparsedEntities.add(ent);
    }

    protected void handleFallbackElement(){
        if(!getSawInclude(fDepth-1)){
            if(getState()==STATE_IGNORE){
                return;
            }
            reportFatalError("FallbackParent");
        }
        setSawInclude(fDepth,false);
        fNamespaceContext.setContextInvalid();
        if(getSawFallback(fDepth)){
            reportFatalError("MultipleFallbacks");
        }else{
            setSawFallback(fDepth,true);
        }
        // Either the state is STATE_EXPECT_FALLBACK or it's STATE_IGNORE.
        // If we're ignoring, we want to stay ignoring. But if we're expecting this fallback element,
        // we want to signal that we should process the children.
        if(getState()==STATE_EXPECT_FALLBACK){
            setState(STATE_NORMAL_PROCESSING);
        }
    }

    protected boolean handleIncludeElement(XMLAttributes attributes)
            throws XNIException{
        if(getSawInclude(fDepth-1)){
            reportFatalError("IncludeChild",new Object[]{XINCLUDE_INCLUDE});
        }
        if(getState()==STATE_IGNORE){
            return true;
        }
        setSawInclude(fDepth,true);
        fNamespaceContext.setContextInvalid();
        // TODO: does Java use IURIs by default?
        //       [Definition: An internationalized URI reference, or IURI, is a URI reference that directly uses [Unicode] characters.]
        // TODO: figure out what section 4.1.1 of the XInclude spec is talking about
        //       has to do with disallowed ASCII character escaping
        //       this ties in with the above IURI section, but I suspect Java already does it
        String href=attributes.getValue(XINCLUDE_ATTR_HREF);
        String parse=attributes.getValue(XINCLUDE_ATTR_PARSE);
        String xpointer=attributes.getValue(XPOINTER);
        String accept=attributes.getValue(XINCLUDE_ATTR_ACCEPT);
        String acceptLanguage=attributes.getValue(XINCLUDE_ATTR_ACCEPT_LANGUAGE);
        if(parse==null){
            parse=XINCLUDE_PARSE_XML;
        }
        if(href==null){
            href=XMLSymbols.EMPTY_STRING;
        }
        if(href.length()==0&&XINCLUDE_PARSE_XML.equals(parse)){
            if(xpointer==null){
                reportFatalError("XpointerMissing");
            }else{
                // When parse="xml" and an xpointer is specified treat
                // all absences of the href attribute as a resource error.
                Locale locale=(fErrorReporter!=null)?fErrorReporter.getLocale():null;
                String reason=fXIncludeMessageFormatter.formatMessage(locale,"XPointerStreamability",null);
                reportResourceError("XMLResourceError",new Object[]{href,reason});
                return false;
            }
        }
        URI hrefURI=null;
        // Check whether href is correct and perform escaping as per section 4.1.1 of the XInclude spec.
        // Report fatal error if the href value contains a fragment identifier or if the value after
        // escaping is a syntactically invalid URI or IRI.
        try{
            hrefURI=new URI(href,true);
            if(hrefURI.getFragment()!=null){
                reportFatalError("HrefFragmentIdentifierIllegal",new Object[]{href});
            }
        }catch(MalformedURIException exc){
            String newHref=escapeHref(href);
            if(href!=newHref){
                href=newHref;
                try{
                    hrefURI=new URI(href,true);
                    if(hrefURI.getFragment()!=null){
                        reportFatalError("HrefFragmentIdentifierIllegal",new Object[]{href});
                    }
                }catch(MalformedURIException exc2){
                    reportFatalError("HrefSyntacticallyInvalid",new Object[]{href});
                }
            }else{
                reportFatalError("HrefSyntacticallyInvalid",new Object[]{href});
            }
        }
        // Verify that if an accept and/or an accept-language attribute exist
        // that the value(s) don't contain disallowed characters.
        if(accept!=null&&!isValidInHTTPHeader(accept)){
            reportFatalError("AcceptMalformed",null);
            accept=null;
        }
        if(acceptLanguage!=null&&!isValidInHTTPHeader(acceptLanguage)){
            reportFatalError("AcceptLanguageMalformed",null);
            acceptLanguage=null;
        }
        XMLInputSource includedSource=null;
        if(fEntityResolver!=null){
            try{
                XMLResourceIdentifier resourceIdentifier=
                        new XMLResourceIdentifierImpl(
                                null,
                                href,
                                fCurrentBaseURI.getExpandedSystemId(),
                                XMLEntityManager.expandSystemId(
                                        href,
                                        fCurrentBaseURI.getExpandedSystemId(),
                                        false));
                includedSource=
                        fEntityResolver.resolveEntity(resourceIdentifier);
                if(includedSource!=null&&
                        !(includedSource instanceof HTTPInputSource)&&
                        (accept!=null||acceptLanguage!=null)&&
                        includedSource.getCharacterStream()==null&&
                        includedSource.getByteStream()==null){
                    includedSource=createInputSource(includedSource.getPublicId(),includedSource.getSystemId(),
                            includedSource.getBaseSystemId(),accept,acceptLanguage);
                }
            }catch(IOException e){
                reportResourceError(
                        "XMLResourceError",
                        new Object[]{href,e.getMessage()});
                return false;
            }
        }
        if(includedSource==null){
            // setup an HTTPInputSource if either of the content negotation attributes were specified.
            if(accept!=null||acceptLanguage!=null){
                includedSource=createInputSource(null,href,fCurrentBaseURI.getExpandedSystemId(),accept,acceptLanguage);
            }else{
                includedSource=new XMLInputSource(null,href,fCurrentBaseURI.getExpandedSystemId());
            }
        }
        if(parse.equals(XINCLUDE_PARSE_XML)){
            // Instead of always creating a new configuration, the first one can be reused
            if((xpointer!=null&&fXPointerChildConfig==null)
                    ||(xpointer==null&&fXIncludeChildConfig==null)){
                String parserName=XINCLUDE_DEFAULT_CONFIGURATION;
                if(xpointer!=null)
                    parserName="com.sun.org.apache.xerces.internal.parsers.XPointerParserConfiguration";
                fChildConfig=
                        (XMLParserConfiguration)ObjectFactory.newInstance(
                                parserName,
                                true);
                // use the same symbol table, error reporter, entity resolver, security manager and buffer size.
                if(fSymbolTable!=null) fChildConfig.setProperty(SYMBOL_TABLE,fSymbolTable);
                if(fErrorReporter!=null) fChildConfig.setProperty(ERROR_REPORTER,fErrorReporter);
                if(fEntityResolver!=null) fChildConfig.setProperty(ENTITY_RESOLVER,fEntityResolver);
                fChildConfig.setProperty(SECURITY_MANAGER,fSecurityManager);
                fChildConfig.setProperty(XML_SECURITY_PROPERTY_MANAGER,fSecurityPropertyMgr);
                fChildConfig.setProperty(BUFFER_SIZE,new Integer(fBufferSize));
                // features must be copied to child configuration
                fNeedCopyFeatures=true;
                // use the same namespace context
                fChildConfig.setProperty(
                        Constants.XERCES_PROPERTY_PREFIX
                                +Constants.NAMESPACE_CONTEXT_PROPERTY,
                        fNamespaceContext);
                fChildConfig.setFeature(
                        XINCLUDE_FIXUP_BASE_URIS,
                        fFixupBaseURIs);
                fChildConfig.setFeature(
                        XINCLUDE_FIXUP_LANGUAGE,
                        fFixupLanguage);
                // If the xpointer attribute is present
                if(xpointer!=null){
                    XPointerHandler newHandler=
                            (XPointerHandler)fChildConfig.getProperty(
                                    Constants.XERCES_PROPERTY_PREFIX
                                            +Constants.XPOINTER_HANDLER_PROPERTY);
                    fXPtrProcessor=newHandler;
                    // ???
                    ((XPointerHandler)fXPtrProcessor).setProperty(
                            Constants.XERCES_PROPERTY_PREFIX
                                    +Constants.NAMESPACE_CONTEXT_PROPERTY,
                            fNamespaceContext);
                    ((XPointerHandler)fXPtrProcessor).setProperty(XINCLUDE_FIXUP_BASE_URIS,
                            fFixupBaseURIs);
                    ((XPointerHandler)fXPtrProcessor).setProperty(
                            XINCLUDE_FIXUP_LANGUAGE,fFixupLanguage);
                    if(fErrorReporter!=null)
                        ((XPointerHandler)fXPtrProcessor).setProperty(ERROR_REPORTER,fErrorReporter);
                    // ???
                    newHandler.setParent(this);
                    newHandler.setDocumentHandler(this.getDocumentHandler());
                    fXPointerChildConfig=fChildConfig;
                }else{
                    XIncludeHandler newHandler=
                            (XIncludeHandler)fChildConfig.getProperty(
                                    Constants.XERCES_PROPERTY_PREFIX
                                            +Constants.XINCLUDE_HANDLER_PROPERTY);
                    newHandler.setParent(this);
                    newHandler.setDocumentHandler(this.getDocumentHandler());
                    fXIncludeChildConfig=fChildConfig;
                }
            }
            // If an xpointer attribute is present
            if(xpointer!=null){
                fChildConfig=fXPointerChildConfig;
                // Parse the XPointer expression
                try{
                    ((XPointerProcessor)fXPtrProcessor).parseXPointer(xpointer);
                }catch(XNIException ex){
                    // report the XPointer error as a resource error
                    reportResourceError(
                            "XMLResourceError",
                            new Object[]{href,ex.getMessage()});
                    return false;
                }
            }else{
                fChildConfig=fXIncludeChildConfig;
            }
            // set all features on parserConfig to match this parser configuration
            if(fNeedCopyFeatures){
                copyFeatures(fSettings,fChildConfig);
            }
            fNeedCopyFeatures=false;
            try{
                fNamespaceContext.pushScope();
                fChildConfig.parse(includedSource);
                // necessary to make sure proper location is reported in errors
                if(fErrorReporter!=null){
                    fErrorReporter.setDocumentLocator(fDocLocation);
                }
                // If the xpointer attribute is present
                if(xpointer!=null){
                    // and it was not resolved
                    if(!((XPointerProcessor)fXPtrProcessor).isXPointerResolved()){
                        Locale locale=(fErrorReporter!=null)?fErrorReporter.getLocale():null;
                        String reason=fXIncludeMessageFormatter.formatMessage(locale,"XPointerResolutionUnsuccessful",null);
                        reportResourceError("XMLResourceError",new Object[]{href,reason});
                        // use the fallback
                        return false;
                    }
                }
            }catch(XNIException e){
                // necessary to make sure proper location is reported in errors
                if(fErrorReporter!=null){
                    fErrorReporter.setDocumentLocator(fDocLocation);
                }
                reportFatalError("XMLParseError",new Object[]{href,e.getMessage()});
            }catch(IOException e){
                // necessary to make sure proper location is reported in errors
                if(fErrorReporter!=null){
                    fErrorReporter.setDocumentLocator(fDocLocation);
                }
                // An IOException indicates that we had trouble reading the file, not
                // that it was an invalid XML file.  So we send a resource error, not a
                // fatal error.
                reportResourceError(
                        "XMLResourceError",
                        new Object[]{href,e.getMessage()});
                return false;
            }finally{
                fNamespaceContext.popScope();
            }
        }else if(parse.equals(XINCLUDE_PARSE_TEXT)){
            // we only care about encoding for parse="text"
            String encoding=attributes.getValue(XINCLUDE_ATTR_ENCODING);
            includedSource.setEncoding(encoding);
            XIncludeTextReader textReader=null;
            try{
                // Setup the appropriate text reader.
                if(!fIsXML11){
                    if(fXInclude10TextReader==null){
                        fXInclude10TextReader=new XIncludeTextReader(includedSource,this,fBufferSize);
                    }else{
                        fXInclude10TextReader.setInputSource(includedSource);
                    }
                    textReader=fXInclude10TextReader;
                }else{
                    if(fXInclude11TextReader==null){
                        fXInclude11TextReader=new XInclude11TextReader(includedSource,this,fBufferSize);
                    }else{
                        fXInclude11TextReader.setInputSource(includedSource);
                    }
                    textReader=fXInclude11TextReader;
                }
                textReader.setErrorReporter(fErrorReporter);
                textReader.parse();
            }
            // encoding errors
            catch(MalformedByteSequenceException ex){
                fErrorReporter.reportError(ex.getDomain(),ex.getKey(),
                        ex.getArguments(),XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }catch(CharConversionException e){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",null,XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }catch(IOException e){
                reportResourceError(
                        "TextResourceError",
                        new Object[]{href,e.getMessage()});
                return false;
            }finally{
                if(textReader!=null){
                    try{
                        textReader.close();
                    }catch(IOException e){
                        reportResourceError(
                                "TextResourceError",
                                new Object[]{href,e.getMessage()});
                        return false;
                    }
                }
            }
        }else{
            reportFatalError("InvalidParseValue",new Object[]{parse});
        }
        return true;
    }

    protected boolean sameBaseURIAsIncludeParent(){
        String parentBaseURI=getIncludeParentBaseURI();
        String baseURI=fCurrentBaseURI.getExpandedSystemId();
        // REVISIT: should we use File#sameFile() ?
        //          I think the benefit of using it is that it resolves host names
        //          instead of just doing a string comparison.
        // TODO: [base URI] is still an open issue with the working group.
        //       They're deciding if xml:base should be added if the [base URI] is different in terms
        //       of resolving relative references, or if it should be added if they are different at all.
        //       Revisit this after a final decision has been made.
        //       The decision also affects whether we output the file name of the URI, or just the path.
        return parentBaseURI!=null&&parentBaseURI.equals(baseURI);
    }

    protected boolean sameLanguageAsIncludeParent(){
        String parentLanguage=getIncludeParentLanguage();
        return parentLanguage!=null&&parentLanguage.equalsIgnoreCase(fCurrentLanguage);
    }

    protected boolean searchForRecursiveIncludes(XMLLocator includedSource){
        String includedSystemId=includedSource.getExpandedSystemId();
        if(includedSystemId==null){
            try{
                includedSystemId=
                        XMLEntityManager.expandSystemId(
                                includedSource.getLiteralSystemId(),
                                includedSource.getBaseSystemId(),
                                false);
            }catch(MalformedURIException e){
                reportFatalError("ExpandedSystemId");
            }
        }
        if(includedSystemId.equals(fCurrentBaseURI.getExpandedSystemId())){
            return true;
        }
        if(fParentXIncludeHandler==null){
            return false;
        }
        return fParentXIncludeHandler.searchForRecursiveIncludes(
                includedSource);
    }

    protected XMLAttributes processAttributes(XMLAttributes attributes){
        if(isTopLevelIncludedItem()){
            // Modify attributes to fix the base URI (spec 4.5.5).
            // We only do it to top level included elements, which have a different
            // base URI than their include parent.
            if(fFixupBaseURIs&&!sameBaseURIAsIncludeParent()){
                if(attributes==null){
                    attributes=new XMLAttributesImpl();
                }
                // This causes errors with schema validation, if the schema doesn't
                // specify that these elements can have an xml:base attribute
                String uri=null;
                try{
                    uri=this.getRelativeBaseURI();
                }catch(MalformedURIException e){
                    // this shouldn't ever happen, since by definition, we had to traverse
                    // the same URIs to even get to this place
                    uri=fCurrentBaseURI.getExpandedSystemId();
                }
                int index=
                        attributes.addAttribute(
                                XML_BASE_QNAME,
                                XMLSymbols.fCDATASymbol,
                                uri);
                attributes.setSpecified(index,true);
            }
            // Modify attributes to perform language-fixup (spec 4.5.6).
            // We only do it to top level included elements, which have a different
            // [language] than their include parent.
            if(fFixupLanguage&&!sameLanguageAsIncludeParent()){
                if(attributes==null){
                    attributes=new XMLAttributesImpl();
                }
                int index=
                        attributes.addAttribute(
                                XML_LANG_QNAME,
                                XMLSymbols.fCDATASymbol,
                                fCurrentLanguage);
                attributes.setSpecified(index,true);
            }
            // Modify attributes of included items to do namespace-fixup. (spec 4.5.4)
            Enumeration inscopeNS=fNamespaceContext.getAllPrefixes();
            while(inscopeNS.hasMoreElements()){
                String prefix=(String)inscopeNS.nextElement();
                String parentURI=
                        fNamespaceContext.getURIFromIncludeParent(prefix);
                String uri=fNamespaceContext.getURI(prefix);
                if(parentURI!=uri&&attributes!=null){
                    if(prefix==XMLSymbols.EMPTY_STRING){
                        if(attributes
                                .getValue(
                                        NamespaceContext.XMLNS_URI,
                                        XMLSymbols.PREFIX_XMLNS)
                                ==null){
                            if(attributes==null){
                                attributes=new XMLAttributesImpl();
                            }
                            QName ns=(QName)NEW_NS_ATTR_QNAME.clone();
                            ns.prefix=null;
                            ns.localpart=XMLSymbols.PREFIX_XMLNS;
                            ns.rawname=XMLSymbols.PREFIX_XMLNS;
                            int index=
                                    attributes.addAttribute(
                                            ns,
                                            XMLSymbols.fCDATASymbol,
                                            uri!=null?uri:XMLSymbols.EMPTY_STRING);
                            attributes.setSpecified(index,true);
                            // Need to re-declare this prefix in the current context
                            // in order for the SAX parser to report the appropriate
                            // start and end prefix mapping events. -- mrglavas
                            fNamespaceContext.declarePrefix(prefix,uri);
                        }
                    }else if(
                            attributes.getValue(NamespaceContext.XMLNS_URI,prefix)
                                    ==null){
                        if(attributes==null){
                            attributes=new XMLAttributesImpl();
                        }
                        QName ns=(QName)NEW_NS_ATTR_QNAME.clone();
                        ns.localpart=prefix;
                        ns.rawname+=prefix;
                        ns.rawname=(fSymbolTable!=null)?
                                fSymbolTable.addSymbol(ns.rawname):
                                ns.rawname.intern();
                        int index=
                                attributes.addAttribute(
                                        ns,
                                        XMLSymbols.fCDATASymbol,
                                        uri!=null?uri:XMLSymbols.EMPTY_STRING);
                        attributes.setSpecified(index,true);
                        // Need to re-declare this prefix in the current context
                        // in order for the SAX parser to report the appropriate
                        // start and end prefix mapping events. -- mrglavas
                        fNamespaceContext.declarePrefix(prefix,uri);
                    }
                }
            }
        }
        if(attributes!=null){
            int length=attributes.getLength();
            for(int i=0;i<length;i++){
                String type=attributes.getType(i);
                String value=attributes.getValue(i);
                if(type==XMLSymbols.fENTITYSymbol){
                    this.checkUnparsedEntity(value);
                }
                if(type==XMLSymbols.fENTITIESSymbol){
                    // 4.5.1 - Unparsed Entities
                    StringTokenizer st=new StringTokenizer(value);
                    while(st.hasMoreTokens()){
                        String entName=st.nextToken();
                        this.checkUnparsedEntity(entName);
                    }
                }else if(type==XMLSymbols.fNOTATIONSymbol){
                    // 4.5.2 - Notations
                    this.checkNotation(value);
                }
                /** We actually don't need to do anything for 4.5.3, because at this stage the
                 * value of the attribute is just a string. It will be taken care of later
                 * in the pipeline, when the IDREFs are actually resolved against IDs.
                 *
                 * if (type == XMLSymbols.fIDREFSymbol || type == XMLSymbols.fIDREFSSymbol) { }
                 */
            }
        }
        return attributes;
    }

    protected String getRelativeBaseURI() throws MalformedURIException{
        int includeParentDepth=getIncludeParentDepth();
        String relativeURI=this.getRelativeURI(includeParentDepth);
        if(isRootDocument()){
            return relativeURI;
        }else{
            if(relativeURI.equals("")){
                relativeURI=fCurrentBaseURI.getLiteralSystemId();
            }
            if(includeParentDepth==0){
                if(fParentRelativeURI==null){
                    fParentRelativeURI=
                            fParentXIncludeHandler.getRelativeBaseURI();
                }
                if(fParentRelativeURI.equals("")){
                    return relativeURI;
                }
                URI base=new URI(fParentRelativeURI,true);
                URI uri=new URI(base,relativeURI);
                /** Check whether the scheme components are equal. */
                final String baseScheme=base.getScheme();
                final String literalScheme=uri.getScheme();
                if(!Objects.equals(baseScheme,literalScheme)){
                    return relativeURI;
                }
                /** Check whether the authority components are equal. */
                final String baseAuthority=base.getAuthority();
                final String literalAuthority=uri.getAuthority();
                if(!Objects.equals(baseAuthority,literalAuthority)){
                    return uri.getSchemeSpecificPart();
                }
                /**
                 * The scheme and authority components are equal,
                 * return the path and the possible query and/or
                 * fragment which follow.
                 */
                final String literalPath=uri.getPath();
                final String literalQuery=uri.getQueryString();
                final String literalFragment=uri.getFragment();
                if(literalQuery!=null||literalFragment!=null){
                    final StringBuilder buffer=new StringBuilder();
                    if(literalPath!=null){
                        buffer.append(literalPath);
                    }
                    if(literalQuery!=null){
                        buffer.append('?');
                        buffer.append(literalQuery);
                    }
                    if(literalFragment!=null){
                        buffer.append('#');
                        buffer.append(literalFragment);
                    }
                    return buffer.toString();
                }
                return literalPath;
            }else{
                return relativeURI;
            }
        }
    }

    private String getIncludeParentBaseURI(){
        int depth=getIncludeParentDepth();
        if(!isRootDocument()&&depth==0){
            return fParentXIncludeHandler.getIncludeParentBaseURI();
        }else{
            return this.getBaseURI(depth);
        }
    }

    private String getIncludeParentLanguage(){
        int depth=getIncludeParentDepth();
        if(!isRootDocument()&&depth==0){
            return fParentXIncludeHandler.getIncludeParentLanguage();
        }else{
            return getLanguage(depth);
        }
    }

    private int getIncludeParentDepth(){
        // We don't start at fDepth, since it is either the top level included item,
        // or an include element, when this method is called.
        for(int i=fDepth-1;i>=0;i--){
            // This technically might not always return the first non-include/fallback
            // element that it comes to, since sawFallback() returns true if a fallback
            // was ever encountered at that depth.  However, if a fallback was encountered
            // at that depth, and it wasn't the direct descendant of the current element
            // then we can't be in a situation where we're calling this method (because
            // we'll always be in STATE_IGNORE)
            if(!getSawInclude(i)&&!getSawFallback(i)){
                return i;
            }
        }
        // shouldn't get here, since depth 0 should never have an include element or
        // a fallback element
        return 0;
    }

    private int getResultDepth(){
        return fResultDepth;
    }

    protected int getState(int depth){
        return fState[depth];
    }

    protected boolean getSawInclude(int depth){
        if(depth>=fSawInclude.length){
            return false;
        }
        return fSawInclude[depth];
    }

    protected void reportResourceError(String key){
        this.reportFatalError(key,null);
    }

    protected void reportResourceError(String key,Object[] args){
        this.reportError(key,args,XMLErrorReporter.SEVERITY_WARNING);
    }

    protected void setParent(XIncludeHandler parent){
        fParentXIncludeHandler=parent;
    }

    protected void checkUnparsedEntity(String entName){
        UnparsedEntity ent=new UnparsedEntity();
        ent.name=entName;
        int index=fUnparsedEntities.indexOf(ent);
        if(index!=-1){
            ent=(UnparsedEntity)fUnparsedEntities.get(index);
            // first check the notation of the unparsed entity
            checkNotation(ent.notation);
            checkAndSendUnparsedEntity(ent);
        }
    }

    protected void checkNotation(String notName){
        Notation not=new Notation();
        not.name=notName;
        int index=fNotations.indexOf(not);
        if(index!=-1){
            not=(Notation)fNotations.get(index);
            checkAndSendNotation(not);
        }
    }

    protected void checkAndSendUnparsedEntity(UnparsedEntity ent){
        if(isRootDocument()){
            int index=fUnparsedEntities.indexOf(ent);
            if(index==-1){
                // There is no unparsed entity with the same name that we have sent.
                // Calling unparsedEntityDecl() will add the entity to our local store,
                // and also send the unparsed entity to the DTDHandler
                XMLResourceIdentifier id=
                        new XMLResourceIdentifierImpl(
                                ent.publicId,
                                ent.systemId,
                                ent.baseURI,
                                ent.expandedSystemId);
                addUnparsedEntity(
                        ent.name,
                        id,
                        ent.notation,
                        ent.augmentations);
                if(fSendUEAndNotationEvents&&fDTDHandler!=null){
                    fDTDHandler.unparsedEntityDecl(
                            ent.name,
                            id,
                            ent.notation,
                            ent.augmentations);
                }
            }else{
                UnparsedEntity localEntity=
                        (UnparsedEntity)fUnparsedEntities.get(index);
                if(!ent.isDuplicate(localEntity)){
                    reportFatalError(
                            "NonDuplicateUnparsedEntity",
                            new Object[]{ent.name});
                }
            }
        }else{
            fParentXIncludeHandler.checkAndSendUnparsedEntity(ent);
        }
    }

    protected void checkAndSendNotation(Notation not){
        if(isRootDocument()){
            int index=fNotations.indexOf(not);
            if(index==-1){
                // There is no notation with the same name that we have sent.
                XMLResourceIdentifier id=
                        new XMLResourceIdentifierImpl(
                                not.publicId,
                                not.systemId,
                                not.baseURI,
                                not.expandedSystemId);
                addNotation(not.name,id,not.augmentations);
                if(fSendUEAndNotationEvents&&fDTDHandler!=null){
                    fDTDHandler.notationDecl(not.name,id,not.augmentations);
                }
            }else{
                Notation localNotation=(Notation)fNotations.get(index);
                if(!not.isDuplicate(localNotation)){
                    reportFatalError(
                            "NonDuplicateNotation",
                            new Object[]{not.name});
                }
            }
        }else{
            fParentXIncludeHandler.checkAndSendNotation(not);
        }
    }

    private void checkMultipleRootElements(){
        if(getRootElementProcessed()){
            reportFatalError("MultipleRootElements");
        }
        setRootElementProcessed(true);
    }

    private boolean getRootElementProcessed(){
        return isRootDocument()?fSeenRootElement:fParentXIncludeHandler.getRootElementProcessed();
    }

    private void setRootElementProcessed(boolean seenRoot){
        if(isRootDocument()){
            fSeenRootElement=seenRoot;
            return;
        }
        fParentXIncludeHandler.setRootElementProcessed(seenRoot);
    }

    protected void copyFeatures(
            XMLComponentManager from,
            XMLParserConfiguration to){
        Enumeration features=Constants.getXercesFeatures();
        copyFeatures1(features,Constants.XERCES_FEATURE_PREFIX,from,to);
        features=Constants.getSAXFeatures();
        copyFeatures1(features,Constants.SAX_FEATURE_PREFIX,from,to);
    }

    private void copyFeatures1(
            Enumeration features,
            String featurePrefix,
            XMLComponentManager from,
            XMLParserConfiguration to){
        while(features.hasMoreElements()){
            String featureId=featurePrefix+(String)features.nextElement();
            boolean value=from.getFeature(featureId);
            try{
                to.setFeature(featureId,value);
            }catch(XMLConfigurationException e){
                // componentManager doesn't support this feature,
                // so we won't worry about it
            }
        }
    }

    public String getBaseURI(int depth){
        int scope=scopeOfBaseURI(depth);
        return (String)fExpandedSystemID.elementAt(scope);
    }
    // The following methods are used for XML Base processing

    public String getLanguage(int depth){
        int scope=scopeOfLanguage(depth);
        return (String)fLanguageStack.elementAt(scope);
    }

    public String getRelativeURI(int depth) throws MalformedURIException{
        // The literal system id at the location given by "start" is *in focus* at
        // the given depth. So we need to adjust it to the next scope, so that we
        // only process out of focus literal system ids
        int start=scopeOfBaseURI(depth)+1;
        if(start==fBaseURIScope.size()){
            // If that is the last system id, then we don't need a relative URI
            return "";
        }
        URI uri=new URI("file",(String)fLiteralSystemID.elementAt(start));
        for(int i=start+1;i<fBaseURIScope.size();i++){
            uri=new URI(uri,(String)fLiteralSystemID.elementAt(i));
        }
        return uri.getPath();
    }
    // The following methods are used for language processing

    // We need to find two consecutive elements in the scope stack,
    // such that the first is lower than 'depth' (or equal), and the
    // second is higher.
    private int scopeOfBaseURI(int depth){
        for(int i=fBaseURIScope.size()-1;i>=0;i--){
            if(fBaseURIScope.elementAt(i)<=depth)
                return i;
        }
        // we should never get here, because 0 was put on the stack in startDocument()
        return -1;
    }

    private int scopeOfLanguage(int depth){
        for(int i=fLanguageScope.size()-1;i>=0;i--){
            if(fLanguageScope.elementAt(i)<=depth)
                return i;
        }
        // we should never get here, because 0 was put on the stack in startDocument()
        return -1;
    }

    protected void processXMLBaseAttributes(XMLAttributes attributes){
        String baseURIValue=
                attributes.getValue(NamespaceContext.XML_URI,"base");
        if(baseURIValue!=null){
            try{
                String expandedValue=
                        XMLEntityManager.expandSystemId(
                                baseURIValue,
                                fCurrentBaseURI.getExpandedSystemId(),
                                false);
                fCurrentBaseURI.setLiteralSystemId(baseURIValue);
                fCurrentBaseURI.setBaseSystemId(
                        fCurrentBaseURI.getExpandedSystemId());
                fCurrentBaseURI.setExpandedSystemId(expandedValue);
                // push the new values on the stack
                saveBaseURI();
            }catch(MalformedURIException e){
                // REVISIT: throw error here
            }
        }
    }

    protected void processXMLLangAttributes(XMLAttributes attributes){
        String language=attributes.getValue(NamespaceContext.XML_URI,"lang");
        if(language!=null){
            fCurrentLanguage=language;
            saveLanguage(fCurrentLanguage);
        }
    }

    private boolean isValidInHTTPHeader(String value){
        char ch;
        for(int i=value.length()-1;i>=0;--i){
            ch=value.charAt(i);
            if(ch<0x20||ch>0x7E){
                return false;
            }
        }
        return true;
    }

    private XMLInputSource createInputSource(String publicId,
                                             String systemId,String baseSystemId,
                                             String accept,String acceptLanguage){
        HTTPInputSource httpSource=new HTTPInputSource(publicId,systemId,baseSystemId);
        if(accept!=null&&accept.length()>0){
            httpSource.setHTTPRequestProperty(XIncludeHandler.HTTP_ACCEPT,accept);
        }
        if(acceptLanguage!=null&&acceptLanguage.length()>0){
            httpSource.setHTTPRequestProperty(XIncludeHandler.HTTP_ACCEPT_LANGUAGE,acceptLanguage);
        }
        return httpSource;
    }

    //
    // Escape an href value according to (4.1.1):
    //
    // To convert the value of the href attribute to an IRI reference, the following characters must be escaped:
    // space #x20
    // the delimiters < #x3C, > #x3E and " #x22
    // the unwise characters { #x7B, } #x7D, | #x7C, \ #x5C, ^ #x5E and ` #x60
    //
    // To convert an IRI reference to a URI reference, the following characters must also be escaped:
    // the Unicode plane 0 characters #xA0 - #xD7FF, #xF900-#xFDCF, #xFDF0-#xFFEF
    // the Unicode plane 1-14 characters #x10000-#x1FFFD ... #xE0000-#xEFFFD
    //
    private String escapeHref(String href){
        int len=href.length();
        int ch;
        final StringBuilder buffer=new StringBuilder(len*3);
        // for each character in the href
        int i=0;
        for(;i<len;i++){
            ch=href.charAt(i);
            // if it's not an ASCII character (excluding 0x7F), break here, and use UTF-8 encoding
            if(ch>0x7E){
                break;
            }
            // abort: href does not allow this character
            if(ch<0x20){
                return href;
            }
            if(gNeedEscaping[ch]){
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
            }else{
                buffer.append((char)ch);
            }
        }
        // we saw some non-ascii character
        if(i<len){
            // check if remainder of href contains any illegal characters before proceeding
            for(int j=i;j<len;++j){
                ch=href.charAt(j);
                if((ch>=0x20&&ch<=0x7E)||
                        (ch>=0xA0&&ch<=0xD7FF)||
                        (ch>=0xF900&&ch<=0xFDCF)||
                        (ch>=0xFDF0&&ch<=0xFFEF)){
                    continue;
                }
                if(XMLChar.isHighSurrogate(ch)&&++j<len){
                    int ch2=href.charAt(j);
                    if(XMLChar.isLowSurrogate(ch2)){
                        ch2=XMLChar.supplemental((char)ch,(char)ch2);
                        if(ch2<0xF0000&&(ch2&0xFFFF)<=0xFFFD){
                            continue;
                        }
                    }
                }
                // abort: href does not allow this character
                return href;
            }
            // get UTF-8 bytes for the remaining sub-string
            byte[] bytes=null;
            byte b;
            try{
                bytes=href.substring(i).getBytes("UTF-8");
            }catch(java.io.UnsupportedEncodingException e){
                // should never happen
                return href;
            }
            len=bytes.length;
            // for each byte
            for(i=0;i<len;i++){
                b=bytes[i];
                // for non-ascii character: make it positive, then escape
                if(b<0){
                    ch=b+256;
                    buffer.append('%');
                    buffer.append(gHexChs[ch>>4]);
                    buffer.append(gHexChs[ch&0xf]);
                }else if(gNeedEscaping[b]){
                    buffer.append('%');
                    buffer.append(gAfterEscaping1[b]);
                    buffer.append(gAfterEscaping2[b]);
                }else{
                    buffer.append((char)b);
                }
            }
        }
        // If escaping happened, create a new string;
        // otherwise, return the orginal one.
        if(buffer.length()!=len){
            return buffer.toString();
        }else{
            return href;
        }
    }

    // This is a storage class to hold information about the notations.
    // We're not using XMLNotationDecl because we don't want to lose the augmentations.
    protected static class Notation{
        public String name;
        public String systemId;
        public String baseURI;
        public String publicId;
        public String expandedSystemId;
        public Augmentations augmentations;

        @Override
        public int hashCode(){
            return Objects.hashCode(name);
        }        // equals() returns true if two Notations have the same name.
        // Useful for searching Vectors for notations with the same name
        @Override
        public boolean equals(Object obj){
            return obj==this||obj instanceof Notation
                    &&Objects.equals(name,((Notation)obj).name);
        }

        // from 4.5.2
        // Notation items with the same [name], [system identifier],
        // [public identifier], and [declaration base URI] are considered
        // to be duplicate. An application may also be able to detect that
        // notations are duplicate through other means. For instance, the URI
        // resulting from combining the system identifier and the declaration
        // base URI is the same.
        public boolean isDuplicate(Object obj){
            if(obj!=null&&obj instanceof Notation){
                Notation other=(Notation)obj;
                return Objects.equals(name,other.name)
                        &&Objects.equals(publicId,other.publicId)
                        &&Objects.equals(expandedSystemId,other.expandedSystemId);
            }
            return false;
        }


    }

    // This is a storage class to hold information about the unparsed entities.
    // We're not using XMLEntityDecl because we don't want to lose the augmentations.
    protected static class UnparsedEntity{
        public String name;
        public String systemId;
        public String baseURI;
        public String publicId;
        public String expandedSystemId;
        public String notation;
        public Augmentations augmentations;

        // from 4.5.1:
        // Unparsed entity items with the same [name], [system identifier],
        // [public identifier], [declaration base URI], [notation name], and
        // [notation] are considered to be duplicate. An application may also
        // be able to detect that unparsed entities are duplicate through other
        // means. For instance, the URI resulting from combining the system
        // identifier and the declaration base URI is the same.
        public boolean isDuplicate(Object obj){
            if(obj!=null&&obj instanceof UnparsedEntity){
                UnparsedEntity other=(UnparsedEntity)obj;
                return Objects.equals(name,other.name)
                        &&Objects.equals(publicId,other.publicId)
                        &&Objects.equals(expandedSystemId,other.expandedSystemId)
                        &&Objects.equals(notation,other.notation);
            }
            return false;
        }        // equals() returns true if two UnparsedEntities have the same name.
        // Useful for searching Vectors for entities with the same name
        @Override
        public boolean equals(Object obj){
            return obj==this||obj instanceof UnparsedEntity
                    &&Objects.equals(name,((UnparsedEntity)obj).name);
        }

        @Override
        public int hashCode(){
            return Objects.hashCode(name);
        }


    }













}
