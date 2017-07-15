/**
 * Copyright (c) 2005, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.util.*;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.xml.internal.stream.Entity;
import com.sun.xml.internal.stream.StaxErrorReporter;
import com.sun.xml.internal.stream.XMLEntityStorage;
import com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar;
import com.sun.xml.internal.stream.dtd.nonvalidating.XMLNotationDecl;
import com.sun.xml.internal.stream.events.EntityDeclarationImpl;
import com.sun.xml.internal.stream.events.NotationDeclarationImpl;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class XMLStreamReaderImpl implements javax.xml.stream.XMLStreamReader{
    protected static final String ENTITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_MANAGER_PROPERTY;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String READER_IN_DEFINED_STATE=
            Constants.READER_IN_DEFINED_STATE;
    static final boolean DEBUG=false;
    protected XMLDocumentScannerImpl fScanner=new XMLNSDocumentScannerImpl();
    //make Global NamespaceContextWrapper object,  fScanner.getNamespaceContext() is dynamic object and ita value changes
    //as per the state of the parser.
    protected NamespaceContextWrapper fNamespaceContextWrapper=new NamespaceContextWrapper((NamespaceSupport)fScanner.getNamespaceContext());
    protected XMLEntityManager fEntityManager=new XMLEntityManager();
    protected StaxErrorReporter fErrorReporter=new StaxErrorReporter();
    protected XMLEntityScanner fEntityScanner=null;
    protected XMLInputSource fInputSource=null;
    protected PropertyManager fPropertyManager=null;
    private SymbolTable fSymbolTable=new SymbolTable();
    private int fEventType;
    private boolean fReuse=true;
    private boolean fReaderInDefinedState=true;
    private boolean fBindNamespaces=true;
    private String fDTDDecl=null;
    private String versionStr=null;

    public XMLStreamReaderImpl(InputStream inputStream,PropertyManager props) throws XMLStreamException{
        init(props);
        //publicId, systemid, baseSystemId, inputStream, enocding
        XMLInputSource inputSource=new XMLInputSource(null,null,null,inputStream,null);
        //pass the input source to document scanner impl.
        setInputSource(inputSource);
    }

    public void setInputSource(XMLInputSource inputSource) throws XMLStreamException{
        //once setInputSource() is called this instance is busy parsing the inputsource supplied
        //this instances is free for reuse if parser has reached END_DOCUMENT state or application has
        //called close()
        fReuse=false;
        try{
            fScanner.setInputSource(inputSource);
            //XMLStreamReader should be in defined state
            if(fReaderInDefinedState){
                fEventType=fScanner.next();
                if(versionStr==null)
                    versionStr=getVersion();
                if(fEventType==XMLStreamConstants.START_DOCUMENT&&versionStr!=null&&versionStr.equals("1.1")){
                    switchToXML11Scanner();
                }
            }
        }catch(IOException ex){
            throw new XMLStreamException(ex);
        }catch(XNIException ex){ //Issue 56 XNIException not caught
            throw new XMLStreamException(ex.getMessage(),getLocation(),ex.getException());
        }
    }//setInputSource

    private void switchToXML11Scanner() throws IOException{
        int oldEntityDepth=fScanner.fEntityDepth;
        com.sun.org.apache.xerces.internal.xni.NamespaceContext oldNamespaceContext=fScanner.fNamespaceContext;
        fScanner=new XML11NSDocumentScannerImpl();
        //get the new scanner state to old scanner's previous state
        fScanner.reset(fPropertyManager);
        fScanner.setPropertyManager(fPropertyManager);
        fEntityScanner=(XMLEntityScanner)fEntityManager.getEntityScanner();
        fEntityManager.fCurrentEntity.mayReadChunks=true;
        fScanner.setScannerState(XMLEvent.START_DOCUMENT);
        fScanner.fEntityDepth=oldEntityDepth;
        fScanner.fNamespaceContext=oldNamespaceContext;
        fEventType=fScanner.next();
    }

    void init(PropertyManager propertyManager) throws XMLStreamException{
        fPropertyManager=propertyManager;
        //set Stax internal properties -- Note that these instances are being created in XMLReaderImpl.
        //1.SymbolTable
        //2.XMLMessageFormatter
        //3.XMLEntityManager
        //4. call reset()
        //1.
        propertyManager.setProperty(SYMBOL_TABLE,fSymbolTable);
        //2.
        propertyManager.setProperty(ERROR_REPORTER,fErrorReporter);
        //3.
        propertyManager.setProperty(ENTITY_MANAGER,fEntityManager);
        //4.
        reset();
    }

    public void reset(){
        fReuse=true;
        fEventType=0;
        //reset entity manager
        fEntityManager.reset(fPropertyManager);
        //reset the scanner
        fScanner.reset(fPropertyManager);
        //REVISIT:this is too ugly -- we are getting XMLEntityManager and XMLEntityReader from
        //property manager, it should be only XMLEntityManager
        fDTDDecl=null;
        fEntityScanner=(XMLEntityScanner)fEntityManager.getEntityScanner();
        //default value for this property is true. However, this should be false when using XMLEventReader... Ugh..
        //because XMLEventReader should not have defined state.
        fReaderInDefinedState=((Boolean)fPropertyManager.getProperty(READER_IN_DEFINED_STATE)).booleanValue();
        fBindNamespaces=((Boolean)fPropertyManager.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE)).booleanValue();
        versionStr=null;
    }

    public XMLStreamReaderImpl(String systemid,PropertyManager props) throws XMLStreamException{
        init(props);
        //publicId, systemid, baseSystemId, inputStream, enocding
        XMLInputSource inputSource=new XMLInputSource(null,systemid,null);
        //pass the input source to document scanner impl.
        setInputSource(inputSource);
    }

    public XMLStreamReaderImpl(InputStream inputStream,String encoding,PropertyManager props) throws XMLStreamException{
        init(props);
        //publicId, systemid, baseSystemId, inputStream, enocding
        XMLInputSource inputSource=new XMLInputSource(null,null,null,new BufferedInputStream(inputStream),encoding);
        //pass the input source to document scanner impl.
        setInputSource(inputSource);
    }

    public XMLStreamReaderImpl(Reader reader,PropertyManager props) throws XMLStreamException{
        init(props);
        //publicId, systemid, baseSystemId, inputStream, enocding
        //xxx: Using buffered reader
        XMLInputSource inputSource=new XMLInputSource(null,null,null,new BufferedReader(reader),null);
        //pass the input source to document scanner impl.
        setInputSource(inputSource);
    }

    public XMLStreamReaderImpl(XMLInputSource inputSource,PropertyManager props) throws XMLStreamException{
        init(props);
        //pass the input source to document scanner impl.
        setInputSource(inputSource);
    }

    public XMLDocumentScannerImpl getScanner(){
        System.out.println("returning scanner");
        return fScanner;
    }

    public boolean canReuse(){
        if(DEBUG){
            System.out.println("fReuse = "+fReuse);
            System.out.println("fEventType = "+getEventTypeString(fEventType));
        }
        //when parsing begins, fReuse is set to false
        //fReuse is set to 'true' when application calls close()
        return fReuse;
    }

    final static String getEventTypeString(int eventType){
        switch(eventType){
            case XMLEvent.START_ELEMENT:
                return "START_ELEMENT";
            case XMLEvent.END_ELEMENT:
                return "END_ELEMENT";
            case XMLEvent.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLEvent.CHARACTERS:
                return "CHARACTERS";
            case XMLEvent.COMMENT:
                return "COMMENT";
            case XMLEvent.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLEvent.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLEvent.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLEvent.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLEvent.DTD:
                return "DTD";
            case XMLEvent.CDATA:
                return "CDATA";
            case XMLEvent.SPACE:
                return "SPACE";
        }
        return "UNKNOWN_EVENT_TYPE, "+String.valueOf(eventType);
    }

    public int getColumnNumber(){
        return fEntityScanner.getColumnNumber();
    }//getColumnNumber

    public int getLineNumber(){
        return fEntityScanner.getLineNumber();
    }//getLineNumber

    public String getValue(){
        if(fEventType==XMLEvent.PROCESSING_INSTRUCTION){
            return fScanner.getPIData().toString();
        }else if(fEventType==XMLEvent.COMMENT){
            return fScanner.getComment();
        }else if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT){
            return fScanner.getElementQName().localpart;
        }else if(fEventType==XMLEvent.CHARACTERS){
            return fScanner.getCharacterData().toString();
        }
        return null;
    }//getValue()

    public boolean hasAttributes(){
        return fScanner.getAttributeIterator().getLength()>0?true:false;
    }

    public boolean hasValue(){
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT
                ||fEventType==XMLEvent.ENTITY_REFERENCE||fEventType==XMLEvent.PROCESSING_INSTRUCTION
                ||fEventType==XMLEvent.COMMENT||fEventType==XMLEvent.CHARACTERS){
            return true;
        }else{
            return false;
        }
    }

    public QName getAttributeQName(int index){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            // create new object at runtime..
            String localName=fScanner.getAttributeIterator().getLocalName(index);
            String uri=fScanner.getAttributeIterator().getURI(index);
            return new QName(uri,localName);
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributeQName()");
        }
    }//getAttributeQName

    public Object getProperty(String name) throws IllegalArgumentException{
        if(name==null) throw new IllegalArgumentException();
        if(fPropertyManager!=null){
            if(name.equals(fPropertyManager.STAX_NOTATIONS)){
                return getNotationDecls();
            }else if(name.equals(fPropertyManager.STAX_ENTITIES)){
                return getEntityDecls();
            }else
                return fPropertyManager.getProperty(name);
        }
        return null;
    }

    public int next() throws XMLStreamException{
        if(!hasNext()){
            if(fEventType!=-1){
                throw new java.util.NoSuchElementException("END_DOCUMENT reached: no more elements on the stream.");
            }else{
                throw new XMLStreamException("Error processing input source. The input stream is not complete.");
            }
        }
        try{
            fEventType=fScanner.next();
            if(versionStr==null){
                versionStr=getVersion();
            }
            if(fEventType==XMLStreamConstants.START_DOCUMENT
                    &&versionStr!=null
                    &&versionStr.equals("1.1")){
                switchToXML11Scanner();
            }
            if(fEventType==XMLStreamConstants.CHARACTERS||
                    fEventType==XMLStreamConstants.ENTITY_REFERENCE||
                    fEventType==XMLStreamConstants.PROCESSING_INSTRUCTION||
                    fEventType==XMLStreamConstants.COMMENT||
                    fEventType==XMLStreamConstants.CDATA){
                fEntityScanner.checkNodeCount(fEntityScanner.fCurrentEntity);
            }
            return fEventType;
        }catch(IOException ex){
            // if this error occured trying to resolve the external DTD subset
            // and IS_VALIDATING == false, then this is not an XML error
            if(fScanner.fScannerState==fScanner.SCANNER_STATE_DTD_EXTERNAL){
                Boolean isValidating=(Boolean)fPropertyManager.getProperty(
                        XMLInputFactory.IS_VALIDATING);
                if(isValidating!=null
                        &&!isValidating.booleanValue()){
                    // ignore the error, set scanner to known state
                    fEventType=XMLEvent.DTD;
                    fScanner.setScannerState(fScanner.SCANNER_STATE_PROLOG);
                    fScanner.setDriver(fScanner.fPrologDriver);
                    if(fDTDDecl==null
                            ||fDTDDecl.length()==0){
                        fDTDDecl="<!-- "
                                +"Exception scanning External DTD Subset.  "
                                +"True contents of DTD cannot be determined.  "
                                +"Processing will continue as XMLInputFactory.IS_VALIDATING == false."
                                +" -->";
                    }
                    return XMLEvent.DTD;
                }
            }
            // else real error
            throw new XMLStreamException(ex.getMessage(),getLocation(),ex);
        }catch(XNIException ex){
            throw new XMLStreamException(
                    ex.getMessage(),
                    getLocation(),
                    ex.getException());
        }
    } //next()

    public void require(int type,String namespaceURI,String localName) throws XMLStreamException{
        if(type!=fEventType)
            throw new XMLStreamException("Event type "+getEventTypeString(type)+" specified did "+
                    "not match with current parser event "+getEventTypeString(fEventType));
        if(namespaceURI!=null&&!namespaceURI.equals(getNamespaceURI()))
            throw new XMLStreamException("Namespace URI "+namespaceURI+" specified did not match "+
                    "with current namespace URI");
        if(localName!=null&&!localName.equals(getLocalName()))
            throw new XMLStreamException("LocalName "+localName+" specified did not match with "+
                    "current local name");
        return;
    }

    public String getElementText() throws XMLStreamException{
        if(getEventType()!=XMLStreamConstants.START_ELEMENT){
            throw new XMLStreamException(
                    "parser must be on START_ELEMENT to read next text",getLocation());
        }
        int eventType=next();
        StringBuffer content=new StringBuffer();
        while(eventType!=XMLStreamConstants.END_ELEMENT){
            if(eventType==XMLStreamConstants.CHARACTERS
                    ||eventType==XMLStreamConstants.CDATA
                    ||eventType==XMLStreamConstants.SPACE
                    ||eventType==XMLStreamConstants.ENTITY_REFERENCE){
                content.append(getText());
            }else if(eventType==XMLStreamConstants.PROCESSING_INSTRUCTION
                    ||eventType==XMLStreamConstants.COMMENT){
                // skipping
            }else if(eventType==XMLStreamConstants.END_DOCUMENT){
                throw new XMLStreamException("unexpected end of document when reading element text content");
            }else if(eventType==XMLStreamConstants.START_ELEMENT){
                throw new XMLStreamException(
                        "elementGetText() function expects text only elment but START_ELEMENT was encountered.",getLocation());
            }else{
                throw new XMLStreamException(
                        "Unexpected event type "+eventType,getLocation());
            }
            eventType=next();
        }
        return content.toString();
    }

    public int nextTag() throws XMLStreamException{
        int eventType=next();
        while((eventType==XMLStreamConstants.CHARACTERS&&isWhiteSpace()) // skip whitespace
                ||(eventType==XMLStreamConstants.CDATA&&isWhiteSpace())
                // skip whitespace
                ||eventType==XMLStreamConstants.SPACE
                ||eventType==XMLStreamConstants.PROCESSING_INSTRUCTION
                ||eventType==XMLStreamConstants.COMMENT
                ){
            eventType=next();
        }
        if(eventType!=XMLStreamConstants.START_ELEMENT&&eventType!=XMLStreamConstants.END_ELEMENT){
            throw new XMLStreamException(
                    "found: "+getEventTypeString(eventType)
                            +", expected "+getEventTypeString(XMLStreamConstants.START_ELEMENT)
                            +" or "+getEventTypeString(XMLStreamConstants.END_ELEMENT),
                    getLocation());
        }
        return eventType;
    }

    public boolean hasNext() throws XMLStreamException{
        //the scanner returns -1 when it detects a broken stream
        if(fEventType==-1) return false;
        //we can check in scanners if the scanner state is not set to
        //terminating, we still have more events.
        return fEventType!=XMLEvent.END_DOCUMENT;
    }

    public void close() throws XMLStreamException{
        //xxx: Check what this function is intended to do.
        //reset();
        fReuse=true;
    }

    public String getNamespaceURI(String prefix){
        if(prefix==null) throw new IllegalArgumentException("prefix cannot be null.");
        //first add the string to symbol table.. since internally identity comparisons are done.
        return fScanner.getNamespaceContext().getURI(fSymbolTable.addSymbol(prefix));
    }

    public boolean isStartElement(){
        return fEventType==XMLEvent.START_ELEMENT;
    }

    public boolean isEndElement(){
        return fEventType==XMLEvent.END_ELEMENT;
    }

    public boolean isCharacters(){
        return fEventType==XMLEvent.CHARACTERS;
    }

    public boolean isWhiteSpace(){
        if(isCharacters()||(fEventType==XMLStreamConstants.CDATA)){
            char[] ch=this.getTextCharacters();
            final int start=this.getTextStart();
            final int end=start+this.getTextLength();
            for(int i=start;i<end;i++){
                if(!XMLChar.isSpace(ch[i])){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String getAttributeValue(String namespaceURI,String localName){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            XMLAttributesImpl attributes=fScanner.getAttributeIterator();
            if(namespaceURI==null){ //sjsxp issue 70
                return attributes.getValue(attributes.getIndexByLocalName(localName));
            }else{
                return fScanner.getAttributeIterator().getValue(
                        namespaceURI.length()==0?null:namespaceURI,localName);
            }
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributeValue()");
        }
    }

    public int getAttributeCount(){
        //xxx: recognize SAX properties namespace, namespace-prefix to get XML Namespace declarations
        //does length includes namespace declarations ?
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            return fScanner.getAttributeIterator().getLength();
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributeCount()");
        }
    }//getAttributeCount

    public QName getAttributeName(int index){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            return convertXNIQNametoJavaxQName(fScanner.getAttributeIterator().getQualifiedName(index));
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributeName()");
        }
    }//getAttributeName

    public String getAttributeNamespace(int index){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            return fScanner.getAttributeIterator().getURI(index);
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributeNamespace()");
        }
    }//getAttributeNamespace

    public String getAttributeLocalName(int index){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            return fScanner.getAttributeIterator().getLocalName(index);
        }else{
            throw new IllegalStateException();
        }
    }//getAttributeName

    public String getAttributePrefix(int index){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            return fScanner.getAttributeIterator().getPrefix(index);
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributePrefix()");
        }
    }//getAttributePrefix

    public String getAttributeType(int index){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            return fScanner.getAttributeIterator().getType(index);
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributeType()");
        }
    }//getAttributeType

    public String getAttributeValue(int index){
        //State should be either START_ELEMENT or ATTRIBUTE
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.ATTRIBUTE){
            return fScanner.getAttributeIterator().getValue(index);
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for getAttributeValue()");
        }
    }//getAttributeValue

    public boolean isAttributeSpecified(int index){
        //check that current state should be either START_ELEMENT or ATTRIBUTE
        if((fEventType==XMLEvent.START_ELEMENT)||(fEventType==XMLEvent.ATTRIBUTE)){
            return fScanner.getAttributeIterator().isSpecified(index);
        }else{
            throw new IllegalStateException("Current state is not among the states "
                    +getEventTypeString(XMLEvent.START_ELEMENT)+" , "
                    +getEventTypeString(XMLEvent.ATTRIBUTE)
                    +"valid for isAttributeSpecified()");
        }
    }

    public int getNamespaceCount(){
        //namespaceContext is dynamic object.
        //REVISIT: check if it specifies all conditions mentioned in the javadoc
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT||fEventType==XMLEvent.NAMESPACE){
            return fScanner.getNamespaceContext().getDeclaredPrefixCount();
        }else{
            throw new IllegalStateException("Current event state is "+getEventTypeString(fEventType)
                    +" is not among the states "+getEventTypeString(XMLEvent.START_ELEMENT)
                    +", "+getEventTypeString(XMLEvent.END_ELEMENT)+", "
                    +getEventTypeString(XMLEvent.NAMESPACE)
                    +" valid for getNamespaceCount().");
        }
    }

    public String getNamespacePrefix(int index){
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT||fEventType==XMLEvent.NAMESPACE){
            //namespaceContext is dynamic object.
            String prefix=fScanner.getNamespaceContext().getDeclaredPrefixAt(index);
            return prefix.equals("")?null:prefix;
        }else{
            throw new IllegalStateException("Current state "+getEventTypeString(fEventType)
                    +" is not among the states "+getEventTypeString(XMLEvent.START_ELEMENT)
                    +", "+getEventTypeString(XMLEvent.END_ELEMENT)+", "
                    +getEventTypeString(XMLEvent.NAMESPACE)
                    +" valid for getNamespacePrefix().");
        }
    }

    public String getNamespaceURI(int index){
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT||fEventType==XMLEvent.NAMESPACE){
            //namespaceContext is dynamic object.
            return fScanner.getNamespaceContext().getURI(fScanner.getNamespaceContext().getDeclaredPrefixAt(index));
        }else{
            throw new IllegalStateException("Current state "+getEventTypeString(fEventType)
                    +" is not among the states "+getEventTypeString(XMLEvent.START_ELEMENT)
                    +", "+getEventTypeString(XMLEvent.END_ELEMENT)+", "
                    +getEventTypeString(XMLEvent.NAMESPACE)
                    +" valid for getNamespaceURI().");
        }
    }

    public NamespaceContext getNamespaceContext(){
        return fNamespaceContextWrapper;
    }

    public int getEventType(){
        return fEventType;
    }//getEventType

    public String getText(){
        if(fEventType==XMLEvent.CHARACTERS||fEventType==XMLEvent.COMMENT
                ||fEventType==XMLEvent.CDATA||fEventType==XMLEvent.SPACE){
            //this requires creation of new string
            //fEventType == XMLEvent.ENTITY_REFERENCE
            return fScanner.getCharacterData().toString();
        }else if(fEventType==XMLEvent.ENTITY_REFERENCE){
            String name=fScanner.getEntityName();
            if(name!=null){
                if(fScanner.foundBuiltInRefs)
                    return fScanner.getCharacterData().toString();
                XMLEntityStorage entityStore=fEntityManager.getEntityStore();
                Entity en=entityStore.getEntity(name);
                if(en==null)
                    return null;
                if(en.isExternal())
                    return ((Entity.ExternalEntity)en).entityLocation.getExpandedSystemId();
                else
                    return ((Entity.InternalEntity)en).text;
            }else
                return null;
        }else if(fEventType==XMLEvent.DTD){
            if(fDTDDecl!=null){
                return fDTDDecl;
            }
            XMLStringBuffer tmpBuffer=fScanner.getDTDDecl();
            fDTDDecl=tmpBuffer.toString();
            return fDTDDecl;
        }else{
            throw new IllegalStateException("Current state "+getEventTypeString(fEventType)
                    +" is not among the states"+getEventTypeString(XMLEvent.CHARACTERS)+", "
                    +getEventTypeString(XMLEvent.COMMENT)+", "
                    +getEventTypeString(XMLEvent.CDATA)+", "
                    +getEventTypeString(XMLEvent.SPACE)+", "
                    +getEventTypeString(XMLEvent.ENTITY_REFERENCE)+", "
                    +getEventTypeString(XMLEvent.DTD)+" valid for getText() ");
        }
    }//getText

    public char[] getTextCharacters(){
        if(fEventType==XMLEvent.CHARACTERS||fEventType==XMLEvent.COMMENT
                ||fEventType==XMLEvent.CDATA||fEventType==XMLEvent.SPACE){
            return fScanner.getCharacterData().ch;
        }else{
            throw new IllegalStateException("Current state = "+getEventTypeString(fEventType)
                    +" is not among the states "+getEventTypeString(XMLEvent.CHARACTERS)+" , "
                    +getEventTypeString(XMLEvent.COMMENT)+" , "+getEventTypeString(XMLEvent.CDATA)
                    +" , "+getEventTypeString(XMLEvent.SPACE)+" valid for getTextCharacters() ");
        }
    }

    public int getTextCharacters(int sourceStart,char[] target,int targetStart,int length) throws XMLStreamException{
        if(target==null){
            throw new NullPointerException("target char array can't be null");
        }
        if(targetStart<0||length<0||sourceStart<0||targetStart>=target.length||
                (targetStart+length)>target.length){
            throw new IndexOutOfBoundsException();
        }
        //getTextStart() + sourceStart should not be greater than the lenght of number of characters
        //present
        int copiedLength=0;
        //int presentDataLen = getTextLength() - (getTextStart()+sourceStart);
        int available=getTextLength()-sourceStart;
        if(available<0){
            throw new IndexOutOfBoundsException("sourceStart is greater than"+
                    "number of characters associated with this event");
        }
        if(available<length){
            copiedLength=available;
        }else{
            copiedLength=length;
        }
        System.arraycopy(getTextCharacters(),getTextStart()+sourceStart,target,targetStart,copiedLength);
        return copiedLength;
    }

    public int getTextStart(){
        if(fEventType==XMLEvent.CHARACTERS||fEventType==XMLEvent.COMMENT||fEventType==XMLEvent.CDATA||fEventType==XMLEvent.SPACE){
            return fScanner.getCharacterData().offset;
        }else{
            throw new IllegalStateException("Current state = "+getEventTypeString(fEventType)
                    +" is not among the states "+getEventTypeString(XMLEvent.CHARACTERS)+" , "
                    +getEventTypeString(XMLEvent.COMMENT)+" , "+getEventTypeString(XMLEvent.CDATA)
                    +" , "+getEventTypeString(XMLEvent.SPACE)+" valid for getTextStart() ");
        }
    }

    public int getTextLength(){
        if(fEventType==XMLEvent.CHARACTERS||fEventType==XMLEvent.COMMENT
                ||fEventType==XMLEvent.CDATA||fEventType==XMLEvent.SPACE){
            return fScanner.getCharacterData().length;
        }else{
            throw new IllegalStateException("Current state = "+getEventTypeString(fEventType)
                    +" is not among the states "+getEventTypeString(XMLEvent.CHARACTERS)+" , "
                    +getEventTypeString(XMLEvent.COMMENT)+" , "+getEventTypeString(XMLEvent.CDATA)
                    +" , "+getEventTypeString(XMLEvent.SPACE)+" valid for getTextLength() ");
        }
    }

    public String getEncoding(){
        return fEntityScanner.getEncoding();
    }//getEncoding

    public boolean hasText(){
        if(DEBUG) pr("XMLReaderImpl#EVENT TYPE = "+fEventType);
        if(fEventType==XMLEvent.CHARACTERS||fEventType==XMLEvent.COMMENT||fEventType==XMLEvent.CDATA){
            return fScanner.getCharacterData().length>0;
        }else if(fEventType==XMLEvent.ENTITY_REFERENCE){
            String name=fScanner.getEntityName();
            if(name!=null){
                if(fScanner.foundBuiltInRefs)
                    return true;
                XMLEntityStorage entityStore=fEntityManager.getEntityStore();
                Entity en=entityStore.getEntity(name);
                if(en==null)
                    return false;
                if(en.isExternal()){
                    return ((Entity.ExternalEntity)en).entityLocation.getExpandedSystemId()!=null;
                }else{
                    return ((Entity.InternalEntity)en).text!=null;
                }
            }else
                return false;
        }else{
            if(fEventType==XMLEvent.DTD)
                return fScanner.fSeenDoctypeDecl;
        }
        return false;
    }

    public Location getLocation(){
        return new Location(){
            String _systemId=fEntityScanner.getExpandedSystemId();
            String _publicId=fEntityScanner.getPublicId();
            int _offset=fEntityScanner.getCharacterOffset();
            int _columnNumber=fEntityScanner.getColumnNumber();
            int _lineNumber=fEntityScanner.getLineNumber();

            public String toString(){
                StringBuffer sbuffer=new StringBuffer();
                sbuffer.append("Line number = "+getLineNumber());
                sbuffer.append("\n");
                sbuffer.append("Column number = "+getColumnNumber());
                sbuffer.append("\n");
                sbuffer.append("System Id = "+getSystemId());
                sbuffer.append("\n");
                sbuffer.append("Public Id = "+getPublicId());
                sbuffer.append("\n");
                sbuffer.append("Location Uri= "+getLocationURI());
                sbuffer.append("\n");
                sbuffer.append("CharacterOffset = "+getCharacterOffset());
                sbuffer.append("\n");
                return sbuffer.toString();
            }

            public String getLocationURI(){
                return _systemId;
            }

            public int getLineNumber(){
                return _lineNumber;
            }

            public int getColumnNumber(){
                return _columnNumber;
            }

            public int getCharacterOffset(){
                return _offset;
            }

            public String getPublicId(){
                return _publicId;
            }

            public String getSystemId(){
                return _systemId;
            }
        };
    }

    public QName getName(){
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT)
            return convertXNIQNametoJavaxQName(fScanner.getElementQName());
        else
            throw new IllegalStateException("Illegal to call getName() "+
                    "when event type is "+getEventTypeString(fEventType)+"."
                    +" Valid states are "+getEventTypeString(XMLEvent.START_ELEMENT)+", "
                    +getEventTypeString(XMLEvent.END_ELEMENT));
    }

    public String getLocalName(){
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT){
            //xxx check whats the value of fCurrentElement
            return fScanner.getElementQName().localpart;
        }else if(fEventType==XMLEvent.ENTITY_REFERENCE){
            return fScanner.getEntityName();
        }
        throw new IllegalStateException("Method getLocalName() cannot be called for "+
                getEventTypeString(fEventType)+" event.");
    }//getLocalName()

    public boolean hasName(){
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT){
            return true;
        }else{
            return false;
        }
    }//hasName()

    public String getNamespaceURI(){
        //doesn't take care of Attribute as separte event
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT){
            return fScanner.getElementQName().uri;
        }
        return null;
    }//getNamespaceURI

    public String getPrefix(){
        if(fEventType==XMLEvent.START_ELEMENT||fEventType==XMLEvent.END_ELEMENT){
            String prefix=fScanner.getElementQName().prefix;
            return prefix==null?XMLConstants.DEFAULT_NS_PREFIX:prefix;
        }
        return null;
    }//getPrefix()

    public String getVersion(){
        //apply SAP's patch: the default version in the scanner was set to 1.0 because of DOM and SAX
        //so this patch is a workaround of the difference between StAX and DOM
        // SAPJVM: Return null if the XML version has not been declared (as specified in the JavaDoc).
        String version=fEntityScanner.getXMLVersion();
        return "1.0".equals(version)&&!fEntityScanner.xmlVersionSetExplicitly?null:version;
    }

    public boolean isStandalone(){
        return fScanner.isStandAlone();
    }

    public boolean standaloneSet(){
        //xxx: it requires if the standalone was set in the document ? This is different that if the document
        // is standalone
        return fScanner.standaloneSet();
    }

    public String getCharacterEncodingScheme(){
        return fScanner.getCharacterEncodingScheme();
    }

    public String getPITarget(){
        if(fEventType==XMLEvent.PROCESSING_INSTRUCTION){
            return fScanner.getPITarget();
        }else throw new IllegalStateException("Current state of the parser is "+getEventTypeString(fEventType)+
                " But Expected state is "+XMLEvent.PROCESSING_INSTRUCTION);
    }//getPITarget

    public String getPIData(){
        if(fEventType==XMLEvent.PROCESSING_INSTRUCTION){
            return fScanner.getPIData().toString();
        }else throw new IllegalStateException("Current state of the parser is "+getEventTypeString(fEventType)+
                " But Expected state is "+XMLEvent.PROCESSING_INSTRUCTION);
    }//getPIData

    static void pr(String str){
        System.out.println(str);
    }

    public QName convertXNIQNametoJavaxQName(com.sun.org.apache.xerces.internal.xni.QName qname){
        if(qname==null) return null;
        //xxx: prefix definition ?
        if(qname.prefix==null){
            return new QName(qname.uri,qname.localpart);
        }else{
            return new QName(qname.uri,qname.localpart,qname.prefix);
        }
    }

    protected List getEntityDecls(){
        if(fEventType==XMLStreamConstants.DTD){
            XMLEntityStorage entityStore=fEntityManager.getEntityStore();
            ArrayList list=null;
            if(entityStore.hasEntities()){
                EntityDeclarationImpl decl=null;
                list=new ArrayList(entityStore.getEntitySize());
                Enumeration enu=entityStore.getEntityKeys();
                while(enu.hasMoreElements()){
                    String key=(String)enu.nextElement();
                    Entity en=(Entity)entityStore.getEntity(key);
                    decl=new EntityDeclarationImpl();
                    decl.setEntityName(key);
                    if(en.isExternal()){
                        decl.setXMLResourceIdentifier(((Entity.ExternalEntity)en).entityLocation);
                        decl.setNotationName(((Entity.ExternalEntity)en).notation);
                    }else
                        decl.setEntityReplacementText(((Entity.InternalEntity)en).text);
                    list.add(decl);
                }
            }
            return list;
        }
        return null;
    }

    protected List getNotationDecls(){
        if(fEventType==XMLStreamConstants.DTD){
            if(fScanner.fDTDScanner==null) return null;
            DTDGrammar grammar=((XMLDTDScannerImpl)(fScanner.fDTDScanner)).getGrammar();
            if(grammar==null) return null;
            List notations=grammar.getNotationDecls();
            Iterator it=notations.iterator();
            ArrayList list=new ArrayList();
            while(it.hasNext()){
                XMLNotationDecl ni=(XMLNotationDecl)it.next();
                if(ni!=null){
                    list.add(new NotationDeclarationImpl(ni));
                }
            }
            return list;
        }
        return null;
    }

    protected PropertyManager getPropertyManager(){
        return fPropertyManager;
    }

    //xxx: this function is not being used.
    protected void setPropertyManager(PropertyManager propertyManager){
        fPropertyManager=propertyManager;
        //REVISIT: we were supplying hashmap ealier
        fScanner.setProperty("stax-properties",propertyManager);
        fScanner.setPropertyManager(propertyManager);
    }
}//XMLReaderImpl
