/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.*;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;

public abstract class DTDParser
        extends XMLGrammarParser
        implements XMLDTDHandler, XMLDTDContentModelHandler{
    //
    // Data
    //
    protected XMLDTDScanner fDTDScanner;
    //
    // Constructors
    //

    public DTDParser(SymbolTable symbolTable){
        super(symbolTable);
    }
    //
    // Methods
    //

    public DTDGrammar getDTDGrammar(){
        return null;
    } // getDTDGrammar
    //
    // XMLDTDHandler methods
    //

    public void startEntity(String name,String publicId,String systemId,
                            String encoding) throws XNIException{
    }

    public void textDecl(String version,String encoding) throws XNIException{
    }

    public void startDTD(XMLLocator locator,Augmentations augmentations)
            throws XNIException{
    }

    public void startExternalSubset(XMLResourceIdentifier identifier,
                                    Augmentations augmentations) throws XNIException{
    } // startExternalSubset

    public void endExternalSubset(Augmentations augmentations) throws XNIException{
    } // endExternalSubset

    public void comment(XMLString text,Augmentations augmentations) throws XNIException{
    } // comment

    public void processingInstruction(String target,XMLString data,
                                      Augmentations augmentations)
            throws XNIException{
    } // processingInstruction

    public void elementDecl(String name,String contentModel,
                            Augmentations augmentations)
            throws XNIException{
    } // elementDecl

    public void startAttlist(String elementName,
                             Augmentations augmentations) throws XNIException{
    } // startAttlist

    public void attributeDecl(String elementName,String attributeName,
                              String type,String[] enumeration,
                              String defaultType,XMLString defaultValue,
                              XMLString nonNormalizedDefaultValue,Augmentations augmentations)
            throws XNIException{
    } // attributeDecl

    public void endAttlist(Augmentations augmentations) throws XNIException{
    } // endAttlist

    public void internalEntityDecl(String name,XMLString text,
                                   XMLString nonNormalizedText,
                                   Augmentations augmentations)
            throws XNIException{
    } // internalEntityDecl(String,XMLString,XMLString)

    public void externalEntityDecl(String name,
                                   XMLResourceIdentifier identifier,
                                   Augmentations augmentations)
            throws XNIException{
    } // externalEntityDecl

    public void unparsedEntityDecl(String name,
                                   XMLResourceIdentifier identifier,
                                   String notation,Augmentations augmentations)
            throws XNIException{
    } // unparsedEntityDecl

    public void notationDecl(String name,XMLResourceIdentifier identifier,
                             Augmentations augmentations)
            throws XNIException{
    } // notationDecl

    public void startConditional(short type,Augmentations augmentations) throws XNIException{
    } // startConditional

    public void endConditional(Augmentations augmentations) throws XNIException{
    } // endConditional

    public void endDTD(Augmentations augmentations) throws XNIException{
    } // endDTD

    public void endEntity(String name,Augmentations augmentations) throws XNIException{
    }
    //
    // XMLDTDContentModelHandler methods
    //

    public void startContentModel(String elementName,short type)
            throws XNIException{
    } // startContentModel

    public void mixedElement(String elementName) throws XNIException{
    } // mixedElement

    public void childrenStartGroup() throws XNIException{
    } // childrenStartGroup

    public void childrenElement(String elementName) throws XNIException{
    } // childrenElement

    public void childrenSeparator(short separator) throws XNIException{
    } // childrenSeparator

    public void childrenOccurrence(short occurrence) throws XNIException{
    } // childrenOccurrence

    public void childrenEndGroup() throws XNIException{
    } // childrenEndGroup

    public void endContentModel() throws XNIException{
    } // endContentModel
} // class DTDParser
