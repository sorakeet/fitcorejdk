/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX exception class.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: SAXParseException.java,v 1.2 2004/11/03 22:55:32 jsuttor Exp $
package org.xml.sax;

public class SAXParseException extends SAXException{
    //////////////////////////////////////////////////////////////////////
    // Constructors.
    //////////////////////////////////////////////////////////////////////
    // Added serialVersionUID to preserve binary compatibility
    static final long serialVersionUID=-5651165872476709336L;
    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////
    private String publicId;
    private String systemId;
    private int lineNumber;
    private int columnNumber;

    public SAXParseException(String message,Locator locator){
        super(message);
        if(locator!=null){
            init(locator.getPublicId(),locator.getSystemId(),
                    locator.getLineNumber(),locator.getColumnNumber());
        }else{
            init(null,null,-1,-1);
        }
    }

    private void init(String publicId,String systemId,
                      int lineNumber,int columnNumber){
        this.publicId=publicId;
        this.systemId=systemId;
        this.lineNumber=lineNumber;
        this.columnNumber=columnNumber;
    }

    public SAXParseException(String message,Locator locator,
                             Exception e){
        super(message,e);
        if(locator!=null){
            init(locator.getPublicId(),locator.getSystemId(),
                    locator.getLineNumber(),locator.getColumnNumber());
        }else{
            init(null,null,-1,-1);
        }
    }

    public SAXParseException(String message,String publicId,String systemId,
                             int lineNumber,int columnNumber){
        super(message);
        init(publicId,systemId,lineNumber,columnNumber);
    }

    public SAXParseException(String message,String publicId,String systemId,
                             int lineNumber,int columnNumber,Exception e){
        super(message,e);
        init(publicId,systemId,lineNumber,columnNumber);
    }

    public String getPublicId(){
        return this.publicId;
    }

    public String getSystemId(){
        return this.systemId;
    }

    public int getLineNumber(){
        return this.lineNumber;
    }

    public int getColumnNumber(){
        return this.columnNumber;
    }

    public String toString(){
        StringBuilder buf=new StringBuilder(getClass().getName());
        String message=getLocalizedMessage();
        if(publicId!=null) buf.append("publicId: ").append(publicId);
        if(systemId!=null) buf.append("; systemId: ").append(systemId);
        if(lineNumber!=-1) buf.append("; lineNumber: ").append(lineNumber);
        if(columnNumber!=-1) buf.append("; columnNumber: ").append(columnNumber);
        //append the exception message at the end
        if(message!=null) buf.append("; ").append(message);
        return buf.toString();
    }
}
// end of SAXParseException.java
