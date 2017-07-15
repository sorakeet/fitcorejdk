/**
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.util.XML11Char;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;

import java.io.IOException;

public class XML11DocumentScannerImpl
        extends XMLDocumentScannerImpl{
    private final XMLStringBuffer fStringBuffer=new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer2=new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer3=new XMLStringBuffer();
    //
    // Constructors
    //

    public XML11DocumentScannerImpl(){
        super();
    } // <init>()
    //
    // overridden methods
    //
    // XMLDocumentFragmentImpl methods

    protected int scanContent(XMLStringBuffer content) throws IOException, XNIException{
        fTempString.length=0;
        int c=fEntityScanner.scanContent(fTempString);
        content.append(fTempString);
        if(c=='\r'||c==0x85||c==0x2028){
            // happens when there is the character reference &#13;
            // but scanContent doesn't do entity expansions...
            // is this *really* necessary???  - NG
            fEntityScanner.scanChar(null);
            content.append((char)c);
            c=-1;
        }
        /**if (fDocumentHandler != null && content.length > 0) {
         fDocumentHandler.characters(content, null);
         } */
        if(c==']'){
            content.append((char)fEntityScanner.scanChar(null));
            // remember where we are in case we get an endEntity before we
            // could flush the buffer out - this happens when we're parsing an
            // entity which ends with a ]
            fInScanContent=true;
            //
            // We work on a single character basis to handle cases such as:
            // ']]]>' which we might otherwise miss.
            //
            if(fEntityScanner.skipChar(']',null)){
                content.append(']');
                while(fEntityScanner.skipChar(']',null)){
                    content.append(']');
                }
                if(fEntityScanner.skipChar('>',null)){
                    reportFatalError("CDEndInContent",null);
                }
            }
            /**if (fDocumentHandler != null && fStringBuffer.length != 0) {
             fDocumentHandler.characters(fStringBuffer, null);
             }*/
            fInScanContent=false;
            c=-1;
        }
        return c;
    } // scanContent():int

    protected boolean scanAttributeValue(XMLString value,
                                         XMLString nonNormalizedValue,
                                         String atName,
                                         boolean checkEntities,String eleName,boolean isNSURI)
            throws IOException, XNIException{
        // quote
        int quote=fEntityScanner.peekChar();
        if(quote!='\''&&quote!='"'){
            reportFatalError("OpenQuoteExpected",new Object[]{eleName,atName});
        }
        fEntityScanner.scanChar(NameType.ATTRIBUTE);
        int entityDepth=fEntityDepth;
        int c=fEntityScanner.scanLiteral(quote,value,isNSURI);
        if(DEBUG_ATTR_NORMALIZATION){
            System.out.println("** scanLiteral -> \""
                    +value.toString()+"\"");
        }
        int fromIndex=0;
        if(c==quote&&(fromIndex=isUnchangedByNormalization(value))==-1){
            /** Both the non-normalized and normalized attribute values are equal. **/
            nonNormalizedValue.setValues(value);
            int cquote=fEntityScanner.scanChar(NameType.ATTRIBUTE);
            if(cquote!=quote){
                reportFatalError("CloseQuoteExpected",new Object[]{eleName,atName});
            }
            return true;
        }
        fStringBuffer2.clear();
        fStringBuffer2.append(value);
        normalizeWhitespace(value,fromIndex);
        if(DEBUG_ATTR_NORMALIZATION){
            System.out.println("** normalizeWhitespace -> \""
                    +value.toString()+"\"");
        }
        if(c!=quote){
            fScanningAttribute=true;
            fStringBuffer.clear();
            do{
                fStringBuffer.append(value);
                if(DEBUG_ATTR_NORMALIZATION){
                    System.out.println("** value2: \""
                            +fStringBuffer.toString()+"\"");
                }
                if(c=='&'){
                    fEntityScanner.skipChar('&',NameType.REFERENCE);
                    if(entityDepth==fEntityDepth){
                        fStringBuffer2.append('&');
                    }
                    if(fEntityScanner.skipChar('#',NameType.REFERENCE)){
                        if(entityDepth==fEntityDepth){
                            fStringBuffer2.append('#');
                        }
                        int ch=scanCharReferenceValue(fStringBuffer,fStringBuffer2);
                        if(ch!=-1){
                            if(DEBUG_ATTR_NORMALIZATION){
                                System.out.println("** value3: \""
                                        +fStringBuffer.toString()
                                        +"\"");
                            }
                        }
                    }else{
                        String entityName=fEntityScanner.scanName(NameType.REFERENCE);
                        if(entityName==null){
                            reportFatalError("NameRequiredInReference",null);
                        }else if(entityDepth==fEntityDepth){
                            fStringBuffer2.append(entityName);
                        }
                        if(!fEntityScanner.skipChar(';',NameType.REFERENCE)){
                            reportFatalError("SemicolonRequiredInReference",
                                    new Object[]{entityName});
                        }else if(entityDepth==fEntityDepth){
                            fStringBuffer2.append(';');
                        }
                        if(resolveCharacter(entityName,fStringBuffer)){
                            checkEntityLimit(false,fEntityScanner.fCurrentEntity.name,1);
                        }else{
                            if(fEntityManager.isExternalEntity(entityName)){
                                reportFatalError("ReferenceToExternalEntity",
                                        new Object[]{entityName});
                            }else{
                                if(!fEntityManager.isDeclaredEntity(entityName)){
                                    //WFC & VC: Entity Declared
                                    if(checkEntities){
                                        if(fValidation){
                                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                                    "EntityNotDeclared",
                                                    new Object[]{entityName},
                                                    XMLErrorReporter.SEVERITY_ERROR);
                                        }
                                    }else{
                                        reportFatalError("EntityNotDeclared",
                                                new Object[]{entityName});
                                    }
                                }
                                fEntityManager.startEntity(true,entityName,true);
                            }
                        }
                    }
                }else if(c=='<'){
                    reportFatalError("LessthanInAttValue",
                            new Object[]{eleName,atName});
                    fEntityScanner.scanChar(null);
                    if(entityDepth==fEntityDepth){
                        fStringBuffer2.append((char)c);
                    }
                }else if(c=='%'||c==']'){
                    fEntityScanner.scanChar(null);
                    fStringBuffer.append((char)c);
                    if(entityDepth==fEntityDepth){
                        fStringBuffer2.append((char)c);
                    }
                    if(DEBUG_ATTR_NORMALIZATION){
                        System.out.println("** valueF: \""
                                +fStringBuffer.toString()+"\"");
                    }
                }
                // note that none of these characters should ever get through
                // XML11EntityScanner.  Not sure why
                // this check was originally necessary.  - NG
                else if(c=='\n'||c=='\r'||c==0x85||c==0x2028){
                    fEntityScanner.scanChar(null);
                    fStringBuffer.append(' ');
                    if(entityDepth==fEntityDepth){
                        fStringBuffer2.append('\n');
                    }
                }else if(c!=-1&&XMLChar.isHighSurrogate(c)){
                    fStringBuffer3.clear();
                    if(scanSurrogates(fStringBuffer3)){
                        fStringBuffer.append(fStringBuffer3);
                        if(entityDepth==fEntityDepth){
                            fStringBuffer2.append(fStringBuffer3);
                        }
                        if(DEBUG_ATTR_NORMALIZATION){
                            System.out.println("** valueI: \""
                                    +fStringBuffer.toString()
                                    +"\"");
                        }
                    }
                }else if(c!=-1&&isInvalidLiteral(c)){
                    reportFatalError("InvalidCharInAttValue",
                            new Object[]{eleName,atName,Integer.toString(c,16)});
                    fEntityScanner.scanChar(null);
                    if(entityDepth==fEntityDepth){
                        fStringBuffer2.append((char)c);
                    }
                }
                c=fEntityScanner.scanLiteral(quote,value,isNSURI);
                if(entityDepth==fEntityDepth){
                    fStringBuffer2.append(value);
                }
                normalizeWhitespace(value);
            }while(c!=quote||entityDepth!=fEntityDepth);
            fStringBuffer.append(value);
            if(DEBUG_ATTR_NORMALIZATION){
                System.out.println("** valueN: \""
                        +fStringBuffer.toString()+"\"");
            }
            value.setValues(fStringBuffer);
            fScanningAttribute=false;
        }
        nonNormalizedValue.setValues(fStringBuffer2);
        // quote
        int cquote=fEntityScanner.scanChar(null);
        if(cquote!=quote){
            reportFatalError("CloseQuoteExpected",new Object[]{eleName,atName});
        }
        return nonNormalizedValue.equals(value.ch,value.offset,value.length);
    } // scanAttributeValue()

    protected void normalizeWhitespace(XMLString value,int fromIndex){
        int end=value.offset+value.length;
        for(int i=value.offset+fromIndex;i<end;++i){
            int c=value.ch[i];
            if(XMLChar.isSpace(c)){
                value.ch[i]=' ';
            }
        }
    }

    protected int isUnchangedByNormalization(XMLString value){
        int end=value.offset+value.length;
        for(int i=value.offset;i<end;++i){
            int c=value.ch[i];
            if(XMLChar.isSpace(c)){
                return i-value.offset;
            }
        }
        return -1;
    }

    //
    // XMLScanner methods
    //
    // NOTE:  this is a carbon copy of the code in XML11DTDScannerImpl;
    // we need to override these methods in both places.
    // this needs to be refactored!!!  - NG
    protected boolean scanPubidLiteral(XMLString literal)
            throws IOException, XNIException{
        int quote=fEntityScanner.scanChar(null);
        if(quote!='\''&&quote!='"'){
            reportFatalError("QuoteRequiredInPublicID",null);
            return false;
        }
        fStringBuffer.clear();
        // skip leading whitespace
        boolean skipSpace=true;
        boolean dataok=true;
        while(true){
            int c=fEntityScanner.scanChar(null);
            // REVISIT:  none of these except \n and 0x20 should make it past the entity scanner
            if(c==' '||c=='\n'||c=='\r'||c==0x85||c==0x2028){
                if(!skipSpace){
                    // take the first whitespace as a space and skip the others
                    fStringBuffer.append(' ');
                    skipSpace=true;
                }
            }else if(c==quote){
                if(skipSpace){
                    // if we finished on a space let's trim it
                    fStringBuffer.length--;
                }
                literal.setValues(fStringBuffer);
                break;
            }else if(XMLChar.isPubid(c)){
                fStringBuffer.append((char)c);
                skipSpace=false;
            }else if(c==-1){
                reportFatalError("PublicIDUnterminated",null);
                return false;
            }else{
                dataok=false;
                reportFatalError("InvalidCharInPublicID",
                        new Object[]{Integer.toHexString(c)});
            }
        }
        return dataok;
    }

    protected void normalizeWhitespace(XMLString value){
        int end=value.offset+value.length;
        for(int i=value.offset;i<end;++i){
            int c=value.ch[i];
            if(XMLChar.isSpace(c)){
                value.ch[i]=' ';
            }
        }
    }

    // returns true if the given character is not
    // valid with respect to the version of
    // XML understood by this scanner.
    protected boolean isInvalid(int value){
        return (XML11Char.isXML11Invalid(value));
    } // isInvalid(int):  boolean

    // returns true if the given character is not
    // valid or may not be used outside a character reference
    // with respect to the version of XML understood by this scanner.
    protected boolean isInvalidLiteral(int value){
        return (!XML11Char.isXML11ValidLiteral(value));
    } // isInvalidLiteral(int):  boolean

    // returns true if the given character is
    // a valid nameChar with respect to the version of
    // XML understood by this scanner.
    protected boolean isValidNameChar(int value){
        return (XML11Char.isXML11Name(value));
    } // isValidNameChar(int):  boolean

    // returns true if the given character is
    // a valid NCName character with respect to the version of
    // XML understood by this scanner.
    protected boolean isValidNCName(int value){
        return (XML11Char.isXML11NCName(value));
    } // isValidNCName(int):  boolean

    // returns true if the given character is
    // a valid nameStartChar with respect to the version of
    // XML understood by this scanner.
    protected boolean isValidNameStartChar(int value){
        return (XML11Char.isXML11NameStart(value));
    } // isValidNameStartChar(int):  boolean

    protected boolean versionSupported(String version){
        return (version.equals("1.1")||version.equals("1.0"));
    } // versionSupported(String):  boolean

    // returns true if the given character is
    // a valid high surrogate for a nameStartChar
    // with respect to the version of XML understood
    // by this scanner.
    protected boolean isValidNameStartHighSurrogate(int value){
        return XML11Char.isXML11NameHighSurrogate(value);
    } // isValidNameStartHighSurrogate(int):  boolean

    // returns the error message key for unsupported
    // versions of XML with respect to the version of
    // XML understood by this scanner.
    protected String getVersionNotSupportedKey(){
        return "VersionNotSupported11";
    } // getVersionNotSupportedKey: String
} // class XML11DocumentScannerImpl
