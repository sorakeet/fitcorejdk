/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: NamespaceMappings.java,v 1.2.4.1 2005/09/15 08:15:19 suresh_emailid Exp $
 */
/**
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: NamespaceMappings.java,v 1.2.4.1 2005/09/15 08:15:19 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class NamespaceMappings{
    private static final String EMPTYSTRING="";
    private static final String XML_PREFIX="xml"; // was "xmlns"
    private int count;
    private HashMap m_namespaces=new HashMap();
    private Stack m_nodeStack=new Stack();

    public NamespaceMappings(){
        initNamespaces();
    }

    private void initNamespaces(){
        // Define the default namespace (initially maps to "" uri)
        Stack stack;
        m_namespaces.put(EMPTYSTRING,stack=new Stack());
        stack.push(new MappingRecord(EMPTYSTRING,EMPTYSTRING,0));
        m_namespaces.put(XML_PREFIX,stack=new Stack());
        stack.push(new MappingRecord(XML_PREFIX,
                "http://www.w3.org/XML/1998/namespace",0));
        m_nodeStack.push(new MappingRecord(null,null,-1));
    }

    public String lookupPrefix(String uri){
        String foundPrefix=null;
        Iterator<String> itr=m_namespaces.keySet().iterator();
        while(itr.hasNext()){
            String prefix=itr.next();
            String uri2=lookupNamespace(prefix);
            if(uri2!=null&&uri2.equals(uri)){
                foundPrefix=prefix;
                break;
            }
        }
        return foundPrefix;
    }

    public String lookupNamespace(String prefix){
        final Stack stack=(Stack)m_namespaces.get(prefix);
        return stack!=null&&!stack.isEmpty()?
                ((MappingRecord)stack.peek()).m_uri:null;
    }

    MappingRecord getMappingFromURI(String uri){
        MappingRecord foundMap=null;
        Iterator<String> itr=m_namespaces.keySet().iterator();
        while(itr.hasNext()){
            String prefix=itr.next();
            MappingRecord map2=getMappingFromPrefix(prefix);
            if(map2!=null&&(map2.m_uri).equals(uri)){
                foundMap=map2;
                break;
            }
        }
        return foundMap;
    }

    MappingRecord getMappingFromPrefix(String prefix){
        final Stack stack=(Stack)m_namespaces.get(prefix);
        return stack!=null&&!stack.isEmpty()?
                ((MappingRecord)stack.peek()):null;
    }

    boolean pushNamespace(String prefix,String uri,int elemDepth){
        // Prefixes "xml" and "xmlns" cannot be redefined
        if(prefix.startsWith(XML_PREFIX)){
            return false;
        }
        Stack stack;
        // Get the stack that contains URIs for the specified prefix
        if((stack=(Stack)m_namespaces.get(prefix))==null){
            m_namespaces.put(prefix,stack=new Stack());
        }
        if(!stack.empty()&&uri.equals(((MappingRecord)stack.peek()).m_uri)){
            return false;
        }
        MappingRecord map=new MappingRecord(prefix,uri,elemDepth);
        stack.push(map);
        m_nodeStack.push(map);
        return true;
    }

    void popNamespaces(int elemDepth,ContentHandler saxHandler){
        while(true){
            if(m_nodeStack.isEmpty())
                return;
            MappingRecord map=(MappingRecord)(m_nodeStack.peek());
            int depth=map.m_declarationDepth;
            if(depth<elemDepth)
                return;
            /** the depth of the declared mapping is elemDepth or deeper
             * so get rid of it
             */
            map=(MappingRecord)m_nodeStack.pop();
            final String prefix=map.m_prefix;
            popNamespace(prefix);
            if(saxHandler!=null){
                try{
                    saxHandler.endPrefixMapping(prefix);
                }catch(SAXException e){
                    // not much we can do if they aren't willing to listen
                }
            }
        }
    }

    boolean popNamespace(String prefix){
        // Prefixes "xml" and "xmlns" cannot be redefined
        if(prefix.startsWith(XML_PREFIX)){
            return false;
        }
        Stack stack;
        if((stack=(Stack)m_namespaces.get(prefix))!=null){
            stack.pop();
            return true;
        }
        return false;
    }

    public String generateNextPrefix(){
        return "ns"+(count++);
    }

    public Object clone() throws CloneNotSupportedException{
        NamespaceMappings clone=new NamespaceMappings();
        clone.m_nodeStack=(Stack)m_nodeStack.clone();
        clone.m_namespaces=(HashMap)m_namespaces.clone();
        clone.count=count;
        return clone;
    }

    final void reset(){
        this.count=0;
        this.m_namespaces.clear();
        this.m_nodeStack.clear();
        initNamespaces();
    }

    class MappingRecord{
        final String m_prefix;  // the prefix
        final String m_uri;     // the uri
        // the depth of the element where declartion was made
        final int m_declarationDepth;

        MappingRecord(String prefix,String uri,int depth){
            m_prefix=prefix;
            m_uri=uri;
            m_declarationDepth=depth;
        }
    }
}
