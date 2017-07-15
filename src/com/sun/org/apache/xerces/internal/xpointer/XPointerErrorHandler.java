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

import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;

import java.io.PrintWriter;

class XPointerErrorHandler implements XMLErrorHandler{
    //
    // Data
    //
    protected PrintWriter fOut;
    //
    // Constructors
    //

    public XPointerErrorHandler(){
        this(new PrintWriter(System.err));
    } // <init>()

    public XPointerErrorHandler(PrintWriter out){
        fOut=out;
    } // <init>(PrintWriter)
    //
    // ErrorHandler methods
    //

    public void warning(String domain,String key,XMLParseException ex)
            throws XNIException{
        printError("Warning",ex);
    } // warning(XMLParseException)

    public void error(String domain,String key,XMLParseException ex)
            throws XNIException{
        printError("Error",ex);
        //throw ex;
    } // error(XMLParseException)

    public void fatalError(String domain,String key,XMLParseException ex)
            throws XNIException{
        printError("Fatal Error",ex);
        throw ex;
    } // fatalError(XMLParseException)
    //
    // Private methods
    //

    private void printError(String type,XMLParseException ex){
        fOut.print("[");
        fOut.print(type);
        fOut.print("] ");
        String systemId=ex.getExpandedSystemId();
        if(systemId!=null){
            int index=systemId.lastIndexOf('/');
            if(index!=-1)
                systemId=systemId.substring(index+1);
            fOut.print(systemId);
        }
        fOut.print(':');
        fOut.print(ex.getLineNumber());
        fOut.print(':');
        fOut.print(ex.getColumnNumber());
        fOut.print(": ");
        fOut.print(ex.getMessage());
        fOut.println();
        fOut.flush();
    } // printError(String,SAXParseException)
} // class DefaultErrorHandler
