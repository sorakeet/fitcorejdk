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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.XMLScanner.NameType;
import com.sun.org.apache.xerces.internal.impl.io.ASCIIReader;
import com.sun.org.apache.xerces.internal.impl.io.UCSReader;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.util.EncodingMap;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager.Limit;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.xml.internal.stream.Entity.ScannedEntity;
import com.sun.xml.internal.stream.XMLBufferListener;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class XMLEntityScanner implements XMLLocator{
    //Will be used only during internal subsets.
    //for appending data.
    protected static final String SYMBOL_TABLE=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ALLOW_JAVA_ENCODINGS=
            Constants.XERCES_FEATURE_PREFIX+Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    private static final boolean DEBUG_ENCODINGS=false;
    private static final boolean[] VALID_NAMES=new boolean[127];
    private static final boolean DEBUG_BUFFER=false;
    private static final boolean DEBUG_SKIP_STRING=false;
    private static final EOFException END_OF_DOCUMENT_ENTITY=new EOFException(){
        private static final long serialVersionUID=980337771224675268L;

        public Throwable fillInStackTrace(){
            return this;
        }
    };

    static{
        for(int i=0x0041;i<=0x005A;i++){
            VALID_NAMES[i]=true;
        }
        for(int i=0x0061;i<=0x007A;i++){
            VALID_NAMES[i]=true;
        }
        for(int i=0x0030;i<=0x0039;i++){
            VALID_NAMES[i]=true;
        }
        VALID_NAMES[45]=true;
        VALID_NAMES[46]=true;
        VALID_NAMES[58]=true;
        VALID_NAMES[95]=true;
    }

    protected ScannedEntity fCurrentEntity=null;
    protected int fBufferSize=XMLEntityManager.DEFAULT_BUFFER_SIZE;
    protected XMLEntityManager fEntityManager;
    protected XMLSecurityManager fSecurityManager=null;
    protected XMLLimitAnalyzer fLimitAnalyzer=null;
    protected SymbolTable fSymbolTable=null;
    protected XMLErrorReporter fErrorReporter=null;
    protected boolean fAllowJavaEncodings;
    protected PropertyManager fPropertyManager=null;
    // Remember, that the XML version has explicitly been set,
    // so that XMLStreamReader.getVersion() can find that out.
    protected boolean xmlVersionSetExplicitly=false;
    int[] whiteSpaceLookup=new int[100];
    int whiteSpaceLen=0;
    boolean whiteSpaceInfoNeeded=true;
    boolean isExternal=false;
    // indicates that the operation is for detecting XML version
    boolean detectingVersion=false;
    private ArrayList<XMLBufferListener> listeners=new ArrayList<>();
    //
    // Constructors
    //

    public XMLEntityScanner(){
    } // <init>()

    public XMLEntityScanner(PropertyManager propertyManager,XMLEntityManager entityManager){
        fEntityManager=entityManager;
        reset(propertyManager);
    } // <init>()

    public void reset(PropertyManager propertyManager){
        fSymbolTable=(SymbolTable)propertyManager.getProperty(SYMBOL_TABLE);
        fErrorReporter=(XMLErrorReporter)propertyManager.getProperty(ERROR_REPORTER);
        resetCommon();
    }

    private void resetCommon(){
        fCurrentEntity=null;
        whiteSpaceLen=0;
        whiteSpaceInfoNeeded=true;
        listeners.clear();
        fLimitAnalyzer=fEntityManager.fLimitAnalyzer;
        fSecurityManager=fEntityManager.fSecurityManager;
    }

    // set buffer size:
    public final void setBufferSize(int size){
        // REVISIT: Buffer size passed to entity scanner
        // was not being kept in synch with the actual size
        // of the buffers in each scanned entity. If any
        // of the buffers were actually resized, it was possible
        // that the parser would throw an ArrayIndexOutOfBoundsException
        // for documents which contained names which are longer than
        // the current buffer size. Conceivably the buffer size passed
        // to entity scanner could be used to determine a minimum size
        // for resizing, if doubling its size is smaller than this
        // minimum. -- mrglavas
        fBufferSize=size;
    }

    public void reset(XMLComponentManager componentManager)
            throws XMLConfigurationException{
        // xerces features
        fAllowJavaEncodings=componentManager.getFeature(ALLOW_JAVA_ENCODINGS,false);
        //xerces properties
        fSymbolTable=(SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        fErrorReporter=(XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        resetCommon();
    } // reset(XMLComponentManager)

    public final void reset(SymbolTable symbolTable,XMLEntityManager entityManager,
                            XMLErrorReporter reporter){
        fCurrentEntity=null;
        fSymbolTable=symbolTable;
        fEntityManager=entityManager;
        fErrorReporter=reporter;
        fLimitAnalyzer=fEntityManager.fLimitAnalyzer;
        fSecurityManager=fEntityManager.fSecurityManager;
    }

    public ScannedEntity getCurrentEntity(){
        return fCurrentEntity;
    }

    public final void setCurrentEntity(ScannedEntity scannedEntity){
        fCurrentEntity=scannedEntity;
        if(fCurrentEntity!=null){
            isExternal=fCurrentEntity.isExternal();
            if(DEBUG_BUFFER)
                System.out.println("Current Entity is "+scannedEntity.name);
        }
    }

    public final String getPublicId(){
        return (fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null)?fCurrentEntity.entityLocation.getPublicId():null;
    }

    public final String getLiteralSystemId(){
        return (fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null)?fCurrentEntity.entityLocation.getLiteralSystemId():null;
    }
    //
    // XMLEntityReader methods
    //

    public final String getBaseSystemId(){
        return (fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null)?fCurrentEntity.entityLocation.getExpandedSystemId():null;
    } // getBaseSystemId():String

    public void setBaseSystemId(String systemId){
        //no-op
    }

    public final String getExpandedSystemId(){
        return (fCurrentEntity!=null&&fCurrentEntity.entityLocation!=null)?fCurrentEntity.entityLocation.getExpandedSystemId():null;
    }

    ///////////// Locator methods start.
    public final int getLineNumber(){
        //if the entity is closed, we should return -1
        //xxx at first place why such call should be there...
        return fCurrentEntity!=null?fCurrentEntity.lineNumber:-1;
    }

    public void setLineNumber(int line){
        //no-op
    }

    public final int getColumnNumber(){
        //if the entity is closed, we should return -1
        //xxx at first place why such call should be there...
        return fCurrentEntity!=null?fCurrentEntity.columnNumber:-1;
    }

    public void setColumnNumber(int col){
        // no-op
    }

    public final int getCharacterOffset(){
        return fCurrentEntity!=null?fCurrentEntity.fTotalCountTillLastLoad+fCurrentEntity.position:-1;
    }

    public final String getEncoding(){
        if(fCurrentEntity!=null){
            return fCurrentEntity.encoding;
        }
        return null;
    } // getEncoding():String

    public final String getXMLVersion(){
        if(fCurrentEntity!=null){
            return fCurrentEntity.xmlVersion;
        }
        return null;
    } // getXMLVersion():String

    public final void setXMLVersion(String xmlVersion){
        xmlVersionSetExplicitly=true;
        fCurrentEntity.xmlVersion=xmlVersion;
    } // setXMLVersion(String)

    public final void setEncoding(String encoding) throws IOException{
        if(DEBUG_ENCODINGS){
            System.out.println("$$$ setEncoding: "+encoding);
        }
        if(fCurrentEntity.stream!=null){
            // if the encoding is the same, don't change the reader and
            // re-use the original reader used by the OneCharReader
            // NOTE: Besides saving an object, this overcomes deficiencies
            //       in the UTF-16 reader supplied with the standard Java
            //       distribution (up to and including 1.3). The UTF-16
            //       decoder buffers 8K blocks even when only asked to read
            //       a single char! -Ac
            if(fCurrentEntity.encoding==null||
                    !fCurrentEntity.encoding.equals(encoding)){
                // UTF-16 is a bit of a special case.  If the encoding is UTF-16,
                // and we know the endian-ness, we shouldn't change readers.
                // If it's ISO-10646-UCS-(2|4), then we'll have to deduce
                // the endian-ness from the encoding we presently have.
                if(fCurrentEntity.encoding!=null&&fCurrentEntity.encoding.startsWith("UTF-16")){
                    String ENCODING=encoding.toUpperCase(Locale.ENGLISH);
                    if(ENCODING.equals("UTF-16")) return;
                    if(ENCODING.equals("ISO-10646-UCS-4")){
                        if(fCurrentEntity.encoding.equals("UTF-16BE")){
                            fCurrentEntity.reader=new UCSReader(fCurrentEntity.stream,UCSReader.UCS4BE);
                        }else{
                            fCurrentEntity.reader=new UCSReader(fCurrentEntity.stream,UCSReader.UCS4LE);
                        }
                        return;
                    }
                    if(ENCODING.equals("ISO-10646-UCS-2")){
                        if(fCurrentEntity.encoding.equals("UTF-16BE")){
                            fCurrentEntity.reader=new UCSReader(fCurrentEntity.stream,UCSReader.UCS2BE);
                        }else{
                            fCurrentEntity.reader=new UCSReader(fCurrentEntity.stream,UCSReader.UCS2LE);
                        }
                        return;
                    }
                }
                // wrap a new reader around the input stream, changing
                // the encoding
                if(DEBUG_ENCODINGS){
                    System.out.println("$$$ creating new reader from stream: "+
                            fCurrentEntity.stream);
                }
                //fCurrentEntity.stream.reset();
                fCurrentEntity.reader=createReader(fCurrentEntity.stream,encoding,null);
                fCurrentEntity.encoding=encoding;
            }else{
                if(DEBUG_ENCODINGS)
                    System.out.println("$$$ reusing old reader on stream");
            }
        }
    } // setEncoding(String)

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
            return new UTF8Reader(inputStream,fCurrentEntity.fBufferSize,fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN),fErrorReporter.getLocale());
        }
        if(ENCODING.equals("US-ASCII")){
            if(DEBUG_ENCODINGS){
                System.out.println("$$$ creating ASCIIReader");
            }
            return new ASCIIReader(inputStream,fCurrentEntity.fBufferSize,fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN),fErrorReporter.getLocale());
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
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
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
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "EncodingByteOrderUnsupported",
                        new Object[]{encoding},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
        }
        // check for valid name
        boolean validIANA=XMLChar.isValidIANAEncoding(encoding);
        boolean validJava=XMLChar.isValidJavaEncoding(encoding);
        if(!validIANA||(fAllowJavaEncodings&&!validJava)){
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
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
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "EncodingDeclInvalid",
                        new Object[]{encoding},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
                // see comment above.
                javaEncoding="ISO8859_1";
            }
        }else if(javaEncoding.equals("ASCII")){
            if(DEBUG_ENCODINGS){
                System.out.println("$$$ creating ASCIIReader");
            }
            return new ASCIIReader(inputStream,fBufferSize,fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN),fErrorReporter.getLocale());
        }
        if(DEBUG_ENCODINGS){
            System.out.print("$$$ creating Java InputStreamReader: encoding="+javaEncoding);
            if(javaEncoding==encoding){
                System.out.print(" (IANA encoding)");
            }
            System.out.println();
        }
        return new InputStreamReader(inputStream,javaEncoding);
    } // createReader(InputStream,String, Boolean): Reader
    ///////////////// Locator methods finished.

    public void setExpandedSystemId(String systemId){
        //no-op
    }

    public void setLiteralSystemId(String systemId){
        //no-op
    }

    public void setPublicId(String publicId){
        //no-op
    }

    public String getVersion(){
        if(fCurrentEntity!=null)
            return fCurrentEntity.version;
        return null;
    }

    public void setVersion(String version){
        fCurrentEntity.version=version;
    }

    public final boolean isExternal(){
        return fCurrentEntity.isExternal();
    } // isExternal():boolean

    public int getChar(int relative) throws IOException{
        if(arrangeCapacity(relative+1,false)){
            return fCurrentEntity.ch[fCurrentEntity.position+relative];
        }else{
            return -1;
        }
    }//getChar()

    public int peekChar() throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(peekChar: ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }
        // peek at character
        int c=fCurrentEntity.ch[fCurrentEntity.position];
        // return peeked character
        if(DEBUG_BUFFER){
            System.out.print(")peekChar: ");
            print();
            if(isExternal){
                System.out.println(" -> '"+(c!='\r'?(char)c:'\n')+"'");
            }else{
                System.out.println(" -> '"+(char)c+"'");
            }
        }
        if(isExternal){
            return c!='\r'?c:'\n';
        }else{
            return c;
        }
    } // peekChar():int

    protected int scanChar(NameType nt) throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(scanChar: ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }
        // scan character
        int offset=fCurrentEntity.position;
        int c=fCurrentEntity.ch[fCurrentEntity.position++];
        if(c=='\n'||(c=='\r'&&isExternal)){
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber=1;
            if(fCurrentEntity.position==fCurrentEntity.count){
                invokeListeners(1);
                fCurrentEntity.ch[0]=(char)c;
                load(1,false,false);
                offset=0;
            }
            if(c=='\r'&&isExternal){
                if(fCurrentEntity.ch[fCurrentEntity.position++]!='\n'){
                    fCurrentEntity.position--;
                }
                c='\n';
            }
        }
        // return character that was scanned
        if(DEBUG_BUFFER){
            System.out.print(")scanChar: ");
            print();
            System.out.println(" -> '"+(char)c+"'");
        }
        fCurrentEntity.columnNumber++;
        if(!detectingVersion){
            checkEntityLimit(nt,fCurrentEntity,offset,fCurrentEntity.position-offset);
        }
        return c;
    } // scanChar():int

    protected String scanNmtoken() throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(scanNmtoken: ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }
        // scan nmtoken
        int offset=fCurrentEntity.position;
        boolean vc=false;
        char c;
        while(true){
            //while (XMLChar.isName(fCurrentEntity.ch[fCurrentEntity.position])) {
            c=fCurrentEntity.ch[fCurrentEntity.position];
            if(c<127){
                vc=VALID_NAMES[c];
            }else{
                vc=XMLChar.isName(c);
            }
            if(!vc) break;
            if(++fCurrentEntity.position==fCurrentEntity.count){
                int length=fCurrentEntity.position-offset;
                invokeListeners(length);
                if(length==fCurrentEntity.fBufferSize){
                    // bad luck we have to resize our buffer
                    char[] tmp=new char[fCurrentEntity.fBufferSize*2];
                    System.arraycopy(fCurrentEntity.ch,offset,
                            tmp,0,length);
                    fCurrentEntity.ch=tmp;
                    fCurrentEntity.fBufferSize*=2;
                }else{
                    System.arraycopy(fCurrentEntity.ch,offset,
                            fCurrentEntity.ch,0,length);
                }
                offset=0;
                if(load(length,false,false)){
                    break;
                }
            }
        }
        int length=fCurrentEntity.position-offset;
        fCurrentEntity.columnNumber+=length;
        // return nmtoken
        String symbol=null;
        if(length>0){
            symbol=fSymbolTable.addSymbol(fCurrentEntity.ch,offset,length);
        }
        if(DEBUG_BUFFER){
            System.out.print(")scanNmtoken: ");
            print();
            System.out.println(" -> "+String.valueOf(symbol));
        }
        return symbol;
    } // scanNmtoken():String

    protected String scanName(NameType nt) throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(scanName: ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }
        // scan name
        int offset=fCurrentEntity.position;
        int length;
        if(XMLChar.isNameStart(fCurrentEntity.ch[offset])){
            if(++fCurrentEntity.position==fCurrentEntity.count){
                invokeListeners(1);
                fCurrentEntity.ch[0]=fCurrentEntity.ch[offset];
                offset=0;
                if(load(1,false,false)){
                    fCurrentEntity.columnNumber++;
                    String symbol=fSymbolTable.addSymbol(fCurrentEntity.ch,0,1);
                    if(DEBUG_BUFFER){
                        System.out.print(")scanName: ");
                        print();
                        System.out.println(" -> "+String.valueOf(symbol));
                    }
                    return symbol;
                }
            }
            boolean vc=false;
            while(true){
                //XMLChar.isName(fCurrentEntity.ch[fCurrentEntity.position])) ;
                char c=fCurrentEntity.ch[fCurrentEntity.position];
                if(c<127){
                    vc=VALID_NAMES[c];
                }else{
                    vc=XMLChar.isName(c);
                }
                if(!vc) break;
                if((length=checkBeforeLoad(fCurrentEntity,offset,offset))>0){
                    offset=0;
                    if(load(length,false,false)){
                        break;
                    }
                }
            }
        }
        length=fCurrentEntity.position-offset;
        fCurrentEntity.columnNumber+=length;
        // return name
        String symbol;
        if(length>0){
            checkLimit(Limit.MAX_NAME_LIMIT,fCurrentEntity,offset,length);
            checkEntityLimit(nt,fCurrentEntity,offset,length);
            symbol=fSymbolTable.addSymbol(fCurrentEntity.ch,offset,length);
        }else
            symbol=null;
        if(DEBUG_BUFFER){
            System.out.print(")scanName: ");
            print();
            System.out.println(" -> "+String.valueOf(symbol));
        }
        return symbol;
    } // scanName():String

    protected boolean scanQName(QName qname,NameType nt) throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(scanQName, "+qname+": ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }
        // scan qualified name
        int offset=fCurrentEntity.position;
        //making a check if if the specified character is a valid name start character
        //as defined by production [5] in the XML 1.0 specification.
        // Name ::= (Letter | '_' | ':') (NameChar)*
        if(XMLChar.isNameStart(fCurrentEntity.ch[offset])){
            if(++fCurrentEntity.position==fCurrentEntity.count){
                invokeListeners(1);
                fCurrentEntity.ch[0]=fCurrentEntity.ch[offset];
                offset=0;
                if(load(1,false,false)){
                    fCurrentEntity.columnNumber++;
                    //adding into symbol table.
                    //XXX We are trying to add single character in SymbolTable??????
                    String name=fSymbolTable.addSymbol(fCurrentEntity.ch,0,1);
                    qname.setValues(null,name,name,null);
                    if(DEBUG_BUFFER){
                        System.out.print(")scanQName, "+qname+": ");
                        print();
                        System.out.println(" -> true");
                    }
                    checkEntityLimit(nt,fCurrentEntity,0,1);
                    return true;
                }
            }
            int index=-1;
            boolean vc=false;
            int length;
            while(true){
                //XMLChar.isName(fCurrentEntity.ch[fCurrentEntity.position])) ;
                char c=fCurrentEntity.ch[fCurrentEntity.position];
                if(c<127){
                    vc=VALID_NAMES[c];
                }else{
                    vc=XMLChar.isName(c);
                }
                if(!vc) break;
                if(c==':'){
                    if(index!=-1){
                        break;
                    }
                    index=fCurrentEntity.position;
                    //check prefix before further read
                    checkLimit(Limit.MAX_NAME_LIMIT,fCurrentEntity,offset,index-offset);
                }
                if((length=checkBeforeLoad(fCurrentEntity,offset,index))>0){
                    if(index!=-1){
                        index=index-offset;
                    }
                    offset=0;
                    if(load(length,false,false)){
                        break;
                    }
                }
            }
            length=fCurrentEntity.position-offset;
            fCurrentEntity.columnNumber+=length;
            if(length>0){
                String prefix=null;
                String localpart=null;
                String rawname=fSymbolTable.addSymbol(fCurrentEntity.ch,
                        offset,length);
                if(index!=-1){
                    int prefixLength=index-offset;
                    //check the result: prefix
                    checkLimit(Limit.MAX_NAME_LIMIT,fCurrentEntity,offset,prefixLength);
                    prefix=fSymbolTable.addSymbol(fCurrentEntity.ch,
                            offset,prefixLength);
                    int len=length-prefixLength-1;
                    //check the result: localpart
                    checkLimit(Limit.MAX_NAME_LIMIT,fCurrentEntity,index+1,len);
                    localpart=fSymbolTable.addSymbol(fCurrentEntity.ch,
                            index+1,len);
                }else{
                    localpart=rawname;
                    //check the result: localpart
                    checkLimit(Limit.MAX_NAME_LIMIT,fCurrentEntity,offset,length);
                }
                qname.setValues(prefix,localpart,rawname,null);
                if(DEBUG_BUFFER){
                    System.out.print(")scanQName, "+qname+": ");
                    print();
                    System.out.println(" -> true");
                }
                checkEntityLimit(nt,fCurrentEntity,offset,length);
                return true;
            }
        }
        // no qualified name found
        if(DEBUG_BUFFER){
            System.out.print(")scanQName, "+qname+": ");
            print();
            System.out.println(" -> false");
        }
        return false;
    } // scanQName(QName):boolean

    protected int checkBeforeLoad(ScannedEntity entity,int offset,
                                  int nameOffset) throws IOException{
        int length=0;
        if(++entity.position==entity.count){
            length=entity.position-offset;
            int nameLength=length;
            if(nameOffset!=-1){
                nameOffset=nameOffset-offset;
                nameLength=length-nameOffset;
            }else{
                nameOffset=offset;
            }
            //check limit before loading more data
            checkLimit(Limit.MAX_NAME_LIMIT,entity,nameOffset,nameLength);
            invokeListeners(length);
            if(length==entity.ch.length){
                // bad luck we have to resize our buffer
                char[] tmp=new char[entity.fBufferSize*2];
                System.arraycopy(entity.ch,offset,tmp,0,length);
                entity.ch=tmp;
                entity.fBufferSize*=2;
            }else{
                System.arraycopy(entity.ch,offset,entity.ch,0,length);
            }
        }
        return length;
    }

    protected void checkEntityLimit(NameType nt,ScannedEntity entity,int offset,int length){
        if(entity==null||!entity.isGE){
            return;
        }
        if(nt!=NameType.REFERENCE){
            checkLimit(Limit.GENERAL_ENTITY_SIZE_LIMIT,entity,offset,length);
        }
        if(nt==NameType.ELEMENTSTART||nt==NameType.ATTRIBUTENAME){
            checkNodeCount(entity);
        }
    }

    protected void checkNodeCount(ScannedEntity entity){
        if(entity!=null&&entity.isGE){
            checkLimit(Limit.ENTITY_REPLACEMENT_LIMIT,entity,0,1);
        }
    }

    protected void checkLimit(Limit limit,ScannedEntity entity,int offset,int length){
        fLimitAnalyzer.addValue(limit,entity.name,length);
        if(fSecurityManager.isOverLimit(limit,fLimitAnalyzer)){
            fSecurityManager.debugPrint(fLimitAnalyzer);
            Object[] e=(limit==Limit.ENTITY_REPLACEMENT_LIMIT)?
                    new Object[]{fLimitAnalyzer.getValue(limit),
                            fSecurityManager.getLimit(limit),fSecurityManager.getStateLiteral(limit)}:
                    new Object[]{entity.name,fLimitAnalyzer.getValue(limit),
                            fSecurityManager.getLimit(limit),fSecurityManager.getStateLiteral(limit)};
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,limit.key(),
                    e,XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        if(fSecurityManager.isOverLimit(Limit.TOTAL_ENTITY_SIZE_LIMIT,fLimitAnalyzer)){
            fSecurityManager.debugPrint(fLimitAnalyzer);
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,"TotalEntitySizeLimit",
                    new Object[]{fLimitAnalyzer.getTotalValue(Limit.TOTAL_ENTITY_SIZE_LIMIT),
                            fSecurityManager.getLimit(Limit.TOTAL_ENTITY_SIZE_LIMIT),
                            fSecurityManager.getStateLiteral(Limit.TOTAL_ENTITY_SIZE_LIMIT)},
                    XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
    }

    protected int scanContent(XMLString content) throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(scanContent: ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }else if(fCurrentEntity.position==fCurrentEntity.count-1){
            invokeListeners(1);
            fCurrentEntity.ch[0]=fCurrentEntity.ch[fCurrentEntity.count-1];
            load(1,false,false);
            fCurrentEntity.position=0;
        }
        // normalize newlines
        int offset=fCurrentEntity.position;
        int c=fCurrentEntity.ch[offset];
        int newlines=0;
        boolean counted=false;
        if(c=='\n'||(c=='\r'&&isExternal)){
            if(DEBUG_BUFFER){
                System.out.print("[newline, "+offset+", "+fCurrentEntity.position+": ");
                print();
                System.out.println();
            }
            do{
                c=fCurrentEntity.ch[fCurrentEntity.position++];
                if(c=='\r'&&isExternal){
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber=1;
                    if(fCurrentEntity.position==fCurrentEntity.count){
                        checkEntityLimit(null,fCurrentEntity,offset,newlines);
                        offset=0;
                        fCurrentEntity.position=newlines;
                        if(load(newlines,false,true)){
                            counted=true;
                            break;
                        }
                    }
                    if(fCurrentEntity.ch[fCurrentEntity.position]=='\n'){
                        fCurrentEntity.position++;
                        offset++;
                    }
                    /*** NEWLINE NORMALIZATION ***/
                    else{
                        newlines++;
                    }
                }else if(c=='\n'){
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber=1;
                    if(fCurrentEntity.position==fCurrentEntity.count){
                        checkEntityLimit(null,fCurrentEntity,offset,newlines);
                        offset=0;
                        fCurrentEntity.position=newlines;
                        if(load(newlines,false,true)){
                            counted=true;
                            break;
                        }
                    }
                }else{
                    fCurrentEntity.position--;
                    break;
                }
            }while(fCurrentEntity.position<fCurrentEntity.count-1);
            for(int i=offset;i<fCurrentEntity.position;i++){
                fCurrentEntity.ch[i]='\n';
            }
            int length=fCurrentEntity.position-offset;
            if(fCurrentEntity.position==fCurrentEntity.count-1){
                checkEntityLimit(null,fCurrentEntity,offset,length);
                //CHANGED: dont replace the value.. append to the buffer. This gives control to the callee
                //on buffering the data..
                content.setValues(fCurrentEntity.ch,offset,length);
                //content.append(fCurrentEntity.ch, offset, length);
                if(DEBUG_BUFFER){
                    System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                    print();
                    System.out.println();
                }
                return -1;
            }
            if(DEBUG_BUFFER){
                System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                print();
                System.out.println();
            }
        }
        while(fCurrentEntity.position<fCurrentEntity.count){
            c=fCurrentEntity.ch[fCurrentEntity.position++];
            if(!XMLChar.isContent(c)){
                fCurrentEntity.position--;
                break;
            }
        }
        int length=fCurrentEntity.position-offset;
        fCurrentEntity.columnNumber+=length-newlines;
        if(!counted){
            checkEntityLimit(null,fCurrentEntity,offset,length);
        }
        //CHANGED: dont replace the value.. append to the buffer. This gives control to the callee
        //on buffering the data..
        content.setValues(fCurrentEntity.ch,offset,length);
        //content.append(fCurrentEntity.ch, offset, length);
        // return next character
        if(fCurrentEntity.position!=fCurrentEntity.count){
            c=fCurrentEntity.ch[fCurrentEntity.position];
            // REVISIT: Does this need to be updated to fix the
            //          #x0D ^#x0A newline normalization problem? -Ac
            if(c=='\r'&&isExternal){
                c='\n';
            }
        }else{
            c=-1;
        }
        if(DEBUG_BUFFER){
            System.out.print(")scanContent: ");
            print();
            System.out.println(" -> '"+(char)c+"'");
        }
        return c;
    } // scanContent(XMLString):int

    protected int scanLiteral(int quote,XMLString content,boolean isNSURI)
            throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(scanLiteral, '"+(char)quote+"': ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }else if(fCurrentEntity.position==fCurrentEntity.count-1){
            invokeListeners(1);
            fCurrentEntity.ch[0]=fCurrentEntity.ch[fCurrentEntity.count-1];
            load(1,false,false);
            fCurrentEntity.position=0;
        }
        // normalize newlines
        int offset=fCurrentEntity.position;
        int c=fCurrentEntity.ch[offset];
        int newlines=0;
        if(whiteSpaceInfoNeeded)
            whiteSpaceLen=0;
        if(c=='\n'||(c=='\r'&&isExternal)){
            if(DEBUG_BUFFER){
                System.out.print("[newline, "+offset+", "+fCurrentEntity.position+": ");
                print();
                System.out.println();
            }
            do{
                c=fCurrentEntity.ch[fCurrentEntity.position++];
                if(c=='\r'&&isExternal){
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber=1;
                    if(fCurrentEntity.position==fCurrentEntity.count){
                        offset=0;
                        fCurrentEntity.position=newlines;
                        if(load(newlines,false,true)){
                            break;
                        }
                    }
                    if(fCurrentEntity.ch[fCurrentEntity.position]=='\n'){
                        fCurrentEntity.position++;
                        offset++;
                    }
                    /*** NEWLINE NORMALIZATION ***/
                    else{
                        newlines++;
                    }
                    /***/
                }else if(c=='\n'){
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber=1;
                    if(fCurrentEntity.position==fCurrentEntity.count){
                        offset=0;
                        fCurrentEntity.position=newlines;
                        if(load(newlines,false,true)){
                            break;
                        }
                    }
                    /*** NEWLINE NORMALIZATION ***
                     * if (fCurrentEntity.ch[fCurrentEntity.position] == '\r'
                     * && external) {
                     * fCurrentEntity.position++;
                     * offset++;
                     * }
                     * /***/
                }else{
                    fCurrentEntity.position--;
                    break;
                }
            }while(fCurrentEntity.position<fCurrentEntity.count-1);
            int i=0;
            for(i=offset;i<fCurrentEntity.position;i++){
                fCurrentEntity.ch[i]='\n';
                storeWhiteSpace(i);
            }
            int length=fCurrentEntity.position-offset;
            if(fCurrentEntity.position==fCurrentEntity.count-1){
                content.setValues(fCurrentEntity.ch,offset,length);
                if(DEBUG_BUFFER){
                    System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                    print();
                    System.out.println();
                }
                return -1;
            }
            if(DEBUG_BUFFER){
                System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                print();
                System.out.println();
            }
        }
        // scan literal value
        for(;fCurrentEntity.position<fCurrentEntity.count;fCurrentEntity.position++){
            c=fCurrentEntity.ch[fCurrentEntity.position];
            if((c==quote&&
                    (!fCurrentEntity.literal||isExternal))||
                    c=='%'||!XMLChar.isContent(c)){
                break;
            }
            if(whiteSpaceInfoNeeded&&c=='\t'){
                storeWhiteSpace(fCurrentEntity.position);
            }
        }
        int length=fCurrentEntity.position-offset;
        fCurrentEntity.columnNumber+=length-newlines;
        checkEntityLimit(null,fCurrentEntity,offset,length);
        if(isNSURI){
            checkLimit(Limit.MAX_NAME_LIMIT,fCurrentEntity,offset,length);
        }
        content.setValues(fCurrentEntity.ch,offset,length);
        // return next character
        if(fCurrentEntity.position!=fCurrentEntity.count){
            c=fCurrentEntity.ch[fCurrentEntity.position];
            // NOTE: We don't want to accidentally signal the
            //       end of the literal if we're expanding an
            //       entity appearing in the literal. -Ac
            if(c==quote&&fCurrentEntity.literal){
                c=-1;
            }
        }else{
            c=-1;
        }
        if(DEBUG_BUFFER){
            System.out.print(")scanLiteral, '"+(char)quote+"': ");
            print();
            System.out.println(" -> '"+(char)c+"'");
        }
        return c;
    } // scanLiteral(int,XMLString):int

    private void storeWhiteSpace(int whiteSpacePos){
        if(whiteSpaceLen>=whiteSpaceLookup.length){
            int[] tmp=new int[whiteSpaceLookup.length+100];
            System.arraycopy(whiteSpaceLookup,0,tmp,0,whiteSpaceLookup.length);
            whiteSpaceLookup=tmp;
        }
        whiteSpaceLookup[whiteSpaceLen++]=whiteSpacePos;
    }

    //CHANGED:
    protected boolean scanData(String delimiter,XMLStringBuffer buffer)
            throws IOException{
        boolean done=false;
        int delimLen=delimiter.length();
        char charAt0=delimiter.charAt(0);
        do{
            if(DEBUG_BUFFER){
                System.out.print("(scanData: ");
                print();
                System.out.println();
            }
            // load more characters, if needed
            if(fCurrentEntity.position==fCurrentEntity.count){
                load(0,true,false);
            }
            boolean bNextEntity=false;
            while((fCurrentEntity.position>fCurrentEntity.count-delimLen)
                    &&(!bNextEntity)){
                System.arraycopy(fCurrentEntity.ch,
                        fCurrentEntity.position,
                        fCurrentEntity.ch,
                        0,
                        fCurrentEntity.count-fCurrentEntity.position);
                bNextEntity=load(fCurrentEntity.count-fCurrentEntity.position,false,false);
                fCurrentEntity.position=0;
                fCurrentEntity.startPosition=0;
            }
            if(fCurrentEntity.position>fCurrentEntity.count-delimLen){
                // something must be wrong with the input:  e.g., file ends in an unterminated comment
                int length=fCurrentEntity.count-fCurrentEntity.position;
                checkEntityLimit(NameType.COMMENT,fCurrentEntity,fCurrentEntity.position,length);
                buffer.append(fCurrentEntity.ch,fCurrentEntity.position,length);
                fCurrentEntity.columnNumber+=fCurrentEntity.count;
                fCurrentEntity.baseCharOffset+=(fCurrentEntity.position-fCurrentEntity.startPosition);
                fCurrentEntity.position=fCurrentEntity.count;
                fCurrentEntity.startPosition=fCurrentEntity.count;
                load(0,true,false);
                return false;
            }
            // normalize newlines
            int offset=fCurrentEntity.position;
            int c=fCurrentEntity.ch[offset];
            int newlines=0;
            if(c=='\n'||(c=='\r'&&isExternal)){
                if(DEBUG_BUFFER){
                    System.out.print("[newline, "+offset+", "+fCurrentEntity.position+": ");
                    print();
                    System.out.println();
                }
                do{
                    c=fCurrentEntity.ch[fCurrentEntity.position++];
                    if(c=='\r'&&isExternal){
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber=1;
                        if(fCurrentEntity.position==fCurrentEntity.count){
                            offset=0;
                            fCurrentEntity.position=newlines;
                            if(load(newlines,false,true)){
                                break;
                            }
                        }
                        if(fCurrentEntity.ch[fCurrentEntity.position]=='\n'){
                            fCurrentEntity.position++;
                            offset++;
                        }
                        /*** NEWLINE NORMALIZATION ***/
                        else{
                            newlines++;
                        }
                    }else if(c=='\n'){
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber=1;
                        if(fCurrentEntity.position==fCurrentEntity.count){
                            offset=0;
                            fCurrentEntity.position=newlines;
                            fCurrentEntity.count=newlines;
                            if(load(newlines,false,true)){
                                break;
                            }
                        }
                    }else{
                        fCurrentEntity.position--;
                        break;
                    }
                }while(fCurrentEntity.position<fCurrentEntity.count-1);
                for(int i=offset;i<fCurrentEntity.position;i++){
                    fCurrentEntity.ch[i]='\n';
                }
                int length=fCurrentEntity.position-offset;
                if(fCurrentEntity.position==fCurrentEntity.count-1){
                    checkEntityLimit(NameType.COMMENT,fCurrentEntity,offset,length);
                    buffer.append(fCurrentEntity.ch,offset,length);
                    if(DEBUG_BUFFER){
                        System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                        print();
                        System.out.println();
                    }
                    return true;
                }
                if(DEBUG_BUFFER){
                    System.out.print("]newline, "+offset+", "+fCurrentEntity.position+": ");
                    print();
                    System.out.println();
                }
            }
            // iterate over buffer looking for delimiter
            OUTER:
            while(fCurrentEntity.position<fCurrentEntity.count){
                c=fCurrentEntity.ch[fCurrentEntity.position++];
                if(c==charAt0){
                    // looks like we just hit the delimiter
                    int delimOffset=fCurrentEntity.position-1;
                    for(int i=1;i<delimLen;i++){
                        if(fCurrentEntity.position==fCurrentEntity.count){
                            fCurrentEntity.position-=i;
                            break OUTER;
                        }
                        c=fCurrentEntity.ch[fCurrentEntity.position++];
                        if(delimiter.charAt(i)!=c){
                            fCurrentEntity.position-=i;
                            break;
                        }
                    }
                    if(fCurrentEntity.position==delimOffset+delimLen){
                        done=true;
                        break;
                    }
                }else if(c=='\n'||(isExternal&&c=='\r')){
                    fCurrentEntity.position--;
                    break;
                }else if(XMLChar.isInvalid(c)){
                    fCurrentEntity.position--;
                    int length=fCurrentEntity.position-offset;
                    fCurrentEntity.columnNumber+=length-newlines;
                    checkEntityLimit(NameType.COMMENT,fCurrentEntity,offset,length);
                    buffer.append(fCurrentEntity.ch,offset,length);
                    return true;
                }
            }
            int length=fCurrentEntity.position-offset;
            fCurrentEntity.columnNumber+=length-newlines;
            checkEntityLimit(NameType.COMMENT,fCurrentEntity,offset,length);
            if(done){
                length-=delimLen;
            }
            buffer.append(fCurrentEntity.ch,offset,length);
            // return true if string was skipped
            if(DEBUG_BUFFER){
                System.out.print(")scanData: ");
                print();
                System.out.println(" -> "+done);
            }
        }while(!done);
        return !done;
    } // scanData(String, XMLStringBuffer)

    protected boolean skipChar(int c,NameType nt) throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(skipChar, '"+(char)c+"': ");
            print();
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }
        // skip character
        int offset=fCurrentEntity.position;
        int cc=fCurrentEntity.ch[fCurrentEntity.position];
        if(cc==c){
            fCurrentEntity.position++;
            if(c=='\n'){
                fCurrentEntity.lineNumber++;
                fCurrentEntity.columnNumber=1;
            }else{
                fCurrentEntity.columnNumber++;
            }
            if(DEBUG_BUFFER){
                System.out.print(")skipChar, '"+(char)c+"': ");
                print();
                System.out.println(" -> true");
            }
            checkEntityLimit(nt,fCurrentEntity,offset,fCurrentEntity.position-offset);
            return true;
        }else if(c=='\n'&&cc=='\r'&&isExternal){
            // handle newlines
            if(fCurrentEntity.position==fCurrentEntity.count){
                invokeListeners(1);
                fCurrentEntity.ch[0]=(char)cc;
                load(1,false,false);
            }
            fCurrentEntity.position++;
            if(fCurrentEntity.ch[fCurrentEntity.position]=='\n'){
                fCurrentEntity.position++;
            }
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber=1;
            if(DEBUG_BUFFER){
                System.out.print(")skipChar, '"+(char)c+"': ");
                print();
                System.out.println(" -> true");
            }
            checkEntityLimit(nt,fCurrentEntity,offset,fCurrentEntity.position-offset);
            return true;
        }
        // character was not skipped
        if(DEBUG_BUFFER){
            System.out.print(")skipChar, '"+(char)c+"': ");
            print();
            System.out.println(" -> false");
        }
        return false;
    } // skipChar(int):boolean

    public boolean isSpace(char ch){
        return (ch==' ')||(ch=='\n')||(ch=='\t')||(ch=='\r');
    }

    protected boolean skipSpaces() throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(skipSpaces: ");
            print();
            System.out.println();
        }
        //boolean entityChanged = false;
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,true);
        }
        //we are doing this check only in skipSpace() because it is called by
        //fMiscDispatcher and we want the parser to exit gracefully when document
        //is well-formed.
        //it is possible that end of document is reached and
        //fCurrentEntity becomes null
        //nothing was read so entity changed  'false' should be returned.
        if(fCurrentEntity==null){
            return false;
        }
        // skip spaces
        int c=fCurrentEntity.ch[fCurrentEntity.position];
        int offset=fCurrentEntity.position-1;
        if(XMLChar.isSpace(c)){
            do{
                boolean entityChanged=false;
                // handle newlines
                if(c=='\n'||(isExternal&&c=='\r')){
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber=1;
                    if(fCurrentEntity.position==fCurrentEntity.count-1){
                        invokeListeners(1);
                        fCurrentEntity.ch[0]=(char)c;
                        entityChanged=load(1,true,false);
                        if(!entityChanged){
                            // the load change the position to be 1,
                            // need to restore it when entity not changed
                            fCurrentEntity.position=0;
                        }else if(fCurrentEntity==null){
                            return true;
                        }
                    }
                    if(c=='\r'&&isExternal){
                        // REVISIT: Does this need to be updated to fix the
                        //          #x0D ^#x0A newline normalization problem? -Ac
                        if(fCurrentEntity.ch[++fCurrentEntity.position]!='\n'){
                            fCurrentEntity.position--;
                        }
                    }
                }else{
                    fCurrentEntity.columnNumber++;
                }
                //If this is a general entity, spaces within a start element should be counted
                checkEntityLimit(null,fCurrentEntity,offset,fCurrentEntity.position-offset);
                offset=fCurrentEntity.position;
                // load more characters, if needed
                if(!entityChanged){
                    fCurrentEntity.position++;
                }
                if(fCurrentEntity.position==fCurrentEntity.count){
                    load(0,true,true);
                    //we are doing this check only in skipSpace() because it is called by
                    //fMiscDispatcher and we want the parser to exit gracefully when document
                    //is well-formed.
                    //it is possible that end of document is reached and
                    //fCurrentEntity becomes null
                    //nothing was read so entity changed  'false' should be returned.
                    if(fCurrentEntity==null){
                        return true;
                    }
                }
            }while(XMLChar.isSpace(c=fCurrentEntity.ch[fCurrentEntity.position]));
            if(DEBUG_BUFFER){
                System.out.print(")skipSpaces: ");
                print();
                System.out.println(" -> true");
            }
            return true;
        }
        // no spaces were found
        if(DEBUG_BUFFER){
            System.out.print(")skipSpaces: ");
            print();
            System.out.println(" -> false");
        }
        return false;
    } // skipSpaces():boolean

    public boolean arrangeCapacity(int length) throws IOException{
        return arrangeCapacity(length,false);
    }

    public boolean arrangeCapacity(int length,boolean changeEntity) throws IOException{
        //check if the capacity is availble in the current buffer
        //count is no. of characters in the buffer   [x][m][l]
        //position is '0' based
        //System.out.println("fCurrent Entity " + fCurrentEntity);
        if((fCurrentEntity.count-fCurrentEntity.position)>=length){
            return true;
        }
        if(DEBUG_SKIP_STRING){
            System.out.println("fCurrentEntity.count = "+fCurrentEntity.count);
            System.out.println("fCurrentEntity.position = "+fCurrentEntity.position);
            System.out.println("length = "+length);
        }
        boolean entityChanged=false;
        //load more characters -- this function shouldn't change the entity
        while((fCurrentEntity.count-fCurrentEntity.position)<length){
            if((fCurrentEntity.ch.length-fCurrentEntity.position)<length){
                invokeListeners(0);
                System.arraycopy(fCurrentEntity.ch,fCurrentEntity.position,fCurrentEntity.ch,0,fCurrentEntity.count-fCurrentEntity.position);
                fCurrentEntity.count=fCurrentEntity.count-fCurrentEntity.position;
                fCurrentEntity.position=0;
            }
            if((fCurrentEntity.count-fCurrentEntity.position)<length){
                int pos=fCurrentEntity.position;
                invokeListeners(pos);
                entityChanged=load(fCurrentEntity.count,changeEntity,false);
                fCurrentEntity.position=pos;
                if(entityChanged) break;
            }
            if(DEBUG_SKIP_STRING){
                System.out.println("fCurrentEntity.count = "+fCurrentEntity.count);
                System.out.println("fCurrentEntity.position = "+fCurrentEntity.position);
                System.out.println("length = "+length);
            }
        }
        //load changes the position.. set it back to the point where we started.
        //after loading check again.
        if((fCurrentEntity.count-fCurrentEntity.position)>=length){
            return true;
        }else{
            return false;
        }
    }

    protected boolean skipString(String s) throws IOException{
        final int length=s.length();
        //first make sure that required capacity is avaible
        if(arrangeCapacity(length,false)){
            final int beforeSkip=fCurrentEntity.position;
            int afterSkip=fCurrentEntity.position+length-1;
            if(DEBUG_SKIP_STRING){
                System.out.println("skipString,length = "+s+","+length);
                System.out.println("Buffer string to be skipped = "+new String(fCurrentEntity.ch,beforeSkip,length));
            }
            //s.charAt() indexes are 0 to 'Length -1' based.
            int i=length-1;
            //check from reverse
            while(s.charAt(i--)==fCurrentEntity.ch[afterSkip]){
                if(afterSkip--==beforeSkip){
                    fCurrentEntity.position=fCurrentEntity.position+length;
                    fCurrentEntity.columnNumber+=length;
                    if(!detectingVersion){
                        checkEntityLimit(null,fCurrentEntity,beforeSkip,length);
                    }
                    return true;
                }
            }
        }
        return false;
    } // skipString(String):boolean
    //
    // Locator methods
    //
    //
    // Private methods
    //

    protected boolean skipString(char[] s) throws IOException{
        final int length=s.length;
        //first make sure that required capacity is avaible
        if(arrangeCapacity(length,false)){
            int beforeSkip=fCurrentEntity.position;
            if(DEBUG_SKIP_STRING){
                System.out.println("skipString,length = "+new String(s)+","+length);
                System.out.println("skipString,length = "+new String(s)+","+length);
            }
            for(int i=0;i<length;i++){
                if(!(fCurrentEntity.ch[beforeSkip++]==s[i])){
                    return false;
                }
            }
            fCurrentEntity.position=fCurrentEntity.position+length;
            fCurrentEntity.columnNumber+=length;
            if(!detectingVersion){
                checkEntityLimit(null,fCurrentEntity,beforeSkip,length);
            }
            return true;
        }
        return false;
    }

    final boolean load(int offset,boolean changeEntity,boolean notify)
            throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(load, "+offset+": ");
            print();
            System.out.println();
        }
        if(notify){
            invokeListeners(offset);
        }
        //maintaing the count till last load
        fCurrentEntity.fTotalCountTillLastLoad=fCurrentEntity.fTotalCountTillLastLoad+fCurrentEntity.fLastCount;
        // read characters
        int length=fCurrentEntity.ch.length-offset;
        if(!fCurrentEntity.mayReadChunks&&length>XMLEntityManager.DEFAULT_XMLDECL_BUFFER_SIZE){
            length=XMLEntityManager.DEFAULT_XMLDECL_BUFFER_SIZE;
        }
        if(DEBUG_BUFFER) System.out.println("  length to try to read: "+length);
        int count=fCurrentEntity.reader.read(fCurrentEntity.ch,offset,length);
        if(DEBUG_BUFFER) System.out.println("  length actually read:  "+count);
        // reset count and position
        boolean entityChanged=false;
        if(count!=-1){
            if(count!=0){
                // record the last count
                fCurrentEntity.fLastCount=count;
                fCurrentEntity.count=count+offset;
                fCurrentEntity.position=offset;
            }
        }
        // end of this entity
        else{
            fCurrentEntity.count=offset;
            fCurrentEntity.position=offset;
            entityChanged=true;
            if(changeEntity){
                //notify the entity manager about the end of entity
                fEntityManager.endEntity();
                //return if the current entity becomes null
                if(fCurrentEntity==null){
                    throw END_OF_DOCUMENT_ENTITY;
                }
                // handle the trailing edges
                if(fCurrentEntity.position==fCurrentEntity.count){
                    load(0,true,false);
                }
            }
        }
        if(DEBUG_BUFFER){
            System.out.print(")load, "+offset+": ");
            print();
            System.out.println();
        }
        return entityChanged;
    } // load(int, boolean):boolean

    protected Object[] getEncodingName(byte[] b4,int count){
        if(count<2){
            return new Object[]{"UTF-8",null};
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
            return new Object[]{"UTF-8",null};
        }
        // UTF-8 with a BOM
        int b2=b4[2]&0xFF;
        if(b0==0xEF&&b1==0xBB&&b2==0xBF){
            return new Object[]{"UTF-8",null};
        }
        // default to UTF-8 if we don't have enough bytes to make a
        // good determination of the encoding
        if(count<4){
            return new Object[]{"UTF-8",null};
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
        // default encoding
        return new Object[]{"UTF-8",null};
    } // getEncodingName(byte[],int):Object[]

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
    }

    public void registerListener(XMLBufferListener listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public void invokeListeners(int loadPos){
        for(int i=0;i<listeners.size();i++){
            listeners.get(i).refresh(loadPos);
        }
    }

    protected final boolean skipDeclSpaces() throws IOException{
        if(DEBUG_BUFFER){
            System.out.print("(skipDeclSpaces: ");
            //XMLEntityManager.print(fCurrentEntity);
            System.out.println();
        }
        // load more characters, if needed
        if(fCurrentEntity.position==fCurrentEntity.count){
            load(0,true,false);
        }
        // skip spaces
        int c=fCurrentEntity.ch[fCurrentEntity.position];
        if(XMLChar.isSpace(c)){
            boolean external=fCurrentEntity.isExternal();
            do{
                boolean entityChanged=false;
                // handle newlines
                if(c=='\n'||(external&&c=='\r')){
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber=1;
                    if(fCurrentEntity.position==fCurrentEntity.count-1){
                        fCurrentEntity.ch[0]=(char)c;
                        entityChanged=load(1,true,false);
                        if(!entityChanged)
                            // the load change the position to be 1,
                            // need to restore it when entity not changed
                            fCurrentEntity.position=0;
                    }
                    if(c=='\r'&&external){
                        // REVISIT: Does this need to be updated to fix the
                        //          #x0D ^#x0A newline normalization problem? -Ac
                        if(fCurrentEntity.ch[++fCurrentEntity.position]!='\n'){
                            fCurrentEntity.position--;
                        }
                    }
                    /*** NEWLINE NORMALIZATION ***
                     * else {
                     * if (fCurrentEntity.ch[fCurrentEntity.position + 1] == '\r'
                     * && external) {
                     * fCurrentEntity.position++;
                     * }
                     * }
                     * /***/
                }else{
                    fCurrentEntity.columnNumber++;
                }
                // load more characters, if needed
                if(!entityChanged)
                    fCurrentEntity.position++;
                if(fCurrentEntity.position==fCurrentEntity.count){
                    load(0,true,false);
                }
            }while(XMLChar.isSpace(c=fCurrentEntity.ch[fCurrentEntity.position]));
            if(DEBUG_BUFFER){
                System.out.print(")skipDeclSpaces: ");
                //  XMLEntityManager.print(fCurrentEntity);
                System.out.println(" -> true");
            }
            return true;
        }
        // no spaces were found
        if(DEBUG_BUFFER){
            System.out.print(")skipDeclSpaces: ");
            //XMLEntityManager.print(fCurrentEntity);
            System.out.println(" -> false");
        }
        return false;
    } // skipDeclSpaces():boolean
} // class XMLEntityScanner
