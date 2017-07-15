/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLNSDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLNSDTDValidator;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;

public class IntegratedParserConfiguration
        extends StandardParserConfiguration{
    //
    // REVISIT: should this configuration depend on the others
    //          like DTD/Standard one?
    //
    protected XMLNSDocumentScannerImpl fNamespaceScanner;
    protected XMLDocumentScannerImpl fNonNSScanner;
    protected XMLDTDValidator fNonNSDTDValidator;
    //
    // Constructors
    //

    public IntegratedParserConfiguration(){
        this(null,null,null);
    } // <init>()

    public IntegratedParserConfiguration(SymbolTable symbolTable,
                                         XMLGrammarPool grammarPool,
                                         XMLComponentManager parentSettings){
        super(symbolTable,grammarPool,parentSettings);
        // create components
        fNonNSScanner=new XMLDocumentScannerImpl();
        fNonNSDTDValidator=new XMLDTDValidator();
        // add components
        addComponent((XMLComponent)fNonNSScanner);
        addComponent((XMLComponent)fNonNSDTDValidator);
    } // <init>(SymbolTable,XMLGrammarPool)

    public IntegratedParserConfiguration(SymbolTable symbolTable){
        this(symbolTable,null,null);
    } // <init>(SymbolTable)

    public IntegratedParserConfiguration(SymbolTable symbolTable,
                                         XMLGrammarPool grammarPool){
        this(symbolTable,grammarPool,null);
    } // <init>(SymbolTable,XMLGrammarPool)

    protected void configurePipeline(){
        // use XML 1.0 datatype library
        setProperty(DATATYPE_VALIDATOR_FACTORY,fDatatypeValidatorFactory);
        // setup DTD pipeline
        configureDTDPipeline();
        // setup document pipeline
        if(fFeatures.get(NAMESPACES)==Boolean.TRUE){
            fProperties.put(NAMESPACE_BINDER,fNamespaceBinder);
            fScanner=fNamespaceScanner;
            fProperties.put(DOCUMENT_SCANNER,fNamespaceScanner);
            if(fDTDValidator!=null){
                fProperties.put(DTD_VALIDATOR,fDTDValidator);
                fNamespaceScanner.setDTDValidator(fDTDValidator);
                fNamespaceScanner.setDocumentHandler(fDTDValidator);
                fDTDValidator.setDocumentSource(fNamespaceScanner);
                fDTDValidator.setDocumentHandler(fDocumentHandler);
                if(fDocumentHandler!=null){
                    fDocumentHandler.setDocumentSource(fDTDValidator);
                }
                fLastComponent=fDTDValidator;
            }else{
                fNamespaceScanner.setDocumentHandler(fDocumentHandler);
                fNamespaceScanner.setDTDValidator(null);
                if(fDocumentHandler!=null){
                    fDocumentHandler.setDocumentSource(fNamespaceScanner);
                }
                fLastComponent=fNamespaceScanner;
            }
        }else{
            fScanner=fNonNSScanner;
            fProperties.put(DOCUMENT_SCANNER,fNonNSScanner);
            if(fNonNSDTDValidator!=null){
                fProperties.put(DTD_VALIDATOR,fNonNSDTDValidator);
                fNonNSScanner.setDocumentHandler(fNonNSDTDValidator);
                fNonNSDTDValidator.setDocumentSource(fNonNSScanner);
                fNonNSDTDValidator.setDocumentHandler(fDocumentHandler);
                if(fDocumentHandler!=null){
                    fDocumentHandler.setDocumentSource(fNonNSDTDValidator);
                }
                fLastComponent=fNonNSDTDValidator;
            }else{
                fScanner.setDocumentHandler(fDocumentHandler);
                if(fDocumentHandler!=null){
                    fDocumentHandler.setDocumentSource(fScanner);
                }
                fLastComponent=fScanner;
            }
        }
        // setup document pipeline
        if(fFeatures.get(XMLSCHEMA_VALIDATION)==Boolean.TRUE){
            // If schema validator was not in the pipeline insert it.
            if(fSchemaValidator==null){
                fSchemaValidator=new XMLSchemaValidator();
                // add schema component
                fProperties.put(SCHEMA_VALIDATOR,fSchemaValidator);
                addComponent(fSchemaValidator);
                // add schema message formatter
                if(fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN)==null){
                    XSMessageFormatter xmft=new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,xmft);
                }
            }
            fLastComponent.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentSource(fLastComponent);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            if(fDocumentHandler!=null){
                fDocumentHandler.setDocumentSource(fSchemaValidator);
            }
            fLastComponent=fSchemaValidator;
        }
    } // configurePipeline()

    protected XMLDocumentScanner createDocumentScanner(){
        fNamespaceScanner=new XMLNSDocumentScannerImpl();
        return fNamespaceScanner;
    } // createDocumentScanner():XMLDocumentScanner

    protected XMLDTDValidator createDTDValidator(){
        return new XMLNSDTDValidator();
    } // createDTDValidator():XMLDTDValidator
} // class IntegratedParserConfiguration
