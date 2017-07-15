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

import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import org.xml.sax.ext.Locator2;

public class LocatorProxy implements Locator2{
    //
    // Data
    //
    private final XMLLocator fLocator;
    //
    // Constructors
    //

    public LocatorProxy(XMLLocator locator){
        fLocator=locator;
    }
    //
    // Locator methods
    //

    public String getPublicId(){
        return fLocator.getPublicId();
    }

    public String getSystemId(){
        return fLocator.getExpandedSystemId();
    }

    public int getLineNumber(){
        return fLocator.getLineNumber();
    }

    public int getColumnNumber(){
        return fLocator.getColumnNumber();
    }
    //
    // Locator2 methods
    //

    public String getXMLVersion(){
        return fLocator.getXMLVersion();
    }

    public String getEncoding(){
        return fLocator.getEncoding();
    }
}
