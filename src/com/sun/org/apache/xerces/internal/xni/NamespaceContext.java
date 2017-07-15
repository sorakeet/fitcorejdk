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
package com.sun.org.apache.xerces.internal.xni;

import java.util.Enumeration;

public interface NamespaceContext{
    //
    // Constants
    //
    public final static String XML_URI="http://www.w3.org/XML/1998/namespace".intern();
    public final static String XMLNS_URI="http://www.w3.org/2000/xmlns/".intern();
    //
    // NamespaceContext methods
    //

    public void pushContext();

    public void popContext();

    public boolean declarePrefix(String prefix,String uri);

    public String getURI(String prefix);

    public String getPrefix(String uri);

    public int getDeclaredPrefixCount();

    public String getDeclaredPrefixAt(int index);

    public Enumeration getAllPrefixes();

    public void reset();
} // interface NamespaceContext
