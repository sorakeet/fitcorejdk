/**
 * Copyright (c) 2003, 2016, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.io.ASCIIReader;
import com.sun.org.apache.xerces.internal.impl.io.UCSReader;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.*;
import com.sun.xml.internal.stream.Entity;
import com.sun.xml.internal.stream.StaxEntityResolverWrapper;
import com.sun.xml.internal.stream.StaxXMLInputSource;
import com.sun.xml.internal.stream.XMLEntityStorage;

import javax.xml.stream.XMLInputFactory;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class XMLEntityManager implements XMLComponent, XMLEntityResolver{
    //
    // Constants
    //
    public static final int DEFAULT_BUFFER_SIZE=8192;
    public static final int DEFAULT_XMLDECL_BUFFER_SIZE=64;
    public static final int DEFAULT_INTERNAL_BUFFER_SIZE=1024;
    // feature identifiers
    protected static final String VALIDATION=
            Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
    protected static final String EXTERNAL_GENERAL_ENTITIES=
            Constants.SAX_FEATURE_PREFIX+Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;
    protected static final String EXTERNAL_PARAMETER_ENTITIES=
            Constants.SAX_FEATURE_PREFIX+Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS=
            Constants.XERCES_FEATURE_PREFIX+Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF=
            Constants.XERCES_FEATURE_PREFIX+Constants.WARN_ON_DUPLICATE_ENTITYDEF_FEATURE;
    protected static final String LOAD_EXTERNAL_DTD=
            Constants.XERCES_FEATURE_PREFIX+Constants.LOAD_EXTERNAL_DTD_FEATURE;
    // property identifiers
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    protected static final String STANDARD_URI_CONFORMANT=
            Constants.XERCES_FEATURE_PREFIX+Constants.STANDARD_URI_CONFORMANT_FEATURE;
    protected static final String ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String STAX_ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.STAX_ENTITY_RESOLVER_PROPERTY;
    // property identifier:  ValidationManager
    protected static final String VALIDATION_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String BUFFER_SIZE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.BUFFER_SIZE_PROPERTY;
    protected static final String SECURITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SECURITY_MANAGER_PROPERTY;
    protected static final String PARSER_SETTINGS=
            Constants.XERCES_FEATURE_PREFIX+Constants.PARSER_SETTINGS;
    static final String EXTERNAL_ACCESS_DEFAULT=Constants.EXTERNAL_ACCESS_DEFAULT;
    private static final String XML_SECURITY_PROPERTY_MANAGER=
            Constants.XML_SECURITY_PROPERTY_MANAGER;
    // recognized features and properties
    private static final String[] RECOGNIZED_FEATURES={
            VALIDATION,
            EXTERNAL_GENERAL_ENTITIES,
            EXTERNAL_PARAMETER_ENTITIES,
            ALLOW_JAVA_ENCODINGS,
            WARN_ON_DUPLICATE_ENTITYDEF,
            STANDARD_URI_CONFORMANT
    };
    private static final Boolean[] FEATURE_DEFAULTS={
            null,
            Boolean.TRUE,
            Boolean.TRUE,
            Boolean.TRUE,
            Boolean.FALSE,
            Boolean.FALSE
    };
    private static final String[] RECOGNIZED_PROPERTIES={
            SYMBOL_TABLE,
            ERROR_REPORTER,
            ENTITY_RESOLVER,
            VALIDATION_MANAGER,
            BUFFER_SIZE,
            SECURITY_MANAGER,
            XML_SECURITY_PROPERTY_MANAGER
    };
    private static final Object[] PROPERTY_DEFAULTS={
            null,
            null,
            null,
            null,
            new Integer(DEFAULT_BUFFER_SIZE),
            null,
            null
    };
    private static final String XMLEntity="[xml]".intern();
    private static final String DTDEntity="[dtd]".intern();
    // debugging
    private static final boolean DEBUG_BUFFER=false;
    private static final boolean DEBUG_ENTITIES=false;
    private static final boolean DEBUG_ENCODINGS=false;
    // should be diplayed trace resolving messages
    private static final boolean DEBUG_RESOLVER=false;
    //
    // Public static methods
    //
    // current value of the "user.dir" property
    private static String gUserDir;
    // cached URI object for the current value of the escaped "user.dir" property stored as a URI
    private static URI gUserDirURI;
    // which ASCII characters need to be escaped
    private static boolean gNeedEscaping[]=new boolean[128];
    // the first hex character if a character needs to be escaped
    private static char gAfterEscaping1[]=new char[128];
    // the second hex character if a character needs to be escaped
    private static char gAfterEscaping2[]=new char[128];
    private static char[] gHexChs={'0','1','2','3','4','5','6','7',
            '8','9','A','B','C','D','E','F'};

    // initialize the above 3 arrays
    static{
        for(int i=0;i<=0x1f;i++){
            gNeedEscaping[i]=true;
            gAfterEscaping1[i]=gHexChs[i>>4];
            gAfterEscaping2[i]=gHexChs[i&0xf];
        }
        gNeedEscaping[0x7f]=true;
        gAfterEscaping1[0x7f]='7';
        gAfterEscaping2[0x7f]='F';
        char[] escChs={' ','<','>','#','%','"','{','}',
                '|','\\','^','~','[',']','`'};
        int len=escChs.length;
        char ch;
        for(int i=0;i<len;i++){
            ch=escChs[i];
            gNeedEscaping[ch]=true;
            gAfterEscaping1[ch]=gHexChs[ch>>4];
            gAfterEscaping2[ch]=gHexChs[ch&0xf];
        }
    }

    protected final Object[] defaultEncoding=new Object[]{"UTF-8",null};
    // temp vars
    private final XMLResourceIdentifierImpl fResourceIdentifier=new XMLResourceIdentifierImpl();
    private final Augmentations fEntityAugs=new AugmentationsImpl();
    protected boolean fStrictURI;
    protected boolean fWarnDuplicateEntityDef;
    //
    // Data
    //
    // features
    protected boolean fValidation;
    protected boolean fExternalGeneralEntities;
    protected boolean fExternalParameterEntities;
    protected boolean fAllowJavaEncodings=true;
    protected boolean fLoadExternalDTD=true;
    // properties
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityResolver fEntityResolver;
    protected StaxEntityResolverWrapper fStaxEntityResolver;
    protected PropertyManager fPropertyManager;
    protected String fAccessExternalDTD=EXTERNAL_ACCESS_DEFAULT;
    // settings
    protected ValidationManager fValidationManager;
    // settings
    protected int fBufferSize=DEFAULT_BUFFER_SIZE;
    protected XMLSecurityManager fSecurityManager=null;
    protected XMLLimitAnalyzer fLimitAnalyzer=null;
    protected int entityExpansionIndex;
    protected boolean fStandalone;
    // are the entities being parsed in the external subset?
    // NOTE:  this *is not* the same as whether they're external entities!
    protected boolean fInExternalSubset=false;
    // handlers
    protected XMLEntityHandler fEntityHandler;
    protected XMLEntityScanner fEntityScanner;
    protected XMLEntityScanner fXML10EntityScanner;
    protected XMLEntityScanner fXML11EntityScanner;
    protected int fEntityExpansionCount=0;
    // entities
    protected Map<String,Entity> fEntities=new HashMap<>();
    protected Stack<Entity> fEntityStack=new Stack<>();
    //
    // Constructors
    //
    protected Entity.ScannedEntity fCurrentEntity=null;
    // shared context
    protected XMLEntityStorage fEntityStorage;
    boolean fSupportDTD=true;
    boolean fReplaceEntityReferences=true;
    boolean fSupportExternalEntities=true;
    boolean fISCreatedByResolver=false;
    private CharacterBufferPool fBufferPool=new CharacterBufferPool(fBufferSize,DEFAULT_INTERNAL_BUFFER_SIZE);

    public XMLEntityManager(){
        //for entity managers not created by parsers
        fSecurityManager=new XMLSecurityManager(true);
        fEntityStorage=new XMLEntityStorage(this);
        setScannerVersion(Constants.XML_VERSION_1_0);
    } // <init>()

    public void setScannerVersion(short version){
        if(version==Constants.XML_VERSION_1_0){
            if(fXML10EntityScanner==null){
                fXML10EntityScanner=new XMLEntityScanner();
            }
            fXML10EntityScanner.reset(fSymbolTable,this,fErrorReporter);
            fEntityScanner=fXML10EntityScanner;
            fEntityScanner.setCurrentEntity(fCurrentEntity);
        }else{
            if(fXML11EntityScanner==null){
                fXML11EntityScanner=new XML11EntityScanner();
            }
            fXML11EntityScanner.reset(fSymbolTable,this,fErrorReporter);
            fEntityScanner=fXML11EntityScanner;
            fEntityScanner.setCurrentEntity(fCurrentEntity);
        }
    }

    public XMLEntityManager(PropertyManager propertyManager){
        fPropertyManager=propertyManager;
        //pass a reference to current entity being scanned
        //fEntityStorage = new XMLEntityStorage(fCurrentEntity) ;
        fEntityStorage=new XMLEntityStorage(this);
        fEntityScanner=new XMLEntityScanner(propertyManager,this);
        reset(propertyManager);
    } // <init>()

    //
    // XMLComponent methods
    //
    public void reset(PropertyManager propertyManager){
        // xerces properties
        fSymbolTable=(SymbolTable)propertyManager.getProperty(Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY);
        fErrorReporter=(XMLErrorReporter)propertyManager.getProperty(Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY);
        try{
            fStaxEntityResolver=(StaxEntityResolverWrapper)propertyManager.getProperty(STAX_ENTITY_RESOLVER);
        }catch(XMLConfigurationException e){
            fStaxEntityResolver=null;
        }
        fSupportDTD=((Boolean)propertyManager.getProperty(XMLInputFactory.SUPPORT_DTD)).booleanValue();
        fReplaceEntityReferences=((Boolean)propertyManager.getProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)).booleanValue();
        fSupportExternalEntities=((Boolean)propertyManager.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)).booleanValue();
        // Zephyr feature ignore-external-dtd is the opposite of Xerces' load-external-dtd
        fLoadExternalDTD=!((Boolean)propertyManager.getProperty(Constants.ZEPHYR_PROPERTY_PREFIX+Constants.IGNORE_EXTERNAL_DTD)).booleanValue();
        // JAXP 1.5 feature
        XMLSecurityPropertyManager spm=(XMLSecurityPropertyManager)propertyManager.getProperty(XML_SECURITY_PROPERTY_MANAGER);
        fAccessExternalDTD=spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        fSecurityManager=(XMLSecurityManager)propertyManager.getProperty(SECURITY_MANAGER);
        fLimitAnalyzer=new XMLLimitAnalyzer();
        //reset fEntityStorage
        fEntityStorage.reset(propertyManager);
        //reset XMLEntityReaderImpl
        fEntityScanner.reset(propertyManager);
        // initialize state
        //fStandalone = false;
        fEntities.clear();
        fEntityStack.removeAllElements();
        fCurrentEntity=null;
        fValidation=false;
        fExternalGeneralEntities=true;
        fExternalParameterEntities=true;
        fAllowJavaEncodings=true;
    }
    //
    // Public methods
    //

    public static String expandSystemId(String systemId){
        return expandSystemId(systemId,null);
    } // expandSystemId(String):String
    // setStandalone(boolean)

    public static String expandSystemId(String systemId,String baseSystemId){
        // check for bad parameters id
        if(systemId==null||systemId.length()==0){
            return systemId;
        }
        // if id already expanded, return
        try{
            URI uri=new URI(systemId);
            if(uri!=null){
                return systemId;
            }
        }catch(URI.MalformedURIException e){
            // continue on...
        }
        // normalize id
        String id=fixURI(systemId);
        // normalize base
        URI base=null;
        URI uri=null;
        try{
            if(baseSystemId==null||baseSystemId.length()==0||
                    baseSystemId.equals(systemId)){
                String dir=getUserDir().toString();
                base=new URI("file","",dir,null,null);
            }else{
                try{
                    base=new URI(fixURI(baseSystemId));
                }catch(URI.MalformedURIException e){
                    if(baseSystemId.indexOf(':')!=-1){
                        // for xml schemas we might have baseURI with
                        // a specified drive
                        base=new URI("file","",fixURI(baseSystemId),null,null);
                    }else{
                        String dir=getUserDir().toString();
                        dir=dir+fixURI(baseSystemId);
                        base=new URI("file","",dir,null,null);
                    }
                }
            }
            // expand id
            uri=new URI(base,id);
        }catch(Exception e){
            // let it go through
        }
        if(uri==null){
            return systemId;
        }
        return uri.toString();
    } // expandSystemId(String,String):String

    public static void absolutizeAgainstUserDir(URI uri)
            throws URI.MalformedURIException{
        uri.absolutize(getUserDir());
    }

    private static String expandSystemIdStrictOn(String systemId,String baseSystemId)
            throws URI.MalformedURIException{
        URI systemURI=new URI(systemId,true);
        // If it's already an absolute one, return it
        if(systemURI.isAbsoluteURI()){
            return systemId;
        }
        // If there isn't a base URI, use the working directory
        URI baseURI=null;
        if(baseSystemId==null||baseSystemId.length()==0){
            baseURI=getUserDir();
        }else{
            baseURI=new URI(baseSystemId,true);
            if(!baseURI.isAbsoluteURI()){
                // assume "base" is also a relative uri
                baseURI.absolutize(getUserDir());
            }
        }
        // absolutize the system identifier using the base URI
        systemURI.absolutize(baseURI);
        // return the string rep of the new uri (an absolute one)
        return systemURI.toString();
        // if any exception is thrown, it'll get thrown to the caller.
    } // expandSystemIdStrictOn(String,String):String

    public void addUnparsedEntity(String name,
                                  String publicId,String systemId,
                                  String baseSystemId,String notation){
        if(!fEntities.containsKey(name)){
            Entity.ExternalEntity entity=new Entity.ExternalEntity(name,
                    new XMLEntityDescriptionImpl(name,publicId,systemId,baseSystemId,null),
                    notation,fInExternalSubset);
            fEntities.put(name,entity);
        }else{
            if(fWarnDuplicateEntityDef){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "MSG_DUPLICATE_ENTITY_DEFINITION",
                        new Object[]{name},
                        XMLErrorReporter.SEVERITY_WARNING);
            }
        }
    } // addUnparsedEntity(String,String,String,String)

    public XMLEntityStorage getEntityStore(){
        return fEntityStorage;
    }

    public boolean isExternalEntity(String entityName){
        Entity entity=fEntities.get(entityName);
        if(entity==null){
            return false;
        }
        return entity.isExternal();
    }

    public boolean isEntityDeclInExternalSubset(String entityName){
        Entity entity=fEntities.get(entityName);
        if(entity==null){
            return false;
        }
        return entity.isEntityDeclInExternalSubset();
    }

    public boolean isStandalone(){
        return fStandalone;
    }  //isStandalone():boolean

    public void setStandalone(boolean standalone){
        fStandalone=standalone;
    }

    public boolean isDeclaredEntity(String entityName){
        Entity entity=fEntities.get(entityName);
        return entity!=null;
    }

    public boolean isUnparsedEntity(String entityName){
        Entity entity=fEntities.get(entityName);
        if(entity==null){
            return false;
        }
        return entity.isUnparsed();
    }

    // this simply returns the fResourceIdentifier object;
    // this should only be used with caution by callers that
    // carefully manage the entity manager's behaviour, so that
    // this doesn't returning meaningless or misleading data.
    // @return  a reference to the current fResourceIdentifier object
    public XMLResourceIdentifier getCurrentResourceIdentifier(){
        return fResourceIdentifier;
    }

    public void setEntityHandler(XMLEntityHandler entityHandler){
        fEntityHandler=(XMLEntityHandler)entityHandler;
    } // setEntityHandler(XMLEntityHandler)

    //this function returns StaxXMLInputSource
    public StaxXMLInputSource resolveEntityAsPerStax(XMLResourceIdentifier resourceIdentifier) throws IOException{
        if(resourceIdentifier==null) return null;
        String publicId=resourceIdentifier.getPublicId();
        String literalSystemId=resourceIdentifier.getLiteralSystemId();
        String baseSystemId=resourceIdentifier.getBaseSystemId();
        String expandedSystemId=resourceIdentifier.getExpandedSystemId();
        // if no base systemId given, assume that it's relative
        // to the systemId of the current scanned entity
        // Sometimes the system id is not (properly) expanded.
        // We need to expand the system id if:
        // a. the expanded one was null; or
        // b. the base system id was null, but becomes non-null from the current entity.
        boolean needExpand=(expandedSystemId==null);
        // REVISIT:  why would the baseSystemId ever be null?  if we
        // didn't have to make this check we wouldn't have to reuse the
        // fXMLResourceIdentifier object...
        if(baseSystemId==null&&fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null){
            baseSystemId=fCurrentEntity.entityLocation.getExpandedSystemId();
            if(baseSystemId!=null)
                needExpand=true;
        }
        if(needExpand)
            expandedSystemId=expandSystemId(literalSystemId,baseSystemId,false);
        // give the entity resolver a chance
        StaxXMLInputSource staxInputSource=null;
        XMLInputSource xmlInputSource=null;
        XMLResourceIdentifierImpl ri=null;
        if(resourceIdentifier instanceof XMLResourceIdentifierImpl){
            ri=(XMLResourceIdentifierImpl)resourceIdentifier;
        }else{
            fResourceIdentifier.clear();
            ri=fResourceIdentifier;
        }
        ri.setValues(publicId,literalSystemId,baseSystemId,expandedSystemId);
        if(DEBUG_RESOLVER){
            System.out.println("BEFORE Calling resolveEntity");
        }
        fISCreatedByResolver=false;
        //either of Stax or Xerces would be null
        if(fStaxEntityResolver!=null){
            staxInputSource=fStaxEntityResolver.resolveEntity(ri);
            if(staxInputSource!=null){
                fISCreatedByResolver=true;
            }
        }
        if(fEntityResolver!=null){
            xmlInputSource=fEntityResolver.resolveEntity(ri);
            if(xmlInputSource!=null){
                fISCreatedByResolver=true;
            }
        }
        if(xmlInputSource!=null){
            //wrap this XMLInputSource to StaxInputSource
            staxInputSource=new StaxXMLInputSource(xmlInputSource,fISCreatedByResolver);
        }
        // do default resolution
        //this works for both stax & Xerces, if staxInputSource is null, it means parser need to revert to default resolution
        if(staxInputSource==null){
            // REVISIT: when systemId is null, I think we should return null.
            //          is this the right solution? -SG
            //if (systemId != null)
            staxInputSource=new StaxXMLInputSource(new XMLInputSource(publicId,literalSystemId,baseSystemId));
        }else if(staxInputSource.hasXMLStreamOrXMLEventReader()){
            //Waiting for the clarification from EG. - nb
        }
        if(DEBUG_RESOLVER){
            System.err.println("XMLEntityManager.resolveEntity("+publicId+")");
            System.err.println(" = "+xmlInputSource);
        }
        return staxInputSource;
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws IOException, XNIException{
        if(resourceIdentifier==null) return null;
        String publicId=resourceIdentifier.getPublicId();
        String literalSystemId=resourceIdentifier.getLiteralSystemId();
        String baseSystemId=resourceIdentifier.getBaseSystemId();
        String expandedSystemId=resourceIdentifier.getExpandedSystemId();
        // if no base systemId given, assume that it's relative
        // to the systemId of the current scanned entity
        // Sometimes the system id is not (properly) expanded.
        // We need to expand the system id if:
        // a. the expanded one was null; or
        // b. the base system id was null, but becomes non-null from the current entity.
        boolean needExpand=(expandedSystemId==null);
        // REVISIT:  why would the baseSystemId ever be null?  if we
        // didn't have to make this check we wouldn't have to reuse the
        // fXMLResourceIdentifier object...
        if(baseSystemId==null&&fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null){
            baseSystemId=fCurrentEntity.entityLocation.getExpandedSystemId();
            if(baseSystemId!=null)
                needExpand=true;
        }
        if(needExpand)
            expandedSystemId=expandSystemId(literalSystemId,baseSystemId,false);
        // give the entity resolver a chance
        XMLInputSource xmlInputSource=null;
        if(fEntityResolver!=null){
            resourceIdentifier.setBaseSystemId(baseSystemId);
            resourceIdentifier.setExpandedSystemId(expandedSystemId);
            xmlInputSource=fEntityResolver.resolveEntity(resourceIdentifier);
        }
        // do default resolution
        // REVISIT: what's the correct behavior if the user provided an entity
        // resolver (fEntityResolver != null), but resolveEntity doesn't return
        // an input source (xmlInputSource == null)?
        // do we do default resolution, or do we just return null? -SG
        if(xmlInputSource==null){
            // REVISIT: when systemId is null, I think we should return null.
            //          is this the right solution? -SG
            //if (systemId != null)
            xmlInputSource=new XMLInputSource(publicId,literalSystemId,baseSystemId);
        }
        if(DEBUG_RESOLVER){
            System.err.println("XMLEntityManager.resolveEntity("+publicId+")");
            System.err.println(" = "+xmlInputSource);
        }
        return xmlInputSource;
    } // resolveEntity(XMLResourceIdentifier):XMLInputSource

    public static String expandSystemId(String systemId,String baseSystemId,
                                        boolean strict)
            throws URI.MalformedURIException{
        // check if there is a system id before
        // trying to expand it.
        if(systemId==null){
            return null;
        }
        // system id has to be a valid URI
        if(strict){
            try{
                // if it's already an absolute one, return it
                new URI(systemId);
                return systemId;
            }catch(URI.MalformedURIException ex){
            }
            URI base=null;
            // if there isn't a base uri, use the working directory
            if(baseSystemId==null||baseSystemId.length()==0){
                base=new URI("file","",getUserDir().toString(),null,null);
            }
            // otherwise, use the base uri
            else{
                try{
                    base=new URI(baseSystemId);
                }catch(URI.MalformedURIException e){
                    // assume "base" is also a relative uri
                    String dir=getUserDir().toString();
                    dir=dir+baseSystemId;
                    base=new URI("file","",dir,null,null);
                }
            }
            // absolutize the system id using the base
            URI uri=new URI(base,systemId);
            // return the string rep of the new uri (an absolute one)
            return uri.toString();
            // if any exception is thrown, it'll get thrown to the caller.
        }
        // Assume the URIs are well-formed. If it turns out they're not, try fixing them up.
        try{
            return expandSystemIdStrictOff(systemId,baseSystemId);
        }catch(URI.MalformedURIException e){
            /** Xerces URI rejects unicode, try java.net.URI
             * this is not ideal solution, but it covers known cases which either
             * Xerces URI or java.net.URI can handle alone
             * will file bug against java.net.URI
             */
            try{
                return expandSystemIdStrictOff1(systemId,baseSystemId);
            }catch(URISyntaxException ex){
                // continue on...
            }
        }
        // check for bad parameters id
        if(systemId.length()==0){
            return systemId;
        }
        // normalize id
        String id=fixURI(systemId);
        // normalize base
        URI base=null;
        URI uri=null;
        try{
            if(baseSystemId==null||baseSystemId.length()==0||
                    baseSystemId.equals(systemId)){
                base=getUserDir();
            }else{
                try{
                    base=new URI(fixURI(baseSystemId).trim());
                }catch(URI.MalformedURIException e){
                    if(baseSystemId.indexOf(':')!=-1){
                        // for xml schemas we might have baseURI with
                        // a specified drive
                        base=new URI("file","",fixURI(baseSystemId).trim(),null,null);
                    }else{
                        base=new URI(getUserDir(),fixURI(baseSystemId));
                    }
                }
            }
            // expand id
            uri=new URI(base,id.trim());
        }catch(Exception e){
            // let it go through
        }
        if(uri==null){
            return systemId;
        }
        return uri.toString();
    } // expandSystemId(String,String,boolean):String

    // To escape the "user.dir" system property, by using %HH to represent
    // special ASCII characters: 0x00~0x1F, 0x7F, ' ', '<', '>', '#', '%'
    // and '"'. It's a static method, so needs to be synchronized.
    // this method looks heavy, but since the system property isn't expected
    // to change often, so in most cases, we only need to return the URI
    // that was escaped before.
    // According to the URI spec, non-ASCII characters (whose value >= 128)
    // need to be escaped too.
    // REVISIT: don't know how to escape non-ASCII characters, especially
    // which encoding to use. Leave them for now.
    private static synchronized URI getUserDir() throws URI.MalformedURIException{
        // get the user.dir property
        String userDir="";
        try{
            userDir=SecuritySupport.getSystemProperty("user.dir");
        }catch(SecurityException se){
        }
        // return empty string if property value is empty string.
        if(userDir.length()==0)
            return new URI("file","","",null,null);
        // compute the new escaped value if the new property value doesn't
        // match the previous one
        if(gUserDirURI!=null&&userDir.equals(gUserDir)){
            return gUserDirURI;
        }
        // record the new value as the global property value
        gUserDir=userDir;
        char separator=File.separatorChar;
        userDir=userDir.replace(separator,'/');
        int len=userDir.length(), ch;
        StringBuffer buffer=new StringBuffer(len*3);
        // change C:/blah to /C:/blah
        if(len>=2&&userDir.charAt(1)==':'){
            ch=Character.toUpperCase(userDir.charAt(0));
            if(ch>='A'&&ch<='Z'){
                buffer.append('/');
            }
        }
        // for each character in the path
        int i=0;
        for(;i<len;i++){
            ch=userDir.charAt(i);
            // if it's not an ASCII character, break here, and use UTF-8 encoding
            if(ch>=128)
                break;
            if(gNeedEscaping[ch]){
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
                // record the fact that it's escaped
            }else{
                buffer.append((char)ch);
            }
        }
        // we saw some non-ascii character
        if(i<len){
            // get UTF-8 bytes for the remaining sub-string
            byte[] bytes=null;
            byte b;
            try{
                bytes=userDir.substring(i).getBytes("UTF-8");
            }catch(UnsupportedEncodingException e){
                // should never happen
                return new URI("file","",userDir,null,null);
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
        // change blah/blah to blah/blah/
        if(!userDir.endsWith("/"))
            buffer.append('/');
        gUserDirURI=new URI("file","",buffer.toString(),null,null);
        return gUserDirURI;
    }

    private static String expandSystemIdStrictOff(String systemId,String baseSystemId)
            throws URI.MalformedURIException{
        URI systemURI=new URI(systemId,true);
        // If it's already an absolute one, return it
        if(systemURI.isAbsoluteURI()){
            if(systemURI.getScheme().length()>1){
                return systemId;
            }
            /**
             * If the scheme's length is only one character,
             * it's likely that this was intended as a file
             * path. Fixing this up in expandSystemId to
             * maintain backwards compatibility.
             */
            throw new URI.MalformedURIException();
        }
        // If there isn't a base URI, use the working directory
        URI baseURI=null;
        if(baseSystemId==null||baseSystemId.length()==0){
            baseURI=getUserDir();
        }else{
            baseURI=new URI(baseSystemId,true);
            if(!baseURI.isAbsoluteURI()){
                // assume "base" is also a relative uri
                baseURI.absolutize(getUserDir());
            }
        }
        // absolutize the system identifier using the base URI
        systemURI.absolutize(baseURI);
        // return the string rep of the new uri (an absolute one)
        return systemURI.toString();
        // if any exception is thrown, it'll get thrown to the caller.
    } // expandSystemIdStrictOff(String,String):String

    private static String expandSystemIdStrictOff1(String systemId,String baseSystemId)
            throws URISyntaxException, URI.MalformedURIException{
        java.net.URI systemURI=new java.net.URI(systemId);
        // If it's already an absolute one, return it
        if(systemURI.isAbsolute()){
            if(systemURI.getScheme().length()>1){
                return systemId;
            }
            /**
             * If the scheme's length is only one character,
             * it's likely that this was intended as a file
             * path. Fixing this up in expandSystemId to
             * maintain backwards compatibility.
             */
            throw new URISyntaxException(systemId,"the scheme's length is only one character");
        }
        // If there isn't a base URI, use the working directory
        URI baseURI=null;
        if(baseSystemId==null||baseSystemId.length()==0){
            baseURI=getUserDir();
        }else{
            baseURI=new URI(baseSystemId,true);
            if(!baseURI.isAbsoluteURI()){
                // assume "base" is also a relative uri
                baseURI.absolutize(getUserDir());
            }
        }
        // absolutize the system identifier using the base URI
//        systemURI.absolutize(baseURI);
        systemURI=(new java.net.URI(baseURI.toString())).resolve(systemURI);
        // return the string rep of the new uri (an absolute one)
        return systemURI.toString();
        // if any exception is thrown, it'll get thrown to the caller.
    } // expandSystemIdStrictOff(String,String):String

    protected static String fixURI(String str){
        // handle platform dependent strings
        str=str.replace(File.separatorChar,'/');
        // Windows fix
        if(str.length()>=2){
            char ch1=str.charAt(1);
            // change "C:blah" to "/C:blah"
            if(ch1==':'){
                char ch0=Character.toUpperCase(str.charAt(0));
                if(ch0>='A'&&ch0<='Z'){
                    str="/"+str;
                }
            }
            // change "//blah" to "file://blah"
            else if(ch1=='/'&&str.charAt(0)=='/'){
                str="file:"+str;
            }
        }
        // replace spaces in file names with %20.
        // Original comment from JDK5: the following algorithm might not be
        // very performant, but people who want to use invalid URI's have to
        // pay the price.
        int pos=str.indexOf(' ');
        if(pos>=0){
            StringBuilder sb=new StringBuilder(str.length());
            // put characters before ' ' into the string builder
            for(int i=0;i<pos;i++)
                sb.append(str.charAt(i));
            // and %20 for the space
            sb.append("%20");
            // for the remamining part, also convert ' ' to "%20".
            for(int i=pos+1;i<str.length();i++){
                if(str.charAt(i)==' ')
                    sb.append("%20");
                else
                    sb.append(str.charAt(i));
            }
            str=sb.toString();
        }
        // done
        return str;
    } // fixURI(String):String

    public void startEntity(boolean isGE,String entityName,boolean literal)
            throws IOException, XNIException{
        // was entity declared?
        Entity entity=fEntityStorage.getEntity(entityName);
        if(entity==null){
            if(fEntityHandler!=null){
                String encoding=null;
                fResourceIdentifier.clear();
                fEntityAugs.removeAllItems();
                fEntityAugs.putItem(Constants.ENTITY_SKIPPED,Boolean.TRUE);
                fEntityHandler.startEntity(entityName,fResourceIdentifier,encoding,fEntityAugs);
                fEntityAugs.removeAllItems();
                fEntityAugs.putItem(Constants.ENTITY_SKIPPED,Boolean.TRUE);
                fEntityHandler.endEntity(entityName,fEntityAugs);
            }
            return;
        }
        // should we skip external entities?
        boolean external=entity.isExternal();
        Entity.ExternalEntity externalEntity=null;
        String extLitSysId=null, extBaseSysId=null, expandedSystemId=null;
        if(external){
            externalEntity=(Entity.ExternalEntity)entity;
            extLitSysId=(externalEntity.entityLocation!=null?externalEntity.entityLocation.getLiteralSystemId():null);
            extBaseSysId=(externalEntity.entityLocation!=null?externalEntity.entityLocation.getBaseSystemId():null);
            expandedSystemId=expandSystemId(extLitSysId,extBaseSysId);
            boolean unparsed=entity.isUnparsed();
            boolean parameter=entityName.startsWith("%");
            boolean general=!parameter;
            if(unparsed||(general&&!fExternalGeneralEntities)||
                    (parameter&&!fExternalParameterEntities)||
                    !fSupportDTD||!fSupportExternalEntities){
                if(fEntityHandler!=null){
                    fResourceIdentifier.clear();
                    final String encoding=null;
                    fResourceIdentifier.setValues(
                            (externalEntity.entityLocation!=null?externalEntity.entityLocation.getPublicId():null),
                            extLitSysId,extBaseSysId,expandedSystemId);
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED,Boolean.TRUE);
                    fEntityHandler.startEntity(entityName,fResourceIdentifier,encoding,fEntityAugs);
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED,Boolean.TRUE);
                    fEntityHandler.endEntity(entityName,fEntityAugs);
                }
                return;
            }
        }
        // is entity recursive?
        int size=fEntityStack.size();
        for(int i=size;i>=0;i--){
            Entity activeEntity=i==size
                    ?fCurrentEntity
                    :(Entity)fEntityStack.elementAt(i);
            if(activeEntity.name==entityName){
                String path=entityName;
                for(int j=i+1;j<size;j++){
                    activeEntity=(Entity)fEntityStack.elementAt(j);
                    path=path+" -> "+activeEntity.name;
                }
                path=path+" -> "+fCurrentEntity.name;
                path=path+" -> "+entityName;
                fErrorReporter.reportError(this.getEntityScanner(),XMLMessageFormatter.XML_DOMAIN,
                        "RecursiveReference",
                        new Object[]{entityName,path},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
                if(fEntityHandler!=null){
                    fResourceIdentifier.clear();
                    final String encoding=null;
                    if(external){
                        fResourceIdentifier.setValues(
                                (externalEntity.entityLocation!=null?externalEntity.entityLocation.getPublicId():null),
                                extLitSysId,extBaseSysId,expandedSystemId);
                    }
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED,Boolean.TRUE);
                    fEntityHandler.startEntity(entityName,fResourceIdentifier,encoding,fEntityAugs);
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED,Boolean.TRUE);
                    fEntityHandler.endEntity(entityName,fEntityAugs);
                }
                return;
            }
        }
        // resolve external entity
        StaxXMLInputSource staxInputSource=null;
        XMLInputSource xmlInputSource=null;
        if(external){
            staxInputSource=resolveEntityAsPerStax(externalEntity.entityLocation);
            /** xxx:  Waiting from the EG
             * //simply return if there was entity resolver registered and application
             * //returns either XMLStreamReader or XMLEventReader.
             * if(staxInputSource.hasXMLStreamOrXMLEventReader()) return ;
             */
            xmlInputSource=staxInputSource.getXMLInputSource();
            if(!fISCreatedByResolver){
                //let the not-LoadExternalDTD or not-SupportDTD process to handle the situation
                if(fLoadExternalDTD){
                    String accessError=SecuritySupport.checkAccess(expandedSystemId,fAccessExternalDTD,Constants.ACCESS_EXTERNAL_ALL);
                    if(accessError!=null){
                        fErrorReporter.reportError(this.getEntityScanner(),XMLMessageFormatter.XML_DOMAIN,
                                "AccessExternalEntity",
                                new Object[]{SecuritySupport.sanitizePath(expandedSystemId),accessError},
                                XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
            }
        }
        // wrap internal entity
        else{
            Entity.InternalEntity internalEntity=(Entity.InternalEntity)entity;
            Reader reader=new StringReader(internalEntity.text);
            xmlInputSource=new XMLInputSource(null,null,null,reader,null);
        }
        // start the entity
        startEntity(isGE,entityName,xmlInputSource,literal,external);
    } // startEntity(String,boolean)

    public void startDocumentEntity(XMLInputSource xmlInputSource)
            throws IOException, XNIException{
        startEntity(false,XMLEntity,xmlInputSource,false,true);
    } // startDocumentEntity(XMLInputSource)

    public void startEntity(boolean isGE,String name,
                            XMLInputSource xmlInputSource,
                            boolean literal,boolean isExternal)
            throws IOException, XNIException{
        String encoding=setupCurrentEntity(isGE,name,xmlInputSource,literal,isExternal);
        //when entity expansion limit is set by the Application, we need to
        //check for the entity expansion limit set by the parser, if number of entity
        //expansions exceeds the entity expansion limit, parser will throw fatal error.
        // Note that this represents the nesting level of open entities.
        fEntityExpansionCount++;
        if(fLimitAnalyzer!=null){
            fLimitAnalyzer.addValue(entityExpansionIndex,name,1);
        }
        if(fSecurityManager!=null&&fSecurityManager.isOverLimit(entityExpansionIndex,fLimitAnalyzer)){
            fSecurityManager.debugPrint(fLimitAnalyzer);
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,"EntityExpansionLimit",
                    new Object[]{fSecurityManager.getLimitValueByIndex(entityExpansionIndex)},
                    XMLErrorReporter.SEVERITY_FATAL_ERROR);
            // is there anything better to do than reset the counter?
            // at least one can envision debugging applications where this might
            // be useful...
            fEntityExpansionCount=0;
        }
        // call handler
        if(fEntityHandler!=null){
            fEntityHandler.startEntity(name,fResourceIdentifier,encoding,null);
        }
    } // startEntity(String,XMLInputSource)

    public String setupCurrentEntity(boolean reference,String name,XMLInputSource xmlInputSource,
                                     boolean literal,boolean isExternal)
            throws IOException, XNIException{
        // get information
        final String publicId=xmlInputSource.getPublicId();
        String literalSystemId=xmlInputSource.getSystemId();
        String baseSystemId=xmlInputSource.getBaseSystemId();
        String encoding=xmlInputSource.getEncoding();
        final boolean encodingExternallySpecified=(encoding!=null);
        Boolean isBigEndian=null;
        // create reader
        InputStream stream=null;
        Reader reader=xmlInputSource.getCharacterStream();
        // First chance checking strict URI
        String expandedSystemId=expandSystemId(literalSystemId,baseSystemId,fStrictURI);
        if(baseSystemId==null){
            baseSystemId=expandedSystemId;
        }
        if(reader==null){
            stream=xmlInputSource.getByteStream();
            if(stream==null){
                URL location=new URL(expandedSystemId);
                URLConnection connect=location.openConnection();
                if(!(connect instanceof HttpURLConnection)){
                    stream=connect.getInputStream();
                }else{
                    boolean followRedirects=true;
                    // setup URLConnection if we have an HTTPInputSource
                    if(xmlInputSource instanceof HTTPInputSource){
                        final HttpURLConnection urlConnection=(HttpURLConnection)connect;
                        final HTTPInputSource httpInputSource=(HTTPInputSource)xmlInputSource;
                        // set request properties
                        Iterator<Map.Entry<String,String>> propIter=httpInputSource.getHTTPRequestProperties();
                        while(propIter.hasNext()){
                            Map.Entry<String,String> entry=propIter.next();
                            urlConnection.setRequestProperty(entry.getKey(),entry.getValue());
                        }
                        // set preference for redirection
                        followRedirects=httpInputSource.getFollowHTTPRedirects();
                        if(!followRedirects){
                            setInstanceFollowRedirects(urlConnection,followRedirects);
                        }
                    }
                    stream=connect.getInputStream();
                    // REVISIT: If the URLConnection has external encoding
                    // information, we should be reading it here. It's located
                    // in the charset parameter of Content-Type. -- mrglavas
                    if(followRedirects){
                        String redirect=connect.getURL().toString();
                        // E43: Check if the URL was redirected, and then
                        // update literal and expanded system IDs if needed.
                        if(!redirect.equals(expandedSystemId)){
                            literalSystemId=redirect;
                            expandedSystemId=redirect;
                        }
                    }
                }
            }
            // wrap this stream in RewindableInputStream
            stream=new RewindableInputStream(stream);
            // perform auto-detect of encoding if necessary
            if(encoding==null){
                // read first four bytes and determine encoding
                final byte[] b4=new byte[4];
                int count=0;
                for(;count<4;count++){
                    b4[count]=(byte)stream.read();
                }
                if(count==4){
                    Object[] encodingDesc=getEncodingName(b4,count);
                    encoding=(String)(encodingDesc[0]);
                    isBigEndian=(Boolean)(encodingDesc[1]);
                    stream.reset();
                    // Special case UTF-8 files with BOM created by Microsoft
                    // tools. It's more efficient to consume the BOM than make
                    // the reader perform extra checks. -Ac
                    if(count>2&&encoding.equals("UTF-8")){
                        int b0=b4[0]&0xFF;
                        int b1=b4[1]&0xFF;
                        int b2=b4[2]&0xFF;
                        if(b0==0xEF&&b1==0xBB&&b2==0xBF){
                            // ignore first three bytes...
                            stream.skip(3);
                        }
                    }
                    reader=createReader(stream,encoding,isBigEndian);
                }else{
                    reader=createReader(stream,encoding,isBigEndian);
                }
            }
            // use specified encoding
            else{
                encoding=encoding.toUpperCase(Locale.ENGLISH);
                // If encoding is UTF-8, consume BOM if one is present.
                if(encoding.equals("UTF-8")){
                    final int[] b3=new int[3];
                    int count=0;
                    for(;count<3;++count){
                        b3[count]=stream.read();
                        if(b3[count]==-1)
                            break;
                    }
                    if(count==3){
                        if(b3[0]!=0xEF||b3[1]!=0xBB||b3[2]!=0xBF){
                            // First three bytes are not BOM, so reset.
                            stream.reset();
                        }
                    }else{
                        stream.reset();
                    }
                }
                // If encoding is UTF-16, we still need to read the first four bytes
                // in order to discover the byte order.
                else if(encoding.equals("UTF-16")){
                    final int[] b4=new int[4];
                    int count=0;
                    for(;count<4;++count){
                        b4[count]=stream.read();
                        if(b4[count]==-1)
                            break;
                    }
                    stream.reset();
                    String utf16Encoding="UTF-16";
                    if(count>=2){
                        final int b0=b4[0];
                        final int b1=b4[1];
                        if(b0==0xFE&&b1==0xFF){
                            // UTF-16, big-endian
                            utf16Encoding="UTF-16BE";
                            isBigEndian=Boolean.TRUE;
                        }else if(b0==0xFF&&b1==0xFE){
                            // UTF-16, little-endian
                            utf16Encoding="UTF-16LE";
                            isBigEndian=Boolean.FALSE;
                        }else if(count==4){
                            final int b2=b4[2];
                            final int b3=b4[3];
                            if(b0==0x00&&b1==0x3C&&b2==0x00&&b3==0x3F){
                                // UTF-16, big-endian, no BOM
                                utf16Encoding="UTF-16BE";
                                isBigEndian=Boolean.TRUE;
                            }
                            if(b0==0x3C&&b1==0x00&&b2==0x3F&&b3==0x00){
                                // UTF-16, little-endian, no BOM
                                utf16Encoding="UTF-16LE";
                                isBigEndian=Boolean.FALSE;
                            }
                        }
                    }
                    reader=createReader(stream,utf16Encoding,isBigEndian);
                }
                // If encoding is UCS-4, we still need to read the first four bytes
                // in order to discover the byte order.
                else if(encoding.equals("ISO-10646-UCS-4")){
                    final int[] b4=new int[4];
                    int count=0;
                    for(;count<4;++count){
                        b4[count]=stream.read();
                        if(b4[count]==-1)
                            break;
                    }
                    stream.reset();
                    // Ignore unusual octet order for now.
                    if(count==4){
                        // UCS-4, big endian (1234)
                        if(b4[0]==0x00&&b4[1]==0x00&&b4[2]==0x00&&b4[3]==0x3C){
                            isBigEndian=Boolean.TRUE;
                        }
                        // UCS-4, little endian (1234)
                        else if(b4[0]==0x3C&&b4[1]==0x00&&b4[2]==0x00&&b4[3]==0x00){
                            isBigEndian=Boolean.FALSE;
                        }
                    }
                }
                // If encoding is UCS-2, we still need to read the first four bytes
                // in order to discover the byte order.
                else if(encoding.equals("ISO-10646-UCS-2")){
                    final int[] b4=new int[4];
                    int count=0;
                    for(;count<4;++count){
                        b4[count]=stream.read();
                        if(b4[count]==-1)
                            break;
                    }
                    stream.reset();
                    if(count==4){
                        // UCS-2, big endian
                        if(b4[0]==0x00&&b4[1]==0x3C&&b4[2]==0x00&&b4[3]==0x3F){
                            isBigEndian=Boolean.TRUE;
                        }
                        // UCS-2, little endian
                        else if(b4[0]==0x3C&&b4[1]==0x00&&b4[2]==0x3F&&b4[3]==0x00){
                            isBigEndian=Boolean.FALSE;
                        }
                    }
                }
                reader=createReader(stream,encoding,isBigEndian);
            }
            // read one character at a time so we don't jump too far
            // ahead, converting characters from the byte stream in
            // the wrong encoding
            if(DEBUG_ENCODINGS){
                System.out.println("$$$ no longer wrapping reader in OneCharReader");
            }
            //reader = new OneCharReader(reader);
        }
        // We've seen a new Reader.
        // Push it on the stack so we can close it later.
        //fOwnReaders.add(reader);
        // push entity on stack
        if(fCurrentEntity!=null){
            fEntityStack.push(fCurrentEntity);
        }
        // create entity
        /** if encoding is specified externally, 'encoding' information present
         * in the prolog of the XML document is not considered. Hence, prolog can
         * be read in Chunks of data instead of byte by byte.
         */
        fCurrentEntity=new Entity.ScannedEntity(reference,name,
                new XMLResourceIdentifierImpl(publicId,literalSystemId,baseSystemId,expandedSystemId),
                stream,reader,encoding,literal,encodingExternallySpecified,isExternal);
        fCurrentEntity.setEncodingExternallySpecified(encodingExternallySpecified);
        fEntityScanner.setCurrentEntity(fCurrentEntity);
        fResourceIdentifier.setValues(publicId,literalSystemId,baseSystemId,expandedSystemId);
        if(fLimitAnalyzer!=null){
            fLimitAnalyzer.startEntity(name);
        }
        return encoding;
    } //setupCurrentEntity(String, XMLInputSource, boolean, boolean):  String

    public static void setInstanceFollowRedirects(HttpURLConnection urlCon,boolean followRedirects){
        try{
            Method method=HttpURLConnection.class.getMethod("setInstanceFollowRedirects",new Class[]{Boolean.TYPE});
            method.invoke(urlCon,new Object[]{followRedirects?Boolean.TRUE:Boolean.FALSE});
        }
        // setInstanceFollowRedirects doesn't exist.
        catch(Exception exc){
        }
    }

    protected Object[] getEncodingName(byte[] b4,int count){
        if(count<2){
            return defaultEncoding;
        }
        // UTF-16, with BOM
        int b0=b4[0]&0xFF;
        int b1=b4[1]&0xFF;
        if(b0==0xFE&&b1==0xFF){
            // UTF-16, big-endian
            return new Object[]{"UTF-16BE",new Boolean(true)};
        }
        if(b0==0xFF&&b1==0xFE){
            // UTF-16, little-endian
            return new Object[]{"UTF-16LE",new Boolean(false)};
        }
        // default to UTF-8 if we don't have enough bytes to make a
        // good determination of the encoding
        if(count<3){
            return defaultEncoding;
        }
        // UTF-8 with a BOM
        int b2=b4[2]&0xFF;
        if(b0==0xEF&&b1==0xBB&&b2==0xBF){
            return defaultEncoding;
        }
        // default to UTF-8 if we don't have enough bytes to make a
        // good determination of the encoding
        if(count<4){
            return defaultEncoding;
        }
        // other encodings
        int b3=b4[3]&0xFF;
        if(b0==0x00&&b1==0x00&&b2==0x00&&b3==0x3C){
            // UCS-4, big endian (1234)
            return new Object[]{"ISO-10646-UCS-4",new Boolean(true)};
        }
        if(b0==0x3C&&b1==0x00&&b2==0x00&&b3==0x00){
            // UCS-4, little endian (4321)
            return new Object[]{"ISO-10646-UCS-4",new Boolean(false)};
        }
        if(b0==0x00&&b1==0x00&&b2==0x3C&&b3==0x00){
            // UCS-4, unusual octet order (2143)
            // REVISIT: What should this be?
            return new Object[]{"ISO-10646-UCS-4",null};
        }
        if(b0==0x00&&b1==0x3C&&b2==0x00&&b3==0x00){
            // UCS-4, unusual octect order (3412)
            // REVISIT: What should this be?
            return new Object[]{"ISO-10646-UCS-4",null};
        }
        if(b0==0x00&&b1==0x3C&&b2==0x00&&b3==0x3F){
            // UTF-16, big-endian, no BOM
            // (or could turn out to be UCS-2...
            // REVISIT: What should this be?
            return new Object[]{"UTF-16BE",new Boolean(true)};
        }
        if(b0==0x3C&&b1==0x00&&b2==0x3F&&b3==0x00){
            // UTF-16, little-endian, no BOM
            // (or could turn out to be UCS-2...
            return new Object[]{"UTF-16LE",new Boolean(false)};
        }
        if(b0==0x4C&&b1==0x6F&&b2==0xA7&&b3==0x94){
            // EBCDIC
            // a la xerces1, return CP037 instead of EBCDIC here
            return new Object[]{"CP037",null};
        }
        return defaultEncoding;
    } // getEncodingName(byte[],int):Object[]

    protected Reader createReader(InputStream inputStream,String encoding,Boolean isBigEndian)
            throws IOException{
        // normalize encoding name
        if(encoding==null){
            encoding="UTF-8";
        }
        // try to use an optimized reader
        String ENCODING=encoding.toUpperCase(Locale.ENGLISH);
        if(ENCODING.equals("UTF-8")){
            if(DEBUG_ENCODINGS){
                System.out.println("$$$ creating UTF8Reader");
            }
            return new UTF8Reader(inputStream,fBufferSize,fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN),fErrorReporter.getLocale());
        }
        if(ENCODING.equals("US-ASCII")){
            if(DEBUG_ENCODINGS){
                System.out.println("$$$ creating ASCIIReader");
            }
            return new ASCIIReader(inputStream,fBufferSize,fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN),fErrorReporter.getLocale());
        }
        if(ENCODING.equals("ISO-10646-UCS-4")){
            if(isBigEndian!=null){
                boolean isBE=isBigEndian.booleanValue();
                if(isBE){
                    return new UCSReader(inputStream,UCSReader.UCS4BE);
                }else{
                    return new UCSReader(inputStream,UCSReader.UCS4LE);
                }
            }else{
                fErrorReporter.reportError(this.getEntityScanner(),XMLMessageFormatter.XML_DOMAIN,
                        "EncodingByteOrderUnsupported",
                        new Object[]{encoding},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
        }
        if(ENCODING.equals("ISO-10646-UCS-2")){
            if(isBigEndian!=null){ // sould never happen with this encoding...
                boolean isBE=isBigEndian.booleanValue();
                if(isBE){
                    return new UCSReader(inputStream,UCSReader.UCS2BE);
                }else{
                    return new UCSReader(inputStream,UCSReader.UCS2LE);
                }
            }else{
                fErrorReporter.reportError(this.getEntityScanner(),XMLMessageFormatter.XML_DOMAIN,
                        "EncodingByteOrderUnsupported",
                        new Object[]{encoding},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
        }
        // check for valid name
        boolean validIANA=XMLChar.isValidIANAEncoding(encoding);
        boolean validJava=XMLChar.isValidJavaEncoding(encoding);
        if(!validIANA||(fAllowJavaEncodings&&!validJava)){
            fErrorReporter.reportError(this.getEntityScanner(),XMLMessageFormatter.XML_DOMAIN,
                    "EncodingDeclInvalid",
                    new Object[]{encoding},
                    XMLErrorReporter.SEVERITY_FATAL_ERROR);
            // NOTE: AndyH suggested that, on failure, we use ISO Latin 1
            //       because every byte is a valid ISO Latin 1 character.
            //       It may not translate correctly but if we failed on
            //       the encoding anyway, then we're expecting the content
            //       of the document to be bad. This will just prevent an
            //       invalid UTF-8 sequence to be detected. This is only
            //       important when continue-after-fatal-error is turned
            //       on. -Ac
            encoding="ISO-8859-1";
        }
        // try to use a Java reader
        String javaEncoding=EncodingMap.getIANA2JavaMapping(ENCODING);
        if(javaEncoding==null){
            if(fAllowJavaEncodings){
                javaEncoding=encoding;
            }else{
                fErrorReporter.reportError(this.getEntityScanner(),XMLMessageFormatter.XML_DOMAIN,
                        "EncodingDeclInvalid",
                        new Object[]{encoding},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
                // see comment above.
                javaEncoding="ISO8859_1";
            }
        }
        if(DEBUG_ENCODINGS){
            System.out.print("$$$ creating Java InputStreamReader: encoding="+javaEncoding);
            if(javaEncoding==encoding){
                System.out.print(" (IANA encoding)");
            }
            System.out.println();
        }
        return new BufferedReader(new InputStreamReader(inputStream,javaEncoding));
    } // createReader(InputStream,String, Boolean): Reader
    //
    // Public static methods
    //

    public XMLEntityScanner getEntityScanner(){
        if(fEntityScanner==null){
            // default to 1.0
            if(fXML10EntityScanner==null){
                fXML10EntityScanner=new XMLEntityScanner();
            }
            fXML10EntityScanner.reset(fSymbolTable,this,fErrorReporter);
            fEntityScanner=fXML10EntityScanner;
        }
        return fEntityScanner;
    }

    //xxx these methods are not required.
    public void startDTDEntity(XMLInputSource xmlInputSource)
            throws IOException, XNIException{
        startEntity(false,DTDEntity,xmlInputSource,false,true);
    } // startDTDEntity(XMLInputSource)

    // indicate start of external subset so that
    // location of entity decls can be tracked
    public void startExternalSubset(){
        fInExternalSubset=true;
    }

    public void endExternalSubset(){
        fInExternalSubset=false;
    }

    public Entity.ScannedEntity getCurrentEntity(){
        return fCurrentEntity;
    }

    public Entity.ScannedEntity getTopLevelEntity(){
        return (Entity.ScannedEntity)
                (fEntityStack.empty()?null:fEntityStack.elementAt(0));
    }

    public void closeReaders(){
        /** this call actually does nothing, readers are closed in the endEntity method
         * through the current entity.
         * The change seems to have happened during the jdk6 development with the
         * addition of StAX
         **/
    }

    public void endEntity() throws IOException, XNIException{
        // call handler
        if(DEBUG_BUFFER){
            System.out.print("(endEntity: ");
            print();
            System.out.println();
        }
        //pop the entity from the stack
        Entity.ScannedEntity entity=fEntityStack.size()>0?(Entity.ScannedEntity)fEntityStack.pop():null;
        /** need to close the reader first since the program can end
         *  prematurely (e.g. fEntityHandler.endEntity may throw exception)
         *  leaving the reader open
         */
        //close the reader
        if(fCurrentEntity!=null){
            //close the reader
            try{
                if(fLimitAnalyzer!=null){
                    fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT,fCurrentEntity.name);
                    if(fCurrentEntity.name.equals("[xml]")){
                        fSecurityManager.debugPrint(fLimitAnalyzer);
                    }
                }
                fCurrentEntity.close();
            }catch(IOException ex){
                throw new XNIException(ex);
            }
        }
        if(fEntityHandler!=null){
            //so this is the last opened entity, signal it to current fEntityHandler using Augmentation
            if(entity==null){
                fEntityAugs.removeAllItems();
                fEntityAugs.putItem(Constants.LAST_ENTITY,Boolean.TRUE);
                fEntityHandler.endEntity(fCurrentEntity.name,fEntityAugs);
                fEntityAugs.removeAllItems();
            }else{
                fEntityHandler.endEntity(fCurrentEntity.name,null);
            }
        }
        //check if it is a document entity
        boolean documentEntity=fCurrentEntity.name==XMLEntity;
        //set popped entity as current entity
        fCurrentEntity=entity;
        fEntityScanner.setCurrentEntity(fCurrentEntity);
        //check if there are any entity left in the stack -- if there are
        //no entries EOF has been reached.
        // throw exception when it is the last entity but it is not a document entity
        if(fCurrentEntity==null&!documentEntity){
            throw new EOFException();
        }
        if(DEBUG_BUFFER){
            System.out.print(")endEntity: ");
            print();
            System.out.println();
        }
    } // endEntity()

    //
    // Package visible methods
    //
    final void print(){
        if(DEBUG_BUFFER){
            if(fCurrentEntity!=null){
                System.out.print('[');
                System.out.print(fCurrentEntity.count);
                System.out.print(' ');
                System.out.print(fCurrentEntity.position);
                if(fCurrentEntity.count>0){
                    System.out.print(" \"");
                    for(int i=0;i<fCurrentEntity.count;i++){
                        if(i==fCurrentEntity.position){
                            System.out.print('^');
                        }
                        char c=fCurrentEntity.ch[i];
                        switch(c){
                            case '\n':{
                                System.out.print("\\n");
                                break;
                            }
                            case '\r':{
                                System.out.print("\\r");
                                break;
                            }
                            case '\t':{
                                System.out.print("\\t");
                                break;
                            }
                            case '\\':{
                                System.out.print("\\\\");
                                break;
                            }
                            default:{
                                System.out.print(c);
                            }
                        }
                    }
                    if(fCurrentEntity.position==fCurrentEntity.count){
                        System.out.print('^');
                    }
                    System.out.print('"');
                }
                System.out.print(']');
                System.out.print(" @ ");
                System.out.print(fCurrentEntity.lineNumber);
                System.out.print(',');
                System.out.print(fCurrentEntity.columnNumber);
            }else{
                System.out.print("*NO CURRENT ENTITY*");
            }
        }
    } // print()

    public void reset(XMLComponentManager componentManager)
            throws XMLConfigurationException{
        boolean parser_settings=componentManager.getFeature(PARSER_SETTINGS,true);
        if(!parser_settings){
            // parser settings have not been changed
            reset();
            if(fEntityScanner!=null){
                fEntityScanner.reset(componentManager);
            }
            if(fEntityStorage!=null){
                fEntityStorage.reset(componentManager);
            }
            return;
        }
        // sax features
        fValidation=componentManager.getFeature(VALIDATION,false);
        fExternalGeneralEntities=componentManager.getFeature(EXTERNAL_GENERAL_ENTITIES,true);
        fExternalParameterEntities=componentManager.getFeature(EXTERNAL_PARAMETER_ENTITIES,true);
        // xerces features
        fAllowJavaEncodings=componentManager.getFeature(ALLOW_JAVA_ENCODINGS,false);
        fWarnDuplicateEntityDef=componentManager.getFeature(WARN_ON_DUPLICATE_ENTITYDEF,false);
        fStrictURI=componentManager.getFeature(STANDARD_URI_CONFORMANT,false);
        fLoadExternalDTD=componentManager.getFeature(LOAD_EXTERNAL_DTD,true);
        // xerces properties
        fSymbolTable=(SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        fErrorReporter=(XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        fEntityResolver=(XMLEntityResolver)componentManager.getProperty(ENTITY_RESOLVER,null);
        fStaxEntityResolver=(StaxEntityResolverWrapper)componentManager.getProperty(STAX_ENTITY_RESOLVER,null);
        fValidationManager=(ValidationManager)componentManager.getProperty(VALIDATION_MANAGER,null);
        fSecurityManager=(XMLSecurityManager)componentManager.getProperty(SECURITY_MANAGER,null);
        entityExpansionIndex=fSecurityManager.getIndex(Constants.JDK_ENTITY_EXPANSION_LIMIT);
        //StAX Property
        fSupportDTD=true;
        fReplaceEntityReferences=true;
        fSupportExternalEntities=true;
        // JAXP 1.5 feature
        XMLSecurityPropertyManager spm=(XMLSecurityPropertyManager)componentManager.getProperty(XML_SECURITY_PROPERTY_MANAGER,null);
        if(spm==null){
            spm=new XMLSecurityPropertyManager();
        }
        fAccessExternalDTD=spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        //reset general state
        reset();
        fEntityScanner.reset(componentManager);
        fEntityStorage.reset(componentManager);
    } // reset(XMLComponentManager)

    // reset general state.  Should not be called other than by
    // a class acting as a component manager but not
    // implementing that interface for whatever reason.
    public void reset(){
        fLimitAnalyzer=new XMLLimitAnalyzer();
        // initialize state
        fStandalone=false;
        fEntities.clear();
        fEntityStack.removeAllElements();
        fEntityExpansionCount=0;
        fCurrentEntity=null;
        // reset scanner
        if(fXML10EntityScanner!=null){
            fXML10EntityScanner.reset(fSymbolTable,this,fErrorReporter);
        }
        if(fXML11EntityScanner!=null){
            fXML11EntityScanner.reset(fSymbolTable,this,fErrorReporter);
        }
        // DEBUG
        if(DEBUG_ENTITIES){
            addInternalEntity("text","Hello, World.");
            addInternalEntity("empty-element","<foo/>");
            addInternalEntity("balanced-element","<foo></foo>");
            addInternalEntity("balanced-element-with-text","<foo>Hello, World</foo>");
            addInternalEntity("balanced-element-with-entity","<foo>&text;</foo>");
            addInternalEntity("unbalanced-entity","<foo>");
            addInternalEntity("recursive-entity","<foo>&recursive-entity2;</foo>");
            addInternalEntity("recursive-entity2","<bar>&recursive-entity3;</bar>");
            addInternalEntity("recursive-entity3","<baz>&recursive-entity;</baz>");
            try{
                addExternalEntity("external-text",null,"external-text.ent","test/external-text.xml");
                addExternalEntity("external-balanced-element",null,"external-balanced-element.ent","test/external-balanced-element.xml");
                addExternalEntity("one",null,"ent/one.ent","test/external-entity.xml");
                addExternalEntity("two",null,"ent/two.ent","test/ent/one.xml");
            }catch(IOException ex){
                // should never happen
            }
        }
        fEntityHandler=null;
        // reset scanner
        //if(fEntityScanner!=null)
        //  fEntityScanner.reset(fSymbolTable, this,fErrorReporter);
    }

    public void addInternalEntity(String name,String text){
        if(!fEntities.containsKey(name)){
            Entity entity=new Entity.InternalEntity(name,text,fInExternalSubset);
            fEntities.put(name,entity);
        }else{
            if(fWarnDuplicateEntityDef){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "MSG_DUPLICATE_ENTITY_DEFINITION",
                        new Object[]{name},
                        XMLErrorReporter.SEVERITY_WARNING);
            }
        }
    } // addInternalEntity(String,String)

    public void addExternalEntity(String name,
                                  String publicId,String literalSystemId,
                                  String baseSystemId) throws IOException{
        if(!fEntities.containsKey(name)){
            if(baseSystemId==null){
                // search for the first external entity on the stack
                int size=fEntityStack.size();
                if(size==0&&fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null){
                    baseSystemId=fCurrentEntity.entityLocation.getExpandedSystemId();
                }
                for(int i=size-1;i>=0;i--){
                    Entity.ScannedEntity externalEntity=
                            (Entity.ScannedEntity)fEntityStack.elementAt(i);
                    if(externalEntity.entityLocation!=null&&externalEntity.entityLocation.getExpandedSystemId()!=null){
                        baseSystemId=externalEntity.entityLocation.getExpandedSystemId();
                        break;
                    }
                }
            }
            Entity entity=new Entity.ExternalEntity(name,
                    new XMLEntityDescriptionImpl(name,publicId,literalSystemId,baseSystemId,
                            expandSystemId(literalSystemId,baseSystemId,false)),null,fInExternalSubset);
            fEntities.put(name,entity);
        }else{
            if(fWarnDuplicateEntityDef){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "MSG_DUPLICATE_ENTITY_DEFINITION",
                        new Object[]{name},
                        XMLErrorReporter.SEVERITY_WARNING);
            }
        }
    } // addExternalEntity(String,String,String,String)

    public String[] getRecognizedFeatures(){
        return (String[])(RECOGNIZED_FEATURES.clone());
    } // getRecognizedFeatures():String[]

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        // xerces features
        if(featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)){
            final int suffixLength=featureId.length()-Constants.XERCES_FEATURE_PREFIX.length();
            if(suffixLength==Constants.ALLOW_JAVA_ENCODINGS_FEATURE.length()&&
                    featureId.endsWith(Constants.ALLOW_JAVA_ENCODINGS_FEATURE)){
                fAllowJavaEncodings=state;
            }
            if(suffixLength==Constants.LOAD_EXTERNAL_DTD_FEATURE.length()&&
                    featureId.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)){
                fLoadExternalDTD=state;
                return;
            }
        }
    } // setFeature(String,boolean)

    public String[] getRecognizedProperties(){
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]
    //
    // Protected methods
    //

    public void setProperty(String propertyId,Object value){
        // Xerces properties
        if(propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.XERCES_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.SYMBOL_TABLE_PROPERTY.length()&&
                    propertyId.endsWith(Constants.SYMBOL_TABLE_PROPERTY)){
                fSymbolTable=(SymbolTable)value;
                return;
            }
            if(suffixLength==Constants.ERROR_REPORTER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.ERROR_REPORTER_PROPERTY)){
                fErrorReporter=(XMLErrorReporter)value;
                return;
            }
            if(suffixLength==Constants.ENTITY_RESOLVER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.ENTITY_RESOLVER_PROPERTY)){
                fEntityResolver=(XMLEntityResolver)value;
                return;
            }
            if(suffixLength==Constants.BUFFER_SIZE_PROPERTY.length()&&
                    propertyId.endsWith(Constants.BUFFER_SIZE_PROPERTY)){
                Integer bufferSize=(Integer)value;
                if(bufferSize!=null&&
                        bufferSize.intValue()>DEFAULT_XMLDECL_BUFFER_SIZE){
                    fBufferSize=bufferSize.intValue();
                    fEntityScanner.setBufferSize(fBufferSize);
                    fBufferPool.setExternalBufferSize(fBufferSize);
                }
            }
            if(suffixLength==Constants.SECURITY_MANAGER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.SECURITY_MANAGER_PROPERTY)){
                fSecurityManager=(XMLSecurityManager)value;
            }
        }
        //JAXP 1.5 properties
        if(propertyId.equals(XML_SECURITY_PROPERTY_MANAGER)){
            XMLSecurityPropertyManager spm=(XMLSecurityPropertyManager)value;
            fAccessExternalDTD=spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        }
    }

    public Boolean getFeatureDefault(String featureId){
        for(int i=0;i<RECOGNIZED_FEATURES.length;i++){
            if(RECOGNIZED_FEATURES[i].equals(featureId)){
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean

    public Object getPropertyDefault(String propertyId){
        for(int i=0;i<RECOGNIZED_PROPERTIES.length;i++){
            if(RECOGNIZED_PROPERTIES[i].equals(propertyId)){
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object

    public void setLimitAnalyzer(XMLLimitAnalyzer fLimitAnalyzer){
        this.fLimitAnalyzer=fLimitAnalyzer;
    }

    public String getPublicId(){
        return (fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null)?fCurrentEntity.entityLocation.getPublicId():null;
    } // getPublicId():String

    public String getExpandedSystemId(){
        if(fCurrentEntity!=null){
            if(fCurrentEntity.entityLocation!=null&&
                    fCurrentEntity.entityLocation.getExpandedSystemId()!=null){
                return fCurrentEntity.entityLocation.getExpandedSystemId();
            }else{
                // search for the first external entity on the stack
                int size=fEntityStack.size();
                for(int i=size-1;i>=0;i--){
                    Entity.ScannedEntity externalEntity=
                            (Entity.ScannedEntity)fEntityStack.elementAt(i);
                    if(externalEntity.entityLocation!=null&&
                            externalEntity.entityLocation.getExpandedSystemId()!=null){
                        return externalEntity.entityLocation.getExpandedSystemId();
                    }
                }
            }
        }
        return null;
    } // getExpandedSystemId():String

    public String getLiteralSystemId(){
        if(fCurrentEntity!=null){
            if(fCurrentEntity.entityLocation!=null&&
                    fCurrentEntity.entityLocation.getLiteralSystemId()!=null){
                return fCurrentEntity.entityLocation.getLiteralSystemId();
            }else{
                // search for the first external entity on the stack
                int size=fEntityStack.size();
                for(int i=size-1;i>=0;i--){
                    Entity.ScannedEntity externalEntity=
                            (Entity.ScannedEntity)fEntityStack.elementAt(i);
                    if(externalEntity.entityLocation!=null&&
                            externalEntity.entityLocation.getLiteralSystemId()!=null){
                        return externalEntity.entityLocation.getLiteralSystemId();
                    }
                }
            }
        }
        return null;
    } // getLiteralSystemId():String
    //
    // Protected static methods
    //

    public int getLineNumber(){
        if(fCurrentEntity!=null){
            if(fCurrentEntity.isExternal()){
                return fCurrentEntity.lineNumber;
            }else{
                // search for the first external entity on the stack
                int size=fEntityStack.size();
                for(int i=size-1;i>0;i--){
                    Entity.ScannedEntity firstExternalEntity=(Entity.ScannedEntity)fEntityStack.elementAt(i);
                    if(firstExternalEntity.isExternal()){
                        return firstExternalEntity.lineNumber;
                    }
                }
            }
        }
        return -1;
    } // getLineNumber():int

    public int getColumnNumber(){
        if(fCurrentEntity!=null){
            if(fCurrentEntity.isExternal()){
                return fCurrentEntity.columnNumber;
            }else{
                // search for the first external entity on the stack
                int size=fEntityStack.size();
                for(int i=size-1;i>0;i--){
                    Entity.ScannedEntity firstExternalEntity=(Entity.ScannedEntity)fEntityStack.elementAt(i);
                    if(firstExternalEntity.isExternal()){
                        return firstExternalEntity.columnNumber;
                    }
                }
            }
        }
        return -1;
    } // getColumnNumber():int

    public void test(){
        //System.out.println("TESTING: Added familytree to entityManager");
        //Usecase1
        fEntityStorage.addExternalEntity("entityUsecase1",null,
                "/space/home/stax/sun/6thJan2004/zephyr/data/test.txt",
                "/space/home/stax/sun/6thJan2004/zephyr/data/entity.xml");
        //Usecase2
        fEntityStorage.addInternalEntity("entityUsecase2","<Test>value</Test>");
        fEntityStorage.addInternalEntity("entityUsecase3","value3");
        fEntityStorage.addInternalEntity("text","Hello World.");
        fEntityStorage.addInternalEntity("empty-element","<foo/>");
        fEntityStorage.addInternalEntity("balanced-element","<foo></foo>");
        fEntityStorage.addInternalEntity("balanced-element-with-text","<foo>Hello, World</foo>");
        fEntityStorage.addInternalEntity("balanced-element-with-entity","<foo>&text;</foo>");
        fEntityStorage.addInternalEntity("unbalanced-entity","<foo>");
        fEntityStorage.addInternalEntity("recursive-entity","<foo>&recursive-entity2;</foo>");
        fEntityStorage.addInternalEntity("recursive-entity2","<bar>&recursive-entity3;</bar>");
        fEntityStorage.addInternalEntity("recursive-entity3","<baz>&recursive-entity;</baz>");
        fEntityStorage.addInternalEntity("ch","&#x00A9;");
        fEntityStorage.addInternalEntity("ch1","&#84;");
        fEntityStorage.addInternalEntity("% ch2","param");
    }

    private static class CharacterBuffer{
        private char[] ch;
        private boolean isExternal;

        public CharacterBuffer(boolean isExternal,int size){
            this.isExternal=isExternal;
            ch=new char[size];
        }
    }

    private static class CharacterBufferPool{
        private static final int DEFAULT_POOL_SIZE=3;
        private CharacterBuffer[] fInternalBufferPool;
        private CharacterBuffer[] fExternalBufferPool;
        private int fExternalBufferSize;
        private int fInternalBufferSize;
        private int poolSize;
        private int fInternalTop;
        private int fExternalTop;

        public CharacterBufferPool(int externalBufferSize,int internalBufferSize){
            this(DEFAULT_POOL_SIZE,externalBufferSize,internalBufferSize);
        }

        public CharacterBufferPool(int poolSize,int externalBufferSize,int internalBufferSize){
            fExternalBufferSize=externalBufferSize;
            fInternalBufferSize=internalBufferSize;
            this.poolSize=poolSize;
            init();
        }

        private void init(){
            fInternalBufferPool=new CharacterBuffer[poolSize];
            fExternalBufferPool=new CharacterBuffer[poolSize];
            fInternalTop=-1;
            fExternalTop=-1;
        }

        public CharacterBuffer getBuffer(boolean external){
            if(external){
                if(fExternalTop>-1){
                    return (CharacterBuffer)fExternalBufferPool[fExternalTop--];
                }else{
                    return new CharacterBuffer(true,fExternalBufferSize);
                }
            }else{
                if(fInternalTop>-1){
                    return (CharacterBuffer)fInternalBufferPool[fInternalTop--];
                }else{
                    return new CharacterBuffer(false,fInternalBufferSize);
                }
            }
        }

        public void returnToPool(CharacterBuffer buffer){
            if(buffer.isExternal){
                if(fExternalTop<fExternalBufferPool.length-1){
                    fExternalBufferPool[++fExternalTop]=buffer;
                }
            }else if(fInternalTop<fInternalBufferPool.length-1){
                fInternalBufferPool[++fInternalTop]=buffer;
            }
        }

        public void setExternalBufferSize(int bufferSize){
            fExternalBufferSize=bufferSize;
            fExternalBufferPool=new CharacterBuffer[poolSize];
            fExternalTop=-1;
        }
    }

    protected final class RewindableInputStream extends InputStream{
        private InputStream fInputStream;
        private byte[] fData;
        private int fStartOffset;
        private int fEndOffset;
        private int fOffset;
        private int fLength;
        private int fMark;

        public RewindableInputStream(InputStream is){
            fData=new byte[DEFAULT_XMLDECL_BUFFER_SIZE];
            fInputStream=is;
            fStartOffset=0;
            fEndOffset=-1;
            fOffset=0;
            fLength=0;
            fMark=0;
        }

        public void setStartOffset(int offset){
            fStartOffset=offset;
        }

        public void rewind(){
            fOffset=fStartOffset;
        }

        public int read() throws IOException{
            int b=0;
            if(fOffset<fLength){
                return fData[fOffset++]&0xff;
            }
            if(fOffset==fEndOffset){
                return -1;
            }
            if(fOffset==fData.length){
                byte[] newData=new byte[fOffset<<1];
                System.arraycopy(fData,0,newData,0,fOffset);
                fData=newData;
            }
            b=fInputStream.read();
            if(b==-1){
                fEndOffset=fOffset;
                return -1;
            }
            fData[fLength++]=(byte)b;
            fOffset++;
            return b&0xff;
        }

        public int read(byte[] b,int off,int len) throws IOException{
            int bytesLeft=fLength-fOffset;
            if(bytesLeft==0){
                if(fOffset==fEndOffset){
                    return -1;
                }
                /**
                 * //System.out.println("fCurrentEntitty = " + fCurrentEntity );
                 * //System.out.println("fInputStream = " + fInputStream );
                 * // better get some more for the voracious reader... */
                if(fCurrentEntity.mayReadChunks||!fCurrentEntity.xmlDeclChunkRead){
                    if(!fCurrentEntity.xmlDeclChunkRead){
                        fCurrentEntity.xmlDeclChunkRead=true;
                        len=Entity.ScannedEntity.DEFAULT_XMLDECL_BUFFER_SIZE;
                    }
                    return fInputStream.read(b,off,len);
                }
                int returnedVal=read();
                if(returnedVal==-1){
                    fEndOffset=fOffset;
                    return -1;
                }
                b[off]=(byte)returnedVal;
                return 1;
            }
            if(len<bytesLeft){
                if(len<=0){
                    return 0;
                }
            }else{
                len=bytesLeft;
            }
            if(b!=null){
                System.arraycopy(fData,fOffset,b,off,len);
            }
            fOffset+=len;
            return len;
        }

        public long skip(long n)
                throws IOException{
            int bytesLeft;
            if(n<=0){
                return 0;
            }
            bytesLeft=fLength-fOffset;
            if(bytesLeft==0){
                if(fOffset==fEndOffset){
                    return 0;
                }
                return fInputStream.skip(n);
            }
            if(n<=bytesLeft){
                fOffset+=n;
                return n;
            }
            fOffset+=bytesLeft;
            if(fOffset==fEndOffset){
                return bytesLeft;
            }
            n-=bytesLeft;
            /**
             * In a manner of speaking, when this class isn't permitting more
             * than one byte at a time to be read, it is "blocking".  The
             * available() method should indicate how much can be read without
             * blocking, so while we're in this mode, it should only indicate
             * that bytes in its buffer are available; otherwise, the result of
             * available() on the underlying InputStream is appropriate.
             */
            return fInputStream.skip(n)+bytesLeft;
        }

        public int available() throws IOException{
            int bytesLeft=fLength-fOffset;
            if(bytesLeft==0){
                if(fOffset==fEndOffset){
                    return -1;
                }
                return fCurrentEntity.mayReadChunks?fInputStream.available()
                        :0;
            }
            return bytesLeft;
        }

        public void mark(int howMuch){
            fMark=fOffset;
        }

        public void reset(){
            fOffset=fMark;
            //test();
        }

        public boolean markSupported(){
            return true;
        }

        public void close() throws IOException{
            if(fInputStream!=null){
                fInputStream.close();
                fInputStream=null;
            }
        }
    } // end of RewindableInputStream class
} // class XMLEntityManager
