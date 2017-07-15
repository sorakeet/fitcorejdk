/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004 The Apache Software Foundation.
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
 * $Id: DOM2Helper.java,v 1.1.4.1 2005/09/08 11:03:09 suresh_emailid Exp $
 */
/**
 * Copyright 2004 The Apache Software Foundation.
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
 * $Id: DOM2Helper.java,v 1.1.4.1 2005/09/08 11:03:09 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer.utils;

import org.w3c.dom.Node;

public final class DOM2Helper{
    public DOM2Helper(){
    }

    public String getLocalNameOfNode(Node n){
        String name=n.getLocalName();
        return (null==name)?getLocalNameOfNodeFallback(n):name;
    }

    private String getLocalNameOfNodeFallback(Node n){
        String qname=n.getNodeName();
        int index=qname.indexOf(':');
        return (index<0)?qname:qname.substring(index+1);
    }

    public String getNamespaceOfNode(Node n){
        return n.getNamespaceURI();
    }
    /** Field m_useDOM2getNamespaceURI is a compile-time flag which
     *  gates some of the parser options used to build a DOM -- but
     * that code is commented out at this time and nobody else
     * references it, so I've commented this out as well. */
    //private boolean m_useDOM2getNamespaceURI = false;
}
