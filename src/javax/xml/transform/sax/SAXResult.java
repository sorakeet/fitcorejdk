/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.Result;

public class SAXResult implements Result{
    public static final String FEATURE=
            "http://javax.xml.transform.sax.SAXResult/feature";
    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////
    private ContentHandler handler;
    private LexicalHandler lexhandler;
    private String systemId;

    public SAXResult(){
    }

    public SAXResult(ContentHandler handler){
        setHandler(handler);
    }

    public ContentHandler getHandler(){
        return handler;
    }

    public void setHandler(ContentHandler handler){
        this.handler=handler;
    }    public void setSystemId(String systemId){
        this.systemId=systemId;
    }

    public LexicalHandler getLexicalHandler(){
        return lexhandler;
    }    public String getSystemId(){
        return systemId;
    }

    public void setLexicalHandler(LexicalHandler handler){
        this.lexhandler=handler;
    }


}
