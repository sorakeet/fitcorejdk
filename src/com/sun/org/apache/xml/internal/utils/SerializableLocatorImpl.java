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
 * $Id: SerializableLocatorImpl.java,v 1.2.4.1 2005/09/15 08:15:54 suresh_emailid Exp $
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
 * $Id: SerializableLocatorImpl.java,v 1.2.4.1 2005/09/15 08:15:54 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class SerializableLocatorImpl
        implements org.xml.sax.Locator, java.io.Serializable{
    static final long serialVersionUID=-2660312888446371460L;
    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////
    private String publicId;
    private String systemId;
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Locator
    ////////////////////////////////////////////////////////////////////
    private int lineNumber;
    private int columnNumber;

    public SerializableLocatorImpl(){
    }

    public SerializableLocatorImpl(org.xml.sax.Locator locator){
        setPublicId(locator.getPublicId());
        setSystemId(locator.getSystemId());
        setLineNumber(locator.getLineNumber());
        setColumnNumber(locator.getColumnNumber());
    }
    ////////////////////////////////////////////////////////////////////
    // Setters for the properties (not in org.xml.sax.Locator)
    ////////////////////////////////////////////////////////////////////

    public String getPublicId(){
        return publicId;
    }

    public String getSystemId(){
        return systemId;
    }

    public int getLineNumber(){
        return lineNumber;
    }

    public int getColumnNumber(){
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber){
        this.columnNumber=columnNumber;
    }

    public void setLineNumber(int lineNumber){
        this.lineNumber=lineNumber;
    }

    public void setSystemId(String systemId){
        this.systemId=systemId;
    }

    public void setPublicId(String publicId){
        this.publicId=publicId;
    }
}
// end of LocatorImpl.java
