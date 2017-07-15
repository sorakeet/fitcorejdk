/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: SerializationHandler.java,v 1.2.4.1 2005/09/15 08:15:22 suresh_emailid Exp $
 */
/**
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: SerializationHandler.java,v 1.2.4.1 2005/09/15 08:15:22 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;

import javax.xml.transform.Transformer;
import java.io.IOException;

public interface SerializationHandler
        extends
        ExtendedContentHandler,
        ExtendedLexicalHandler,
        XSLOutputAttributes,
        DeclHandler,
        org.xml.sax.DTDHandler,
        ErrorHandler,
        DOMSerializer,
        Serializer{
    public void setContentHandler(ContentHandler ch);

    public void close();

    public void serialize(Node node) throws IOException;

    public boolean setEscaping(boolean escape) throws SAXException;

    public void setIndentAmount(int spaces);

    public Transformer getTransformer();

    public void setTransformer(Transformer transformer);

    public void setNamespaceMappings(NamespaceMappings mappings);

    public void flushPending() throws SAXException;

    public void setDTDEntityExpansion(boolean expand);

    public void setIsStandalone(boolean b);
}
