/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xni.grammars;

import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.io.IOException;
import java.util.Locale;

public interface XMLGrammarLoader{
    public String[] getRecognizedFeatures();

    public boolean getFeature(String featureId)
            throws XMLConfigurationException;

    public void setFeature(String featureId,
                           boolean state) throws XMLConfigurationException;

    public String[] getRecognizedProperties();

    public Object getProperty(String propertyId)
            throws XMLConfigurationException;

    public void setProperty(String propertyId,
                            Object state) throws XMLConfigurationException;

    public Locale getLocale();

    public void setLocale(Locale locale);

    public XMLErrorHandler getErrorHandler();

    public void setErrorHandler(XMLErrorHandler errorHandler);

    public XMLEntityResolver getEntityResolver();

    public void setEntityResolver(XMLEntityResolver entityResolver);

    public Grammar loadGrammar(XMLInputSource source)
            throws IOException, XNIException;
} // XMLGrammarLoader
