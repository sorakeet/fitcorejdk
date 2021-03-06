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
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.IOException;

public abstract class XMLParser{
    //
    // Constants
    //
    // properties
    protected static final String ENTITY_RESOLVER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String ERROR_HANDLER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_HANDLER_PROPERTY;
    private static final String[] RECOGNIZED_PROPERTIES={
            ENTITY_RESOLVER,
            ERROR_HANDLER,
    };
    //
    // Data
    //
    protected XMLParserConfiguration fConfiguration;
    XMLSecurityManager securityManager;
    XMLSecurityPropertyManager securityPropertyManager;
    //
    // Constructors
    //

    protected XMLParser(XMLParserConfiguration config){
        // save configuration
        fConfiguration=config;
        // add default recognized properties
        fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
    } // <init>(XMLParserConfiguration)

    public boolean getFeature(String featureId)
            throws SAXNotSupportedException, SAXNotRecognizedException{
        return fConfiguration.getFeature(featureId);
    }
    //
    // Public methods
    //

    public void parse(XMLInputSource inputSource)
            throws XNIException, IOException{
        // null indicates that the parser is called directly, initialize them
        if(securityManager==null){
            securityManager=new XMLSecurityManager(true);
            fConfiguration.setProperty(Constants.SECURITY_MANAGER,securityManager);
        }
        if(securityPropertyManager==null){
            securityPropertyManager=new XMLSecurityPropertyManager();
            fConfiguration.setProperty(Constants.XML_SECURITY_PROPERTY_MANAGER,securityPropertyManager);
        }
        reset();
        fConfiguration.parse(inputSource);
    } // parse(XMLInputSource)
    //
    // Protected methods
    //

    protected void reset() throws XNIException{
    } // reset()
} // class XMLParser
