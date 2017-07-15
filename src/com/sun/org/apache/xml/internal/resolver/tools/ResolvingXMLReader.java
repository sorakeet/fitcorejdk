/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
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
// ResolvingXMLReader.java - An XMLReader that performs catalog resolution
/**
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
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
package com.sun.org.apache.xml.internal.resolver.tools;

import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ResolvingXMLReader extends ResolvingXMLFilter{
    public static boolean namespaceAware=true;
    public static boolean validating=false;

    public ResolvingXMLReader(){
        super();
        SAXParserFactory spf=catalogManager.useServicesMechanism()?
                SAXParserFactory.newInstance():new SAXParserFactoryImpl();
        spf.setNamespaceAware(namespaceAware);
        spf.setValidating(validating);
        try{
            SAXParser parser=spf.newSAXParser();
            setParent(parser.getXMLReader());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public ResolvingXMLReader(CatalogManager manager){
        super(manager);
        SAXParserFactory spf=catalogManager.useServicesMechanism()?
                SAXParserFactory.newInstance():new SAXParserFactoryImpl();
        spf.setNamespaceAware(namespaceAware);
        spf.setValidating(validating);
        try{
            SAXParser parser=spf.newSAXParser();
            setParent(parser.getXMLReader());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
