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
package com.sun.org.apache.xerces.internal.xni.parser;

import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;

public class XMLParseException
        extends XNIException{
    static final long serialVersionUID=1732959359448549967L;
    //
    // Data
    //
    protected String fPublicId;
    protected String fLiteralSystemId;
    protected String fExpandedSystemId;
    protected String fBaseSystemId;
    protected int fLineNumber=-1;
    protected int fColumnNumber=-1;
    protected int fCharacterOffset=-1;
    //
    // Constructors
    //

    public XMLParseException(XMLLocator locator,String message){
        super(message);
        if(locator!=null){
            fPublicId=locator.getPublicId();
            fLiteralSystemId=locator.getLiteralSystemId();
            fExpandedSystemId=locator.getExpandedSystemId();
            fBaseSystemId=locator.getBaseSystemId();
            fLineNumber=locator.getLineNumber();
            fColumnNumber=locator.getColumnNumber();
            fCharacterOffset=locator.getCharacterOffset();
        }
    } // <init>(XMLLocator,String)

    public XMLParseException(XMLLocator locator,
                             String message,Exception exception){
        super(message,exception);
        if(locator!=null){
            fPublicId=locator.getPublicId();
            fLiteralSystemId=locator.getLiteralSystemId();
            fExpandedSystemId=locator.getExpandedSystemId();
            fBaseSystemId=locator.getBaseSystemId();
            fLineNumber=locator.getLineNumber();
            fColumnNumber=locator.getColumnNumber();
            fCharacterOffset=locator.getCharacterOffset();
        }
    } // <init>(XMLLocator,String,Exception)
    //
    // Public methods
    //

    public String getPublicId(){
        return fPublicId;
    } // getPublicId():String

    public String getExpandedSystemId(){
        return fExpandedSystemId;
    } // getExpandedSystemId():String

    public String getLiteralSystemId(){
        return fLiteralSystemId;
    } // getLiteralSystemId():String

    public String getBaseSystemId(){
        return fBaseSystemId;
    } // getBaseSystemId():String

    public int getLineNumber(){
        return fLineNumber;
    } // getLineNumber():int

    public int getColumnNumber(){
        return fColumnNumber;
    } // getRowNumber():int

    public int getCharacterOffset(){
        return fCharacterOffset;
    } // getCharacterOffset():int
    //
    // Object methods
    //

    public String toString(){
        StringBuffer str=new StringBuffer();
        if(fPublicId!=null){
            str.append(fPublicId);
        }
        str.append(':');
        if(fLiteralSystemId!=null){
            str.append(fLiteralSystemId);
        }
        str.append(':');
        if(fExpandedSystemId!=null){
            str.append(fExpandedSystemId);
        }
        str.append(':');
        if(fBaseSystemId!=null){
            str.append(fBaseSystemId);
        }
        str.append(':');
        str.append(fLineNumber);
        str.append(':');
        str.append(fColumnNumber);
        str.append(':');
        str.append(fCharacterOffset);
        str.append(':');
        String message=getMessage();
        if(message==null){
            Exception exception=getException();
            if(exception!=null){
                message=exception.getMessage();
            }
        }
        if(message!=null){
            str.append(message);
        }
        return str.toString();
    } // toString():String
} // XMLParseException
