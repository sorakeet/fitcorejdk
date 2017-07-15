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
 * $Id: SAXSourceLocator.java,v 1.2.4.1 2005/09/15 08:15:52 suresh_emailid Exp $
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
 * $Id: SAXSourceLocator.java,v 1.2.4.1 2005/09/15 08:15:52 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.transform.SourceLocator;
import java.io.Serializable;

public class SAXSourceLocator extends LocatorImpl
        implements SourceLocator, Serializable{
    static final long serialVersionUID=3181680946321164112L;
    Locator m_locator;

    public SAXSourceLocator(){
    }

    public SAXSourceLocator(Locator locator){
        m_locator=locator;
        this.setColumnNumber(locator.getColumnNumber());
        this.setLineNumber(locator.getLineNumber());
        this.setPublicId(locator.getPublicId());
        this.setSystemId(locator.getSystemId());
    }

    public SAXSourceLocator(SourceLocator locator){
        m_locator=null;
        this.setColumnNumber(locator.getColumnNumber());
        this.setLineNumber(locator.getLineNumber());
        this.setPublicId(locator.getPublicId());
        this.setSystemId(locator.getSystemId());
    }

    public SAXSourceLocator(SAXParseException spe){
        this.setLineNumber(spe.getLineNumber());
        this.setColumnNumber(spe.getColumnNumber());
        this.setPublicId(spe.getPublicId());
        this.setSystemId(spe.getSystemId());
    }

    public String getPublicId(){
        return (null==m_locator)?super.getPublicId():m_locator.getPublicId();
    }

    public String getSystemId(){
        return (null==m_locator)?super.getSystemId():m_locator.getSystemId();
    }

    public int getLineNumber(){
        return (null==m_locator)?super.getLineNumber():m_locator.getLineNumber();
    }

    public int getColumnNumber(){
        return (null==m_locator)?super.getColumnNumber():m_locator.getColumnNumber();
    }
}
