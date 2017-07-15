/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2005 The Apache Software Foundation.
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
 * $Id: XPathNSResolverImpl.java,v 1.2.4.1 2005/09/10 04:13:19 jeffsuttor Exp $
 */
/**
 * Copyright 2002-2005 The Apache Software Foundation.
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
 * $Id: XPathNSResolverImpl.java,v 1.2.4.1 2005/09/10 04:13:19 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.domapi;

import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathNSResolver;

class XPathNSResolverImpl extends PrefixResolverDefault implements XPathNSResolver{
    public XPathNSResolverImpl(Node xpathExpressionContext){
        super(xpathExpressionContext);
    }

    public String lookupNamespaceURI(String prefix){
        return super.getNamespaceForPrefix(prefix);
    }
}
