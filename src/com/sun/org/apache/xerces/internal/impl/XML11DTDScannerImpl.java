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

import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XML11Char;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;

import java.io.IOException;

public class XML11DTDScannerImpl
        extends XMLDTDScannerImpl{
    private XMLStringBuffer fStringBuffer=new XMLStringBuffer();
    //
    // Constructors
    //

    public XML11DTDScannerImpl(){
        super();
    } // <init>()

    public XML11DTDScannerImpl(SymbolTable symbolTable,
                               XMLErrorReporter errorReporter,XMLEntityManager entityManager){
        super(symbolTable,errorReporter,entityManager);
    }
    //
    // XMLDTDScanner methods
    //

    //
    // XMLScanner methods
    //
    // NOTE:  this is a carbon copy of the code in XML11DocumentScannerImpl;
    // we need to override these methods in both places.  Ah for
    // multiple inheritance...
    // This needs to be refactored!!!  - NG
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
            // REVISIT:  it could really only be \n or 0x20; all else is normalized, no?  - neilg
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
        return (!XML11Char.isXML11Valid(value));
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

    // note that, according to 4.3.4 of the XML 1.1 spec, XML 1.1
    // documents may invoke 1.0 entities; thus either version decl (or none!)
    // is allowed to appear in this context
    protected boolean versionSupported(String version){
        return version.equals("1.1")||version.equals("1.0");
    } // versionSupported(String):  boolean

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
} // class XML11DTDScannerImpl
