/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.org.apache.xml.internal.security.transforms.params;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.transforms.TransformParam;
import com.sun.org.apache.xml.internal.security.utils.ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class InclusiveNamespaces extends ElementProxy implements TransformParam{
    public static final String _TAG_EC_INCLUSIVENAMESPACES=
            "InclusiveNamespaces";
    public static final String _ATT_EC_PREFIXLIST="PrefixList";
    public static final String ExclusiveCanonicalizationNamespace=
            "http://www.w3.org/2001/10/xml-exc-c14n#";

    public InclusiveNamespaces(Document doc,String prefixList){
        this(doc,InclusiveNamespaces.prefixStr2Set(prefixList));
    }

    public InclusiveNamespaces(Document doc,Set<String> prefixes){
        super(doc);
        SortedSet<String> prefixList=null;
        if(prefixes instanceof SortedSet<?>){
            prefixList=(SortedSet<String>)prefixes;
        }else{
            prefixList=new TreeSet<String>(prefixes);
        }
        StringBuilder sb=new StringBuilder();
        for(String prefix : prefixList){
            if(prefix.equals("xmlns")){
                sb.append("#default ");
            }else{
                sb.append(prefix+" ");
            }
        }
        this.constructionElement.setAttributeNS(
                null,InclusiveNamespaces._ATT_EC_PREFIXLIST,sb.toString().trim());
    }

    public static SortedSet<String> prefixStr2Set(String inclusiveNamespaces){
        SortedSet<String> prefixes=new TreeSet<String>();
        if((inclusiveNamespaces==null)||(inclusiveNamespaces.length()==0)){
            return prefixes;
        }
        String[] tokens=inclusiveNamespaces.split("\\s");
        for(String prefix : tokens){
            if(prefix.equals("#default")){
                prefixes.add("xmlns");
            }else{
                prefixes.add(prefix);
            }
        }
        return prefixes;
    }

    public InclusiveNamespaces(Element element,String BaseURI)
            throws XMLSecurityException{
        super(element,BaseURI);
    }

    public String getInclusiveNamespaces(){
        return this.constructionElement.getAttributeNS(null,InclusiveNamespaces._ATT_EC_PREFIXLIST);
    }

    public String getBaseNamespace(){
        return InclusiveNamespaces.ExclusiveCanonicalizationNamespace;
    }

    public String getBaseLocalName(){
        return InclusiveNamespaces._TAG_EC_INCLUSIVENAMESPACES;
    }
}
