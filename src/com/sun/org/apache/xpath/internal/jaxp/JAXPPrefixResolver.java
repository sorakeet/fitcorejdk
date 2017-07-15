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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// $Id: JAXPPrefixResolver.java,v 1.1.2.1 2005/08/01 01:30:18 jeffsuttor Exp $
package com.sun.org.apache.xpath.internal.jaxp;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;

public class JAXPPrefixResolver implements PrefixResolver{
    public static final String S_XMLNAMESPACEURI=
            "http://www.w3.org/XML/1998/namespace";
    private NamespaceContext namespaceContext;

    public JAXPPrefixResolver(NamespaceContext nsContext){
        this.namespaceContext=nsContext;
    }

    public String getNamespaceForPrefix(String prefix){
        return namespaceContext.getNamespaceURI(prefix);
    }

    public String getNamespaceForPrefix(String prefix,
                                        Node namespaceContext){
        Node parent=namespaceContext;
        String namespace=null;
        if(prefix.equals("xml")){
            namespace=S_XMLNAMESPACEURI;
        }else{
            int type;
            while((null!=parent)&&(null==namespace)
                    &&(((type=parent.getNodeType())==Node.ELEMENT_NODE)
                    ||(type==Node.ENTITY_REFERENCE_NODE))){
                if(type==Node.ELEMENT_NODE){
                    NamedNodeMap nnm=parent.getAttributes();
                    for(int i=0;i<nnm.getLength();i++){
                        Node attr=nnm.item(i);
                        String aname=attr.getNodeName();
                        boolean isPrefix=aname.startsWith("xmlns:");
                        if(isPrefix||aname.equals("xmlns")){
                            int index=aname.indexOf(':');
                            String p=isPrefix?aname.substring(index+1):"";
                            if(p.equals(prefix)){
                                namespace=attr.getNodeValue();
                                break;
                            }
                        }
                    }
                }
                parent=parent.getParentNode();
            }
        }
        return namespace;
    }

    public String getBaseIdentifier(){
        return null;
    }

    public boolean handlesNullPrefixes(){
        return false;
    }
}
