/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2004 The Apache Software Foundation.
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
 * $Id: ListingErrorHandler.java,v 1.2.4.1 2005/09/15 08:15:46 suresh_emailid Exp $
 */
/**
 * Copyright 2000-2004 The Apache Software Foundation.
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
 * $Id: ListingErrorHandler.java,v 1.2.4.1 2005/09/15 08:15:46 suresh_emailid Exp $
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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class ListingErrorHandler implements ErrorHandler, ErrorListener{
    protected PrintWriter m_pw=null;
    protected boolean throwOnWarning=false;
    protected boolean throwOnError=true;
    protected boolean throwOnFatalError=true;

    public ListingErrorHandler(PrintWriter pw){
        if(null==pw)
            throw new NullPointerException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER,null));
        // "ListingErrorHandler created with null PrintWriter!");
        m_pw=pw;
    }

    public ListingErrorHandler(){
        m_pw=new PrintWriter(System.err,true);
    }

    public void warning(SAXParseException exception)
            throws SAXException{
        logExceptionLocation(m_pw,exception);
        // Note: should we really call .toString() below, since
        //  sometimes the message is not properly set?
        m_pw.println("warning: "+exception.getMessage());
        m_pw.flush();
        if(getThrowOnWarning())
            throw exception;
    }

    public void error(SAXParseException exception)
            throws SAXException{
        logExceptionLocation(m_pw,exception);
        m_pw.println("error: "+exception.getMessage());
        m_pw.flush();
        if(getThrowOnError())
            throw exception;
    }

    public void fatalError(SAXParseException exception)
            throws SAXException{
        logExceptionLocation(m_pw,exception);
        m_pw.println("fatalError: "+exception.getMessage());
        m_pw.flush();
        if(getThrowOnFatalError())
            throw exception;
    }

    public boolean getThrowOnFatalError(){
        return throwOnFatalError;
    }

    public void setThrowOnFatalError(boolean b){
        throwOnFatalError=b;
    }

    public boolean getThrowOnError(){
        return throwOnError;
    }

    public void setThrowOnError(boolean b){
        throwOnError=b;
    }

    public static void logExceptionLocation(PrintWriter pw,Throwable exception){
        if(null==pw)
            pw=new PrintWriter(System.err,true);
        SourceLocator locator=null;
        Throwable cause=exception;
        // Try to find the locator closest to the cause.
        do{
            // Find the current locator, if one present
            if(cause instanceof SAXParseException){
                // A SAXSourceLocator is a Xalan helper class
                //  that implements both a SourceLocator and a SAX Locator
                //@todo check that the new locator actually has
                //  as much or more information as the
                //  current one already does
                locator=new SAXSourceLocator((SAXParseException)cause);
            }else if(cause instanceof TransformerException){
                SourceLocator causeLocator=((TransformerException)cause).getLocator();
                if(null!=causeLocator){
                    locator=causeLocator;
                }
            }
            // Then walk back down the chain of exceptions
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
        // Formatting note: mimic javac-like errors:
        //  path\filename:123: message-here
        //  systemId:L=1;C=2: message-here
        if(null!=locator){
            String id=(locator.getPublicId()!=locator.getPublicId())
                    ?locator.getPublicId()
                    :(null!=locator.getSystemId())
                    ?locator.getSystemId():"SystemId-Unknown";
            pw.print(id+":Line="+locator.getLineNumber()
                    +";Column="+locator.getColumnNumber()+": ");
            pw.println("exception:"+exception.getMessage());
            pw.println("root-cause:"
                    +((null!=cause)?cause.getMessage():"null"));
            logSourceLine(pw,locator);
        }else{
            pw.print("SystemId-Unknown:locator-unavailable: ");
            pw.println("exception:"+exception.getMessage());
            pw.println("root-cause:"
                    +((null!=cause)?cause.getMessage():"null"));
        }
    }

    public static void logSourceLine(PrintWriter pw,SourceLocator locator){
        if(null==locator)
            return;
        if(null==pw)
            pw=new PrintWriter(System.err,true);
        String url=locator.getSystemId();
        // Bail immediately if we get SystemId-Unknown
        //@todo future improvement: attempt to get resource
        //  from a publicId if possible
        if(null==url){
            pw.println("line: (No systemId; cannot read file)");
            pw.println();
            return;
        }
        //@todo attempt to get DOM backpointer or other ids
        try{
            int line=locator.getLineNumber();
            int column=locator.getColumnNumber();
            pw.println("line: "+getSourceLine(url,line));
            StringBuffer buf=new StringBuffer("line: ");
            for(int i=1;i<column;i++){
                buf.append(' ');
            }
            buf.append('^');
            pw.println(buf.toString());
        }catch(Exception e){
            pw.println("line: logSourceLine unavailable due to: "+e.getMessage());
            pw.println();
        }
    }

    protected static String getSourceLine(String sourceUrl,int lineNum)
            throws Exception{
        URL url=null;
        // Get a URL from the sourceUrl
        try{
            // Try to get a URL from it as-is
            url=new URL(sourceUrl);
        }catch(java.net.MalformedURLException mue){
            int indexOfColon=sourceUrl.indexOf(':');
            int indexOfSlash=sourceUrl.indexOf('/');
            if((indexOfColon!=-1)
                    &&(indexOfSlash!=-1)
                    &&(indexOfColon<indexOfSlash)){
                // The url is already absolute, but we could not get
                //  the system to form it, so bail
                throw mue;
            }else{
                // The url is relative, so attempt to get absolute
                url=new URL(SystemIDResolver.getAbsoluteURI(sourceUrl));
                // If this fails, allow the exception to propagate
            }
        }
        String line=null;
        InputStream is=null;
        BufferedReader br=null;
        try{
            // Open the URL and read to our specified line
            URLConnection uc=url.openConnection();
            is=uc.getInputStream();
            br=new BufferedReader(new InputStreamReader(is));
            // Not the most efficient way, but it works
            // (Feel free to patch to seek to the appropriate line)
            for(int i=1;i<=lineNum;i++){
                line=br.readLine();
            }
        }
        // Allow exceptions to propagate from here, but ensure
        //  streams are closed!
        finally{
            br.close();
            is.close();
        }
        // Return whatever we found
        return line;
    }

    public boolean getThrowOnWarning(){
        return throwOnWarning;
    }

    public void setThrowOnWarning(boolean b){
        throwOnWarning=b;
    }

    public void warning(TransformerException exception)
            throws TransformerException{
        logExceptionLocation(m_pw,exception);
        m_pw.println("warning: "+exception.getMessage());
        m_pw.flush();
        if(getThrowOnWarning())
            throw exception;
    }

    public void error(TransformerException exception)
            throws TransformerException{
        logExceptionLocation(m_pw,exception);
        m_pw.println("error: "+exception.getMessage());
        m_pw.flush();
        if(getThrowOnError())
            throw exception;
    }

    public void fatalError(TransformerException exception)
            throws TransformerException{
        logExceptionLocation(m_pw,exception);
        m_pw.println("error: "+exception.getMessage());
        m_pw.flush();
        if(getThrowOnError())
            throw exception;
    }
}
