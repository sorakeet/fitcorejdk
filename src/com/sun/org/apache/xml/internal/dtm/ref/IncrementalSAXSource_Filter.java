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
 * $Id: IncrementalSAXSource_Filter.java,v 1.2.4.1 2005/09/15 08:15:07 suresh_emailid Exp $
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
 * $Id: IncrementalSAXSource_Filter.java,v 1.2.4.1 2005/09/15 08:15:07 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;

import java.io.IOException;

final class IncrementalSAXSource_Filter
        implements IncrementalSAXSource, ContentHandler, DTDHandler, LexicalHandler, ErrorHandler, Runnable{
    boolean DEBUG=false; //Internal status report
    //
    // Data
    //
    private CoroutineManager fCoroutineManager=null;
    private int fControllerCoroutineID=-1;
    private int fSourceCoroutineID=-1;
    private ContentHandler clientContentHandler=null; // %REVIEW% support multiple?
    private LexicalHandler clientLexicalHandler=null; // %REVIEW% support multiple?
    private DTDHandler clientDTDHandler=null; // %REVIEW% support multiple?
    private ErrorHandler clientErrorHandler=null; // %REVIEW% support multiple?
    private int eventcounter;
    private int frequency=5;
    // Flag indicating that no more events should be delivered -- either
    // because input stream ran to completion (endDocument), or because
    // the user requested an early stop via deliverMoreNodes(false).
    private boolean fNoMoreEvents=false;
    // Support for startParse()
    private XMLReader fXMLReader=null;
    private InputSource fXMLReaderInputSource=null;
    //
    // Constructors
    //

    public IncrementalSAXSource_Filter(){
        this.init(new CoroutineManager(),-1,-1);
    }

    public void init(CoroutineManager co,int controllerCoroutineID,
                     int sourceCoroutineID){
        if(co==null)
            co=new CoroutineManager();
        fCoroutineManager=co;
        fControllerCoroutineID=co.co_joinCoroutineSet(controllerCoroutineID);
        fSourceCoroutineID=co.co_joinCoroutineSet(sourceCoroutineID);
        if(fControllerCoroutineID==-1||fSourceCoroutineID==-1)
            throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COJOINROUTINESET_FAILED,null)); //"co_joinCoroutineSet() failed");
        fNoMoreEvents=false;
        eventcounter=frequency;
    }

    public IncrementalSAXSource_Filter(CoroutineManager co,int controllerCoroutineID){
        this.init(co,controllerCoroutineID,-1);
    }
    //
    // Public methods
    //

    //
    // Factories
    //
    static public IncrementalSAXSource createIncrementalSAXSource(CoroutineManager co,int controllerCoroutineID){
        return new IncrementalSAXSource_Filter(co,controllerCoroutineID);
    }

    public void setXMLReader(XMLReader eventsource){
        fXMLReader=eventsource;
        eventsource.setContentHandler(this);
        eventsource.setDTDHandler(this);
        eventsource.setErrorHandler(this); // to report fatal errors in filtering mode
        // Not supported by all SAX2 filters:
        try{
            eventsource.
                    setProperty("http://xml.org/sax/properties/lexical-handler",
                            this);
        }catch(SAXNotRecognizedException e){
            // Nothing we can do about it
        }catch(SAXNotSupportedException e){
            // Nothing we can do about it
        }
        // Should we also bind as other varieties of handler?
        // (DTDHandler and so on)
    }

    // Register a content handler for us to output to
    public void setContentHandler(ContentHandler handler){
        clientContentHandler=handler;
    }

    // Register a lexical handler for us to output to
    // Not all filters support this...
    // ??? Should we register directly on the filter?
    // NOTE NAME -- subclassing issue in the Xerces version
    public void setLexicalHandler(LexicalHandler handler){
        clientLexicalHandler=handler;
    }

    // Register a DTD handler for us to output to
    public void setDTDHandler(DTDHandler handler){
        clientDTDHandler=handler;
    }

    public Object deliverMoreNodes(boolean parsemore){
        // If parsing is already done, we can immediately say so
        if(fNoMoreEvents)
            return Boolean.FALSE;
        try{
            Object result=
                    fCoroutineManager.co_resume(parsemore?Boolean.TRUE:Boolean.FALSE,
                            fControllerCoroutineID,fSourceCoroutineID);
            if(result==Boolean.FALSE)
                fCoroutineManager.co_exit(fControllerCoroutineID);
            return result;
        }
        // SHOULD NEVER OCCUR, since the coroutine number and coroutine manager
        // are those previously established for this IncrementalSAXSource_Filter...
        // So I'm just going to return it as a parsing exception, for now.
        catch(NoSuchMethodException e){
            return e;
        }
    }

    public void startParse(InputSource source) throws SAXException{
        if(fNoMoreEvents)
            throw new SAXException(XMLMessages.createXMLMessage(XMLErrorResources.ER_INCRSAXSRCFILTER_NOT_RESTARTABLE,null)); //"IncrmentalSAXSource_Filter not currently restartable.");
        if(fXMLReader==null)
            throw new SAXException(XMLMessages.createXMLMessage(XMLErrorResources.ER_XMLRDR_NOT_BEFORE_STARTPARSE,null)); //"XMLReader not before startParse request");
        fXMLReaderInputSource=source;
        // Xalan thread pooling...
        // com.sun.org.apache.xalan.internal.transformer.TransformerImpl.runTransformThread(this);
        ThreadControllerWrapper.runThread(this,-1);
    }

    // Register an error handler for us to output to
    // NOTE NAME -- subclassing issue in the Xerces version
    public void setErrHandler(ErrorHandler handler){
        clientErrorHandler=handler;
    }

    // Set the number of events between resumes of our coroutine
    // Immediately resets number of events before _next_ resume as well.
    public void setReturnFrequency(int events){
        if(events<1) events=1;
        frequency=eventcounter=events;
    }

    public void setDocumentLocator(Locator locator){
        if(--eventcounter<=0){
            // This can cause a hang.  -sb
            // co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.setDocumentLocator(locator);
    }

    public void startDocument()
            throws SAXException{
        co_entry_pause();
        // Otherwise, begin normal event delivery
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.startDocument();
    }

    public void endDocument()
            throws SAXException{
        // EXCEPTION: In this case we need to run the event BEFORE we yield.
        if(clientContentHandler!=null)
            clientContentHandler.endDocument();
        eventcounter=0;
        co_yield(false);
    }

    public void startPrefixMapping(String prefix,String uri)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.startPrefixMapping(prefix,uri);
    }

    public void endPrefixMapping(String prefix)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.endPrefixMapping(prefix);
    }

    public void startElement(String namespaceURI,String localName,
                             String qName,Attributes atts)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.startElement(namespaceURI,localName,qName,atts);
    }

    public void endElement(String namespaceURI,String localName,
                           String qName)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.endElement(namespaceURI,localName,qName);
    }

    //
    // ContentHandler methods
    // These  pass the data to our client ContentHandler...
    // but they also count the number of events passing through,
    // and resume our coroutine each time that counter hits zero and
    // is reset.
    //
    // Note that for everything except endDocument and fatalError, we do the count-and-yield
    // BEFORE passing the call along. I'm hoping that this will encourage JIT
    // compilers to realize that these are tail-calls, reducing the expense of
    // the additional layer of data flow.
    //
    // %REVIEW% Glenn suggests that pausing after endElement, endDocument,
    // and characters may be sufficient. I actually may not want to
    // stop after characters, since in our application these wind up being
    // concatenated before they're processed... but that risks huge blocks of
    // text causing greater than usual readahead. (Unlikely? Consider the
    // possibility of a large base-64 block in a SOAP stream.)
    //
    public void characters(char[] ch,int start,int length)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.characters(ch,start,length);
    }

    public void ignorableWhitespace(char[] ch,int start,int length)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.ignorableWhitespace(ch,start,length);
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.processingInstruction(target,data);
    }

    public void skippedEntity(String name)
            throws SAXException{
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
        if(clientContentHandler!=null)
            clientContentHandler.skippedEntity(name);
    }

    private void co_yield(boolean moreRemains) throws SAXException{
        // Horrendous kluge to run filter to completion. See below.
        if(fNoMoreEvents)
            return;
        try // Coroutine manager might throw no-such.
        {
            Object arg=Boolean.FALSE;
            if(moreRemains){
                // Yield control, resume parsing when done
                arg=fCoroutineManager.co_resume(Boolean.TRUE,fSourceCoroutineID,
                        fControllerCoroutineID);
            }
            // If we're at end of document or were told to stop early
            if(arg==Boolean.FALSE){
                fNoMoreEvents=true;
                if(fXMLReader!=null)    // Running under startParseThread()
                    throw new StopException(); // We'll co_exit from there.
                // Yield control. We do NOT expect anyone to ever ask us again.
                fCoroutineManager.co_exit_to(Boolean.FALSE,fSourceCoroutineID,
                        fControllerCoroutineID);
            }
        }catch(NoSuchMethodException e){
            // Shouldn't happen unless we've miscoded our coroutine logic
            // "Shut down the garbage smashers on the detention level!"
            fNoMoreEvents=true;
            fCoroutineManager.co_exit(fSourceCoroutineID);
            throw new SAXException(e);
        }
    }

    private void co_entry_pause() throws SAXException{
        if(fCoroutineManager==null){
            // Nobody called init()? Do it now...
            init(null,-1,-1);
        }
        try{
            Object arg=fCoroutineManager.co_entry_pause(fSourceCoroutineID);
            if(arg==Boolean.FALSE)
                co_yield(false);
        }catch(NoSuchMethodException e){
            // Coroutine system says we haven't registered. That's an
            // application coding error, and is unrecoverable.
            if(DEBUG) e.printStackTrace();
            throw new SAXException(e);
        }
    }

    public void startDTD(String name,String publicId,
                         String systemId)
            throws SAXException{
        if(null!=clientLexicalHandler)
            clientLexicalHandler.startDTD(name,publicId,systemId);
    }

    public void endDTD()
            throws SAXException{
        if(null!=clientLexicalHandler)
            clientLexicalHandler.endDTD();
    }

    public void startEntity(String name)
            throws SAXException{
        if(null!=clientLexicalHandler)
            clientLexicalHandler.startEntity(name);
    }
    //
    // DTDHandler support.

    public void endEntity(String name)
            throws SAXException{
        if(null!=clientLexicalHandler)
            clientLexicalHandler.endEntity(name);
    }

    public void startCDATA()
            throws SAXException{
        if(null!=clientLexicalHandler)
            clientLexicalHandler.startCDATA();
    }

    public void endCDATA()
            throws SAXException{
        if(null!=clientLexicalHandler)
            clientLexicalHandler.endCDATA();
    }

    //
    // LexicalHandler support. Not all SAX2 filters support these events
    // but we may want to pass them through when they exist...
    //
    // %REVIEW% These do NOT currently affect the eventcounter; I'm asserting
    // that they're rare enough that it makes little or no sense to
    // pause after them. As such, it may make more sense for folks who
    // actually want to use them to register directly with the filter.
    // But I want 'em here for now, to remind us to recheck this assertion!
    //
    public void comment(char[] ch,int start,int length)
            throws SAXException{
        if(null!=clientLexicalHandler)
            clientLexicalHandler.comment(ch,start,length);
    }

    public void notationDecl(String a,String b,String c) throws SAXException{
        if(null!=clientDTDHandler)
            clientDTDHandler.notationDecl(a,b,c);
    }
    //
    // coroutine support
    //

    public void unparsedEntityDecl(String a,String b,String c,String d) throws SAXException{
        if(null!=clientDTDHandler)
            clientDTDHandler.unparsedEntityDecl(a,b,c,d);
    }

    public void warning(SAXParseException exception) throws SAXException{
        if(null!=clientErrorHandler)
            clientErrorHandler.error(exception);
    }

    //
    // ErrorHandler support.
    //
    // PROBLEM: Xerces is apparently _not_ calling the ErrorHandler for
    // exceptions thrown by the ContentHandler, which prevents us from
    // handling this properly when running in filtering mode with Xerces
    // as our event source.  It's unclear whether this is a Xerces bug
    // or a SAX design flaw.
    //
    // %REVIEW% Current solution: In filtering mode, it is REQUIRED that
    // event source make sure this method is invoked if the event stream
    // abends before endDocument is delivered. If that means explicitly calling
    // us in the exception handling code because it won't be delivered as part
    // of the normal SAX ErrorHandler stream, that's fine; Not Our Problem.
    //
    public void error(SAXParseException exception) throws SAXException{
        if(null!=clientErrorHandler)
            clientErrorHandler.error(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException{
        // EXCEPTION: In this case we need to run the event BEFORE we yield --
        // just as with endDocument, this terminates the event stream.
        if(null!=clientErrorHandler)
            clientErrorHandler.error(exception);
        eventcounter=0;
        co_yield(false);
    }

    public int getSourceCoroutineID(){
        return fSourceCoroutineID;
    }

    public int getControllerCoroutineID(){
        return fControllerCoroutineID;
    }
    //
    // Convenience: Run an XMLReader in a thread
    //

    public CoroutineManager getCoroutineManager(){
        return fCoroutineManager;
    }

    protected void count_and_yield(boolean moreExpected) throws SAXException{
        if(!moreExpected) eventcounter=0;
        if(--eventcounter<=0){
            co_yield(true);
            eventcounter=frequency;
        }
    }

    public void run(){
        // Guard against direct invocation of start().
        if(fXMLReader==null) return;
        if(DEBUG) System.out.println("IncrementalSAXSource_Filter parse thread launched");
        // Initially assume we'll run successfully.
        Object arg=Boolean.FALSE;
        // For the duration of this operation, all coroutine handshaking
        // will occur in the co_yield method. That's the nice thing about
        // coroutines; they give us a way to hand off control from the
        // middle of a synchronous method.
        try{
            fXMLReader.parse(fXMLReaderInputSource);
        }catch(IOException ex){
            arg=ex;
        }catch(StopException ex){
            // Expected and harmless
            if(DEBUG) System.out.println("Active IncrementalSAXSource_Filter normal stop exception");
        }catch(SAXException ex){
            Exception inner=ex.getException();
            if(inner instanceof StopException){
                // Expected and harmless
                if(DEBUG) System.out.println("Active IncrementalSAXSource_Filter normal stop exception");
            }else{
                // Unexpected malfunction
                if(DEBUG){
                    System.out.println("Active IncrementalSAXSource_Filter UNEXPECTED SAX exception: "+inner);
                    inner.printStackTrace();
                }
                arg=ex;
            }
        } // end parse
        // Mark as no longer running in thread.
        fXMLReader=null;
        try{
            // Mark as done and yield control to the controller coroutine
            fNoMoreEvents=true;
            fCoroutineManager.co_exit_to(arg,fSourceCoroutineID,
                    fControllerCoroutineID);
        }catch(NoSuchMethodException e){
            // Shouldn't happen unless we've miscoded our coroutine logic
            // "CPO, shut down the garbage smashers on the detention level!"
            e.printStackTrace(System.err);
            fCoroutineManager.co_exit(fSourceCoroutineID);
        }
    }

    class StopException extends RuntimeException{
        static final long serialVersionUID=-1129245796185754956L;
    }
    //================================================================
    /** Simple unit test. Attempt coroutine parsing of document indicated
     * by first argument (as a URI), report progress.
     */
    /**
     public static void _main(String args[])
     {
     System.out.println("Starting...");

     org.xml.sax.XMLReader theSAXParser=
     new com.sun.org.apache.xerces.internal.parsers.SAXParser();


     for(int arg=0;arg<args.length;++arg)
     {
     // The filter is not currently designed to be restartable
     // after a parse has ended. Generate a new one each time.
     IncrementalSAXSource_Filter filter=
     new IncrementalSAXSource_Filter();
     // Use a serializer as our sample output
     com.sun.org.apache.xml.internal.serialize.XMLSerializer trace;
     trace=new com.sun.org.apache.xml.internal.serialize.XMLSerializer(System.out,null);
     filter.setContentHandler(trace);
     filter.setLexicalHandler(trace);

     try
     {
     InputSource source = new InputSource(args[arg]);
     Object result=null;
     boolean more=true;

     // init not issued; we _should_ automagically Do The Right Thing

     // Bind parser, kick off parsing in a thread
     filter.setXMLReader(theSAXParser);
     filter.startParse(source);

     for(result = filter.deliverMoreNodes(more);
     (result instanceof Boolean && ((Boolean)result)==Boolean.TRUE);
     result = filter.deliverMoreNodes(more))
     {
     System.out.println("\nSome parsing successful, trying more.\n");

     // Special test: Terminate parsing early.
     if(arg+1<args.length && "!".equals(args[arg+1]))
     {
     ++arg;
     more=false;
     }

     }

     if (result instanceof Boolean && ((Boolean)result)==Boolean.FALSE)
     {
     System.out.println("\nFilter ended (EOF or on request).\n");
     }
     else if (result == null) {
     System.out.println("\nUNEXPECTED: Filter says shut down prematurely.\n");
     }
     else if (result instanceof Exception) {
     System.out.println("\nFilter threw exception:");
     ((Exception)result).printStackTrace();
     }

     }
     catch(SAXException e)
     {
     e.printStackTrace();
     }
     } // end for
     }
     */
} // class IncrementalSAXSource_Filter
