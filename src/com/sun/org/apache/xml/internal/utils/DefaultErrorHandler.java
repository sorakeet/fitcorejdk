/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: DefaultErrorHandler.java,v 1.2.4.1 2005/09/15 08:15:43 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: DefaultErrorHandler.java,v 1.2.4.1 2005/09/15 08:15:43 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class DefaultErrorHandler implements ErrorHandler, ErrorListener{
    PrintWriter m_pw;
    boolean m_throwExceptionOnError=true;

    public DefaultErrorHandler(PrintWriter pw){
        m_pw=pw;
    }

    public DefaultErrorHandler(PrintStream pw){
        m_pw=new PrintWriter(pw,true);
    }

    public DefaultErrorHandler(){
        this(true);
    }

    public DefaultErrorHandler(boolean throwExceptionOnError){
        m_pw=new PrintWriter(System.err,true);
        m_throwExceptionOnError=throwExceptionOnError;
    }

    public static void ensureLocationSet(TransformerException exception){
        // SourceLocator locator = exception.getLocator();
        SourceLocator locator=null;
        Throwable cause=exception;
        // Try to find the locator closest to the cause.
        do{
            if(cause instanceof SAXParseException){
                locator=new SAXSourceLocator((SAXParseException)cause);
            }else if(cause instanceof TransformerException){
                SourceLocator causeLocator=((TransformerException)cause).getLocator();
                if(null!=causeLocator)
                    locator=causeLocator;
            }
            if(cause instanceof TransformerException)
                cause=((TransformerException)cause).getCause();
            else if(cause instanceof SAXException)
                cause=((SAXException)cause).getException();
            else
                cause=null;
        }
        while(null!=cause);
        exception.setLocator(locator);
    }

    public static void printLocation(PrintStream pw,TransformerException exception){
        printLocation(new PrintWriter(pw),exception);
    }

    public static void printLocation(PrintStream pw,SAXParseException exception){
        printLocation(new PrintWriter(pw),exception);
    }

    public void warning(SAXParseException exception) throws SAXException{
        printLocation(m_pw,exception);
        m_pw.println("Parser warning: "+exception.getMessage());
    }

    public void error(SAXParseException exception) throws SAXException{
        //printLocation(exception);
        // m_pw.println(exception.getMessage());
        throw exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException{
        // printLocation(exception);
        // m_pw.println(exception.getMessage());
        throw exception;
    }

    public static void printLocation(PrintWriter pw,Throwable exception){
        SourceLocator locator=null;
        Throwable cause=exception;
        // Try to find the locator closest to the cause.
        do{
            if(cause instanceof SAXParseException){
                locator=new SAXSourceLocator((SAXParseException)cause);
            }else if(cause instanceof TransformerException){
                SourceLocator causeLocator=((TransformerException)cause).getLocator();
                if(null!=causeLocator)
                    locator=causeLocator;
            }
            if(cause instanceof TransformerException)
                cause=((TransformerException)cause).getCause();
            else if(cause instanceof WrappedRuntimeException)
                cause=((WrappedRuntimeException)cause).getException();
            else if(cause instanceof SAXException)
                cause=((SAXException)cause).getException();
            else
                cause=null;
        }
        while(null!=cause);
        if(null!=locator){
            // m_pw.println("Parser fatal error: "+exception.getMessage());
            String id=(null!=locator.getPublicId())
                    ?locator.getPublicId()
                    :(null!=locator.getSystemId())
                    ?locator.getSystemId():XMLMessages.createXMLMessage(XMLErrorResources.ER_SYSTEMID_UNKNOWN,null); //"SystemId Unknown";
            pw.print(id+"; "+XMLMessages.createXMLMessage("line",null)+locator.getLineNumber()
                    +"; "+XMLMessages.createXMLMessage("column",null)+locator.getColumnNumber()+"; ");
        }else
            pw.print("("+XMLMessages.createXMLMessage(XMLErrorResources.ER_LOCATION_UNKNOWN,null)+")");
    }

    public void warning(TransformerException exception) throws TransformerException{
        printLocation(m_pw,exception);
        m_pw.println(exception.getMessage());
    }

    public void error(TransformerException exception) throws TransformerException{
        // If the m_throwExceptionOnError flag is true, rethrow the exception.
        // Otherwise report the error to System.err.
        if(m_throwExceptionOnError)
            throw exception;
        else{
            printLocation(m_pw,exception);
            m_pw.println(exception.getMessage());
        }
    }

    public void fatalError(TransformerException exception) throws TransformerException{
        // If the m_throwExceptionOnError flag is true, rethrow the exception.
        // Otherwise report the error to System.err.
        if(m_throwExceptionOnError)
            throw exception;
        else{
            printLocation(m_pw,exception);
            m_pw.println(exception.getMessage());
        }
    }
}
