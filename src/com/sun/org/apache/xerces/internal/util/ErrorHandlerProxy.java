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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class ErrorHandlerProxy implements ErrorHandler{
    public void warning(SAXParseException e) throws SAXException{
        XMLErrorHandler eh=getErrorHandler();
        if(eh instanceof ErrorHandlerWrapper){
            ((ErrorHandlerWrapper)eh).fErrorHandler.warning(e);
        }else{
            eh.warning("","",ErrorHandlerWrapper.createXMLParseException(e));
        }
    }

    public void error(SAXParseException e) throws SAXException{
        XMLErrorHandler eh=getErrorHandler();
        if(eh instanceof ErrorHandlerWrapper){
            ((ErrorHandlerWrapper)eh).fErrorHandler.error(e);
        }else{
            eh.error("","",ErrorHandlerWrapper.createXMLParseException(e));
        }
        // if an XNIException is thrown, just let it go.
        // REVISIT: is this OK? or should we try to wrap it into SAXException?
    }

    public void fatalError(SAXParseException e) throws SAXException{
        XMLErrorHandler eh=getErrorHandler();
        if(eh instanceof ErrorHandlerWrapper){
            ((ErrorHandlerWrapper)eh).fErrorHandler.fatalError(e);
        }else{
            eh.fatalError("","",ErrorHandlerWrapper.createXMLParseException(e));
        }
    }

    protected abstract XMLErrorHandler getErrorHandler();
}
