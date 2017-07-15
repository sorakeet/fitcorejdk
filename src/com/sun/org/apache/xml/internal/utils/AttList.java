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
 * $Id: AttList.java,v 1.2.4.1 2005/09/15 08:15:35 suresh_emailid Exp $
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
 * $Id: AttList.java,v 1.2.4.1 2005/09/15 08:15:35 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

public class AttList implements Attributes{
    NamedNodeMap m_attrs;
    int m_lastIndex;
    // ARGHH!!  JAXP Uses Xerces without setting the namespace processing to ON!
    // DOM2Helper m_dh = new DOM2Helper();
    DOMHelper m_dh;
//  /**
//   * Constructor AttList
//   *
//   *
//   * @param attrs List of attributes this will contain
//   */
//  public AttList(NamedNodeMap attrs)
//  {
//
//    m_attrs = attrs;
//    m_lastIndex = m_attrs.getLength() - 1;
//    m_dh = new DOM2Helper();
//  }

    public AttList(NamedNodeMap attrs,DOMHelper dh){
        m_attrs=attrs;
        m_lastIndex=m_attrs.getLength()-1;
        m_dh=dh;
    }

    public int getLength(){
        return m_attrs.getLength();
    }

    public String getURI(int index){
        String ns=m_dh.getNamespaceOfNode(((Attr)m_attrs.item(index)));
        if(null==ns)
            ns="";
        return ns;
    }

    public String getLocalName(int index){
        return m_dh.getLocalNameOfNode(((Attr)m_attrs.item(index)));
    }

    public String getQName(int i){
        return ((Attr)m_attrs.item(i)).getName();
    }

    public String getType(int i){
        return "CDATA";  // for the moment
    }

    public String getValue(int i){
        return ((Attr)m_attrs.item(i)).getValue();
    }

    public int getIndex(String uri,String localPart){
        for(int i=m_attrs.getLength()-1;i>=0;--i){
            Node a=m_attrs.item(i);
            String u=a.getNamespaceURI();
            if((u==null?uri==null:u.equals(uri))
                    &&
                    a.getLocalName().equals(localPart))
                return i;
        }
        return -1;
    }

    public int getIndex(String qName){
        for(int i=m_attrs.getLength()-1;i>=0;--i){
            Node a=m_attrs.item(i);
            if(a.getNodeName().equals(qName))
                return i;
        }
        return -1;
    }

    public String getType(String uri,String localName){
        return "CDATA";  // for the moment
    }

    public String getType(String name){
        return "CDATA";  // for the moment
    }

    public String getValue(String uri,String localName){
        Node a=m_attrs.getNamedItemNS(uri,localName);
        return (a==null)?null:a.getNodeValue();
    }

    public String getValue(String name){
        Attr attr=((Attr)m_attrs.getNamedItem(name));
        return (null!=attr)
                ?attr.getValue():null;
    }
}
