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
package com.sun.org.apache.xerces.internal.jaxp.validation;

import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;

final class Util{
    public static final XMLInputSource toXMLInputSource(StreamSource in){
        if(in.getReader()!=null)
            return new XMLInputSource(
                    in.getPublicId(),in.getSystemId(),in.getSystemId(),
                    in.getReader(),null);
        if(in.getInputStream()!=null)
            return new XMLInputSource(
                    in.getPublicId(),in.getSystemId(),in.getSystemId(),
                    in.getInputStream(),null);
        return new XMLInputSource(
                in.getPublicId(),in.getSystemId(),in.getSystemId());
    }

    public static SAXException toSAXException(XNIException e){
        if(e instanceof XMLParseException)
            return toSAXParseException((XMLParseException)e);
        if(e.getException() instanceof SAXException)
            return (SAXException)e.getException();
        return new SAXException(e.getMessage(),e.getException());
    }

    public static SAXParseException toSAXParseException(XMLParseException e){
        if(e.getException() instanceof SAXParseException)
            return (SAXParseException)e.getException();
        return new SAXParseException(e.getMessage(),
                e.getPublicId(),e.getExpandedSystemId(),
                e.getLineNumber(),e.getColumnNumber(),
                e.getException());
    }
} // Util
