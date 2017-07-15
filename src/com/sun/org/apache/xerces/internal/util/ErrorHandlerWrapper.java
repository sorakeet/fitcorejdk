/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * Copyright 2001-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ErrorHandlerWrapper
        implements XMLErrorHandler{
    //
    // Data
    //
    protected ErrorHandler fErrorHandler;
    //
    // Constructors
    //

    public ErrorHandlerWrapper(){
    }

    public ErrorHandlerWrapper(ErrorHandler errorHandler){
        setErrorHandler(errorHandler);
    } // <init>(ErrorHandler)
    //
    // Public methods
    //

    public ErrorHandler getErrorHandler(){
        return fErrorHandler;
    } // getErrorHandler():ErrorHandler

    public void setErrorHandler(ErrorHandler errorHandler){
        fErrorHandler=errorHandler;
    } // setErrorHandler(ErrorHandler)
    //
    // XMLErrorHandler methods
    //

    public void warning(String domain,String key,
                        XMLParseException exception) throws XNIException{
        if(fErrorHandler!=null){
            SAXParseException saxException=createSAXParseException(exception);
            try{
                fErrorHandler.warning(saxException);
            }catch(SAXParseException e){
                throw createXMLParseException(e);
            }catch(SAXException e){
                throw createXNIException(e);
            }
        }
    } // warning(String,String,XMLParseException)

    public void error(String domain,String key,
                      XMLParseException exception) throws XNIException{
        if(fErrorHandler!=null){
            SAXParseException saxException=createSAXParseException(exception);
            try{
                fErrorHandler.error(saxException);
            }catch(SAXParseException e){
                throw createXMLParseException(e);
            }catch(SAXException e){
                throw createXNIException(e);
            }
        }
    } // error(String,String,XMLParseException)

    public void fatalError(String domain,String key,
                           XMLParseException exception) throws XNIException{
        if(fErrorHandler!=null){
            SAXParseException saxException=createSAXParseException(exception);
            try{
                fErrorHandler.fatalError(saxException);
            }catch(SAXParseException e){
                throw createXMLParseException(e);
            }catch(SAXException e){
                throw createXNIException(e);
            }
        }
    } // fatalError(String,String,XMLParseException)
    //
    // Protected methods
    //

    protected static SAXParseException createSAXParseException(XMLParseException exception){
        return new SAXParseException(exception.getMessage(),
                exception.getPublicId(),
                exception.getExpandedSystemId(),
                exception.getLineNumber(),
                exception.getColumnNumber(),
                exception.getException());
    } // createSAXParseException(XMLParseException):SAXParseException

    protected static XMLParseException createXMLParseException(SAXParseException exception){
        final String fPublicId=exception.getPublicId();
        final String fExpandedSystemId=exception.getSystemId();
        final int fLineNumber=exception.getLineNumber();
        final int fColumnNumber=exception.getColumnNumber();
        XMLLocator location=new XMLLocator(){
            public String getPublicId(){
                return fPublicId;
            }

            public String getLiteralSystemId(){
                return null;
            }

            public String getBaseSystemId(){
                return null;
            }

            public String getExpandedSystemId(){
                return fExpandedSystemId;
            }

            public int getLineNumber(){
                return fLineNumber;
            }

            public int getColumnNumber(){
                return fColumnNumber;
            }

            public int getCharacterOffset(){
                return -1;
            }

            public String getEncoding(){
                return null;
            }

            public String getXMLVersion(){
                return null;
            }
        };
        return new XMLParseException(location,exception.getMessage(),exception);
    } // createXMLParseException(SAXParseException):XMLParseException

    protected static XNIException createXNIException(SAXException exception){
        return new XNIException(exception.getMessage(),exception);
    } // createXNIException(SAXException):XMLParseException
} // class ErrorHandlerWrapper
