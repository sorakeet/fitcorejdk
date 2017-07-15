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
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;
// REVISIT: the implementation of ErrorReporter.
//          we probably should not pass XMLParseException
//

public class DOMErrorImpl implements DOMError{
    //
    // Data
    //
    public short fSeverity=DOMError.SEVERITY_WARNING;
    public String fMessage=null;
    public DOMLocatorImpl fLocator=new DOMLocatorImpl();
    public Exception fException=null;
    public String fType;
    public Object fRelatedData;
    //
    // Constructors
    //

    public DOMErrorImpl(){
    }

    public DOMErrorImpl(short severity,XMLParseException exception){
        fSeverity=severity;
        fException=exception;
        fLocator=createDOMLocator(exception);
    }

    // method to get the DOMLocator Object
    private DOMLocatorImpl createDOMLocator(XMLParseException exception){
        // assuming DOMLocator wants the *expanded*, not the literal, URI of the doc... - neilg
        return new DOMLocatorImpl(exception.getLineNumber(),
                exception.getColumnNumber(),
                exception.getCharacterOffset(),
                exception.getExpandedSystemId());
    } // createDOMLocator()

    public short getSeverity(){
        return fSeverity;
    }

    public String getMessage(){
        return fMessage;
    }

    public String getType(){
        return fType;
    }

    public Object getRelatedException(){
        return fException;
    }

    public Object getRelatedData(){
        return fRelatedData;
    }

    public DOMLocator getLocation(){
        return fLocator;
    }

    public void reset(){
        fSeverity=DOMError.SEVERITY_WARNING;
        fException=null;
    }
}// class DOMErrorImpl
