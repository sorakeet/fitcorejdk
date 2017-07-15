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
 * $Id: XMLStringFactoryImpl.java,v 1.2.4.1 2005/09/10 17:44:29 jeffsuttor Exp $
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
 * $Id: XMLStringFactoryImpl.java,v 1.2.4.1 2005/09/10 17:44:29 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;

public class XMLStringFactoryImpl extends XMLStringFactory{
    private static XMLStringFactory m_xstringfactory=
            new XMLStringFactoryImpl();

    public static XMLStringFactory getFactory(){
        return m_xstringfactory;
    }

    public XMLString newstr(String string){
        return new XString(string);
    }

    public XMLString newstr(FastStringBuffer fsb,int start,int length){
        return new XStringForFSB(fsb,start,length);
    }

    public XMLString newstr(char[] string,int start,int length){
        return new XStringForChars(string,start,length);
    }

    public XMLString emptystr(){
        return XString.EMPTYSTRING;
    }
}
