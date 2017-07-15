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
 * $Id: DTMNamedNodeMap.java,v 1.2.4.1 2005/09/15 08:15:03 suresh_emailid Exp $
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
 * $Id: DTMNamedNodeMap.java,v 1.2.4.1 2005/09/15 08:15:03 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.DTM;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DTMNamedNodeMap implements NamedNodeMap{
    DTM dtm;
    int element;
    short m_count=-1;

    public DTMNamedNodeMap(DTM dtm,int element){
        this.dtm=dtm;
        this.element=element;
    }

    public Node getNamedItem(String name){
        for(int n=dtm.getFirstAttribute(element);n!=DTM.NULL;
            n=dtm.getNextAttribute(n)){
            if(dtm.getNodeName(n).equals(name))
                return dtm.getNode(n);
        }
        return null;
    }

    public Node setNamedItem(Node newNode){
        throw new DTMException(DTMException.NO_MODIFICATION_ALLOWED_ERR);
    }

    public Node removeNamedItem(String name){
        throw new DTMException(DTMException.NO_MODIFICATION_ALLOWED_ERR);
    }

    public Node item(int i){
        int count=0;
        for(int n=dtm.getFirstAttribute(element);n!=-1;
            n=dtm.getNextAttribute(n)){
            if(count==i)
                return dtm.getNode(n);
            else
                ++count;
        }
        return null;
    }

    public int getLength(){
        if(m_count==-1){
            short count=0;
            for(int n=dtm.getFirstAttribute(element);n!=-1;
                n=dtm.getNextAttribute(n)){
                ++count;
            }
            m_count=count;
        }
        return (int)m_count;
    }

    public Node getNamedItemNS(String namespaceURI,String localName){
        Node retNode=null;
        for(int n=dtm.getFirstAttribute(element);n!=DTM.NULL;
            n=dtm.getNextAttribute(n)){
            if(localName.equals(dtm.getLocalName(n))){
                String nsURI=dtm.getNamespaceURI(n);
                if((namespaceURI==null&&nsURI==null)
                        ||(namespaceURI!=null&&namespaceURI.equals(nsURI))){
                    retNode=dtm.getNode(n);
                    break;
                }
            }
        }
        return retNode;
    }

    public Node setNamedItemNS(Node arg) throws DOMException{
        throw new DTMException(DTMException.NO_MODIFICATION_ALLOWED_ERR);
    }

    public Node removeNamedItemNS(String namespaceURI,String localName)
            throws DOMException{
        throw new DTMException(DTMException.NO_MODIFICATION_ALLOWED_ERR);
    }

    public class DTMException extends DOMException{
        static final long serialVersionUID=-8290238117162437678L;

        public DTMException(short code,String message){
            super(code,message);
        }

        public DTMException(short code){
            super(code,"");
        }
    }
}
